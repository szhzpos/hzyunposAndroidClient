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
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.adapter.report.AbstractDataAdapter;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobilePurchaseOrderDetailsAdapter
 * @Description: 采购订货单商品明细适配器
 * @Author: wyc
 * @CreateDate: 2021/2/23 15:32
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/23 15:32
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class MobilePurchaseOrderDetailsAdapter extends AbstractBusinessOrderDetailsDataAdapter<MobilePurchaseOrderDetailsAdapter.MyViewHolder> {

    public MobilePurchaseOrderDetailsAdapter(final MainActivity activity){
        super(activity);
    }

    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView barcode_tv,name_tv,num_tv,price_tv,amt_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            barcode_tv = itemView.findViewById(R.id.barcode_tv);
            name_tv = itemView.findViewById(R.id.name_tv);
            num_tv = itemView.findViewById(R.id.num_tv);
            price_tv = itemView.findViewById(R.id.price_tv);
            amt_tv = itemView.findViewById(R.id.amt_tv);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext, R.layout.mobile_business_order_details_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final JSONObject object = mDatas.getJSONObject(position);

        holder.barcode_tv.setText(object.getString("barcode"));
        holder.name_tv.setText(object.getString("goods_title"));

        double num = Utils.getNotKeyAsNumberDefault(object, "xnum", 0.0), price = Utils.getNotKeyAsNumberDefault(object, "last_jh_price", 0.0);
        holder.num_tv.setText(String.format(Locale.CHINA, "%.2f", num));
        holder.price_tv.setText(String.format(Locale.CHINA, "%.2f/%s", price, object.getString("unit_name")));
        holder.amt_tv.setText(String.format(Locale.CHINA, "￥%.2f", price * num));
    }


    @Override
    public JSONObject updateGoodsDetails(JSONObject object) {
        if (null != object){
            object.put("last_jh_price",Utils.getNotKeyAsNumberDefault(object,"new_price",Utils.getNotKeyAsNumberDefault(object,"buying_price",0.0)));
            object.put("xnum",Utils.getNotKeyAsNumberDefault(object,"new_num",1.00));
            Logger.d_json(object.toString());
        }
        return object;
    }

}
