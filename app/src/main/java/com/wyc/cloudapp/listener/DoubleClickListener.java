package com.wyc.cloudapp.listener;

import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;

public class DoubleClickListener implements View.OnTouchListener {
    private final String TAG = this.getClass().getSimpleName();
    private int count = 0;
    private long firClick = 0;
    private onDoubleClickListener mDoubleListener;
    private onSingleClickListener mSingleListener;
    private Handler mHandler;
    public interface onDoubleClickListener {
        void onDoubleClick(View v);
    }

    public interface onSingleClickListener{
        void onClick(View v);
    }

    public DoubleClickListener(onDoubleClickListener d,onSingleClickListener s) {
        super();
        this.mDoubleListener = d;
        this.mSingleListener = s;
        mHandler = new Handler();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        long secClick = 0;
        int interval = 200;
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            count++;
            if (1 == count) {
                firClick = System.currentTimeMillis();
                mHandler.postDelayed(()->{
                    synchronized (DoubleClickListener.this){
                        if (mSingleListener != null){
                            mSingleListener.onClick(v);
                        }
                        count = 0;
                        firClick = 0;
                    }
                },250);

            } else if (2 == count) {
                secClick = System.currentTimeMillis();
                if (secClick - firClick < interval) {
                    synchronized (this){
                        mHandler.removeCallbacksAndMessages(null);
                    }
                    if (mDoubleListener != null) {
                        mDoubleListener.onDoubleClick(v);
                    }
                    count = 0;
                    firClick = 0;
                } else {
                    firClick = secClick;
                    count = 1;
                }
            }
        }
        return true;
    }
}
