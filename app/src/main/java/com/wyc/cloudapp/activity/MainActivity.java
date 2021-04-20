package com.wyc.cloudapp.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.VerifyPermissionDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public class MainActivity extends BaseActivity {
    protected final CustomApplication mApplication = CustomApplication.self();
    protected final String mAppId = mApplication.getAppId(), mAppSecret = mApplication.getAppSecret(),mUrl = mApplication.getUrl();
    protected String mPermissionCashierId = "";
    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);
        setIntent(intent);
    }

    protected boolean verifyNumBtnPermissions(){
        return verifyPermissions("25",null);
    }
    protected boolean verifyQueryBtnPermissions(){
        return verifyPermissions("26",null);
    }

    protected boolean verifyClearPermissions(){
        return verifyPermissions("2",null);
    }

    public boolean verifyOpenCashboxPermissions(){
        return verifyPermissions("5",null);
    }

    public String getNotEmptyHintsString(final String sz){
        return getString(R.string.not_empty_hint_sz,sz);
    }
    public String getNotExistHintsString(final String sz){
        return getString(R.string.not_exist_hint_sz,sz);
    }

    public String getPosNum(){return mApplication.getPosNum();}
    public String getCashierName(){
        return mApplication.getCashierName();
    }
    public String getCashierId(){
        return mApplication.getCashierId();
    }
    public String getCashierCode(){
        return mApplication.getCashierCode();
    }
    public String getStoreName(){
        return mApplication.getStoreName();
    }
    public String getStoreId(){
        return mApplication.getStoreId();
    }
    public String getPermissionCashierId(){
        if ("".equals(mPermissionCashierId))return mApplication.getCashierInfo().getString("cas_id");
        return mPermissionCashierId;
    }
    public String getPtUserId(){
        return mApplication.getPtUserId();
    }

    public boolean isConnection(){
        return mApplication.isConnection();
    }

    public void data_upload(){
        mApplication.data_upload();
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

    /*
    *@param per_id 权限编号
    *@param requested_cas_code 授权操作员编号。当当前操作员没有权限时可以通过此参数获取其他操作员的权限
    *@param isShow 是否显示授权界面
    * */
    public boolean verifyPermissions(final String per_id,final String requested_cas_code,boolean isShow){
        boolean code = false;
        final JSONObject mCashierInfo = mApplication.getCashierInfo(),mStoreInfo = mApplication.getStoreInfo();
        String cashier_id = Utils.getNullStringAsEmpty(mCashierInfo,"cas_code"),cas_pwd = Utils.getNullStringAsEmpty(mCashierInfo,"cas_pwd"),stores_id = mStoreInfo.getString("stores_id");
        final StringBuilder err = new StringBuilder();
        if (null != requested_cas_code){
            cas_pwd = Utils.getUserIdAndPasswordCombinationOfMD5(requested_cas_code);
            Logger.i("操作员:%s,向:%s请求权限:%s",cashier_id,cas_pwd,per_id);
        }
        final String authority = SQLiteHelper.getString("SELECT authority FROM cashier_info where cas_pwd = '" + cas_pwd +"' and stores_id = " + stores_id,err);
        if (Utils.isNotEmpty(authority)){
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
                    MyDialog.displayErrorMessage(this, "未找到授权工号的权限记录,请确定输入是否正确!");
                }
            }catch (JSONException e){
                e.printStackTrace();
                MyDialog.displayErrorMessage(this, "权限数据解析错误：" + e.getMessage());
            }
        }else {
            MyDialog.displayErrorMessage(this, "权限查询错误：" + err);
        }
        return code;
    }
    public boolean verifyDiscountPermissions(double discount,final String requested_cas_code){
        boolean code = false;
        final JSONObject mCashierInfo = mApplication.getCashierInfo(),mStoreInfo = mApplication.getStoreInfo();
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
                MyDialog.displayErrorMessage(this, "未找到授权工号的权限记录,请确定输入是否正确!");
            }
        } else {
            MyDialog.displayErrorMessage(this, "权限查询错误：" + discount_ojb.getString("info"));
        }
        return code;
    }

    public boolean lessThan7Inches(){
        return Utils.lessThan7Inches(this);
    }

    public int getStatusBarHeight() {
        if (isStatusBarShow()){
            final Resources resources = getResources();
            int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
            return resources.getDimensionPixelSize(resourceId);
        }
        return 0;
    }
    private boolean isStatusBarShow(){
        final WindowManager.LayoutParams params = getWindow().getAttributes();
        int paramsFlag = params.flags & (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        return paramsFlag == params.flags;
    }

    //业务公共方法
    public void switchPrintStatus(){
        final StringBuilder err = new StringBuilder();
        final JSONObject object = new JSONObject();
        if (SQLiteHelper.getLocalParameter("print_s",object)){

            boolean print_s = object.getBooleanValue("v");
            if (print_s){
                Toast.makeText(this,"打印功能已关闭！",Toast.LENGTH_SHORT).show();
            }else
                Toast.makeText(this,"打印功能已开启！",Toast.LENGTH_SHORT).show();

            object.put("v",!print_s);
            if (!SQLiteHelper.saveLocalParameter("print_s",object,"打印开关",err)){
                MyDialog.ToastMessage("保存打印状态错误:" + err,this,getWindow());
            }
        }
    }

    public boolean getPrintStatus(){
        final JSONObject object = new JSONObject();
        if (SQLiteHelper.getLocalParameter("print_s",object)){
            return object.getBooleanValue("v");
        }
        return false;
    }


    public void disposeHangBill(){}

    public JSONArray getSaleData(){
        return new JSONArray();
    }
    public boolean getSingleRefundStatus(){
        return false;
    }
    public void setSingleRefundStatus(boolean b){};

    public void resetOrderInfo(){};

    public interface ScanCallback{
        void callback(final String code);
    }
    public void setScanCallback(final ScanCallback callback){
    }
}
