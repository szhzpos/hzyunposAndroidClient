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

public final class MobileRetailDetailsGoodsInfoAdapter extends AbstractTableDataAdapter<MobileRetailDetailsGoodsInfoAdapter.MyViewHolder> {

    public MobileRetailDetailsGoodsInfoAdapter(MainActivity context){
        super(context);
    }

    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView row_id_tv,goods_title_tv,xnum_tv,price_tv,sale_amt_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            row_id_tv = itemView.findViewById(R.id.row_id);
            goods_title_tv = itemView.findViewById(R.id.goods_title);
            xnum_tv = itemView.findViewById(R.id.xnum);
            price_tv = itemView.findViewById(R.id.price);
            sale_amt_tv = itemView.findViewById(R.id.sale_amt);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext, R.layout.mobile_retail_details_goods_info_content_layout, null);
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
                holder.xnum_tv.setText(String.format(Locale.CHINA, "%.3f", sale_goods_info.getDoubleValue("xnum")));
                holder.price_tv.setText(String.format(Locale.CHINA, "%.2f/%s", sale_goods_info.getDoubleValue("price"),sale_goods_info.getString("unit_name")));
                holder.sale_amt_tv.setText(String.format(Locale.CHINA, "%s%.2f", mContext.getString(R.string.currency_symbol_sz),sale_goods_info.getDoubleValue("sale_amt")));
            }
        }
    }

    public void setDatas(final String order_code){
        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT a.goods_title,a.unit_name,a.type,b.price,b.total_money sale_amt,b.xnum,(b.xnum * y_price - b.total_money) discount_amt,b.xnum * y_price original_amt,b.y_price original_price FROM " +
                "retail_order_goods b left join barcode_info a on a.barcode_id = b.barcode_id\n" +
                "where a.goods_status = 1 and a.barcode_status = 1 and b.order_code = '" + order_code + "'";

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
