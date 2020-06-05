package com.wyc.cloudapp.adapter;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;
import android.widget.Adapter;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.logger.Logger;

public class SaleGoodsItemDecoration extends SuperItemDecoration {
    private SaleGoodsViewAdapter mSaleGoodsViewAdapter;
    public SaleGoodsItemDecoration(int color,SaleGoodsViewAdapter adapter){
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        mSaleGoodsViewAdapter = adapter;
    }
    @Override
    public void onDrawOver(@NonNull Canvas c,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);

        if (mSaleGoodsViewAdapter.getSingle()){
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            paint.setTextSize(18);
            paint.setStyle(Paint.Style.STROKE);
            final Rect rect = new Rect();
            rect.set(parent.getWidth() /2,parent.getHeight() / 2,48,parent.getHeight() / 2 + 48);
            c.drawRect(rect,paint);

            final String sz = "单品退货";
            float[] ints = new float[sz.length()];
            paint.getTextWidths(sz,ints);
            c.drawText(sz,(rect.left  - ints[0] * (ints.length / 2)) + rect.width() / 2,rect.bottom - rect.height() / 3,paint);
        }

        final RecyclerView.LayoutManager manager = parent.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            if (((LinearLayoutManager) manager).getOrientation() == LinearLayoutManager.VERTICAL) {
                drawVertical(c, parent);
            } else {
                drawHorizontal(c, parent);
            }
        }
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect,@NonNull View view,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof LinearLayoutManager) {
            final RecyclerView.Adapter adapter = parent.getAdapter();
            if (null != adapter){
                if (((LinearLayoutManager) layoutManager).getOrientation() == LinearLayoutManager.HORIZONTAL) {
                    if (parent.getChildAdapterPosition(view) != adapter.getItemCount() - 1)
                        outRect.set(0, 0,mSpace, 0);
                } else {
                    if (parent.getChildAdapterPosition(view) != adapter.getItemCount() - 1)
                        outRect.set(0, 0, 0,mSpace);
                }
            }
        }
    }

    @Override
    protected void drawVertical(final Canvas c, final RecyclerView parent) {
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

    protected void drawHorizontal(final Canvas c,final RecyclerView parent) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + params.rightMargin + Math.round(child.getTranslationX());
            final int right =(left + mSpace);
            c.drawRect(left, bottom + 1, right, bottom, mPaint);
        }
    }
}
