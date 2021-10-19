package com.wyc.cloudapp.adapter.business;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.CustomizationView.SwipeLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForJson;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileInventoryOrderDetailsAdapter
 * @Description: 盘点实盘录入单商品明细适配器
 * @Author: wyc
 * @CreateDate: 2021/4/25 9:51
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/4/25 9:51
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileInventoryOrderDetailsAdapter extends  AbstractBusinessOrderDetailsDataAdapter<MobileInventoryOrderDetailsAdapter.MyViewHolder> {
    public MobileInventoryOrderDetailsAdapter(MainActivity activity) {
        super(activity);
    }

    static class MyViewHolder extends AbstractDataAdapterForJson.SuperViewHolder {
        TextView barcode_tv,name_tv,num_tv,unit_name;
        MyViewHolder(View itemView) {
            super(itemView);
            barcode_tv = findViewById(R.id.barcode_tv);
            name_tv = findViewById(R.id.name_tv);
            unit_name = findViewById(R.id.unit_name);
            num_tv = findViewById(R.id.num_tv);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final SwipeLayout itemView = (SwipeLayout) View.inflate(mContext, R.layout.inventory_swipe_layout, null);
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

        double num = Utils.getNotKeyAsNumberDefault(object, getNumKey(), 0.0);
        holder.num_tv.setText(String.format(Locale.CHINA, "%.2f", num));
        holder.unit_name.setText(object.getString("unit_name"));
    }

    @Override
    public JSONObject updateGoodsDetail(JSONObject object) {
        if (null != object){
            object.put("app_xnum",Utils.getNotKeyAsNumberDefault(object,"new_num",1.00));
        }
        return object;
    }

    @Override
    public String getNumKey() {
        return "app_xnum";
    }

    @Override
    protected String[] getCumulativeKey() {
        return new String[]{"app_xnum"};
    }
}
