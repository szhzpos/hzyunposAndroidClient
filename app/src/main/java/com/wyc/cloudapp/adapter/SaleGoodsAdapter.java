package com.wyc.cloudapp.adapter;

import android.app.Dialog;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
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
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.dialog.serialScales.GoodsWeighDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.callback.ClickListener;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.FontSizeTagHandler;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class SaleGoodsAdapter extends RecyclerView.Adapter<SaleGoodsAdapter.MyViewHolder> {
    private MainActivity mContext;
    private JSONArray mDatas,mDiscountRecords;
    private View mCurrentItemView;
    private int mCurrentItemIndex;
    private int mOrderType = 1;//订单类型 1线下 2线上
    private boolean mSingleRefundStatus = false,d_discount = false;//d_discount是否折上折
    private JSONObject mFullReduceRecord;
    private GoodsWeighDialog mWeighDialog;
    public SaleGoodsAdapter(MainActivity context){
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
        static final int FULL_REDUCE = 1,PRESENT = 2,PROMOTION = 3,M_DISCOUNT = 4,V_DISCOUNT = 5,A_DISCOUNT = 6,AUTO_MOL =7,M_MOL = 8;
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView row_id,gp_id,goods_id,goods_title,unit_name,barcode_id,barcode,sale_price,sale_num,sale_amt,discount_sign,original_price;
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
            discount_sign = itemView.findViewById(R.id.discount_sign);
            sale_price =  itemView.findViewById(R.id.sale_price);
            sale_num = itemView.findViewById(R.id.sale_num);
            sale_amt = itemView.findViewById(R.id.sale_amt);
            original_price = itemView.findViewById(R.id.original_price);
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View itemView = View.inflate(mContext, R.layout.sale_goods_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.sale_goods_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        final JSONObject goods_info = mDatas.getJSONObject(i);
        if (goods_info != null){
            final int discount_type = Utils.getNotKeyAsNumberDefault(goods_info,"discount_type",-1);
            switch (discount_type){
                case DISCOUNT_TYPE.PRESENT:
                case DISCOUNT_TYPE.PROMOTION:
                    myViewHolder.discount_sign.setText(getDiscountName(discount_type));
                    break;
                    default:
                        myViewHolder.discount_sign.setText(mContext.getString(R.string.space_sz));
                        break;
            }

            myViewHolder.discount_sign.setTag(goods_info.getIntValue("sale_type"));

            myViewHolder.row_id.setText(String.format(Locale.CHINA,"%s%s",i + 1,"、"));
            myViewHolder.goods_id.setText(goods_info.getString("goods_id"));
            myViewHolder.gp_id.setText(goods_info.getString("gp_id"));
            myViewHolder.goods_title.setText(goods_info.getString("goods_title"));
            myViewHolder.unit_name.setText(goods_info.getString("unit_name"));
            myViewHolder.barcode_id.setText(goods_info.getString("barcode_id"));
            myViewHolder.barcode.setText(goods_info.getString("barcode"));
            myViewHolder.original_price.setText(String.format(Locale.CHINA,"%.2f",goods_info.getDoubleValue("original_price")));
            myViewHolder.sale_price.setText(String.format(Locale.CHINA,"%.2f",goods_info.getDoubleValue("price")));
            myViewHolder.sale_num.setText(String.format(Locale.CHINA,"%.3f",goods_info.getDoubleValue("xnum")));
            myViewHolder.sale_amt.setText(String.format(Locale.CHINA,"%.2f",goods_info.getDoubleValue("sale_amt")));

            if(myViewHolder.goods_title.getCurrentTextColor() == mContext.getResources().getColor(R.color.blue,null)){
                myViewHolder.goods_title.setTextColor(mContext.getColor(R.color.black));//需要重新设置颜色；不然重用之后内容颜色为重用之前的。
            }

            myViewHolder.mCurrentLayoutItemView.setOnTouchListener(onTouchListener);

            if (mCurrentItemIndex == i){
                setSelectStatus(myViewHolder.mCurrentLayoutItemView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    private ClickListener onTouchListener = new ClickListener(v -> {
        setCurrentItemIndexAndItemView(v);
        deleteSaleGoods(mCurrentItemIndex,0);
    }, this::setSelectStatus);

    private boolean isScalesWeighingGoods(final JSONObject object){
        return object != null && Utils.getNullStringAsEmpty(object,GoodsInfoViewAdapter.W_G_MARK).isEmpty() && (object.getIntValue("type") == 2);
    }
    private double getScalesWeighingGoodsSumNum(){
        double num = 0.0;
        for (int i = 0,size = mDatas.size();i < size;i++){
            final JSONObject object = mDatas.getJSONObject(i);
            if (object != getCurrentContent() && isScalesWeighingGoods(object))num += object.getDoubleValue("xnum");
        }
        return num;
    }
    public void addSaleGoods(final JSONObject goods){
        if (null != goods && !goods.isEmpty()){
            double xnum = goods.getDoubleValue("xnum");//新增的商品不存在xnum字段，xnum等于0.0。挂单以及条码秤称重商品已经存在,以及该字段值不会为0
            boolean isBarcodeWeighingGoods = !Utils.getNullStringAsEmpty(goods,GoodsInfoViewAdapter.W_G_MARK).isEmpty(),isZero = Utils.equalDouble(xnum,0.0);
            if(!isBarcodeWeighingGoods && isZero && goods.getIntValue("type") == 2){//type 1 普通 2散装称重 3鞋帽
                if (mWeighDialog == null)mWeighDialog = new GoodsWeighDialog(mContext,mContext.getString(R.string.goods_i_sz));
                mWeighDialog.setOnYesOnclickListener(num -> {
                    if (!Utils.equalDouble(num,0.0)){
                        if (mWeighDialog.isContinuousWeighing()) {
                            if (isScalesWeighingGoods(getCurrentContent())){
                                num -= getScalesWeighingGoodsSumNum();
                                if (!Utils.equalDouble(num,0.0))updateSaleGoodsInfo(num, 0);
                            }
                        }else{
                            addSaleGoods(goods,num,false);
                        }
                    }
                });
                if (mWeighDialog.isContinuousWeighing()){
                    addSaleGoods(goods,1,false);
                }else{
                    mWeighDialog.setBarcodeId(goods.getIntValue("barcode_id"));
                    mWeighDialog.read();
                    mWeighDialog.show();
                }
            }else{
                if (!isBarcodeWeighingGoods && isZero)xnum = 1.0;
                addSaleGoods(goods,xnum,isBarcodeWeighingGoods);
            }
        }
    }
    private void addSaleGoods(final @NonNull JSONObject goods,double num,boolean isBarcodeWeighingGoods){
        final JSONObject copy = verifyPromotion(goods,num);
        if (copy != null){
            double diff_xnum = copy.getDoubleValue("xnum");
            addSaleGoods(copy,diff_xnum,isBarcodeWeighingGoods);//拆分超过促销数量的商品
            num -= diff_xnum;
            if (Utils.equalDouble(num,0.0))return;
        }
        mContext.clearSearchEt();

        int barcode_id = goods.getIntValue("barcode_id"),gp_id = goods.getIntValue("gp_id"),discount_type = -1,sale_type = goods.getIntValue("sale_type");;
        double  sale_price,discount = 1.0,discount_amt = 0.0,new_price = 0.0,current_sale_amt = 0.0,current_discount_amt = 0.0,original_price = 0.0,original_amt = 0.0,sum_xnum = 0.0;
        boolean exist = false,isPromotion = (sale_type == GoodsInfoViewAdapter.SALE_TYPE.SPECIAL_PROMOTION);
        JSONObject tmp_obj;

        for (int i = 0,length = mDatas.size();i < length;i++){
            tmp_obj = mDatas.getJSONObject(i);

            if (barcode_id == tmp_obj.getIntValue("barcode_id") && gp_id == tmp_obj.getIntValue("gp_id") && sale_type == tmp_obj.getIntValue("sale_type")){
                exist = true;

                original_price = tmp_obj.getDoubleValue("original_price");
                sum_xnum = tmp_obj.getDoubleValue("xnum") + num;
                new_price = tmp_obj.getDoubleValue("price");

                original_amt = original_price * sum_xnum;
                current_sale_amt = sum_xnum * new_price;

                current_discount_amt = Utils.formatDouble((original_price - new_price) * num,4);

                discount_amt =  original_amt - current_sale_amt;

                Logger.d("new_price:%f,original_price:%f,current_sale_amt:%f,original_amt:%f,current_discount_amt:%f,sum_xnum:%f",new_price,original_price,current_sale_amt,original_amt,current_discount_amt,sum_xnum);

                tmp_obj.put("discount_amt", discount_amt);
                tmp_obj.put("xnum",Utils.formatDouble(sum_xnum,4));
                tmp_obj.put("sale_amt",Utils.formatDouble(current_sale_amt,4));
                tmp_obj.put("original_amt",Utils.formatDouble(original_amt,4));
                mCurrentItemIndex = i;

                discount_type = tmp_obj.getIntValue("discount_type");
                break;
            }
        }

        if (!exist){
            original_price = goods.getDoubleValue("retail_price");
            sale_price = Utils.getNotKeyAsNumberDefault(goods,"price",1.0);

            if (isPromotion){//促销商品
                if (!Utils.equalDouble(original_price,0.0))discount = sale_price / original_price;
                discount_type = DISCOUNT_TYPE.PROMOTION;
                goods.put("discount_type", discount_type);
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
                discount_type = DISCOUNT_TYPE.V_DISCOUNT;
                goods.put("discount_type", discount_type);
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
            final JSONArray discount_details = new JSONArray();
            final JSONObject discount_json = new JSONObject();
            discount_json.put("gp_id",goods.getIntValue("gp_id"));
            discount_json.put("barcode_id",goods.getIntValue("barcode_id"));
            discount_json.put("sale_type",goods.getIntValue("sale_type"));
            discount_json.put("price",current_discount_amt);
            discount_details.add(discount_json);

            //处理商品优惠明细
            addDiscountRecords(discount_type,discount_details);
        }

        this.notifyDataSetChanged();
    }

    private JSONObject verifyPromotion(final @NonNull JSONObject object,double num){
        JSONObject tmp_obj;
        int sale_type = object.getIntValue("sale_type");
        double limit_xnum = object.getDoubleValue("limit_xnum");
        if (sale_type == GoodsInfoViewAdapter.SALE_TYPE.SPECIAL_PROMOTION && !Utils.equalDouble(limit_xnum,0.0)){
            int barcode_id = object.getIntValue("barcode_id"),gp_id = object.getIntValue("gp_id");
            double sum_xnum = 0.0,diff_xnum = 0.0,ori_price = 0.0;
            for (int i = 0,length = mDatas.size();i < length;i++) {
                tmp_obj = mDatas.getJSONObject(i);
                if (sale_type == tmp_obj.getIntValue("sale_type") && barcode_id == tmp_obj.getIntValue("barcode_id") && gp_id == tmp_obj.getIntValue("gp_id")){
                    sum_xnum  += tmp_obj.getDoubleValue("xnum");
                }
            }
            if ((diff_xnum = sum_xnum + num - limit_xnum) > 0){
                final JSONObject copy = Utils.JsondeepCopy(object);
                ori_price = copy.getDoubleValue("retail_price");
                copy.put("sale_type",GoodsInfoViewAdapter.SALE_TYPE.COMMON);
                copy.put("price",ori_price);
                copy.put("xnum",diff_xnum);
                copy.put("sale_amt",diff_xnum * ori_price);
                if (DISCOUNT_TYPE.PROMOTION == copy.getIntValue("discount_type")){
                    copy.remove("discount_type");
                }
                Logger.d("diff_xnum:%f",diff_xnum);

                return copy;
            }
        }

        return null;
    }

    public void deleteSaleGoods(int index,double num){
        int size = mDatas.size();
        if (0 <= index && index < size){
            final JSONObject jsonObject = mDatas.getJSONObject(index);
            deleteDiscountRecordForGoods(jsonObject,num);
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
                    dialog = new ChangeNumOrPriceDialog(mContext,Html.fromHtml("折扣率<size value='14'>[1-10],10为不折扣</size> ",null,new FontSizeTagHandler(mContext)),String.format(Locale.CHINA,"%.2f",Utils.getNotKeyAsNumberDefault(cur_json,"discount",1.0) * 10));
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
    private void updateSaleGoodsInfo(double value,int type){//type 0 修改数量 1修改价格 2打折 3赠送
        final JSONObject json = getCurrentContent();
        double  discount_amt = 0.0,original_price = 0.0,original_amt = 0.0,new_price = 0.0,xnum = 0.0,current_discount_amt = 0.0,current_sale_amt = 0.0,new_discount = 1.0;
        int discount_type = 0;

        original_price = Utils.getNotKeyAsNumberDefault(json,"original_price",1.0);

        final JSONArray  discount_details = new JSONArray();;
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

                    discount_amt = original_amt - current_sale_amt;
                    current_discount_amt = discount_amt - json.getDoubleValue("discount_amt");

                    json.put("discount_amt", Utils.formatDouble(discount_amt,2));
                    json.put("xnum",Utils.formatDouble(xnum,3));
                    json.put("original_amt",original_amt);

                    discount_type = json.getIntValue("discount_type");
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
                    discount_type = DISCOUNT_TYPE.M_DISCOUNT;
                }

                if(!d_discount){
                    deleteDiscountRecordForGoods(json,0);
                }
                xnum = json.getDoubleValue("xnum");
                original_amt = xnum * original_price;

                current_sale_amt = xnum * new_price;

                discount_amt = Utils.formatDouble(original_amt - current_sale_amt,2);
                current_discount_amt = discount_amt - json.getDoubleValue("discount_amt");

                json.put("discount", new_discount);
                json.put("discount_amt", discount_amt);
                json.put("price",Utils.formatDouble(new_price,4));
                break;

            case 2://手动不处理折上折。每次打折都是从原价的基础上折
                if (isNotParticipateDiscount(json))return;

                if (Utils.equalDouble(value / 10,1.0)){
                    deleteDiscountRecordForType(DISCOUNT_TYPE.M_DISCOUNT);
                    deleteDiscountRecordForType(DISCOUNT_TYPE.A_DISCOUNT);
                    deleteDiscountRecordForType(DISCOUNT_TYPE.PRESENT);
                    return;
                }

                new_discount = Utils.formatDouble(value / 10,4);

                xnum = Utils.getNotKeyAsNumberDefault(json,"xnum",1.0);
                original_amt = Utils.formatDouble(xnum * original_price,4);

                discount_type = DISCOUNT_TYPE.M_DISCOUNT;

                if (!d_discount){
                    deleteDiscountRecordForGoods(json,0);
                }

                current_sale_amt = original_amt * new_discount;
                if (!Utils.equalDouble(xnum,0.0))new_price = Utils.formatDouble(current_sale_amt / xnum,4);

                Logger.d("current_sale_amt :%f,xnum:%f,new_discount:%f",current_sale_amt,xnum,new_discount);

                discount_amt = Utils.formatDouble(original_amt - current_sale_amt,4);
                current_discount_amt = discount_amt - json.getDoubleValue("discount_amt");

                json.put("discount", new_discount);
                json.put("discount_amt", discount_amt);
                json.put("price",new_price);
                break;
        }
        json.put("discount_type",discount_type);
        json.put("sale_amt",Utils.formatDouble(current_sale_amt,4));

        if (!Utils.equalDouble(current_discount_amt,0.0)){
            final JSONObject discount_json = new JSONObject();
            discount_json.put("gp_id",json.getIntValue("gp_id"));
            discount_json.put("barcode_id",json.getIntValue("barcode_id"));
            discount_json.put("sale_type",json.getIntValue("sale_type"));
            discount_json.put("price",current_discount_amt);
            discount_details.add(discount_json);

            //处理商品优惠明细
            addDiscountRecords(discount_type,discount_details);
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
        boolean code;
        if (code = verifyPresentPermissions()){
            updateSaleGoodsInfo(0,3);
        }
        return code;
    }
    public boolean allDiscount(double value){//整单折 6
        if (!mContext.verifyDiscountPermissions(value /10,null))return false;

        double  discount_amt = 0.0,original_amt = 0.0,new_price = 0.0,xnum = 1.0,current_discount_amt = 0.0,current_sale_amt = 0.0,new_discount = 1.0;
        int discount_type = DISCOUNT_TYPE.A_DISCOUNT;
        if (Utils.equalDouble(value / 10,1.0)){//discount 1.0 还原价格并清除折扣记录
            deleteDiscountRecordForType(discount_type);
        }else{
            JSONObject discount_json,json;
            final JSONArray discount_details = new JSONArray();
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
                    deleteDiscountRecordForGoods(json,0);
                }
                current_sale_amt = original_amt * new_discount;

                new_price = Utils.formatDouble(current_sale_amt / xnum,4);
                discount_amt = Utils.formatDouble(original_amt - current_sale_amt,2);
                current_discount_amt = discount_amt - json.getDoubleValue("discount_amt");

                json.put("discount",Utils.formatDouble(new_discount,4));
                json.put("discount_amt", discount_amt);
                json.put("price",new_price);
                json.put("discount_type",discount_type);
                json.put("sale_amt",Utils.formatDouble(current_sale_amt,4));

                if (!Utils.equalDouble(current_discount_amt,0.0)){
                    discount_json = new JSONObject();
                    discount_json.put("gp_id",json.getIntValue("gp_id"));
                    discount_json.put("barcode_id",json.getIntValue("barcode_id"));
                    discount_json.put("sale_type",json.getIntValue("sale_type"));
                    discount_json.put("price",current_discount_amt);
                    discount_details.add(discount_json);
                }
            }
            //处理商品优惠明细
            if (!discount_details.isEmpty())
                addDiscountRecords(discount_type,discount_details);
        }
        notifyDataSetChanged();

        return true;
    }
    public void clearGoods(){
        if (mOrderType != 1)mOrderType = 1;
        mDiscountRecords.fluentClear();
        mDatas.fluentClear();
        if(mFullReduceRecord != null)mFullReduceRecord = null;
        if (mWeighDialog != null){
            mWeighDialog.stopRead();
            mWeighDialog = null;
        }
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
    public SaleGoodsAdapter setCurrentItemIndex(int index){mCurrentItemIndex = index;return this;}
    public JSONArray getDatas() {
        return mDatas;
    }
    public JSONArray getDiscountRecords(){
        return mDiscountRecords;
    }
    public void deleteMolDiscountRecord(){
        deleteDiscountRecordForType(DISCOUNT_TYPE.AUTO_MOL);
        deleteDiscountRecordForType(DISCOUNT_TYPE.M_MOL);
    }
    public void deleteVipDiscountRecord(){
         deleteDiscountRecordForType(DISCOUNT_TYPE.V_DISCOUNT);
    }
    public void deleteAlldiscountRecord(){
        deleteDiscountRecordForType(DISCOUNT_TYPE.A_DISCOUNT);
    }
    public String discountRecordsToString() {
        final StringBuilder stringBuilder = new StringBuilder();
        if (!mDiscountRecords.isEmpty()) {
            for (int i = 0, size = mDiscountRecords.size(); i < size; i++) {
                final JSONObject record = mDiscountRecords.getJSONObject(i);
                if (stringBuilder.length() > 0) stringBuilder.append(",");
                stringBuilder.append(getDiscountName(Utils.getNotKeyAsNumberDefault(record,"discount_type",-1)));
                stringBuilder.append("：").append(String.format(Locale.CHINA, "%.2f", record.getDoubleValue("discount_money")));
            }
        }
        return stringBuilder.toString();
    }
    private String getDiscountName(int discount_type){
        switch (discount_type) {
            case DISCOUNT_TYPE.FULL_REDUCE:
                return "全场满减";
            case DISCOUNT_TYPE.PRESENT:
                return "赠送";
            case DISCOUNT_TYPE.M_DISCOUNT:
                return "手动折扣";
            case DISCOUNT_TYPE.PROMOTION:
                return "促销";
            case DISCOUNT_TYPE.V_DISCOUNT:
                return "会员折扣";
            case DISCOUNT_TYPE.A_DISCOUNT:
                return "整单折扣";
            case DISCOUNT_TYPE.AUTO_MOL:
                return "自动抹零";
            case DISCOUNT_TYPE.M_MOL:
                return "手动抹零";
        }
        return "";
    }

    public void autoMol(double mol_amt){
        mol(mol_amt,DISCOUNT_TYPE.AUTO_MOL);
    }
    public void manualMol(double mol_amt){
        mol(mol_amt,DISCOUNT_TYPE.M_MOL);
    }

    public void updateGoodsInfoToVip(final JSONObject vip){
        double discount ,new_price = 0.0,original_price,discount_amt = 0.0,xnum = 1.0,current_discount_amt = 0.0,original_amt = 0.0,current_sale_amt = 0.0,new_discount = 1.0;
        if (vip != null){
            JSONObject jsonObject,discount_json;
            JSONArray discount_details = new JSONArray();
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
                            deleteDiscountRecordForGoods(jsonObject,0);
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
                            deleteDiscountRecordForGoods(jsonObject,0);
                        }
                        current_sale_amt = original_amt * new_discount;

                        new_price = Utils.formatDouble(current_sale_amt / xnum,4);
                        break;
                }
                discount_amt = Utils.formatDouble(original_amt - current_sale_amt,4);
                current_discount_amt = discount_amt - jsonObject.getDoubleValue("discount_amt");

                jsonObject.put("discount_type",DISCOUNT_TYPE.V_DISCOUNT);
                jsonObject.put("discount", new_discount);
                jsonObject.put("discount_amt", discount_amt);
                jsonObject.put("price",new_price);
                jsonObject.put("sale_amt",Utils.formatDouble(current_sale_amt,2));
                jsonObject.put("original_amt",original_amt);

                if (!Utils.equalDouble(current_discount_amt,0.0)){
                    discount_json = new JSONObject();
                    discount_json.put("gp_id",jsonObject.getIntValue("gp_id"));
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
    private boolean isNotParticipateDiscount(final JSONObject goods){
        if (null != goods)
            return  goods.getIntValue("sale_type") == GoodsInfoViewAdapter.SALE_TYPE.SPECIAL_PROMOTION;
        return true;
    }

    public String generateOrderCode(final String pos_num, int order_type){
        String order_code;
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
    private void setCurrentItemIndexAndItemView(View v){
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
    private void deleteDiscountRecordForType(int discount_type){
        JSONObject record_json,discount_goods,goods;
        if (!mDiscountRecords.isEmpty()){
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
                            if (getGoodsId(discount_goods) == getGoodsId(goods) && discount_goods.getIntValue("sale_type") == goods.getIntValue("sale_type")){
                                double discount_money = discount_goods.getDoubleValue("price"),
                                        sale_amt = goods.getDoubleValue("sale_amt") + discount_money,
                                        xnum =Utils.getNotKeyAsNumberDefault(goods,"xnum",1.0),
                                        price = sale_amt / xnum;
                                goods.remove("discount_type");
                                goods.put("price",price);
                                goods.put("sale_amt",Utils.formatDouble(sale_amt,2));
                                goods.put("discount",price / Utils.getNotKeyAsNumberDefault(goods,"original_price",1.0));
                                goods.put("discount_amt",Utils.formatDouble(goods.getDoubleValue("discount_amt") - discount_money,2));
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
        if (!Utils.equalDouble(mol_amt,0.0) && sale_record != 0){
            double per_record_mol_amt = Utils.formatDoubleDown(mol_amt / sale_record,2);//保留到分
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
        double per_record_mol_amt = 0.0, o_per_record_mol_amt = getPerRecordMolAmt(mol_amt,sale_record),original_sale_amt = 0.0,new_discount = 0.0
                ,xnum = 0.0,new_price = 0.0,current_sale_amt = 0.0;

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
            discount_obj.put("price",per_record_mol_amt);//单品折扣金额
            discount_details.add(discount_obj);

            xnum = Utils.getNotKeyAsNumberDefault(object,"xnum",1.0);

            new_price = Utils.formatDouble(current_sale_amt / xnum,4);

            object.put("discount", new_discount);
            object.put("discount_amt", Utils.formatDouble(per_record_mol_amt + object.getDoubleValue("discount_amt"),2));
            object.put("price",new_price);
            object.put("discount_type",type);
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
        if (!discount_details.isEmpty()){
            addDiscountRecords(type,discount_details);
        }
        notifyDataSetChanged();
    }

    public void fullReduceDiscount(){
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
                            reduce_money = rule_json.getDoubleValue("reduce_money");

                            rule_json.put("status",1);
                            break;
                        }
                    }

                    generateFullReduceDes(sale_amt,full_reduce_obj,rules);

                    if (fold == 2){
                        int counts = (int) (sale_amt / full_money);
                        reduce_money = counts * reduce_money;
                    }
                    if (!Utils.equalDouble(reduce_money,0.0))addFullReduce(reduce_money,sale_amt);
                }
            }
        }else {
            MyDialog.displayErrorMessage(null,"满减优惠错误:" + err,mContext);
        }
    }
    private void addFullReduce(double reduce_money,double sale_amt){
        JSONObject object,discount_obj;
        final JSONArray discount_details = new JSONArray();
        int sale_record = mDatas.size();
        double per_goods_dis_amt = 0.0, percent = Utils.formatDouble(reduce_money / sale_amt,4),original_sale_amt = 0.0,new_discount = 0.0,
                xnum = 0.0,new_price = 0.0,current_sale_amt = 0.0;

        for (int i = 0;i < sale_record;i++){
            object = mDatas.getJSONObject(i);

            if (isNotParticipateDiscount(object))continue;

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
            discount_obj.put("price",per_goods_dis_amt);//单品折扣金额
            discount_details.add(discount_obj);

            xnum = Utils.getNotKeyAsNumberDefault(object,"xnum",1.0);

            new_price = Utils.formatDouble(current_sale_amt / xnum,4);

            object.put("discount", new_discount);
            object.put("discount_amt", Utils.formatDouble(per_goods_dis_amt + object.getDoubleValue("discount_amt"),2));
            object.put("price",new_price);
            object.put("discount_type",DISCOUNT_TYPE.FULL_REDUCE);
            object.put("sale_amt",current_sale_amt);
        }
        if (!discount_details.isEmpty()){
            addDiscountRecords(DISCOUNT_TYPE.FULL_REDUCE,discount_details);
        }
        notifyDataSetChanged();
    }

    private void generateFullReduceDes(double sale_amt,final @NonNull JSONObject object,final @NonNull JSONArray rules){
        final String name = String.format(Locale.CHINA,"%s(%s)",getDiscountName(DISCOUNT_TYPE.FULL_REDUCE),object.getString("title")),
                time =  String.format(Locale.CHINA,"%s至%s",object.getString("start_time"),object.getString("end_time"));
        final JSONArray rules_des = new JSONArray();
        JSONObject rule_obj,tmp;
        double full_money = 0.0;
        for (int i = 0,size = rules.size();i < size;i++){
            tmp = rules.getJSONObject(i);
            rule_obj = new JSONObject();

            full_money = tmp.getDoubleValue("full_money");
            rule_obj.put("rule_des",mContext.getString(R.string.fullreduce_des_sz,String.valueOf(full_money),tmp.getString("reduce_money")));

            if (sale_amt >= full_money){
                if (tmp.containsKey("status")){
                    rule_obj.put("status",tmp.getIntValue("status"));
                }else
                    rule_obj.put("status",2);
            }else {
                rule_obj.put("status",3);
                rule_obj.put("diff_amt_des",mContext.getString(R.string.diff_des_sz,String.format(Locale.CHINA,"%.2f",full_money -sale_amt)));
            }

            rules_des.add(rule_obj);
        }
        mFullReduceRecord = new JSONObject();
        mFullReduceRecord.put("name",name);
        mFullReduceRecord.put("time",time);
        mFullReduceRecord.put("rules_des",rules_des);
    }

    public JSONObject getFullReduceRecord(){
        return mFullReduceRecord;
    }

    public void deleteFullReduceRecord(){
        if(mFullReduceRecord != null)mFullReduceRecord = null;
        deleteDiscountRecordForType(DISCOUNT_TYPE.FULL_REDUCE);
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
        String key;
        switch (type){
            case 1:
                key = "discount_amt"; //折扣金额
                break;
            case 2:
                key = "original_amt";//原价金额
                break;
                default:
                    key = "sale_amt";//销售金额
                    break;
        }
        double amt = 0.0;
        for (int i = 0,size = mDatas.size();i < size;i++){
            final JSONObject object = mDatas.getJSONObject(i);
            if (null != object)
                amt += object.getDoubleValue(key);
        }
        return amt;
    }
    public void setDatas(final JSONArray array){
        if (null != array)
            mDatas = array;
        else
            mDatas = new JSONArray();
    }

    public void setSingle(final boolean b){
        mSingleRefundStatus = b;
        notifyDataSetChanged();
    }
    public boolean getSingle(){
        return mSingleRefundStatus;
    }
    public boolean isEmpty(){
        return mDatas.isEmpty();
    }
}