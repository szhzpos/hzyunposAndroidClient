package com.wyc.cloudapp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.listener.ClickListener;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PayDetailViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int HEADER = -1;
    private static final int CONTENT = -2;
    private static final int FOOTER = -3;
    private Context mContext;
    private JSONArray mDatas;
    private View mCurrentItemView;
    private int mCurrentItemIndex;
    private OnItemClickListener mOnItemClickListener;
    private OnItemDoubleClickListener mOnItemDoubleClickListener;
    public PayDetailViewAdapter(Context context){
        this.mContext = context;
        mDatas = new JSONArray();
    }
    static class ContentHolder extends RecyclerView.ViewHolder {
        private TextView row_id,pay_method_id,pay_method_name,pay_detail_amt,pay_detail_zl,pay_detail_v_num;
        private View mCurrentLayoutItemView;//当前布局的item
        ContentHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;

            row_id = itemView.findViewById(R.id.row_id);
            pay_method_id =  itemView.findViewById(R.id.pay_method_id);
            pay_method_name =  itemView.findViewById(R.id.pay_method_name);
            pay_detail_amt =  itemView.findViewById(R.id.pay_detail_amt);
            pay_detail_zl =  itemView.findViewById(R.id.pay_detail_zl);
            pay_detail_v_num =  itemView.findViewById(R.id.pay_detail_v_num);
        }
    }

    static class HeaderHolder extends RecyclerView.ViewHolder {
        private TextView row_id,pay_method_id,pay_method_name,pay_detail_amt,pay_detail_zl,pay_detail_v_num;
        private View mCurrentLayoutItemView;//当前布局的item
        HeaderHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;

            row_id = itemView.findViewById(R.id.row_id);
            pay_method_id =  itemView.findViewById(R.id.pay_method_id);
            pay_method_name =  itemView.findViewById(R.id.pay_method_name);
            pay_detail_amt =  itemView.findViewById(R.id.pay_detail_amt);
            pay_detail_zl =  itemView.findViewById(R.id.pay_detail_zl);
            pay_detail_v_num =  itemView.findViewById(R.id.pay_detail_v_num);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = View.inflate(mContext, R.layout.pay_detail_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.pay_detail_height)));
        RecyclerView.ViewHolder holder;
        if (i == HEADER){
            holder = new HeaderHolder(itemView);
        }else{
            holder = new ContentHolder(itemView);
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder myViewHolder, int i) {
        if (myViewHolder instanceof HeaderHolder){
            HeaderHolder headerHolder = (HeaderHolder)myViewHolder;
            headerHolder.row_id.setTextColor(mContext.getResources().getColor(R.color.white,null));
            headerHolder.pay_method_id.setTextColor(mContext.getResources().getColor(R.color.white,null));
            headerHolder.pay_method_name.setTextColor(mContext.getResources().getColor(R.color.white,null));
            headerHolder.pay_detail_amt.setTextColor(mContext.getResources().getColor(R.color.white,null));
            headerHolder.pay_detail_zl.setTextColor(mContext.getResources().getColor(R.color.white,null));
            headerHolder.pay_detail_v_num.setTextColor(mContext.getResources().getColor(R.color.white,null));
            headerHolder.mCurrentLayoutItemView.setBackgroundColor(mContext.getResources().getColor(R.color.pay_detail_header,null));
        }else{
            JSONObject pay_detail = mDatas.optJSONObject(i - 1);
            if (pay_detail != null){
                ContentHolder contentHolder = (ContentHolder)myViewHolder;
                contentHolder.row_id.setText(String.valueOf(i));
                contentHolder.pay_method_id.setText(pay_detail.optString("pay_method_id"));
                contentHolder.pay_method_name.setText(pay_detail.optString("name"));
                contentHolder.pay_detail_amt.setText(pay_detail.optString("pamt"));
                contentHolder.pay_detail_zl.setText(pay_detail.optString("pzl"));
                contentHolder.pay_detail_v_num.setText(pay_detail.optString("v_num"));

                contentHolder.mCurrentLayoutItemView.setOnTouchListener(new ClickListener(v -> {
                    setCurrentItemIndexAndItemView(v);
                    deletePayDetail(mCurrentItemIndex);
                    if (mOnItemDoubleClickListener != null){
                        mOnItemDoubleClickListener.onClick(v,i);
                    }
                }, v -> {
                    setSelectStatus(v);
                    if (mOnItemClickListener != null){
                        mOnItemClickListener.onClick(v,i); }
                }));

                if (mCurrentItemIndex == i){
                    setSelectStatus(contentHolder.mCurrentLayoutItemView);
                    mCurrentItemView = contentHolder.mCurrentLayoutItemView;
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 1 : mDatas.length() + 1;
    }

    @Override
    public int getItemViewType(int position){
        if (0 == position){
            return HEADER;
        }
        return CONTENT;
    }

    public interface OnItemClickListener{
        void onClick(View v,int pos);
    }
    public interface OnItemDoubleClickListener{
        void onClick(View v,int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }
    public void setOnItemDoubleClickListener(OnItemDoubleClickListener onItemDoubleClickListener){
        this.mOnItemDoubleClickListener = onItemDoubleClickListener;
    }
    public JSONObject getCurrentContent() {
        return mDatas.optJSONObject(mCurrentItemIndex);
    }

    private void setCurrentItemIndexAndItemView(View v){
        TextView tv_id;
        mCurrentItemView = v;
        if (null != mCurrentItemView && (tv_id = mCurrentItemView.findViewById(R.id.pay_method_id)) != null){
            String id = tv_id.getText().toString();
            if (mDatas != null ){
                for (int i = 0,length = mDatas.length();i < length;i ++){
                    JSONObject json = mDatas.optJSONObject(i);
                    if (id.equals(json.optString("pay_method_id"))){
                        mCurrentItemIndex = i;
                        return;
                    }
                }
            }
        }
        mCurrentItemIndex = -1;
    }

    public int getCurrentItemIndex(){
        return mCurrentItemIndex;
    }

    public @NonNull JSONArray getDatas(){
        return mDatas;
    }

    public void addPayDetail(JSONObject pay_detail_info){
        if (pay_detail_info != null){
            try {
                JSONObject object = findPayDetailById(pay_detail_info.getString("pay_method_id"));
                if (object != null){
                    double payed_amt = object.getDouble("pamt"),
                            zl_amt = object.getDouble("pzl");

                    object.put("pamt",Utils.formatDouble(payed_amt + pay_detail_info.getDouble("pamt"),2));
                    object.put("pzl",Utils.formatDouble(zl_amt + pay_detail_info.getDouble("pzl"),2));
                    object.put("v_num",pay_detail_info.getString("v_num"));
                }else{
                    mDatas.put(pay_detail_info);
                }
                notifyDataSetChanged();
            }catch (JSONException e){
                e.printStackTrace();
                MyDialog.ToastMessage("付款错误：" + e.getMessage(),mContext,null);
            }

        }
    }

    public void deletePayDetail(int index){
        Logger.d("index:%d",index);
        if (0 <= index && index < mDatas.length()){
            mDatas.remove(index);
            if (mCurrentItemIndex == index){//如果删除的是当前选择的item则重置当前index以及View
                mCurrentItemIndex = -1;
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
        try {
            for (int i = 0,length = mDatas.length();i < length;i ++){//0为表头
                JSONObject jsonObject = mDatas.getJSONObject(i);
                if (id != null && id.equals(jsonObject.getString("pay_method_id"))){
                    return jsonObject;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            MyDialog.ToastMessage("查找付款记录错误：" + e.getMessage(),mContext,null);
        }
        return null;
    }
    public void clearPayDetail(){
        Utils.ClearJsons(mDatas);
        notifyDataSetChanged();
    }

    public double getPaySumAmt(){//验证付款金额
        double amt = 0.0;
        for (int i = 0,size = mDatas.length();i < size;i++){
            JSONObject object = mDatas.optJSONObject(i);
            if (null != object)
                amt += object.optDouble("pamt") - object.optDouble("pzl");
        }
        return amt;
    }
}
