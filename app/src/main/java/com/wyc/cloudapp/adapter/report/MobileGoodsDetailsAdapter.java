package com.wyc.cloudapp.adapter.report;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.AbstractTableDataAdapter;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.report
 * @ClassName: GoodsDetailsAdapter
 * @Description: 交易查询报表商品明细适配器
 * @Author: wyc
 * @CreateDate: 2021/2/1 15:00
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/2/1 15:00
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class MobileGoodsDetailsAdapter extends AbstractDataAdapter<MobileGoodsDetailsAdapter.MyViewHolder>{

    private int mShowType = 0;
    public MobileGoodsDetailsAdapter(MainActivity context) {
        super(context);
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = View.inflate(mContext, R.layout.goods_details_content_layout, null);
        final RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dpToPx(mContext,88));
        final int margin = Utils.dpToPx(mContext,24);
        lp.setMarginStart(margin);
        lp.setMarginEnd(margin);
        itemView.setLayoutParams(lp);
        return new MyViewHolder(itemView);
    }

    static class MyViewHolder extends AbstractTableDataAdapter.SuperViewHolder{
        TextView goods_title_tv,sale_price_tv,sale_amt_tv,sale_num_tv;
        LinearLayout price_num_layout;
        MyViewHolder(View itemView) {
            super(itemView);
            price_num_layout = itemView.findViewById(R.id.price_num_layout);
            goods_title_tv =  itemView.findViewById(R.id.goods_title_tv);
            sale_price_tv = itemView.findViewById(R.id.sale_price_tv);
            sale_amt_tv = itemView.findViewById(R.id.sale_amt_tv);
            sale_num_tv = itemView.findViewById(R.id.sale_num_tv);
        }
    }


    @Override
    public void onBindViewHolder( @NonNull final  MyViewHolder holder, int position) {
        if (mDatas != null) {
            final JSONObject object = mDatas.getJSONObject(position);
            holder.goods_title_tv.setText(object.getString("goods_title"));
            if (mShowType == 1) {
                holder.price_num_layout.setVisibility(View.GONE);
            } else {
                holder.sale_price_tv.setText(String.format(Locale.CHINA, "%.2f", object.getDoubleValue("price")));
                holder.sale_num_tv.setText(object.getString("xnum"));
            }
            holder.sale_amt_tv.setText(String.format(Locale.CHINA, "%.2f%s", object.getDoubleValue("price_xnum"), "元"));
        }
    }

    public void setDatas(final JSONArray array, int type){
        mShowType = type;
        mDatas = array;
        notifyDataSetChanged();
    }

}
