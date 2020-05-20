package com.wyc.cloudapp.adapter;

import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundOrderDetailsDialog;
import com.wyc.cloudapp.dialog.orderDialog.RetailOrderDetailsDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public final class RefundOrderViewAdapter extends RecyclerView.Adapter<RefundOrderViewAdapter.MyViewHolder>  {
    private MainActivity mContext;
    private JSONArray mDatas;
    private View mCurrentItemView;
    public RefundOrderViewAdapter(final MainActivity context){
        mContext = context;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView row_id,retail_order_code,refund_order_code,refund_order_amt,refund_amt,refund_type,refund_status,cas_name,upload_status,oper_time;
        View mCurrentLayoutItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;

            row_id = itemView.findViewById(R.id.row_id);
            retail_order_code = itemView.findViewById(R.id.retail_order_code);
            refund_order_code = itemView.findViewById(R.id.refund_order_code);
            refund_order_amt = itemView.findViewById(R.id.refund_order_amt);
            refund_amt = itemView.findViewById(R.id.refund_amt);
            refund_type = itemView.findViewById(R.id.refund_type);
            refund_status = itemView.findViewById(R.id.refund_status);
            cas_name = itemView.findViewById(R.id.cas_name);
            upload_status = itemView.findViewById(R.id.upload_status);
            oper_time = itemView.findViewById(R.id.oper_time);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.refund_order_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) mContext.getResources().getDimension(R.dimen.table_row_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (null != mDatas){
            final JSONObject order_info = mDatas.getJSONObject(position);
            if (order_info != null){
                holder.row_id.setText(String.valueOf(position+1));
                holder.retail_order_code.setText(order_info.getString("retail_order_code"));
                holder.refund_order_code.setText(order_info.getString("refund_order_code"));
                holder.refund_order_amt.setText(String.format(Locale.CHINA,"%.2f",order_info.getDoubleValue("refund_order_amt")));
                holder.refund_amt.setText(String.format(Locale.CHINA,"%.2f",order_info.getDoubleValue("refund_amt")));

                int order_status = order_info.getIntValue("refund_status");
                if (order_status == 1)
                    holder.refund_status.setTextColor(mContext.getColor(R.color.orange_1));
                else
                    holder.refund_status.setTextColor(mContext.getColor(R.color.text_color));
                holder.refund_status.setText(order_info.getString("refund_status_name"));
                holder.refund_status.setTag(order_info.getIntValue("refund_status"));

                holder.refund_type.setText(order_info.getString("refund_type_name"));


                holder.cas_name.setText(order_info.getString("cas_name"));

                holder.upload_status.setText(order_info.getString("upload_status_name"));

                holder.oper_time.setText(order_info.getString("oper_time"));

                holder.mCurrentLayoutItemView.setOnTouchListener(touchListener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0: mDatas.size();
    }

    private void setViewBackgroundColor(View view,boolean s){
        if(view!= null) {
            View child;
            int selected_color, item_color,text_color;
            if (s) {
                selected_color = mContext.getColor(R.color.listSelected);
                item_color = Color.YELLOW;
                text_color = mContext.getColor(R.color.white);
            }else {
                selected_color = mContext.getColor(R.color.white);
                item_color = mContext.getColor(R.color.appColor);
                text_color = mContext.getColor(R.color.text_color);
            }
            view.setBackgroundColor(selected_color);
            if (view instanceof LinearLayout){
                LinearLayout linearLayout = (LinearLayout)view;
                int count = linearLayout.getChildCount();
                for (int i = 0;i < count;i++){
                    child = linearLayout.getChildAt(i);
                    if (child instanceof TextView){
                        final TextView tv = ((TextView) child);
                        switch (tv.getId()) {
                            case R.id.retail_order_code:
                            case R.id.refund_order_code:
                                tv.setTextColor(item_color);
                                break;
                            case R.id.refund_status:
                                if (Utils.getViewTagValue(child,2) == 1){
                                    tv.setTextColor(mContext.getColor(R.color.orange_1));
                                }else{
                                    tv.setTextColor(text_color);
                                }
                                break;
                            default:
                                tv.setTextColor(text_color);
                                break;
                        }
                    }
                }
            }
        }
    }

    private void setCurrentItemView(View v){
        if (mCurrentItemView == null){
            mCurrentItemView = v;
            setViewBackgroundColor(v,true);
        }else if(mCurrentItemView != v){
            setViewBackgroundColor(mCurrentItemView,false);
            mCurrentItemView = v;
            setViewBackgroundColor(v,true);
        }
    }

    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN){
                setCurrentItemView(v);
                final TextView order_code_tv = v.findViewById(R.id.retail_order_code),sale_refund_tv = v.findViewById(R.id.refund_order_code);
                if (isClickView(order_code_tv,event.getX(),event.getY())){
                    RetailOrderDetailsDialog retailOrderDetailsDialog = new RetailOrderDetailsDialog(mContext,getCurrentRetailOrder());
                    retailOrderDetailsDialog.show();
                }else if (isClickView(sale_refund_tv,event.getX(),event.getY())){
                    RefundOrderDetailsDialog refundOrderDetailsDialog = new RefundOrderDetailsDialog(mContext,getCurentRefundOrder());
                    refundOrderDetailsDialog.show();
                }
            }
            v.performClick();
            return false;
        }
    };

    private boolean isClickView(final View view,float x,float y){
        if (view == null)return false;
        float v_x = view.getX(),v_y = view.getY();
        return x >= v_x && x <= v_x + view.getWidth() && y >= v_y && y <= v_y + view.getHeight();
    }

    private JSONObject getCurrentRetailOrder(){
        final JSONObject retail_order_info = new JSONObject();
        if (null != mCurrentItemView){
            final TextView order_code_tv = mCurrentItemView.findViewById(R.id.retail_order_code);
            if (null != order_code_tv){
                final String sz_order_code = order_code_tv.getText().toString(),
                        sql = "SELECT \n" +
                        "       a.remark," +
                        "       a.card_code," +
                        "       a.name vip_name," +
                        "       a.mobile," +
                        "       a.transfer_status s_e_status,\n" +
                        "       case a.transfer_status when 1 then '未交班' when 2 then '已交班' else '其他' end s_e_status_name,\n" +
                        "       a.upload_status,\n" +
                        "       case a.upload_status when 1 then '未上传' when 2 then '已上传' else '其他' end upload_status_name,\n" +
                        "       a.pay_status,\n" +
                        "       case a.pay_status when 1 then '未支付' when 2 then '已支付' else '支付中' end pay_status_name,\n" +
                        "       a.order_status,\n" +
                        "       case a.order_status when 1 then '未付款' when 2 then '已付款' when 3 then '已取消' when 4 then '已退货' else '其他'  end order_status_name,\n" +
                        "       datetime(a.addtime, 'unixepoch', 'localtime') oper_time,\n" +
                        "       a.remark,\n" +
                        "       a.cashier_id,\n" +
                        "       b.cas_name,\n" +
                        "       a.discount_price reality_amt,\n" +
                        "       a.total order_amt,\n" +
                        "       a.order_code\n" +
                        "  FROM retail_order a left join cashier_info b on a.cashier_id = b.cas_id where order_code = '" + sz_order_code + "'";

                if (!SQLiteHelper.execSql(retail_order_info,sql)){
                    MyDialog.ToastMessage("查询零售单据错误：" + retail_order_info.getString("info"),mContext,null);
                }
            }
        }
        return retail_order_info;
    }
    private JSONObject getCurentRefundOrder(){
        if (null != mCurrentItemView){
            final TextView order_code_tv = mCurrentItemView.findViewById(R.id.refund_order_code);
            if (null != order_code_tv){
                final String sz_order_code = order_code_tv.getText().toString();
                for (int i = 0,size = mDatas.size();i < size;i ++){
                    final JSONObject object = mDatas.getJSONObject(i);
                    if (object != null && sz_order_code.equals(object.getString("refund_order_code"))){
                        return object;
                    }
                }
            }
        }
        return null;
    }

    public void setDatas(final String where_sql){
        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT  order_code retail_order_code,ro_code refund_order_code,total refund_order_amt,refund_total refund_amt,type refund_type,\n" +
                "case type when 1 then '整单退货' when 2 then '部分退货' else '其他' end refund_type_name,order_status refund_status,\n" +
                "case order_status when 1 then '未退货' when 2 then '已退货' else '其他' end refund_status_name,upload_status,\n" +
                " case a.transfer_status when 1 then '未交班' when 2 then '已交班' else '其他' end s_e_status_name,\n" +
                "case upload_status when 1 then '未上传' when 2 then '已上传' else '其他' end upload_status_name,cashier_id,b.cas_name,\n" +
                "datetime(a.addtime, 'unixepoch', 'localtime') oper_time FROM refund_order a left join cashier_info b on a.cashier_id = b.cas_id " + where_sql;

        Logger.d("sql:%s",sql);
        mDatas = SQLiteHelper.getListToJson(sql,err);
        if (mDatas != null){
            notifyDataSetChanged();
        }else
            MyDialog.ToastMessage("加载退货单据错误：" + err,mContext,null);
    }
}
