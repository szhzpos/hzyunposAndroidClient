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
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileTransferOutOrderAdapter
 * @Description: 调出单单据适配器
 * @Author: wyc
 * @CreateDate: 2021/4/26 10:18
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/4/26 10:18
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileTransferOutOrderAdapter extends MobileBaseOrderAdapter<MobileTransferOutOrderAdapter.MyViewHolder> {

    public MobileTransferOutOrderAdapter(MainActivity activity) {
        super(activity);
    }

    static class MyViewHolder extends MobileBaseOrderAdapter.MyViewHolder {
        TextView transfer_in_wh_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            transfer_in_wh_tv = findViewById(R.id.transfer_in_wh_tv);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.mobile_transfer_out_order_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(itemView);
    }

    @Override
    void bindViewHolder(MyViewHolder holder, JSONObject object) {
        holder.order_code_tv.setText(Html.fromHtml("<u>" + object.getString("ckd_code") + "</u>"));
        holder.order_code_tv.setTag(object.getString("ckd_id"));
        holder.date_tv.setText(FormatDateTimeUtils.formatDataWithTimestamp(object.getLongValue("addtime") * 1000));
        holder.transfer_in_wh_tv.setText(object.getString("dr_wh_name"));
    }
}
