package com.wyc.cloudapp.adapter.business;

import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.CustomizationView.InterceptLinearLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList;
import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.dialog.ChangeNumOrPriceDialog;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: MobileTimeCardSaleAdapter
 * @Description: 次卡销售数据适配器
 * @Author: wyc
 * @CreateDate: 2021-06-30 15:56
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-30 15:56
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileTimeCardSaleAdapter extends AbstractDataAdapterForList<TimeCardSaleInfo, MobileTimeCardSaleAdapter.MyViewHolder> implements View.OnClickListener {

    private final MainActivity mContext;
    private View mCurrentItemView;
    private int mCurrentIndex;

    public MobileTimeCardSaleAdapter(MainActivity context){
        mContext = context;
        mData = new ArrayList<>();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id._minus_btn:
                switchStatus((View) v.getTag());
                modifyCurrentNum(-1);
                break;
            case R.id._plus_btn:
                switchStatus((View) v.getTag());
                modifyCurrentNum(1);
                break;
            case R.id.alter_num_btn:
                alterNumber();
                break;
            case R.id.del_btn:
                modifyCurrentNum(-Integer.MAX_VALUE);
                break;
            default:
                switchStatus(v);
        }

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
        @BindView(R.id._minus_btn)
        Button _minus_btn;
        @BindView(R.id._plus_btn)
        Button _plus_btn;
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
        view.setOnClickListener(this);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final TimeCardSaleInfo saleInfo = getItem(position);
        if (null != saleInfo){
            holder._minus_btn.setTag(holder.itemView);
            if(!holder._minus_btn.hasOnClickListeners()){
                holder._minus_btn.setOnClickListener(this);
            }
            holder._plus_btn.setTag(holder.itemView);
            if(!holder._plus_btn.hasOnClickListeners()){
                holder._plus_btn.setOnClickListener(this);
            }

            holder.once_card_id.setText(String.valueOf(saleInfo.getOnce_card_id()));
            holder.name_tv.setText(saleInfo.getName());
            holder.sale_price.setText(String.format(Locale.CHINA,"%.2f",saleInfo.getPrice()));
            holder.sale_num.setText(String.valueOf(saleInfo.getNum()));
            holder.sale_amt.setText(String.format(Locale.CHINA,"%.2f",saleInfo.getAmt()));

            holder.itemView.setTag(position);
            if (mCurrentIndex == position){
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

                manipulateFloatBtn(false);
            }
        }

        name_tv = v.findViewById(R.id.name_tv);
        name_tv.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.shake_x));
        name_tv.setTextColor(mContext.getColor(R.color.blue));

        mCurrentItemView = v;
        mCurrentIndex = Utils.getViewTagValue(v,-1);
        manipulateFloatBtn(true);
    }
    private void manipulateFloatBtn(boolean add){
        if (mCurrentItemView instanceof ViewGroup){
            ViewGroup viewGroup = (ViewGroup)mCurrentItemView;
            View child = viewGroup.getChildAt(viewGroup.getChildCount() - 1);
            if (child.getId() != R.id._float_fun_btn){
                if (add){
                    InterceptLinearLayout v = (InterceptLinearLayout) View.inflate(mContext,R.layout.float_fun_btn,null);
                    v.setClickListener(this);
                    viewGroup.addView(v);
                }
            }else if (!add)viewGroup.removeView(child);
        }
    }

    public void addTimeCard(TimeCardSaleInfo saleInfo){
        boolean isExist = false;
        int index = 0;
        for (TimeCardSaleInfo info : mData){
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

    private void modifyCurrentNum(int n){
        final TimeCardSaleInfo saleInfo = getItem(mCurrentIndex);
        if (saleInfo != null){
            int num = saleInfo.getNum() + n;
            if (Utils.notGreaterDouble(num,0.0)){
                mData.remove(mCurrentIndex);
            }else {
                saleInfo.setNum(num);
            }
            notifyDataSetChanged();
        }
    }
    public void alterNumber(){
        final TimeCardSaleInfo saleInfo = getItem(mCurrentIndex);
        if (null != saleInfo){
            ChangeNumOrPriceDialog dialog = new ChangeNumOrPriceDialog(mContext,"新数量",String.format(Locale.CHINA,"%d",saleInfo.getNum()));
            dialog.setYesOnclickListener(myDialog -> {
                int content = (int) myDialog.getContent();
                if (Utils.notGreaterDouble(content,0.0)){
                    mData.remove(mCurrentIndex);
                }else {
                    saleInfo.setNum(content);
                }
                notifyDataSetChanged();
                myDialog.dismiss();
            }).setNoOnclickListener(Dialog::dismiss).show();
        }
    }
}
