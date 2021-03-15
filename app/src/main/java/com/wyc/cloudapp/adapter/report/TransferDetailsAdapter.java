package com.wyc.cloudapp.adapter.report;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapter;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.activity.mobile.report
 * @ClassName: TransferDetailsAdapter
 * @Description: 交班单明细适配器
 * @Author: wyc
 * @CreateDate: 2021/2/20 14:07
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/20 14:07
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TransferDetailsAdapter extends AbstractDataAdapter<TransferDetailsAdapter.MyViewHolder> {
    private final MainActivity mContext;
    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView pay_name_tv,pay_money_tv,order_num_tv;
        MyViewHolder(View itemView) {
            super(itemView);
            pay_name_tv = itemView.findViewById(R.id.pay_name_tv);
            pay_money_tv =  itemView.findViewById(R.id.pay_money_tv);
            order_num_tv = itemView.findViewById(R.id.order_num_tv);
        }
    }

    public TransferDetailsAdapter(final MainActivity context,final JSONArray array) {
         mContext = context;
         mDatas = array;
    }

    @Override
    public @NonNull
    MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext, R.layout.mobile_transfer_details_adapter_layout, null);
        final RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(mContext,58));

        itemView.setLayoutParams(lp);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder( @NonNull final  MyViewHolder holder, int position) {
        if (mDatas != null && !mDatas.isEmpty()) {
            final JSONObject object = mDatas.getJSONObject(position);
            holder.pay_name_tv.setText(object.getString("pay_name"));
            holder.pay_money_tv.setText(String.format(Locale.CHINA, "%.2f", object.getDoubleValue("pay_money")));
            holder.order_num_tv.setText(object.getString("order_num"));
        }
    }


    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }
}
