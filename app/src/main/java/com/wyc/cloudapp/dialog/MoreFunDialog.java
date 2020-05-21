package com.wyc.cloudapp.dialog;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.barcodeScales.BarCodeScaleDownDialog;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnMainActivityImp;
import com.wyc.cloudapp.dialog.orderDialog.QueryRefundOrderDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.dialog.vip.VipDepositOrderDialog;
import com.wyc.cloudapp.print.Printer;

public class MoreFunDialog extends DialogBaseOnMainActivityImp {
    public MoreFunDialog(@NonNull MainActivity context, final String title) {
        super(context,title);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化按钮事件
        initSyncBtn();
        initSetupBtn();
        initOpenCashboxBtn();
        initBarcodeScaleBtn();
        initAllRefundBtn();
        initQueryRefundOrderBtn();
        initVipDepositOrderBtn();
    }
    @Override
    protected int getContentLayoutId(){
        return R.layout.more_fun_dialog_layout;
    }

    private void initAllRefundBtn(){
        final Button btn = findViewById(R.id.all_refund_btn);
        btn.setOnClickListener(v -> {
            RefundDialog dialog = new RefundDialog(mContext,null);
            dialog.show();
            this.dismiss();
        });
    }
    private void initBarcodeScaleBtn(){
        final Button btn = findViewById(R.id.barcode_scale);
        btn.setOnClickListener(v -> {
            BarCodeScaleDownDialog barCodeScaleDownDialog = new BarCodeScaleDownDialog(mContext);
            barCodeScaleDownDialog.show();
            this.dismiss();
        });
    }
    private void initSetupBtn(){
        final Button btn = findViewById(R.id.setup_btn);
        btn.setOnClickListener(v -> {
            ParameterSettingDialog parameterSettingDialog = new ParameterSettingDialog(mContext);
            parameterSettingDialog.show(mContext.getSupportFragmentManager(),"");
            this.dismiss();
        });
    }
    private void initOpenCashboxBtn(){
        final Button btn = findViewById(R.id.o_cashbox);
        btn.setOnClickListener(v -> Printer.print(mContext, Printer.commandToStr(Printer.OPEN_CASHBOX)));
    }
    private void initSyncBtn(){
        final Button sync_btn = findViewById(R.id.sync_btn);
        sync_btn.setOnClickListener(v->{
            StringBuilder err = new StringBuilder();
            if (!SQLiteHelper.execDelete("barcode_info",null,null,err)){
                MyDialog.ToastMessage(err.toString(),mContext,getWindow());
            }
            mContext.sync(true);
            this.dismiss();
        });
    }
    private void initQueryRefundOrderBtn(){
        final Button btn = findViewById(R.id.query_local_refund_btn);
        btn.setOnClickListener(v -> {
            QueryRefundOrderDialog queryRefundOrderDialog = new QueryRefundOrderDialog(mContext);
            queryRefundOrderDialog.show();
            this.dismiss();
        });
    }
    private void initVipDepositOrderBtn(){
        final Button btn = findViewById(R.id.vip_deposit_order);
        btn.setOnClickListener(v -> {
            VipDepositOrderDialog vipDepositOrderDialog = new VipDepositOrderDialog(mContext);
            vipDepositOrderDialog.show();
            this.dismiss();
        });
    }
}
