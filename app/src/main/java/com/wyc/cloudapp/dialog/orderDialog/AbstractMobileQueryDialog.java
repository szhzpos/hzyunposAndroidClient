package com.wyc.cloudapp.dialog.orderDialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.TreeListDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.DrawableUtil;
import com.wyc.cloudapp.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public abstract class AbstractMobileQueryDialog extends AbstractDialogMainActivity {
    protected long mStartTime,mEndTime;
    protected Button mCurrentDateBtn;
    protected EditText mSearchContent;
    protected TextView mStartDateTv,mEndDateTv;
    private AbstractQueryDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> mAdapter;
    public AbstractMobileQueryDialog(@NonNull MainActivity context, CharSequence title) {
        super(context, title);
    }

    public abstract AbstractQueryDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter();
    public abstract String generateQueryCondition();
    public abstract JSONArray getConditionSwitchContent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initQueryTimeBtn();
        initEndDateAndTime();
        initStartDateAndTime();
        initOrderList();
        initSearchContent();
        initSwitchCondition();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        initTodayBtn();
    }
    protected double getWidthRatio(){
        return ViewGroup.LayoutParams.MATCH_PARENT;
    }
    @Override
    protected double getHeightRatio(){
        return ViewGroup.LayoutParams.MATCH_PARENT;
    }

    private void initTodayBtn(){
        final Button today = findViewById(R.id.m_today_btn);
        today.setOnClickListener(mClickListener);
        CustomApplication.runInMainThread(today::callOnClick);
    }

    private void initOrderList(){
        final RecyclerView order_list = findViewById(R.id.m_order_list);
        if (null != order_list){
            mAdapter = getAdapter();
            order_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            order_list.setAdapter(mAdapter);
            order_list.addItemDecoration(new LinearItemDecoration(mContext.getColor(R.color.white)));
        }
    }

    private void initEndDateAndTime(){
        final TextView end_date = findViewById(R.id.m_end_date);
        if (null != end_date){
            end_date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
            end_date.setOnClickListener(v -> Utils.showDatePickerDialog(mContext,(TextView) v, Calendar.getInstance()));

            mEndDateTv = end_date;
        }
    }
    private void initStartDateAndTime(){
        final TextView start_date = findViewById(R.id.m_start_date);
        if (null != start_date) {
            start_date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
            start_date.setOnClickListener(v -> Utils.showDatePickerDialog(mContext, (TextView) v, Calendar.getInstance()));
            mStartDateTv = start_date;
        }
    }

    private void initQueryTimeBtn(){
        final LinearLayout query_time_btn_layout = findViewById(R.id.query_time_btn_layout);
        final Button yesterday = query_time_btn_layout.findViewById(R.id.m_yesterday_btn),
                other = query_time_btn_layout.findViewById(R.id.m_other_btn);
        CustomApplication.runInMainThread(()->{
            float corner_size = query_time_btn_layout.getHeight() / 2.0f;
            query_time_btn_layout.setForeground(DrawableUtil.createDrawable(new float[]{corner_size,corner_size,corner_size,corner_size,corner_size,corner_size,corner_size,corner_size}
                    ,mContext.getColor(R.color.transparent),Utils.dpToPx(mContext,1),mContext.getColor(R.color.blue)));
        });
        yesterday.setOnClickListener(mClickListener);
        other.setOnClickListener(mClickListener);
    }

    private final View.OnClickListener mClickListener = v -> {
        final Button btn = (Button) v;

        int white = mContext.getColor(R.color.white),text_color = mContext.getColor(R.color.text_color),blue = mContext.getColor(R.color.blue);
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
                    mCurrentDateBtn.setBackground(mContext.getDrawable(R.drawable.left_right_separator));
                }else
                    mCurrentDateBtn.setBackground(DrawableUtil.createDrawable(corners,white,0,blue));
            }
            mCurrentDateBtn = btn;
        }
        query();
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

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText order_vip__search = findViewById(R.id.order_vip__search);
        order_vip__search.setTransformationMethod(new ReplacementTransformationMethod() {
            @Override
            protected char[] getOriginal() {
                return new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
            }

            @Override
            protected char[] getReplacement() {
                return new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
                        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
            }
        });
        order_vip__search.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_UP){
                if (mCurrentDateBtn != null)mCurrentDateBtn.callOnClick();
                return true;
            }
            return false;
        });
        order_vip__search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = order_vip__search.getWidth();
                if (dx > (w - order_vip__search.getCompoundPaddingRight())) {
                    if (mCurrentDateBtn != null)mCurrentDateBtn.callOnClick();
                }
            }
            return false;
        });

        mSearchContent = order_vip__search;
    }

    private void initSwitchCondition(){
        final TextView switch_condition = findViewById(R.id.switch_condition);
        final JSONArray array = createSwitchConditionContentAndSetDefaultValue(switch_condition);
        switch_condition.setOnClickListener(v -> {
            final TreeListDialog treeListDialog = new TreeListDialog(mContext,mContext.getString(R.string.query_way_sz));
            treeListDialog.setDatas(array,null,true);
            CustomApplication.runInMainThread(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    switch_condition.setTag(object.getIntValue(TreeListBaseAdapter.COL_ID));
                    switch_condition.setText(object.getString(TreeListBaseAdapter.COL_NAME));
                }
            });
        });
    }
    private JSONArray createSwitchConditionContentAndSetDefaultValue(@NonNull final TextView view){
        final JSONArray array = getConditionSwitchContent();
        if (!array.isEmpty()){
            final JSONObject object = array.getJSONObject(0);
            view.setTag(object.getIntValue(TreeListBaseAdapter.COL_ID));
            view.setText(object.getString(TreeListBaseAdapter.COL_NAME));
        }
        return array;
    }

    private void query(){
        mAdapter.setDatas(generateQueryCondition());
    }
}
