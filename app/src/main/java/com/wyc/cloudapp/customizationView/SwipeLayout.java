package com.wyc.cloudapp.customizationView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.TextView;

import com.wyc.cloudapp.R;

public class SwipeLayout extends FrameLayout {
    private OverScroller mScroller;

    private int mTouchSlop;

    private float mXDown,mXMove,mLastXMove;

    private View mContentView;

    private int mLeftBorder;

    private int mRightBorder;

    private LinearLayout mMenuLayout;

    public SwipeLayout(Context context) {
        this(context,null);
    }

    public SwipeLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr,0);
    }

    public SwipeLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initContentView(context,attrs);
        init(context);
    }

    private void initContentView(final Context context,AttributeSet attrs){
        final TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.SwipeLayout, 0, 0);
        int contentViewId = typedArray.getResourceId(R.styleable.SwipeLayout_layout,0);
        mContentView = ((FrameLayout)View.inflate(context,contentViewId,this)).getChildAt(0);
        if (mContentView == null){
            final TextView tv = new TextView(getContext());
            tv.setText(R.string.not_found_hint);
            mContentView = tv;
        }
        typedArray.recycle();
    }

    private void init(final Context context){
        mScroller = new OverScroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledPagingTouchSlop();

        mMenuLayout = new LinearLayout(context);
        mMenuLayout.setOrientation(LinearLayout.HORIZONTAL);
        final LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mMenuLayout.setLayoutParams(layoutParams);
        addView(mMenuLayout);
    }

    public void addMenuItem(final String title,View.OnClickListener listener,int color){
        final TextView tv = generateMenuView();
        tv.setText(title);
        tv.setOnClickListener(listener);
        tv.setBackgroundColor(color);
        mMenuLayout.addView(tv);
    }

    private static class MenuItem{
        public MenuItem(Builder builder){
            mTitle = builder.mTitle;
            mListener = builder.mListener;
            mBackgroundColor = builder.mBackgroundColor;
        }
        private final String mTitle;
        private final View.OnClickListener mListener;
        private final int mBackgroundColor;

        static class Builder{
            private String mTitle;
            private View.OnClickListener mListener;
            private int mBackgroundColor;

            public Builder setTitle(String mTitle) {
                this.mTitle = mTitle;
                return this;
            }

            public Builder setBackgroundColor(int mBackgroundColor) {
                this.mBackgroundColor = mBackgroundColor;
                return this;
            }

            public Builder setListener(OnClickListener mListener) {
                this.mListener = mListener;
                return this;
            }

            public MenuItem builder(){
                return new MenuItem(this);
            }
            public static Builder create(){
                return new Builder();
            }
        }
    }

    private TextView generateMenuView(){
        final TextView tv = new TextView(getContext());
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(12);
        tv.setTextColor(getResources().getColor(R.color.white,null));
        measure(MeasureSpec.UNSPECIFIED,MeasureSpec.UNSPECIFIED);
        tv.setLayoutParams(new LinearLayout.LayoutParams(getMeasuredHeightAndState(), ViewGroup.LayoutParams.MATCH_PARENT));
        return tv;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()){
            case MotionEvent.ACTION_DOWN:
                mLastXMove = mXDown = ev.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                mXMove = ev.getX();
                float diff = Math.abs(mXMove - mXDown);
                mLastXMove = mXMove;
                if (diff > mTouchSlop){
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mXDown + getScrollX() < mContentView.getRight())
                    return false;
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int moveX = 0,w = getWidth(),scrollX= getScrollX();
        switch (event.getAction()){
            case MotionEvent.ACTION_MOVE:
                mXMove = event.getX();
                moveX = (int) (mLastXMove - mXMove);
                mLastXMove = mXMove;
                if (scrollX + moveX < mLeftBorder){
                    scrollTo(mLeftBorder,0);
                    return true;
                }else if (scrollX + moveX + w > mRightBorder){
                    scrollTo(mRightBorder - w,0);
                    return true;
                }
                scrollBy(moveX,0);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (Math.abs(mLastXMove - mXDown) > 0){
                    moveX = scrollX + w;
                    int dx = 0;
                    if (moveX > mContentView.getRight() + mMenuLayout.getWidth() / 2){
                        dx = mRightBorder - moveX;
                    }else {
                        dx = mContentView.getRight() - moveX;
                    }
                    mScroller.startScroll(scrollX,0,dx,0);
                    invalidate();
                    return true;
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        int scrollX = getScrollX();
        if (direction < 0){
            return scrollX + getWidth() + getPaddingRight() < mRightBorder;
        }else {
            return scrollX > mLeftBorder;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int t, int right, int bottom) {
        if (changed){
            int contentViewWidth = mContentView.getMeasuredWidthAndState(),contentViewHeight = mContentView.getMeasuredHeightAndState();
            LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
            int start = getPaddingLeft();
            int top = getPaddingTop() + lp.topMargin ;
            mContentView.layout(start, top, start + contentViewWidth, top + contentViewHeight);

            int menuViewWidth = mMenuLayout.getMeasuredWidthAndState(),menuViewHeight = mMenuLayout.getMeasuredHeightAndState();
            lp = (LayoutParams) mMenuLayout.getLayoutParams();
            top = getPaddingTop() + lp.topMargin ;
            int parentViewWidth = getMeasuredWidthAndState();
            mMenuLayout.layout(parentViewWidth, top, parentViewWidth + menuViewWidth, top + menuViewHeight);

            mLeftBorder = 0;
            mRightBorder = mMenuLayout.getRight();
        }
    }

    public void closeRightMenu(){
        int scrollX = getScrollX();
        if (scrollX != 0)mScroller.startScroll(scrollX,0, -scrollX,0);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()){
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }
}