package com.wyc.cloudapp.dialog.goods;

import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.dialog.goods
 * @ClassName: BusinessSelectGoodsDialog
 * @Description: 业务单价选择商品对话框
 * @Author: wyc
 * @CreateDate: 2021/2/25 17:10
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/25 17:10
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class BusinessSelectGoodsDialog extends AbstractDialogMainActivity {
    public BusinessSelectGoodsDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.scan_code_label));
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.business_select_goods_dialog_layout;
    }

    @Override
    protected void initWindowSize() {
        final Display d = mContext.getDisplay(); // 获取屏幕宽、高用
        final Point point = new Point();
        d.getSize(point);
        final Window dialogWindow = this.getWindow();
        if (dialogWindow != null){
            final WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.CENTER);
            lp.width = (int)(0.9 * point.x);
            dialogWindow.setAttributes(lp);
        }
    }
}
