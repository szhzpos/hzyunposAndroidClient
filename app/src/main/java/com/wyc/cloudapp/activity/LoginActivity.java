package com.wyc.cloudapp.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
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
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.keyboard.SoftKeyBoardListener;
import com.wyc.cloudapp.logger.AndroidLogAdapter;
import com.wyc.cloudapp.logger.Logger;

public class LoginActivity extends AppCompatActivity {
    private RelativeLayout mMain;
    private EditText mUser_id,mPassword;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Logger.addLogAdapter(new AndroidLogAdapter());

        Button b_login,b_cancel;

        mMain = findViewById(R.id.main);
        mUser_id = findViewById(R.id.user_id);
        mPassword = findViewById(R.id.password);

        //局部变量
        b_login = findViewById(R.id.b_login);
        b_cancel = findViewById(R.id.cancel);

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

    }

    @Override
    public void onResume(){
        super.onResume();
    }
}
