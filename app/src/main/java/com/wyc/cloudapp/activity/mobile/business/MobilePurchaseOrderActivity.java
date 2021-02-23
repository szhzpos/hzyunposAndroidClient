package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.business.MobilePurchaseOrderAdapter;
import com.wyc.cloudapp.adapter.business.MobilePurchaseOrderDetailsAdapter;
import com.wyc.cloudapp.adapter.report.AbstractDataAdapter;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

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
    public Class<?> jumpAddTarget() {
        return MobileAddPurchaseOrderActivity.class;
    }

    public static class MobileAddPurchaseOrderActivity extends AbstractMobileAddOrderActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            initView();
        }

        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_purchase_order;
        }

        @Override
        protected JSONObject generateQueryCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/cgd/xinfo");
            return condition;
        }

        @Override
        protected void showOrder() {
            super.showOrder();

            final JSONObject object = mOrderInfo;
            setView(mSupplierTV, Utils.getNullStringAsEmpty(object, "gs_id"), Utils.getNullStringAsEmpty(object, "gs_name"));
            setView(mSaleOperatorTv, Utils.getNullStringAsEmpty(object, "cg_pt_user_id"), Utils.getNullStringAsEmpty(object, "cg_user_cname"));
            setView(mOrderCodeTv, "", Utils.getNullStringAsEmpty(object, "cgd_code"));
            setView(mDateTv, "", Utils.getNullStringAsEmpty(object, "add_datetime"));
            setView(mRemarkEt, "", Utils.getNullStringAsEmpty(object, "remark"));
        }

        @Override
        protected AbstractDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
            return new MobilePurchaseOrderDetailsAdapter(this);
        }

        @Override
        protected String generateOrderCodePrefix() {
            return "CG";
        }

    }
}