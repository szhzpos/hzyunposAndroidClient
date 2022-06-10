package com.wyc.cloudapp.dialog.pay;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.SaleActivity;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONObject;

public final class NormalSettlementDialog extends AbstractSettlementDialog {
    private String mMobilePayCode = "";
    public NormalSettlementDialog(SaleActivity context, String title) {
        super(context, title);
    }

    @Override
    protected int getContentLayoutId(){
        return R.layout.pay_dialog_content_layout;
    }

    @Override
    public void show(){
        super.show();
        if (Utils.isNotEmpty(mMobilePayCode)){
            defaultMobilePay();
            autoPay(mMobilePayCode);
        }
    }

    public void setMobilePayCode(String code) {
        this.mMobilePayCode = code;
    }
}
