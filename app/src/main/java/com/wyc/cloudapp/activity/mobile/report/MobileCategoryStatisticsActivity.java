package com.wyc.cloudapp.activity.mobile.report;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.report.MobileCategoryContentAdapter;
import com.wyc.cloudapp.adapter.report.MobileCategoryNameAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class MobileCategoryStatisticsActivity extends AbstractReportActivity {

    private MobileCategoryNameAdapter mNames;
    private MobileCategoryContentAdapter mContents;
    private LinearLayout mSelectedCategory;
    private boolean isScroll = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDateCondition();
        initCategoryStatisticsList();
        initCategoryLayout();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_category_statistics;
    }


    private void initCategoryLayout(){
        mSelectedCategory = findViewById(R.id.selected_category_layout);
        final TextView first = findViewById(R.id.first_category_tv);
        if (first != null){
            first.setOnClickListener(mSelectedCategoryListener);
            first.setTag(-1);
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
                    ((TextView)view).setTextColor(getColor(R.color.colorPrimary));
                    view.setBackground(getDrawable(R.drawable.bottom_separator));
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

        final SimpleDateFormat sdf = new SimpleDateFormat(FormatDateTimeUtils.YYYY_MM_DD_1, Locale.CHINA);
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

            mQueryConditionObj.put("start_time",start_time / 1000);
            mQueryConditionObj.put("end_time",end_time / 1000);

            Logger.d("start:%s,end:%s",sdf.format(new Date(start_time)),sdf.format(new Date(end_time)));

            Logger.d_json(mQueryConditionObj.toString());

            getDatas();

        } catch (JSONException | ParseException e) {
            e.printStackTrace();
            Toast.makeText(this,"加载门店信息错误:" + e.getMessage(),Toast.LENGTH_LONG).show();
        }
    };

    private void getDatas(){
        final ProgressDialog progressDialog = ProgressDialog.show(this,"",getString(R.string.hints_query_data_sz),true);
        final JEventLoop loop = new JEventLoop();
        final StringBuilder err = new StringBuilder();

        CustomApplication.execute(()->{
            final JSONObject object = mQueryConditionObj;
            try {
                object.put("appid",mAppId);

                final JSONObject retJson = HttpUtils.sendPost(mUrl + "/api/boss/goods_sales_category", HttpRequest.generate_request_parm(object, mAppSecret),true);

                switch (retJson.getIntValue("flag")) {
                    case 0:
                        loop.done(0);
                        err.append(retJson.getString("info"));
                        break;
                    case 1:
                        final JSONObject info = JSON.parseObject(retJson.getString("info"));
                        final JSONArray datas = Utils.getNullObjectAsEmptyJsonArray(info,"data");
                        runOnUiThread(()->{
                            if (!datas.isEmpty()){
                                final JSONObject obj = datas.getJSONObject(0);
                                showTotalContent(info.getJSONObject("total_sum"), obj.getIntValue("depth") == 1);
                            }

                            mContents.setDataForArray(datas);
                            mNames.setDataForArray(datas);
                        });
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
        if (code != 1)Toast.makeText(this,err.toString(),Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    private void showTotalContent(final JSONObject object, boolean show){
        if (null == object)return;
        final LinearLayout linearLayout = findViewById(R.id.content_sum_layout);
        final TextView sum_tv = findViewById(R.id.sum_tv);
        if (show){
            linearLayout.setVisibility(View.VISIBLE);
            sum_tv.setVisibility(View.VISIBLE);

            final TextView total_sales_money = linearLayout.findViewById(R.id.total_sales_money),total_sum_xnum = findViewById(R.id.total_sum_xnum),
                    total_real_profit = linearLayout.findViewById(R.id.total_real_profit),total_real_profit_rate = linearLayout.findViewById(R.id.total_real_profit_rate);

            total_sum_xnum.setText(String.format(Locale.CHINA, "%.2f", object.getDoubleValue("total_sum_xnum")));
            total_sales_money.setText(String.format(Locale.CHINA, "%.2f", object.getDoubleValue("total_sales_money")));
            total_real_profit.setText(String.format(Locale.CHINA, "%.2f", object.getDoubleValue("total_real_profit")));
            total_real_profit_rate.setText(object.getString("total_real_profit_rate"));
        }else{
            linearLayout.setVisibility(View.GONE);
            sum_tv.setVisibility(View.GONE);
        }
    }

    private JSONObject getDate(){
        final JSONObject object = new JSONObject();
        final LinearLayout custom_date_layout = findViewById(R.id.custome_date_layout);
        final EditText s = custom_date_layout.findViewById(R.id.start_date),end = custom_date_layout.findViewById(R.id.end_date);
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
            }catch (JSONException | ParseException e){
                e.printStackTrace();
            }
        }
        return object;
    }

    private void showCustomDate(boolean b){
        final LinearLayout custom_date_layout = findViewById(R.id.custome_date_layout);
        final EditText s = custom_date_layout.findViewById(R.id.start_date),e = custom_date_layout.findViewById(R.id.end_date);
        if (b){
            if (custom_date_layout.getVisibility() == View.GONE){
                custom_date_layout.setVisibility(View.VISIBLE);
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                s.setText(sdf.format(new Date()));
                s.setOnClickListener(view ->showDatePickerDialog(this,s, Calendar.getInstance()));
                s.setOnFocusChangeListener((view, b12) -> {
                    if (b12)view.callOnClick();
                    Utils.hideKeyBoard(s);
                });
                e.setText(sdf.format(new Date()));
                e.setOnClickListener(view -> showDatePickerDialog(this,e,Calendar.getInstance()));
                e.setOnFocusChangeListener((view, b1) -> {
                    if (b1)view.callOnClick();
                    Utils.hideKeyBoard(e);
                });
            }
        }else {
            if (custom_date_layout.getVisibility() == View.VISIBLE){
                custom_date_layout.setVisibility(View.GONE);
                s.getText().clear();
                s.setOnClickListener(null);
                s.setOnFocusChangeListener(null);
                e.getText().clear();
                e.setOnClickListener(null);
                e.setOnFocusChangeListener(null);
            }
        }
    }

    private void initCategoryStatisticsList(){
        final RecyclerView name_list = findViewById(R.id.category_name_list),category_content_list = findViewById(R.id.category_content_list);
        mNames = new MobileCategoryNameAdapter(this);
        mNames.setItemListener(v -> {
            final String name = ((TextView)v).getText().toString();
            int  id = (int) v.getTag();
            Logger.d("id:%s,name:%s",id,name);
            addCategoryView(name.split("\n")[0],id);
            mQueryConditionObj.put("category_id",id);
            if (mCurrentDateView != null)mCurrentDateView.callOnClick();
        });
         name_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isScroll){
                    isScroll = true;
                    category_content_list.scrollBy(dx,dy);
                    isScroll = false;
                }
            }
        });
        name_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        name_list.setAdapter(mNames);

        mContents = new MobileCategoryContentAdapter(this);

        category_content_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!isScroll){
                    isScroll = true;
                    name_list.scrollBy(dx,dy);
                    isScroll = false;
                }
            }
        });
        category_content_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        category_content_list.setAdapter(mContents);
    }

    private void addCategoryView(final String name,int id){
        if (mSelectedCategory != null){
            final TextView end_tv = (TextView) mSelectedCategory.getChildAt(mSelectedCategory.getChildCount() - 1);
            if (end_tv != null){
                final Drawable drawable = getDrawable(R.drawable.fold);
                if (drawable != null) drawable.setBounds(0, 0, drawable.getIntrinsicWidth() / 2 , drawable.getIntrinsicHeight() / 2 );
                end_tv.setCompoundDrawables(null,null,drawable,null);
            }

            final TextView tv = new TextView(this);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(16);
            tv.setText(name);
            tv.setTag(id);
            tv.setOnClickListener(mSelectedCategoryListener);
            tv.setBackgroundColor(getColor(R.color.encode_view));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mSelectedCategory.addView(tv,lp);
        }
    }

    private final View.OnClickListener mSelectedCategoryListener  = (view)->{
        int counts = mSelectedCategory.getChildCount(),delete_counts = 0,delete_index = 0;
        for (int i = 0;i < counts;i++){
            final View tv = mSelectedCategory.getChildAt(i);
            if (view == tv){
                ((TextView)view).setCompoundDrawables(null,null,null,null);
                int category_id = (int) view.getTag();
                if (category_id == -1){
                    mQueryConditionObj.remove("category_id");
                }else{
                    mQueryConditionObj.put("category_id",category_id);
                }
                delete_index = i + 1;
                delete_counts = counts - delete_index;
                while (delete_counts -- > 0){
                    mSelectedCategory.removeView(mSelectedCategory.getChildAt(delete_index));
                }
                if (mCurrentDateView != null)mCurrentDateView.callOnClick();
                break;
            }
        }
    };
}