package com.wyc.cloudapp.utils;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;

public class DrawableUtil {
    public static GradientDrawable createDrawable(final float[] corners,int color,int borderWidth,int borderColor){
        final GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadii(corners);
        drawable.setStroke(borderWidth,borderColor);
        drawable.setColor(color);
        return drawable;
    }

    public static StateListDrawable createStateDrawable(final Drawable press, final Drawable normal){
        final StateListDrawable drawable = new StateListDrawable();
        //按下
        drawable.addState(new int[]{android.R.attr.state_pressed}, press);
        //正常
        drawable.addState(new int[]{}, normal);
        return drawable;
    }

    public static ColorStateList createColorStateList(int normal_color, int pressed_color, int focused_color, int unable_color) {
        final int[] colors = new int[] { pressed_color, focused_color, normal_color, focused_color, unable_color, normal_color };
        final int[][] states = new int[6][];
        states[0] = new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled };
        states[1] = new int[] { android.R.attr.state_enabled, android.R.attr.state_focused };
        states[2] = new int[] { android.R.attr.state_enabled };
        states[3] = new int[] { android.R.attr.state_focused };
        states[4] = new int[] { android.R.attr.state_window_focused };
        states[5] = new int[] {};
        return new ColorStateList(states, colors);
    }
}
