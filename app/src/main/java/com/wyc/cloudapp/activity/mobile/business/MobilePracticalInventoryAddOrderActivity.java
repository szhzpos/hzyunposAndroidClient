package com.wyc.cloudapp.activity.mobile.business;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.CustomizationView.ItemPaddingLinearLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDetailsDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileInventoryOrderDetailsAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.constants.WholesalePriceType;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.TreeListDialog;
import com.wyc.cloudapp.dialog.baseDialog.BusinessSelectGoodsDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.lang.ref.WeakReference;
import java.util.Locale;
/*新增实盘数据录入单*/
public class MobilePracticalInventoryAddOrderActivity extends AbstractMobileActivity {
    private AbstractBusinessOrderDetailsDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> mAdapter;
    private RecyclerView mDetailsView;
    private TextView mSaleOperatorTv;
    private TextView mOrderCodeTv;
    private TextView mDateTv;
    private TextView mRemarkEt;
    private TextView mSumNumTv;
    private TextView mInventoryTaskTv,mInventoryWayTv;
    private CustomProgressDialog mProgressDialog;
    private WeakReference<ScanCallback> mScanCallback;
    private String mTaskCategory;
    private JSONObject mOrderInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initTitle();

        initStores();
        initSaleOperator();
        initOrderCode();
        initDate();
        initRemark();
        initOrderDetailsList();
        initFooterView();
        initFunctionBtn();
        initInventoryTask();

