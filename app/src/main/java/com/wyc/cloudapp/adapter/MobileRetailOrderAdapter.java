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
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.orderDialog.AbstractRetailOrderDetailsDialog;
import com.wyc.cloudapp.dialog.orderDialog.MobileRetailOrderDetailsDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.logger.Logger;

import java.util.Locale;

public final class MobileRetailOrderAdapter extends AbstractQueryDataAdapter<MobileRetailOrderAdapter.MyViewHolder> {
    public MobileRetailOrderAdapter(final MainActivity activity){
        mContext = activity;
    }

    static class MyViewHolder extends AbstractQueryDataAdapter.SuperViewHolder {
        TextView  order_code,goods_num,order_amt,order_status,cas_name,oper_time,m_vip_label,m_retail_order_detail,m_refund_retail_order;
        MyViewHolder(View itemView) {
            super(itemView);
            order_code = itemView.findViewById(R.id.order_code);
            goods_num = itemView.findViewById(R.id.m_retail_order_num);
            order_amt = itemView.findViewById(R.id.m_retail_order_amt);
            order_status = itemView.findViewById(R.id.m_retail_order_status);
            cas_name = itemView.findViewById(R.id.m_retail_order_cas_name);
            oper_time = itemView.findViewById(R.id.m_retail_order_time);
            m_vip_label = itemView.findViewById(R.id.m_vip_label);

            m_retail_order_detail = itemView.findViewById(R.id.m_retail_order_detail);
            m_refund_retail_order = itemView.findViewById(R.id.m_refund_retail_order);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.mobile_query_retail_order_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (null != mDatas) {
            final JSONObject order_info = mDatas.getJSONObject(position);
            if (order_info != null) {
                holder.order_code.setText(order_info.getString("order_code"));
                holder.order_amt.setText(String.format(Locale.CHINA, "%.2f", order_info.getDoubleValue("order_amt")));

                holder.goods_num.setText(String.format(Locale.CHINA, "%.2f", order_info.getDoubleValue("xnum")));

                int order_status = order_info.getIntValue("order_status");
                if (order_status == 4)
                    holder.order_status.setTextColor(mContext.getColor(R.color.orange_1));
                else
                    holder.order_status.setTextColor(mContext.getColor(R.color.mobile_order_status));

                holder.order_status.setText(order_info.getString("order_status_name"));
                holder.order_status.setTag(order_info.getIntValue("order_status"));

                holder.cas_name.setText(order_info.getString("cas_name"));

                holder.oper_time.setText(order_info.getString("oper_time"));

                String vip_name = order_info.getString("vip_name");
                if ("".equals(vip_name)){
                    vip_name = mContext.getString(R.string.not_vip_sz);
                }else {
                    vip_name = String.format(Locale.CHINA,"会员：%s(%s)",vip_name,order_info.getString("mobile"));
                }
                holder.m_vip_label.setText(vip_name);

                holder.mCurrentLayoutItemView.setOnTouchListener(touchListener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0: mDatas.size();
    }

    private final View.OnTouchListener touchListener = (v, event) -> {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            setmCurrentItemViewAndIndex(v);
            final TextView details_btn = v.findViewById(R.id.m_retail_order_detail),refund_btn = v.findViewById(R.id.m_refund_retail_order);
            if (isClickView(details_btn,event.getX(),event.getY())){
                final AbstractRetailOrderDetailsDialog retailOrderDetailsDialog = new MobileRetailOrderDetailsDialog(mContext,getCurrentOrder());
                retailOrderDetailsDialog.show();
            }else if (isClickView(refund_btn,event.getX(),event.getY())){
                refund_btn.post(()->{
                    if (RefundDialog.verifyRefundPermission(mContext)){
                        if (mContext.getSingleRefundStatus())mContext.setSingleRefundStatus(false);
                        final TextView order_code_tv = v.findViewById(R.id.order_code);
                        final RefundDialog refundDialog = new RefundDialog(mContext,order_code_tv.getText().toString());
                        refundDialog.show();
                    }
                });
            }
        }
        return v.performClick();
    };

    @Override
    public void setDatas(final String where_sql){
        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT \n" +
                "       a.remark," +
                "       a.card_code," +
                "       a.name vip_name," +
                "       a.mobile," +
                "       a.transfer_status s_e_status,\n" +
                "       case a.transfer_status when 1 then '未交班' when 2 then '已交班' else '其他' end s_e_status_name,\n" +
                "       a.upload_status,\n" +
                "       case a.upload_status when 1 then '未上传' when 2 then '已上传' else '其他' end upload_status_name,\n" +
                "       a.pay_status,\n" +
                "       case a.pay_status when 1 then '未支付' when 2 then '已支付' when 3 then '支付中' else '其他' end pay_status_name,\n" +
                "       a.order_status,\n" +
                "       case a.order_status when 1 then '未付款' when 2 then '已付款' when 3 then '已取消' when 4 then '已退货' else '其他'  end order_status_name,\n" +
                "       datetime(a.addtime, 'unixepoch', 'localtime') oper_time,\n" +
                "       a.remark,\n" +
                "       a.cashier_id,\n" +
                "       b.cas_name,\n" +
                "       a.discount_price reality_amt,\n" +
                "       a.total order_amt,\n" +
                "       a.order_code,\n" +
                "       ifnull(c.sc_name,'') sc_name,\n" +
                "       (select count(c.xnum) from retail_order_goods c where c.order_code = a.order_code) xnum\n" +
                "  FROM retail_order a left join cashier_info b on a.cashier_id = b.cas_id left join  sales_info c on a.sc_ids = c.sc_id " + where_sql + " order by a.addtime desc";

        Logger.d("sql:%s",sql);
        mDatas = SQLiteHelper.getListToJson(sql,err);
        if (mDatas != null){
            notifyDataSetChanged();
        }else
            MyDialog.ToastMessage("加载销售单据错误：" + err,mContext,null);
    }
}
