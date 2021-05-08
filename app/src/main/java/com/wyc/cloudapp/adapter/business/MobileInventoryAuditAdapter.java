package com.wyc.cloudapp.adapter.business;

import android.text.Html;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.business.MobilePracticalInventoryAddOrderActivity;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileInventoryAuditAdapter
 * @Description: 盘点单审核单据适配器
 * @Author: wyc
 * @CreateDate: 2021/5/8 10:29
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/8 10:29
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileInventoryAuditAdapter extends MobileInventoryTaskAdapter {
    public MobileInventoryAuditAdapter(MainActivity activity) {
        super(activity);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        super.onBindViewHolder(holder,position);
        holder.inventory_task_code_tv.setTag(mDatas.getJSONObject(position).getString("pcd_task_id"));
    }
}
