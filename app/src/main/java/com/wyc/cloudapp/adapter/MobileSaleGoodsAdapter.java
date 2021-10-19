package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.CustomizationView.InterceptLinearLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.SaleActivity;

public final class MobileSaleGoodsAdapter extends AbstractSaleGoodsAdapter implements View.OnClickListener {
    public MobileSaleGoodsAdapter(final SaleActivity context){
        super(context);
    }

    @Override
    public void onClick(View v) {
        btn(v);
        final int id= v.getId();
        if (id == R.id.mobile_minus_btn){
            mContext.minusOneGoods();
        }else if(id == R.id.mobile_plus_btn) {
            mContext.addOneSaleGoods();
        }else if(id == R.id.mobile_alter_price_btn){
            mContext.alterGoodsPrice();
        }else if (id == R.id.mobile_discount_btn){
            mContext.discount();
        }else if (id == R.id.mobile_alter_num_btn){
            mContext.alterGoodsNumber();
        }else if (id == R.id.mobile_del_btn){
            mContext.deleteGoodsRecord();
        }
    }

    private static class MyViewHolder extends AbstractSaleGoodsAdapter.MyViewHolder {
        Button plus_btn,minus_btn;
        InterceptLinearLayout mobile_float_fun_btn;
        MyViewHolder(View itemView) {
            super(itemView);
            plus_btn = itemView.findViewById(R.id.mobile_plus_btn);
            minus_btn = itemView.findViewById(R.id.mobile_minus_btn);
            mobile_float_fun_btn = itemView.findViewById(R.id.mobile_float_fun_btn);
        }
    }
    @NonNull
    @Override
    public AbstractSaleGoodsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View itemView = View.inflate(mContext, R.layout.mobile_sale_goods_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT));
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
        if (mCurrentItemIndex != i){
            holder.mobile_float_fun_btn.setClickListener(null);
            holder.mobile_float_fun_btn.setVisibility(View.GONE);
        }
        myViewHolder.itemView.setOnClickListener(onClickListener);
    }


    private final View.OnClickListener onClickListener = this::setSelectStatus;


    @Override
    protected void setSelectStatus(View v){
        TextView goods_name;
        InterceptLinearLayout mobile_float_fun_btn;
        if (mCurrentItemView != v){
            if(null != mCurrentItemView){
                goods_name = mCurrentItemView.findViewById(R.id.goods_title);
                goods_name.clearAnimation();
                goods_name.setTextColor(mContext.getColor(R.color.good_name_color));

                mobile_float_fun_btn = mCurrentItemView.findViewById(R.id.mobile_float_fun_btn);
                if (mobile_float_fun_btn != null)mobile_float_fun_btn.setVisibility(View.GONE);
            }
            setCurrentItemIndexAndItemView(v);
        }

        goods_name = v.findViewById(R.id.goods_title);
        goods_name.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.shake_x));
        goods_name.setTextColor(mContext.getColor(R.color.blue));

        mobile_float_fun_btn = v.findViewById(R.id.mobile_float_fun_btn);
        if (mobile_float_fun_btn != null){
            mobile_float_fun_btn.setVisibility(View.VISIBLE);
            mobile_float_fun_btn.setClickListener(this);
        }
    }

    private void btn(View v){
        View p = (View) v.getParent();
        if (p != null)p = (View) p.getParent();
        if (p != null)setSelectStatus(p);
    }


}
