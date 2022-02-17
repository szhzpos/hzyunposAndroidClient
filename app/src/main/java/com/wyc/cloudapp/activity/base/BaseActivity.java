package com.wyc.cloudapp.activity.base;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.wyc.cloudapp.activity.normal.LoginActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.NotchUtils;

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
        if (NotchUtils.hasNotchScreen(this)){
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            hideNavigationBar();
        }

        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O){
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        if (CustomApplication.self().isNotLogin() && !(this instanceof LoginActivity)){
            LoginActivity.start(this);
            Logger.d("restart from Activity:%s",getLocalClassName());
            finish();
        }
    }
    private void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

        decorView.setOnSystemUiVisibilityChangeListener(visibility -> decorView.setSystemUiVisibility(uiOptions));
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
            CustomApplication.cancelGlobalToast();
            return hookEnterKey() || super.dispatchKeyEvent(event);
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN){
            CustomApplication.cancelGlobalToast();
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean hookEnterKey() {
        return false;
    }

    @Override
    public Resources getResources() {
        final Resources res = super.getResources();
        final Configuration configuration = res.getConfiguration();
        if (configuration.fontScale != 1f){
            configuration.fontScale = 1f;
            res.updateConfiguration(configuration,res.getDisplayMetrics());
        }
        return res;
    }
}
