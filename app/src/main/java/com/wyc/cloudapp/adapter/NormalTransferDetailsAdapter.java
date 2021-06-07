package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;

import java.util.Locale;

public final class NormalTransferDetailsAdapter extends AbstractTransferDetailsAdapter {
    public NormalTransferDetailsAdapter(MainActivity context){
        super(context);
    }

    static class MyViewHolder extends AbstractTransferDetailsAdapter.MyViewHolder {
        TextView pay_m_name_tv,retail_order_num_tv,retail_amt_tv,refund_order_num_tv,refund_amt_tv,deposit_order_num_tv,deposit_amt_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            pay_m_name_tv = itemView.findViewById(R.id.pay_m_name);
            retail_order_num_tv = itemView.findViewById(R.id.retail_order_num);
            retail_amt_tv = itemView.findViewById(R.id.retail_amt);
            refund_order_num_tv = itemView.findViewById(R.id.refund_order_num);
            refund_amt_tv = itemView.findViewById(R.id.refund_amt);
            deposit_order_num_tv = itemView.findViewById(R.id.deposit_order_num);
            deposit_amt_tv = itemView.findViewById(R.id.deposit_amt);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext,R.layout.transfer_details_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.height_40)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractTransferDetailsAdapter.MyViewHolder t_holder, int position) {
        super.onBindViewHolder(t_holder, position);
        if (null != mData) {
            final JSONObject pay_info = mData.getJSONObject(position);
            if (pay_info != null) {
                final MyViewHolder holder = (MyViewHolder) t_holder;
                boolean visible = mTransferAmtNotVisible;
                holder.pay_m_name_tv.setText(pay_info.getString("pay_m_name"));

                holder.retail_order_num_tv.setText(String.valueOf(pay_info.getIntValue("retail_order_num")));

                holder.retail_amt_tv.setText(String.format(Locale.CHINA, "%.2f", pay_info.getDoubleValue("retail_amt")));
                if (visible) holder.retail_amt_tv.setTransformationMethod(editTextReplacement);

                holder.refund_order_num_tv.setText(String.valueOf(pay_info.getIntValue("refund_order_num")));

                holder.refund_amt_tv.setText(String.format(Locale.CHINA, "%.2f", pay_info.getDoubleValue("refund_amt")));
                if (visible) holder.refund_amt_tv.setTransformationMethod(editTextReplacement);

                holder.deposit_order_num_tv.setText(String.valueOf(pay_info.getIntValue("deposit_order_num")));

                holder.deposit_amt_tv.setText(String.format(Locale.CHINA, "%.2f", pay_info.getDoubleValue("deposit_amt")));
                if (visible) holder.deposit_amt_tv.setTransformationMethod(editTextReplacement);
            }
        }
    }
}
