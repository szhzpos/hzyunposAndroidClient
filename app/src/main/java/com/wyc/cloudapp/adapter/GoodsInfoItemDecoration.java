package com.wyc.cloudapp.adapter;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.logger.Logger;

public class GoodsInfoItemDecoration extends RecyclerView.ItemDecoration {
    private int mSpace;
    public GoodsInfoItemDecoration(int space){
        this.mSpace =space;
    }
    @Override
    public void onDraw(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        outRect.top = mSpace;
        outRect.left = 1;
        outRect.right = 1;
    }
}