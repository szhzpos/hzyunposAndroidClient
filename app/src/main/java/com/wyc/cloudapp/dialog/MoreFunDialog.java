package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.barcodeScales.BarCodeScaleDownDialog;
import com.wyc.cloudapp.print.Printer;

public class MoreFunDialog extends BaseDialog {
    public MoreFunDialog(@NonNull MainActivity context,final String title) {
        super(context,title);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.more_fun_dialog_layout);

        //初始化按钮事件
        findViewById(R.id.sync_btn).setOnClickListener(v->{
            StringBuilder err = new StringBuilder();
            if (!SQLiteHelper.execDelete("barcode_info",null,null,err)){
                MyDialog.ToastMessage(err.toString(),mContext,getWindow());
            }
            mContext.sync(true);
            this.dismiss();
        });
        findViewById(R.id.o_cashbox).setOnClickListener(v -> Printer.print(mContext, Printer.commandToStr(Printer.OPEN_CASHBOX)));
        findViewById(R.id.setup_btn).setOnClickListener(v -> {
            ParameterSettingDialog parameterSettingDialog = new ParameterSettingDialog(mContext);
            parameterSettingDialog.show(mContext.getSupportFragmentManager(),"");
            this.dismiss();
        });
        findViewById(R.id.barcode_scale).setOnClickListener(v -> {
            BarCodeScaleDownDialog barCodeScaleDownDialog = new BarCodeScaleDownDialog(mContext);
            barCodeScaleDownDialog.show();
            this.dismiss();
        });
    }

}
