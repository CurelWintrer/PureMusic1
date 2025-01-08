package com.wintercruel.puremusic1.service;

import static com.wintercruel.puremusic1.MusicPlay.audioVisualizerView;
import static com.wintercruel.puremusic1.cloud.Net.GetLyrics;
import static com.wintercruel.puremusic1.cloud.Net.REAL_IP;
import static com.wintercruel.puremusic1.cloud.Net.client;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.browse.MediaBrowser;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.ResultReceiver;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.DefaultRenderersFactory;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;


import com.wintercruel.puremusic1.R;
import com.wintercruel.puremusic1.audio.CustomRenderersFactory;
import com.wintercruel.puremusic1.cloud.Net;
import com.wintercruel.puremusic1.cloud.server;
import com.wintercruel.puremusic1.database.MusicDatabase;
import com.wintercruel.puremusic1.entity.MusicHolder;
import com.wintercruel.puremusic1.event_bus.MusicPlayUpdateUI;
import com.wintercruel.puremusic1.items.MusicItem;
import com.wintercruel.puremusic1.tools.LyricsFileUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MyMusicService extends Service {

    private static ExoPlayer player;
    private ArrayList<MediaItem> mediaItems;
    private HashSet<Integer>playedIndices;
    private Random random=new Random();
    private Handler handler=new Handler();

    private final IBinder binder = new LocalBinder();

    private MusicDatabase musicDatabase;

    private MediaSessionCompat mediaSessionCompat;

    private int PlayModel=0;   //播放模式: 0顺序列表循环播放， 1单曲循环， 2随机播放

    private Runnable updatePositionRunnable = new Runnable() {
        @Override
        public void run() {
            if (player != null) {
                if(player.getCurrentPosition()==player.getDuration()){
//                    Log.d("current", String.valueOf(player.getCurrentPosition()));
//                    Log.d("duration", String.valueOf(player.getDuration()));
                    Next();
                }
                handler.postDelayed(this, 1000); // 每秒更新
            }
        }
    };


    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onCreate(){
        super.onCreate(); // 不要忘记调用父类的方法
        //player=new ExoPlayer.Builder(this).build();

        // 创建 CustomRenderersFactory 并传递 visualizerView
        CustomRenderersFactory renderersFactory = new CustomRenderersFactory(this);
        renderersFactory.setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON);

        // 初始化 ExoPlayer
        player = new ExoPlayer.Builder(this, renderersFactory).build();


        mediaItems = new ArrayList<>();

        random = new Random();

        mediaSessionCompat = new MediaSessionCompat(this, "MusicService");



        mediaSessionCompat.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                player.play();

                updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
            }

            @Override
            public void onPause() {
                super.onPause();
//                player.pause(); // 调用 ExoPlayer 的暂停方法
                Pause();

                updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
            }

            @Override
            public void onStop() {
                super.onStop();
                player.stop(); // 停止播放


                updatePlaybackState(PlaybackStateCompat.STATE_STOPPED);
            }

            @Override
            public void onSkipToNext(){
                super.onSkipToNext();
                Next();

                updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_NEXT);
            }

            @Override
            public void onSkipToPrevious(){
                super.onSkipToPrevious();
                Prev();

                updatePlaybackState(PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS);
            }
            @Override
            public void onSeekTo(long pos) {
                super.onSeekTo(pos);
                if (player != null) {
                    player.seekTo(pos);  // 将播放器跳转到拖动的目标位置
                }
            }
        });


        // 启用 MediaSession 以接收控制命令
        // 允许媒体按钮控制
        mediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSessionCompat.setActive(true);



        Log.d("音乐服务", "已经onCreate");

        player.addListener(new Player.Listener() {
            @Override
            public void onEvents(Player player, Player.Events events) {
                Player.Listener.super.onEvents(player, events);

            }

            @Override
            public void onMediaMetadataChanged(MediaMetadata mediaMetadata) {
                Player.Listener.super.onMediaMetadataChanged(mediaMetadata);

            }

            @Override
            public void onTimelineChanged(Timeline timeline, int reason){
            }

            @Override
            public void onPlaylistMetadataChanged(MediaMetadata mediaMetadata) {
                Player.Listener.super.onPlaylistMetadataChanged(mediaMetadata);
            }

            @Override
            public void onPlaybackStateChanged(int playbackState) {
                Player.Listener.super.onPlaybackStateChanged(playbackState);
                switch (playbackState) {
                    case Player.STATE_BUFFERING:
                        Log.d("ExoPlayer", "Player is buffering");
                        updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING);

                        break;

                    case Player.STATE_READY:
                        if (player.getPlayWhenReady()) {
                            updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                            Log.d("ExoPlayer", "Player is ready");
                            //加载音乐元数据
                            LoadMusicMetadata();
                        } else {
                            updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
                        }
                        Log.d("ExoPlayer", "Player is ready to play");
                        EventBus.getDefault().postSticky(new MusicPlayUpdateUI());
                        break;

                    case Player.STATE_ENDED:
                        Log.d("ExoPlayer状态", "Player is ended");

                        updatePlaybackState(PlaybackStateCompat.STATE_STOPPED);

                        break;

                    default:

                        Log.d("ExoPlayer", "Unknown playback state: " + playbackState);
                        break;
                }
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
                Player.Listener.super.onPlayWhenReadyChanged(playWhenReady, reason);
                if (playWhenReady) {
                    updatePlaybackState(PlaybackStateCompat.STATE_PLAYING);
                } else {
                    updatePlaybackState(PlaybackStateCompat.STATE_PAUSED);
                }
            }
            @Override
            public void onPositionDiscontinuity(Player.PositionInfo oldPosition, Player.PositionInfo newPosition, int reason) {
                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION && !player.hasNextMediaItem()) {
                    Log.d("ExoPlayer", "Manually setting playback to ended.");
                    // 自定义逻辑模拟 STATE_ENDED
//                    Next();
                }

            }

        });

    }

    private void updatePlaybackState(int state) {
        long position = player.getCurrentPosition();
        long bufferedPosition = player.getBufferedPosition();
        float playbackSpeed = player.getPlaybackParameters().speed;

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setState(state, position, playbackSpeed)
                .setBufferedPosition(bufferedPosition)
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PAUSE |
                                PlaybackStateCompat.ACTION_STOP |
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                                PlaybackStateCompat.ACTION_SEEK_TO // 确保启用拖动
                );

        // 如果播放器支持获取总时长，进一步设置持续时间
        stateBuilder.setExtras(Bundle.EMPTY);  // 确保 Extras 存在

        mediaSessionCompat.setPlaybackState(stateBuilder.build());
    }

    public class LocalBinder extends Binder {
        public MyMusicService getService() {
            return MyMusicService.this;
        }
    }



    public void Next(){

        switch (MusicHolder.getPlayingMode()){
            case 0:
                //列表顺序播放
                LoadMusic(MusicHolder.getPosition()+1);
                MusicHolder.setPosition(MusicHolder.getPosition()+1);
                break;

            case 1:
                //单曲循环播放
                LoadMusic(MusicHolder.getPosition());
                break;

            case  2:
                //随机播放
                int position=random.nextInt(musicDatabase.getMusicTotalCount(MusicHolder.getPlayListId())+1);
                LoadMusic(position);
                MusicHolder.setPosition(position);
                break;

        }


    }

    public void Prev(){

        switch (MusicHolder.getPlayingMode()){
            case 0:
                //列表顺序播放
                LoadMusic(MusicHolder.getPosition()-1);
                MusicHolder.setPosition(MusicHolder.getPosition()-1);
                break;

            case 1:
                //单曲循环播放
                LoadMusic(MusicHolder.getPosition());
                break;

            case  2:
                //随机播放
                int position=random.nextInt(musicDatabase.getMusicTotalCount(MusicHolder.getPlayListId())+1);
                LoadMusic(position);
                MusicHolder.setPosition(position);
                break;

        }


    }

    public void Pause(){
        if(player.isPlaying()){
            player.pause();

        }else {
            player.play();

        }

    }

    public void LoadMusic(int position) {
        handler.removeCallbacks(updatePositionRunnable);
        musicDatabase=new MusicDatabase(getApplicationContext());
        if (musicDatabase == null) {
            Log.e("MyMusicService", "LoadMusic called but musicDatabase is null");
            return; // 早期返回，避免 NullPointerException
        }

        Cursor cursor = musicDatabase.getPlaylistItem(MusicHolder.getPlayListId(), position);
        if (cursor == null) {
            Log.e("MyMusicService", "Cursor is null. No data found for the given position.");
            return; // 确保游标不为 null
        }

        // 处理游标数据
        if (cursor.moveToFirst()) {
            @SuppressLint("Range") String id = cursor.getString(cursor.getColumnIndex("id"));
            MusicHolder.setMusicId(id);
            @SuppressLint("Range") String title = cursor.getString(cursor.getColumnIndex("title"));
            MusicHolder.setMusicName(title);
            @SuppressLint("Range") String artist = cursor.getString(cursor.getColumnIndex("artist"));
            MusicHolder.setArtistName(artist);
            @SuppressLint("Range") String albumUrl = cursor.getString(cursor.getColumnIndex("albumUrl"));
            MusicHolder.setAlbumArtUrl(albumUrl);

            Log.e("得到的音乐id", id);
            GetMusic(id);
            GetLyrics(getApplicationContext(), id, new Net.LyricsCallback() {
                @Override
                public void onSuccess(String lyrics) {
                    MusicHolder.setLyrics(lyrics);
                    SaveLyrics(getApplicationContext(),lyrics);
                }

                @Override
                public void onFailure(Exception e) {
//                    Toast.makeText(getApplicationContext(), "歌词解析失败", Toast.LENGTH_LONG).show();
                }
            });


        }
        cursor.close(); // 记得关闭游标
    }


    //保存歌词文件
    private void SaveLyrics(Context context, String lyrics){
        if(lyrics !=null&&!lyrics.isEmpty()){
            String fileName=MusicHolder.getMusicName();
            File lrcFile= LyricsFileUtils.saveLyricsToLrcFile(context,lyrics,fileName);
//            lyricFile=lrcFile;
            if (lrcFile != null) {
                // 文件创建成功
                System.out.println("歌词文件路径: " + lrcFile.getAbsolutePath());
            } else {
                System.out.println("歌词文件保存失败");
            }
        }else {
            System.out.println("歌词内容为空");
        }
    }

