package com.wyc.cloudapp.CustomizationView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.utils.Utils;

public class ItemPaddingLinearLayout extends LinearLayout {
    private boolean disable = false;
    private String mCentreLabel;
    private Paint mPaint;
    private Rect mTextBounds;
    public ItemPaddingLinearLayout(Context context) {
        this(context,null);
    }
    public ItemPaddingLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onDrawForeground(Canvas canvas) {
        super.onDrawForeground(canvas);
        if (isDrawLabel()){
            drawLabel(canvas);
        }
    }

    private boolean isDrawLabel(){
        return Utils.isNotEmpty(mCentreLabel) && mTextBounds != null && mPaint != null;
    }
    private void drawLabel(final Canvas canvas){
        int width = getMeasuredWidth(),height = getMeasuredHeight();
        final int t_w = mTextBounds.width(),t_h = mTextBounds.height(),w = t_w << 1,h = t_h * 3,left = (width - w) >> 1,top = (height - h) / 3;
        final RectF rect = new RectF(left,top,w + left ,h + top);

        canvas.save();
        matrixToRect(canvas,width,height,rect);
        canvas.drawText(mCentreLabel,left + ((w - t_w) >> 1),top + h - t_h,mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(rect,mPaint);
        canvas.restore();
    }
    private void matrixToRect(Canvas canvas,int width,int height,RectF rect){
        final float degrees = 15;
        double r_w = rect.width(),r_h = rect.height();
        double radian = (Math.PI / 180 * degrees);
        double new_w = Math.cos(radian) * r_w + Math.sin(radian) * r_h;
        double new_h = Math.sin(radian) * r_w + Math.cos(radian) * r_h;
        double scale = Math.min(width / new_w,height / new_h) - 0.05;
        if (Utils.lessDouble(scale,1.0)){
            canvas.scale((float)scale, (float)scale,rect.centerX(),rect.centerY());
        }
        canvas.rotate(degrees,rect.centerX(),rect.centerY());
    }

    public void setCentreLabel(final String label){
        if (null == label)return;
        if (mPaint == null || mTextBounds == null){
            final Paint paint = new Paint();
            paint.setAntiAlias(true);
            paint.setColor(Color.RED);
            final Rect bounds = new Rect();
            paint.setTextSize(getResources().getDimension(R.dimen.font_size_18));
            paint.getTextBounds(label,0,label.length(),bounds);
            mPaint = paint;
            mTextBounds = bounds;
        }else {
            mPaint.getTextBounds(label,0,label.length(),mTextBounds);
        }
        mCentreLabel = label;
        invalidate();
    }

    public ItemPaddingLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    @SuppressLint("ResourceType")
    public ItemPaddingLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ItemPaddingLinearLayout, 0, 0);
        init(typedArray.getDimension(0, 2),typedArray.getColor( 1,Color.TRANSPARENT));
     }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent ev){
        if (disable && !(ev.getAction() == MotionEvent.ACTION_MOVE || ev.getAction() == MotionEvent.ACTION_DOWN)){//过滤滑动
            return true;
        }else
            return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return (disable || super.dispatchKeyEvent(event));
    }

    public void setDisableEvent(boolean b){
        disable = b;
    }

    private void init(float padding,int c){
        final GradientDrawable drawable = new GradientDrawable();
        if (getOrientation() == HORIZONTAL){
            drawable.setSize((int) padding,0);
        }else
            drawable.setSize(0,(int) padding);

        drawable.setColor(c);
        setDividerDrawable(drawable);
        setShowDividers(SHOW_DIVIDER_MIDDLE);
    }
}
