package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;

public class ParameterSettingDialog extends Dialog {
    private Button mBase_param_btn,mPeripheral_param_btn,mPrint_format_btn,mCurentBtn;
    private Context mContext;
    public ParameterSettingDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.parameter_setting_dialog_layout);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        mBase_param_btn = findViewById(R.id.base_param_btn);
        mPeripheral_param_btn = findViewById(R.id.peripheral_param_btn);
        mPrint_format_btn = findViewById(R.id.print_format_btn);


        //按钮事件
        findViewById(R.id._close).setOnClickListener(v -> ParameterSettingDialog.this.dismiss());
        mBase_param_btn.setOnClickListener(param_btn_click);
        mPeripheral_param_btn.setOnClickListener(param_btn_click);
        mPrint_format_btn.setOnClickListener(param_btn_click);

        //默认显示基本设置
        mBase_param_btn.callOnClick();
    }

    private View.OnClickListener param_btn_click = (v)->{
        if (v instanceof Button){
            Button btn = (Button)v;
            if (mCurentBtn == null){
                mCurentBtn = btn;
                btn.setBackgroundColor(mContext.getColor(R.color.blue));
                btn.setTextColor(mContext.getColor(R.color.white));
            }else{
                if (mCurentBtn != btn){
                    mCurentBtn.setBackgroundColor(mContext.getColor(R.color.white));
                    mCurentBtn.setTextColor(mContext.getColor(R.color.blue));
                    mCurentBtn = btn;

                    btn.setBackgroundColor(mContext.getColor(R.color.blue));
                    btn.setTextColor(mContext.getColor(R.color.white));
                }
            }
        }
    };

}
