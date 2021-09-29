package com.wyc.cloudapp.activity.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.OverScroller;
import android.widget.TextView;

import androidx.annotation.CallSuper;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;

public abstract class AbstractMobileActivity extends MainActivity {
    public static final String TITLE_KEY = "TL";
    private TextView mLeft,mMiddle,mRight;
    private View mRoot;
    private float mTouchX = -1;
    private OverScroller mScroller;
    private int mStatusBarAlpha;
    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout();
        initWindow();
        initTitle();
        initTitleText();
        initTitleClickListener();
    }

    @Override
    protected void onDestroy() {
        stopScroll();
        super.onDestroy();
    }

    private void initWindow(){
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) ;//显示状态栏
        mRoot = window.getDecorView();
        mRoot.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                updateStatusBarColor(Math.abs((float)scrollX / (float)v.getWidth()));
            }
        });
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
            switch (ev.getAction()){
                case MotionEvent.ACTION_DOWN:
                    mTouchX = ev.getX();
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mTouchX == -1)mTouchX = ev.getX();
                    mRoot.scrollTo((int) (mTouchX - ev.getX()),0);
                    break;
                case MotionEvent.ACTION_UP:
                    mTouchX = -1;
                    int w = mRoot.getWidth();
                    int scrollX = mRoot.getScrollX(),distance_scrollX = Math.abs(scrollX);
                    int dx;
                    if (distance_scrollX > (w >> 1)){
                        dx = distance_scrollX - w;
                    }else {
                        dx = distance_scrollX;
                    }
                    if (scrollX > 0)dx = -dx;

                    if (mScroller == null)mScroller = new OverScroller(this);
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