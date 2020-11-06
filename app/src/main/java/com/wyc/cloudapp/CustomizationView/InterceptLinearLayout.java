package com.wyc.cloudapp.CustomizationView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

public class InterceptLinearLayout extends LinearLayout {
    private View.OnClickListener mClickListener;
    public InterceptLinearLayout(Context context) {
        super(context);
    }

    public InterceptLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InterceptLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public InterceptLinearLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public boolean onInterceptTouchEvent (MotionEvent ev){
        if (ev.getAction() == MotionEvent.ACTION_UP){
            final int counts = getChildCount();
            for (int i = 0;i < counts; i ++){
                final View btn = getChildAt(i);
                if (isClickView(btn,ev.getX(),ev.getY())){
                    if (mClickListener != null){
                        mClickListener.onClick(btn);
                        return true;
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    boolean isClickView(final View view, float x, float y){
        if (view == null)return false;
        float v_x = view.getX(),v_y = view.getY();
        return x >= v_x && x <= v_x + view.getWidth() && y >= v_y && y <= v_y + view.getHeight();
    }
    public void setClickListener(final View.OnClickListener listener){
        mClickListener = listener;
    }
}
