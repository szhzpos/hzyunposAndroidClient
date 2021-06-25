package com.wyc.cloudapp.mobileFragemt;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.MobileCashierActivity;
import com.wyc.cloudapp.activity.mobile.MobileNavigationActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.orderDialog.MobileQueryRefundOrderDialog;
import com.wyc.cloudapp.dialog.orderDialog.MobileQueryRetailOrderDialog;
import com.wyc.cloudapp.dialog.vip.AbstractVipChargeDialog;
import com.wyc.cloudapp.dialog.vip.MobileVipChargeDialog;
import com.wyc.cloudapp.dialog.vip.MobileVipDepositOrderDialog;
import com.wyc.cloudapp.dialog.vip.VipInfoDialog;

import java.util.ArrayList;
import java.util.List;

public final class MobileCashierDeskFragment extends AbstractJumpFragment {
    @Override
    protected int getRootLayout() {
        return R.layout.mobile_cashier_desk_fragment_layout;
    }
    @Override
    protected int getMainViewId() {
        return R.id.main_linearLayout;
    }

    @Override
    protected List<Integer> getIgnoreView() {
        final List<Integer> ids = new ArrayList<>();
        if (!verifyOnceCardPermissions()){
            ids.add(R.id.once_card_layout);
        }
        if (!verifyGiftCardPermissions()){
            ids.add(R.id.gift_card_layout);
        }
        return ids;
    }

    private boolean verifyOnceCardPermissions(){
        return mContext.verifyPermissions("27",null,false);
    }
    private boolean verifyGiftCardPermissions(){
        return mContext.verifyPermissions("30",null,false);
    }

    @Override
    protected void triggerItemClick(View v) {
        final Context context = getContext();
        if (null != context){
            final int v_id = v.getId();
            try {
                if (v_id == R.id.data_exchange_tv){
                    final CustomApplication app = CustomApplication.self();
                    if (app.isConnection()){
                        if (MyDialog.showMessageToModalDialog(context,"是否进行数据交换?") == 1){
                            app.manualSync();
                        }
                    }else {
                        MyDialog.ToastMessage("网络异常不允许同步!",context,null);
                    }
                }else if (v_id ==R.id.fd_shift_exchange_tv){
                    final Activity activity = getActivity();
                    if (activity instanceof MobileNavigationActivity){
                        ((MobileNavigationActivity)activity).transfer();
                    }
                }else if(v_id == R.id.vip_charge_tv){
                    if (VipInfoDialog.verifyVipDepositPermissions(mContext)){
                        final AbstractVipChargeDialog dialog = new MobileVipChargeDialog(mContext);
                        dialog.show();
                    }
                }else if (v_id == R.id.fg_refund_tv){
                    final Intent intent = new Intent();
                    intent.setClass(context, MobileCashierActivity.class);
                    intent.putExtra("title",context.getString(R.string.fg_casher_sz));
                    intent.putExtra("singleRefundStatus",true);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }else if (v_id == R.id.trade_query_tv){
                    final MobileQueryRetailOrderDialog dialog = new MobileQueryRetailOrderDialog(mContext);
                    dialog.show();
                }else if (v_id == R.id.refund_query_tv){
                    final MobileQueryRefundOrderDialog dialog = new MobileQueryRefundOrderDialog(mContext);
                    dialog.show();
                }else if (v_id == R.id.charge_query_tv){
                    if (VipInfoDialog.verifyVipDepositOrderPermissions(mContext)){
                        final MobileVipDepositOrderDialog depositOrderDialog = new MobileVipDepositOrderDialog(mContext);
                        depositOrderDialog.show();
                    }
                }else{
                    final Intent intent = new Intent();
                    intent.setClassName(context,context.getPackageName().concat(".") + v.getTag());
                    if (v instanceof TextView)intent.putExtra("title",((TextView)v).getText());
                    startActivity(intent);
                }
            }catch (ActivityNotFoundException e){
                e.printStackTrace();
                MyDialog.ToastMessage("暂不支持此功能!",context,null);
            }
        }
    }
}