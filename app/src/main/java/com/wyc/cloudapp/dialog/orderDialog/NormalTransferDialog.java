package com.wyc.cloudapp.dialog.orderDialog;

import android.os.Bundle;
import android.text.Editable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.NormalTransferDetailsAdapter;
import com.wyc.cloudapp.callback.PasswordEditTextReplacement;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class NormalTransferDialog extends AbstractTransferDialog {
    public NormalTransferDialog(@NonNull MainActivity context) {
        super(context);
        mTransferDetailsAdapter = new NormalTransferDetailsAdapter(mContext);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTransferInfoList();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.transfer_dialog_layout;
    }

    private void initTransferInfoList(){
        final RecyclerView transfer_list = findViewById(R.id.transfer_info_list);
        mTransferDetailsAdapter.setDatas(mContext.getCashierInfo().getString("cas_id"));
        transfer_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        transfer_list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        transfer_list.setAdapter(mTransferDetailsAdapter);

        setFooterInfo();
    }

    private void setFooterInfo(){
        final JSONObject object = mTransferDetailsAdapter.getTransferSumInfo();
        final TextView cas_name = findViewById(R.id.cas_name);
        cas_name.setText(mContext.getCashierInfo().getString("cas_name"));

        if (!object.isEmpty()){
            final TextView retail_sum_order_num_tv = findViewById(R.id.retail_sum_order_num),retail_sum_amt_tv = findViewById(R.id.retail_sum_amt),
                    refund_sum_order_num_tv = findViewById(R.id.refund_sum_order_num),refund_sum_amt_tv = findViewById(R.id.refund_sum_amt),
                    deposit_sum_order_num_tv = findViewById(R.id.deposit_sum_order_num),rdeposit_sum_amt_tv = findViewById(R.id.rdeposit_sum_amt),
                    transfer_time = findViewById(R.id.start_transfer_time),payable_amt = findViewById(R.id.payable_amt);

            boolean visible = mTransferDetailsAdapter.isTransferAmtNotVisible();
            if (visible){
                final PasswordEditTextReplacement editTextReplacement = new PasswordEditTextReplacement();
                retail_sum_amt_tv.setTransformationMethod(editTextReplacement);

                refund_sum_amt_tv.setTransformationMethod(editTextReplacement);

                rdeposit_sum_amt_tv.setTransformationMethod(editTextReplacement);

                payable_amt.setTransformationMethod(editTextReplacement);
            }

            retail_sum_order_num_tv.setText(object.getString("order_num"));

            retail_sum_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("order_money")));

            refund_sum_order_num_tv.setText(object.getString("refund_num"));
            refund_sum_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("refund_money")));

            deposit_sum_order_num_tv.setText(object.getString("recharge_num"));
            rdeposit_sum_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("recharge_money")));

            final Editable editable = transfer_time.getEditableText();
            editable.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(object.getLongValue("order_b_date") * 1000)).append(" ").append(mContext.getString(R.string.to_sz)).append(" ").
                    append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(object.getLongValue("order_e_date") * 1000));

            payable_amt.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("sj_money")));
        }
    }
}
