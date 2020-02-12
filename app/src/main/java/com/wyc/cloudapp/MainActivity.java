package com.wyc.cloudapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.wyc.cloudapp.keyboard.SoftKeyBoardListener;

public class MainActivity extends AppCompatActivity {
    private RelativeLayout login_info;
    private EditText user_id,password;
    private int mKeyboard_height = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login_info = findViewById(R.id.login_info);
        user_id = findViewById(R.id.user_id);
        password = findViewById(R.id.password);

        SoftKeyBoardListener.setListener(this, new SoftKeyBoardListener.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                Toast.makeText(MainActivity.this, "键盘显示 高度" + height, Toast.LENGTH_SHORT).show();
                mKeyboard_height = height;
                setLogin_info_height();
            }

            @Override
            public void keyBoardHide(int height) {
            }
        });

        user_id.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
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
        int top = login_info.getTop();
        int bottom = login_info.getBottom();
        int diff_bottom = mKeyboard_height - bottom;
        login_info.setTop(top + diff_bottom);
        login_info.setBottom(mKeyboard_height);
        Toast.makeText(MainActivity.this, "登录框 高度" + login_info.getTop(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume(){
        super.onResume();

    }
}
