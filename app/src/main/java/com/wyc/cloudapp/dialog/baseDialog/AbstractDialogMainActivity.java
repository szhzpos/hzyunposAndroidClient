package com.wyc.cloudapp.dialog.baseDialog;

import android.view.KeyEvent;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.activity.MainActivity;

public abstract class AbstractDialogMainActivity extends AbstractDialog {
    protected MainActivity mContext;
    public AbstractDialogMainActivity(@NonNull MainActivity context, final String title) {
        super(context, title);
        mContext = context;

        setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP){
                keyListenerCallBack();
                return true;
            }
            return false;
        });
    }
    public AbstractDialogMainActivity(@NonNull MainActivity context, final CharSequence title) {
        super(context, title);
        mContext = context;
    }

    @Override
    public MainActivity getPrivateContext() {
        return (MainActivity) mContext;
    }

    protected void keyListenerCallBack(){

    }
}
