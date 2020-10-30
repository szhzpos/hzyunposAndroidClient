package com.wyc.cloudapp.dialog.CustomizationView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.wyc.cloudapp.print.PrintUtilsToBitbmp;
import com.wyc.cloudapp.utils.Utils;

public class BasketView extends androidx.appcompat.widget.AppCompatImageView {
    private float mNumber;
    private int mWidth,mHeight;
    private final Paint mPaint;
    private float mDrawableStartScale = 1.0f,mScaleStep = 0.02f;
    private final Rect mTextBounds = new Rect();
    private Bitmap mOldBitmap;
    public BasketView(Context context) {
        this(context,null,0);
    }

    public BasketView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public BasketView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom){
        super.onLayout(changed, left, top, right, bottom);
        setDrawingCacheEnabled(true);
        mOldBitmap= getDrawingCache();
        setDrawingCacheEnabled(false);
    }

    @Override
    public void onDraw(Canvas canvas){
        float dx = mWidth >> 1,dy = mHeight >> 1;
        canvas.save();

        canvas.translate(dx,dy);
        canvas.scale(mDrawableStartScale, mDrawableStartScale);
        canvas.translate(-dx,-dy);

        super.onDraw(canvas);

        final String text = String.valueOf(mNumber);
        mPaint.setTextSize(Utils.sp2px(getContext(),14));
        mPaint.getTextBounds(text,0,text.length(),mTextBounds);

        int w = mTextBounds.width(),h = mTextBounds.height();

        float radius = Math.max(w,h) / 1.5f;
        dx = mWidth - radius;
        dy = radius;

        mPaint.setColor(Color.RED);
        canvas.drawCircle(dx,dy,radius,mPaint);

        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(2);
        canvas.drawText(text,dx - (w >> 1),dy + (h >> 1),mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.restore();
    }

    public void update(float num){
        mNumber = num;
        int font_color = Color.GRAY;
        if (!Utils.equalDouble(num,0.0)){
            font_color = Color.BLUE;
        }

        setDrawingCacheEnabled(true);
        final Bitmap bitmap = getDrawingCache();
        final int w = bitmap.getWidth(),h = bitmap.getHeight();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels,0,w,0,0,w,h);

        setDrawingCacheEnabled(false);
        for (int i = 0;i < h;i ++){
            for (int j = 0;j < w;j ++){
                final int color = pixels[i * w + j];
                if (color != 0 || color != Color.BLUE){
                    final int a = (color >> 24) & 0xff,r = (font_color >> 16) & 0xff,g = (font_color >> 8) & 0xff,bc = font_color & 0xff;
                    pixels[i * w + j] = PrintUtilsToBitbmp.getPixel(a,r,g,bc);
                }
            }
        }
        setImageBitmap(Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888));
        updateDrawableStartScale();
    }

    private void updateDrawableStartScale(){
        if (mDrawableStartScale <=  0.8f)mScaleStep = 0.08f;
        if (mDrawableStartScale >=  1.0f)mScaleStep = -0.08f;
        mDrawableStartScale += mScaleStep;
        if (mDrawableStartScale < 1.0f)postDelayed(updateRunnable,10);
        invalidate();
    }
    private final Runnable updateRunnable = this::updateDrawableStartScale;

}
