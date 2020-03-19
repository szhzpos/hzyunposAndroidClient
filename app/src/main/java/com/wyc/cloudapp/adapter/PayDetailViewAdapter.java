package com.wyc.cloudapp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.listener.ClickListener;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PayDetailViewAdapter extends RecyclerView.Adapter<PayDetailViewAdapter.MyViewHolder> {
    private Context mContext;
    private JSONArray mDatas;
    private View mCurrentItemView;
    private int mCurrentItemIndex;
    private OnItemClickListener mOnItemClickListener;
    private OnItemDoubleClickListener mOnItemDoubleClickListener;
    public PayDetailViewAdapter(Context context){
        this.mContext = context;
        mDatas = new JSONArray();
        initHead();
    }
    static class MyViewHolder extends RecyclerView.ViewHolder {
        private TextView row_id,pay_method_id,pay_method_name,pay_detail_amt,pay_detail_zl,pay_detail_v_num;
        private View mCurrentLayoutItemView;//当前布局的item
        MyViewHolder(View itemView) {
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
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = View.inflate(mContext, R.layout.pay_detail_content, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.pay_detail_height)));

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (i == 0){
            myViewHolder.row_id.setText(mContext.getString(R.string.pay_detail_row_id_sz));
            myViewHolder.pay_method_id.setText(mContext.getString(R.string.space_sz));
            myViewHolder.pay_method_name.setText(mContext.getString(R.string.pay_detail_name_sz));
            myViewHolder.pay_detail_amt.setText(mContext.getString(R.string.pay_detail_amt_sz));
            myViewHolder.pay_detail_zl.setText(mContext.getString(R.string.pay_detail_zl_sz));
            myViewHolder.pay_detail_v_num.setText(mContext.getString(R.string.pay_detail_v_num_sz));

            myViewHolder.row_id.setTextColor(mContext.getResources().getColor(R.color.white,null));
            myViewHolder.pay_method_id.setTextColor(mContext.getResources().getColor(R.color.white,null));
            myViewHolder.pay_method_name.setTextColor(mContext.getResources().getColor(R.color.white,null));
            myViewHolder.pay_detail_amt.setTextColor(mContext.getResources().getColor(R.color.white,null));
            myViewHolder.pay_detail_zl.setTextColor(mContext.getResources().getColor(R.color.white,null));
            myViewHolder.pay_detail_v_num.setTextColor(mContext.getResources().getColor(R.color.white,null));

            myViewHolder.mCurrentLayoutItemView.setBackgroundColor(mContext.getResources().getColor(R.color.blue_subtransparent,null));
        }else{
            JSONObject pay_detail = mDatas.optJSONObject(i);
            if (pay_detail != null){
                myViewHolder.row_id.setText(String.valueOf(i));
                myViewHolder.pay_method_id.setText(pay_detail.optString("pay_method_id"));
                myViewHolder.pay_method_name.setText(pay_detail.optString("name"));
                myViewHolder.pay_detail_amt.setText(pay_detail.optString("pamt"));
                myViewHolder.pay_detail_zl.setText(pay_detail.optString("pzl"));
                myViewHolder.pay_detail_v_num.setText(pay_detail.optString("v_num"));

                myViewHolder.mCurrentLayoutItemView.setOnTouchListener(new ClickListener(v -> {
                    setCurrentItemIndexAndItemView(v);
                    deletePayDetail(mCurrentItemIndex,0);
                    if (mOnItemDoubleClickListener != null){
                        mOnItemDoubleClickListener.onClick(v,i);
                    }
                }, v -> {
                    setSelectStatus(v);
                    if (mOnItemClickListener != null){
                        mOnItemClickListener.onClick(v,i); }
                }));

                if (mCurrentItemIndex == i){
                    setSelectStatus(myViewHolder.mCurrentLayoutItemView);
                    mCurrentItemView = myViewHolder.mCurrentLayoutItemView;
                }
            }
        }

    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.length();
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
                }else{
                    mDatas.put(pay_detail_info);
                }
                notifyDataSetChanged();
            }catch (JSONException e){
                e.printStackTrace();
                MyDialog.ToastMessage("付款错误：" + e.getMessage(),mContext);
            }

        }
    }

    public void deletePayDetail(int index,double num){
        Logger.d("index:%d",index);
        if (0 <= index && index < mDatas.length()){
            if (num == 0){//等于0全部删除
                mDatas.remove(index);
                if (mCurrentItemIndex == index){//如果删除的是当前选择的item则重置当前index以及View
                    mCurrentItemIndex = -1;
                    mCurrentItemView = null;
                }
            }else{
                JSONObject jsonObject = mDatas.optJSONObject(index);
                try {
                    double current_num = jsonObject.getDouble("sale_num"),
                            price = jsonObject.getDouble("buying_price");
                    if ((current_num = current_num - num) <= 0){
                        mDatas.remove(index);
                    }else{
                        jsonObject.put("sale_num",current_num);
                        jsonObject.put("sale_amount", Utils.formatDouble(current_num * price,4));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.displayErrorMessage("删除商品错误：" + e.getMessage(),mContext);
                }
            }
            this.notifyDataSetChanged();
        }
    }

    private void setSelectStatus(View v){
        if(null != mCurrentItemView){
            mCurrentItemView.setBackgroundColor(mContext.getResources().getColor(R.color.white,null));
        }
        v.setBackgroundColor(mContext.getResources().getColor(R.color.pink,null));
        setCurrentItemIndexAndItemView(v);
    }

    private JSONObject findPayDetailById(final String id){
        try {
            for (int i = 1,length = mDatas.length();i < length;i ++){//0为表头
                JSONObject jsonObject = mDatas.getJSONObject(i);
                if (id != null && id.equals(jsonObject.getString("pay_method_id"))){
                    return jsonObject;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            MyDialog.ToastMessage("查找付款记录错误：" + e.getMessage(),mContext);
        }
        return null;
    }

    private void initHead(){
        mDatas.put(new JSONObject());
        notifyDataSetChanged();
    }
}
