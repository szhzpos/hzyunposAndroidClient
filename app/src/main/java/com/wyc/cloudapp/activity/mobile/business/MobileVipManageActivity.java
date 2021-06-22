package com.wyc.cloudapp.activity.mobile.business;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForJson;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.vip.AddVipInfoDialog;
import com.wyc.cloudapp.dialog.vip.VipDetailInfoWindow;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

public class MobileVipManageActivity extends AbstractMobileBaseArchiveActivity {
    private VipCategoryAdapter mVipCategoryAdapter;
    private VipRecordAdapter mVipRecordAdapter;
    private EditText mSearchEt;
    private Spinner mConditionSpinner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initVipCategory();
        initVipRecord();

        initSearchEt();
        initConditionSpinner();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_vip_manage;
    }

    private void initConditionSpinner(){
        final Spinner spinner = findViewById(R.id._spinner);
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(this,R.layout.drop_down_style);
        adapter.add("手机号");
        adapter.add("卡号");
        adapter.add("名称");
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mSearchEt.setHint(getString(R.string.input_hint,adapter.getItem(position)));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mConditionSpinner = spinner;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchEt(){
        final EditText search = findViewById(R.id.vip_search_et);
        search.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_UP){
                mVipRecordAdapter.loadVip(search.getText().toString(),mConditionSpinner.getSelectedItemPosition());
                return true;
            }
            return false;
        });
        search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = search.getWidth();
                if (dx > (w - search.getCompoundPaddingRight())) {
                    mVipRecordAdapter.loadVip(search.getText().toString(),mConditionSpinner.getSelectedItemPosition());
                }
            }
            return false;
        });
        mSearchEt = search;
    }

    private void loadVipForCategoryId(){
        mVipRecordAdapter.loadVipForCategoryId(-1);
    }

    private void initVipCategory(){
        final RecyclerView vip_type_list = findViewById(R.id.vip_type_list);
        vip_type_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        mVipCategoryAdapter = new VipCategoryAdapter(this);
        vip_type_list.setAdapter(mVipCategoryAdapter);

        loadVipCategory();
    }

    private void initVipRecord(){
        final RecyclerView vip_record_list = findViewById(R.id.vip_record_list);
        vip_record_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        vip_record_list.addItemDecoration(new LinearItemDecoration(getColor(R.color.white)));
        mVipRecordAdapter = new VipRecordAdapter(this);
        vip_record_list.setAdapter(mVipRecordAdapter);

        loadVipForCategoryId();
    }

    private void loadVipCategory(){
        CustomApplication.execute(()->{
            JSONObject object = new JSONObject();
            object.put("appid",getAppId());
            object = HttpUtils.sendPost(getUrl() + "/api/member/get_member_grade", HttpRequest.generate_request_parm(object,getAppSecret()),true);
            if (HttpUtils.checkRequestSuccess(object)){
                object = JSONObject.parseObject(object.getString("info"));
                if (HttpUtils.checkBusinessSuccess(object)){
                    final JSONArray array = Utils.getNullObjectAsEmptyJsonArray(object,"data");
                    object = new JSONObject();
                    object.put("grade_id",-1);
                    object.put("grade_name","全部");
                    array.add(0,object);
                    mVipCategoryAdapter.setDataForArray(array);
                }else {
                    MyDialog.ToastMessageInMainThread(object.getString("info"));
                }
            }
        });
    }

    @Override
    protected void add() {
        if (AddVipInfoDialog.verifyVipModifyOrAddPermissions(this)){
            EditVipInfoActivity.start(this);
        }
    }

    @Override
    protected String title() {
        return getString(R.string.vip_record_sz);
    }

    private final static class VipRecordAdapter extends AbstractDataAdapterForJson<VipRecordAdapter.MyViewHolder> implements View.OnClickListener{
        private final MainActivity mContext;
        private int mCurrentRow;
        private int mCategoryId,mVagueType;
        private String mQueryContent;
        private boolean isReload = true;
        private VipRecordAdapter(MainActivity mContext) {
            this.mContext = mContext;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View view = View.inflate(parent.getContext(),R.layout.mobile_vip_record_layout,null);
            view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            int size = mData.size();
            if (position < size){//由于mDatas 赋值与取值不在同一线程，有可能出现数组越界，所以得加个判断
                final JSONObject vip = mData.getJSONObject(position);
                holder.vip_name.setText(vip.getString("name"));
                holder.card_code_tv.setText(vip.getString("card_code"));
                holder.phone_num_tv.setText(vip.getString("mobile"));
                holder.integral_tv.setText(vip.getString("points_sum"));
                holder.balance_tv.setText(vip.getString("money_sum"));
                holder.detail_btn.setTag(vip);
                holder.detail_btn.setOnClickListener(this);

                if (isReload && position + 5 == size){
                    reload();
                }
            }
        }

        @Override
        public void onClick(View v) {
            final JSONObject object = Utils.getViewTagValue(v);
            final VipDetailInfoWindow detailInfoDialog = new VipDetailInfoWindow(mContext,object);
            detailInfoDialog.showAtLocation(mContext.getWindow().getDecorView(), Gravity.CENTER,0,0);
        }

        static class MyViewHolder extends AbstractDataAdapterForJson.SuperViewHolder {
            private final TextView vip_name,card_code_tv,phone_num_tv,integral_tv,balance_tv;
            private final Button detail_btn;
            MyViewHolder(View itemView) {
                super(itemView);
                vip_name = itemView.findViewById(R.id.vip_name);
                card_code_tv =  findViewById(R.id.card_code_tv);
                phone_num_tv =  findViewById(R.id.phone_num_tv);
                integral_tv =  findViewById(R.id.integral_tv);
                balance_tv =  findViewById(R.id.balance_tv);

                detail_btn = findViewById(R.id.detail_btn);
            }
        }


        private void loadVipForCategoryId(int id){
            mCurrentRow = 0;
            mCategoryId = id;
            isReload = true;
            mVagueType = -1;

            loadVip();
        }

        private void loadVip(final String content,int type){
            if (Utils.isNotEmpty(content)){
                mCurrentRow = 0;
                mQueryContent = content;
                isReload = true;
                mVagueType = type;

                loadVip();
            }else {
                MyDialog.ToastMessageInMainThread("请输入查询内容。");
            }
        }

        private void loadVip(){
            final CustomProgressDialog progressDialog = new CustomProgressDialog(mContext);
            progressDialog.setCancel(false).setMessage(mContext.getString(R.string.hints_query_data_sz)).show();
            CustomApplication.execute(()->{
                JSONObject object = new JSONObject();
                object.put("appid",mContext.getAppId());

                switch (mVagueType){
                    case 0://手机
                        object.put("mobile",mQueryContent);
                        break;
                    case 1://卡号
                        object.put("card_code",mQueryContent);
                        break;
                    case 2://名称
                        object.put("name",mQueryContent);
                        break;
                }
                object.put("limit",50);
                object.put("offset",mCurrentRow);

                object = HttpUtils.sendPost(mContext.getUrl() + "/api/member/get_member_list",HttpRequest.generate_request_parm(object,mContext.getAppSecret()),true);
                if (HttpUtils.checkRequestSuccess(object)){
                    object = JSONObject.parseObject(object.getString("info"));
                    if (HttpUtils.checkBusinessSuccess(object)){
                        int total = object.getIntValue("total");
                        final JSONArray data = Utils.getNullObjectAsEmptyJsonArray(object,"data");
                        if (data.isEmpty()){
                            isReload = false;
                        }else{
                            if (total <= data.size())isReload = false;
                        }
                        setDataForArray(data);
                    }else {
                        MyDialog.ToastMessageInMainThread(object.getString("info"));
                    }
                }
                progressDialog.dismiss();
            });
        }

        private void reload(){
            mCurrentRow++;
            loadVip();
            Logger.d("reload mCurrentRow:%d,size:%d",mCurrentRow, mData.size());
        }

        @Override
        public void setDataForArray(JSONArray array) {
            if (mVagueType >= 0 || mCurrentRow == 0){
                mData = array;
            }else {
                mData.addAll(array);
            }
            if (Looper.myLooper() != Looper.getMainLooper()){
                CustomApplication.runInMainThread(this::notifyDataSetChanged);
            }else
                notifyDataSetChanged();
        }
    }

    private final static class VipCategoryAdapter extends AbstractDataAdapterForJson<VipCategoryAdapter.MyViewHolder> implements View.OnClickListener{
        final MobileVipManageActivity mContext;
        private View mCurrentItemView;
        private VipCategoryAdapter(MobileVipManageActivity mContext) {
            this.mContext = mContext;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            final View itemView = View.inflate(mContext, R.layout.vip_type_info_layout, null);
            itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.height_40)));
            itemView.setOnClickListener(this);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final JSONObject object = mData.getJSONObject(position);

            final String grade_id = object.getString("grade_id");
            holder.category_id.setText(grade_id);
            holder.category_name.setText(object.getString("grade_name"));
        }

        static class MyViewHolder extends AbstractDataAdapterForJson.SuperViewHolder {
            private final TextView category_id;
            private final TextView category_name;
            MyViewHolder(View itemView) {
                super(itemView);
                category_id = itemView.findViewById(R.id.category_id);
                category_name =  itemView.findViewById(R.id.category_name);
            }
        }

        @Override
        public void onClick(View view) {
            TextView name_tv = view.findViewById(R.id.category_name),category_id = view.findViewById(R.id.category_id);
            if (category_id != null && name_tv != null){
                final Resources resources = mContext.getResources();
                int white = resources.getColor(R.color.white,null),blue = resources.getColor(R.color.blue,null);
                if (null != mCurrentItemView){
                    if (mCurrentItemView != view){
                        mCurrentItemView.setBackgroundColor(white);
                        name_tv = mCurrentItemView.findViewById(R.id.category_name);
                        name_tv.setTextColor(blue);

                        mCurrentItemView = view;
                        mCurrentItemView.setBackgroundColor(blue);
                        name_tv = view.findViewById(R.id.category_name);
                        name_tv.setTextColor(white);
                    }
                }else{
                    view.setBackgroundColor(blue);
                    name_tv.setTextColor(white);
                    mCurrentItemView = view;
                }
                try {
                    int id = Integer.parseInt(category_id.getText().toString());
                    signSelectCategory(id);


                }catch (NumberFormatException e){
                    MyDialog.ToastMessageInMainThread(e.getMessage());
                }
            }
        }

        private void signSelectCategory(int id){
            if (mData != null){
                for (int i = 0, size = mData.size(); i < size; i++){
                    final JSONObject object = mData.getJSONObject(i);
                    if (id == object.getIntValue("grade_id")){
                        object.put("sel",true);
                    }else {
                        object.remove("sel");
                    }
                }
            }
        }

    }

}