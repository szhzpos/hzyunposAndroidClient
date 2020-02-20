package com.wyc.cloudapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.ConnSettingDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.keyboard.SoftKeyBoardListener;
import com.wyc.cloudapp.logger.AndroidLogAdapter;
import com.wyc.cloudapp.logger.Logger;

public class LoginActivity extends AppCompatActivity {
    private static final int REQUEST_STORAGE_PERMISSIONS  = 800;
    private RelativeLayout mMain;
    private EditText mUser_id,mPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Logger.addLogAdapter(new AndroidLogAdapter());

        Button b_login,b_cancel;
        View b_setup;

        mMain = findViewById(R.id.main);
        mUser_id = findViewById(R.id.user_id);
        mPassword = findViewById(R.id.password);

        //局部变量
        b_login = findViewById(R.id.b_login);
        b_cancel = findViewById(R.id.cancel);
        b_setup = findViewById(R.id.setup_ico);

        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            ViewGroup.LayoutParams mLayoutParams = mMain.getLayoutParams();
            @Override
            public void keyBoardShow(int height) {
                WindowManager m = (WindowManager)LoginActivity.this.getSystemService(WINDOW_SERVICE);
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
            Activity login = LoginActivity.this;
            Intent intent = new Intent(login,MainActivity.class);
            startActivity(intent);
            login.finish();
        });

        b_cancel.setOnClickListener((View V)->{
            final Activity activity = LoginActivity.this;
            MyDialog dialog = new MyDialog(activity);
            dialog.setMessage("是否取消登录？").setYesOnclickListener("是",(MyDialog mydialog)->{
                activity.finish();
                mydialog.dismiss();
            }).setNoOnclickListener("否",(MyDialog mydialog)->{
                mydialog.dismiss();
            }).show();

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

}
