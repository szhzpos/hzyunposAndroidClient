package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.data.room.entity.GiftCardSaleDetail;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.adapter
 * @ClassName: MobileGiftCardSaleAdapter
 * @Description: 购物卡销售适配器
 * @Author: wyc
 * @CreateDate: 2021-07-22 10:24
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-22 10:24
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileGiftCardSaleAdapter extends AbstractSelectAdapter<GiftCardSaleDetail,MobileGiftCardSaleAdapter.MyViewHolder>  {

    private final MainActivity mContext;
    private View mCurrentItemView;
    private int mCurrentIndex;

    public MobileGiftCardSaleAdapter(MainActivity activity){
        mContext = activity;
        mData = new ArrayList<>();
    }

    static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder{
        @BindView(R.id.id_tv)
        TextView id_tv;
        @BindView(R.id.name_tv)
        TextView name_tv;
        @BindView(R.id.card_code_tv)
        TextView card_code_tv;
        @BindView(R.id.face_value_tv)
        TextView face_value_tv;
        @BindView(R.id.price_tv)
        TextView price_tv;
        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this,itemView);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view  = View.inflate(mContext, R.layout.mobile_gift_card_sale_adapter,null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) mContext.getResources().getDimension(R.dimen.once_card_item_height)));
        view.setOnClickListener(this::switchStatus);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final GiftCardSaleDetail detail = getItem(position);
        if (null != detail){
            holder.id_tv.setText(String.format(Locale.CHINA,"%d、",position + 1));
            holder.name_tv.setText(detail.getName());
            holder.card_code_tv.setText(String.format(Locale.CHINA,"%s%s",mContext.getString(R.string.gift_card_code),detail.getGift_card_code()));
            holder.face_value_tv.setText(String.format(Locale.CHINA,"%s%.2f",mContext.getString(R.string.gift_card_face_value),detail.getFace_value()));
            holder.price_tv.setText(String.format(Locale.CHINA,"%s%.2f",mContext.getString(R.string.gift_card_sale_price),detail.getPrice()));
            holder.itemView.setTag(position);
            if (position == mCurrentIndex){
                holder.itemView.callOnClick();
            }
        }
    }

    private void switchStatus(View v){
        TextView name_tv;
        if (mCurrentItemView != v){
            if(null != mCurrentItemView){
                name_tv = mCurrentItemView.findViewById(R.id.name_tv);
                name_tv.clearAnimation();
                name_tv.setTextColor(mContext.getColor(R.color.good_name_color));
            }
        }

        name_tv = v.findViewById(R.id.name_tv);
        name_tv.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.shake_x));
        name_tv.setTextColor(mContext.getColor(R.color.blue));

        mCurrentItemView = v;
        mCurrentIndex = Utils.getViewTagValue(v,-1);
    }

    public void addGiftCard(GiftCardSaleDetail saleInfo){
        boolean isExist = false;
        int index = 0;
        for (GiftCardSaleDetail info : mData){
            if (info.equalsWithTimeCardInfo(saleInfo)){
                info.setNum(saleInfo.getNum() + info.getNum());
                isExist = true;
                break;
            }
            index++;
        }
        if (!isExist){
            index = mData.size();
            saleInfo.setRowId(index);
            mData.add(saleInfo);
        }
        mCurrentIndex = index;
        notifyDataSetChanged();
    }

}
