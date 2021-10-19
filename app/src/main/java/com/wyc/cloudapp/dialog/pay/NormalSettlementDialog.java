package com.wyc.cloudapp.dialog.pay;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.SaleActivity;

public final class NormalSettlementDialog extends AbstractSettlementDialog {
    public NormalSettlementDialog(SaleActivity context, String title) {
        super(context, title);
    }

    @Override
    protected int getContentLayoutId(){
        return R.layout.pay_dialog_content_layout;
    }
}
