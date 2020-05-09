package com.wyc.cloudapp.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.SaleDetailBodyViewAdapter;
import com.wyc.cloudapp.adapter.SaleDetailHeaderViewAdapter;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.WINDOW_SERVICE;

public class QuerySaleDetailsDialog extends Dialog {
    private MainActivity mContext;
    private int mCurrentStatusIndex = 0;
    private String[] mCashierItems;
    private final String[] mPayStatusItems = new String[]{"所有","未支付","已支付","支付中"},mS_ex_statusItems = new String[]{"所有","未交班","已交班"},
            mUploadStatusItems = new String[]{"所有","未上传","已上传"},mOrderStatusItems = new String[]{"所有","未付款","已付款","已取消","已退货"};
    private EditText mStartDateEt,mStartTimeEt,mEndDateEt,mEndTimeEt,mPayStatusEt,mCashierEt,mS_ex_statusEt,mUploadStatusEt,mOrderStatusEt;
    private SaleDetailBodyViewAdapter mSaleDetailBodyViewAdapter;
    public QuerySaleDetailsDialog(@NonNull MainActivity context) {
        super(context);
        mContext = context;
        mSaleDetailBodyViewAdapter = new SaleDetailBodyViewAdapter(mContext);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_sale_detail_dialog_layout);
        setCancelable(false);

        initStartDateAndTime();
        initEndDateAndTime();

        initStatusEt();
        initCashierEt();
        //初始化表格
        initOrderDetailTable();

        //初始化按钮事件
        findViewById(R.id._close).setOnClickListener(v->QuerySaleDetailsDialog.this.dismiss());

