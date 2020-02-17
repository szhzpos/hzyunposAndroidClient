package com.wyc.cloudapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.keyboard.SoftKeyBoardListener;
import com.wyc.cloudapp.utils.Utils;

public class LoginActivity extends AppCompatActivity {
    private RelativeLayout mLogin_info;
    private EditText mUser_id,mPassword;
    private int mKeyboard_height = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button b_login,b_cancel;

        mLogin_info = findViewById(R.id.login_info);
        mUser_id = findViewById(R.id.user_id);
        mPassword = findViewById(R.id.password);

        //局部变量
        b_login = findViewById(R.id.b_login);
        b_cancel = findViewById(R.id.cancel);

        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                mKeyboard_height = height;
                setLogin_info_height();
            }

            @Override
            public void keyBoardHide(int height) {
            }
        });

        b_login.setOnClickListener((View v)->{
            MyDialog dialog = new MyDialog(v.getContext());
            dialog.setMessage("8888888").setYesOnclickListener("确定", new MyDialog.onYesOnclickListener() {
                @Override
                public void onYesClick(MyDialog myDialog) {
                    myDialog.dismiss();
                }
            }).setNoOnclickListener("取消", new MyDialog.onNoOnclickListener() {
                @Override
                public void onNoClick(MyDialog myDialog) {
                    myDialog.dismiss();
                }
            }).show();
        });

        mUser_id.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setLogin_info_height();
                        }
                    },300);
                }
            }
        });
        mPassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setLogin_info_height();
                        }
                    },300);
                }
            }
        });

    }

    private void setLogin_info_height(){
        int top = mLogin_info.getTop();
        int bottom = mLogin_info.getBottom();
        int diff_bottom = mKeyboard_height - bottom;
        mLogin_info.setTop(top + diff_bottom);
        mLogin_info.setBottom(mKeyboard_height);
    }

    @Override
    public void onResume(){
        super.onResume();

    }
}
