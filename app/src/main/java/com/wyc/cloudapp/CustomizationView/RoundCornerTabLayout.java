package com.wyc.cloudapp.CustomizationView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.tabs.TabLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.utils.DrawableUtil;
import com.wyc.cloudapp.utils.Utils;

import java.lang.reflect.Field;

public final class RoundCornerTabLayout extends TabLayout {
    private Drawable mLeft,mRight,mMiddle;
    private final int mColor, mBorderWidth;
    public RoundCornerTabLayout(Context context) {
        this(context,null);
    }

    @SuppressLint("ResourceType")
    public RoundCornerTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.RoundCornerTabLayout, 0, 0);
        mColor = typedArray.getColor( 1,getResources().getColor(R.color.blue,context.getTheme()));
        mBorderWidth = (int) typedArray.getDimension(0,1);

        try {
            Field field = getClass().getSuperclass().getDeclaredField("slidingTabIndicator");
            field.setAccessible(true);
            final Object o = field.get(this);
            if (o instanceof LinearLayout){
                LinearLayout linearLayout = (LinearLayout) o;
                linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
                linearLayout.setDividerDrawable(ContextCompat.getDrawable(getContext(),R.drawable.tablayout_divider_vertical));
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed){
            float corner_size = getHeight() / 2.0f;
            mLeft = DrawableUtil.createDrawable(new float[]{corner_size,corner_size,0,0,0,0,corner_size,corner_size},mColor, mBorderWidth,mColor);

            mRight = DrawableUtil.createDrawable(new float[]{0,0,corner_size,corner_size,corner_size,corner_size,0,0},mColor, mBorderWidth,mColor);

            mMiddle = DrawableUtil.createDrawable(new float[]{0,0,0,0,0,0,0,0},mColor, mBorderWidth,mColor);

            setForeground(DrawableUtil.createDrawable(new float[]{corner_size,corner_size,corner_size,corner_size,corner_size,corner_size,corner_size,corner_size}
                    ,getResources().getColor(R.color.transparent,getContext().getTheme()), mBorderWidth,mColor));

            //默认左边
            setSelectedTabIndicator(mLeft);
        }
    }

    @Override
    public void selectTab(@Nullable final TabLayout.Tab tab, boolean updateIndicator){
        if (null != tab){
            int pos = tab.getPosition();
            if (pos == 0){
                setSelectedTabIndicator(mLeft);
            }else if (pos == getTabCount() - 1){
                setSelectedTabIndicator(mRight);
            }else
                setSelectedTabIndicator(mMiddle);
        }
        super.selectTab(tab,updateIndicator);
    }
}
