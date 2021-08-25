package com.wyc.cloudapp.activity.mobile.business;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.CustomizationView.InterceptLinearLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.business.MobileInventoryOrderAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.DrawableUtil;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
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
/*实盘数据录入单*/
public class MobilePracticalInventoryOrderActivity extends AbstractMobileActivity {
    public static String WH_ID_KEY = "wh_id";
    private long mStartTime = 0,mEndTime = 0;
    private Button mCurrentDateBtn;
    private TextView mStartDateTv,mEndDateTv;
    private MobileInventoryOrderAdapter mAdapter;
    private JSONObject mParameterObj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //验证权限
        if (!verifyPermission()){
            finish();
            return;
        }

        mParameterObj = new JSONObject();

        initQueryTimeBtn();
        initEndDateAndTime();
        initStartDateAndTime();
        initOrderList();
        initTitle();
    }

    public Class<?> jumpAddTarget() {
        return MobilePracticalInventoryAddOrderActivity.class;
    }

    private boolean verifyPermission(){
        boolean code = verifyPermissions("43",null,false);
        if (!code)MyDialog.toastMessage("当前操作员没有此功能权限!");
        return code;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCurrentDateBtn != null )mCurrentDateBtn.callOnClick();
    }

    private void initTitle(){
        setRightText(getString(R.string.add_sz));
        setRightListener(v -> add());
    }
    private void add(){
        final CharSequence title = getRightText().toString() + getMiddleText();
        final Intent intent = new Intent();
        intent.setClass(this, MobilePracticalInventoryAddOrderActivity.class);
        intent.putExtra(AbstractMobileActivity.TITLE_KEY, title);
        try {
            startActivity(intent);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
            MyDialog.ToastMessage("暂不支持" + title, null);
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
        query_time_btn_layout.post(today::callOnClick);
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
                final SimpleDateFormat sdf = new SimpleDateFormat(FormatDateTimeUtils.YYYY_MM_DD_1, Locale.CHINA);
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

        query();

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
        final SimpleDateFormat sdf = new SimpleDateFormat(FormatDateTimeUtils.YYYY_MM_DD_1, Locale.CHINA);

        mParameterObj.put("appid",getAppId());
        mParameterObj.put(WH_ID_KEY,getWhId());
        mParameterObj.put("pt_user_id",getPtUserId());
        mParameterObj.put("begin_time",sdf.format(new Date(mStartTime)));
        mParameterObj.put("end_time",sdf.format(new Date(mEndTime)));

        final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(this,getString(R.string.hints_query_data_sz));
        CustomApplication.execute(()->{
            final String param =  HttpRequest.generate_request_parm(mParameterObj,getAppSecret());

            Logger.d("业务查询参数:%s",param);

            final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/inventory/spd_list",param,true);
            if (HttpUtils.checkRequestSuccess(retJson)){
                try {
                    final JSONObject info = JSONObject.parseObject(retJson.getString("info"));
                    if (HttpUtils.checkBusinessSuccess(info)){
                        runOnUiThread(()-> mAdapter.setDataForArray(Utils.getNullObjectAsEmptyJsonArray(info,"data")));
                    }else {
                        throw new JSONException(info.getString("info"));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.ToastMessageInMainThread(e.getMessage());
                }
            }
            progressDialog.dismiss();
        });
    }

    protected boolean orderIsNotShow(final JSONObject order){
        /*
         * 判断线上返回的订单信息是否需要显示
         *
         * 默认实现采购入库单查询来源采购订货单时，如果rk_status==3 完全入库的情况从显示列表中删除
         * */
        return order != null && order.getIntValue("rk_status") == 3;
    }


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
            final Calendar rightNow = Calendar.getInstance();
            rightNow.add(Calendar.DATE,-6);
            start_date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(rightNow.getTime()));
            start_date.setOnClickListener(v -> Utils.showDatePickerDialog(this,mCurrentDateBtn,(TextView) v, Calendar.getInstance()));
            mStartDateTv = start_date;
        }
    }

    private void initOrderList(){
        final RecyclerView order_list = findViewById(R.id.m_order_list);
        if (null != order_list){
            mAdapter = new MobileInventoryOrderAdapter(this);
            order_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
            order_list.setAdapter(mAdapter);
            order_list.addItemDecoration(new LinearItemDecoration(this.getColor(R.color.gray_subtransparent)));
        }
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_inventory_order;
    }
}