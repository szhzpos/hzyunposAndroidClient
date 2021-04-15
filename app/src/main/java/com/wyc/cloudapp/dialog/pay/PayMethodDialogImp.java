package com.wyc.cloudapp.dialog.pay;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.activity.mobile.MobileCashierActivity;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.constants.MessageID;
import com.wyc.cloudapp.utils.Utils;

public class PayMethodDialogImp extends AbstractPayDialog implements MobileCashierActivity.ScanCallback {
    PayMethodDialogImp(@NonNull SaleActivity context, @NonNull final JSONObject pay_method) {
        super(context,Utils.getNullStringAsEmpty(pay_method,"name"));
        mPayMethod = pay_method;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        initPayMethod();
        setWatcherToPayAmt();
    }

    @Override
    public JSONObject getContent() {
        mPayMethod.put("pay_code",getPayCode(mContext.getPosNum()));
        mPayMethod.put("pamt", mPayAmtEt.getText().toString());
        mPayMethod.put("pzl",0.00);
        mPayMethod.put("v_num",mPayCode.getText().toString());
         return mPayMethod;
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initPayMethod(){
        if (mPayMethod != null) {
            if (mPayMethod.getIntValue("is_check") != 2){ //显示付款码输入框
                mPayCode.postDelayed(()->mPayCode.requestFocus(),350);
                mPayCode.setVisibility(View.VISIBLE);

                if (mContext.lessThan7Inches()){
                    mPayCode.setOnTouchListener((view, motionEvent) -> {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            final float dx = motionEvent.getX();
                            final int w = mPayCode.getWidth();
                            if (dx > (w - mPayCode.getCompoundPaddingRight())) {
                                mPayCode.requestFocus();
                                final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                                mContext.startActivityForResult(intent, MessageID.PAY_REQUEST_CODE);
                            }
                        }
                        return false;
                    });
                    mContext.setScanCallback(this);
                }

                if (mPayMethod.containsKey("card_code"))
                    mPayCode.setText(mPayMethod.getString("card_code"));
                else
                    mPayCode.setHint(mPayMethod.getString("xtype"));

                //mPayAmtEt.setEnabled(false);
                if (Utils.equalDouble(mOriginalPayAmt,0.0)){
                    mPayAmtEt.setVisibility(View.GONE);
                }
            }else{
                mPayCode.clearFocus();

                mPayCode.setOnTouchListener(null);
                mContext.setScanCallback(null);

                mPayCode.getText().clear();
                mPayCode.setVisibility(View.GONE);
                mPayAmtEt.postDelayed(()-> mPayAmtEt.requestFocus(),300);
            }
        }
    }
    @Override
    public void callback(String code) {
        if (mPayCode.isShown()){
            mPayCode.setText(code);
            if (mOk != null)mOk.callOnClick();
        }
    }

    private void setWatcherToPayAmt(){
        //付款方式不能找零
        mPayAmtEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                if (length > 0){
                    int index = editable.toString().indexOf('.');
                    if (length == 1 && index > -1)return;
                    if (length >= (index += 3)){
                        Logger.d("index:%d",index);
                        editable.delete(index,editable.length());
                    }
                    if (Double.valueOf(editable.toString()) - mOriginalPayAmt> 0){
                        refreshContent();
                        MyDialog.SnackbarMessage(mDialogWindow,getTitle().concat(mContext.getString(R.string.not_zl_hint_sz)),null);
                    }
                }
            }
        });
    }
}
