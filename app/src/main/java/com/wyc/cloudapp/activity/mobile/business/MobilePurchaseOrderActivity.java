package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobilePurchaseOrderAdapter;
import com.wyc.cloudapp.adapter.business.MobilePurchaseOrderDetailsAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForJson;
import com.wyc.cloudapp.bean.OrderPrintContentBase;
import com.wyc.cloudapp.bean.BusinessOrderPrintSetting;
import com.wyc.cloudapp.bean.PurchaseOrderPrintContent;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/*采购订货单*/
public class MobilePurchaseOrderActivity extends AbstractMobileBusinessOrderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public MobilePurchaseOrderAdapter getAdapter() {
        return new MobilePurchaseOrderAdapter(this);
    }

    @Override
    protected JSONObject generateQueryCondition() {
        final JSONObject condition = new JSONObject();
        condition.put("api","/api/cgd/xlist");
        return condition;
    }
    @Override
    protected String getPermissionId() {
        return "10";
    }

    @Override
    public Class<?> jumpAddTarget() {
        return MobileAddPurchaseOrderActivity.class;
    }

    public static class MobileAddPurchaseOrderActivity extends AbstractMobileAddOrderActivity {
        private double mTotal;
        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_purchase_order;
        }

        @Override
        protected JSONObject generateQueryDetailCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/cgd/xinfo");
            return condition;
        }

        @Override
        protected void showOrder() {
            super.showOrder();
            setView(mOrderCodeTv, "", Utils.getNullStringAsEmpty(mOrderInfo, "cgd_code"));
        }

        @Override
        protected AbstractBusinessOrderDetailsDataAdapter<? extends AbstractDataAdapterForJson.SuperViewHolder> getAdapter() {
            return new MobilePurchaseOrderDetailsAdapter(this);
        }

        @Override
        protected String generateOrderCodePrefix() {
            return "CG";
        }

        @Override
        protected JSONObject generateUploadCondition() {
            final JSONObject upload_obj = super.generateUploadCondition(),object = new JSONObject();

            upload_obj.put("cgd_code",mOrderCodeTv.getText().toString());
            upload_obj.put("cgd_id",Utils.getNullStringAsEmpty(mOrderInfo,getOrderIDKey()));
            upload_obj.put("validity_time",getOrderValidityDate());
            upload_obj.put("goods_list_json",getGoodsList());
            upload_obj.put("total",mTotal);

            object.put("api","/api/cgd/add");
            object.put("upload_obj",upload_obj);
            return object;
        }

        @Override
        protected JSONObject generateAuditCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/cgd/sh");
            return condition;
        }

        @Override
        protected String getOrderIDKey() {
            return "cgd_id";
        }

        private JSONArray getGoodsList(){
            final JSONArray array = getOrderDetails(),data = new JSONArray();
            double xnum = 0.0,price = 0.0;
            for (int i = 0,size = array.size();i < size;i ++){
                final JSONObject object = new JSONObject(),old_obj = array.getJSONObject(i);

                xnum = old_obj.getDoubleValue("xnum");
                price = old_obj.getDoubleValue("price");

                mTotal += xnum * price;

                object.put("xnum",xnum);
                object.put("price",price);
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
        protected String getPrintContent(BusinessOrderPrintSetting setting) {
            final OrderPrintContentBase.Builder Builder = new OrderPrintContentBase.Builder(new PurchaseOrderPrintContent());
            final List<OrderPrintContentBase.Goods> details = new ArrayList<>();
            final String name = getString(R.string.purchase_order_sz);
            JSONArray goods_list;
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
                        .operateDate(mOrderInfo.getString("add_datetime"))
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