package com.wyc.cloudapp.activity.mobile.business;

import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileWarehouseOrderAdapter;
import com.wyc.cloudapp.adapter.business.MobileWarehouseOrderDetailsAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForJson;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

/*采购入库单*/
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
    protected String getPermissionId() {
        return "11";
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

    public static class MobileAddWarehouseOrderActivity extends AbstractMobileQuerySourceOrderActivity {
        @Override
        protected Intent launchSourceActivity() {
            final Intent intent = super.launchSourceActivity();
            intent.setClass(this,MobilePurchaseOrderActivity.class);
            intent.putExtra(AbstractMobileActivity.TITLE_KEY,getString(R.string.select_anything_hint,getString(R.string.purchase_order_sz)));
            return intent;
        }

        @Override
        protected void querySourceOrderInfo(final String id){
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
                }
                progressDialog.dismiss();
            });
        }

        private void showPurchaseOrderInfo(final JSONObject object){
            setSourceOrder(Utils.getNullStringAsEmpty(object,"cgd_id"),Utils.getNullStringAsEmpty(object,"cgd_code"));
            setWarehouse(object);
            setView(mSaleOperatorTv,Utils.getNullStringAsEmpty(object,"cg_pt_user_id"),Utils.getNullStringAsEmpty(object,"cg_user_cname"));
            setView(mSupplierTV,Utils.getNullStringAsEmpty(object,"gs_id"),Utils.getNullStringAsEmpty(object,"gs_name"));

            setOrderDetailsWithSourceOrder(Utils.getNullObjectAsEmptyJsonArray(object,"goods_list"));
        }

         @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_warehouse_order;
        }

        @Override
        protected JSONObject generateQueryDetailCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/rkd/xinfo");
            return condition;
        }

        @Override
        protected void showOrder() {
            super.showOrder();
            setView(mOrderCodeTv, "", Utils.getNullStringAsEmpty(mOrderInfo, "rkd_code"));
            setSourceOrder(Utils.getNullStringAsEmpty(mOrderInfo,"cgd_id"),Utils.getNullStringAsEmpty(mOrderInfo,"cgd_code"));
        }

        @Override
        protected AbstractBusinessOrderDetailsDataAdapter<? extends AbstractDataAdapterForJson.SuperViewHolder> getAdapter() {
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
            upload_obj.put("cgd_code",getSourceOrder());
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
