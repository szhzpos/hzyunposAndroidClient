package com.wyc.cloudapp.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.BitmapFactory;
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
    private boolean mSearchLoad = false;//是否按搜索框条件加载
    private boolean mShowPic = true;
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
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            JSONObject goods_info = mDatas.optJSONObject(i);
            String szImage;
            if (goods_info != null){
                szImage = (String) goods_info.remove("img_url");
                if (mShowPic && szImage != null){
                    if (!"".equals(szImage)){
                        szImage = szImage.substring(szImage.lastIndexOf("/") + 1);
                        myViewHolder.goods_img.setImageBitmap(BitmapFactory.decodeFile(SQLiteHelper.IMG_PATH + szImage));
                    }else{
                        myViewHolder.goods_img.setImageDrawable(mContext.getDrawable(R.drawable.nodish));
                    }
                }else{
                    myViewHolder.goods_img.setVisibility(View.GONE);
                }

                myViewHolder.goods_id.setText(goods_info.optString("goods_id"));
                myViewHolder.goods_title.setText(goods_info.optString("goods_title"));
                myViewHolder.unit_id.setText(goods_info.optString("unit_id"));
                myViewHolder.unit_name.setText(goods_info.optString("unit_name"));
                myViewHolder.barcode_id.setText(goods_info.optString("barcode_id"));
                myViewHolder.barcode.setText(goods_info.optString("barcode"));
                myViewHolder.buying_price.setText(goods_info.optString("buying_price"));

                if(myViewHolder.goods_title.getCurrentTextColor() == mContext.getResources().getColor(R.color.blue,null)){
                    myViewHolder.goods_title.setTextColor(mContext.getColor(R.color.good_name_color));//需要重新设置颜色；不然重用之后内容颜色为重用之前的。
                }

                if (mOnItemClickListener != null){
                    myViewHolder.mCurrentItemView.setOnClickListener((View v)->{
                        mOnItemClickListener.onClick(v,i);
                    });
                }

                if (mSearchLoad && mDatas.length() == 1 && null != myViewHolder.mCurrentItemView){//搜索只有一个的时候自动选择
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
        String sql = "select goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,buying_price,ifnull(img_url,'') img_url from barcode_info";
        if (category_id != null){
            category_id = SQLiteHelper.getString("select category_id from shop_category where path like '" + category_id +"%'",err);
            if (null == category_id){
                MyDialog.displayErrorMessage("加载类别错误：" + err,mContext);
                return;
            }
            category_id = category_id.replace("\r\n",",");
            sql = "select goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,buying_price,ifnull(img_url,'') img_url from barcode_info where goods_status = '1' and category_id in (" + category_id + ")";
        }
        mDatas = SQLiteHelper.getList(sql,0,0,false,err);
        if (mDatas != null){
            if (mSearchLoad)mSearchLoad = false;
            this.notifyDataSetChanged();
        }else{
            MyDialog.displayErrorMessage("加载类别错误：" + err,mContext);
        }
    }

    public void fuzzy_search_goods(String search_content){
        StringBuilder err = new StringBuilder();
        String sql = "select goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,buying_price,ifnull(img_url,'') img_url from " +
                "barcode_info where goods_status = '1' and  (barcode like '" + search_content + "%' or mnemonic_code like '" + search_content +"%')";
        mDatas = SQLiteHelper.getList(sql,0,0,false,err);
        if (mDatas != null){
            if(mDatas.length() != 0){
                if (!mSearchLoad)mSearchLoad = true;
                this.notifyDataSetChanged();
            }else{
                MyDialog.ToastMessage("无此商品！",mContext);
            }
        }else{
            MyDialog.displayErrorMessage("搜索商品错误：" + err,mContext);
        }
    }

    public boolean getSingleGoods(@NonNull JSONObject object,final String goods_id,final String barcode_id){
       return SQLiteHelper.execSql(object,"select goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,buying_price,yh_mode,yh_price from " +
               "barcode_info where goods_status = '1' and goods_id = '" + goods_id + "' and barcode_id = '" + barcode_id +"'");
    }

    public interface OnItemClickListener{
        void onClick(View v,int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }
}
