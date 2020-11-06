package com.wyc.cloudapp.CustomizationView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatButton;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.utils.Utils;

public final class TmpOrderButton extends AppCompatButton {
    private final Paint mPaint;
    private final Path mPath;
    private int mOrderNum,mShape;
    private final Context mContext;
    public TmpOrderButton(Context context) {
        this(context,null);
    }

    public TmpOrderButton(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public TmpOrderButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPath = new Path();

        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.TmpOrderButton, 0, 0);
        int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = typedArray.getIndex(i);
            if (index == R.styleable.TmpOrderButton_shape) {
                mShape = typedArray.getInteger(index, 0);
            }
        }
        typedArray.recycle();
    }

    @Override
    public void onMeasure(int widthMeaSpec,int heightMeaSpec){
        super.onMeasure(widthMeaSpec,heightMeaSpec);
    }

    @Override
    public void onLayout(boolean change,int left,int top,int right,int bottom){
        super.onLayout(change,left,top,right,bottom);
    }

    @Override
    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        if (mShape == 1){
            drawCircle(canvas);
        }else
            drawTriangle(canvas);
    }

    private void drawTriangle(final Canvas canvas){
        mPaint.setColor(mContext.getColor(R.color.orange));

        int w = getMeasuredWidth(),h = getMeasuredHeight(),offset = Utils.dpToPx(mContext,30),arcAngle = Utils.dpToPx(mContext,5);

        mPath.moveTo(w - offset, 0);
        mPath.arcTo(w - arcAngle,0,w,arcAngle,-90,90,false);
        mPath.lineTo(w, h - Utils.dpToPx(mContext,10));
        mPath.close();
        canvas.drawPath(mPath, mPaint);

        mPaint.setColor(mContext.getColor(R.color.white));
        mPaint.setTextSize(mContext.getResources().getDimension(R.dimen.font_size_12));
        canvas.drawText(String.valueOf(mOrderNum),w - Utils.dpToPx(mContext,12),Utils.dpToPx(mContext,15),mPaint);
    }

    private void drawCircle(final Canvas canvas){
        final Context context = mContext;
        int w = getMeasuredWidth(),h = getMeasuredHeight();
        mPaint.setColor(context.getColor(R.color.orange));
        final int border = Utils.dpToPx(context,1);
        canvas.drawArc(border,border,w ,h ,-20,-140,false,mPaint);
        mPaint.setColor(context.getColor(R.color.white));
        mPaint.setTextSize(context.getResources().getDimension(R.dimen.font_size_12));
        final String num = String.valueOf(mOrderNum);
        canvas.drawText(num,(w >> 1) - (mPaint.measureText(num) / 2),Utils.dpToPx(context,14),mPaint);
    }

    public void setNum(int num){
        mOrderNum = num;
        invalidate();
    }

}
