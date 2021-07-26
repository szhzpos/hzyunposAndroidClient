package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

import java.util.Locale;

public final class GoodsManageViewAdapter extends AbstractTableDataAdapter<GoodsManageViewAdapter.MyViewHolder> {
    private String mWhereCondition;
    private int mCurrentPage;
    private int mAllRowsForQueryCondition;
    private int mDataSize;
    private final int mPerPageRows = 50;
    public GoodsManageViewAdapter(SaleActivity context){
        super(context);
        mData = new JSONArray();
    }
    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView _row_id_tv,_item_id_tv,_barcode_tv,_name_tv,_mnemonic_code_tv,_unit_name_tv,_specification_tv,_origin_tv,
                _retail_price_tv,_vip_price_tv,_category_tv,_attr_tv,_status_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            _row_id_tv = itemView.findViewById(R.id.row_id);
            _item_id_tv = itemView.findViewById(R.id._item_id);
            _barcode_tv = itemView.findViewById(R.id._barcode);
            _name_tv = itemView.findViewById(R.id._name);
            _mnemonic_code_tv = itemView.findViewById(R.id._mnemonic_code);
            _unit_name_tv = itemView.findViewById(R.id._unit_name);
            _specification_tv = itemView.findViewById(R.id._specification);
            _origin_tv = itemView.findViewById(R.id._origin);

            _retail_price_tv = itemView.findViewById(R.id._retail_price);
            _vip_price_tv = itemView.findViewById(R.id._vip_price);
            _attr_tv = itemView.findViewById(R.id._attr);
            _status_tv = itemView.findViewById(R.id._status);
            _category_tv = itemView.findViewById(R.id._category);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext, R.layout.goods_manage_list_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) mContext.getResources().getDimension(R.dimen.table_row_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        super.onBindViewHolder(holder,position);

        final JSONObject goods_info = mData.getJSONObject(position);
        if (goods_info != null){
            holder._row_id_tv.setText(String.valueOf(position+1));
            holder._item_id_tv.setText(goods_info.getString("item_no"));
            holder._barcode_tv.setText(goods_info.getString("barcode"));
            holder._name_tv.setText(goods_info.getString("goods_title"));
            holder._mnemonic_code_tv.setText(goods_info.getString("mnemonic_code"));
            holder._unit_name_tv.setText(goods_info.getString("unit_name"));
            holder._specification_tv.setText(goods_info.getString("specifi"));
            holder._origin_tv.setText(goods_info.getString("origin"));

            holder._retail_price_tv.setText(String.format(Locale.CHINA,"%.2f",goods_info.getDoubleValue("retail_price")));
            holder._vip_price_tv.setText(String.format(Locale.CHINA,"%.2f",goods_info.getDoubleValue("yh_price")));
            holder._attr_tv.setText(goods_info.getString("attr"));
            holder._category_tv.setText(goods_info.getString("category_name"));

            final TextView _status_tv = holder._status_tv;
            int code = goods_info.getIntValue("status_code");
            _status_tv.setText(goods_info.getString("status"));
            _status_tv.setTag(code);
            if (mCurrentItemView != holder.itemView){
                setViewBackgroundColor(holder.itemView,false);
            }
            preload(position,_status_tv);

            holder.itemView.setOnClickListener(mItemClickListener);
        }
    }



    private void preload(int position,final @NonNull View view){
        if (mDataSize < mAllRowsForQueryCondition && mAllRowsForQueryCondition > mPerPageRows && position + 5 == mDataSize){//提前5行加载
            view.postDelayed(()-> setDatas(mWhereCondition,mCurrentPage + 1),100);
        }
    }

    public void setDatas(final String where_condition,int page){
        final int per_page_rows = mPerPageRows,start_row =page * per_page_rows;
        final StringBuilder err = new StringBuilder();
        final String counts = SQLiteHelper.getString("select count(bi_id) counts from barcode_info" + where_condition,err);

        final String sql = " select only_coding item_no,barcode,goods_title,ifnull(mnemonic_code,'') mnemonic_code,unit_name,ifnull(specifi,'') specifi,ifnull(origin,'') as origin,retail_price,yh_price,category_name,\n" +
                " case type when 1 then '普通商品' when 2 then '称重商品' when 3 then '服装' else '其他' end attr,barcode_status status_code,\n" +
                " case when barcode_status = 1 then '正常在售' when barcode_status = 2 then '下架停售' else '已删除' end status  from barcode_info" + where_condition + " limit "+ per_page_rows +" offset " + start_row;

        Logger.d("sql:%s",sql);
        final JSONArray array = SQLiteHelper.getListToJson(sql,err);
        if (array != null && counts != null){
            mWhereCondition = where_condition;
            mCurrentPage = page;
            if (page == 0) {
                mData = array;
                mAllRowsForQueryCondition = Integer.parseInt(counts);
                mDataSize = array.size();
            }else{
                mDataSize += array.size();
                mData.addAll(array);
            }
            notifyDataSetChanged();
        }else{
            MyDialog.ToastMessage("加载商品明细错误：" + err, null);
        }
    }

}