        //查询单据号或者根据单号查询订单信息
        runOnUiThread(this::queryData);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null && mProgressDialog.isShowing())mProgressDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK ){
            if (requestCode == MobileSelectGoodsActivity.SELECT_GOODS_CODE) {
                if (null != data){
                    final String barcode_id = data.getStringExtra("barcode_id");
                    Logger.d("barcode_id:%s",barcode_id);
                    final JSONObject object = new JSONObject();
                    if (BusinessSelectGoodsDialog.selectGoodsWithBarcodeId(object,barcode_id,WholesalePriceType.BUYING_PRICE)){
                        addGoodsDetails(object,false);
                    }else {
                        MyDialog.ToastMessage(object.getString("info"),this,getWindow());
                    }
                }
            }else if (requestCode == BusinessSelectGoodsDialog.BARCODE_REQUEST_CODE){
                if (mScanCallback != null){
                    final ScanCallback callback = mScanCallback.get();
                    if (callback != null){
                        final String _code = data.getStringExtra("auth_code");
                        callback.callback(_code);
                    }
                }
            }
        }
    }

    @Override
    public void setScanCallback(final ScanCallback callback){
        if (mScanCallback == null || callback != mScanCallback.get()){
            mScanCallback = new WeakReference<>(callback);
        }
    }

    private void initInventoryTask(){
        mInventoryTaskTv = findViewById(R.id.inventory_task_tv);
        mInventoryWayTv = findViewById(R.id.inventory_way_tv);
    }


    private void initStores(){
        TextView warehouseTv = findViewById(R.id.m_business_warehouse_tv);
        warehouseTv.setTag(getStoreId());
        warehouseTv.setText(getStoreName());
    }

    private void initSaleOperator(){
        final TextView business_operator_tv = findViewById(R.id.m_business_operator_tv);
        final String sz = getString(R.string.sale_operator_sz);
        final JSONArray array = getSaleOperator(business_operator_tv);
        business_operator_tv.setOnClickListener(v -> CustomApplication.runInMainThread(()->{
            final TreeListDialog treeListDialog = new TreeListDialog(this,sz.substring(0,sz.length() - 1));
            treeListDialog.setDatas(array,null,true);
            if (treeListDialog.exec() == 1){
                final JSONObject object = treeListDialog.getSingleContent();
                setSaleOperator(object.getString("item_id"),object.getString("item_name"));
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
    private void setSaleOperator(final String id,final String name){
        mSaleOperatorTv.setText(name);
        mSaleOperatorTv.setTag(id);
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
    private boolean isShowOrder(){
        final Intent intent = getIntent();
        return null != intent && intent.hasExtra("order_id");
    }
    private void initRemark(){
        mRemarkEt = findViewById(R.id.m_business_remark_et);
    }

    private void initOrderDetailsList(){
        final RecyclerView details_list = findViewById(R.id.details_list);
        if (null != details_list){
            mAdapter = new MobileInventoryOrderDetailsAdapter(this);
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    final JSONArray array = mAdapter.getData();
                    double num = 0.0,sum_num= 0.0;

                    int size = array.size();
                    for (int i = 0;i < size;i ++){
                        final JSONObject object = array.getJSONObject(i);
                        num = Utils.getNotKeyAsNumberDefault(object,mAdapter.getNumKey(),0.0);
                        sum_num += num;
                    }
                    mSumNumTv.setText(String.format(Locale.CHINA,"%.2f",sum_num));
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
        final JSONObject goods = Utils.JsondeepCopy(object);
        goods.put("xnum",object.getDoubleValue("app_xnum"));
        final BusinessSelectGoodsDialog dialog = new SelectGoodsDialog(this,false,goods);
        dialog.setDelListener(v -> mAdapter.deleteDetails());
        if (dialog.exec() == 1){
            addGoodsDetails(dialog.getContentObj(),true);
        }
    }

    private void addGoodsDetails(final JSONObject object, boolean modify){
        if (mAdapter != null){
            int index = mAdapter.isExist(object);
            if (index >= 0 && !modify){
                mDetailsView.scrollToPosition(index);
                MyDialog.displayAskMessage(this, getString(R.string.ask_exist_hints, MobileSelectGoodsActivity.getGoodsName(object)), myDialog -> {
                    mAdapter.addDetails(mAdapter.updateGoodsDetail(object),index,false);
                    myDialog.dismiss();
                }, MyDialog::dismiss);
            }else
                mAdapter.addDetails(mAdapter.updateGoodsDetail(object),index,modify);
        }
    }

    private void initFooterView(){
        mSumNumTv = findViewById(R.id.business_sum_num_tv);
    }

    private void initFunctionBtn(){
        final Button new_order_btn = findViewById(R.id.new_order_btn),business_save_btn = findViewById(R.id.m_business_save_btn),
                business_scan_btn = findViewById(R.id.m_business_scan_btn),pick_goods_btn = findViewById(R.id.m_pick_goods_btn);

        new_order_btn.setOnClickListener(mFunctionClickListener);
        pick_goods_btn.setOnClickListener(mFunctionClickListener);
        business_scan_btn.setOnClickListener(mFunctionClickListener);
        business_save_btn.setOnClickListener(mFunctionClickListener);
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
                final Intent intent = new Intent(MobilePracticalInventoryAddOrderActivity.this, MobileSelectGoodsActivity.class);
                intent.putExtra(MobileSelectGoodsActivity.TITLE_KEY,getString(R.string.select_goods_label));
                intent.putExtra(MobileSelectGoodsActivity.IS_SEL_KEY,true);
                intent.putExtra(MobileSelectGoodsActivity.TASK_CATEGORY_KEY,mTaskCategory);
                startActivityForResult(intent, MobileSelectGoodsActivity.SELECT_GOODS_CODE);
            }else if (id == R.id.m_business_scan_btn){
                final BusinessSelectGoodsDialog dialog = new SelectGoodsDialog(MobilePracticalInventoryAddOrderActivity.this);
                dialog.setGoodsCategory(mTaskCategory);
                if (dialog.exec() == 1){
                    addGoodsDetails(dialog.getContentObj(),false);
                }
            }else if (id == R.id.m_business_save_btn){
                if (mAdapter.isEmpty()){
                    MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz,getString(R.string.goods_i_sz)),v.getContext(),getWindow());
                }else
                    uploadOrderInfo();
            }
        }
    };

    private static class SelectGoodsDialog extends BusinessSelectGoodsDialog{

        public SelectGoodsDialog(@NonNull MainActivity context) {
            super(context);
        }

        public SelectGoodsDialog(@NonNull MainActivity context, boolean source, JSONObject object) {
            super(context, source, object);
        }

        @Override
        protected int getContentLayoutId() {
            return R.layout.inventory_select_goods_dialog_layout;
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {
        }
    }

    private void generateOrderCode() {
        CustomApplication.execute(()->{
            final JSONObject parameterObj = new JSONObject();
            parameterObj.put("appid",getAppId());
            parameterObj.put("prefix","PCT");
            final String sz_param = HttpRequest.generate_request_parm(parameterObj,getAppSecret());
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/codes/mk_code",sz_param,true);

            if (HttpUtils.checkRequestSuccess(retJson)){
                try {
                    final JSONObject info = JSON.parseObject(retJson.getString("info"));
                    if (HttpUtils.checkBusinessSuccess(info)){
                        CustomApplication.runInMainThread(()-> mOrderCodeTv.setText(info.getString("code")));
                    }else {
                        runOnUiThread(()-> Toast.makeText(this,info.getString("info"),Toast.LENGTH_LONG));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    runOnUiThread(()-> Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_LONG));
                }
            }else {
                runOnUiThread(()-> MyDialog.ToastMessage(getString(R.string.query_business_order_id_hint_sz,retJson.getString("info")),this,null));
            }
        });
    }

    private void uploadOrderInfo(){
        showProgress(getString(R.string.upload_order_hints));
        CustomApplication.execute(()->{
            final JSONObject param_obj = new JSONObject();
            param_obj.put("appid",getAppId());
            param_obj.put("pcd_task_id",Utils.getViewTagValue(mInventoryTaskTv,""));

            final String pcd_id = Utils.getNullStringAsEmpty(mOrderInfo,"pcd_id");
            if (Utils.isNotEmpty(pcd_id))param_obj.put("pcd_id",pcd_id);
            param_obj.put("pcd_code",mOrderCodeTv.getText().toString());

            param_obj.put("pt_user_id",getPtUserId());
            param_obj.put("js_pt_user_id",Utils.getViewTagValue(mSaleOperatorTv,""));
            param_obj.put("remark",mRemarkEt.getText().toString());

            param_obj.put("goods",getGoodsList());

            Logger.d_json(param_obj.toString());

            String sz_param = HttpRequest.generate_request_parm(param_obj,getAppSecret()),err = "";
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/inventory/create_task_spd",sz_param,true);
            if (HttpUtils.checkRequestSuccess(retJson)){
                final JSONObject info = JSON.parseObject(retJson.getString("info"));
                Logger.d_json(retJson.toString());
                if (HttpUtils.checkBusinessSuccess(info)){
                    CustomApplication.runInMainThread(()->{
                        resetBusinessOrderInfo();
                        Toast.makeText(this,getString(R.string.upload_order_success_hints),Toast.LENGTH_LONG).show();
                    });
                }else {
                    err = info.getString("info");
                }
            }
            if (Utils.isNotEmpty(err)){
                MyDialog.ToastMessageInMainThread(getString(R.string.upload_business_order_hint_sz,err));
            }
            mProgressDialog.dismiss();
        });
    }

    private JSONArray getGoodsList(){
        final JSONArray array = mAdapter.getData(),data = new JSONArray();
        for (int i = 0,size = array.size();i < size;i ++){
            final JSONObject object = new JSONObject(),old_obj = array.getJSONObject(i);

            object.put("app_xnum",old_obj.getDoubleValue("app_xnum"));
            object.put("barcode_id",old_obj.getString("barcode_id"));
            object.put("goods_id",old_obj.getString("goods_id"));
            object.put("unit_id",old_obj.getString("unit_id"));
            object.put("conversion",old_obj.getString("conversion"));

            data.add(object);
        }

        return data;
    }

    private void resetBusinessOrderInfo(){
        finish();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_inventory_add_order_acitity;
    }

    private void initTitle(){
        final Intent intent = getIntent();
        if (intent != null)setMiddleText(intent.getStringExtra("title"));
    }
    private void showProgress(final String mess){
        if (mProgressDialog == null)
            mProgressDialog = CustomProgressDialog.showProgress(this,mess);
        else
            mProgressDialog.setMessage(mess).refreshMessage();

        if (!mProgressDialog.isShowing())mProgressDialog.show();
    }

    private void queryData(){
        if (isShowOrder()){
            query(getIntent().getStringExtra("order_id"));
        }else {
            queryInventoryTask();
            generateOrderCode();
        }
    }
    private void queryInventoryTask(){
        final JSONObject parameterObj = new JSONObject();
        parameterObj.put("appid",getAppId());
        parameterObj.put("wh_id",getWhId());
        parameterObj.put("pt_user_id",getPtUserId());

        showProgress(getString(R.string.hints_query_data_sz));
        CustomApplication.execute(()->{
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/inventory/task_order_list", HttpRequest.generate_request_parm(parameterObj,getAppSecret()),true);
            if (HttpUtils.checkRequestSuccess(retJson)){
                try {
                    final JSONObject info = JSONObject.parseObject(retJson.getString("info"));
                    if (HttpUtils.checkBusinessSuccess(info)){
                        final JSONArray data = Utils.getNullObjectAsEmptyJsonArray(info,"data");
                        if (data.isEmpty()){
                            CustomApplication.runInMainThread(()-> Toast.makeText(MobilePracticalInventoryAddOrderActivity.this,"当前没有盘点任务...",Toast.LENGTH_LONG).show());
                        }else {
                            CustomApplication.runInMainThread(()->setInventoryTask(data.getJSONObject(0)));
                        }
                    }else throw new JSONException(info.getString("info"));
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.ToastMessageInMainThread(e.getMessage());
                }
            }
            mProgressDialog.dismiss();
        });
    }
    private void setInventoryTask(final JSONObject task){
        mInventoryTaskTv.setText(task.getString("task_name"));
        mInventoryTaskTv.setTag(task.getString("pcd_task_id"));

        final String task_mode = task.getString("task_mode");
        mInventoryWayTv.setText(getInventoryModeName(task_mode));
        mInventoryWayTv.setTag(task_mode);

        mTaskCategory = task.getString("task_category");
    }
    public static String getInventoryModeName(final String id){
        final JSONArray array = getInventoryWay();
        for (int i = 0,size = array.size();i < size;i ++){
            final JSONObject object = array.getJSONObject(i);
            if (object.getString("item_id").equals(id)){
                return object.getString("item_name");
            }
        }
        return CustomApplication.self().getString(R.string.other_sz);
    }
    public static JSONArray getInventoryWay(){
        return JSONArray.parseArray("[{\"item_id\":1,\"item_name\":\"全部盘点\"},{\"item_id\":2,\"item_name\":\"部分盘点\"}]");
    }

    private void query(final String id){
        final JSONObject parameterObj = new JSONObject();
        parameterObj.put("appid",getAppId());
        parameterObj.put("wh_id",getWhId());
        parameterObj.put("pcd_id",id);

        showProgress(getString(R.string.hints_query_data_sz));
        CustomApplication.execute(()->{
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/inventory/spd_detail", HttpRequest.generate_request_parm(parameterObj,getAppSecret()),true);
            if (HttpUtils.checkRequestSuccess(retJson)){
                try {
                    final JSONObject info = JSONObject.parseObject(retJson.getString("info"));
                    if (HttpUtils.checkBusinessSuccess(info)){
                        mOrderInfo = info.getJSONObject("data");
                        if (mOrderInfo != null){
                            runOnUiThread(this::showOrder);
                        }
                    }else throw new JSONException(info.getString("info"));
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.ToastMessageInMainThread(e.getMessage());
                }
            }
            mProgressDialog.dismiss();
        });
    }
    private void showOrder(){
        setOrderStatus();

        final JSONObject object = mOrderInfo;
        setInventoryTask(object);

        mOrderCodeTv.setText(object.getString("pcd_code"));
        mDateTv.setText(Utils.formatDataWithTimestamp(object.getLongValue("addtime") * 1000));
        mDateTv.setText(Utils.formatDataWithTimestamp(object.getLongValue("addtime") * 1000));
        setSaleOperator(object.getString("js_pt_user_id"),object.getString("js_pt_user_name"));
        mRemarkEt.setText(object.getString("remark"));

        mTaskCategory = object.getString("task_category");

        mAdapter.setDataForArray(Utils.getNullObjectAsEmptyJsonArray(mOrderInfo,"goods_list"));
    }

    private void setOrderStatus(){
        final ItemPaddingLinearLayout business_main = findViewById(R.id.business_add_main_layout);
        if (isAudit()){
            business_main.setIgnore(true);
            business_main.setCentreLabel(getString(R.string.audited_sz));
        }
    }
    private boolean isAudit(){
        return Utils.getNotKeyAsNumberDefault(mOrderInfo,"sh_status",1) == 3;
    }
}