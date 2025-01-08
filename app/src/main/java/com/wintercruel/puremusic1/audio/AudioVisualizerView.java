package com.wintercruel.puremusic1.audio;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.OptIn;
import androidx.media3.common.util.Log;
import androidx.media3.common.util.UnstableApi;

import java.util.Arrays;

public class AudioVisualizerView extends View {

    private Paint paint;
    private float[] magnitudes; // 存储频率分量
    private int barCount = 40;  // 可视化柱的数量
    private float[] barHeights;
    private int[] gradientColors = {Color.parseColor("#FF69B4"), Color.parseColor("#00E6FF")};  // 默认的粉红色、浅蓝色、浅绿色




    public AudioVisualizerView(Context context) {
        super(context);
        init();
    }

    public AudioVisualizerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
    }

//    @OptIn(markerClass = UnstableApi.class) public void updateFrequencies(float[] magnitudes) {
//        this.magnitudes = magnitudes;
//        this.barHeights = new float[barCount];
//
//        // 计算每个频率段的平均值
//        int binSize = magnitudes.length / barCount;
//        for (int i = 0; i < barCount; i++) {
//            float sum = 0;
//            for (int j = 0; j < binSize; j++) {
//                sum += magnitudes[i * binSize + j];
//            }
//            barHeights[i] = sum / binSize;
//
//        }
//        Log.d("数据：", Arrays.toString(barHeights));
//
//        invalidate(); // 通知视图重绘
//    }

    @OptIn(markerClass = UnstableApi.class)
    public void updateFrequencies(float[] magnitudes) {
        this.magnitudes = magnitudes;
        this.barHeights = new float[barCount];

        int binSize = magnitudes.length / barCount;

        for (int i = 0; i < barCount; i++) {
            float sum = 0;

            for (int j = 0; j < binSize; j++) {
                sum += magnitudes[i * binSize + j];
            }

            barHeights[i] = sum / binSize;
        }

        // 定义最大允许高度（视图高度的 3/4）和缩放比例
        float maxAllowedHeight = getHeight();
        float scaleFactor = 0.01f; // 缩小比例，越小缩小越明显

        // 等比例缩放超出限制的柱形高度
        for (int i = 0; i < barCount; i++) {
            if (barHeights[i] > maxAllowedHeight) {
                barHeights[i] = maxAllowedHeight + (barHeights[i] - maxAllowedHeight) * scaleFactor;
            }
        }

        // 将左右两部分交换（可选逻辑）
        float[] swappedBarHeights = new float[barCount];
        int mid = barCount / 2;
        for (int i = 0; i < mid; i++) {
            swappedBarHeights[i] = barHeights[mid + i];
            swappedBarHeights[mid + i] = barHeights[i];
        }

        this.barHeights = swappedBarHeights;

        invalidate(); // 通知视图重绘
    }






    // 新增方法来设置自定义的渐变色
    public void setGradientColors(String color1, String color2) {

        if(color1==null){
            color1="#c4c4c4";
        }
        if(color2==null){
            color2="#f2f2f2";
        }

        gradientColors = new int[]{Color.parseColor(color1), Color.parseColor(color2)};

        invalidate();  // 更新视图
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (barHeights == null) return;

        float barWidth = (getWidth() - (barCount - 1) * 4) / (float) barCount; // 留下间隙

        // 创建一个渐变色从左到右覆盖整个视图
        LinearGradient gradient = new LinearGradient(0, 0, getWidth(), 0, gradientColors[0], gradientColors[1], Shader.TileMode.CLAMP);
        paint.setShader(gradient);


        for (int i = 0; i < barCount; i++) {

            float barHeight = barHeights[i] * getHeight()/2;
//            if(barHeight>getHeight()){
//                while (barHeight>getHeight()){
//                    barHeight*=0.5f;
//                }
//
//            }

            // 计算每个柱形的起始X位置，增加间隙
            float left = i * (barWidth + 5);  // 每个柱形的间隙为4px
            float right = left + barWidth;

            canvas.drawRect(left, getHeight() - barHeight, right, getHeight(), paint);
        }
    }


}




