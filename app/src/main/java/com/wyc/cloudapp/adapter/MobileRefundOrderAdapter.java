package com.wyc.cloudapp.adapter;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.orderDialog.MobileRefundOrderDetailsDialog;
import com.wyc.cloudapp.logger.Logger;

import java.util.Locale;

public final class MobileRefundOrderAdapter extends AbstractQueryDataAdapter<MobileRefundOrderAdapter.MyViewHolder> {
    public MobileRefundOrderAdapter(final MainActivity activity){
        super(activity);
    }

    static class MyViewHolder extends AbstractQueryDataAdapter.SuperViewHolder {
        TextView  order_code,order_amt,order_status,cas_name,oper_time,m_vip_label,m_retail_order_detail;
        MyViewHolder(View itemView) {
            super(itemView);
            order_code = itemView.findViewById(R.id.order_code);
            order_amt = itemView.findViewById(R.id.m_refund_order_amt);
            order_status = itemView.findViewById(R.id.m_refund_order_status);
            cas_name = itemView.findViewById(R.id.m_refund_order_cas_name);
            oper_time = itemView.findViewById(R.id.m_refund_order_time);
            m_vip_label = itemView.findViewById(R.id.m_vip_label);

            m_retail_order_detail = itemView.findViewById(R.id.m_refund_order_detail);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.mobile_query_refund_order_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (null != mData) {
            final JSONObject order_info = mData.getJSONObject(position);
            if (order_info != null) {
                holder.order_code.setText(order_info.getString("refund_order_code"));
                holder.order_amt.setText(String.format(Locale.CHINA, "%.2f", order_info.getDoubleValue("refund_order_amt")));

                int order_status = order_info.getIntValue("refund_status");
                if (order_status == 4)
                    holder.order_status.setTextColor(mContext.getColor(R.color.orange_1));
                else
                    holder.order_status.setTextColor(mContext.getColor(R.color.mobile_order_status));

                holder.order_status.setText(order_info.getString("refund_status_name"));
                holder.order_status.setTag(order_info.getIntValue("refund_status"));

                holder.cas_name.setText(order_info.getString("cas_name"));

                holder.oper_time.setText(order_info.getString("oper_time"));

                String vip_name = order_info.getString("vip_name");
                if ("".equals(vip_name)){
                    vip_name = mContext.getString(R.string.not_vip_sz);
                }else {
                    vip_name = String.format(Locale.CHINA,"会员：%s(%s)",vip_name,order_info.getString("mobile"));
                }
                holder.m_vip_label.setText(vip_name);

                holder.itemView.setOnTouchListener(touchListener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0: mData.size();
    }

    private final View.OnTouchListener touchListener = (v, event) -> {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            setCurrentItemViewAndIndex(v);
            final TextView details_btn = v.findViewById(R.id.m_refund_order_detail);
            if (isClickView(details_btn,event.getX(),event.getY())){
                final MobileRefundOrderDetailsDialog detailsDialog = new MobileRefundOrderDetailsDialog(mContext,getCurrentOrder());
                detailsDialog.show();
            }
        }
        return v.performClick();
    };

    protected int getStatusViewId(){
        return R.id.m_refund_order_status;
    }

    @Override
    protected void setViewBackgroundColor(View view,boolean s){

    }

    @Override
    protected JSONObject getCurrentOrder(){
        if (null != mCurrentItemView){
            final TextView order_code_tv = mCurrentItemView.findViewById(R.id.order_code);
            if (null != order_code_tv){
                final String sz_order_code = order_code_tv.getText().toString();
                for (int i = 0, size = mData.size(); i < size; i ++){
                    final JSONObject object = mData.getJSONObject(i);
                    if (object != null && sz_order_code.equals(object.getString("refund_order_code"))){
                        return object;
                    }
                }
            }
        }
        return new JSONObject();
    }

    @Override
    public void setDatas(final String where_sql){
        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT  order_code retail_order_code,ro_code refund_order_code,total refund_order_amt,refund_total refund_amt,type refund_type,\n" +
                "a.card_code,a.name vip_name,a.mobile," +
                "case type when 1 then '整单退货' when 2 then '部分退货' when 3 then '单品退货'  else '其他' end refund_type_name,order_status refund_status,\n" +
                "case order_status when 1 then '未退货' when 2 then '已退货' else '其他' end refund_status_name,upload_status,\n" +
                "case a.transfer_status when 1 then '未交班' when 2 then '已交班' else '其他' end s_e_status_name,\n" +
                "case upload_status when 1 then '未上传' when 2 then '已上传' else '其他' end upload_status_name,cashier_id,b.cas_name,\n" +
                "datetime(a.addtime, 'unixepoch', 'localtime') oper_time FROM refund_order a left join cashier_info b on a.cashier_id = b.cas_id " + where_sql +" order by a.addtime desc";

        Logger.d("sql:%s",sql);
        mData = SQLiteHelper.getListToJson(sql,err);
        if (mData != null){
            notifyDataSetChanged();
        }else
            MyDialog.ToastMessage("加载退货单据错误：" + err, null);
    }
}
