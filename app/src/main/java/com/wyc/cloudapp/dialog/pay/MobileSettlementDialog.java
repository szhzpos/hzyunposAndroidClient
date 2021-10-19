package com.wyc.cloudapp.dialog.pay;

import android.graphics.Point;
import android.view.Display;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.SaleActivity;

import static android.content.Context.WINDOW_SERVICE;

public final class MobileSettlementDialog extends AbstractSettlementDialog {
    public MobileSettlementDialog(SaleActivity context, String title) {
        super(context, title);
    }
    @Override
    protected int getContentLayoutId(){
        return R.layout.mobile_pay_dialog_content_layout;
    }
    @Override
    protected void initWindowSize(){
        final WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
        if (m != null){
            final Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
            final Point point = new Point();
            d.getSize(point);
            final Window dialogWindow = this.getWindow();
            if (dialogWindow != null){
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.width = point.x;
                lp.height = point.y;
                dialogWindow.setAttributes(lp);
            }
        }
    }
}
