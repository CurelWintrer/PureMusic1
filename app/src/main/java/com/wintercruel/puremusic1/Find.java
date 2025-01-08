package com.wintercruel.puremusic1;

import static com.wintercruel.puremusic1.MainActivity.search;
import static com.wintercruel.puremusic1.MainActivity.sideBar;
import static com.wintercruel.puremusic1.cloud.Net.GetPlayListMusic;


import static com.wintercruel.puremusic1.cloud.Net.client;
import static com.wintercruel.puremusic1.cloud.Net.createTable;
import static com.wintercruel.puremusic1.cloud.Net.initialize;
import static com.wintercruel.puremusic1.cloud.Net.isTableExist;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wintercruel.puremusic1.adapter.RecommendArtistAdapter;
import com.wintercruel.puremusic1.adapter.RecommendMusicAdapter;
import com.wintercruel.puremusic1.adapter.RecommendPlaylistAdapter;
import com.wintercruel.puremusic1.cloud.Net;
import com.wintercruel.puremusic1.cloud.server;
import com.wintercruel.puremusic1.database.MusicDatabase;
import com.wintercruel.puremusic1.items.ArtistItem;
import com.wintercruel.puremusic1.items.MusicItem;
import com.wintercruel.puremusic1.items.PlayListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Find#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Find extends Fragment {


    private RecyclerView recyclerView;
    private RecommendMusicAdapter adapter;
    private ArrayList<MusicItem> musicItems;


    private RecyclerView playlistRecommend;
    private RecommendPlaylistAdapter playlistAdapter;
    private ArrayList<PlayListItem> playlistItems;

    private RecyclerView artistRecommend;
    private RecommendArtistAdapter artistAdapter;
    private ArrayList<ArtistItem> artistItems;

    private Boolean isSameDay=false;



    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public Find() {
        // Required empty public constructor
    }


    public static Find newInstance(String param1, String param2) {
        Find fragment = new Find();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public boolean isSameDay(Context context) {


        // 获取当前日期
        SharedPreferences sharedPreferences = context.getSharedPreferences("SystemTime", Context.MODE_PRIVATE);
        String savedTime = sharedPreferences.getString("systemTime", null);

        Log.d("保存的当前时间：", "savedTime = " + savedTime);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());

        // 如果没有保存的时间，表示第一次打开，返回 false
        if (savedTime==null) {
            Log.d("当前时间：", "没有保存时间");
            SharedPreferences sharedPreferences_edit = getContext().getSharedPreferences("SystemTime", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences_edit.edit();
            long currentTimeMillis = System.currentTimeMillis();
            String time = String.valueOf(currentTimeMillis);
            editor.putString("systemTime", time);
            editor.apply();
            return false;
        }else {
            // 如果保存的时间格式无效，直接返回 false
            long savedTimeLong;
            try {
                savedTimeLong = Long.parseLong(savedTime);
            } catch (NumberFormatException e) {
                Log.d("当前时间：", "保存的时间格式无效");
                return false;
            }

            // 格式化保存的时间并与当前时间比较
            String savedFormattedTime = sdf.format(new Date(savedTimeLong));

            Log.d("当前时间：", currentDate + " " + savedFormattedTime);

            // 判断日期是否相同
            if (!savedFormattedTime.equals(currentDate)) {
                Log.d("当前时间：", "不同");
                return false;
            }

            Log.d("当前时间：", "相同");
            return true;
        }


    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_find, container, false);
        recyclerView = view.findViewById(R.id.dailyRecommendations);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        musicItems=new ArrayList<>();
        adapter=new RecommendMusicAdapter(getContext(),musicItems);
        recyclerView.setAdapter(adapter);

        playlistRecommend=view.findViewById(R.id.playlistRecommendations);
        playlistRecommend.setItemAnimator(new DefaultItemAnimator());
        playlistRecommend.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        playlistItems=new ArrayList<>();
        playlistAdapter=new RecommendPlaylistAdapter(getContext(),playlistItems);
        playlistRecommend.setAdapter(playlistAdapter);

        artistRecommend=view.findViewById(R.id.artistRecommendations);
        artistRecommend.setItemAnimator(new DefaultItemAnimator());
        artistRecommend.setLayoutManager(new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false));
        artistItems=new ArrayList<>();
        artistAdapter=new RecommendArtistAdapter(getContext(),artistItems);
        artistRecommend.setAdapter(artistAdapter);

        isSameDay=isSameDay(getContext());


        GetRecommendMusic(getContext());



        GetRecommendPlayList();

        GetRecommendArtist();



        return view;
    }




    private void GetRecommendPlayList(){
        List<PlayListItem> items=new ArrayList<>();
        if(isSameDay){
            SharedPreferences sharedPreferences=getContext().getSharedPreferences("recommendPlayList", Context.MODE_PRIVATE);
            String responseDate=sharedPreferences.getString("playList","");
            try {
                JSONObject jsonObject=new JSONObject(responseDate);
                JSONArray jsonArray=jsonObject.getJSONArray("recommend");
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject object=jsonArray.getJSONObject(i);
                    String name=object.getString("name");
                    String picUrl=object.getString("picUrl");
                    String id=object.getString("id");


                    PlayListItem Item=new PlayListItem();
                    Item.setPlayListName(name);
                    Item.setImgUrl(picUrl);
                    items.add(Item);
                }

            } catch (JSONException e) {
                Log.d("每日推荐歌单：", "解析失败");
            }
            getActivity().runOnUiThread(()->{
                playlistItems.clear();
                playlistItems.addAll(items);
                playlistAdapter.notifyDataSetChanged();

            });


        }else {
            Request request=new Request.Builder()
                    .url(server.ADDRESS+"/recommend/resource")
                    .build();


            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d("每日推荐歌单：", "请求失败");
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if(response.isSuccessful()){
                        String responseDate=response.body().string();
                        SharedPreferences sharedPreferences_edit = getContext().getSharedPreferences("recommendPlayList", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences_edit.edit();
                        editor.putString("playList", responseDate);
                        editor.apply();

                        try {
                            JSONObject jsonObject=new JSONObject(responseDate);
                            JSONArray jsonArray=jsonObject.getJSONArray("recommend");
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject object=jsonArray.getJSONObject(i);
                                String name=object.getString("name");
                                String picUrl=object.getString("picUrl");
                                String id=object.getString("id");

                                GetPlayListMusic(id,getContext());

                                PlayListItem Item=new PlayListItem();
                                Item.setPlayListName(name);
                                Item.setImgUrl(picUrl);
                                items.add(Item);
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                    getActivity().runOnUiThread(()->{
                        playlistItems.clear();
                        playlistItems.addAll(items);
                        playlistAdapter.notifyDataSetChanged();

                    });

                }
            });
        }





    }

    private void GetRecommendArtist(){
        List<ArtistItem> items=new ArrayList<>();
        if(isSameDay){
            SharedPreferences sharedPreferences=getContext().getSharedPreferences("recommendArtist", Context.MODE_PRIVATE);
            String responseDate=sharedPreferences.getString("artists","");
            try {
                JSONObject jsonObject=new JSONObject(responseDate);
                JSONArray jsonArray=jsonObject.getJSONArray("artists");
                for(int i=0;i<jsonArray.length();i++){
                    JSONObject object=jsonArray.getJSONObject(i);
                    String name=object.getString("name");
                    String picUrl=object.getString("picUrl");
                    String id=object.getString("id");


                    ArtistItem Item=new ArtistItem();
                    Item.setName(name);
                    Item.setImageUrl(picUrl);
                    items.add(Item);
                }

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            getActivity().runOnUiThread(()->{
                artistItems.clear();
                artistItems.addAll(items);
                artistAdapter.notifyDataSetChanged();

            });

        }else {
            Request request=new Request.Builder()
                    .url(server.ADDRESS+"/top/artists")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d("每日推荐歌手：", "请求失败");
                    e.printStackTrace();
                }


                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                    if(response.isSuccessful()){
                        String responseDate=response.body().string();
                        Log.d("每日推荐歌手：",responseDate);
                        SharedPreferences sharedPreferences_edit = getContext().getSharedPreferences("recommendArtist", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences_edit.edit();
                        editor.putString("artists", responseDate);
                        editor.apply();


                        try {
                            JSONObject jsonObject=new JSONObject(responseDate);
                            JSONArray jsonArray=jsonObject.getJSONArray("artists");
                            for(int i=0;i<jsonArray.length();i++){
                                JSONObject object=jsonArray.getJSONObject(i);
                                String name=object.getString("name");
                                String picUrl=object.getString("picUrl");
                                String id=object.getString("id");

                                GetPlayListMusic(id,getContext());

                                ArtistItem Item=new ArtistItem();
                                Item.setName(name);
                                Item.setImageUrl(picUrl);
                                items.add(Item);
                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                    getActivity().runOnUiThread(()->{
                        artistItems.clear();
                        artistItems.addAll(items);
                        artistAdapter.notifyDataSetChanged();

                    });
                }
            });
        }



    }




    public void GetRecommendMusic(Context context){
        if(isSameDay){
            LoadRecommendMusic();
        }else {
            Request request=new Request.Builder()
                    .url(server.ADDRESS+"/recommend/songs")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d("每日推荐歌曲：", "请求失败");
                    e.printStackTrace();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if(response.isSuccessful()){
                        String responseDate=response.body().string();
                        System.out.println("每日推荐歌曲："+responseDate);
                        SaveRecommendMusic(context,responseDate);
                    }
                }
            });
        }



    }



    private void SaveRecommendMusic(Context context, String responseDate) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MusicDatabase musicDatabase = new MusicDatabase(context.getApplicationContext());
                String tableName = "playlist_recommend";
                SQLiteDatabase db = musicDatabase.getWritableDatabase();

                // 检查表是否存在，表不存在时才创建
                if (!isTableExist(db, tableName)) {
                    // 如果表不存在，创建表
                    createTable(db, tableName);
                } else {
                    // 清理旧数据
                    db.delete(tableName, null, null);  // 删除该歌单表中的所有数据
                }

                // 解析新数据并插入
                try {
                    JSONObject jsonObject = new JSONObject(responseDate);
                    JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                    JSONArray musicArray = jsonObject1.getJSONArray("dailySongs");

                    for (int i = 0; i < musicArray.length(); i++) {
                        JSONObject musicData = musicArray.getJSONObject(i);
                        String name = musicData.getString("name");
                        String id = musicData.getString("id");

                        // 获取专辑信息
                        JSONObject albumObject = musicData.getJSONObject("al");
                        String albumPicUrl = albumObject.getString("picUrl");

                        JSONArray arArray = musicData.getJSONArray("ar");
                        StringBuilder artistNames = new StringBuilder();

                        for (int j = 0; j < arArray.length(); j++) {
                            JSONObject artistObject = arArray.getJSONObject(j);
                            if (j > 0) artistNames.append("/");  // 用斜杠隔开
                            artistNames.append(artistObject.getString("name"));
                        }

                        // 插入音乐数据
                        ContentValues values = new ContentValues();
                        values.put("id", id);
                        values.put("title", name);
                        values.put("artist", artistNames.toString());
                        values.put("albumUrl", albumPicUrl);

                        // 使用insertWithOnConflict来避免重复数据，REPLACE表示重复时替换
                        db.insertWithOnConflict(tableName, null, values, SQLiteDatabase.CONFLICT_REPLACE);

//                        if (result == -1) {
//                            Log.e("数据库操作", "插入失败: " + id + ", " + name);
//                        } else {
//                            Log.d("数据库操作", "插入成功: " + id + ", " + name);
//                        }
                    }



                } catch (JSONException e) {
                    Log.e("JSON解析错误", "解析数据失败", e);
                } finally {
                    db.close();
                }
                LoadRecommendMusic();
            }
        }).start();

    }

    private void LoadRecommendMusic(){
//        Handler handler=new Handler(Looper.getMainLooper());
//        String tableName="playlist_recommend";
        MusicDatabase musicDatabase=new MusicDatabase(getContext());
        List<MusicItem> items=new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cursor cursor=musicDatabase.getAllMusic("recommend");
                if(cursor!=null){
                    while(cursor.moveToNext()){
                        @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex("title"));
                        @SuppressLint("Range") String albumPicUrl = cursor.getString(cursor.getColumnIndex("albumUrl"));
                        @SuppressLint("Range") String artistNames = cursor.getString(cursor.getColumnIndex("artist"));
//                        Log.d("加载音乐：",name);
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
                    adapter.notifyDataSetChanged();
                });

            }
        }).start();
    }

    @Override
    public void onResume(){
        super.onResume();
        sideBar.setVisibility(View.VISIBLE);
        search.setVisibility(View.VISIBLE);
    }
}