package com.wyc.cloudapp.dialog.CustomizationView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

import com.wyc.cloudapp.utils.Utils;

public class BasketView extends androidx.appcompat.widget.AppCompatImageView {
    private float mNumber;
    private int mWidth,mHeight;
    private final Paint mPaint;
    private float mDrawableStartScale = 1.0f,mScaleStep = 0.02f;
    private final Rect mTextBounds = new Rect();
    private final Context mContext;
    public BasketView(Context context) {
        this(context,null,0);
    }

    public BasketView(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public BasketView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
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

        float round = Utils.dpToPx(mContext,3),padding = Utils.dpToPx(mContext,8);
        dx = mWidth - w - padding;
        dy = 0;

        mPaint.setColor(Color.RED);
        canvas.drawRoundRect(dx,dy,dx + w + padding,dy + h + padding,round,round,mPaint);

        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(3);
        padding /= 2 ;
        canvas.drawText(text,dx + padding  ,dy + h + padding,mPaint);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.restore();
    }

    public void update(float num){
        mNumber = num;
        int _color = Color.BLUE;
        if (Utils.equalDouble(num,0.0)){
            _color = Color.GRAY;
        }

        setDrawingCacheEnabled(true);
        final Bitmap bitmap = getDrawingCache();
        final int w = bitmap.getWidth(),h = bitmap.getHeight();
        final int[] pixels = new int[w * h];
        bitmap.getPixels(pixels,0,w,0,0,w,h);

        setDrawingCacheEnabled(false);
        for (int i = 0;i < h;i ++){
            for (int j = 0;j < w;j ++){
                final int color = pixels[i * w + j],alpha = color & 0xFF000000;
                if (color == ((Color.RED & 0x00FFFFFF) | alpha) || color == ((Color.WHITE & 0x00FFFFFF) | alpha)){
                    pixels[i * w + j] = ((Color.WHITE & 0x00FFFFFF) | alpha);
                }else{
                    pixels[i * w + j] = ((_color & 0x00FFFFFF) | alpha);
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
