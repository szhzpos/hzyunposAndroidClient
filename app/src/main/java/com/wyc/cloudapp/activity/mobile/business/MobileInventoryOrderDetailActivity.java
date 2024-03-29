package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.customizationView.ItemPaddingLinearLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDataAdapter;
import com.wyc.cloudapp.adapter.business.MobileInventoryAuditDetailAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.business.InventoryClearHintDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.util.Locale;

public class MobileInventoryOrderDetailActivity extends AbstractDefinedTitleActivity {
    private TextView mOrderCodeTv,mInventoryWayTv,mInventoryCategoryTv,mTaskName,mRemarkEt,mSumNumTv,mSumAmtTv;
    private JSONObject mTaskInfo;
    private CustomProgressDialog mProgressDialog;
    private MobileInventoryAuditDetailAdapter mAdapter;
    private String mExamineMessage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initStores();
        initView();
        initAuditBtn();
        initOrderDetailsList();

        runOnUiThread(this::queryData);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mProgressDialog != null && mProgressDialog.isShowing())mProgressDialog.dismiss();
    }

    private void initOrderDetailsList(){
        final RecyclerView details_list = findViewById(R.id.details_list);
        if (null != details_list){
            mAdapter = new MobileInventoryAuditDetailAdapter(this);
            mAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    final JSONArray array = mAdapter.getData();
                    double num = 0.0,price = 0.0,sum_num= 0.0,amt = 0.0;

                    int size = array.size();
                    for (int i = 0;i < size;i ++){
                        final JSONObject object = array.getJSONObject(i);
                        num = Utils.getNotKeyAsNumberDefault(object,"sum_xnum",0.0);
                        price = Utils.getNotKeyAsNumberDefault(object,"",0.0);

                        sum_num += num;
                        amt += num * price;
                    }
                    mSumNumTv.setText(String.format(Locale.CHINA,"%.2f",sum_num));
                    mSumAmtTv.setText(String.format(Locale.CHINA,"%.2f",amt));
                    details_list.scrollToPosition(size - 1);
                }
            });
            details_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
            details_list.setAdapter(mAdapter);
            details_list.addItemDecoration(new LinearItemDecoration(getColor(R.color.white),3));
        }
    }

    private void initView(){
        mOrderCodeTv = findViewById(R.id.m_business_order_tv);
        mTaskName = findViewById(R.id.inventory_task_name_tv);
        mRemarkEt = findViewById(R.id.m_business_remark_et);
        mInventoryWayTv = findViewById(R.id.inventory_way_tv);
        mInventoryCategoryTv = findViewById(R.id.inventory_category_tv);

        mSumNumTv = findViewById(R.id.business_sum_num_tv);
        mSumAmtTv = findViewById(R.id.business_sum_amt_tv);
    }

    private void initAuditBtn(){
        final Button btn = findViewById(R.id.inventory_audit_btn);
        btn.setOnClickListener(v -> {
            if (Utils.isNotEmpty(mExamineMessage)){
                final InventoryClearHintDialog dialog = new InventoryClearHintDialog(this);
                dialog.setHintMsg(mExamineMessage);
                if (dialog.exec() == 1){
                    int flag = dialog.getClearFlag();
                    String hint = CustomApplication.getStringByResId(R.string.audit_verify_hint,CustomApplication.getStringByResId(R.string.no_zero_audit_hint));
                    if (flag == 1){
                        hint = CustomApplication.getStringByResId(R.string.audit_verify_hint,CustomApplication.getStringByResId(R.string.zero_audit_hint));
                    }
                    MyDialog.displayAskMessage(this, hint, myDialog -> {
                        myDialog.dismiss();
                        auditInfo(flag);
                    }, MyDialog::dismiss);
                }
            }else auditInfo(0);
        });
    }
    /**
     * clearFlag 1 清零
     * */
    private void auditInfo(int clearFlag){
        showProgress(getString(R.string.auditing_hints));
        CustomApplication.execute(()->{
            final JSONObject param_obj = new JSONObject();
            param_obj.put("appid",getAppId());
            param_obj.put("stores_id",getStoreId());
            param_obj.put("pt_user_id",getPtUserId());
            param_obj.put("pcd_task_id",Utils.getNullStringAsEmpty(mTaskInfo,"pcd_task_id"));
            param_obj.put("reset",clearFlag);

            Logger.d_json(param_obj.toString());

            String sz_param = HttpRequest.generate_request_parma(param_obj,getAppSecret()) ,err = "";
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/inventory/examine_task",sz_param,true);
            Logger.d_json(retJson.toString());
            if (HttpUtils.checkRequestSuccess(retJson)){
                final JSONObject info = JSON.parseObject(retJson.getString("info"));
                if (HttpUtils.checkBusinessSuccess(info)){
                    CustomApplication.runInMainThread(()->{
                        finish();
                        MyDialog.toastMessage(Utils.getNullStringAsEmpty(info,"info"));
                    });
                }else {
                    err = Utils.getNullStringAsEmpty(info,"info");
                }
            }
            if (Utils.isNotEmpty(err)){
                MyDialog.toastMessage("审核单据错误:" + err);
            }
            mProgressDialog.dismiss();
        });
    }

    private void setInventoryWay(final String id,final String name){
        mInventoryWayTv.setTag(id);
        mInventoryWayTv.setText(name);
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
                names.append(object.getString(TreeListBaseAdapter.COL_NAME));
            }
        }
        return names.toString();
    }

    private boolean noHideCategory(final String way){
        final ViewGroup category_layout = findViewById(R.id.category_layout);
        boolean code = "1".equals(way);
        category_layout.setVisibility(code ? View.GONE : View.VISIBLE);
        return !code;
    }

    private void queryData(){
        final JSONObject parameterObj = new JSONObject();
        parameterObj.put("appid",getAppId());
        parameterObj.put("stores_id",getStoreId());
        parameterObj.put("pt_user_id",getPtUserId());
        parameterObj.put("pcd_task_id",getIntent().getStringExtra(AbstractBusinessOrderDataAdapter.KEY));

        Logger.d_json(parameterObj);

        showProgress(getString(R.string.hints_query_data_sz));
        CustomApplication.execute(()->{
            final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/inventory/pd_detail", HttpRequest.generate_request_parma(parameterObj,getAppSecret()),true);
            if (HttpUtils.checkRequestSuccess(retJson)){
                try {
                    final JSONObject info = JSONObject.parseObject(retJson.getString("info"));
                    if (HttpUtils.checkBusinessSuccess(info)){
                        mExamineMessage = Utils.getNullStringAsEmpty(info,"examine_message");
                        mTaskInfo = info.getJSONObject("data");
                        runOnUiThread(this::showOrder);
                    }else throw new JSONException(info.getString("info"));
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.toastMessage(e.getMessage());
                }
            }
            mProgressDialog.dismiss();
        });
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

            mAdapter.setDataForArray(Utils.getNullObjectAsEmptyJsonArray(mTaskInfo,"goods_list"));
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
    private void initStores(){
        TextView warehouseTv = findViewById(R.id.m_business_warehouse_tv);
        warehouseTv.setTag(getStoreId());
        warehouseTv.setText(getStoreName());
    }
    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_inventory_order_detail;
    }
}