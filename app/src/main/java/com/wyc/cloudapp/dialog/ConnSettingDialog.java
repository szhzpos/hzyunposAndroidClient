package com.wyc.cloudapp.dialog;

import android.content.Context;

import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnContextImp;
import com.wyc.cloudapp.logger.Logger;
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

import static android.content.Context.WINDOW_SERVICE;

public class ConnSettingDialog extends DialogBaseOnContextImp {
    private TextView mUrl,mAppId,mAppscret,mStore_name;
    private CustomProgressDialog mDialog;
    private Myhandler mHandler;
    private EditText mShopId;
    public ConnSettingDialog(final Context context,final String title) {
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

        mShopId = findViewById(R.id.shop_id_et);
        mUrl = findViewById(R.id.server_url);
        mAppId = findViewById(R.id.appId);
        mAppscret = findViewById(R.id.appSecret);
        mStore_name = findViewById(R.id.sec_store_name);

        mDialog = new CustomProgressDialog(mContext);
        mHandler = new Myhandler(this);

        mUrl.setSelectAllOnFocus(true);
        mAppscret.setSelectAllOnFocus(true);
        mAppId.setSelectAllOnFocus(true);


        mUrl.setOnFocusChangeListener((v,hasFocus)->{
            if (!hasFocus){
                verifyUrl();
            }
        });

/*        mStore_name.setOnClickListener((View v)-> queryStoreInfo());

        mStore_name.setOnFocusChangeListener((v, hasFocus) -> {
            Utils.hideKeyBoard((EditText)v);
            if (hasFocus){
                queryStoreInfo();
            }
        });*/

        initCancelBtn();
        initSave();
        initWindowSize();

    }

    private void initCancelBtn(){
        final Button cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener((View v)-> closeWindow());
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

    private void initSave(){
        final Button save_btn = findViewById(R.id.save);
        if (null != save_btn){
            save_btn.setOnClickListener((View v)->{
                final String shop_id = mShopId.getText().toString();
                if (!"".equals(shop_id)){
                    final JSONObject json = new JSONObject(),param = new JSONObject();
                    if(SQLiteHelper.getLocalParameter("connParam",param)){
                        if (!param.isEmpty()){
                            final JSONObject store_info = param.getJSONObject("storeInfo");
                            if (!store_info.isEmpty()){
                                final String local_shop_id = Utils.getNullStringAsEmpty(store_info,"stores_id");
                                if (!local_shop_id.equals(shop_id)){

                                }
                            }

                        }

                    }else{
                        MyDialog.ToastMessage(param.getString("info"),mContext,getWindow());
                    }

                    json.put("server_url",verifyUrl());
                    json.put("appId",mAppId.getText().toString());
                    json.put("url",mUrl.getText().toString());
                    json.put("shop_id",shop_id);
                    json.put("appScret",mAppscret.getText().toString());
                    json.put("storeInfo","{}");


                    param.put("parameter_id","connParam");
                    param.put("parameter_content",json);
                    param.put("parameter_desc","门店信息、服务器连接参数");
                    final StringBuilder err = new StringBuilder();
                    if (SQLiteHelper.saveFormJson(param,"local_parameter",null,1,err)){
                        MyDialog.ToastMessage("保存成功！",mContext,null);
                        ConnSettingDialog.this.dismiss();
                    }else
                        MyDialog.displayMessage(null,err.toString(),v.getContext());
                }else{
                    MyDialog.SnackbarMessage(getWindow(),mContext.getString(R.string.not_empty_hint_sz,"商户号"),getCurrentFocus());
                }

            });
        }
    }

    private static class Myhandler extends Handler {
        private WeakReference<ConnSettingDialog> weakConnSettingDialog;
        private Myhandler(ConnSettingDialog dialog){
            this.weakConnSettingDialog = new WeakReference<>(dialog);
        }
        public void handleMessage(@NonNull Message msg){
            ConnSettingDialog settingDialog = weakConnSettingDialog.get();
            if (settingDialog == null)return;
            if (settingDialog.mDialog !=null)settingDialog.mDialog.dismiss();
            switch (msg.what){
                case MessageID.DIS_ERR_INFO_ID:
                    if (msg.obj != null)
                        MyDialog.displayErrorMessage(null,msg.obj.toString(), settingDialog.mContext);
                    break;
                case MessageID.DIS_STORE_INFO_ID://查询门店信息正确,则要在线请求仓库信息

                    break;
            }

        }
    }

    private void queryStoreInfo(){
        if (mUrl.getText().length() == 0){
            mUrl.requestFocus();
            MyDialog.SnackbarMessage(getWindow(),"服务器URL不能为空！",getCurrentFocus());
            return;
        }

        mDialog.setMessage("正在查询门店信息...").show();
        final HttpRequest httpRequest = new HttpRequest();
        AsyncTask.execute(()->{
            String  url = mUrl.getText() + "/api/scale/get_stores",sz_param;
            JSONObject object = new JSONObject(),retJson,info_json;

            try {
                object.put("appid",mAppId.getText());

                sz_param = HttpRequest.generate_request_parm(object,mAppscret.getText().toString());
                retJson = httpRequest.sendPost(url,sz_param,true);
                switch (retJson.getIntValue("flag")) {
                    case 0:
                        mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,retJson.getString("info")).sendToTarget();
                        break;
                    case 1:
                        info_json = JSON.parseObject(retJson.getString("info"));
                        switch (info_json.getString("status")){
                            case "n":
                                mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,info_json.getString("info")).sendToTarget();
                                break;
                            case "y":
                                mHandler.obtainMessage(MessageID.DIS_STORE_INFO_ID,info_json.getJSONArray("data")).sendToTarget();
                                break;
                        }
                        break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,e.getMessage()).sendToTarget();
            }
        });
    }

    private void showConnParam(){
        final JSONObject param = new JSONObject();
        if(SQLiteHelper.getLocalParameter("connParam",param)){
            if (Utils.JsonIsNotEmpty(param)){
                try {
                    mShopId.setText(param.getString("shop_id"));
                    mUrl.setText(param.getString("url"));
                    //mAppId.setText(param.getString("appId"));
                    //mAppscret.setText(param.getString("appScret"));
                    final JSONObject storeInfo = JSON.parseObject(param.getString("storeInfo"));
                    mStore_name.setText(storeInfo.getString("stores_name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    MyDialog.ToastMessage("显示门店信息错误：" + e.getMessage(),mContext,null);
                }
            }
        }else{
            MyDialog.ToastMessage(param.getString("info"),mContext,getWindow());
        }
    }

    private String verifyUrl(){
        String url = mUrl.getText().toString();
        if (!url.isEmpty() && !url.contains("http")){
            url = "http://" + mShopId.getText() + url;
        }
        return url;
    }

    public String getUrl(){
        return mShopId.getText().toString();
    }

}