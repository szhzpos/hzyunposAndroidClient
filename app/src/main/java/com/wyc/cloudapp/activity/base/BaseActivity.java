package com.wyc.cloudapp.activity.base;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wyc.cloudapp.activity.normal.LoginActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.logger.Logger;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.activity.mobile
 * @ClassName: BaseActivity
 * @Description: app Activity基类
 * @Author: wyc
 * @CreateDate: 2021/3/4 15:05
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/3/4 15:05
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class BaseActivity extends AppCompatActivity implements IHookKey {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        if (CustomApplication.self().isNotLogin() && !(this instanceof LoginActivity)){
            LoginActivity.start(this);
            Logger.d("restart Activity:%s",getLocalClassName());
            finish();
        }
    }

    @Override
    protected void finalize(){
        Logger.d(getClass().getSimpleName() + " finalized");
    }

    @Override
    public void onBackPressed() {
        hideInputMethod();
        super.onBackPressed();
    }

    @Override
    public void finish() {
        hideInputMethod();
        super.finish();
    }
    protected final void hideInputMethod(){
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    @Override
    public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
        if ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER || event.getKeyCode() == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_UP){
            return hookEnterKey();
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    public boolean hookEnterKey() {
        return false;
    }
}