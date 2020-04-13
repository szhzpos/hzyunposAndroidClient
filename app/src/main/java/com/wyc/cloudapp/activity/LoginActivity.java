package com.wyc.cloudapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.ReplacementTransformationMethod;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import android.widget.RelativeLayout;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.ConnSettingDialog;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.keyboard.SoftKeyBoardListener;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.network.sync.SyncManagement;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
public class LoginActivity extends AppCompatActivity {
    public static final String IMG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzYunPos/goods_img/";
    private static final int REQUEST_STORAGE_PERMISSIONS  = 800;
    private RelativeLayout mMain;
    private EditText mUser_id,mPassword;
    private Handler myHandler;
    private LoginActivity mSelf;
    private CustomProgressDialog mProgressDialog;
    private MyDialog myDialog;
    private SyncManagement mSyncManagement;
    private Button mCancel;
    private String mAppId,mAppScret,mUrl,mPosNum,mOperId,mStoresId;
    private Future<?> mLoginTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mMain = findViewById(R.id.main);
        mUser_id = findViewById(R.id.user_id);
        mPassword = findViewById(R.id.password);
        mCancel = findViewById(R.id.cancel);

        myHandler = new Myhandler(this);
        mSelf = this;
        mProgressDialog = new CustomProgressDialog(this);
        myDialog = new MyDialog(this);

        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            ViewGroup.LayoutParams mLayoutParams = mMain.getLayoutParams();
            @Override
            public void keyBoardShow(int height) {
                WindowManager m = (WindowManager) mSelf.getSystemService(WINDOW_SERVICE);
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

        mUser_id.setOnFocusChangeListener((v, hasFocus) -> Utils.hideKeyBoard((EditText)v));
        mUser_id.postDelayed(()->mUser_id.requestFocus(),300);
        mUser_id.setSelectAllOnFocus(true);

        mPassword.setTransformationMethod(new ReplacementTransformationMethod() {
            @Override
            protected char[] getOriginal() {
                StringBuilder strWord = new StringBuilder();
                for (char i = 0; i < 256; i++) {
                    strWord.append(i);
                }
                return strWord.toString().toCharArray();
            }

            @Override
            protected char[] getReplacement() {
                char[] charReplacement = new char[255];
                for (int i = 0; i < 255; i++) {
                    charReplacement[i] = '*';
                }
                return charReplacement;
            }
        });
        mPassword.setOnFocusChangeListener((v, hasFocus) -> Utils.hideKeyBoard((EditText)v));
        mPassword.setSelectAllOnFocus(true);

        findViewById(R.id.b_login).setOnClickListener((View v)->{
            login();
        });

        mCancel.setOnClickListener((View V)->{
            MyDialog.displayAskMessage(myDialog,"是否退出？", mSelf, myDialog -> {
                mSelf.finish();
                myDialog.dismiss();
            }, Dialog::dismiss);

        });

        findViewById(R.id.setup_ico).setOnClickListener((View v)->{
            ConnSettingDialog connSettingDialog = new ConnSettingDialog(v.getContext());
            connSettingDialog.setOnDismissListener(dialog -> {
                EditText et_url = findViewById(R.id._url_text);
                String url = connSettingDialog.getUrl();
                if (url.length() != 0){
                    et_url.setText(url.substring(url.lastIndexOf('/') + 1));
                }
            });
            connSettingDialog.show();
        });

        //初始化数字键盘
        ConstraintLayout keyboard_linear_layout;
        keyboard_linear_layout = findViewById(R.id.keyboard);
        for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
            View tmp_v = keyboard_linear_layout.getChildAt(i);
            tmp_v.setOnClickListener(mKeyboardListener);
        }
    }

    @Override
    public void onStart(){
        super.onStart();

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if (mSyncManagement != null) mSyncManagement.quit();
        if (myHandler != null)myHandler.removeCallbacksAndMessages(null);
        if (mProgressDialog.isShowing())mProgressDialog.dismiss();
        if (myDialog.isShowing())myDialog.dismiss();
    }

    @Override
    public void onBackPressed(){
        mCancel.callOnClick();
    }

    @Override
    public void onResume(){
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))){
                myDialog.setTitle("提示信息").setMessage("APP不能存储数据,请设置允许APP读写手机存储权限").setNoOnclickListener("退出", myDialog -> {
                    myDialog.dismiss();
                    LoginActivity.this.finish();
                }).setYesOnclickListener("重新获取",(MyDialog myDialog)->{
                    myDialog.dismiss();
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_STORAGE_PERMISSIONS );
                }).show();
            }else {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_STORAGE_PERMISSIONS );
            }
        }else{
            SQLiteHelper.initDb(this);
            initGoodsImgDirectory();
            //显示商户域名
            show_url();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull  int[]  grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    SQLiteHelper.initDb(this);
                    initGoodsImgDirectory();
                    //显示商户域名
                    show_url();
                } else {

                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    private View.OnClickListener mKeyboardListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int v_id = view.getId();
            EditText et_view = (EditText) getCurrentFocus();
            if (null == et_view){
                et_view = mUser_id;
                mUser_id.requestFocus();
            }
            Editable editable = et_view.getText();
            if (v_id == R.id._ok){
                editable.clear();
            }else if (v_id == R.id._back){
                if (editable.length() != 0)
                    editable.delete(editable.length() - 1,editable.length());
            }else{
                if (et_view.getSelectionStart() != et_view.getSelectionEnd()){
                    editable.replace(0,editable.length(),((Button)view).getText());
                    et_view.setSelection(editable.length());
                }else
                    editable.append(((Button)view).getText());
            }
        }
    };
    private void login(){
        final HttpRequest httpRequest = new HttpRequest();
        mProgressDialog.setCancel(false).setMessage("正在登录...").refreshMessage().show();
        myHandler.postDelayed(()->{
            if (mProgressDialog.isShowing())
                mProgressDialog.setCancel(true).setOnCancelListener(dialog -> {
                    if (mSyncManagement != null)mSyncManagement.pauseSync();
                    MyDialog.displayAskMessage(myDialog,"是否取消登录？",mSelf,(MyDialog mydialog)->{
                        mydialog.dismiss();
                        if (mSyncManagement != null){
                            mSyncManagement.continueSync();
                            mSyncManagement.stop_sync();
                        }
                        httpRequest.clearConnection(HttpRequest.CLOSEMODE.POST);
                        mProgressDialog.setMessage("正在取消登录...").refreshMessage().show();
                        CustomApplication.execute(()->{
                            if (mLoginTask != null){
                                try {
                                    mLoginTask.get();
                                    myHandler.sendMessageAtFrontOfQueue(myHandler.obtainMessage(MessageID.CANCEL_LOGIN_ID));
                                } catch (ExecutionException | CancellationException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    },(MyDialog myDialog)->{
                        myDialog.dismiss();
                        if (mSyncManagement != null)mSyncManagement.continueSync();
                        if (mProgressDialog != null && !mProgressDialog.isShowing()){
                            mProgressDialog.setRestShowTime(false).show();
                        }
                    });
                });
        },1000);
        mLoginTask = CustomApplication.submit(()-> {
            JSONObject object = new JSONObject(), param_json = new JSONObject(), cashier_json, retJson, info_json, jsonLogin, store_info;
            String url, sz_param, err_info;
            if (SQLiteHelper.getLocalParameter("connParam", param_json)) {
                if (Utils.JsonIsNotEmpty(param_json)) {
                    try {
                        mUrl = param_json.getString("server_url");
                        mAppId = param_json.getString("appId");
                        mAppScret = param_json.getString("appScret");
                        mOperId = mUser_id.getText().toString();

                        object.put("appid", mAppId);
                        object.put("cas_account", mOperId);
                        object.put("cas_pwd", mPassword.getText());

                        sz_param = HttpRequest.generate_request_parm(object, mAppScret);

                        url = mUrl + "/api/cashier/login";

                        retJson = httpRequest.setReadTimeOut(3000).setConnTimeOut(3000).sendPost(url, sz_param, true);

                        switch (retJson.optInt("flag")) {
                            case 0:
                                myHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID, retJson.optString("info")).sendToTarget();
                                break;
                            case 1:
                                info_json = new JSONObject(retJson.getString("info"));
                                switch (info_json.getString("status")) {
                                    case "n":
                                        err_info = info_json.getString("info");
                                        if (err_info.contains("密码")) {
                                            myHandler.obtainMessage(MessageID.LOGIN_PW_ERROR_ID, "登录失败：" + err_info).sendToTarget();
                                        } else if (err_info.contains("账号") || err_info.contains("登录")) {
                                            myHandler.obtainMessage(MessageID.LOGIN_ID_ERROR_ID, "登录失败：" + err_info).sendToTarget();
                                        } else
                                            myHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID, "登录失败：" + err_info).sendToTarget();
                                        break;
                                    case "y":
                                        cashier_json = new JSONObject(info_json.getString("cashier"));
                                        store_info = new JSONObject(param_json.getString("storeInfo"));
                                        mStoresId = store_info.getString("stores_id");

                                        url = mUrl + "/api/cashier/set_ps";
                                        jsonLogin = new JSONObject();
                                        jsonLogin.put("appid", mAppId);
                                        jsonLogin.put("pos_code", Utils.getDeviceId(mSelf));
                                        jsonLogin.put("pos_name", Utils.getDeviceId(mSelf));
                                        jsonLogin.put("stores_id", mStoresId);
                                        sz_param = HttpRequest.generate_request_parm(jsonLogin, mAppScret);
                                        retJson = httpRequest.sendPost(url, sz_param, true);
                                        switch (retJson.getInt("flag")) {
                                            case 0:
                                                myHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID, "设置收银终端错误：" + retJson.optString("info")).sendToTarget();
                                                break;
                                            case 1:
                                                info_json = new JSONObject(retJson.getString("info"));
                                                switch (info_json.getString("status")) {
                                                    case "n":
                                                        myHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID, "设置收银终端错误：" + info_json.optString("info")).sendToTarget();
                                                        break;
                                                    case "y":
                                                        cashier_json.put("pos_num", (mPosNum = info_json.getString("pos_num")));
                                                        myHandler.obtainMessage(MessageID.LOGIN_OK_ID, cashier_json).sendToTarget();
                                                        break;
                                                }
                                        }
                                        break;
                                }
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        myHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID, e.getMessage()).sendToTarget();
                    }
                } else {
                    myHandler.obtainMessage(MessageID.CONN_PARAM_ERR_ID, "连接参数不能为空！").sendToTarget();
                }
            } else {
                myHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID, param_json.optString("info")).sendToTarget();
            }
        });
    }
    private void show_url(){
        EditText et_url = findViewById(R.id._url_text);
        if (et_url != null){
            if(et_url.getText().length() != 0)return;
            JSONObject param = new JSONObject();
            if(SQLiteHelper.getLocalParameter("connParam",param)){
                if (Utils.JsonIsNotEmpty(param)){
                    try {
                            String url = param.getString("server_url");
                            if (url.length() != 0){
                                url = url.substring(url.lastIndexOf('/') + 1);
                            }
                            et_url.setText(url);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        MyDialog.SnackbarMessage(getWindow(),"显示服务器地址：" + e.getMessage(),getCurrentFocus());
                    }
                }
            }else{
                MyDialog.SnackbarMessage(getWindow(),param.optString("info"),getCurrentFocus());
            }
        }
    }

    private static class Myhandler extends Handler {
        private WeakReference<LoginActivity> weakHandler;
        private Myhandler(LoginActivity loginActivity){
            this.weakHandler = new WeakReference<>(loginActivity);
        }
        public void handleMessage(@NonNull Message msg){
            LoginActivity activity = weakHandler.get();
            if (null == activity)return;
            if (activity.mProgressDialog.isShowing() && msg.what != MessageID.SYNC_DIS_INFO_ID)activity.mProgressDialog.dismiss();
            switch (msg.what){
                case MessageID.DIS_ERR_INFO_ID:
                case MessageID.SYNC_ERR_ID://资料同步错误
                    if (msg.obj != null){
                        MyDialog.displayErrorMessage(activity.myDialog,msg.obj.toString(),activity);
                    }
                    break;
                case MessageID.CANCEL_LOGIN_ID:
                    removeCallbacksAndMessages(null);
                    activity.finish();
                    break;
                case MessageID.SYNC_FINISH_ID://同步成功启动主界面
                    Intent intent = new Intent(activity,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    activity.startActivity(intent);
                    activity.finish();
                    break;
                case MessageID.LOGIN_OK_ID://登录成功
                    if (msg.obj instanceof  JSONObject){
                        JSONObject cashier_json = (JSONObject) msg.obj,param_json = new JSONObject();
                        StringBuilder err = new StringBuilder();
                        try {
                            param_json.put("parameter_id","cashierInfo");
                            param_json.put("parameter_content",cashier_json);
                            param_json.put("parameter_desc","收银员信息");
                            if (SQLiteHelper.saveToDatabaseFormJson(param_json,"local_parameter",null,"REPLACE",err)){
                                activity.mSyncManagement = new SyncManagement(this,activity.mUrl,activity.mAppId,activity.mAppScret,activity.mStoresId,activity.mPosNum,activity.mOperId);
                                activity.mSyncManagement.start_sync(true);
                            }else{
                                MyDialog.ToastMessage(activity.mUser_id,"保存收银员信息错误：" + err,activity,null);
                            }
                        } catch (JSONException e) {
                            MyDialog.ToastMessage(activity.mUser_id,"保存收银员信息错误：" + e.getMessage(),activity,null);
                            e.printStackTrace();
                        }
                    }else{
                          MyDialog.displayErrorMessage(null,"收银员信息为空！",activity);
                    }
                    break;
                case MessageID.LOGIN_ID_ERROR_ID://账号错误
                    activity.mUser_id.requestFocus();
                    activity.mUser_id.selectAll();
                    activity.mUser_id.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake_x));
                    if (msg.obj instanceof String)
                        MyDialog.ToastMessage(activity.mUser_id,msg.obj.toString(),activity,null);
                    break;
                case MessageID.LOGIN_PW_ERROR_ID://密码错误
                    activity.mPassword.requestFocus();
                    activity.mPassword.selectAll();
                    activity.mPassword.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake_x));
                    if (msg.obj instanceof String)
                        MyDialog.ToastMessage(activity.mPassword,msg.obj.toString(),activity,null);
                    break;
                case MessageID.SYNC_DIS_INFO_ID://资料同步进度信息
                    if (activity.mProgressDialog != null){
                        activity.mProgressDialog.setMessage(msg.obj.toString()).refreshMessage();
                        if (!activity.mProgressDialog.isShowing()) {
                            activity.mProgressDialog.show();
                        }
                    }
                    break;
                case MessageID.CONN_PARAM_ERR_ID:
                    if (msg.obj instanceof String){
                        MyDialog.SnackbarMessage(activity.getWindow(),msg.obj.toString(),activity.getCurrentFocus());
                        activity.findViewById(R.id.setup_ico).callOnClick();
                    }
                    break;
            }
        }
    }

    public void initGoodsImgDirectory(){
        File file = new File(IMG_PATH);
        if (!file.exists()){
            if (!file.mkdir()){
                MyDialog.ToastMessage("初始化商品图片目录错误！",this,null);
            }
        }
    }

}
