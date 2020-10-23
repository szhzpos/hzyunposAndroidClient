package com.wyc.cloudapp.dialog.CustomizationView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.wyc.cloudapp.R;

public final class DrawableCenterTextView extends androidx.appcompat.widget.AppCompatTextView{
    private KeyboardView.OnCurrentFocusListener mCurrentFocusListener;
    private int mVerSpacing = 12;
    private final Drawable[] mDrawables;
    private final Paint mPaint = new Paint();;
    private final Rect font_bounds = new Rect();
    private int mViewH,mViewW;
    private float mDrawableStartScale = 1.0f,mScaleStep = 0.02f;
    private boolean isClick = false;
    public DrawableCenterTextView(Context context) {
        this(context, null);
    }

    public DrawableCenterTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawableCenterTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DrawableTextViewTopMargin, 0, 0);
        int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = typedArray.getIndex(i);
            if (index == R.styleable.DrawableTextViewTopMargin_verSpacing) {
                mVerSpacing = typedArray.getInteger(index, 28);
            }
        }
        mDrawables = getCompoundDrawables();

        mPaint.setAntiAlias(true);

        typedArray.recycle();
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
        final Drawable[] drawables = mDrawables;
        int drawable_height = 0,drawable_width = 0;
        if (drawables != null){
            final Drawable topDrawable = drawables[1];
            if (topDrawable != null){
                mPaint.setColor(getCurrentTextColor());
                mPaint.setTextSize(getTextSize());

                drawable_height = topDrawable.getIntrinsicHeight();
                drawable_width = topDrawable.getIntrinsicWidth();

                final String text = getText().toString();
                mPaint.getTextBounds(text,0,text.length(),font_bounds);

                int font_h = font_bounds.height(),font_w = font_bounds.width();
                int h = drawable_height + mVerSpacing + font_h;

                int top = (mViewH - h) / 2,left = (mViewW - drawable_width) >> 1;


                final Rect rect = topDrawable.getBounds();
                rect.set(rect.left + left,rect.top + top,rect.right + left,rect.bottom + top);
                topDrawable.setBounds(rect);
                if (isClick){
                    canvas.save();
                    canvas.scale(mDrawableStartScale,mDrawableStartScale);
                    topDrawable.draw(canvas);
                    canvas.restore();
                }else {
                    topDrawable.draw(canvas);
                }

                canvas.drawText(text,(mViewW - font_w) >> 1,h + (font_bounds.height() >> 1),mPaint);
            }else
                super.onDraw(canvas);
        }else
          super.onDraw(canvas);
    }
    public void setClick(boolean b){
        if (b) {
            updateDrawableStartScale();
        }else{
            mDrawableStartScale = 1.0f;
            removeCallbacks(updateRunnable);
        }

        isClick = b;
    }
    private void updateDrawableStartScale(){
        if (mDrawableStartScale <=  0.5f)mScaleStep = 0.02f;
        if (mDrawableStartScale >=  1.0f)mScaleStep = -0.02f;

        mDrawableStartScale += mScaleStep;

        if (mDrawableStartScale < 1.0f){
            postDelayed(updateRunnable,5);
        }

        invalidate();
    }
    private final Runnable updateRunnable = this::updateDrawableStartScale;
}