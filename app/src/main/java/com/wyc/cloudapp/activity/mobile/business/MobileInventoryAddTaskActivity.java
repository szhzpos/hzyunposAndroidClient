package com.wyc.cloudapp.activity.mobile.business;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.CustomizationView.ItemPaddingLinearLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDataAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForJson;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

public class MobileInventoryAddTaskActivity extends AbstractMobileActivity {
    private TextView mOrderCodeTv,mInventoryWayTv,mInventoryCategoryTv;
    private EditText mTaskName,mRemarkEt;
    private JSONObject mTaskInfo;
    private CustomProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initStores();

        mOrderCodeTv = findViewById(R.id.m_business_order_tv);
        mTaskName = findViewById(R.id.inventory_task_name_tv);
        mRemarkEt = findViewById(R.id.m_business_remark_et);

        initInventoryWay();
        initInventoryCategory();
        initSaveBtn();
        initDelBtn();
        //查询单据号或者根据单号查询订单信息
        runOnUiThread(this::queryData);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null && mProgressDialog.isShowing())mProgressDialog.dismiss();
    }

    private void initSaveBtn(){
        final Button btn = findViewById(R.id.m_business_save_btn);
        btn.setOnClickListener(v -> uploadTaskInfo());
    }
    private void uploadTaskInfo(){
        final String way = Utils.getViewTagValue(mInventoryWayTv,""),ids = Utils.getViewTagValue(mInventoryCategoryTv,"");
        if (noHideCategory(way) && !Utils.isNotEmpty(ids)){
            mInventoryCategoryTv.callOnClick();
            MyDialog.ToastMessageInMainThread(getString(R.string.not_empty_hint_sz,"task_category"));
            return;
        }

        showProgress(getString(R.string.upload_order_hints));
        CustomApplication.execute(()->{
            final JSONObject param_obj = new JSONObject();
            param_obj.put("appid",getAppId());
            param_obj.put("stores_id",getStoreId());

            final String pcd_task_id = Utils.getNullStringAsEmpty(mTaskInfo,"pcd_task_id");
            if (Utils.isNotEmpty(pcd_task_id))param_obj.put("pcd_task_id",pcd_task_id);

            param_obj.put("pcd_task_code",mOrderCodeTv.getText().toString());
            param_obj.put("task_name",mTaskName.getText());
            param_obj.put("task_category",ids);
            param_obj.put("task_mode",way);
            param_obj.put("pt_user_id",getPtUserId());
            param_obj.put("remark",mRemarkEt.getText().toString());

            Logger.d_json(param_obj.toString());

            String sz_param = HttpRequest.generate_request_parm(param_obj,getAppSecret()),err = "";
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/inventory/create_task",sz_param,true);
            if (HttpUtils.checkRequestSuccess(retJson)){
                final JSONObject info = JSON.parseObject(retJson.getString("info"));
                Logger.d_json(retJson.toString());
                if (HttpUtils.checkBusinessSuccess(info)){
                    CustomApplication.runInMainThread(()->{
                        finish();
                        MyDialog.toastMessage(getString(R.string.upload_order_success_hints));
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

    private void initDelBtn(){
        final Button btn = findViewById(R.id.task_del_btn);
        btn.setOnClickListener(v -> deleteTaskInfo());
    }
    private void deleteTaskInfo(){
        if ("2".equals(Utils.getNullStringAsEmpty(mTaskInfo,"status"))){
            if (MyDialog.showMessageToModalDialog(this,"当前任务已存在实盘数据，是否继续删除?") != 1){
                return;
            }
        }
        final String pcd_task_id = Utils.getNullStringAsEmpty(mTaskInfo,"pcd_task_id");
        if (!Utils.isNotEmpty(pcd_task_id)){
            MyDialog.ToastMessageInMainThread(getString(R.string.not_empty_hint_sz,"pcd_task_id"));
            return;
        }
        showProgress(getString(R.string.upload_order_hints));
        CustomApplication.execute(()->{
            final JSONObject param_obj = new JSONObject();
            param_obj.put("appid",getAppId());
            param_obj.put("stores_id",getStoreId());
            param_obj.put("pcd_task_id",pcd_task_id);
            param_obj.put("pt_user_id",getPtUserId());

            Logger.d_json(param_obj.toString());

            String sz_param = HttpRequest.generate_request_parm(param_obj,getAppSecret()),err = "";
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/inventory/del_task",sz_param,true);
            if (HttpUtils.checkRequestSuccess(retJson)){
                final JSONObject info = JSON.parseObject(retJson.getString("info"));
                Logger.d_json(retJson.toString());
                if (HttpUtils.checkBusinessSuccess(info)){
                    CustomApplication.runInMainThread(()->{
                        finish();
                        MyDialog.toastMessage(getString(R.string.upload_order_success_hints));
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


    private void initInventoryWay(){
        final TextView view = findViewById(R.id.inventory_way_tv);
        final String sz = getString(R.string.inventory_way);
        view.setOnClickListener(v -> CustomApplication.runInMainThread(()->{
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,sz.substring(0,sz.length() - 1));
            treeListDialog.setData(MobilePracticalInventoryAddOrderActivity.getInventoryWay(),null,true);
            if (treeListDialog.exec() == 1){
                final JSONObject object = treeListDialog.getSingleContent();
                final String way = object.getString(TreeListBaseAdapter.COL_ID);
                setInventoryWay(way,object.getString(TreeListBaseAdapter.COL_NAME));
                noHideCategory(way);
            }
        }));
        mInventoryWayTv = view;
    }
    private void setInventoryWay(final String id,final String name){
        mInventoryWayTv.setTag(id);
        mInventoryWayTv.setText(name);
    }

    private void initInventoryCategory(){
        final TextView view = findViewById(R.id.inventory_category_tv);
        final String sz = getString(R.string.inventoried_category_sz);
        view.setOnClickListener(v -> CustomApplication.runInMainThread(()->{
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,sz.substring(0,sz.length() - 1));
            treeListDialog.setData(getInventoryCategory(null),null,false);
            if (treeListDialog.exec() == 1){
                final JSONArray array = treeListDialog.getMultipleContent();
                final String ids = getInventoryCategoryIds(array);
                setInventoryCategory(ids,getInventoryCategoryName(ids));
            }
        }));
        mInventoryCategoryTv = view;
    }

    private JSONArray getInventoryCategory(final String id){
        /*id盘点大类类别id，用逗号隔开(盘点方式为2时，必传)*/
        String sql = "SELECT name item_name, category_id item_id FROM shop_category where depth = 1";
        if (null != id){
            sql = sql.concat(" and category_id in ("+ id +")");
        }

        Logger.d("sql:%s",sql);

        final StringBuilder err = new StringBuilder();
        final JSONArray array = SQLiteHelper.getListToJson(sql,err);
        if (null == array){
            MyDialog.ToastMessage(err.toString(), null);
        }
        return array;
    }
    private void setInventoryCategory(final String id,final String name){
        mInventoryCategoryTv.setTag(id);
        mInventoryCategoryTv.setText(name);
    }
    private String getInventoryCategoryName(final String ids){
        final JSONArray array = getInventoryCategory(ids);
        final StringBuilder names = new StringBuilder();
        if (null != array){
            for (int i = 0,size = array.size();i < size;i ++){
                final JSONObject object = array.getJSONObject(i);
                if (names.length() != 0){
                    names.append("|");
                }
                names.append(object.getString("item_name"));
            }
        }
        return names.toString();
    }
    private String getInventoryCategoryIds(final JSONArray array){
        final StringBuilder ids = new StringBuilder();
        if (null != array){
            for (int i = 0,size = array.size();i < size;i ++){
                final JSONObject object = array.getJSONObject(i);
                if (ids.length() != 0){
                    ids.append(",");
                }
                ids.append(object.getString(TreeListBaseAdapter.COL_ID));
            }
        }
        return ids.toString();
    }

    private boolean noHideCategory(final String way){
        final ViewGroup category_layout = findViewById(R.id.category_layout);
        boolean code = "1".equals(way);
        category_layout.setVisibility(code ? View.GONE : View.VISIBLE);
        return !code;
    }

    private void queryData(){
        if (isShowOrder()){
            mTaskInfo = JSONObject.parseObject(getIntent().getStringExtra(AbstractBusinessOrderDataAdapter.KEY));
            showOrder();
        }else {
            generateOrderCode();
        }
    }

    private boolean isShowOrder(){
        final Intent intent = getIntent();
        return null != intent && intent.hasExtra(AbstractBusinessOrderDataAdapter.KEY);
    }

    private void showProgress(final String mess){
        if (mProgressDialog == null)
            mProgressDialog = CustomProgressDialog.showProgress(this,mess);
        else
            mProgressDialog.setMessage(mess).refreshMessage();

        if (!mProgressDialog.isShowing())mProgressDialog.show();
    }

    private void showOrder(){
        Logger.d_json(mTaskInfo);
        if (mTaskInfo != null){
            setTaskStatus();

            mOrderCodeTv.setText(mTaskInfo.getString("pcd_task_code"));
            mTaskName.setText(mTaskInfo.getString("task_name"));

            final String id = mTaskInfo.getString("task_mode");
            setInventoryWay(id, MobilePracticalInventoryAddOrderActivity.getInventoryModeName(id));
            if(noHideCategory(id)){
                final String category_ids = mTaskInfo.getString("task_category");
                setInventoryCategory(category_ids,getInventoryCategoryName(category_ids));
            }
            setTaskDate();
            mRemarkEt.setText(mTaskInfo.getString("xnote"));
        }
    }
    private void setTaskStatus(){
        final ItemPaddingLinearLayout business_main = findViewById(R.id.business_add_main_layout);
        if (isInventoried()){
            business_main.setDisableEvent(true);
            business_main.setCentreLabel(getString(R.string.inventoried_sz));
        }
    }

    private boolean isInventoried(){
        return "3".equals(Utils.getNullStringAsEmpty(mTaskInfo,"status"));
    }

    private void setTaskDate(){
        final ViewGroup date_layout = findViewById(R.id.date_layout);
        date_layout.setVisibility(View.VISIBLE);
        final TextView m_business_date_tv = date_layout.findViewById(R.id.m_business_date_tv);
        m_business_date_tv.setText(FormatDateTimeUtils.formatTimeWithTimestamp(mTaskInfo.getLongValue("addtime") * 1000));

    }

    private void generateOrderCode() {
        CustomApplication.execute(()->{
            final JSONObject parameterObj = new JSONObject();
            parameterObj.put("appid",getAppId());
            parameterObj.put("prefix","PC");
            final String sz_param = HttpRequest.generate_request_parm(parameterObj,getAppSecret());
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/codes/mk_code",sz_param,true);

            if (HttpUtils.checkRequestSuccess(retJson)){
                try {
                    final JSONObject info = JSON.parseObject(retJson.getString("info"));
                    if (HttpUtils.checkBusinessSuccess(info)){
                        CustomApplication.runInMainThread(()-> mOrderCodeTv.setText(info.getString("code")));
                    }else {
                        MyDialog.ToastMessageInMainThread(info.getString("info"));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.ToastMessageInMainThread(e.getLocalizedMessage());
                }
            }else {
                MyDialog.ToastMessageInMainThread(getString(R.string.query_business_order_id_hint_sz,retJson.getString("info")));
            }
        });
    }

    private void initStores(){
        TextView warehouseTv = findViewById(R.id.m_business_warehouse_tv);
        warehouseTv.setTag(getStoreId());
        warehouseTv.setText(getStoreName());
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_inventory_add_task;
    }
}