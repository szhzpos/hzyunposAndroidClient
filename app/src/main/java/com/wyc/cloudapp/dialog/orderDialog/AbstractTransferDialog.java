package com.wyc.cloudapp.dialog.orderDialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.normal.LoginActivity;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.AbstractTransferDetailsAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.ChangeNumOrPriceDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.print.receipts.TransferReceipts;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public abstract class AbstractTransferDialog extends AbstractDialogMainActivity {
    protected AbstractTransferDetailsAdapter mTransferDetailsAdapter;
    private onFinishListener mFinishListener;
    public AbstractTransferDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.s_e_sz));
    }

    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTransferBtn();
    }


    @Override
    public void dismiss(){
        super.dismiss();
        Printer.dismissPrintIcon(mContext);
    }
    @Override
    public void show(){
        super.show();
        Printer.showPrintIcon(mContext);
        updatePrintIcon();
    }

    @Override
    protected void updatePrintIcon() {
        final Window window = getWindow();
        final View view = window.getDecorView();
        CustomApplication.runInMainThread(()->{
            int[] ints = new int[2];
            view.getLocationOnScreen(ints);
            Printer.updatePrintIcon(mContext,ints[0] ,ints[1]);
        });
    }

    private void initTransferBtn(){
        final Button btn = findViewById(R.id.ok_);
        if (btn != null){
            btn.setOnClickListener(v -> {
                if (mTransferDetailsAdapter != null && !mTransferDetailsAdapter.isTransferInfoEmpty()){
                    final ChangeNumOrPriceDialog dialog = new ChangeNumOrPriceDialog(mContext,"钱箱现金",String.valueOf(0.0));
                    dialog.setYesOnclickListener(myDialog -> {
                        final StringBuilder err  = new StringBuilder();
                        if (mTransferDetailsAdapter.saveTransferDetailInfo(dialog.getContent(),err)){
                            TransferReceipts.print(mTransferDetailsAdapter.getTransferSumInfo().getString("ti_code"),mTransferDetailsAdapter.isTransferAmtNotVisible(),false);
                            mContext.runOnUiThread(()-> {
                                if (mFinishListener != null)mFinishListener.onFinish();
                                transferSuccess();
                            });
                        }else {
                            mContext.runOnUiThread(()-> MyDialog.displayErrorMessage(mContext, "保存交班信息错误：" +err));
                        }
                        myDialog.dismiss();
                    }).setNoOnclickListener(ChangeNumOrPriceDialog::dismiss).show();
                }
            });
        }
    }
    private void transferSuccess(){
        final CustomApplication app = CustomApplication.self();
        app.sync_transfer_order();
        MyDialog dialog = new MyDialog(mContext,"");
        dialog.setMessage("交班成功！");
        dialog.setYesOnclickListener(mContext.getString(R.string.OK), myDialog -> {
            dismiss();
            myDialog.dismiss();

            app.resetSync();

            LoginActivity.start(mContext);
            mContext.finish();
        }).show();
    }

    public interface onFinishListener{
        void onFinish();
    }
    public void setFinishListener(onFinishListener listener){
        mFinishListener = listener;
    }
    public void verifyTransfer(){
        final StringBuilder info  = new StringBuilder();
        int code = mTransferDetailsAdapter.verifyTransfer(info);
        switch (code){
            case 0:
                this.show();
                break;
            case 1://有正在支付的订单
                MyDialog.displayAskMessage(mContext, "有正在支付订单，是否现在处理?", myDialog -> {
                    final QueryRetailOrderDialog queryRetailOrderDialog = new QueryRetailOrderDialog(mContext);
                    queryRetailOrderDialog.show();
                    queryRetailOrderDialog.setQueryCondition(info.toString());
                    myDialog.dismiss();
                }, MyDialog::dismiss);
                break;
            case 2://当前收银员有挂单没处理
                MyDialog.displayAskMessage(mContext, "当前收银员有挂单信息没处理，是否现在处理?", myDialog -> {
                    mContext.disposeHangBill();
                    myDialog.dismiss();
                }, MyDialog::dismiss);
                break;
            default:
                MyDialog.displayErrorMessage(mContext, "确定是否可以交班错误：" + info);
                break;
        }
    }
    public static boolean verifyTransferPermissions(final MainActivity activity){
        return activity.verifyPermissions("6",null);
    }
}
