package com.wyc.cloudapp.adapter;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.ChangeNumOrPriceDialog;
import com.wyc.cloudapp.dialog.GoodsWeighDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.callback.ClickListener;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SaleGoodsViewAdapter extends RecyclerView.Adapter<SaleGoodsViewAdapter.MyViewHolder> {

    private Context mContext;
    private JSONArray mDatas;
    private OnItemClickListener mOnItemClickListener;
    private OnItemDoubleClickListener mOnItemDoubleClickListener;
    private View mCurrentItemView;
    private int mCurrentItemIndex;
    private int mOrderType = 1;//订单类型 1线下 2线上
    public SaleGoodsViewAdapter(Context context){
        this.mContext = context;
        mDatas = new JSONArray();
    }
    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView row_id,gp_id,goods_id,goods_title,unit_name,barcode_id,barcode,sale_price,sale_num,sale_amt;
        View mCurrentLayoutItemView;
        MyViewHolder(View itemView) {
            super(itemView);
            mCurrentLayoutItemView = itemView;
            row_id = itemView.findViewById(R.id.row_id);
            goods_id = itemView.findViewById(R.id.goods_id);
            gp_id = itemView.findViewById(R.id.gp_id);
            goods_title =  itemView.findViewById(R.id.goods_title);
            unit_name =  itemView.findViewById(R.id.unit_name);
            barcode_id =  itemView.findViewById(R.id.barcode_id);
            barcode =  itemView.findViewById(R.id.barcode);
            sale_price =  itemView.findViewById(R.id.sale_price);
            sale_num = itemView.findViewById(R.id.sale_num);
            sale_amt = itemView.findViewById(R.id.sale_amt);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = View.inflate(mContext, R.layout.sale_goods_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.sale_goods_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        if (mDatas != null){
            final JSONObject goods_info = mDatas.getJSONObject(i);
            if (goods_info != null){
                myViewHolder.row_id.setText(String.format(Locale.CHINA,"%s%s",i + 1,"、"));
                myViewHolder.goods_id.setText(goods_info.getString("goods_id"));
                myViewHolder.gp_id.setText(goods_info.getString("gp_id"));
                myViewHolder.goods_title.setText(goods_info.getString("goods_title"));
                myViewHolder.unit_name.setText(goods_info.getString("unit_name"));
                myViewHolder.barcode_id.setText(goods_info.getString("barcode_id"));
                myViewHolder.barcode.setText(goods_info.getString("barcode"));
                myViewHolder.sale_price.setText(goods_info.getString("price"));
                myViewHolder.sale_num.setText(goods_info.getString("xnum"));
                myViewHolder.sale_amt.setText(goods_info.getString("sale_amt"));

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
        return mDatas == null ? 0 : mDatas.size();
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

    public void addSaleGoods(final JSONObject goods,final JSONObject vip){
        if (null != goods){
            double xnum = goods.getDoubleValue("xnum");//如果不存在xnum,返回0.0；新增的商品不存在xnum字段，以及该字段值不会为0;挂单以及条码秤称重商品已经存在
            boolean isBarcodeWeighingGoods = !Utils.getNullStringAsEmpty(goods,GoodsInfoViewAdapter.W_G_MARK).isEmpty(),isZero = Utils.equalDouble(xnum,0.0);
            if(!isBarcodeWeighingGoods && isZero && goods.getIntValue("type") == 2){//type 1 普通 2散装称重 3鞋帽
                GoodsWeighDialog goodsWeighDialog = new GoodsWeighDialog(mContext,goods.getString("barcode_id"));
                goodsWeighDialog.setOnYesOnclickListener(myDialog -> {
                    double num = myDialog.getContent();
                    if (!Utils.equalDouble(num,0.0))
                        addSaleGoods(goods,num,false,vip);
                    myDialog.dismiss();
                });
                goodsWeighDialog.show();
            }else{
                if (isZero)xnum = 1.0;
                addSaleGoods(goods,xnum,isBarcodeWeighingGoods,vip);
            }
        }
    }

    public void deleteSaleGoods(int index,double num){
        int size = mDatas.size();
        if (0 <= index && index < size){
            if (num == 0){//等于0删除整条记录
                mDatas.remove(index);
                if (mCurrentItemIndex == index){//如果删除的是当前选择的item则重置当前index以及View
                    if (index == size - 1){
                        mCurrentItemIndex--;
                    }else{
                        mCurrentItemIndex =  mDatas.size() - 1;
                    }
                    mCurrentItemView = null;
                }
            }else{
                JSONObject jsonObject = mDatas.getJSONObject(index);
                double current_num = jsonObject.getDoubleValue("xnum"),
                        price = jsonObject.getDoubleValue("price");
                if ((current_num = current_num - num) <= 0){
                    Logger.d("index:%d,mCurrentItemIndex:%d",index,mCurrentItemIndex);
                    mDatas.remove(index);
                    if (index == size - 1){
                        mCurrentItemIndex--;
                    }else{
                        mCurrentItemIndex = mDatas.size() - 1;
                    }
                    mCurrentItemView = null;
                }else{
                    jsonObject.put("xnum",current_num);
                    jsonObject.put("sale_amt",Utils.formatDouble(current_num * price,2));
                    jsonObject.put("old_amt",Utils.formatDouble(current_num * jsonObject.getDoubleValue("old_price"),2));
                }
            }
            notifyDataSetChanged();
        }
    }
    public void updateSaleGoodsDialog(final short type){//type 0 修改数量 1修改价格 2打折
        JSONObject cur_json = getCurrentContent();
        if (cur_json != null){
            ChangeNumOrPriceDialog dialog;
            switch (type){
                case 1:
                    dialog = new ChangeNumOrPriceDialog(mContext,"新价格",cur_json.getString("price"));
                    break;
                case 2:
                    dialog = new ChangeNumOrPriceDialog(mContext,mContext.getString(R.string.discount_sz),String.format(Locale.CHINA,"%.0f",Utils.getNotKeyAsDefault(cur_json,"discount",1.0)*100));
                    break;
                    default:
                        dialog = new ChangeNumOrPriceDialog(mContext,"新数量",String.format(Locale.CHINA,"%.2f",Utils.getNotKeyAsDefault(cur_json,"xnum",1.0)));
                        break;
            }
            dialog.setYesOnclickListener(myDialog -> {
                updateSaleGoodsInfo(myDialog.getContentToDouble(),type);
                myDialog.dismiss();
            }).setNoOnclickListener(Dialog::dismiss).show();
        }else{
            MyDialog.ToastMessage("请选择需要修改的商品!",mContext,null);
        }
    }
    public JSONArray discount(double value){//整单折
        double  discount_amt = 0.0,old_price = 0.0,new_price = 0.0,xnum = 0.0,discount;
        boolean d_discount = false;//是否折上折
        for(int i = 0,length = mDatas.size();i < length;i++){
            JSONObject json = mDatas.getJSONObject(i);
            old_price = json.getDoubleValue("old_price");
            discount = Utils.formatDouble(value / 100,4);
            new_price = json.getDoubleValue("price");
            xnum = json.getDoubleValue("xnum");

            if (d_discount){
                new_price = Utils.formatDouble(new_price * discount,2);
            }else{
                new_price = Utils.formatDouble(old_price * discount,2);
            }

            discount_amt = Utils.formatDouble(xnum * (old_price - new_price),2);

            json.put("discount", discount);
            json.put("discount_amt", discount_amt);
            json.put("price",new_price);
            json.put("dis_type",6);
            json.put("sale_amt",Utils.formatDouble(xnum * new_price,2));
            json.put("old_amt",Utils.formatDouble(old_price * xnum,2));
        }
        notifyDataSetChanged();
        return mDatas;
    }
    public void clearGoods(){
        mDatas.fluentClear();
        this.notifyDataSetChanged();
    }
    public JSONObject getCurrentContent() {
        if (0 <= mCurrentItemIndex && mCurrentItemIndex <= mDatas.size() - 1){
            return mDatas.getJSONObject(mCurrentItemIndex);
        }
        return new JSONObject();
    }
    public int getCurrentItemIndex(){
        return mCurrentItemIndex;
    }
    public SaleGoodsViewAdapter setCurrentItemIndex(int index){mCurrentItemIndex = index;return this;}
    public JSONArray getDatas() {
        return mDatas;
    }
    public JSONArray updateGoodsInfoToVip(final JSONObject vip){
        double discount = 1.0,new_price = 0.0,old_price,discount_amt = 0.0,xnum = 0.0;
        if (vip != null){
            JSONObject jsonObject;
            for (int i = 0,length = mDatas.size();i < length;i++){
                jsonObject = mDatas.getJSONObject(i);
                old_price = jsonObject.getDoubleValue("old_price");
                xnum = jsonObject.getDoubleValue("xnum");

                switch (jsonObject.getIntValue("yh_mode")){
                    case 0://无优惠
                        discount  = 1.0;
                        new_price = old_price;
                        break;
                    case 1://会员价
                        new_price = Utils.formatDouble(jsonObject.getDoubleValue("yh_price"),2);
                        discount  = Utils.formatDouble(new_price / (old_price == 0 ? 1 : old_price),2);
                        break;
                    case 2://会员折扣
                        discount  = Utils.formatDouble(Utils.getNotKeyAsDefault(vip,"discount",1.0) / 10,2);
                        new_price = Utils.formatDouble(old_price * discount,2);
                        break;
                }
                discount_amt = Utils.formatDouble(xnum * (old_price - new_price),2);
                jsonObject.put("dis_type",5);
                jsonObject.put("discount", discount);
                jsonObject.put("discount_amt", discount_amt);
                jsonObject.put("price",new_price);
                jsonObject.put("sale_amt",Utils.formatDouble(xnum * new_price,2));
                jsonObject.put("old_amt",Utils.formatDouble(old_price * xnum,2));
            }
            notifyDataSetChanged();
        }
        return mDatas;
    }
    public String generateOrderCode(final String pos_num,int order_type){
        String prefix = "N" + pos_num + "-" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + "-",order_code ;
        JSONObject orders= new JSONObject();
        if (SQLiteHelper.execSql(orders,"SELECT count(order_id) + 1 order_id from retail_order where date(addtime,'unixepoch' ) = date('now')")){
            order_code =orders.getString("order_id");
            order_code = prefix + "0000".substring(order_code.length()) + order_code;
            mOrderType = order_type;
        }else{
            order_code = prefix + "0001";;
            MyDialog.ToastMessage("生成订单号错误：" + orders.getString("info"),mContext,null);
        }
        return order_code;
    }
    public int getOrderType(){return mOrderType;}
    private void updateSaleGoodsInfo(double value,int type){//type 0 修改数量 1修改价格 2打折
        JSONObject json = getCurrentContent();
        double discount = 1.0,discount_amt = 0.0,old_price = 0.0,new_price = 0.0,xnum = 0.0;
        boolean d_discount = false;//是否折上折
        old_price = json.getDoubleValue("old_price");
        switch (type){
            case 0:
                if (value <= 0){
                    deleteSaleGoods(getCurrentItemIndex(),0);
                }else{
                    xnum = value;
                    new_price = json.getDoubleValue("price");
                    discount_amt = xnum * (old_price - new_price);

                    json.put("discount_amt", Utils.formatDouble(discount_amt + json.getDoubleValue("discount_amt"),2));
                    json.put("xnum",Utils.formatDouble(xnum,4));
                }
                break;
            case 1:
                xnum = json.getDoubleValue("xnum");

                new_price = value;
                discount = Utils.formatDouble(new_price / old_price,4);
                discount_amt = Utils.formatDouble(xnum * (old_price - new_price),2);

                json.put("dis_type", 4);
                json.put("discount", discount);
                json.put("discount_amt", discount_amt);
                json.put("price",Utils.formatDouble(new_price,2));
                break;
            case 2:
                json.put("dis_type",6);
                discount = Utils.formatDouble(value / 100,4);
                new_price = json.getDoubleValue("price");
                xnum = json.getDoubleValue("xnum");

                if (d_discount){
                    new_price = Utils.formatDouble(new_price * discount,2);
                }else{
                    new_price = Utils.formatDouble(old_price * discount,2);
                }

                discount_amt = Utils.formatDouble(xnum * (old_price - new_price),2);

                json.put("discount", discount);
                json.put("discount_amt", discount_amt);
                json.put("price",new_price);
                break;
        }
        json.put("sale_amt",Utils.formatDouble(xnum * new_price,2));
        json.put("old_amt",Utils.formatDouble(old_price * xnum,2));

        Logger.d_json(json.toString());
        notifyDataSetChanged();
    }
    private void setCurrentItemIndexAndItemView(View v){
        TextView tv_id,tv_barcode_id,tv_gp_id;
        mCurrentItemView = v;
        if (null != mCurrentItemView && (tv_id = mCurrentItemView.findViewById(R.id.goods_id)) != null &&
                (tv_barcode_id = mCurrentItemView.findViewById(R.id.barcode_id)) != null && (tv_gp_id = mCurrentItemView.findViewById(R.id.gp_id) ) != null){

            CharSequence id = tv_id.getText(),barcode_id = tv_barcode_id.getText(),gp_id = tv_gp_id.getText();
            if (mDatas != null ){
                for (int i = 0,length = mDatas.size();i < length;i ++){
                    JSONObject json = mDatas.getJSONObject(i);
                    if (id.equals(json.getString("goods_id")) && barcode_id.equals(json.getString("barcode_id")) &&
                            gp_id.equals(json.getString("gp_id"))){
                        mCurrentItemIndex = i;
                        return;
                    }
                }
            }
        }
        mCurrentItemIndex = -1;
    }
    private void setSelectStatus(View v){
        TextView goods_name;
        if(null != mCurrentItemView){
            goods_name = mCurrentItemView.findViewById(R.id.goods_title);
            goods_name.clearAnimation();
            goods_name.setTextColor(mContext.getColor(R.color.good_name_color));
        }
        goods_name = v.findViewById(R.id.goods_title);
        goods_name.startAnimation(AnimationUtils.loadAnimation(mContext, R.anim.shake_x));
        goods_name.setTextColor(mContext.getColor(R.color.blue));

        if (mCurrentItemView != v)setCurrentItemIndexAndItemView(v);
    }
    private void addSaleGoods(final JSONObject goods,double num,boolean isBarcodeWeighingGoods,final JSONObject vip){
        boolean exist = false;
        int barcode_id = goods.getIntValue("barcode_id"),gp_id = goods.getIntValue("gp_id");
        double  sel_amount,price,discount = 1.0,discount_amt = 0.0,new_price = 0.0,
                sale_num = 0.0,sale_amount = 0.0,sale_discount_amt = 0.0;
        JSONObject tmp_obj;

        price = goods.getDoubleValue("price");

        for (int i = 0,length = mDatas.size();i < length;i++){
            tmp_obj = mDatas.getJSONObject(i);
            if (barcode_id == tmp_obj.getIntValue("barcode_id") && gp_id == tmp_obj.getIntValue("gp_id")){
                exist = true;
                sale_num = tmp_obj.getDoubleValue("xnum");
                sale_amount = tmp_obj.getDoubleValue("sale_amt");
                sale_discount_amt = tmp_obj.getDoubleValue("discount_amt");
                discount  = Utils.getNotKeyAsDefault(tmp_obj,"discount",1.0);
                new_price = Utils.formatDouble(price * discount,2);
                if (isBarcodeWeighingGoods){
                    sel_amount = goods.getDoubleValue("sale_amt");
                }else
                    sel_amount = Utils.formatDouble(num * new_price,2);
                discount_amt = Utils.formatDouble((num * (price - new_price)) + sale_discount_amt,2);
                tmp_obj.put("old_price", price);
                tmp_obj.put("price", new_price);
                tmp_obj.put("discount", discount);
                tmp_obj.put("discount_amt", discount_amt);
                tmp_obj.put("xnum",Utils.formatDouble(sale_num + num,4));
                tmp_obj.put("sale_amt",Utils.formatDouble(sale_amount + sel_amount,2));
                tmp_obj.put("old_amt",Utils.formatDouble(sale_amount + sel_amount + discount_amt,2));
                Logger.d_json(tmp_obj.toString());
                mCurrentItemIndex = i;
                break;
            }
        }

        if (!exist){
            if (vip != null){
                goods.put("card_code",vip.getString("card_code"));
                goods.put("name",vip.getString("name"));
                goods.put("mobile",vip.getString("mobile"));

                switch (goods.getIntValue("yh_mode")){
                    case 0://无优惠
                        discount  = 1.0;
                        new_price = price;
                        break;
                    case 1://会员价
                        new_price = Utils.formatDouble(goods.getDoubleValue("yh_price"),2);
                        discount  = Utils.formatDouble(new_price / (price == 0 ? 1 : price),2);
                        break;
                    case 2://会员折扣
                        discount  = Utils.formatDouble(Utils.getNotKeyAsDefault(goods,"discount",10.0) / 10,2);
                        new_price = Utils.formatDouble(price * discount,2);
                        break;
                }
                goods.put("dis_type",5);
            }else{
                discount  = Utils.formatDouble(Utils.getNotKeyAsDefault(goods,"discount",1.0),2);
                new_price = Utils.formatDouble(price * discount,2);
            }

            if (isBarcodeWeighingGoods){
                sel_amount = goods.getDoubleValue("sale_amt");
            }else
                sel_amount = Utils.formatDouble(num * new_price,2);

            discount_amt = Utils.formatDouble(num * (price - new_price),2);

            goods.put("dis_type",0);//无优惠
            goods.put("old_price", price);
            goods.put("price",new_price);
            goods.put("discount", discount);
            goods.put("discount_amt", discount_amt);
            goods.put("xnum",num);
            goods.put("sale_amt",sel_amount);
            goods.put("old_amt",Utils.formatDouble(sel_amount + discount_amt,2));
            mDatas.add(goods);
            mCurrentItemIndex = mDatas.size() - 1;
        }
        this.notifyDataSetChanged();
    }

    public boolean splitCombinationalGoods(final JSONArray arrays,int gp_id,double gp_price,double gp_num,int dis_type,StringBuilder err){
        final String sql = "select b.xnum xnum,(b.xnum * b.retail_price / a.amt) * " + gp_price +" / case b.xnum when 0 then 1 else b.xnum end price," +
                "b.barcode_id barcode_id,b.barcode barcode,b.conversion conversion,b.gp_id gp_id,b.tc_rate tc_rate,b.tc_mode tc_mode,b.tax_rate tax_rate,b.ps_price ps_price,b.cost_price cost_price\n" +
                ",b.trade_price trade_price,b.retail_price retail_price,b.buying_price buying_price from vi_goods_group_info b inner join \n" +
                "(select case sum(xnum * retail_price) when 0 then 1 else sum(xnum * retail_price) end amt,gp_id \n" +
                "from vi_goods_group_info group by gp_id) a on a.gp_id = b.gp_id where b.gp_id =" +  gp_id +" group by barcode_id;";

        Logger.d("拆分组合商品：%s",sql);

        JSONArray tmp;
        JSONObject tmp_obj;
        while (gp_num-- != 0){
            if (null != (tmp = SQLiteHelper.getListToJson(sql,err))){
                for (int k = 0,length = tmp.size();k < length;k++){
                    tmp_obj = tmp.getJSONObject(k);
                    tmp_obj.put("dis_type",dis_type);
                    arrays.add(tmp_obj);
                }
            }else{
                return false;
            }
        }
        return true;
    }

    public double getSaleSumAmt(){//验证销售金额
        double amt = 0.0;
        for (int i = 0,size = mDatas.size();i < size;i++){
            JSONObject object = mDatas.getJSONObject(i);
            if (null != object)
                amt += object.getDoubleValue("sale_amt");
        }
        return amt;
    }

    public void setDatas(@NonNull final JSONArray array){
        mDatas = array;
    }

}
