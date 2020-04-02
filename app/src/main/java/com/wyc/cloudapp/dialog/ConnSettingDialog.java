package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;

import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
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

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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

        setContentView(this.getLayoutInflater().inflate(R.layout.con_param_setting_dialog_layout, null));
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

        mDialog = new CustomProgressDialog(mContext);
        mPopupWindow = new CustomePopupWindow(mContext);
        mHandler = new Myhandler(this);

        mUrl.setSelectAllOnFocus(true);
        mAppscret.setSelectAllOnFocus(true);
        mAppId.setSelectAllOnFocus(true);

        save.setOnClickListener((View v)->{
            JSONObject json = new JSONObject(),param = new JSONObject();
            try {
                verifyUrl();
                json.put("server_url",mUrl.getText());
                json.put("appId",mAppId.getText());
                json.put("appScret",mAppscret.getText());
                if (Utils.JsonIsNotEmpty(mStoreInfo)){
                    json.put("storeInfo",mStoreInfo.toString());
                    param.put("parameter_id","connParam");
                    param.put("parameter_content",json);
                    param.put("parameter_desc","门店信息、服务器连接参数");
                    StringBuilder err = new StringBuilder();
                    if (SQLiteHelper.saveToDatabaseFormJson(param,"local_parameter",null,"REPLACE",err)){
                        MyDialog.ToastMessage("保存成功！",mContext,null);
                        ConnSettingDialog.this.dismiss();
                    }else
                        MyDialog.displayMessage(null,err.toString(),v.getContext());
                }else{
                    MyDialog.SnackbarMessage(getWindow(),"门店不能为空！",getCurrentFocus());
                }
            } catch (JSONException e) {
                MyDialog.SnackbarMessage(getWindow(),"保存错误：" + e.getMessage(),getCurrentFocus());
                e.printStackTrace();
            }
        });
        cancel.setOnClickListener((View v)->{
            ConnSettingDialog.this.dismiss();
        });

        mUrl.setOnFocusChangeListener((v,hasFocus)->{
            if (!hasFocus){
                verifyUrl();
            }
        });

        mStore_name.setOnClickListener((View v)->{
                queryStoreInfo();
        });

        mStore_name.setOnFocusChangeListener((v, hasFocus) -> {
            Utils.hideKeyBoard((EditText)v);
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
                    if (msg.obj instanceof  JSONArray){
                        settingDialog.mPopupWindow.initContent(null,settingDialog.mStore_name,(JSONArray)msg.obj,new String[]{"stores_name"},2,true,(JSONObject json)->{
                            settingDialog.mStoreInfo = json;
                        });
                        settingDialog.mPopupWindow.show(settingDialog.mStore_name,3);
                    }
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
                switch (retJson.optInt("flag")) {
                    case 0:
                        mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,retJson.optString("info")).sendToTarget();
                        break;
                    case 1:
                        info_json = new JSONObject(retJson.optString("info"));
                        switch (info_json.getString("status")){
                            case "n":
                                mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,info_json.optString("info")).sendToTarget();
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
        JSONObject param = new JSONObject();
        if(SQLiteHelper.getLocalParameter("connParam",param)){
            if (Utils.JsonIsNotEmpty(param)){
                try {
                    mUrl.setText(param.getString("server_url"));
                    mAppId.setText(param.getString("appId"));
                    mAppscret.setText(param.getString("appScret"));
                    mStoreInfo = new JSONObject(param.getString("storeInfo"));
                    mStore_name.setText(mStoreInfo.getString("stores_name"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    MyDialog.ToastMessage("显示门店信息错误：" + e.getMessage(),mContext,null);
                }
            }
        }else{
            MyDialog.SnackbarMessage(getWindow(),param.optString("info"),getCurrentFocus());
        }
    }

    private void verifyUrl(){
        String url = mUrl.getText().toString();
        if (!url.isEmpty() && !url.contains("http")){
            url = "http://" + url;
            mUrl.setText(url);
        }
    }

    public String getUrl(){
        return mUrl.getText().toString();
    }

}