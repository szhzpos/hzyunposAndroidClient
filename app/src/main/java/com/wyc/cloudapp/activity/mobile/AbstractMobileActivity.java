package com.wyc.cloudapp.activity.mobile;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;

public abstract class AbstractMobileActivity extends MainActivity {
    private TextView mLeft,mMiddle,mRight;
    @Override
    @CallSuper
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) ;//显示状态栏
        setContentLayout();

        initTitle();
        initTitleText();
        initTitleClickListener();
    }

    private void initTitle(){
        mLeft = findViewById(R.id.left_title_tv);
        mMiddle = findViewById(R.id.middle_title_tv);
        mRight = findViewById(R.id.right_title_tv);

        //默认退出
        mLeft.setOnClickListener(v -> onBackPressed());
    }

    private void setContentLayout() {
        setContentView(R.layout.mobile_activity_main);
        final LinearLayout main_layout = findViewById(R.id._main);
        if (null != main_layout) {
            main_layout.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            View.inflate(this,getContentLayoutId(), main_layout);
        }
    }

    protected abstract int getContentLayoutId();


    protected void initTitleText(){//子类重写

    }

    protected void initTitleClickListener(){

    }


    protected void setLeftText(final String text){
        if (text != null && mLeft != null){
            mLeft.setText(text);
        }
    }
    protected void setMiddleText(final String text){
        if (text != null && mMiddle != null){
            mMiddle.setText(text);
        }
    }

    protected CharSequence getMiddleText(){
        if (mMiddle != null){
            return mMiddle.getText();
        }
        return "";
    }

    protected void setRightText(final String text){
        if (text != null && mRight != null){
            mRight.setText(text);
        }
    }

    protected CharSequence getRightText(){
        if (mRight != null){
            return mRight.getText();
        }
        return "";
    }

    protected void setLeftListener(final View.OnClickListener listener){
        if (mLeft != null)mLeft.setOnClickListener(listener);
    }

    protected void setMiddleListener(final View.OnClickListener listener){
        if (mMiddle != null)mMiddle.setOnClickListener(listener);
    }

    protected void setRightListener(final View.OnClickListener listener){
        if (mRight != null)mRight.setOnClickListener(listener);
    }


}