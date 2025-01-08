package com.wintercruel.puremusic1;

import static com.wintercruel.puremusic1.cloud.Net.GetPlayList;

import static com.wintercruel.puremusic1.cloud.Net.initialize;
import static com.wintercruel.puremusic1.cloud.Net.logout;



import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

import android.graphics.Color;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;

import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;

import com.wintercruel.puremusic1.animator.ClickAnimator;
import com.wintercruel.puremusic1.audio.AudioSpectrumView;
import com.wintercruel.puremusic1.cloud.Net;
import com.wintercruel.puremusic1.entity.Lyric;
import com.wintercruel.puremusic1.entity.MusicHolder;
import com.wintercruel.puremusic1.event_bus.MusicPlayUpdateUI;
import com.wintercruel.puremusic1.service.MyMusicService;
import com.wintercruel.puremusic1.tools.TransparentBar;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Stack;


public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private static final Fragment fragment_index=new Index();
    private static final Fragment fragment_find=new Find();
    private static final Fragment fragment_music=new Music();
    private static final Fragment fragment_search_music=new SearchMusic();
    private static final Fragment fragment_artist=new Aritist();
    private static FragmentManager fragmentManager;

    private static MyMusicService musicService;
    private boolean isServiceBound = false;
    private ExoPlayer player;

    public static ImageButton sideBar;
    public static ImageButton search;

    private ImageButton MusicPlay;
    private TextView musicName;
    private TextView artistName;
    private ImageButton play;
    private static CardView IndexCard;

    private AudioSpectrumView audioSpectrumView;

    public static boolean isVisible = true;

    public static final int REQUEST_MEDIA_PROJECTION = 1001;
    public static MediaProjectionManager mediaProjectionManager = null;
    public static MediaProjection mediaProjection = null;


    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable lyricsUpdater;
    private boolean isUpdating = false;

    private static final Stack<Integer> pageStack = new Stack<>();



    private ServiceConnection serviceConnection=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            MyMusicService.LocalBinder binder = (MyMusicService.LocalBinder) service;
            musicService = binder.getService();
            isServiceBound = true;
            player=musicService.getPlayer();
            player.addListener(new Player.Listener() {
                @Override
                public void onIsPlayingChanged(boolean isPlaying) {
                    Player.Listener.super.onIsPlayingChanged(isPlaying);
                    if(!player.isPlaying()){
                        play.setImageResource(R.drawable.icon_pause);
                        pauseLyrics();
                    }else {
                        play.setImageResource(R.drawable.icon_play);
                        resumeLyrics();
//                        updateLyrics(artistName,parseLyrics(MusicHolder.getLyrics()));

                    }
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };


    public static MyMusicService GetBindService(){
        return musicService;
    }



    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        MusicHolder.initSharedP(this);

        TransparentBar.transparentNavBar(this);
        TransparentBar.transparentStatusBar(this);

        musicName = findViewById(R.id.MusicNameIndex);
        artistName = findViewById(R.id.MusicArtistNameIndex);
        play = findViewById(R.id.PlayIndex);
        IndexCard = findViewById(R.id.IndexCard);
        MusicPlay = findViewById(R.id.MusicImageIndex);

        IndexCard = findViewById(R.id.IndexCard);

        audioSpectrumView=findViewById(R.id.AudioLoading);


        initialize(this);


        //绑定音乐服务
        Intent intent1 = new Intent(this, MyMusicService.class);
        startService(intent1); // 确保服务在绑定前启动
        bindService(intent1, serviceConnection, Context.BIND_AUTO_CREATE);


        drawerLayout = findViewById(R.id.main);
        sideBar = findViewById(R.id.SideBar);
        search = findViewById(R.id.SearchMusic);
        fragmentManager = getSupportFragmentManager();
        SideBarClick();
        SelectPage(1);
        UpdateUi();
        OnClick();

        new Thread(()->{
            if(!MusicHolder.isVisitLogin()){
                Net.visitorLogin(this);
            }

        }).start();



    }


    private void syncLyricsWithExoPlayer(TextView textView, ExoPlayer exoPlayer, List<Lyric> lyricsList) {
        if (lyricsUpdater != null) {
            handler.removeCallbacks(lyricsUpdater);
        }

        lyricsUpdater = new Runnable() {
            int currentLyricIndex = 0;

            @Override
            public void run() {
                if (!isUpdating || lyricsList.isEmpty() || currentLyricIndex >= lyricsList.size()) return;

                long currentPosition = exoPlayer.getCurrentPosition(); // 获取当前播放时间
                Lyric currentLyric = lyricsList.get(currentLyricIndex);

                // 检查是否需要更新歌词
                if (currentPosition >= currentLyric.time) {
                    String lyricText = currentLyric.text;

                    // 设置渐隐动画
                    textView.animate()
                            .alpha(0f) // 透明度变为 0（渐隐）
                            .setDuration(300) // 动画时长 300ms
                            .withEndAction(() -> { // 动画结束后更新歌词并设置渐显动画
                                textView.setText(lyricText);
                                textView.animate()
                                        .alpha(1f) // 透明度变为 1（渐显）
                                        .setDuration(300) // 动画时长 300ms
                                        .start();
                            }).start();

                    currentLyricIndex++; // 移动到下一句歌词
                }

                // 如果还没有到最后一句歌词，则继续定时更新
                if (currentLyricIndex < lyricsList.size()) {
                    handler.postDelayed(this, 100); // 每 100ms 检查一次
                }
            }
        };

        isUpdating = true;
        handler.post(lyricsUpdater);
    }


    public void pauseLyrics() {
        isUpdating = false;
        if (lyricsUpdater != null) {
            handler.removeCallbacks(lyricsUpdater);
        }
    }

    public void resumeLyrics() {
        if (!isUpdating && lyricsUpdater != null) {
            isUpdating = true;
            handler.post(lyricsUpdater);
        }
    }



    public List<Lyric> parseLyrics(String lyricsContent) {
        List<Lyric> lyricsList = new ArrayList<>();
        String[] lines = lyricsContent.split("\n"); // 按行拆分歌词

        for (String line : lines) {
            if (line.trim().isEmpty() || !line.contains("]")) continue; // 跳过空行或无时间戳的行

            try {
                int endIndex = line.indexOf("]");
                String timeStr = line.substring(1, endIndex); // 提取时间部分
                String text = line.substring(endIndex + 1).trim(); // 提取歌词部分

                // 将时间转换为毫秒
                String[] timeParts = timeStr.split("[:.]");
                long minutes = Long.parseLong(timeParts[0]) * 60 * 1000; // 分钟转毫秒
                long seconds = Long.parseLong(timeParts[1]) * 1000; // 秒转毫秒
                long milliseconds = Long.parseLong(timeParts[2]); // 毫秒
                long time = minutes + seconds + milliseconds;

                lyricsList.add(new Lyric(time, text));
            } catch (Exception e) {
                e.printStackTrace(); // 跳过解析错误的行
            }
        }

        return lyricsList;
    }

    private void updateLyrics(TextView textView, List<Lyric> lyricsList) {
        new Thread(() -> {
            long startTime = System.currentTimeMillis(); // 记录开始时间

            for (int i = 0; i < lyricsList.size(); i++) {
                Lyric currentLyric = lyricsList.get(i);
                long currentTime = System.currentTimeMillis() - startTime; // 当前播放时间

                // 等待到当前歌词的显示时间
                long delay = currentLyric.time - currentTime;
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // 更新歌词内容到主线程
                String lyricText = currentLyric.text;
                textView.post(() -> textView.setText(lyricText));
            }
        }).start();
    }







    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    }



    private void OnClick(){
        sideBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 检查 DrawerLayout 是否打开
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START); // 关闭 Drawer
                } else {
                    drawerLayout.openDrawer(GravityCompat.START); // 打开 Drawer
                }
            }
        });

        MusicPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("UnsafeOptInUsageError") Intent intent=new Intent(MainActivity.this,MusicPlay.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_top);
            }
        });

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClickAnimator.ButtonClickAnimator(v);
                if(player.isPlaying()){
                    musicService.Pause();
                    play.setImageResource(R.drawable.icon_pause);
                }else {
                    player.play();
                    play.setImageResource(R.drawable.icon_play);
                }

            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectPage(4);
            }
        });

        IndexCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                @SuppressLint("UnsafeOptInUsageError") Intent intent=new Intent(MainActivity.this,MusicPlay.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_top);
            }
        });

    }


    private void UpdateUi(){
        if(MusicHolder.getMusicName()!=null){
            musicName.setText(MusicHolder.getMusicName());
            musicName.setSelected(true);

        }
        if(MusicHolder.getArtistName()!=null){
            artistName.setText(MusicHolder.getArtistName());
            artistName.setSelected(true);

        }


        if(MusicHolder.getAlbumArtUrl()!=null){
            Glide.with(this)
                    .load(MusicHolder.getAlbumArtUrl())
                    .into(MusicPlay);
        }



    }

    private void SideBarClick(){
        NavigationView navigationView=findViewById(R.id.navigation_view);

        View HeadView=navigationView.getHeaderView(0);
        ImageButton find=HeadView.findViewById(R.id.Find);
        ImageButton index=HeadView.findViewById(R.id.Index);
        ImageButton Logout=HeadView.findViewById(R.id.Logout);

        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                selectFragment(2);
                SelectPage(2);
                drawerLayout.closeDrawer(GravityCompat.START);
//                GetRecommendMusic(getApplicationContext());
            }
        });
        index.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                selectFragment(1);
                SelectPage(1);
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        });

        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheetDialog();
            }
        });

    }



    public static void SelectPage(int i){

        switch (i){
            case 1:
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_main,fragment_index)
                        .commit();
                sideBar.setVisibility(View.VISIBLE);
                search.setVisibility(View.VISIBLE);
                break;
            case 2:
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_main,fragment_find)
                        .addToBackStack(null)
                        .commit();
                sideBar.setVisibility(View.VISIBLE);
                search.setVisibility(View.VISIBLE);
                break;
            case 3:
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_main,fragment_music)
                        .addToBackStack(null)
                        .commit();
                sideBar.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
                break;
            case 4:
                fragmentManager.beginTransaction()
                        .replace(R.id.frame_main,fragment_search_music)
                        .addToBackStack(null)
                        .commit();
                sideBar.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
                break;

                case 5:
                    fragmentManager.beginTransaction()
                        .replace(R.id.frame_main,fragment_artist)
                        .addToBackStack(null)
                        .commit();
                    sideBar.setVisibility(View.GONE);
                    search.setVisibility(View.GONE);
                    break;

            default:
                break;
        }

    }


