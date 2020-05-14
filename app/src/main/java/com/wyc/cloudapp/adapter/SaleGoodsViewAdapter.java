package com.wyc.cloudapp.adapter;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
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
    private MainActivity mContext;
    private JSONArray mDatas,mDiscountRecords;
    private View mCurrentItemView;
    private int mCurrentItemIndex;
    private int mOrderType = 1;//订单类型 1线下 2线上
    private boolean d_discount = true;//是否折上折
    public SaleGoodsViewAdapter(MainActivity context){
        this.mContext = context;
        mDatas = new JSONArray();
        mDiscountRecords = new JSONArray();
    }

    private final static class DISCOUNT_TYPE {
/*             1 => '满减',
                     2 => '赠送',
                     3 => '促销或折扣',
                     4 => '手动折扣',
                     5 => '会员折扣',
                     6 => '整单折扣',
                     7 => '自动抹零',
                     8 => '手动抹零',*/
        static final int MONEY_OFF = 1,PRESENT = 2,PROMOTION = 3,M_DISCOUNT = 4,V_DISCOUNT = 5,A_DISCOUNT = 6,AUTO_MOL =7,M_MOL = 8;
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
                myViewHolder.sale_price.setText(String.format(Locale.CHINA,"%.2f",goods_info.getDoubleValue("price")));
                myViewHolder.sale_num.setText(String.format(Locale.CHINA,"%.2f",goods_info.getDoubleValue("xnum")));
                myViewHolder.sale_amt.setText(String.format(Locale.CHINA,"%.2f",goods_info.getDoubleValue("sale_amt")));

                if(myViewHolder.goods_title.getCurrentTextColor() == mContext.getResources().getColor(R.color.blue,null)){
                    myViewHolder.goods_title.setTextColor(mContext.getColor(R.color.black));//需要重新设置颜色；不然重用之后内容颜色为重用之前的。
                }

                myViewHolder.mCurrentLayoutItemView.setOnTouchListener(new ClickListener(v -> {
                    setCurrentItemIndexAndItemView(v);
                    deleteSaleGoods(mCurrentItemIndex,0);
                }, this::setSelectStatus));

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


    public void addSaleGoods(final JSONObject goods,final JSONObject vip){
        if (null != goods){
            double xnum = goods.getDoubleValue("xnum");//如果不存在xnum,返回0.0；新增的商品不存在xnum字段，以及该字段值不会为0;挂单以及条码秤称重商品已经存在
            boolean isBarcodeWeighingGoods = !Utils.getNullStringAsEmpty(goods,GoodsInfoViewAdapter.W_G_MARK).isEmpty(),isZero = Utils.equalDouble(xnum,0.0);
            if(!isBarcodeWeighingGoods && isZero && goods.getIntValue("type") == 2){//type 1 普通 2散装称重 3鞋帽
                GoodsWeighDialog goodsWeighDialog = new GoodsWeighDialog(mContext,mContext.getString(R.string.goods_i_s),goods.getString("barcode_id"));
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
                    jsonObject.put("original_amt",Utils.formatDouble(current_num * jsonObject.getDoubleValue("original_price"),2));
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
                updateSaleGoodsInfo(myDialog.getContent(),type);
                myDialog.dismiss();
            }).setNoOnclickListener(Dialog::dismiss).show();
        }else{
            MyDialog.ToastMessage("请选择需要修改的商品!",mContext,null);
        }
    }
    public void allDiscount(double value){//整单折 6
        double  discount_amt = 0.0,original_amt = 0.0,new_price = 0.0,xnum = 0.0,discount,current_discount_amt = 0.0,old_sale_amt = 0.0,current_sale_amt = 0.0;
        int discount_type = DISCOUNT_TYPE.A_DISCOUNT;

        if (Utils.equalDouble(value / 100,1.0)){//discount 1.0 还原价格并清除折扣记录
            deleteDiscountRecord(discount_type);
        }else{
            JSONObject discount_json,json;
            JSONArray discount_details = new JSONArray();
            for(int i = 0,length = mDatas.size();i < length;i++){
                json  = mDatas.getJSONObject(i);
                original_amt = json.getDoubleValue("original_amt");
                discount = Utils.formatDouble(value / 100,4);
                xnum = Utils.getNotKeyAsDefault(json,"xnum",1.0);
                old_sale_amt = json.getDoubleValue("sale_amt");

                if (d_discount){
                    current_sale_amt = Utils.formatDouble(old_sale_amt * discount,2);
                }else{
                    current_sale_amt = Utils.formatDouble(original_amt * discount,2);
                }
                new_price = Utils.formatDouble(current_sale_amt / xnum,4);
                discount_amt = Utils.formatDouble(original_amt - current_sale_amt,2);


                json.put("discount", discount);
                json.put("discount_amt", discount_amt);
                json.put("price",new_price);
                json.put("discount_type",DISCOUNT_TYPE.A_DISCOUNT);
                json.put("sale_amt",current_sale_amt);

                current_discount_amt = old_sale_amt - current_sale_amt;

                if (!Utils.equalDouble(current_discount_amt,0.0)){
                    discount_json = new JSONObject();
                    discount_json.put("gp_id",json.getIntValue("gp_id"));
                    discount_json.put("barcode_id",json.getIntValue("barcode_id"));
                    discount_json.put("price",current_discount_amt);
                    discount_details.add(discount_json);
                }
            }
            //处理商品优惠明细
            if (!discount_details.isEmpty())
                addDiscountRecords(discount_type,discount_details);
        }
        notifyDataSetChanged();
    }
    public void clearGoods(){
        if (mOrderType != 1)mOrderType = 1;
        mDiscountRecords.fluentClear();
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
    public JSONArray getDiscountRecords(){
        return mDiscountRecords;
    }
    public void deleteMolDiscountRecord(){
        deleteDiscountRecord(DISCOUNT_TYPE.AUTO_MOL);
        deleteDiscountRecord(DISCOUNT_TYPE.M_MOL);
    }
    public void deleteVipDiscountRecord(){
         deleteDiscountRecord(DISCOUNT_TYPE.V_DISCOUNT);
    }
    public void deleteAlldiscountRecord(){
        deleteDiscountRecord(DISCOUNT_TYPE.A_DISCOUNT);
    }
    public String discountRecordsToString() {
        final StringBuilder stringBuilder = new StringBuilder();
        if (!mDiscountRecords.isEmpty()) {
            for (int i = 0, size = mDiscountRecords.size(); i < size; i++) {
                final JSONObject record = mDiscountRecords.getJSONObject(i);
                if (stringBuilder.length() > 0) stringBuilder.append(",");
                switch (record.getIntValue("discount_type")) {
                    case SaleGoodsViewAdapter.DISCOUNT_TYPE.MONEY_OFF:
                        stringBuilder.append("满减：");
                        break;
                    case SaleGoodsViewAdapter.DISCOUNT_TYPE.PRESENT:
                        stringBuilder.append("赠送：");
                        break;
                    case SaleGoodsViewAdapter.DISCOUNT_TYPE.M_DISCOUNT:
                        stringBuilder.append("手动折扣：");
                        break;
                    case SaleGoodsViewAdapter.DISCOUNT_TYPE.PROMOTION:
                        stringBuilder.append("促销：");
                        break;
                    case SaleGoodsViewAdapter.DISCOUNT_TYPE.V_DISCOUNT:
                        stringBuilder.append("会员折扣：");
                        break;
                    case SaleGoodsViewAdapter.DISCOUNT_TYPE.A_DISCOUNT:
                        stringBuilder.append("整单折扣：");
                        break;
                    case SaleGoodsViewAdapter.DISCOUNT_TYPE.AUTO_MOL:
                        stringBuilder.append("自动抹零：");
                        break;
                    case SaleGoodsViewAdapter.DISCOUNT_TYPE.M_MOL:
                        stringBuilder.append("手动抹零：");
                        break;
                }
                stringBuilder.append(String.format(Locale.CHINA, "%.2f", record.getDoubleValue("discount_money")));
            }
        }
        return stringBuilder.toString();
    }
    public void autoMol(double mol_amt){
        mol(mol_amt,DISCOUNT_TYPE.AUTO_MOL);
    }
    public void manualMol(double mol_amt){
        mol(mol_amt,DISCOUNT_TYPE.M_MOL);
    }

    public void updateGoodsInfoToVip(final JSONObject vip){
        double discount = 1.0,new_price = 0.0,original_price,discount_amt = 0.0,xnum = 0.0,current_discount_amt = 0.0,old_sale_amt  = 0.0,original_amt = 0.0,current_sale_amt = 0.0;
        if (vip != null){
            JSONObject jsonObject,discount_json;
            JSONArray discount_details = new JSONArray();
            for (int i = 0,length = mDatas.size();i < length;i++){
                jsonObject = mDatas.getJSONObject(i);
                xnum = Utils.getNotKeyAsDefault(jsonObject,"xnum",1.0);
                original_price = jsonObject.getDoubleValue("original_price");
                original_amt = Utils.formatDouble(xnum * original_price,2);
                old_sale_amt = jsonObject.getDoubleValue("sale_amt");

                switch (jsonObject.getIntValue("yh_mode")){
                    case 0://无优惠
                        discount  = 1.0;
                        new_price = original_price;
                        current_sale_amt = original_amt;
                        break;
                    case 1://会员价
                        new_price = Utils.formatDouble(jsonObject.getDoubleValue("yh_price"),4);
                        if (d_discount){
                            discount  = Utils.getNotKeyAsDefault(jsonObject,"discount",1.0);
                            new_price = Utils.formatDouble(new_price * discount,2);
                        }

                        current_sale_amt = Utils.formatDouble(xnum * new_price,2);

                        if (!Utils.equalDouble(original_amt,0.0))
                            discount  = Utils.formatDouble(current_sale_amt / original_amt,4);
                        break;
                    case 2://会员折扣
                        discount  = Utils.formatDouble(Utils.getNotKeyAsDefault(vip,"discount",1.0) / 10,2);
                        if (d_discount){
                            current_sale_amt = Utils.formatDouble(old_sale_amt * discount,2);
                        }else
                            current_sale_amt = Utils.formatDouble(original_amt * discount,2);
                        new_price = Utils.formatDouble(current_sale_amt / xnum,4);
                        break;
                }
                discount_amt = Utils.formatDouble(original_amt - current_sale_amt,2);

                jsonObject.put("discount_type",5);
                jsonObject.put("discount", discount);
                jsonObject.put("discount_amt", discount_amt);
                jsonObject.put("price",new_price);
                jsonObject.put("sale_amt",current_sale_amt);
                jsonObject.put("original_amt",original_amt);

                current_discount_amt = old_sale_amt - current_sale_amt;
                if (!Utils.equalDouble(current_discount_amt,0.0)){
                    discount_json = new JSONObject();
                    discount_json.put("barcode_id",jsonObject.getIntValue("barcode_id"));
                    discount_json.put("price",Utils.formatDouble(current_discount_amt,2));
                    discount_details.add(discount_json);
                }
            }
            //处理商品优惠明细
            if (!discount_details.isEmpty())
                addDiscountRecords(DISCOUNT_TYPE.V_DISCOUNT,discount_details);

            notifyDataSetChanged();
        }
    }
    public String generateSaleOrderCode(final String pos_num, int order_type){
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

    private void updateSaleGoodsInfo(double value,int type){//type 0 修改数量 1修改价格 2打折
        JSONObject json = getCurrentContent(),discount_json;
        double discount = 1.0,discount_amt = 0.0,original_price = 0.0,original_amt = 0.0,new_price = 0.0,xnum = 0.0,current_discount_amt = 0.0,old_sale_amt = 0.0,current_sale_amt = 0.0;
        int discount_type = 0;

        original_price = Utils.getNotKeyAsDefault(json,"original_price",1.0);
        original_amt = json.getDoubleValue("original_amt");

        JSONArray discount_details = null;
        switch (type){
            case 0:
                if (value <= 0){
                    deleteSaleGoods(getCurrentItemIndex(),0);
                }else{
                    xnum = value;
                    new_price = json.getDoubleValue("price");
                    original_amt = Utils.formatDouble(xnum * original_price,2);
                    current_sale_amt = Utils.formatDouble(xnum * new_price,2);

                    discount_amt = original_amt - current_sale_amt;

                    json.put("discount_amt", Utils.formatDouble(discount_amt,2));
                    json.put("xnum",Utils.formatDouble(xnum,3));
                    json.put("original_amt",original_amt);
                }
                break;
            case 1:
                discount_type = 4;
                discount_details = new JSONArray();

                old_sale_amt = json.getDoubleValue("sale_amt");
                xnum = json.getDoubleValue("xnum");
                new_price = value;
/*                if(d_discount){
                    original_price = Utils.getNotKeyAsDefault(json,"price",1.0);//改价都是从原始价开始算，不管有没有启用折上折
                }*/
                current_sale_amt = Utils.formatDouble(xnum * new_price,2);

                discount = Utils.formatDouble(current_sale_amt / original_amt,4);
                discount_amt = Utils.formatDouble(original_amt - current_sale_amt,2);

                json.put("discount", discount);
                json.put("discount_amt", discount_amt);
                json.put("price",Utils.formatDouble(new_price,4));
                break;
            case 2:
                if (Utils.equalDouble(value / 100,1.0)){
                    deleteDiscountRecord(DISCOUNT_TYPE.M_DISCOUNT);
                    return;
                }

                discount = Utils.formatDouble(value / 100,4);
                xnum = Utils.getNotKeyAsDefault(json,"xnum",1.0);
                old_sale_amt = json.getDoubleValue("sale_amt");

                discount_type = 4;
                discount_details = new JSONArray();
                if (d_discount){
                    current_sale_amt = Utils.formatDouble(old_sale_amt * discount,2);
                }else{
                    current_sale_amt = Utils.formatDouble(original_amt * discount,2);
                }
                new_price = Utils.formatDouble(current_sale_amt / xnum,4);
                discount_amt = Utils.formatDouble(original_amt - current_sale_amt,2);

                json.put("discount", discount);
                json.put("discount_amt", discount_amt);
                json.put("price",new_price);
                break;
        }
        json.put("discount_type",discount_type);
        json.put("sale_amt",current_sale_amt);


        current_discount_amt = Utils.formatDouble(old_sale_amt - current_sale_amt,2);

        if (null != discount_details && !Utils.equalDouble(current_discount_amt,0.0)){
            discount_json = new JSONObject();
            discount_json.put("gp_id",json.getIntValue("gp_id"));
            discount_json.put("barcode_id",json.getIntValue("barcode_id"));
            discount_json.put("price",current_discount_amt);
            discount_details.add(discount_json);

            //处理商品优惠明细
            addDiscountRecords(DISCOUNT_TYPE.M_DISCOUNT,discount_details);
        }
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
        int barcode_id = goods.getIntValue("barcode_id"),gp_id = goods.getIntValue("gp_id"),discount_type = 0;
        double  add_amount,add_goods_price,discount = 1.0,discount_amt = 0.0,new_price = 0.0,current_sale_amt = 0.0,
                saled_amount = 0.0,current_discount_amt = 0.0,original_price = 0.0,original_amt = 0.0;
        JSONObject tmp_obj;
        add_goods_price = Utils.getNotKeyAsDefault(goods,"price",1.0);
        for (int i = 0,length = mDatas.size();i < length;i++){
            tmp_obj = mDatas.getJSONObject(i);
            if (barcode_id == tmp_obj.getIntValue("barcode_id") && gp_id == tmp_obj.getIntValue("gp_id")){
                exist = true;
                original_price = tmp_obj.getDoubleValue("original_price");

                saled_amount = tmp_obj.getDoubleValue("sale_amt");
                discount  = Utils.getNotKeyAsDefault(tmp_obj,"discount",1.0);
                new_price = Utils.formatDouble(add_goods_price * discount,4);

                num += tmp_obj.getDoubleValue("xnum");
                if (isBarcodeWeighingGoods){
                    add_amount = goods.getDoubleValue("sale_amt");
                }else
                    add_amount = Utils.formatDouble(num * new_price,2);

                current_sale_amt = Utils.formatDouble(saled_amount + add_amount,2);
                original_amt = Utils.formatDouble(original_price * num,2);
                discount_amt = current_discount_amt = Utils.formatDouble(original_amt - current_sale_amt,2);

                tmp_obj.put("price", new_price);
                tmp_obj.put("discount", discount);
                tmp_obj.put("discount_amt", discount_amt);
                tmp_obj.put("xnum",Utils.formatDouble(num,4));
                tmp_obj.put("sale_amt",current_sale_amt);
                tmp_obj.put("original_amt",original_amt);
                mCurrentItemIndex = i;

                discount_type = tmp_obj.getIntValue("discount_type");
                break;
            }
        }

        if (!exist){

            if (isBarcodeWeighingGoods){
                original_amt = goods.getDoubleValue("sale_amt");
            }else
                original_amt = Utils.formatDouble(add_goods_price * num,2);

            if (vip != null){
                goods.put("card_code",vip.getString("card_code"));
                goods.put("name",vip.getString("name"));
                goods.put("mobile",vip.getString("mobile"));
                switch (goods.getIntValue("yh_mode")){
                    case 0://无优惠
                        discount  = 1.0;
                        new_price = add_goods_price;
                        current_sale_amt = original_amt;
                        break;
                    case 1://会员价
                        new_price = Utils.formatDouble(goods.getDoubleValue("yh_price"),4);
                        current_sale_amt = Utils.formatDouble(new_price * num,2);
                        if (!Utils.equalDouble(original_amt,0)){
                            discount  = Utils.formatDouble(current_sale_amt / original_amt,4);
                        }
                        break;
                    case 2://会员折扣
                        discount  = Utils.formatDouble(Utils.getNotKeyAsDefault(vip,"discount",10.0) / 10,2);
                        current_sale_amt = Utils.formatDouble(original_amt * discount,2);
                        if (!Utils.equalDouble(num,0)){
                            new_price = Utils.formatDouble(current_sale_amt / num,4);
                        }
                        break;
                }
                discount_type = DISCOUNT_TYPE.V_DISCOUNT;
                goods.put("discount_type", discount_type);
            }else {
                new_price = add_goods_price;
                current_sale_amt = original_amt;
            }

            discount_amt = current_discount_amt = Utils.formatDouble(original_amt - current_sale_amt,2);

            goods.put("original_price", add_goods_price);
            goods.put("price",new_price);
            goods.put("discount", discount);
            goods.put("discount_amt", discount_amt);
            goods.put("xnum",num);
            goods.put("sale_amt",current_sale_amt);
            goods.put("original_amt",original_amt);
            mDatas.add(goods);
            mCurrentItemIndex = mDatas.size() - 1;
        }
        //处理优惠记录
        if (!Utils.equalDouble(current_discount_amt,0.0)){
            JSONArray discount_details = new JSONArray();
            JSONObject discount_json = new JSONObject();
            discount_json.put("gp_id",goods.getIntValue("gp_id"));
            discount_json.put("barcode_id",goods.getIntValue("barcode_id"));
            discount_json.put("price",current_discount_amt);
            discount_details.add(discount_json);

            //处理商品优惠明细
            addDiscountRecords(discount_type,discount_details);
        }
        this.notifyDataSetChanged();
    }
    private void addDiscountRecords(int type,final JSONArray new_details){
        boolean isDiscountTypeExist = false;
        JSONObject record_json,original_goods,new_goods;
        int ori_id,new_id;
        double new_discount_amt = 0.0;
        for (Object record_obj : mDiscountRecords){
            if (record_obj instanceof JSONObject){
                record_json = (JSONObject)record_obj;
                final JSONArray original_details = JSON.parseArray(record_json.getString("details"));
                double original_discount_amt = record_json.getDoubleValue("discount_money");
                int original_type = record_json.getIntValue("discount_type");
                if (original_type == type){
                    isDiscountTypeExist = true;
                    int j = 0;
                    for (int i = 0,size = new_details.size();i < size;i++){
                        new_goods = new_details.getJSONObject(i);
                        new_id = getGoodsId(new_goods);
                        j = 0;
                        new_discount_amt = new_goods.getDoubleValue("price");

                        for (int length = original_details.size();j < length ;j++){
                            original_goods = original_details.getJSONObject(j);
                            ori_id = getGoodsId(original_goods);
                            if (new_id == ori_id){
                                new_goods.put("price",Utils.formatDouble(original_goods.getDoubleValue("price") + new_discount_amt,2));
                                original_details.remove(j);
                                break;
                            }
                        }
                        original_discount_amt += new_discount_amt;

                        original_details.add(new_goods);
                    }
                    record_json.put("discount_money",Utils.formatDouble(original_discount_amt,2));
                    record_json.put("details",original_details.toJSONString());
                }
            }
        }
        if (!isDiscountTypeExist){
            JSONObject record = new JSONObject();
            for (int i = 0,size = new_details.size();i < size;i++){
                new_goods = new_details.getJSONObject(i);
                new_discount_amt += new_goods.getDoubleValue("price");
            }

            Logger.d("new_discount_amt:%f",new_discount_amt);

            record.put("discount_type",type);
            record.put("type",mOrderType);
            record.put("relevant_id","");
            record.put("discount_money",Utils.formatDouble(new_discount_amt,2));
            record.put("details",new_details.toJSONString());
            mDiscountRecords.add(record);
        }
    }
    private void deleteDiscountRecord(int discount_type){
        JSONObject record_json,discount_goods,goods;
        for (int i = 0;i < mDiscountRecords.size();i++){
            record_json = mDiscountRecords.getJSONObject(i);
            if (discount_type == record_json.getIntValue("discount_type")){
                record_json = (JSONObject) mDiscountRecords.remove(i);
                final JSONArray details = JSON.parseArray(Utils.getNullOrEmptyStringAsDefault(record_json,"details","[]"));
                if (details.isEmpty())continue;
                for (int j = 0,length = details.size();j < length;j++){
                    discount_goods = details.getJSONObject(j);
                    for (int k = 0,size = mDatas.size();k < size;k++){
                        goods = mDatas.getJSONObject(k);
                        if (getGoodsId(discount_goods) == getGoodsId(goods)){
                            double discount_money = discount_goods.getDoubleValue("price");
                            double sale_amt = goods.getDoubleValue("sale_amt") + discount_money;
                            double xnum =Utils.getNotKeyAsDefault(goods,"xnum",1.0);
                            double price = sale_amt / xnum;
                            goods.put("price",price);
                            goods.put("sale_amt",Utils.formatDouble(sale_amt,2));
                            goods.put("discount",price / Utils.getNotKeyAsDefault(goods,"original_price",1.0));
                            goods.put("discount_amt",Utils.formatDouble(goods.getDoubleValue("discount_amt") - discount_money,2));
                        }
                    }
                }
            }
        }
        notifyDataSetChanged();
    }

    private int getGoodsId(final JSONObject jsonObject){
        int id;
        if (jsonObject == null)return -1;
        id = jsonObject.getIntValue("barcode_id");
        if (-1 == id){//组合商品
            id = jsonObject.getIntValue("gp_id");
        }
        return id;
    }

    private double getPerRecordMolAmt(double mol_amt,int sale_record){
        if (!Utils.equalDouble(mol_amt,0.0) && sale_record != 0){
            double per_record_mol_amt = Utils.formatDouble(mol_amt / sale_record,2);//保留到分
            Logger.d("per_record_mol_amt:%f",per_record_mol_amt);
            if (Math.abs(per_record_mol_amt) < 0.01){
                Logger.d("sale_record:%d",sale_record);
                per_record_mol_amt = getPerRecordMolAmt(mol_amt,sale_record / 2);
            }
            Logger.d("per_record_mol_amt:%f",per_record_mol_amt);
            return per_record_mol_amt;
        }
        return 0.0;
    }

    private void mol(double mol_amt,int type){
        Logger.d("mol_amt:%f,type:%d",mol_amt,type);

        JSONObject object,discount_obj;
        final JSONArray discount_details = new JSONArray();
        int sale_record = mDatas.size();
        double per_record_mol_amt = getPerRecordMolAmt(mol_amt,sale_record),original_sale_amt = 0.0,new_discount = 0.0,xnum = 0.0,new_price = 0.0,
                discount_amt = 0.0,current_sale_amt = 0.0;
        boolean isPreform = true;

        Utils.sortJsonArrayFromDoubleCol(mDatas,"sale_amt");

        for (int i = 0;i < sale_record && isPreform;i++){
            object = mDatas.getJSONObject(i);
            original_sale_amt = object.getDoubleValue("sale_amt");
            current_sale_amt = Utils.formatDouble(original_sale_amt - per_record_mol_amt,2);

            if (current_sale_amt > 0.0){
                new_discount = Utils.formatDouble(current_sale_amt / original_sale_amt,3);

                //处理优惠记录
                discount_obj = new JSONObject();
                discount_obj.put("gp_id",object.getIntValue("gp_id"));
                discount_obj.put("barcode_id",object.getIntValue("barcode_id"));
                discount_obj.put("price",per_record_mol_amt);//单品折扣金额
                discount_details.add(discount_obj);

                xnum = Utils.getNotKeyAsDefault(object,"xnum",1.0);

                new_price = Utils.formatDouble(current_sale_amt / xnum,4);

                discount_amt = original_sale_amt - current_sale_amt;

                object.put("discount", new_discount);
                object.put("discount_amt", Utils.formatDouble(discount_amt + object.getDoubleValue("discount_amt"),2));
                object.put("price",new_price);
                object.put("discount_type",type);
                object.put("sale_amt",current_sale_amt);

                if (mol_amt == 0){
                    isPreform = false;
                }else {
                    mol_amt -= per_record_mol_amt;
                    //计算下一次抹零金额，最后一条记录或者剩余抹零金额小于平均抹零金额；要扣除剩余的抹零
                    if (mol_amt - per_record_mol_amt < 0.0 || i + 2 == sale_record){
                        per_record_mol_amt = mol_amt;
                        mol_amt = 0;
                    }
                }
            }
        }
        if (!discount_details.isEmpty()){
            addDiscountRecords(type,discount_details);
        }
        notifyDataSetChanged();
    }


    public boolean splitCombinationalGoods(final JSONArray arrays,int gp_id,double gp_price,double gp_num,StringBuilder err){
        final String sql = "select b.xnum xnum,(b.xnum * b.retail_price / a.amt) * " + gp_price +" / case b.xnum when 0 then 1 else b.xnum end price," +
                "b.barcode_id barcode_id,b.barcode barcode,b.conversion conversion,b.gp_id gp_id,b.tc_rate tc_rate,b.tc_mode tc_mode,b.tax_rate tax_rate,b.ps_price ps_price,b.cost_price cost_price\n" +
                ",b.trade_price trade_price,b.retail_price retail_price,b.buying_price buying_price from vi_goods_group_info b inner join \n" +
                "(select case sum(xnum * retail_price) when 0 then 1 else sum(xnum * retail_price) end amt,gp_id \n" +
                "from vi_goods_group_info group by gp_id) a on a.gp_id = b.gp_id where b.gp_id =" +  gp_id +" group by barcode_id;";

        Logger.d("拆分组合商品：%s",sql);

        JSONArray tmps;
        JSONObject tmp_obj;
        while (gp_num-- != 0){
            if (null != (tmps = SQLiteHelper.getListToJson(sql,err))){
                for (int k = 0,length = tmps.size();k < length;k++){
                    tmp_obj = tmps.getJSONObject(k);
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
