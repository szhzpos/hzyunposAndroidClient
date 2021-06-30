package com.wyc.cloudapp.adapter.business;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList;
import com.wyc.cloudapp.bean.OnceCardInfo;
import com.wyc.cloudapp.bean.OnceCardSaleInfo;
import com.wyc.cloudapp.logger.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileOnceCardSaleAdapter
 * @Description: 次卡销售数据适配器
 * @Author: wyc
 * @CreateDate: 2021-06-30 15:56
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-30 15:56
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileOnceCardSaleAdapter extends AbstractDataAdapterForList<OnceCardSaleInfo, MobileOnceCardSaleAdapter.MyViewHolder> {

    private final Context mContext;

    public MobileOnceCardSaleAdapter(Context context){
        mContext = context;
        mData = new ArrayList<>();
    }

    static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder{
        @BindView(R.id.once_card_id)
        TextView once_card_id;
        @BindView(R.id.name_tv)
        TextView name_tv;
        @BindView(R.id.sale_price)
        TextView sale_price;
        @BindView(R.id.sale_num)
        TextView sale_num;
        @BindView(R.id.sale_amt)
        TextView sale_amt;
        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View view = View.inflate(mContext, R.layout.once_card_sale,null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final OnceCardSaleInfo saleInfo = getItem(position);
        holder.once_card_id.setText(String.valueOf(saleInfo.getOnce_card_id()));
        holder.name_tv.setText(saleInfo.getName());
        holder.sale_price.setText(String.format(Locale.CHINA,"%.2f",saleInfo.getPrice()));
        holder.sale_num.setText(String.valueOf(saleInfo.getNum()));
        holder.sale_amt.setText(String.format(Locale.CHINA,"%.2f",saleInfo.getAmt()));
    }

    public void addOnceCard(OnceCardSaleInfo saleInfo){
        boolean isExist = false;
        for (OnceCardSaleInfo info : mData){
            if (info.equals(saleInfo)){
                info.setNum(saleInfo.getNum() + info.getNum());
                isExist = true;
                break;
            }
        }
        if (!isExist){
            mData.add(saleInfo);
        }
        Logger.d(Arrays.toString(mData.toArray()));
        notifyDataSetChanged();
    }
}
