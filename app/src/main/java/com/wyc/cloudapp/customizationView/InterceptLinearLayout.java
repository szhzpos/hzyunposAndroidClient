package com.wyc.cloudapp.customizationView;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class InterceptLinearLayout extends ItemPaddingLinearLayout {
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

    public void setClickListener(final View.OnClickListener listener){
        setChildrenClickListener(listener);
    }
    private void setChildrenClickListener(final View.OnClickListener listener){
        final int counts = getChildCount();
        for (int i = 0;i < counts; i ++){
            final View btn = getChildAt(i);
            if (btn.getVisibility() == GONE)continue;
            btn.setOnClickListener(listener);
        }
    }
}
