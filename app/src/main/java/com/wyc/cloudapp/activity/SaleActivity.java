package com.wyc.cloudapp.activity;

import android.os.Bundle;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.mobile.MobileCashierActivity;
import com.wyc.cloudapp.adapter.AbstractSaleGoodsAdapter;
import com.wyc.cloudapp.adapter.MobileSaleGoodsAdapter;
import com.wyc.cloudapp.adapter.NormalSaleGoodsAdapter;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;

public class SaleActivity extends MainActivity{
    protected AbstractSaleGoodsAdapter mSaleGoodsAdapter;
    protected JSONObject mVipInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (this instanceof MobileCashierActivity)
            mSaleGoodsAdapter = new MobileSaleGoodsAdapter(this);
        else
            mSaleGoodsAdapter = new NormalSaleGoodsAdapter(this);
    }

    public boolean allDiscount(double v){
        return mSaleGoodsAdapter.allDiscount(v);
    }

    public void deleteMolDiscountRecord(){
        mSaleGoodsAdapter.deleteMolDiscountRecord();
    }

    public void deleteAllDiscountRecord(){
        mSaleGoodsAdapter.deleteAllDiscountRecord();
    }

    public String discountRecordsToString(){
        return mSaleGoodsAdapter.discountRecordsToString();
    }

    public void autoMol(double mol_amt){
        mSaleGoodsAdapter.autoMol(mol_amt);
    }

    public void manualMol(double mol_amt){
        mSaleGoodsAdapter.manualMol(mol_amt);
    }

    public void fullReduceDiscount(){
        mSaleGoodsAdapter.fullReduceDiscount();
    }

    public void deleteFullReduce(){
        mSaleGoodsAdapter.deleteFullReduceRecord();
    }

    public JSONObject getFullReduceRecord(){
        return mSaleGoodsAdapter.getFullReduceRecord();
    }

    public JSONArray getSaleData(){
        return mSaleGoodsAdapter.getDatas();
    }

    public JSONArray getDiscountRecords(){
        return mSaleGoodsAdapter.getDiscountRecords();
    }

    public boolean splitCombinationalGoods(final JSONArray combination_goods,int gp_id,double gp_price,double gp_num,StringBuilder err){
        return mSaleGoodsAdapter.splitCombinationalGoods(combination_goods,gp_id,gp_price,gp_num,err);
    }

    public void alterGoodsNumber(){
        if (verifyNumBtnPermissions())mSaleGoodsAdapter.updateSaleGoodsDialog((short) 0);
    }

    public void discount(){
        mSaleGoodsAdapter.updateSaleGoodsDialog((short) 2);
    }

    public void deleteGoodsRecord(){
        mSaleGoodsAdapter.deleteSaleGoods(mSaleGoodsAdapter.getCurrentItemIndex(),0);
    }

    public void alterGoodsPrice(){
        mSaleGoodsAdapter.updateSaleGoodsDialog((short) 1);
    }

    public double getSumAmt(int type){
        return mSaleGoodsAdapter.getSumAmt(type);
    }
    @CallSuper
    public void clearVipInfo(){
        if (mVipInfo != null)mVipInfo = null;
    }

    public void setSingle(boolean b){
        if (mSaleGoodsAdapter != null) mSaleGoodsAdapter.setSingle(b);
        if (b)resetOrderCode();
    }

    public boolean getSingle(){
        return mSaleGoodsAdapter != null && mSaleGoodsAdapter.getSingle();
    }

    public boolean present(){
        return null != mSaleGoodsAdapter && mSaleGoodsAdapter.present();
    }

    public JSONObject getVipInfo(){
        return mVipInfo;
    }

    @CallSuper
    public void resetOrderInfo(){
        if (mSaleGoodsAdapter != null) mSaleGoodsAdapter.clearGoods();
        mPermissionCashierId = "";
        clearVipInfo();
        setSingle(false);
        resetOrderCode();
    }
    public boolean clearSaleGoods(){
        boolean code ;
        if (code = verifyClearPermissions()){
            if (code =(MyDialog.showMessageToModalDialog(this,"是否清除销售商品？") == 1)){
                resetOrderInfo();
            }
        }
        return code;
    }
    @CallSuper
    public void showVipInfo(final JSONObject vip){
        mVipInfo = vip;
    }

    @CallSuper
    public void minusOneGoods(){
        if (mSaleGoodsAdapter != null)mSaleGoodsAdapter.deleteSaleGoods(mSaleGoodsAdapter.getCurrentItemIndex(),1);
    }

    @CallSuper
    public void addOneSaleGoods(){
        final JSONObject object = Utils.JsondeepCopy(mSaleGoodsAdapter.getCurrentContent());
        if (!object.isEmpty()){
            object.put("xnum",1.0);
            mSaleGoodsAdapter.addSaleGoods(object);
        }
    }

    //以下方法子类重写
    public void clearSearchEt(){

    }
    public void loadGoods(final String id){}
    public void addSaleGoods(final @NonNull JSONObject jsonObject){}
    public void adjustPriceRefreshGoodsInfo(final JSONArray array){}
    public void showAdjustPriceDialog(){}
    public boolean getPrintStatus(){
        return false;
    }
    public void disposeHangBill(){}
    public String getOrderCode(){ return "";}
    public void resetOrderCode(){}
    public void triggerPsClick(){}


    public interface ScanCallback{
        void callback(final String code);
    }
    public void setScanCallback(final ScanCallback callback){
    }
    public void clearScanCallback(){
    }
}