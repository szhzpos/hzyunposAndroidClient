package com.wyc.cloudapp.adapter;

import android.app.Dialog;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.adapter.bean.FullReduceRule;
import com.wyc.cloudapp.adapter.bean.PromotionRule;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.ChangeNumOrPriceDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.FontSizeTagHandler;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class AbstractSaleGoodsAdapter extends AbstractDataAdapter<AbstractSaleGoodsAdapter.MyViewHolder> {
    protected SaleActivity mContext;
    protected View mCurrentItemView;
    protected int mCurrentItemIndex;

    private final JSONArray mDiscountRecords;
    private int mOrderType = 1;//订单类型 1线下 2线上
    private boolean mSingleRefundStatus = false,d_discount = false;//d_discount是否折上折
    private JSONObject mStepFullReduceRecord;

    private double mTotalSaleAmt;
    private double mTotalDiscountAmt;
    private double mTotalOriginalAmt;
    private OnDataChange mDataListener;

    private JSONArray mFullReduceRuleGroup;

    public AbstractSaleGoodsAdapter(final SaleActivity context){
        mContext = context;
        mDatas = new JSONArray();
        mDiscountRecords = new JSONArray();

        registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged(){
                final JSONArray datas = mDatas;
                double n1 = 0.0,n2 = 0.0,n3 = 0.0,n4 = 0.0;
                for (int i = 0,length = datas.size();i < length;i ++){
                    final JSONObject jsonObject = datas.getJSONObject(i);
                    n1 += jsonObject.getDouble("xnum");
                    n2 += jsonObject.getDouble("sale_amt");
                    n3 += jsonObject.getDouble("discount_amt");
                    n4 += jsonObject.getDouble("original_amt");
                }
                mTotalSaleAmt = n2;
                mTotalDiscountAmt = n3;
                mTotalOriginalAmt = n4;
                if (mDataListener != null)mDataListener.onChange(n1,n2,n3);
            }
        });
    }

    protected final static class DISCOUNT_TYPE {
        /*             1 => '满减',
                             2 => '赠送',
                             3 => '促销或折扣',
                             4 => '手动折扣',
                             5 => '会员折扣',
                             6 => '整单折扣',
                             7 => '自动抹零',
                             8 => '手动抹零',*/
        static final int FULL_REDUCE = 1,PRESENT = 2,PROMOTION = 3,M_DISCOUNT = 4,V_DISCOUNT = 5,A_DISCOUNT = 6,AUTO_MOL =7,M_MOL = 8;
    }

    protected static class MyViewHolder extends AbstractDataAdapter.SuperViewHolder {
        TextView row_id,gp_id,goods_id,goods_title,unit_name,barcode_id,barcode,sale_price,sale_num,sale_amt,discount_sign,original_price;
        MyViewHolder(View itemView) {
            super(itemView);
            row_id = itemView.findViewById(R.id.row_id);
            goods_id = itemView.findViewById(R.id.goods_id);
            gp_id = itemView.findViewById(R.id.gp_id);
            goods_title =  itemView.findViewById(R.id.goods_title);
            unit_name =  itemView.findViewById(R.id.unit_name);
            barcode_id =  itemView.findViewById(R.id.barcode_id);
            barcode =  itemView.findViewById(R.id.barcode);
            discount_sign = itemView.findViewById(R.id.discount_sign);
            sale_price =  itemView.findViewById(R.id.sale_price);
            sale_num = itemView.findViewById(R.id.sale_num);
            sale_amt = itemView.findViewById(R.id.sale_amt);
            original_price = itemView.findViewById(R.id.original_price);
        }
    }

    @NonNull
    @Override
    public AbstractSaleGoodsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View itemView = View.inflate(mContext, R.layout.normal_sale_goods_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.sale_goods_height)));
        return new AbstractSaleGoodsAdapter.MyViewHolder(itemView);
    }
    @CallSuper
    @Override
    public void onBindViewHolder(@NonNull AbstractSaleGoodsAdapter.MyViewHolder myViewHolder, int i) {
        final JSONObject goods_info = mDatas.getJSONObject(i);
        if (goods_info != null){
            myViewHolder.discount_sign.setText(getDiscountNames(getGoodsLastDiscountTypes(goods_info,new int[]{DISCOUNT_TYPE.PRESENT,DISCOUNT_TYPE.PROMOTION})));
            myViewHolder.discount_sign.setTag(goods_info.getIntValue("sale_type"));

            myViewHolder.row_id.setText(String.format(Locale.CHINA,"%s%s",i + 1,"、"));
            myViewHolder.goods_id.setText(goods_info.getString("goods_id"));
            myViewHolder.gp_id.setText(goods_info.getString("gp_id"));

            if (myViewHolder.unit_name != null) {
                myViewHolder.goods_title.setText(goods_info.getString("goods_title"));
                myViewHolder.unit_name.setText(goods_info.getString("unit_name"));
            }else{
                myViewHolder.goods_title.setText(String.format(Locale.CHINA,"%s(%s)",goods_info.getString("goods_title"),goods_info.getString("unit_name")));
            }

            myViewHolder.barcode_id.setText(goods_info.getString("barcode_id"));
            myViewHolder.barcode.setText(goods_info.getString("barcode"));
            myViewHolder.original_price.setText(String.format(Locale.CHINA,"%.2f",goods_info.getDoubleValue("original_price")));
            myViewHolder.sale_price.setText(String.format(Locale.CHINA,"%.2f",goods_info.getDoubleValue("price")));
            myViewHolder.sale_num.setText(String.format(Locale.CHINA,"%.3f",goods_info.getDoubleValue("xnum")));
            myViewHolder.sale_amt.setText(String.format(Locale.CHINA,"%.2f",goods_info.getDoubleValue("sale_amt")));

            if(myViewHolder.goods_title.getCurrentTextColor() == mContext.getResources().getColor(R.color.blue,null)){
                myViewHolder.goods_title.setTextColor(mContext.getColor(R.color.black));//需要重新设置颜色；不然重用之后内容颜色为重用之前的。
            }

            if (mCurrentItemIndex == i){
                setSelectStatus(myViewHolder.itemView);
            }
        }
    }


    public interface OnDataChange{
        void onChange(double num,double sale_amt,double discount_amt);
    }
    public void setDataListener(final OnDataChange listener){
        mDataListener = listener;
    }

    protected boolean isScalesWeighingGoods(final JSONObject object){
        return object != null && Utils.getNullStringAsEmpty(object,GoodsInfoViewAdapter.W_G_MARK).isEmpty() && (object.getIntValue("type") == 2);
    }
    protected double getScalesWeighingGoodsSumNum(){
        double num = 0.0;
        for (int i = 0,size = mDatas.size();i < size;i++){
            final JSONObject object = mDatas.getJSONObject(i);
            if (object != getCurrentContent() && isScalesWeighingGoods(object))num += object.getDoubleValue("xnum");
        }
        return num;
    }

    public void addSaleGoods(final JSONObject goods){
        addSaleGoods(goods,1,false);
    };

    protected void addSaleGoods(@NonNull JSONObject goods,double num,boolean isBarcodeWeighingGoods){
        final JSONObject copy = verifyPromotion(goods,num);
        if (copy != null){
            double diff_xnum = copy.getDoubleValue("xnum");
            addSaleGoods(copy,diff_xnum,isBarcodeWeighingGoods);//拆分超过促销数量的商品
            num -= diff_xnum;
            if (Utils.equalDouble(num,0.0))return;
        }
        mContext.clearSearchEt();

        int barcode_id = goods.getIntValue("barcode_id"),gp_id = goods.getIntValue("gp_id"),discount_type = -1,sale_type = goods.getIntValue("sale_type");
        double  sale_price,discount = 1.0,discount_amt = 0.0,new_price = 0.0,current_sale_amt = 0.0,current_discount_amt = 0.0,original_price = 0.0,original_amt = 0.0,sum_xnum = 0.0;
        boolean exist = false,isPromotion = (sale_type == GoodsInfoViewAdapter.SALE_TYPE.SPECIAL_PROMOTION);
        JSONObject tmp_obj;

        for (int i = 0,length = mDatas.size();i < length;i++){
            tmp_obj = mDatas.getJSONObject(i);

            if (barcode_id == tmp_obj.getIntValue("barcode_id") && gp_id == tmp_obj.getIntValue("gp_id") && sale_type == tmp_obj.getIntValue("sale_type")){
                exist = true;

                original_price = tmp_obj.getDoubleValue("original_price");
                sum_xnum = tmp_obj.getDoubleValue("xnum") + num;

                if (isPromotion){
                    new_price = goods.getDoubleValue("price");
                    tmp_obj.put("price",new_price);
                }else {
                    new_price = tmp_obj.getDoubleValue("price");
                }

                original_amt = original_price * sum_xnum;
                current_sale_amt = sum_xnum * new_price;

                discount_amt = current_discount_amt = original_amt - current_sale_amt;

                Logger.d("new_price:%f,original_price:%f,current_sale_amt:%f,original_amt:%f,current_discount_amt:%f,sum_xnum:%f",new_price,original_price,current_sale_amt,original_amt,current_discount_amt,sum_xnum);

                tmp_obj.put("discount_amt", discount_amt);
                tmp_obj.put("xnum",Utils.formatDouble(sum_xnum,4));
                tmp_obj.put("sale_amt",Utils.formatDouble(current_sale_amt,4));
                tmp_obj.put("original_amt",Utils.formatDouble(original_amt,4));
                mCurrentItemIndex = i;
                discount_type = getGoodsLastDiscountType(tmp_obj);
                goods = tmp_obj;
                break;
            }
        }

        if (!exist){
            original_price = goods.getDoubleValue("retail_price");
            sale_price = Utils.getNotKeyAsNumberDefault(goods,"price",1.0);

            if (isPromotion){//促销商品
                if (!Utils.equalDouble(original_price,0.0))discount = sale_price / original_price;
                discount_type = DISCOUNT_TYPE.PROMOTION;
            }
            if (isBarcodeWeighingGoods){
                original_amt = goods.getDoubleValue("sale_amt");
            }else{
                original_amt = Utils.formatDouble(original_price * num,4);
            }
            new_price = sale_price;

            final JSONObject vip = mContext.getVipInfo();
            if (!isPromotion && vip != null){//暂定促销商品不参与其他优惠活动
                goods.put("card_code",vip.getString("card_code"));
                goods.put("name",vip.getString("name"));
                goods.put("mobile",vip.getString("mobile"));
                switch (goods.getIntValue("yh_mode")){
                    case 0://无优惠
                        discount  = 1.0;
                        new_price = sale_price;
                        current_sale_amt = original_amt * discount;;
                        break;
                    case 1://会员价
                        new_price = Utils.formatDouble(goods.getDoubleValue("yh_price"),4);
                        current_sale_amt = new_price * num;
                        if (!Utils.equalDouble(original_amt,0)){
                            discount  = current_sale_amt / original_amt;
                        }
                        break;
                    case 2://会员折扣
                        if (d_discount){
                            discount  *= Utils.getNotKeyAsNumberDefault(vip,"discount",10.0) / 10;
                        }else {
                            discount  = Utils.getNotKeyAsNumberDefault(vip,"discount",10.0) / 10;
                        }
                        current_sale_amt = original_amt * discount;

                        if (!Utils.equalDouble(num,0)){
                            new_price = Utils.formatDouble(current_sale_amt / num,4);
                        }
                        break;
                }
                discount_type = AbstractSaleGoodsAdapter.DISCOUNT_TYPE.V_DISCOUNT;
            }else {
                current_sale_amt = original_amt * discount;
            }

            discount_amt = current_discount_amt = original_amt - current_sale_amt;

            goods.put("original_price",original_price);
            goods.put("price",new_price);
            goods.put("discount", Utils.formatDouble(discount,4));
            goods.put("discount_amt", discount_amt);
            goods.put("xnum",num);
            goods.put("sale_amt",Utils.formatDouble(current_sale_amt,4));
            goods.put("original_amt",original_amt);

            mDatas.add(goods);
            mCurrentItemIndex = mDatas.size() - 1;
        }
        //处理优惠记录
        if (!Utils.equalDouble(current_discount_amt,0.0) && discount_type != -1){
            final JSONObject discount_json = new JSONObject();
            discount_json.put("gp_id",goods.getIntValue("gp_id"));
            discount_json.put("barcode_id",goods.getIntValue("barcode_id"));
            discount_json.put("discount_type",discount_type);
            discount_json.put("sale_type",goods.getIntValue("sale_type"));
            discount_json.put("price",current_discount_amt);

            //处理商品优惠明细
            addDiscountRecord(goods,discount_json);
        }

        this.notifyDataSetChanged();
    }

    private int getGoodsLastDiscountType(final JSONObject object){
        final JSONArray array = Utils.getNullObjectAsEmptyJsonArray(object,"discount_records");
        if (array.isEmpty())return -1;
        return Utils.getNotKeyAsNumberDefault(array.getJSONObject(array.size() -1),"discount_type",-1);
    }

    private void addDiscountRecord(final JSONObject goods,final JSONObject _record){
        final JSONArray discount_records = Utils.getNullObjectAsEmptyJsonArray(goods,"discount_records");
        int discount_type = Utils.getNotKeyAsNumberDefault(_record,"discount_type",-1);
        JSONObject row;

        boolean exist = false;
        for (int i = 0,size = discount_records.size(); i < size;i ++){
            row = discount_records.getJSONObject(i);
            if (Utils.getNotKeyAsNumberDefault(row,"discount_type",-1) == discount_type){
                row.put("price",_record.getDoubleValue("price"));
                exist = true;
                break;
            }
        }
        if (!exist){
            discount_records.add(_record);
        }
        goods.put("discount_records",discount_records);
    }

    private JSONObject verifyPromotion(final @NonNull JSONObject object,double num){
        JSONObject tmp_obj;
        int sale_type = object.getIntValue("sale_type");
        if (sale_type == GoodsInfoViewAdapter.SALE_TYPE.SPECIAL_PROMOTION){

            int barcode_id = object.getIntValue("barcode_id"),gp_id = object.getIntValue("gp_id");
            double sum_xnum = 0.0,diff_xnum = 0.0,new_price = 0.0;
            for (int i = 0,length = mDatas.size();i < length;i++) {
                tmp_obj = mDatas.getJSONObject(i);
                if (sale_type == tmp_obj.getIntValue("sale_type") && barcode_id == tmp_obj.getIntValue("barcode_id") && gp_id == tmp_obj.getIntValue("gp_id")){
                    sum_xnum  += tmp_obj.getDoubleValue("xnum");
                }
            }
            sum_xnum += num;


            final JSONArray promotion_rules = Utils.getNullObjectAsEmptyJsonArray(object,"promotion_rules");

            JSONObject rule,satisfy_rule;
            boolean isSatisfy  =false;
            double upper_limit_num = 0.0,lower_limit_num = 0.0;
            final JSONArray satisfy_rules = new JSONArray();
            for (int i = 0,size = promotion_rules.size();i < size;i ++){
                 rule = promotion_rules.getJSONObject(i);
                isSatisfy  =false;
                 if (Utils.notLessDouble(sum_xnum,lower_limit_num = rule.getDoubleValue("xnum_one")) && !Utils.equalDouble(lower_limit_num,0.0)){
                     new_price = rule.getDoubleValue("promotion_price_one");
                     upper_limit_num = rule.getDoubleValue("limit_xnum_one");
                     isSatisfy = true;
                 }else if (Utils.notLessDouble(sum_xnum,lower_limit_num = rule.getDoubleValue("xnum_two")) && !Utils.equalDouble(lower_limit_num,0.0)){
                     new_price = rule.getDoubleValue("promotion_price_two");
                     upper_limit_num = rule.getDoubleValue("limit_xnum_two");
                     isSatisfy = true;
                 }else if (Utils.notLessDouble(sum_xnum,lower_limit_num = rule.getDoubleValue("xnum_three")) && !Utils.equalDouble(lower_limit_num,0.0)){
                     new_price = rule.getDoubleValue("promotion_price_three");
                     upper_limit_num = rule.getDoubleValue("limit_xnum_three");
                     isSatisfy = true;
                 }else if (Utils.notLessDouble(sum_xnum,lower_limit_num = rule.getDoubleValue("xnum_four")) && !Utils.equalDouble(lower_limit_num,0.0)){
                     new_price = rule.getDoubleValue("promotion_price_four");
                     upper_limit_num = rule.getDoubleValue("limit_xnum_four");
                     isSatisfy = true;
                 }else if (Utils.notLessDouble(sum_xnum,lower_limit_num = rule.getDoubleValue("xnum_five")) && !Utils.equalDouble(lower_limit_num,0.0)){
                     new_price = rule.getDoubleValue("promotion_price_five");
                     upper_limit_num = rule.getDoubleValue("limit_xnum_five");
                     isSatisfy = true;
                 }

                 if (isSatisfy){
                     satisfy_rule = new JSONObject();
                     satisfy_rule.put("tlp_id",rule.getIntValue("tlp_id"));
                     satisfy_rule.put("lower_limit_num",lower_limit_num);
                     satisfy_rule.put("upper_limit_num",upper_limit_num);
                     satisfy_rule.put("price",new_price);
                     satisfy_rules.add(satisfy_rule);
                 }
            }

            Logger.d_json(satisfy_rules.toString());
            if (satisfy_rules.isEmpty()){
                object.put("sale_type",GoodsInfoViewAdapter.SALE_TYPE.COMMON);
            }else {
                List<PromotionRule> lists = satisfy_rules.toJavaList(PromotionRule.class);
                Collections.sort(lists);
                Logger.d(lists.toString());

                PromotionRule promotionRule = lists.get(0);
                double limit_xnum = promotionRule.getUpper_limit_num();
                new_price = promotionRule.getPrice();

                if (Utils.equalDouble(limit_xnum,0.0)){
                    object.put("price",new_price);
                }else if ((diff_xnum = sum_xnum  - limit_xnum) > 0){
                    final JSONObject copy = Utils.JsondeepCopy(object);
                    double ori_price = copy.getDoubleValue("retail_price");
                    copy.put("sale_type",GoodsInfoViewAdapter.SALE_TYPE.COMMON);
                    copy.put("price",ori_price);
                    copy.put("xnum",diff_xnum);
                    copy.put("sale_amt",diff_xnum * ori_price);
                    if (AbstractSaleGoodsAdapter.DISCOUNT_TYPE.PROMOTION == copy.getIntValue("discount_type")){
                        copy.remove("discount_type");
                    }
                    Logger.d("diff_xnum:%f",diff_xnum);

                    return copy;
                }else {
                    object.put("price",new_price);
                }
            }
        }

        return null;
    }

    public void deleteSaleGoods(int index,double num){
        int size = mDatas.size();
        if (0 <= index && index < size){
            if (num == 0){//等于0删除整条记录
                if (verifyDeletePermissions()){
                    mDatas.remove(index);
                    if (mCurrentItemIndex == index){//如果删除的是当前选择的item则重置当前index以及View
                        if (index == size - 1){
                            mCurrentItemIndex--;
                        }else{
                            mCurrentItemIndex =  mDatas.size() - 1;
                        }
                        mCurrentItemView = null;
                    }
                }else
                    return;
            }else{
                final JSONObject jsonObject = mDatas.getJSONObject(index);
                double current_num = jsonObject.getDoubleValue("xnum"),price = jsonObject.getDoubleValue("price");
                if ((current_num = current_num - num) <= 0){
                    if (verifyDeletePermissions()){
                        mDatas.remove(index);
                        if (index == size - 1){
                            mCurrentItemIndex--;
                        }else{
                            mCurrentItemIndex = mDatas.size() - 1;
                        }
                        mCurrentItemView = null;
                    }else
                        return;
                }else{
                    _deleteDiscountRecordForGoods(jsonObject,num);

                    if (GoodsInfoViewAdapter.isPromotion(jsonObject)){
                        verifyPromotion(jsonObject,-1);
                        price = jsonObject.getDoubleValue("price");
                    }

                    Logger.d("price:%f",price);
                    jsonObject.put("xnum",current_num);
                    jsonObject.put("sale_amt",Utils.formatDouble(current_num * price,4));
                    jsonObject.put("original_amt",Utils.formatDouble(current_num * jsonObject.getDoubleValue("original_price"),2));
                }
            }
            notifyDataSetChanged();
        }
    }
    public void updateSaleGoodsDialog(final short type){//type 0 修改数量 1修改价格 2打折
        final JSONObject cur_json = getCurrentContent();
        if (!cur_json.isEmpty()){
            ChangeNumOrPriceDialog dialog = null;
            switch (type){
                case 1:
                    dialog = new ChangeNumOrPriceDialog(mContext,"新价格",cur_json.getString("price"));
                    break;
                case 2:
                    dialog = new ChangeNumOrPriceDialog(mContext, Html.fromHtml("折扣率<size value='14'>[1-10],10为不折扣</size> ",null,new FontSizeTagHandler(mContext)),
                            String.format(Locale.CHINA,"%.2f",Utils.getNotKeyAsNumberDefault(cur_json,"discount",1.0) * 10),1.0,10.0);
                    break;
                case 0:
                    dialog = new ChangeNumOrPriceDialog(mContext,"新数量",String.format(Locale.CHINA,"%.2f",Utils.getNotKeyAsNumberDefault(cur_json,"xnum",1.0)));
                    break;
            }
            if (dialog != null)
                dialog.setYesOnclickListener(myDialog -> {
                    double content = myDialog.getContent();
                    if (!verifyDiscountPermissions(content,type))return;

                    updateSaleGoodsInfo(content,type);
                    myDialog.dismiss();
                }).setNoOnclickListener(Dialog::dismiss).show();
        }else{
            MyDialog.ToastMessage("请选择需要修改的商品!",mContext,null);
        }
    }
    protected void updateSaleGoodsInfo(double value,int type){//type 0 修改数量 1修改价格 2打折 3赠送
        final JSONObject json = getCurrentContent();
        double  discount_amt = 0.0,original_price = 0.0,original_amt = 0.0,new_price = 0.0,xnum = 0.0,current_discount_amt = 0.0,current_sale_amt = 0.0,new_discount = 1.0;
        int discount_type = -1;

        original_price = Utils.getNotKeyAsNumberDefault(json,"original_price",1.0);

        switch (type){
            case 0:
                if (value <= 0){
                    deleteSaleGoods(getCurrentItemIndex(),value);
                }else{
                    double ori_xnum = json.getDoubleValue("xnum");
                    final JSONObject copy_obj = verifyPromotion(json,value - ori_xnum);
                    if (copy_obj != null){
                        double diff_xnum = copy_obj.getDoubleValue("xnum");
                        addSaleGoods(copy_obj,diff_xnum,!Utils.getNullStringAsEmpty(copy_obj,GoodsInfoViewAdapter.W_G_MARK).isEmpty());
                        xnum = value - diff_xnum;
                    }else
                        xnum = value;

                    new_price = json.getDoubleValue("price");
                    original_amt = Utils.formatDouble(xnum * original_price,4);
                    current_sale_amt = xnum * new_price;

                    current_discount_amt = discount_amt = original_amt - current_sale_amt;

                    discount_type = getGoodsLastDiscountType(json);

                    json.put("discount_amt", Utils.formatDouble(discount_amt,2));
                    json.put("xnum",Utils.formatDouble(xnum,3));
                    json.put("original_amt",original_amt);
                }
                break;
            case 1:
            case 3://赠送
                new_price = value;
                if (type == 3){
                    discount_type = DISCOUNT_TYPE.PRESENT;
                    new_discount = 0.0;
                }else{
                    if (isNotParticipateDiscount(json))return;
                    if (!Utils.equalDouble(original_price,0.0))new_discount = value / original_price;
                    discount_type = AbstractSaleGoodsAdapter.DISCOUNT_TYPE.M_DISCOUNT;
                }

                if(!d_discount){
                    _deleteDiscountRecordForGoods(json,0);
                }
                xnum = json.getDoubleValue("xnum");
                original_amt = xnum * original_price;

                current_sale_amt = xnum * new_price;

                current_discount_amt = discount_amt = Utils.formatDouble(original_amt - current_sale_amt,4);

                json.put("discount", new_discount);
                json.put("discount_amt", discount_amt);
                json.put("price",Utils.formatDouble(new_price,4));
                break;

            case 2://手动不处理折上折。每次打折都是从原价的基础上折
                if (isNotParticipateDiscount(json))return;

                if (Utils.equalDouble(value / 10,1.0)){
                    _deleteDiscountRecordForType(AbstractSaleGoodsAdapter.DISCOUNT_TYPE.M_DISCOUNT);
                    _deleteDiscountRecordForType(AbstractSaleGoodsAdapter.DISCOUNT_TYPE.A_DISCOUNT);
                    _deleteDiscountRecordForType(AbstractSaleGoodsAdapter.DISCOUNT_TYPE.PRESENT);
                    return;
                }

                new_discount = Utils.formatDouble(value / 10,4);

                xnum = Utils.getNotKeyAsNumberDefault(json,"xnum",1.0);
                original_amt = Utils.formatDouble(xnum * original_price,4);

                discount_type = AbstractSaleGoodsAdapter.DISCOUNT_TYPE.M_DISCOUNT;

                if (!d_discount){
                    _deleteDiscountRecordForGoods(json,0);
                }

                current_sale_amt = original_amt * new_discount;
                if (!Utils.equalDouble(xnum,0.0))new_price = Utils.formatDouble(current_sale_amt / xnum,4);

                Logger.d("current_sale_amt :%f,xnum:%f,new_discount:%f",current_sale_amt,xnum,new_discount);

                current_discount_amt = discount_amt = Utils.formatDouble(original_amt - current_sale_amt,4);

                json.put("discount", new_discount);
                json.put("discount_amt", discount_amt);
                json.put("price",new_price);
                break;
        }

        json.put("sale_amt",Utils.formatDouble(current_sale_amt,4));

        if (!Utils.equalDouble(current_discount_amt,0.0) && discount_type != -1){
            final JSONObject discount_json = new JSONObject();
            discount_json.put("gp_id",json.getIntValue("gp_id"));
            discount_json.put("barcode_id",json.getIntValue("barcode_id"));
            discount_json.put("sale_type",json.getIntValue("sale_type"));
            discount_json.put("discount_type",discount_type);
            discount_json.put("price",current_discount_amt);

            //处理商品优惠明细
            addDiscountRecord(json,discount_json);
        }
        notifyDataSetChanged();
    }

    private boolean verifyDiscountPermissions(double content,int type){
        if (2 == type || 1 == type){
            if (type == 1){
                double price;
                if (d_discount){
                    price = Utils.getNotKeyAsNumberDefault(getCurrentContent(),"price",0.0);
                }else {
                    price = Utils.getNotKeyAsNumberDefault(getCurrentContent(),"original_price",0.0);
                }
                if (Utils.equalDouble(price,0.0)){
                    content = 10;
                }else
                    content = content / price * 10;
            }
            return mContext.verifyDiscountPermissions(content / 10,null);
        } else  if (0 == type && Utils.equalDouble(content,0.0))
            return verifyDeletePermissions();

        return true;
    }

    private boolean verifyPresentPermissions(){
        return mContext.verifyPermissions("4",null);
    }

    private boolean verifyDeletePermissions(){
        return mContext.verifyPermissions("1",null);
    }

    public boolean present(){
        boolean code = false;
        final JSONObject object = getCurrentContent();
        if (!object.isEmpty()){
            if (code = verifyPresentPermissions()){
                if (Utils.getNotKeyAsNumberDefault(object,"discount_type",-1) == DISCOUNT_TYPE.PRESENT){
                    _deleteDiscountRecordForType(DISCOUNT_TYPE.PRESENT);
                }else
                    if (MyDialog.showMessageToModalDialog(mContext,String.format(Locale.CHINA,"是否赠送商品:%s?",Html.fromHtml(object.getString("goods_title")))) == 1){
                        updateSaleGoodsInfo(0,3);
                    }
            }
        }
        return code;
    }
    public boolean allDiscount(double value){//整单折 6
        if (!mContext.verifyDiscountPermissions(value /10,null))return false;

        double  discount_amt = 0.0,original_amt = 0.0,new_price = 0.0,xnum = 1.0,current_discount_amt = 0.0,current_sale_amt = 0.0,new_discount = 1.0;
        int discount_type = AbstractSaleGoodsAdapter.DISCOUNT_TYPE.A_DISCOUNT;
        if (Utils.equalDouble(value / 10,1.0)){//discount 1.0 还原价格并清除折扣记录
            _deleteDiscountRecordForType(discount_type);
        }else{
            JSONObject discount_json,json;
            final boolean d_dis = d_discount;
            for(int i = 0,length = mDatas.size();i < length;i++){
                json  = mDatas.getJSONObject(i);

                if (isNotParticipateDiscount(json))continue;

                new_discount = value / 10;

                xnum = Utils.getNotKeyAsNumberDefault(json,"xnum",1.0);
                original_amt = xnum * Utils.getNotKeyAsNumberDefault(json,"original_price",1.0);

                if (d_dis){
                    new_discount = Utils.getNotKeyAsNumberDefault(json,"discount",1.0) / 10 * value;
                }else{
                    _deleteDiscountRecordForGoods(json,0);
                }
                current_sale_amt = original_amt * new_discount;

                new_price = Utils.formatDouble(current_sale_amt / xnum,4);
                discount_amt = Utils.formatDouble(original_amt - current_sale_amt,4);
                current_discount_amt = discount_amt - json.getDoubleValue("discount_amt");

                json.put("discount",Utils.formatDouble(new_discount,4));
                json.put("discount_amt", discount_amt);
                json.put("price",new_price);
                json.put("sale_amt",Utils.formatDouble(current_sale_amt,4));

                if (!Utils.equalDouble(current_discount_amt,0.0)){
                    discount_json = new JSONObject();
                    discount_json.put("gp_id",json.getIntValue("gp_id"));
                    discount_json.put("barcode_id",json.getIntValue("barcode_id"));
                    discount_json.put("sale_type",json.getIntValue("sale_type"));
                    discount_json.put("discount_type",discount_type);
                    discount_json.put("price",current_discount_amt);

                    //处理商品优惠明细
                    addDiscountRecord(json,discount_json);
                }
            }
        }
        notifyDataSetChanged();

        return true;
    }
    public void clearGoods(){
        if (mOrderType != 1)mOrderType = 1;
        mDiscountRecords.fluentClear();
        mDatas.fluentClear();
        if(mStepFullReduceRecord != null) mStepFullReduceRecord = null;

        notifyDataSetChanged();
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
    public AbstractSaleGoodsAdapter setCurrentItemIndex(int index){mCurrentItemIndex = index;return this;}

    public JSONArray getDiscountRecords(){
        return mDiscountRecords;
    }
    public void deleteMolDiscountRecord(){
        _deleteDiscountRecordForType(AbstractSaleGoodsAdapter.DISCOUNT_TYPE.AUTO_MOL);
        _deleteDiscountRecordForType(AbstractSaleGoodsAdapter.DISCOUNT_TYPE.M_MOL);
    }
    public void deleteVipDiscountRecord(){
        getFullReduceInfo("-1");
        if (!isEmpty()){
            _deleteDiscountRecordForType(DISCOUNT_TYPE.V_DISCOUNT);
            _deleteDiscountRecordForType(DISCOUNT_TYPE.PROMOTION);
        }
    }
    public void deleteAllDiscountRecord(){
        _deleteDiscountRecordForType(AbstractSaleGoodsAdapter.DISCOUNT_TYPE.A_DISCOUNT);
    }
    public String discountRecordsToString() {
        final StringBuilder stringBuilder = new StringBuilder();
        generateDiscountRecords();
        if (!mDiscountRecords.isEmpty()) {
            for (int i = 0, size = mDiscountRecords.size(); i < size; i++) {
                final JSONObject record = mDiscountRecords.getJSONObject(i);
                if (stringBuilder.length() > 0) stringBuilder.append(",");
                stringBuilder.append(getDiscountName(Utils.getNotKeyAsNumberDefault(record,"discount_type",-1)));
                stringBuilder.append("：").append(String.format(Locale.CHINA, "%.3f", record.getDoubleValue("discount_money")));
            }
        }
        return stringBuilder.toString();
    }
    private void generateDiscountRecords(){
        boolean isDiscountTypeExist = false;
        JSONArray discount_records;
        JSONObject goods,ok_discount_record,goods_discount_record;
        int goods_id,discount_type;

        double new_discount_amt = 0.0;

        mDiscountRecords.clear();

        for (int i = 0,size = mDatas.size();i < size;i ++){
            goods = mDatas.getJSONObject(i);
            discount_records = Utils.getNullObjectAsEmptyJsonArray(goods,"discount_records");
            for (int j = 0,len = discount_records.size() ;j  < len;j ++){
                isDiscountTypeExist = false;
                goods_discount_record = discount_records.getJSONObject(j);
                goods_id = getGoodsId(goods_discount_record);
                discount_type = goods_discount_record.getIntValue("discount_type");
                new_discount_amt = goods_discount_record.getDoubleValue("price");

                for (int k = 0,k_size = mDiscountRecords.size();k < k_size;k ++){
                    ok_discount_record = mDiscountRecords.getJSONObject(k);

                    if (ok_discount_record.getIntValue("discount_type") == discount_type){
                        isDiscountTypeExist = true;
                        final JSONArray details = JSONArray.parseArray(Utils.getNullOrEmptyStringAsDefault(ok_discount_record,"details","[]"));
                        boolean details_exist = false;
                        for (int p = 0,p_size = details.size();p < p_size ;p ++){
                            final JSONObject detail = details.getJSONObject(p);
                            if (goods_id== getGoodsId(detail)){
                                detail.put("price",detail.getDoubleValue("price") + new_discount_amt);
                                details_exist  = true;
                                break;
                            }
                        }
                        if (!details_exist){
                            details.add(goods_discount_record);
                        }

                        double discount_money = ok_discount_record.getDoubleValue("discount_money");
                        ok_discount_record.put("discount_money",discount_money + new_discount_amt);
                        ok_discount_record.put("details",details.toString());
                        break;
                    }
                }
                if (!isDiscountTypeExist){
                    final JSONObject record = new JSONObject();
                    final JSONArray details = new JSONArray();

                    details.add(goods_discount_record);

                    record.put("discount_type",discount_type);
                    record.put("type",mOrderType);
                    record.put("relevant_id","");
                    record.put("discount_money",Utils.formatDouble(new_discount_amt,4));
                    record.put("details",details.toString());
                    mDiscountRecords.add(record);
                }
            }
        }
        Logger.d_json(mDiscountRecords.toString());
    }
    private String getDiscountName(int discount_type){
        switch (discount_type) {
            case AbstractSaleGoodsAdapter.DISCOUNT_TYPE.FULL_REDUCE:
                return "全场满减";
            case AbstractSaleGoodsAdapter.DISCOUNT_TYPE.PRESENT:
                return "赠送";
            case AbstractSaleGoodsAdapter.DISCOUNT_TYPE.M_DISCOUNT:
                return "手动折扣";
            case AbstractSaleGoodsAdapter.DISCOUNT_TYPE.PROMOTION:
                return "促销";
            case AbstractSaleGoodsAdapter.DISCOUNT_TYPE.V_DISCOUNT:
                return "会员折扣";
            case AbstractSaleGoodsAdapter.DISCOUNT_TYPE.A_DISCOUNT:
                return "整单折扣";
            case AbstractSaleGoodsAdapter.DISCOUNT_TYPE.AUTO_MOL:
                return "自动抹零";
            case AbstractSaleGoodsAdapter.DISCOUNT_TYPE.M_MOL:
                return "手动抹零";
        }
        return "";
    }

    private String getDiscountNames(int[] types){
        final StringBuilder sb = new StringBuilder();
        if (types != null){
            for (int type : types){
                if (sb.length() > 0)sb.append(",");
                sb.append(getDiscountName(type));
            }
        }
        return sb.toString();
    }
    private int[] getGoodsLastDiscountTypes(final JSONObject goods,int[] selector){
        final JSONArray array = Utils.getNullObjectAsEmptyJsonArray(goods,"discount_records");
        int[] types = null;
        int size = array.size();
        if (size != 0){
            types = new int[size];
            int type = -1,index = 0;
            for (int i = 0;i < size ;i ++){
                type = Utils.getNotKeyAsNumberDefault(array.getJSONObject(i),"discount_type",-1);
                if (selector != null && selector.length != 0){
                    for (int t : selector){
                        if (type == t)types[index++] = t;
                    }
                }else
                    types[index++] = type;
            }
        }
        return types;
    }

    public void autoMol(double mol_amt){
        mol(mol_amt, AbstractSaleGoodsAdapter.DISCOUNT_TYPE.AUTO_MOL);
    }
    public void manualMol(double mol_amt){
        mol(mol_amt, AbstractSaleGoodsAdapter.DISCOUNT_TYPE.M_MOL);
    }

    public void updateGoodsInfoToVip(final JSONObject vip){
        getFullReduceInfo(mContext.getVipGradeId());

        double discount ,new_price = 0.0,original_price,discount_amt = 0.0,xnum = 1.0,current_discount_amt = 0.0,original_amt = 0.0,current_sale_amt = 0.0,new_discount = 1.0;
        if (vip != null){
            JSONObject jsonObject,discount_json;
            for (int i = 0,length = mDatas.size();i < length;i++){
                jsonObject = mDatas.getJSONObject(i);

                if (isNotParticipateDiscount(jsonObject))continue;

                discount = Utils.getNotKeyAsNumberDefault(jsonObject,"discount",1.0);
                xnum = Utils.getNotKeyAsNumberDefault(jsonObject,"xnum",1.0);
                original_price = jsonObject.getDoubleValue("original_price");
                original_amt = Utils.formatDouble(xnum * original_price,4);

                switch (jsonObject.getIntValue("yh_mode")){
                    case 0://无优惠
                        new_discount  = 1.0;
                        new_price = original_price;
                        current_sale_amt = original_amt;
                        break;
                    case 1://会员价
                        new_price = Utils.formatDouble(jsonObject.getDoubleValue("yh_price"),4);
                        if (!d_discount){
                            _deleteDiscountRecordForGoods(jsonObject,0);
                        }
                        if (!Utils.equalDouble(original_price,0.0))new_discount = new_price / original_price;

                        current_sale_amt = xnum * new_price;
                        break;
                    case 2://会员折扣
                        Logger.d_json(vip.toString());
                        new_discount = Utils.getNotKeyAsNumberDefault(vip,"discount",10.0) / 10;
                        if (d_discount){
                            new_discount *= discount;
                        }else{
                            _deleteDiscountRecordForGoods(jsonObject,0);
                        }
                        current_sale_amt = original_amt * new_discount;

                        new_price = Utils.formatDouble(current_sale_amt / xnum,4);
                        break;
                }
                current_discount_amt = discount_amt = Utils.formatDouble(original_amt - current_sale_amt,4);

                jsonObject.put("discount", new_discount);
                jsonObject.put("discount_amt", discount_amt);
                jsonObject.put("price",new_price);
                jsonObject.put("sale_amt",Utils.formatDouble(current_sale_amt,2));
                jsonObject.put("original_amt",original_amt);

                if (!Utils.equalDouble(current_discount_amt,0.0)){
                    discount_json = new JSONObject();
                    discount_json.put("gp_id",jsonObject.getIntValue("gp_id"));
                    discount_json.put("barcode_id",jsonObject.getIntValue("barcode_id"));
                    discount_json.put("sale_type",jsonObject.getIntValue("sale_type"));
                    discount_json.put("discount_type", AbstractSaleGoodsAdapter.DISCOUNT_TYPE.V_DISCOUNT);
                    discount_json.put("price",Utils.formatDouble(current_discount_amt,4));

                    //处理商品优惠明细
                    addDiscountRecord(jsonObject,discount_json);

                }
            }
            notifyDataSetChanged();
        }
    }
    private boolean isNotParticipateDiscount(final JSONObject goods){
        if (null != goods)
            return  goods.getIntValue("sale_type") == GoodsInfoViewAdapter.SALE_TYPE.SPECIAL_PROMOTION;
        return true;
    }

    public String generateOrderCode(final String pos_num, int order_type){
        final String order_code;
        if (mSingleRefundStatus){
            order_code = RefundDialog.generateRefundOrderCode(mContext,pos_num);
        }else {
            order_code  = generateSaleOrderCode(pos_num,order_type);
        }
        return order_code;
    }
    private String generateSaleOrderCode(final String pos_num, int order_type){
        String prefix = "N" + pos_num + "-" + new SimpleDateFormat("yyMMddHHmmss",Locale.CHINA).format(new Date()) + "-",order_code;
        final JSONObject orders= new JSONObject();
        if (SQLiteHelper.execSql(orders,"SELECT count(order_id) + 1 order_id from retail_order where date(addtime,'unixepoch' ) = date('now') and pos_code = '" + pos_num +"'")){
            order_code =orders.getString("order_id");
            order_code = prefix + "0000".substring(order_code.length()) + order_code;
            mOrderType = order_type;
        }else{
            order_code = prefix + "0001";;
            Logger.e("生成订单号错误：" + orders.getString("info"));
        }
        return order_code;
    }
    protected void setCurrentItemIndexAndItemView(View v){
        final TextView tv_id,tv_barcode_id,tv_gp_id,sale_type_tv;
        mCurrentItemView = v;
        if (null != v && (tv_id = v.findViewById(R.id.goods_id)) != null && (sale_type_tv = v.findViewById(R.id.discount_sign)) != null &&
                (tv_barcode_id = v.findViewById(R.id.barcode_id)) != null && (tv_gp_id = v.findViewById(R.id.gp_id) ) != null){

            final CharSequence id = tv_id.getText(),barcode_id = tv_barcode_id.getText(),gp_id = tv_gp_id.getText();
            int sale_type = Utils.getViewTagValue(sale_type_tv,0);

            for (int i = 0,length = mDatas.size();i < length;i ++){
                final JSONObject json = mDatas.getJSONObject(i);
                if (id.equals(json.getString("goods_id")) && barcode_id.equals(json.getString("barcode_id")) &&
                        gp_id.equals(json.getString("gp_id")) && sale_type == json.getIntValue("sale_type")){
                    mCurrentItemIndex = i;
                    return;
                }
            }
        }
        mCurrentItemIndex = -1;
    }

    protected void setSelectStatus(View v){
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
    @Deprecated
    private void addDiscountRecords(int type,final JSONArray new_details){
        boolean isDiscountTypeExist = false;
        JSONObject record_json,original_goods,new_goods;
        int ori_id,new_id;
        double new_discount_amt = 0.0,new_discount_price = 0.0;
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
                                new_discount_price  = original_goods.getDoubleValue("price");
                                if (GoodsInfoViewAdapter.isPromotion(original_goods)){//促销保存的是当前记录的总金额
                                    new_discount_amt -= new_discount_price;
                                    new_goods.put("price",Utils.formatDouble( new_discount_amt,4));
                                }else
                                    new_goods.put("price",Utils.formatDouble(new_discount_price + new_discount_amt,4));
                                original_details.remove(j);
                                break;
                            }
                        }
                        original_discount_amt += new_discount_amt;

                        original_details.add(new_goods);
                    }
                    record_json.put("discount_money",Utils.formatDouble(original_discount_amt,4));
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
            record.put("discount_money",Utils.formatDouble(new_discount_amt,4));
            record.put("details",new_details.toJSONString());
            mDiscountRecords.add(record);
        }
    }

    private void _deleteDiscountRecordForGoods(final JSONObject object,double del_num){
        JSONObject discount_goods;
        double discount_money = 0.0,xnum = Utils.getNotKeyAsNumberDefault(object,"xnum",1.0),current_discount_amt = 0.0;
        final JSONArray discount_records = Utils.getNullObjectAsEmptyJsonArray(object,"discount_records");

        if (Utils.equalDouble(del_num,0.0) || Utils.equalDouble(xnum,del_num)){
            discount_records.clear();
            object.put("discount_amt",0.0);
        }else {
            for (int j = discount_records.size() - 1;j >= 0 ;j--){
                discount_goods = discount_records.getJSONObject(j);
                if (getGoodsId(discount_goods) == getGoodsId(object)){
                    //更新销售商品信息
                    discount_money = discount_goods.getDoubleValue("price");

                    current_discount_amt = Utils.formatDouble(discount_money / xnum * del_num,4);
                    discount_goods.put("price",discount_money - current_discount_amt);

                    object.put("discount_amt",Utils.formatDouble(object.getDoubleValue("discount_amt") - current_discount_amt,4));
                }
            }
        }
        notifyDataSetChanged();
    }
    @Deprecated
    private void deleteDiscountRecordForGoods(final JSONObject object,double del_num){
        JSONObject record_json,discount_goods;
        double old_discount_money = 0.0;
        for (int i = 0;i < mDiscountRecords.size();i++){
            record_json = mDiscountRecords.getJSONObject(i);
            final JSONArray details = JSON.parseArray(Utils.getNullOrEmptyStringAsDefault(record_json,"details","[]"));
            if (!details.isEmpty()){
                old_discount_money = record_json.getDoubleValue("discount_money");
                for (int j = 0;j < details.size();j++){
                    discount_goods = details.getJSONObject(j);
                    if (getGoodsId(discount_goods) == getGoodsId(object) && discount_goods.getIntValue("sale_type") == object.getIntValue("sale_type")){
                        //更新销售商品信息
                        double discount_money = discount_goods.getDoubleValue("price"),xnum =Utils.getNotKeyAsNumberDefault(object,"xnum",1.0),current_discount_amt = 0.0;

                        if (Utils.equalDouble(xnum,0.0))continue;

                        if (Utils.equalDouble(del_num,0.0) || Utils.equalDouble(xnum,del_num)){
                            current_discount_amt = discount_money;
                            details.remove(j--);
                        }else {
                            current_discount_amt = Utils.formatDouble(discount_money / xnum * del_num,4);
                            discount_goods.put("price",discount_money - current_discount_amt);
                        }
                        object.put("discount_amt",Utils.formatDouble(object.getDoubleValue("discount_amt") - current_discount_amt,2));

                        if (details.isEmpty()){
                            mDiscountRecords.remove(i--);
                        }else {
                            record_json.put("discount_money",old_discount_money -= current_discount_amt);
                            record_json.put("details",details.toJSONString());
                        }
                    }
                }
            }
        }
    }

    private void _deleteDiscountRecordForType(int discount_type){
        JSONObject discount_goods,goods;
        JSONArray discount_records;

        for (int k = 0,size = mDatas.size();k < size;k++){
            goods = mDatas.getJSONObject(k);
            discount_records = Utils.getNullObjectAsEmptyJsonArray(goods,"discount_records");
            for (int j = discount_records.size() - 1;j >= 0;j --){
                discount_goods = discount_records.getJSONObject(j);
                if (getGoodsId(discount_goods) == getGoodsId(goods) && discount_goods.getIntValue("discount_type") == discount_type){

                    double discount_money = discount_goods.getDoubleValue("price"),
                            sale_amt = goods.getDoubleValue("sale_amt") + discount_money,
                            xnum =Utils.getNotKeyAsNumberDefault(goods,"xnum",0.0),
                            price = Utils.equalDouble(xnum,0.0) ? 0 : sale_amt / xnum,
                            original_price = Utils.getNotKeyAsNumberDefault(goods,"original_price",0.0);

                    goods.put("price",price);
                    goods.put("sale_amt",Utils.formatDouble(sale_amt,4));
                    goods.put("discount",Utils.equalDouble(original_price,0.0) ? 0 : price / original_price);
                    goods.put("discount_amt",Utils.formatDouble(goods.getDoubleValue("discount_amt") - discount_money,4));

                    discount_records.remove(j);
                }
            }
        }
        notifyDataSetChanged();
    }

    @Deprecated
    private void deleteDiscountRecordForType(int discount_type){
        JSONObject record_json,discount_goods,goods;
        if (!mDiscountRecords.isEmpty()){
            for (int i = mDiscountRecords.size() - 1;i >= 0;i--){
                record_json = mDiscountRecords.getJSONObject(i);
                if (discount_type == record_json.getIntValue("discount_type")){
                    record_json = (JSONObject) mDiscountRecords.remove(i);
                    final JSONArray details = JSON.parseArray(Utils.getNullOrEmptyStringAsDefault(record_json,"details","[]"));
                    if (details.isEmpty())continue;
                    for (int j = 0,length = details.size();j < length;j++){
                        discount_goods = details.getJSONObject(j);
                        for (int k = 0,size = mDatas.size();k < size;k++){
                            goods = mDatas.getJSONObject(k);
                            if (getGoodsId(discount_goods) == getGoodsId(goods) && discount_goods.getIntValue("sale_type") == goods.getIntValue("sale_type")){

                                double discount_money = discount_goods.getDoubleValue("price"),
                                        sale_amt = goods.getDoubleValue("sale_amt") + discount_money,
                                        xnum =Utils.getNotKeyAsNumberDefault(goods,"xnum",1.0),
                                        price = sale_amt / xnum;

                                goods.remove("discount_type");
                                goods.put("price",price);
                                goods.put("sale_amt",Utils.formatDouble(sale_amt,4));
                                goods.put("discount",price / Utils.getNotKeyAsNumberDefault(goods,"original_price",1.0));
                                goods.put("discount_amt",Utils.formatDouble(goods.getDoubleValue("discount_amt") - discount_money,4));
                            }
                        }
                    }
                }
            }
            notifyDataSetChanged();
        }
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
        if (!Utils.equalDouble(mol_amt,0.0)){
            if (sale_record <= 1){
                return mol_amt;
            }else {
                double per_record_mol_amt = Utils.formatDoubleDown(mol_amt / sale_record,4);//保留到分
                Logger.d("per_record_mol_amt:%f",per_record_mol_amt);
                if (Math.abs(per_record_mol_amt) < 0.01){
                    Logger.d("sale_record:%d",sale_record);
                    per_record_mol_amt = getPerRecordMolAmt(mol_amt,sale_record / 2);
                }
                Logger.d("per_record_mol_amt:%f",per_record_mol_amt);
                return per_record_mol_amt;
            }
        }
        return 0.0;
    }

    private void mol(double mol_amt,int type){
        Logger.d("mol_amt:%f,type:%d",mol_amt,type);

        JSONObject object,discount_obj;
        int sale_record = mDatas.size();
        double per_record_mol_amt = 0.0, o_per_record_mol_amt = getPerRecordMolAmt(mol_amt,sale_record),original_sale_amt = 0.0,new_discount = 0.0
                ,xnum = 0.0,new_price = 0.0,current_sale_amt = 0.0,discount_amt = 0.0;

        //Utils.sortJsonArrayFromDoubleCol(mDatas,"sale_amt");

        for (int i = 0;i < sale_record;i++){
            per_record_mol_amt = o_per_record_mol_amt;

            object = mDatas.getJSONObject(i);
            original_sale_amt = object.getDoubleValue("sale_amt");

            if (Utils.equalDouble(original_sale_amt,0.0))continue;

            if (original_sale_amt < per_record_mol_amt){
                per_record_mol_amt = original_sale_amt;
                current_sale_amt = 0.0;
            }else
                current_sale_amt = original_sale_amt - per_record_mol_amt;

            new_discount = Utils.formatDouble(current_sale_amt / original_sale_amt,3);

            //处理优惠记录
            discount_obj = new JSONObject();
            discount_obj.put("gp_id",object.getIntValue("gp_id"));
            discount_obj.put("barcode_id",object.getIntValue("barcode_id"));
            discount_obj.put("sale_type",object.getIntValue("sale_type"));
            discount_obj.put("discount_type",type);
            discount_obj.put("price",per_record_mol_amt);//单品折扣金额
            addDiscountRecord(object,discount_obj);


            xnum = Utils.getNotKeyAsNumberDefault(object,"xnum",1.0);

            new_price = Utils.formatDouble(current_sale_amt / xnum,4);

            object.put("discount", new_discount);
            object.put("discount_amt", Utils.formatDouble(per_record_mol_amt + object.getDoubleValue("discount_amt"),4));
            object.put("price",new_price);
            object.put("sale_amt",current_sale_amt);

            Logger.d("%f -= %f:%f",mol_amt,per_record_mol_amt,mol_amt - per_record_mol_amt);
            mol_amt -= per_record_mol_amt;
            if (Utils.equalDouble(Math.abs(mol_amt),0.0)){
                break;
            }else {
                if (Math.abs(Math.abs(mol_amt) - Math.abs(per_record_mol_amt)) < 0.00 || i + 2 == sale_record){
                    o_per_record_mol_amt = mol_amt;
                    mol_amt = 0;
                    Logger.d("o_per_record_mol_amt:%f",o_per_record_mol_amt);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void stepFullReduceDiscount(){
        final StringBuilder err = new StringBuilder();
        final JSONArray array = SQLiteHelper.getListToJson("select * from fullreduce_info where starttime <= datetime('now') <= endtime",err);
        if (null != array){
            if (!array.isEmpty()){
                final JSONObject full_reduce_obj = array.getJSONObject(0);
                final JSONArray rules = JSON.parseArray(Utils.getNullOrEmptyStringAsDefault(full_reduce_obj,"rule","[]"));
                if (!rules.isEmpty()){
                    Utils.sortJsonArrayFromDoubleCol(rules,"full_money");
                    int modes = full_reduce_obj.getIntValue("modes"),fold = full_reduce_obj.getIntValue("fold");
                    double sale_amt = 0.0;
                    switch (modes){
                        case 1:
                            sale_amt = getSumAmt(3);
                            break;
                        case 2:
                            sale_amt = getSumAmt(2);
                            break;
                    }
                    Logger.d("fold:%d,modes:%d,sale_amt:%f",fold,modes,sale_amt);

                    double reduce_money = 0.0,full_money = 0.0;
                    for (int i = rules.size() - 1;i >= 0;i--){
                        final JSONObject rule_json = rules.getJSONObject(i);
                        full_money = rule_json.getDoubleValue("full_money");
                        if (sale_amt >= full_money){
                            if (fold == 2){
                                int counts = (int) (sale_amt / full_money);
                                reduce_money = counts * reduce_money;
                                rule_json.put("reduce_money",reduce_money);
                            }
                            rule_json.put("status",1);
                            break;
                        }
                    }

                    rules.addAll(calculateAmtForRule());
                    Utils.sortJsonArrayFromDoubleCol(rules,"reduce_money");

                    generateStepFullReduceDes(sale_amt,full_reduce_obj,rules);


                    if (!Utils.equalDouble(reduce_money,0.0)) addStepFullReduceDiscount(reduce_money,sale_amt);
                }
            }
        }else {
            MyDialog.displayErrorMessage(mContext, "满减优惠错误:" + err);
        }
    }
    private void addStepFullReduceDiscount(double reduce_money, double sale_amt){
        JSONObject object,discount_obj;
        int sale_record = mDatas.size();
        double per_goods_dis_amt = 0.0, percent = Utils.formatDouble(reduce_money / sale_amt,4),original_sale_amt = 0.0,new_discount = 0.0,
                xnum = 0.0,new_price = 0.0,current_sale_amt = 0.0;

        for (int i = 0;i < sale_record;i++){
            object = mDatas.getJSONObject(i);

            original_sale_amt = object.getDoubleValue("sale_amt");
            if (Utils.equalDouble(original_sale_amt,0.0))continue;

            if (i + 1 == sale_record){
                per_goods_dis_amt = reduce_money;
                current_sale_amt = original_sale_amt - per_goods_dis_amt;
            }else{
                per_goods_dis_amt = Utils.formatDouble(percent * original_sale_amt,4);
                current_sale_amt = original_sale_amt - per_goods_dis_amt;
            }
            reduce_money -= per_goods_dis_amt;

            new_discount = Utils.formatDouble(current_sale_amt / original_sale_amt,3);

            //处理优惠记录
            discount_obj = new JSONObject();
            discount_obj.put("gp_id",object.getIntValue("gp_id"));
            discount_obj.put("barcode_id",object.getIntValue("barcode_id"));
            discount_obj.put("sale_type",object.getIntValue("sale_type"));
            discount_obj.put("discount_type", AbstractSaleGoodsAdapter.DISCOUNT_TYPE.FULL_REDUCE);
            discount_obj.put("price",per_goods_dis_amt);//单品折扣金额
            addDiscountRecord(object,discount_obj);

            xnum = Utils.getNotKeyAsNumberDefault(object,"xnum",1.0);

            new_price = Utils.formatDouble(current_sale_amt / xnum,4);

            object.put("discount", new_discount);
            object.put("discount_amt", Utils.formatDouble(per_goods_dis_amt + object.getDoubleValue("discount_amt"),2));
            object.put("price",new_price);
            object.put("discount_type", AbstractSaleGoodsAdapter.DISCOUNT_TYPE.FULL_REDUCE);
            object.put("sale_amt",current_sale_amt);
        }

        notifyDataSetChanged();
    }

    private void generateStepFullReduceDes(double sale_amt, final @NonNull JSONObject object, final @NonNull JSONArray rules){
        final String name = String.format(Locale.CHINA,"%s(%s)",getDiscountName(AbstractSaleGoodsAdapter.DISCOUNT_TYPE.FULL_REDUCE),object.getString("title"));
        final JSONArray rules_des = new JSONArray();
        JSONObject rule_obj,tmp;
        double full_money = 0.0,reduce_money = 0.0,max_reduce_money = 0.0;
        for (int i = rules.size() - 1;i >= 0;i --){
            tmp = rules.getJSONObject(i);
            rule_obj = new JSONObject();

            full_money = tmp.getDoubleValue("full_money");
            reduce_money = tmp.getDoubleValue("reduce_money");
            rule_obj.put("rule_des",mContext.getString(R.string.fullreduce_des_sz,String.valueOf(full_money),String.valueOf(reduce_money)));
            rule_obj.put("title",tmp.containsKey("title") ? tmp.getString("title") : name);

            if (sale_amt >= full_money){
                if (tmp.containsKey("status")){
                    if (reduce_money > max_reduce_money){
                        max_reduce_money = reduce_money;
                    }else {
                        continue;
                    }
                    rule_obj.put("status",tmp.getIntValue("status"));
                }else
                    rule_obj.put("status",2);
            }else {
                rule_obj.put("status",3);
                rule_obj.put("diff_amt_des",mContext.getString(R.string.diff_des_sz,String.format(Locale.CHINA,"%.2f",full_money -sale_amt)));
            }

            rules_des.add(rule_obj);
        }
        Logger.d_json(rules.toString());

        mStepFullReduceRecord = new JSONObject();
        mStepFullReduceRecord.put("name","满减促销方案");
        mStepFullReduceRecord.put("rules_des",rules_des);
    }

    public JSONObject getStepFullReduceRecord(){
        return mStepFullReduceRecord;
    }

    public void deleteStepFullReduceRecord(){
        if(mStepFullReduceRecord != null) mStepFullReduceRecord = null;
        _deleteDiscountRecordForType(AbstractSaleGoodsAdapter.DISCOUNT_TYPE.FULL_REDUCE);
    }

    private void getFullReduceInfo(final String grade_id){
        /*在使用会员、清空会员时调用刷新满减信息*/
        final StringBuilder err = new StringBuilder();
        final String sql = "SELECT tlpb_id,title,type_detail_id,promotion_type,promotion_object,promotion_grade_id,cumulation_give,buyfull_money,reduce_money FROM fullreduce_info_new where status = 1 and " +
                "(promotion_object = 0 or ((promotion_object = 2 and "+ grade_id +" > 0) or promotion_grade_id = "+ grade_id +")) and " +
                "date(start_date, 'unixepoch', 'localtime') || ' ' ||begin_time  <= datetime('now', 'localtime') \n" +
                " and datetime('now', 'localtime') <= date(end_date, 'unixepoch', 'localtime') || ' ' ||end_time and \n" +
                "promotion_week like '%' ||case strftime('%w','now' ) when 0 then 7 else strftime('%w','now' ) end||'%'";

        Logger.d("ReduceInfo_sql:%s",sql);
        final JSONArray array = SQLiteHelper.getListToJson(sql,err);
        if (null != array){
            groupingFullReducePlan(array.toJavaList(FullReduceRule.class));
        }else {
            Toast.makeText(mContext,"初始化满减信息错误:" + err,Toast.LENGTH_LONG).show();
        }
    }

    private void groupingFullReducePlan(final List<FullReduceRule> fullReduceRules){
        JSONObject ruleObj;
        JSONArray detail_ids;
        mFullReduceRuleGroup = new JSONArray();
        for (FullReduceRule rule : fullReduceRules){
            boolean isExist = false;
            for (int i = 0,size = mFullReduceRuleGroup.size();i < size;i ++){
                ruleObj = mFullReduceRuleGroup.getJSONObject(i);
                if (ruleObj.getIntValue("tlpb_id") == rule.getTlpb_id()){
                    detail_ids = Utils.getNullObjectAsEmptyJsonArray(ruleObj,"detail_ids");
                    detail_ids.add(rule.getType_detail_id());
                    isExist = true;
                    break;
                }
            }
            if (!isExist){

                ruleObj = new JSONObject();
                ruleObj.put("tlpb_id",rule.getTlpb_id());
                ruleObj.put("title",rule.getTitle());
                ruleObj.put("promotion_type",rule.getPromotion_type());
                ruleObj.put("cumulation_give",rule.getCumulation_give());
                ruleObj.put("full_money",rule.getBuyfull_money());
                ruleObj.put("reduce_money",rule.getReduce_money());

                detail_ids = new JSONArray();
                detail_ids.add(rule.getType_detail_id());
                ruleObj.put("detail_ids",detail_ids);

                mFullReduceRuleGroup.add(ruleObj);
            }
        }
    }

    private JSONArray calculateAmtForRule(){
        /* promotion_type 0全场促销,1时是商品id,2为类别id,3货商id,4品牌id*/
        double plan_amt = 0.0;
        JSONArray detail_ids;
        String value_key = null;
        JSONObject goods,rule;
        int promotion_type = -1;
        String detail_id,id;
        final JSONArray rules = Utils.JsondeepCopy(mFullReduceRuleGroup);
        for (int i = 0,size = rules.size();i < size;i ++){
            rule = rules.getJSONObject(i);
            promotion_type = Utils.getNotKeyAsNumberDefault(rule,"promotion_type",-1);
            switch (promotion_type){
                case 1:
                    value_key = "barcode_id";
                    break;
                case 2:
                    value_key = "category_id";
                    break;
                case 3:
                    value_key = "gs_id";
                    break;
                case 4:
                    value_key = "brand_id";
                    break;
                default:
                    plan_amt = mTotalSaleAmt;
                    value_key = null;
            }
            if (value_key != null){
                detail_ids = rule.getJSONArray("detail_ids");
                plan_amt = 0.0;
                for (int j = 0,len = detail_ids.size();j < len;j ++){
                    detail_id = detail_ids.getString(j);
                    for (int k = 0,k_len = mDatas.size();k < k_len;k++){
                        goods = mDatas.getJSONObject(k);
                        if (promotion_type == 2){//类别要判断path
                            id = Utils.getNullStringAsEmpty(goods,"path");
                            if (id.contains(detail_id + "@")){
                                plan_amt += goods.getDoubleValue("sale_amt");
                            }
                        }else {
                            id = Utils.getNullStringAsEmpty(goods,value_key);
                            if (id.equals(detail_id)){
                                plan_amt += goods.getDoubleValue("sale_amt");
                            }
                        }
                    }
                }
                calculateReduceMoney(rule,plan_amt);
            }else {
                calculateReduceMoney(rule,plan_amt);
            }
        }
        Logger.d_json(rules.toString());
        return rules;
    }
    private void calculateReduceMoney(final JSONObject rule,double sale_amt){
        double full_money = rule.getDoubleValue("full_money");
        if (Utils.notLessDouble(sale_amt,full_money)){
            rule.put("status",1);
            int cumulation_give = rule.getIntValue("cumulation_give");
            double reduce_money = rule.getDoubleValue("reduce_money");
            if (cumulation_give == 1 && !Utils.equalDouble(full_money,0.0)){
                int times =  (int) (sale_amt / full_money);
                if (times > 1){
                    reduce_money *= ((double) times);
                    rule.put("reduce_money",reduce_money);
                }
            }
        }
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
    public double getSumAmt(int type){//计算金额
        double amt = 0.0;
        switch (type){
            case 1:
                amt = mTotalDiscountAmt; //折扣金额
                break;
            case 2:
                amt = mTotalOriginalAmt;//原价金额
                break;
            default:
                amt = mTotalSaleAmt;
        }
        return amt;
    }

    @Override
    public void setDataForArray(JSONArray array) {
        if (null == array){
            array = new JSONArray();
        }
        super.setDataForArray(array);
    }

    public void setSingleRefundStatus(final boolean b){
        if (b && mSingleRefundStatus){
            mSingleRefundStatus = false;
        }else
            mSingleRefundStatus = b;

        notifyDataSetChanged();
    }
    public boolean getSingleRefundStatus(){
        return mSingleRefundStatus;
    }
}
