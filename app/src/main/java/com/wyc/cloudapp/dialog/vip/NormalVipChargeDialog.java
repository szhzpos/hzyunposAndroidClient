package com.wyc.cloudapp.dialog.vip;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;


public class NormalVipChargeDialog extends AbstractVipChargeDialog {
    public NormalVipChargeDialog(@NonNull MainActivity context, final JSONObject vip) {
        super(context,vip);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public boolean checkPayMethod() {
        if (mPayCodeEt != null){
            int is_check = mPayMethodSelected.getIntValue("is_check");
            if (is_check != 2){
                if (mPayCodeEt.getText().length() == 0){
                    mPayCodeEt.setVisibility(View.VISIBLE);
                    mPayCodeEt.requestFocus();
                    final String xtype = Utils.getNullStringAsEmpty(mPayMethodSelected,"xtype");
                    mPayCodeEt.setHint(xtype);
                    MyDialog.ToastMessage(mPayCodeEt,xtype,mContext,getWindow());

                    return false;
                }
            }else {
                mPayCodeEt.setVisibility(View.GONE);
                mPayCodeEt.clearFocus();
                mPayCodeEt.setText(mContext.getString(R.string.space_sz));
            }
        }
        return true;
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.normal_vip_charge_dialog_layout;
    }

    @Override
    protected void initWindowSize(){

    }
}
