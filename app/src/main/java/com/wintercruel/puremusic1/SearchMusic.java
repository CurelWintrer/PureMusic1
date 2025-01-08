package com.wintercruel.puremusic1;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.wintercruel.puremusic1.adapter.MusicAdapter;
import com.wintercruel.puremusic1.adapter.SearchItemAdapter;
import com.wintercruel.puremusic1.cloud.Net;
import com.wintercruel.puremusic1.database.MusicDatabase;
import com.wintercruel.puremusic1.entity.MusicHolder;
import com.wintercruel.puremusic1.items.MusicItem;
import com.wintercruel.puremusic1.play_fragment.CloudMusic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchMusic#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchMusic extends Fragment {

    private RecyclerView musicList;
    private SearchItemAdapter musicAdapter;
    private ArrayList<MusicItem> musicItems;

    private SearchView searchView;
    private Button button;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SearchMusic() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SearchMusic.
     */
    // TODO: Rename and change types and number of parameters
    public static SearchMusic newInstance(String param1, String param2) {
        SearchMusic fragment = new SearchMusic();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_search_music, container, false);

        searchView=view.findViewById(R.id.SearchText);
        button=view.findViewById(R.id.SearchButton);

        musicList=view.findViewById(R.id.SearchResult);
        musicList.setItemAnimator(new DefaultItemAnimator());
        musicList.setLayoutManager(new LinearLayoutManager(getContext()));
        musicItems=new ArrayList<>();
        musicAdapter=new SearchItemAdapter(getContext(),musicItems);
        musicList.setAdapter(musicAdapter);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text= searchView.getQuery().toString();
                LoadSearchResult(text);
            }
        });


        return view;
    }

    private void LoadSearchResult(String SearchText) {
        // 使用 Handler 将 UI 更新切换到主线程
        Handler handler = new Handler(Looper.getMainLooper());
        String tableName = "playlist_search";
        // 解析音乐信息，保存到数据库
        MusicDatabase musicDatabase = new MusicDatabase(getContext());

        // 在后台线程中执行网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = musicDatabase.getWritableDatabase();
                try {
                    // 删除表（如果存在）
                    String dropTableQuery = "DROP TABLE IF EXISTS " + tableName;
                    db.execSQL(dropTableQuery);

                    // 重新创建表
                    String CREATE_TABLE_MUSIC = "CREATE TABLE " + tableName + " (" +
                            "id TEXT PRIMARY KEY," +
                            "title TEXT," +
                            "artist TEXT," +
                            "albumUrl TEXT)";
                    db.execSQL(CREATE_TABLE_MUSIC);

                    // 执行网络请求
                    String result = Net.SearchMusic(requireActivity(), SearchText);

                    if (result != null && !result.isEmpty()) {
                        List<MusicItem> Items = new ArrayList<>();
                        try {
                            JSONObject jsonObject = new JSONObject(result);
                            // 获取嵌套的 "result" 对象
                            JSONObject resultObject = jsonObject.getJSONObject("result");

                            MusicHolder.setSearchResult(String.valueOf(resultObject));

                            // 现在从 "result" 对象中获取 "songs" 数组
                            JSONArray jsonArray = resultObject.getJSONArray("songs");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject musicData = jsonArray.getJSONObject(i);
                                String name = musicData.getString("name");
                                String id = musicData.getString("id");
                                // 获取艺术家数组
                                JSONArray arArray = musicData.getJSONArray("ar");
                                StringBuilder artistNames = new StringBuilder();

                                for (int j = 0; j < arArray.length(); j++) {
                                    JSONObject artistObject = arArray.getJSONObject(j);
                                    String artistName = artistObject.getString("name");
                                    if (j > 0) {
                                        artistNames.append("/"); // 用斜杠隔开
                                    }
                                    artistNames.append(artistName);
                                }

                                // 获取专辑信息
                                JSONObject albumObject = musicData.getJSONObject("al");
                                String albumPicUrl = albumObject.getString("picUrl");

                                // 插入音乐数据
                                musicDatabase.InsertMusic(tableName, id, name, artistNames.toString(), albumPicUrl);

                                MusicItem item = new MusicItem();
                                item.setMusicImage(albumPicUrl);
                                item.setMusicName(name);
                                item.setArtistName(String.valueOf(artistNames));
                                Items.add(item);
                            }

                            // 在主线程更新 UI
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    musicItems.clear();
                                    musicItems.addAll(Items);  // 更新数据
                                    musicAdapter.notifyDataSetChanged(); // 通知适配器更新 UI
                                }
                            });

                        } catch (JSONException e) {
                            // 在主线程显示错误提示
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(requireActivity(), "解析出错: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        // 如果没有结果，显示提示
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(requireActivity(), "无搜索结果", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    // 在主线程显示错误提示
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(requireActivity(), "网络请求失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                } finally {
                    db.close();
                }
            }
        }).start();  // 启动后台线程
    }


}