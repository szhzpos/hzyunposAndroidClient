package com.wyc.cloudapp.adapter.business;

import android.app.Dialog;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList;
import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.dialog.ChangeNumOrPriceDialog;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.adapter.business
 * @ClassName: TimeCardSaleAdapterBase
 * @Description: 次卡销售基类
 * @Author: wyc
 * @CreateDate: 2021-10-19 14:46
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-10-19 14:46
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
abstract public class TimeCardSaleAdapterBase<T extends AbstractDataAdapter.SuperViewHolder> extends AbstractDataAdapterForList<TimeCardSaleInfo,T> {
    protected MainActivity mContext;
    private int mCurrentIndex = -1;
    private View mCurrentItemView;
    public TimeCardSaleAdapterBase(final MainActivity c){
        mContext = c;
        mData = new ArrayList<>();
    }
    protected abstract void bindView(@NonNull T holder, int position);
    @Override
    public final void onBindViewHolder(@NonNull T holder, int position) {

        bindView(holder,position);

        if (!holder.itemView.hasOnClickListeners()){
            holder.itemView.setOnClickListener(this::switchStatus);
        }
        holder.itemView.setTag(position);
        if (mCurrentIndex == position && holder.itemView != mCurrentItemView){
            holder.itemView.callOnClick();
        }
    }

    protected final void switchStatus(View v){
        TextView name_tv;
        if (null != mCurrentItemView && mCurrentItemView != v){
            name_tv = mCurrentItemView.findViewById(R.id.name_tv);
            name_tv.clearAnimation();
            name_tv.setTextColor(mContext.getColor(R.color.good_name_color));

            select(mCurrentItemView,false);
        }
        name_tv = v.findViewById(R.id.name_tv);
        name_tv.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.shake_x));
        name_tv.setTextColor(mContext.getColor(R.color.blue));

        mCurrentItemView = v;
        mCurrentIndex = Utils.getViewTagValue(v,-1);

        select(v,true);
    }

    protected void select(@NonNull View view,boolean s){

    }

    public final void modifyCurrentNum(int n){
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

    public final void addTimeCard(TimeCardSaleInfo saleInfo){
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
    public final void alterNumber(){
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
