package com.wyc.cloudapp.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.callback.ClickListener;
import com.wyc.cloudapp.utils.Utils;

public class PayDetailViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int HEADER = -1;
    private static final int CONTENT = -2;
    private final Context mContext;
    private final JSONArray mDatas;
    private View mCurrentItemView;
    private int mCurrentItemIndex;
    public PayDetailViewAdapter(Context context){
        this.mContext = context;
        mDatas = new JSONArray();
    }
    static class ContentHolder extends RecyclerView.ViewHolder {
        private final TextView row_id,pay_method_id,pay_method_name,pay_detail_amt,pay_detail_zl,pay_detail_v_num;
        ContentHolder(View itemView) {
            super(itemView);
            row_id = itemView.findViewById(R.id.row_id);
            pay_method_id =  itemView.findViewById(R.id.pay_method_id);
            pay_method_name =  itemView.findViewById(R.id.pay_method_name);
            pay_detail_amt =  itemView.findViewById(R.id.pay_detail_amt);
            pay_detail_zl =  itemView.findViewById(R.id.pay_detail_zl);
            pay_detail_v_num =  itemView.findViewById(R.id.pay_detail_v_num);
        }
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        HeaderHolder(View itemView) {
            super(itemView);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView;
        RecyclerView.ViewHolder holder;
        if (i == HEADER){
            itemView = View.inflate(mContext, R.layout.pay_details_header_layout, null);
            holder = new HeaderHolder(itemView);
        }else{
            itemView = View.inflate(mContext, R.layout.pay_detail_content_layout, null);
            holder = new ContentHolder(itemView);
        }
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.pay_detail_row_height)));
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder myViewHolder, int i) {
        if (myViewHolder instanceof ContentHolder){
            final JSONObject pay_detail = mDatas.getJSONObject(i - 1);
            if (pay_detail != null){
                ContentHolder contentHolder = (ContentHolder)myViewHolder;
                contentHolder.row_id.setText(String.valueOf(i));
                contentHolder.pay_method_id.setText(pay_detail.getString("pay_method_id"));
                contentHolder.pay_method_name.setText(pay_detail.getString("name"));
                contentHolder.pay_detail_amt.setText(pay_detail.getString("pamt"));
                contentHolder.pay_detail_zl.setText(pay_detail.getString("pzl"));
                contentHolder.pay_detail_v_num.setText(pay_detail.getString("v_num"));

                contentHolder.itemView.setOnTouchListener(new ClickListener(v -> {
                    setCurrentItemIndexAndItemView(v);
                    deletePayDetail(mCurrentItemIndex);
                }, this::setSelectStatus));

                if (mCurrentItemIndex == i){
                    setSelectStatus(contentHolder.itemView);
                    mCurrentItemView = contentHolder.itemView;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size() + 1;
    }

    @Override
    public int getItemViewType(int position){
        if (0 == position){
            return HEADER;
        }
        return CONTENT;
    }

    private void setCurrentItemIndexAndItemView(View v){
        final TextView tv_id;
        mCurrentItemView = v;
        if (null != mCurrentItemView && (tv_id = mCurrentItemView.findViewById(R.id.pay_method_id)) != null){
            final String id = tv_id.getText().toString();
            for (int i = 0,length = mDatas.size();i < length;i ++){
                final JSONObject json = mDatas.getJSONObject(i);
                if (id.equals(json.getString("pay_method_id"))){
                    mCurrentItemIndex = i;
                    return;
                }
            }
        }
        mCurrentItemIndex = -1;
    }

    public @NonNull JSONArray getDatas(){
        return mDatas;
    }

    public void addPayDetail(JSONObject pay_detail_info){
        double amt = 0.0;
        if (pay_detail_info != null){
            amt = pay_detail_info.getDouble("pamt");
            final JSONObject object = findPayDetailById(pay_detail_info.getString("pay_method_id"));
            if (object != null){
                double payed_amt = object.getDouble("pamt"),
                        zl_amt = object.getDouble("pzl");

                amt += payed_amt;

                object.put("pamt",Utils.formatDouble(amt,2));
                object.put("pzl",Utils.formatDouble(zl_amt + pay_detail_info.getDouble("pzl"),2));
                object.put("v_num",pay_detail_info.getString("v_num"));

            }else{
                mDatas.add(pay_detail_info);
            }
            notifyDataSetChanged();
        }
    }

    private void deletePayDetail(int index){
        int size = mDatas.size();
        if (0 <= index && index < size){
            mDatas.remove(index);
            if (mCurrentItemIndex == index){//如果删除的是当前选择的item则重置当前index以及View
                if (index == size - 1){
                    mCurrentItemIndex--;
                }else{
                    mCurrentItemIndex++;
                }
                mCurrentItemView = null;
            }
            notifyDataSetChanged();
        }
    }

    private void setSelectStatus(View v){
        if(null != mCurrentItemView){
            mCurrentItemView.setBackgroundColor(mContext.getResources().getColor(R.color.white,null));
        }
        v.setBackgroundColor(mContext.getResources().getColor(R.color.pink,null));
        setCurrentItemIndexAndItemView(v);
    }

    public JSONObject findPayDetailById(final String id){
        if (id != null){
            for (int i = 0,length = mDatas.size();i < length;i ++){
                final JSONObject jsonObject = mDatas.getJSONObject(i);
                if (id.equals(jsonObject.getString("pay_method_id"))){
                    return jsonObject;
                }
            }
        }
        return null;
    }

    public double getPaySumAmt(){
        double amt = 0.0;
        for (int i = 0,size = mDatas.size();i < size;i++){
            final JSONObject object = mDatas.getJSONObject(i);
            if (null != object)
                amt += object.getDoubleValue("pamt") - object.getDoubleValue("pzl");
        }
        return amt;
    }
}
