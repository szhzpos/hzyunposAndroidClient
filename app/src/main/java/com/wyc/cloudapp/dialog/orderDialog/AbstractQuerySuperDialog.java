package com.wyc.cloudapp.dialog.orderDialog;

import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.SaleGoodsItemDecoration;
import com.wyc.cloudapp.adapter.SuperItemDecoration;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogBaseOnMainActivityImp;
import com.wyc.cloudapp.dialog.vip.VipDepositOrderDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.WINDOW_SERVICE;

public abstract class AbstractQuerySuperDialog extends AbstractDialogBaseOnMainActivityImp {
    private int mCurrentStatusIndex = 0;
    private String[] mCashierNames,mCashierIDs;
    protected EditText mStartDateEt,mStartTimeEt,mEndDateEt,mEndTimeEt,mPayStatusEt,mCashierEt,mS_ex_statusEt,mUploadStatusEt,mOrderStatusEt;
    private AbstractQueryDataAdapter mAdapter;
    private Button mQueryBtn;
    protected AbstractQuerySuperDialog(@NonNull MainActivity context, final String title) {
        super(context, title);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTableLayout();
        initStartDateAndTime();
        initEndDateAndTime();
        initStatusEt();
        initCashierEt();
        initTable();
        initQueryBtn();
        initWindowSize();
    }
    @Override
    protected int getContentLayoutId(){
        return R.layout.query_surper_dialog_layout;
    }

    protected abstract int getTableLayoutId();
    protected abstract String query();
    protected abstract AbstractQueryDataAdapter getAdapter();

    protected void initWindowSize(){//初始化窗口尺寸
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
    protected String getCasId(final EditText et){
        Object tag;
        String tag_v = "0";
        if (et != null && (tag = et.getTag()) != null){
            if (tag instanceof String){
                tag_v = (String) tag;
            }
        }
        return tag_v;
    }
    protected void hideEt(){}
    public void triggerQuery(){
        if (mQueryBtn != null)mQueryBtn.callOnClick();
    }


    private void initTable() {
        mAdapter = getAdapter();
        final RecyclerView body = findViewById(R.id.table_body);//子类的布局中id必须为table_body
        if (body != null && mAdapter != null){
            body.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            body.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            body.setAdapter(mAdapter);
            final SuperItemDecoration superItemDecoration = new SuperItemDecoration();
            superItemDecoration.registerGlobalLayoutToRecyclerView(body,mContext.getResources().getDimension(R.dimen.table_row_height));
        }
    }
    private void setTableLayout(){
        final LinearLayout main_layout = findViewById(R.id.query_main_layout);
        if (null != main_layout) {
            final View dialog_content = View.inflate(mContext, getTableLayoutId(), null);
            if (dialog_content != null)
                main_layout.addView(dialog_content, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }
    private void initQueryBtn(){
        final Button query_btn = findViewById(R.id.query_btn);
        if (query_btn != null){
            query_btn.setOnClickListener(v -> {
                mAdapter.setDatas(query());
            });
            mQueryBtn = query_btn;
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
    private void initStatusEt(){
        final String sz_all = "所有";
        final EditText pay_status_et = mPayStatusEt = findViewById(R.id.pay_status_et),s_ex_status_et = mS_ex_statusEt = findViewById(R.id.s_ex_status_et),
                upload_status_et = mUploadStatusEt = findViewById(R.id.upload_status_et),order_status_et = mOrderStatusEt = findViewById(R.id.order_status_et);

        hideEt();

        if (pay_status_et.getVisibility() == View.VISIBLE){
            pay_status_et.setOnFocusChangeListener(etFocusChangeListener);
            pay_status_et.setTag(0);
            pay_status_et.setText(sz_all);
            pay_status_et.setOnClickListener(etClickListener);
        }

        //交班状态
        if (s_ex_status_et.getVisibility() == View.VISIBLE){
            s_ex_status_et.setOnFocusChangeListener(etFocusChangeListener);
            s_ex_status_et.setTag(0);
            s_ex_status_et.setText(sz_all);
            s_ex_status_et.setOnClickListener(etClickListener);
        }

        if (upload_status_et.getVisibility() == View.VISIBLE){
            upload_status_et.setOnFocusChangeListener(etFocusChangeListener);
            upload_status_et.setTag(0);
            upload_status_et.setText(sz_all);
            upload_status_et.setOnClickListener(etClickListener);
        }

        if (order_status_et.getVisibility() == View.VISIBLE){
            order_status_et.setOnFocusChangeListener(etFocusChangeListener);
            order_status_et.setTag(0);
            order_status_et.setText(sz_all);
            order_status_et.setOnClickListener(etClickListener);
        }
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

                    if (this instanceof VipDepositOrderDialog)
                    items = new String[]{"所有","未付款","已付款","已完成","已关闭"};
                    else
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
                triggerQuery();
            }
            dialog.dismiss();
        });
        builder.setNegativeButton(mContext.getString(R.string.cancel), (dialog, which) -> {
            dialog.dismiss();
        });

        final AlertDialog alertDialog = builder.create();

        int blue = mContext.getColor(R.color.blue);

        final Resources resources = mContext.getResources();
        final TextView titleTv = new TextView(mContext);
        titleTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        titleTv.setPadding(5,5,5,5);
        titleTv.setTextSize(Utils.px2sp(mContext,resources.getDimension(R.dimen.font_size_22)));
        titleTv.setTextColor(blue);
        titleTv.setText(title);
        alertDialog.setCustomTitle(titleTv);

        alertDialog.show();

        final ListView listView = alertDialog.getListView();
        listView.setDivider(mContext.getDrawable(R.color.gray_subtransparent));
        listView.setDividerHeight(1);
        listView.setBackground(mContext.getDrawable(R.drawable.border_sub_gray));

        float btn_font_size = Utils.px2sp(mContext,resources.getDimension(R.dimen.font_size_16));
        final Button cancel = alertDialog.getButton(BUTTON_NEGATIVE), ok = alertDialog.getButton(BUTTON_POSITIVE);
        cancel.setTextColor(blue);
        cancel.setTextSize(btn_font_size);

        ok.setTextColor(blue);
        ok.setTextSize(btn_font_size);

        final Window window = alertDialog.getWindow();
        if (null != window){
            final WindowManager.LayoutParams  lp= window.getAttributes();
            lp.width= (int) resources.getDimension(R.dimen.size_368);
            if (currentStatusItems.length > 3){
                lp.height= (int) resources.getDimension(R.dimen.size_288);
            }
            window.setAttributes(lp);
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

}
