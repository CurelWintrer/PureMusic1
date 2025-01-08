package com.wintercruel.puremusic1;







import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.media3.common.MediaItem;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.wintercruel.puremusic1.adapter.MusicAdapter;
import com.wintercruel.puremusic1.animator.ScaleInAnimator;
import com.wintercruel.puremusic1.database.MusicDatabase;
import com.wintercruel.puremusic1.entity.MusicHolder;
import com.wintercruel.puremusic1.event_bus.LoadPlaylistUI;
import com.wintercruel.puremusic1.event_bus.LoginSuccessUpdateUI;
import com.wintercruel.puremusic1.event_bus.MusicPlayUpdateUI;
import com.wintercruel.puremusic1.items.MusicItem;
import com.wintercruel.puremusic1.viewModel.MyViewModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;


public class Music extends Fragment {

    private RecyclerView musicList;
    private TextView PlayListName;
    private static MusicAdapter musicAdapter;
    private static ArrayList<MusicItem> musicItems;

    private static MusicDatabase musicDatabase;

    private static String currentPlayListId = null; // 当前加载的歌单 ID

    //音乐分页加载
    private static final int PAGE_SIZE = 100;  // 每次加载的项数
    private static int offset = 0;  // 当前偏移量
    private static boolean isLoading = false;  // 是否正在加载标志

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Music() {
        // Required empty public constructor
    }


    public static Music newInstance(String param1, String param2) {
        Music fragment = new Music();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        musicDatabase=new MusicDatabase(getContext());

        updateUI();

        Log.d("音乐界面", "onCreate");

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // 做一些操作，如获取宿主 Activity 的引用
        Log.d("音乐界面", "onAttach");
    }

    @Override
    public void onResume() {
        super.onResume();


        Log.d("音乐界面", "onResume");
    }




    @Override
    public void onStop(){
        super.onStop();
//        offset = 0;
//        musicItems.clear(); // 清空旧歌单数据
//        musicAdapter.notifyDataSetChanged();

        Log.d("音乐界面", "onStop");

    }

    @Override
    public void onStart() {
        super.onStart();

        Log.d("音乐界面", "onStart");

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // 初始化和 Activity 相关的操作
        Log.d("音乐界面", "onActivityCreated");
    }

    @Override
    public void onPause() {
        super.onPause();
        // 暂停活动，如动画、数据监听等
        Log.d("音乐界面", "onPause");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 清理视图资源
        Log.d("音乐界面", "onDestroyView");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 清理资源
        offset = 0;
        musicItems.clear(); // 清空旧歌单数据
        musicAdapter.notifyDataSetChanged();
        Log.d("音乐界面", "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        // 清理与宿主活动的引用
        Log.d("音乐界面", "onDetach");
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        Log.d("音乐界面", "onCreateView");

        View view=inflater.inflate(R.layout.fragment_music, container, false);
        musicList=view.findViewById(R.id.Music);
        PlayListName=view.findViewById(R.id.PlayListNamePlaylist);

        musicList.setItemAnimator(new DefaultItemAnimator());
        musicList.setLayoutManager(new LinearLayoutManager(getContext()));
        musicItems=new ArrayList<>();
        musicAdapter=new MusicAdapter(getContext(),musicItems);
        musicList.setAdapter(musicAdapter);

//        musicDatabase=new MusicDatabase(getContext());


        PlayListName.setText(MusicHolder.getPlayListName());
        PlayListName.setSelected(true);

//        updateUI();


        // 1. 初始化 RecyclerView 并设置 OnScrollListener
        musicList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

                if (layoutManager != null) {
                    int totalItemCount = layoutManager.getItemCount();
                    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();

                    // 当剩余项数少于 40 时加载下一页
                    if (!isLoading && totalItemCount <= (lastVisibleItemPosition + 40)) {
                        new Thread(()->{
                            LoadList(MusicHolder.getPlayListId());
//                            loadPlaylist(MusicHolder.getPlayListId());
                        }).start();
                    }
                }

            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // 检查滚动状态，如果开始滚动并且可见，则触发隐藏动画

            }



        });


        return view;
    }

    public static void updateUI() {
        if (currentPlayListId != null) {
            offset = 0;
            musicItems.clear();
            musicAdapter.notifyDataSetChanged(); // 通知数据源被清空


        }
        new Thread(()->{
            LoadList(MusicHolder.getPlayListId());
        }).start();
    }

    // 2. 获取数据库的总项数
    private static int getTotalCount(String PlayListId) {
        SQLiteDatabase db = musicDatabase.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM playlist_" + PlayListId, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }


    // 3. 更新 LoadList 方法：检查总数和分页项数
    private static void LoadList(String PlayListId) {
        if (isLoading) return;
        isLoading = true;

        int totalItems = getTotalCount(PlayListId); // 获取总项数
        int loadSize = Math.min(PAGE_SIZE, totalItems - offset); // 当剩余项不足 PAGE_SIZE 时加载剩余项

        List<MusicItem> items = new ArrayList<>();
        Cursor cursor = musicDatabase.getMusicPaginated(PlayListId, offset, loadSize);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("title"));
                @SuppressLint("Range") String albumPicUrl = cursor.getString(cursor.getColumnIndex("albumUrl"));
                @SuppressLint("Range") String artistNames = cursor.getString(cursor.getColumnIndex("artist"));

                MusicItem item = new MusicItem();
                item.setMusicImage(albumPicUrl);
                item.setMusicName(name);
                item.setArtistName(artistNames);
                items.add(item);
            }
            cursor.close();
        }

        new Handler(Looper.getMainLooper()).post(()->{
            musicItems.addAll(items);
            musicAdapter.notifyItemRangeInserted(offset, items.size());
        });


        offset += loadSize; // 更新偏移量
        isLoading = false; // 加载完成
    }







}