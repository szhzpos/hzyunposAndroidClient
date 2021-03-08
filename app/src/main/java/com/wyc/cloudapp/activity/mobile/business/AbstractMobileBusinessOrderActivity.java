package com.wyc.cloudapp.activity.mobile.business;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.CustomizationView.InterceptLinearLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.report.AbstractDataAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.DrawableUtil;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public abstract class AbstractMobileBusinessOrderActivity extends AbstractMobileActivity {
    private long mStartTime = 0,mEndTime = 0;
    private Button mCurrentDateBtn,mCurrentAuditStatusBtn;
    private TextView mStartDateTv,mEndDateTv;
    private AbstractDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> mAdapter;
    private JSONObject mParameterObj;
    private boolean isFirstLoad = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mParameterObj = new JSONObject();

        initQueryTimeBtn();
        initAuditBtn();
        initEndDateAndTime();
        initStartDateAndTime();
        initOrderList();
        initTitle();
    }

    protected abstract AbstractDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter();
    protected abstract JSONObject generateQueryCondition();
    public abstract Class<?> jumpAddTarget();

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_business_order;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentDateBtn != null && !isFirstLoad)mCurrentDateBtn.callOnClick();
    }

    private void initTitle(){
        final Intent intent = getIntent();
        if (intent != null)setMiddleText(intent.getStringExtra("title"));
        setRightText(getString(R.string.add_sz));
        setRightListener(v -> add());
    }
    private void add(){
        final CharSequence title = getRightText().toString() + getMiddleText();
        final Intent intent = new Intent();
        intent.setClass(this, jumpAddTarget());
        intent.putExtra("title", title);
        try {
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
            MyDialog.ToastMessage("暂不支持" + title,this,null);
        }
    };


    private void initQueryTimeBtn(){
        final InterceptLinearLayout query_time_btn_layout = findViewById(R.id.query_time_btn_layout);
        final Button today = query_time_btn_layout.findViewById(R.id.m_today_btn);
        query_time_btn_layout.post(()->{
            float corner_size = query_time_btn_layout.getHeight() / 2.0f;
            query_time_btn_layout.setForeground(DrawableUtil.createDrawable(new float[]{corner_size,corner_size,corner_size,corner_size,corner_size,corner_size,corner_size,corner_size}
                    ,getColor(R.color.transparent), Utils.dpToPx(this,1),getColor(R.color.blue)));
        });
        query_time_btn_layout.setClickListener(mClickListener);
        today.post(today::callOnClick);
    }
    private final View.OnClickListener mClickListener = v -> {
        final Button btn = (Button) v;

        int white = getColor(R.color.white),text_color = getColor(R.color.text_color),blue = getColor(R.color.blue);
        final int id = btn.getId();

        final LinearLayout m_query_time_tv_layout = findViewById(R.id.m_query_time_tv_layout);
        float corner_size = (float) (btn.getHeight() / 2.0);
        float[] corners = new float[8];

        final Calendar rightNow = Calendar.getInstance();
        rightNow.setTimeZone(TimeZone.getDefault());
        if (id == R.id.m_today_btn){
            corners[0] = corners[1] =  corners[6] = corners[7] = corner_size;

            m_query_time_tv_layout.setVisibility(View.GONE);

            rightNow.setTime(new Date());

            setStartTime(rightNow);
            mStartTime = rightNow.getTime().getTime();

            setEndTime(rightNow);
            mEndTime = rightNow.getTime().getTime();

        }else if (id == R.id.m_yesterday_btn){
            m_query_time_tv_layout.setVisibility(View.GONE);

            rightNow.setTime(new Date());

            rightNow.add(Calendar.DAY_OF_YEAR,-1);

            setStartTime(rightNow);
            mStartTime = rightNow.getTime().getTime();

            setEndTime(rightNow);
            mEndTime = rightNow.getTime().getTime();

        }else if (id == R.id.m_other_btn){
            m_query_time_tv_layout.setVisibility(View.VISIBLE);
            corners[2] = corners[3] =  corners[4] = corners[5] = corner_size;
            if (mEndDateTv != null && mStartDateTv != null){
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
                try {
                    rightNow.setTime(Objects.requireNonNull(sdf.parse(mStartDateTv.getText() + " 00:00:00")));
                    mStartTime = rightNow.getTime().getTime();
                    rightNow.setTime(Objects.requireNonNull(sdf.parse(mEndDateTv.getText() + " 23:59:59")));
                    mEndTime = rightNow.getTime().getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!isFirstLoad)query();

        if (btn != mCurrentDateBtn){
            btn.setTextColor(white);
            btn.setBackground(DrawableUtil.createDrawable(corners,blue,0,blue));
            if (null != mCurrentDateBtn){
                mCurrentDateBtn.setTextColor(text_color);
                if (mCurrentDateBtn.getId() == R.id.m_yesterday_btn){
                    mCurrentDateBtn.setBackground(getDrawable(R.drawable.left_right_separator));
                }else
                    mCurrentDateBtn.setBackground(DrawableUtil.createDrawable(corners,white,0,blue));
            }
            mCurrentDateBtn = btn;
        }
    };

    private void query(){
        final JSONObject condition = generateQueryCondition();
        if (null != condition){
            final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

            mParameterObj.put("appid",getAppId());
            mParameterObj.put("stores_id",getStoreId());
            mParameterObj.put("pt_user_id",getPtUserId());
            mParameterObj.put("begin_time",sdf.format(new Date(mStartTime)));
            mParameterObj.put("end_time",sdf.format(new Date(mEndTime)));

            final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(this,getString(R.string.hints_query_data_sz));
            CustomApplication.execute(()->{
                final String param =  HttpRequest.generate_request_parm(mParameterObj,getAppSecret());

                Logger.d("业务查询参数:%s",param);

                final JSONObject retJson = HttpUtils.sendPost(getUrl() + condition.getString("api"),param,true);
                if (HttpUtils.checkRequestSuccess(retJson)){
                    try {
                        final JSONObject info = JSONObject.parseObject(retJson.getString("info"));
                        if (HttpUtils.checkBusinessSuccess(info)){
                            final JSONArray data = Utils.getNullObjectAsEmptyJsonArray(info,"data");
                            runOnUiThread(()-> mAdapter.setDataForArray(data));
                        }else {
                            throw new JSONException(info.getString("info"));
                        }
                    }catch (JSONException e){
                        e.printStackTrace();
                        runOnUiThread(()-> MyDialog.ToastMessage(e.getMessage(),this,null));
                    }
                }else {
                    runOnUiThread(()-> MyDialog.ToastMessage(retJson.getString("info"),this,null));
                }
                progressDialog.dismiss();
            });
        }
    }

    private void initAuditBtn(){
        final InterceptLinearLayout audit_btn_layout = findViewById(R.id.audit_btn_layout);
        final Button m_all_btn = audit_btn_layout.findViewById(R.id.m_all_btn);
        audit_btn_layout.post(()->{
            float corner_size = audit_btn_layout.getHeight() / 2.0f;
            audit_btn_layout.setForeground(DrawableUtil.createDrawable(new float[]{corner_size,corner_size,corner_size,corner_size,corner_size,corner_size,corner_size,corner_size}
                    ,getColor(R.color.transparent), Utils.dpToPx(this,1),getColor(R.color.blue)));
        });
        audit_btn_layout.setClickListener(mAuditStatusClickListener);
        if (isFindSourceOrderId()){//如果是查找来源单号则隐藏审核查询条件。
            final Button audit_btn = audit_btn_layout.findViewById(R.id.m_audit_btn);
            audit_btn.post(audit_btn::callOnClick);
            audit_btn_layout.setVisibility(View.GONE);
        }else
            m_all_btn.post(m_all_btn::callOnClick);
    }

    public boolean isFindSourceOrderId(){
        final Intent result = getIntent();
        return result != null && result.getBooleanExtra("FindSource",false);
    }

    private final View.OnClickListener mAuditStatusClickListener = v -> {
        final Button btn = (Button) v;

        int white = getColor(R.color.white),text_color = getColor(R.color.text_color),blue = getColor(R.color.blue);
        final int id = btn.getId();

        float corner_size = (float) (btn.getHeight() / 2.0);
        float[] corners = new float[8];

        if (id == R.id.m_all_btn){
            corners[0] = corners[1] =  corners[6] = corners[7] = corner_size;
            mParameterObj.remove("sh_status");
        }else if (id == R.id.m_unaudited_btn){
            mParameterObj.put("sh_status",1);
        }else if (id == R.id.m_audit_btn){
            corners[2] = corners[3] =  corners[4] = corners[5] = corner_size;
            mParameterObj.put("sh_status",2);
        }
        //请求数据
        if (isFirstLoad){
            isFirstLoad = false;
            query();
        }else
            mCurrentDateBtn.callOnClick();

        if (btn != mCurrentAuditStatusBtn){
            btn.setTextColor(white);
            btn.setBackground(DrawableUtil.createDrawable(corners,blue,0,blue));
            if (null != mCurrentAuditStatusBtn){
                mCurrentAuditStatusBtn.setTextColor(text_color);
                if (mCurrentAuditStatusBtn.getId() == R.id.m_unaudited_btn){
                    mCurrentAuditStatusBtn.setBackground(getDrawable(R.drawable.left_right_separator));
                }else
                    mCurrentAuditStatusBtn.setBackground(DrawableUtil.createDrawable(corners,white,0,blue));
            }
            mCurrentAuditStatusBtn = btn;
        }
    };


    private void setStartTime(final Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
    }
    private void setEndTime(final Calendar calendar){
        calendar.set(Calendar.HOUR_OF_DAY,23);
        calendar.set(Calendar.MINUTE,59);
        calendar.set(Calendar.SECOND,59);
    }
    private void initEndDateAndTime(){
        final TextView end_date = findViewById(R.id.m_end_date);
        if (null != end_date){
            end_date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
            end_date.setOnClickListener(v -> Utils.showDatePickerDialog(this,mCurrentDateBtn,(TextView) v, Calendar.getInstance()));

            mEndDateTv = end_date;
        }
    }
    private void initStartDateAndTime(){
        final TextView start_date = findViewById(R.id.m_start_date);
        if (null != start_date) {
            start_date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
            start_date.setOnClickListener(v -> Utils.showDatePickerDialog(this,mCurrentDateBtn,(TextView) v, Calendar.getInstance()));
            mStartDateTv = start_date;
        }
    }

    private void initOrderList(){
        final RecyclerView order_list = findViewById(R.id.m_order_list);
        if (null != order_list){
            mAdapter = getAdapter();
            order_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
            order_list.setAdapter(mAdapter);
            order_list.addItemDecoration(new LinearItemDecoration(this.getColor(R.color.gray_subtransparent)));
        }
    }
}