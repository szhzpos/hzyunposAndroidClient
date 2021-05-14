package com.wyc.cloudapp.activity.mobile.business;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.wyc.cloudapp.R;
//编辑供应商
public class EditSupplierActivity extends AbstractEditArchiveActivity {
    private static final String KEY = "modify";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMiddleText(getString(getIntent().getBooleanExtra(KEY,false) ? R.string.modify_supplier_sz : R.string.new_supplier_sz));

    }

    @Override
    protected int getLayout() {
        return R.layout.activity_edit_supplier;
    }

    @Override
    protected void sure() {

    }

    public static void start(Context context,boolean modify){
        final Intent intent = new Intent();
        intent.setClass(context,EditSupplierActivity.class);
        intent.putExtra(KEY,modify);
        context.startActivity(intent);
    }
}