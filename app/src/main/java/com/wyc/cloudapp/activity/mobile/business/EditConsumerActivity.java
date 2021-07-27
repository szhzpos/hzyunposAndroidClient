package com.wyc.cloudapp.activity.mobile.business;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.bean.Consumer;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.tree.TreeListDialogForJson;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.util.Locale;

import butterknife.BindView;

/*编辑客户*/
public class EditConsumerActivity extends AbstractEditArchiveActivity {

    @BindView(R.id.consumer_code_et)
    EditText mConsumerCode;
    @BindView(R.id.consumer_name_et)
    EditText mConsumerName;
    @BindView(R.id.consumer_addr_et)
    EditText mConsumerAddr;
    @BindView(R.id.contacts_job_et)
    EditText mContactsJob;
    @BindView(R.id.contacts_name_et)
    EditText mContactsName;
    @BindView(R.id.contacts_mobile_et)
    EditText mContactsMobile;

    private TextView mDefaultPrice,mConsumerSettlementWay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initDefaultPrice();
        initSettlementWay();

        initContent();

    }

    private void initSettlementWay(){
        final TextView consumer_settlement_way_tv = findViewById(R.id.consumer_settlement_way_tv);
        consumer_settlement_way_tv.setOnClickListener(v -> {
            final String sz = getString(R.string.supplier_cooperation_way_sz);
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,sz.substring(0,sz.length() - 1));
            treeListDialog.setData(getSettlementWay(),null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    consumer_settlement_way_tv.setTag(object.getIntValue(TreeListBaseAdapter.COL_ID));
                    consumer_settlement_way_tv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                }
            });
        });
        mConsumerSettlementWay = consumer_settlement_way_tv;

        setDefaultSettlementWay();
    }
    private JSONArray getSettlementWay(){
        return JSONArray.parseArray("[{\"item_id\":1,\"item_name\":\"现结\"},{\"item_id\":2,\"item_name\":\"不定周期\"},{\"item_id\":3,\"item_name\":\"月结\"}," +
                "{\"item_id\":4,\"item_name\":\"季结\"},{\"item_id\":5,\"item_name\":\"年结\"}]");
    }
    private void setDefaultSettlementWay(){
        if (mConsumerSettlementWay != null){
            final JSONObject default_way = getSettlementWay().getJSONObject(1);
            mConsumerSettlementWay.setTag(default_way.getIntValue(TreeListBaseAdapter.COL_ID));
            mConsumerSettlementWay.setText(default_way.getString(TreeListBaseAdapter.COL_NAME));
        }
    }

    private void initDefaultPrice(){
        final TextView default_price_tv = findViewById(R.id.default_price_tv);
        default_price_tv.setOnClickListener(v -> {
            final String sz = getString(R.string.supplier_settlement_way_sz);
            final TreeListDialogForJson treeListDialog = new TreeListDialogForJson(this,sz.substring(0,sz.length() - 1));
            treeListDialog.setData(getDefaultPrice(),null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    default_price_tv.setTag(object.getIntValue(TreeListBaseAdapter.COL_ID));
                    default_price_tv.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                }
            });
        });
        mDefaultPrice = default_price_tv;
        setDefaultCooperationWay();
    }
    private JSONArray getDefaultPrice(){
        /*1零售价，2优惠价，3配送价，4批发价*/
        return JSONArray.parseArray("[{\"item_id\":1,\"item_name\":\"零售价\"},{\"item_id\":2,\"item_name\":\"优惠价\"},{\"item_id\":3,\"item_name\":\"配送价\"},{\"item_id\":4,\"item_name\":\"批发价\"}]");
    }
    private void setDefaultCooperationWay(){
        if (null != mDefaultPrice){
            final JSONObject default_way = getDefaultPrice().getJSONObject(1);
            mDefaultPrice.setTag(default_way.getIntValue(TreeListBaseAdapter.COL_ID));
            mDefaultPrice.setText(default_way.getString(TreeListBaseAdapter.COL_NAME));
        }
    }

    private void initContent(){
        final Consumer consumer = getIntent().getParcelableExtra("obj");
        if (consumer != null){
            mConsumerCode.setTag(consumer.getC_s_id());
            mConsumerCode.setText(consumer.getCs_code());
            mConsumerCode.setEnabled(false);

            mConsumerName.requestFocus();
            mConsumerName.setText(consumer.getCs_name());
            mConsumerAddr.setText(consumer.getAddress());
            mDefaultPrice.setTag(consumer.getCs_kf_price());
            mDefaultPrice.setText(consumer.getCs_kf_price_name());

            mConsumerSettlementWay.setTag(consumer.getCustomer_settlement_cycle_id());
            mConsumerSettlementWay.setText(consumer.getCustomer_settlement_cycle_name());
            mContactsJob.setText(consumer.getRoles());
            mContactsName.setText(consumer.getName());
            mContactsMobile.setText(consumer.getMobile());
        }
    }


    @Override
    protected int getLayout() {
        return R.layout.activity_edit_consumer;
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
        final String code = mConsumerCode.getText().toString(),name = mConsumerName.getText().toString();
        if (code.isEmpty()){
            MyDialog.ToastMessage(mConsumerCode,getString(R.string.not_empty_hint_sz,getString(R.string.consumer_code_sz)), getWindow());
            return;
        }
        if (name.isEmpty()){
            MyDialog.ToastMessage(mConsumerName,getString(R.string.not_empty_hint_sz,getString(R.string.consumer_name_sz)), getWindow());
            return;
        }
        showProgress();
        CustomApplication.execute(()->{
            final JSONObject param = new JSONObject();

            param.put("appid",getAppId());
            param.put("pt_user_id",getPtUserId());
            param.put("cs_name",name);
            param.put("c_s_id", Utils.getViewTagValue(mConsumerCode,""));
            param.put("cs_code",code);
            param.put("address",mConsumerAddr.getText());
            param.put("cs_kf_price",Utils.getViewTagValue(mDefaultPrice,-1));
            param.put("customer_settlement_cycle_id",Utils.getViewTagValue(mConsumerSettlementWay,-1));
            param.put("name",mContactsName.getText());
            param.put("mobile",mContactsMobile.getText());
            param.put("roles",mContactsJob.getText());

            JSONObject ret_obj = HttpUtils.sendPost(getUrl() + "/api/supplier_search/customer_set", HttpRequest.generate_request_parm(param,getAppSecret()),true);
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
                    MyDialog.ToastMessageInMainThread(e.getMessage());
                }
            }
            dismissProgress();
        });
    }

    private void reset(){
        final String space = getString(R.string.space_sz);
        mConsumerCode.setText(resetSupplierCode());
        mConsumerCode.setTag(null);

        mConsumerName.setText(space);
        mConsumerAddr.setText(space);
        mContactsJob.setText(space);
        mContactsName.setText(space);
        mContactsMobile.setText(space);
        setDefaultCooperationWay();
        setDefaultSettlementWay();
    }
    private String resetSupplierCode(){
        if (mConsumerCode == null)return "";
        String code = mConsumerCode.getText().toString(),new_code = "";
        try {
            new_code = String.format(Locale.CHINA,"%0"+ code.length() +"d",Integer.parseInt(code) + 1);
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
        return new_code;
    }

    @Override
    protected boolean isExist() {
        return mConsumerCode == null || mConsumerCode.getText().length() == 0;
    }

    public static void start(MobileConsumerInfoActivity context, boolean modify, final Parcelable obj){
        final Intent intent = new Intent();
        intent.setClass(context,EditConsumerActivity.class);
        intent.putExtra("key",modify);
        intent.putExtra(AbstractMobileActivity.TITLE_KEY,context.getString(modify ? R.string.modify_consumer_sz : R.string.new_consumer_sz));
        intent.putExtra("obj",obj);
        context.startActivity(intent);
    }
}