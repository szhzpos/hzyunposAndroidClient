package com.wyc.cloudapp.dialog.orderDialog;

import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.MobileTransferDetailsAdapter;
import com.wyc.cloudapp.callback.PasswordEditTextReplacement;

import java.text.SimpleDateFormat;
import java.util.Locale;

import static android.content.Context.WINDOW_SERVICE;

public final class MobileTransferDialog extends AbstractTransferDialog {
    public MobileTransferDialog(@NonNull MainActivity context) {
        super(context);
        mTransferDetailsAdapter = new MobileTransferDetailsAdapter(mContext,new JSONArray());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTransferInfoList();

        initWindowSize();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.mobile_transfer_dialog_layout;
    }

    private void initWindowSize(){//初始化窗口尺寸
        final WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
        if (m != null){
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
            Point point = new Point();
            d.getSize(point);
            final Window dialogWindow = this.getWindow();
            if (dialogWindow != null){
                final WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.width =  point.x;
                lp.height = (int) (0.98 * point.y);
                dialogWindow.setAttributes(lp);
            }
        }
    }

    private void initTransferInfoList(){
        mTransferDetailsAdapter.setDatas(mContext.getCashierInfo().getString("cas_id"));
        final RecyclerView retail_details_list = findViewById(R.id.retail_details_list),refund_details_list = findViewById(R.id.refund_details_list),
                recharge_details_list = findViewById(R.id.recharge_details_list);

        retail_details_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        refund_details_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        recharge_details_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));

        retail_details_list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        refund_details_list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        recharge_details_list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        retail_details_list.setAdapter(new MobileTransferDetailsAdapter(mContext,mTransferDetailsAdapter.getTransferRetails()));
        refund_details_list.setAdapter(new MobileTransferDetailsAdapter(mContext,mTransferDetailsAdapter.getTransferRefunds()));
        recharge_details_list.setAdapter(new MobileTransferDetailsAdapter(mContext,mTransferDetailsAdapter.getTransferDeposits()));

        setFooterInfo();
    }



    private void setFooterInfo(){
        final JSONObject object = mTransferDetailsAdapter.getTransferSumInfo();
        final TextView cas_name = findViewById(R.id.cas_name_tv);
        cas_name.setText(mContext.getCashierInfo().getString("cas_name"));

        if (!object.isEmpty()){
            final TextView ti_start_time_tv = findViewById(R.id.ti_start_time_tv),payable_amt = findViewById(R.id.payable_amt)
                    ,ti_end_time_tv = findViewById(R.id.ti_end_time_tv),ti_code_tv = findViewById(R.id.ti_code_tv);

            ti_code_tv.setText(object.getString("ti_code"));
            if (mTransferDetailsAdapter.isTransferAmtNotVisible())payable_amt.setTransformationMethod(new PasswordEditTextReplacement());

            final SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
            ti_start_time_tv.setText(sf.format(object.getLongValue("order_b_date") * 1000));
            ti_end_time_tv.setText(sf.format(object.getLongValue("order_e_date") * 1000));

            payable_amt.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("sj_money")));
        }
    }
}
