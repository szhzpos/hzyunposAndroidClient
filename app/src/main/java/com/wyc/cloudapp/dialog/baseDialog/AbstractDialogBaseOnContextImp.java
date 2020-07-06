package com.wyc.cloudapp.dialog.baseDialog;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

public abstract class AbstractDialogBaseOnContextImp extends AbstractDialog {

    public AbstractDialogBaseOnContextImp(@NonNull Context context, final String title){
        super(context,title);
    }
    public AbstractDialogBaseOnContextImp(@NonNull Context context, final String title, int style){
        super(context,title,style);
    }

    @Override
    public Context getPrivateContext() {
        return mContext;
    }
}
