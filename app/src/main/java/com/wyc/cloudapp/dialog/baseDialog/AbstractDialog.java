package com.wyc.cloudapp.dialog.baseDialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wyc.cloudapp.R;

import java.util.Stack;

public abstract class AbstractDialog extends Dialog {
    protected Context mContext;
    protected String mTitle;
    private int mCode;
    private Stack mModelMessage;
    private AbstractDialog(@NonNull Context context){
        super(context);
        mContext = context;
        mModelMessage = new Stack();
    }
    AbstractDialog(@NonNull Context context, final String title, int style){
        super(context,style);
        mContext = context;
        mModelMessage = new Stack();
        mTitle = title;
    }
    AbstractDialog(@NonNull Context context, final String title) {
        this(context);
        mTitle = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout();
        setCancelable(false);

        setTitle();
        initCloseBtn();
    }

    @Override
    public void dismiss(){
        super.dismiss();
        exit();
    }

    protected void setExitCode(int code ){
        mCode = code;
    }

    @SuppressWarnings("unchecked")
    public int exec(){
        mModelMessage.push(Thread.currentThread().getId());
        show();
        try {
            Looper.loop();
        }catch (NullPointerException ignored){
        }
        return mCode;
    }

    private void exit(){
        if (!mModelMessage.empty()){
            mModelMessage.pop();
            throw new NullPointerException();
        }
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
        this.dismiss();
    }

    public abstract Context getPrivateContext();

    protected abstract int getContentLayoutId();
}
