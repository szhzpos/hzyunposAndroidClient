package com.wyc.cloudapp.activity.mobile.business;

import android.content.Intent;
import android.os.Bundle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobilePurchaseOrderAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileWholesaleRefundOrderAdapter;
import com.wyc.cloudapp.adapter.business.MobileWholesaleRefundOrderDetailAdapter;
import com.wyc.cloudapp.utils.Utils;

/*批发退货单*/
public class MobileWholesaleRefundOrderActivity extends AbstractMobileBusinessOrderActivity {
    @Override
    protected MobileWholesaleRefundOrderAdapter getAdapter() {
        return new MobileWholesaleRefundOrderAdapter(this);
    }

    @Override
    protected JSONObject generateQueryCondition() {
        final JSONObject condition = new JSONObject();
        condition.put("api","/api/pf_refund/xlist");
        return condition;
    }

    @Override
    protected String getPermissionId() {
        return "42";
    }

    @Override
    public Class<?> jumpAddTarget() {
        return MobileAddWholesaleRefundOrderActivity.class;
    }

    public static class MobileAddWholesaleRefundOrderActivity extends MobileWholesaleBaseActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        protected JSONObject generateQueryDetailCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/pf_refund/xinfo");
            return condition;
        }

        @Override
        protected void showOrder() {
            super.showOrder();
            setView(mOrderCodeTv, "",Utils.getNullStringAsEmpty(mOrderInfo,"refund_code"));
            setSourceOrder("",Utils.getNullStringAsEmpty(mOrderInfo,"order_code"));
        }

        @Override
        protected AbstractBusinessOrderDetailsDataAdapter<? extends AbstractDataAdapter.SuperViewHolder> getAdapter() {
            return new MobileWholesaleRefundOrderDetailAdapter(this);
        }

        @Override
        protected String generateOrderCodePrefix() {
            return "PT";
        }

        @Override
        protected JSONObject generateUploadCondition() {
            final JSONObject upload_obj = super.generateUploadCondition(),object = new JSONObject();
            upload_obj.remove("gs_id");

            upload_obj.put("refund_code",mOrderCodeTv.getText().toString());
            upload_obj.put("order_id", Utils.getNullStringAsEmpty(mOrderInfo,getOrderIDKey()));
            upload_obj.put("c_s_id",getCustomerId());
            upload_obj.put("settlement_mode",getSettlementType());
            upload_obj.put("goods_list_json",getGoodsList());

            object.put("api","/api/pf_refund/add");
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
            condition.put("api","/api/pf_refund/sh");
            return condition;
        }

        @Override
        protected String getOrderIDKey() {
            return "order_id";
        }

        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_wholesale_refund_order;
        }

        @Override
        protected void querySourceOrderInfo(String order_id) {

        }

        @Override
        protected Intent launchSourceActivity() {
            final Intent intent = super.launchSourceActivity();
            intent.setClass(this,MobileWholesaleSellOrderActivity.class);
            intent.putExtra("title",getString(R.string.select_anything_hint,getString(R.string.wholesale_sale_sz)));
            return intent;
        }
    }
}
