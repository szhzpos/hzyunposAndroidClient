package com.wyc.cloudapp.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.SaleOrderBodyViewAdapter;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnMainActivity;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.WINDOW_SERVICE;

public class QuerySaleOrderDialog extends DialogBaseOnMainActivity {
    private int mCurrentStatusIndex = 0;
    private String[] mCashierNames,mCashierIDs;
    private EditText mStartDateEt,mStartTimeEt,mEndDateEt,mEndTimeEt,mPayStatusEt,mCashierEt,mS_ex_statusEt,mUploadStatusEt,mOrderStatusEt;
    private SaleOrderBodyViewAdapter mSaleOrderBodyViewAdapter;
    public QuerySaleOrderDialog(@NonNull MainActivity context, final String title) {
        super(context,title);
        mSaleOrderBodyViewAdapter = new SaleOrderBodyViewAdapter(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.query_sale_order_dialog_layout);
        setCancelable(false);

        initStartDateAndTime();
        initEndDateAndTime();

        initStatusEt();
        initCashierEt();
        //初始化表格
        initOrderDetailTable();

        //初始窗口尺寸
        initWindowSize();
        //
        initQueryBtn();
    }

    private void initQueryBtn(){
        final Button query_btn = findViewById(R.id.query_btn);
        if (query_btn != null){
            query_btn.setOnClickListener(v -> {
                final EditText order_code_et = findViewById(R.id.order_code);
                final String start_date_time = mStartDateEt.getText() + " " + mStartTimeEt.getText(),end_date_time = mEndDateEt.getText() + " " + mEndTimeEt.getText(),
                        sz_order_code = order_code_et.getText().toString(),sz_cashier = getCasId(mCashierEt);
                int pay_status = Utils.getViewTagValue(mPayStatusEt,0),s_ex_status = Utils.getViewTagValue(mS_ex_statusEt,0),
                        upload_status = Utils.getViewTagValue(mUploadStatusEt,0),order_status = Utils.getViewTagValue(mOrderStatusEt,0);

                final StringBuilder where_sql = new StringBuilder();

                where_sql.append("where a.stores_id = ").append(mContext.getStoreInfo().getIntValue("stores_id"));

                if(sz_order_code.length() != 0){
                    if(where_sql.length() != 0)
                        where_sql.append(" and ");

                    where_sql.append(" order_code").append(" like ").append("'%").append(sz_order_code).append("'");
                }
                if(!"0".equals(sz_cashier)){
                    if(where_sql.length() != 0)
                        where_sql.append(" and ");

                    where_sql.append(" a.cashier_id").append("=").append(sz_cashier);
                }
                if(pay_status != 0){
                    if(where_sql.length() != 0)
                        where_sql.append(" and ");

                    where_sql.append(" pay_status").append("=").append(pay_status);
                }

                if(order_status != 0){
                    if(where_sql.length() != 0)
                        where_sql.append(" and ");

                    where_sql.append(" order_status").append("=").append(order_status);

                }
                if(upload_status != 0){
                    if(where_sql.length() != 0)
                        where_sql.append(" and ");

                    where_sql.append(" upload_status").append("=").append(upload_status);
                }
                if(s_ex_status != 0){
                    if(where_sql.length() != 0)
                        where_sql.append(" and ");

                    where_sql.append(" transfer_status = ").append(s_ex_status);
                }

                if(where_sql.length() != 0){
                    where_sql.append(" and ");
                }
                where_sql.append("datetime(addtime, 'unixepoch', 'localtime') ").append("between ").append("'").append(start_date_time).append("'").append(" and ").append("'").append(end_date_time).append("'");

                mSaleOrderBodyViewAdapter.setDatas(where_sql.toString());
            });
            query_btn.callOnClick();
        }
    }

