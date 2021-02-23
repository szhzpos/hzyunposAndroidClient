package com.wyc.cloudapp.activity.mobile.report;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
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
import com.wyc.cloudapp.adapter.report.CashierTransferContentAdapter;
import com.wyc.cloudapp.adapter.report.CashierTransferNameAdapter;
import com.wyc.cloudapp.adapter.report.TransferDetailsAdapter;
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
import java.util.Objects;
import java.util.TimeZone;

public class MobileCashierTransfer extends AbstractReportActivity {
    private CashierTransferContentAdapter mAdapter;
    private CashierTransferNameAdapter mNameAdapter;
    private boolean isFirst = true,isScroll = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initDateCondition();
        initTransferInfoList();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_cashier_transfer;
    }

    @Override
    public void onResume(){
        super.onResume();
        if (isFirst && null != mCurrentDateView){
            mCurrentDateView.post(()-> mCurrentDateView.callOnClick());
            isFirst = false;
        }
    }

    private void initTransferInfoList(){
        final RecyclerView content_list  = findViewById(R.id.transfer_order_list),cashier_name_list = findViewById(R.id.cashier_name_list);
        if (null != content_list && null != cashier_name_list){
            mAdapter = new CashierTransferContentAdapter(this);
            content_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (!isScroll){
                        isScroll = true;
                        cashier_name_list.scrollBy(dx,dy);
                        isScroll = false;
                    }
                }
            });
            content_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
            content_list.setAdapter(mAdapter);


            mNameAdapter = new CashierTransferNameAdapter(this);
            mNameAdapter.setItemListener(view -> {
                final TextView ti_tv = (TextView)view;
                queryDetails(ti_tv.getText().toString());
            });
            cashier_name_list.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (!isScroll){
                        isScroll = true;
                        content_list.scrollBy(dx,dy);
                        isScroll = false;
                    }
                }
            });
            cashier_name_list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
            cashier_name_list.setAdapter(mNameAdapter);
        }

    }

    private void queryDetails(final String ti_code){
        final ProgressDialog progressDialog = ProgressDialog.show(this,"",getString(R.string.hints_query_data_sz),true);
        final JEventLoop loop = new JEventLoop();
        final StringBuilder err = new StringBuilder();

        CustomApplication.execute(()->{
            final JSONObject object = new JSONObject();
            try {
                object.put("appid",getAppId());
                object.put("stores_id",mQueryConditionObj.getIntValue("stores_id"));
                object.put("ti_code",ti_code);

                final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/transfer/get_transfer_detail",HttpRequest.generate_request_parm(object, getAppSecret()),true);
                switch (retJson.getIntValue("flag")) {
                    case 0:
                        loop.done(0);
                        err.append(retJson.getString("info"));
                        break;
                    case 1:
                        final JSONObject info = JSON.parseObject(retJson.getString("info"));
                        final JSONObject data = Utils.getNullObjectAsEmptyJson(info,"data");
                        if (!data.isEmpty()){
                            data.put("stores_name",getStoreName());
                            runOnUiThread(()->{
                                final Intent intent = new Intent();
                                intent.putExtra("data",data.toString());
                                intent.setClass(this, TransferDetailsActivity.class);
                                startActivity(intent);
                            });
                        }
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
            ((TextView)view).setTextColor(getColor(R.color.lightBlue));
            view.setBackground(getDrawable(R.drawable.mobile_report_date_btn_style));
            mCurrentDateView = view;
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
                        rightNow.setTime(Objects.requireNonNull(sdf.parse(st + " 00:00:00")));
                        start_time = rightNow.getTime().getTime();
                        rightNow.setTime(Objects.requireNonNull(sdf.parse(et + " 23:59:59")));
                        end_time = rightNow.getTime().getTime();
                    }
                    break;
            }

            mQueryConditionObj.put("start_time",start_time / 1000);
            mQueryConditionObj.put("end_time",end_time / 1000);

            Logger.d("start:%s,end:%s",sdf.format(new Date(start_time)),sdf.format(new Date(end_time)));

            Logger.d(mQueryConditionObj.toString());

            getDatas();

        } catch (ParseException e) {
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
                object.put("appid",getAppId());

                final JSONObject retJson = HttpUtils.sendPost(getUrl() + "/api/transfer/get_transfer_list", HttpRequest.generate_request_parm(object, getAppSecret()),true);
                int code = 0;
                switch (retJson.getIntValue("flag")) {
                    case 0:
                        err.append(retJson.getString("info"));
                        break;
                    case 1:
                        final JSONObject info = JSON.parseObject(retJson.getString("info"));

                        if ("y".equals(info.getString("status"))){
                            if (mAdapter != null){
                                final JSONArray array = info.getJSONArray("data");
                                if (array != null) {
                                    if (array.size() != 0) {
                                        code = 1;
                                    }else {
                                        err.append(info.getString("info"));
                                    }
                                    runOnUiThread(() -> {
                                        mAdapter.setDataForArray(array);
                                        mNameAdapter.setDataForArray(array);
                                    });
                                }else {
                                    err.append(info.getString("info"));
                                }
                            }
                        }else{
                            err.append(info.getString("info"));
                        }
                        break;
                }
                loop.done(code);
            } catch (JSONException e) {
                e.printStackTrace();
                err.append(e.getMessage());
                loop.done(0);
            }
        });
        final int code = loop.exec();
        if (code != 1)Toast.makeText(this,err.toString(),Toast.LENGTH_SHORT).show();
        progressDialog.dismiss();
    }

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
                if (between_days >= 0 && between_days <= 180){
                    object.put("s",start_t);
                    object.put("e",end_t);
                }else {
                    Toast.makeText(this,"只能查询180日内的数据!",Toast.LENGTH_SHORT).show();
                }
            }catch (JSONException | ParseException e){
                e.printStackTrace();
            }
        }
        return object;
    }

    private void showCustomDate(boolean b){
        final LinearLayout custome_date_layout = findViewById(R.id.custome_date_layout);
        final EditText s = custome_date_layout.findViewById(R.id.start_date),e = custome_date_layout.findViewById(R.id.end_date);
        if (b){
            if (custome_date_layout.getVisibility() == View.GONE){
                custome_date_layout.setVisibility(View.VISIBLE);
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                s.setText(sdf.format(new Date()));
                s.setOnClickListener(view ->showDatePickerDialog(this,s,Calendar.getInstance()));
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


    public static final class TransferDetailsActivity extends AbstractMobileActivity{
        private JSONObject mData;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            initData();
            iniMenu();
            initDetailsView();
            initOrderInfo();
            initSumInfo();
        }

        @Override
        protected int getContentLayoutId() {
            return R.layout.activity_transfer_details;
        }

        private void initSumInfo(){
            final TextView amt_money_tv = findViewById(R.id.amt_money_tv),trans_money_tv = findViewById(R.id.trans_money_tv),
                    sj_money_tv = findViewById(R.id.sj_money_tv),cashbox_money_tv = findViewById(R.id.cashbox_money_tv);

            amt_money_tv.setText(String.format(Locale.CHINA,"%.2f",mData.getDoubleValue("sum_sk")));
            trans_money_tv.setText(String.format(Locale.CHINA,"%.2f",mData.getDoubleValue("sum_money")));
            sj_money_tv.setText(String.format(Locale.CHINA,"%.2f",mData.getDoubleValue("sj_money")));
            cashbox_money_tv.setText(getString(R.string.zero_p_z_sz));
        }

        private void initOrderInfo(){
            final TextView ti_code_tv = findViewById(R.id.ti_code_tv),cas_name = findViewById(R.id.cas_name_tv),
                    upload_time_tv = findViewById(R.id.upload_time_tv),ti_start_time_tv = findViewById(R.id.ti_start_time_tv),
                    ti_end_time_tv = findViewById(R.id.ti_end_time_tv);
            final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

            ti_code_tv.setText(mData.getString("ti_code"));
            cas_name.setText(mData.getString("cas_name"));
            upload_time_tv.setText(sf.format(mData.getLongValue("upload_time") * 1000));
            ti_start_time_tv.setText(sf.format(mData.getLongValue("ti_start_time") * 1000));
            ti_end_time_tv.setText(sf.format(mData.getLongValue("ti_end_time") * 1000));
        }

        private void initDetailsView(){
            final RecyclerView sale_pay_details = findViewById(R.id.sale_pay_details),refund_pay_details = findViewById(R.id.refund_pay_details),
                    recharge_pay_details = findViewById(R.id.recharge_pay_details),cik_pay_details = findViewById(R.id.cik_pay_details);

            JSONArray transfer_money_info = mData.getJSONArray("transfer_money_info"),transfer_refund_money = mData.getJSONArray("transfer_refund_money"),
                    transfer_recharge_money = mData.getJSONArray("transfer_recharge_money"),transfer_oncecard_money = mData.getJSONArray("transfer_oncecard_money");

            final JSONObject obj = new JSONObject();
            obj.put("pay_name","暂无数据");
            obj.put("pay_money",0.0);
            obj.put("order_num",0);
            JSONObject sum_obj = null;

            if (transfer_money_info == null){
                transfer_money_info = new JSONArray();
                sum_obj = obj;
            }else{
                sum_obj = new  JSONObject();
                sum_obj.put("pay_name","合计");
                sum_obj.put("pay_money",mData.getDoubleValue("order_money"));
                sum_obj.put("order_num",mData.getIntValue("order_num"));
            }
            transfer_money_info.add(sum_obj);


            if (transfer_refund_money == null){
                transfer_refund_money = new JSONArray();
                sum_obj = obj;
            }else{
                sum_obj = new JSONObject();
                sum_obj.put("pay_name","合计");
                sum_obj.put("pay_money",mData.getDoubleValue("refund_money"));
                sum_obj.put("order_num",mData.getIntValue("refund_num"));
            }
            transfer_refund_money.add(sum_obj);

            if (transfer_recharge_money == null){
                transfer_recharge_money = new JSONArray();
                sum_obj = obj;
            }else{
                sum_obj = new JSONObject();
                sum_obj.put("pay_name","合计");
                sum_obj.put("pay_money",mData.getDoubleValue("recharge_money"));
                sum_obj.put("order_num",mData.getIntValue("recharge_num"));
            }
            transfer_recharge_money.add(sum_obj);

            if (transfer_oncecard_money == null){
                transfer_oncecard_money = new JSONArray();
                sum_obj = obj;
            }else{
                sum_obj = new JSONObject();
                sum_obj.put("pay_name","合计");
                sum_obj.put("pay_money",mData.getDoubleValue("oncecard_money"));
                sum_obj.put("order_num",mData.getIntValue("oncecard_num"));
            }
            transfer_oncecard_money.add(sum_obj);

            if (sale_pay_details != null){
                final TransferDetailsAdapter adapter = new TransferDetailsAdapter(this,transfer_money_info);
                sale_pay_details.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
                sale_pay_details.setAdapter(adapter);
            }

            if (refund_pay_details != null){
                final TransferDetailsAdapter adapter = new TransferDetailsAdapter(this,transfer_refund_money);
                refund_pay_details.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
                refund_pay_details.setAdapter(adapter);
            }

            if (recharge_pay_details != null){
                final TransferDetailsAdapter adapter = new TransferDetailsAdapter(this,transfer_recharge_money);
                recharge_pay_details.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
                recharge_pay_details.setAdapter(adapter);
            }

            if (cik_pay_details != null){
                final TransferDetailsAdapter adapter = new TransferDetailsAdapter(this,transfer_oncecard_money);
                cik_pay_details.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
                cik_pay_details.setAdapter(adapter);
            }

        }

        private void iniMenu() {
            setLeftText("交班信息");
            setMiddleText(getStoreName());
        }

        private void initData(){
            final Intent intent = getIntent();
            if (intent != null){
                final String sz = intent.getStringExtra("data");
                if (null != sz){
                    try {
                        mData = JSONObject.parseObject(sz);
                        Logger.d_json(mData.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this,"加载门店信息错误:" + e.getMessage(),Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }

}