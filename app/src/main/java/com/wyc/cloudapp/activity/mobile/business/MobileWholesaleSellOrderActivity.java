package com.wyc.cloudapp.activity.mobile.business;

import android.content.Intent;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileAddWholesaleSellOrderDetailAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForJson;
import com.wyc.cloudapp.adapter.business.MobileWholesaleSellOrderAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.OrderPrintContentBase;
import com.wyc.cloudapp.bean.WholesaleSoldPrintContent;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

/*批发销售单*/
public class MobileWholesaleSellOrderActivity extends AbstractMobileBusinessOrderActivity {
    @Override
    protected MobileWholesaleSellOrderAdapter getAdapter() {
        return new MobileWholesaleSellOrderAdapter(this);
    }

    @Override
    protected String getPermissionId() {
        return "41";
    }

    @Override
    protected JSONObject generateQueryCondition() {
        final JSONObject condition = new JSONObject();
        condition.put("api","/api/pfd/xlist");
        return condition;
    }

    @Override
    public Class<?> jumpAddTarget() {
        return MobileAddWholesaleSellOrderActivity.class;
    }

    public static class MobileAddWholesaleSellOrderActivity extends MobileWholesaleBaseActivity {

        @Override
        protected JSONObject generateQueryDetailCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/pfd/xinfo");
            return condition;
        }

        @Override
        protected void showOrder() {
            super.showOrder();
            setView(mDateTv, "", FormatDateTimeUtils.formatTimeWithTimestamp(mOrderInfo.getLongValue("addtime") * 1000));
            setView(mOrderCodeTv, "",Utils.getNullStringAsEmpty(mOrderInfo,"order_code"));
            setSourceOrder("",Utils.getNullStringAsEmpty(mOrderInfo,"pfd_code"));
        }

        @Override
        protected AbstractBusinessOrderDetailsDataAdapter<? extends AbstractDataAdapterForJson.SuperViewHolder> getAdapter() {
            return new MobileAddWholesaleSellOrderDetailAdapter(this);
        }

        @Override
        protected String generateOrderCodePrefix() {
            return "PF";
        }

        @Override
        protected JSONObject generateUploadCondition() {
            final JSONObject upload_obj = super.generateUploadCondition(),object = new JSONObject();
            upload_obj.remove("gs_id");

            upload_obj.put("order_code",mOrderCodeTv.getText().toString());
            upload_obj.put("order_id", Utils.getNullStringAsEmpty(mOrderInfo,getOrderIDKey()));
            upload_obj.put("c_s_id",getCustomerId());
            upload_obj.put("settlement_mode",getSettlementType());
            upload_obj.put("goods_list_json",getGoodsList());

            object.put("api","/api/pfd/add");
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
                object.put("unit_id",old_obj.getString("unit_id"));
                data.add(object);
            }

            return data;
        }

        @Override
        protected JSONObject generateAuditCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/pfd/sh");
            return condition;
        }

        @Override
        protected String getOrderIDKey() {
            return "order_id";
        }

        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_wholesale_sell_order;
        }

        @Override
        protected String getOrderPrintName() {
            return getString(R.string.wholesale_sale_sz);
        }
        @Override
        protected OrderPrintContentBase getPrintContent(){
            return new WholesaleSoldPrintContent();
        }

        @Override
        protected void querySourceOrderInfo(final String id){
            final JSONObject parameterObj = new JSONObject();
            parameterObj.put("appid",getAppId());
            parameterObj.put("stores_id",getStoreId());
            parameterObj.put("pt_user_id",getPtUserId());
            parameterObj.put("order_id",id);

            final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(this,getString(R.string.hints_query_data_sz));
            CustomApplication.execute(()->{
                final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/pfdhd/xinfo", HttpRequest.generate_request_parma(parameterObj,getAppSecret()),true);
                if (HttpUtils.checkRequestSuccess(retJson)){
                    try {
                        final JSONObject info = JSONObject.parseObject(retJson.getString("info"));
                        if (HttpUtils.checkBusinessSuccess(info)){
                            Logger.d_json(info.getJSONObject("data").toString());
                            runOnUiThread(()-> showWholesaleOrderInfo(info.getJSONObject("data")));
                        }else throw new JSONException(info.getString("info"));
                    }catch (JSONException e){
                        e.printStackTrace();
                        MyDialog.toastMessage(e.getMessage());
                    }
                }
                progressDialog.dismiss();
            });
        }

        private void showWholesaleOrderInfo(final JSONObject object){
            setSourceOrder(Utils.getNullStringAsEmpty(object,"order_id"),Utils.getNullStringAsEmpty(object,"order_code"));
            setWarehouse(object);
            setView(mSaleOperatorTv,Utils.getNullStringAsEmpty(object,getSaleOperatorKey()),Utils.getNullStringAsEmpty(object,getSaleOperatorNameKey()));
            setCustomer(Utils.getNullStringAsEmpty(object,"c_s_id"),Utils.getNullStringAsEmpty(object,"cs_xname"));
            setOrderDetailsWithSourceOrder(Utils.getNullObjectAsEmptyJsonArray(object,"goods_list"));
        }

        @Override
        protected Intent launchSourceActivity() {
            final Intent intent = super.launchSourceActivity();
            intent.setClass(this,MobileWholesaleOrderActivity.class);
            intent.putExtra(AbstractDefinedTitleActivity.TITLE_KEY,getString(R.string.select_anything_hint,getString(R.string.wholesale_order_sz)));
            return intent;
        }
    }
}
