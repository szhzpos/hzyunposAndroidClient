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
import com.wyc.cloudapp.utils.FormatDateTimeUtils;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileTransferInOrderAdapter
 * @Description: 调入单单据适配器
 * @Author: wyc
 * @CreateDate: 2021/4/26 13:58
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/4/26 13:58
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileTransferInOrderAdapter extends MobileBaseOrderAdapter<MobileTransferInOrderAdapter.MyViewHolder> {

    public MobileTransferInOrderAdapter(MainActivity activity) {
        super(activity);
    }

    static class MyViewHolder extends MobileBaseOrderAdapter.MyViewHolder {
        TextView transfer_out_wh_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            transfer_out_wh_tv = findViewById(R.id.transfer_out_wh_tv);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.activity_mobile_add_transfer_in_order, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(itemView);
    }

    @Override
    void bindViewHolder(MyViewHolder holder, JSONObject object) {
        holder.order_code_tv.setText(Html.fromHtml("<u>" + object.getString("ckd_code") + "</u>"));
        holder.order_code_tv.setTag(object.getString("ckd_id"));
        holder.date_tv.setText(FormatDateTimeUtils.formatTimeWithTimestamp(object.getLongValue("addtime") * 1000));
        holder.transfer_out_wh_tv.setText(object.getString("wh_name"));
        holder.audit_tv.setText("1".equals(object.getString("qr_status")) ? mContext.getString(R.string.unconfirm_receipt) : mContext.getString(R.string.confirmed_receipt));
    }
}