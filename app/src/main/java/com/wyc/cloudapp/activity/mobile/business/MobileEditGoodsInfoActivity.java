package com.wyc.cloudapp.activity.mobile.business;

import android.content.Context;
import android.content.Intent;
import com.wyc.cloudapp.R;
public class MobileEditGoodsInfoActivity extends EditGoodsInfoBaseActivity {

    @Override
    protected int getLayout() {
        return R.layout.activity_mobile_edit_good_info;
    }

    public static void start(Context context,final String barcode_id){
        /*barcode_id 为空时 ，以新增模式打开*/
        Intent intent = new Intent(context, MobileEditGoodsInfoActivity.class);
        intent.putExtra("barcodeId",barcode_id);
        context.startActivity(intent);
    }
}