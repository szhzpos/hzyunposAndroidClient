package com.wyc.cloudapp.dialog.vip;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.dialog.vip
 * @ClassName: VipDetailInfoDialog
 * @Description: 显示会员详细信息
 * @Author: wyc
 * @CreateDate: 2021/4/14 14:34
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/4/14 14:34
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class VipDetailInfoWindow extends PopupWindow {
    private final JSONObject mVip;
    private final MainActivity mContext;
    public VipDetailInfoWindow(@NonNull MainActivity context, final JSONObject vip) {
        super(context);
        mContext = context;
        mVip = vip;

        setOutsideTouchable(true);
        setBackgroundDrawable(context.getDrawable(R.color.transparent));
        setContentView(View.inflate(context,R.layout.vip_details_pop_window_layout,null));

        showVipInfo();

        if (context instanceof LifecycleOwner){
            ((LifecycleOwner)context).getLifecycle().addObserver(new LifecycleEventObserver() {
                @Override
                public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
                    if (Lifecycle.Event.ON_DESTROY == event){
                        dismiss();
                        source.getLifecycle().removeObserver(this);
                    }
                }
            });
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        setBackgroundAlpha(1.0f);
    }

    @Override
    public void showAtLocation(View anchor,int gravity,int x,int y) {
        initWidth();
        super.showAtLocation(anchor,gravity,x, y);
        setBackgroundAlpha(0.5f);
    }
    private void initWidth(){
        final Display d = mContext.getDisplay(); // 获取屏幕宽、高用
        final Point point = new Point();
        d.getSize(point);
        setWidth((int) (point.x * 0.95));
    }

    private void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = mContext.getWindow()
                .getAttributes();
        lp.alpha = bgAlpha;
        mContext.getWindow().setAttributes(lp);
    }

    private void showVipInfo(){
        final JSONObject object = mVip;
        if (null != object){
            final View root = getContentView();
           final TextView vip_name = root.findViewById(R.id.vip_name),vip_sex = root.findViewById(R.id.vip_sex),vip_p_num = root.findViewById(R.id.vip_p_num),
            vip_card_id = root.findViewById(R.id.vip_card_id),vip_balance = root.findViewById(R.id.vip_balance),vip_integral = root.findViewById(R.id.vip_integral),
            vipGrade = root.findViewById(R.id.vip_grade_tv),vipDiscount = root.findViewById(R.id.vip_discount);

            vipGrade.setText(Utils.getNullStringAsEmpty(object,"grade_name"));

            vip_name.setText(object.getString("name"));
            vip_sex.setText(object.getString("sex"));
            vip_p_num.setText(object.getString("mobile"));

            vipDiscount.setText(object.getString("discount"));
            vip_card_id.setText(object.getString("card_code"));
            vip_balance.setText(String.format(Locale.CHINA,"%.2f",object.getDouble("money_sum")));
            vip_integral.setText(String.format(Locale.CHINA,"%.2f",object.getDouble("points_sum")));
        }
    }
}
