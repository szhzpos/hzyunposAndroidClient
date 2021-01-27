package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.utils.Utils;

public class PayMethodStatisticsViewAdapter extends AbstractTableDataAdapter<PayMethodStatisticsViewAdapter.ViewHolder> {
    static class ViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView title,xx_money,xx_num;
        ViewHolder(View itemView) {
            super(itemView);
            title =  itemView.findViewById(R.id.title);
            xx_money = itemView.findViewById(R.id.xx_money);
            xx_num = itemView.findViewById(R.id.xx_num);
        }
    }

    public PayMethodStatisticsViewAdapter(final MainActivity context) {
        mContext = context;
     }

    @Override
    public @NonNull
    ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.pay_method_view_content_layout, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,Utils.dpToPx(mContext,45));
        itemView.setLayoutParams(lp);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder( @NonNull final  ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (mDatas != null) {
            final JSONObject object = mDatas.getJSONObject(position);
            holder.title.setText(object.getString("title"));
            holder.xx_money.setText(object.getString("xx_money"));
            holder.xx_num.setText(object.getString("xx_num"));
        }
    }


    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public void setDataForArray(final JSONArray array){
        mDatas = array;
        notifyDataSetChanged();
    }
}