package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;

import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.Logger;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

import static android.content.Context.WINDOW_SERVICE;

public class ConnSettingDialog extends Dialog {
    private Context mContext;
    private EditText mUrl,mAppId,mAppscret,mStore_name;
    private CustomProgressDialog mDialog;
    private CustomePopupWindow mPopupWindow;
    private Myhandler mHandler;
    private JSONObject mStoreInfo;

    public ConnSettingDialog(Context context) {
        super(context,R.style.MyDialog);
        this.mContext = context;
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

        setContentView(this.getLayoutInflater().inflate(R.layout.con_param_setting_dialog, null));
        //按空白处不能取消动画
        setCanceledOnTouchOutside(false);

        //初始化界面控件
        initView();

    }
       /**
     * 初始化界面控件
     */
    private void initView() {
        Button save = findViewById(R.id.save),
                cancel = findViewById(R.id.cancel);

        mUrl = findViewById(R.id.server_url);
        mAppId = findViewById(R.id.appId);
        mAppscret = findViewById(R.id.appSecret);
        mStore_name = findViewById(R.id.store_name);

        mDialog = new CustomProgressDialog(mContext,R.style.CustomDialog);
        mPopupWindow = new CustomePopupWindow(mContext);
        mHandler = new Myhandler(this);

        mUrl.setSelectAllOnFocus(true);
        mAppscret.setSelectAllOnFocus(true);
        mAppId.setSelectAllOnFocus(true);

        save.setOnClickListener((View v)->{
            JSONObject json = new JSONObject(),param = new JSONObject();
            String url = mUrl.getText().toString();
            try {
                if (!url.contains("http")){
                    url = "http://" + url;
                }
                json.put("server_url",url);
                json.put("appId",mAppId.getText());
                json.put("appScret",mAppscret.getText());
                json.put("storeInfo",mStoreInfo.toString());

                param.put("parameter_id","connParam");
                param.put("parameter_content",json);
                StringBuilder err = new StringBuilder();
                if (SQLiteHelper.replaceJson(param,"local_parameter",null,err)){
                    Utils.ToastMessage("保存成功！",mContext);
                    ConnSettingDialog.this.dismiss();
                }else
                    Utils.displayMessage(err.toString(),v.getContext());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
        cancel.setOnClickListener((View v)->{
            ConnSettingDialog.this.dismiss();
        });

        mStore_name.setOnClickListener((View v)->{
            queryStoreInfo();
        });

        mStore_name.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus){
                queryStoreInfo();
            }
        });

        WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
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

    private static class Myhandler extends Handler {
        private WeakReference<ConnSettingDialog> weakConnSettingDialog;
        private Myhandler(ConnSettingDialog dialog){
            this.weakConnSettingDialog = new WeakReference<>(dialog);
        }
        public void handleMessage(Message msg){
            ConnSettingDialog settingDialog = weakConnSettingDialog.get();
            if (settingDialog == null)return;
            if (settingDialog.mDialog !=null)settingDialog.mDialog.dismiss();
            switch (msg.what){
                case 0:
                    if (msg.obj != null)
                        Utils.displayErrorMessage(msg.obj.toString(), settingDialog.mContext);
                    break;
                case 1:
                    if (msg.obj != null)
                        Utils.displayMessage(msg.obj.toString(),"确定", settingDialog.mContext);
                    break;
                case 2://查询门店信息正确,如果是云数据库，则要在线请求仓库信息
                    JSONArray shop_list = (JSONArray)msg.obj;
                    settingDialog.mPopupWindow.initContent(null,settingDialog.mStore_name,shop_list,new String[]{"stores_name"},2,true,(JSONObject json)->{
                        settingDialog.mStoreInfo = json;
                    });
                    settingDialog.mPopupWindow.show(settingDialog.mStore_name,2);
                    break;
            }

        }
    }

    private void queryStoreInfo(){
        mDialog.setTitle("正在查询门店信息...").show();
        AsyncTask.execute(()->{
            String  url = mUrl.getText() + "/api/scale/get_stores",sz_param;
            JSONObject object = new JSONObject(),retJson,info_json;

            try {
                object.put("appid",mAppId.getText());

                sz_param = Utils.jsonToMd5_hz(object,mAppscret.getText().toString());
                retJson = HttpRequest.sendPost(url,sz_param,true);

                Logger.json(retJson.toString());

                switch (retJson.optInt("flag")) {
                    case 0:
                        mHandler.obtainMessage(0,retJson.optString("info")).sendToTarget();
                        break;
                    case 1:
                        info_json = new JSONObject(retJson.optString("info"));
                        JSONArray shop_list = info_json.getJSONArray("data");
                        Logger.json(shop_list.toString());
                        mHandler.obtainMessage(2,shop_list).sendToTarget();
                        break;
                }
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
                mHandler.obtainMessage(0,e.getMessage()).sendToTarget();
            }
        });
    }

    public void showConnParam(){
        JSONObject param = new JSONObject();
        if(SQLiteHelper.getLocalParameter("connParam",param)){

            Logger.json(param.toString());

            mUrl.setText(param.optString("server_url"));
            mAppId.setText(param.optString("appId"));
            mAppscret.setText(param.optString("appScret"));
            JSONObject store_info;
            try {
                store_info = new JSONObject(param.optString("storeInfo"));
                mStore_name.setText(store_info.optString("stores_name"));
            } catch (JSONException e) {
                e.printStackTrace();
                Utils.displayErrorMessage("显示门店信息错误：" + e.getMessage(),mContext);
            }
        }else{
            try {
                Utils.displayMessage(param.getString("info"),mContext);
            } catch (JSONException e) {
                Utils.displayErrorMessage(e.getMessage(),mContext);
                e.printStackTrace();
            }
        }
    }

}