package com.wyc.cloudapp.activity.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.MyDialog;

public class CashierActivity extends AbstractMobileActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_cashier_desk;
    }

    @Override
    protected void initTitleText(){
        final Intent intent = getIntent();
        setMiddleText(intent.getStringExtra("title"));
        setRightText(getString(R.string.clear_sz));
    }

    @Override
    protected void initTitleClickListener(){
        setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog.ToastMessage("清空",v.getContext(),null);
            }
        });
    }
}