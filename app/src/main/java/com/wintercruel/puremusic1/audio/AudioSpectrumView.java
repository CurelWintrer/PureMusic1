package com.wintercruel.puremusic1.audio;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import java.util.Random;

public class AudioSpectrumView extends View {

    private Paint paint;  // 绘制频谱的画笔
    private int barCount = 5;  // 频谱条的数量
    private float[] barHeights;  // 每个频谱条的高度
    private float barWidth;  // 每个频谱条的宽度
    private ValueAnimator animator;  // 动画控制
    private Random random;  // 用于生成随机高度

    public AudioSpectrumView(Context context) {
        super(context);
        init();
    }

    public AudioSpectrumView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AudioSpectrumView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // 初始化画笔
        paint = new Paint();
        paint.setColor(Color.GREEN);  // 设置频谱条的颜色
        paint.setStyle(Paint.Style.FILL);  // 填充样式
        paint.setAntiAlias(true);  // 抗锯齿

        random = new Random();  // 随机数生成器

        barHeights = new float[barCount];  // 初始化每个频谱条的高度

        // 动画控制，每秒刷新30次
        animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(1000 / 30);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(animation -> {
            // 每帧更新频谱条的高度
            for (int i = 0; i < barCount; i++) {
                barHeights[i] = random.nextFloat() * getHeight() * 0.8f;  // 随机高度，最高不超过视图高度的80%
            }
            invalidate();  // 请求重新绘制
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        barWidth = (float) w*0.8f / barCount;  // 每个频谱条的宽度等于总宽度除以条数
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < barCount; i++) {
            float left = i * barWidth;  // 每个条形的左边界
            float top = getHeight() - barHeights[i];  // 条形的高度，从底部向上
            float right = left + barWidth * 0.8f;  // 频谱条之间有间隙
            float bottom = getHeight();  // 条形的底部与视图底部对齐

            // 绘制矩形条
            canvas.drawRect(left, top, right, bottom, paint);
        }
    }

    // 启动频谱动画
    public void startAnimation() {
        if (!animator.isRunning()) {
            animator.start();
        }
    }

    // 停止频谱动画
    public void stopAnimation() {
        if (animator.isRunning()) {
            animator.end();
        }
    }
}
