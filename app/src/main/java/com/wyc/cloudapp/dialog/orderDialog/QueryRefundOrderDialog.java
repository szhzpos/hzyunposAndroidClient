package com.wyc.cloudapp.dialog.orderDialog;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import android.widget.EditText;
import android.widget.LinearLayout;


import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.adapter.RefundOrderViewAdapter;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import static android.content.Context.WINDOW_SERVICE;

public class QueryRefundOrderDialog extends AbstractQuerySuperDialog {
    public QueryRefundOrderDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.local_refund_order_sz));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        hideEt();
    }

    @Override
    protected int getTableLayoutId() {
        return R.layout.refund_order_dialog_layout;
    }

    @Override
    public void onAttachedToWindow(){
        triggerQuery();
        super.onAttachedToWindow();
    }

    @Override
    protected String query(){
        final LinearLayout refund_order_code_layout = findViewById(R.id.refund_order_code_layout);
        final StringBuilder where_sql = new StringBuilder();
        if (refund_order_code_layout != null){
            refund_order_code_layout.setVisibility(View.VISIBLE);
            final EditText order_code_et = findViewById(R.id.order_code),refund_order_code_et = refund_order_code_layout.findViewById(R.id.refund_order_code);
            final String start_date_time = mStartDateEt.getText() + " " + mStartTimeEt.getText(),end_date_time = mEndDateEt.getText() + " " + mEndTimeEt.getText(),
                    sz_order_code = order_code_et.getText().toString(),sz_cashier = Utils.getViewTagValue(mCashierEt,""),sz_refund_code = refund_order_code_et.getText().toString();

            where_sql.append("where a.stores_id = ").append(mContext.getStoreInfo().getIntValue("stores_id"));

            if(sz_order_code.length() != 0){
                if(where_sql.length() != 0)
                    where_sql.append(" and ");

                where_sql.append(" a.order_code").append(" like ").append("'%").append(sz_order_code).append("'");
            }
            if(sz_refund_code.length() != 0){
                if(where_sql.length() != 0)
                    where_sql.append(" and ");

                where_sql.append(" a.ro_code").append(" like ").append("'%").append(sz_refund_code).append("'");
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
        }

        return where_sql.toString();
    }

    @Override
    protected AbstractQueryDataAdapter getAdapter() {
        return new RefundOrderViewAdapter(mContext);
    }

    @Override
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

                Logger.d("屏幕尺寸X:%d,Y:%d",point.x,point.y);

                lp.height = (int)(0.95 * point.y);
                lp.width = (int)(0.9 * point.x) - 20;
                dialogWindow.setAttributes(lp);
            }
        }
    }
    @Override
    protected void hideEt(){
        LinearLayout other_status__layout = findViewById(R.id.other_status__layout);
        if (null != other_status__layout)other_status__layout.setVisibility(View.GONE);
    }
}
