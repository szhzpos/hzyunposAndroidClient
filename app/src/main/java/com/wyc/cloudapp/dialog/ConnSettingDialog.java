package com.wyc.cloudapp.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogContext;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConnSettingDialog extends AbstractDialogContext {
    private TextView mUrlTv, mAppIdTv, mAppSecretTv,mStore_nameTv;
    private EditText mShopIdEt;

    public ConnSettingDialog(final Context context, final String title) {
        super(context,title,R.style.MyDialog);
    }

    @Override
    public void  onBackPressed(){
        super.onBackPressed();
    }

    @Override
    public void show(){
        super.show();
        showConnParam();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }
    @Override
    protected int getContentLayoutId(){
        return R.layout.con_param_setting_dialog_layout;
    }

    private void initView() {

        mShopIdEt = findViewById(R.id.shop_id_et);

        mAppIdTv = findViewById(R.id.appId);
        mAppSecretTv = findViewById(R.id.appSecret);
        mStore_nameTv = findViewById(R.id.store_name);

        mAppSecretTv.setSelectAllOnFocus(true);
        mAppIdTv.setSelectAllOnFocus(true);

        initUrlTv();
        initCancelBtn();
        initSaveBtn();
    }

    private void initUrlTv(){
        final TextView tv = findViewById(R.id.server_url);
        tv.setSelectAllOnFocus(true);
        tv.setOnFocusChangeListener((v, hasFocus)->{
            if (!hasFocus){
                verifyUrl();
            }
        });
        mUrlTv = tv;
    }

    private void initCancelBtn(){
        final Button cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener((View v)-> {
            if (mShopIdEt != null){
                mShopIdEt.setText(Utils.getViewTagValue(mShopIdEt,""));
            }
            closeWindow();
        });
    }

    protected double getWidthRatio(){
        double ratio = 0.4;
        if (Utils.lessThan7Inches(mContext)) {
            ratio = 0.9;
        }
        return ratio;
    }

    private boolean check_shop_id(final String shop_id,final JSONObject param){
        if (!param.isEmpty()){
            return !Utils.getNullStringAsEmpty(param,"shop_id").equals(shop_id);
        }
        return false;
    }

    private void initSaveBtn(){
        final Button save_btn = findViewById(R.id.save);
        if (null != save_btn){
            save_btn.setOnClickListener((View v)->{
                final String shop_id = mShopIdEt.getText().toString();
                if (!"".equals(shop_id)){
                    final JSONObject param = CustomApplication.getConnParam();
                    if (check_shop_id(shop_id,param)){
                        MyDialog.displayAskMessage(mContext, "当前商户与数据库中的商户不一致，是否需要保存？", myDialog -> {
                            myDialog.dismiss();
                            if (mStore_nameTv != null)mStore_nameTv.setText(mContext.getString(R.string.space_sz));

                            final JEventLoop loop = new JEventLoop();
                            final StringBuilder err = new StringBuilder();
                            final CustomProgressDialog customProgressDialog = new CustomProgressDialog(mContext);
                            customProgressDialog.setCancel(false).setMessage("正在备份数据库...").show();
                            CustomApplication.execute(()->{
                                if (SQLiteHelper.backupDBPublicDir(mContext,String.format(Locale.CHINA,"hzYunPos<%s>%s",Utils.getNullStringAsEmpty(param,"shop_id"), new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date())),err)){
                                    loop.done(1);
                                }else
                                    loop.done(0);
                            });
                            final int code = loop.exec();
                            customProgressDialog.dismiss();
                            if (code == 1){
                                save(shop_id);
                            }else {
                                MyDialog.displayMessage(v.getContext(), err.toString());
                            }
                        }, MyDialog::dismiss);
                    }else {
                        save(shop_id);
                    }
                }else{
                    MyDialog.SnackbarMessage(getWindow(),mContext.getString(R.string.not_empty_hint_sz,"商户号"),getCurrentFocus());
                }
            });
        }
    }

    private void save(final String shop_id){
        final JSONObject json = new JSONObject();
        json.put("server_url",verifyUrl());
        json.put("appId", mAppIdTv.getText().toString());
        json.put("url", mUrlTv.getText().toString());
        json.put("shop_id",shop_id);
        json.put("appSecret", mAppSecretTv.getText().toString());

        CustomApplication.setConnParam(json);
        dismiss();
    }

    private void showConnParam(){
        final JSONObject param = CustomApplication.getConnParam();
        if (Utils.JsonIsNotEmpty(param)){
            try {
                mShopIdEt.setText(param.getString("shop_id"));
                mShopIdEt.setTag(mShopIdEt.getText().toString());

                mUrlTv.setText(param.getString("url"));
                final JSONObject storeInfo = JSON.parseObject(Utils.getNullOrEmptyStringAsDefault(param,"storeInfo","{}"));
                if (storeInfo.containsKey("stores_name")){
                    mStore_nameTv.setText(String.format("%s%s%s%s",storeInfo.getString("stores_name"),"[",storeInfo.getString("stores_id"),"]"));
                }else {
                    final View view = findViewById(R.id.ip_fo);
                    view.setVisibility(View.GONE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                MyDialog.ToastMessage("显示门店信息错误：" + e.getMessage(),mContext,null);
            }
        }else {
            final View view = findViewById(R.id.ip_fo);
            view.setVisibility(View.GONE);
        }
    }

    private String verifyUrl(){
        String url = mUrlTv.getText().toString();
        if (!url.isEmpty() && !url.contains("http")){
            url = "http://" + mShopIdEt.getText() + url;
        }
        return url;
    }

    public JSONObject getShopInfo(){
        JSONObject object = new JSONObject();
        if (null != mShopIdEt && mStore_nameTv != null){
            object.put("shop_id", mShopIdEt.getText().toString());
            object.put("shop_name",mStore_nameTv.getText().toString());
        }
        return object;
    }

}