//    private void GetMusic(String id){
//        Request request = new Request.Builder()
//                .url(server.ADDRESS + "/song/url?id="+id+"&realIP="+REAL_IP) // 替换为你的基础 URL
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                new Handler(Looper.getMainLooper()).post(() ->
//                        Toast.makeText(getApplicationContext(), "音乐加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
//                );
//                Log.e("请求播放地址", "请求失败", e);
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                try (ResponseBody responseBody = response.body()) {
//                    if (response.isSuccessful() && responseBody != null) {
//                        String responseData = responseBody.string();
//                        Log.e("得到的音乐信息", responseData);
//                        parseMusicUrls(responseData);
//                    } else {
//                        Log.e("GetMusic", "Unexpected response code: " + response.code());
//                    }
//                }
//            }
//
//        });
//    }

    private void GetMusic(String id) {
        String cacheFilePath = getCacheFilePath(id); // 获取缓存路径
        File cacheFile = new File(cacheFilePath);

        // 如果缓存文件存在，直接播放
        if (cacheFile.exists()) {
            Log.d("音乐缓存", "从本地缓存播放");
            MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(cacheFile));
            player.setMediaItem(mediaItem, 0);
            player.prepare();
            player.play();
            handler.post(updatePositionRunnable);
        } else {
            // 否则从服务器请求并缓存文件
            Request request = new Request.Builder()
                    .url(server.ADDRESS + "/song/url?id=" + id + "&realIP=" + REAL_IP) // 替换为你的基础 URL
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(getApplicationContext(), "音乐加载失败: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                    Log.e("请求播放地址", "请求失败", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        if (response.isSuccessful() && responseBody != null) {
                            String responseData = responseBody.string();
                            Log.e("得到的音乐信息", responseData);
                            parseMusicUrls(responseData, id);
                        } else {
                            Log.e("GetMusic", "Unexpected response code: " + response.code());
                        }
                    }
                }
            });
        }
    }

    // 解析音乐流地址并缓存到本地
    @OptIn(markerClass = UnstableApi.class)
    private void parseMusicUrls(String responseData, String id) {
        try {
            JSONObject jsonResponse = new JSONObject(responseData);
            JSONArray songsArray = jsonResponse.getJSONArray("data");

            JSONObject song = songsArray.getJSONObject(0);
            String url = song.getString("url");

            // 创建缓存文件路径
            String cacheFilePath = getCacheFilePath(id);
            File cacheFile = new File(cacheFilePath);

            // 如果缓存文件不存在，则下载并保存
            if (!cacheFile.exists()) {
                downloadAndCacheMusic(url, cacheFile);
            }

            MediaItem mediaItem=MediaItem.fromUri(url);
            // 在 UI 线程中添加媒体项
            new Handler(Looper.getMainLooper()).post(() -> {

                player.setMediaItem(mediaItem,0);
                player.prepare();
                player.play();

                handler.post(updatePositionRunnable);

                Log.d("音乐ID数据：", String.valueOf(player.getAudioSessionId()));

            });

            // 播放缓存的音乐文件
//            MediaItem mediaItem = MediaItem.fromUri(Uri.fromFile(cacheFile));
//            new Handler(Looper.getMainLooper()).post(() -> {
//                player.setMediaItem(mediaItem, 0);
//                player.prepare();
//                player.play();
//                handler.post(updatePositionRunnable);
//            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // 下载并缓存音乐文件
    private void downloadAndCacheMusic(String url, File cacheFile) {
        new Thread(() -> {
            try (InputStream inputStream = new URL(url).openStream();
                 FileOutputStream fileOutputStream = new FileOutputStream(cacheFile)) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, bytesRead);
                }

                Log.d("音乐缓存", "音乐文件已缓存到本地：" + cacheFile.getAbsolutePath());

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("音乐缓存", "下载音乐文件失败", e);
            }
        }).start();
    }

    // 获取缓存文件路径
    private String getCacheFilePath(String id) {
        File cacheDir = new File(getApplicationContext().getCacheDir(), "music_cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdir();
        }
        return new File(cacheDir, id + ".mp3").getAbsolutePath();
    }


    // 解析音乐流地址
