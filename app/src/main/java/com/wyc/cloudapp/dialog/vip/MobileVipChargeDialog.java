package com.wyc.cloudapp.dialog.vip;

import android.content.Intent;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.cashierDesk.MobileCashierActivity;
import com.wyc.cloudapp.constants.ScanCallbackCode;
import com.wyc.cloudapp.dialog.MyDialog;

public class MobileVipChargeDialog extends AbstractVipChargeDialog implements MobileCashierActivity.ScanCallback  {
    public MobileVipChargeDialog(@NonNull MainActivity context) {
        super(context);
    }

    @Override
    public boolean checkPayMethod() {
        if (mPayMethodSelected != null){
            int is_check = mPayMethodSelected.getIntValue("is_check");
            if (is_check != 2){
                if (!mPayMethodSelected.containsKey(PAY_CODE_LABEL)){
                    final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    mContext.startActivityForResult(intent, ScanCallbackCode.PAY_REQUEST_CODE);
                    mContext.setScanCallback(this);
                    return false;
                }
            }
        }else {
            MyDialog.ToastMessage(mContext.getString(R.string.pay_m_hint_sz), getWindow());
            return false;
        }
        return true;
    }

    @Override
    public void clearPayCode(boolean b) {
        if (mPayMethodSelected != null)mPayMethodSelected.remove(PAY_CODE_LABEL);
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
        if (mPayMethodSelected != null)mPayMethodSelected.put(PAY_CODE_LABEL,code);
        triggerCharge();
    }
}