    private void initCashierEt(){
        final EditText cashier_et = mCashierEt = findViewById(R.id.cashier_et);
        if (null != cashier_et){
            cashier_et.setOnFocusChangeListener(etFocusChangeListener);
            final StringBuilder err = new StringBuilder();
            final String sz_cas_info = SQLiteHelper.getString("SELECT cas_id,cas_name FROM cashier_info where cas_status = 1  union select 0 cas_id,'所有' cas_name ",err);
            if (sz_cas_info != null){
                final String[] cas_items_tmp = sz_cas_info.split("\r\n");
                int size = cas_items_tmp.length;
                mCashierNames = new String[size];
                mCashierIDs = new String[size];
                for(int i = 0;i < size;i++){
                    final String sz_item = cas_items_tmp[i];
                    final String[] cas_infos = sz_item.split("\t");
                    if (cas_infos.length >= 2){
                        mCashierIDs[i] = cas_infos[0];
                        mCashierNames[i] = cas_infos[1];
                    }
                }
                cashier_et.setOnClickListener(etClickListener);
                setCashierEt(cashier_et);
            }else{
                MyDialog.ToastMessage(cashier_et,"初始化收银员错误：" + err,mContext,getWindow());
            }
        }
    }
    private void setCashierEt(final @NonNull EditText cashier_et){
        cashier_et.setTag(mCashierIDs[mCurrentStatusIndex]);
        cashier_et.setText(mCashierNames[mCurrentStatusIndex]);
    }
    private int getCashierIdIndex(final  String cas_id){
        int index = -1;
        for (String info: mCashierIDs){
            index++;
            if (null != cas_id){
                if (cas_id.equals(info)){
                    break;
                }
            }
        }
        return index;
    }
    private String getCasId(final EditText et){
        Object tag;
        String tag_v = "0";
        if (et != null && (tag = et.getTag()) != null){
            if (tag instanceof String){
                tag_v = (String) tag;
            }
        }
        return tag_v;
    }

    private void initStatusEt(){
        final String sz_all = "所有";
        final EditText pay_status_et = mPayStatusEt = findViewById(R.id.pay_status_et),s_ex_status_et = mS_ex_statusEt = findViewById(R.id.s_ex_status_et),
                upload_status_et = mUploadStatusEt = findViewById(R.id.upload_status_et),order_status_et = mOrderStatusEt = findViewById(R.id.order_status_et);

        pay_status_et.setOnFocusChangeListener(etFocusChangeListener);
        pay_status_et.setTag(0);
        pay_status_et.setText(sz_all);
        pay_status_et.setOnClickListener(etClickListener);

        //交班状态
        s_ex_status_et.setOnFocusChangeListener(etFocusChangeListener);
        s_ex_status_et.setTag(0);
        s_ex_status_et.setText(sz_all);
        s_ex_status_et.setOnClickListener(etClickListener);

        upload_status_et.setOnFocusChangeListener(etFocusChangeListener);
        upload_status_et.setTag(0);
        upload_status_et.setText(sz_all);
        upload_status_et.setOnClickListener(etClickListener);

        order_status_et.setOnFocusChangeListener(etFocusChangeListener);
        order_status_et.setTag(0);
        order_status_et.setText(sz_all);
        order_status_et.setOnClickListener(etClickListener);

    }
    private View.OnClickListener etClickListener = this::showChooseDialog;
    private void showChooseDialog(final @NonNull View et){
        if (et instanceof EditText){
            String title = "";
            int index = 0;
            final Object et_tag = et.getTag();
            String[] items = new String[]{""};
            switch (et.getId()){
                case R.id.pay_status_et:
                    items = new String[]{"所有","未支付","已支付","支付中"};
                    title = mContext.getString(R.string.pay_s_sz);
                    break;
                case R.id.cashier_et:
                    items = mCashierNames;
                    title = mContext.getString(R.string.cashier_not_colon_sz);
                    break;
                case R.id.s_ex_status_et:
                    items = new String[]{"所有","未交班","已交班"};
                    title = mContext.getString(R.string.s_e_status_sz);
                    break;
                case R.id.upload_status_et:
                    items = new String[]{"所有","未上传","已上传"};
                    title = mContext.getString(R.string.upload_s_sz);
                    break;
                case R.id.order_status_et:
                    items = new String[]{"所有","未付款","已付款","已取消","已退货"};
                    title = mContext.getString(R.string.order_s_sz);
                    break;
            }
            final String [] currentStatusItems = items;

            if (currentStatusItems == mCashierNames){
                if (et_tag instanceof String)
                    index = getCashierIdIndex((String) et.getTag());
            }else {
                if (et_tag instanceof Integer)
                    index = (int)et.getTag();
            }
            Logger.d("index:%d",index);
            chooseDialog((EditText) et,currentStatusItems,index,title);
        }
    }
    private void chooseDialog(final @NonNull EditText et,final String[] currentStatusItems,int index,final String title){
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setSingleChoiceItems(currentStatusItems, index, (dialog, which) -> mCurrentStatusIndex = which);
        builder.setPositiveButton(mContext.getString(R.string.OK), (dialog, which) -> {
            if (mCurrentStatusIndex < currentStatusItems.length && mCurrentStatusIndex >= 0){
                if (currentStatusItems == mCashierNames){
                    setCashierEt(et);
                }else {
                    et.setTag(mCurrentStatusIndex);
                    et.setText(currentStatusItems[mCurrentStatusIndex]);
                }
            }
            dialog.dismiss();
        });
        builder.setNegativeButton(mContext.getString(R.string.cancel), (dialog, which) -> {
            dialog.dismiss();
        });

        final AlertDialog alertDialog = builder.create();

        int blue = mContext.getColor(R.color.blue);

        final TextView titleTv = new TextView(mContext);
        titleTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        titleTv.setPadding(5,5,5,5);
        titleTv.setTextSize(mContext.getResources().getDimension(R.dimen.font_size_22));
        titleTv.setTextColor(blue);
        titleTv.setText(title);
        alertDialog.setCustomTitle(titleTv);

        alertDialog.show();

        final ListView listView = alertDialog.getListView();
        listView.setDivider(mContext.getDrawable(R.color.gray__subtransparent));
        listView.setDividerHeight(1);
        listView.setBackground(mContext.getDrawable(R.drawable.border_sub_gray));

        final Button cancel = alertDialog.getButton(BUTTON_NEGATIVE), ok = alertDialog.getButton(BUTTON_POSITIVE);
        cancel.setTextColor(blue);
        cancel.setTextSize(mContext.getResources().getDimension(R.dimen.font_size_16));

        ok.setTextColor(blue);
        ok.setTextSize(mContext.getResources().getDimension(R.dimen.font_size_16));

        final WindowManager.LayoutParams  lp= alertDialog.getWindow().getAttributes();
        lp.width= 368;
        if (currentStatusItems.length > 3){
            lp.height= 288;
        }
        alertDialog.getWindow().setAttributes(lp);
    }

