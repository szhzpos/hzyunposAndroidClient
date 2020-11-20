package com.wyc.cloudapp.dialog.baseDialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.JEventLoop;

public abstract class AbstractDialog extends Dialog {
    protected CharSequence mTitle;
    private WindowManager mWM;
    private WindowManager.LayoutParams mLayoutParams;
    private View mRootView;
    private JEventLoop mEventLoop;
    private int mCode;
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

        final Window window = getWindow();
        mRootView =  window.getDecorView();
        mLayoutParams = (WindowManager.LayoutParams)mRootView.getLayoutParams();
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

    @Override
    @CallSuper
    public void dismiss(){
        super.dismiss();
        if (mEventLoop != null)mEventLoop.done(mCode);
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
                break;
            default:
        }
        return super.onTouchEvent(event);
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
