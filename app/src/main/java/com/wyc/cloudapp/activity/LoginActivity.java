package com.wyc.cloudapp.activity;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.MobileNavigationActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.callback.PasswordEditTextReplacement;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.ConnSettingDialog;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.service.AppUpdateService;
import com.wyc.cloudapp.constants.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LoginActivity extends BaseActivity implements CustomApplication.MessageCallback {
    private static final int REQUEST_STORAGE_PERMISSIONS  = 800;
    private EditText mUserIdEt, mPasswordEt;
    private LoginActivity mSelf;
    private CustomProgressDialog mProgressDialog;
    private Button mCancelBtn,mLoginBtn;
    private String mPosNum,mOperId,mStoresId;
    private Call mLoginCall;
    private JSONObject mConnParam;
    private boolean isSmallScreen = false;
    private final CustomApplication mApplication = CustomApplication.self();
    private boolean isFirstRequestPermissions = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //加载布局
        loadView();

        //初始化成员变量
        mSelf = this;
        mProgressDialog = new CustomProgressDialog(this);

        initCloseMainWindow();
        initLoginBtn();
        initUserId();
        initPassword();
        initSetup();

        initKeyboard();
        initDisplayInfoAndVersion();
        initSetupDisplaySize();

        mApplication.registerHandleMessage(this);

        registerReceiver(updateReceiver,new IntentFilter(AppUpdateService.APP_PROGRESS_BROADCAST));
    }

    private void loadView(){
        if ((isSmallScreen = Utils.lessThan7Inches(this))){
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) ;//显示状态栏

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.mobile_activity_login);
        }else{
            setContentView(R.layout.activity_login);
        }
    }

    public static void start(final Context context){
        final Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        clearResource();
        unregisterReceiver(updateReceiver);
    }

    @Override
    public void onBackPressed(){
        mCancelBtn.callOnClick();
    }

    @Override
    public void onResume(){
        super.onResume();
        checkSelfPermission();
        show_shop_info();
        setLastUser();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        setIntent(intent);

        Logger.d("onNewIntentAction:%s",intent.getAction());
        //if activity called finish method ,onNewIntent will not be called;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull  int[]  grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //onResume 请求存储权限，如果拒绝是无法进入软件的，如果同意用户登录之后才能打开数据库，所以此处无业务代码。
                Toast.makeText(this,"ACQUIRE WRITE_EXTERNAL_STORAGE SUCCESSFULLY",Toast.LENGTH_SHORT).show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initSetupDisplaySize(){
        final Button display_size_btn = findViewById(R.id.display_size_btn);
        if (null != display_size_btn){
            final RadioGroup display_size_group = findViewById(R.id.display_size_group);
            if (display_size_group != null){
                display_size_btn.setOnClickListener(v -> {
                    if (display_size_group.getVisibility() == View.VISIBLE){
                        display_size_group.setOnClickListener(null);
                        display_size_group.setVisibility(View.GONE);
                    }else {
                        double size = Utils.getDisplaySize(this);
                        if (Utils.equalDouble(size,8)){
                            display_size_group.check(R.id.tablet_size);
                        }else if (Utils.equalDouble(size,6)){
                            display_size_group.check(R.id.mobile_size);
                        }else {
                            display_size_group.check(R.id.auto_size);
                        }
                        display_size_group.setVisibility(View.VISIBLE);
                        display_size_group.setOnCheckedChangeListener((group, checkedId) -> {
                            final SharedPreferences preferences=getSharedPreferences("display_size", Context.MODE_PRIVATE);
                            final SharedPreferences.Editor editor= preferences.edit();
                            if (checkedId == R.id.tablet_size){
                                editor.putInt("size", 8);
                            }else if (checkedId == R.id.mobile_size){
                                editor.putInt("size", 6);
                            }else {
                                editor.putInt("size", -1);
                            }
                            editor.apply();
                            relaunch();
                        });
                    }
                });
            }
        }
    }

    private void initDisplayInfoAndVersion(){
        final TextView tv = findViewById(R.id.display_info_tv);
        if (null != tv){
            final DisplayMetrics displayMetrics = new DisplayMetrics();
            Utils.getDisplayMetrics(this,displayMetrics);
            try {
                final PackageInfo packageInfo = getPackageManager().getPackageInfo("com.wyc.cloudapp",0);
                tv.setText(String.format(Locale.CHINA,"高:%d x 宽:%d DPI:%d 版本号:%s",displayMetrics.heightPixels,displayMetrics.widthPixels,displayMetrics.densityDpi,packageInfo.versionName));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                MyDialog.ToastMessage(e.getMessage(),this,getWindow());
            }
        }
    }

    private void saveLastUser(){
        final SharedPreferences preferences=getSharedPreferences("login_user", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor= preferences.edit();
        editor.putString("user", mUserIdEt.getText().toString());
        editor.apply();
    }

    private void setLastUser(){
        final SharedPreferences preferences=getSharedPreferences("login_user", Context.MODE_PRIVATE);
        if (mUserIdEt != null){
            final String user = preferences.getString("user","");
            if ("".equals(user)){
                mUserIdEt.postDelayed(()-> mUserIdEt.requestFocus(),400);
            }else{
                mUserIdEt.setText(user);
                if (mPasswordEt != null) mPasswordEt.postDelayed(()-> mPasswordEt.requestFocus(),300);
            }

        }
    }

    private void initLoginBtn(){
        final Button login_btn = findViewById(R.id.b_login);
        if (null != login_btn)
            login_btn.setOnClickListener((View v)-> {
                saveLastUser();

                check_ver();
            });
        mLoginBtn = login_btn;
    }

    private void check_ver(){
        mProgressDialog.setCancel(false).setMessage("正在检查更新...").refreshMessage().show();
        final JSONObject conn_param = CustomApplication.getConnParam();
        if (Utils.JsonIsNotEmpty(conn_param)) {
            final String url = Utils.getNullStringAsEmpty(conn_param,"server_url"),appid = Utils.getNullStringAsEmpty(conn_param,"appId"),
                    appSecret = Utils.getNullStringAsEmpty(conn_param,"appSecret");

            final Intent intentService = new Intent(this, AppUpdateService.class);
            intentService.putExtra("url",url);
            intentService.putExtra("appid",appid);
            intentService.putExtra("appSecret",appSecret);
            startService(intentService);
            mConnParam = conn_param;
        } else {
            mProgressDialog.dismiss();
            MyDialog.ToastMessage("连接参数不能为空!",this,getWindow());
            final View view = findViewById(R.id.setup_ico);
            if (null != view)view.callOnClick();
        }
    }

    //广播监听App更新的进度
    private final BroadcastReceiver updateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final LoginActivity activity = LoginActivity.this;
            int status = intent.getIntExtra("status",0);
            switch (status){
                case AppUpdateService.SUCCESS_STATUS:
                    syncLogin();
                    break;
                case AppUpdateService.BAD_NETWORK_STATUS:
                    offline_login();
                    break;
                case AppUpdateService.PROGRESS_STATUS:
                    mProgressDialog.setMessage(String.format(Locale.CHINA,"正在更新,请稍后...%d%s",(int)(intent.getDoubleExtra("Progress",0) * 100),"%")).refreshMessage().show();
                    break;
                case AppUpdateService.INSTALL_STATUS:
                    mProgressDialog.dismiss();
                    installAPK(intent.getStringExtra("filePath"));
                    break;
                case AppUpdateService.ERROR_STATUS:
                    default:
                        mProgressDialog.dismiss();
                        MyDialog.displayErrorMessage(activity, "检查版本错误:" + intent.getStringExtra("info"));
                        break;
            }
        }
    };

    private void installAPK(final String filepath) {
        Uri data ;
        final Intent intent= new Intent(Intent.ACTION_VIEW);
        final File file =  new File(filepath);

        // 判断版本大于等于7.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // "com.wyc.cloudapp.fileprovider"即是在清单文件中配置的authorities
            data = FileProvider.getUriForFile(this, "com.wyc.cloudapp.fileprovider",file);
            // 给目标应用一个临时授权
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            data = Uri.fromFile(file);
        }
        intent.setDataAndType(data, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent,0x000000cc);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode,resultCode,intent);
        if (resultCode == RESULT_CANCELED && requestCode == 0x000000cc){
            relaunch();
        }
    }

    private void relaunch(){
        finish();
        final Intent intent_launch = new Intent(this,LoginActivity.class);
        startActivity(intent_launch);
    }

    private void initCloseMainWindow(){
        mCancelBtn = findViewById(R.id.cancel);
        if (null != mCancelBtn)
            mCancelBtn.setOnClickListener((View V)-> MyDialog.displayAskMessage(mSelf, "是否退出？", myDialog -> {
                mSelf.finish();
                myDialog.dismiss();
            }, Dialog::dismiss));
    }
    private void initSetup(){
        View setup = findViewById(R.id.setup_ico);
        if (null != setup)
            setup.setOnClickListener((View v)->{
                ConnSettingDialog connSettingDialog = new ConnSettingDialog(mSelf,mSelf.getString(R.string.conn_dialog_title_sz));
                connSettingDialog.setOnDismissListener(dialog -> show_shop_info(connSettingDialog.getShopInfo()));
                connSettingDialog.show();
            });
    }
    private void show_shop_info(final JSONObject shop_info){
        final TextView et_url = findViewById(R.id._url_text),shop_name_tv = findViewById(R.id.shop_name);
        if (et_url != null && shop_name_tv != null && shop_info != null){
            et_url.setText(Utils.getNullStringAsEmpty(shop_info,"shop_id"));
            shop_name_tv.setText(Utils.getNullStringAsEmpty(shop_info,"shop_name"));
        }
    }
    private void initPassword(){
        final EditText password  = findViewById(R.id.password);
        password.setTransformationMethod(new PasswordEditTextReplacement());
        password.setOnKeyListener((view, i, keyEvent) -> {
            int keyCode = keyEvent.getKeyCode();
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                if (mLoginBtn != null)mLoginBtn.callOnClick();
                return true;
            }
            return false;
        });
        mPasswordEt = password;
    }
    private void initUserId(){
        final EditText user_id = findViewById(R.id.user_id);
        user_id.setOnKeyListener((view, i, keyEvent) -> {
            int keyCode = keyEvent.getKeyCode();
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && keyEvent.getAction() == KeyEvent.ACTION_DOWN){
                if (mLoginBtn != null)mLoginBtn.callOnClick();
                return false;
            }
            if (keyCode == KeyEvent.KEYCODE_TAB){
                if (mPasswordEt != null) mPasswordEt.requestFocus();
                return true;
            }
            return false;
        });
        mUserIdEt = user_id;
    }

    private void clearResource(){
        if (mProgressDialog.isShowing())mProgressDialog.dismiss();
    }
    private void checkSelfPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if (!Environment.isExternalStorageManager()){
                final Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                    final MyDialog dialog = new MyDialog(mSelf, "提示信息");
                    dialog.setMessage("APP不能存储数据,请设置允许APP读写手机存储权限").setNoOnclickListener("退出", myDialog -> {
                        dialog.dismiss();
                        LoginActivity.this.finish();
                    }).setYesOnclickListener("重新获取", (MyDialog myDialog) -> {
                        myDialog.dismiss();
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSIONS);
                    }).show();
                } else {
                    if (isFirstRequestPermissions) {
                        isFirstRequestPermissions = false;
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_STORAGE_PERMISSIONS);
                    } else
                        MyDialog.displayAskMessage(this, "用户已经禁用存储读写权限，是否手动授权?", myDialog -> {
                            final Intent intent = new Intent();
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                            startActivity(intent);
                            myDialog.dismiss();
                        }, myDialog -> LoginActivity.this.finish());
                }
            }
        }
    }
    private void show_shop_info(){
        final JSONObject param = CustomApplication.getConnParam();
        if (Utils.JsonIsNotEmpty(param)){
            final JSONObject shop_info = Utils.getNullObjectAsEmptyJson(param,"storeInfo");
            shop_info.put("shop_id",Utils.getNullStringAsEmpty(param,"shop_id"));
            shop_info.put("shop_name",String.format("%s%s%s%s",Utils.getNullStringAsEmpty(shop_info,"stores_name"),"[",Utils.getNullStringAsEmpty(shop_info,"stores_id"),"]"));
            show_shop_info(shop_info);
        }
    }

    private void initKeyboard(){
        final ConstraintLayout keyboard_linear_layout = findViewById(R.id.keyboard);
        if (null != keyboard_linear_layout)
            for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
                View tmp_v = keyboard_linear_layout.getChildAt(i);
                tmp_v.setOnClickListener(mKeyboardListener);
            }
    }
    private final View.OnClickListener mKeyboardListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int v_id = view.getId();
            EditText et_view = (EditText) getCurrentFocus();
            if (null == et_view){
                et_view = mUserIdEt;
                mUserIdEt.requestFocus();
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

    private void syncLogin(){
        mProgressDialog.setCancel(false).setMessage("正在登录...").refreshMessage().show();

        JSONObject object = new JSONObject(), conn_param = mConnParam;
        final String url, sz_param, base_url = Utils.getNullStringAsEmpty(conn_param,"server_url"),appid =  Utils.getNullStringAsEmpty(conn_param,"appId"),
                appSecret = Utils.getNullStringAsEmpty(conn_param,"appSecret");
        mOperId = mUserIdEt.getText().toString();

        object.put("appid",appid);
        object.put("cas_account", mOperId);
        object.put("cas_pwd", mPasswordEt.getText());

        sz_param = HttpRequest.generate_request_parm(object,appSecret);

        url = base_url + "/api/cashier/login";

        mLoginCall = HttpUtils.sendAsyncPost(url,sz_param);
        mLoginCall.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call,@NonNull IOException e) {
                e.printStackTrace();
                CustomApplication.sendMessage(MessageID.DIS_ERR_INFO_ID,e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call,@NonNull Response response) {
                JSONObject cashier_json, info_json, jsonLogin, store_info;

                final ResponseBody responseBody = response.body();
                if (responseBody == null)return;

                try {
                    info_json = JSON.parseObject(responseBody.string());
                    switch (info_json.getString("status")) {
                        case "n":
                            final String err_info = info_json.getString("info");
                            if (err_info.contains("密码")) {
                                CustomApplication.sendMessage(MessageID.LOGIN_PW_ERROR_ID, "登录失败：" + err_info);
                            } else if (err_info.contains("账号") || err_info.contains("登录")) {
                                CustomApplication.sendMessage(MessageID.LOGIN_ID_ERROR_ID, "登录失败：" + err_info);
                            } else
                                CustomApplication.sendMessage(MessageID.DIS_ERR_INFO_ID, "登录失败：" + err_info);
                            break;
                        case "y":
                            store_info = JSON.parseObject(info_json.getString("shop_info"));
                            mStoresId = Utils.getNullStringAsEmpty(store_info,"stores_id");
                            cashier_json = JSON.parseObject(info_json.getString("cashier"));

                            if (!Utils.isNotEmpty(mStoresId)){
                                CustomApplication.sendMessage(MessageID.DIS_ERR_INFO_ID, "登录错误，门店编号不能为空!");
                                return;
                            }

                            //初始化本地数据库
                        {
                            final String id = mApplication.getStoreIdWithSharedPreferences();
                            if (!id.isEmpty() && !id.equals(mStoresId)){
                                final String message = String.format(Locale.CHINA,"本次登录的操作员【%s<%s>】所属门店/仓库【%s】，确定登录将切换前台数据库，是否登录?"
                                        ,Utils.getNullStringAsEmpty(cashier_json,"cas_name"),mOperId,Utils.getNullStringAsEmpty(store_info,"stores_name"));
                                if(MyDialog.showMessageToModalDialog(mSelf,message) == 0){
                                    mProgressDialog.dismiss();
                                    return;
                                }
                            }
                            CustomApplication.initDb(mStoresId);
                        }

                            final String set_url = base_url + "/api/pos/set_ps";
                            jsonLogin = new JSONObject();
                            jsonLogin.put("appid", appid);
                            jsonLogin.put("pos_code", Utils.getDeviceId(mSelf));
                            jsonLogin.put("pos_name", Utils.getDeviceId(mSelf));
                            jsonLogin.put("stores_id", mStoresId);

                            final String set_sz_param = HttpRequest.generate_request_parm(jsonLogin, appSecret);

                            mLoginCall.cancel();
                            mLoginCall = HttpUtils.sendAsyncPost(set_url,set_sz_param);
                            mLoginCall.enqueue(new Callback() {
                                @Override
                                public void onFailure(@NonNull Call call,@NonNull IOException e) {
                                    e.printStackTrace();
                                    CustomApplication.sendMessage(MessageID.DIS_ERR_INFO_ID,"设置收银终端错误：" + e.getMessage());
                                }

                                @Override
                                public void onResponse(@NonNull Call call,@NonNull Response response) {
                                    try (final ResponseBody set_responseBody = response.body()){
                                        if (set_responseBody != null){
                                            final JSONObject info_json = JSON.parseObject(set_responseBody.string());//Closes ResponseBody automatically

                                            switch (info_json.getString("status")) {
                                                case "n":
                                                    CustomApplication.sendMessage(MessageID.DIS_ERR_INFO_ID, "设置收银终端错误：" + info_json.getString("info"));
                                                    break;
                                                case "y":
                                                    final StringBuilder err = new StringBuilder();
                                                    final JSONArray params = new JSONArray();
                                                    mPosNum = info_json.getString("pos_num");

                                                    conn_param.put("pos_num",mPosNum);
                                                    conn_param.put("storeInfo",store_info);
                                                    CustomApplication.setConnParam(conn_param);

                                                    JSONObject _json = new JSONObject();
                                                    _json.put("parameter_id","scale_setting");
                                                    _json.put("parameter_content",info_json.getJSONObject("scale"));
                                                    _json.put("parameter_desc","条码秤参数信息");
                                                    params.add(_json);

                                                    cashier_json.put("pos_num",mPosNum);
                                                    _json = new JSONObject();
                                                    _json.put("parameter_id","cashierInfo");
                                                    _json.put("parameter_content",cashier_json);
                                                    _json.put("parameter_desc","收银员信息");
                                                    params.add(_json);

                                                    _json = new JSONObject();
                                                    _json.put("parameter_id","pos_moling");
                                                    _json.put("parameter_content",info_json.getJSONObject("pos_moling"));
                                                    _json.put("parameter_desc","收银抹零参数");
                                                    params.add(_json);

                                                    _json = new JSONObject();
                                                    _json.put("parameter_id","MEMBER_PARAMETER");
                                                    _json.put("parameter_content",info_json.getJSONObject("member_parameter"));
                                                    _json.put("parameter_desc","会员参数");
                                                    params.add(_json);

                                                    if (SQLiteHelper.execSQLByBatchFromJson(params,"local_parameter",null,err,1)){
                                                        CustomApplication.sendMessage(MessageID.LOGIN_OK_ID);
                                                    }else {
                                                        CustomApplication.sendMessage(MessageID.DIS_ERR_INFO_ID, "保存当前收银参数错误：" + err);
                                                    }
                                                    break;
                                            }
                                        }
                                    } catch (IOException | JSONException e) {
                                        e.printStackTrace();
                                        CustomApplication.sendMessage(MessageID.DIS_ERR_INFO_ID, e.getMessage());
                                    }
                                }
                            });
                            break;
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                    CustomApplication.sendMessage(MessageID.DIS_ERR_INFO_ID, e.getMessage());
                }finally {
                    response.close();
                }
            }
        });

        mProgressDialog.setCancel(true).setOnCancelListener(dialog -> {
            final CustomApplication application = CustomApplication.self();
            application.pauseSync();
            if (MyDialog.showMessageToModalDialog(mSelf,"是否取消登录？") == 1){
                application.stop_sync();
                mLoginCall.cancel();
                if (mLoginCall.isCanceled()){
                    CustomApplication.sendMessageAtFrontOfQueue(MessageID.CANCEL_LOGIN_ID);
                }
            }else {
                application.continueSync();
                if (mProgressDialog.isShowing())mProgressDialog.setRestShowTime(false).show();
            }
        });
    }

    @Override
    public void handleMessage(final Handler handler, Message msg) {
        if (mProgressDialog.isShowing() && msg.what != MessageID.SYNC_DIS_INFO_ID)mProgressDialog.dismiss();
        switch (msg.what){
            case MessageID.DIS_ERR_INFO_ID:
            case MessageID.SYNC_ERR_ID://资料同步错误
                if (msg.obj != null){
                    MyDialog.displayErrorMessage(this, msg.obj.toString());
                }
                break;
            case MessageID.CANCEL_LOGIN_ID:
                handler.removeCallbacksAndMessages(null);
                finish();
                break;
            case MessageID.SYNC_FINISH_ID://同步成功启动主界面
                launchLogin(true);
                break;
            case MessageID.LOGIN_OK_ID://登录成功
                mApplication.initSyncManagement(Utils.getNullStringAsEmpty(mConnParam,"server_url"),Utils.getNullStringAsEmpty(mConnParam,"appId"),
                        Utils.getNullStringAsEmpty(mConnParam,"appSecret"),mStoresId,mPosNum,mOperId);

                if (SQLiteHelper.isNew()) {
                    mProgressDialog.setMessage("准备重新同步...").refreshMessage().show();
                    mApplication.manualSync();
                }else
                    mApplication.start_sync(true);
                break;
            case MessageID.LOGIN_ID_ERROR_ID://账号错误
                mUserIdEt.requestFocus();
                mUserIdEt.selectAll();
                mUserIdEt.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_x));
                if (msg.obj instanceof String)
                    MyDialog.ToastMessage(mUserIdEt,msg.obj.toString(),this,null);
                break;
            case MessageID.LOGIN_PW_ERROR_ID://密码错误
                mPasswordEt.requestFocus();
                mPasswordEt.selectAll();
                mPasswordEt.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_x));
                if (msg.obj instanceof String)
                    MyDialog.ToastMessage(mPasswordEt,msg.obj.toString(),this,null);
                break;
            case MessageID.SYNC_DIS_INFO_ID://资料同步进度信息
                mProgressDialog.setMessage(msg.obj.toString()).refreshMessage().show();
                break;
            case MessageID.OFF_LINE_LOGIN_ID:
                offline_login();
                break;
        }
    }

    private void launchLogin(boolean isConnection){
        mApplication.setNetworkStatus(isConnection);
        if (mApplication.initCashierInfoAndStoreInfo(this)){
            final Intent intent = new Intent(this,NormalMainActivity.class);
            if (isSmallScreen)intent.setClass(this, MobileNavigationActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    private void offline_login(){
        MyDialog.displayAskMessage(this, "连接服务器失败，是否离线登录？", myDialog -> {
            myDialog.dismiss();
            final JSONObject connParam = mConnParam;
            mStoresId = Utils.getNullObjectAsEmptyJson(connParam, "storeInfo").getString("stores_id");
            if (Utils.isNotEmpty(mStoresId)){
                CustomApplication.initDb(mStoresId);
                if (CustomApplication.verifyOfflineTime(this)){
                    mOperId = mUserIdEt.getText().toString();

                    final String password = mPasswordEt.getText().toString();
                    final String local_password = Utils.getUserIdAndPasswordCombinationOfMD5(mOperId + password);
                    final StringBuilder err = new StringBuilder();


                    mPosNum = Utils.getNullStringAsEmpty(connParam, "pos_num");

                    final String sz_count = SQLiteHelper.getString("SELECT count(cas_id) count FROM cashier_info where " +
                            "cas_account = '" + mOperId + "' and stores_id = '" + mStoresId + "' and cas_pwd = '" + local_password + "'", err);

                    if (Integer.parseInt(sz_count) > 0) {
                        mApplication.initSyncManagement(Utils.getNullStringAsEmpty(connParam,"server_url"),Utils.getNullStringAsEmpty(connParam,"appId"),
                                Utils.getNullStringAsEmpty(connParam,"appSecret"),mStoresId,mPosNum,mOperId);
                        launchLogin(false);
                    } else {
                        CustomApplication.sendMessage(MessageID.LOGIN_ID_ERROR_ID, "不存在此用户！");
                    }
                }else {
                    if (mProgressDialog.isShowing())mProgressDialog.dismiss();
                }
            }else {
                CustomApplication.sendMessage(MessageID.DIS_ERR_INFO_ID, "离线登录，门店编号不能为空!");
            }
        }, myDialog -> {
            myDialog.dismiss();
            finish();
        });
    }
}
