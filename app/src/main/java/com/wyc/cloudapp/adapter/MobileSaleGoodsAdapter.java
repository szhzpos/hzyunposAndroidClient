package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.SaleActivity;

public final class MobileSaleGoodsAdapter extends AbstractSaleGoodsAdapter implements View.OnClickListener {
    public MobileSaleGoodsAdapter(final SaleActivity context){
        super(context);
    }

    @Override
    public void onClick(View v) {
        btn(v);
        if (v.getId() == R.id.mobile_minus_btn){
            mContext.minusOneGoods();
        }else {
            mContext.addOneSaleGoods();
        }
    }

    private static class MyViewHolder extends AbstractSaleGoodsAdapter.MyViewHolder {
        Button plus_btn,minus_btn;
        MyViewHolder(View itemView) {
            super(itemView);
            plus_btn = itemView.findViewById(R.id.mobile_plus_btn);
            minus_btn = itemView.findViewById(R.id.mobile_minus_btn);
        }
    }
    @NonNull
    @Override
    public AbstractSaleGoodsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View itemView = View.inflate(mContext, R.layout.mobile_sale_goods_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.mobile_sale_goods_height)));
        return new MobileSaleGoodsAdapter.MyViewHolder(itemView);
    }
    @Override
    public void onBindViewHolder(@NonNull AbstractSaleGoodsAdapter.MyViewHolder myViewHolder, int i) {
        super.onBindViewHolder(myViewHolder,i);
        final MobileSaleGoodsAdapter.MyViewHolder holder = (MyViewHolder)myViewHolder;
        holder.minus_btn.setOnClickListener(this);
        holder.plus_btn.setOnClickListener(this);

        holder.minus_btn.setTag(i);
        holder.plus_btn.setTag(i);
    }

    private void btn(View v){
        View p = (View) v.getParent();
        if (p != null)p = (View) p.getParent();
        if (p != null)setSelectStatus(p);
    }
}
