package com.wintercruel.puremusic1.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class NonScrollableNavigationView extends NavigationView {

    public NonScrollableNavigationView(@NonNull Context context) {
        super(context);
    }

    public NonScrollableNavigationView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public NonScrollableNavigationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int expandedHeightSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandedHeightSpec); // 使用最大值
    }
}