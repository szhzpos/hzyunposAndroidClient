package com.wyc.cloudapp.dialog.vip;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.MobileCashierActivity;
import com.wyc.cloudapp.utils.MessageID;

public class MobileVipChargeDialog extends AbstractVipChargeDialog implements MobileCashierActivity.ScanCallback  {
    public MobileVipChargeDialog(@NonNull MainActivity context) {
        super(context);
    }

    @Override
    public boolean checkPayMethod() {
        int is_check = mPayMethodSelected.getIntValue("is_check");
        if (is_check != 2){
            final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            mContext.startActivityForResult(intent, MessageID.PAY_REQUEST_CODE);
            mContext.setScanCallback(this);

            return false;
        }

        return true;
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.mobile_vip_charge_dialog_layout;
    }

    @Override
    protected void initWindowSize(){
        fullScreen();
    }

    @Override
    public void callback(String code) {
        if (mPayCodeEt != null)mPayCodeEt.setText(code);
        triggerCharge();
    }
}
