package com.wintercruel.puremusic1.adapter;

import static android.content.Context.MODE_PRIVATE;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wintercruel.puremusic1.MainActivity;
import com.wintercruel.puremusic1.Music;
import com.wintercruel.puremusic1.R;
import com.wintercruel.puremusic1.entity.MusicHolder;
import com.wintercruel.puremusic1.event_bus.LoadPlaylistUI;
import com.wintercruel.puremusic1.event_bus.MusicPlayUpdateUI;
import com.wintercruel.puremusic1.items.PlayListItem;
import com.wintercruel.puremusic1.viewModel.MyViewModel;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class PlayListAdapter extends RecyclerView.Adapter<PlayListAdapter.ViewHolder>{

    private Context mContext;
    private List<PlayListItem> mPlayListItem;

    public PlayListAdapter(final Context context, final List<PlayListItem> playListItems){
        mContext=context;
        mPlayListItem=playListItems;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.playlist_item,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(mPlayListItem.get(position));
    }

    @Override
    public int getItemCount() {
        return mPlayListItem.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView PlayListImg;
        private TextView PlayListName;
        private TextView TrackCount;





        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            PlayListImg=itemView.findViewById(R.id.PlayListImg);
            PlayListName=itemView.findViewById(R.id.PlayListName);
            TrackCount=itemView.findViewById(R.id.MusicCount);
            itemView.setOnClickListener(this);

        }

        public void bind(PlayListItem playListItem){
            Log.d("歌单图片地址：",playListItem.getImgUrl());
            Glide.with(itemView.getContext())
                    .load(playListItem.getImgUrl())
                    .placeholder(R.drawable.my_like_image_show)
                    .into(PlayListImg);
            PlayListName.setText(playListItem.getPlayListName());
            TrackCount.setText(playListItem.getTrackCount()+"首");
        }

        @Override
        public void onClick(View v) {

            int position=getPosition();

            Log.d("歌单点击位置：", String.valueOf(position));

            SharedPreferences sharedPreferences = itemView.getContext().getSharedPreferences("playList", MODE_PRIVATE);
            String playListJSONData = sharedPreferences.getString("playList", null);

            System.out.println(playListJSONData);

            if (playListJSONData == null) {
                Log.e("Error", "No playlist data found in SharedPreferences");
                return;  // 处理没有播放列表数据的情况
            }

            try {
                JSONObject jsonObject = new JSONObject(playListJSONData);
                JSONArray playlistArray = jsonObject.getJSONArray("playlist");

                // 确保 position 在数组范围内
                if (position < 0 || position >= playlistArray.length()) {
                    Log.e("Error", "Invalid position: " + position);
                    return; // 处理无效位置
                }

                JSONObject playlistObject = playlistArray.getJSONObject(position); // 获取每个歌单对象
                String playListId = playlistObject.getString("id");
                String playListName=playlistObject.getString("name");
                if(MusicHolder.getPlayListId()!=playListId){
                    MusicHolder.setPlayListId(playListId);
                    MusicHolder.setPlayListName(playListName);
                    EventBus.getDefault().postSticky(new LoadPlaylistUI());
                }




            } catch (JSONException e) {
                Log.e("Error", "Failed to parse playlist JSON", e);
            }

            if(position !=RecyclerView.NO_POSITION){

                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                        .withEndAction(()->{
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();

                            MainActivity.SelectPage(3);




                        }).start();

            }


        }
    }


    private boolean isSharedPreferencesFileExists(Context context, String playListId) {
        // Construct the shared preferences file name
        String prefsFileName = "PlayList" + playListId + ".xml"; // SharedPreferences 文件以 .xml 结尾
        // Get the directory for shared preferences
        File sharedPrefsDir = new File(context.getApplicationInfo().dataDir + "/shared_prefs");
        // Create the file object for the specific SharedPreferences file
        File sharedPrefsFile = new File(sharedPrefsDir, prefsFileName);

        // Check if the file exists
        boolean exists = sharedPrefsFile.exists();
        Log.d("Debug", "Checking for SharedPreferences file: " + sharedPrefsFile.getAbsolutePath() + ", exists: " + exists);
        return exists;
    }

}
