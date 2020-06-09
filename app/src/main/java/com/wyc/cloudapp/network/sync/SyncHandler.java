package com.wyc.cloudapp.network.sync;

import android.content.ContentValues;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.LoginActivity;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;

import static com.wyc.cloudapp.utils.MessageID.SYNC_DIS_INFO_ID;

public final class SyncHandler extends Handler {
    private HttpRequest mHttp;
    private Handler syncActivityHandler;
    private volatile boolean mReportProgress = true;
    private volatile int mCurrentNeworkStatusCode = HttpURLConnection.HTTP_OK;
    private long mLoseTime = 0;//mSyncInterval 同步时间间隔，默认3秒
    private SyncManagement mSync;
    SyncHandler(final Handler handler,final SyncManagement syncManagement){
        syncActivityHandler = handler;
        mSync = syncManagement;

        mHttp = new HttpRequest();
        mHttp.setConnTimeOut(3000);
    }
    @Override
    public void handleMessage(Message msg){
        final SyncManagement sync = mSync;

        final String base_url =  sync.getUrl(),app_id = sync.getAppId(),appSecret = sync.getAppSecret(),pos_num = sync.getPosNum(),oper_id = sync.getOperId(),stores_id = sync.getStoresId();
        JSONObject object = new JSONObject(),info_json,retJson;
        String table_name = "",sys_name = "",url = "",sz_param = "";
        String[] table_cls = null;
        boolean code = true;
        try{
            switch (msg.what) {
                case MessageID.SYNC_GOODS_BASE_ID:

                    break;
                case MessageID.SYNC_PAUSE_ID:
                    synchronized (this){
                        try {
                            wait(15000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    return;
                case MessageID.SYNC_GOODS_CATEGORY_ID:
                    table_name = "shop_category";
                    sys_name = "正在同步商品类别";
                    table_cls = new String[]{"category_id","name","parent_id","depth","path","status","sort"};
                    url = base_url + "/api/scale/get_category_info";
                    object.put("pos_num",pos_num);
                    break;
                case MessageID.SYNC_STORES_ID://仓库信息
                    table_name = "shop_stores";
                    table_cls = new String[]{"stores_id","stores_name","manager","telphone","region","status","nature"};
                    sys_name = "正在同步仓库";
                    url = base_url + "/api/scale/get_stores";
                    object.put("pt_user_id",oper_id);
                    break;
                case MessageID.SYNC_GOODS_ID://商品信息
                    table_name = "barcode_info";
                    sys_name = "正在同步商品";
                    table_cls = new String[]{"goods_id","barcode_id","barcode","goods_title","only_coding","retail_price","buying_price","trade_price","cost_price","ps_price",
                            "unit_id","unit_name","specifi","category_name","metering_id","shelf_life","goods_status","brand","origin","type","goods_tare","barcode_status","category_id",
                            "tax_rate","tc_mode","tc_rate","yh_mode","yh_price","mnemonic_code","image","attr_id","attr_name","attr_code","conversion","update_price","stock_unit_id","stock_unit_name","img_url"};
                    url = base_url + "/api/goods/get_goods_all";
                    object.put("pos_num",pos_num);
                    object.put("page",msg.obj);
                    object.put("limit",100);
                    break;
                case MessageID.SYNC_PAY_METHOD_ID://支付方式
                    table_name = "pay_method";
                    table_cls = new String[]{"pay_method_id","name","status","remark","is_check","shortcut_key","sort","xtype","pay_img","master_img",
                            "is_show_client","is_cardno","is_scan","wr_btn_img","unified_pay_order","unified_pay_query","rule","is_open","is_enable","support"};
                    sys_name = "正在同步支付方式";
                    url = base_url + "/api/cashier/get_pm_info";
                    object.put("stores_id",stores_id);
                    object.put("pos_num",pos_num);
                    break;
                case MessageID.SYNC_CASHIER_ID://收银员
                    table_name = "cashier_info";
                    table_cls = new String[]{"cas_id","stores_id","stores_name","cas_name","cas_account","cas_pwd","cas_addtime","cas_code","cas_phone","cas_status",
                            "min_discount","is_refund","is_give","is_put","pt_user_id","pt_user_cname","remark","authority"};

                    sys_name = "正在同步门店收银员";
                    url = base_url + "/api_v2/scale/get_cashier_info";

                    object.put("cas_id",oper_id);
                    object.put("pos_num",pos_num);
                    object.put("stores_id",stores_id);
                    break;
                case MessageID.SYNC_GP_INFO_ID://商品组合信息
                    sys_name = "正在同步组合商品";
                    url = base_url + "/api/promotion/get_gp_info";

                    object.put("pos_num",pos_num);
                    object.put("stores_id",stores_id);
                    break;
                case MessageID.SYNC_FINISH_ID:
                    syncActivityHandler.obtainMessage(MessageID.SYNC_FINISH_ID).sendToTarget();//同步完成
                    return;
                case MessageID.NETWORKSTATUS_ID:
                    testNetworkStatus();
                    return;
                case MessageID.UPLOAD_ORDER_ID:
                    uploadRetailOrderInfo(app_id,base_url,appSecret);
                    return;
                case MessageID.UPLOAD_TRANS_ORDER_ID:
                    uploadTransferOrderInfo(app_id,base_url,appSecret);
                    return;
                case MessageID.UPLOAD_REFUND_ORDER_ID:
                    uploadRefundOrderInfo(app_id,base_url,appSecret);
                    return;
                case MessageID.MODFIY_REPORT_PROGRESS_ID:
                    if (msg.obj instanceof  Boolean)
                        mReportProgress = (boolean)msg.obj;
                    return;
                case MessageID.MARK_GOODS_STATUS_ID:
                    if (mReportProgress)
                        syncActivityHandler.obtainMessage(SYNC_DIS_INFO_ID,"准备同步...").sendToTarget();
                    upload_barcode_id(null);
                    return;
                case MessageID.SYNC_THREAD_QUIT_ID://由于处理程序内部会发送消息，消息队列退出需在处理程序内部处理
                    if (mHttp != null)mHttp.clearConnection(HttpRequest.CLOSEMODE.BOTH);
                    this.removeCallbacksAndMessages(null);
                    getLooper().quit();
                    return;

            }

            if (mReportProgress)
                syncActivityHandler.obtainMessage(SYNC_DIS_INFO_ID,sys_name + "信息....").sendToTarget();

            object.put("appid",app_id);
            sz_param = HttpRequest.generate_request_parm(object,appSecret);

            retJson = mHttp.sendPost(url,sz_param,true);
            switch (retJson.getIntValue("flag")) {
                case 0:
                    code = false;
                    sys_name = sys_name.concat("错误:").concat(retJson.getString("info"));
                    break;
                case 1:
                    info_json = JSON.parseObject(retJson.getString("info"));
                    switch (info_json.getString("status")){
                        case "n":
                            code = false;
                            sys_name = sys_name.concat("错误:").concat(info_json.getString("info"));
                            break;
                        case "y":
                            final JSONArray data = info_json.getJSONArray("data");
                            if(data.size() != 0){
                                StringBuilder err = new StringBuilder();
                                switch (msg.what){
                                    case MessageID.SYNC_GP_INFO_ID:
                                        code = deal_good_group(data,err);
                                        break;
                                    case MessageID.SYNC_PAY_METHOD_ID:
                                        if((code = SQLiteHelper.execSQLByBatchFromJson(data,table_name ,table_cls,err,1))){
                                            down_load_pay_method_img(data,sys_name);
                                        }
                                        break;
                                    case MessageID.SYNC_GOODS_ID: {
                                        int max_page = info_json.getIntValue("max_page"),current_page = (int)msg.obj;
                                        if((code = SQLiteHelper.execSQLByBatchFromJson(data,table_name ,table_cls,err,1))){
                                            down_laod_goods_img_and_upload_barcode_id(data,sys_name);//保存成功才能标记已获取
                                        }
                                        if ((current_page++ <= max_page) && code){
                                            Logger.d("current_page:%d,max_page:%d",current_page,max_page);
                                            sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_GOODS_ID,current_page));
                                        }
                                    }
                                        break;
                                        default:
                                            code = SQLiteHelper.execSQLByBatchFromJson(data,table_name ,table_cls,err,1);
                                            break;
                                }
                                if (!code)
                                    sys_name = sys_name.concat("错误:").concat(err.toString());
                            }
                            break;
                    }
                    break;
            }
            if (!code) {
                stopSync();
                if (mReportProgress) {
                    syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, sys_name).sendToTarget();
                }else{
                    syncActivityHandler.obtainMessage(MessageID.TRANSFERSTATUS_ID,false).sendToTarget();
                    Logger.e("%s", sys_name);
                }
            }else{
                if (!mReportProgress)
                    syncActivityHandler.obtainMessage(MessageID.TRANSFERSTATUS_ID,true).sendToTarget();
            }
        }catch (JSONException e){
            e.printStackTrace();
            sys_name = "同步" + table_name + "错误:" +  e.getMessage();
            if (mReportProgress) {
                removeCallbacksAndMessages(null);
                syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, sys_name).sendToTarget();
            }else {
                syncActivityHandler.obtainMessage(MessageID.TRANSFERSTATUS_ID,false).sendToTarget();
                Logger.e("%s", sys_name);
            }
        }
    }

    private void testNetworkStatus() throws JSONException {
        int syncInterval = 5000;
        final JSONObject data = new JSONObject();
        final SyncManagement sync = mSync;
        final String test_url = sync.getUrl() + "/api/heartbeat/index";
        int err_code;
        data.put("appid",sync.getAppId());
        data.put("pos_num",sync.getPosNum());
        data.put("randstr", Utils.getNonce_str(8));
        data.put("cas_id",sync.getOperId());

        final JSONObject retJson = mHttp.sendPost(test_url,HttpRequest.generate_request_parm(data,sync.getAppSecret()),true);
        err_code = retJson.getIntValue("rsCode");
        switch (retJson.getIntValue("flag")) {
            case 0:
                if (mCurrentNeworkStatusCode != err_code){
                    Logger.e("连接服务器错误：" + retJson.getString("info"));
                }
                syncActivityHandler.obtainMessage(MessageID.NETWORKSTATUS_ID,false).sendToTarget();
                break;
            case 1:
                syncActivityHandler.obtainMessage(MessageID.NETWORKSTATUS_ID,true).sendToTarget();
                if (mCurrentNeworkStatusCode != HttpURLConnection.HTTP_OK){//如果之前网络响应状态不为OK,则重连成功
                    mCurrentNeworkStatusCode = HttpURLConnection.HTTP_OK;
                    Logger.i("重新连接服务器成功！");
                    startUploadRetailOrder();
                    startUploadRefundOrder();
                    startUploadTransferOrder();
                }
                final JSONObject  info_json = JSON.parseObject(retJson.getString("info"));
                switch (info_json.getString("status")){
                    case "n":
                        syncActivityHandler.obtainMessage(MessageID.NETWORKSTATUS_ID,false).sendToTarget();
                        Logger.e("网络检测错误：" + info_json.getString("info"));
                        break;
                    case "y":
                        if (System.currentTimeMillis() - mLoseTime >= syncInterval && mCurrentNeworkStatusCode == HttpURLConnection.HTTP_OK) {
                            mLoseTime = System.currentTimeMillis();
                            modifyReportProgressStatus(false);
                            sync();
                        }
                        //Logger.d_json(info_json.getString("data"));
                        break;
                }
                break;
        }
        mCurrentNeworkStatusCode = err_code;
    }
    private void uploadRetailOrderInfo(final String appid,final String url,final String appSecret) {
        final StringBuilder err = new StringBuilder(),order_gp_ids = new StringBuilder();
        final String sql_orders = "SELECT discount_money,card_code,member_id,name,mobile,transfer_time,transfer_status,pay_time,pay_status,order_status,pos_code,addtime,cashier_id,total,\n" +
                "discount_price,order_code,stores_id,spare_param1,spare_param2,replace(remark,'&','') remark FROM retail_order where order_status = 2 and pay_status = 2 and upload_status = 1 limit 100",
                sql_goods_detail = "select conversion,zk_cashier_id,gp_id,tc_rate,tc_mode,tax_rate,ps_price,cost_price,trade_price,retail_price,buying_price,price,xnum,barcode_id from retail_order_goods where order_code = '%1'",
                sql_pays_detail = "select print_info,return_code,card_no,xnote,discount_money,give_change_money,pre_sale_money,zk_money,is_check,remark,pay_code,pay_serial_no,pay_status,pay_time,pay_money,pay_method,order_code from retail_order_pays where order_code = '%1'",
                sql_combination_goods = "SELECT b.retail_price,a.xnum,c.gp_price,c.gp_id,d.zk_cashier_id,d.order_code FROM  goods_group_info a LEFT JOIN  barcode_info b on a.barcode_id = b.barcode_id\n" +
                        " LEFT JOIN goods_group c on c.gp_id = a.gp_id AND c.status = 1 left join retail_order_goods d on c.gp_id = d.gp_id and d.barcode_id = b.barcode_id " +
                        "WHERE d.order_code = '%2' and d.gp_id in (%1)",
                sql_discount_record = "SELECT order_code,discount_type,type,stores_id,relevant_id,discount_money,details FROM discount_record where order_code = '%1'";


        JSONArray orders,sales ,pays ,combinations,discount_records;
        JSONObject data = new JSONObject(),send_data = new JSONObject(),retJson,tmp_jsonObject,order_info;

        boolean code;
        int gp_id;
        String order_code;

        if (code = null != (orders = SQLiteHelper.getListToJson(sql_orders,err))){
            try {
                for (int i = 0,size = orders.size();i < size;i++){
                    order_gp_ids.delete(0,order_gp_ids.length());

                    order_info = orders.getJSONObject(i);

                    order_code = order_info.getString("order_code");

                    sales = SQLiteHelper.getListToJson(sql_goods_detail.replace("%1",order_code),err);
                    pays = SQLiteHelper.getListToJson(sql_pays_detail.replace("%1",order_code),err);
                    if (code = (null != sales && null != pays)){

                        if (code = (!sales.isEmpty() && !pays.isEmpty())){
                            for (int j = 0,j_size = sales.size();j < j_size;j++){//销售明细
                                tmp_jsonObject = sales.getJSONObject(j);
                                gp_id = tmp_jsonObject.getIntValue("gp_id");
                                if (-1 != gp_id){
                                    if (j > 0){
                                        if (gp_id == sales.getJSONObject(j -1).getIntValue("gp_id"))continue;
                                    }
                                    if (order_gp_ids.length() == 0){
                                        order_gp_ids.append("'").append(gp_id).append("'");
                                    }else{
                                        order_gp_ids.append(",").append("'").append(gp_id).append("'");
                                    }
                                }
                            }

                            //组合商品
                            if (code = ((combinations = SQLiteHelper.getListToJson(sql_combination_goods.replace("%1",order_gp_ids).replace("%2",order_code),err)) != null)){

                                if ((discount_records = SQLiteHelper.getListToJson(sql_discount_record.replace("%1",order_code),err)) != null){
                                    data.put("order_info",Utils.JsondeepCopy(order_info));
                                    data.put("goods_list",sales);
                                    data.put("pay_list",pays);
                                    data.put("group_list",combinations);
                                    data.put("discount_record",discount_records);

                                    send_data.put("appid",appid);
                                    send_data.put("data",data);

                                    retJson = mHttp.sendPost(url + "/api_v2/retail_upload/order_upload",HttpRequest.generate_request_parm(send_data,appSecret),true);
                                    switch (retJson.getIntValue("flag")){
                                        case 0:
                                            code = false;
                                            err.append(retJson.getString("info"));
                                            break;
                                        case 1:
                                            retJson = JSON.parseObject(retJson.getString("info"));
                                            switch (retJson.getString("status")){
                                                case "n":
                                                    code = false;
                                                    err.append(retJson.getString("info"));
                                                    break;
                                                case "y":
                                                    Logger.d("old_order_code:%s,order_code:%s",order_code,retJson.getString("order_code"));
                                                    final ContentValues values = new ContentValues();
                                                    values.put("upload_status",2);
                                                    values.put("upload_time",System.currentTimeMillis() / 1000);
                                                    int rows = SQLiteHelper.execUpdateSql("retail_order",values,"order_code = ?",new String[]{order_code},err);
                                                    code = rows > 0;
                                                    if (rows == 0){
                                                        err.append("未更新任何数据！");
                                                        Logger.e("销售单:%s,order_code:%s",err,order_code);
                                                    }
                                                    break;
                                            }
                                            break;
                                    }
                                }
                            }
                        }else {
                            err.append("上传明细为空！");
                            Logger.e("销售单:%s,order_code:%s",err,order_code);
                        }
                    }
                }
            }catch (JSONException e){
                code = false;
                err.append(e.getMessage());
                e.printStackTrace();
            }
        }
        if (!code){
            Logger.e("上传单据错误：%s",err);
            syncActivityHandler.obtainMessage(MessageID.TRANSFERSTATUS_ID,false).sendToTarget();
        }
    }

    private void uploadTransferOrderInfo(final String appid,final String url,final String appSecret){
        //上传交班单据
        String transfer_sum_sql = "SELECT sj_money,cards_num oncecard_num,cards_money oncecard_money,\n" +
                "       order_money,order_e_date,order_b_date,recharge_num,recharge_money,refund_num,\n" +
                "       refund_money,cashbox_money unpaid_money,sum_money,ti_code,transfer_time,order_num,cas_id,stores_id FROM transfer_info where upload_status = 1 limit 50",details_where_sql = "where ti_code  = ",
                transfer_orders_sql = "SELECT ifnull(order_code,'') order_code FROM transfer_order ",
                transfer_retails_sql = "SELECT order_num,pay_method,pay_money FROM transfer_money_info ",transfer_cards_sql = "SELECT order_num,pay_method,pay_money FROM transfer_once_cardsc ",
                transfer_recharge_sql = "SELECT order_num,pay_method,pay_money FROM transfer_recharge_money ",transfer_refund_sql = "SELECT order_num,pay_method,pay_money FROM transfer_refund_money ";
        final StringBuilder err = new StringBuilder();
        final JSONArray transfer_sum_arr = SQLiteHelper.getListToJson(transfer_sum_sql,err);
        if (null != transfer_sum_arr){
            final StringBuilder sz_ti_code = new StringBuilder(),sql_sb = new StringBuilder();

            JSONObject transfer_sum_obj;
            for (int i = 0,size = transfer_sum_arr.size();i < size;i++){
                transfer_sum_obj = transfer_sum_arr.getJSONObject(i);

                sz_ti_code.delete(0,sz_ti_code.length()).append(details_where_sql).append("'").append(transfer_sum_obj.getString("ti_code")).append("'");
                Logger.d("sz_ti_code:%s",sz_ti_code);

                if (sz_ti_code.length() != 0){

                    sql_sb.delete(0,sql_sb.length()).append(transfer_orders_sql).append(sz_ti_code);
                    final JSONArray transfer_orders_arr = SQLiteHelper.getListToValue(sql_sb.toString(),err);

                    sql_sb.delete(0,sql_sb.length()).append(transfer_retails_sql).append(sz_ti_code);
                    final JSONArray transfer_retails_arr = SQLiteHelper.getListToJson(sql_sb.toString(),err);

                    sql_sb.delete(0,sql_sb.length()).append(transfer_cards_sql).append(sz_ti_code);
                    final JSONArray transfer_cards_arr = SQLiteHelper.getListToJson(sql_sb.toString(),err);

                    sql_sb.delete(0,sql_sb.length()).append(transfer_recharge_sql).append(sz_ti_code);
                    final JSONArray transfer_recharge_arr = SQLiteHelper.getListToJson(sql_sb.toString(),err);

                    sql_sb.delete(0,sql_sb.length()).append(transfer_refund_sql).append(sz_ti_code);
                    final JSONArray transfer_refund_arr = SQLiteHelper.getListToJson(sql_sb.toString(),err);

                    if (null != transfer_orders_arr && null != transfer_retails_arr && transfer_cards_arr != null && transfer_recharge_arr != null && transfer_refund_arr != null){
                        final JSONObject data = new JSONObject(),send_data = new JSONObject();

                        data.put("order_arr",transfer_sum_obj);
                        data.put("order_list",transfer_orders_arr);
                        data.put("retail_money",transfer_retails_arr);
                        data.put("refund_money",transfer_refund_arr);
                        data.put("recharge_money",transfer_recharge_arr);
                        data.put("oncecard_money",transfer_cards_arr);
                        data.put("giftcard_money",new JSONArray());

                        Logger.d_json(data.toJSONString());

                        send_data.put("appid",appid);
                        send_data.put("data",data);

                        JSONObject retJson = mHttp.sendPost(url + "/api/transfer/transfer_upload",HttpRequest.generate_request_parm(send_data,appSecret),true);
                        switch (retJson.getIntValue("flag")){
                            case 0:
                                err.append(retJson.getString("info")).append(" sz_ti_code:").append(sz_ti_code);
                                break;
                            case 1:
                                retJson = JSON.parseObject(retJson.getString("info"));
                                switch (retJson.getString("status")){
                                    case "n":
                                        err.append(retJson.getString("info")).append(" sz_ti_code:").append(sz_ti_code);
                                        break;
                                    case "y":
                                        final String ti_code = retJson.getString("ti_code");
                                        final ContentValues values = new ContentValues();
                                        values.put("upload_time",System.currentTimeMillis() / 1000);
                                        values.put("upload_status",2);
                                        SQLiteHelper.execUpdateSql("transfer_info",values,"ti_code = ?",new String[]{ti_code},err);
                                        break;
                                }
                                break;
                        }
                    }
                }

            }
        }
        if (err.length() != 0){
            Logger.e("上传交班单据错误%s,ti_code:%s",err);
            syncActivityHandler.obtainMessage(MessageID.TRANSFERSTATUS_ID,false).sendToTarget();
        }
    }

    private void uploadRefundOrderInfo(final String appid,final String url,final String appSecret){
        final StringBuilder err = new StringBuilder();
        final JSONArray refund_orders = SQLiteHelper.getListToJson("SELECT ifnull(order_code,'') order_code,ro_code FROM refund_order where upload_status = 1 limit 500",err);
        if (refund_orders != null){
            JSONObject obj;
            for (int i = 0,size = refund_orders.size();i < size;i++){
                obj = refund_orders.getJSONObject(i);
                RefundDialog.uploadRefundOrder(appid,url,appSecret,Utils.getNullStringAsEmpty(obj,"order_code"),Utils.getNullStringAsEmpty(obj,"ro_code"),err);
            }
        }
        if (err.length() != 0){
            syncActivityHandler.obtainMessage(MessageID.TRANSFERSTATUS_ID,false).sendToTarget();
        }
    }

    private void down_load_pay_method_img(@NonNull final JSONArray datas,final String sys_name) throws JSONException {
        String img_url_info,img_file_name;
        JSONObject object;
        for (int k = 0,length = datas.size();k < length;k++){
            object = datas.getJSONObject(k);
            img_url_info = object.getString("pay_img");
            if (!img_url_info.equals("")){
                img_file_name = img_url_info.substring(img_url_info.lastIndexOf("/") + 1);
                File file = new File(LoginActivity.IMG_PATH + img_file_name);
                if (!file.exists()){
                    JSONObject load_img = mHttp.getFile(file,img_url_info);
                    if (load_img.getIntValue("flag") == 0){
                        Logger.e(sys_name + "图片错误：" + load_img.getString("info"));
                    }
                }
            }
        }
    }
    private void down_laod_goods_img_and_upload_barcode_id(@NonNull final JSONArray datas,final String sys_name) throws JSONException {
        String img_url_info,img_file_name;
        final JSONArray goods_ids = new JSONArray();
        JSONObject object;
        for (int k = 0,length = datas.size();k < length;k++){
            object = datas.getJSONObject(k);
            goods_ids.add(object.getIntValue("barcode_id"));
            img_url_info = object.getString("img_url");
            if (!img_url_info.equals("")){
                img_file_name = img_url_info.substring(img_url_info.lastIndexOf("/") + 1);
                File file = new File(LoginActivity.IMG_PATH + img_file_name);
                if (!file.exists()){
                    JSONObject load_img = mHttp.getFile(file,img_url_info);
                    if (load_img.getIntValue("flag") == 0){
                        Logger.e(sys_name + "图片错误：" + load_img.getString("info"));
                    }
                }
            }
        }
        if (goods_ids.size() != 0){
            upload_barcode_id(goods_ids);
        }
    }
    private boolean deal_good_group(@NonNull JSONArray data,StringBuilder err) throws JSONException {
        final JSONArray goods_list = new JSONArray();
        final JSONObject tmp_goods = new JSONObject();
        for (int k = 0, size = data.size(); k < size; k++) {
            final JSONArray tmp = (JSONArray) data.getJSONObject(k).remove("goods_list");
            if (tmp != null)
                for (int j = 0, length = tmp.size(); j < length; j++) {
                    goods_list.add(tmp.get(j));
                }
        }
        tmp_goods.put("goods_group", data);
        tmp_goods.put("goods_group_info", goods_list);
        List<String> goods_group_cols = Arrays.asList("mnemonic_code", "gp_id", "gp_code", "gp_title", "gp_price", "status", "addtime", "unit_name", "stores_id", "img_url"),
                goods_group_info_cols = Arrays.asList("xnum", "barcode_id", "gp_id", "_id");

        return SQLiteHelper.execSQLByBatchFromJson(tmp_goods, Arrays.asList("goods_group", "goods_group_info"), Arrays.asList(goods_group_cols, goods_group_info_cols), err, 1);
    }
    private void upload_barcode_id(JSONArray datas) throws JSONException {
        JSONObject object;
        final StringBuilder err = new StringBuilder();
        String url;
        final SyncManagement sync = mSync;
        if(datas == null){
            datas = SQLiteHelper.getListToValue("select barcode_id from barcode_info",err);
            if (null == datas){
                Logger.e("标记已获取的商品错误：" + err);
                return;
            }
        }
        object = new JSONObject();
        if (datas.size() == 0){
            url = sync.getUrl() + "/api/cashier/clear_download_record";
            object.put("appid",sync.getAppId());
            object.put("pos_num",sync.getPosNum());
            object.put("stores_id",sync.getStoresId());
            err.append("清除已标记商品错误:");
        }else{
            err.append("标记已获取商品错误:");
            url = sync.getUrl() + "/api/goods/up_goods";
            object.put("appid",sync.getAppId());
            object.put("goods_ids",datas);
            object.put("pos_num",sync.getPosNum());
        }

        object = mHttp.sendPost(url,HttpRequest.generate_request_parm(object,sync.getAppSecret()),true);
        if (object.getIntValue("flag") == 1){
            object = JSON.parseObject(object.getString("info"));
            if ("n".equals(object.getString("status"))){
                Logger.e(err + object.getString("info"));
            }
        }else{
            Logger.e(err + object.getString("info"));
        }
    }

    void stop(){
        sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_THREAD_QUIT_ID));
    }
    void sync(){
        if (mCurrentNeworkStatusCode == HttpURLConnection.HTTP_OK){
            if (!hasMessages(MessageID.SYNC_CASHIER_ID))obtainMessage(MessageID.SYNC_CASHIER_ID).sendToTarget();//收银员
            if (!hasMessages(MessageID.SYNC_GOODS_CATEGORY_ID))obtainMessage(MessageID.SYNC_GOODS_CATEGORY_ID).sendToTarget();//商品类别
            if (!hasMessages(MessageID.SYNC_PAY_METHOD_ID))obtainMessage(MessageID.SYNC_PAY_METHOD_ID).sendToTarget();//支付方式
            if (!hasMessages(MessageID.SYNC_STORES_ID))obtainMessage(MessageID.SYNC_STORES_ID).sendToTarget();//仓库信息
            if (!hasMessages(MessageID.SYNC_GP_INFO_ID))obtainMessage(MessageID.SYNC_GP_INFO_ID).sendToTarget();//商品组合ID
            if (!hasMessages(MessageID.SYNC_GOODS_ID))obtainMessage(MessageID.SYNC_GOODS_ID,0).sendToTarget();//商品信息obj代表当前下载页数
        }
    }
    void stopSync(){
        removeMessages(MessageID.MARK_GOODS_STATUS_ID);
        removeMessages(MessageID.SYNC_CASHIER_ID);
        removeMessages(MessageID.SYNC_GOODS_CATEGORY_ID);
        removeMessages(MessageID.SYNC_STORES_ID);
        removeMessages(MessageID.SYNC_GP_INFO_ID);
        removeMessages(MessageID.SYNC_GOODS_ID);
        removeMessages(MessageID.SYNC_FINISH_ID);
    }
    void modifyReportProgressStatus(boolean b){
        obtainMessage(MessageID.MODFIY_REPORT_PROGRESS_ID,b).sendToTarget();//通过消息保证串行修改
    }
    void startNetworkTest(){
        if (!hasMessages(MessageID.NETWORKSTATUS_ID)){
            obtainMessage(MessageID.NETWORKSTATUS_ID).sendToTarget();
            postDelayed(this::startNetworkTest,1000);
        }
    }
    void startUploadRetailOrder(){
        if (mCurrentNeworkStatusCode == HttpURLConnection.HTTP_OK){
            sendMessageAtFrontOfQueue(obtainMessage(MessageID.UPLOAD_ORDER_ID));
        }
    }
    void startUploadTransferOrder(){
        if (mCurrentNeworkStatusCode == HttpURLConnection.HTTP_OK){
            sendMessageAtFrontOfQueue(obtainMessage(MessageID.UPLOAD_TRANS_ORDER_ID));
        }
    }
    void startUploadRefundOrder(){
        if (mCurrentNeworkStatusCode == HttpURLConnection.HTTP_OK){
            sendMessageAtFrontOfQueue(obtainMessage(MessageID.UPLOAD_REFUND_ORDER_ID));
        }
    }

    void pause(){
        sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_PAUSE_ID));
    }
    void _continue(){
        synchronized (this){
            notify();
        }
    }
    void sign_downloaded(){//标记已下载
        if (mCurrentNeworkStatusCode == HttpURLConnection.HTTP_OK) {
            if (!hasMessages(MessageID.MARK_GOODS_STATUS_ID))
                obtainMessage(MessageID.MARK_GOODS_STATUS_ID).sendToTarget();
        }
    }
}
