package com.wyc.cloudapp.activity.mobile.business;

import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForJson;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobilePurchaseRefundOrderAdapter;
import com.wyc.cloudapp.adapter.business.MobilePurchaseRefundOrderDetailsAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.OrderPrintContentBase;
import com.wyc.cloudapp.bean.BusinessOrderPrintSetting;
import com.wyc.cloudapp.bean.PurchaseRefundOrderPrintContent;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.util.ArrayList;
import java.util.List;

/*采购退货单*/
public class MobilePurchaseRefundOrderActivity extends AbstractMobileBusinessOrderActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public MobilePurchaseRefundOrderAdapter getAdapter() {
        return new MobilePurchaseRefundOrderAdapter(this);
    }

    @Override
    protected String getPermissionId() {
        return "47";
    }

    @Override
    protected JSONObject generateQueryCondition() {
        final JSONObject condition = new JSONObject();
        condition.put("api","/api/thd/xlist");
        return condition;
    }

    @Override
    public Class<?> jumpAddTarget() {
        return MobileAddPurchaseRefundOrderActivity.class;
    }

    public static class MobileAddPurchaseRefundOrderActivity extends AbstractMobileQuerySourceOrderActivity {
        @Override
        protected Intent launchSourceActivity() {
            final Intent intent = super.launchSourceActivity();
            intent.setClass(this,MobileWarehouseOrderActivity.class);
            intent.putExtra(AbstractDefinedTitleActivity.TITLE_KEY,getString(R.string.select_anything_hint,getString(R.string.warehouse_order_sz)));
            return intent;
        }

        protected String getSaleOperatorNameKey(){
            return "js_user_name";
        }

        @Override
        protected void querySourceOrderInfo(final String id){
            final JSONObject parameterObj = new JSONObject();
            parameterObj.put("appid",getAppId());
            parameterObj.put("stores_id",getStoreId());
            parameterObj.put("pt_user_id",getPtUserId());
            parameterObj.put("rkd_id",id);

            final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(this,getString(R.string.hints_query_data_sz));
            CustomApplication.execute(()->{
                final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/rkd/xinfo", HttpRequest.generate_request_parma(parameterObj,getAppSecret()),true);
                if (HttpUtils.checkRequestSuccess(retJson)){
                    try {
                        final JSONObject info = JSONObject.parseObject(retJson.getString("info"));
                        if (HttpUtils.checkBusinessSuccess(info)){
                            Logger.d_json(info.getJSONObject("data").toString());
                            runOnUiThread(()-> showWarehouseOrderInfo(info.getJSONObject("data")));
                        }else throw new JSONException(info.getString("info"));
                    }catch (JSONException e){
                        e.printStackTrace();
                        MyDialog.toastMessage(e.getMessage());
                    }
                }
                progressDialog.dismiss();
            });
        }

        private void showWarehouseOrderInfo(final JSONObject object){
            setSourceOrder(Utils.getNullStringAsEmpty(object,"rkd_id"),Utils.getNullStringAsEmpty(object,"rkd_code"));
            setWarehouse(object);
            setView(mSaleOperatorTv,Utils.getNullStringAsEmpty(object,"cg_pt_user_id"),Utils.getNullStringAsEmpty(object,"cg_user_cname"));
            setView(mSupplierTV,Utils.getNullStringAsEmpty(object,"gs_id"),Utils.getNullStringAsEmpty(object,"gs_name"));

            setOrderDetailsWithSourceOrder(Utils.getNullObjectAsEmptyJsonArray(object,"goods_list"));
        }
        ///

        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_purchase_refund_order;
        }

        @Override
        protected JSONObject generateQueryDetailCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/thd/xinfo");
            return condition;
        }

        @Override
        protected void showOrder() {
            super.showOrder();
            setView(mOrderCodeTv, "", Utils.getNullStringAsEmpty(mOrderInfo, "cgd_code"));
            setView(mDateTv, "", FormatDateTimeUtils.formatTimeWithTimestamp(mOrderInfo.getLongValue("addtime") * 1000));
            setSourceOrder("",Utils.getNullStringAsEmpty(mOrderInfo,"out_cgd_code"));
        }

        @Override
        protected AbstractBusinessOrderDetailsDataAdapter<? extends AbstractDataAdapterForJson.SuperViewHolder> getAdapter() {
            return new MobilePurchaseRefundOrderDetailsAdapter(this);
        }

        @Override
        protected String generateOrderCodePrefix() {
            return "GT";
        }

        @Override
        protected JSONObject generateUploadCondition() {
            final JSONObject upload_obj = super.generateUploadCondition(),object = new JSONObject();

            upload_obj.put("out_cgd_code",getSourceOrder());
            upload_obj.put("cgd_code",mOrderCodeTv.getText());
            upload_obj.put("cgd_id",Utils.getNullStringAsEmpty(mOrderInfo,"cgd_id"));
            upload_obj.put("goods_list_json",getGoodsList());

            object.put("api","/api/thd/add");
            object.put("upload_obj",upload_obj);
            return object;
        }

        private JSONArray getGoodsList(){
            final JSONArray array = getOrderDetails(),data = new JSONArray();

            for (int i = 0,size = array.size();i < size;i ++){
                final JSONObject object = new JSONObject(),old_obj = array.getJSONObject(i);

                object.put("xnum",old_obj.getDoubleValue("xnum"));
                object.put("price",old_obj.getDoubleValue("price"));
                object.put("xnote",old_obj.getString("xnote"));
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
            condition.put("api","/api/thd/sh");
            return condition;
        }

        @Override
        protected String getOrderIDKey() {
            return "cgd_id";
        }

        @Override
        protected String getPrintContent(BusinessOrderPrintSetting setting) {
            final OrderPrintContentBase.Builder Builder = new OrderPrintContentBase.Builder(new PurchaseRefundOrderPrintContent());
            final List<OrderPrintContentBase.Goods> details = new ArrayList<>();
            JSONArray goods_list;
            final String name = getString(R.string.purchase_refund_order_sz);
            if (isNewOrder()){
                goods_list = getOrderDetails();
                Builder.company(getStoreName())
                        .orderName(name)
                        .storeName(mWarehouseTv.getText().toString())
                        .supOrCus(mSupplierTV.getText().toString())
                        .operator(mSaleOperatorTv.getText().toString())
                        .orderNo(mOrderCodeTv.getText().toString())
                        .operateDate(FormatDateTimeUtils.formatCurrentTime(FormatDateTimeUtils.YYYY_MM_DD_1))
                        .remark(mRemarkEt.getText().toString());
            }else {
                goods_list = mOrderInfo.getJSONArray("goods_list");
                Builder.company(getStoreName())
                        .orderName(name)
                        .storeName(getStoreName())
                        .supOrCus(mOrderInfo.getString("gs_name"))
                        .operator(mOrderInfo.getString(getSaleOperatorNameKey()))
                        .orderNo(mOrderInfo.getString("cgd_code"))
                        .operateDate(FormatDateTimeUtils.formatTimeWithTimestamp(mOrderInfo.getLongValue("addtime") * 1000))
                        .remark(mOrderInfo.getString("remark"));
            }
            for (int i = 0,size = goods_list.size();i < size; i++){
                final JSONObject object = goods_list.getJSONObject(i);
                final OrderPrintContentBase.Goods goods = new OrderPrintContentBase.Goods.Builder()
                        .barcodeId(object.getString("barcode_id"))
                        .barcode(object.getString("barcode"))
                        .name(object.getString("goods_title"))
                        .unit(object.getString("unit_name"))
                        .num(object.getDoubleValue("xnum"))
                        .price(object.getDoubleValue("price"))
                        .build();

                details.add(goods);
            }
            return Builder.goodsList(details).build().format58(this,setting);
        }
    }
}
