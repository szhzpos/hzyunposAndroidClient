package com.wyc.cloudapp.dialog.baseDialog;

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
    }
    @Override
    public MainActivity getPrivateContext() {
        return mContext;
    }
}
