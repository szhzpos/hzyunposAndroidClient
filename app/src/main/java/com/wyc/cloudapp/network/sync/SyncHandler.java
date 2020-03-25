package com.wyc.cloudapp.network.sync;

import android.content.ContentValues;
import android.os.Handler;
import android.os.Message;

import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;

import static com.wyc.cloudapp.utils.MessageID.SYNC_DIS_INFO_ID;

public class SyncHandler extends Handler {
    private HttpRequest mHttp;
    private Handler syncActivityHandler;
    private String mAppId,mAppScret,mUrl,mPosNum,mOperId,mStoresId;
    private boolean mReportProgress = true;
    private int mCurrentNeworkStatusCode = HttpURLConnection.HTTP_OK;
    private long mSyncInterval = 3000,mLoseTime = 0;//mSyncInterval 同步时间间隔，默认3秒
    SyncHandler(Handler handler,final String url, final String appid, final String appscret,final String stores_id,final String pos_num, final String operid){
        this.syncActivityHandler = handler;
        mHttp = new HttpRequest();
        mHttp.setConnTimeOut(3000);

        mOperId = operid;
        mPosNum = pos_num;
        mUrl = url;
        mAppId = appid;
        mAppScret = appscret;
        mStoresId = stores_id;
    }
    @Override
    public void handleMessage(Message msg){
        JSONObject object = new JSONObject(),info_json,retJson;
        String table_name = "",sys_name = "",url = "",sz_param = "",img_url_info,img_file_name,img_url_col_name = null;
        String[] table_cls = null;
        boolean code = true;
        try{
            switch (msg.what) {
                case MessageID.SYNC_GOODS_BASE_ID:

                    break;
                case MessageID.SYNC_GOODS_CATEGORY_ID:
                    table_name = "shop_category";
                    sys_name = "正在同步商品类别";
                    table_cls = new String[]{"category_id","name","parent_id","depth","path","status","sort"};
                    url = mUrl + "/api/scale/get_category_info";
                    object.put("pos_num",mPosNum);
                    break;
                case MessageID.SYNC_STORES_ID://仓库信息
                    table_name = "shop_stores";
                    table_cls = new String[]{"stores_id","stores_name","manager","telphone","region","status","nature"};
                    sys_name = "正在同步仓库";
                    url = mUrl + "/api/scale/get_stores";
                    object.put("pt_user_id",mOperId);
                    break;
                case MessageID.SYNC_GOODS_ID://商品信息
                    img_url_col_name = "img_url";
                    table_name = "barcode_info";
                    sys_name = "正在同步商品";
                    table_cls = new String[]{"goods_id","barcode_id","barcode","goods_title","only_coding","retail_price","buying_price","trade_price","cost_price","ps_price",
                    "unit_id","unit_name","specifi","category_name","metering_id","shelf_life","goods_status","brand","origin","type","goods_tare","barcode_status","category_id",
                    "tax_rate","tc_mode","tc_rate","yh_mode","yh_price","mnemonic_code","image","attr_id","attr_name","attr_code","conversion","update_price","stock_unit_id","stock_unit_name","img_url"};
                    url = mUrl + "/api/goods/get_goods_all";
                    object.put("pos_num",mPosNum);
                    break;
                case MessageID.SYNC_PAY_METHOD_ID://支付方式
                    table_name = "pay_method";
                    img_url_col_name = "pay_img";
                    table_cls = new String[]{"pay_method_id","name","status","remark","is_check","shortcut_key","sort","xtype","pay_img","master_img",
                            "is_show_client","is_cardno","is_scan","wr_btn_img","unified_pay_order","unified_pay_query","rule","is_open","is_enable","support"};
                    sys_name = "正在同步支付方式";
                    url = mUrl + "/api/cashier/get_pm_info";
                    object.put("stores_id",mStoresId);
                    object.put("pos_num",mPosNum);
                    break;
                case MessageID.SYNC_CASHIER_ID://收银员
                    table_name = "cashier_info";
                    table_cls = new String[]{"cas_id","stores_id","stores_name","cas_name","cas_account","cas_pwd","cas_addtime","cas_code","cas_phone","cas_status",
                            "min_discount","is_refund","is_give","is_put","pt_user_id","pt_user_cname","remark","authority"};

                    sys_name = "正在同步门店收银员";
                    url = mUrl + "/api/cashier_dwn/get_cashier_info";

                    object.put("cas_id",mOperId);
                    object.put("pos_num",mPosNum);
                    object.put("stores_id",mStoresId);
                    break;
                case MessageID.SYNC_GP_INFO_ID://商品组合信息
                    sys_name = "正在同步商品组合信息";
                    url = mUrl + "/api/promotion/get_gp_info";

                    object.put("pos_num",mPosNum);
                    object.put("stores_id",mStoresId);
                    break;
                case MessageID.SYNC_FINISH_ID:
                    syncActivityHandler.obtainMessage(MessageID.SYNC_FINISH_ID).sendToTarget();//同步完成
                    return;
                case MessageID.NETWORKSTATUS_ID:
                    testNetworkStatus();
                    if (System.currentTimeMillis() - mLoseTime >= mSyncInterval && mCurrentNeworkStatusCode == HttpURLConnection.HTTP_OK){
                        mLoseTime = System.currentTimeMillis();
                        sync();
                    }
                    return;
                case MessageID.UPLOAD_ORDER_ID:
                    uploadOrderInfo();
                    return;
            }

            if (mReportProgress)
                syncActivityHandler.obtainMessage(SYNC_DIS_INFO_ID,sys_name + "信息....").sendToTarget();

            object.put("appid",mAppId);

            sz_param = HttpRequest.generate_request_parm(object,mAppScret);
            retJson = mHttp.sendPost(url,sz_param,true);
            switch (retJson.optInt("flag")) {
                case 0:
                    sys_name = sys_name.concat("错误:").concat(retJson.optString("info"));
                    if (mReportProgress) {
                        removeCallbacksAndMessages(null);
                        syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, sys_name).sendToTarget();
                    }else {
                        syncActivityHandler.obtainMessage(MessageID.TRANSFERSTATUS_ID,false).sendToTarget();
                        Logger.e("%s", sys_name);
                    }
                    break;
                case 1:
                    info_json = new JSONObject(retJson.optString("info"));
                    switch (info_json.optString("status")){
                        case "n":
                            sys_name = sys_name.concat("错误:").concat(info_json.optString("info"));
                            if (mReportProgress) {
                                removeCallbacksAndMessages(null);
                                syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, sys_name).sendToTarget();
                            } else{
                                syncActivityHandler.obtainMessage(MessageID.TRANSFERSTATUS_ID,false).sendToTarget();
                                Logger.e("%s", sys_name);
                            }
                            break;
                        case "y":
                            JSONArray data = info_json.getJSONArray("data");
                            if(data.length() != 0){
                                if (img_url_col_name != null){
                                    for (int k = 0,length = data.length();k < length;k++){
                                        img_url_info = data.getJSONObject(k).getString(img_url_col_name);
                                        if (!img_url_info.equals("")){
                                            img_file_name = img_url_info.substring(img_url_info.lastIndexOf("/") + 1);
                                            File file = new File(SQLiteHelper.IMG_PATH + img_file_name);
                                            if (!file.exists()){
                                                JSONObject load_img = mHttp.getFile(file,img_url_info);
                                                if (load_img.getInt("flag") == 0){
                                                    Logger.e("下载商品图片错误：" + load_img.getString("info"));
                                                }
                                            }
                                        }
                                    }
                                }

                                StringBuilder err = new StringBuilder();

                                if (msg.what == MessageID.SYNC_GP_INFO_ID){//商品组合信息要单独处理
                                    JSONArray goods_list = new JSONArray();
                                    JSONObject tmp_goods = new JSONObject();
                                    for (int k = 0,size = data.length();k < size;k++){
                                        JSONArray tmp = (JSONArray) data.getJSONObject(k).remove("goods_list");
                                        if (tmp != null)
                                        for (int j = 0,length = tmp.length();j < length;j++){
                                            goods_list.put(tmp.get(j));
                                        }
                                    }
                                    tmp_goods.put("goods_group",data);
                                    tmp_goods.put("goods_group_info",goods_list);
                                     List<String> goods_group_cols = Arrays.asList("mnemonic_code","gp_id","gp_code","gp_title","gp_price","status","addtime","unit_name","stores_id","img_url"),
                                            goods_group_info_cols = Arrays.asList("xnum","barcode_id","gp_id","_id");

                                    code = SQLiteHelper.execSQLByBatchForJson(tmp_goods,Arrays.asList("goods_group","goods_group_info"),Arrays.asList(goods_group_cols,goods_group_info_cols),err,1);
                                }else{
                                    code = SQLiteHelper.execSQLByBatchForJson(data,table_name ,table_cls,err);
                                }
                                if (!code) {
                                    if (mReportProgress) {
                                        sys_name = sys_name.concat("错误:").concat(err.toString());
                                        removeCallbacksAndMessages(null);
                                        syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, sys_name).sendToTarget();
                                    }else{
                                        syncActivityHandler.obtainMessage(MessageID.TRANSFERSTATUS_ID,false).sendToTarget();
                                        Logger.e("%s", sys_name);
                                    }
                                }else{
                                    if (!mReportProgress)
                                        syncActivityHandler.obtainMessage(MessageID.TRANSFERSTATUS_ID,true).sendToTarget();
                                }
                            }
                            break;
                    }
                    break;
            }
        }catch (JSONException e){
            this.removeCallbacksAndMessages(null);
            sys_name = "同步" + table_name + "错误:" +  e.getMessage();
            if (mReportProgress) {
                removeCallbacksAndMessages(null);
                syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, sys_name).sendToTarget();
            }else
                Logger.e("%s",sys_name);
        }
    }

    private void testNetworkStatus(){
        JSONObject data = new JSONObject(),retJson,info_json;
        final String prefix = "网络检测错误：",test_url = mUrl + "/api/heartbeat/index";
        int err_code;
        try {
            data.put("appid",mAppId);
            data.put("pos_num",mPosNum);
            data.put("randstr", Utils.getNonce_str(8));
            data.put("cas_id",mOperId);
            retJson = mHttp.sendPost(test_url,HttpRequest.generate_request_parm(data,mAppScret),true);
            err_code = retJson.getInt("rsCode");
            switch (retJson.optInt("flag")) {
                case 0:
                    if (mCurrentNeworkStatusCode != err_code){
                        Logger.e("连接服务器错误：" + retJson.optString("info"));
                    }
                    syncActivityHandler.obtainMessage(MessageID.NETWORKSTATUS_ID,false).sendToTarget();
                    break;
                case 1:
                    syncActivityHandler.obtainMessage(MessageID.NETWORKSTATUS_ID,true).sendToTarget();
                    if (mCurrentNeworkStatusCode != HttpURLConnection.HTTP_OK){//如果之前网络响应状态不为OK,则重连成功
                        Logger.i("重新连接服务器成功！");
                    }
                    info_json = new JSONObject(retJson.getString("info"));
                    switch (info_json.getString("status")){
                        case "n":
                            syncActivityHandler.obtainMessage(MessageID.NETWORKSTATUS_ID,false).sendToTarget();
                            Logger.e(prefix + info_json.optString("info"));
                            break;
                        case "y":
                            //Logger.json(info_json.toString());
                            break;
                    }
                    break;
            }
            mCurrentNeworkStatusCode = err_code;
        } catch (JSONException e) {
            Logger.e("检测网络错误：" + e.getMessage());
            e.printStackTrace();
        }

    }
    private void uploadOrderInfo() {
        boolean code = true;
        final StringBuilder err = new StringBuilder(),order_gp_ids = new StringBuilder();
        final String sql_orders = "SELECT discount_money,card_code,name,mobile,transfer_time,transfer_status,pay_time,pay_status,order_status,pos_code,addtime,cashier_id,total,\n" +
                "discount_price,order_code,stores_id,spare_param1,spare_param2,remark FROM retail_order where order_status = 2 and pay_status = 2 and upload_status = 1 limit 50",
                sql_goods_detail = "select conversion,zk_cashier_id,gp_id,tc_rate,tc_mode,tax_rate,ps_price,cost_price,trade_price,retail_price,buying_price,price,xnum,barcode_id from retail_order_goods where order_code = '%1'",
                sql_pays_detail = "select print_info,return_code,card_no,xnote,discount_money,give_change_money,pre_sale_money,zk_money,is_check,remark,pay_code,pay_serial_no,pay_status,pay_time,pay_money,pay_method,order_code from retail_order_pays where order_code = '%1'",
                sql_combination_goods = "SELECT b . retail_price, a . xnum , c.gp_price,c.gp_id,d.zk_cashier_id,d.order_code FROM  goods_group_info a LEFT JOIN  barcode_info b on a . barcode_id = b . barcode_id\n" +
                        " LEFT JOIN goods_group c on c . gp_id = a . gp_id  AND c . status = 1 left join retail_order_goods d on c.gp_id = d.gp_id and d.barcode_id = b.barcode_id " +
                        "WHERE d.order_code = '%2' and d.gp_id in (%1)";
        int gp_id;
        String order_code;

        JSONArray orders,sales ,pays ,combinations;
        JSONObject data = new JSONObject(),send_data = new JSONObject(),retJson,tmp_jsonObject;
        HttpRequest httpRequest = new HttpRequest();

        if (null != (orders = SQLiteHelper.getListToJson(sql_orders,err))){
            try {
                for (int i = 0,size = orders.length();i < size;i++){
                    order_gp_ids.delete(0,order_gp_ids.length());

                    JSONArray order_arr = new JSONArray();
                    tmp_jsonObject = orders.getJSONObject(i);
                    order_arr.put(tmp_jsonObject);

                    order_code = tmp_jsonObject.getString("order_code");

                    sales = SQLiteHelper.getListToJson(sql_goods_detail.replace("%1",order_code),err);
                    pays = SQLiteHelper.getListToJson(sql_pays_detail.replace("%1",order_code),err);
                    if (null != sales && null != pays){

                        for (int j = 0,j_size = sales.length();j < j_size;j++){
                            tmp_jsonObject = sales.getJSONObject(j);
                            gp_id = tmp_jsonObject.getInt("gp_id");
                            if (-1 != gp_id){
                                if (j > 0){
                                    if (gp_id == sales.getJSONObject(j -1).getInt("gp_id"))continue;
                                }
                                if (order_gp_ids.length() == 0){
                                    order_gp_ids.append("'").append(gp_id).append("'");
                                }else{
                                    order_gp_ids.append(",").append("'").append(gp_id).append("'");
                                }
                            }
                        }

                        Logger.d(sql_combination_goods.replace("%1",order_gp_ids).replace("%2",order_code));

                        if ((combinations = SQLiteHelper.getListToJson(sql_combination_goods.replace("%1",order_gp_ids).replace("%2",order_code),err)) != null){
                            data.put("order_arr",order_arr);
                            data.put("order_goods",sales);
                            data.put("order_pay",pays);
                            data.put("order_group",combinations);

                            Logger.d_json(data.toString());

                            send_data.put("appid",mAppId);
                            send_data.put("data",data);

                            retJson = httpRequest.sendPost(mUrl + "/api/retail/order_upload",HttpRequest.generate_request_parm(send_data,mAppScret),true);
                            switch (retJson.getInt("flag")){
                                case 0:
                                    code = false;
                                    err.append(retJson.getString("info"));
                                    break;
                                case 1:
                                    retJson = new JSONObject(retJson.getString("info"));
                                    switch (retJson.getString("status")){
                                        case "n":
                                            code = false;
                                            err.append(retJson.getString("info"));
                                            break;
                                        case "y":
                                            Logger.d("old_order_code:%s,order_code:%s",order_code,retJson.getString("order_code"));
                                            ContentValues values = new ContentValues();
                                            values.put("upload_status",2);
                                            values.put("upload_time",System.currentTimeMillis() / 1000);
                                            if (!SQLiteHelper.execUpdateSql("retail_order",values,"order_code = ?",new String[]{order_code},err)){
                                                code = false;
                                            }
                                            break;
                                    }
                                    break;
                            }
                        }else{
                            code = false;
                        }
                    }else{
                        code = false;
                    }
                }
            }catch (JSONException e){
                code = false;
                err.append(e.getMessage());
                e.printStackTrace();
            }
        }else{
            code = false;
        }

        if (!code){
            Logger.e("上传单据错误：%s",err);
            syncActivityHandler.obtainMessage(MessageID.TRANSFERSTATUS_ID,false).sendToTarget();
        }
    }

    void stop(){
        this.removeCallbacksAndMessages(null);
        if (mHttp != null)mHttp.clearConnection(HttpRequest.CLOSEMODE.BOTH);
    }
    void setReportProgress(boolean b){
        mReportProgress = b;
    }
    void sync(){
        this.obtainMessage(MessageID.SYNC_CASHIER_ID).sendToTarget();//收银员
        this.obtainMessage(MessageID.SYNC_GOODS_CATEGORY_ID).sendToTarget();//商品类别
        this.obtainMessage(MessageID.SYNC_GOODS_ID).sendToTarget();//商品信息
        this.obtainMessage(MessageID.SYNC_PAY_METHOD_ID).sendToTarget();//支付方式
        this.obtainMessage(MessageID.SYNC_STORES_ID).sendToTarget();//仓库信息
        this.obtainMessage(MessageID.SYNC_GP_INFO_ID).sendToTarget();//商品组合ID
    }
    void syncFinish(){
        obtainMessage(MessageID.SYNC_FINISH_ID).sendToTarget();//最后发送同步完成消息
    }
    void startNetworkTest(){
        if (!hasMessages(MessageID.NETWORKSTATUS_ID)){
            obtainMessage(MessageID.NETWORKSTATUS_ID).sendToTarget();
            postDelayed(this::startNetworkTest,1000);
        }
    }
    void startUploadOrder(){
        obtainMessage(MessageID.UPLOAD_ORDER_ID).sendToTarget();
    }
}
