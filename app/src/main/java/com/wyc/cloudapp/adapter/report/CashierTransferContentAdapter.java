package com.wyc.cloudapp.adapter.report;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.report
 * @ClassName: CashierTransferContentAdapter
 * @Description: 收银员对账数据适配器
 * @Author: wyc
 * @CreateDate: 2021/2/20 10:52
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/20 10:52
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class CashierTransferContentAdapter extends AbstractDataAdapter<CashierTransferContentAdapter.MyViewHolder> {
    final MainActivity mContext;
    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView sum_money,sj_money,order_money,order_num,refund_money,refund_num,recharge_money,recharge_num,oncecard_money,oncecard_num;
        MyViewHolder(View itemView) {
            super(itemView);
            sum_money = itemView.findViewById(R.id.sum_money);
            sj_money = itemView.findViewById(R.id.sj_money);

            order_money = itemView.findViewById(R.id.order_money);
            order_num = itemView.findViewById(R.id.order_num);
            refund_money = itemView.findViewById(R.id.refund_money);
            refund_num = itemView.findViewById(R.id.refund_num);
            recharge_money = itemView.findViewById(R.id.recharge_money);
            recharge_num = itemView.findViewById(R.id.recharge_num);
            oncecard_money = itemView.findViewById(R.id.oncecard_money);
            oncecard_num = itemView.findViewById(R.id.oncecard_num);
        }
    }

    public CashierTransferContentAdapter(final MainActivity activity) {
        mContext = activity;
    }

    @Override
    public @NonNull
    MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext, R.layout.mobile_cashier_transfer_content_layout, null);
        final RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(mContext,68));

        itemView.setLayoutParams(lp);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder( @NonNull final  MyViewHolder holder, int position) {
        if (mDatas != null) {
            final JSONObject object = mDatas.getJSONObject(position);

            holder.sum_money.setText(String.format(Locale.CHINA, "%.2f%s", object.getDoubleValue("sum_money"), "元"));
            holder.sj_money.setText(String.format(Locale.CHINA, "%.2f%s", object.getDoubleValue("sj_money"), "元"));

            holder.order_num.setText(String.format(Locale.CHINA, "%d%s", object.getIntValue("order_num"), "笔"));
            holder.order_money.setText(String.format(Locale.CHINA, "%.2f%s", object.getDoubleValue("order_money"), "元"));

            holder.refund_num.setText(String.format(Locale.CHINA, "%d%s", object.getIntValue("refund_num"), "笔"));
            holder.refund_money.setText(String.format(Locale.CHINA, "%.2f%s", object.getDoubleValue("refund_money"), "元"));

            holder.recharge_num.setText(String.format(Locale.CHINA, "%d%s", object.getIntValue("recharge_num"), "笔"));
            holder.recharge_money.setText(String.format(Locale.CHINA, "%.2f%s", object.getDoubleValue("recharge_money"), "元"));

            holder.oncecard_num.setText(String.format(Locale.CHINA, "%d%s", object.getIntValue("oncecard_num"), "笔"));
            holder.oncecard_money.setText(String.format(Locale.CHINA, "%.2f%s", object.getDoubleValue("oncecard_money"), "元"));
        }
    }


    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }
}
