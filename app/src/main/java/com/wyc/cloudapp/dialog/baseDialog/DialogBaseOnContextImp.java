package com.wyc.cloudapp.dialog.baseDialog;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

public class DialogBaseOnContextImp extends AbstractDialog {

    public DialogBaseOnContextImp(@NonNull Context context, final String title){
        super(context,title);
    }
    public DialogBaseOnContextImp(@NonNull Context context, final String title, int style){
        super(context,title,style);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public Context getPrivateContext() {
        return mContext;
    }
}