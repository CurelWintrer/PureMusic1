package com.wintercruel.puremusic1;

import static com.wintercruel.puremusic1.MainActivity.mediaProjection;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.C;
import androidx.media3.common.Player;
import androidx.media3.common.Timeline;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.exoplayer.audio.DefaultAudioSink;
import androidx.media3.ui.PlayerControlView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.wintercruel.puremusic1.animator.ClickAnimator;

import com.wintercruel.puremusic1.audio.AudioVisualizerView;
import com.wintercruel.puremusic1.audio.CustomAudioSink;
import com.wintercruel.puremusic1.entity.MusicHolder;
import com.wintercruel.puremusic1.event_bus.MusicPlayUpdateUI;
import com.wintercruel.puremusic1.service.MyMusicService;
import com.wintercruel.puremusic1.tools.BrightnessTransformation;
import com.wintercruel.puremusic1.tools.LyricsManager;
import com.wintercruel.puremusic1.tools.TransparentBar;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import jp.wasabeef.glide.transformations.BlurTransformation;
import me.zhengken.lyricview.LyricView;



@UnstableApi
public class MusicPlay extends AppCompatActivity {

    private PlayerControlView playerControlView;
    private ExoPlayer exoPlayer;

    private MyMusicService musicService;

    private ImageButton prev;
    private ImageButton play;
    private ImageButton next;

    private LyricView lyricView;

    private TextView musicName;
    private TextView artistName;
    private ImageView musicPlayBackground;
    private ImageView musicAlbum;

    private TextView playedTime;
    private TextView totalTime;

    private Handler handler;
    private Runnable updateRunnable;

    private ImageButton Like;
    private ImageButton PlayModel;

    public static AudioVisualizerView audioVisualizerView;




