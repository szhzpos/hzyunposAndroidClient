package com.wyc.cloudapp.adapter.business;

import android.text.Html;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.utils.Utils;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileWholesaleRefundOrderAdapter
 * @Description: 批发退货单适配器
 * @Author: wyc
 * @CreateDate: 2021/4/23 15:06
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/4/23 15:06
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileWholesaleRefundOrderAdapter extends MobileWholesaleOrderAdapter {
    public MobileWholesaleRefundOrderAdapter(MainActivity activity) {
        super(activity);
    }
    @Override
    void bindViewHolder(MobileBaseOrderAdapter.MyViewHolder holder, JSONObject object) {
        ((MyViewHolder)holder).cs_name_tv.setText(object.getString("cs_xname"));
        holder.order_code_tv.setText(Html.fromHtml("<u>" + object.getString("refund_code") + "</u>"));
        holder.order_code_tv.setTag(object.getString("order_id"));
        holder.date_tv.setText(Utils.formatDataWithTimestamp(object.getLongValue("addtime") * 1000));
    }
}
