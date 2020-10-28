package com.wyc.cloudapp.activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.EditText;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractSaleGoodsAdapter;
import com.wyc.cloudapp.adapter.SuperItemDecoration;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.VerifyPermissionDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.network.sync.SyncManagement;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class MainActivity extends AppCompatActivity {
    protected AbstractSaleGoodsAdapter mNormalSaleGoodsAdapter;
    protected EditText mSearch_content;
    protected JSONObject mCashierInfo,mStoreInfo,mVipInfo;
    protected CustomProgressDialog mProgressDialog;
    protected MyDialog mDialog;
    protected String mAppId, mAppSecret,mUrl;
    protected RecyclerView mSaleGoodsRecyclerView;
    protected AtomicBoolean mNetworkStatus;
    private String mPermissionCashierId = "";
    private SyncManagement mSyncManagement;
    @CallSuper
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initMemberVariable();

        //初始化收银员、仓库信息
        initCashierInfoAndStoreInfo();

        //初始化数据管理对象
        initSyncManagement();

        initNetworkStatus();
    }
    private void initMemberVariable(){
        mProgressDialog = new CustomProgressDialog(this);
        mDialog = new MyDialog(this);
    }
    @Override
    public void onResume(){
        super.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
    }

    @CallSuper
    @Override
    public void onDestroy(){
        super.onDestroy();
        //清除资源
        clearResource();
    }
    @Override
    public void finalize(){
        Logger.d("SuperMainActivity finalized");
    }
    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        setIntent(intent);
    }


    private void initSyncManagement(){
        mSyncManagement = new SyncManagement(mUrl,mAppId, mAppSecret,mStoreInfo.getString("stores_id"),mCashierInfo.getString("pos_num"),mCashierInfo.getString("cas_id"));
    }

    protected void setSyncManagementNotifyHandler(final Handler handler){
        if (mSyncManagement != null)mSyncManagement.setNotifyHandlerAndStart(handler);
    }

    protected void firstSync(){
        if (mSyncManagement != null){
            mSyncManagement.sync_order_info();
            mSyncManagement.start_sync(false);
        }
    }

    public void data_upload(){
        if (mSyncManagement != null)mSyncManagement.sync_order_info();
    }

    protected void sync_transfer_order(){
        if (mSyncManagement != null)mSyncManagement.sync_transfer_order();
    }

    protected void sync_retail_order(){
        if (mSyncManagement != null)mSyncManagement.sync_retail_order();
    }

    protected void start_sync(boolean b){
        if (mSyncManagement != null)mSyncManagement.start_sync(b);
    }

    private void initNetworkStatus(){
        final Intent intent = getIntent();
        boolean code = intent.getBooleanExtra("network",false);
        mNetworkStatus = new AtomicBoolean(code);
    }

    protected boolean verifyNumBtnPermissions(){
        return verifyPermissions("25",null);
    }
    protected boolean verifyQueryBtnBtnPermissions(){
        return verifyPermissions("26",null);
    }

    protected boolean verifyClearPermissions(){
        return verifyPermissions("2",null);
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
    protected void clearResource(){
        if (mSyncManagement != null) {
            mSyncManagement.quit();
        }
        if (mProgressDialog.isShowing())mProgressDialog.dismiss();
        if (mDialog.isShowing())mDialog.dismiss();
    }
    private void initCashierInfoAndStoreInfo(){
        final JSONObject cas_info = mCashierInfo = new JSONObject();
        final JSONObject st_info = mStoreInfo = new JSONObject();
        if (SQLiteHelper.getLocalParameter("cashierInfo",cas_info)){
            if (SQLiteHelper.getLocalParameter("connParam",st_info)){
                try {
                    mUrl = st_info.getString("server_url");
                    mAppId = st_info.getString("appId");
                    mAppSecret = st_info.getString("appSecret");
                    mStoreInfo = JSON.parseObject(st_info.getString("storeInfo"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    mDialog.setMessage(e.getMessage()).setNoOnclickListener("取消", myDialog -> this.finish()).show();
                }
            }else{
                mDialog.setMessage(cas_info.getString("info")).setNoOnclickListener("取消", myDialog -> this.finish()).show();
            }
        }else{
            mDialog.setMessage(st_info.getString("info")).setNoOnclickListener("取消", myDialog -> this.finish()).show();
        }
    }

    protected void reSizeSaleGoodsView(){
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(mSaleGoodsRecyclerView,getResources().getDimension(R.dimen.sale_goods_height),null);
        mSaleGoodsRecyclerView.scrollToPosition(0);
    }

    @CallSuper
    public void resetOrderInfo(){
        mPermissionCashierId = "";
        if (mNormalSaleGoodsAdapter != null)mNormalSaleGoodsAdapter.clearGoods();
        clearVipInfo();
        setSingle(false);
        resetOrderCode();
    }

    @CallSuper
    public void clearVipInfo(){
        if (mVipInfo != null)mVipInfo = null;
    }

    public boolean allDiscount(double v){
        return mNormalSaleGoodsAdapter.allDiscount(v);
    }
    public void deleteMolDiscountRecord(){
        mNormalSaleGoodsAdapter.deleteMolDiscountRecord();
    }
    public void deleteAllDiscountRecord(){
        mNormalSaleGoodsAdapter.deleteAllDiscountRecord();
    }
    public String discountRecordsToString(){
        return mNormalSaleGoodsAdapter.discountRecordsToString();
    }
    public void autoMol(double mol_amt){
        mNormalSaleGoodsAdapter.autoMol(mol_amt);
    }
    public void manualMol(double mol_amt){
        mNormalSaleGoodsAdapter.manualMol(mol_amt);
    }
    public void fullReduceDiscount(){
        mNormalSaleGoodsAdapter.fullReduceDiscount();
    }
    public void deleteFullReduce(){
        mNormalSaleGoodsAdapter.deleteFullReduceRecord();
    }
    public JSONObject getFullReduceRecord(){
        return mNormalSaleGoodsAdapter.getFullReduceRecord();
    }
    public void manualSync(){
        if (mSyncManagement != null){
            if (mProgressDialog != null && !mProgressDialog.isShowing())mProgressDialog.setMessage("正在同步...").refreshMessage().show();
            mSyncManagement.afresh_sync();
        }
    }
    public void sync_refund_order(){
        if (mSyncManagement != null) mSyncManagement.sync_refund_order();
    }
    public JSONArray getSaleData(){
        return mNormalSaleGoodsAdapter.getDatas();
    }
    public JSONArray getDiscountRecords(){
        return mNormalSaleGoodsAdapter.getDiscountRecords();
    }
    public boolean splitCombinationalGoods(final JSONArray combination_goods,int gp_id,double gp_price,double gp_num,StringBuilder err){
        return mNormalSaleGoodsAdapter.splitCombinationalGoods(combination_goods,gp_id,gp_price,gp_num,err);
    }
    public double getSumAmt(int type){
        return mNormalSaleGoodsAdapter.getSumAmt(type);
    }
    public String getPosNum(){if (null == mCashierInfo)return "";return mCashierInfo.getString("pos_num");}
    public JSONObject getCashierInfo(){
        return mCashierInfo;
    }
    public JSONObject getStoreInfo(){
        return mStoreInfo;
    }
    public String getPermissionCashierId(){
        if ("".equals(mPermissionCashierId))return mCashierInfo.getString("cas_id");
        return mPermissionCashierId;
    }

    public String getAppId(){
        return mAppId;
    }
    public String getAppSecret(){
        return mAppSecret;
    }
    public String getUrl(){
        return mUrl;
    }

    public boolean verifyPermissions(final String per_id,final String requested_cas_code){
        return verifyPermissions(per_id,requested_cas_code,true);
    }
    public boolean verifyPermissions(final String per_id,final String requested_cas_code,boolean isShow){
        boolean code = false;
        if (mCashierInfo != null && mStoreInfo != null){
            String cashier_id = Utils.getNullStringAsEmpty(mCashierInfo,"cas_code"),cas_pwd = Utils.getNullStringAsEmpty(mCashierInfo,"cas_pwd"),stores_id = mStoreInfo.getString("stores_id");
            final StringBuilder err = new StringBuilder();
            if (null != requested_cas_code){
                cas_pwd = Utils.getUserIdAndPasswordCombinationOfMD5(requested_cas_code);
                Logger.i("操作员:%s,向:%s请求权限:%s",cashier_id,cas_pwd,per_id);
            }
            final String authority = SQLiteHelper.getString("SELECT authority FROM cashier_info where cas_pwd = '" + cas_pwd +"' and stores_id = " + stores_id,err);
            if (null != authority){
                try {
                    JSONArray permissions;
                    if (authority.startsWith("{")){
                        final JSONObject jsonObject = JSON.parseObject(authority);
                        permissions = new JSONArray();
                        for (String key : jsonObject.keySet()){
                            permissions.add(jsonObject.get(key));
                        }
                    }else {
                        permissions = JSON.parseArray(authority);
                    }
                    if (permissions != null){
                        boolean isExist = false;
                        JSONObject obj = null;
                        for (int i = 0,size = permissions.size();i < size;i ++){
                            obj = permissions.getJSONObject(i);
                            if (obj != null){
                                if (Utils.getNullStringAsEmpty(obj,"authority").equals(per_id)){
                                    code = (1 == obj.getIntValue("is_have"));
                                    isExist = true;
                                    break;
                                }
                            }
                        }
                        if (!isExist || !code){
                            if (isShow){
                                final VerifyPermissionDialog verifyPermissionDialog = new VerifyPermissionDialog(this);
                                if (isExist)
                                    verifyPermissionDialog.setHintPerName(Utils.getNullStringAsEmpty(obj,"authority_name"));
                                else
                                    verifyPermissionDialog.setHintPerName(String.format(Locale.CHINA,"权限编号为%s",per_id));
                                verifyPermissionDialog.setFinishListener(dialog -> {
                                    if (verifyPermissions(per_id,dialog.getContent(),true)){
                                        dialog.setCodeAndExit(1);
                                    }else{
                                        dialog.setCodeAndExit(0);
                                    }

                                });
                                code = verifyPermissionDialog.exec() == 1;
                                verifyPermissionDialog.dismiss();
                            }
                        }else {
                            mPermissionCashierId = Utils.getNullStringAsEmpty(obj,"cas_id");
                        }
                    }else {
                        MyDialog.displayErrorMessage(mDialog,"未找到授权工号的权限记录,请确定输入是否正确!",this);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.displayErrorMessage(mDialog,"权限数据解析错误：" + e.getMessage(),this);
                }
            }else {
                MyDialog.displayErrorMessage(mDialog,"权限查询错误：" + err,this);
            }
        }
        return code;
    }
    public boolean verifyDiscountPermissions(double discount,final String requested_cas_code){
        boolean code = false;
        if (mCashierInfo != null && mStoreInfo != null){
            String cashier_id = Utils.getNullStringAsEmpty(mCashierInfo,"cas_code"),cas_pwd = Utils.getNullStringAsEmpty(mCashierInfo,"cas_pwd"),stores_id = mStoreInfo.getString("stores_id");
            if (null != requested_cas_code){
                cas_pwd = Utils.getUserIdAndPasswordCombinationOfMD5(requested_cas_code);
                Logger.i("操作员:%s,向:%s请求折扣为%f的权限",cashier_id,cas_pwd,discount);
            }
            final JSONObject discount_ojb = new JSONObject();
            if (SQLiteHelper.execSql(discount_ojb,"SELECT ifnull(min_discount,0.0) min_discount,cas_id FROM cashier_info where cas_pwd = '" + cas_pwd +"' and stores_id = " + stores_id)){
                if (!discount_ojb.isEmpty()){
                    double local_dis = discount_ojb.getDoubleValue("min_discount") / 10;

                    Logger.d("local_dis:%f,discount:%f",local_dis,discount);

                    if (local_dis > discount){
                        final VerifyPermissionDialog verifyPermissionDialog = new VerifyPermissionDialog(this);
                        verifyPermissionDialog.setHintPerName(String.format(Locale.CHINA,"%.1f%s",discount * 10,"折"));
                        verifyPermissionDialog.setFinishListener(dialog -> {
                            if (verifyDiscountPermissions(discount,dialog.getContent())){
                                dialog.setCodeAndExit(1);
                            }else
                                dialog.setCodeAndExit(0);
                        });
                        code = verifyPermissionDialog.exec() == 1;
                    }else {
                        mPermissionCashierId = discount_ojb.getString("cas_id");
                        code = true;
                    }
                }else{
                    MyDialog.displayErrorMessage(mDialog,"未找到授权工号的权限记录,请确定输入是否正确!",this);
                }
            } else {
                MyDialog.displayErrorMessage(mDialog,"权限查询错误：" + discount_ojb.getString("info"),this);
            }
        }
        return code;
    }

    public void setSingle(boolean b){
        if (mNormalSaleGoodsAdapter != null) mNormalSaleGoodsAdapter.setSingle(b);
        if (b)resetOrderCode();
    }
    public boolean getSingle(){
        return mNormalSaleGoodsAdapter != null && mNormalSaleGoodsAdapter.getSingle();
    }
    public boolean present(){
        return null != mNormalSaleGoodsAdapter && mNormalSaleGoodsAdapter.present();
    }

    public JSONObject getVipInfo(){
        return mVipInfo;
    }

    public boolean isConnection(){
        return mNetworkStatus.get();
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

    @CallSuper
    public void showVipInfo(final JSONObject vip){
        mVipInfo = vip;
    }
    public void triggerPsClick(){}
}
