package com.wyc.cloudapp.activity;

import android.os.Bundle;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (CustomApplication.self().isNotLogin() && !(this instanceof LoginActivity)){
            LoginActivity.start(this);
            finish();
        }
    }

    @Override
    protected void finalize(){
        Logger.d(getClass().getSimpleName() + " finalized");
    }

    @Override
    public void onBackPressed() {
        hide();
        super.onBackPressed();
    }

    @Override
    public void finish() {
        hide();
        super.finish();
    }
    private void hide(){
        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

}