package com.wyc.cloudapp.adapter.business;

import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.activity.mobile.business.MobilePracticalInventoryAddOrderActivity;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileInventoryTaskAdapter
 * @Description: 盘点任务适配器
 * @Author: wyc
 * @CreateDate: 2021/5/6 17:02
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/6 17:02
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileInventoryTaskAdapter extends AbstractBusinessOrderDataAdapter<MobileInventoryTaskAdapter.MyViewHolder> {
    public MobileInventoryTaskAdapter(MainActivity activity) {
        super(activity);
    }

    protected static class MyViewHolder extends AbstractQueryDataAdapter.SuperViewHolder {
        TextView inventory_task_tv,inventory_task_code_tv,inventory_wh_name_tv,inventory_way_tv,date_tv,status_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            inventory_task_tv = findViewById(R.id.inventory_task_tv);
            inventory_task_code_tv = findViewById(R.id.inventory_task_code_tv);
            inventory_wh_name_tv = findViewById(R.id.inventory_wh_name_tv);
            inventory_way_tv = findViewById(R.id.inventory_way_tv);
            date_tv = findViewById(R.id.date_tv);
            status_tv = findViewById(R.id.status_tv);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.mobile_inventory_task_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final JSONObject object = mData.getJSONObject(position);

        holder.inventory_task_tv.setText(object.getString("task_name"));

        if (!holder.inventory_task_code_tv.hasOnClickListeners())holder.inventory_task_code_tv.setOnClickListener(this);
        holder.inventory_task_code_tv.setText(Html.fromHtml("<u>" + object.getString("pcd_task_code") + "</u>"));
        holder.inventory_task_code_tv.setTag(object.toString());

        holder.status_tv.setText(object.getString("status_name"));
        holder.inventory_wh_name_tv.setText(mContext.getStoreName());
        holder.inventory_way_tv.setText(MobilePracticalInventoryAddOrderActivity.getInventoryModeName(object.getString("task_mode")));
        holder.date_tv.setText(object.getString("addtime"));
    }
}
