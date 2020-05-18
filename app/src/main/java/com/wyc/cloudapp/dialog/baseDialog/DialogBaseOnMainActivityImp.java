package com.wyc.cloudapp.dialog.baseDialog;

import androidx.annotation.NonNull;
import com.wyc.cloudapp.activity.MainActivity;

public class DialogBaseOnMainActivityImp extends AbstractDialog {
    protected MainActivity mContext;

    public DialogBaseOnMainActivityImp(@NonNull MainActivity context, String title) {
        super(context, title);
        mContext = context;
    }

    @Override
    public MainActivity getPrivateContext() {
        return mContext;
    }
}
