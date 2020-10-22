package com.wyc.cloudapp.dialog.baseDialog;

import android.content.Context;

import androidx.annotation.NonNull;

public abstract class AbstractDialogBaseOnContextImp extends AbstractDialog {

    public AbstractDialogBaseOnContextImp(@NonNull Context context, final String title){
        this(context,title,0);
    }
    public AbstractDialogBaseOnContextImp(@NonNull Context context, final String title, int style){
        super(context,title,style);
    }

    @Override
    public Context getPrivateContext() {
        return mContext;
    }
}
