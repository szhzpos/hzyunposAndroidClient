package com.wyc.cloudapp.adapter;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.DigitKeyboardPopup;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogBaseOnMainActivityImp;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.util.Locale;

public final class RefundGoodsInfoAdapter extends RecyclerView.Adapter<RefundGoodsInfoAdapter.MyViewHolder>  {
    private AbstractDialogBaseOnMainActivityImp mDialog;
    private MainActivity mContext;
    private JSONArray mGoodsDatas,mPayDatas,mOriPayDatas;;
    private onRefundGoodsDataChange mRefundGoodsDataChange;
    private onRefundPayDataChange mRefundPayDataChange;
    private JSONObject mVipInfo;
    private DigitKeyboardPopup mDigitKeyboardPopup;
    private boolean mSingleRefundStatus;
    public RefundGoodsInfoAdapter(AbstractDialogBaseOnMainActivityImp dialog){
        mDialog = dialog;
        mContext = dialog.getPrivateContext();
        mDigitKeyboardPopup = new DigitKeyboardPopup(mContext);
        mSingleRefundStatus = mContext.getSingle();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView row_id,rog_id_tv,barcode_id_tv,barcode_tv,goods_title_tv,unit_name_tv,price_tv,num_tv,returnable_num_tv;
        private EditText cur_refund_num_et,cur_refund_amt_et;
        private CheckBox sel_status_cb;
        View mCurrentLayoutItemView;
        private TextWatcher mTextWatcher;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;
            rog_id_tv = itemView.findViewById(R.id.rog_id);
            barcode_id_tv = itemView.findViewById(R.id.barcode_id);

            sel_status_cb = itemView.findViewById(R.id.sel_status);
            row_id = itemView.findViewById(R.id.row_id);
            barcode_tv = itemView.findViewById(R.id.barcode);
            goods_title_tv = itemView.findViewById(R.id.goods_title);
            price_tv = itemView.findViewById(R.id.price);
            num_tv = itemView.findViewById(R.id.xnum);
            unit_name_tv = itemView.findViewById(R.id.unit_name);
            returnable_num_tv = itemView.findViewById(R.id.returnable_num);
            cur_refund_num_et = itemView.findViewById(R.id.cur_refund_num);
            cur_refund_amt_et = itemView.findViewById(R.id.cur_refund_amt);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext, R.layout.refund_goods_info_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) mContext.getResources().getDimension(R.dimen.table_row_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (null != mGoodsDatas){
            final JSONObject refund_goods_info = mGoodsDatas.getJSONObject(position);
            if (refund_goods_info != null){

                holder.rog_id_tv.setText(refund_goods_info.getString("rog_id"));
                holder.barcode_id_tv.setText(refund_goods_info.getString("barcode_id"));

                holder.row_id.setText(String.valueOf(position + 1));
                holder.barcode_tv.setText(refund_goods_info.getString("barcode"));
                holder.goods_title_tv.setText(refund_goods_info.getString("goods_title"));
                holder.price_tv.setText(String.format(Locale.CHINA,"%.2f",refund_goods_info.getDoubleValue("price")));
                holder.num_tv.setText(String.format(Locale.CHINA,"%.3f",refund_goods_info.getDoubleValue("xnum")));
                holder.unit_name_tv.setText(refund_goods_info.getString("unit_name"));

                double returnable_num = refund_goods_info.getDoubleValue("returnable_num");
                if (Utils.equalDouble(returnable_num,0.0)){
                    holder.goods_title_tv.setTextColor(mContext.getColor(R.color.orange_1));
                }else {
                    holder.goods_title_tv.setTextColor(mContext.getColor(R.color.text_color));
                }
                holder.returnable_num_tv.setText(String.format(Locale.CHINA,"%.3f",returnable_num));

                if (holder.mTextWatcher == null){
                    holder.mTextWatcher = new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                        }

                        @Override
                        public void onTextChanged(CharSequence s, int start, int before, int count) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {

                            double num = 0.0;
                            double returnable_num = 0.0;
                            if (s.length() != 0)
                                num = Double.valueOf(s.toString());

                            returnable_num = Double.valueOf(holder.returnable_num_tv.getText().toString());
                            if (num > returnable_num){
                                MyDialog.ToastMessage("退货数量不能大于可退数量！",mContext,mDialog.getWindow());
                                holder.cur_refund_num_et.setText(String.format(Locale.CHINA,"%.3f",returnable_num));
                                holder.cur_refund_num_et.setSelection(holder.cur_refund_num_et.length() - 1);
                            }else{
                                if (!mSingleRefundStatus){
                                    updateRefundNum(holder.mCurrentLayoutItemView,num);
                                    if (Utils.equalDouble(num,0.0)){
                                        if (holder.sel_status_cb.isChecked()){
                                            holder.sel_status_cb.setChecked(false);
                                        }
                                    }else if(Utils.equalDouble(num,returnable_num)){
                                        if (!holder.sel_status_cb.isChecked()){
                                            holder.sel_status_cb.setChecked(true);
                                        }
                                    }
                                }
                                holder.cur_refund_amt_et.setText(String.format(Locale.CHINA,"%.2f",Double.valueOf(holder.price_tv.getText().toString()) * num));
                            }
                        }
                    };
                    holder.cur_refund_num_et.addTextChangedListener(holder.mTextWatcher);
                }

                if (!mSingleRefundStatus){
                    holder.cur_refund_num_et.setOnFocusChangeListener(cur_refund_num_focusChangeListener);
                    holder.cur_refund_num_et.setOnClickListener(cur_refund_num_click);
                    holder.sel_status_cb.setOnCheckedChangeListener(sel_status_CheckedChange);
                }else {
                    holder.cur_refund_num_et.setEnabled(false);
                    holder.sel_status_cb.setVisibility(View.INVISIBLE);
                }

                holder.cur_refund_num_et.setText(String.format(Locale.CHINA,"%.3f",refund_goods_info.getDoubleValue("refund_num")));

            }
        }
    }

    @Override
    public int getItemCount() {
        return mGoodsDatas == null ? 0: mGoodsDatas.size();
    }

    private View.OnFocusChangeListener cur_refund_num_focusChangeListener = (v, hasFocus) -> {
        if (!hasFocus)
            mDigitKeyboardPopup.dismiss();
        else
            v.callOnClick();
    };
    private View.OnClickListener cur_refund_num_click = (v)->{
        Utils.hideKeyBoard((EditText)v);
        mDigitKeyboardPopup.showAtLocation(v);
    };

    private CompoundButton.OnCheckedChangeListener sel_status_CheckedChange = (buttonView, isChecked) -> {
        final View parent = (View) buttonView.getParent();
        if (null != parent){
            final TextView returnable_num_tv = parent.findViewById(R.id.returnable_num),cur_refund_num_et = parent.findViewById(R.id.cur_refund_num);
            if (null != returnable_num_tv && null != cur_refund_num_et){
                if (isChecked){
                    final String sz_returnable_num = returnable_num_tv.getText().toString();
                    if (Utils.equalDouble(Double.valueOf(sz_returnable_num),0.0)){
                        mContext.runOnUiThread(()->MyDialog.ToastMessage(returnable_num_tv,"当前可退数量为零！",mContext,mDialog.getWindow()));
                    }
                    cur_refund_num_et.setText(sz_returnable_num);
                }
                else
                    cur_refund_num_et.setText(mContext.getString(R.string.z_p_z_sz));
            }
        }
    };

    private void updateRefundNum(final View parent,double num){
        if (parent != null && mGoodsDatas != null){
            final TextView rog_id_tv = parent.findViewById(R.id.rog_id),barcode_id_tv = parent.findViewById(R.id.barcode_id);
            if (null != rog_id_tv && null != barcode_id_tv){
                final String rog_id = rog_id_tv.getText().toString(),barcode_id = barcode_id_tv.getText().toString();
                for (int i = 0,size = mGoodsDatas.size();i < size;i ++){
                    final JSONObject record = mGoodsDatas.getJSONObject(i);
                    if (null != record){
                        if (barcode_id.equals(record.getString("barcode_id")) && rog_id.equals(record.getString("rog_id"))){
                            record.put("refund_num",num);
                            if (mRefundGoodsDataChange != null && mGoodsDatas != null){
                                mRefundGoodsDataChange.onChange(mGoodsDatas);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    public interface onRefundPayDataChange{
        void onChange(final JSONArray datas);
    }
    public interface onRefundGoodsDataChange {
        void onChange(final JSONArray datas);
    }
    public void setRefundDataChange(onRefundGoodsDataChange dataChange){
        mRefundGoodsDataChange = dataChange;
    }
    public void setmRefundPayDataChange(onRefundPayDataChange dataChange){
        mRefundPayDataChange = dataChange;
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
        final JSONArray refund_order_goods = refunded_order.getJSONArray("refund_order_goods");

        for (int i = 0,size = merge_goods.size();i < size;i ++){
            sale_goods_record = merge_goods.getJSONObject(i);

            double sale_num = sale_goods_record.getDoubleValue("xnum");
            sale_goods_record.put("refund_price",sale_goods_record.getDoubleValue("price"));
            sale_goods_record.put("returnable_num",sale_num);
            sale_goods_record.put("refund_num",0);
            sale_goods_record.put("is_rk",2);//默认需要入库

            for (int j = 0,length = refund_order_goods.size();j < length;j ++){
                refunded_goods_record = refund_order_goods.getJSONObject(j);

                sale_barcode_id = sale_goods_record.getIntValue("barcode_id");
                sale_rog_id = sale_goods_record.getIntValue("rog_id");
                refund_barcode_id = refunded_goods_record.getIntValue("barcode_id");
                refund_rog_id = refunded_goods_record.getIntValue("rog_id");

                if (sale_barcode_id == refund_barcode_id && sale_rog_id == refund_rog_id){
                    sale_num = sale_goods_record.getDoubleValue("returnable_num");

                    double refunded_num = refunded_goods_record.getDoubleValue("xnum"),returnable_num = sale_num - refunded_num;

                    sale_goods_record.put("returnable_num",returnable_num);
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
        return new JSONArray();
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

    private void notifyPayDataChange(){
        if (mRefundPayDataChange != null && mPayDatas != null){
            mRefundPayDataChange.onChange(mPayDatas);
        }
    }
    public void setData(final JSONArray array){
        final JSONArray sales = Utils.JsondeepCopy(array);
        if (mGoodsDatas == null)mGoodsDatas = new JSONArray();
        JSONObject obj,json;
        double sale_num = 0.0,price = 0.0;
        int barcode_id = -1;
        for (int i = 0;i < sales.size();i ++){
            obj = sales.getJSONObject(i);
            sale_num = obj.getDoubleValue("xnum");
            price = obj.getDoubleValue("price");

            json = new JSONObject();

            barcode_id = Utils.getNotKeyAsNumberDefault(obj,"barcode_id",-1);
            json.put("rog_id",barcode_id);
            json.put("barcode_id",barcode_id);

            json.put("barcode",obj.getString("barcode"));
            json.put("goods_title",obj.getString("goods_title"));
            json.put("is_rk",2);
            json.put("conversion",1);
            json.put("price",price);
            json.put("refund_price",price);
            json.put("xnum",sale_num);
            json.put("unit_name",obj.getString("unit_name"));
            json.put("returnable_num",sale_num);
            json.put("refund_num",sale_num);

            mGoodsDatas.add(json);

        }
        if (mRefundGoodsDataChange != null && mGoodsDatas != null){
            mRefundGoodsDataChange.onChange(mGoodsDatas);
        }
        mPayDatas = new JSONArray();
    }
    public void setDatas(final String order_code,final StringBuilder err){

        final JSONObject object = new JSONObject();
        final HttpRequest httpRequest = new HttpRequest();

        object.put("appid",mContext.getAppId());
        object.put("retail_code",order_code);
        object.put("stores_id",mContext.getStoreInfo().getString("stores_id"));


        final JSONObject retJson = httpRequest.sendPost(mContext.getUrl() + "/api/refund/getretailrefund",HttpRequest.generate_request_parm(object,mContext.getAppSecret()),true);

        if (retJson.getIntValue("flag") == 1){
            final JSONObject info = JSON.parseObject(retJson.getString("info"));
            if (Utils.getNullStringAsEmpty(info,"status").equals("y")){
                final JSONObject data = info.getJSONObject("data");

                Logger.d_json(data.toJSONString());

                final JSONArray refund_order_array = Utils.getNullObjectAsEmptyJsonArray(data,"refund_order");
                final JSONObject refunded_order = mergeRefundOrderInfo(refund_order_array);
                final JSONObject retail_order = Utils.getNullObjectAsEmptyJson(data,"retail_order");

                mVipInfo = generateVipInfo(retail_order);
                mGoodsDatas = mergeGoodsInfo(refunded_order,retail_order);
                mPayDatas = Utils.JsondeepCopy(mOriPayDatas = mergePayInfo(refunded_order,retail_order));

            }else {
                err.append(info.getString("info"));
            }
        }else {
            err.append(retJson.getString("info"));
        }
    }
    public boolean isSingleRefundStatus(){
        return mSingleRefundStatus;
    }
    public void clearOrderInfo(){
        if (mVipInfo != null){
            mVipInfo = null;
        }
        if (mOriPayDatas != null && !mOriPayDatas.isEmpty()){
            mOriPayDatas.fluentClear();
        }

        if (mPayDatas != null && !mPayDatas.isEmpty()){
            mPayDatas.fluentClear();
        }
        if (mGoodsDatas != null && !mGoodsDatas.isEmpty()){
            mGoodsDatas.fluentClear();
            if (mRefundGoodsDataChange != null) mRefundGoodsDataChange.onChange(mGoodsDatas);
            notifyDataSetChanged();
        }
        if (isSingleRefundStatus()){
            mContext.resetOrderInfo();
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
    public void sync_refund_order(){
        mContext.sync_refund_order();
    }
    public void allRefund(){
        restoreRefundGOodsInfo();
        mPayDatas = Utils.JsondeepCopy(mOriPayDatas);
        notifyPayDataChange();
    }
    private void restoreRefundGOodsInfo(){
        JSONObject goods;
        boolean isRefresh = false;
        double refund_num = 0.0,returnable_num = 0.0;
        for (int i = 0,size = mGoodsDatas.size();i < size;i++){
            goods = mGoodsDatas.getJSONObject(i);
            refund_num = goods.getDoubleValue("refund_num");
            returnable_num = goods.getDoubleValue("returnable_num");
            if (!Utils.equalDouble(refund_num,returnable_num)){
                if (!isRefresh)isRefresh = true;
                refund_num = returnable_num;
                goods.put("refund_num",refund_num);
            }
        }
        if (isRefresh){
            notifyDataSetChanged();
        }
    }

    public void addPayInfo(final JSONObject object){
        if (mPayDatas != null){
            if (!mPayDatas.isEmpty())mPayDatas.fluentClear();
            mPayDatas.add(object);
        }
        notifyPayDataChange();
    }
    public double getRefundAmt(){
        JSONObject record;
        double refund_num = 0.0,refund_sum_amt = 0.0,refund_price = 0.0;
        if (null != mGoodsDatas)
            for (int i = 0,size = mGoodsDatas.size() ;i < size;i++) {
                record = mGoodsDatas.getJSONObject(i);
                refund_price = record.getDoubleValue("refund_price");
                refund_num = record.getDoubleValue("refund_num");
                refund_sum_amt += Utils.formatDouble(refund_num * refund_price,2);
            }
        return refund_sum_amt;
    }

    public String PayDatasToString(){
        final StringBuilder sz_pay_info = new StringBuilder();
        final JSONArray pay_datas = getPayDatas();
        if (null != pay_datas)
            for (int i = 0,size = pay_datas.size();i < size;i++){
                final JSONObject pay_record = pay_datas.getJSONObject(i);
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
