package com.wintercruel.puremusic1;

import static android.content.Context.MODE_PRIVATE;
import static com.wintercruel.puremusic1.MainActivity.search;
import static com.wintercruel.puremusic1.MainActivity.sideBar;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import com.wintercruel.puremusic1.entity.MusicHolder;
import com.wintercruel.puremusic1.event_bus.LoginSuccessUpdateUI;
import com.wintercruel.puremusic1.play_fragment.CloudMusic;
import com.wintercruel.puremusic1.play_fragment.PlayList;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;




public class  Index extends Fragment {

    public static boolean isLogin=false;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";


    private final Fragment PlayList=new PlayList();
    private final Fragment CloudMusic=new CloudMusic();

    private FragmentManager fragmentManager;




    private String mParam1;
    private String mParam2;

    public Index() {
        // Required empty public constructor
    }

    @Override
    public void onResume(){
        super.onResume();
        sideBar.setVisibility(View.VISIBLE);
        search.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        LoginSuccessUpdateUI stickyEvent = EventBus.getDefault().getStickyEvent(LoginSuccessUpdateUI.class);
        if (stickyEvent != null) {
            onLoginUpdateUI(stickyEvent);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }


    public static Index newInstance(String param1, String param2) {
        Index fragment = new Index();
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
        View view=inflater.inflate(R.layout.fragment_index, container, false);
        View LoginButton=view.findViewById(R.id.HeadImageIndex);
        View Background=view.findViewById(R.id.BackGroundIndex);
        View NickName=view.findViewById(R.id.UserNameIndex);
        View PlayList=view.findViewById(R.id.SongListIndex);
        View CloudMusic=view.findViewById(R.id.CloudStorage);
        View PlayListBac=view.findViewById(R.id.SongListIndexBac);
        View CloudMusicBac=view.findViewById(R.id.CloudStorageBac);

        LoadUsers((ImageButton) LoginButton, (TextView) NickName, (ImageView) Background); //加载用户资料

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isLogin){
                    Toast.makeText(getContext(), "已登录", Toast.LENGTH_SHORT).show();
                }else {
                    Intent intent=new Intent(getActivity(), Login.class);
                    startActivity(intent);
                }
            }
        });

        PlayList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFragment(1);
                PlayListBac.setBackgroundResource(R.drawable.button_b_background);
                CloudMusicBac.setBackgroundColor(Color.TRANSPARENT);
            }
        });

        CloudMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectFragment(2);
                CloudMusicBac.setBackgroundResource(R.drawable.button_b_background);
                PlayListBac.setBackgroundColor(Color.TRANSPARENT);
            }
        });


        InitFragment();


        return view;
    }

    private void InitFragment() {
        fragmentManager = getChildFragmentManager();  // 使用 getChildFragmentManager()
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 如果 Fragment 尚未被添加，则添加它们
        if (!PlayList.isAdded()) {
            transaction.add(R.id.frame_index, PlayList);
        }
        if (!CloudMusic.isAdded()) {
            transaction.add(R.id.frame_index, CloudMusic);
        }

        // 隐藏所有 Fragment
        hideView(transaction);

        // 显示默认的 Fragment（例如 PlayList）
        transaction.show(PlayList);



        // 提交事务
        transaction.commit();
    }

    private void hideView(FragmentTransaction transaction) {
//        if (PlayList.isAdded()) {
            transaction.hide(PlayList);
//        }
//        if (CloudMusic.isAdded()) {
            transaction.hide(CloudMusic);
//        }
    }

    private void selectFragment(int i) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // 隐藏所有 Fragment
        hideView(transaction);

        // 根据选择显示对应的 Fragment
        switch (i) {
            case 1:
                if (PlayList.isAdded()) {
                    transaction.show(PlayList);
                }
                break;
            case 2:
                if (CloudMusic.isAdded()) {
                    transaction.show(CloudMusic);
                }
                break;
            default:
                break;
        }

        transaction.commit();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onLoginUpdateUI(LoginSuccessUpdateUI event) {
//        if(MusicHolder.isPlayerMode()){
//            // 调用 LoadUsers 方法更新 UI
//            Handler handler=new Handler(Looper.getMainLooper());
//            new Thread(()->{
//                if(MusicHolder.isPlayerMode()){
//                    GetUserDetail(getContext());
//                }
//                handler.post(()->{
//                    LoadUsers((ImageButton) getView().findViewById(R.id.HeadImageIndex),
//                            (TextView) getView().findViewById(R.id.UserNameIndex),
//                            (ImageView) getView().findViewById(R.id.BackGroundIndex));
//                });
//            }).start();
//        }else {
//            LoadUsers((ImageButton) getView().findViewById(R.id.HeadImageIndex),
//                    (TextView) getView().findViewById(R.id.UserNameIndex),
//                    (ImageView) getView().findViewById(R.id.BackGroundIndex));
//        }
        LoadUsers((ImageButton) getView().findViewById(R.id.HeadImageIndex),
                (TextView) getView().findViewById(R.id.UserNameIndex),
                (ImageView) getView().findViewById(R.id.BackGroundIndex));
//        LoadPlayList(requireContext());
    }

    private void LoadUsers(ImageButton HeadImage, TextView Nickname, ImageView Background){


        SharedPreferences sharedPreferences = this.getContext().getSharedPreferences("User", MODE_PRIVATE);
        String avatarUrl=sharedPreferences.getString("avatarUrl",null);
        String nickname=sharedPreferences.getString("nickname",null);
        String background=sharedPreferences.getString("backgroundUrl",null);
        System.out.println(avatarUrl);
        System.out.println(nickname);
        System.out.println(background);

        if(avatarUrl!=null){
            isLogin =true;
            Glide.with(getContext())
                    .load(avatarUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(HeadImage);
//            HeadImage.setImageURI(Uri.parse(avatarUrl));
            Glide.with(getContext())
                    .load(background)
                    .placeholder(R.drawable.round_background)
                    .into(Background);
            Nickname.setText(nickname);
        }


//            Background.setImageURI(Uri.parse(background));
    }













}