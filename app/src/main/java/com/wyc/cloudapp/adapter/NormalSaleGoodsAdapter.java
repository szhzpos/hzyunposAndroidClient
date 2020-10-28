package com.wyc.cloudapp.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.serialScales.GoodsWeighDialog;
import com.wyc.cloudapp.utils.Utils;

public final class NormalSaleGoodsAdapter extends AbstractSaleGoodsAdapter {
    private GoodsWeighDialog mWeighDialog;
    public NormalSaleGoodsAdapter(final MainActivity context){
        super(context);
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View itemView = View.inflate(mContext, R.layout.normal_sale_goods_content_layout, null);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,(int)mContext.getResources().getDimension(R.dimen.sale_goods_height)));
        return new MyViewHolder(itemView);
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
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
