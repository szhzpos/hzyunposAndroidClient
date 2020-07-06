package com.wyc.cloudapp.adapter;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.vip.VipDepositDetailsDialog;
import com.wyc.cloudapp.logger.Logger;

import java.util.Locale;

public class VipDepositOrderViewAdapter extends AbstractQueryDataAdapter<VipDepositOrderViewAdapter.MyViewHolder> {

    public VipDepositOrderViewAdapter(MainActivity context){
        mContext = context;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView row_id,card_code,mobile,vip_name,order_code,order_amt,give_amt,order_status,s_e_status,cas_name,oper_time;
        View mCurrentLayoutItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;

            row_id = itemView.findViewById(R.id.row_id);
            order_code = itemView.findViewById(R.id.order_code);
            card_code = itemView.findViewById(R.id.card_code);
            mobile = itemView.findViewById(R.id.mobile);
            vip_name = itemView.findViewById(R.id.name);

            order_amt = itemView.findViewById(R.id.order_amt);

            give_amt = itemView.findViewById(R.id.give_amt);
            order_status = itemView.findViewById(R.id.order_status);

            s_e_status = itemView.findViewById(R.id.s_e_status);
            cas_name = itemView.findViewById(R.id.cas_name);

            oper_time = itemView.findViewById(R.id.oper_time);
        }
    }
    @NonNull
    @Override
    public VipDepositOrderViewAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = View.inflate(mContext, R.layout.vip_deposit_order_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int) mContext.getResources().getDimension(R.dimen.table_row_height)));
        return new VipDepositOrderViewAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VipDepositOrderViewAdapter.MyViewHolder holder, int position) {
        if (null != mDatas){
            final JSONObject order_info = mDatas.getJSONObject(position);
            if (order_info != null){
                holder.row_id.setText(String.valueOf(position+1));
                holder.order_code.setText(order_info.getString("order_code"));
                holder.order_amt.setText(String.format(Locale.CHINA,"%.2f",order_info.getDoubleValue("order_amt")));
                holder.give_amt.setText(String.format(Locale.CHINA,"%.2f",order_info.getDoubleValue("give_amt")));

                holder.card_code.setText(order_info.getString("card_code"));
                holder.vip_name.setText(order_info.getString("name"));
                holder.mobile.setText(order_info.getString("mobile"));

                int order_status = order_info.getIntValue("status");
                if (order_status == 1)
                    holder.order_status.setTextColor(mContext.getColor(R.color.orange_1));

                holder.order_status.setText(order_info.getString("status_name"));
                holder.order_status.setTag(order_info.getIntValue("status"));

                holder.s_e_status.setText(order_info.getString("s_e_status_name"));
                holder.s_e_status.setTag(order_info.getIntValue("s_e_status"));

                holder.cas_name.setText(order_info.getString("cas_name"));

                holder.oper_time.setText(order_info.getString("oper_time"));

                holder.mCurrentLayoutItemView.setOnTouchListener(touchListener);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0: mDatas.size();
    }

    private View.OnTouchListener touchListener = (v, event) -> {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            setCurrentItemView(v);
            final TextView order_code_tv = v.findViewById(R.id.order_code);

            if (isClickView(order_code_tv,event.getX(),event.getY())){
                final VipDepositDetailsDialog vipDepositDetailsDialog = new VipDepositDetailsDialog(mContext,getCurrentOrder());
                vipDepositDetailsDialog.show();
            }
        }
        v.performClick();
        return false;
    };

    public void setDatas(final String where_sql){
        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT \n" +
                "       datetime(a.addtime, 'unixepoch', 'localtime') oper_time,\n" +
                "       case transfer_status when 1 then '未交班' when 2 then '已交班' else '其他' end s_e_status_name,\n" +
                "       status ,\n" +
                "       case status when 1 then '未付款' when '2' then '已付款' when '3' then '已完成' when '4' then '已关闭' end status_name,\n" +
                "       b.cas_name,\n" +
                "       name,\n" +
                "       mobile,\n" +
                "       card_code,\n" +
                "       order_money order_amt,\n" +
                "       give_money give_amt,\n" +
                "       order_code\n" +
                "  FROM member_order_info a left join cashier_info b on a.cashier_id = b.cas_id " + where_sql;

        Logger.d("sql:%s",sql);
        mDatas = SQLiteHelper.getListToJson(sql,err);
        if (mDatas != null){
            notifyDataSetChanged();
        }else
            MyDialog.ToastMessage("加载销售单据错误：" + err,mContext,null);
    }
}
