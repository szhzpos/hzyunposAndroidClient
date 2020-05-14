package com.wyc.cloudapp.dialog.baseDialog;

import androidx.annotation.NonNull;
import com.wyc.cloudapp.activity.MainActivity;

public class DialogBaseOnMainActivity extends AbstractDialog {
    protected MainActivity mContext;

    public DialogBaseOnMainActivity(@NonNull MainActivity context, String title) {
        super(context, title);
        mContext = context;
    }

    @Override
    public MainActivity getPrivateContext() {
        return mContext;
    }
}
