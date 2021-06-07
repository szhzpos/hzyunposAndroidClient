package com.wyc.cloudapp.adapter.report;

import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractDataAdapterForJson;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.report
 * @ClassName: CashierTransferNameAdapter
 * @Description: 收银员对账收银员名字适配器
 * @Author: wyc
 * @CreateDate: 2021/2/20 11:03
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/20 11:03
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class CashierTransferNameAdapter extends AbstractDataAdapterForJson<CashierTransferNameAdapter.MyViewHolder> {
    final MainActivity mContext;
    private View.OnClickListener mListener;
    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder {
        TextView cas_name,transfer_id,trans_time;
        MyViewHolder(View itemView) {
            super(itemView);
            cas_name = itemView.findViewById(R.id.cas_name);
            transfer_id =  itemView.findViewById(R.id.transfer_id);
            trans_time = itemView.findViewById(R.id.trans_time);
        }
    }

    public CashierTransferNameAdapter(final MainActivity context) {
        mContext = context;
    }

    @Override
    public @NonNull
    MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext, R.layout.mobile_cashier_transfer_name_layout, null);
        final RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(mContext,68));

        itemView.setLayoutParams(lp);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder( @NonNull final  MyViewHolder holder, int position) {
        if (mData != null) {
            final JSONObject object = mData.getJSONObject(position);
            holder.cas_name.setText(object.getString("cas_name"));
            holder.transfer_id.setText(Html.fromHtml("<u>" + object.getString("ti_code") + "</u>"));
            holder.transfer_id.setOnClickListener(mListener);
            holder.trans_time.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(object.getLongValue("transfer_time") * 1000));
        }
    }


    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void setItemListener(final View.OnClickListener listener){
        mListener = listener;
    }

}
