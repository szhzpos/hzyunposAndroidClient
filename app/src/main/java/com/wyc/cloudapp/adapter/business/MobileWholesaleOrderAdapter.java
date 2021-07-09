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
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileWholesaleOrderAdapter
 * @Description: 批发订货单适配器
 * @Author: wyc
 * @CreateDate: 2021/4/20 9:39
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/4/20 9:39
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileWholesaleOrderAdapter extends MobileBaseOrderAdapter<MobileWholesaleOrderAdapter.MyViewHolder> {
    public MobileWholesaleOrderAdapter(MainActivity activity) {
        super(activity);
    }

    static class MyViewHolder extends MobileBaseOrderAdapter.MyViewHolder {
        TextView cs_name_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            cs_name_tv = findViewById(R.id.cs_name_tv);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.mobile_wholesale_order_content, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(itemView);
    }

    @Override
    void bindViewHolder(MyViewHolder holder, JSONObject object) {
        holder.cs_name_tv.setText(object.getString("cs_xname"));
        holder.order_code_tv.setText(Html.fromHtml("<u>" + object.getString("order_code") + "</u>"));
        holder.order_code_tv.setTag(object.getString("order_id"));
        holder.date_tv.setText(FormatDateTimeUtils.formatDataWithTimestamp(object.getLongValue("addtime") * 1000));
    }

}
