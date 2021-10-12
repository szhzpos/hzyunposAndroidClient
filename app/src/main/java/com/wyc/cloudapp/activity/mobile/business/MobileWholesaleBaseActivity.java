package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.WholesalePrintContent;
import com.wyc.cloudapp.bean.BusinessOrderPrintSetting;
import com.wyc.cloudapp.bean.OrderPrintContentBase;
import com.wyc.cloudapp.data.viewModel.ConsumerViewModel;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForJson;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.activity.mobile.business
 * @ClassName: MobileWholesaleBaseActivity
 * @Description: 新增批发单相关基类
 * @Author: wyc
 * @CreateDate: 2021/4/20 15:02
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/4/20 15:02
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class MobileWholesaleBaseActivity extends AbstractMobileQuerySourceOrderActivity {
    private TextView mBusinessCustomerTv;
    private JSONArray mCustomerList;
    private int mPriceType = 1;
    private TextView mSettlementWayTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCustomer();
        initSettlementWay();
    }

    @Override
    protected void showOrder() {
        super.showOrder();
        setView(mDateTv, "", FormatDateTimeUtils.formatTimeWithTimestamp(mOrderInfo.getLongValue("addtime") * 1000));
        setSettlementType();
        setCustomer(Utils.getNullStringAsEmpty(mOrderInfo,"c_s_id"),Utils.getNullStringAsEmpty(mOrderInfo,"cs_xname"));
    }

    @Override
    protected String getSaleOperatorKey() {
        return "js_pt_user_id";
    }
    @Override
    protected String getSaleOperatorNameKey() {
        return "js_pt_user_name";
    }

    private void initSettlementWay(){
        final LinearLayout settlement_layout = findViewById(R.id.settlement_layout);
        if (null != settlement_layout){
            settlement_layout.setVisibility(View.VISIBLE);
            mSettlementWayTv = settlement_layout.findViewById(R.id.settlement_way_tv);
            final String sz = getString(R.string.settlement_way);
            mSettlementWayTv.setOnClickListener(v -> CustomApplication.runInMainThread(()->{
                final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,sz.substring(0,sz.length() - 1));
                treeListDialog.setData(getSettlementTypes(),null,true);
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    mSettlementWayTv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    mSettlementWayTv.setTag(object.getString(TreeListBaseAdapter.COL_ID));
                }
            }));
            if (!isShowOrder())setSettlementType();//设置默认方式
        }
    }

    private JSONArray getSettlementTypes(){
        return JSONArray.parseArray("[{\"item_id\":1,\"item_name\":\"现结\"},{\"item_id\":2,\"item_name\":\"挂账\"}]");
    }

    private void setSettlementType(){
        final JSONArray array = getSettlementTypes();
        final String settlement_type = Utils.getNullOrEmptyStringAsDefault(mOrderInfo,"settlement_mode","2");
        for (int i = 0,size = array.size();i < size;i ++){
            final JSONObject object = array.getJSONObject(i);
            if (settlement_type.equals(object.getString(TreeListBaseAdapter.COL_ID))){
                setView(mSettlementWayTv,settlement_type,object.getString(TreeListBaseAdapter.COL_NAME));
                return;
            }
        }
    }
    protected String getSettlementType(){
        return Utils.getViewTagValue(mSettlementWayTv,"2");
    }

    protected String getCustomerId(){
        return Utils.getViewTagValue(mBusinessCustomerTv,"-1");
    }

    protected String getCustomerName(){
        return mBusinessCustomerTv.getText().toString();
    }

    protected void setCustomer(final String _id,final String code){
        setView(mBusinessCustomerTv,_id,code);
    }

    private void initCustomer(){
        mBusinessCustomerTv = findViewById(R.id.m_business_customer_tv);
        final String sup = getString(R.string.customer_colon_sz);
        mBusinessCustomerTv.setOnClickListener(v -> CustomApplication.runInMainThread(()->{
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,sup.substring(0,sup.length() - 1));
            treeListDialog.setData(mCustomerList,null,true);
            if (treeListDialog.exec() == 1){
                final JSONObject object = treeListDialog.getSingleContent();
                mBusinessCustomerTv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                mBusinessCustomerTv.setTag(object.getString(TreeListBaseAdapter.COL_ID));
                mPriceType = object.getIntValue("price_type");
            }
        }));

        new ViewModelProvider(this).get(ConsumerViewModel.class)
                .getCurrentModel().observe(this, consumers -> {
                    mCustomerList = parse_customer_info_and_set_default(JSONArray.parseArray(JSON.toJSONString(consumers)));
        });
    }

    public int getCustomerPriceType(){
        return mPriceType;
    }

    private JSONArray parse_customer_info_and_set_default(final JSONArray customers){
        final JSONArray array  = new JSONArray();
        if (customers != null){
            JSONObject object;
            for (int i = 0,size = customers.size();i < size;i++){
                final JSONObject tmp = customers.getJSONObject(i);
                final String id = Utils.getNullOrEmptyStringAsDefault(tmp,"c_s_id",""),name = Utils.getNullStringAsEmpty(tmp,"cs_xname");
                int cs_kf_price = tmp.getIntValue("cs_kf_price");

                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put(TreeListBaseAdapter.COL_ID,id);
                object.put(TreeListBaseAdapter.COL_NAME,name);
                object.put("price_type",cs_kf_price);
                array.add(object);

                //
                if (!isShowOrder() && "0000".equals(Utils.getNullStringAsEmpty(tmp,"cs_code"))  && mBusinessCustomerTv != null){
                    CustomApplication.runInMainThread(()->{
                        mBusinessCustomerTv.setText(name);
                        mBusinessCustomerTv.setTag(id);
                        mPriceType = cs_kf_price;
                    });
                }
            }
        }
        return array;
    }

    @Override
    protected String getPrintContent(BusinessOrderPrintSetting setting) {
        final OrderPrintContentBase.Builder Builder = new OrderPrintContentBase.Builder(getPrintContent());
        final List<OrderPrintContentBase.Goods> details = new ArrayList<>();
        final String name = getOrderPrintName();
        JSONArray goods_list;
        if (isNewOrder()){
            goods_list = getOrderDetails();
            Builder.company(getStoreName())
                    .orderName(name)
                    .storeName(mWarehouseTv.getText().toString())
                    .supOrCus(getCustomerName())
                    .operator(mSaleOperatorTv.getText().toString())
                    .orderNo(mOrderCodeTv.getText().toString())
                    .operateDate(FormatDateTimeUtils.formatCurrentTime(FormatDateTimeUtils.YYYY_MM_DD_1))
                    .remark(mRemarkEt.getText().toString());
        }else {
            goods_list = mOrderInfo.getJSONArray("goods_list");
            Builder.company(getStoreName())
                    .orderName(name)
                    .storeName(getStoreName())
                    .supOrCus(mOrderInfo.getString("cs_xname"))
                    .operator(mOrderInfo.getString(getSaleOperatorNameKey()))
                    .orderNo(Utils.getNullStringAsEmpty(mOrderInfo,"refund_code"))
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
    protected String getOrderPrintName(){
        return getString(R.string.wholesale_order_sz);
    }
    protected OrderPrintContentBase getPrintContent(){
        return new WholesalePrintContent();
    }
}
