package com.wyc.cloudapp.dialog.orderDialog;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.PayMethodViewAdapter;
import com.wyc.cloudapp.decoration.GridItemDecoration;
import com.wyc.cloudapp.decoration.SuperItemDecoration;
import com.wyc.cloudapp.dialog.pay.AbstractPayDialog;

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
        if (mPayMethod == null)mPayMethod = mPayMethodViewAdapter.getDefaultPayMethod();

        pay_info.put("pay_method",mPayMethod.getIntValue("pay_method_id"));
        pay_info.put("pay_method_name",mPayMethod.getString("name"));
        pay_info.put("pay_money", mPayAmtEt.getText().toString());
        pay_info.put("pay_code", getPayCode(mContext.getPosNum()));
        pay_info.put("is_check", mPayMethod.getIntValue("is_check"));

        return pay_info;
    }

    @Override
    protected void initPayMethod(){
        final PayMethodViewAdapter payMethodViewAdapter = new PayMethodViewAdapter(mContext,null);
        payMethodViewAdapter.loadRefundPayMethod();
        payMethodViewAdapter.setOnItemClickListener((object) -> {
            mPayMethod = object;
            final EditText pay_code = mPayCode;
            if (mPayMethod.getIntValue("is_check") != 2){ //显示付款码输入框
                pay_code.setVisibility(View.VISIBLE);
                pay_code.requestFocus();
                pay_code.setHint(mPayMethod.getString("xtype"));
            }else{
                pay_code.callOnClick();
                pay_code.getText().clear();
                pay_code.setVisibility(View.GONE);
                mPayAmtEt.requestFocus();
            }
        });
        final RecyclerView recyclerView = findViewById(R.id.pay_method_list);
        recyclerView.setVisibility(View.VISIBLE);
        recyclerView.setLayoutManager(new GridLayoutManager(mContext,4));
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(recyclerView,mContext.getResources().getDimension(R.dimen.pay_method_height),new GridItemDecoration());
        recyclerView.setAdapter(payMethodViewAdapter);

        mPayMethodViewAdapter = payMethodViewAdapter;
    }

}
