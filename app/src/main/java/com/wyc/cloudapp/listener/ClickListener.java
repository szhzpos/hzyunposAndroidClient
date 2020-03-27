package com.wyc.cloudapp.listener;
import android.view.MotionEvent;
import android.view.View;

public class ClickListener implements View.OnTouchListener {
    private final String TAG = this.getClass().getSimpleName();
    private int count = 0;
    private long firClick = 0;
    private onDoubleClickListener mDoubleListener;
    private onSingleClickListener mSingleListener;
    private View mView;
    public interface onDoubleClickListener {
        void onDoubleClick(View v);
    }

    public interface onSingleClickListener{
        void onClick(View v);
    }

    public ClickListener(onDoubleClickListener d, onSingleClickListener s) {
        super();
        this.mDoubleListener = d;
        this.mSingleListener = s;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mView = v;
        long secClick = 0;
        int interval = 200;
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            count++;
            if (1 == count) {
                firClick = System.currentTimeMillis();
                v.postDelayed(SingleClick,250);
            } else if (2 == count) {
                secClick = System.currentTimeMillis();
                if (secClick - firClick < interval) {
                     v.removeCallbacks(SingleClick);
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
        v.performClick();
        return true;
    }
    private Runnable SingleClick = ()->{
            if (mSingleListener != null){
                mSingleListener.onClick(mView);
            }
            count = 0;
            firClick = 0;
    };
}
