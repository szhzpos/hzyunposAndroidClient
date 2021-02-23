package com.wyc.cloudapp.activity.mobile.business;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.CustomizationView.InterceptLinearLayout;
import com.wyc.cloudapp.CustomizationView.ItemPaddingLinearLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.report.AbstractDataAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.TreeListDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.util.Calendar;
import java.util.Locale;

public abstract class AbstractMobileAddOrderActivity extends AbstractMobileActivity {
    private AbstractDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> mAdapter;
    private JSONArray mSupplierList;

    protected JSONObject mOrderInfo;
    protected TextView mSupplierTV,mSaleOperatorTv,mWarehouseTv,mOrderCodeTv,mDateTv,mRemarkEt,mSumNumTv,mSumAmtTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);

        initTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getSupplier();
        CustomApplication.self().getAppHandler().post(this::queryData);
    }

    protected abstract JSONObject generateQueryCondition();
    protected abstract AbstractDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter();
    protected abstract String generateOrderCodePrefix();

    @CallSuper
    protected void showOrder(){
        setOrderStatus();

        mAdapter.setDataForArray(Utils.getNullObjectAsEmptyJsonArray(mOrderInfo,"goods_list"));
        mAdapter.notifyDataSetChanged();
    }
    protected void setView(@NonNull final TextView view, final String id, final String name){
        view.setTag(id);
        view.setText(name);
    }

    private void generateOrderCode() {
        CustomApplication.execute(()->{
            final JSONObject parameterObj = new JSONObject();
            parameterObj.put("appid",getAppId());
            parameterObj.put("prefix",generateOrderCodePrefix());
            final String sz_param = HttpRequest.generate_request_parm(parameterObj,getAppSecret());
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/codes/mk_code",sz_param,true);
            switch (retJson.getIntValue("flag")){
                case 0:
                    runOnUiThread(()-> MyDialog.ToastMessage("查询订单编号错误:" + retJson.getString("info"),this,null));
                    break;
                case 1:
                    try {
                        final JSONObject info = JSON.parseObject(retJson.getString("info"));
                        if ("y".equals(info.getString("status"))){
                            mOrderCodeTv.post(()-> mOrderCodeTv.setText(info.getString("code")));
                        }else {
                            runOnUiThread(()-> Toast.makeText(this,info.getString("info"),Toast.LENGTH_LONG));
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                        runOnUiThread(()-> Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_LONG));
                    }
                    break;
            }
        });
    }

    private void setOrderStatus(){
        final ItemPaddingLinearLayout business_function_btn_layout = findViewById(R.id.business_function_btn_layout);
        if (Utils.getNotKeyAsNumberDefault(mOrderInfo,"sh_status",1) == 2){

            final ItemPaddingLinearLayout business_main = findViewById(R.id.business_add_main_layout);
            business_main.setIgnore(true);

            for (int i = 0,size = business_function_btn_layout.getChildCount(); i < size ;i ++){
                final View view = business_function_btn_layout.getChildAt(i);
                if (view.getId() == R.id.audit_status_tv){
                    view.setVisibility(View.VISIBLE);
                }else {
                    view.setVisibility(View.GONE);
                }
            }
        }
    }

    private void getSupplier(){
        CustomApplication.execute(()->{
            final HttpRequest httpRequest = new HttpRequest();
            final JSONObject object = new JSONObject();
            object.put("appid",getAppId());
            object.put("stores_id",getStoreId());
            final String sz_param = HttpRequest.generate_request_parm(object,getAppSecret());
            final JSONObject retJson = httpRequest.sendPost(this.getUrl() + "/api/supplier_search/xlist",sz_param,true);
            switch (retJson.getIntValue("flag")){
                case 0:
                    runOnUiThread(()->{
                        MyDialog.ToastMessage("查询供应商信息错误:" + retJson.getString("info"),this,null);
                    });
                    break;
                case 1:
                    final JSONObject info_obj = JSONObject.parseObject(retJson.getString("info"));
                    if ("n".equals(Utils.getNullOrEmptyStringAsDefault(info_obj,"status","n"))){
                        runOnUiThread(()-> MyDialog.ToastMessage("查询供应商信息错误:" + info_obj.getString("info"),this,null));
                    }else{
                        final JSONArray data = info_obj.getJSONArray("data");
                        mSupplierList = parse_supplier_info_and_set_default(data);
                    }
                    break;
            }
        });
    }
    private JSONArray parse_supplier_info_and_set_default(final JSONArray suppliers){
        final JSONArray array  = new JSONArray();
        if (suppliers != null){
            JSONObject object;
            for (int i = 0,size = suppliers.size();i < size;i++){
                final JSONObject tmp = suppliers.getJSONObject(i);
                final String id = Utils.getNullOrEmptyStringAsDefault(tmp,"gs_id",""),name = Utils.getNullStringAsEmpty(tmp,"gs_name");
                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put("item_id",id);
                object.put("item_name",name);
                array.add(object);

                //
                if ("0000".equals(Utils.getNullStringAsEmpty(tmp,"gs_code")) && mSupplierTV != null){
                    mSupplierTV.post(()->{
                        mSupplierTV.setText(name);
                        mSupplierTV.setTag(id);
                    });
                }
            }
        }
        return array;
    }

    protected void initView(){
        initSupplier();
        initStores();
        initSaleOperator();
        initOrderCode();
        initDate();
        initRemark();
        initOrderDetailsList();
        initFooterView();
    }
    private void initSupplier(){//初始化供应商
        final TextView business_supplier_tv = findViewById(R.id.m_business_supplier_tv);
        final String sup = getString(R.string.supplier_colon_sz);
        business_supplier_tv.setOnClickListener(v -> v.post(()->{
            final TreeListDialog treeListDialog = new TreeListDialog(this,sup.substring(0,sup.length() - 1));
            treeListDialog.setDatas(mSupplierList,null,true);
            if (treeListDialog.exec() == 1){
                final JSONObject object = treeListDialog.getSingleContent();
                business_supplier_tv.setText(object.getString("item_name"));
                business_supplier_tv.setTag(object.getIntValue("item_id"));
            }
        }));

        mSupplierTV = business_supplier_tv;
    }

    private void initStores(){
        final TextView business_warehouse_tv = findViewById(R.id.m_business_warehouse_tv);
        business_warehouse_tv.setTag(getStoreId());
        business_warehouse_tv.setText(getStoreName());
        mWarehouseTv = business_warehouse_tv;
    }

    private void initSaleOperator(){
        final TextView business_operator_tv = findViewById(R.id.m_business_operator_tv);
        final String sz = getString(R.string.sale_operator_sz);
        final JSONArray array = getSaleOperator(business_operator_tv);
        business_operator_tv.setOnClickListener(v -> v.post(()->{
            final TreeListDialog treeListDialog = new TreeListDialog(this,sz.substring(0,sz.length() - 1));
            treeListDialog.setDatas(array,null,true);
            if (treeListDialog.exec() == 1){
                final JSONObject object = treeListDialog.getSingleContent();
                business_operator_tv.setText(object.getString("item_name"));
                business_operator_tv.setTag(object.getIntValue("item_id"));
            }
        }));

        mSaleOperatorTv = business_operator_tv;
    }
    private JSONArray getSaleOperator(final TextView tv){
        final StringBuilder err = new StringBuilder();
        final JSONArray array = SQLiteHelper.getListToJson("SELECT sales_id,name FROM sale_operator_info;",err),data = new JSONArray();

        if (null != array){
            JSONObject object;
            for (int i = 0,size = array.size();i < size;i++){
                final JSONObject tmp = array.getJSONObject(i);
                final String id = Utils.getNullStringAsEmpty(tmp,"sales_id"),name = Utils.getNullStringAsEmpty(tmp,"name");

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

    private void initOrderCode(){
        mOrderCodeTv = findViewById(R.id.m_business_order_tv);

    }

    private void initDate(){
        final TextView business_date_tv = findViewById(R.id.m_business_date_tv);
        business_date_tv.setOnClickListener(v -> Utils.showDatePickerDialog(this,(TextView) v, Calendar.getInstance()));
        mDateTv = business_date_tv;
    }

    private void initRemark(){
        mRemarkEt = findViewById(R.id.m_business_remark_et);
    }

    private void initOrderDetailsList(){
        final RecyclerView details_list = findViewById(R.id.details_list);
        if (null != details_list){
            mAdapter = getAdapter();
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    final JSONArray array = mAdapter.getData();
                    double num = 0.0,amt = 0.0;
                    for (int i = 0,size = array.size();i < size;i ++){
                        final JSONObject object = array.getJSONObject(i);
                        num += Utils.getNotKeyAsNumberDefault(object,"xnum",0.0);
                    }
                    mSumNumTv.setText(String.format(Locale.CHINA,"%.2f",num));
                    mSumAmtTv.setText(String.format(Locale.CHINA,"%.2f",Utils.getNotKeyAsNumberDefault(mOrderInfo,"total",0.0)));
                }
            });
            details_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
            details_list.setAdapter(mAdapter);
            details_list.addItemDecoration(new LinearItemDecoration(this.getColor(R.color.white)));
        }
    }

    private void initFooterView(){
        mSumNumTv = findViewById(R.id.business_sum_num_tv);
        mSumAmtTv = findViewById(R.id.business_sum_amt_tv);
    }

    private void initTitle(){
        final Intent intent = getIntent();
        if (intent != null)setMiddleText(intent.getStringExtra("title"));
    }

    private void queryData(){
        final Intent intent = getIntent();
        if (intent != null){
            final String order_id = intent.getStringExtra("order_id");
            if (Utils.isNotEmpty(order_id)){
                query(order_id);
            }else {
                generateOrderCode();
            }
        }
    }

    private void query(final String id){
        final JSONObject condition = generateQueryCondition();
        if (null != condition){
            final JSONObject parameterObj = new JSONObject();
            parameterObj.put("appid",getAppId());
            parameterObj.put("stores_id",getStoreId());
            parameterObj.put("pt_user_id",getPtUserId());
            parameterObj.put("cgd_id",id);

            final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(this,getString(R.string.hints_query_data_sz));
            CustomApplication.execute(()->{
                final JSONObject retJson = HttpUtils.sendPost(getUrl() + condition.getString("api"), HttpRequest.generate_request_parm(parameterObj,getAppSecret()),true);
                if (retJson.getIntValue("flag") == 1){
                    try {
                        final JSONObject info = JSON.parseObject(retJson.getString("info"));
                        mOrderInfo = info.getJSONObject("data");
                        Logger.d_json(mOrderInfo.toString());
                        runOnUiThread(this::showOrder);
                    }catch (JSONException e){
                        e.printStackTrace();
                        runOnUiThread(()-> MyDialog.ToastMessage(e.getMessage(),this,null));
                    }
                }else {
                    runOnUiThread(()-> MyDialog.ToastMessage(retJson.getString("info"),this,null));
                }
                progressDialog.dismiss();
            });
        }
    }
}
