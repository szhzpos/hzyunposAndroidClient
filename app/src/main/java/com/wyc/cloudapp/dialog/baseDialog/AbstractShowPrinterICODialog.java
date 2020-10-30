package com.wyc.cloudapp.dialog.baseDialog;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.print.Printer;

public abstract class AbstractShowPrinterICODialog extends AbstractDialogSaleActivity {
    public AbstractShowPrinterICODialog(@NonNull SaleActivity context, String title) {
        super(context, title);
    }
    @CallSuper
    @Override
    public void dismiss(){
        super.dismiss();
        Printer.showPrintIcon(mContext,false);
    }
    @CallSuper
    @Override
    public void show(){
        super.show();
        Printer.showPrintIcon(mContext,true);
    }
}
