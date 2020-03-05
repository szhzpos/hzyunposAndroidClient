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
import com.wyc.cloudapp.utils.MessageID;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

public class CustomProgressDialog extends ProgressDialog
{
    private String szMessage;
    private TextView mMessage,mShowTimeView;
    private Myhandler mHandler;
    private Timer mTimer;
    private long mShowTime = 0;
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
        init();
    }

    private void init()
    {
        setContentView(R.layout.custom_dialog);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        mMessage = findViewById(R.id.title);
        mShowTimeView = findViewById(R.id.show_time);
        mHandler = new Myhandler(this);

        mMessage.setText(szMessage);

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
                    mHandler.obtainMessage(MessageID.DIALOG_SHOW_TIME_ID).sendToTarget();
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

    public CustomProgressDialog setMessage(final String m){
        szMessage = m;
        return this;
    }

    public CustomProgressDialog refreshMessage(){
        mHandler.obtainMessage(MessageID.DIALOG_UPDATE_MESSAGE_ID).sendToTarget();
        return this;
    }

    public CustomProgressDialog setRestShowTime(boolean b){
        mRestShowTime = b;
        return this;
    }

    public String getSzMessage(){
        return szMessage;
    }

    public CustomProgressDialog setCancel(boolean b){
        setCancelable(b);
        setCanceledOnTouchOutside(b);
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
                case MessageID.DIALOG_UPDATE_MESSAGE_ID:
                    dialog.mMessage.setText(dialog.szMessage);
                    break;
                case MessageID.DIALOG_SHOW_TIME_ID:
                    dialog.mShowTimeView.setText(String.valueOf(++dialog.mShowTime));
                    break;
            }
        }
    }
}