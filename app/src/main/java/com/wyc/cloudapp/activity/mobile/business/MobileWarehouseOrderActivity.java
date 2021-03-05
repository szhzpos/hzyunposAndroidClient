package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobilePurchaseOrderAdapter;
import com.wyc.cloudapp.adapter.business.MobilePurchaseOrderDetailsAdapter;
import com.wyc.cloudapp.adapter.business.MobileWarehouseOrderAdapter;
import com.wyc.cloudapp.adapter.report.AbstractDataAdapter;
import com.wyc.cloudapp.utils.Utils;

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
        private TextView mPurchaseOrderCodeTv;
        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_warehouse_order;
        }

        @Override
        protected void initView() {
            super.initView();
            mPurchaseOrderCodeTv = findViewById(R.id.m_purchase_order_tv);
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
        }

        @Override
        protected AbstractBusinessOrderDetailsDataAdapter<? extends AbstractDataAdapter.SuperViewHolder> getAdapter() {
            return new MobilePurchaseOrderDetailsAdapter(this);
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
