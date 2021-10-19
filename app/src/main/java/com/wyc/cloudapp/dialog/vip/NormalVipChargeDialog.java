package com.wyc.cloudapp.dialog.vip;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.bean.VipInfo;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;


public class NormalVipChargeDialog extends AbstractVipChargeDialog {
    private EditText mPayCodeEt;
    public NormalVipChargeDialog(@NonNull MainActivity context, final VipInfo vip) {
        super(context,vip);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPayCode();
    }

    private void initPayCode(){
        mPayCodeEt = findViewById(R.id.pay_code);
    }

    @Override
    public boolean hookEnterKey() {
        triggerCharge();
        return true;
    }

    @Override
    public boolean checkPayMethod() {
        if (null != mPayMethodSelected){
            int is_check = mPayMethodSelected.getIntValue("is_check");
            if (is_check != 2){
                if (mPayCodeEt != null){
                    final String pay_code = mPayCodeEt.getText().toString();
                    if (pay_code.isEmpty()){
                        mPayCodeEt.setVisibility(View.VISIBLE);
                        mPayCodeEt.requestFocus();
                        final String xtype = Utils.getNullStringAsEmpty(mPayMethodSelected,"xtype");
                        mPayCodeEt.setHint(xtype);
                        mPayCodeEt.postDelayed(()-> MyDialog.ToastMessage(mPayCodeEt,xtype, getWindow()),300);
                        return false;
                    }else {
                        mPayMethodSelected.put(PAY_CODE_LABEL,pay_code);
                    }
                }else {
                    MyDialog.ToastMessage(mContext.getString(R.string.pay_m_hint_sz), getWindow());
                }
            }else {
                if (null != mPayCodeEt){
                    mPayCodeEt.setVisibility(View.GONE);
                    mPayCodeEt.clearFocus();
                    mPayCodeEt.setText(mContext.getString(R.string.space_sz));
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
        if (b)if (mPayCodeEt != null)mPayCodeEt.getText().clear();
        if (mPayMethodSelected != null)mPayMethodSelected.remove(PAY_CODE_LABEL);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.normal_vip_charge_dialog_layout;
    }

    @Override
    protected void initWindowSize(){

    }
}
