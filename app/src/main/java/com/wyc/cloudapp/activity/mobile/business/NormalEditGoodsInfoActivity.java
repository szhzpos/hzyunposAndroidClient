package com.wyc.cloudapp.activity.mobile.business;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.WindowManager;

import com.wyc.cloudapp.R;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.activity.mobile.business
 * @ClassName: NomorlEditGoodsInfoActivity
 * @Description: java类作用描述
 * @Author: wyc
 * @CreateDate: 2021-09-23 10:36
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-09-23 10:36
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class NormalEditGoodsInfoActivity extends EditGoodsInfoBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        final Display d = wm.getDefaultDisplay(); // 获取屏幕宽、高用
        final Point point = new Point();
        d.getSize(point);

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        attributes.dimAmount = 0.5f;
        attributes.x = 0;
        attributes.y = 0;
        attributes.width = (int) (point.x * 0.70);
        attributes.height = (int) (point.y * 0.98);
        getWindow().setAttributes(attributes);
    }
    public static void start(Context context, final String barcode_id){
        /*barcode_id 为空时 ，以新增模式打开*/
        Intent intent = new Intent(context, NormalEditGoodsInfoActivity.class);
        intent.putExtra("barcodeId",barcode_id);
        context.startActivity(intent);
    }
    @Override
    protected int getLayout() {
        return R.layout.activity_normal_edit_goods_info;
    }
}
