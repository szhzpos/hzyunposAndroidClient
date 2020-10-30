package com.wyc.cloudapp.dialog.baseDialog;

import android.view.KeyEvent;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.activity.SaleActivity;

public abstract class AbstractDialogSaleActivity extends AbstractDialog {
    protected SaleActivity mContext;
    public AbstractDialogSaleActivity(@NonNull SaleActivity context, final CharSequence title) {
        super(context, title);
        mContext = context;
        initKeyListener();
    }

    @Override
    public SaleActivity getPrivateContext() {
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
