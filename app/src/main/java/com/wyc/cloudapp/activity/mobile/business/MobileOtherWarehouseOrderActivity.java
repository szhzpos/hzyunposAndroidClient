package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDataAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileOtherWarehouseOrderAdapter;
import com.wyc.cloudapp.adapter.business.MobileOtherWarehouseOrderDetailsAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.TreeListDialog;
import com.wyc.cloudapp.utils.Utils;
/*其他出入库单*/
public final class MobileOtherWarehouseOrderActivity extends AbstractMobileBusinessOrderActivity {

    @Override
    protected AbstractBusinessOrderDataAdapter<? extends AbstractDataAdapter.SuperViewHolder> getAdapter() {
        return new MobileOtherWarehouseOrderAdapter(this);
    }

    @Override
    protected JSONObject generateQueryCondition() {
        final JSONObject condition = new JSONObject();
        condition.put("api","/api/bgd/xlist");
        return condition;
    }

    @Override
    public Class<?> jumpAddTarget() {
        return MobileAddOtherWarehouseOrderActivity.class;
    }

    @Override
    protected String getPermissionId() {
        return "12";
    }

    public static final class MobileAddOtherWarehouseOrderActivity extends AbstractMobileAddOrderActivity{
        private TextView mOutInTypeTv;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            initOutInType();
        }

        private void initOutInType(){
            final TextView out_in_type_tv = findViewById(R.id.out_in_type_tv);
            final String sz = getString(R.string.out_in_warehouse);

            out_in_type_tv.setOnClickListener(v -> CustomApplication.runInMainThread(()->{
                final TreeListDialog treeListDialog = new TreeListDialog(this,sz.substring(0,sz.length() - 1));
                treeListDialog.setDatas(getOutInTypes(),null,true);
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    out_in_type_tv.setText(object.getString("item_name"));
                    out_in_type_tv.setTag(object.getString("item_id"));
                }
            }));
            mOutInTypeTv = out_in_type_tv;
        }

        private JSONArray getOutInTypes(){
            return JSONArray.parseArray("[{\"item_id\":1,\"item_name\":\"其它入库\"},{\"item_id\":2,\"item_name\":\"归还入库\"},{\"item_id\":4,\"item_name\":\"其它出库\"}," +
                    "{\"item_id\":5,\"item_name\":\"领用出库\"},{\"item_id\":6,\"item_name\":\"报损出库\"}]");
        }

        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_otherwarehouse_order;
        }

        @Override
        protected String getSaleOperatorKey() {
            return "js_pt_user_id";
        }
        @Override
        protected String getSaleOperatorNameKey() {
            return "js_user_cname";
        }

        @Override
        protected void showOrder() {
            super.showOrder();
            setView(mOrderCodeTv, Utils.getNullStringAsEmpty(mOrderInfo, "bgd_id"), Utils.getNullStringAsEmpty(mOrderInfo, "bgd_code"));
            setOutInType();
        }
        private void setOutInType(){
            final JSONArray array = getOutInTypes();
            final String bgd_type = Utils.getNullStringAsEmpty(mOrderInfo,"bgd_type");
            for (int i = 0,size = array.size();i < size;i ++){
                final JSONObject object = array.getJSONObject(i);
                if (bgd_type.equals(object.getString("item_id"))){
                    setView(mOutInTypeTv,bgd_type,object.getString("item_name"));
                    return;
                }
            }
        }

        @Override
        protected JSONObject generateUploadCondition() {
            final JSONObject upload_obj = super.generateUploadCondition(),object = new JSONObject();

            upload_obj.put("bgd_code",mOrderCodeTv.getText().toString());
            upload_obj.put("bgd_id", Utils.getNullStringAsEmpty(mOrderInfo,getOrderIDKey()));
            upload_obj.put("bgd_type",Utils.getViewTagValue(mOutInTypeTv,"-1"));
            upload_obj.put("goods_list_json",getGoodsList());

            object.put("api","/api/bgd/add");
            object.put("upload_obj",upload_obj);
            return object;
        }

        private JSONArray getGoodsList(){
            final JSONArray array = getOrderDetails(),data = new JSONArray();

            for (int i = 0,size = array.size();i < size;i ++){
                final JSONObject object = new JSONObject(),old_obj = array.getJSONObject(i);
                object.put("xnum",old_obj.getDoubleValue("xnum"));
                object.put("price",old_obj.getDoubleValue("price"));
                object.put("buying_price",old_obj.getDoubleValue("price"));
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
        protected JSONObject generateQueryDetailCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/bgd/xinfo");
            return condition;
        }

        @Override
        protected AbstractBusinessOrderDetailsDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
            return new MobileOtherWarehouseOrderDetailsAdapter(this);
        }

        @Override
        protected String generateOrderCodePrefix() {
            return "BG";
        }

        @Override
        protected JSONObject generateAuditCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/bgd/sh");
            return condition;
        }

        @Override
        protected String getOrderIDKey() {
            return "bgd_id";
        }
    }

}