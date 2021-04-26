package com.wyc.cloudapp.adapter.business;

import android.text.Html;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.MainActivity;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobilePurchaseRefundOrderAdapter
 * @Description: 采购退货单适配器
 * @Author: wyc
 * @CreateDate: 2021/4/12 9:47
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/4/12 9:47
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobilePurchaseRefundOrderAdapter extends MobileBaseOrderAdapter<MobileBaseOrderAdapter.MyViewHolder> {
    public MobilePurchaseRefundOrderAdapter(MainActivity activity) {
        super(activity);
    }

    @Override
    void bindViewHolder(MyViewHolder holder, JSONObject object) {
        holder.order_code_tv.setText(Html.fromHtml("<u>" + object.getString("cgd_code") + "</u>"));
        holder.order_code_tv.setTag(object.getString("cgd_id"));
    }
}
