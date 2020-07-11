package com.wyc.cloudapp.dialog.CustomizationView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.Button;

import androidx.appcompat.widget.AppCompatButton;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.utils.Utils;

public final class TmpOrderButton extends AppCompatButton {
    private Paint mPaint;
    private Path mPath;
    private int mOrderNum;
    private Context mContext;
    public TmpOrderButton(Context context) {
        super(context);
        mPaint = new Paint();
    }

    public TmpOrderButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mPath = new Path();

    }

    public TmpOrderButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
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
        mPaint.setColor(mContext.getColor(R.color.orange));

        int w = getMeasuredWidth(),h = getMeasuredHeight(),offset = Utils.dpToPx(mContext,30),arcAngle = Utils.dpToPx(mContext,5);
        mPath.moveTo(w - offset, 0);
        mPath.arcTo(w - arcAngle,0,w,arcAngle,-90,90,false);
        mPath.lineTo(w, h - Utils.dpToPx(mContext,10));
        mPath.close();
        canvas.drawPath(mPath, mPaint);

        mPaint.setColor(mContext.getColor(R.color.white));
        mPaint.setTextSize(Utils.dpToPx(mContext,12));
        canvas.drawText(String.valueOf(mOrderNum),getMeasuredWidth() - Utils.dpToPx(mContext,12),Utils.dpToPx(mContext,15),mPaint);
    }

    public void setNum(int num){
        mOrderNum = num;
        invalidate();
    }

}
