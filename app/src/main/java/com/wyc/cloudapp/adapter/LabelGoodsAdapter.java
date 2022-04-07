package com.wyc.cloudapp.adapter;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.customizationView.SwipeLayout;
import com.wyc.cloudapp.design.DataItem;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: LabelGoodsAdapter
 * @Description: 标签打印商品适配器
 * @Author: wyc
 * @CreateDate: 2022/4/2 16:34
 * @UpdateUser: 更新者：
 * @UpdateDate: 2022/4/2 16:34
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class LabelGoodsAdapter extends AbstractSelectAdapter<DataItem.LabelGoods,LabelGoodsAdapter.MyViewHolder> implements View.OnClickListener {

    public LabelGoodsAdapter(){
        super();
        mData = new ArrayList<>();
    }

    @Override
    public void onClick(View v) {
        final Object obj = v.getTag();
        if (obj instanceof DataItem.LabelGoods){
            invoke((DataItem.LabelGoods) obj);
        }
    }

    static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder {
        @BindView(R.id.rowId)
        TextView rowId;
        @BindView(R.id.goods_title)
        TextView goods_title;

        @BindView(R.id.barcode)
        TextView barcode;

        @BindView(R.id.price)
        TextView price;

        @BindView(R.id.spec)
        TextView spec;

        MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final SwipeLayout itemView = (SwipeLayout)View.inflate(parent.getContext(), R.layout.label_print_swipe_container, null);
        final RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        itemView.addMenuItem(parent.getContext().getString(R.string.delete_sz), v -> {
            mData.remove((DataItem.LabelGoods)itemView.getTag());
            itemView.setTag(null);
            itemView.closeRightMenu();
            notifyDataSetChanged();
        }, Color.RED);
        itemView.setLayoutParams(lp);
        itemView.setOnClickListener(this);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final DataItem.LabelGoods goods = mData.get(position);
        holder.rowId.setText(String.format(Locale.CHINA,"%d、",position + 1));
        holder.goods_title.setText(goods.getGoodsTitle());
        holder.barcode.setText(goods.getBarcode());
        holder.price.setText(String.format(Locale.CHINA,"￥%.2f",goods.getRetail_price()));
        holder.spec.setText(String.format(Locale.CHINA,"规格:%s",goods.getSpec()));
        holder.itemView.setTag(goods);
    }

    public void addData(final DataItem.LabelGoods object){
        mData.add(object);
        notifyItemChanged(mData.size() - 1);
    }
}