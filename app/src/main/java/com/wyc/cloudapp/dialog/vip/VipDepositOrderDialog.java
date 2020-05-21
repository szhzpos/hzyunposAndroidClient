package com.wyc.cloudapp.dialog.vip;

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

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnContextImp;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.WINDOW_SERVICE;

public class VipDepositOrderDialog extends DialogBaseOnContextImp {
    private int mCurrentStatusIndex = 0;
    private String[] mCashierNames,mCashierIDs;
    private EditText mStartDateEt,mStartTimeEt,mEndDateEt,mEndTimeEt,mCashierEt,mS_ex_statusEt,mOrderStatusEt;
    public VipDepositOrderDialog(@NonNull Context context) {
        super(context, context.getString(R.string.vip_deposit_o_sz));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.vip_deposit_order_dialog_layout);


        initWindowSize();
        initEndDateAndTime();
        initStartDateAndTime();
        initStatusEt();
        initCashierEt();
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
                //lp.width = (int)(0.85 * point.x) - 4;
                dialogWindow.setAttributes(lp);
            }
        }
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

    private void initStatusEt(){
        final String sz_all = "所有";
        final EditText s_ex_status_et = mS_ex_statusEt = findViewById(R.id.s_ex_status_et),order_status_et = mOrderStatusEt = findViewById(R.id.order_status_et);

        //交班状态
        s_ex_status_et.setOnFocusChangeListener(etFocusChangeListener);
        s_ex_status_et.setTag(0);
        s_ex_status_et.setText(sz_all);
        s_ex_status_et.setOnClickListener(etClickListener);

        order_status_et.setOnFocusChangeListener(etFocusChangeListener);
        order_status_et.setTag(0);
        order_status_et.setText(sz_all);
        order_status_et.setOnClickListener(etClickListener);

    }

    private View.OnClickListener etClickListener = this::showChooseDialog;

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

    private void showChooseDialog(final @NonNull View et){
        if (et instanceof EditText){
            String title = "";
            int index = 0;
            final Object et_tag = et.getTag();
            String[] items = new String[]{""};
            switch (et.getId()){
                case R.id.cashier_et:
                    items = mCashierNames;
                    title = mContext.getString(R.string.cashier_not_colon_sz);
                    break;
                case R.id.s_ex_status_et:
                    items = new String[]{"所有","未交班","已交班"};
                    title = mContext.getString(R.string.s_e_status_sz);
                    break;
                case R.id.order_status_et:
                    items = new String[]{"所有","未付款","已付款","已完成","已关闭"};
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
    private void setCashierEt(final @NonNull EditText cashier_et){
        cashier_et.setTag(mCashierIDs[mCurrentStatusIndex]);
        cashier_et.setText(mCashierNames[mCurrentStatusIndex]);
    }
}
