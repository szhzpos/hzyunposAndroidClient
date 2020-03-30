package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;

public class MoreFunDialog extends Dialog {
    private MainActivity mContext;
    public MoreFunDialog(@NonNull MainActivity context) {
        super(context);
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.more_fun_dialog_layout);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        //初始化按钮事件
        findViewById(R.id._close).setOnClickListener(v -> this.dismiss());
        findViewById(R.id.sync_btn).setOnClickListener(v->mContext.sync(true));
        findViewById(R.id.setup_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ParameterSettingDialog parameterSettingDialog = new ParameterSettingDialog(mContext);
                parameterSettingDialog.show();
            }
        });
    }
}
