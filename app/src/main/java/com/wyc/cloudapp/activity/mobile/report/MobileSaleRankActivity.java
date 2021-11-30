package com.wyc.cloudapp.activity.mobile.report;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.report.MobileSaleRankAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.activity.mobile.report
 * @ClassName: MobileSaleRankActivity
 * @Description: 商品销售排行报表
 * @Author: wyc
 * @CreateDate: 2021/1/27 16:46
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/1/27 16:46
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class MobileSaleRankActivity extends AbstractReportActivity {
    private int mCurrentDateViewId = -1;
    private MobileSaleRankAdapter mAdapter;
    private View mToday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initOrderList();
        initDateCondition();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_mobile_sale_rank;
    }
    @Override
    public void onResume(){
        super.onResume();
        if (null != mToday)CustomApplication.runInMainThread(()-> mToday.callOnClick());
    }

    private void initOrderList(){
        final RecyclerView recyclerView  = findViewById(R.id.rank_list);
        if (null != recyclerView){
            mAdapter = new MobileSaleRankAdapter(this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
            recyclerView.setAdapter(mAdapter);
        }
    }

    private void initDateCondition(){
        final LinearLayout date_layout = findViewById(R.id.date_layout);
        View view;
        if (null != date_layout){
            for (int i = 0,counts = date_layout.getChildCount();i < counts; i++){
                view = date_layout.getChildAt(i);
                view.setOnClickListener(date_view_listener);
                if (view.getId() == R.id.today_tv)mToday = view;
            }
        }
    }
    private final View.OnClickListener date_view_listener = view -> {
        int view_id = view.getId(),last_id = mCurrentDateViewId;
        if (last_id != view_id){
            mCurrentDateViewId = view_id;
            final TextView last_v = findViewById(last_id);
            if (last_v != null){
                last_v.setTextColor(getColor(R.color.text_color));
                last_v.setBackgroundColor(Color.WHITE);
            }
            ((TextView)view).setTextColor(getColor(R.color.lightBlue));
            view.setBackground(getDrawable(R.drawable.mobile_report_date_btn_style));
        }

        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        final Calendar rightNow = Calendar.getInstance();
        String start_time = "",end_time = "";
        try {
            switch (view_id){
                case R.id.today_tv:
                    mQueryConditionObj.put("time_type",2);

                    start_time = sdf.format(rightNow.getTime());

                    end_time = sdf.format(rightNow.getTime());

                    break;
                case R.id.yestoday_tv:
                    mQueryConditionObj.put("time_type",2);

                    rightNow.add(Calendar.DAY_OF_YEAR,-1);

                    start_time = sdf.format(rightNow.getTime());

                    end_time = sdf.format(rightNow.getTime());

                    break;
                case R.id.ft_days_tv:
                    mQueryConditionObj.put("time_type",1);
                    mQueryConditionObj.put("days",14);
                    break;
                case R.id.last_month_tv:
                    mQueryConditionObj.put("time_type",2);

                    rightNow.add(Calendar.MONTH,-1);
                    int fday = rightNow.getActualMinimum(5);
                    rightNow.set(rightNow.get(Calendar.YEAR),rightNow.get(Calendar.MONTH),fday);
                    start_time = sdf.format(rightNow.getTime());

                    int day = rightNow.getActualMaximum(5);
                    rightNow.set(rightNow.get(Calendar.YEAR),rightNow.get(Calendar.MONTH),day);

                    end_time = sdf.format(rightNow.getTime());
                    break;
            }

            mQueryConditionObj.put("start_time",start_time);
            mQueryConditionObj.put("end_time",end_time);

            Logger.d_json(mQueryConditionObj.toJSONString());

            getDatas();

        } catch (JSONException e) {
            e.printStackTrace();
            MyDialog.toastMessage("加载门店信息错误:" + e.getMessage());
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
                int stores_id = object.getIntValue("stores_id");
                if (-1 == stores_id){
                    object.put("see_type",1);
                }else {
                    object.put("see_type",2);
                }

                final JSONObject retJson = HttpUtils.sendPost(mUrl + "/api/boss/goods_sales_rank", HttpRequest.generate_request_parma(object, mAppSecret),true);

                switch (retJson.getIntValue("flag")) {
                    case 0:
                        loop.done(0);
                        err.append(retJson.getString("info"));
                        break;
                    case 1:
                        final JSONObject info = JSONObject.parseObject(retJson.getString("info")),data = Utils.getNullObjectAsEmptyJson(info,"data");
                        final Activity activity = MobileSaleRankActivity.this;
                        activity.runOnUiThread(()->{
                            if (!data.isEmpty()){
                                mAdapter.setDataForArray(data.getJSONArray("goods"));
                            }else {
                                mAdapter.setDataForArray(null);
                                MyDialog.toastMessage(info.getString("info"));
                            }
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
        if (code != 1)MyDialog.toastMessage(err.toString());
        progressDialog.dismiss();
    }

}
