package com.wyc.cloudapp.dialog;

import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnMainActivityImp;

public class AddGoodsInfoDialog extends DialogBaseOnMainActivityImp {
    private MainActivity mContext;
    private String mBarcode;
    public AddGoodsInfoDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.goods_i_sz));
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initBarcode();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.add_goods_dialog_layout;
    }

    private void initBarcode(){
        final EditText barcdoe_et = findViewById(R.id.barcode_et);
        if (barcdoe_et != null){
            barcdoe_et.setText(mBarcode);
        }
    }

    public static boolean verifyGoodsAddPermissions(MainActivity context){
        return context.verifyPermissions("31",null,false);
    }

    public void setBarcode(final String barcode){
        mBarcode = barcode;
    }
}