//    @OptIn(markerClass = UnstableApi.class)
//    private void parseMusicUrls(String responseData) {
//        try {
//            // 解析响应 JSON
//            JSONObject jsonResponse = new JSONObject(responseData);
//            JSONArray songsArray = jsonResponse.getJSONArray("data"); // 假设返回的数据在 "data" 字段中
//
//            System.out.println("音乐数据："+songsArray);
//
//            JSONObject song = songsArray.getJSONObject(0);
//            String url = song.getString("url"); // 根据实际 JSON 结构修改
//            System.out.println(url);
//            String id = song.getString("id"); // 如果需要 ID，可以提取
//
//            MediaItem mediaItem=MediaItem.fromUri(url);
//            // 在 UI 线程中添加媒体项
//            new Handler(Looper.getMainLooper()).post(() -> {
//
//                player.setMediaItem(mediaItem,0);
//                player.prepare();
//                player.play();
//
//                handler.post(updatePositionRunnable);
//
//                Log.d("音乐ID数据：", String.valueOf(player.getAudioSessionId()));
//
////                EventBus.getDefault().postSticky(new MusicPlayUpdateUI());
//            });
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    public ExoPlayer getPlayer(){
        return player;
    }

    //加载音乐元数据
    private void LoadMusicMetadata(){
        Glide.with(getApplicationContext())
                .asBitmap()
                .load(MusicHolder.getAlbumArtUrl())
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap bitmap, @Nullable Transition<? super Bitmap> transition) {
                        // 当图片加载完成后更新 metadata
                        MediaMetadataCompat mediaMetadataCompat = new MediaMetadataCompat.Builder()
                                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, MusicHolder.getMusicName())
                                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, MusicHolder.getArtistName())
                                .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, bitmap)
                                .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, player.getDuration())
                                .build();
                        mediaSessionCompat.setMetadata(mediaMetadataCompat);
                        extractColorsFromBitmap(bitmap);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {}
                });


    }

