package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

import java.util.Locale;

public final class MobileRefundDetailsGoodsInfoAdapter extends AbstractTableDataAdapter<MobileRefundDetailsGoodsInfoAdapter.MyViewHolder> {

    public MobileRefundDetailsGoodsInfoAdapter(MainActivity context){
        super(context);
    }

    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView row_id_tv,goods_title_tv,xnum_tv,price_tv,refund_amt_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            row_id_tv = itemView.findViewById(R.id.row_id);
            goods_title_tv = itemView.findViewById(R.id.goods_title);
            xnum_tv = itemView.findViewById(R.id.xnum);
            price_tv = itemView.findViewById(R.id.price);
            refund_amt_tv = itemView.findViewById(R.id.refund_amt);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext, R.layout.mobile_refund_details_goods_info_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (null != mDatas) {
            final JSONObject sale_goods_info = mDatas.getJSONObject(position);
            if (sale_goods_info != null) {
                holder.row_id_tv.setText(String.valueOf(position + 1));
                holder.goods_title_tv.setText(String.format(Locale.CHINA,"、%s",sale_goods_info.getString("goods_title")));
                holder.xnum_tv.setText(String.format(Locale.CHINA, "%.3f", sale_goods_info.getDoubleValue("refund_num")));
                holder.price_tv.setText(String.format(Locale.CHINA, "%.2f", sale_goods_info.getDoubleValue("price")));
                holder.refund_amt_tv.setText(String.format(Locale.CHINA, "%.2f", sale_goods_info.getDoubleValue("refund_amt")));
            }
        }
    }

    public void setDatas(final String order_code){
        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT a.goods_title,a.type,b.refund_price,(b.refund_price * b.refund_num) refund_amt,b.refund_num FROM " +
                "refund_order_goods b left join barcode_info a on a.barcode_id = b.barcode_id\n" +
                "where a.goods_status = 1 and a.barcode_status = 1 and b.ro_code = '" + order_code + "'";

        Logger.d("sql:%s",sql);
        mDatas = SQLiteHelper.getListToJson(sql,err);
        if (mDatas != null){
            notifyDataSetChanged();
        }else{
            mDatas = new JSONArray();
            MyDialog.ToastMessage("加载商品明细错误：" + err,mContext,null);
        }
    }
}
