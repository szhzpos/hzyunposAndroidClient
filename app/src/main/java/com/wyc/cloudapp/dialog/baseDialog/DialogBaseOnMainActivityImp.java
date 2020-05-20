package com.wyc.cloudapp.dialog.baseDialog;

import android.view.KeyEvent;
import android.view.View;

import androidx.annotation.NonNull;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.logger.Logger;

public class DialogBaseOnMainActivityImp extends AbstractDialog {
    protected MainActivity mContext;

    public DialogBaseOnMainActivityImp(@NonNull MainActivity context, String title) {
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

    @Override
    public MainActivity getPrivateContext() {
        return mContext;
    }

    protected void keyListenerCallBack(){

    }
}
