package com.wyc.cloudapp.adapter;

import android.content.DialogInterface;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.constants.RetailOrderStatus;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.orderDialog.AbstractMobileQueryDialog;
import com.wyc.cloudapp.dialog.orderDialog.MobileRetailOrderDetailsDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;
public final class MobileRetailOrderAdapter extends AbstractQueryDataAdapter<MobileRetailOrderAdapter.MyViewHolder> {
    protected AbstractMobileQueryDialog mDialog;
    public MobileRetailOrderAdapter(final AbstractMobileQueryDialog dialog){
        super(dialog.getPrivateContext());
        mDialog = dialog;
    }

    static class MyViewHolder extends AbstractQueryDataAdapter.SuperViewHolder {
        TextView  order_code,goods_num,order_amt,order_status,cas_name,oper_time,m_vip_label,m_retail_order_detail,m_action_retail_order;
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
            m_action_retail_order = itemView.findViewById(R.id.m_action_retail_order);
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
        if (null != mData) {
            final JSONObject order_info = mData.getJSONObject(position);
            if (order_info != null) {
                holder.order_code.setText(order_info.getString("order_code"));
                holder.order_amt.setText(String.format(Locale.CHINA, "%.2f", order_info.getDoubleValue("order_amt")));

                holder.goods_num.setText(String.format(Locale.CHINA, "%.2f", order_info.getDoubleValue("xnum")));

                int order_status = order_info.getIntValue("order_status");
                if (order_status != 2)
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

                int upload_status = order_info.getIntValue("upload_status");
                if (upload_status == RetailOrderStatus.UPLOAD_ERROR){
                    holder.m_action_retail_order.setText(R.string.reupload_sz);
                }

                holder.itemView.setOnTouchListener(touchListener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0: mData.size();
    }


    @Override
    protected void setViewBackgroundColor(View view,boolean s){

    }

    private final View.OnTouchListener touchListener = (v, event) -> {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            setCurrentItemViewAndIndex(v);
            final TextView details_btn = v.findViewById(R.id.m_retail_order_detail),refund_btn = v.findViewById(R.id.m_action_retail_order);

            final JSONObject curr = getCurrentOrder();
            if (isClickView(details_btn,event.getX(),event.getY())){
                final MobileRetailOrderDetailsDialog retailOrderDetailsDialog = new MobileRetailOrderDetailsDialog(mContext,curr);
                retailOrderDetailsDialog.show();
            }else if (isClickView(refund_btn,event.getX(),event.getY())){
                int order_status = Utils.getNotKeyAsNumberDefault(curr,"order_status",-1),upload_status = Utils.getNotKeyAsNumberDefault(curr,"upload_status",-1);
                if (upload_status == RetailOrderStatus.UPLOAD_ERROR){
                    CustomApplication.self().reupload_retail_order();
                }else if (order_status == 2 || order_status == 88){
                    CustomApplication.runInMainThread(()->{
                        if (RefundDialog.verifyRefundPermission(mContext)){
                            if (mContext.getSingleRefundStatus())mContext.setSingleRefundStatus(false);
                            final TextView order_code_tv = v.findViewById(R.id.order_code);
                            final RefundDialog refundDialog = new RefundDialog(mContext,order_code_tv.getText().toString());
                            refundDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                                @Override
                                public void onDismiss(DialogInterface dialog) {
                                    setDatas(mDialog.generateQueryCondition());
                                }
                            });
                            refundDialog.show();
                        }
                    });
                }else{
                    MyDialog.ToastMessage(v,Utils.getNullStringAsEmpty(curr,"order_status_name"), null);
                }
            }
        }
        return v.performClick();
    };

    protected int getStatusViewId(){
        return R.id.m_retail_order_status;
    }

    @Override
    public void setDatas(final String where_sql){
        final StringBuilder err = new StringBuilder();
        final String sql = RetailOrderAdapter.getQuery() + where_sql + " order by a.addtime desc";

        Logger.d("sql:%s",sql);
        mData = SQLiteHelper.getListToJson(sql,err);
        if (mData != null){
            notifyDataSetChanged();
        }else
            MyDialog.ToastMessage("加载充值订单错误：" + err, null);
    }
}
