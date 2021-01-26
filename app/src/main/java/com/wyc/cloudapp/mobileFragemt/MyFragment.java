package com.wyc.cloudapp.mobileFragemt;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.LoginActivity;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.MobileSetupActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;

public final class MyFragment extends AbstractMobileFragment {
    public MyFragment(final MainActivity activity) {
        super(activity);

    }

    private void initExit(){
        final Button exit = findViewById(R.id.m_exit_btn);
        exit.setOnClickListener(v -> {
            if (MyDialog.showMessageToModalDialog(mContext,"是否退出登录?") == 1){
                CustomApplication.self().resetSync();
                final Intent intent = new Intent(mContext, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mContext.startActivity(intent);
                mContext.finish();
            }
        });
    }

    private void initUserInfo(){
        final TextView job_num_tv = findViewById(R.id.job_num_tv),m_name_tv = findViewById(R.id.m_name_tv);
        final JSONObject object = mContext.getCashierInfo();
        job_num_tv.setText(object.getString("cas_code"));
        m_name_tv.setText(object.getString("cas_name"));
    }

    @Override
    protected int getRootLayout() {
        return R.layout.mobile_setup_fragment_layout;
    }

    @Override
    protected int getMainViewId() {
        return R.id.setup_main_function_layout;
    }

    @Override
    protected void triggerItemClick(View v) {
        final int id = v.getId();
        final Intent intent = new Intent(mContext, MobileSetupActivity.class);
        if (id == R.id.m_print_options_tv){
            intent.putExtra("frag","PrintFormatFragment");
            if (v instanceof TextView)intent.putExtra("title",((TextView)v).getText());
        }
        startActivity(intent);
    }

    @Override
    protected void viewCreated(boolean created) {
        if (created){
            initExit();
            initUserInfo();
        }
    }
}