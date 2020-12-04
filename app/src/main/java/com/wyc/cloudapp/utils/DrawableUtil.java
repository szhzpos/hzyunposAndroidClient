package com.wyc.cloudapp.utils;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.Shape;

import com.wyc.cloudapp.logger.Logger;

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

    public static LayerDrawable createLayerDrawable(int color,int left,int top,int right,int bottom){
        final ShapeDrawable[] shapeDrawables = new ShapeDrawable[2];
        final ShapeDrawable shapeDrawable = new ShapeDrawable(),shapeDrawable1 = new ShapeDrawable();
        shapeDrawable.getPaint().setColor(0xffffffff);
        shapeDrawable1.setPadding(left,top,right,bottom);
        shapeDrawable1.getPaint().setColor(color);

        shapeDrawables[0] = shapeDrawable1;
        shapeDrawables[1] = shapeDrawable;

        return new LayerDrawable(shapeDrawables);
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
