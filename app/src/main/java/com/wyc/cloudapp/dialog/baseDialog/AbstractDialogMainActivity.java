package com.wyc.cloudapp.dialog.baseDialog;

import android.view.KeyEvent;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.activity.MainActivity;

public abstract class AbstractDialogMainActivity extends AbstractDialog {
    protected MainActivity mContext;

    public AbstractDialogMainActivity(@NonNull MainActivity context, final CharSequence title){
        this(context,title,0);
    }
    public AbstractDialogMainActivity(@NonNull MainActivity context, final CharSequence title, int style){
        super(context,title,style);
        mContext = context;
        initKeyListener();
    }
    @Override
    public MainActivity getPrivateContext() {
        return mContext;
    }
    protected void keyListenerCallBack(){

    }
    private void initKeyListener(){
        setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
                keyListenerCallBack();
                return true;
            }
            return false;
        });
    }
}
