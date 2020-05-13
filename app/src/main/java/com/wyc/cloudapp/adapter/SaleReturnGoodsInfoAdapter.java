package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.util.Locale;

public final class SaleReturnGoodsInfoAdapter extends RecyclerView.Adapter<SaleReturnGoodsInfoAdapter.MyViewHolder>  {
    private MainActivity mContext;
    private JSONArray mDatas;
    private View mCurrentItemView;
    public SaleReturnGoodsInfoAdapter(MainActivity context){
        mContext = context;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView barcode_tv,goods_title_tv,unit_name_tv,price_tv,num_tv,returnable_num_tv;
        EditText cur_ret_num_et,cur_ret_amt_et;
        View mCurrentLayoutItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;

            barcode_tv = itemView.findViewById(R.id.barcode);
            goods_title_tv = itemView.findViewById(R.id.goods_title);
            price_tv = itemView.findViewById(R.id.price);
            num_tv = itemView.findViewById(R.id.xnum);
            unit_name_tv = itemView.findViewById(R.id.unit_name);
            returnable_num_tv = itemView.findViewById(R.id.returnable_num);
            cur_ret_num_et = itemView.findViewById(R.id.cur_ret_num);
            cur_ret_amt_et = itemView.findViewById(R.id.cur_ret_amt);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.sale_return_goods_info_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) mContext.getResources().getDimension(R.dimen.table_row_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (null != mDatas){
            final JSONObject sale_goods_info = mDatas.getJSONObject(position);
            if (sale_goods_info != null){
                holder.barcode_tv.setText(sale_goods_info.getString("barcode"));
                holder.goods_title_tv.setText(sale_goods_info.getString("goods_title"));
                holder.price_tv.setText(String.format(Locale.CHINA,"%.2f",sale_goods_info.getDoubleValue("price")));
                holder.num_tv.setText(String.format(Locale.CHINA,"%.2f",sale_goods_info.getDoubleValue("xnum")));
                holder.unit_name_tv.setText(sale_goods_info.getString("unit_name"));
                holder.returnable_num_tv.setText(String.format(Locale.CHINA,"%.2f",sale_goods_info.getDoubleValue("returnable_num")));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0: mDatas.size();
    }

    public void setDatas(final String order_code){

        CustomApplication.execute(()->{

            final JSONObject object = new JSONObject();
            object.put("appid",mContext.getAppId());
            object.put("retail_code",order_code);
            object.put("stores_id",mContext.getStoreInfo().getString("stores_id"));
            HttpRequest httpRequest = new HttpRequest();
            final JSONObject retJson = httpRequest.sendPost(mContext.getUrl() + "/api/refund/getretailrefund",
                    HttpRequest.generate_request_parm(object,mContext.getAppScret()),true);
            final JSONObject info = JSON.parseObject(retJson.getString("info"));
            Logger.d_json(info.toJSONString());


        });

        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT c.barcode_id,c.barcode,c.goods_title,c.unit_name,a.price,a.xnum,a.xnum returnable_num FROM\n" +
                "retail_order_goods a  left join barcode_info c on a.barcode_id = c.barcode_id\n" +
                "where c.goods_status = 1 and c.barcode_status = 1 and a.order_code = '" + order_code + "'";

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
