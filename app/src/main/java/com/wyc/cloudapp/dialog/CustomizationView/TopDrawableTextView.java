package com.wyc.cloudapp.dialog.CustomizationView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.print.PrintUtilsToBitbmp;

public final class TopDrawableTextView extends androidx.appcompat.widget.AppCompatTextView{
    private float mVerSpacing;
    private final Drawable[] mDrawables;
    private Bitmap mBitmap;
    private final Paint mPaint = new Paint();;
    private final Rect font_bounds = new Rect();
    private int mViewH,mViewW;
    private float mDrawableStartScale = 1.0f,mScaleStep = 0.02f;
    float mDrawRotate = 0;
    private boolean mAnimationFlag = false;
    private int mAnimType = 0;//默认缩放动画
    private int mSelectTextColor;
    public TopDrawableTextView(Context context) {
        this(context, null);
    }

    public TopDrawableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopDrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final int default_color = getResources().getColor(R.color.mobile_fun_view_click,null);

        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DrawableTextViewTopMargin, 0, 0);
        final int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = typedArray.getIndex(i);
            if (index == R.styleable.DrawableTextViewTopMargin_verSpacing) {
                mVerSpacing = typedArray.getDimension(index, 28);
            }else if (index == R.styleable.DrawableTextViewTopMargin_animType){
                mAnimType = typedArray.getInt(index, 0);
            }else if (index == R.styleable.DrawableTextViewTopMargin_selectTextColor){
                mSelectTextColor = typedArray.getColor(index,default_color);
            }
        }
        if (mSelectTextColor == 0)mSelectTextColor = default_color;

        mDrawables = getCompoundDrawables();

        mPaint.setAntiAlias(true);

        typedArray.recycle();
    }

    private BitmapDrawable getTopBitmapDrawable(){
        final Drawable[] drawables = mDrawables;
        if (drawables != null){
            final Drawable drawable = drawables[1];
            if (drawable instanceof BitmapDrawable)return (BitmapDrawable)drawable;
        }
        return null;
    }

    @Override
    public void onMeasure(int widthMeaSpec, int heightMeaSpec) {
        super.onMeasure(widthMeaSpec, heightMeaSpec);
        mViewH = getMeasuredHeight();
        mViewW = getMeasuredWidth();
    }

    @Override
    public void onLayout(boolean change, int left, int top, int right, int bottom) {
        super.onLayout(change, left, top, right, bottom);
    }

    @Override
    public void onDraw(Canvas canvas) {
        final Drawable topDrawable = getTopBitmapDrawable();
        if (topDrawable != null){
            final Rect rect = topDrawable.getBounds();
            final int bound_height = rect.height(),bound_width = rect.width();

            final String text = getText().toString();
            mPaint.setTextSize(getTextSize());
            mPaint.getTextBounds(text,0,text.length(),font_bounds);

            int font_h = font_bounds.height(),font_w = font_bounds.width();
            int h = (int) (bound_height + mVerSpacing + font_h);

            int top = (mViewH - h) >> 1,left = (mViewW - bound_width) >> 1;

            if (mAnimationFlag){
                mPaint.setColor(mSelectTextColor);
                final int dx = mViewW >> 1,dy = (bound_height + (top << 1)) >> 1;

                canvas.save();
                canvas.translate(dx,dy);
                if (mAnimType == 0) {
                    canvas.scale(mDrawableStartScale, mDrawableStartScale);
                }else if (mAnimType == 1){
                    canvas.rotate(mDrawRotate);
                }
                canvas.translate(-dx,-dy);
                canvas.drawBitmap(mBitmap,dx - (mBitmap.getWidth() >> 1),dy- (mBitmap.getHeight() >> 1),mPaint);
                canvas.restore();
            }else {
                mPaint.setColor(getCurrentTextColor());
                rect.set(rect.left + left,rect.top + top,rect.right + left,rect.bottom + top);
                topDrawable.draw(canvas);
            }
            canvas.drawText(text,(mViewW - font_w) >> 1,h + (font_bounds.height() >> 1),mPaint);
        }else
            super.onDraw(canvas);
    }
    public void triggerAnimation(boolean b){
        final BitmapDrawable drawable = getTopBitmapDrawable();
        if (drawable != null){
            final Bitmap bitmap = drawable.getBitmap();
            if (b) {
                final int w = bitmap.getWidth(),h = bitmap.getHeight(),font_color = mSelectTextColor;
                final int[] pixels = new int[w * h];

                bitmap.getPixels(pixels,0,w,0,0,w,h);
                for (int i = 0;i < h;i ++){
                    for (int j = 0;j < w;j ++){
                        final int color = pixels[i * w + j];
                        if (color != 0){
                            final int a = (color >> 24) & 0xff,r = (font_color >> 16) & 0xff,g = (font_color >> 8) & 0xff,bc = font_color & 0xff;
                            pixels[i * w + j] = PrintUtilsToBitbmp.getPixel(a,r,g,bc);
                        }
                    }
                }
                mBitmap = Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888);
                updateDrawableStartScale();
            }else{
                mBitmap = null;
                mDrawableStartScale = 1.0f;
                mDrawRotate = 0;
                removeCallbacks(updateRunnable);
            }
            mAnimationFlag = b;
        }
    }
    private void updateDrawableStartScale(){
        if (mAnimType == 1){
            mDrawRotate += 9;
            if (mDrawRotate < 360)postDelayed(updateRunnable,5);
        }else {
            if (mDrawableStartScale <=  0.5f)mScaleStep = 0.05f;
            if (mDrawableStartScale >=  1.0f)mScaleStep = -0.05f;

            mDrawableStartScale += mScaleStep;

            if (mDrawableStartScale < 1.0f){
                postDelayed(updateRunnable,5);
            }
        }
        invalidate();
    }

    private final Runnable updateRunnable = this::updateDrawableStartScale;
}