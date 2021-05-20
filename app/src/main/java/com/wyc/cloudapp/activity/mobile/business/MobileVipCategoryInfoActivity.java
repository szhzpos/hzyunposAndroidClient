package com.wyc.cloudapp.activity.mobile.business;

import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.business.MobileVipCategoryAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.business.EditVipCategoryDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

/*会员分类管理*/
public class MobileVipCategoryInfoActivity extends AbstractMobileBaseArchiveActivity {
    private MobileVipCategoryAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initCategoryList();
    }

    @Override
    protected void onStart() {
        super.onStart();
        query();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Logger.d(getClass().getName());
    }

    private void initCategoryList(){
        final RecyclerView item_list = findViewById(R.id._category_list);
        mAdapter = new MobileVipCategoryAdapter(this);
        item_list.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        item_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        item_list.setAdapter(mAdapter);
    }

    public void query(){
        final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(this,getString(R.string.hints_query_data_sz));
        CustomApplication.execute(()->{
            final JSONObject param = new JSONObject();
            param.put("appid",getAppId());
            JSONObject ret_obj = HttpUtils.sendPost(getUrl() + "/api/member/get_member_grade", HttpRequest.generate_request_parm(param,getAppSecret()),true);
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

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_vip_category;
    }

    @Override
    protected void add() {
        EditVipCategoryDialog.start(this,null,false);
    }

}