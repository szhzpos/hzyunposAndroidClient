package com.wyc.cloudapp.adapter;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GoodsInfoItemDecoration extends SuperItemDecoration {
    public GoodsInfoItemDecoration(int color){
        super(color);
    }
    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

    }

    protected void drawVerticalPadding(Canvas c, RecyclerView parent) {

    }

    protected void drawHorizontalPadding(Canvas c, RecyclerView parent) {

    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int size = mSpace >> 1;
        outRect.set(size, size,size, size);
    }
}