package com.wyc.cloudapp.activity.mobile;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;

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
        Toast.makeText(this,"add",Toast.LENGTH_LONG).show();
    }

    @Override
    protected JSONObject generateQueryCondition() {
        return null;
    }
}