    private void initEndDateAndTime(){
        final EditText end_date = mEndDateEt = findViewById(R.id.end_date),end_time = mEndTimeEt = findViewById(R.id.end_time);
        if (null != end_date && null != end_time){
            end_date.setOnFocusChangeListener(etFocusChangeListener);
            end_date.setText(new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA).format(new Date()));
            end_date.setOnClickListener(v -> showDatePickerDialog(mContext,(TextView) v,Calendar.getInstance()));

            end_time.setOnFocusChangeListener(etFocusChangeListener);
            end_time.setOnClickListener(v -> showTimePickerDialog(mContext,(TextView) v,Calendar.getInstance()));
        }
    }
    private void initStartDateAndTime(){
        final EditText start_date = mStartDateEt = findViewById(R.id.start_date),start_time = mStartTimeEt = findViewById(R.id.start_time);
        if (null != start_date && null != start_time) {
            start_date.setOnFocusChangeListener(etFocusChangeListener);
            start_date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
            start_date.setOnClickListener(v -> showDatePickerDialog(mContext, (TextView) v, Calendar.getInstance()));

            start_time.setOnFocusChangeListener(etFocusChangeListener);
            start_time.setOnClickListener(v -> showTimePickerDialog(mContext, (TextView) v, Calendar.getInstance()));
        }
    }

    private View.OnFocusChangeListener etFocusChangeListener = (v, b)->{
        if (b)v.callOnClick();
        Utils.hideKeyBoard((EditText) v);
    };

    private void initWindowSize(){//初始化窗口尺寸
        WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
        if (m != null){
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
            Point point = new Point();
            d.getSize(point);
            Window dialogWindow = this.getWindow();
            if (dialogWindow != null){
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.height = (int)(0.9 * point.y);
                lp.width = (int)(0.85 * point.x) - 4;
                dialogWindow.setAttributes(lp);
            }
        }
    }
    private void initOrderDetailTable(){
        mSaleOrderBodyViewAdapter = new SaleOrderBodyViewAdapter(mContext);
        final RecyclerView body = findViewById(R.id.order_body);
        body.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        body.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        body.setAdapter(mSaleOrderBodyViewAdapter);
    }

    private static void showTimePickerDialog(final Activity activity, final TextView tv, Calendar calendar) {
        new TimePickerDialog( activity,3,
                // 绑定监听器
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view,int hourOfDay, int minute) {
                        tv.setText(String.format(Locale.CHINA,"%02d:%02d:%02d",hourOfDay,minute,0));
                    }
                }
                // 设置初始时间
                , calendar.get(Calendar.HOUR_OF_DAY)
                , calendar.get(Calendar.MINUTE)
                // true表示采用24小时制
                ,true).show();
    }

    private static void showDatePickerDialog(final Activity activity, final TextView tv, Calendar calendar) {
        new DatePickerDialog(activity,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
                        tv.setText(String.format(Locale.CHINA,"%d-%02d-%02d",year,monthOfYear + 1,dayOfMonth));
                    }
                }
                // 设置初始日期
                , calendar.get(Calendar.YEAR)
                ,calendar.get(Calendar.MONTH)
                ,calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

}
