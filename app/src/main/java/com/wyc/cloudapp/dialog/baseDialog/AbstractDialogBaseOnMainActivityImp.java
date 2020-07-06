package com.wyc.cloudapp.dialog.baseDialog;

import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.logger.Logger;

public abstract class AbstractDialogBaseOnMainActivityImp extends AbstractDialog {
    protected MainActivity mContext;
    public AbstractDialogBaseOnMainActivityImp(@NonNull MainActivity context, String title) {
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
