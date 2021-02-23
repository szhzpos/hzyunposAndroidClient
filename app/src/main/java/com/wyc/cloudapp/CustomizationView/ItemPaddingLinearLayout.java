package com.wyc.cloudapp.CustomizationView;

import android.content.Context;
import android.content.res.TypedArray;
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

public class ItemPaddingLinearLayout extends LinearLayout {
    private boolean ignore = false;
    public ItemPaddingLinearLayout(Context context) {
        this(context,null);
    }
    public ItemPaddingLinearLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
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
        if (ignore){
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
