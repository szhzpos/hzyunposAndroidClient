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
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.wyc.cloudapp.keyboard.SoftKeyBoardListener;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.network.sync.SyncManagement;
import com.wyc.cloudapp.service.AppUpdateService;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.io.File;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.Locale;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
public class LoginActivity extends AppCompatActivity {
    public static final String IMG_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/hzYunPos/goods_img/";
    private static final int REQUEST_STORAGE_PERMISSIONS  = 800;
    private EditText mUserId,mPassword;
    private Handler myHandler;
    private LoginActivity mSelf;
    private CustomProgressDialog mProgressDialog;
    private MyDialog myDialog;
    private SyncManagement mSyncManagement;
    private Button mCancelBtn,mLoginBtn;
    private String mPosNum,mOperId,mStoresId;
    private Future<?> mLoginTask;
    private JSONObject mConnParam;
    private DisplayMetrics mDisplayMetrics;
    private boolean isSmallScreen = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //加载布局
        loadView();

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

        initKeyboard();
        initDisplayInfoAndVersion();

        registerReceiver(receiver,new IntentFilter(AppUpdateService.APP_PROGRESS_BROADCAST));
    }

    private void loadView(){
        mDisplayMetrics = new DisplayMetrics();
        double diagonal = Utils.getDisplayMetrics((WindowManager)getSystemService(WINDOW_SERVICE),mDisplayMetrics);

        Logger.d("diagonal:%f",diagonal);

        if ((isSmallScreen = diagonal < 7)){
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) ;//显示状态栏

            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.mobile_activity_login);
        }else{
            setContentView(R.layout.activity_login);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        clearResource();
        unregisterReceiver(receiver);
    }

    @Override
    public void onBackPressed(){
        mCancelBtn.callOnClick();
    }

    @Override
    public void onResume(){
        super.onResume();
        checkSelfPermissionAndInitDb();
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
    protected void finalize(){
        Logger.d("LoginActivity finalized");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull  int[]  grantResults) {
        if (requestCode == REQUEST_STORAGE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                SQLiteHelper.initDb(this);
                initGoodsImgDirectory();
                //显示商户域名
                show_shop_info();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initDisplayInfoAndVersion(){
        final TextView tv = findViewById(R.id.display_info_tv);
        if (null != tv && mDisplayMetrics != null){
            try {
                final PackageInfo packageInfo = getPackageManager().getPackageInfo("com.wyc.cloudapp",0);
                tv.setText(String.format(Locale.CHINA,"高:%d x 宽:%d DPI:%d 版本号:%s",mDisplayMetrics.heightPixels,mDisplayMetrics.widthPixels,mDisplayMetrics.densityDpi,packageInfo.versionName));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                MyDialog.ToastMessage(e.getMessage(),this,getWindow());
            }
        }
    }

    private void saveLastUser(){
        final SharedPreferences preferences=getSharedPreferences("login_user", Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor= preferences.edit();
        editor.putString("user", mUserId.getText().toString());
        editor.apply();
    }

    private void setLastUser(){
        final SharedPreferences preferences=getSharedPreferences("login_user", Context.MODE_PRIVATE);
        if (mUserId != null){
            final String user = preferences.getString("user","");
            if ("".equals(user)){
                mUserId.postDelayed(()-> mUserId.requestFocus(),400);
            }else{
                mUserId.setText(user);
                if (mPassword != null)mPassword.postDelayed(()-> mPassword.requestFocus(),300);
            }

        }
    }

    private void initLoginBtn(){
        final Button login_btn = findViewById(R.id.b_login);
        if (null != login_btn)
            login_btn.setOnClickListener((View v)-> {

                saveLastUser();

                //login();
                check_ver();
            });
        mLoginBtn = login_btn;
    }
    private void check_ver(){
        mProgressDialog.setCancel(false).setMessage("正在检查更新...").refreshMessage().show();

        final JSONObject conn_param = new JSONObject();
        if (SQLiteHelper.getLocalParameter("connParam", conn_param)) {
            if (Utils.JsonIsNotEmpty(conn_param)) {
                final Intent intentService = new Intent(this, AppUpdateService.class);
                intentService.putExtra("url",Utils.getNullStringAsEmpty(conn_param,"server_url"));
                intentService.putExtra("appid",Utils.getNullStringAsEmpty(conn_param,"appId"));
                intentService.putExtra("appSecret",Utils.getNullStringAsEmpty(conn_param,"appSecret"));
                startService(intentService);
                mConnParam = conn_param;
            } else {
                mProgressDialog.dismiss();
                MyDialog.ToastMessage("连接参数不能为空!",this,getWindow());
                final View view = findViewById(R.id.setup_ico);
                if (null != view)view.callOnClick();
            }
        }else {
            mProgressDialog.dismiss();
            MyDialog.displayErrorMessage(myDialog,conn_param.getString("info"),this);
        }
    }

    //广播监听下载的进度
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final LoginActivity activity = LoginActivity.this;
            int status = intent.getIntExtra("status",0);
            switch (status){
                case AppUpdateService.SUCCESS_STATUS:
                    login();
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
                        MyDialog.displayErrorMessage(myDialog,"检查版本错误:" + intent.getStringExtra("info"),activity);
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
            finish();
            final Intent intent_launch = new Intent(this,LoginActivity.class);
            startActivity(intent_launch);
        }
    }

    private void initCloseMainWindow(){
        mCancelBtn = findViewById(R.id.cancel);
        if (null != mCancelBtn)
            mCancelBtn.setOnClickListener((View V)-> MyDialog.displayAskMessage(myDialog,"是否退出？", mSelf, myDialog -> {
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
        mPassword = password;
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
                if (mPassword != null)mPassword.requestFocus();
                return true;
            }
            return false;
        });
        mUserId = user_id;
    }
    @SuppressWarnings("unused")
    private void initSoftKeyBoardListener(){
        final RelativeLayout main_window = findViewById(R.id.main);
        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            final ViewGroup.LayoutParams mLayoutParams = main_window.getLayoutParams();
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
            show_shop_info();
        }
    }
    private void show_shop_info(){
        final JSONObject param = new JSONObject();
        if(SQLiteHelper.getLocalParameter("connParam",param)){
            if (Utils.JsonIsNotEmpty(param)){
                final JSONObject shop_info = JSON.parseObject(Utils.getNullOrEmptyStringAsDefault(param,"storeInfo","{}"));
                shop_info.put("shop_id",Utils.getNullStringAsEmpty(param,"shop_id"));
                shop_info.put("shop_name",String.format("%s%s%s%s",Utils.getNullStringAsEmpty(shop_info,"stores_name"),"[",Utils.getNullStringAsEmpty(shop_info,"stores_id"),"]"));
                show_shop_info(shop_info);
            }
        }else{
            MyDialog.ToastMessage(param.getString("info"),this,getWindow());
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
    private final View.OnClickListener mKeyboardListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int v_id = view.getId();
            EditText et_view = (EditText) getCurrentFocus();
            if (null == et_view){
                et_view = mUserId;
                mUserId.requestFocus();
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
            JSONObject object = new JSONObject(), conn_param = mConnParam, cashier_json, retJson, info_json, jsonLogin, store_info;
            String url, sz_param, err_info,base_url = Utils.getNullStringAsEmpty(conn_param,"server_url"),appid =  Utils.getNullStringAsEmpty(conn_param,"appId"),
                    appSecret = Utils.getNullStringAsEmpty(conn_param,"appSecret");
            try {
                mOperId = mUserId.getText().toString();

                object.put("appid",appid);
                object.put("cas_account", mOperId);
                object.put("cas_pwd", mPassword.getText());

                sz_param = HttpRequest.generate_request_parm(object,appSecret);

                url = Utils.getNullStringAsEmpty(conn_param,"server_url") + "/api/cashier/login";

                retJson = httpRequest.setConnTimeOut(3000).setReadTimeOut(3000).sendPost(url, sz_param, true);

                switch (retJson.getIntValue("flag")) {
                    case 0:
                        int rsCode = Utils.getNotKeyAsNumberDefault(retJson,"rsCode",-1);
                        if (rsCode == HttpURLConnection.HTTP_BAD_REQUEST  || rsCode == HttpURLConnection.HTTP_INTERNAL_ERROR){
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

                                url = base_url + "/api_v2/pos/set_ps";
                                jsonLogin = new JSONObject();
                                jsonLogin.put("appid", appid);
                                jsonLogin.put("pos_code", Utils.getDeviceId(mSelf));
                                jsonLogin.put("pos_name", Utils.getDeviceId(mSelf));
                                jsonLogin.put("stores_id", mStoresId);
                                sz_param = HttpRequest.generate_request_parm(jsonLogin, appSecret);
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

        });
    }

    private static class Myhandler extends Handler {
        private final WeakReference<LoginActivity> weakHandler;
        private Myhandler(LoginActivity loginActivity){
            this.weakHandler = new WeakReference<>(loginActivity);
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
                    activity.launchLogin(true);
                    break;
                case MessageID.LOGIN_OK_ID://登录成功
                    activity.mSyncManagement = new SyncManagement(Utils.getNullStringAsEmpty(activity.mConnParam,"server_url"),Utils.getNullStringAsEmpty(activity.mConnParam,"appId"),
                            Utils.getNullStringAsEmpty(activity.mConnParam,"appSecret"),activity.mStoresId,activity.mPosNum,activity.mOperId);
                    activity.mSyncManagement.setNotifyHandlerAndStart(this);
                    if (SQLiteHelper.isNew()) {
                        activity.mProgressDialog.setMessage("准备重新同步...").refreshMessage().show();
                        activity.mSyncManagement.afresh_sync();
                    }else
                        activity.mSyncManagement.start_sync(true);
                    break;
                case MessageID.LOGIN_ID_ERROR_ID://账号错误
                    activity.mUserId.requestFocus();
                    activity.mUserId.selectAll();
                    activity.mUserId.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake_x));
                    if (msg.obj instanceof String)
                        MyDialog.ToastMessage(activity.mUserId,msg.obj.toString(),activity,null);
                    break;
                case MessageID.LOGIN_PW_ERROR_ID://密码错误
                    activity.mPassword.requestFocus();
                    activity.mPassword.selectAll();
                    activity.mPassword.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake_x));
                    if (msg.obj instanceof String)
                        MyDialog.ToastMessage(activity.mPassword,msg.obj.toString(),activity,null);
                    break;
                case MessageID.SYNC_DIS_INFO_ID://资料同步进度信息
                    activity.mProgressDialog.setMessage(msg.obj.toString()).refreshMessage().show();
                    break;
                case MessageID.OFF_LINE_LOGIN_ID:
                    activity.offline_login();
                    break;
            }
        }
    }

    private void launchLogin(boolean isConnection){
        final Intent intent = new Intent(this,NormalMainActivity.class);
        if (isSmallScreen)intent.setClass(this, MobileNavigationActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("network",isConnection);
        startActivity(intent);
        finish();
    }

    private void offline_login(){
        MyDialog.displayAskMessage(myDialog, "连接服务器失败，是否离线登录？", this, myDialog -> {
            myDialog.dismiss();
            final String user_id = mUserId.getText().toString(),password = mPassword.getText().toString();
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

                if (Integer.parseInt(sz_count) > 0){
                    launchLogin(false);
                }else {
                    myHandler.obtainMessage(MessageID.LOGIN_ID_ERROR_ID, "不存在此用户！").sendToTarget();
                }
            }else {
                MyDialog.displayErrorMessage(myDialog,"查询连接参数错误:" + param_obj.getString("info"),this);
            }
        }, MyDialog::dismiss);
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
