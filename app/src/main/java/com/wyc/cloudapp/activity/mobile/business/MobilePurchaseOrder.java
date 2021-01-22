package com.wyc.cloudapp.activity.mobile.business;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.dialog.MyDialog;

import org.json.JSONObject;

public class MobilePurchaseOrder extends AbstractMobileBusinessOrderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public AbstractQueryDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
        return null;
    }

    @Override
    protected void add() {
        final CharSequence title = getMiddleText();
        final Intent intent = new Intent();
        intent.setClass(this,MobileAddPurchaseOrder.class);
        intent.putExtra("title",getRightText().toString() + title);
        try {
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
            MyDialog.ToastMessage("暂不支持" + title,this,null);
        }
    }

    @Override
    protected JSONObject generateQueryCondition() {
        return null;
    }
}