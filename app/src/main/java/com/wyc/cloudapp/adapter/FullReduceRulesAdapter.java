package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.utils.Utils;

public class FullReduceRulesAdapter extends AbstractDataAdapterForJson<FullReduceRulesAdapter.MyViewHolder> {
    private final Context mContext;
    public FullReduceRulesAdapter(Context context){
        mContext = context;
    }

    static class MyViewHolder extends AbstractDataAdapterForJson.SuperViewHolder{
        TextView rule_des_tv,diff_amt_des_tv,name;
        ImageView status_img;
        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.fullReduce_name);
            rule_des_tv = itemView.findViewById(R.id.rule_des_tv);
            diff_amt_des_tv = itemView.findViewById(R.id.diff_amt_des_tv);
            status_img = itemView.findViewById(R.id.status_img);
        }
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.fullreduce_rules_layout, null);
        itemView.setLayoutParams( new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) mContext.getResources().getDimension(R.dimen.size_25)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (mData != null){
            final JSONObject object = mData.getJSONObject(position);
            holder.name.setText(Utils.getNullStringAsEmpty(object,"title"));

            int fold = object.getIntValue("fold"),status = object.getIntValue("status");

            holder.rule_des_tv.setText(Html.fromHtml((status == 1 && (fold == 1 || fold == 2) ? "【<font color='red'>累加</font>】" : "") + Utils.getNullStringAsEmpty(object,"rule_des")));
            switch (status){
                case 1:
                    if (holder.diff_amt_des_tv.getVisibility() == View.VISIBLE)holder.diff_amt_des_tv.setVisibility(View.INVISIBLE);
                    if (holder.status_img.getVisibility() == View.GONE)holder.status_img.setVisibility(View.VISIBLE);
                    holder.status_img.setBackground(mContext.getResources().getDrawable(R.drawable.selected,null));
                    break;
                case 2:
                    if (holder.diff_amt_des_tv.getVisibility() == View.VISIBLE)holder.diff_amt_des_tv.setVisibility(View.INVISIBLE);
                    if (holder.status_img.getVisibility() == View.GONE)holder.status_img.setVisibility(View.VISIBLE);
                        holder.status_img.setBackground(mContext.getResources().getDrawable(R.drawable.unsel,null));
                    break;
                case 3:
                    if (holder.diff_amt_des_tv.getVisibility() == View.INVISIBLE)holder.diff_amt_des_tv.setVisibility(View.VISIBLE);
                    holder.diff_amt_des_tv.setText(Utils.getNullStringAsEmpty(object,"diff_amt_des"));
                    if (holder.status_img.getVisibility() == View.VISIBLE)holder.status_img.setVisibility(View.GONE);
                    break;
            }
        }
    }
}
