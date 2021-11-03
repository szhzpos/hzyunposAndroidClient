package com.wyc.cloudapp.activity.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.TextView;

import androidx.annotation.CallSuper;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.logger.Logger;

public abstract class AbstractDefinedTitleActivity extends MainActivity {
    public static final String TITLE_KEY = "TL";
    private TextView mLeft,mMiddle,mRight;
    private View mRoot;
    private float mTouchX = -1;
    private OverScroller mScroller;
    private int mStatusBarAlpha;

    private VelocityTracker mVelocityTracker;
    private int mMinimumVelocity;
    private int mMaximumVelocity;
    private int mActivePointerId = -1;

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout();
        initWindow();
        initTitle();
        initTitleText();
        initTitleClickListener();

        initVelocity();
    }

    @Override
    protected void onDestroy() {
        stopScroll();
        recycleVelocityTracker();
        super.onDestroy();
    }

    private void initVelocity(){
        final ViewConfiguration configuration = ViewConfiguration.get(this);
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    private void initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
            Logger.d("VelocityTracker has initialized.");
        }
    }

    private void recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            Logger.d("VelocityTracker has been recycled.");
            mVelocityTracker = null;
        }
    }

    private void initWindow(){
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) ;//显示状态栏
        mRoot = window.getDecorView();
        mRoot.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> updateStatusBarColor(Math.abs((float)scrollX / (float)v.getWidth())));
        mStatusBarAlpha = ((window.getStatusBarColor() >> 24) & 0xff);
    }

    private void updateStatusBarColor(float ratio){
        Window window = getWindow();
        int color = window.getStatusBarColor();
        int a = mStatusBarAlpha;
        a = (int) ((float)a * (1 - Math.sin( 2 * Math.PI * ratio)));
        color &= 0x00ffffff;
        color |= ((a << 24) & 0xff000000);
        window.setStatusBarColor(color);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (hasSlide()){
            initVelocityTrackerIfNotExists();
            switch (ev.getAction()){
                case MotionEvent.ACTION_DOWN:
                    mTouchX = ev.getX();

                    mActivePointerId = ev.getPointerId(0);
                    mVelocityTracker.addMovement(ev);
                    break;
                case MotionEvent.ACTION_MOVE:
                    mVelocityTracker.addMovement(ev);

                    if (mTouchX == -1)mTouchX = ev.getX();
                    mRoot.scrollTo((int) (mTouchX - ev.getX()),0);
                    break;
                case MotionEvent.ACTION_UP:
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialXVelocity = (int) velocityTracker.getXVelocity(mActivePointerId);

                    mActivePointerId = -1;
                    mTouchX = -1;
                    recycleVelocityTracker();

                    if (mScroller == null)mScroller = new OverScroller(this);
                    if (Math.abs(initialXVelocity) > mMinimumVelocity){
                        mScroller.fling(mRoot.getScrollX(),0,initialXVelocity ,0,0,0,0,0);
                        mRoot.scrollBy(mScroller.getCurrX(),0);
                    }

                    int w = mRoot.getWidth();
                    int scrollX = mRoot.getScrollX(),distance_scrollX = Math.abs(scrollX);
                    int dx;
                    if (distance_scrollX > (w >> 1)){
                        dx = distance_scrollX - w;
                    }else {
                        dx = distance_scrollX;
                    }
                    if (scrollX > 0)dx = -dx;

                    mScroller.startScroll(scrollX,0,dx,0);
                    startScroll();
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    protected boolean hasSlide(){
        return true;
    }

    private void startScroll(){
        if (mScroller.computeScrollOffset()){
            int cur_x = mScroller.getCurrX();
            if (Math.abs(cur_x) < mRoot.getRight()) {
                mRoot.scrollTo(cur_x,0);
                mRoot.postDelayed(this::startScroll,16);
            } else {
                mScroller.abortAnimation();
                finish();
            }
        }
    }

    private void stopScroll(){
        if (mScroller != null){
            mScroller.abortAnimation();
        }
    }

    private void initTitle(){
        mLeft = findViewById(R.id.left_title_tv);
        mMiddle = findViewById(R.id.middle_title_tv);
        mRight = findViewById(R.id.right_title_tv);

        mMiddle.setText(getCustomTitle());
        //默认退出
        mLeft.setOnClickListener(v -> onBackPressed());
    }

    protected String getCustomTitle(){
        final Intent intent = getIntent();
        if (intent != null)return intent.getStringExtra(TITLE_KEY);
        return "";
    }

    private void setContentLayout() {
        setContentView(R.layout.mobile_activity_main);
        final LinearLayout main_layout = findViewById(R.id._main);
        if (null != main_layout) {
            main_layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            View.inflate(this,getContentLayoutId(), main_layout);
        }
    }

    protected abstract int getContentLayoutId();


    protected void initTitleText(){//子类重写

    }

    protected void initTitleClickListener(){

    }


    protected void setLeftText(final String text){
        if (text != null && mLeft != null){
            mLeft.setText(text);
        }
    }
    protected void setMiddleText(final String text){
        if (text != null && mMiddle != null){
            mMiddle.setText(text);
        }
    }

    protected CharSequence getMiddleText(){
        if (mMiddle != null){
            return mMiddle.getText();
        }
        return "";
    }

    protected void setRightText(final String text){
        if (text != null && mRight != null){
            mRight.setText(text);
        }
    }

    protected CharSequence getRightText(){
        if (mRight != null){
            return mRight.getText();
        }
        return "";
    }

    protected void setLeftListener(final View.OnClickListener listener){
        if (mLeft != null)mLeft.setOnClickListener(listener);
    }

    protected void setMiddleListener(final View.OnClickListener listener){
        if (mMiddle != null)mMiddle.setOnClickListener(listener);
    }

    protected void setRightListener(final View.OnClickListener listener){
        if (mRight != null)mRight.setOnClickListener(listener);
    }


}