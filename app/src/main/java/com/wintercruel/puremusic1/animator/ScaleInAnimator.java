package com.wintercruel.puremusic1.animator;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;

public class ScaleInAnimator extends DefaultItemAnimator {
    @Override
    public boolean animateAdd(@NonNull RecyclerView.ViewHolder holder) {
        // 将 View 的缩放设置为 0（不可见）
        holder.itemView.setScaleX(0f);
        holder.itemView.setScaleY(0f);

        // 使用 ObjectAnimator 执行缩放动画
        Animator scaleX = ObjectAnimator.ofFloat(holder.itemView, "scaleX", 0f, 1f);
        Animator scaleY = ObjectAnimator.ofFloat(holder.itemView, "scaleY", 0f, 1f);

        // 动画时长，可以根据需求调整
        scaleX.setDuration(300);
        scaleY.setDuration(300);

        // 同时播放两个动画
        scaleX.start();
        scaleY.start();

        return super.animateAdd(holder);
    }

    @Override
    public boolean animateRemove(@NonNull RecyclerView.ViewHolder holder) {
        return super.animateRemove(holder);
    }
}