//    public static void SelectPage(int i) {
//        FragmentTransaction transaction = fragmentManager.beginTransaction();
//
//        switch (i) {
//            case 1:
//                // 如果 fragment_index 不在栈中，则添加它
//                if (fragment_index.isAdded()) {
//                    transaction.show(fragment_index);
//                } else {
//                    transaction.add(R.id.frame_main, fragment_index);
//                }
//                // 隐藏其他页面
//                hideOtherFragments(transaction, fragment_index);
//                sideBar.setVisibility(View.VISIBLE);
//                search.setVisibility(View.VISIBLE);
//                pageStack.push(1);
//                break;
//            case 2:
//                // 如果 fragment_find 不在栈中，则添加它
//                if (fragment_find.isAdded()) {
//                    transaction.show(fragment_find);
//                } else {
//                    transaction.add(R.id.frame_main, fragment_find);
//                }
//                // 隐藏其他页面
//                hideOtherFragments(transaction, fragment_find);
//                sideBar.setVisibility(View.VISIBLE);
//                search.setVisibility(View.VISIBLE);
//                pageStack.push(2);
//                break;
//            case 3:
//                // 如果 fragment_music 不在栈中，则添加它
//                fragmentManager.beginTransaction()
//                        .replace(R.id.frame_main,fragment_music)
//                        .addToBackStack(null)
//                        .commit();
//
////                if (fragment_music.isAdded()) {
//////                    transaction.show(fragment_music);
////                    transaction.replace(R.id.frame_main, fragment_music);
////
////
////                } else {
////                    transaction.add(R.id.frame_main, fragment_music);
////                }
//
//                // 隐藏其他页面
////                hideOtherFragments(transaction, fragment_music);
//                sideBar.setVisibility(View.GONE);
//                search.setVisibility(View.GONE);
//                pageStack.push(3);
//                break;
//            case 4:
//                // 如果 fragment_search_music 不在栈中，则添加它
//                if (fragment_search_music.isAdded()) {
//                    transaction.show(fragment_search_music);
//                } else {
//                    transaction.add(R.id.frame_main, fragment_search_music);
//                }
//                // 隐藏其他页面
//                hideOtherFragments(transaction, fragment_search_music);
//                sideBar.setVisibility(View.GONE);
//                search.setVisibility(View.GONE);
//                pageStack.push(4);
//                break;
//            case 5:
//                // 如果 fragment_artist 不在栈中，则添加它
//                if (fragment_artist.isAdded()) {
//                    transaction.show(fragment_artist);
//                } else {
//                    transaction.add(R.id.frame_main, fragment_artist);
//                }
//                // 隐藏其他页面
//                hideOtherFragments(transaction, fragment_artist);
//                sideBar.setVisibility(View.GONE);
//                search.setVisibility(View.GONE);
//                pageStack.push(5);
//                break;
//
//            default:
//                break;
//        }
//
//        transaction.commit();
//    }

    // 隐藏其他已经添加的 Fragment
    private static void hideOtherFragments(FragmentTransaction transaction, Fragment currentFragment) {
        if (fragment_index != currentFragment && fragment_index.isAdded()) {
            transaction.hide(fragment_index);
        }
        if (fragment_find != currentFragment && fragment_find.isAdded()) {
            transaction.hide(fragment_find);
        }
        if (fragment_music != currentFragment && fragment_music.isAdded()) {
            transaction.hide(fragment_music);
        }
        if (fragment_search_music != currentFragment && fragment_search_music.isAdded()) {
            transaction.hide(fragment_search_music);
        }
        if (fragment_artist != currentFragment && fragment_artist.isAdded()) {
            transaction.hide(fragment_artist);
        }
    }



    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onEvent(MusicPlayUpdateUI event) {
        // 处理接收到的粘性事件
        UpdateUi();
        play.setImageResource(R.drawable.icon_play);
        if(player != null && player.isPlaying()){
            play.setImageResource(R.drawable.icon_play);
            syncLyricsWithExoPlayer(artistName,player,parseLyrics(MusicHolder.getLyrics()));
        }else {
            play.setImageResource(R.drawable.icon_pause);
        }
    }

    // 处理返回按钮
    @Override
    public void onBackPressed() {
//        if(pageStack.isEmpty()){
//            super.onBackPressed(); // 如果没有回退栈，执行默认行为
//        }
//
//        if(pageStack.peek()==1){
//            finish(); // 结束 Activity
//        }else {
//            int page=pageStack.pop();
//            Log.d("页面栈", String.valueOf(page));
//            SelectPage(pageStack.pop());
//        }
//
//        Log.d("页面栈", "触发返回按钮");
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack(); // 弹出栈顶的 Fragment
        } else {
            super.onBackPressed(); // 执行默认的返回操作
        }

    }

    private void showBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialog);

        // Inflate the layout for the bottom sheet dialog
        View bottomView = LayoutInflater.from(this).inflate(R.layout.pop_window, null);
        bottomSheetDialog.setContentView(bottomView);

        // Find the close button in the bottom sheet layout
        Button close = bottomView.findViewById(R.id.ClosePopWindow);
        Button logout=bottomView.findViewById(R.id.Logout);
        Button button=bottomView.findViewById(R.id.ChangeUpdateHeadImage);
        // Set an OnClickListener for the close button
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Dismiss the bottom sheet dialog when the button is clicked
                bottomSheetDialog.dismiss();
            }
        });


        //退出登录
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                logout(MainActivity.this);
                Index.isLogin=false;

            }
        });

        // Show the bottom sheet dialog
        bottomSheetDialog.show();
    }

    @Override
    protected void onStart(){
        super.onStart();
        EventBus.getDefault().register(this);

    }


    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        // 解绑服务
        unbindService(serviceConnection);
        isServiceBound = false; // 重置标志
        Log.d("音乐服务", "解除绑定");
    }

    @Override
    protected void onResume(){
        super.onResume();

    }


}