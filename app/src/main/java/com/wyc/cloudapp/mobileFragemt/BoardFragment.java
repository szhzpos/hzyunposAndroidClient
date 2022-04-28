package com.wyc.cloudapp.mobileFragemt;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.mobile.report.MobileDealQueryActivity;
import com.wyc.cloudapp.customizationView.PieView;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.PayMethodStatisticsViewAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class BoardFragment extends AbstractMobileFragment {
    private int mCurrentTimeBtnId,mCurrentPayMethodBtnId;
    private JSONObject mQueryCondition;
    private PayMethodStatisticsViewAdapter mAdapter;
    private View mToday;
    private PieView mPieStatistics;

    @Override
    protected int getRootLayout() {
        return R.layout.mobile_board_fragment_layout;
    }

    @Override
    protected void viewCreated() {
        initPayMethodBtn();
        initPayMethodList();
        initTimeBtn();
        initPie();
    }

    private void initPie(){
        mPieStatistics = findViewById(R.id.pieView);
    }

    private void initTimeBtn(){
        final LinearLayout time_btn_layout = findViewById(R.id.time_btn_layout);
        if (null != time_btn_layout){
            for (int i = 0,counts = time_btn_layout.getChildCount();i < counts;i ++){
                final View child_v = time_btn_layout.getChildAt(i);
                if (child_v instanceof Button){
                    if (child_v.getId() == R.id.today_btn)mToday = child_v;
                    child_v.setOnClickListener(time_btn_listener);
                }
            }

            if (mToday != null){
                mToday.postDelayed(()->mToday.callOnClick(),100);
            }
        }
    }

    private final View.OnClickListener time_btn_listener = view -> {
        int id = view.getId();

        setCurrentTimeBtn(id,view);

        final JSONObject condition = new JSONObject();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        final Calendar rightNow = Calendar.getInstance();
        try {

            switch (id){
                case R.id.today_btn:
                    showCustomDate(false);
                    final String today = sdf.format(rightNow.getTime());
                    condition.put("time_type",1);
                    condition.put("days",1);
                    condition.put("start_time",today);
                    condition.put("end_time",today);
                    break;
                case R.id.yestoday_btn:
                    showCustomDate(false);
                    rightNow.add(Calendar.DAY_OF_YEAR,-1);
                    final String yestoday = sdf.format(rightNow.getTime());
                    condition.put("time_type",2);
                    condition.put("start_time",yestoday);
                    condition.put("end_time",yestoday);
                    break;
                case R.id.seven_days_btn:
                    showCustomDate(false);
                    condition.put("time_type",1);
                    condition.put("days",7);
                    break;
                case R.id.c_btn:
                    showCustomDate(true);
                    final JSONObject date_obj = getDate();
                    if (date_obj.size() == 0){
                        return;
                    }

                    final String st = Utils.getNullStringAsEmpty(date_obj,"s"),et = Utils.getNullStringAsEmpty(date_obj,"e");
                    if (Utils.verifyDate(st) && Utils.verifyDate(et)){

                        condition.put("start_time",st);

                        condition.put("end_time",et);

                        condition.put("time_type",2);

                    }
                    break;
            }
            mQueryCondition = condition;
            getBusinessDatas(condition);
        }catch (JSONException e){
            e.printStackTrace();
            Toast.makeText(mContext,e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
        }
    };

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
                if (between_days >= 0 && between_days <= 31){
                    object.put("s",start_t);
                    object.put("e",end_t);
                }else {
                    MyDialog.displayMessage(getContext(),"只能查询30日内的数据!");
                }
            }catch ( JSONException | ParseException e){
                e.printStackTrace();
            }
        }
        return object;
    }

    private void showCustomDate(boolean b){
        final LinearLayout consume_date_layout = findViewById(R.id.custome_date_layout);
        final EditText s = consume_date_layout.findViewById(R.id.start_date),e = consume_date_layout.findViewById(R.id.end_date);
        if (b){
            if (consume_date_layout.getVisibility() == View.GONE){
                consume_date_layout.setVisibility(View.VISIBLE);
                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                s.setText(sdf.format(new Date()));
                s.setOnClickListener(view ->Utils.showDatePickerDialog(getContext(),findViewById(mCurrentTimeBtnId),s,Calendar.getInstance()));
                s.setOnFocusChangeListener((view, b12) -> {
                    if (b12)view.callOnClick();
                    Utils.hideKeyBoard(s);
                });
                e.setText(sdf.format(new Date()));
                e.setOnClickListener(view -> Utils.showDatePickerDialog(getContext(),findViewById(mCurrentTimeBtnId),e,Calendar.getInstance()));
                e.setOnFocusChangeListener((view, b1) -> {
                    if (b1)view.callOnClick();
                    Utils.hideKeyBoard(e);
                });
            }
        }else {
            if (consume_date_layout.getVisibility() == View.VISIBLE){
                consume_date_layout.setVisibility(View.GONE);
                s.getText().clear();
                s.setOnClickListener(null);
                s.setOnFocusChangeListener(null);
                e.getText().clear();
                e.setOnClickListener(null);
                e.setOnFocusChangeListener(null);
            }
        }
    }

    private void getBusinessDatas(final JSONObject condition){
        final CustomProgressDialog progressDialog = new CustomProgressDialog(mContext);
        progressDialog.setMessage(getString(R.string.hints_query_data_sz)).show();
        final JEventLoop loop = new JEventLoop();
        final StringBuilder err = new StringBuilder();

        CustomApplication.execute(()->{
            getPayMethodDatasFromSale(condition);
        });

        CustomApplication.execute(()->{
            final String base_url = mContext.getUrl(),appid = mContext.getAppId(),appsecret = mContext.getAppSecret(),
                    start_time = condition.getString("start_time"),end_time = condition.getString("end_time");

            final JSONObject object = new JSONObject();
            int time_type = condition.getIntValue("time_type"),days = condition.getIntValue("days");

            try {

                object.put("appid",appid);
                object.put("time_type",time_type);

                switch (time_type){
                    case 1:
                        object.put("days",days);
                        break;
                    case 2:
                        object.put("start_time",start_time);
                        object.put("end_time",end_time);
                        break;
                    case 3:
                        break;
                }

                object.put("stores_id",mContext.getStoreId());
                object.put("see_type",2);

                final JSONObject retJson = HttpUtils.sendPost(base_url + "/api/boss/sales_volume",HttpRequest.generate_request_parma(object, appsecret),true);

                switch (retJson.getIntValue("flag")) {
                    case 0:
                        loop.done(0);
                        err.append(retJson.getString("info"));
                        break;
                    case 1:
                        final JSONObject info = JSON.parseObject(retJson.getString("info")),data = info.getJSONObject("data");
                        final  JSONArray data_list = data.getJSONArray("list");
                        mContext.runOnUiThread(()->{
                            showBusinessData(data_list);
                        });
                        loop.done(1);
                        break;
                }

            } catch ( JSONException e) {
                e.printStackTrace();
                err.append(e.getMessage());
                loop.done(0);
            }
        });
        final int code = loop.exec();
        if (code != 1)Toast.makeText(mContext,err.toString(),Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }

    private void showBusinessData(final @NonNull JSONArray array){
        JSONObject object;
        double xx_money = 0.0,xx_num = 0.0,sale_amt = 0.0,refund_amt = 0.0,gift_amt = 0.0;
        TextView amt_tv = null,num_tv = null;
        for (int i = 0,size = array.size();i < size;i++){
            object = array.getJSONObject(i);
            if (object != null){
                xx_money = object.getDoubleValue("xx_money");
                xx_num = object.getDoubleValue("xx_num");
                switch (object.getIntValue("search_type")){
                    case 1:
                        sale_amt = xx_money;
                        amt_tv = findViewById(R.id.sale_amt_tv);
                        num_tv = findViewById(R.id.sale_num_tv);

                        break;
                    case 2:
                        amt_tv = findViewById(R.id.charge_amt_tv);
                        num_tv = null;
                        break;
                    case 3:
                        amt_tv = findViewById(R.id.ck_amt_tv);
                        num_tv = null;
                        break;
                    case 4:
                        amt_tv = findViewById(R.id.gift_amt_tv);
                        num_tv = null;
                        break;
                    case 5:
                        refund_amt = xx_money;
                        amt_tv = findViewById(R.id.refund_amt_tv);
                        num_tv = findViewById(R.id.refund_num_tv);
                        break;
                }
                if (null != amt_tv)amt_tv.setText(String.format(Locale.CHINA,"%.2f",xx_money));
                if (num_tv != null)num_tv.setText(String.format(Locale.CHINA,"%.2f",xx_num));
            }
        }

        final TextView bus_amt_tv = findViewById(R.id.busi_amt_tv),view = findViewById(R.id.busi_amt_tv_o);
        final String amt = String.format(Locale.CHINA,"%.2f",sale_amt - refund_amt);
        if (bus_amt_tv != null){
            bus_amt_tv.setText(amt);
        }
        if (view != null)view.setText(amt);
    }

    private void getPayMethodDatasFromSale(final JSONObject condition){
        final String base_url = mContext.getUrl(),appid = mContext.getAppId(),appsecret = mContext.getAppSecret(),
                start_time = condition.getString("start_time"),end_time = condition.getString("end_time");
        final JSONObject object = new JSONObject();
        int time_type = condition.getIntValue("time_type"),days = condition.getIntValue("days");

        mContext.runOnUiThread(()->{
            if (mCurrentPayMethodBtnId != R.id.sale_btn_layout){
                final View view = findViewById(mCurrentPayMethodBtnId),sale_btn = findViewById(R.id.sale_btn_layout);
                if (null != sale_btn)sale_btn.setBackground(mContext.getDrawable(R.drawable.business_monitor_btn_selected_status));
                if (null != view)view.setBackground(mContext.getDrawable(R.drawable.new_button_style));
                mCurrentPayMethodBtnId = R.id.sale_btn_layout;
            }
        });

        try {
            object.put("appid",appid);
            object.put("time_type",time_type);
            switch (time_type){
                case 1:
                    object.put("days",days);
                    break;
                case 2:
                    object.put("start_time",start_time);
                    object.put("end_time",end_time);
                    break;
                case 3:
                    break;
            }

            object.put("stores_id",mContext.getStoreId());
            object.put("search_type",1);
            object.put("see_type",2);

            final JSONObject retJson = HttpUtils.sendPost(base_url + "/api/boss/sales_paymethod",HttpRequest.generate_request_parma(object, appsecret),true);

            switch (retJson.getIntValue("flag")) {
                case 0:
                    mContext.runOnUiThread(()-> Toast.makeText(mContext,retJson.getString("info"),Toast.LENGTH_LONG).show());
                    break;
                case 1:
                    final JSONObject info = JSONObject.parseObject(retJson.getString("info")),data = info.getJSONObject("data");
                    final  JSONArray data_list = data.getJSONArray("paymethod");

                    mContext.runOnUiThread(()-> setPayMethodDatas(data_list));
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            mContext.runOnUiThread(()-> Toast.makeText(mContext,e.getLocalizedMessage(),Toast.LENGTH_LONG).show());
        }
    }

    private void setCurrentTimeBtn(int id,View view){
        if (id != mCurrentTimeBtnId){
            final Button btn = findViewById(mCurrentTimeBtnId);
            if (null != btn)btn.setBackground(mContext.getDrawable(R.drawable.new_button_style));
            view.setBackground(mContext.getDrawable(R.drawable.business_monitor_btn_selected_status));
            mCurrentTimeBtnId = id;
        }
    }

    private void initPayMethodList(){
        final RecyclerView recyclerView  = findViewById(R.id.pay_method_details);
        if (null != recyclerView){
            mAdapter = new PayMethodStatisticsViewAdapter(mContext);
            recyclerView.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            recyclerView.setAdapter(mAdapter);
        }
    }
    private void initPayMethodBtn(){
        final LinearLayout pay_method_layout = findViewById(R.id.pay_method_layout);
        if (null != pay_method_layout){
            for (int i = 0,counts = pay_method_layout.getChildCount();i < counts;i ++){
                final View child_v = pay_method_layout.getChildAt(i);
                if (child_v instanceof LinearLayout){
                    child_v.setOnClickListener(mPayMethodClick);
                }
            }
        }
    }
    private final View.OnClickListener mPayMethodClick = view -> {
        int id = view.getId();

        setCurrentPayMethodBtn(id,view);

        if (null != mQueryCondition){
            int search_type = 0;
            switch (id){
                case R.id.sale_btn_layout:
                    search_type = 1;
                    break;
                case R.id.gift_btn_layout:
                    search_type = 4;
                    break;
                case R.id.charge_btn_layout:
                    search_type = 2;
                    break;
                case R.id.ck_btn_layout:
                    search_type = 3;
                    break;
            }
            getPayMethodDatas(search_type,mQueryCondition);
        }
    };
    private void setCurrentPayMethodBtn(int id,View view){
        if (id != mCurrentPayMethodBtnId){
            final View btn = findViewById(mCurrentPayMethodBtnId);
            if (null != btn)btn.setBackground(mContext.getDrawable(R.drawable.new_button_style));
            view.setBackground(mContext.getDrawable(R.drawable.business_monitor_btn_selected_status));
            mCurrentPayMethodBtnId = id;
        }
    }
    private void getPayMethodDatas(int search_type,final JSONObject condition){
        //search_type 1商品销售额 2充值金额 3次卡销售额 4购物卡销售额 默认全部
        final CustomProgressDialog progressDialog = new CustomProgressDialog(mContext);
        progressDialog.setMessage(getString(R.string.hints_query_data_sz)).show();
        final JEventLoop loop = new JEventLoop();
        final StringBuilder err = new StringBuilder();
        CustomApplication.execute(()->{

            final String base_url = mContext.getUrl(),appId = mContext.getAppId(),appSecret = mContext.getAppSecret(),
                    start_time = condition.getString("start_time"),end_time = condition.getString("end_time");

            final JSONObject object = new JSONObject();
            int time_type = condition.getIntValue("time_type"),days = condition.getIntValue("days");
            try {

                object.put("appid",appId);
                object.put("time_type",time_type);

                switch (time_type){
                    case 1:
                        object.put("days",days);
                        break;
                    case 2:
                        object.put("start_time",start_time);
                        object.put("end_time",end_time);
                        break;
                    case 3:
                        break;
                }
                object.put("stores_id",mContext.getStoreId());

                object.put("search_type",search_type);
                object.put("see_type",2);

                final JSONObject retJson = HttpUtils.sendPost(base_url + "/api/boss/sales_paymethod", HttpRequest.generate_request_parma(object,appSecret),true);

                switch (retJson.getIntValue("flag")) {
                    case 0:
                        loop.done(0);
                        err.append(retJson.getString("info"));
                        break;
                    case 1:
                        final JSONObject info = JSONObject.parseObject(retJson.getString("info")),data = info.getJSONObject("data");
                        final JSONArray data_list = data.getJSONArray("paymethod");
                        mContext.runOnUiThread(()->{
                            setPayMethodDatas(data_list);
                        });
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
        if (code != 1) Toast.makeText(mContext,err.toString(),Toast.LENGTH_LONG).show();
        progressDialog.dismiss();
    }
    private void setPayMethodDatas(final JSONArray array){
        if (mAdapter != null){
            mAdapter.setDataForArray(array);
        }
        if (mPieStatistics != null)mPieStatistics.setDatas(mergeData(array));
    }
    private JSONArray mergeData(final JSONArray array){
        final JSONArray result = new JSONArray();
        JSONObject other = null,tmp;
        try {
            for (int i = 0,size = array.size();i < size;i++){
                tmp = array.getJSONObject(i);
                switch (tmp.getIntValue("pay_method")){
                    case 1:
                    case 560:
                    case 4:
                    case 5:
                        result.add(tmp);
                        break;
                    default:
                        if (other == null){
                            other = JSONObject.parseObject(tmp.toString());
                            other.put("title","其他");
                        }else {
                            other.put("xx_money",other.getDoubleValue("xx_money") + tmp.getDoubleValue("xx_money"));
                        }
                        break;
                }
            }
            if (other != null)result.add(other);

            return createPieData(result);
        }catch (org.json.JSONException e){
            e.printStackTrace();
        }
        return null;
    }
    private JSONArray createPieData(final JSONArray array) throws org.json.JSONException {
        JSONObject obj,tmp;
        final JSONArray result = new JSONArray();
        for (int i = 0,size = array.size();i < size;i++){
            tmp = array.getJSONObject(i);
            obj = new JSONObject();
            obj.put("label",tmp.getString("title"));
            obj.put("value",tmp.getDouble("xx_money"));
            result.add(obj);
        }
        return result;
    }
}