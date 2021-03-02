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
import com.wyc.cloudapp.adapter.AbstractQueryDataAdapter;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

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
public class MobilePurchaseOrderAdapter extends AbstractBusinessOrderDataAdapter<MobilePurchaseOrderAdapter.MyViewHolder> {

    public MobilePurchaseOrderAdapter(final MainActivity activity){
        super(activity);
    }

    static class MyViewHolder extends AbstractQueryDataAdapter.SuperViewHolder {
        TextView cgd_code_tv,gs_name_tv,wh_name_tv,audit_tv,amt_tv,date_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            cgd_code_tv = itemView.findViewById(R.id.cgd_code_tv);
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
        View itemView = View.inflate(mContext, R.layout.mobile_purchase_order_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final JSONObject object = mDatas.getJSONObject(position);

        holder.cgd_code_tv.setText(Html.fromHtml("<u>" + object.getString("cgd_code") + "</u>"));
        holder.cgd_code_tv.setTag(object.getString("cgd_id"));
        holder.cgd_code_tv.setOnClickListener(this);

        holder.gs_name_tv.setText(object.getString("gs_name"));
        holder.wh_name_tv.setText(mContext.getStoreName());
        holder.audit_tv.setText("1".equals(object.getString("sh_status")) ? mContext.getString(R.string.unaudited_sz) : mContext.getString(R.string.audited_sz));
        holder.amt_tv.setText(String.format(Locale.CHINA,"%.2f", Utils.getNotKeyAsNumberDefault(object,"total",0.0)));
        holder.date_tv.setText(object.getString("add_datetime"));
    }
}
