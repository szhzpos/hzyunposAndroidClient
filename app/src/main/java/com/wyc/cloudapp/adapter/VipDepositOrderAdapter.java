package com.wyc.cloudapp.adapter;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.vip.VipDepositDetailsDialog;

import java.util.Locale;

public class VipDepositOrderAdapter extends AbstractChargeOrderAdapter<VipDepositOrderAdapter.MyViewHolder> {
    public VipDepositOrderAdapter(final MainActivity activity){
        super(activity);
    }

    static class MyViewHolder extends AbstractQueryDataAdapter.SuperViewHolder {
        TextView row_id,card_code,mobile,vip_name,order_code,order_amt,give_amt,order_status,s_e_status,cas_name,oper_time,origin_order_code;
        MyViewHolder(View itemView) {
            super(itemView);
            row_id = itemView.findViewById(R.id.row_id);
            order_code = itemView.findViewById(R.id.order_code);
            origin_order_code = itemView.findViewById(R.id.origin_order_code);
            card_code = itemView.findViewById(R.id.card_code);
            mobile = itemView.findViewById(R.id.mobile);
            vip_name = itemView.findViewById(R.id.name);

            order_amt = itemView.findViewById(R.id.order_amt);

            give_amt = itemView.findViewById(R.id.give_amt);
            order_status = itemView.findViewById(R.id.order_status);

            s_e_status = itemView.findViewById(R.id.s_e_status);
            cas_name = itemView.findViewById(R.id.cas_name);

            oper_time = itemView.findViewById(R.id.oper_time);
        }
    }
    @NonNull
    @Override
    public VipDepositOrderAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.vip_deposit_order_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) mContext.getResources().getDimension(R.dimen.table_row_height)));
        return new VipDepositOrderAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (null != mDatas) {
            final JSONObject order_info = mDatas.getJSONObject(position);
            if (order_info != null) {
                holder.row_id.setText(String.valueOf(position + 1));
                holder.order_code.setText(order_info.getString("order_code"));
                holder.origin_order_code.setText(order_info.getString("origin_order_code"));
                holder.order_amt.setText(String.format(Locale.CHINA, "%.2f", order_info.getDoubleValue("order_amt")));
                holder.give_amt.setText(String.format(Locale.CHINA, "%.2f", order_info.getDoubleValue("give_amt")));

                holder.card_code.setText(order_info.getString("card_code"));
                holder.vip_name.setText(order_info.getString("name"));
                holder.mobile.setText(order_info.getString("mobile"));

                int order_status = order_info.getIntValue("status");
                if (order_status == 1)
                    holder.order_status.setTextColor(mContext.getColor(R.color.orange_1));

                holder.order_status.setText(order_info.getString("status_name"));
                holder.order_status.setTag(order_info.getIntValue("status"));

                holder.s_e_status.setText(order_info.getString("s_e_status_name"));
                holder.s_e_status.setTag(order_info.getIntValue("s_e_status"));

                holder.cas_name.setText(order_info.getString("cas_name"));

                holder.oper_time.setText(order_info.getString("oper_time"));

                holder.itemView.setOnTouchListener(touchListener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0: mDatas.size();
    }

    private final View.OnTouchListener touchListener = (v, event) -> {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            setCurrentItemView(v);
            final TextView order_code_tv = v.findViewById(R.id.order_code);

            if (isClickView(order_code_tv,event.getX(),event.getY())){
                final VipDepositDetailsDialog vipDepositDetailsDialog = new VipDepositDetailsDialog(mContext,getCurrentOrder());
                vipDepositDetailsDialog.show();
            }
        }
        v.performClick();
        return false;
    };
}
