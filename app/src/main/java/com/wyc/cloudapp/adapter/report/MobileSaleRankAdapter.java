package com.wyc.cloudapp.adapter.report;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.utils.Utils;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: MobileSaleRankAdapter
 * @Description: java类作用描述
 * @Author: wyc
 * @CreateDate: 2021/1/27 17:07
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/1/27 17:07
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class MobileSaleRankAdapter extends AbstractDataAdapter<MobileSaleRankAdapter.MyViewHolder> {
    private final MainActivity mContext;
    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView goods_name_tv,sale_num_tv,sale_amt_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            goods_name_tv =  itemView.findViewById(R.id.goods_name_tv);
            sale_num_tv = itemView.findViewById(R.id.sale_num_tv);
            sale_amt_tv = itemView.findViewById(R.id.sale_amt_tv);
        }
    }

    public MobileSaleRankAdapter(MainActivity context) {
        mContext = context;
    }

    @Override
    public @NonNull MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.sale_rank_content_layout, null);
        final RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,Utils.dpToPx(mContext,58));
        lp.setMarginStart(24);
        lp.setMarginEnd(24);
        itemView.setLayoutParams(lp);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder( @NonNull final  MyViewHolder holder, int position) {
        if (mDatas != null) {
            final JSONObject object = mDatas.getJSONObject(position);
            holder.goods_name_tv.setText(object.getString("goods_title"));
            holder.sale_num_tv.setText(object.getString("sales_num"));
            holder.sale_amt_tv.setText(object.getString("sales_money"));
        }
    }


    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

}
