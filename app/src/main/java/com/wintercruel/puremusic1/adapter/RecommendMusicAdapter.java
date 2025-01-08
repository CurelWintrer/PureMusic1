package com.wintercruel.puremusic1.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.wintercruel.puremusic1.MainActivity;
import com.wintercruel.puremusic1.R;
import com.wintercruel.puremusic1.entity.MusicHolder;
import com.wintercruel.puremusic1.items.MusicItem;
import com.wintercruel.puremusic1.service.MyMusicService;

import java.util.List;

public class RecommendMusicAdapter extends RecyclerView.Adapter<RecommendMusicAdapter.ViewHolder>{

    private Context mContext;
    private List<MusicItem>mMusicList;
    private final MyMusicService musicService= MainActivity.GetBindService();

    public RecommendMusicAdapter(Context context, List<MusicItem> musicList){
        this.mContext=context;
        this.mMusicList=musicList;
    }


    @NonNull
    @Override
    public RecommendMusicAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.recomend_item,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendMusicAdapter.ViewHolder holder, int position) {
        holder.bind(mMusicList.get(position));
    }

    @Override
    public int getItemCount() {
        return mMusicList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView MusicImage;
        private TextView MusicName;
        private TextView MusicSinger;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            MusicImage=itemView.findViewById(R.id.RecommendItemImage);
            MusicName=itemView.findViewById(R.id.RecommendItemTitle);
            MusicSinger=itemView.findViewById(R.id.RecommendItemAuthor);
            itemView.setOnClickListener(this);
        }

        public void bind(MusicItem musicItem){
            Glide.with(itemView.getContext())
                    .load(musicItem.getMusicImage())
                    .into(MusicImage);

            MusicName.setText(musicItem.getMusicName());
            MusicSinger.setText(musicItem.getArtistName());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition(); // 确保使用正确的方法获取项索引

            if (position != RecyclerView.NO_POSITION) {
                MusicHolder.setPlayListId("recommend");
                // 动画
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                        .withEndAction(() -> {
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100)
                                    .withEndAction(() -> v.setEnabled(true)) // 动画完成后重新启用点击
                                    .start();
                            // 加载音乐
                            musicService.LoadMusic(position+1);
                            MusicHolder.setPosition(position+1);
                            Log.d("音乐点击位置", String.valueOf(position));
                        })
                        .start();
            }
        }
    }
}
