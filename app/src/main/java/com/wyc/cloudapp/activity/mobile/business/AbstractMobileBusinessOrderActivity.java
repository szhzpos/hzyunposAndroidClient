package com.wyc.cloudapp.activity.mobile.business;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.DrawableUtil;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public abstract class AbstractMobileBusinessOrderActivity extends AbstractMobileActivity {
    protected long mStartTime,mEndTime;
    protected Button mCurrentDateBtn,mCurrentAuditStatusBtn;
    protected TextView mStartDateTv,mEndDateTv;
    private AbstractQueryDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initQueryTimeBtn();
        initAuditBtn();
        initEndDateAndTime();
        initStartDateAndTime();
        initOrderList();
        initTitle();
    }

    protected abstract AbstractQueryDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter();
    protected abstract void add();
    protected abstract JSONObject generateQueryCondition();

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_business_order;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initTitle(){
        final Intent intent = getIntent();
        if (intent != null)setMiddleText(intent.getStringExtra("title"));
        setRightText(getString(R.string.add_sz));
        setRightListener(v -> add());
    }

    private void initQueryTimeBtn(){
        final LinearLayout query_time_btn_layout = findViewById(R.id.query_time_btn_layout);
        final Button yesterday = query_time_btn_layout.findViewById(R.id.m_yesterday_btn),today = findViewById(R.id.m_today_btn),
                other = query_time_btn_layout.findViewById(R.id.m_other_btn);
        query_time_btn_layout.post(()->{
            float corner_size = query_time_btn_layout.getHeight() / 2.0f;
            query_time_btn_layout.setForeground(DrawableUtil.createDrawable(new float[]{corner_size,corner_size,corner_size,corner_size,corner_size,corner_size,corner_size,corner_size}
                    ,getColor(R.color.transparent), Utils.dpToPx(this,1),getColor(R.color.blue)));
        });
        yesterday.setOnClickListener(mClickListener);
        other.setOnClickListener(mClickListener);
        today.setOnClickListener(mClickListener);

        today.post(today::callOnClick);
    }
    private final View.OnClickListener mClickListener = v -> {
        final Button btn = (Button) v;

        int white = getColor(R.color.white),text_color = getColor(R.color.text_color),blue = getColor(R.color.blue);
        final int id = btn.getId();

        final LinearLayout m_query_time_tv_layout = findViewById(R.id.m_query_time_tv_layout);
        float corner_size = (float) (btn.getHeight() / 2.0);
        float[] corners = new float[8];


        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
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


            Logger.d("start:%s,end:%s",sdf.format(new Date(mStartTime)),sdf.format(new Date(mEndTime)));
        }else if (id == R.id.m_yesterday_btn){
            m_query_time_tv_layout.setVisibility(View.GONE);

            rightNow.setTime(new Date());

            rightNow.add(Calendar.DAY_OF_YEAR,-1);

            setStartTime(rightNow);
            mStartTime = rightNow.getTime().getTime();

            setEndTime(rightNow);
            mEndTime = rightNow.getTime().getTime();

            Logger.d("start:%s,end:%s",sdf.format(new Date(mStartTime)),sdf.format(new Date(mEndTime)));
        }else if (id == R.id.m_other_btn){
            m_query_time_tv_layout.setVisibility(View.VISIBLE);
            corners[2] = corners[3] =  corners[4] = corners[5] = corner_size;
            if (mEndDateTv != null && mStartDateTv != null){
                try {
                    rightNow.setTime(Objects.requireNonNull(sdf.parse(mStartDateTv.getText() + " 00:00:00")));
                    mStartTime = rightNow.getTime().getTime();
                    rightNow.setTime(Objects.requireNonNull(sdf.parse(mEndDateTv.getText() + " 23:59:59")));
                    mEndTime = rightNow.getTime().getTime();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            Logger.d("start:%s,end:%s",sdf.format(new Date(mStartTime)),sdf.format(new Date(mEndTime)));
        }
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

    private void initAuditBtn(){
        final LinearLayout audit_btn_layout = findViewById(R.id.audit_btn_layout);
        final Button m_all_btn = audit_btn_layout.findViewById(R.id.m_all_btn),m_audit_btn = audit_btn_layout.findViewById(R.id.m_audit_btn),
                m_unaudited_btn = audit_btn_layout.findViewById(R.id.m_unaudited_btn);
        audit_btn_layout.post(()->{
            float corner_size = audit_btn_layout.getHeight() / 2.0f;
            audit_btn_layout.setForeground(DrawableUtil.createDrawable(new float[]{corner_size,corner_size,corner_size,corner_size,corner_size,corner_size,corner_size,corner_size}
                    ,getColor(R.color.transparent), Utils.dpToPx(this,1),getColor(R.color.blue)));
        });
        m_all_btn.setOnClickListener(mAuditStatusClickListener);
        m_audit_btn.setOnClickListener(mAuditStatusClickListener);
        m_unaudited_btn.setOnClickListener(mAuditStatusClickListener);

        m_all_btn.post(m_all_btn::callOnClick);
    }

    private final View.OnClickListener mAuditStatusClickListener = v -> {
        final Button btn = (Button) v;

        int white = getColor(R.color.white),text_color = getColor(R.color.text_color),blue = getColor(R.color.blue);
        final int id = btn.getId();

        float corner_size = (float) (btn.getHeight() / 2.0);
        float[] corners = new float[8];

        if (id == R.id.m_all_btn){
            corners[0] = corners[1] =  corners[6] = corners[7] = corner_size;

        }else if (id == R.id.m_unaudited_btn){

        }else if (id == R.id.m_audit_btn){
            corners[2] = corners[3] =  corners[4] = corners[5] = corner_size;
        }
        if (btn != mCurrentAuditStatusBtn){
            btn.setTextColor(white);
            btn.setBackground(DrawableUtil.createDrawable(corners,blue,0,blue));
            if (null != mCurrentAuditStatusBtn){
                mCurrentAuditStatusBtn.setTextColor(text_color);
                if (mCurrentAuditStatusBtn.getId() == R.id.m_yesterday_btn){
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
            end_date.setOnClickListener(v -> Utils.showDatePickerDialog(this,(TextView) v, Calendar.getInstance()));

            mEndDateTv = end_date;
        }
    }
    private void initStartDateAndTime(){
        final TextView start_date = findViewById(R.id.m_start_date);
        if (null != start_date) {
            start_date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
            start_date.setOnClickListener(v -> Utils.showDatePickerDialog(this, (TextView) v, Calendar.getInstance()));
            mStartDateTv = start_date;
        }
    }

    private void initOrderList(){
        final RecyclerView order_list = findViewById(R.id.m_order_list);
        if (null != order_list){
            mAdapter = getAdapter();
            order_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
            order_list.setAdapter(mAdapter);
            order_list.addItemDecoration(new LinearItemDecoration(this.getColor(R.color.white)));
        }
    }
}