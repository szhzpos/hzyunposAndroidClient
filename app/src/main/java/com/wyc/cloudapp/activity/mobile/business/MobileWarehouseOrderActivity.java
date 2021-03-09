package com.wyc.cloudapp.activity.mobile.business;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileWarehouseOrderAdapter;
import com.wyc.cloudapp.adapter.business.MobileWarehouseOrderDetailsAdapter;
import com.wyc.cloudapp.adapter.report.AbstractDataAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.util.Locale;

public final class MobileWarehouseOrderActivity extends AbstractMobileBusinessOrderActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    protected MobileWarehouseOrderAdapter getAdapter() {
        return new MobileWarehouseOrderAdapter(this);
    }
    @Override
    protected JSONObject generateQueryCondition() {
        final JSONObject condition = new JSONObject();
        condition.put("api","/api/rkd/xlist");
        return condition;
    }

    @Override
    public Class<?> jumpAddTarget() {
        return MobileAddWarehouseOrderActivity.class;
    }

    public static class MobileAddWarehouseOrderActivity extends AbstractMobileAddOrderActivity {
        public static final int SELECT_ORDER_CODE = 12;
        private TextView mPurchaseOrderCodeTv;
        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_warehouse_order;
        }

        @Override
        protected void initView() {
            super.initView();
            initSourceOrder();
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (resultCode == RESULT_OK ){
                if (requestCode == SELECT_ORDER_CODE){
                    queryPurchaseOrderInfo(data.getStringExtra("order_id"));
                }
            }
        }

        @Override
        protected boolean hasSource() {
            return null != mPurchaseOrderCodeTv && !mPurchaseOrderCodeTv.getText().toString().isEmpty();
        }

        private void initSourceOrder(){
            mPurchaseOrderCodeTv = findViewById(R.id.m_source_order_tv);
            mPurchaseOrderCodeTv.setOnClickListener(v -> {
                if (!isDetailsEmpty()){
                    if (MyDialog.showMessageToModalDialog(this,"已存在商品明细，是否替换？") == 0){
                        return;
                    }
                }
                final Intent intent = new Intent();
                intent.setClass(this,MobilePurchaseOrderActivity.class);
                intent.putExtra("title",getString(R.string.select_anything_hint,getString(R.string.purchase_order_sz)));
                intent.putExtra("FindSource",true);
                startActivityForResult(intent,SELECT_ORDER_CODE);
            });
        }

        private void queryPurchaseOrderInfo(final String id){
            final JSONObject parameterObj = new JSONObject();
            parameterObj.put("appid",getAppId());
            parameterObj.put("stores_id",getStoreId());
            parameterObj.put("pt_user_id",getPtUserId());
            parameterObj.put("cgd_id",id);

            final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(this,getString(R.string.hints_query_data_sz));
            CustomApplication.execute(()->{
                final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/cgd/xinfo", HttpRequest.generate_request_parm(parameterObj,getAppSecret()),true);
                if (HttpUtils.checkRequestSuccess(retJson)){
                    try {
                        final JSONObject info = JSONObject.parseObject(retJson.getString("info"));
                        if (HttpUtils.checkBusinessSuccess(info)){
                            Logger.d_json(info.getJSONObject("data").toString());
                            runOnUiThread(()-> showPurchaseOrderInfo(info.getJSONObject("data")));
                        }else throw new JSONException(info.getString("info"));
                    }catch (JSONException e){
                        e.printStackTrace();
                        MyDialog.ToastMessageInMainThread(e.getMessage());
                    }
                }else {
                    MyDialog.ToastMessageInMainThread(retJson.getString("info"));
                }
                progressDialog.dismiss();
            });
        }

        private void showPurchaseOrderInfo(final JSONObject object){
            setView(mPurchaseOrderCodeTv,Utils.getNullStringAsEmpty(object,"cgd_id"),Utils.getNullStringAsEmpty(object,"cgd_code"));
            setWarehouse(object);
            setView(mSaleOperatorTv,Utils.getNullStringAsEmpty(object,"cg_pt_user_id"),Utils.getNullStringAsEmpty(object,"cg_user_cname"));
            setView(mSupplierTV,Utils.getNullStringAsEmpty(object,"gs_id"),Utils.getNullStringAsEmpty(object,"gs_name"));

            setOrderDetailsWithSourceOrder(Utils.getNullObjectAsEmptyJsonArray(object,"goods_list"));
        }
        private void setWarehouse(final JSONObject order){
            final JSONObject object = new JSONObject();
            if (SQLiteHelper.execSql(object,String.format(Locale.CHINA,"SELECT stores_name,stores_id,wh_id FROM shop_stores where wh_id = '%s'",Utils.getNullStringAsEmpty(order,"wh_id")))){
                if (object.isEmpty()){
                    Toast.makeText(this,"仓库对应门店信息不存在!",Toast.LENGTH_SHORT).show();
                }else{
                    setView(mWarehouseTv,Utils.getNullStringAsEmpty(object,"stores_id"),Utils.getNullStringAsEmpty(object,"stores_name"));
                }
            }else {
                Toast.makeText(this,"查询门店信息错误," + object.getString("info"),Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected JSONObject generateQueryCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/rkd/xinfo");
            condition.put("id_name","rkd_id");
            return condition;
        }

        @Override
        protected void showOrder() {
            super.showOrder();
            setView(mOrderCodeTv, "", Utils.getNullStringAsEmpty(mOrderInfo, "rkd_code"));
            setView(mPurchaseOrderCodeTv,Utils.getNullStringAsEmpty(mOrderInfo,"cgd_id"),Utils.getNullStringAsEmpty(mOrderInfo,"cgd_code"));
        }

        @Override
        protected AbstractBusinessOrderDetailsDataAdapter<? extends AbstractDataAdapter.SuperViewHolder> getAdapter() {
            return new MobileWarehouseOrderDetailsAdapter(this);
        }

        @Override
        protected String generateOrderCodePrefix() {
            return "RK";
        }

        @Override
        protected JSONObject generateUploadCondition() {
            final JSONObject upload_obj = super.generateUploadCondition(),object = new JSONObject();

            upload_obj.put("rkd_code",mOrderCodeTv.getText().toString());
            upload_obj.put("rkd_id",Utils.getNullStringAsEmpty(mOrderInfo,"cgd_id"));
            upload_obj.put("cgd_code",mPurchaseOrderCodeTv.getText());
            upload_obj.put("goods_list_json",getGoodsList());

            object.put("api","/api/rkd/add");
            object.put("upload_obj",upload_obj);
            return object;
        }

        private JSONArray getGoodsList(){
            final JSONArray array = getOrderDetails(),data = new JSONArray();

            for (int i = 0,size = array.size();i < size;i ++){
                final JSONObject object = new JSONObject(),old_obj = array.getJSONObject(i);

                object.put("xnum",old_obj.getDoubleValue("xnum"));
                object.put("price",old_obj.getDoubleValue("price"));
                object.put("xnote","");
                object.put("barcode_id",old_obj.getString("barcode_id"));
                object.put("goods_id",old_obj.getString("goods_id"));
                object.put("conversion",old_obj.getString("conversion"));
                object.put("produce_date","");
                object.put("unit_id",old_obj.getString("unit_id"));

                data.add(object);
            }

            return data;
        }

        @Override
        protected JSONObject generateAuditCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/rkd/sh");
            return condition;
        }

        @Override
        protected String getOrderIDKey() {
            return "rkd_id";
        }

    }
}
