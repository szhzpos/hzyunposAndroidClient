package com.wyc.cloudapp.activity.mobile.business;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.business.MobileSupplierAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import static com.wyc.cloudapp.activity.mobile.business.MobileSelectGoodsActivity.TITLE_KEY;

public class MobileSupplierInfoActivity extends AbstractMobileBaseArchiveActivity {
    private MobileSupplierAdapter mAdapter;
    private EditText mSearchContent;
    private Spinner mConditionSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initSearchContent();
        initSupplierList();
        initConditionSpinner();
    }

    @Override
    public void onStart(){
        super.onStart();
        query();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText search = findViewById(R.id._search);
        search.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_UP){
                query();
                return true;
            }
            return false;
        });
        search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = search.getWidth();
                if (dx > (w - search.getCompoundPaddingRight())) {
                    query();
                }
            }
            return false;
        });
        mSearchContent = search;
    }
    private void query(){
        final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(this,getString(R.string.hints_query_data_sz));
        CustomApplication.execute(()->{
            int xtype = mConditionSpinner.getSelectedItemPosition();
            final String keyword = mSearchContent.getText().toString();
            final JSONObject param = new JSONObject();
            param.put("appid",getAppId());
            if (Utils.isNotEmpty(keyword)){
                if (xtype == 0){
                    param.put("xtype","gs_code");
                }else {
                    param.put("xtype","gs_name");
                }
                param.put("keyword",keyword);
            }
            JSONObject ret_obj = HttpUtils.sendPost(getUrl() + "/api/supplier_search/xlist", HttpRequest.generate_request_parm(param,getAppSecret()),true);
            if (HttpUtils.checkRequestSuccess(ret_obj)){
                try {
                    ret_obj = JSONObject.parseObject(ret_obj.getString("info"));
                    if (HttpUtils.checkBusinessSuccess(ret_obj)){
                        final JSONArray data = ret_obj.getJSONArray("data");
                        mAdapter.setDataForArray(data);
                    }else throw new JSONException(ret_obj.getString("info"));
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.ToastMessageInMainThread(e.getMessage());
                }
            }
            progressDialog.dismiss();
        });
    }

    private void initSupplierList(){
        final RecyclerView _supplier_list = findViewById(R.id._supplier_list);
        mAdapter = new MobileSupplierAdapter(this);
        _supplier_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        _supplier_list.setAdapter(mAdapter);
        _supplier_list.addItemDecoration(new LinearItemDecoration(this.getColor(R.color.gray_subtransparent),3));
    }

    private void initConditionSpinner(){
        final Spinner spinner = findViewById(R.id.spinner_);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.drop_down_style);
        adapter.add(getString(R.string.supplier_code_sz));
        adapter.add(getString(R.string.supplier_name_sz));
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSearchContent.setHint(getString(R.string.input_hint,adapter.getItem(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mConditionSpinner = spinner;
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_supplier_info;
    }

    @Override
    protected void add() {
        EditSupplierActivity.start(this,false,null);
    }

    @Override
    protected String title() {
        return getIntent().getStringExtra(TITLE_KEY);
    }
}