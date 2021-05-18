package com.wyc.cloudapp.adapter;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.orderDialog.AbstractMobileQueryDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.dialog.vip.AbstractVipChargeDialog;
import com.wyc.cloudapp.dialog.vip.MobileChargeOrderDetailsDialog;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;
public final class MobileChargeOrderAdapter extends AbstractChargeOrderAdapter<MobileChargeOrderAdapter.MyViewHolder> {
    protected AbstractMobileQueryDialog mDialog;
    public MobileChargeOrderAdapter(final AbstractMobileQueryDialog dialog){
        super(dialog.getPrivateContext());
        mDialog = dialog;
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

                int order_status = order_info.getIntValue("status");
                if (order_status == 3 || order_status == 6) {
                    int order_type = Utils.getNotKeyAsNumberDefault(order_info,"order_type",-1);
                    if ((order_type == 1 && order_status == 6) || (order_type == 2 && order_status == 3))
                        holder.m_refund_order.setVisibility(View.GONE);
                    else
                        holder.m_refund_order.setVisibility(View.VISIBLE);

                    holder.order_status.setTextColor(mContext.getColor(R.color.mobile_order_status));
                }else{
                    holder.order_status.setTextColor(mContext.getColor(R.color.orange_1));
                }

                holder.order_status.setText(order_info.getString("status_name"));
                holder.order_status.setTag(order_status);

                holder.cas_name.setText(order_info.getString("cas_name"));

                holder.oper_time.setText(order_info.getString("oper_time"));

                String vip_name = order_info.getString("name");
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
                    CustomApplication.runInMainThread(()->{
                        if (RefundDialog.verifyRefundPermission(mContext)){
                            final TextView order_code_tv = v.findViewById(R.id.order_code);
                            AbstractVipChargeDialog.vipRefundAmt(mContext,order_code_tv.getText().toString());
                            setDatas(mDialog.generateQueryCondition());
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
}
