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
import com.wyc.cloudapp.listener.DoubleClickListener;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class SaleGoodsViewAdapter extends RecyclerView.Adapter<SaleGoodsViewAdapter.MyViewHolder> {

    private Context mContext;
    private JSONArray mDatas;
    private OnItemClickListener mOnItemClickListener;
    private OnItemDoubleClickListener mOnItemDoubleClickListener;
    private View mCurrentItemView;
    private int mCurrentItemIndex;
    public SaleGoodsViewAdapter(Context context){
        this.mContext = context;
        mDatas = new JSONArray();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView goods_id,goods_title,unit_id,unit_name,barcode_id,barcode,buying_price,sale_num,sale_amount;
        View mCurrentLayoutItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;

            goods_id = itemView.findViewById(R.id.goods_id);
            goods_title =  itemView.findViewById(R.id.goods_title);
            unit_id =  itemView.findViewById(R.id.unit_id);
            unit_name =  itemView.findViewById(R.id.unit_name);
            barcode_id =  itemView.findViewById(R.id.barcode_id);
            barcode =  itemView.findViewById(R.id.barcode);
            buying_price =  itemView.findViewById(R.id.buying_price);
            sale_num = itemView.findViewById(R.id.sale_sum_num);
            sale_amount = itemView.findViewById(R.id.sale_sum_amount);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = View.inflate(mContext, R.layout.sale_goods_content, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.goods_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            final JSONObject goods_info = mDatas.optJSONObject(i);
            if (goods_info != null){
                myViewHolder.goods_id.setText(goods_info.optString("goods_id"));
                myViewHolder.goods_title.setText(goods_info.optString("goods_title"));
                myViewHolder.unit_id.setText(goods_info.optString("unit_id"));
                myViewHolder.unit_name.setText(goods_info.optString("unit_name"));
                myViewHolder.barcode_id.setText(goods_info.optString("barcode_id"));
                myViewHolder.barcode.setText(goods_info.optString("barcode"));
                myViewHolder.buying_price.setText(goods_info.optString("buying_price"));
                myViewHolder.sale_num.setText(goods_info.optString("sale_num"));
                myViewHolder.sale_amount.setText(goods_info.optString("sale_amount"));

                if(myViewHolder.goods_title.getCurrentTextColor() == mContext.getResources().getColor(R.color.blue,null)){
                    myViewHolder.goods_title.setTextColor(mContext.getColor(R.color.green));//需要重新设置颜色；不然重用之后内容颜色为重用之前的。
                }

                myViewHolder.mCurrentLayoutItemView.setOnTouchListener(new DoubleClickListener(v -> {
                    setCurrentItemIndexAndItemView(v);
                    deleteSaleGoods(mCurrentItemIndex,0);
                    if (mOnItemDoubleClickListener != null){
                        mOnItemDoubleClickListener.onClick(v,i);
                    }
                }, v -> {
                    setSelectStatus(v);
                    setCurrentItemIndexAndItemView(v);
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

    public void addSaleGoods(JSONObject json){
        boolean exist = false;
        if (json != null){
            try {
                String id = json.getString("goods_id");
                double sel_num = 1.00,sel_amount,price;

                price = json.getDouble("buying_price");
                sel_amount = Utils.formatDouble(sel_num * price,4);

                for (int i = 0,length = mDatas.length();i < length;i++){
                    JSONObject tmp = mDatas.getJSONObject(i);
                    if (id.equals(tmp.getString("goods_id"))){
                        exist = true;
                        double sale_num = tmp.getDouble("sale_num");
                        double sale_amount = tmp.getDouble("sale_amount");

                        tmp.put("sale_num",Utils.formatDouble(sale_num + sel_num,4));
                        tmp.put("sale_amount",Utils.formatDouble(sale_amount + sel_amount,4));

                        mCurrentItemIndex = i;

                        break;
                    }
                }
                if (!exist){
                    json.put("sale_num",sel_num);
                    json.put("sale_amount",sel_amount);
                    mDatas.put(json);

                    mCurrentItemIndex = mDatas.length() - 1;
                }
                this.notifyDataSetChanged();
            }catch (JSONException e){
                MyDialog.displayErrorMessage("选择商品错误：" + e.getMessage(),mContext);
                e.printStackTrace();
            }
        }
    }

    public void deleteSaleGoods(int index,double num){
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
                        jsonObject.put("sale_amount",Utils.formatDouble(current_num * price,4));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.displayErrorMessage("删除商品错误：" + e.getMessage(),mContext);
                }
            }
            this.notifyDataSetChanged();
        }
    }

    public void clearGoods(){
        mDatas = new JSONArray();
        this.notifyDataSetChanged();
    }

    public JSONObject getCurrentContent() {
        return mDatas.optJSONObject(mCurrentItemIndex);
    }

    private void setCurrentItemIndexAndItemView(View v){
        TextView tv_id;
        mCurrentItemView = v;
        if (null != mCurrentItemView && (tv_id = mCurrentItemView.findViewById(R.id.goods_id)) != null){
            String id = tv_id.getText().toString();
            if (mDatas != null ){
                for (int i = 0,length = mDatas.length();i < length;i ++){
                    JSONObject json = mDatas.optJSONObject(i);
                    if (id.equals(json.optString("goods_id"))){
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

    public JSONArray getDatas() {
        return mDatas;
    }

    private void setSelectStatus(View v){
        TextView goods_name;
        if(null != mCurrentItemView){
            goods_name = mCurrentItemView.findViewById(R.id.goods_title);
            goods_name.clearAnimation();
            goods_name.setTextColor(mContext.getColor(R.color.green));
        }
        goods_name = v.findViewById(R.id.goods_title);
        Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);
        goods_name.startAnimation(shake);
        goods_name.setTextColor(mContext.getColor(R.color.blue));
    }
}
