package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.utils.Utils;

public class SaleGoodsItemDecoration extends SuperItemDecoration {
    public SaleGoodsItemDecoration(int color){
        super(color);
    }
    @Override
    public void onDrawOver(@NonNull Canvas c,@NonNull RecyclerView parent,@NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        final SaleGoodsAdapter adapter = (SaleGoodsAdapter) parent.getAdapter();
        final Context context = parent.getContext();
        if (adapter != null && adapter.getSingle()){
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            paint.setTextSize(context.getResources().getDimension(R.dimen.font_size_18));
            paint.setStyle(Paint.Style.STROKE);
            final Rect rect = new Rect();
            rect.set(parent.getWidth() /2,parent.getHeight() / 2, Utils.dpToPx(context,48),parent.getHeight() / 2 + Utils.dpToPx(context,48));
            c.drawRect(rect,paint);

            final String sz = "单品退货";
            float[] ints = new float[sz.length()];
            paint.getTextWidths(sz,ints);
            c.drawText(sz,(rect.left  - ints[0] * (ints.length / 2)) + rect.width() / 2,rect.bottom - rect.height() / 3,paint);
        }
    }
}
