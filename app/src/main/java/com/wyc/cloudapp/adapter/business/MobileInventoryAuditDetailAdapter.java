package com.wyc.cloudapp.adapter.business;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileInventoryAuditDetailAdapter
 * @Description: 盘点审核单据明细适配器
 * @Author: wyc
 * @CreateDate: 2021/5/8 14:24
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/8 14:24
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileInventoryAuditDetailAdapter extends AbstractBusinessOrderDataAdapter<MobileInventoryAuditDetailAdapter.MyViewHolder>  {
    public MobileInventoryAuditDetailAdapter(MainActivity activity) {
        super(activity);
    }

    static class MyViewHolder extends AbstractQueryDataAdapter.SuperViewHolder {
        TextView barcode_tv,name_tv,lock_stock_num_tv,app_num_tv,manual_num_tv,practical_sum_tv,profit_loss_sum_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            barcode_tv = itemView.findViewById(R.id.barcode_tv);
            name_tv = itemView.findViewById(R.id.name_tv);

            lock_stock_num_tv = itemView.findViewById(R.id.lock_stock_num_tv);
            app_num_tv = itemView.findViewById(R.id.app_num_tv);
            manual_num_tv = itemView.findViewById(R.id.manual_num_tv);
            practical_sum_tv = itemView.findViewById(R.id.practical_sum_tv);
            profit_loss_sum_tv = itemView.findViewById(R.id.profit_loss_sum_tv);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.mobile_inventory_audit_detail_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final JSONObject object = mData.getJSONObject(position);
        holder.barcode_tv.setText(object.getString("barcode"));
        holder.name_tv.setText(object.getString("goods_title"));

        holder.lock_stock_num_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("lock_stock_num")));
        holder.app_num_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("app_xnum")));
        holder.manual_num_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("xnum")));
        holder.practical_sum_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("sum_xnum")));
        holder.profit_loss_sum_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("yk_xnum")));
    }
}
