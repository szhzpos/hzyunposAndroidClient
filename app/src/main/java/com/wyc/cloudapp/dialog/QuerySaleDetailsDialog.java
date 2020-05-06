package com.wyc.cloudapp.dialog;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.wyc.cloudapp.utils.Utils;

import java.util.Calendar;
import java.util.Locale;

import static android.content.Context.WINDOW_SERVICE;

public class QuerySaleDetailsDialog extends Dialog {
    private MainActivity mContext;
    private EditText mStartTime,mEndTime;
    public QuerySaleDetailsDialog(@NonNull MainActivity context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.query_sale_detail_dialog_layout);
        setCancelable(false);


        mEndTime = findViewById(R.id.end_time);
        initStartTime();

        //初始化表格
        initDetailTable();

        //初始化按钮事件
        findViewById(R.id._close).setOnClickListener(v->QuerySaleDetailsDialog.this.dismiss());

        //初始窗口尺寸
        initWindowSize();
    }

    private void initStartTime(){
        mStartTime = findViewById(R.id.start_time);
        mStartTime.setOnFocusChangeListener((v,b)-> {
            if (b)v.callOnClick();
            Utils.hideKeyBoard((EditText) v);
        });
        mStartTime.setSelectAllOnFocus(true);
        mStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog(mContext,(TextView) v,Calendar.getInstance());
            }
        });
    }

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
    private void initDetailTable(){
        final RecyclerView header = findViewById(R.id.detail_header),body = findViewById(R.id.detail_body);
        final SaleDetailBodyViewAdapter saleDetailBodyViewAdapter = new SaleDetailBodyViewAdapter(mContext);
        header.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        header.setAdapter(new SaleDetailHeaderViewAdapter(mContext));
        body.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        body.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        body.setAdapter(saleDetailBodyViewAdapter);
        saleDetailBodyViewAdapter.setDatas(mContext.getStoreInfo().getIntValue("stores_id"));
    }

    public static void showTimePickerDialog(final Activity activity, final TextView tv, Calendar calendar) {
        new TimePickerDialog( activity,3,
                // 绑定监听器
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view,int hourOfDay, int minute) {
                        tv.append(" ");
                        tv.append(String.format(Locale.CHINA,"%02d:%02d",hourOfDay,minute));
                    }
                }
                // 设置初始时间
                , calendar.get(Calendar.HOUR_OF_DAY)
                , calendar.get(Calendar.MINUTE)
                // true表示采用24小时制
                ,true).show();
    }

    public static void showDatePickerDialog(final Activity activity, final TextView tv, Calendar calendar) {
        new DatePickerDialog(activity,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,int monthOfYear, int dayOfMonth) {
                        tv.setText(String.format(Locale.CHINA,"%d-%02d-%02d",year,monthOfYear,dayOfMonth));
                        showTimePickerDialog(activity,tv,calendar);
                    }
                }
                // 设置初始日期
                , calendar.get(Calendar.YEAR)
                ,calendar.get(Calendar.MONTH)
                ,calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
}
