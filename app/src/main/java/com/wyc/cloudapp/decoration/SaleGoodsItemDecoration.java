package com.wyc.cloudapp.decoration;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractSaleGoodsAdapter;
import com.wyc.cloudapp.application.CustomApplication;

public final class SaleGoodsItemDecoration extends LinearItemDecoration {
    public SaleGoodsItemDecoration(int color){
        super(color);
    }
    @Override
    public void onDrawOver(@NonNull Canvas c,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        final AbstractSaleGoodsAdapter adapter = (AbstractSaleGoodsAdapter) parent.getAdapter();
        final Context context = parent.getContext();
        if (adapter != null && adapter.getSingleRefundStatus()){
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.RED);

            final int p_w = parent.getMeasuredWidth(),p_h = parent.getMeasuredHeight();

            final String sz = context.getString(R.string.single_refund_sz);
            final Rect bounds = new Rect();
            paint.setTextSize(context.getResources().getDimension(R.dimen.font_size_18));
            paint.getTextBounds(sz,0,sz.length(),bounds);
            final int t_w = bounds.width(),t_h = bounds.height();

            final int w = t_w << 1,h = t_h * 3;

            final int left = (p_w - w) / 2,top = (p_h - h) / 2;
            final Rect rect = new Rect(left,top,w + left ,h + top);

            c.drawText(sz,rect.left + ((w - t_w) >> 1),rect.top + h - t_h,paint);

            paint.setStyle(Paint.Style.STROKE);
            c.drawRect(rect,paint);
        }

        if (CustomApplication.isPracticeMode()){
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.BLUE);

            final int p_w = parent.getMeasuredWidth(),p_h = parent.getMeasuredHeight();

            final String sz = context.getString(R.string.practice);
            final Rect bounds = new Rect();
            paint.setTextSize(context.getResources().getDimension(R.dimen.font_size_18));
            paint.getTextBounds(sz,0,sz.length(),bounds);
            final int t_w = bounds.width(),t_h = bounds.height();

            final int w = t_w << 1,h = t_h * 3;

            final int left = (p_w - w) / 2,top = p_h - h - 8;
            final Rect rect = new Rect(left,top,w + left ,h + top);

            c.drawText(sz,rect.left + ((w - t_w) >> 1),rect.top + h - t_h,paint);

            paint.setStrokeWidth(3);
            paint.setStyle(Paint.Style.STROKE);
            c.drawRect(rect,paint);
        }
    }
}
