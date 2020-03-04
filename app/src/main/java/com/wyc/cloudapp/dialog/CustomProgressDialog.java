package com.wyc.cloudapp.dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.logger.Logger;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class CustomProgressDialog extends ProgressDialog
{
    private String szTitle;
    private TextView mTitle,mShowTimeView;
    private Myhandler mHandler;
    private Timer mTimer;
    private long mShowTime = 0;
    private boolean mCancel = false;
    private boolean mRestShowTime = true;
    public CustomProgressDialog(Context context)
    {
        super(context);
    }

    public CustomProgressDialog(Context context, int theme)
    {
        super(context, theme);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        init(getContext());
    }

    private void init(Context context)
    {
        setContentView(R.layout.custom_dialog);

        mTitle = findViewById(R.id.title);
        mShowTimeView = findViewById(R.id.show_time);
        mHandler = new Myhandler(this);

        Window window = getWindow();
        if (null != window){
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }

        findViewById(R.id.dialog_linearLayout).setMinimumWidth(100);

    }

    @Override
    public void show()
    {
        super.show();
        setCancelable(mCancel);
        setCanceledOnTouchOutside(mCancel);
        mTitle.setText(szTitle);
        startTimer();
    }

    @Override
    public void dismiss(){
        super.dismiss();
    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        if (mRestShowTime){
            stopTimer();
            mShowTime = 0;
        }
    }

    private void startTimer(){
        if (null == mTimer){
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mHandler.obtainMessage(1).sendToTarget();
                }
            },0,1000);
        }
    }

    private void stopTimer(){
        if (null != mTimer){
            mTimer.cancel();
            mTimer = null;
        }
    }

    public CustomProgressDialog setTitle(final String title){
        szTitle = title;
        return this;
    }

    public CustomProgressDialog setRestShowTime(boolean b){
        mRestShowTime = b;
        return this;
    }

    public String getSzTitle(){
        return szTitle;
    }

    public void refreshTitle(){
        mHandler.obtainMessage(0).sendToTarget();
    }

    public CustomProgressDialog setCancel(boolean b){
        mCancel = b;
        return this;
    }

    private static class Myhandler extends Handler {
        private WeakReference<CustomProgressDialog> weakHandler;
        private Myhandler(CustomProgressDialog customDialog){
            this.weakHandler = new WeakReference<>(customDialog);
        }
        public void handleMessage(Message msg){
            CustomProgressDialog dialog = weakHandler.get();
            if (null == dialog)return;
            switch (msg.what){
                case 0:
                    dialog.mTitle.setText(dialog.szTitle);
                    break;
                case 1:
                    dialog.mShowTimeView.setText(String.valueOf(++dialog.mShowTime));
                    break;
            }
        }
    }
}