package com.wyc.cloudapp.activity.mobile.business;

import android.content.Intent;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileAddWholesaleOrderDetailAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForJson;
import com.wyc.cloudapp.adapter.business.MobileWholesaleOrderAdapter;
import com.wyc.cloudapp.utils.Utils;

/*批发订货单*/
public final class MobileWholesaleOrderActivity extends AbstractMobileBusinessOrderActivity {
    @Override
    protected MobileWholesaleOrderAdapter getAdapter() {
        return new MobileWholesaleOrderAdapter(this);
    }

    @Override
    protected JSONObject generateQueryCondition() {
        final JSONObject condition = new JSONObject();
        condition.put("api","/api/pfdhd/xlist");
        return condition;
    }

    @Override
    protected boolean orderIsNotShow(final JSONObject order){
        /*
         * 判断线上返回的订单信息是否需要显示
         *
         * 3全部发货 4己退货的情况从显示列表中删除
         * */
        if (order == null)return false;
        int status = order.getIntValue("rk_status");
        return status == 3 || status == 4;
    }

    @Override
    protected String getPermissionId() {
        return "39";
    }

    @Override
    public Class<?> jumpAddTarget() {
        return MobileAddWholesaleOrderActivity.class;
    }

    public static final class MobileAddWholesaleOrderActivity extends MobileWholesaleBaseActivity {

        @Override
        protected JSONObject generateQueryDetailCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/pfdhd/xinfo");
            return condition;
        }

        @Override
        protected void showOrder() {
            super.showOrder();
            setView(mOrderCodeTv, "",Utils.getNullStringAsEmpty(mOrderInfo,"order_code"));
        }

        @Override
        protected AbstractBusinessOrderDetailsDataAdapter<? extends AbstractDataAdapterForJson.SuperViewHolder> getAdapter() {
            return new MobileAddWholesaleOrderDetailAdapter(this);
        }

        @Override
        protected String generateOrderCodePrefix() {
            return "PD";
        }

        @Override
        protected JSONObject generateUploadCondition() {
            final JSONObject upload_obj = super.generateUploadCondition(),object = new JSONObject();
            upload_obj.remove("gs_id");

            upload_obj.put("order_code",mOrderCodeTv.getText().toString());
            upload_obj.put("order_id", Utils.getNullStringAsEmpty(mOrderInfo,getOrderIDKey()));
            upload_obj.put("c_s_id",getCustomerId());
            upload_obj.put("goods_list_json",getGoodsList());

            object.put("api","/api/pfdhd/add");
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
            condition.put("api","/api/pfdhd/sh");
            return condition;
        }

        @Override
        protected String getOrderIDKey() {
            return "order_id";
        }

        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_wholesale_order;
        }

        @Override
        protected void querySourceOrderInfo(String order_id) {

        }

        @Override
        protected Intent launchSourceActivity() {
            return super.launchSourceActivity();
        }
    }
}
