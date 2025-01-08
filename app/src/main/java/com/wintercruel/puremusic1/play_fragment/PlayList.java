package com.wintercruel.puremusic1.play_fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.wintercruel.puremusic1.R;
import com.wintercruel.puremusic1.adapter.PlayListAdapter;
import com.wintercruel.puremusic1.event_bus.LoginSuccessUpdateUI;
import com.wintercruel.puremusic1.items.PlayListItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class PlayList extends Fragment {

    private RecyclerView playListView;
    private PlayListAdapter playListAdapter;
    private List<PlayListItem>playListItems;


    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public PlayList() {
        // Required empty public constructor
    }


    public static PlayList newInstance(String param1, String param2) {
        PlayList fragment = new PlayList();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {

        super.onStart();
        LoginSuccessUpdateUI stickyEvent = EventBus.getDefault().getStickyEvent(LoginSuccessUpdateUI.class);
        if (stickyEvent != null) {
            onLoginUpdateUI(stickyEvent);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoginUpdateUI(LoginSuccessUpdateUI event) {
        LoadPlayList();
    }


    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
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
        View view=inflater.inflate(R.layout.fragment_play_list, container, false);

        playListView=view.findViewById(R.id.PlayList);
        playListView.setItemAnimator(new DefaultItemAnimator());
        playListView.setLayoutManager(new LinearLayoutManager(getContext()));
        playListItems=new ArrayList<>();
        playListAdapter=new PlayListAdapter(getContext(),playListItems);
        playListView.setAdapter(playListAdapter);

        LoadPlayList();

        return view;
    }

    public void LoadPlayList(){
        List<PlayListItem> Items=new ArrayList<>();
        SharedPreferences sharedPreferences= getContext().getSharedPreferences("playList",MODE_PRIVATE);
        String playListJSONDate= sharedPreferences.getString("playList",null);
        if(playListJSONDate!=null){
            try {
                JSONObject jsonObject=new JSONObject(playListJSONDate);
                JSONArray playlistArray=jsonObject.getJSONArray("playlist");

                String coverImgUrl=null;
                String name=null;
                String trackCount=null;

                // 遍历每个歌单
                for (int i = 0; i < playlistArray.length(); i++) {
                    JSONObject playlistObject = playlistArray.getJSONObject(i); // 获取每个歌单对象
                    coverImgUrl = playlistObject.getString("coverImgUrl"); // 获取封面图 URL
                    name = playlistObject.getString("name"); // 获取歌单名称
                    trackCount=playlistObject.getString("trackCount");//歌单音乐数量

                    PlayListItem Item=new PlayListItem();
                    Item.setPlayListName(name);
                    Item.setImgUrl(coverImgUrl);
                    Item.setTrackCount(trackCount);
                    Items.add(Item);
                }


            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        playListItems.clear();
        playListAdapter.notifyDataSetChanged();
        playListItems.addAll(Items);
        int startPosition=playListItems.size();
        playListAdapter.notifyItemChanged(startPosition,Items.size());
    }




}