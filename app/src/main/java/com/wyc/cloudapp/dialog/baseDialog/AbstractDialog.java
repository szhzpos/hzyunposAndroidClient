package com.wyc.cloudapp.dialog.baseDialog;

import android.annotation.SuppressLint;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.logger.Logger;

import java.util.Arrays;

public abstract class AbstractDialog extends Dialog {
    protected Context mContext;
    protected String mTitle;
    private WindowManager mWM;
    private WindowManager.LayoutParams mLayoutParams;
    private View mRootView;
    private JEventLoop mEventLoop;
    private int mCode;
    private double mTouchX,mTouchY;
    private AbstractDialog(@NonNull Context context){
        super(context);
        init(context);
    }
    AbstractDialog(@NonNull Context context, final String title, int style){
        super(context,style);
        init(context);
        mTitle = title;
    }
    AbstractDialog(@NonNull Context context, final String title) {
        this(context);
        mTitle = title;
    }

    private void init(final Context context){
        mContext = context;
        mWM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        final Display display = mWM.getDefaultDisplay();
        final Point point = new Point();
        display.getSize(point);

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

        Logger.d("action:%d",event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchX = event.getX();
                mTouchY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                Logger.d("X:%f,Y:%f",event.getX(),event.getY());
                mLayoutParams.x = (int) (event.getRawX() - mTouchY);
                mLayoutParams.y = (int) (event.getRawY() - mTouchY);
                mWM.updateViewLayout(mRootView,mLayoutParams);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                break;
            default:
        }
        return super.onTouchEvent(event);
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

    @SuppressLint("ClickableViewAccessibility")
    private void setContentLayout() {
        setContentView(R.layout.base_dialog_layout);
        final LinearLayout main_layout = findViewById(R.id.dialog_main_layout);
        if (null != main_layout) {
            final View dialog_content = View.inflate(mContext,getContentLayoutId(), null);
            if (dialog_content != null)
                main_layout.addView(dialog_content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    private void setTitle() {
        final TextView title_tv = findViewById(R.id.title);
        if (null != title_tv && null != mTitle) {
            title_tv.setText(mTitle);
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
