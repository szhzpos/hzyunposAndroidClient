package com.wyc.cloudapp.dialog.baseDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.JEventLoop;

public abstract class AbstractDialog extends Dialog {
    protected Context mContext;
    protected String mTitle;
    private JEventLoop mEventLoop;
    private int mCode;
    private AbstractDialog(@NonNull Context context){
        super(context);
        mContext = context;
    }
    AbstractDialog(@NonNull Context context, final String title, int style){
        super(context,style);
        mContext = context;
        mTitle = title;
    }
    AbstractDialog(@NonNull Context context, final String title) {
        this(context);
        mTitle = title;
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
