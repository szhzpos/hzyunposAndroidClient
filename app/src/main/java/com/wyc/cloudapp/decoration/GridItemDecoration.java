package com.wyc.cloudapp.decoration;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
/**
 * mSpace 垂直间隔
 * 水平间隔和垂直一样。这要求item的高度必须为确切尺寸以便计算出间隔，item的宽度应为LayoutParams.MATCH_PARENT
 * */
public class GridItemDecoration extends SuperItemDecoration {
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int size = mSpace >> 1;
        outRect.set(size, size,size, size);
    }
}