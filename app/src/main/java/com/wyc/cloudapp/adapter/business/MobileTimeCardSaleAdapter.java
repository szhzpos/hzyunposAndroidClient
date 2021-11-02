package com.wyc.cloudapp.adapter.business;

import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.customizationView.InterceptLinearLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.bean.TimeCardSaleInfo;

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
public class MobileTimeCardSaleAdapter extends TimeCardSaleAdapterBase<MobileTimeCardSaleAdapter.MyViewHolder> implements View.OnClickListener {

     public MobileTimeCardSaleAdapter(MainActivity context){
        super(context);
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
        final View view = View.inflate(mContext, R.layout.time_card_sale,null);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return new MyViewHolder(view);
    }

    @Override
    public void bindView(@NonNull MyViewHolder holder, int position) {
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
        }
    }

    @Override
    protected void select(@NonNull View view,boolean add){
        if (view instanceof ViewGroup){
            ViewGroup viewGroup = (ViewGroup)view;
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
}
