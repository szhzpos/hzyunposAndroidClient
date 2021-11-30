package com.wyc.cloudapp.application;

import android.content.ContentValues;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.application.syncinstance.AbstractSyncBase;
import com.wyc.cloudapp.constants.MessageID;
import com.wyc.cloudapp.constants.RetailOrderStatus;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;

final class SyncHandler1 extends Handler {
    private volatile boolean isPause = false;
    private volatile int mCurrentNetworkStatusCode = HttpURLConnection.HTTP_OK;
    private JSONObject mHeartbeat;

    SyncHandler1(Looper looper){
        super(looper);
    }

    private static final List<Integer> PracticeModeMsgFilter = Collections.singletonList(MessageID.SYNC_THREAD_QUIT_ID);

    @Override
    public void handleMessage(Message msg){
        /*
         * 练习模式下不处理业务同步事件
         * */
        if (CustomApplication.isPracticeMode() && !PracticeModeMsgFilter.contains(msg.what)){
            return;
        }
        /*
         *
         * */
        switch (msg.what) {
            case MessageID.SYNC_BASICS_ID:
                AbstractSyncBase.syncAllBasics();
                break;
            case MessageID.SYNC_PAUSE_ID:
                synchronized (this){
                    try {
                        isPause = true;
                        wait(15000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    isPause = false;
                }
                return;
            case MessageID.UPLOAD_ORDER_ID:
                if (msg.obj instanceof Boolean){
                    uploadRetailOrderInfo((Boolean) msg.obj);
                }else
                    uploadRetailOrderInfo(false);
                return;
            case MessageID.UPLOAD_TRANS_ORDER_ID:
                uploadTransferOrderInfo();
                return;
            case MessageID.UPLOAD_REFUND_ORDER_ID:
                uploadRefundOrderInfo();
                return;
            case MessageID.NETWORKSTATUS_ID:
                testNetworkStatus();
                return;
            case MessageID.MARK_DOWNLOAD_RECORD_ID:
                CustomApplication.showMsg("正在更新信息....");
                clear_download_record();
                return;
            case MessageID.SYNC_THREAD_QUIT_ID://由于处理程序内部会发送消息，消息队列退出需在处理程序内部处理
                this.removeCallbacksAndMessages(null);
                getLooper().quitSafely();
        }
    }

    private void testNetworkStatus() throws JSONException {
        final String test_url = CustomApplication.self().getUrl() + "/api/heartbeat/index";
        int err_code;
        if (mHeartbeat == null){
            final JSONObject data = new JSONObject();
            data.put("appid",CustomApplication.self().getAppId());
            data.put("pos_num",CustomApplication.self().getPosNum());
            data.put("cas_id",CustomApplication.self().getCashierId());
            mHeartbeat = data;
        }
        mHeartbeat.put("randstr", Utils.getNonce_str(8));
        final JSONObject retJson = HttpUtils.sendPost(test_url,HttpRequest.generate_request_parma(mHeartbeat,CustomApplication.self().getAppSecret()),true);
        err_code = retJson.getIntValue("rsCode");
        switch (retJson.getIntValue("flag")) {
            case 0:
                if (mCurrentNetworkStatusCode != err_code){
                    CustomApplication.updateOfflineTime(System.currentTimeMillis());
                    Logger.e("连接服务器错误：" + retJson.getString("info"));
                }
                CustomApplication.sendMessage(MessageID.NETWORKSTATUS_ID,false);
                break;
            case 1:
                CustomApplication.updateOfflineTime(-1);
                CustomApplication.sendMessage(MessageID.NETWORKSTATUS_ID,true);
                if (mCurrentNetworkStatusCode != HttpURLConnection.HTTP_OK){//如果之前网络响应状态不为OK,则重连成功
                    mCurrentNetworkStatusCode = HttpURLConnection.HTTP_OK;
                    Logger.i("重新连接服务器成功！");
                    sync_order_info();
                }
                final JSONObject  info_json = JSON.parseObject(retJson.getString("info"));
                switch (info_json.getString("status")){
                    case "n":
                        CustomApplication.sendMessage(MessageID.NETWORKSTATUS_ID,false);
                        Logger.e("网络检测服务器错误：" + info_json.getString("info"));
                        break;
                    case "y":
                        AbstractSyncBase.dealHeartBeatUpdate(Utils.getNullObjectAsEmptyJsonArray(info_json,"data"));
                        break;
                }
                break;
        }
        mCurrentNetworkStatusCode = err_code;

        postDelayed(this::testNetworkStatus,1000);
    }

    void sync_order_info(){
        startUploadRefundOrder();
        startUploadTransferOrder();
        startUploadRetailOrder(false);

        CustomApplication.sendMessageAtFrontOfQueue(MessageID.START_SYNC_ORDER_INFO_ID);
        CustomApplication.sendMessage(MessageID.FINISH_SYNC_ORDER_INFO_ID);
    }

    /*@param reupload true只会上传 上传状态失败的订单 false 只上传 未上传的订单*/
    private void uploadRetailOrderInfo(boolean reupload) {
        final StringBuilder err = new StringBuilder(),order_gp_ids = new StringBuilder();
        final String sql_orders = "SELECT discount_money,sc_ids sc_id,card_code,member_id,name,mobile,transfer_time,transfer_status,pay_time,pay_status,order_status,pos_code,addtime,cashier_id,total,\n" +
                "discount_price,order_code,stores_id,spare_param1,spare_param2,replace(remark,'&','') remark FROM retail_order where order_status = 2 and pay_status = 2 and upload_status = " + (reupload ? RetailOrderStatus.UPLOAD_ERROR : RetailOrderStatus.UN_UPLOAD) +" limit 200",
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
            if (!orders.isEmpty()){
                startUploadRetailOrder(reupload);
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

                                    send_data.put("appid",CustomApplication.self().getAppId());
                                    send_data.put("data",data);

                                    Logger.d_json(data.toJSONString());

                                    retJson = HttpUtils.sendPost(CustomApplication.self().getUrl() + "/api/retail_upload/order_upload",HttpRequest.generate_request_parma(send_data,CustomApplication.self().getAppSecret()),true);
                                    switch (retJson.getIntValue("flag")){
                                        case 0:
                                            code = false;
                                            err.append(retJson.getString("info"));
                                            break;
                                        case 1:
                                            retJson = JSON.parseObject(retJson.getString("info"));
                                            final ContentValues values = new ContentValues();
                                            switch (retJson.getString("status")){
                                                case "n":
                                                    values.put("upload_status", RetailOrderStatus.UPLOAD_ERROR);
                                                    err.append(retJson.getString("info"));
                                                    if (reupload){
                                                        MyDialog.toastMessage(err.toString());
                                                    }
                                                    break;
                                                case "y":
                                                    values.put("upload_status", RetailOrderStatus.UPLOADED);
                                                    break;
                                            }
                                            values.put("upload_time",System.currentTimeMillis() / 1000);
                                            int rows = SQLiteHelper.execUpdateSql("retail_order",values,"order_code = ?",new String[]{order_code},err);
                                            code = rows > 0;
                                            if (rows == 0){
                                                err.append("未更新任何数据！");
                                                Logger.e("销售单:%s,order_code:%s",err,order_code);
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
            }
        }
        if (!code || err.length() > 0){
            Logger.e("上传销售单据错误：%s",err);
            CustomApplication.transFailure();
        }else CustomApplication.transSuccess();
    }

    private void uploadTransferOrderInfo(){
        //上传交班单据
        String transfer_sum_sql = "SELECT  shopping_num,shopping_money,sj_money,cards_num oncecard_num,cards_money oncecard_money,\n" +
                "       order_money,order_e_date,order_b_date,recharge_num,recharge_money,refund_num,\n" +
                "       refund_money,cashbox_money unpaid_money,sum_money,ti_code,transfer_time,order_num,cas_id,stores_id FROM transfer_info where upload_status = 1 limit 100",details_where_sql = "where ti_code  = ",
                transfer_orders_sql = "SELECT ifnull(order_code,'') order_code FROM transfer_order ",
                transfer_retails_sql = "SELECT order_num,pay_method,pay_money FROM transfer_money_info ",transfer_cards_sql = "SELECT order_num,pay_method,pay_money FROM transfer_once_cardsc ",
                transfer_recharge_sql = "SELECT order_num,pay_method,pay_money FROM transfer_recharge_money ",transfer_refund_sql = "SELECT order_num,pay_method,pay_money FROM transfer_refund_money ",
                transfer_gift_sql = "SELECT order_num,pay_method,pay_money FROM transfer_gift_money ";
        final StringBuilder err = new StringBuilder();

        final JSONArray transfer_sum_arr = SQLiteHelper.getListToJson(transfer_sum_sql,err);
        if (null != transfer_sum_arr){
            final StringBuilder sz_ti_code = new StringBuilder(),sql_sb = new StringBuilder();
            String ti_code;
            JSONObject transfer_sum_obj;
            for (int i = 0,size = transfer_sum_arr.size();i < size;i++){
                transfer_sum_obj = transfer_sum_arr.getJSONObject(i);
                ti_code = transfer_sum_obj.getString("ti_code");

                if (!Utils.isNotEmpty(ti_code))continue;

                sz_ti_code.delete(0,sz_ti_code.length()).append(details_where_sql).append("'").append(ti_code).append("'");
                Logger.d("sz_ti_code:%s",sz_ti_code);

                sql_sb.delete(0,sql_sb.length()).append(transfer_orders_sql).append(sz_ti_code);
                final JSONArray transfer_orders_arr = SQLiteHelper.getListToValue(sql_sb.toString(),err);

                sql_sb.delete(0,sql_sb.length()).append(transfer_retails_sql).append(sz_ti_code);
                final JSONArray transfer_retails_arr = SQLiteHelper.getListToJson(sql_sb.toString(),err);

                sql_sb.delete(0,sql_sb.length()).append(transfer_cards_sql).append(sz_ti_code);
                final JSONArray transfer_cards_arr = SQLiteHelper.getListToJson(sql_sb.toString(),err);

                sql_sb.delete(0,sql_sb.length()).append(transfer_gift_sql).append(sz_ti_code);
                final JSONArray transfer_gift_arr = SQLiteHelper.getListToJson(sql_sb.toString(),err);

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
                    data.put("shopping_money",transfer_gift_arr);

                    Logger.d_json(data.toJSONString());

                    send_data.put("appid",CustomApplication.self().getAppId());
                    send_data.put("data",data);

                    JSONObject retJson = HttpUtils.sendPost(CustomApplication.self().getUrl() + "/api/transfer/transfer_upload",HttpRequest.generate_request_parma(send_data,CustomApplication.self().getAppSecret()),true);
                    switch (retJson.getIntValue("flag")){
                        case 0:
                            err.append(retJson.getString("info")).append(" sz_ti_code:").append(sz_ti_code);
                            break;
                        case 1:
                            retJson = JSON.parseObject(retJson.getString("info"));
                            final ContentValues values = new ContentValues();
                            switch (retJson.getString("status")){
                                case "n":
                                    values.put("upload_status",3);
                                    err.append(retJson.getString("info")).append(" sz_ti_code:").append(sz_ti_code);
                                    break;
                                case "y":
                                    ti_code = retJson.getString("ti_code");
                                    values.put("upload_status",2);
                                    break;
                            }
                            values.put("upload_time",System.currentTimeMillis() / 1000);
                            SQLiteHelper.execUpdateSql("transfer_info",values,"ti_code = ?",new String[]{ti_code},err);
                            break;
                    }
                }
            }
        }
        if (err.length() == 0){
            CustomApplication.transSuccess();
        }else {
            Logger.e("上传交班单据错误:%s",err);
            CustomApplication.transFailure();
        }
    }

    private void uploadRefundOrderInfo(){
        final StringBuilder err = new StringBuilder();

        final JSONArray refund_orders = SQLiteHelper.getListToJson("SELECT ifnull(order_code,'') order_code,ro_code FROM refund_order where upload_status = 1 and order_status = 2 limit 500",err);
        if (refund_orders != null){
            JSONObject obj;
            for (int i = 0,size = refund_orders.size();i < size;i++){
                obj = refund_orders.getJSONObject(i);
                RefundDialog.uploadRefundOrder(CustomApplication.self().getAppId(),CustomApplication.self().getUrl(),CustomApplication.self().getAppSecret(),Utils.getNullStringAsEmpty(obj,"order_code"),Utils.getNullStringAsEmpty(obj,"ro_code"),err);
            }
        }
        if (err.length() != 0){
            Logger.e("上传退货单据错误：%s",err);
            CustomApplication.transFailure();
        }else CustomApplication.transSuccess();
    }

    private void clear_download_record(){
        final String url = CustomApplication.self().getUrl() + "/api/cashier/clear_download_record";

        JSONObject object = new JSONObject();

        object.put("appid",CustomApplication.self().getAppId());
        object.put("pos_num",CustomApplication.self().getPosNum());
        object.put("stores_id",CustomApplication.self().getStoreId());

        object = HttpUtils.sendPost(url,HttpRequest.generate_request_parma(object,CustomApplication.self().getAppSecret()),true);
        boolean success;
        if (success = (object.getIntValue("flag") == 1)){
            object = JSON.parseObject(object.getString("info"));
            success = "y".equals(object.getString("status"));
        }
        if (!success)Logger.e("清空已同步数据错误:%s",object.getString("info"));
    }


    void stop(){
        sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_THREAD_QUIT_ID));
    }

