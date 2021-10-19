package com.wyc.cloudapp.adapter.business;

import android.text.Html;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.base.MainActivity;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileWarehouseOrderAdapter
 * @Description: 库存单据适配器
 * @Author: wyc
 * @CreateDate: 2021/3/5 18:10
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/3/5 18:10
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileWarehouseOrderAdapter extends MobileBaseOrderAdapter<MobileBaseOrderAdapter.MyViewHolder> {

    public MobileWarehouseOrderAdapter(final MainActivity activity){
        super(activity);
    }
    @Override
    void bindViewHolder(MyViewHolder holder, JSONObject object) {
        holder.order_code_tv.setText(Html.fromHtml("<u>" + object.getString("rkd_code") + "</u>"));
        holder.order_code_tv.setTag(object.getString("rkd_id"));
    }

}