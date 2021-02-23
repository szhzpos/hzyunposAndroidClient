package com.wyc.cloudapp.dialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.wyc.cloudapp.R;

public class CustomProgressDialog extends ProgressDialog
{
    private String szMessage;
    private TextView mMessage,mShowTimeView;
    private long mShowTime = 0;
    private boolean mRestShowTime = true;
    public CustomProgressDialog(Context context)
    {
        super(context,R.style.CustomProgressDialog);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        init();
        //开始计时
        startTimer();
    }

    private void init()
    {
        setContentView(R.layout.custom_progress_dialog_layout);
        mMessage = findViewById(R.id.title);
        mShowTimeView = findViewById(R.id.show_time);
        mMessage.setText(szMessage);
        Window window = getWindow();
        if (null != window){
            WindowManager.LayoutParams params = window.getAttributes();
            params.width = WindowManager.LayoutParams.WRAP_CONTENT;
            params.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(params);
        }
        findViewById(R.id.progress_linearLayout).setMinimumWidth(100);

    }

    @Override
    public void show()
    {
        super.show();
    }

    @Override
    public void dismiss(){
        super.dismiss();
        szMessage = "";
    }

    @Override
    public void onAttachedToWindow (){
        super.onAttachedToWindow();
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
        updateTime();
    }

    private void stopTimer(){
        if (mShowTimeView != null)
            mShowTimeView.removeCallbacks(null);
    }

    public CustomProgressDialog setMessage(final String m){
        szMessage = m;
        return this;
    }

    public CustomProgressDialog refreshMessage(){
        if (null != mMessage)mMessage.post(()-> mMessage.setText(szMessage));
        return this;
    }

    public CustomProgressDialog setRestShowTime(boolean b){
        mRestShowTime = b;
        return this;
    }

    public CustomProgressDialog setCancel(boolean b){
        setCancelable(b);
        setCanceledOnTouchOutside(b);
        return this;
    }
    private void updateTime(){
        mShowTimeView.setText(String.valueOf(++mShowTime));
        if (mShowTime == 300)dismiss();
        mShowTimeView.postDelayed(this::updateTime,1000);
    }
    public static CustomProgressDialog showProgress(final Context context,final String message){
        final CustomProgressDialog progressDialog = new CustomProgressDialog(context);
        progressDialog.setMessage(message).show();
        return progressDialog;
    }
}