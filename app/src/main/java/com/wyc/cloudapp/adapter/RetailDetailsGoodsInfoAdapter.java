package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

import java.util.Locale;

public final class RetailDetailsGoodsInfoAdapter extends AbstractTableDataAdapter<RetailDetailsGoodsInfoAdapter.MyViewHolder> {

    public RetailDetailsGoodsInfoAdapter(MainActivity context){
        super(context);
    }

    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView row_id_tv,item_no_tv,barcode_tv,goods_title_tv,xnum_tv,unit_name_tv,price_tv,sale_amt_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            row_id_tv = itemView.findViewById(R.id.row_id);
            item_no_tv = itemView.findViewById(R.id.item_no);
            barcode_tv = itemView.findViewById(R.id.barcode);
            goods_title_tv = itemView.findViewById(R.id.goods_title);
            xnum_tv = itemView.findViewById(R.id.xnum);
            unit_name_tv = itemView.findViewById(R.id.unit_name);
            price_tv = itemView.findViewById(R.id.price);
            sale_amt_tv = itemView.findViewById(R.id.sale_amt);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.retail_details_goods_info_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) mContext.getResources().getDimension(R.dimen.table_row_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (null != mData) {
            final JSONObject sale_goods_info = mData.getJSONObject(position);
            if (sale_goods_info != null) {
                holder.row_id_tv.setText(String.valueOf(position + 1));
                holder.item_no_tv.setText(sale_goods_info.getString("item_no"));
                holder.barcode_tv.setText(sale_goods_info.getString("barcode"));
                holder.goods_title_tv.setText(sale_goods_info.getString("goods_title"));
                holder.xnum_tv.setText(String.format(Locale.CHINA, "%.3f", sale_goods_info.getDoubleValue("xnum")));
                holder.unit_name_tv.setText(sale_goods_info.getString("unit_name"));
                holder.price_tv.setText(String.format(Locale.CHINA, "%.2f", sale_goods_info.getDoubleValue("price")));
                holder.sale_amt_tv.setText(String.format(Locale.CHINA, "%.2f", sale_goods_info.getDoubleValue("sale_amt")));

                holder.itemView.setOnClickListener(this::setCurrentItemView);
            }
        }
    }

    public void setDatas(final String order_code){
        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT a.only_coding item_no,a.barcode,a.goods_title,a.unit_name,a.type,b.price,b.total_money sale_amt,b.xnum,(b.xnum * y_price - b.total_money) discount_amt,b.xnum * y_price original_amt,b.y_price original_price FROM " +
                "retail_order_goods b left join barcode_info a on a.barcode_id = b.barcode_id\n" +
                "where a.goods_status = 1 and a.barcode_status = 1 and b.order_code = '" + order_code + "'";

        Logger.d("sql:%s",sql);
        mData = SQLiteHelper.getListToJson(sql,err);
        if (mData != null){
            notifyDataSetChanged();
        }else{
            mData = new JSONArray();
            MyDialog.ToastMessage("加载商品明细错误：" + err, null);
        }
    }
}
