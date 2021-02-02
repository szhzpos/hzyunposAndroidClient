package com.wyc.cloudapp.activity.mobile.report;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.report.MobileDealQueryAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DealQueryActivity extends AbstractMobileActivity {
    private JSONObject mQueryCondition;
    private MobileDealQueryAdapter mAdapter;
    private View mCurrentDateView;
    private boolean isFirst = true;
    private Button mQuery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMiddleText(getStoreName());
        mQueryCondition = new JSONObject();
        mQueryCondition.put("stores_id",getStoreId());

        initVipAndOrder();
        initOrderList();

        initDateCondition();
        initQueryBtn();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_deal_query;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (isFirst && null != mCurrentDateView){
            mCurrentDateView.post(()-> mCurrentDateView.callOnClick());
            isFirst = false;
        }
    }

    private void initDateCondition(){
        final LinearLayout date_layout = findViewById(R.id.date_layout);
        View view;
        if (null != date_layout){
            for (int i = 0,counts = date_layout.getChildCount();i < counts; i++){
                view = date_layout.getChildAt(i);
                view.setOnClickListener(date_view_listener);
                if (view.getId() == R.id.today_tv){
                    ((TextView)view).setTextColor(getColor(R.color.lightBlue));
                    view.setBackground(getDrawable(R.drawable.mobile_report_date_btn_style));
                    mCurrentDateView = view;
                }
            }
        }
    }

    private final View.OnClickListener date_view_listener = view -> {
        if (mCurrentDateView != view){
            if (mCurrentDateView != null){
                ((TextView)mCurrentDateView).setTextColor(getColor(R.color.text_color));
                mCurrentDateView.setBackgroundColor(Color.WHITE);
            }
            mCurrentDateView = view;
            ((TextView)view).setTextColor(getColor(R.color.lightBlue));
            view.setBackground(getDrawable(R.drawable.mobile_report_date_btn_style));
        }

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
        final Calendar rightNow = Calendar.getInstance();
        rightNow.setTimeZone(TimeZone.getDefault());
        long start_time = -1,end_time = -1;
        try {
            switch (view.getId()){
                case R.id.today_tv:
                    showCustomDate(false);

                    setStartTime(rightNow);
                    start_time = rightNow.getTime().getTime();

                    setEndTime(rightNow);
                    end_time = rightNow.getTime().getTime();

                    break;
                case R.id.yestoday_tv:
                    showCustomDate(false);

                    rightNow.add(Calendar.DAY_OF_YEAR,-1);

                    setStartTime(rightNow);
                    start_time = rightNow.getTime().getTime();

                    setEndTime(rightNow);
                    end_time = rightNow.getTime().getTime();

                    break;
                case R.id.this_week_tv:
                    showCustomDate(false);

                    rightNow.set(Calendar.DAY_OF_WEEK,Calendar.MONDAY);
                    setStartTime(rightNow);
                    start_time = rightNow.getTime().getTime();

                    rightNow.add(Calendar.DATE,6);
                    setEndTime(rightNow);
                    end_time = rightNow.getTime().getTime();

                    break;
                case R.id.last_week_tv:
                    showCustomDate(false);

                    int dayofweek = rightNow.get(Calendar.DAY_OF_WEEK);
                    if (dayofweek == 1) {
                        dayofweek += 7;
                    }
                    rightNow.add(Calendar.DATE, 2 - dayofweek - 7);

                    setStartTime(rightNow);
                    start_time = rightNow.getTime().getTime();

                    rightNow.add(Calendar.DATE,6);
                    setEndTime(rightNow);
                    end_time = rightNow.getTime().getTime();
                    break;
                case R.id.this_month_tv:
                    showCustomDate(false);

                    rightNow.set(rightNow.get(Calendar.YEAR),rightNow.get(Calendar.MONTH),1);
                    setStartTime(rightNow);
                    start_time = rightNow.getTime().getTime();

                    int day = rightNow.getActualMaximum(5);
                    rightNow.set(rightNow.get(Calendar.YEAR),rightNow.get(Calendar.MONTH),day);

                    setEndTime(rightNow);
                    end_time = rightNow.getTime().getTime();
                    break;
                case R.id.custom_date_tv:
                    showCustomDate(true);

                    final JSONObject date_obj = getDate();
                    if (date_obj.size() == 0){

                        return;
                    }

                    final String st = Utils.getNullStringAsEmpty(date_obj,"s"),et = Utils.getNullStringAsEmpty(date_obj,"e");
                    if (Utils.verifyDate(st) && Utils.verifyDate(et)){
                        rightNow.setTime(sdf.parse(st + " 00:00:00"));
                        start_time = rightNow.getTime().getTime();
                        rightNow.setTime(sdf.parse(et + " 23:59:59"));
                        end_time = rightNow.getTime().getTime();
                    }
                    break;
            }

            mQueryCondition.put("start_time",start_time / 1000);
            mQueryCondition.put("end_time",end_time / 1000);

            Logger.d("start:%s,end:%s",sdf.format(new Date(start_time)),sdf.format(new Date(end_time)));

            Logger.d_json(mQueryCondition.toString());

            getDatas();

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
            Toast.makeText(this,"加载门店信息错误:" + e.getMessage(),Toast.LENGTH_LONG).show();
        }
    };

    private JSONObject getDate(){
        final JSONObject object = new JSONObject();
        final LinearLayout custome_date_layout = findViewById(R.id.custome_date_layout);
        final EditText s = custome_date_layout.findViewById(R.id.start_date),end = custome_date_layout.findViewById(R.id.end_date);
        if (s != null && end != null){
            try{
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                final Calendar calendar = Calendar.getInstance();
                final String start_t = s.getText().toString(),end_t = end.getText().toString();

                calendar.setTime(sdf.parse(start_t));
                long st = calendar.getTimeInMillis();
                calendar.setTime(sdf.parse(end_t));
                long et = calendar.getTimeInMillis();

                int between_days = (int) ((et - st) / (1000 * 3600 * 24));

                Logger.d("between_days:%d",between_days);
                if (between_days >= 0 && between_days <= 60){
                    object.put("s",start_t);
                    object.put("e",end_t);
                }else {
                    Toast.makeText(this,"只能查询60日内的数据!",Toast.LENGTH_SHORT).show();
                }
            }catch ( JSONException | ParseException e){
                e.printStackTrace();
            }
        }
        return object;
    }

    private void getDatas(){
        final ProgressDialog progressDialog = ProgressDialog.show(this,"","正在查询数据...",true);
        final JEventLoop loop = new JEventLoop();
        final StringBuilder err = new StringBuilder();

        CustomApplication.execute(()->{
            final  JSONObject object = mQueryCondition;
            try {
                object.put("appid",mAppId);

                final JSONObject retJson = HttpUtils.sendPost(mUrl + "/api_v2/boss/get_retail_order",HttpRequest.generate_request_parm(object, mAppSecret),true);

                switch (retJson.getIntValue("flag")) {
                    case 0:
                        loop.done(0);
                        err.append(retJson.getString("info"));
                        break;
                    case 1:
                        final JSONObject info = JSONObject.parseObject(retJson.getString("info"));
                        this.runOnUiThread(()-> showOrderInfo(Utils.getNullObjectAsEmptyJson(info,"data")));
                        loop.done(1);
                        break;
                }

            } catch (JSONException e) {
                e.printStackTrace();
                err.append(e.getMessage());
                loop.done(0);
            }
        });
        final int code = loop.exec();
        if (code != 1)Toast.makeText(this,err.toString(),Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    private void showOrderInfo(final JSONObject data){
        final TextView orders_num_tv = findViewById(R.id.orders_num_tv),orders_amt_tv = findViewById(R.id.orders_amt_tv);
        if (null != orders_num_tv && null != orders_amt_tv){
            int order = 0;
            double order_moneys = 0.0;
            JSONArray orders = null;
            if (!data.isEmpty()){
                orders = data.getJSONArray("order");

                order = orders.size();
                order_moneys = data.getDoubleValue("discount_prices");
            }else {
                Toast.makeText(this,"暂无数据!",Toast.LENGTH_SHORT).show();
            }
            mAdapter.setDataForArray(orders);
            orders_num_tv.setText(String.valueOf(order));
            orders_amt_tv.setText(String.format(Locale.CHINA,"%.2f",order_moneys));
        }
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

    private void showCustomDate(boolean b){
        final LinearLayout custome_date_layout = findViewById(R.id.custome_date_layout);
        final EditText s = custome_date_layout.findViewById(R.id.start_date),e = custome_date_layout.findViewById(R.id.end_date);
        if (b){
            if (custome_date_layout.getVisibility() == View.GONE){
                custome_date_layout.setVisibility(View.VISIBLE);
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                s.setText(sdf.format(new Date()));
                s.setOnClickListener(view ->showDatePickerDialog(DealQueryActivity.this,s,Calendar.getInstance()));
                s.setOnFocusChangeListener((view, b12) -> {
                    if (b12)view.callOnClick();
                    Utils.hideKeyBoard(s);
                });
                e.setText(sdf.format(new Date()));
                e.setOnClickListener(view -> showDatePickerDialog(DealQueryActivity.this,e,Calendar.getInstance()));
                e.setOnFocusChangeListener((view, b1) -> {
                    if (b1)view.callOnClick();
                    Utils.hideKeyBoard(e);
                });
            }
        }else {
            if (custome_date_layout.getVisibility() == View.VISIBLE){
                custome_date_layout.setVisibility(View.GONE);
                s.getText().clear();
                s.setOnClickListener(null);
                s.setOnFocusChangeListener(null);
                e.getText().clear();
                e.setOnClickListener(null);
                e.setOnFocusChangeListener(null);
            }
        }
    }

    public void showDatePickerDialog(final Context context, final TextView tv, Calendar calendar) {
        new DatePickerDialog(context,
                (view, year, monthOfYear, dayOfMonth) -> {
                    tv.setText(String.format(Locale.CHINA,"%d-%02d-%02d",year,monthOfYear + 1,dayOfMonth));
                    if (mQuery != null)mQuery.callOnClick();
                }
                // 设置初始日期
                , calendar.get(Calendar.YEAR)
                ,calendar.get(Calendar.MONTH)
                ,calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void initOrderList(){
        final RecyclerView recyclerView  = findViewById(R.id.order_list);
        if (null != recyclerView){
            mAdapter = new MobileDealQueryAdapter(this);
            mAdapter.setItemClickListener(record -> {
                final String order_code = record.getString("order_code");
                queryOrderDetails(order_code);
            });
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
            recyclerView.setAdapter(mAdapter);
        }
    }
    private void queryOrderDetails(final String order_code){
        final ProgressDialog progressDialog = ProgressDialog.show(this,"","正在查询数据...",true);
        final JEventLoop loop = new JEventLoop();
        final StringBuilder err = new StringBuilder();

        CustomApplication.execute(()->{

            final JSONObject object = new JSONObject();
            try {
                object.put("appid", mAppId);
                object.put("order_code",order_code);

                final JSONObject retJson = HttpUtils.sendPost(mUrl + "/api_v2/boss/get_retail_order_goods", HttpRequest.generate_request_parm(object, mAppSecret),true);

                switch (retJson.getIntValue("flag")) {
                    case 0:
                        loop.done(0);
                        err.append(retJson.getString("info"));
                        break;
                    case 1:
                        final JSONObject info = JSON.parseObject(retJson.getString("info"));
                        final JSONObject data = Utils.getNullObjectAsEmptyJson(info,"data");
                        if (!data.isEmpty()){
                            data.put("stores_name",getMiddleText());
                            final Activity activity = DealQueryActivity.this;
                            activity.runOnUiThread(()->{
                                final Intent intent = new Intent();
                                intent.putExtra("order_info",data.toString());
                                intent.setClass(activity, OrderDetailsActivity.class);
                                startActivity(intent);
                            });
                        }
                        loop.done(1);
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
                err.append(e.getMessage());
                loop.done(0);
            }
        });
        final int code = loop.exec();
        if (code != 1) Toast.makeText(this,err.toString(),Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }
    private void initVipAndOrder(){
        final EditText vip_id_order_id_et = findViewById(R.id.vip_id_order_id_et);
        if (vip_id_order_id_et != null){
            vip_id_order_id_et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    final String sz = editable.toString();
                    if (!"".equals(sz)){
                        if (sz.startsWith("N")){
                            mQueryCondition.remove("members");
                            mQueryCondition.put("order_code",sz);
                        }else {
                            mQueryCondition.remove("order_code");
                            mQueryCondition.put("members",sz);
                        }
                    }else {
                        mQueryCondition.remove("members");
                        mQueryCondition.remove("order_code");
                    }
                }
            });
        }
    }
    private void initQueryBtn(){
        final Button btn = findViewById(R.id.query);
        if (btn != null){
            btn.setOnClickListener(v -> {
                if (mCurrentDateView != null)mCurrentDateView.callOnClick();
            });
        }
        mQuery = btn;
    }
}