package com.wyc.cloudapp.dialog.orderDialog;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.RetailOrderAdapter;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.WINDOW_SERVICE;

public class QueryRetailOrderDialog extends AbstractQuerySuperDialog {
    public QueryRetailOrderDialog(@NonNull MainActivity context) {
        super(context,context.getString(R.string.retail_sz));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getTableLayoutId() {
        return R.layout.retail_order_dialog_layout;
    }

    @Override
    protected String query() {
        final EditText order_code_et = findViewById(R.id.order_code);
        final String start_date_time = mStartDateEt.getText() + " " + mStartTimeEt.getText(),end_date_time = mEndDateEt.getText() + " " + mEndTimeEt.getText(),
                sz_order_code = order_code_et.getText().toString(),sz_cashier = getCasId(mCashierEt);
        int pay_status = Utils.getViewTagValue(mPayStatusEt,0),s_ex_status = Utils.getViewTagValue(mS_ex_statusEt,0),
                upload_status = Utils.getViewTagValue(mUploadStatusEt,0),order_status = Utils.getViewTagValue(mOrderStatusEt,0);

        final StringBuilder where_sql = new StringBuilder();

        where_sql.append("where a.stores_id = ").append(mContext.getStoreId());

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
        where_sql.append("datetime(addtime, 'unixepoch', 'localtime') ").append("between ").append("'").append(start_date_time).append("'").append(" and ").append("'").append(end_date_time).append("'").append(" order by addtime desc");

        return where_sql.toString();
    }

    @Override
    protected AbstractQueryDataAdapter<? extends AbstractTableDataAdapter.SuperViewHolder> getAdapter() {
        return new RetailOrderAdapter(this);
    }

    @Override
    protected void initWindowSize(){//初始化窗口尺寸
        final WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
        if (m != null){
            final Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
            final Point point = new Point();
            d.getSize(point);
            final Window dialogWindow = this.getWindow();
            if (dialogWindow != null){
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.height = (int)(0.9 * point.y);
                dialogWindow.setAttributes(lp);
            }
        }
    }

    void setQueryCondition(final String start_time){
        long timestamp = Integer.valueOf(start_time) ;

        if (mStartDateEt != null)mStartDateEt.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(timestamp * 1000));
        if (mStartTimeEt != null)mStartTimeEt.setText(new SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(timestamp * 1000));

        if (mEndDateEt != null)mEndDateEt.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(new Date()));
        if (mEndTimeEt != null)mEndTimeEt.setText(mContext.getString(R.string.end_time_sz));

        if (mCashierEt != null){
            mCashierEt.setText(mContext.getCashierName());
            mCashierEt.setTag(mContext.getCashierId());
        }

        if (mPayStatusEt != null){
            mPayStatusEt.setText("支付中");
            mPayStatusEt.setTag(3);
        }

        triggerQuery();
    }
}
