package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobilePurchaseOrderAdapter;
import com.wyc.cloudapp.adapter.business.MobilePurchaseOrderDetailsAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.utils.Utils;

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
        protected AbstractBusinessOrderDetailsDataAdapter<? extends AbstractDataAdapter.SuperViewHolder> getAdapter() {
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
    }
}