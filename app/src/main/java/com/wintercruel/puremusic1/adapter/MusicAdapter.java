package com.wintercruel.puremusic1.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.C;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.wintercruel.puremusic1.MainActivity;
import com.wintercruel.puremusic1.Music;
import com.wintercruel.puremusic1.R;
import com.wintercruel.puremusic1.entity.MusicHolder;
import com.wintercruel.puremusic1.items.MusicItem;
import com.wintercruel.puremusic1.service.MyMusicService;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> {

    private Context mContext;
    private List<MusicItem> mMusicItems;
    private final MyMusicService musicService = MainActivity.GetBindService();


    public MusicAdapter(final Context context, final List<MusicItem> music) {
        mContext = context;
        mMusicItems = music;
    }

    @NonNull
    @Override
    public MusicAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.music_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicAdapter.ViewHolder holder, int position) {
        holder.bind(mMusicItems.get(position));

    }


    @Override
    public int getItemCount() {
        return mMusicItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private ImageView MusicImage;
        private TextView MusicName;
        private TextView ArtistName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            MusicImage = itemView.findViewById(R.id.MusicListImage);
            MusicName = itemView.findViewById(R.id.MusicListName);
            ArtistName = itemView.findViewById(R.id.MusicListArtistName);

            // 为 itemView 设置点击监听器
            itemView.setOnClickListener(this);
        }

        public void bind(MusicItem musicItem) {
            Glide.with(itemView.getContext())
                    .load(musicItem.getMusicImage())
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(40))
                            .placeholder(R.drawable.music_item)) // 添加占位符
                    .into(MusicImage);

            MusicName.setText(musicItem.getMusicName());
            ArtistName.setText(musicItem.getArtistName());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition(); // 确保使用正确的方法获取项索引

            if (position != RecyclerView.NO_POSITION) {
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

