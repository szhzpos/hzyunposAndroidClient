package com.wyc.cloudapp.dialog.CustomizationView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.widget.Button;

import com.wyc.cloudapp.R;

@SuppressLint("AppCompatCustomView")
public class TmpOrderButton extends Button {
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
        mPath.moveTo(50, 0);
        mPath.arcTo(80,0,85,5,-90,90,false);
        mPath.lineTo(85, 40);
        mPath.close();
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
        canvas.drawPath(mPath, mPaint);

        mPaint.setColor(mContext.getColor(R.color.white));
        canvas.drawText(String.valueOf(mOrderNum),getMeasuredWidth() - 16,15,mPaint);
    }

    public void setNum(int num){
        mOrderNum = num;
        invalidate();
    }

}
