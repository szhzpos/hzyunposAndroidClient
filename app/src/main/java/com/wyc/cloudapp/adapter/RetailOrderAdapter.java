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
import com.wyc.cloudapp.constants.RetailOrderStatus;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.dialog.orderDialog.AbstractRetailOrderDetailsDialog;
import com.wyc.cloudapp.dialog.orderDialog.NormalRetailOrderDetailsDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.logger.Logger;

import java.util.Locale;

public final class RetailOrderAdapter extends AbstractQueryDataAdapter<RetailOrderAdapter.MyViewHolder> {
    private AbstractDialogMainActivity mDialog;
    public RetailOrderAdapter(final AbstractDialogMainActivity dialogMainActivity){
        super(dialogMainActivity.getPrivateContext());
        mDialog = dialogMainActivity;
    }

    static class MyViewHolder extends AbstractQueryDataAdapter.SuperViewHolder {
        TextView row_id,order_code,order_amt,reality_amt,order_status,pay_status,s_e_status,cas_name,upload_status,oper_time,order_action;
        MyViewHolder(View itemView) {
            super(itemView);
            row_id = itemView.findViewById(R.id.row_id);
            order_code = itemView.findViewById(R.id.order_code);
            order_amt = itemView.findViewById(R.id.order_amt);
            reality_amt = itemView.findViewById(R.id.reality_amt);
            order_status = itemView.findViewById(R.id.order_status);
            pay_status = itemView.findViewById(R.id.pay_status);
            s_e_status = itemView.findViewById(R.id.s_e_status);
            cas_name = itemView.findViewById(R.id.cas_name);
            upload_status = itemView.findViewById(R.id.upload_status);
            oper_time = itemView.findViewById(R.id.oper_time);
            order_action = findViewById(R.id.order_action);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.retail_order_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) mContext.getResources().getDimension(R.dimen.table_row_height)));

        itemView.setOnTouchListener(touchListener);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (null != mData) {
            final JSONObject order_info = mData.getJSONObject(position);
            if (order_info != null) {
                holder.row_id.setText(String.valueOf(position + 1));
                holder.order_code.setText(order_info.getString("order_code"));
                holder.order_amt.setText(String.format(Locale.CHINA, "%.2f", order_info.getDoubleValue("order_amt")));
                holder.reality_amt.setText(String.format(Locale.CHINA, "%.2f", order_info.getDoubleValue("reality_amt")));

                int order_status = order_info.getIntValue("order_status");
                if (order_status != 2)
                    holder.order_status.setTextColor(mContext.getColor(R.color.orange_1));
                else
                    holder.order_status.setTextColor(mContext.getColor(R.color.text_color));

                holder.order_status.setText(order_info.getString("order_status_name"));
                holder.order_status.setTag(order_status);

                holder.pay_status.setText(order_info.getString("pay_status_name"));

                holder.s_e_status.setText(order_info.getString("s_e_status_name"));

                holder.cas_name.setText(order_info.getString("cas_name"));

                int upload_status = order_info.getIntValue("upload_status");
                if (upload_status == RetailOrderStatus.UPLOAD_ERROR){
                    holder.order_action.setText(R.string.reupload_sz);
                }
                holder.upload_status.setText(order_info.getString("upload_status_name"));
                holder.upload_status.setTag(upload_status);

                holder.oper_time.setText(order_info.getString("oper_time"));

                holder.itemView.setTag(order_info);//保存订单信息
            }
        }
    }



    @Override
    public int getItemCount() {
        return mData == null ? 0: mData.size();
    }

    private final View.OnTouchListener touchListener = (v, event) -> {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            setCurrentItemView(v);
            final TextView order_code_tv = v.findViewById(R.id.order_code),order_action = v.findViewById(R.id.order_action),order_status_tv = v.findViewById(R.id.order_status);

            final JSONObject cur_order = (JSONObject) v.getTag();

            if (isClickView(order_code_tv,event.getX(),event.getY())){
                final AbstractRetailOrderDetailsDialog retailOrderDetailsDialog = new NormalRetailOrderDetailsDialog(mContext,cur_order);
                retailOrderDetailsDialog.show();
            }else if (isClickView(order_action,event.getX(),event.getY())){
                int order_status = cur_order.getIntValue("order_status"),upload_status = cur_order.getIntValue("upload_status");
                if (upload_status == RetailOrderStatus.UPLOAD_ERROR){//启动重传
                    CustomApplication.self().reupload_retail_order();
                }else if (order_status == 2 || order_status == 88){
                    CustomApplication.runInMainThread(()->{
                        if (RefundDialog.verifyRefundPermission(mContext)){
                            if (mContext.getSingleRefundStatus())mContext.setSingleRefundStatus(false);
                            final RefundDialog refundDialog = new RefundDialog(mContext,order_code_tv.getText().toString());
                            refundDialog.show();
                            mDialog.dismiss();
                        }
                    });
                }else{
                    order_status_tv.postDelayed(()-> MyDialog.ToastMessage(order_status_tv,order_status_tv.getText().toString(),mContext,mDialog.getWindow()),100);
                }
            }
        }
        return v.performClick();
    };

    @Override
    public void setDatas(final String where_sql){
        final StringBuilder err = new StringBuilder();
        final String sql = getQuery() + where_sql;

        Logger.d("sql:%s",sql);
        mData = SQLiteHelper.getListToJson(sql,err);
        if (mData != null){
            notifyDataSetChanged();
        }else
            MyDialog.ToastMessage("加载销售单据错误：" + err,mContext,null);
    }
    public static String getQuery(){
        return "SELECT \n" +
                "       a.remark," +
                "       a.card_code," +
                "       a.name vip_name," +
                "       a.mobile," +
                "       a.transfer_status s_e_status,\n" +
                "       case a.transfer_status when 1 then '未交班' when 2 then '已交班' else '其他' end s_e_status_name,\n" +
                "       a.upload_status,\n" +
                "       case a.upload_status when 1 then '未上传' when 2 then '已上传' when 3 then '失败' else '其他' end upload_status_name,\n" +
                "       a.pay_status,\n" +
                "       case a.pay_status when 1 then '未支付' when 2 then '已支付' when 3 then '支付中' else '其他' end pay_status_name,\n" +
                "       a.order_status,\n" +
                "       case a.order_status when 1 then '未付款' when 2 then '已付款' when 3 then '已取消' when 4 then '已退货' when 88 then '部分退货' else '其他'  end order_status_name,\n" +
                "       datetime(a.addtime, 'unixepoch', 'localtime') oper_time,\n" +
                "       a.remark,\n" +
                "       a.cashier_id,\n" +
                "       b.cas_name,\n" +
                "       a.discount_price reality_amt,\n" +
                "       a.total order_amt,\n" +
                "       a.order_code\n" +
                "  FROM retail_order a left join cashier_info b on a.cashier_id = b.cas_id ";
    }
}
