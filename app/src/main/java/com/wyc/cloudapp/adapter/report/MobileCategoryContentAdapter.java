package com.wyc.cloudapp.adapter.report;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForJson;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.utils.Utils;
import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.report
 * @ClassName: MobileCategoryContentAdapter
 * @Description: 类别统计报表类别数据适配器
 * @Author: wyc
 * @CreateDate: 2021/2/2 10:22
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/2 10:22
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class MobileCategoryContentAdapter extends AbstractDataAdapterForJson<MobileCategoryContentAdapter.MyViewHolder> {
    final MainActivity mContext;
    public MobileCategoryContentAdapter(final MainActivity activity){
        mContext = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext, R.layout.category_content_adapter_layout, null);
        final RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(mContext,58));
        itemView.setLayoutParams(lp);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder( @NonNull final  MyViewHolder holder, int position) {
        if (mData != null) {
            final JSONObject object = mData.getJSONObject(position);
            holder.sale_num_tv.setText(String.format(Locale.CHINA, "%.2f", object.getDoubleValue("sum_xnum")));
            holder.sale_amt_tv.setText(String.format(Locale.CHINA, "%.2f", object.getDoubleValue("sales_money")));
            holder.profit_tv.setText(String.format(Locale.CHINA, "%.2f", object.getDoubleValue("real_profit")));
            holder.profit_rate_tv.setText(object.getString("real_profit_rate"));
        }
    }

    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView sale_num_tv,sale_amt_tv,profit_tv,profit_rate_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            sale_num_tv = itemView.findViewById(R.id.sale_num_tv);
            sale_amt_tv =  itemView.findViewById(R.id.sale_amt_tv);
            profit_tv = itemView.findViewById(R.id.profit_tv);
            profit_rate_tv = itemView.findViewById(R.id.profit_rate_tv);
        }
    }

}
