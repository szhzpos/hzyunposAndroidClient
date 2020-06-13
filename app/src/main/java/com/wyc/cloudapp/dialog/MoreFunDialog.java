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

public final class MoreFunDialog extends DialogBaseOnMainActivityImp {
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
        initBarcodeScaleDownLoadBtn();
        initAllRefundBtn();
        initQueryRefundOrderBtn();
        initVipDepositOrderBtn();
        initSingleRefundBtn();
        initPresentBtn();
        initDataUploadBtn();
    }
    @Override
    protected int getContentLayoutId(){
        return R.layout.more_fun_dialog_layout;
    }

    private void initDataUploadBtn(){
        final Button btn = findViewById(R.id.data_upload_btn);
        btn.setOnClickListener(v -> {
            this.dismiss();
            mContext.data_upload();
        });
    }

    private void initPresentBtn(){
        final Button btn = findViewById(R.id.present_btn);
        btn.setOnClickListener(v -> {
            if (mContext.present())
                this.dismiss();
        });
    }

    private void initSingleRefundBtn(){
        final Button btn = findViewById(R.id.single_refund_btn);
        btn.setOnClickListener(v -> {
            if (RefundDialog.verifyRefundPermission(mContext)){
                mContext.setSingle(true);
                this.dismiss();
            }
        });
    }

    private void initAllRefundBtn(){
        final Button btn = findViewById(R.id.all_refund_btn);
        btn.setOnClickListener(v -> {
            RefundDialog dialog = new RefundDialog(mContext,null);
            dialog.show();
            this.dismiss();
        });
    }

    private boolean verifyBSdownloadPermissions(){
        return mContext.verifyPermissions("15",null);
    }
    private void initBarcodeScaleDownLoadBtn(){
        final Button btn = findViewById(R.id.barcode_scale);
        btn.setOnClickListener(v -> {
            if (verifyBSdownloadPermissions()){
                final BarCodeScaleDownDialog barCodeScaleDownDialog = new BarCodeScaleDownDialog(mContext);
                barCodeScaleDownDialog.show();
                this.dismiss();
            }
        });
    }

    private boolean verifySetupPermissions(){
        return mContext.verifyPermissions("16",null);
    }
    private void initSetupBtn(){
        final Button btn = findViewById(R.id.setup_btn);
        btn.setOnClickListener(v -> {
            if (verifySetupPermissions()){
                final ParameterSettingDialog parameterSettingDialog = new ParameterSettingDialog(mContext);
                parameterSettingDialog.show(mContext.getSupportFragmentManager(),"");
                this.dismiss();
            }
        });
    }

    private boolean verifyOpenCashboxPermissions(){
        return mContext.verifyPermissions("5",null);
    }
    private void initOpenCashboxBtn(){
        final Button btn = findViewById(R.id.o_cashbox);
        btn.setOnClickListener(v -> {
            if (verifyOpenCashboxPermissions())
                Printer.print(mContext, Printer.commandToStr(Printer.OPEN_CASHBOX));
        });
    }
    private void initSyncBtn(){
        final Button sync_btn = findViewById(R.id.sync_btn);
        sync_btn.setOnClickListener(v->{
            StringBuilder err = new StringBuilder();
            if (SQLiteHelper.execDelete("barcode_info",null,null,err) < 0){
                MyDialog.ToastMessage(err.toString(),mContext,getWindow());
            }
            mContext.sync(true);
            this.dismiss();
        });
    }
    private void initQueryRefundOrderBtn(){
        final Button btn = findViewById(R.id.query_local_refund_btn);
        btn.setOnClickListener(v -> {
            final QueryRefundOrderDialog queryRefundOrderDialog = new QueryRefundOrderDialog(mContext);
            queryRefundOrderDialog.show();
            this.dismiss();
        });
    }

    private boolean verifyVipDepositOrderPermissions(){
        return mContext.verifyPermissions("24",null);
    }
    private void initVipDepositOrderBtn(){
        final Button btn = findViewById(R.id.vip_deposit_order);
        btn.setOnClickListener(v -> {
            if (verifyVipDepositOrderPermissions()){
                final VipDepositOrderDialog vipDepositOrderDialog = new VipDepositOrderDialog(mContext);
                vipDepositOrderDialog.show();
                this.dismiss();
            }
        });
    }
}
