package com.wyc.cloudapp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wyc.cloudapp.R;

import org.json.JSONArray;
import org.json.JSONObject;

public class SaleGoodsViewAdapter extends RecyclerView.Adapter<SaleGoodsViewAdapter.MyViewHolder> {

    private Context mContext;
    private JSONArray mDatas;
    private OnItemClickListener mOnItemClickListener;
    public SaleGoodsViewAdapter(Context context){
        this.mContext = context;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView goods_title,only_coding,p_t_g_id,barcode,in_num,unit_name,unit_id,inventory;
        MyViewHolder(View itemView) {
            super(itemView);
            goods_title = itemView.findViewById(R.id.goods_title);
            only_coding =  itemView.findViewById(R.id.only_coding);
            p_t_g_id = itemView.findViewById(R.id.p_t_g_id);
            barcode = itemView.findViewById(R.id.barcode);
            in_num = itemView.findViewById(R.id.in_num);
            unit_name = itemView.findViewById(R.id.unit_name);
            unit_id = itemView.findViewById(R.id.unit_id);
            inventory = itemView.findViewById(R.id.inventory);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = View.inflate(mContext, R.layout.goods_info_detail_content, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        itemView.setLayoutParams(lp);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            JSONObject task_info = mDatas.optJSONObject(i);
            if (task_info != null){
                myViewHolder.goods_title.setText(task_info.optString("goods_title"));
                myViewHolder.only_coding.setText(task_info.optString("only_coding"));
                myViewHolder.p_t_g_id.setText(task_info.optString("goods_spec_code"));
                myViewHolder.barcode.setText(task_info.optString("barcode"));
                myViewHolder.in_num.setText(task_info.optString("pt_xnum_sum"));
                myViewHolder.unit_name.setText(task_info.optString("unit_name"));
                myViewHolder.unit_id.setText(task_info.optString("unit_id"));

            }
            if (mOnItemClickListener != null){
                myViewHolder.inventory.setOnClickListener((View view)->{
                    mOnItemClickListener.onClick(i);
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.length();
    }

    public interface OnItemClickListener{
        void onClick(int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }
}
