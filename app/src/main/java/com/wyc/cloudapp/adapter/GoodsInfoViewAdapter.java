package com.wyc.cloudapp.adapter;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.LoginActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GoodsInfoViewAdapter extends RecyclerView.Adapter<GoodsInfoViewAdapter.MyViewHolder> {

    private Context mContext;
    private JSONArray mDatas;
    private OnItemClickListener mOnItemClickListener;
    private boolean mSearchLoad = false;//是否按搜索框条件加载
    private boolean mShowPic = true;

    public GoodsInfoViewAdapter(Context context){
        this.mContext = context;
        JSONObject jsonObject = new JSONObject();
        if (SQLiteHelper.getLocalParameter("g_i_show",jsonObject)){
            mShowPic = (jsonObject.optInt("s",1) == 1);
        }else{
            MyDialog.ToastMessage("加载是否显示商品图片参数错误：" + jsonObject.optString("info"),mContext,null);
        }
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView gp_id,goods_id,goods_title,unit_id,unit_name,barcode_id,barcode,price;
        ImageView goods_img;
        View mCurrentItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentItemView = itemView;

            goods_img = itemView.findViewById(R.id.goods_img);
            goods_id = itemView.findViewById(R.id.goods_id);
            gp_id = itemView.findViewById(R.id.gp_id);
            goods_title =  itemView.findViewById(R.id.goods_title);
            unit_id =  itemView.findViewById(R.id.unit_id);
            unit_name =  itemView.findViewById(R.id.unit_name);
            barcode_id =  itemView.findViewById(R.id.barcode_id);
            barcode =  itemView.findViewById(R.id.barcode);
            price =  itemView.findViewById(R.id.sale_price);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = View.inflate(mContext, R.layout.goods_info_content_layout, null);
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.goods_height));
        itemView.setLayoutParams(lp);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            JSONObject goods_info = mDatas.optJSONObject(i);
            if (goods_info != null){
                final String img_url = (String) goods_info.remove("img_url");
                if (mShowPic && img_url != null){
                    if (!"".equals(img_url)){
                        final String szImage = img_url.substring(img_url.lastIndexOf("/") + 1);
                        CustomApplication.execute(()->{
                            if (mContext instanceof Activity){
                                Activity activity = (Activity)mContext;
                                final Bitmap bitmap = BitmapFactory.decodeFile(LoginActivity.IMG_PATH + szImage);
                                activity.runOnUiThread(()->{
                                    myViewHolder.goods_img.setImageBitmap(bitmap);
                                });
                            }
                        });
                    }else{
                        myViewHolder.goods_img.setImageDrawable(mContext.getDrawable(R.drawable.nodish));
                    }
                }else{
                    myViewHolder.goods_img.setVisibility(View.GONE);
                }

                myViewHolder.goods_id.setText(goods_info.optString("goods_id"));
                myViewHolder.gp_id.setText(goods_info.optString("gp_id"));
                myViewHolder.goods_title.setText(goods_info.optString("goods_title"));
                myViewHolder.unit_id.setText(goods_info.optString("unit_id"));
                myViewHolder.unit_name.setText(goods_info.optString("unit_name"));
                myViewHolder.barcode_id.setText(goods_info.optString("barcode_id"));
                myViewHolder.barcode.setText(goods_info.optString("barcode"));
                myViewHolder.price.setText(goods_info.optString("price"));

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

    void setDatas(int id){

        StringBuilder err = new StringBuilder();
        String sql = "",category_id;
        if (-1 == id){
            sql = "select gp_id,-1 goods_id,ifnull(gp_title,'') goods_title,'' unit_id,ifnull(unit_name,'') unit_name,\n" +
                    " -1  barcode_id,ifnull(gp_code,'') barcode,gp_price price,ifnull(img_url,'') img_url from goods_group \n" +
                    "where status = '1'";
        }else{
            category_id = SQLiteHelper.getString("select category_id from shop_category where path like '%" + id +"%'",err);
            if (null == category_id){
                MyDialog.ToastMessage("加载商品错误：" + err,mContext,null);
                return;
            }
            category_id = category_id.replace("\r\n",",");
            sql = "select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,retail_price price,ifnull(img_url,'') img_url from barcode_info where goods_status = '1' and category_id in (" + category_id + ")";
        }

        mDatas = SQLiteHelper.getListToJson(sql,0,0,false,err);
        if (mDatas != null){
            if (mSearchLoad)mSearchLoad = false;
            this.notifyDataSetChanged();
        }else{
            MyDialog.ToastMessage("加载商品错误：" + err,mContext,null);
        }
    }

    public void fuzzy_search_goods(@NonNull final EditText search){
        final StringBuilder err = new StringBuilder();
        final String search_content = search.getText().toString();
        String sql = "select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,unit_id,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode,retail_price price\n" +
                ",ifnull(img_url,'') img_url from barcode_info where goods_status = '1' and  (barcode like '" + search_content + "%' or mnemonic_code like '" + search_content +"%')\n" +
                "UNION\n" +
                "select gp_id,-1 goods_id,ifnull(gp_title,'') goods_title,'' unit_id,ifnull(unit_name,'') unit_name,\n" +
                "-1 barcode_id,ifnull(gp_code,'') barcode,gp_price price,ifnull(img_url,'') img_url from goods_group \n" +
                "where status = '1' and  (barcode like '" + search_content + "%' or mnemonic_code like '" + search_content +"%')";
        mDatas = SQLiteHelper.getListToJson(sql,0,0,false,err);
        if (mDatas != null){
            if(mDatas.length() != 0){
                if (!mSearchLoad)mSearchLoad = true;
            }else{
                search.selectAll();
                MyDialog.ToastMessage("无此商品！",mContext,null);
            }
            this.notifyDataSetChanged();
        }else{
            search.selectAll();
            MyDialog.ToastMessage("搜索商品错误：" + err,mContext,null);
        }
    }

    public boolean getSingleGoods(@NonNull JSONObject object,int id){
       return SQLiteHelper.execSql(object,"select -1 gp_id,goods_id,ifnull(goods_title,'') goods_title,ifnull(unit_name,'') unit_name,barcode_id,ifnull(barcode,'') barcode," +
               "retail_price,retail_price price,tc_rate,tc_mode,tax_rate,ps_price,cost_price,trade_price,buying_price,yh_mode,yh_price,conversion from barcode_info where goods_status = '1' and barcode_id = '" + id +"'" +
               " UNION\n" +
               "select gp_id ,-1 goods_id,ifnull(gp_title,'') goods_title,ifnull(unit_name,'') unit_name,\n" +
               "-1 barcode_id,ifnull(gp_code,'') barcode,gp_price retail_price,gp_price price,0 tc_rate,0 tc_mode,0 tax_rate,0 ps_price,0 cost_price,0 trade_price,gp_price buying_price,0 yh_mode,0 yh_price,1 conversion from goods_group \n" +
               "where status = '1' and gp_id = '" + id +"'");
    }





    public interface OnItemClickListener{
        void onClick(View v,int pos);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.mOnItemClickListener = onItemClickListener;
    }

    public String generateOrderCode(final String pos_num){
        String prefix = "P" + pos_num + "-" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + "-",order_code ;
        JSONObject orders= new JSONObject();
        if (SQLiteHelper.execSql(orders,"SELECT count(order_id) + 1 order_id from retail_order where date(addtime,'unixepoch' ) = date('now')")){
            order_code =orders.optString("order_id");
            order_code = prefix + "0000".substring(order_code.length()) + order_code;
            Logger.d("order_id:%s,length:%d",order_code,order_code.length());
        }else{
            order_code = prefix + "0001";;
            MyDialog.ToastMessage("生成订单号错误：" + orders.optString("info"),mContext,null);
        }
        return order_code;
    }
}
