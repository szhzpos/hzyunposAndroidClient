package com.wyc.cloudapp.dialog.baseDialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

public abstract class AbstractDialog extends Dialog {
    protected CharSequence mTitle;
    private WindowManager mWM;
    private WindowManager.LayoutParams mLayoutParams;
    protected View mRootView;
    private JEventLoop mEventLoop;
    private int mCode = 0;
    private float mTouchX,mTouchY;
    private boolean isTitle;
    AbstractDialog(@NonNull Context context, final CharSequence title, int style){
        super(context,(style == 0 ? R.style.MyDialog : style));
        init(context);
        mTitle = title;
    }

    private AbstractDialog(@NonNull Context context){
        this(context,null,0);
    }

    private void init(final Context context){
        mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        final Window window = getWindow();
        window.setWindowAnimations(R.style.dialog_anim);
        mRootView =  window.getDecorView();
        mLayoutParams = window.getAttributes();
    }

    @Override
    protected void finalize(){
        Logger.d(getClass().getSimpleName() + " finalized");
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        dismiss();
    }

    @CallSuper
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        //初始化窗口尺寸
        initWindowSize();
    }

    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout();
        setCancelable(false);
        setTitle();
        initCloseBtn();
    }

    protected void initWindowSize(){
        final Display d = mWM.getDefaultDisplay(); // 获取屏幕宽、高用
        final Point point = new Point();
        d.getSize(point);
        final Window dialogWindow = this.getWindow();
        dialogWindow.setGravity(Gravity.CENTER);
        double w = getWidthRatio(),h = getHeightRatio();
        if (w < 0.0 || Utils.greaterDouble(w,1.0)){
            mLayoutParams.width = (int) w;
        }else {
            mLayoutParams.width = (int)(w * point.x);
        }
        if (h < 0.0|| Utils.greaterDouble(w,1.0)){
            mLayoutParams.height = (int) h;
        }else {
            mLayoutParams.height = (int)(h * point.y);
        }
        dialogWindow.setAttributes(mLayoutParams);
    }

    protected double getWidthRatio(){
        //返回值： //小于0 是系统WRAP_CONTENT、MATCH_PARENT 在0到1直接为屏幕比例 大于1为具体大小
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }
    protected double getHeightRatio(){
        //返回值： 小于0 是系统WRAP_CONTENT、MATCH_PARENT 在0到1直接为屏幕比例 大于1为具体大小
        return ViewGroup.LayoutParams.WRAP_CONTENT;
    }

    protected final void fullScreen(){
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    protected final void widthFullScreen(){
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    @CallSuper
    public void dismiss(){
        super.dismiss();
        hideInputMethod();
        if (mEventLoop != null)mEventLoop.done(mCode);
    }

    private void hideInputMethod(){
        final InputMethodManager imm = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (isTitle = isTitleView(event.getX(), event.getY())){
                    mTouchX = event.getRawX() - mLayoutParams.x;
                    mTouchY = event.getRawY() - mLayoutParams.y;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (isTitle){
                    mLayoutParams.x = (int) (event.getRawX() - mTouchX);
                    mLayoutParams.y = (int) (event.getRawY() - mTouchY);
                    mWM.updateViewLayout(mRootView,mLayoutParams);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTitle = false;
                updatePrintIcon();
                break;
            default:
        }
        return super.onTouchEvent(event);
    }


    protected void updatePrintIcon(){

    }

    private boolean isTitleView(float x, float y){
        if (mRootView != null){
            final TextView title_tv = mRootView.findViewById(R.id.title);
            if (title_tv != null){
                float v_x = title_tv.getX(),v_y = title_tv.getY();
                return x >= v_x && x <= v_x + title_tv.getWidth() && y >= v_y && y <= v_y + title_tv.getHeight();
            }
        }
        return false;
    }

    public void setCodeAndExit(int code ){
        mCode = code;
        dismiss();
    }

    public int exec(){
        show();
        if (mEventLoop == null)mEventLoop = new JEventLoop();
        return mEventLoop.exec();
    }

    private void setContentLayout() {
        setContentView(R.layout.base_dialog_layout);
        final LinearLayout main_layout = findViewById(R.id.dialog_main_layout);
        if (null != main_layout) {
            main_layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            View.inflate(getContext(),getContentLayoutId(), main_layout);
        }
    }

    private void setTitle() {
        final TextView title_tv = findViewById(R.id.title);
        if (null != title_tv && null != mTitle) {
            title_tv.setText( mTitle);
        }
    }

    private void initCloseBtn() {
        final Button _close = findViewById(R.id._close);
        if (_close != null) {
            _close.setOnClickListener(v -> closeWindow());
        }
    }

    protected void closeWindow() {
        mCode = 0;
        this.dismiss();
    }

    public abstract Context getPrivateContext();

    protected abstract int getContentLayoutId();
}
