package com.wyc.cloudapp.adapter;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

public class SuperItemDecoration extends RecyclerView.ItemDecoration {
    int mSpace;
    Paint mPaint;
    public SuperItemDecoration(){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(0x4DA1A1A1);
    }
    @Override
    public void onDraw(@NonNull Canvas c,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
        super.onDraw(c, parent, state);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
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
    public void getItemOffsets(@NonNull Rect outRect,@NonNull View view,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        final RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                outRect.set(0, 0,mSpace, 0);
            } else {
                outRect.set(0, 0, 0,mSpace);
            }
        }
    }

    @Override
    public void finalize(){
        Logger.d("SuperItemDecoration finalized");
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
            c.drawRect(left, bottom + 1, right, bottom, mPaint);
        }
    }

    protected void drawHorizontalPadding(final Canvas c, final RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + params.rightMargin + Math.round(child.getTranslationX());
            final int right =(left + mSpace);
            c.drawRect(left, top, right, bottom, mPaint);
        }
    }

    public SuperItemDecoration setSpace(int space){
        mSpace = space;
        return this;
    }

    public void registerGlobalLayoutToRecyclerView(@NonNull final View view,final float size){
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int getVerSpacing(int viewHeight,int m_height){
                double vertical_space ,vertical_counts,per_vertical_space;
                vertical_space = viewHeight % m_height;
                vertical_counts = viewHeight / m_height;
                per_vertical_space = vertical_space / (vertical_counts != 0 ? vertical_counts:1);

                return (int) Utils.formatDouble(per_vertical_space,0);
            }
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = view.getMeasuredHeight();
                if (view instanceof RecyclerView){
                    final RecyclerView recyclerView = ((RecyclerView)view);
                    if (recyclerView.getItemDecorationCount() > 0){
                        recyclerView.removeItemDecorationAt(0);
                    }
                    setSpace(getVerSpacing(height,(int) size));
                    recyclerView.addItemDecoration(SuperItemDecoration.this);
                }
            }
        });
    }
}
