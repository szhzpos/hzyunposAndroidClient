package com.wyc.cloudapp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class GoodsInfoViewAdapter extends RecyclerView.Adapter<GoodsInfoViewAdapter.MyViewHolder> {

    private Context mContext;
    private JSONArray mDatas;
    private OnItemClickListener mOnItemClickListener;

    public GoodsInfoViewAdapter(Context context){
        this.mContext = context;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView goods_id,goods_title,unit_id,unit_name,barcode_id,barcode,buying_price;
        ImageView goods_img;
        View mCurrentItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentItemView = itemView;

            goods_img = itemView.findViewById(R.id.goods_img);
            goods_id = itemView.findViewById(R.id.goods_id);
            goods_title =  itemView.findViewById(R.id.goods_title);
            unit_id =  itemView.findViewById(R.id.unit_id);
            unit_name =  itemView.findViewById(R.id.unit_name);
            barcode_id =  itemView.findViewById(R.id.barcode_id);
            barcode =  itemView.findViewById(R.id.barcode);
            buying_price =  itemView.findViewById(R.id.buying_price);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = View.inflate(mContext, R.layout.goods_info_content, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.goods_height));
        itemView.setLayoutParams(lp);

        if (mOnItemClickListener != null){
            itemView.setOnClickListener((View v)->{
                mOnItemClickListener.onClick(v,i);
            });
        }
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            JSONObject goods_info = mDatas.optJSONObject(i);
            String szImage;
            if (goods_info != null){
                szImage = goods_info.optString("image");
                if (!"".equals(szImage)){
                    Logger.d("图片内容：%s",szImage);
                }else{
                    myViewHolder.goods_img.setImageDrawable(mContext.getDrawable(R.drawable.nodish));
                }
                myViewHolder.goods_id.setText(goods_info.optString("goods_id"));
                myViewHolder.goods_title.setText(goods_info.optString("goods_title"));
                myViewHolder.unit_id.setText(goods_info.optString("unit_id"));
                myViewHolder.unit_name.setText(goods_info.optString("unit_name"));
                myViewHolder.barcode_id.setText(goods_info.optString("barcode_id"));
                myViewHolder.barcode.setText(goods_info.optString("barcode"));
                myViewHolder.buying_price.setText(goods_info.optString("buying_price"));

                if (mDatas.length() == 1 && null != myViewHolder.mCurrentItemView){
                        myViewHolder.mCurrentItemView.callOnClick();
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.length();
    }

    public JSONObject getItem(int i){
        return mDatas == null ? null : mDatas.optJSONObject(i);
    }

    public void setDatas(String category_id){
        StringBuilder err = new StringBuilder();
        String sql = "select goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,buying_price,ifnull(image,'') image from barcode_info";
        if (category_id != null){
            category_id = SQLiteHelper.getString("select category_id from shop_category where path like '" + category_id +"%'",err);
            if (null == category_id){
                MyDialog.displayErrorMessage("加载类别错误：" + err,mContext);
                return;
            }
            category_id = category_id.replace("\r\n",",");
            sql = "select goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,buying_price,ifnull(image,'') image from barcode_info where category_id in (" + category_id + ")";
        }
        mDatas = SQLiteHelper.getList(sql,0,0,false,err);
        if (mDatas != null){
            this.notifyDataSetChanged();
        }else{
            MyDialog.displayErrorMessage("加载类别错误：" + err,mContext);
        }
    }

    public void search_goods(String search_content){
        StringBuilder err = new StringBuilder();
        String sql = "select goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,buying_price,ifnull(image,'') image from " +
                "barcode_info where (barcode like '" + search_content + "%' or mnemonic_code like '" + search_content +"%')";
        mDatas = SQLiteHelper.getList(sql,0,0,false,err);
        if (mDatas != null){
            this.notifyDataSetChanged();
        }else{
            MyDialog.displayErrorMessage("加载类别错误：" + err,mContext);
        }
    }

    public interface OnItemClickListener{
        void onClick(View v,int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }
}
