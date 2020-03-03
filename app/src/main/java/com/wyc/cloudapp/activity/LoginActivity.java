package com.wyc.cloudapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import android.widget.RelativeLayout;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.ConnSettingDialog;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.keyboard.SoftKeyBoardListener;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_STORAGE_PERMISSIONS  = 800;
    private RelativeLayout mMain;
    private EditText mUser_id,mPassword;
    private Handler myHandler;
    private LoginActivity mLogin;
    private CustomProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button b_login,b_cancel;
        View b_setup;

        mMain = findViewById(R.id.main);
        mUser_id = findViewById(R.id.user_id);
        mPassword = findViewById(R.id.password);

        myHandler = new Myhandler(this);
        mLogin = this;
        mProgressDialog = new CustomProgressDialog(this,R.style.CustomDialog);

        //局部变量
        b_login = findViewById(R.id.b_login);
        b_cancel = findViewById(R.id.cancel);
        b_setup = findViewById(R.id.setup_ico);

        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            ViewGroup.LayoutParams mLayoutParams = mMain.getLayoutParams();
            @Override
            public void keyBoardShow(int height) {
                WindowManager m = (WindowManager)mLogin.getSystemService(WINDOW_SERVICE);
                if (m != null){
                    Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
                    Point point = new Point();
                    d.getSize(point);
                    mLayoutParams.height = point.y - height;
                   mMain.setLayoutParams(mLayoutParams);
                }
            }

            @Override
            public void keyBoardHide(int height) {
                mLayoutParams.height = mLayoutParams.height + height;
                mMain.setLayoutParams(mLayoutParams);
            }
        });

        mUser_id.setSelectAllOnFocus(true);
        mPassword.setSelectAllOnFocus(true);

        b_login.setOnClickListener((View v)->{
            login();
        });

        b_cancel.setOnClickListener((View V)->{
            MyDialog dialog = new MyDialog(mLogin);
            dialog.setMessage("是否取消登录？").setYesOnclickListener("是",(MyDialog mydialog)->{
                mLogin.finish();
                mydialog.dismiss();
            }).setNoOnclickListener("否",MyDialog::dismiss).show();

        });

        b_setup.setOnClickListener((View v)->{
            ConnSettingDialog dialog = new ConnSettingDialog(v.getContext());
            dialog.show();
        });

    }

    @Override
    public void onResume(){
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))){
                MyDialog dialogTmp = new MyDialog(this);
                dialogTmp.setTitle("提示信息").setMessage("APP不能存储数据,请设置允许APP读写手机存储权限").setNoOnclickListener("取消", new MyDialog.onNoOnclickListener() {
                    @Override
                    public void onNoClick(MyDialog myDialog) {
                        myDialog.dismiss();
                    }
                }).show();
            }else {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_STORAGE_PERMISSIONS );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull  int[]  grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private void login(){
        mProgressDialog.setTitle("正在登录...").setCancel(true).show();
        mProgressDialog.setOnCancelListener(dialog -> {
            MyDialog d = new MyDialog(mLogin);
            d.setMessage("是否取消登录？").setYesOnclickListener("是",(MyDialog mydialog)->{
                mLogin.finish();
                mydialog.dismiss();
            }).setNoOnclickListener("否",MyDialog::dismiss).show();
        });
        AsyncTask.execute(()->{
            JSONObject object = new JSONObject(),param_json = new JSONObject(),cashier_json = new JSONObject(),retJson,info_json,jsonLogin,store_info;
            String url,login_url,pos_url,app_id,appscret,sz_param;
            if (SQLiteHelper.getLocalParameter("connParam",param_json)){
                try {
                    url = param_json.getString("server_url");
                    app_id = param_json.getString("appId");
                    appscret = param_json.getString("appScret");

                    object.put("appid",app_id);
                    object.put("cas_account",mUser_id.getText());
                    object.put("cas_pwd",mPassword.getText());

                    sz_param = Utils.jsonToMd5_hz(object,appscret);

                    login_url = url  + "/api/cashier/login";
                    retJson = HttpRequest.sendPost(login_url,sz_param,true);

                    switch (retJson.optInt("flag")) {
                        case 0:
                            myHandler.obtainMessage(0,retJson.optString("info")).sendToTarget();
                            break;
                        case 1:
                            info_json = new JSONObject(retJson.getString("info"));
                            switch (info_json.getString("status")){
                                case "n":
                                    myHandler.obtainMessage(0,"登录失败：" + info_json.getString("info")).sendToTarget();
                                    break;
                                case "y":
                                    cashier_json = new JSONObject(info_json.getString("cashier"));
                                    store_info = new JSONObject(param_json.getString("storeInfo"));

                                    pos_url =  url + "/api/cashier/set_ps";
                                    jsonLogin = new JSONObject();
                                    jsonLogin.put("appid",app_id);
                                    jsonLogin.put("pos_code",Utils.getDeviceId(mLogin));
                                    jsonLogin.put("pos_name",Utils.getDeviceId(mLogin));
                                    jsonLogin.put("stores_id",store_info.getString("stores_id"));
                                    sz_param = Utils.jsonToMd5_hz(jsonLogin,appscret);
                                    retJson = HttpRequest.sendPost(pos_url,sz_param,true);

                                    switch (retJson.getInt("flag")) {
                                        case 0:
                                            myHandler.obtainMessage(0,"设置收银终端错误：" + retJson.optString("info")).sendToTarget();
                                            break;
                                        case 1:
                                            info_json = new JSONObject(retJson.getString("info"));
                                            switch (info_json.getString("status")) {
                                                case "n":
                                                    myHandler.obtainMessage(0,"设置收银终端错误：" + info_json.optString("info")).sendToTarget();
                                                    break;
                                                case "y":
                                                    cashier_json.put("pos_num", info_json.getString("pos_num"));
                                                    myHandler.obtainMessage(1,cashier_json).sendToTarget();
                                                    break;
                                            }
                                    }
                                    break;
                            }
                            break;
                    }
                } catch (JSONException | UnsupportedEncodingException e) {
                    e.printStackTrace();
                    myHandler.obtainMessage(0,e.getMessage()).sendToTarget();
                }
            }else {
                myHandler.obtainMessage(0,param_json.optString("info")).sendToTarget();
            }
        });
    }

    private static class Myhandler extends Handler {
        private WeakReference<LoginActivity> weakHandler;
        private Myhandler(LoginActivity loginActivity){
            this.weakHandler = new WeakReference<LoginActivity>(loginActivity);
        }
        public void handleMessage(Message msg){
            LoginActivity activity = weakHandler.get();
            if (null == activity)return;
            if (activity.mProgressDialog != null)activity.mProgressDialog.dismiss();
            switch (msg.what){
                case 0:
                    if (msg.obj != null)
                        Utils.displayErrorMessage(msg.obj.toString(),activity);
                    break;
                case 1://登录成功
                    JSONObject cashier_json = (JSONObject) msg.obj,param_json = new JSONObject();
                    StringBuilder err = new StringBuilder();
                    try {
                        param_json.put("parameter_id","cashierInfo");
                        param_json.put("parameter_content",cashier_json);
                        if (SQLiteHelper.replaceJson(param_json,"local_parameter",null,err)){
                            activity.mProgressDialog.dismiss();
                            Intent intent = new Intent(activity.mLogin,MainActivity.class);
                            activity.startActivity(intent);
                            activity.mLogin.finish();
                        }else{
                            Utils.displayMessage("保存收银员信息错误：" + err,activity);
                        }
                    } catch (JSONException e) {
                        Utils.displayMessage("保存收银员信息错误：" + e.getMessage(),activity);
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

}
