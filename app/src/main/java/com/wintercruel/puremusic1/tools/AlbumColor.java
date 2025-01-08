package com.wintercruel.puremusic1.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;

import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.List;

public class AlbumColor {

    /**
     * 提取网络图片的主要颜色。
     *
     * @param imageUrl 图片的 URL 地址
     * @param context  上下文对象
     * @param callback 提取完成后的回调接口
     */
    public void extractColorsFromImage(String imageUrl, Context context, OnColorsExtractedCallback callback) {
        Glide.with(context)
                .asBitmap()
                .load(imageUrl)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        extractColorsFromBitmap(resource, callback);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        // 可处理加载清理逻辑（如释放资源），这里暂时留空
                    }
                });
    }

    /**
     * 从 Bitmap 提取主要颜色。
     *
     * @param bitmap   图片的 Bitmap 对象
     * @param callback 提取完成后的回调接口
     */
    private void extractColorsFromBitmap(Bitmap bitmap, OnColorsExtractedCallback callback) {
        Palette.from(bitmap).generate(new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(@Nullable Palette palette) {
                if (palette != null) {
                    // 提取主色和鲜艳色
                    int dominantColor = palette.getDominantColor(Color.BLACK);
                    int vibrantColor = palette.getVibrantColor(Color.BLACK);

                    // 转换为十六进制颜色字符串
                    String dominantColorHex = String.format("#%06X", (0xFFFFFF & dominantColor));
                    String vibrantColorHex = String.format("#%06X", (0xFFFFFF & vibrantColor));

                    // 通过回调返回颜色值
                    callback.onColorsExtracted(new String[]{dominantColorHex, vibrantColorHex});
                } else {
                    // 如果无法生成 Palette，则返回空数组
                    callback.onColorsExtracted(new String[0]);
                }
            }
        });
    }

    /**
     * 提取颜色完成后的回调接口。
     */
    public interface OnColorsExtractedCallback {
        void onColorsExtracted(String[] colors);
    }
}
