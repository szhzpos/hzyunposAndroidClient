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
import com.wyc.cloudapp.dialog.vip.MobileChargeOrderDetailsDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;
public final class MobileChargeOrderAdapter extends AbstractQueryDataAdapter<MobileChargeOrderAdapter.MyViewHolder> {
    public MobileChargeOrderAdapter(final MainActivity activity){
        mContext = activity;
    }

    static class MyViewHolder extends AbstractQueryDataAdapter.SuperViewHolder {
        TextView  order_code,order_amt,order_status,cas_name,oper_time,m_vip_label,m_order_detail,m_refund_order;
        MyViewHolder(View itemView) {
            super(itemView);
            order_code = itemView.findViewById(R.id.order_code);
            order_amt = itemView.findViewById(R.id.m_charge_order_amt);
            order_status = itemView.findViewById(R.id.m_charge_order_status);
            cas_name = itemView.findViewById(R.id.m_charge_order_cas_name);
            oper_time = itemView.findViewById(R.id.m_charge_order_time);
            m_vip_label = itemView.findViewById(R.id.m_vip_label);

            m_order_detail = itemView.findViewById(R.id.m_order_detail);
            m_refund_order = itemView.findViewById(R.id.m_refund_order);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.mobile_vip_charge_order_content_layout, null);
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

                int order_status = order_info.getIntValue("order_status");
                if (order_status != 2)
                    holder.order_status.setTextColor(mContext.getColor(R.color.orange_1));
                else
                    holder.order_status.setTextColor(mContext.getColor(R.color.mobile_order_status));

                holder.order_status.setText(order_info.getString("status_name"));
                holder.order_status.setTag(order_info.getIntValue("status"));

                holder.cas_name.setText(order_info.getString("cas_name"));

                holder.oper_time.setText(order_info.getString("oper_time"));

                String vip_name = order_info.getString("name");
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

    @Override
    protected void setViewBackgroundColor(View view,boolean s){

    }

    private final View.OnTouchListener touchListener = (v, event) -> {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            setCurrentItemViewAndIndex(v);
            final TextView details_btn = v.findViewById(R.id.m_order_detail),refund_btn = v.findViewById(R.id.m_refund_order);

            if (isClickView(details_btn,event.getX(),event.getY())){
                final MobileChargeOrderDetailsDialog chargeOrderDetailsDialog = new MobileChargeOrderDetailsDialog(mContext,getCurrentOrder());
                chargeOrderDetailsDialog.show();
            }else if (isClickView(refund_btn,event.getX(),event.getY())){
                if (Utils.getNotKeyAsNumberDefault(getCurrentOrder(),"order_status",2) == 2){
                    refund_btn.post(()->{
                        if (RefundDialog.verifyRefundPermission(mContext)){
                            if (mContext.getSingleRefundStatus())mContext.setSingleRefundStatus(false);
                            final TextView order_code_tv = v.findViewById(R.id.order_code);
                            final RefundDialog refundDialog = new RefundDialog(mContext,order_code_tv.getText().toString());
                            refundDialog.show();
                        }
                    });
                }else{
                    MyDialog.ToastMessage(v,"订单状态不正常不能退款!",mContext,null);
                }
            }
        }
        return v.performClick();
    };

    protected int getStatusViewId(){
        return R.id.m_charge_order_status;
    }


    public void setDatas(final String where_sql){
        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT \n" +
                "       datetime(a.addtime, 'unixepoch', 'localtime') oper_time,\n" +
                "       case transfer_status when 1 then '未交班' when 2 then '已交班' else '其他' end s_e_status_name,\n" +
                "       status ,\n" +
                "       case status when 1 then '未付款' when '2' then '已付款' when '3' then '已完成' when '4' then '已关闭' end status_name,\n" +
                "       b.cas_name,\n" +
                "       name,\n" +
                "       mobile,\n" +
                "       card_code,\n" +
                "       order_money order_amt,\n" +
                "       give_money give_amt,\n" +
                "       order_code,\n" +
                "       ifnull(c.sc_name,'') sc_name\n" +
                "  FROM member_order_info a left join cashier_info b on a.cashier_id = b.cas_id left join sales_info c on a.sc_id = c.sc_id " + where_sql;

        Logger.d("sql:%s",sql);
        mDatas = SQLiteHelper.getListToJson(sql,err);
        if (mDatas != null){
            notifyDataSetChanged();
        }else
            MyDialog.ToastMessage("加载充值订单错误：" + err,mContext,null);
    }
}
