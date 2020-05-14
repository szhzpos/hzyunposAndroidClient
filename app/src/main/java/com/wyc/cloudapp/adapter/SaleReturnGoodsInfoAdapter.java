package com.wyc.cloudapp.adapter;

import android.text.Editable;
import android.text.TextWatcher;
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
import com.wyc.cloudapp.dialog.BaseDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.util.Locale;

public final class SaleReturnGoodsInfoAdapter extends RecyclerView.Adapter<SaleReturnGoodsInfoAdapter.MyViewHolder>  {
    private BaseDialog mDialog;
    private MainActivity mContext;
    private JSONArray mDatas;
    private onRefundDataChange mRefundDataChange;
    public SaleReturnGoodsInfoAdapter(BaseDialog dialog){
        mDialog = dialog;
        mContext = dialog.getActivityContext();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView rog_id_tv,barcode_id_tv,barcode_tv,goods_title_tv,unit_name_tv,price_tv,num_tv,returnable_num_tv;
        EditText cur_ret_num_et,cur_ret_amt_et;
        View mCurrentLayoutItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;
            rog_id_tv = itemView.findViewById(R.id.rog_id);
            barcode_id_tv = itemView.findViewById(R.id.barcode_id);

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
                double returnable_num = sale_goods_info.getDoubleValue("returnable_num");

                holder.rog_id_tv.setText(sale_goods_info.getString("rog_id"));
                holder.barcode_id_tv.setText(sale_goods_info.getString("barcode_id"));

                holder.barcode_tv.setText(sale_goods_info.getString("barcode"));
                holder.goods_title_tv.setText(sale_goods_info.getString("goods_title"));
                holder.price_tv.setText(String.format(Locale.CHINA,"%.2f",sale_goods_info.getDoubleValue("price")));
                holder.num_tv.setText(String.format(Locale.CHINA,"%.2f",sale_goods_info.getDoubleValue("xnum")));
                holder.unit_name_tv.setText(sale_goods_info.getString("unit_name"));
                holder.returnable_num_tv.setText(String.format(Locale.CHINA,"%.2f",returnable_num));

                holder.cur_ret_num_et.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        double num = 0.0;
                        try {
                            if (s.length() != 0)
                                num = Double.valueOf(s.toString());

                            if (num > returnable_num){
                                MyDialog.ToastMessage(holder.cur_ret_num_et,"退货数量不能大于可退数量！",mContext,mDialog.getWindow());
                                holder.cur_ret_num_et.setText(String.format(Locale.CHINA,"%.2f",returnable_num));
                            }else{
                                updateRefundNum(holder.cur_ret_num_et,num);
                                holder.cur_ret_amt_et.setText(String.format(Locale.CHINA,"%.2f",sale_goods_info.getDoubleValue("price") * num));
                            }
                        }catch (NumberFormatException e){
                            e.printStackTrace();
                            MyDialog.ToastMessage(e.getMessage(),mContext,mDialog.getWindow());
                        }
                    }
                });
                holder.cur_ret_num_et.setText(String.format(Locale.CHINA,"%.2f",returnable_num));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0: mDatas.size();
    }

    private void updateRefundNum(final View view,double num){
        final View parent = (View)view.getParent();
        if (parent != null){
            final TextView rog_id_tv = parent.findViewById(R.id.rog_id),barcode_id_tv = parent.findViewById(R.id.barcode_id);
            if (null != rog_id_tv && null != barcode_id_tv){
                final String rog_id = rog_id_tv.getText().toString(),barcode_id = barcode_id_tv.getText().toString();
                for (int i = 0,size = mDatas.size();i < size;i ++){
                    final JSONObject record = mDatas.getJSONObject(i);
                    if (null != record){
                        if (barcode_id.equals(record.getString("barcode_id")) && rog_id.equals(record.getString("rog_id"))){
                            record.put("refund_num",num);
                            if (mRefundDataChange != null){
                                mRefundDataChange.onChange(mDatas);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    public interface onRefundDataChange{
        void onChange(final JSONArray datas);
    }
    public void setRefundDataChange(onRefundDataChange dataChange){
        mRefundDataChange = dataChange;
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

        });

        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT a.rog_id,a.barcode_id,a.barcode,c.goods_title,c.unit_name,c.conversion,1 is_rk,0 produce_date,a.price,a.xnum,total_money sale_amt,a.xnum returnable_num " +
                "FROM retail_order_goods a  left join barcode_info c on a.barcode_id = c.barcode_id\n" +
                "where c.goods_status = 1 and c.barcode_status = 1 and a.order_code = '" + order_code + "'",
                refund_sql = "select ifnull(a.rog_id,0) rog_id,a.is_rk,a.refund_price,a.xnum,a.barcode_id from refund_order_goods a inner join refund_order b on a.ro_code = b.ro_code where b.order_status = 2 and b.order_code = '" + order_code +"'";

        Logger.d("sql:%s",sql);
        boolean isSuccess = false;
        JSONObject sale_record,refunded_record;
        int sale_barcode_id,sale_rog_id,refund_barcode_id,refund_rog_id;
        if ((mDatas = SQLiteHelper.getListToJson(sql,err)) != null){
            final JSONArray refund_datas = SQLiteHelper.getListToJson(refund_sql,err);
            if (refund_datas != null){
                for (int i = 0,size = mDatas.size();i < size;i ++){
                    sale_record = mDatas.getJSONObject(i);

                    sale_record.put("refund_price",sale_record.getDoubleValue("price"));
                    sale_record.put("refund_num",sale_record.getDoubleValue("returnable_num"));

                    for (int j = 0,length = refund_datas.size();j < length;j ++){
                        refunded_record = refund_datas.getJSONObject(j);

                        sale_barcode_id = sale_record.getIntValue("barcode_id");
                        sale_rog_id = sale_record.getIntValue("rog_id");
                        refund_barcode_id = refunded_record.getIntValue("barcode_id");
                        refund_rog_id = refunded_record.getIntValue("rog_id");

                        if (sale_barcode_id == refund_barcode_id && sale_rog_id == refund_rog_id){
                            double sale_num = sale_record.getDoubleValue("xnum"),refund_num = refunded_record.getDoubleValue("xnum"),
                                    returnable_num = sale_num - refund_num;
                            sale_record.put("returnable_num",returnable_num);
                            sale_record.put("refund_num",returnable_num);
                            break;
                        }
                    }
                }
                isSuccess = true;
                mContext.runOnUiThread(this::notifyDataSetChanged);;
            }
        }
        if (!isSuccess){
            mDatas = new JSONArray();
            mContext.runOnUiThread(()->MyDialog.ToastMessage("加载商品明细错误：" + err,mContext,null));
        }
    }
    public JSONArray getRefundGoods(){
        return mDatas;
    }
}
