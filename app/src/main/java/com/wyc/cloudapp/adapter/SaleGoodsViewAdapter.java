package com.wyc.cloudapp.adapter;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.ChangeNumOrPriceDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.PayDialog;
import com.wyc.cloudapp.listener.ClickListener;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

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
        TextView row_id,goods_id,goods_title,unit_id,unit_name,barcode_id,barcode,buying_price,sale_num,sale_amt;
        View mCurrentLayoutItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;
            row_id = itemView.findViewById(R.id.row_id);
            goods_id = itemView.findViewById(R.id.goods_id);
            goods_title =  itemView.findViewById(R.id.goods_title);
            unit_id =  itemView.findViewById(R.id.unit_id);
            unit_name =  itemView.findViewById(R.id.unit_name);
            barcode_id =  itemView.findViewById(R.id.barcode_id);
            barcode =  itemView.findViewById(R.id.barcode);
            buying_price =  itemView.findViewById(R.id.buying_price);
            sale_num = itemView.findViewById(R.id.sale_num);
            sale_amt = itemView.findViewById(R.id.sale_amt);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = View.inflate(mContext, R.layout.sale_goods_content, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.sale_goods_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            final JSONObject goods_info = mDatas.optJSONObject(i);
            if (goods_info != null){
                myViewHolder.row_id.setText(String.format(Locale.CHINA,"%s%s",i + 1,"、"));
                myViewHolder.goods_id.setText(goods_info.optString("goods_id"));
                myViewHolder.goods_title.setText(goods_info.optString("goods_title"));
                myViewHolder.unit_id.setText(goods_info.optString("unit_id"));
                myViewHolder.unit_name.setText(goods_info.optString("unit_name"));
                myViewHolder.barcode_id.setText(goods_info.optString("barcode_id"));
                myViewHolder.barcode.setText(goods_info.optString("barcode"));
                myViewHolder.buying_price.setText(goods_info.optString("buying_price"));
                myViewHolder.sale_num.setText(goods_info.optString("sale_num"));
                myViewHolder.sale_amt.setText(goods_info.optString("sale_amt"));

                if(myViewHolder.goods_title.getCurrentTextColor() == mContext.getResources().getColor(R.color.blue,null)){
                    myViewHolder.goods_title.setTextColor(mContext.getColor(R.color.black));//需要重新设置颜色；不然重用之后内容颜色为重用之前的。
                }

                myViewHolder.mCurrentLayoutItemView.setOnTouchListener(new ClickListener(v -> {
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

    public void addSaleGoods(JSONObject goods,final JSONObject vip){
        boolean exist = false;
        if (goods != null){
            try {
                String id = goods.getString("goods_id");
                double sel_num = 1.00,sel_amount,price,discount = 1.0,discount_amt = 0.0,new_price = 0.0;

                price = goods.getDouble("buying_price");

                for (int i = 0,length = mDatas.length();i < length;i++){
                    JSONObject tmp = mDatas.getJSONObject(i);
                    if (id.equals(tmp.getString("goods_id"))){
                        exist = true;

                        double sale_num = tmp.getDouble("sale_num");
                        double sale_amount = tmp.getDouble("sale_amt");
                        double sale_discount_amt = tmp.getDouble("discount_amt");

                        discount  = tmp.optDouble("discount",1.0);
                        new_price = Utils.formatDouble(price * discount,2);
                        sel_amount = Utils.formatDouble(sel_num * new_price,2);

                        discount_amt = Utils.formatDouble((sel_num * (price - new_price)) + sale_discount_amt,2);

                        tmp.put("old_price", price);
                        tmp.put("buying_price", new_price);
                        tmp.put("discount", discount);
                        tmp.put("discount_amt", discount_amt);
                        tmp.put("sale_num",Utils.formatDouble(sale_num + sel_num,4));
                        tmp.put("sale_amt",Utils.formatDouble(sale_amount + sel_amount,2));
                        tmp.put("order_amt",Utils.formatDouble(sale_amount + sel_amount + discount_amt,2));

                        Logger.d_json(tmp.toString());

                        mCurrentItemIndex = i;

                        break;
                    }
                }
                if (!exist){

                    if (vip != null){

                        goods.put("card_code",vip.getString("card_code"));
                        goods.put("name",vip.getString("name"));
                        goods.put("mobile",vip.getString("mobile"));

                        switch (goods.getInt("yh_mode")){
                            case 0://无优惠
                                discount  = 1.0;
                                new_price = price;
                                break;
                            case 1://会员价
                                new_price = Utils.formatDouble(goods.getDouble("yh_price"),2);
                                discount  = Utils.formatDouble(new_price / (price == 0 ? 1 : price),2);
                                break;
                            case 2://会员折扣
                                discount  = Utils.formatDouble(vip.optDouble("discount",1.0) / 10,2);
                                new_price = Utils.formatDouble(price * discount,2);
                                break;
                        }
                    }else{
                        discount  = Utils.formatDouble(goods.optDouble("discount",1.0),2);
                        new_price = Utils.formatDouble(price * discount,2);
                    }

                    sel_amount = Utils.formatDouble(sel_num * new_price,2);
                    discount_amt = Utils.formatDouble(sel_num * (price - new_price),2);

                    goods.put("old_price", price);
                    goods.put("buying_price",new_price);
                    goods.put("discount", discount);
                    goods.put("discount_amt", discount_amt);
                    goods.put("sale_num",sel_num);
                    goods.put("sale_amt",sel_amount);
                    goods.put("order_amt",Utils.formatDouble(sel_amount + discount_amt,2));
                    mDatas.put(goods);

                    Logger.d_json(goods.toString());

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
                        jsonObject.put("sale_amt",Utils.formatDouble(current_num * price,2));
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.displayErrorMessage("删除商品错误：" + e.getMessage(),mContext);
                }
            }
            this.notifyDataSetChanged();
        }
    }

    public void updateSaleGoodsDialog(final short type,final JSONObject vip){//type 0 修改数量 1修改价格 2打折
        JSONObject cur_json = getCurrentContent();
        if (cur_json != null){
            ChangeNumOrPriceDialog dialog;
            switch (type){
                case 1:
                    dialog = new ChangeNumOrPriceDialog(mContext,"新价格",cur_json.optString("buying_price"));
                    break;
                case 2:
                    dialog = new ChangeNumOrPriceDialog(mContext,mContext.getString(R.string.discount_sz),String.format(Locale.CHINA,"%.0f",cur_json.optDouble("discount",1.0)*100));
                    break;
                    default:
                        dialog = new ChangeNumOrPriceDialog(mContext,"新数量",String.format(Locale.CHINA,"%.2f",cur_json.optDouble("sale_num",1.0)));
                        break;
            }
            dialog.setYesOnclickListener(myDialog -> {
                updateSaleGoodsInfo(myDialog.getNewNumOrPrice(),type);
                myDialog.dismiss();
            }).setNoOnclickListener(Dialog::dismiss).show();
        }else{
            MyDialog.ToastMessage("请选择需要修改的商品!",mContext);
        }
    }

    private void updateSaleGoodsInfo(double value,short type){//type 0 修改数量 1修改价格 2打折
        JSONObject json = getCurrentContent();
        double discount = 1.0,discount_amt = 0.0,old_price = 0.0,new_price = 0.0,sale_num = 0.0;
        boolean d_discount = false;//是否折上折
        try {
            old_price = json.getDouble("old_price");
            switch (type){
                case 0:
                    if (value <= 0){
                        deleteSaleGoods(getCurrentItemIndex(),0);
                    }else{
                        sale_num = value;
                        new_price = json.getDouble("buying_price");
                        discount_amt = sale_num * (old_price - new_price);

                        json.put("discount_amt", Utils.formatDouble(discount_amt + json.getDouble("discount_amt"),2));
                        json.put("sale_num",Utils.formatDouble(sale_num,4));

                    }
                    break;
                case 1:
                    sale_num = json.getDouble("sale_num");

                    new_price = value;
                    discount = Utils.formatDouble(new_price / old_price,4);
                    discount_amt = Utils.formatDouble(sale_num * (old_price - new_price),2);

                    json.put("discount", discount);
                    json.put("discount_amt", discount_amt);
                    json.put("buying_price",Utils.formatDouble(new_price,2));
                    break;
                case 2:
                    discount = Utils.formatDouble(value / 100,4);
                    new_price = json.getDouble("buying_price");
                    sale_num = json.getDouble("sale_num");

                    if (d_discount){
                        new_price = Utils.formatDouble(new_price * discount,2);
                    }else{
                        new_price = Utils.formatDouble(old_price * discount,2);
                    }

                    discount_amt = Utils.formatDouble(sale_num * (old_price - new_price),2);

                    json.put("discount", discount);
                    json.put("discount_amt", discount_amt);
                    json.put("buying_price",new_price);
                    break;
            }
            json.put("sale_amt",Utils.formatDouble(sale_num * new_price,2));
            json.put("order_amt",Utils.formatDouble(old_price * sale_num,2));

            Logger.d_json(json.toString());
            notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
            MyDialog.displayErrorMessage("修改销售商品信息错误：" + e.getMessage(),mContext);
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
            goods_name.setTextColor(mContext.getColor(R.color.good_name_color));
        }
        goods_name = v.findViewById(R.id.goods_title);
        Animation shake = AnimationUtils.loadAnimation(mContext, R.anim.shake);
        goods_name.startAnimation(shake);
        goods_name.setTextColor(mContext.getColor(R.color.blue));
    }

    public void updateGoodsInfoToVip(final JSONObject vip){
        double discount = 1.0,new_price = 0.0,old_price,discount_amt = 0.0,sale_num = 0.0;
        if (vip != null){
            try {
                for (int i = 0,length = mDatas.length();i < length;i++){
                    JSONObject jsonObject = mDatas.getJSONObject(i);

                    jsonObject.put("card_code",vip.getString("card_code"));
                    jsonObject.put("name",vip.getString("name"));
                    jsonObject.put("mobile",vip.getString("mobile"));

                    old_price = jsonObject.getDouble("old_price");
                    sale_num = jsonObject.getDouble("sale_num");

                    switch (jsonObject.getInt("yh_mode")){
                        case 0://无优惠
                            discount  = 1.0;
                            new_price = old_price;
                            break;
                        case 1://会员价
                            new_price = Utils.formatDouble(jsonObject.getDouble("yh_price"),2);
                            discount  = Utils.formatDouble(new_price / (old_price == 0 ? 1 : old_price),2);
                            break;
                        case 2://会员折扣
                            discount  = Utils.formatDouble(vip.optDouble("discount",1.0) / 10,2);
                            new_price = Utils.formatDouble(old_price * discount,2);
                            break;
                    }
                    discount_amt = Utils.formatDouble(sale_num * (old_price - new_price),2);

                    jsonObject.put("discount", discount);
                    jsonObject.put("discount_amt", discount_amt);
                    jsonObject.put("buying_price",new_price);
                    jsonObject.put("sale_amt",Utils.formatDouble(sale_num * new_price,2));
                    jsonObject.put("order_amt",Utils.formatDouble(old_price * sale_num,2));
                }
                notifyDataSetChanged();
            }catch (JSONException e){
                e.getMessage();
                MyDialog.ToastMessage("会员折扣错误：" + e.getMessage(),mContext);
            }
        }
    }

}
