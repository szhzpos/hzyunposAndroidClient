package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public final class MobileRefundDetailsPayInfoAdapter extends AbstractPayInfoAdapter<MobileRefundDetailsPayInfoAdapter.MyViewHolder> {
    public MobileRefundDetailsPayInfoAdapter(MainActivity context){
        super(context);
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
        View itemView = View.inflate(mContext,R.layout.mobile_refund_details_pay_info_content_layout, null);
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
                holder.pay_method_name_tv.setText(pay_info.getString("name"));
                holder.pay_amt_tv.setText(String.format(Locale.CHINA, "%.2f", pay_info.getDoubleValue("pamt")));
                holder.pay_status_tv.setText(pay_info.getString("pay_status_name"));
                holder.pay_time_tv.setText(pay_info.getString("pay_time"));
                holder.pay_code_tv.setText(Utils.getNullStringAsEmpty(pay_info, "order_code_son"));
            }
        }
    }

    @Override
    public void setDatas(final String order_code){
        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT b.name,a.pay_code order_code_son,a.pay_serial_no pay_code,a.pay_status,case a.pay_status when 1 then '未支付' when 2 then '已支付' else '支付中' end pay_status_name," +
                "datetime(a.pay_time, 'unixepoch', 'localtime') pay_time,a.is_check,a.pay_money pamt,a.pay_method," +
                "b.unified_pay_query FROM refund_order_pays a left join pay_method b on a.pay_method = b.pay_method_id where ro_code = '" + order_code + "'";

        Logger.d("sql:%s",sql);
        mData = SQLiteHelper.getListToJson(sql,err);
        if (mData != null){
            mContext.runOnUiThread(this::notifyDataSetChanged);
        }else{
            mData = new JSONArray();
            mContext.runOnUiThread(()->MyDialog.ToastMessage("加载付款明细错误：" + err, null));
        }
    }

    @Override
    public boolean isPaySuccess() {
        boolean success  = true;
        for (Object o : mData){
            if (o instanceof JSONObject){
                if(2 != Utils.getNotKeyAsNumberDefault((JSONObject)o,"pay_status",1)){
                    success = false;
                    break;
                }
            }
        }
        return success;
    }
}
