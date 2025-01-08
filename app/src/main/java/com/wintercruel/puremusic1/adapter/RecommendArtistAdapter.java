package com.wintercruel.puremusic1.adapter;

import static com.wintercruel.puremusic1.MainActivity.SelectPage;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.media3.common.C;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.wintercruel.puremusic1.MainActivity;
import com.wintercruel.puremusic1.R;
import com.wintercruel.puremusic1.items.ArtistItem;

import java.util.List;

public class RecommendArtistAdapter extends RecyclerView.Adapter<RecommendArtistAdapter.ViewHolder>{
    private Context mContext;
    private List<ArtistItem> mArtistList;

    public RecommendArtistAdapter(Context context, List<ArtistItem> artistList) {
        mContext = context;
        mArtistList = artistList;
    }


    @NonNull
    @Override
    public RecommendArtistAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = View.inflate(parent.getContext(), R.layout.recommend_artist, null);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendArtistAdapter.ViewHolder holder, int position) {
        holder.bind(mArtistList.get(position));
    }

    @Override
    public int getItemCount() {
        return mArtistList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private ImageView mImageView;
        private TextView mTextView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mImageView=itemView.findViewById(R.id.recommend_artist_image);
            mTextView=itemView.findViewById(R.id.recommend_artist_name);
            itemView.setOnClickListener(this);
        }

        public void bind(ArtistItem item){
            mTextView.setText(item.getName());
            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(R.drawable.my_like_image_show)
                    .into(mImageView);
        }

        @Override
        public void onClick(View v) {

            int position = getAdapterPosition();
            if(position !=RecyclerView.NO_POSITION){

//                String finalPlayListId = PlayListId;
                v.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                        .withEndAction(()->{
                            v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                            SelectPage(5);
                        }).start();

            }
        }
    }
}
