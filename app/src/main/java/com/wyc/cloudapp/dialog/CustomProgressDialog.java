package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;
import java.util.concurrent.locks.LockSupport;

public class CustomProgressDialog extends Dialog implements SurfaceHolder.Callback {
    private String szMessage;
    private TextView mMessage;
    private boolean mRestShowTime = true;

    private Paint mPaint;
    private volatile boolean isStart;
    private long mShowTime = 0;
    private Thread mThread;

    public CustomProgressDialog(Context context)
    {
        super(context,R.style.CustomProgressDialog);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        init();
        initSurfaceView();
    }

    private void initSurfaceView(){
        final SurfaceView surfaceView = findViewById(R.id.timer_view);
        surfaceView.setZOrderOnTop(true);
        final SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        surfaceHolder.addCallback(this);

        final Paint paint =  new Paint();
        paint.setTextSize(Utils.sp2px(getContext(),16));
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(CustomApplication.self().getColor(R.color.white));
        mPaint = paint;
    }

    private void init()
    {
        setContentView(R.layout.custom_progress_dialog_layout);
        mMessage = findViewById(R.id.title);
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
    public void dismiss(){
        szMessage = "";
        super.dismiss();
    }

    @Override
    public void onAttachedToWindow (){
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        if (mRestShowTime){
            mShowTime = 0;
        }
    }

    public CustomProgressDialog setMessage(final String m){
        szMessage = m;
        return this;
    }

    public CustomProgressDialog refreshMessage(){
        CustomApplication.postAtFrontOfQueue(()-> mMessage.setText(szMessage));
        return this;
    }

    public CustomProgressDialog setRestShowTime(boolean b){
        mRestShowTime = b;
        return this;
    }

    public CustomProgressDialog setCancel(boolean b){
        setCancelable(b);
        return this;
    }

    public static CustomProgressDialog showProgress(final Context context,final String message){
        final CustomProgressDialog progressDialog = new CustomProgressDialog(context);
        progressDialog.setMessage(message).show();
        progressDialog.setCancelable(false);
        return progressDialog;
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        isStart = true;
        drawTimer(holder);
        mThread = new Thread(() -> {
            while (isStart){
                LockSupport.parkNanos(this,1000L * 1000L * 1000L);
                if (!isStart)break;
                drawTimer(holder);
            }
        });
        mThread.start();
    }
    private void drawTimer(final SurfaceHolder holder){
        final Canvas canvas = holder.lockCanvas();
        if (canvas != null){
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            final String sz = String.format(Locale.CHINA,"%ds",++mShowTime);
            final Rect textBounds = new Rect();
            mPaint.getTextBounds(sz,0,sz.length(),textBounds);
            final int margin = 8;
            canvas.drawText(sz,holder.getSurfaceFrame().width() - textBounds.width() - margin,textBounds.height() + margin,mPaint);
            holder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isStart = false;
        LockSupport.unpark(mThread);
    }
}