    @Override
    protected void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onEvent(MusicPlayUpdateUI event) {
        // 处理接收到的粘性事件
        UpdateView();
        if(exoPlayer.isPlaying()){
            play.setImageResource(R.drawable.icon_play);
        }else {
            play.setImageResource(R.drawable.icon_pause);
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_music_play);


        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/方正粗黑宋简体.ttf")
                                .setFontAttrId(com.chibde.R.attr.font)
                                .build()))
                .build());


        TransparentBar.transparentNavBar(this);
        TransparentBar.transparentStatusBar(this);
        musicService=MainActivity.GetBindService();
        exoPlayer=musicService.getPlayer();
        playerControlView=findViewById(R.id.PlayerControlView);
        playerControlView.setPlayer(exoPlayer);
        playerControlView.show();
        PlayerListener();
        InitView();
        PlayerControl();
        UpdateView();

        LyricsManager lyricsManager=new LyricsManager(exoPlayer,lyricView);
        lyricsManager.startScrollingLyrics();

        handler=new Handler(Looper.getMainLooper());
        updateRunnable=new Runnable() {
            @Override
            public void run() {
                updateProgress();
//                if(exoPlayer.getCurrentPosition()==exoPlayer.getDuration()){   //播放完毕，播放下一曲
//                    musicService.Next();
//                }
                handler.postDelayed(this,1000);
            }
        };

        startUpdatingProgress();


    }





    private void startUpdatingProgress() {
        // 你可以在这里设置一个定时任务来定期调用 updateProgress() 方法

        handler.post(updateRunnable);
    }
    // 停止更新进度
    private void stopUpdatingProgress() {
        handler.removeCallbacks(updateRunnable);
    }

    private void PlayerListener(){
        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onIsPlayingChanged(boolean isPlaying){
                if(!exoPlayer.isPlaying()){
                    play.setImageResource(R.drawable.icon_pause);
                }else {
                    play.setImageResource(R.drawable.icon_play);
                }
            }

        });
    }

    //背景的效果
    MultiTransformation<Bitmap> multiTransformation = new MultiTransformation<>(
            new BlurTransformation(90, 5)
//            new BrightnessTransformation(1.3f)

    );

    private void UpdateView(){
        if(MusicHolder.getMusicName()!=null){
            musicName.setText(MusicHolder.getMusicName());
            musicName.setSelected(true);
        }
        if(MusicHolder.getArtistName()!=null){
            artistName.setText(MusicHolder.getArtistName());
            artistName.setSelected(true);
        }


        Glide.with(this)
                .load(MusicHolder.getAlbumArtUrl())
                .placeholder(musicPlayBackground.getDrawable())
                .transition(DrawableTransitionOptions.withCrossFade(600))
                .transform(multiTransformation)
                .into(musicPlayBackground);

        Glide.with(this)
                .load(MusicHolder.getAlbumArtUrl())
                .placeholder(musicPlayBackground.getDrawable())
                .transition(DrawableTransitionOptions.withCrossFade(600))
                .into(musicAlbum);

        LoadLyrics(this);
        audioVisualizerView.setGradientColors(MusicHolder.getAlbumColor1(),MusicHolder.getAlbumColor2());
        switch (MusicHolder.getPlayingMode()){
            case 0:
                PlayModel.setImageResource(R.drawable.order_play);
                break;

            case 1:
                PlayModel.setImageResource(R.drawable.loop_play);
                break;

            case  2:
                PlayModel.setImageResource(R.drawable.random_play);
                break;

        }


    }


    //初始化控件
    private void InitView(){
        prev=findViewById(R.id.exo_prev);
        play=findViewById(R.id.exo_play);
        next=findViewById(R.id.exo_next);
        playedTime=findViewById(R.id.tv_played_time);
        totalTime=findViewById(R.id.tv_total_time);
        musicName=findViewById(R.id.MusicNamePlay);
        artistName=findViewById(R.id.MusicArtistNamePlay);
        musicPlayBackground=findViewById(R.id.MusicPlayBackground);
        musicAlbum=findViewById(R.id.MusicPlayAlbum);
        lyricView=findViewById(R.id.LyricsView);

        audioVisualizerView=findViewById(R.id.audioVisualizerView);

        CustomAudioSink.bindVisualizerView(audioVisualizerView);
        audioVisualizerView.setGradientColors(MusicHolder.getAlbumColor1(),MusicHolder.getAlbumColor2());

        Like=findViewById(R.id.Like);
        PlayModel=findViewById(R.id.PlayModel);

//        svWave=findViewById(R.id.SurfaceView);

//        waveformView=findViewById(R.id.Wave);

    }

    public AudioVisualizerView GetVisualView(){
        return audioVisualizerView;
    }

    private void LoadLyrics(Context context){
        // 获取应用的私有存储目录
        File directory = context.getFilesDir();  // 应用的私有目录
        File lrcFile = new File(directory, MusicHolder.getMusicName()+".lrc");
        lyricView.setLyricFile(lrcFile);
    }


    // 更新播放进度
    public void updateProgress() {
        if (exoPlayer!= null) {
            long currentPosition = exoPlayer.getCurrentPosition();
            long duration = exoPlayer.getDuration();

            // 如果播放器还未准备好，duration 可能会返回 C.TIME_UNSET
            if (duration == C.TIME_UNSET) {
                duration = 0;
            }

            updateTimes(currentPosition, duration);
        }
    }

    private void updateTimes(long currentPosition, long duration){
        playedTime.setText(formatTime(currentPosition));
        totalTime.setText(formatTime(duration));

    }

    private String formatTime(long timeMs) {
        int totalSeconds = (int) (timeMs / 1000);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d", minutes, seconds);
    }


    private void PlayerControl(){
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.Prev();
                ClickAnimator.ButtonClickAnimator(v);
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicService.Next();
                ClickAnimator.ButtonClickAnimator(v);
            }
        });
        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(exoPlayer.isPlaying()){
                    play.setImageResource(R.drawable.icon_pause);
                    exoPlayer.pause();
                }else {
                    play.setImageResource(R.drawable.icon_play);
                    exoPlayer.play();
                }
                ClickAnimator.ButtonClickAnimator(v);

            }
        });

        PlayModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (MusicHolder.getPlayingMode()){
                    case 0:
                        PlayModel.setImageResource(R.drawable.loop_play);
                        MusicHolder.setPlayingMode(1);
                        Toast.makeText(getApplicationContext(),"单曲循环",Toast.LENGTH_SHORT).show();
                        break;

                    case 1:
                        PlayModel.setImageResource(R.drawable.random_play);
                        MusicHolder.setPlayingMode(2);
                        Toast.makeText(getApplicationContext(),"随机播放",Toast.LENGTH_SHORT).show();
                        break;

                    case  2:
                        PlayModel.setImageResource(R.drawable.order_play);
                        MusicHolder.setPlayingMode(0);
                        Toast.makeText(getApplicationContext(),"顺序播放",Toast.LENGTH_SHORT).show();
                        break;

                }


                ClickAnimator.ButtonClickAnimator(v);

            }
        });

    }



    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0,R.anim.slide_out_bottom);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        stopUpdatingProgress();
    }


    //不重写的Activity还是安卓默认字体
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }




}