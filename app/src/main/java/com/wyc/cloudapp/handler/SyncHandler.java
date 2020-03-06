package com.wyc.cloudapp.handler;

import android.os.Handler;
import android.os.Message;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.http.HttpRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.wyc.cloudapp.utils.MessageID.SYNC_DIS_INFO_ID;

public class SyncHandler extends Handler {
    private HttpRequest mHttp;
    private Handler syncActivityHandler;
    private JSONObject mCashierInfo,mStoreInfo;
    private String mAppId,mAppScret,mUrl;
    public SyncHandler(Handler handler){
        this.syncActivityHandler = handler;
        mHttp = new HttpRequest();

        initSyncParam();
    }

    private void initSyncParam(){
        mCashierInfo = new JSONObject();
        mStoreInfo = new JSONObject();
        if (SQLiteHelper.getLocalParameter("cashierInfo",mCashierInfo)){
            if (SQLiteHelper.getLocalParameter("connParam",mStoreInfo)){
                try {
                    mUrl = mStoreInfo.getString("server_url");
                    mAppId = mStoreInfo.getString("appId");
                    mAppScret = mStoreInfo.getString("appScret");
                    mStoreInfo = new JSONObject(mStoreInfo.getString("storeInfo"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID,"初始化同步参数错误：" + e.getMessage()).sendToTarget();
                }
            }else{
                syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID,"初始化同步参数错误：" + mCashierInfo.optString("info")).sendToTarget();
            }
        }else{
            syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID,"初始化同步参数错误：" + mStoreInfo.optString("info")).sendToTarget();
        }
    }

    @Override
    public void handleMessage(Message msg){
        JSONObject object = new JSONObject(),info_json,retJson;
        String table_name = "",sys_name = "",url = "",sz_param = "";
        String[] table_cls = null;
        StringBuilder err = new StringBuilder();
        try{
            switch (msg.what) {
                case MessageID.SYNC_GOODS_BASE_ID:

                    break;
                case MessageID.SYNC_GOODS_CATEGORY_ID:
                    table_name = "shop_category";
                    sys_name = "正在同步商品类别";
                    table_cls = new String[]{"category_id","name","parent_id","depth","path","status","sort"};
                    url = mUrl + "/api/scale/get_category_info";
                    object.put("pos_num",mCashierInfo.getString("pos_num"));
                    break;
                case MessageID.SYNC_STORES_ID://仓库信息
                    table_name = "shop_stores";
                    table_cls = new String[]{"stores_id","stores_name","manager","telphone","region","status","nature"};
                    sys_name = "正在同步仓库";
                    url = mUrl + "/api/scale/get_stores";
                    object.put("pt_user_id",mCashierInfo.getString("cas_id"));
                    break;
                case MessageID.SYNC_GOODS_ID://商品信息
                    table_name = "barcode_info";
                    sys_name = "正在同步商品";
                    table_cls = new String[]{"goods_id","barcode_id","barcode","goods_title","only_coding","retail_price","buying_price","trade_price","cost_price","ps_price",
                    "unit_id","unit_name","specifi","category_name","metering_id","shelf_life","goods_status","brand","origin","type","goods_tare","barcode_status","category_id",
                    "tax_rate","tc_mode","tc_rate","yh_mode","yh_price","mnemonic_code","image","attr_id","attr_name","attr_code","conversion","update_price","stock_unit_id","stock_unit_name"};
                    url = mUrl + "/api/goods/get_goods_all";
                    object.put("pos_num",mCashierInfo.getString("pos_num"));
                    break;
                case MessageID.SYNC_PAY_METHOD_ID://支付方式
                    table_name = "pay_method";
                    table_cls = new String[]{"pay_method_id","name","status","remark","is_check","shortcut_key","sort","xtype","pay_img","master_img",
                            "is_show_client","is_cardno","is_scan","wr_btn_img","unified_pay_order","unified_pay_query","rule","is_open","is_enable"};
                    sys_name = "正在同步支付方式";
                    url = mUrl + "/api/cashier/get_pm_info";
                    object.put("stores_id",mStoreInfo.getString("stores_id"));
                    object.put("pos_num",mCashierInfo.getString("pos_num"));
                    break;
                case MessageID.SYNC_CASHIER_ID://收银员
                    table_name = "cashier_info";
                    sys_name = "正在同步门店收银员";
                    url = mUrl + "/api/cashier_dwn/get_cashier_info";

                    object.put("cas_id",mCashierInfo.getString("cas_id"));
                    object.put("pos_num",mCashierInfo.getString("pos_num"));
                    object.put("stores_id",mStoreInfo.getString("stores_id"));
                    break;
            }
            if (msg.what == MessageID.SYNC_FINISH_ID){
                syncActivityHandler.obtainMessage(MessageID.SYNC_FINISH_ID).sendToTarget();//同步完成
            }else{
                syncActivityHandler.obtainMessage(SYNC_DIS_INFO_ID,sys_name + "信息....").sendToTarget();

                object.put("appid",mAppId);

                sz_param = HttpRequest.generate_request_parm(object,mAppScret);
                retJson = mHttp.sendPost(url,sz_param,true);
                switch (retJson.optInt("flag")) {
                    case 0:
                        this.removeCallbacksAndMessages(null);
                        syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, sys_name + "错误:" +  retJson.optString("info")).sendToTarget();
                        break;
                    case 1:
                        info_json = new JSONObject(retJson.optString("info"));
                        switch (info_json.optString("status")){
                            case "n":
                                this.removeCallbacksAndMessages(null);
                                syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, sys_name.concat("错误：").concat(info_json.optString("info"))).sendToTarget();
                                break;
                            case "y":
                                JSONArray data = info_json.getJSONArray("data");
                                if(data.length() != 0){
                                    if (!SQLiteHelper.execSQLByBatchReplaceJson(data,table_name ,table_cls,err)) {
                                        this.removeCallbacksAndMessages(null);
                                        syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, sys_name + "错误：" + err).sendToTarget();
                                    }
                                }
                                break;
                        }
                        break;
                }
            }
        }catch (JSONException e){
            this.removeCallbacksAndMessages(null);
            syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, "同步" + table_name + "错误:" +  e.getMessage()).sendToTarget();
        }
    }

    private void parse_category_info(JSONArray category_jsons,JSONArray categorys){
        for(int i = 0,length = category_jsons.length();i < length;i++) {
            JSONObject category_json = category_jsons.optJSONObject(i);
            if (category_json.has("childs")) {
                JSONArray childs = (JSONArray) category_json.remove("childs");
                if(childs != null && childs.length() != 0){
                    parse_category_info(childs,categorys);
                }
            }
            categorys.put(category_json);
        }
    }

}
