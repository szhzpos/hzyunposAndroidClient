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
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: BaseOrderDataAdapter
 * @Description: 业务单据数据适配器父类
 * @Author: wyc
 * @CreateDate: 2021/3/8 9:52
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/3/8 9:52
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public abstract class MobileBaseOrderAdapter extends AbstractBusinessOrderDataAdapter<MobileBaseOrderAdapter.MyViewHolder>  {
    public MobileBaseOrderAdapter(MainActivity activity) {
        super(activity);
    }

    static class MyViewHolder extends AbstractQueryDataAdapter.SuperViewHolder {
        TextView order_code_tv,gs_name_tv,wh_name_tv,audit_tv,amt_tv,date_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            order_code_tv = itemView.findViewById(R.id.order_code_tv);
            gs_name_tv = itemView.findViewById(R.id.gs_name_tv);
            wh_name_tv = itemView.findViewById(R.id.wh_name_tv);
            audit_tv = itemView.findViewById(R.id.audit_tv);
            amt_tv = itemView.findViewById(R.id.amt_tv);
            date_tv = itemView.findViewById(R.id.date_tv);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.mobile_business_order_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(itemView);
    }

    @Override
    public final void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final JSONObject object = mDatas.getJSONObject(position);

        if (!holder.order_code_tv.hasOnClickListeners())holder.order_code_tv.setOnClickListener(this);

        if (holder.gs_name_tv != null)holder.gs_name_tv.setText(object.getString("gs_name"));
        holder.wh_name_tv.setText(mContext.getStoreName());
        holder.audit_tv.setText("1".equals(object.getString("sh_status")) ? mContext.getString(R.string.unaudited_sz) : mContext.getString(R.string.audited_sz));
        holder.amt_tv.setText(String.format(Locale.CHINA,"%.2f", Utils.getNotKeyAsNumberDefault(object,"total",0.0)));
        holder.date_tv.setText(object.getString("add_datetime"));

        bindViewHolder(holder,object);
    }

    abstract void bindViewHolder(MyViewHolder holder,final JSONObject object);
}
