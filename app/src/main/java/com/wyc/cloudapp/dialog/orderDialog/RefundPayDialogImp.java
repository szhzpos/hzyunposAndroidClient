package com.wyc.cloudapp.dialog.orderDialog;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.PayMethodItemDecoration;
import com.wyc.cloudapp.adapter.PayMethodViewAdapter;
import com.wyc.cloudapp.dialog.pay.AbstractPayDialog;
import com.wyc.cloudapp.logger.Logger;

public class RefundPayDialogImp extends AbstractPayDialog {

    private PayMethodViewAdapter mPayMethodViewAdapter;

    RefundPayDialogImp(@NonNull MainActivity context) {
        super(context, "请选择退款方式");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPayAmtEt.setEnabled(false);
        initPayMethod();
    }

    @Override
    public JSONObject getContent() {
        final JSONObject pay_info = new JSONObject();
        if (mPayMethod == null)mPayMethodViewAdapter.setCurrentPayMethod();

        pay_info.put("pay_method",mPayMethod.getIntValue("pay_method_id"));
        pay_info.put("pay_method_name",mPayMethod.getString("name"));
        pay_info.put("pay_money", mPayAmtEt.getText().toString());
        pay_info.put("pay_code", getPayCode(mContext.getPosNum()));
        pay_info.put("is_check", mPayMethod.getIntValue("is_check"));

        return pay_info;
    }

    @Override
    protected void initPayMethod(){
        mPayMethodViewAdapter = new PayMethodViewAdapter(mContext,94);
        mPayMethodViewAdapter.loadRefundPayMeothd();
        mPayMethodViewAdapter.setOnItemClickListener((v, pos) -> {
            mPayMethod = mPayMethodViewAdapter.getItem(pos);
            if (mPayMethod != null) {
                Logger.d_json(mPayMethod.toString());

                if (mPayMethod.getIntValue("is_check") != 2){ //显示付款码输入框
                    mPayCode.setVisibility(View.VISIBLE);
                    mPayCode.requestFocus();
                    mPayCode.setHint(mPayMethod.getString("xtype"));
                }else{
                    mPayCode.callOnClick();
                    mPayCode.getText().clear();
                    mPayCode.setVisibility(View.GONE);
                    mPayAmtEt.requestFocus();
                }
            }
        });
        final RecyclerView recyclerView = findViewById(R.id.pay_method_list);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL,false));
        recyclerView.addItemDecoration(new PayMethodItemDecoration(2));
        recyclerView.setAdapter(mPayMethodViewAdapter);
    }

}
