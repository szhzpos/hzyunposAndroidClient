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
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnMainActivityImp;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.pay.AbstractPayDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.util.Locale;

public final class RefundGoodsInfoAdapter extends RecyclerView.Adapter<RefundGoodsInfoAdapter.MyViewHolder>  {
    private DialogBaseOnMainActivityImp mDialog;
    private MainActivity mContext;
    private JSONArray mGoodsDatas,mPayDatas;
    private onRefundDataChange mRefundDataChange;
    private JSONObject mVipInfo;
    public RefundGoodsInfoAdapter(DialogBaseOnMainActivityImp dialog){
        mDialog = dialog;
        mContext = dialog.getPrivateContext();
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
        View itemView = View.inflate(mContext, R.layout.refund_goods_info_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) mContext.getResources().getDimension(R.dimen.table_row_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (null != mGoodsDatas){
            final JSONObject sale_goods_info = mGoodsDatas.getJSONObject(position);
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
        return mGoodsDatas == null ? 0: mGoodsDatas.size();
    }

    private void updateRefundNum(final View view,double num){
        final View parent = (View)view.getParent();
        if (parent != null && mGoodsDatas != null){
            final TextView rog_id_tv = parent.findViewById(R.id.rog_id),barcode_id_tv = parent.findViewById(R.id.barcode_id);
            if (null != rog_id_tv && null != barcode_id_tv){
                final String rog_id = rog_id_tv.getText().toString(),barcode_id = barcode_id_tv.getText().toString();
                for (int i = 0,size = mGoodsDatas.size();i < size;i ++){
                    final JSONObject record = mGoodsDatas.getJSONObject(i);
                    if (null != record){
                        if (barcode_id.equals(record.getString("barcode_id")) && rog_id.equals(record.getString("rog_id"))){
                            record.put("refund_num",num);
                            if (mRefundDataChange != null && mGoodsDatas != null){
                                mRefundDataChange.onChange(mGoodsDatas);
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

    private JSONObject mergeRefundOrderInfo(final JSONArray refunds){
        final JSONArray refund_orders = new JSONArray(),refund_order_goods = new JSONArray(),refund_order_pays = new JSONArray();

        JSONObject data = new JSONObject(),refund_order;

        for (int i = 0,size = refunds.size();i < size;i++) {
            refund_order = refunds.getJSONObject(i);
            if (refund_order != null){
                Object list = refund_order.remove("pays_list");
                if (list != null){
                    refund_order_pays.addAll(JSON.parseArray(list.toString()));
                }
                list = refund_order.remove("goods_list");
                if (list != null){
                    refund_order_goods.addAll(JSON.parseArray(list.toString()));
                }
                refund_orders.add(Utils.JsondeepCopy(refund_order));
            }
        }

        data.put("refund_order",refund_orders);
        data.put("refund_order_goods",refund_order_goods);
        data.put("refund_order_pays",refund_order_pays);

        return data;
    }

    private JSONArray mergeGoodsInfo(final JSONObject refunded_order,final JSONObject retail_order){
        JSONObject sale_goods_record,refunded_goods_record;
        int sale_barcode_id,sale_rog_id,refund_barcode_id,refund_rog_id;


        final JSONArray merge_goods = mGoodsDatas = Utils.JsondeepCopy(retail_order.getJSONArray("goods_list"));
        for (int i = 0,size = merge_goods.size();i < size;i ++){
            sale_goods_record = merge_goods.getJSONObject(i);

            double sale_num = sale_goods_record.getDoubleValue("xnum");
            sale_goods_record.put("refund_price",sale_goods_record.getDoubleValue("price"));
            sale_goods_record.put("returnable_num",sale_num);
            sale_goods_record.put("refund_num",sale_num);
            sale_goods_record.put("is_rk",2);//默认需要入库

            final JSONArray refund_order_goods = refunded_order.getJSONArray("refund_order_goods");

            for (int j = 0,length = refund_order_goods.size();j < length;j ++){
                refunded_goods_record = refund_order_goods.getJSONObject(j);

                sale_barcode_id = sale_goods_record.getIntValue("barcode_id");
                sale_rog_id = sale_goods_record.getIntValue("rog_id");
                refund_barcode_id = refunded_goods_record.getIntValue("barcode_id");
                refund_rog_id = refunded_goods_record.getIntValue("rog_id");

                if (sale_barcode_id == refund_barcode_id && sale_rog_id == refund_rog_id){
                    sale_num = sale_goods_record.getDoubleValue("refund_num");

                    double refunded_num = refunded_goods_record.getDoubleValue("xnum"),returnable_num = sale_num - refunded_num;

                    sale_goods_record.put("returnable_num",returnable_num);
                    sale_goods_record.put("refund_num",returnable_num);
                    //可能存在多次退款记录不能跳出循环
                }
            }
        }
        return merge_goods;
    }

    private JSONArray mergePayInfo(final JSONObject refunded_order,final JSONObject retail_order){
        final JSONArray refund_order_pays = refunded_order.getJSONArray("refund_order_pays");
        if (refund_order_pays == null || refund_order_pays.isEmpty()){
            return retail_order.getJSONArray("pays_list");
        }

        final JSONArray merge_goods_info = mGoodsDatas;
        double refund_amt = 0.0;
        JSONObject goods_info;
        for (int i = 0,size = merge_goods_info.size();i < size;i ++){
            goods_info = merge_goods_info.getJSONObject(i);
            if (goods_info != null){
                refund_amt += goods_info.getDoubleValue("refund_num") * goods_info.getDoubleValue("refund_price");
            }
        }

        //已经退过款剩余金额默认用现金退款
        final JSONObject pay_info = new JSONObject();
        pay_info.put("pay_method",PayMethodViewAdapter.CASH_METHOD_ID);
        pay_info.put("pay_method_name","现金支付");
        pay_info.put("pay_money",Utils.formatDouble(refund_amt,2));
        pay_info.put("pay_code", AbstractPayDialog.getPayCode(mContext.getPosNum()));
        pay_info.put("is_check", 2);

        Logger.d_json(pay_info.toJSONString());

        return new JSONArray(){{add(pay_info);}};
    }

    private JSONObject generateVipInfo(final JSONObject retail_order){
        JSONObject vip_info = null;
        if (!"".equals(Utils.getNullStringAsEmpty(retail_order,"card_code"))){
            vip_info = new JSONObject();
            vip_info.put("member_id",Utils.getNullStringAsEmpty(retail_order,"member_id"));
            vip_info.put("mobile",retail_order.getString("mobile"));
            vip_info.put("name",retail_order.getString("name"));
            vip_info.put("card_code",retail_order.getString("card_code"));
        }
        return vip_info;
    }

    public void setDatas(final String order_code,final StringBuilder err){
        final JSONObject object = new JSONObject();
        final HttpRequest httpRequest = new HttpRequest();

        object.put("appid",mContext.getAppId());
        object.put("retail_code",order_code);
        object.put("stores_id",mContext.getStoreInfo().getString("stores_id"));


        final JSONObject retJson = httpRequest.sendPost(mContext.getUrl() + "/api/refund/getretailrefund",HttpRequest.generate_request_parm(object,mContext.getAppScret()),true);

        if (retJson.getIntValue("flag") == 1){
            final JSONObject info = JSON.parseObject(retJson.getString("info"));
            if (Utils.getNullStringAsEmpty(info,"status").equals("y")){
                final JSONObject data = info.getJSONObject("data");

                final JSONArray refund_order_array = data.getJSONArray("refund_order");
                final JSONObject refunded_order = mergeRefundOrderInfo(refund_order_array);
                final JSONObject retail_order = data.getJSONObject("retail_order");

                mVipInfo = generateVipInfo(retail_order);
                mGoodsDatas = mergeGoodsInfo(refunded_order,retail_order);

                mPayDatas = mergePayInfo(refunded_order,retail_order);

                Logger.d_json(mGoodsDatas.toJSONString());
            }else {
                err.append(info.getString("info"));
            }
        }else {
            err.append(retJson.getString("info"));
        }
    }
    public void clearOrderInfo(){
        if (mVipInfo != null){
            mVipInfo = null;
        }
        if (mPayDatas != null && !mPayDatas.isEmpty()){
            mPayDatas.fluentClear();
        }
        if (mGoodsDatas != null && !mGoodsDatas.isEmpty()){
            mGoodsDatas.fluentClear();
            if (mRefundDataChange != null)mRefundDataChange.onChange(mGoodsDatas);
            notifyDataSetChanged();
        }
    }
    public JSONArray getRefundGoods(){
        return mGoodsDatas;
    }
    public JSONObject getVipInfo(){
        return mVipInfo;
    }
    public JSONArray getPayDatas(){
        return mPayDatas;
    }
    public String PayDatasToString(){
        final StringBuilder sz_pay_info = new StringBuilder();
        if (null != mPayDatas)
            for (int i = 0,size = mPayDatas.size();i < size;i++){
                final JSONObject pay_record = mPayDatas.getJSONObject(i);
                sz_pay_info.append(Utils.getNullStringAsEmpty(pay_record,"pay_method_name")).append("：").append(pay_record.getDoubleValue("pay_money")).append("元");
                if (i + 1 < size){
                    sz_pay_info.append(",");
                }else {
                    sz_pay_info.append("\r\n");
                }
            }
        return sz_pay_info.toString();
    }
}
