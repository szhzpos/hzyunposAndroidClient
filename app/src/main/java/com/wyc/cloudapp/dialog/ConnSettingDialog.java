package com.wyc.cloudapp.dialog;

import android.content.Context;

import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogBaseOnContextImp;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.Utils;

import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.WINDOW_SERVICE;

public class ConnSettingDialog extends AbstractDialogBaseOnContextImp {
    private TextView mUrlTv, mAppIdTv, mAppscretTv,mStore_nameTv;
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
        mAppscretTv = findViewById(R.id.appSecret);
        mStore_nameTv = findViewById(R.id.store_name);

        mAppscretTv.setSelectAllOnFocus(true);
        mAppIdTv.setSelectAllOnFocus(true);

        initUrlTv();
        initCancelBtn();
        initSaveBtn();
        initWindowSize();

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

    private void initWindowSize(){
        final WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
        if (m != null){
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
            Point point = new Point();
            d.getSize(point);
            Window dialogWindow = this.getWindow();
            if (dialogWindow != null){
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.width = (int)(0.4 * point.x); // 宽度
                dialogWindow.setAttributes(lp);
            }
        }
    }

    private boolean check_shop_id(final String shop_id,final JSONObject param){
        boolean code;
        if(code = SQLiteHelper.getLocalParameter("connParam",param)){
            if (!param.isEmpty()){
                return !Utils.getNullStringAsEmpty(param,"shop_id").equals(shop_id);
            }else {
                code = false;
            }
        }
        return code;
    }

    private void initSaveBtn(){
        final Button save_btn = findViewById(R.id.save);
        if (null != save_btn){
            save_btn.setOnClickListener((View v)->{
                final String shop_id = mShopIdEt.getText().toString();
                if (!"".equals(shop_id)){
                    final JSONObject param = new JSONObject();
                    if (check_shop_id(shop_id,param)){
                        MyDialog.displayAskMessage(null, "当前商户与数据库中的商户不一致，是否需要保存？", mContext, myDialog -> {
                            myDialog.dismiss();
                            if (mStore_nameTv != null)mStore_nameTv.setText(mContext.getString(R.string.space_sz));

                            final JEventLoop loop = new JEventLoop();
                            final StringBuilder err = new StringBuilder();
                            final CustomProgressDialog customProgressDialog = new CustomProgressDialog(mContext);
                            customProgressDialog.setCancel(false).setMessage("正在备份数据库...").show();
                            CustomApplication.execute(()->{
                                if (SQLiteHelper.backupDB(String.format(Locale.CHINA,"hzYunPos<%s>%s",Utils.getNullStringAsEmpty(param,"shop_id"), new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA).format(new Date())),err)){
                                    loop.done(1);
                                }else
                                    loop.done(0);
                            });
                            final int code = loop.exec();
                            customProgressDialog.dismiss();
                            if (code == 1){
                                save(shop_id);
                            }else {
                                MyDialog.displayMessage(null,err.toString(),v.getContext());
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
        if (SQLiteHelper.initDb(mContext)){
            final JSONObject json = new JSONObject();
            final StringBuilder err = new StringBuilder();

            json.put("server_url",verifyUrl());
            json.put("appId", mAppIdTv.getText().toString());
            json.put("url", mUrlTv.getText().toString());
            json.put("shop_id",shop_id);
            json.put("appSecret", mAppscretTv.getText().toString());
            json.put("storeInfo","{}");

            if (SQLiteHelper.saveLocalParameter("connParam",json,"门店信息、服务器连接参数",err)){
                MyDialog.ToastMessage("保存成功！",mContext,null);
                ConnSettingDialog.this.dismiss();
            }else
                MyDialog.displayMessage(null,err.toString(),mContext);
        }
    }

    private void showConnParam(){
        final JSONObject param = new JSONObject();
        if(SQLiteHelper.getLocalParameter("connParam",param)){
            if (Utils.JsonIsNotEmpty(param)){
                try {
                    mShopIdEt.setText(param.getString("shop_id"));
                    mShopIdEt.setTag(mShopIdEt.getText().toString());

                    mUrlTv.setText(param.getString("url"));
                    final JSONObject storeInfo = JSON.parseObject(param.getString("storeInfo"));
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
        }else{
            MyDialog.ToastMessage(param.getString("info"),mContext,getWindow());
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