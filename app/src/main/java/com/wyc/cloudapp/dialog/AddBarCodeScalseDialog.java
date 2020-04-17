package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;

public class AddBarCodeScalseDialog extends Dialog {
    private Context mContext;
    public AddBarCodeScalseDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.add_b_scalse_dialog_layout);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        //初始化按钮事件
        findViewById(R.id._close).setOnClickListener(v -> this.dismiss());

    }
}
