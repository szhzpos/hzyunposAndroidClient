package com.wyc.cloudapp.dialog.vip;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.SaleActivity;
import com.wyc.cloudapp.adapter.VipDepositOrderAdapter;
import com.wyc.cloudapp.dialog.orderDialog.AbstractQuerySuperDialog;
import com.wyc.cloudapp.utils.Utils;

import static android.content.Context.WINDOW_SERVICE;

public class VipDepositOrderDialog extends AbstractQuerySuperDialog {
    public VipDepositOrderDialog(@NonNull SaleActivity context) {
        super(context, context.getString(R.string.vip_deposit_o_sz));

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getTableLayoutId() {
        return R.layout.vip_deposit_order_dialog_layout;
    }
    @Override
    protected String query() {
        final EditText order_code_et = findViewById(R.id.order_code);
        final String start_date_time = mStartDateEt.getText() + " " + mStartTimeEt.getText(),end_date_time = mEndDateEt.getText() + " " + mEndTimeEt.getText(),
                sz_order_code = order_code_et.getText().toString(),sz_cashier = getCasId(mCashierEt);
        int s_ex_status = Utils.getViewTagValue(mS_ex_statusEt,0),order_status = Utils.getViewTagValue(mOrderStatusEt,0);

        final StringBuilder where_sql = new StringBuilder();

        where_sql.append("where a.stores_id = ").append(mContext.getStoreId());

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

        if(order_status != 0){
            if(where_sql.length() != 0)
                where_sql.append(" and ");

            where_sql.append(" status").append("=").append(order_status);

        }

        if(s_ex_status != 0){
            if(where_sql.length() != 0)
                where_sql.append(" and ");

            where_sql.append(" transfer_status = ").append(s_ex_status);
        }

        if(where_sql.length() != 0){
            where_sql.append(" and ");
        }
        where_sql.append("datetime(a.addtime, 'unixepoch', 'localtime') ").append("between ").append("'").append(start_date_time).append("'").append(" and ").append("'").append(end_date_time).append("'");

        return where_sql.toString();
    }

    @Override
    protected VipDepositOrderAdapter getAdapter() {
        return new VipDepositOrderAdapter(mContext);
    }

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
                lp.width = (int)(0.86 * point.x);
                dialogWindow.setAttributes(lp);
            }
        }
    }

    @Override
    protected void hideEt(){
        final TextView pay_s = findViewById(R.id.pay_s_tv),upload_s = findViewById(R.id.upload_s_tv);
        pay_s.setVisibility(View.GONE);
        upload_s.setVisibility(View.GONE);
        mPayStatusEt.setVisibility(View.GONE);
        mUploadStatusEt.setVisibility(View.GONE);
    }

}
