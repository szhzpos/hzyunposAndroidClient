package com.wyc.cloudapp.adapter.business;

import android.text.Html;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.base.MainActivity;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobilePurchaseOrderAdapter
 * @Description: 采购订货单适配器
 * @Author: wyc
 * @CreateDate: 2021/2/22 16:11
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/22 16:11
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobilePurchaseOrderAdapter extends MobileBaseOrderAdapter<MobileBaseOrderAdapter.MyViewHolder> {

    public MobilePurchaseOrderAdapter(final MainActivity activity){
        super(activity);
    }

    @Override
    void bindViewHolder(MyViewHolder holder, JSONObject object) {
        holder.order_code_tv.setText(Html.fromHtml("<u>" + object.getString("cgd_code") + "</u>"));
        holder.order_code_tv.setTag(object.getString("cgd_id"));
    }

}
