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
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.callback.PasswordEditTextReplacement;
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

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
public class LoginActivity extends AppCompatActivity {
    public static final String IMG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzYunPos/goods_img/";

    private static final int REQUEST_STORAGE_PERMISSIONS  = 800;
    private EditText mUser_id,mPassword;
    private Handler myHandler;
    private LoginActivity mSelf;
    private CustomProgressDialog mProgressDialog;
    private MyDialog myDialog;
    private SyncManagement mSyncManagement;
    private Button mCancelBtn,mLoginBtn;
    private String mAppId,mAppScret,mUrl,mPosNum,mOperId,mStoresId;
    private Future<?> mLoginTask;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //初始化成员变量
        myHandler = new Myhandler(this);
        mSelf = this;
        mProgressDialog = new CustomProgressDialog(this);
        myDialog = new MyDialog(this);

        initCloseMainWindow();
        initLoginBtn();
        initUserId();
        initPassword();
        initSetup();

        //初始化数字键盘
        initKeyboard();
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        clearResource();
    }

    @Override
    public void onBackPressed(){
        mCancelBtn.callOnClick();
    }

    @Override
    public void onResume(){
        super.onResume();
        checkSelfPermissionAndInitDb();
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        Logger.d("Action:%s",intent.getAction());
        //if activity called finish method ,onNewIntent will not be called;
    }

    @Override
    protected void finalize(){
        Logger.d("LoginActivity finalize");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull  int[]  grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SQLiteHelper.initDb(this);
                initGoodsImgDirectory();
                //显示商户域名
                show_url();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initLoginBtn(){
        final Button login_btn = findViewById(R.id.b_login);
        if (null != login_btn)
            login_btn.setOnClickListener((View v)-> {
                login();
            });
        mLoginBtn = login_btn;
    }
    private void initCloseMainWindow(){
        mCancelBtn = findViewById(R.id.cancel);
        if (null != mCancelBtn)
            mCancelBtn.setOnClickListener((View V)->{
                MyDialog.displayAskMessage(myDialog,"是否退出？", mSelf, myDialog -> {
                    mSelf.finish();
                    myDialog.dismiss();
                }, Dialog::dismiss);

            });
    }
    private void initSetup(){
        View setup = findViewById(R.id.setup_ico);
        if (null != setup)
            setup.setOnClickListener((View v)->{
                ConnSettingDialog connSettingDialog = new ConnSettingDialog(mSelf,mSelf.getString(R.string.conn_dialog_title_sz));
                connSettingDialog.setOnDismissListener(dialog -> {
                    final EditText et_url = findViewById(R.id._url_text);
                    final String url = connSettingDialog.getShopid();
                    if (url.length() != 0){
                        et_url.setText(url.substring(url.lastIndexOf('/') + 1));
                    }
                });
                connSettingDialog.show();
            });
    }
    private void initPassword(){
        final EditText password  = findViewById(R.id.password);
        password.setTransformationMethod(new PasswordEditTextReplacement());
        password.setOnFocusChangeListener((v, hasFocus) -> Utils.hideKeyBoard((EditText)v));
        password.setSelectAllOnFocus(true);
        password.setOnKeyListener((view, i, keyEvent) -> {
            int keyCode = keyEvent.getKeyCode();
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                if (mLoginBtn != null)mLoginBtn.callOnClick();
                return true;
            }
            return false;
        });
        mPassword = password;
    }
    private void initUserId(){
        final EditText user_id = findViewById(R.id.user_id);
        user_id.setOnFocusChangeListener((v, hasFocus) -> Utils.hideKeyBoard((EditText)v));
        user_id.postDelayed(user_id::requestFocus,300);
        user_id.setSelectAllOnFocus(true);
        user_id.setOnKeyListener((view, i, keyEvent) -> {
            int keyCode = keyEvent.getKeyCode();
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                if (mLoginBtn != null)mLoginBtn.callOnClick();
                return false;
            }
            if (keyCode == KeyEvent.KEYCODE_TAB){
                if (mPassword != null)mPassword.requestFocus();
                return true;
            }
            return false;
        });
        mUser_id = user_id;
    }
    @SuppressWarnings("unused")
    private void initSoftKeyBoardListener(){
        final RelativeLayout main_window = findViewById(R.id.main);;
        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            ViewGroup.LayoutParams mLayoutParams = main_window.getLayoutParams();
            @Override
            public void keyBoardShow(int height) {
                WindowManager m = (WindowManager) mSelf.getSystemService(WINDOW_SERVICE);
                if (m != null){
                    Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
                    Point point = new Point();
                    d.getSize(point);
                    mLayoutParams.height = point.y - height;
                    main_window.setLayoutParams(mLayoutParams);
                }
            }

            @Override
            public void keyBoardHide(int height) {
                mLayoutParams.height = mLayoutParams.height + height;
                main_window.setLayoutParams(mLayoutParams);
            }
        });
    }
    private void clearResource(){
        if (mSyncManagement != null) mSyncManagement.quit();
        if (myHandler != null)myHandler.removeCallbacksAndMessages(null);
        if (mProgressDialog.isShowing())mProgressDialog.dismiss();
        if (myDialog.isShowing())myDialog.dismiss();
    }
    private void checkSelfPermissionAndInitDb(){
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
            initDbAndImgDirectory();
            //显示商户域名
            show_url();
        }
    }
    private void initDbAndImgDirectory(){
        SQLiteHelper.initDb(this);
        initGoodsImgDirectory();
    }
    private void initKeyboard(){
        final ConstraintLayout keyboard_linear_layout = findViewById(R.id.keyboard);
        if (null != keyboard_linear_layout)
            for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
                View tmp_v = keyboard_linear_layout.getChildAt(i);
                tmp_v.setOnClickListener(mKeyboardListener);
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
            final Editable editable = et_view.getText();
            if (v_id == R.id._clear){
                editable.clear();
            }else if (v_id == R.id._back){
                int index = et_view.getSelectionStart(),end = et_view.getSelectionEnd();
                if (index !=end && end == editable.length()){
                    editable.clear();
                }else{
                    if (index != 0 && editable.length() != 0)
                        editable.delete(editable.length() - 1,editable.length());
                }
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
            JSONObject object = new JSONObject(), conn_param = new JSONObject(), cashier_json, retJson, info_json, jsonLogin, store_info;
            String url, sz_param, err_info;
            if (SQLiteHelper.getLocalParameter("connParam", conn_param)) {
                if (Utils.JsonIsNotEmpty(conn_param)) {
                    try {
                        mUrl = conn_param.getString("server_url");
                        mAppId = conn_param.getString("appId");
                        mAppScret = conn_param.getString("appScret");
                        mOperId = mUser_id.getText().toString();

                        object.put("appid", mAppId);
                        object.put("cas_account", mOperId);
                        object.put("cas_pwd", mPassword.getText());

                        sz_param = HttpRequest.generate_request_parm(object, mAppScret);

                        url = mUrl + "/api/cashier/login";

                        retJson = httpRequest.setConnTimeOut(3000).setReadTimeOut(3000).sendPost(url, sz_param, true);

                        switch (retJson.getIntValue("flag")) {
                            case 0:
                                int rsCode = Utils.getNotKeyAsNumberDefault(retJson,"rsCode",-1);
                                if (rsCode == 400 || rsCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
                                    myHandler.obtainMessage(MessageID.OFF_LINE_LOGIN_ID).sendToTarget();
                                }else
                                    myHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID, retJson.getString("info")).sendToTarget();
                                break;
                            case 1:
                                info_json = JSON.parseObject(retJson.getString("info"));
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
                                        store_info = JSON.parseObject(info_json.getString("shop_info"));
                                        mStoresId = Utils.getNullStringAsEmpty(store_info,"stores_id");

                                        cashier_json = JSON.parseObject(info_json.getString("cashier"));

                                        url = mUrl + "/api_v2/pos/set_ps";
                                        jsonLogin = new JSONObject();
                                        jsonLogin.put("appid", mAppId);
                                        jsonLogin.put("pos_code", Utils.getDeviceId(mSelf));
                                        jsonLogin.put("pos_name", Utils.getDeviceId(mSelf));
                                        jsonLogin.put("stores_id", mStoresId);
                                        sz_param = HttpRequest.generate_request_parm(jsonLogin, mAppScret);
                                        retJson = httpRequest.sendPost(url, sz_param, true);
                                        switch (retJson.getIntValue("flag")) {
                                            case 0:
                                                myHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID, "设置收银终端错误：" + retJson.getString("info")).sendToTarget();
                                                break;
                                            case 1:
                                                info_json = JSON.parseObject(retJson.getString("info"));
                                                switch (info_json.getString("status")) {
                                                    case "n":
                                                        myHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID, "设置收银终端错误：" + info_json.getString("info")).sendToTarget();
                                                        break;
                                                    case "y":
                                                        final StringBuilder err = new StringBuilder();
                                                        final JSONArray params = new JSONArray();

                                                        conn_param.put("storeInfo",store_info);
                                                        JSONObject _json = new JSONObject();
                                                        _json.put("parameter_id","connParam");
                                                        _json.put("parameter_content",conn_param);
                                                        _json.put("parameter_desc","门店信息、服务器连接参数");
                                                        params.add(_json);

                                                        _json = new JSONObject();
                                                        _json.put("parameter_id","scale_setting");
                                                        _json.put("parameter_content",info_json.getJSONObject("scale"));
                                                        _json.put("parameter_desc","条码秤参数信息");
                                                        params.add(_json);

                                                        cashier_json.put("pos_num", (mPosNum = info_json.getString("pos_num")));
                                                        _json = new JSONObject();
                                                        _json.put("parameter_id","cashierInfo");
                                                        _json.put("parameter_content",cashier_json);
                                                        _json.put("parameter_desc","收银员信息");
                                                        params.add(_json);

                                                        if (SQLiteHelper.execSQLByBatchFromJson(params,"local_parameter",null,err,1)){
                                                            myHandler.obtainMessage(MessageID.LOGIN_OK_ID).sendToTarget();
                                                        }else {
                                                            myHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID, "保存当前收银参数错误：" + err).sendToTarget();
                                                        }
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
                myHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID, conn_param.getString("info")).sendToTarget();
            }
        });
    }
    private void show_url(){
        final EditText et_url = findViewById(R.id._url_text);
        if (et_url != null){
            if(et_url.getText().length() != 0)return;
            JSONObject param = new JSONObject();
            if(SQLiteHelper.getLocalParameter("connParam",param)){
                if (Utils.JsonIsNotEmpty(param)){
                    et_url.setText(param.getString("shop_id"));
                }
            }else{
                MyDialog.ToastMessage(param.getString("info"),this,getWindow());
            }
        }
    }

    private static class Myhandler extends Handler {
        private WeakReference<LoginActivity> weakHandler;
        private Myhandler(LoginActivity loginActivity){
            this.weakHandler = new WeakReference<>(loginActivity);
        }
        private void launchLogin(LoginActivity activity,boolean isConnection){
            final Intent intent = new Intent(activity,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("network",isConnection);
            activity.startActivity(intent);
            activity.finish();
        }

        public void handleMessage(@NonNull Message msg){
            final LoginActivity activity = weakHandler.get();
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
                    launchLogin(activity,true);
                    break;
                case MessageID.LOGIN_OK_ID://登录成功
                    activity.mSyncManagement = new SyncManagement(this,activity.mUrl,activity.mAppId,activity.mAppScret,activity.mStoresId,activity.mPosNum,activity.mOperId);
                    activity.mSyncManagement.start_sync(true);
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
                case MessageID.OFF_LINE_LOGIN_ID:
                    MyDialog.displayAskMessage(activity.myDialog, "连接服务器失败，是否离线登录？", activity, new MyDialog.onYesOnclickListener() {
                        @Override
                        public void onYesClick(MyDialog myDialog) {
                            myDialog.dismiss();
                            final String user_id = activity.mUser_id.getText().toString(),password = activity.mPassword.getText().toString();
                            final String local_password = Utils.getUserIdAndPasswordCombinationOfMD5(user_id + password);
                            final StringBuilder err = new StringBuilder();
                            JSONObject param_obj = new JSONObject();
                            if (SQLiteHelper.getLocalParameter("connParam",param_obj)){
                                param_obj = Utils.getNullObjectAsEmptyJson(param_obj,"storeInfo");
                                final String stroesid = param_obj.getString("stores_id");
                                final String sz_count = SQLiteHelper.getString("SELECT count(cas_id) count FROM cashier_info where " +
                                        "cas_account = '"+ user_id +"' and stores_id = '" + stroesid +"' and cas_pwd = '"+ local_password +"'",err);

                                Logger.d("SELECT count(cas_id) count FROM cashier_info where " +
                                        "cas_account = '"+ user_id +"' and stores_id = '" + stroesid +"' and cas_pwd = '"+ local_password +"'");

                                if (Integer.valueOf(sz_count) > 0){
                                    launchLogin(activity,false);
                                }else {
                                    activity.myHandler.obtainMessage(MessageID.LOGIN_ID_ERROR_ID, "不存在此用户！").sendToTarget();
                                }
                            }else {
                                MyDialog.displayErrorMessage(activity.myDialog,"查询连接参数错误:" + param_obj.getString("info"),activity);
                            }
                        }
                    }, MyDialog::dismiss);
                    break;
            }
        }
    }

    public void initGoodsImgDirectory(){
        final File file = new File(IMG_PATH);
        if (!file.exists()){
            if (!file.mkdir()){
                MyDialog.ToastMessage("初始化商品图片目录错误！",this,null);
            }
        }
    }
}
