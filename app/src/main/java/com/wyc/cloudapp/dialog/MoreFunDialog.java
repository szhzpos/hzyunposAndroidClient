package com.wyc.cloudapp.dialog;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.barcodeScales.BarCodeScaleDownDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.dialog.goods.GoodsManageDialog;
import com.wyc.cloudapp.dialog.orderDialog.QueryRefundOrderDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.dialog.vip.VipDepositOrderDialog;
import com.wyc.cloudapp.print.Printer;

public final class MoreFunDialog extends AbstractDialogMainActivity {
    public MoreFunDialog(@NonNull MainActivity context, final String title) {
        super(context,title);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);

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
        initPriceAdjustBtn();
        initGoodsManageBtn();
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
            mContext.setSingle(false);
            final RefundDialog dialog = new RefundDialog(mContext,null);
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
            final StringBuilder err = new StringBuilder();
            if (SQLiteHelper.execDelete("barcode_info",null,null,err) < 0){
                MyDialog.ToastMessage(err.toString(),mContext,getWindow());
            }
            if (SQLiteHelper.execDelete("pay_method",null,null,err) < 0){
                MyDialog.ToastMessage(err.toString(),mContext,getWindow());
            }
            if (SQLiteHelper.execDelete("shop_category",null,null,err) < 0){
                MyDialog.ToastMessage(err.toString(),mContext,getWindow());
            }
            if (SQLiteHelper.execDelete("goods_group",null,null,err) < 0){
                MyDialog.ToastMessage(err.toString(),mContext,getWindow());
            }
            if (SQLiteHelper.execDelete("goods_group_info",null,null,err) < 0){
                MyDialog.ToastMessage(err.toString(),mContext,getWindow());
            }
            if (SQLiteHelper.execDelete("promotion_info",null,null,err) < 0){
                MyDialog.ToastMessage(err.toString(),mContext,getWindow());
            }

            mContext.manualSync();
            this.dismiss();
        });
    }
    private void initQueryRefundOrderBtn(){
        final Button btn = findViewById(R.id.query_local_refund_btn);
        btn.setOnClickListener(v -> {
            final QueryRefundOrderDialog queryRefundOrderDialog = new QueryRefundOrderDialog(mContext);
            queryRefundOrderDialog.show();
            closeWindow();
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
                closeWindow();
            }
        });
    }

    private boolean verifyPriceAdjustPermissions(){
        return mContext.verifyPermissions("32",null);
    }
    private void initPriceAdjustBtn(){
        final Button btn = findViewById(R.id.price_adj_btn);
        btn.setOnClickListener(v -> {
            boolean code = true;
            if (!mContext.getSaleData().isEmpty())code = mContext.clearSaleGoods();
            if (code && verifyPriceAdjustPermissions()){
                mContext.showAdjustPriceDialog();
                closeWindow();
            }
        });
    }
    private void initGoodsManageBtn(){
        final Button btn = findViewById(R.id.goods_manage_btn);
        btn.setOnClickListener(v -> {
            final GoodsManageDialog dialog = new GoodsManageDialog(mContext);
            dialog.show();
            this.dismiss();
        });
    }
}
