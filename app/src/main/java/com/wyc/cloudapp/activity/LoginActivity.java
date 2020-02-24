package com.wyc.cloudapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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
import com.wyc.cloudapp.dialog.ConnSettingDialog;
import com.wyc.cloudapp.dialog.CustomDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.keyboard.SoftKeyBoardListener;

import java.lang.ref.WeakReference;

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_STORAGE_PERMISSIONS  = 800;
    private RelativeLayout mMain;
    private EditText mUser_id,mPassword;
    private Handler myHandler;
    private LoginActivity mLogin;
    private CustomDialog mDialog;
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
        mDialog = new CustomDialog(this,R.style.CustomDialog);

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

        b_login.setOnClickListener((View v)->{

            mDialog.setTitle("正在登录...").setmCancel(true).show();
            mDialog.setOnCancelListener(dialog -> {
                MyDialog d = new MyDialog(mLogin);
                d.setMessage("是否取消登录？").setYesOnclickListener("是",(MyDialog mydialog)->{
                    mLogin.finish();
                    mydialog.dismiss();
                }).setNoOnclickListener("否",MyDialog::dismiss).show();
            });
            AsyncTask.execute(()->{
                int cunt = 5;
                while (cunt-- != 0)
                try {
                    Thread.sleep(5000);
                    mDialog.setTitle(mDialog.getSzTitle() + cunt).refreshTitle();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                myHandler.obtainMessage(1).sendToTarget();
            });

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

    private static class Myhandler extends Handler {
        private WeakReference<LoginActivity> weakHandler;
        private Myhandler(LoginActivity loginActivity){
            this.weakHandler = new WeakReference<LoginActivity>(loginActivity);
        }
        public void handleMessage(Message msg){
            LoginActivity activity = weakHandler.get();
            if (null == activity)return;
            switch (msg.what){
                case 0:
                    break;
                case 1://登录成功
                    activity.mDialog.dismiss();
                    Intent intent = new Intent(activity.mLogin,MainActivity.class);
                    activity.startActivity(intent);
                    activity.mLogin.finish();
                    break;
            }
        }
    }

}
