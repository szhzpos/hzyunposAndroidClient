package com.wyc.cloudapp.adapter.business;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.customizationView.SwipeLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileBaseOrderDetailsAdapter
 * @Description: 业务单据明细数据适配器父类
 * @Author: wyc
 * @CreateDate: 2021/3/8 10:12
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/3/8 10:12
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileBaseOrderDetailsAdapter extends AbstractBusinessOrderDetailsDataAdapter<MobileBaseOrderDetailsAdapter.MyViewHolder> {
    public MobileBaseOrderDetailsAdapter(MainActivity activity) {
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
        final SwipeLayout itemView = (SwipeLayout) View.inflate(mContext, R.layout.swipe_container_layout, null);
        itemView.addMenuItem(mContext.getString(R.string.delete_sz), v -> {
            setCurrentItemIndex(Utils.getViewTagValue(itemView,-1));
            deleteDetails();
        }, Color.RED);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onViewRecycled(@NonNull MyViewHolder holder) {
        if (holder.itemView instanceof SwipeLayout){
            ((SwipeLayout)holder.itemView).closeRightMenu();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final JSONObject object = mData.getJSONObject(position);

        holder.barcode_tv.setText(object.getString("barcode"));
        holder.name_tv.setText(object.getString("goods_title"));

        double num = Utils.getNotKeyAsNumberDefault(object, getNumKey(), 0.0), price = Utils.getNotKeyAsNumberDefault(object, getPriceKey(), 0.0);
        holder.num_tv.setText(String.format(Locale.CHINA, "%.2f", num));
        holder.price_tv.setText(String.format(Locale.CHINA, "%.2f/%s", price, object.getString("unit_name")));
        holder.amt_tv.setText(String.format(Locale.CHINA, "￥%.2f", price * num));
    }

    @Override
    public JSONObject updateGoodsDetail(JSONObject object) {
        if (null != object){
            object.put("price",Utils.getNotKeyAsNumberDefault(object,"new_price",Utils.getNotKeyAsNumberDefault(object,"price",0.0)));
            object.put("xnum",Utils.getNotKeyAsNumberDefault(object,"new_num",1.00));
            Logger.d_json(object.toString());
        }
        return object;
    }
}
