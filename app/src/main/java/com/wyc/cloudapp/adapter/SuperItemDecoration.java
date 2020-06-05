package com.wyc.cloudapp.adapter;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.logger.Logger;

public class SuperItemDecoration extends RecyclerView.ItemDecoration {
    int mSpace;
    Paint mPaint;
    SuperItemDecoration(){
    }
    @Override
    public void onDraw(@NonNull Canvas c,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
    }
    @Override
    public void getItemOffsets(@NonNull Rect outRect,@NonNull View view,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
    }

    protected void drawVertical(Canvas c, RecyclerView parent) {

    }

    protected void drawHorizontal(Canvas c, RecyclerView parent) {

     }

    public SuperItemDecoration setSpace(int space){
        mSpace = space;
        return this;
    }
}
