package com.wyc.cloudapp.dialog.orderDialog;

import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;

public abstract class AbstractOrderDetailsDialog extends AbstractDialogMainActivity {
    protected final JSONObject mOrderInfo;
    protected JSONObject mPayRecord;
    public AbstractOrderDetailsDialog(@NonNull MainActivity context, CharSequence title,final JSONObject info) {
        super(context, title);
        mOrderInfo = info;
    }

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showOrderInfo();
        initGoodsDetail();
        initPayDetail();
        initReprint();
        initVerifyPay();
    }

    protected abstract void showOrderInfo();
    protected abstract void initGoodsDetail();
    protected abstract void initPayDetail();
    protected abstract void initReprint();
    protected abstract void initVerifyPay();
}
