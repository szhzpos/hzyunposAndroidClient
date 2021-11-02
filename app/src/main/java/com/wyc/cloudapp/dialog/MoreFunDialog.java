package com.wyc.cloudapp.dialog;

import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.SaleActivity;
import com.wyc.cloudapp.activity.normal.LoginActivity;
import com.wyc.cloudapp.activity.normal.NGiftSaleActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.barcodeScales.BarCodeScaleDownDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogSaleActivity;
import com.wyc.cloudapp.dialog.goods.GoodsManageDialog;
import com.wyc.cloudapp.dialog.orderDialog.QueryRefundOrderDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.dialog.vip.VipDepositOrderDialog;
import com.wyc.cloudapp.dialog.vip.VipInfoDialog;
import com.wyc.cloudapp.print.Printer;

import butterknife.ButterKnife;
import butterknife.OnClick;

public final class MoreFunDialog extends AbstractDialogSaleActivity {
    public MoreFunDialog(@NonNull SaleActivity context, final String title) {
        super(context,title);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(true);
        ButterKnife.bind(this);

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

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @OnClick(R.id.practice)
    void practice(){
        if (mContext.verifyPermissions("60",null) && MyDialog.showMessageToModalDialog(mContext,"是否重新登录练习收银模式?") == 1){
            dismiss();
            mContext.finish();
            CustomApplication.self().resetSync();
            CustomApplication.enterPracticeMode();
            LoginActivity.start(mContext);
        }
    }

    @OnClick(R.id.gift_sold)
    void gift_sold(){//购物卡销售
        if (mContext.verifyPermissions("30",null)){
            dismiss();
            NGiftSaleActivity.start(mContext);
        }
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
                mContext.setSingleRefundStatus(true);
                this.dismiss();
            }
        });
    }

    private void initAllRefundBtn(){
        final Button btn = findViewById(R.id.all_refund_btn);
        btn.setOnClickListener(v -> {
            mContext.setSingleRefundStatus(false);
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
                parameterSettingDialog.show();
                this.dismiss();
            }
        });
    }

    private void initOpenCashboxBtn(){
        final Button btn = findViewById(R.id.o_cashbox);
        btn.setOnClickListener(v -> {
            if (mContext.verifyOpenCashboxPermissions())
                Printer.print(Printer.commandToStr(Printer.OPEN_CASHBOX));
        });
    }
    private void initSyncBtn(){
        final Button sync_btn = findViewById(R.id.sync_btn);
        sync_btn.setOnClickListener(v->{
            clearBasicsData();
            manualSync();
            this.dismiss();
        });
    }

    private void clearBasicsData(){
        CustomApplication.self().clearBasicsData();
    }
    private void manualSync(){
        CustomApplication.self().manualSync();
    }

    private void initQueryRefundOrderBtn(){
        final Button btn = findViewById(R.id.query_local_refund_btn);
        btn.setOnClickListener(v -> {
            final QueryRefundOrderDialog queryRefundOrderDialog = new QueryRefundOrderDialog(mContext);
            queryRefundOrderDialog.show();
            closeWindow();
        });
    }

    private void initVipDepositOrderBtn(){
        final Button btn = findViewById(R.id.vip_deposit_order);
        btn.setOnClickListener(v -> {
            if (VipInfoDialog.verifyVipDepositOrderPermissions(mContext)){
                final VipDepositOrderDialog vipDepositOrderDialog = new VipDepositOrderDialog(mContext);
                vipDepositOrderDialog.show();
                vipDepositOrderDialog.triggerQuery();
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
