package com.wyc.cloudapp.decoration;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.utils.Utils;

public class LinearItemDecoration extends SuperItemDecoration {
    private final Paint mPaint;
    public LinearItemDecoration(int color) {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        /*if (-1 == color)color = 0x4DA1A1A1; The white decimal value is -1*/
        mPaint.setColor(color);
    }
    public LinearItemDecoration(int color,int space){
        this(color);
        mSpace = space;
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final RecyclerView.LayoutManager manager = parent.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            if (((LinearLayoutManager) manager).getOrientation() == LinearLayoutManager.VERTICAL) {
                drawVerticalPadding(c, parent);
            } else {
                drawHorizontalPadding(c, parent);
            }
        }
    }
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            if (mSpace == -1)mSpace = Utils.dpToPx(view.getContext(),5);
            if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                outRect.set(0, 0,mSpace, 0);
            } else {
                outRect.set(0, 0, 0,mSpace);
            }
        }
    }
    protected void drawVerticalPadding(final Canvas c, final RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin + Math.round(child.getTranslationY());
            final int bottom = (top + mSpace);
            c.drawLine(left,bottom,right,bottom,mPaint);
        }
    }

    protected void drawHorizontalPadding(final Canvas c, final RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + params.rightMargin + Math.round(child.getTranslationX());
            final int right =(left + mSpace);
            c.drawLine(right, top, right, child.getBottom(), mPaint);
        }
    }
}
