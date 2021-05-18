package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDataAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileBaseOrderDetailsAdapter;
import com.wyc.cloudapp.adapter.business.MobileTransferOutOrderAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.TreeListDialog;
import com.wyc.cloudapp.utils.Utils;
/*调出单*/
public class MobileTransferOutOrderActivity extends AbstractMobileBusinessOrderActivity {

    @Override
    protected AbstractBusinessOrderDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
        return new MobileTransferOutOrderAdapter(this);
    }

    @Override
    protected JSONObject generateQueryCondition() {
        final JSONObject condition = new JSONObject();
        condition.put("api","/api/api_move_out/xlist");
        return condition;
    }

    @Override
    public Class<?> jumpAddTarget() {
        return MobileAddTransferOutOrderActivity.class;
    }

    @Override
    protected String getPermissionId() {
        return "45";
    }

    public static class MobileAddTransferOutOrderActivity extends AbstractMobileAddOrderActivity{
        private TextView mTransferInWhTv;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            initTransferInWarehouse();
        }
        private void initTransferInWarehouse(){
            final TextView transfer_in_wh_tv = findViewById(R.id.transfer_in_wh_tv);
            final String sz = getString(R.string.transfer_in_wh);
            final JSONArray array = getTransferInWarehouse(transfer_in_wh_tv);
            transfer_in_wh_tv.setOnClickListener(v -> CustomApplication.runInMainThread(()->{
                final TreeListDialog treeListDialog = new TreeListDialog(this,sz.substring(0,sz.length() - 1));
                treeListDialog.setDatas(array,null,true);
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    setTransferInWarehouse(object.getString("item_id"),object.getString("item_name"));
                }
            }));

            mTransferInWhTv = transfer_in_wh_tv;
        }
        private JSONArray getTransferInWarehouse(final TextView tv){
            final StringBuilder err = new StringBuilder();
            final JSONArray array = SQLiteHelper.getListToJson("SELECT wh_id,stores_name name FROM shop_stores where stores_id <>" + getStoreId(),err),data = new JSONArray();

            if (null != array){
                JSONObject object;
                for (int i = 0,size = array.size();i < size;i++){
                    final JSONObject tmp = array.getJSONObject(i);
                    final String id = Utils.getNullStringAsEmpty(tmp,"wh_id"),name = Utils.getNullStringAsEmpty(tmp,"name");

                    object = new JSONObject();
                    object.put("level",0);
                    object.put("unfold",false);
                    object.put("isSel",false);
                    object.put("item_id",id);
                    object.put("item_name",name);
                    data.add(object);

                    if (i == 0){
                        tv.setTag(id);
                        tv.setText(name);
                    }
                }
            }else {
                MyDialog.ToastMessage(err.toString(),this,null);
            }
            return data;
        }
        private void setTransferInWarehouse(final String id,final String name){
            mTransferInWhTv.setText(name);
            mTransferInWhTv.setTag(id);
        }
        private String getTransferInWarehouseId(){
            return Utils.getViewTagValue(mTransferInWhTv,"");
        }

        @Override
        protected String getSaleOperatorKey() {
            return "js_pt_user_id";
        }
        @Override
        protected String getSaleOperatorNameKey() {
            return "js_pt_user_name";
        }

        @Override
        protected void showOrder() {
            super.showOrder();
            setTransferInWarehouse(Utils.getNullStringAsEmpty(mOrderInfo,"dr_wh_id"),Utils.getNullStringAsEmpty(mOrderInfo,"dr_wh_name"));
            setView(mOrderCodeTv,Utils.getNullStringAsEmpty(mOrderInfo,"ckd_id"),Utils.getNullStringAsEmpty(mOrderInfo,"ckd_code"));
            setView(mDateTv, "",Utils.formatDataWithTimestamp(mOrderInfo.getLongValue("addtime") * 1000));
        }

        @Override
        protected JSONObject generateQueryDetailCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/api_move_out/xinfo");
            return condition;
        }

        @Override
        protected AbstractBusinessOrderDetailsDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
            return new MobileBaseOrderDetailsAdapter(this);
        }

        @Override
        protected JSONObject generateUploadCondition() {
            final JSONObject upload_obj = super.generateUploadCondition(),object = new JSONObject();
            upload_obj.remove("gs_id");

            upload_obj.put("ckd_code",mOrderCodeTv.getText().toString());
            upload_obj.put("ckd_id", Utils.getNullStringAsEmpty(mOrderInfo,getOrderIDKey()));
            upload_obj.put("dr_wh_id",getTransferInWarehouseId());
            upload_obj.put("goods_list_json",getGoodsList());

            object.put("api","/api/api_move_out/add");
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
        protected String generateOrderCodePrefix() {
            return "DB";
        }

        @Override
        protected JSONObject generateAuditCondition() {
            final JSONObject condition = new JSONObject();
            condition.put("api","/api/api_move_out/sh");
            return condition;
        }

        @Override
        protected String getOrderIDKey() {
            return "ckd_id";
        }

        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_mobile_add_transfer_out_order;
        }
    }
}