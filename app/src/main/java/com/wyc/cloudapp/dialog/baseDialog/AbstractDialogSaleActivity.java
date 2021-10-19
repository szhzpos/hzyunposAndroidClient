package com.wyc.cloudapp.dialog.baseDialog;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.activity.base.SaleActivity;

public abstract class AbstractDialogSaleActivity extends AbstractDialog {
    protected SaleActivity mContext;
    public AbstractDialogSaleActivity(@NonNull SaleActivity context, final CharSequence title) {
        super(context, title,0);
        mContext = context;
    }

    @Override
    public SaleActivity getPrivateContext() {
        return mContext;
    }
}
