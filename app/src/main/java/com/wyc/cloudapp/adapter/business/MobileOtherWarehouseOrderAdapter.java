package com.wyc.cloudapp.adapter.business;

import android.text.Html;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileOtherWarehouseOrderAdapter
 * @Description: 其他出入库单适配器
 * @Author: wyc
 * @CreateDate: 2021/4/16 13:56
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/4/16 13:56
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileOtherWarehouseOrderAdapter extends MobileBaseOrderAdapter {
    public MobileOtherWarehouseOrderAdapter(MainActivity activity) {
        super(activity);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.mobile_business_order_with_otherwarehouse_content, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(itemView);
    }

    @Override
    void bindViewHolder(MyViewHolder holder, JSONObject object) {
        holder.order_code_tv.setText(Html.fromHtml("<u>" + object.getString("bgd_code") + "</u>"));
        holder.order_code_tv.setTag(object.getString("bgd_id"));
    }
}
