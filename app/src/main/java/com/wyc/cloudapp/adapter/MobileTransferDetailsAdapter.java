package com.wyc.cloudapp.adapter;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

import java.util.Locale;

public final class MobileTransferDetailsAdapter extends AbstractTransferDetailsAdapter {
    public MobileTransferDetailsAdapter(MainActivity context,final JSONArray array){
        super(context);
        mDatas = array;
    }

    static class MyViewHolder extends AbstractTransferDetailsAdapter.MyViewHolder {
        TextView pay_m_name_tv,_order_num,_amt;
        MyViewHolder(View itemView) {
            super(itemView);
            pay_m_name_tv = itemView.findViewById(R.id.pay_m_name);
            _order_num = itemView.findViewById(R.id._order_num);
            _amt = itemView.findViewById(R.id._amt);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext,R.layout.mobile_transfer_details_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.height_40)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractTransferDetailsAdapter.MyViewHolder t_holder, int position) {
        super.onBindViewHolder(t_holder, position);
        if (null != mDatas) {
            final JSONObject pay_info = mDatas.getJSONObject(position);
            if (pay_info != null) {
                final MyViewHolder holder = (MyViewHolder) t_holder;

                if (position == mDatas.size() - 1) {
                    holder.pay_m_name_tv.setTextColor(Color.RED);
                }
                holder.pay_m_name_tv.setText(pay_info.getString("pay_name"));

                holder._order_num.setText(String.valueOf(pay_info.getIntValue("order_num")));

                holder._amt.setText(String.format(Locale.CHINA, "%.2f", pay_info.getDoubleValue("pay_money")));
                if (mTransferAmtNotVisible)
                    holder._amt.setTransformationMethod(editTextReplacement);
            }
        }
    }

    @Override
    public void setDatas(final String cas_id){
        final StringBuilder err = new StringBuilder();
        final String start_time = mTransferStartTime,ti_code = generateTransferIdOrderCode(),stores_id = mContext.getStoreId();

        Logger.d("start_time：%s",start_time);

        if (getTransferOrderCodes(ti_code,cas_id,stores_id,start_time,err) && getTransferDetailsInfo(cas_id,stores_id,start_time,err)){
            double cash_sum_amt = 0.0;

            cash_sum_amt += disposeTransferRetails(ti_code);
            cash_sum_amt += disposeTransferRefunds(ti_code);
            cash_sum_amt += disposeTransferDeposits(ti_code);
            cash_sum_amt += disposeTransferCardsc(ti_code);

            mTransferSumInfo.put("cas_id",cas_id);
            mTransferSumInfo.put("order_b_date",start_time);
            mTransferSumInfo.put("order_e_date",System.currentTimeMillis() / 1000);
            mTransferSumInfo.put("sj_money",cash_sum_amt);
            mTransferSumInfo.put("sum_money",cash_sum_amt);
            mTransferSumInfo.put("transfer_time",start_time);
            mTransferSumInfo.put("stores_id",stores_id);
            mTransferSumInfo.put("ti_code",ti_code);
        }
        if (err.length() != 0)mContext.runOnUiThread(()-> MyDialog.ToastMessage(err.toString(),mContext,null));
    }

    public void setDatas(final JSONArray array){
        mDatas = array;
        notifyDataSetChanged();
    }

}
