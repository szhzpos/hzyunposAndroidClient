package com.wyc.cloudapp.activity.mobile.business;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.EditText;
import android.widget.TextView;

import androidx.lifecycle.ViewModelProvider;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.Supplier;
import com.wyc.cloudapp.data.viewModel.SupplierViewModel;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForJson;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import butterknife.BindView;

//编辑供应商
public class EditSupplierActivity extends AbstractEditArchiveActivity {
    private static final String KEY = "modify";

    @BindView(R.id.supplier_code_et)
    EditText mSupplierCode;
    @BindView(R.id.supplier_name_et)
    EditText mSupplierName;
    @BindView(R.id.supplier_addr_et)
    EditText mSupplierAddr;
    @BindView(R.id.contacts_job_et)
    EditText mContactsJob;
    @BindView(R.id.contacts_name_et)
    EditText mContactsName;
    @BindView(R.id.contacts_mobile_et)
    EditText mContactsMobile;

    private TextView mSupplierCooperationWay,mSupplierSettlementWay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCooperationWay();
        initSettlementWay();

        initContent();
    }

    private void getSupplierCode(){
        new ViewModelProvider(this).get(SupplierViewModel.class).getCodeModel().observe(this, s -> mSupplierCode.setText(s));
    }

    private void initSettlementWay(){
        final TextView supplier_settlement_way_tv = findViewById(R.id.supplier_settlement_way_tv);
        final JSONArray array = getSettlementWay();
        supplier_settlement_way_tv.setOnClickListener(v -> {
            final String sz = getString(R.string.supplier_cooperation_way_sz);
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,sz.substring(0,sz.length() - 1));
            treeListDialog.setData(array,null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    supplier_settlement_way_tv.setTag(object.getIntValue(TreeListBaseAdapter.COL_ID));
                    supplier_settlement_way_tv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                }
            });
        });
        mSupplierSettlementWay = supplier_settlement_way_tv;

        setDefaultSettlementWay();
    }
    private JSONArray getSettlementWay(){
        return JSONArray.parseArray("[{\"item_id\":1,\"item_name\":\"现结\"},{\"item_id\":2,\"item_name\":\"不定周期\"},{\"item_id\":3,\"item_name\":\"月结\"}," +
                "{\"item_id\":4,\"item_name\":\"季结\"},{\"item_id\":5,\"item_name\":\"年结\"}]");
    }
    private void setDefaultSettlementWay(){
        if (mSupplierSettlementWay != null){
            final JSONObject default_way = getSettlementWay().getJSONObject(1);
            mSupplierSettlementWay.setTag(default_way.getIntValue(TreeListBaseAdapter.COL_ID));
            mSupplierSettlementWay.setText(default_way.getString(TreeListBaseAdapter.COL_NAME));
        }
    }

    private void initCooperationWay(){
        final TextView supplier_co_way_tv = findViewById(R.id.supplier_co_way_tv);
        final JSONArray array = getCooperationWay();
        supplier_co_way_tv.setOnClickListener(v -> {
            final String sz = getString(R.string.supplier_settlement_way_sz);
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,sz.substring(0,sz.length() - 1));
            treeListDialog.setData(array,null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    supplier_co_way_tv.setTag(object.getIntValue(TreeListBaseAdapter.COL_ID));
                    supplier_co_way_tv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                }
            });
        });
        mSupplierCooperationWay = supplier_co_way_tv;
        setDefaultCooperationWay();
    }
    public static JSONArray getCooperationWay(){
        return JSONArray.parseArray("[{\"item_id\":1,\"item_name\":\"联营\"},{\"item_id\":2,\"item_name\":\"购销\"},{\"item_id\":3,\"item_name\":\"租赁\"},{\"item_id\":4,\"item_name\":\"代销\"}]");
    }
    private void setDefaultCooperationWay(){
        if (null != mSupplierCooperationWay){
            final JSONObject default_way = getCooperationWay().getJSONObject(1);
            mSupplierCooperationWay.setTag(default_way.getIntValue(TreeListBaseAdapter.COL_ID));
            mSupplierCooperationWay.setText(default_way.getString(TreeListBaseAdapter.COL_NAME));
        }
    }

    private void initContent(){
        final Supplier supplier = getIntent().getParcelableExtra("obj");
        if (supplier != null){
            mSupplierCode.setTag(supplier.getC_s_id());
            mSupplierCode.setText(supplier.getCs_code());
            mSupplierCode.setEnabled(false);

            mSupplierName.requestFocus();
            mSupplierName.setText(supplier.getCs_name());
            mSupplierAddr.setText(supplier.getAddress());
            mSupplierCooperationWay.setTag(supplier.getHz_method());
            mSupplierCooperationWay.setText(supplier.getHz_method_name());
            mSupplierSettlementWay.setTag(supplier.getSupplier_settlement_cycle_id());
            mSupplierSettlementWay.setText(supplier.getSupplier_settlement_cycle_name());
            mContactsJob.setText(supplier.getRoles());
            mContactsName.setText(supplier.getName());
            mContactsMobile.setText(supplier.getMobile());
        }else getSupplierCode();
    }


    @Override
    protected int getLayout() {
        return R.layout.activity_edit_supplier;
    }

    @Override
    protected void sure() {
        submit(false);
    }

    @Override
    protected void saveAndAdd() {
        submit(true);
    }

    private void submit(boolean reset){
        final String code = mSupplierCode.getText().toString(),name = mSupplierName.getText().toString();
        if (code.isEmpty()){
            MyDialog.ToastMessage(mSupplierCode,getString(R.string.not_empty_hint_sz,getString(R.string.supplier_code_sz)), getWindow());
            return;
        }
        if (name.isEmpty()){
            MyDialog.ToastMessage(mSupplierName,getString(R.string.not_empty_hint_sz,getString(R.string.supplier_name_sz)), getWindow());
            return;
        }
        showProgress();
        CustomApplication.execute(()->{
            final JSONObject param = new JSONObject();

            param.put("appid",getAppId());
            param.put("pt_user_id",getPtUserId());
            param.put("cs_name",name);
            param.put("c_s_id", Utils.getViewTagValue(mSupplierCode,""));
            param.put("cs_code",code);
            param.put("address",mSupplierAddr.getText());
            param.put("hz_method",Utils.getViewTagValue(mSupplierCooperationWay,-1));
            param.put("supplier_settlement_cycle_id",Utils.getViewTagValue(mSupplierSettlementWay,-1));
            param.put("name",mContactsName.getText());
            param.put("mobile",mContactsMobile.getText());
            param.put("roles",mContactsJob.getText());

            JSONObject ret_obj = HttpUtils.sendPost(getUrl() + "/api/supplier_search/supplier_set", HttpRequest.generate_request_parma(param,getAppSecret()),true);
            if (HttpUtils.checkRequestSuccess(ret_obj)){
                try {
                    ret_obj = JSONObject.parseObject(ret_obj.getString("info"));
                    if (HttpUtils.checkBusinessSuccess(ret_obj)){
                        if (reset){
                            CustomApplication.runInMainThread(this::reset);
                        }else
                            finish();
                    }else throw new JSONException(ret_obj.getString("info"));
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.toastMessage(e.getMessage());
                }
            }
            dismissProgress();
        });
    }

    private void reset(){
        final String space = getString(R.string.space_sz);
        resetSupplierCode();

        mSupplierName.setText(space);
        mSupplierAddr.setText(space);
        mContactsJob.setText(space);
        mContactsName.setText(space);
        mContactsMobile.setText(space);
        setDefaultCooperationWay();
        setDefaultSettlementWay();
    }
    private void resetSupplierCode(){
        if (mSupplierCode != null){
            getSupplierCode();
            mSupplierCode.setTag(null);
        }
    }

    @Override
    protected boolean isExist() {
        return mSupplierCode == null || mSupplierCode.getText().length() == 0;
    }

    public static void start(MobileSupplierInfoActivity context, boolean modify, final Parcelable obj){
        final Intent intent = new Intent();
        intent.setClass(context,EditSupplierActivity.class);
        intent.putExtra(KEY,modify).putExtra(AbstractDefinedTitleActivity.TITLE_KEY,context.getString(modify ? R.string.modify_supplier_sz : R.string.new_supplier_sz));
        intent.putExtra("obj",obj);
        context.startActivity(intent);
    }
}