package com.wyc.cloudapp.activity.mobile.business;

import androidx.annotation.CallSuper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.customizationView.ItemPaddingLinearLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDataAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileBaseOrderDetailsAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.BusinessOrderPrintSetting;
import com.wyc.cloudapp.bean.OrderPrintContentBase;
import com.wyc.cloudapp.bean.TransferOutInOrder;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MobileTransferInOrderDetailActivity extends AbstractDefinedTitleActivity {
    private static final String ORDER_ID_KEY = "ckd_id";

    private AbstractBusinessOrderDetailsDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> mAdapter;
    private RecyclerView mDetailsView;
    private CustomProgressDialog mProgressDialog;
    private TextView mSumNumTv,mSumAmtTv;

    protected JSONObject mOrderInfo;
    protected TextView mSaleOperatorTv,mWarehouseTv,mOrderCodeTv,mDateTv,mRemarkEt,mTransferOutWhTv;

    private String mOrderID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        CustomApplication.runInMainThread(this::queryData);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null && mProgressDialog.isShowing())mProgressDialog.dismiss();
    }

    @CallSuper
    protected void initView(){
        initStores();
        initSaleOperator();
        initOrderCode();
        initDate();
        initRemark();
        initOrderDetailsList();
        initFooterView();
        initFunctionBtn();

        mTransferOutWhTv = findViewById(R.id.transfer_out_wh_tv);
    }

    private void showPrint(){
        if (isAudit()){
            setRightText(getString(R.string.m_print_sz) + "    ");
            setRightListener(v -> print(false));
        }
    }

    private void print(boolean ask){
        final BusinessOrderPrintSetting setting = BusinessOrderPrintSetting.getSetting();
        if (ask){
            switch (setting.getType()){
                case ASK:
                    if (setting.getType() == BusinessOrderPrintSetting.Type.ASK){
                        MyDialog.displayAskMessage(this, getString(R.string.ask_print_hint), myDialog -> {
                            printContent(setting);
                            resetBusinessOrderInfo();
                        }, myDialog -> {
                            myDialog.dismiss();
                            resetBusinessOrderInfo();
                        });
                    }
                    break;
                case AUTO:
                    printContent(setting);
                    resetBusinessOrderInfo();
                    break;
                default:
                    resetBusinessOrderInfo();
            }
        }else {
            printContent(setting);
        }
    }
    private void printContent(BusinessOrderPrintSetting setting){
        Logger.d(setting);
        if (Utils.isNotEmpty(setting.getPrinter())){
            if (setting.getWay() == BusinessOrderPrintSetting.Way.BLUETOOTH_PRINT){
                Printer.printByBluetooth(getPrintContent(setting),setting.getPrinterAddress());
            }
        }else {
            MyDialog.toastMessage(getString(R.string.printer_empty_hint));
        }
    }

    protected final boolean isAudit(){
        return Utils.getNotKeyAsNumberDefault(mOrderInfo,"qr_status",1) == 2;
    }


    protected void resetBusinessOrderInfo(){
        finish();
    }

    @CallSuper
    protected void showOrder(){
        setOrderStatus();
        Logger.d_json(mOrderInfo.toString());

        final JSONObject object = mOrderInfo;
        setView(mOrderCodeTv,Utils.getNullStringAsEmpty(object,"ckd_id"), Utils.getNullStringAsEmpty(object,"ckd_code"));
        setView(mSaleOperatorTv, Utils.getNullStringAsEmpty(object,"js_pt_user_id"), Utils.getNullStringAsEmpty(object,"js_pt_user_name"));
        setView(mDateTv, "", FormatDateTimeUtils.formatTimeWithTimestamp(object.getLongValue("addtime") * 1000));
        setView(mRemarkEt, "", Utils.getNullStringAsEmpty(object, "remark"));
        setView(mTransferOutWhTv,Utils.getNullStringAsEmpty(object,"wh_id"), Utils.getNullStringAsEmpty(object,"wh_name"));

        mAdapter.setDataForArray(Utils.getNullObjectAsEmptyJsonArray(mOrderInfo,"goods_list"));
    }
    protected void setView(final TextView view, final String id, final String name){
        if (view != null){
            view.setTag(id);
            view.setText(name);
        }
    }

    protected boolean isShowOrder(){
        final Intent intent = getIntent();
        return null != intent && intent.hasExtra(AbstractBusinessOrderDataAdapter.KEY);
    }

    private void setOrderStatus(){
        mOrderID = Utils.getNullStringAsEmpty(mOrderInfo,ORDER_ID_KEY);
        final ItemPaddingLinearLayout business_main = findViewById(R.id.business_add_main_layout);
        if (isAudit()){
            business_main.setDisableEvent(true);
            final ItemPaddingLinearLayout business_function_btn_layout = business_main.findViewById(R.id.business_function_btn_layout);
            for (int i = 0,size = business_function_btn_layout.getChildCount(); i < size ;i ++){
                final View view = business_function_btn_layout.getChildAt(i);
                if (view.getId() == R.id.audit_status_tv){
                    view.setVisibility(View.VISIBLE);
                }else {
                    view.setVisibility(View.GONE);
                }
            }
            showPrint();
        }else if (Utils.isNotEmpty(mOrderID)){
            business_main.setCentreLabel(getString(R.string.saved_sz));
        }
    }

    private void initFunctionBtn(){
        final Button confirm_order_btn = findViewById(R.id.confirm_order_btn);
        confirm_order_btn.setOnClickListener(v -> auditOrder());
    }

    private void showProgress(final String mess){
        if (mProgressDialog == null)
            mProgressDialog = CustomProgressDialog.showProgress(this,mess);
        else
            mProgressDialog.setMessage(mess).refreshMessage();

        if (!mProgressDialog.isShowing())mProgressDialog.show();
    }

    private void auditOrder(){
        showProgress(getString(R.string.confirming_hints));
        CustomApplication.execute(()->{
            final JSONObject param_obj = new JSONObject();
            param_obj.put("appid",getAppId());
            param_obj.put("stores_id",getStoreId());
            param_obj.put("pt_user_id",getPtUserId());
            param_obj.put(ORDER_ID_KEY,mOrderID);

            Logger.d_json(param_obj.toString());

            String sz_param = HttpRequest.generate_request_parma(param_obj,getAppSecret()),err = "";
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/api_move_in/qr_examine",sz_param,true);
            Logger.d_json(retJson.toString());
            if (HttpUtils.checkRequestSuccess(retJson)){
                final JSONObject info = JSON.parseObject(retJson.getString("info"));
                if (HttpUtils.checkBusinessSuccess(info)){
                    CustomApplication.runInMainThread(()->{
                        resetBusinessOrderInfo();
                        MyDialog.toastMessage(getString(R.string.confirmed_receipt));
                    });
                }else {
                    err = info.getString("info");
                }
            }
            if (Utils.isNotEmpty(err)){
                MyDialog.toastMessage("确认收货错误:" + err);
            }
            mProgressDialog.dismiss();
        });
    }

    private void initStores(){
        final TextView business_warehouse_tv = findViewById(R.id.m_business_warehouse_tv);
        business_warehouse_tv.setTag(getStoreId());
        business_warehouse_tv.setText(getStoreName());
        mWarehouseTv = business_warehouse_tv;
    }

    private void initSaleOperator(){
        mSaleOperatorTv = findViewById(R.id.m_business_operator_tv);
    }

    private void initOrderCode(){
        mOrderCodeTv = findViewById(R.id.m_business_order_tv);
    }

    private void initDate(){
        final LinearLayout view = findViewById(R.id.two);
        if (!isShowOrder()){
            view.setVisibility(View.GONE);
        }else
            mDateTv = view.findViewById(R.id.m_business_date_tv);
    }

    private void initRemark(){
        mRemarkEt = findViewById(R.id.m_business_remark_et);
    }

    private void initOrderDetailsList() {
        final RecyclerView details_list = findViewById(R.id.details_list);
        if (null != details_list) {
            mAdapter = new MobileBaseOrderDetailsAdapter(this);
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    final JSONArray array = mAdapter.getData();
                    double num = 0.0, price = 0.0, sum_num = 0.0, amt = 0.0;

                    int size = array.size();
                    for (int i = 0; i < size; i++) {
                        final JSONObject object = array.getJSONObject(i);
                        num = Utils.getNotKeyAsNumberDefault(object, mAdapter.getNumKey(), 0.0);
                        price = Utils.getNotKeyAsNumberDefault(object, mAdapter.getPriceKey(), 0.0);

                        sum_num += num;
                        amt += num * price;
                    }
                    mSumNumTv.setText(String.format(Locale.CHINA, "%.2f", sum_num));
                    mSumAmtTv.setText(String.format(Locale.CHINA, "%.2f", amt));
                    mDetailsView.scrollToPosition(size - 1);
                }
            });
            details_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            details_list.setAdapter(mAdapter);
            details_list.addItemDecoration(new LinearItemDecoration(getColor(R.color.white), 3));

            mDetailsView = details_list;
        }
    }

    private void initFooterView(){
        mSumNumTv = findViewById(R.id.business_sum_num_tv);
        mSumAmtTv = findViewById(R.id.business_sum_amt_tv);
    }

    private void queryData(){
        if (isShowOrder()){
            query(getIntent().getStringExtra(AbstractBusinessOrderDataAdapter.KEY));
        }
    }

    private void query(final String id){
        final JSONObject parameterObj = new JSONObject();
        parameterObj.put("appid",getAppId());
        parameterObj.put("stores_id",getStoreId());
        parameterObj.put("pt_user_id",getPtUserId());
        parameterObj.put(ORDER_ID_KEY,id);

        final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(this,getString(R.string.hints_query_data_sz));
        CustomApplication.execute(()->{
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/api_move_in/xinfo", HttpRequest.generate_request_parma(parameterObj,getAppSecret()),true);
            if (HttpUtils.checkRequestSuccess(retJson)){
                try {
                    final JSONObject info = JSONObject.parseObject(retJson.getString("info"));
                    if (HttpUtils.checkBusinessSuccess(info)){
                        mOrderInfo = info.getJSONObject("data");
                        runOnUiThread(this::showOrder);
                    }else throw new JSONException(info.getString("info"));
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.toastMessage(e.getMessage());
                }
            }
            progressDialog.dismiss();
        });
    }

    private boolean isNewOrder() {
        return mOrderInfo == null || mOrderInfo.isEmpty();
    }

    protected String getPrintContent(BusinessOrderPrintSetting setting) {
        final TransferOutInOrder.Builder Builder = new TransferOutInOrder.Builder(new TransferOutInOrder());
        final List<TransferOutInOrder.Goods> details = new ArrayList<>();
        final String name = getString(R.string.distribution_warehousing_sz);
        JSONArray goods_list;
        if (isNewOrder()){
            goods_list = mAdapter.getData();
            Builder.company(getStoreName())
                    .orderName(name)
                    .storeName(mWarehouseTv.getText().toString())
                    .outStoreName(mTransferOutWhTv.getText().toString())
                    .operator(mSaleOperatorTv.getText().toString())
                    .orderNo(mOrderCodeTv.getText().toString())
                    .operateDate(FormatDateTimeUtils.formatCurrentTime(FormatDateTimeUtils.YYYY_MM_DD_1))
                    .remark(mRemarkEt.getText().toString());
        }else {
            goods_list = mOrderInfo.getJSONArray("goods_list");
            Builder.company(getStoreName())
                    .orderName(name)
                    .storeName(mOrderInfo.getString("dr_wh_name"))
                    .outStoreName(mOrderInfo.getString("wh_name"))
                    .operator(mOrderInfo.getString("js_pt_user_name"))
                    .orderNo(mOrderInfo.getString("ckd_code"))
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

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_transfer_in_order_detail;
    }
}