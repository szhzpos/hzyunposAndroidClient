package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wyc.cloudapp.R;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class CustomDialog extends ProgressDialog
{
    private String szTitle;
    private TextView mTitle,mShowTimeView;
    private Myhandler mHandler;
    private Timer mTimer;
    private long mShowTime = 0;
    private boolean mCancel = false;
    public CustomDialog(Context context)
    {
        super(context);
    }

    public CustomDialog(Context context, int theme)
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
        setCancelable(mCancel);
        setCanceledOnTouchOutside(mCancel);

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
        mTitle.setText(szTitle);

        findViewById(R.id.dialog_linearLayout).setMinimumWidth(100);

    }

    @Override
    public void show()
    {
        super.show();
        startTimer();
    }

    @Override
    public void dismiss(){
        super.dismiss();
        stopTimer();
        mShowTime = 0;
    }

    private void startTimer(){
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.obtainMessage(1).sendToTarget();
            }
        },0,1000);
    }

    private void stopTimer(){
        if (null != mTimer){
            mTimer.cancel();
            mTimer = null;
        }
    }

    public CustomDialog setTitle(final String title){
        szTitle = title;
        return this;
    }

    public String getSzTitle(){
        return szTitle;
    }

    public void refreshTitle(){
        mHandler.obtainMessage(0).sendToTarget();
    }

    public CustomDialog setmCancel(boolean b){
        mCancel = b;
        return this;
    }

    private static class Myhandler extends Handler {
        private WeakReference<CustomDialog> weakHandler;
        private Myhandler(CustomDialog customDialog){
            this.weakHandler = new WeakReference<>(customDialog);
        }
        public void handleMessage(Message msg){
            CustomDialog dialog = weakHandler.get();
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