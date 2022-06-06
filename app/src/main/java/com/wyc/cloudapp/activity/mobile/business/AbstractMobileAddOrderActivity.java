package com.wyc.cloudapp.activity.mobile.business;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.zxing.client.android.CaptureActivity;
import com.wyc.cloudapp.bean.PriceType;
import com.wyc.cloudapp.customizationView.ItemPaddingLinearLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDataAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.BusinessOrderPrintSetting;
import com.wyc.cloudapp.bean.Supplier;
import com.wyc.cloudapp.constants.PriceTypeId;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.data.viewModel.OrderIdViewModel;
import com.wyc.cloudapp.data.viewModel.OrderViewModel;
import com.wyc.cloudapp.data.viewModel.SupplierViewModel;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.business.BusinessSelectGoodsDialog;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForJson;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;


public abstract class AbstractMobileAddOrderActivity extends AbstractDefinedTitleActivity {
    private AbstractBusinessOrderDetailsDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> mAdapter;
    private JSONArray mSupplierList;
    private RecyclerView mDetailsView;
    private CustomProgressDialog mProgressDialog;
    private TextView mSumNumTv,mSumAmtTv;
    private WeakReference<ScanCallback> mScanCallback;

    protected JSONObject mOrderInfo;
    protected TextView mSupplierTV,mSaleOperatorTv,mWarehouseTv,mOrderCodeTv,mDateTv,mRemarkEt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED);

        initView();

        queryData();
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
        if (mProgressDialog != null)mProgressDialog.dismiss();
    }

    @CallSuper
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK ){
            if (requestCode == MobileSelectGoodsActivity.SELECT_GOODS_CODE) {
                if (null != data){
                    final String barcode_id = data.getStringExtra("barcode_id");
                    Logger.d("barcode_id:%s",barcode_id);
                    final JSONObject object = new JSONObject();

                    if (BusinessSelectGoodsDialog.selectGoodsWithBarcodeId(object,barcode_id,getPriceTypeInfo())){
                        addGoodsDetails(object,false);
                    }else {
                        MyDialog.ToastMessage(object.getString("info"), getWindow());
                    }
                }
            }else if (requestCode == BusinessSelectGoodsDialog.BARCODE_REQUEST_CODE){
                if (mScanCallback != null){
                    final ScanCallback callback = mScanCallback.get();
                    if (callback != null){
                        final String _code = data.getStringExtra(CaptureActivity.CALLBACK_CODE);
                        callback.callback(_code);
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!isAudit() && !mAdapter.isEmpty() && mAdapter.isChange()){
            CustomApplication.runInMainThread(()->{
                if (MyDialog.showMessageToModalDialog(this,"已选择商品，是否退出？") == 1){
                    super.onBackPressed();
                }
            });
        }else
            super.onBackPressed();
    }

    private void addGoodsDetails(final JSONObject object, boolean modify){
        if (mAdapter != null){
            int index = mAdapter.isExist(object);
            if (index >= 0 && !modify){
                mDetailsView.scrollToPosition(index);
                MyDialog.displayAskMessage(this,getString(R.string.ask_exist_hints, MobileSelectGoodsActivity.getGoodsName(object)), myDialog -> {
                    mAdapter.addDetails(mAdapter.updateGoodsDetail(object),index,false);
                    myDialog.dismiss();
                }, MyDialog::dismiss);
            }else
                mAdapter.addDetails(mAdapter.updateGoodsDetail(object),index,modify);
        }
    }

    @Override
    public void setScanCallback(final ScanCallback callback){
        if (mScanCallback == null || callback != mScanCallback.get()){
            mScanCallback = new WeakReference<>(callback);
        }
    }

    protected abstract JSONObject generateQueryDetailCondition();
    protected abstract AbstractBusinessOrderDetailsDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter();
    protected abstract String generateOrderCodePrefix();
    protected abstract JSONObject generateAuditCondition();
    protected abstract String getOrderIDKey();

    @CallSuper
    protected JSONObject generateUploadCondition(){
        final JSONObject upload_obj = new JSONObject();
        upload_obj.put("pt_user_id",getPtUserId());

        if (null != mSupplierTV)
            upload_obj.put("gs_id",Utils.getViewTagValue(mSupplierTV,""));

        upload_obj.put(getSaleOperatorKey(),Utils.getViewTagValue(mSaleOperatorTv,""));
        upload_obj.put("remark",mRemarkEt.getText().toString());

        return upload_obj;
    }

    protected String getSaleOperatorKey(){
        return "cg_pt_user_id";
    }

    protected String getSaleOperatorNameKey(){
        return "cg_user_cname";
    }

    @CallSuper
    protected void initView(){
        initSupplier();
        initStores();
        initSaleOperator();
        initOrderCode();
        initDate();
        initRemark();
        initOrderDetailsList();
        initFooterView();
        initFunctionBtn();
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

    protected String getPrintContent(BusinessOrderPrintSetting setting){
        return "";
    }

    protected final boolean isAudit(){
        return Utils.getNotKeyAsNumberDefault(mOrderInfo,"sh_status",1) == 2;
    }

    protected boolean hasSource(){
        return false;
    }

    protected final String getOrderValidityDate(){
        final Calendar now = Calendar.getInstance();
        now.add(Calendar.MONTH,1);
        return new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA).format(now.getTime());
    }

    protected final JSONArray getOrderDetails(){
        if (mAdapter == null)return new JSONArray();
        JSONArray array = mAdapter.getData();
        if (array == null)array = new JSONArray();
        return array;
    }

    //用来源订单明细设置当前明细
    protected final void setOrderDetailsWithSourceOrder(final JSONArray array){
        if (null != mAdapter)mAdapter.setDataForArray(array);
    }

    protected void resetBusinessOrderInfo(){
        finish();
    }

    protected final boolean isDetailsEmpty(){
        return null != mAdapter && mAdapter.isEmpty();
    }

    @CallSuper
    protected void showOrder(){
        setOrderStatus();

        final JSONObject object = mOrderInfo;
        setView(mSupplierTV, Utils.getNullStringAsEmpty(object, "gs_id"), Utils.getNullStringAsEmpty(object, "gs_name"));
        setView(mSaleOperatorTv, Utils.getNullStringAsEmpty(object, getSaleOperatorKey()), Utils.getNullStringAsEmpty(object, getSaleOperatorNameKey()));
        setView(mDateTv, "", Utils.getNullStringAsEmpty(object, "add_datetime"));
        setView(mRemarkEt, "", Utils.getNullStringAsEmpty(object, "remark"));

        Logger.d_json(mOrderInfo.toString());

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

    private void generateOrderCode() {
         final MutableLiveData<String> liveData = new ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(OrderIdViewModel.class).init(generateOrderCodePrefix());
         if (!liveData.hasActiveObservers())liveData.observe(this, s -> mOrderCodeTv.setText(s));
    }

    private void setOrderStatus(){
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
        }else if (hasOrderId()){
            business_main.setCentreLabel(getString(R.string.saved_sz));
        }
    }

    private boolean hasOrderId(){
        return !"".equals(Utils.getNullStringAsEmpty(mOrderInfo,getOrderIDKey()));
    }

    private void getSupplier(){
        final  MutableLiveData<List<Supplier>> liveData = new ViewModelProvider(this).get(SupplierViewModel.class).getCurrentModel();
        if (!liveData.hasActiveObservers())liveData.observe(this, suppliers -> mSupplierList = parse_supplier_info_and_set_default(suppliers));
    }
    private JSONArray parse_supplier_info_and_set_default(final List<Supplier> suppliers){
        final JSONArray array  = new JSONArray();
        if (suppliers != null){
            JSONObject object;

            for (Supplier supplier : suppliers){
                final String id = supplier.getGs_id(),name = supplier.getGs_name();
                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put(TreeListBaseAdapter.COL_ID,id);
                object.put(TreeListBaseAdapter.COL_NAME,name);
                array.add(object);

                //
                if (!isShowOrder() && "0000".equals(supplier.getCs_code()) && mSupplierTV != null){
                    CustomApplication.runInMainThread(()->{
                        mSupplierTV.setText(name);
                        mSupplierTV.setTag(id);
                    });
                }
            }
        }
        return array;
    }

    private void initFunctionBtn(){
        final Button new_order_btn = findViewById(R.id.new_order_btn),business_save_btn = findViewById(R.id.m_business_save_btn),business_audit_btn = findViewById(R.id.m_business_audit_btn),
                business_scan_btn = findViewById(R.id.m_business_scan_btn),pick_goods_btn = findViewById(R.id.m_pick_goods_btn);

        new_order_btn.setOnClickListener(mFunctionClickListener);
        pick_goods_btn.setOnClickListener(mFunctionClickListener);
        business_scan_btn.setOnClickListener(mFunctionClickListener);
        business_save_btn.setOnClickListener(mFunctionClickListener);
        business_audit_btn.setOnClickListener(mFunctionClickListener);

    }
    private final View.OnClickListener mFunctionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final int id = v.getId();
            if (id == R.id.new_order_btn){
                if (!mAdapter.isEmpty()){
                    if (MyDialog.showMessageToModalDialog(v.getContext(),"已存在商品，是否重新开单?") == 1){
                        resetBusinessOrderInfo();
                    }
                }else {
                    generateOrderCode();
                }
            }else if (id == R.id.m_pick_goods_btn){
                AbstractMobileAddOrderActivity activity = AbstractMobileAddOrderActivity.this;
                final Intent intent = new Intent(activity, MobileSelectGoodsActivity.class);
                intent.putExtra(MobileSelectGoodsActivity.TITLE_KEY,getString(R.string.select_goods_label));
                intent.putExtra(MobileSelectGoodsActivity.IS_SEL_KEY,true);
                intent.putExtra(MobileSelectGoodsActivity.MODIFIABLE,false);
                intent.putExtra(MobileSelectGoodsActivity.PRICE_TYPE_KEY,getPriceTypeInfo());
                startActivityForResult(intent, MobileSelectGoodsActivity.SELECT_GOODS_CODE);
            }else if (id == R.id.m_business_scan_btn){
                final BusinessSelectGoodsDialog dialog = new BusinessSelectGoodsDialog(AbstractMobileAddOrderActivity.this);
                dialog.setContinueListener(object -> addGoodsDetails(object,false));
                if (dialog.exec() == 1){
                    addGoodsDetails(dialog.getContentObj(),false);
                }
            }else if (id == R.id.m_business_save_btn){
                if (!hasOrderId() && mAdapter.isEmpty()){
                    MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.goods_i_sz)), getWindow());
                }else
                    uploadOrderInfo();
            }else if (id == R.id.m_business_audit_btn){
                auditOrder();
            }
        }
    };

    public PriceType getPriceTypeInfo(){
        return CustomApplication.self().getPriceType();
    }

    private void uploadOrderInfo(){
        final JSONObject condition = generateUploadCondition();
        if (condition.isEmpty())return;

        showProgress(getString(R.string.upload_order_hints));
        CustomApplication.execute(()->{
            final JSONObject param_obj = condition.getJSONObject("upload_obj");
            param_obj.put("appid",getAppId());
            param_obj.put("stores_id",getStoreId());

            Logger.d_json(param_obj.toString());

            String sz_param = HttpRequest.generate_request_parma(param_obj,getAppSecret()),api = condition.getString("api"),err = "";
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + api,sz_param,true);
            if (HttpUtils.checkRequestSuccess(retJson)){
                final JSONObject info = JSON.parseObject(retJson.getString("info"));
                Logger.d_json(retJson.toString());
                if (HttpUtils.checkBusinessSuccess(info)){
                    mOrderInfo = new JSONObject();
                    mOrderInfo.put(getOrderIDKey(),info.getString(getOrderIDKey()));//新建单据 订单对象只能包含一个数据项并且key是getOrderIDKey的返回值；如果此条件改变了，请务必确保isNewOrder方法的判断条件一致
                    CustomApplication.runInMainThread(()->{
                        setOrderStatus();
                        if (MyDialog.showMessageToModalDialog(this,String.format(Locale.CHINA,"%s,%s",getString(R.string.upload_order_success_hints),getString(R.string.ask_audit_hints))) == 1){
                            auditOrder();
                        }else {
                            resetBusinessOrderInfo();
                            MyDialog.toastMessage(getString(R.string.upload_order_success_hints));
                        }
                    });
                }else {
                    err = info.getString("info");
                }
            }
            if (Utils.isNotEmpty(err)){
                MyDialog.toastMessage(getString(R.string.upload_business_order_hint_sz,err));
            }
            mProgressDialog.dismiss();
        });
    }
    protected boolean isNewOrder(){
        return null == mOrderInfo || (mOrderInfo.size() == 1 && mOrderInfo.containsKey(getOrderIDKey()));
    }

    private void showProgress(final String mess){
        if (mProgressDialog == null)
            mProgressDialog = CustomProgressDialog.showProgress(this,mess);
        else
            mProgressDialog.setMessage(mess).refreshMessage();

        if (!mProgressDialog.isShowing())mProgressDialog.show();
    }
    private void dismissProgress(){
        if (null != mProgressDialog)mProgressDialog.dismiss();
    }

    private void auditOrder(){
        final String orderId = Utils.getNullStringAsEmpty(mOrderInfo,getOrderIDKey());
        if (!Utils.isNotEmpty(orderId)){
            MyDialog.ToastMessage("请先保存单据!", getWindow());
            return;
        }
        showProgress(getString(R.string.auditing_hints));
        CustomApplication.execute(()->{
            final JSONObject condition = generateAuditCondition(),param_obj = new JSONObject();
            param_obj.put("appid",getAppId());
            param_obj.put("stores_id",getStoreId());
            param_obj.put("pt_user_id",getPtUserId());
            param_obj.put(getOrderIDKey(),orderId);

            Logger.d_json(param_obj.toString());

            String sz_param = HttpRequest.generate_request_parma(param_obj,getAppSecret()),api = condition.getString("api"),err = "";
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + api,sz_param,true);
            Logger.d_json(retJson.toString());
            if (HttpUtils.checkRequestSuccess(retJson)){
                final JSONObject info = JSON.parseObject(retJson.getString("info"));
                if (HttpUtils.checkBusinessSuccess(info)){
                    CustomApplication.runInMainThread(()->{
                        MyDialog.toastMessage(getString(R.string.audited_sz));
                        print(true);
                    });
                }else {
                    err = info.getString("info");
                }
            }
            if (Utils.isNotEmpty(err)){
                MyDialog.toastMessage("审核单据错误:" + err);
            }
            dismissProgress();
        });
    }

    private void initSupplier(){//初始化供应商
        final TextView business_supplier_tv = findViewById(R.id.m_business_supplier_tv);
        if (null != business_supplier_tv){
            final String sup = getString(R.string.supplier_colon_sz);
            business_supplier_tv.setOnClickListener(v -> CustomApplication.runInMainThread(()->{
                final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,sup.substring(0,sup.length() - 1));
                treeListDialog.setData(mSupplierList,null,true);
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    business_supplier_tv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                    business_supplier_tv.setTag(object.getString(TreeListBaseAdapter.COL_ID));
                }
            }));
            mSupplierTV = business_supplier_tv;
            getSupplier();
        }
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
        business_operator_tv.setOnClickListener(v -> CustomApplication.runInMainThread(()->{
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,sz.substring(0,sz.length() - 1));
            treeListDialog.setData(array,null,true);
            if (treeListDialog.exec() == 1){
                final JSONObject object = treeListDialog.getSingleContent();
                business_operator_tv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                business_operator_tv.setTag(object.getString(TreeListBaseAdapter.COL_ID));
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
                object.put(TreeListBaseAdapter.COL_ID,id);
                object.put(TreeListBaseAdapter.COL_NAME,name);
                data.add(object);

                if (i == 0){
                    tv.setTag(id);
                    tv.setText(name);
                }
            }
        }else {
            MyDialog.ToastMessage(err.toString(), null);
        }
        return data;
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

    private void initOrderDetailsList(){
        final RecyclerView details_list = findViewById(R.id.details_list);
        if (null != details_list){
            mAdapter = getAdapter();
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    final JSONArray array = mAdapter.getData();
                    double num = 0.0,price = 0.0,sum_num= 0.0,amt = 0.0;

                    int size = array.size();
                    for (int i = 0;i < size;i ++){
                        final JSONObject object = array.getJSONObject(i);
                        num = Utils.getNotKeyAsNumberDefault(object,mAdapter.getNumKey(),0.0);
                        price = Utils.getNotKeyAsNumberDefault(object,mAdapter.getPriceKey(),0.0);

                        sum_num += num;
                        amt += num * price;
                    }
                    mSumNumTv.setText(String.format(Locale.CHINA,"%.2f",sum_num));
                    mSumAmtTv.setText(String.format(Locale.CHINA,"%.2f",amt));
                    mDetailsView.scrollToPosition(size - 1);
                }
            });
            mAdapter.setItemListener(this::modifyGoodsDetails);
            details_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
            details_list.setAdapter(mAdapter);
            details_list.addItemDecoration(new LinearItemDecoration(getColor(R.color.white),3));

            mDetailsView = details_list;
        }
    }

    private void modifyGoodsDetails(@NonNull JSONObject object) {
        final BusinessSelectGoodsDialog dialog = new BusinessSelectGoodsDialog(this,hasSource(),object);
        dialog.setDelListener(v -> mAdapter.deleteDetails());
        if (dialog.exec() == 1){
            addGoodsDetails(dialog.getContentObj(),true);
        }
    }

    private void initFooterView(){
        mSumNumTv = findViewById(R.id.business_sum_num_tv);
        mSumAmtTv = findViewById(R.id.business_sum_amt_tv);
    }

    private void queryData(){
        if (isShowOrder()){
            query(getIntent().getStringExtra(AbstractBusinessOrderDataAdapter.KEY));
        }else {
            generateOrderCode();
        }
    }

    private void query(final String id){
        final JSONObject condition = generateQueryDetailCondition();
        if (null != condition){
            final JSONObject parameterObj = new JSONObject();
            parameterObj.put("appid",getAppId());
            parameterObj.put("stores_id",getStoreId());
            parameterObj.put("pt_user_id",getPtUserId());
            parameterObj.put(getOrderIDKey(),id);

            final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(this,getString(R.string.hints_query_data_sz)).setCancel(true);
            final MutableLiveData<JSONObject> liveData = new ViewModelProvider(this,ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()))
                    .get(OrderViewModel.class).getCurrentModel(getUrl() + condition.getString("api"),HttpRequest.generate_request_parma(parameterObj,getAppSecret()));
            if (!liveData.hasActiveObservers())
                liveData.observe(this, jsonObject -> {
                            mOrderInfo = jsonObject;
                            showOrder();
                            progressDialog.dismiss();
                        });
            progressDialog.setOnCancelListener(dialog -> {
                dialog.dismiss();
                finish();
            });
        }
    }
}