//    private void extractColorsFromBitmap(Bitmap bitmap) {
//        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
//            @Override
//            public void onGenerated(@Nullable Palette palette) {
//                if (palette != null) {
//                    // 提取主要颜色
//                    int dominantColor = palette.getDominantColor(Color.BLACK); // 主色
//                    int vibrantColor = palette.getVibrantColor(Color.BLACK);   // 鲜艳色
//
//                    // 转换为十六进制
//
//
//                    MusicHolder.setAlbumColor1(String.format("#%06X", (0xFFFFFF & dominantColor)));
//                    MusicHolder.setAlbumColor2(String.format("#%06X", (0xFFFFFF & vibrantColor)));
//
//                } else {
//                    Log.e("ColorExtractor", "Palette generation failed.");
//                }
//            }
//        });
//    }

    private void extractColorsFromBitmap(Bitmap bitmap) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@Nullable Palette palette) {
                if (palette != null) {
                    // 提取主要颜色
                    int dominantColor = palette.getDominantColor(Color.WHITE); // 主色
                    int vibrantColor = palette.getMutedColor(0x000000); // 柔和色

                    // 提升颜色亮度
                    int brighterDominantColor = increaseBrightness(dominantColor, 1.2f);
                    int brighterVibrantColor = increaseBrightness(vibrantColor, 1.2f);

                    // 增加 60% 透明度 (alpha = 0x99, 即 153)
                    int dominantColorWithTransparency = addTransparency(brighterDominantColor, 0xCC);
                    int vibrantColorWithTransparency = addTransparency(brighterVibrantColor, 0xCC);

                    // 转换为十六进制
                    MusicHolder.setAlbumColor1(String.format("#%08X", (0xFFFFFFFF & dominantColorWithTransparency)));
                    MusicHolder.setAlbumColor2(String.format("#%08X", (0xFFFFFFFF & vibrantColorWithTransparency)));

                    EventBus.getDefault().postSticky(new MusicPlayUpdateUI());

                } else {
                    Log.e("ColorExtractor", "Palette generation failed.");
                }
            }
        });
    }

    // 添加透明度的方法
    private int addTransparency(int color, int alpha) {
        // 保持颜色的 RGB 部分，修改 alpha 部分
        return (alpha << 24) | (color & 0x00FFFFFF);
    }


    /**
     * 提升颜色的亮度。
     *
     * @param color  原始颜色（ARGB格式）
     * @param factor 亮度提升因子，>1.0 为增加亮度，范围建议在 1.0f~2.0f 之间
     * @return 提升亮度后的颜色
     */
    private int increaseBrightness(int color, float factor) {
        // 提取颜色的 RGB 分量
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);

        // 调整亮度（Value）
        hsv[2] = Math.min(hsv[2] * factor, 1.0f); // 确保亮度不超过最大值 1.0f

        // 转换回颜色
        return Color.HSVToColor(hsv);
    }



    public MyMusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        mediaSessionCompat.release(); // 释放 MediaSession
        super.onDestroy();
    }
}