    void syncAllBasics(){
        if (mCurrentNetworkStatusCode == HttpURLConnection.HTTP_OK){
            obtainMessage(MessageID.SYNC_BASICS_ID).sendToTarget();
        }
    }

    @SuppressWarnings("unused")
    void stopSync(){
        if (isPause)_continue();//如果已经暂停，则先唤醒线程
        removeMessages(MessageID.SYNC_BASICS_ID);
        removeMessages(MessageID.SYNC_FINISH_ID);
    }
    void startTestNetwork(){
        if (!hasMessages(MessageID.NETWORKSTATUS_ID)){
            sendMessageAtFrontOfQueue(obtainMessage(MessageID.NETWORKSTATUS_ID));
        }
    }
    void startUploadRetailOrder(boolean reupload){
        if (mCurrentNetworkStatusCode == HttpURLConnection.HTTP_OK){
            sendMessageAtFrontOfQueue(obtainMessage(MessageID.UPLOAD_ORDER_ID,reupload));
        }
    }
    void startUploadTransferOrder(){
        if (mCurrentNetworkStatusCode == HttpURLConnection.HTTP_OK){
            sendMessageAtFrontOfQueue(obtainMessage(MessageID.UPLOAD_TRANS_ORDER_ID));
        }
    }
    void startUploadRefundOrder(){
        if (mCurrentNetworkStatusCode == HttpURLConnection.HTTP_OK){
            sendMessageAtFrontOfQueue(obtainMessage(MessageID.UPLOAD_REFUND_ORDER_ID));
        }
    }
    @SuppressWarnings("unused")
    void pause(){
        if (!isPause)sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_PAUSE_ID));
    }
    void _continue(){
        synchronized (this){
            if (isPause) notify();
        }
    }
    void sign_downloaded(){//标记已下载
        if (mCurrentNetworkStatusCode == HttpURLConnection.HTTP_OK && !hasMessages(MessageID.MARK_DOWNLOAD_RECORD_ID)) {
            obtainMessage(MessageID.MARK_DOWNLOAD_RECORD_ID).sendToTarget();
        }
    }
}
