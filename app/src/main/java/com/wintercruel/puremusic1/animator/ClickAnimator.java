package com.wintercruel.puremusic1.animator;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.view.View;

public class ClickAnimator {
    public static void ButtonClickAnimator(View v){
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(v, "scaleX", 1f, 0.6f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(v, "scaleY", 1f, 0.6f, 1f);

        // 设置动画持续时间
        scaleX.setDuration(200);
        scaleY.setDuration(200);

        // 启动动画
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleX).with(scaleY);
        animatorSet.start();
    }
}
