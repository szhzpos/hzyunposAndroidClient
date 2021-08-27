package com.wyc.cloudapp.CustomizationView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

public class ItemPaddingLinearLayout extends LinearLayout {
    private boolean ignore = false;
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

        final int t_w = mTextBounds.width(),t_h = mTextBounds.height(),w = t_w << 1,h = t_h * 3,left = (width - w) / 2,top = (height - h) / 3;
        final Rect rect = new Rect(left,top,w + left ,h + top);

        canvas.save();
        canvas.rotate(15,width >> 1,height >> 1);
        canvas.drawText(mCentreLabel,rect.left + ((w - t_w) >> 1),rect.top + h - t_h,mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(rect,mPaint);
        canvas.restore();
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

    public ItemPaddingLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ItemPaddingLinearLayout, 0, 0);
        final int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = typedArray.getIndex(i);
            if (index == R.styleable.ItemPaddingLinearLayout_ItemPadding) {
                init(typedArray.getDimension(index, 2));
            }
        }

    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent ev){
        if (ignore && !(ev.getAction() == MotionEvent.ACTION_MOVE || ev.getAction() == MotionEvent.ACTION_DOWN)){//过滤滑动
            return true;
        }else
            return super.onInterceptTouchEvent(ev);
    }

    public void setIgnore(boolean b){
        ignore = b;
    }

    private void init(float padding){
        final GradientDrawable drawable = new GradientDrawable();
        if (getOrientation() == HORIZONTAL){
            drawable.setSize((int) padding,0);
        }else
            drawable.setSize(0,(int) padding);

        setDividerDrawable(drawable);
        setShowDividers(SHOW_DIVIDER_MIDDLE);
    }
}
