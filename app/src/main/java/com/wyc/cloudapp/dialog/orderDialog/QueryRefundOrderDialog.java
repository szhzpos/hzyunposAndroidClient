package com.wyc.cloudapp.dialog.orderDialog;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.RefundOrderViewAdapter;
import com.wyc.cloudapp.adapter.RetailOrderViewAdapter;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnContextImp;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnMainActivityImp;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.WINDOW_SERVICE;

public class QueryRefundOrderDialog extends DialogBaseOnMainActivityImp {
    private int mCurrentStatusIndex = 0;
    private EditText mStartDateEt,mStartTimeEt,mEndDateEt,mEndTimeEt,mCashierEt;
    private String[] mCashierNames,mCashierIDs;
    private RefundOrderViewAdapter mRefundOrderViewAdapter;
    public QueryRefundOrderDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.local_refund_order_sz));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.query_refund_order_dialog_layout);

        initWindowSize();
        initStartDateAndTime();
        initEndDateAndTime();
        initCashierEt();
        initOrderDetailTable();

        initQueryBtn();
    }

    private void initQueryBtn(){
        final Button query_btn = findViewById(R.id.query_btn);
        if (query_btn != null){
            query_btn.setOnClickListener(v -> {
                final EditText order_code_et = findViewById(R.id.order_code);
                final String start_date_time = mStartDateEt.getText() + " " + mStartTimeEt.getText(),end_date_time = mEndDateEt.getText() + " " + mEndTimeEt.getText(),
                        sz_order_code = order_code_et.getText().toString(),sz_cashier = Utils.getViewTagValue(mCashierEt,"");

                final StringBuilder where_sql = new StringBuilder();

                where_sql.append("where a.stores_id = ").append(mContext.getStoreInfo().getIntValue("stores_id"));

                if(sz_order_code.length() != 0){
                    if(where_sql.length() != 0)
                        where_sql.append(" and ");

                    where_sql.append(" a.order_code").append(" like ").append("'%").append(sz_order_code).append("'");
                }
                if(!"0".equals(sz_cashier)){
                    if(where_sql.length() != 0)
                        where_sql.append(" and ");

                    where_sql.append(" a.cashier_id").append("=").append(sz_cashier);
                }
                if(where_sql.length() != 0){
                    where_sql.append(" and ");
                }
                where_sql.append("datetime(a.addtime, 'unixepoch', 'localtime') ").append("between ").append("'").append(start_date_time).append("'").append(" and ").append("'").append(end_date_time).append("'");

                mRefundOrderViewAdapter.setDatas(where_sql.toString());
            });
            query_btn.callOnClick();
        }
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

                Logger.d("屏幕尺寸X:%d,Y:%d",point.x,point.y);

                lp.height = (int)(0.95 * point.y);
                lp.width = (int)(0.9 * point.x) - 20;
                dialogWindow.setAttributes(lp);
            }
        }
    }
    private void initOrderDetailTable(){
        mRefundOrderViewAdapter = new RefundOrderViewAdapter(mContext);
        final RecyclerView body = findViewById(R.id.refund_order_body);
        body.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        body.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        body.setAdapter(mRefundOrderViewAdapter);
    }
    private void initEndDateAndTime(){
        final EditText end_date = mEndDateEt = findViewById(R.id.end_date),end_time = mEndTimeEt = findViewById(R.id.end_time);
        if (null != end_date && null != end_time){
            end_date.setOnFocusChangeListener(etFocusChangeListener);
            end_date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
            end_date.setOnClickListener(v -> Utils.showDatePickerDialog(mContext,(TextView) v, Calendar.getInstance()));

            end_time.setOnFocusChangeListener(etFocusChangeListener);
            end_time.setOnClickListener(v -> Utils.showTimePickerDialog(mContext,(TextView) v,Calendar.getInstance()));
        }
    }
    private void initStartDateAndTime(){
        final EditText start_date = mStartDateEt = findViewById(R.id.start_date),start_time = mStartTimeEt = findViewById(R.id.start_time);
        if (null != start_date && null != start_time) {
            start_date.setOnFocusChangeListener(etFocusChangeListener);
            start_date.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
            start_date.setOnClickListener(v -> Utils.showDatePickerDialog(mContext, (TextView) v, Calendar.getInstance()));

            start_time.setOnFocusChangeListener(etFocusChangeListener);
            start_time.setOnClickListener(v -> Utils.showTimePickerDialog(mContext, (TextView) v, Calendar.getInstance()));
        }
    }
    private View.OnFocusChangeListener etFocusChangeListener = (v, b)->{
        if (b)v.callOnClick();
        Utils.hideKeyBoard((EditText) v);
    };
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
                cashier_et.setOnClickListener(v -> chooseDialog(mCashierEt,mCashierNames,getCashierIdIndex((String) v.getTag()),mContext.getString(R.string.cashier_not_colon_sz)));
                setCashierEt(cashier_et);
            }else{
                MyDialog.ToastMessage(cashier_et,"初始化收银员错误：" + err,mContext,getWindow());
            }
        }
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
    private void setCashierEt(final @NonNull EditText cashier_et){
        cashier_et.setTag(mCashierIDs[mCurrentStatusIndex]);
        cashier_et.setText(mCashierNames[mCurrentStatusIndex]);
    }
    private void chooseDialog(final @NonNull EditText et,final String[] currentStatusItems,int index,final String title){
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setSingleChoiceItems(currentStatusItems, index, (dialog, which) -> mCurrentStatusIndex = which);
        builder.setPositiveButton(mContext.getString(R.string.OK), (dialog, which) -> {
            if (mCurrentStatusIndex < currentStatusItems.length && mCurrentStatusIndex >= 0){
                if (currentStatusItems == mCashierNames){
                    setCashierEt(et);
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
}
