package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.CashierActivity;

public class MobileSaleGoodsAdapter extends AbstractSaleGoodsAdapter {
    public MobileSaleGoodsAdapter(final CashierActivity context){
        super(context);
    }
    @NonNull
    @Override
    public AbstractSaleGoodsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View itemView = View.inflate(mContext, R.layout.mobile_sale_goods_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.sale_goods_height)));
        return new AbstractSaleGoodsAdapter.MyViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

}
