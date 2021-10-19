package com.wyc.cloudapp.adapter.business;

import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileInventoryOrderAdapter
 * @Description: 盘点单适配器
 * @Author: wyc
 * @CreateDate: 2021/4/23 15:59
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/4/23 15:59
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileInventoryOrderAdapter extends AbstractBusinessOrderDataAdapter<MobileInventoryOrderAdapter.MyViewHolder> {
    public MobileInventoryOrderAdapter(MainActivity activity) {
        super(activity);
    }

    static class MyViewHolder extends AbstractQueryDataAdapter.SuperViewHolder {
        TextView inventory_code_tv,inventory_task_tv,inventory_task_code_tv,inventory_wh_name_tv,inventory_way_tv,date_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            inventory_code_tv = itemView.findViewById(R.id.inventory_code_tv);
            inventory_task_tv = itemView.findViewById(R.id.inventory_task_tv);
            inventory_task_code_tv = itemView.findViewById(R.id.inventory_task_code_tv);
            inventory_wh_name_tv = itemView.findViewById(R.id.inventory_wh_name_tv);
            inventory_way_tv = itemView.findViewById(R.id.inventory_way_tv);
            date_tv = itemView.findViewById(R.id.date_tv);

        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.mobile_inventory_order_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final JSONObject object = mData.getJSONObject(position);
        holder.inventory_code_tv.setText(Html.fromHtml("<u>" + object.getString("pcd_code") + "</u>"));
        holder.inventory_code_tv.setTag(object.getString("pcd_id"));
        if (!holder.inventory_code_tv.hasOnClickListeners())holder.inventory_code_tv.setOnClickListener(this);

        holder.inventory_task_tv.setText(object.getString("task_name"));
        holder.inventory_task_tv.setTag(object.getString("pcd_task_id"));
        holder.inventory_task_code_tv.setText(object.getString("pcd_task_code"));

        holder.inventory_wh_name_tv.setText(mContext.getStoreName());

        holder.inventory_way_tv.setText(object.getString("task_mode_name"));

        holder.date_tv.setText(FormatDateTimeUtils.formatTimeWithTimestamp(object.getLongValue("addtime") * 1000));
    }
}
