package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public final class VipDepositDetailsPayInfoAdapter extends AbstractPayInfoAdapter<VipDepositDetailsPayInfoAdapter.MyViewHolder> {

    public VipDepositDetailsPayInfoAdapter(MainActivity context){
        super(context);
    }

    @Override
    public boolean isPaySuccess() {
        boolean success  = true;
        for (Object o : mData){
            if (o instanceof JSONObject){
                if(2 != Utils.getNotKeyAsNumberDefault((JSONObject)o,"status",1)){
                    success = false;
                    break;
                }
            }
        }
        return success;
    }

    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView row_id_tv,pay_method_name_tv,pay_amt_tv,pay_status_tv,pay_time_tv,pay_code_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            row_id_tv = itemView.findViewById(R.id.row_id);
            pay_method_name_tv = itemView.findViewById(R.id.pay_method_name);
            pay_amt_tv = itemView.findViewById(R.id.pay_amt);
            pay_status_tv = itemView.findViewById(R.id.pay_status);
            pay_time_tv = itemView.findViewById(R.id.pay_time);
            pay_code_tv = itemView.findViewById(R.id.pay_code);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext,mContext.lessThan7Inches() ? R.layout.mobile_vip_deposit_details_pay_info_content_layout : R.layout.vip_deposit_details_pay_info_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) mContext.getResources().getDimension(R.dimen.table_row_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (null != mData) {
            final JSONObject pay_info = mData.getJSONObject(position);
            if (pay_info != null) {
                holder.row_id_tv.setText(String.valueOf(position + 1));
                holder.pay_method_name_tv.setTag(pay_info.getIntValue("pay_method"));
                holder.pay_method_name_tv.setText(pay_info.getString("pay_method_name"));
                holder.pay_amt_tv.setText(String.format(Locale.CHINA, "%.2f", pay_info.getDoubleValue("pay_money")));
                holder.pay_status_tv.setText(pay_info.getString("status_name"));
                holder.pay_time_tv.setText(pay_info.getString("pay_time"));
                holder.pay_code_tv.setText(Utils.getNullStringAsEmpty(pay_info, "order_code_son"));

                holder.itemView.setOnClickListener(this::setCurrentItemView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0: mData.size();
    }

    public void setDatas(final String order_code){
        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT a.order_code,a.member_id,b.pay_method_id,b.name pay_method_name ,a.third_order_id order_code_son,a.status,\n" +
                "case a.status when 1 then '未支付' when 2 then '已支付' when 3 then '已完成' when 4 then '已关闭' when 5 then '待退款' when 6 then '已退款' else '其他' end status_name, \n" +
                "datetime(a.addtime, 'unixepoch', 'localtime') pay_time,b.is_check,a.order_money pay_money \n" +
                "FROM member_order_info a left join pay_method b on a.pay_method_id = b.pay_method_id where order_code = '"+ order_code +"'";

        Logger.d("sql:%s",sql);
        mData = SQLiteHelper.getListToJson(sql,err);
        if (mData != null){
            mContext.runOnUiThread(this::notifyDataSetChanged);
        }else{
            mData = new JSONArray();
            mContext.runOnUiThread(()->MyDialog.ToastMessage("加载付款明细错误：" + err, null));
        }
    }
}
