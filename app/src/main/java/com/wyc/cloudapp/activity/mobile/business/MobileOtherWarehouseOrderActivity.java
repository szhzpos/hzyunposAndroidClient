package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForJson;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDataAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileOtherWarehouseOrderAdapter;
import com.wyc.cloudapp.adapter.business.MobileOtherWarehouseOrderDetailsAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.BusinessOrderPrintSetting;
import com.wyc.cloudapp.bean.OrderPrintContentBase;
import com.wyc.cloudapp.bean.OtherWarehouseOrderPrintContent;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForJson;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/*其他出入库单*/
public final class MobileOtherWarehouseOrderActivity extends AbstractMobileBusinessOrderActivity {

    @Override
    protected AbstractBusinessOrderDataAdapter<? extends AbstractDataAdapterForJson.SuperViewHolder> getAdapter() {
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
                final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,sz.substring(0,sz.length() - 1));
                treeListDialog.setData(getOutInTypes(),null,true);
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    out_in_type_tv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    out_in_type_tv.setTag(Utils.getNullOrEmptyStringAsDefault(object,TreeListBaseAdapter.COL_ID,"-1"));
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
            if ("3".equals(bgd_type)){
                setView(mOutInTypeTv,bgd_type,"会员暂存入库");
            }else if("7".equals(bgd_type)){
                setView(mOutInTypeTv,bgd_type,"会员暂存出库");
            }else
                for (int i = 0,size = array.size();i < size;i ++){
                    final JSONObject object = array.getJSONObject(i);
                    if (bgd_type.equals(object.getString(TreeListBaseAdapter.COL_ID))){
                        setView(mOutInTypeTv,bgd_type,object.getString(TreeListBaseAdapter.COL_NAME));
                        return;
                    }
                }
        }

        private String getOutInType(){
            final JSONArray array = getOutInTypes();
            final String bgd_type = Utils.getNullStringAsEmpty(mOrderInfo,"bgd_type");
            for (int i = 0,size = array.size();i < size;i ++){
                final JSONObject object = array.getJSONObject(i);
                if (bgd_type.equals(object.getString(TreeListBaseAdapter.COL_ID))){
                    return object.getString(TreeListBaseAdapter.COL_NAME);
                }
            }
            return "";
        }

        @Override
        protected JSONObject generateUploadCondition() {
            final JSONObject object = new JSONObject();
            final String bgd_type = Utils.getViewTagValue(mOutInTypeTv,"-1");
            if (!"-1".equals(bgd_type)){
                final JSONObject upload_obj = super.generateUploadCondition();
                upload_obj.put("bgd_code",mOrderCodeTv.getText().toString());
                upload_obj.put("bgd_id", Utils.getNullStringAsEmpty(mOrderInfo,getOrderIDKey()));
                upload_obj.put("bgd_type",Utils.getViewTagValue(mOutInTypeTv,"-1"));
                upload_obj.put("goods_list_json",getGoodsList());

                object.put("api", InterfaceURL.O_OUT_IN_UPLOAD);
                object.put("upload_obj",upload_obj);
            }else {
                MyDialog.toastMessage(getString(R.string.input_hint,getString(R.string.out_in)));
            }
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
            condition.put("api",InterfaceURL.OUT_IN_SH);
            return condition;
        }

        @Override
        protected String getOrderIDKey() {
            return "bgd_id";
        }

        @Override
        protected String getPrintContent(BusinessOrderPrintSetting setting) {
            final OrderPrintContentBase.Builder Builder = new OrderPrintContentBase.Builder(new OtherWarehouseOrderPrintContent());
            final List<OrderPrintContentBase.Goods> details = new ArrayList<>();
            JSONArray goods_list;
            final String name = getString(R.string.other_inventory_sz);
            if (isNewOrder()){
                goods_list = getOrderDetails();
                Builder.company(getStoreName())
                        .orderName(name)
                        .storeName(mWarehouseTv.getText().toString())
                        .inOutType(mOutInTypeTv.getText().toString())
                        .operator(mSaleOperatorTv.getText().toString())
                        .orderNo(mOrderCodeTv.getText().toString())
                        .operateDate(FormatDateTimeUtils.formatCurrentTime(FormatDateTimeUtils.YYYY_MM_DD_1))
                        .remark(mRemarkEt.getText().toString());
            }else {
                goods_list = mOrderInfo.getJSONArray("goods_list");
                Builder.company(getStoreName())
                        .orderName(name)
                        .storeName(getStoreName())
                        .inOutType(getOutInType())
                        .operator(mOrderInfo.getString(getSaleOperatorNameKey()))
                        .orderNo(mOrderInfo.getString("bgd_code"))
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