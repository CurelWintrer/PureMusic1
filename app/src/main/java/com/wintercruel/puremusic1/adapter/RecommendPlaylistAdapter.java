package com.wintercruel.puremusic1.adapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wintercruel.puremusic1.MainActivity;
import com.wintercruel.puremusic1.R;
import com.wintercruel.puremusic1.entity.MusicHolder;
import com.wintercruel.puremusic1.items.PlayListItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class RecommendPlaylistAdapter extends RecyclerView.Adapter<RecommendPlaylistAdapter.ViewHolder>{

    private Context mContext;
    private List<PlayListItem> mPlayListItem;

    public RecommendPlaylistAdapter(Context context, List<PlayListItem> playListItem) {
        mContext = context;
        mPlayListItem = playListItem;
    }

    @NonNull
    @Override
    public RecommendPlaylistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.recommend_playlist,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendPlaylistAdapter.ViewHolder holder, int position) {
        holder.bind(mPlayListItem.get(position));
    }

    @Override
    public int getItemCount() {
        return mPlayListItem.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mImageView;
        private TextView mTextView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView=itemView.findViewById(R.id.recommendPlaylistImage);
            mTextView=itemView.findViewById(R.id.recommendPlaylistTitle);
            itemView.setOnClickListener(this);

        }

        public void bind(PlayListItem playListItem){
            mTextView.setText(playListItem.getPlayListName());
            Glide.with(itemView.getContext())
                    .load(playListItem.getImgUrl())
                    .placeholder(R.drawable.my_like_image_show)
                    .into(mImageView);
        }

        @Override
        public void onClick(View v) {
            int position=getAdapterPosition();

            SharedPreferences sharedPreferences = itemView.getContext().getSharedPreferences("recommendPlayList", MODE_PRIVATE);
            String playListJSONData = sharedPreferences.getString("playList", null);

            try {
                JSONObject jsonObject = new JSONObject(playListJSONData);
                JSONArray playlistArray = jsonObject.getJSONArray("recommend");

                // 确保 position 在数组范围内
                if (position < 0 || position >= playlistArray.length()) {
                    Log.e("Error", "Invalid position: " + position);
                    return; // 处理无效位置
                }

                JSONObject playlistObject = playlistArray.getJSONObject(position); // 获取每个歌单对象
                String playListId = playlistObject.getString("id");
                String playListName=playlistObject.getString("name");
                MusicHolder.setPlayListId(playListId);
                MusicHolder.setPlayListName(playListName);


            } catch (JSONException e) {
                Log.e("Error", "Failed to parse playlist JSON", e);
            }

            if(position !=RecyclerView.NO_POSITION){

//                String finalPlayListId = PlayListId;
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                        .withEndAction(()->{
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                            MainActivity.SelectPage(3);
                        }).start();

            }
        }
    }
}
