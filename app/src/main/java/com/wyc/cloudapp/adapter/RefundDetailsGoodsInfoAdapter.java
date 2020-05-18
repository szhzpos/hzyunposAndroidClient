package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

import java.util.Locale;

public final class RefundDetailsGoodsInfoAdapter extends RecyclerView.Adapter<RefundDetailsGoodsInfoAdapter.MyViewHolder>  {
    private Context mContext;
    private JSONArray mDatas;
    private View mCurrentItemView;
    public RefundDetailsGoodsInfoAdapter(Context context){
        mContext = context;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView row_id_tv,item_no_tv,barcode_tv,goods_title_tv,refund_num_tv,unit_name_tv,refund_price_tv,refund_amt_tv;
        View mCurrentLayoutItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;

            row_id_tv = itemView.findViewById(R.id.row_id);
            item_no_tv = itemView.findViewById(R.id.item_no);
            barcode_tv = itemView.findViewById(R.id.barcode);
            goods_title_tv = itemView.findViewById(R.id.goods_title);
            refund_num_tv = itemView.findViewById(R.id.refund_num);
            unit_name_tv = itemView.findViewById(R.id.unit_name);
            refund_price_tv = itemView.findViewById(R.id.refund_price);
            refund_amt_tv = itemView.findViewById(R.id.refund_amt);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.refund_details_goods_info_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) mContext.getResources().getDimension(R.dimen.table_row_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (null != mDatas){
            final JSONObject sale_goods_info = mDatas.getJSONObject(position);
            if (sale_goods_info != null){
                holder.row_id_tv.setText(String.valueOf(position+1));
                holder.item_no_tv.setText(sale_goods_info.getString("item_no"));
                holder.barcode_tv.setText(sale_goods_info.getString("barcode"));
                holder.goods_title_tv.setText(sale_goods_info.getString("goods_title"));
                holder.refund_num_tv.setText(String.format(Locale.CHINA,"%.2f",sale_goods_info.getDoubleValue("refund_num")));
                holder.unit_name_tv.setText(sale_goods_info.getString("unit_name"));
                holder.refund_price_tv.setText(String.format(Locale.CHINA,"%.2f",sale_goods_info.getDoubleValue("refund_price")));
                holder.refund_amt_tv.setText(String.format(Locale.CHINA,"%.2f",sale_goods_info.getDoubleValue("refund_amt")));

                holder.mCurrentLayoutItemView.setOnClickListener(mItemClickListener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0: mDatas.size();
    }

    private void setViewBackgroundColor(View view,boolean s){
        if(view!= null){
            int item_color;
            if (s){
                item_color = mContext.getColor(R.color.listSelected);
            } else {
                item_color = mContext.getColor(R.color.white);
            }
            view.setBackgroundColor(item_color);
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

    private View.OnClickListener mItemClickListener = this::setCurrentItemView;

    private void setCurrentItemView(View v){
        if (mCurrentItemView == null){
            mCurrentItemView = v;
            setViewBackgroundColor(v,true);
        }else if(mCurrentItemView != v){
            setViewBackgroundColor(mCurrentItemView,false);
            mCurrentItemView = v;
            setViewBackgroundColor(v,true);
        }else {
            setViewBackgroundColor(v,false);
            mCurrentItemView = null;
        }
    }

    public void setDatas(final String ro_code){
        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT a.only_coding item_no,a.barcode,a.goods_title,a.type,a.unit_name,b.refund_num,b.refund_price,b.refund_num * b.refund_price refund_amt FROM " +
                "refund_order_goods b left join barcode_info a on a.barcode_id = b.barcode_id\n" +
                "where a.goods_status = 1 and a.barcode_status = 1 and b.ro_code = '" + ro_code + "'";

        Logger.d("sql:%s",sql);
        mDatas = SQLiteHelper.getListToJson(sql,err);
        if (mDatas != null){
            notifyDataSetChanged();
        }else{
            mDatas = new JSONArray();
            MyDialog.ToastMessage("加载商品明细错误：" + err,mContext,null);
        }
    }
    public JSONArray getSaleGoods(){
        return mDatas;
    }
}