        //初始窗口尺寸
        initWindowSize();
        //
        initQueryBtn();
    }

    private void initQueryBtn(){
        final Button query_btn = findViewById(R.id.query_btn);
        if (query_btn != null){
            query_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final EditText order_code_et = findViewById(R.id.order_code);
                    final String start_date_time = mStartDateEt.getText() + " " + mStartTimeEt.getText(),end_date_time = mEndDateEt.getText() + " " + mEndTimeEt.getText(),
                            sz_pay_status = mPayStatusEt.getTag().toString(),sz_cashier = mCashierEt.getTag().toString(),sz_s_ex_status = mS_ex_statusEt.getTag().toString(),
                            sz_upload_status = mUploadStatusEt.getTag().toString(),sz_order_status = mOrderStatusEt.getTag().toString(),sz_order_code = order_code_et.getText().toString();

                    final StringBuilder where_sql = new StringBuilder();

                    where_sql.append("where a.stores_id = ").append(mContext.getStoreInfo().getIntValue("stores_id"));

                    if(sz_order_code.length() != 0){
                        if(where_sql.length() != 0)
                            where_sql.append(" and ");

                        where_sql.append(" order_code").append(" like ").append("'%").append(sz_order_code).append("'");
                    }
                    if(!"-1".equals(sz_cashier)){
                        if(where_sql.length() != 0)
                            where_sql.append(" and ");

                        where_sql.append(" a.cashier_id").append("=").append(sz_cashier);
                    }
                    if(!"0".equals(sz_pay_status)){
                        if(where_sql.length() != 0)
                            where_sql.append(" and ");

                        where_sql.append(" pay_status").append("=").append(sz_pay_status);

                    }

                    if(!"0".equals(sz_order_status)){
                        if(where_sql.length() != 0)
                            where_sql.append(" and ");

                        where_sql.append(" order_status").append("=").append(sz_order_status);

                    }
                    if(!"0".equals(sz_upload_status)){
                        if(where_sql.length() != 0)
                            where_sql.append(" and ");

                        where_sql.append(" upload_status").append("=").append(sz_upload_status);
                    }
                    if(!"0".equals(sz_s_ex_status)){
                        if(where_sql.length() != 0)
                            where_sql.append(" and ");

                        where_sql.append(" transfer_status = ").append(sz_s_ex_status);
                    }

                    if(where_sql.length() != 0){
                        where_sql.append(" and ");
                    }
                    where_sql.append("datetime(addtime, 'unixepoch', 'localtime') ").append("between ").append("'").append(start_date_time).append("'").append(" and ").append("'").append(end_date_time).append("'");

                    mSaleDetailBodyViewAdapter.setDatas(where_sql.toString());
                }
            });
            query_btn.callOnClick();
        }
    }

    private void initCashierEt(){
        final EditText cashier_et = mCashierEt = findViewById(R.id.cashier_et);
        if (null != cashier_et){
            cashier_et.setOnFocusChangeListener(etFocusChangeListener);
            final StringBuilder err = new StringBuilder();
            final String sz_cas_info = SQLiteHelper.getString("SELECT cas_id,cas_name FROM cashier_info where cas_status = 1  union select -1 cas_id,'所有' cas_name ",err);
            if (sz_cas_info != null){
                mCashierItems = sz_cas_info.split("\r\n");
                cashier_et.setOnClickListener(etClickListener);
                setCashier(cashier_et);
            }else{
                MyDialog.ToastMessage(cashier_et,"初始化收银员错误：" + err,mContext,getWindow());
            }
        }
    }
    private void setCashier(final @NonNull EditText cashier_et){
        final String info = mCashierItems[mCurrentStatusIndex];
        final String[] cas_infos = info.split("\t");
        cashier_et.setTag(cas_infos[0]);
        cashier_et.setText(cas_infos[1]);
    }
    private int getCashierIdIndex(final  String cas_id){
        int index = 0;
        for (String info:mCashierItems){
            index++;
            final String[] cas_infos = info.split("\t");
            if (null != cas_id && cas_infos.length == 2){
                if (cas_id.equals(cas_infos[0])){
                    break;
                }
            }
        }
        return index;
    }

    private void initStatusEt(){
        final EditText pay_status_et = mPayStatusEt = findViewById(R.id.pay_status_et),s_ex_status_et = mS_ex_statusEt = findViewById(R.id.s_ex_status_et),
                upload_status_et = mUploadStatusEt = findViewById(R.id.upload_status_et),order_status_et = mOrderStatusEt = findViewById(R.id.order_status_et);

        pay_status_et.setOnFocusChangeListener(etFocusChangeListener);
        pay_status_et.setTag(mCurrentStatusIndex);
        pay_status_et.setText(mPayStatusItems[mCurrentStatusIndex]);
        pay_status_et.setOnClickListener(etClickListener);

        //交班状态
        s_ex_status_et.setOnFocusChangeListener(etFocusChangeListener);
        s_ex_status_et.setTag(mCurrentStatusIndex);
        s_ex_status_et.setText(mS_ex_statusItems[mCurrentStatusIndex]);
        s_ex_status_et.setOnClickListener(etClickListener);

        upload_status_et.setOnFocusChangeListener(etFocusChangeListener);
        upload_status_et.setTag(mCurrentStatusIndex);
        upload_status_et.setText(mUploadStatusItems[mCurrentStatusIndex]);
        upload_status_et.setOnClickListener(etClickListener);

        order_status_et.setOnFocusChangeListener(etFocusChangeListener);
        order_status_et.setTag(mCurrentStatusIndex);
        order_status_et.setText(mOrderStatusItems[mCurrentStatusIndex]);
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
                    items = mPayStatusItems;
                    title = mContext.getString(R.string.pay_s_sz);
                    break;
                case R.id.cashier_et:
                    items = mCashierItems;
                    title = mContext.getString(R.string.cashier_sz);
                    break;
                case R.id.s_ex_status_et:
                    items = mS_ex_statusItems;
                    title = mContext.getString(R.string.s_e_status_sz);
                    break;
                case R.id.upload_status_et:
                    items = mUploadStatusItems;
                    title = mContext.getString(R.string.upload_s_sz);
                    break;
                case R.id.order_status_et:
                    items = mOrderStatusItems;
                    title = mContext.getString(R.string.order_s_sz);
                    break;
            }
            final String [] currentStatusItems = items;

            if (currentStatusItems == mCashierItems){
                if (et_tag instanceof String)
                    index = getCashierIdIndex((String) et.getTag());
            }else {
                if (et_tag instanceof Integer)
                    index = (int)et.getTag();
            }
            chooseDialog((EditText) et,currentStatusItems,index,title);
        }
    }
    private void chooseDialog(final EditText et,final String[] currentStatusItems,int index,final String title){
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setSingleChoiceItems(currentStatusItems, index, (dialog, which) -> mCurrentStatusIndex = which);
        builder.setPositiveButton(mContext.getString(R.string.OK), (dialog, which) -> {
            if (mCurrentStatusIndex < currentStatusItems.length && mCurrentStatusIndex >= 0){
                if (currentStatusItems == mCashierItems){
                    setCashier(et);
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
                lp.width = (int)(0.9 * point.x);
                dialogWindow.setAttributes(lp);
            }
        }
    }
    private void initOrderDetailTable(){
        mSaleDetailBodyViewAdapter = new SaleDetailBodyViewAdapter(mContext);
        final RecyclerView header = findViewById(R.id.detail_header),body = findViewById(R.id.detail_body);
        header.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        header.setAdapter(new SaleDetailHeaderViewAdapter(mContext));
        body.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        body.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        body.setAdapter(mSaleDetailBodyViewAdapter);
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
                        tv.setText(String.format(Locale.CHINA,"%d-%02d-%02d",year,monthOfYear,dayOfMonth));
                    }
                }
                // 设置初始日期
                , calendar.get(Calendar.YEAR)
                ,calendar.get(Calendar.MONTH)
                ,calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

}
