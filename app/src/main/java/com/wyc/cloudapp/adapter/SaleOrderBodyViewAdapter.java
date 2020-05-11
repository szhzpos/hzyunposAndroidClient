package com.wyc.cloudapp.adapter;

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
import com.wyc.cloudapp.callback.ClickListener;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.OrderDetaislDialog;
import com.wyc.cloudapp.logger.Logger;

import java.util.Locale;

public final class SaleOrderBodyViewAdapter extends RecyclerView.Adapter<SaleOrderBodyViewAdapter.MyViewHolder>  {
    private MainActivity mContext;
    private JSONArray mDatas;
    private View mCurrentItemView;
    public SaleOrderBodyViewAdapter(MainActivity context){
        mContext = context;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView row_id,order_code,order_amt,reality_amt,order_status,pay_status,s_e_status,cas_name,upload_status,oper_time;
        View mCurrentLayoutItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;

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
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.sale_order_body_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) mContext.getResources().getDimension(R.dimen.table_row_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (null != mDatas){
            final JSONObject order_info = mDatas.getJSONObject(position);
            if (order_info != null){
                holder.row_id.setText(String.valueOf(position+1));
                holder.order_code.setText(order_info.getString("order_code"));
                holder.order_amt.setText(String.format(Locale.CHINA,"%.2f",order_info.getDoubleValue("order_amt")));
                holder.reality_amt.setText(String.format(Locale.CHINA,"%.2f",order_info.getDoubleValue("reality_amt")));
                holder.order_status.setText(order_info.getString("order_status_name"));
                holder.order_status.setTag(order_info.getIntValue("order_status"));
                holder.pay_status.setText(order_info.getString("pay_status_name"));
                holder.pay_status.setTag(order_info.getIntValue("pay_status"));
                holder.s_e_status.setText(order_info.getString("s_e_status_name"));
                holder.s_e_status.setTag(order_info.getIntValue("s_e_status"));
                holder.cas_name.setText(order_info.getString("cas_name"));
                holder.cas_name.setTag(order_info.getIntValue("cas_id"));
                holder.upload_status.setText(order_info.getString("upload_status_name"));
                holder.upload_status.setTag(order_info.getIntValue("upload_status"));
                holder.oper_time.setText(order_info.getString("oper_time"));
            }

            holder.mCurrentLayoutItemView.setOnTouchListener(mItemClickListener);
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0: mDatas.size();
    }

    private void setViewBackgroundColor(View view,boolean s){
        if(view!= null){
            int white = mContext.getColor(R.color.white);
            if (s){
                view.setBackgroundColor(mContext.getColor(R.color.listSelected));
                if (view instanceof LinearLayout){
                    LinearLayout linearLayout = (LinearLayout)view;
                    int count = linearLayout.getChildCount();
                    View ch;
                    for (int i = 0;i < count;i++){
                        ch = linearLayout.getChildAt(i);
                        if (ch instanceof TextView){
                            ((TextView) ch).setTextColor(white);
                        }
                    }
                }
            }else{
                view.setBackgroundColor(white);
                if (view instanceof LinearLayout){
                    LinearLayout linearLayout = (LinearLayout)view;
                    int count = linearLayout.getChildCount();
                    View ch;
                    for (int i = 0;i < count;i++){
                        ch = linearLayout.getChildAt(i);
                        if (ch instanceof TextView){
                            ((TextView) ch).setTextColor(mContext.getColor(R.color.text_color));
                        }
                    }
                }
            }
        }
    }

    private ClickListener mItemClickListener =  new ClickListener(v -> {
        setCurrentItemView(v);
        OrderDetaislDialog orderDetaislDialog = new OrderDetaislDialog(mContext,mContext.getString(R.string.order_detail_sz),getCurrentOrder());
        orderDetaislDialog.show();
    }, this::setCurrentItemView);

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

    private JSONObject getCurrentOrder(){
        if (null != mCurrentItemView){
            final TextView order_code_tv = mCurrentItemView.findViewById(R.id.order_code);
            if (null != order_code_tv){
                final String sz_order_code = order_code_tv.getText().toString();
                for (int i = 0,size = mDatas.size();i < size;i ++){
                    final JSONObject object = mDatas.getJSONObject(i);
                    if (object != null && sz_order_code.equals(object.getString("order_code"))){
                        return object;
                    }
                }
            }
        }
        return new JSONObject();
    }

    public void setDatas(final String where_sql){
        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT \n" +
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
                "  FROM retail_order a left join cashier_info b on a.cashier_id = b.cas_id " + where_sql;

        Logger.d("sql:%s",sql);
        mDatas = SQLiteHelper.getListToJson(sql,err);
        if (mDatas != null){
            notifyDataSetChanged();
        }else
            MyDialog.ToastMessage("加载销售单据错误：" + err,mContext,null);
    }
}
