package com.wyc.cloudapp.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.interface_abstract.AbstractPayDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class PayMethodDialog extends AbstractPayDialog {
    public PayMethodDialog(@NonNull Context context,@NonNull JSONObject pay_method) {//show_check_code 是否显示校验码输入框
        super(context);
        mPayMethod = pay_method;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        mOk.setText(R.string.OK);
        setTitle(mPayMethod.optString("name"));

        //初始化支付方式
        initPayMethod();

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
                if (editable.length()> 0){
                    int index = editable.toString().indexOf('.');
                    if (index > -1 && editable.length() >= (index += 3)){
                        Logger.d("index:%d",index);
                        editable.delete(index,editable.length());
                    }
                    if (Double.valueOf(editable.toString()) - mPayAmt> 0){
                        refreshContent();
                        MyDialog.ToastMessage("此付款方式不找零！",mContext);
                    }
                }
            }
        });

    }
    @Override
    public void setPayAmt(double amt) {
        super.setPayAmt(amt);
    }

    @Override
    public void refreshContent(){
        if (mPayAmtEt != null){
            mPayAmtEt.setText(String.format(Locale.CHINA,"%.2f",mPayAmt));
            mPayAmtEt.selectAll();
        }
    }

    @Override
    public JSONObject getContent() {
        try {
            if (mPayCode.getVisibility() == View.VISIBLE){
                if (mPayCode.getText().length() == 0){
                    mPayCode.requestFocus();
                    MyDialog.ToastMessage(mPayCode.getHint() + "不能为空！",mContext);
                    return null;
                }
            }
            mPayMethod.put("pay_code",getPayCode());
            mPayMethod.put("pamt", mPayAmtEt.getText().toString());
            mPayMethod.put("pzl",0.00);
            mPayMethod.put("v_num",mPayCode.getText().toString());
        } catch (JSONException e) {
            e.printStackTrace();
            MyDialog.ToastMessage("支付错误：" + e.getMessage(),mContext);
            return null;
        }
        return mPayMethod;
    }
    @Override
    protected void initPayMethod(){
        if (mPayMethod != null) {
            Logger.d_json(mPayMethod.toString());
            if (mPayMethod.optInt("is_check") != 2){ //显示付款码输入框
                mPayCode.postDelayed(()->mPayCode.requestFocus(),350);
                mPayCode.setVisibility(View.VISIBLE);
                mPayCode.setHint(mPayMethod.optString("xtype",""));
            }else{
                mPayCode.clearFocus();
                mPayCode.getText().clear();
                mPayCode.setVisibility(View.GONE);
            }
        }
    }
}
