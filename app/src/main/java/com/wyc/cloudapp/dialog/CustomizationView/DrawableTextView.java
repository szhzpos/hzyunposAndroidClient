package com.wyc.cloudapp.dialog.CustomizationView;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import com.wyc.cloudapp.R;

public final class DrawableTextView extends androidx.appcompat.widget.AppCompatTextView{
    private final Context mContext;
    private KeyboardView.OnCurrentFocusListener mCurrentFocusListener;
    private int mVerSpacing = 12;
    private final Drawable[] mDrawables;
    private final Paint mPaint = new Paint();;
    private final Rect font_bounds = new Rect();
    private int mViewH,mViewW;
    public DrawableTextView(Context context) {
        this(context, null);
    }

    public DrawableTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DrawableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.DrawableTextViewTopMargin, 0, 0);
        int indexCount = typedArray.getIndexCount();
        for (int i = 0; i < indexCount; i++) {
            int index = typedArray.getIndex(i);
            if (index == R.styleable.DrawableTextViewTopMargin_verSpacing) {
                mVerSpacing = typedArray.getInteger(index, 18);
            }
        }
        mDrawables = getCompoundDrawables();

        mPaint.setColor(getCurrentTextColor());
        mPaint.setTextSize(getTextSize());


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
        int drawable_height = 0;
        if (drawables != null){
            final Drawable topDrawable = drawables[1];
            if (topDrawable != null){
                drawable_height = topDrawable.getIntrinsicHeight();
                final String text = getText().toString();
                mPaint.getTextBounds(text,0,text.length(),font_bounds);

                int font_h = font_bounds.height(),font_w = font_bounds.width();
                int h = drawable_height + mVerSpacing + font_h;

                int top = (mViewH - h) / 2;

                final Rect rect = topDrawable.getBounds();
                rect.set(rect.left,rect.top + top,rect.right,rect.bottom + top);
                topDrawable.setBounds(rect);
                topDrawable.draw(canvas);


                canvas.drawText(text,(mViewW - font_w) >> 1,rect.bottom + mVerSpacing + top,mPaint);
            }
        }
        //super.onDraw(canvas);
    }
}