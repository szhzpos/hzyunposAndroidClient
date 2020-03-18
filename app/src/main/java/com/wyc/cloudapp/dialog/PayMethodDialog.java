package com.wyc.cloudapp.dialog;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.interface_abstract.AbstractPayDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class PayMethodDialog extends AbstractPayDialog {
    private JSONObject mPayMethod;
    public PayMethodDialog(@NonNull Context context,@NonNull JSONObject pay_method) {//show_check_code 是否显示校验码输入框
        super(context);
        mPayMethod = pay_method;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //初始化支付方式
        initPayMethod();
    }
    @Override
    public void setPayAmt(double amt) {
        super.setPayAmt(amt);
    }
    @Override
    public JSONObject getPayContent() {
        try {
            mPayMethod.put("pamt","0.00");
            mPayMethod.put("pzl",mC_amt.getText());
        } catch (JSONException e) {
            mPayMethod = null;
            e.printStackTrace();
            MyDialog.ToastMessage("支付错误：" + e.getMessage(),mContext);
        }
        return mPayMethod;
    }
    @Override
    protected void initPayMethod(){
        if (mPayMethod != null) {
            Logger.d_json(mPayMethod.toString());

            ((TextView)super.findViewById(R.id.title)).setText(mPayMethod.optString("name"));

            if (mPayMethod.optInt("is_check") != 2){ //显示付款码输入框
                mPayCode.setVisibility(View.VISIBLE);
                mPayCode.setHint(mPayMethod.optString("xtype",""));
            }else
                mPayCode.setVisibility(View.GONE);
        }
    }

}
