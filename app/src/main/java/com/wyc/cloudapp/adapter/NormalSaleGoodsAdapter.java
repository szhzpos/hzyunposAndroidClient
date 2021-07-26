package com.wyc.cloudapp.adapter;

import android.app.Dialog;
import android.text.Html;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.callback.ClickListener;
import com.wyc.cloudapp.dialog.ChangeNumOrPriceDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.serialScales.GoodsWeighDialog;
import com.wyc.cloudapp.utils.FontSizeTagHandler;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public final class NormalSaleGoodsAdapter extends AbstractSaleGoodsAdapter {
    private GoodsWeighDialog mWeighDialog;
    public NormalSaleGoodsAdapter(final SaleActivity context){
        super(context);
    }

    @Override
    public void onBindViewHolder(@NonNull AbstractSaleGoodsAdapter.MyViewHolder myViewHolder, int i) {
        super.onBindViewHolder(myViewHolder,i);
        myViewHolder.itemView.setOnTouchListener(onTouchListener);
    }

    private final ClickListener onTouchListener = new ClickListener(v -> {
        setCurrentItemIndexAndItemView(v);
        addCurrentGoods(-Utils.getNotKeyAsNumberDefault(getCurrentContent(),"xnum",0.0));
    }, this::setSelectStatus);

    @Override
    public void addSaleGoods(final JSONObject goods){
        if (null != goods && !goods.isEmpty()){
            double xnum = goods.getDoubleValue("xnum");//新增的商品不存在xnum字段，xnum等于0.0。挂单、条码秤称重商品、买满赠送已经存在,以及该字段值不会为0
            boolean isBarcodeWeighingGoods = !Utils.getNullStringAsEmpty(goods,GoodsInfoViewAdapter.W_G_MARK).isEmpty(),isZero = Utils.equalDouble(xnum,0.0);
            if(!isBarcodeWeighingGoods && isZero && goods.getIntValue("type") == 2){//type 1 普通 2散装称重 3鞋帽
                if (mWeighDialog == null)mWeighDialog = new GoodsWeighDialog(mContext,mContext.getString(R.string.goods_i_sz));
                mWeighDialog.setOnYesOnclickListener(num -> {
                    if (!Utils.equalDouble(num,0.0)){
                        if (mWeighDialog.isContinuousWeighing()) {
                            if (isScalesWeighingGoods(getCurrentContent())){
                                num -= getScalesWeighingGoodsSumNum();
                                if (!Utils.equalDouble(num,0.0))updateSaleGoodsInfoPromotion(num, 0);
                            }
                        }else{
                            addSaleGoodsPromotion(goods,num,false);
                        }
                    }
                });
                if (mWeighDialog.isContinuousWeighing()){
                    addSaleGoodsPromotion(goods,1,false);
                }else{
                    mWeighDialog.setBarcodeId(goods.getIntValue("barcode_id"));
                    mWeighDialog.read();
                    mWeighDialog.show();
                }
            }else{
                if (!isBarcodeWeighingGoods && isZero)xnum = 1.0;
                addSaleGoodsPromotion(goods,xnum,isBarcodeWeighingGoods);
            }
        }
    }

    @Override
    public void updateSaleGoodsDialog(final short type){//type 0 修改数量 1修改价格 2打折
        final JSONObject cur_goods = getCurrentContent();
        if (!cur_goods.isEmpty()){
            ChangeNumOrPriceDialog dialog = null;
            switch (type){
                case 1:
                    dialog = new ChangeNumOrPriceDialog(mContext,"新价格",cur_goods.getString("price"));
                    break;
                case 2:
                    dialog = new ChangeNumOrPriceDialog(mContext, Html.fromHtml("折扣率<size value='14'>[1-10],10为不折扣</size> ",null,new FontSizeTagHandler(mContext)),
                            String.format(Locale.CHINA,"%.2f",Utils.getNotKeyAsNumberDefault(cur_goods,"discount",1.0) * 10),1.0,10.0);
                    break;
                case 0:
                    if(!GoodsInfoViewAdapter.isBarcodeWeighingGoods(cur_goods) && cur_goods.getIntValue("type") == 2){//type 1 普通 2散装称重 3鞋帽
                        if (mWeighDialog == null)mWeighDialog = new GoodsWeighDialog(mContext,mContext.getString(R.string.goods_i_sz));
                        mWeighDialog.setOnYesOnclickListener(num -> {
                            if (!Utils.equalDouble(num,0.0)){
                                if (mWeighDialog.isContinuousWeighing()) {
                                    if (isScalesWeighingGoods(getCurrentContent())){
                                        num -= getScalesWeighingGoodsSumNum();
                                        if (!Utils.equalDouble(num,0.0))updateSaleGoodsInfoPromotion(num, 0);
                                    }
                                }else{
                                    updateSaleGoodsInfoPromotion(num, 0);
                                }
                            }
                        });
                        if (mWeighDialog.isContinuousWeighing()){
                            updateSaleGoodsInfoPromotion(1, 0);
                        }else{
                            mWeighDialog.setBarcodeId(cur_goods.getIntValue("barcode_id"));
                            mWeighDialog.read();
                            mWeighDialog.show();
                        }
                    }else
                        dialog = new ChangeNumOrPriceDialog(mContext,"新数量",String.format(Locale.CHINA,"%.2f",Utils.getNotKeyAsNumberDefault(cur_goods,"xnum",1.0)));

                    break;
            }
            if (dialog != null)
                dialog.setYesOnclickListener(myDialog -> {
                    double content = myDialog.getContent();
                    if (verifyDiscountPermissions(content, type))return;

                    updateSaleGoodsInfoPromotion(content,type);
                    myDialog.dismiss();
                }).setNoOnclickListener(Dialog::dismiss).show();
        }else{
            MyDialog.ToastMessage("请选择需要修改的商品!", null);
        }
    }

    @Override
    public void clearGoods(){
        super.clearGoods();
        if (mWeighDialog != null){
            mWeighDialog.stopRead();
            mWeighDialog = null;
        }
        notifyDataSetChanged();
    }
}
