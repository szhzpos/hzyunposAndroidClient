package com.wyc.cloudapp.dialog.baseDialog;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.activity.SaleActivity;

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

    @Override
    public void dismiss(){
        super.dismiss();
        mContext.setScanCallback(null);//一定要清空，否则对象无法被回收
    }
}
