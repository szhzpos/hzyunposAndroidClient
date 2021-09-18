package com.wyc.cloudapp.application;

import android.content.ContentValues;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.constants.RetailOrderStatus;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.constants.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.io.File;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static com.wyc.cloudapp.constants.MessageID.SYNC_DIS_INFO_ID;

final class SyncHandler extends Handler {
    private final HttpRequest mHttp;
    private volatile boolean mReportProgress = true,isPause = false;
    private volatile int mCurrentNetworkStatusCode = HttpURLConnection.HTTP_OK;
    private long mLoseTime = 0;
    private JSONObject mHeartbeat;

    private String mAppId, mAppSecret,mUrl,mPosNum,mOperId,mStoresId;

    private static final List<Integer> mSyncBasicsDataMessageIds = getBasicsDataMessageIds();

    SyncHandler(Looper looper){
        super(looper);
        mHttp = new HttpRequest();
        mHttp.setConnTimeOut(3000);
    }

    private static List<Integer> getBasicsDataMessageIds(){
        return Arrays.asList(MessageID.SYNC_CASHIER_ID/*收银员*/,MessageID.SYNC_GOODS_CATEGORY_ID/*商品类别*/,MessageID.SYNC_PAY_METHOD_ID/*支付方式*/,MessageID.SYNC_STORES_ID/*仓库信息*/,
                MessageID.SYNC_GP_INFO_ID/*组合商品*/,MessageID.SYNC_GOODS_ID/*商品信息*/,MessageID.SYNC_FULLREDUCE_ID/*满减信息*/,MessageID.SYNC_SALES_INFO_ID/*营业员信息*/,
                MessageID.SYNC_SALE_OPERATOR_INFO_ID/*经办人信息*/,MessageID.SYNC_PROMOTION_ID/*促销信息*/,MessageID.SYNC_STEP_PROMOTION_ID/*阶梯促销*/,MessageID.SYNC_STEP_FULLREDUCE_ID/*阶梯满减*/,
                MessageID.SYNC_BUY_X_GIVE_X_ID,MessageID.SYNC_BUY_FULL_GIVE_X_ID,MessageID.SYNC_AUXILIARY_BARCODE_ID/*辅助条码*/);
    }

    List<String> getSyncDataTableName(){
        return Arrays.asList("shop_category","shop_stores","barcode_info","pay_method","cashier_info","fullreduce_info","sales_info",
                "promotion_info","sale_operator_info","goods_group", "goods_group_info","buyfull_give_x","buy_x_give_x","step_promotion_info","auxiliary_barcode_info");
    }

    String[] getGoodsCols(){
        return new String[]{"goods_id","barcode_id","barcode","goods_title","only_coding","retail_price","buying_price","trade_price","cost_price","ps_price",
                "unit_id","unit_name","specifi","category_name","metering_id","current_goods","shelf_life","goods_status","origin","type","goods_tare","barcode_status","category_id",
                "tax_rate","tc_mode","tc_rate","yh_mode","yh_price","mnemonic_code","image","attr_id","attr_name","attr_code","brand_id","brand","brand_code","gs_id","gs_code","gs_name",
                "conversion","update_price","stock_unit_id","stock_unit_name","goods_img","img_url","spec_str","cash_flow_ratio"};
    }

    void initParameter(final String url, final String appid, final String appsecret, final String stores_id, final String pos_num, final String operid){
        mUrl = url ;
        mAppId = appid;
        mAppSecret = appsecret;
        mPosNum = pos_num;
        mOperId = operid;
        mStoresId = stores_id;
    }

    @Override
    public void handleMessage(Message msg){

        final String base_url =  mUrl,app_id = mAppId,appSecret = mAppSecret,pos_num = mPosNum,oper_id = mOperId,stores_id = mStoresId;
        JSONObject object = new JSONObject(),info_json,retJson;
        String table_name = "",sys_name = "",url = "",sz_param;
        String[] table_cls = null;
        boolean code = true;
        try{
            switch (msg.what) {
                case MessageID.SYNC_GOODS_BASE_ID:

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
                case MessageID.SYNC_GOODS_CATEGORY_ID:
                    table_name = "shop_category";
                    sys_name = "正在同步商品类别";
                    table_cls = new String[]{"category_id","category_code","name","parent_id","depth","path","status","sort"};
                    url = base_url + "/api/scale/get_category_info";
                    object.put("pos_num",pos_num);
                    object.put("page",msg.obj);
                    break;
                case MessageID.SYNC_STORES_ID://仓库信息
                    table_name = "shop_stores";
                    table_cls = new String[]{"stores_id","stores_name","manager","telphone","region","status","nature","wh_id"};
                    sys_name = "正在同步仓库";
                    url = base_url + "/api/scale/get_stores";
                    object.put("pt_user_id",oper_id);
                    break;
                case MessageID.SYNC_GOODS_ID://商品信息
                    table_name = "barcode_info";
                    sys_name = "正在同步商品";
                    table_cls = getGoodsCols();
                    url = base_url + "/api/goods/get_goods_all";
                    object.put("stores_id",stores_id);
                    object.put("pos_num",pos_num);
                    object.put("page",msg.obj);
                    object.put("limit",100);
                    break;
                case MessageID.SYNC_PAY_METHOD_ID://支付方式
                    table_name = "pay_method";
                    table_cls = new String[]{"pay_method_id","name","status","remark","is_check","shortcut_key","sort","xtype","pay_img","master_img",
                            "is_show_client","is_cardno","is_scan","wr_btn_img","unified_pay_order","unified_pay_query","rule","is_open","is_enable","is_moling","support"};
                    sys_name = "正在同步支付方式";
                    url = base_url + "/api/cashier/get_pm_info";
                    object.put("stores_id",stores_id);
                    object.put("page",msg.obj);
                    object.put("pos_num",pos_num);
                    break;
                case MessageID.SYNC_CASHIER_ID://收银员
                    table_name = "cashier_info";
                    table_cls = new String[]{"cas_id","stores_id","stores_name","cas_name","cas_account","cas_pwd","cas_addtime","cas_code","cas_phone","cas_status",
                            "min_discount","is_refund","is_give","is_put","pt_user_id","pt_user_cname","remark","authority"};

                    sys_name = "正在同步门店收银员";
                    url = base_url + "/api/scale/get_cashier_info";

                    object.put("cas_id",oper_id);
                    object.put("pos_num",pos_num);
                    object.put("stores_id",stores_id);
                    break;
                case MessageID.SYNC_GP_INFO_ID://商品组合信息
                    sys_name = "正在同步组合商品";
                    url = base_url + "/api/promotion/get_gp_info";
                    object.put("page",msg.obj);
                    object.put("pos_num",pos_num);
                    object.put("stores_id",stores_id);
                    break;
                case MessageID.SYNC_FULLREDUCE_ID:
                    table_name = "fullreduce_info_new";
                    table_cls = new String[]{"tlp_id","tlpb_id","title","promotion_type","type_detail_id","promotion_object","promotion_grade_id","cumulation_give","buyfull_money","reduce_money","start_date"
                            ,"end_date","promotion_week","begin_time","end_time","status","xtype"};
                    sys_name = "正在同步满减";
                    url = base_url + "/api/promotion/get_promotion_fullreduce";
                    object.put("stores_id",stores_id);
                    object.put("pos_num",pos_num);
                    break;
                case MessageID.SYNC_STEP_FULLREDUCE_ID:
                    table_name = "fullreduce_info";
                    table_cls = new String[]{"full_id","title","modes","fold","rule","start_time","end_time","starttime","endtime"};
                    sys_name = "正在同步阶梯满减";
                    url = base_url + "/api/promotion/fullreduce_info";
                    object.put("stores_id",stores_id);
                    object.put("type",1);
                    break;
                case MessageID.SYNC_BUY_X_GIVE_X_ID:
                    table_name = "buy_x_give_x";
                    table_cls = new String[]{"tlp_id","tlpb_id","promotion_type","promotion_object","promotion_grade_id","cumulation_give","xnum_buy","xnum_give","markup_price","barcode_id"
                            ,"barcode_id_give","start_date","end_date","promotion_week","begin_time","end_time","status","xtype"};
                    sys_name = "正在同步买X送X";
                    url = base_url + "/api/promotion/get_promotion_buyx_givex";
                    object.put("stores_id",stores_id);
                    object.put("pos_num",pos_num);
                    break;
                case MessageID.SYNC_BUY_FULL_GIVE_X_ID:
                    table_name = "buyfull_give_x";
                    table_cls = new String[]{"tlp_id","tlpb_id","title","type_detail_id","promotion_type","promotion_object","promotion_grade_id","cumulation_give","fullgive_way","give_way","item_discount","buyfull_money"
                            ,"givex_goods_info","start_date","end_date","promotion_week","begin_time","end_time","status","xtype"};
                    sys_name = "正在同步买满送X";
                    url = base_url + "/api/promotion/get_promotion_buyfull_givex";
                    object.put("stores_id",stores_id);
                    object.put("pos_num",pos_num);
                    break;
                case MessageID.SYNC_SALES_INFO_ID:
                    table_name = "sales_info";
                    table_cls = new String[]{"sc_id","sc_name","sc_phone","stores_id","tc_mode","is_tc","tc_rate","sc_status","appids","sc_addtime"};
                    sys_name = "正在同步营业员";
                    url = base_url + "/api/cashier/get_sc_info";
                    object.put("stores_id",stores_id);
                    object.put("page",msg.obj);
                    object.put("pos_num",pos_num);
                    break;
                case MessageID.SYNC_PROMOTION_ID:
                    table_name = "promotion_info";
                    table_cls = new String[]{"tlpb_id","tlp_id","barcode_id","type_detail_id","status","way","limit_xnum","promotion_object","promotion_grade_id","promotion_type","promotion_price","stores_id",
                            "start_date","end_date","promotion_week","begin_time","end_time","xtype"};
                    sys_name = "正在同步零售特价促销";
                    url = base_url + "/api/promotion/get_promotion_info";
                    object.put("stores_id",stores_id);
                    object.put("page",msg.obj);
                    object.put("pos_num",pos_num);
                    break;
                case MessageID.SYNC_STEP_PROMOTION_ID:
                    table_name = "step_promotion_info";
                    table_cls = new String[]{"tlpb_id","tlp_id","type_detail_id","status","way","promotion_object","promotion_grade_id","promotion_type",
                            "xnum_one","promotion_price_one","xnum_two","promotion_price_two","xnum_three","promotion_price_three","xnum_four","promotion_price_four","xnum_five","promotion_price_five",
                            "stores_id","start_date","end_date","promotion_week","begin_time","end_time","xtype"};
                    sys_name = "正在同步零售阶梯促销";
                    url = base_url + "/api/promotion/get_promotion_sellstep";
                    object.put("stores_id",stores_id);
                    object.put("page",msg.obj);
                    object.put("pos_num",pos_num);
                    break;
                case MessageID.SYNC_SALE_OPERATOR_INFO_ID:
                    table_name = "sale_operator_info";
                    sys_name = "正在同步经办人";
                    table_cls = new String[]{"sales_id","name","sort"};
                    url = base_url + "/api/Jingbanren/xlist";
                    object.put("stores_id",stores_id);
                    break;
                case MessageID.SYNC_AUXILIARY_BARCODE_ID:
                    table_name = "auxiliary_barcode_info";
                    sys_name = "正在辅助条码";
                    table_cls = new String[]{"id","g_m_id","barcode_id","fuzhu_barcode","status"};
                    url = base_url + "/api/goods_set/get_fuzhu_barcode";
                    object.put("stores_id",stores_id);
                    object.put("page",msg.obj);
                    object.put("limit",500);
                    object.put("pos_num",pos_num);
                    break;
                case MessageID.SYNC_FINISH_ID:
                    CustomApplication.sendMessage(MessageID.SYNC_FINISH_ID);//同步完成
                    return;
                case MessageID.NETWORKSTATUS_ID:
                    testNetworkStatus();
                    return;
                case MessageID.UPLOAD_ORDER_ID:
                    boolean reupload = false;
                    if (msg.obj instanceof Boolean){
                        reupload =  (Boolean) msg.obj;
                    }
                    uploadRetailOrderInfo(app_id,base_url,appSecret,reupload);
                    return;
                case MessageID.UPLOAD_TRANS_ORDER_ID:
                    uploadTransferOrderInfo(app_id,base_url,appSecret);
                    return;
                case MessageID.UPLOAD_REFUND_ORDER_ID:
                    uploadRefundOrderInfo(app_id,base_url,appSecret);
                    return;
                case MessageID.MODIFY_REPORT_PROGRESS_ID:
                    if (msg.obj instanceof  Boolean)
                        mReportProgress = (boolean)msg.obj;
                    return;
                case MessageID.MARK_DOWNLOAD_RECORD_ID:
                    if (mReportProgress)CustomApplication.sendMessage(SYNC_DIS_INFO_ID,"正在更新信息....");
                    clear_download_record();
                    return;
                case MessageID.SYNC_THREAD_QUIT_ID://由于处理程序内部会发送消息，消息队列退出需在处理程序内部处理
                    if (mHttp != null)mHttp.clearConnection(HttpRequest.CLOSEMODE.BOTH);
                    this.removeCallbacksAndMessages(null);
                    getLooper().quitSafely();
                    return;

            }

            if (mReportProgress)CustomApplication.sendMessage(SYNC_DIS_INFO_ID,sys_name + "信息....");

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
                            final JSONArray data;
                            final StringBuilder err = new StringBuilder();
                            if (msg.what == MessageID.SYNC_STEP_FULLREDUCE_ID){
                                if (SQLiteHelper.execDelete("fullreduce_info",null,null,err) < 0){
                                    code = false;
                                    data = new JSONArray();
                                }else {
                                    final String json = Utils.getNullStringAsEmpty(info_json,"data");
                                    if (json.startsWith("{")){
                                        final JSONObject obj = info_json.getJSONObject("data");
                                        data = Utils.getNullObjectAsEmptyJsonArray(obj,"fullreduce");
                                    }else {
                                        data = Utils.getNullObjectAsEmptyJsonArray(info_json,"data");
                                    }
                                }
                            }else {
                                data = Utils.getNullObjectAsEmptyJsonArray(info_json,"data");
                            }

                            if(data.size() != 0){
                                switch (msg.what){
                                    case MessageID.SYNC_GP_INFO_ID:{
                                        if ((code = deal_good_group(data,err))){
                                            int max_page = info_json.getIntValue("max_page"),current_page = (int)msg.obj;
                                            if ((current_page++ <= max_page)){
                                                Logger.d("current_page:%d,max_page:%d",current_page,max_page);
                                                sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_GP_INFO_ID,current_page));
                                            }
                                        }
                                    }
                                        break;
                                    case MessageID.SYNC_BUY_FULL_GIVE_X_ID:
                                    case MessageID.SYNC_BUY_X_GIVE_X_ID:
                                    case MessageID.SYNC_PROMOTION_ID:
                                    case MessageID.SYNC_STEP_PROMOTION_ID:
                                    case MessageID.SYNC_AUXILIARY_BARCODE_ID:
                                    case MessageID.SYNC_FULLREDUCE_ID:
                                    case MessageID.SYNC_PAY_METHOD_ID:
                                    case MessageID.SYNC_GOODS_CATEGORY_ID:
                                    case MessageID.SYNC_SALES_INFO_ID:
                                    case MessageID.SYNC_GOODS_ID: {
                                        int max_page = info_json.getIntValue("max_page"),current_page = (int)msg.obj;
                                        if((code = SQLiteHelper.execSQLByBatchFromJson(data,table_name ,table_cls,err,1))){
                                            if (msg.what ==  MessageID.SYNC_GOODS_ID) {
                                                download_goods_img_and_upload_barcode_id(data);//保存成功才能标记商品已获取
                                                if ((current_page++ <= max_page)){
                                                    Logger.d("current_page:%d,max_page:%d",current_page,max_page);
                                                    sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_GOODS_ID,current_page));
                                                }
                                            }else if (msg.what ==  MessageID.SYNC_GOODS_CATEGORY_ID){
                                                up_category(data);
                                                if ((current_page++ <= max_page)){
                                                    Logger.d("current_page:%d,max_page:%d",current_page,max_page);
                                                    sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_GOODS_CATEGORY_ID,current_page));
                                                }
                                            }else if (msg.what ==  MessageID.SYNC_PAY_METHOD_ID){
                                                download_pay_method_img_and_upload_pay_method(data);
                                                if ((current_page++ <= max_page)){
                                                    Logger.d("current_page:%d,max_page:%d",current_page,max_page);
                                                    sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_PAY_METHOD_ID,current_page));
                                                }
                                            }else if (msg.what == MessageID.SYNC_PROMOTION_ID){
                                                up_promotion(data);
                                                if ((current_page++ <= max_page)){
                                                    Logger.d("current_page:%d,max_page:%d",current_page,max_page);
                                                    sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_PROMOTION_ID,current_page));
                                                }
                                            }else if (MessageID.SYNC_SALES_INFO_ID == msg.what){
                                                up_sales(data);
                                                if ((current_page++ <= max_page)){
                                                    Logger.d("current_page:%d,max_page:%d",current_page,max_page);
                                                    sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_SALES_INFO_ID,current_page));
                                                }
                                            }else if (MessageID.SYNC_FULLREDUCE_ID == msg.what){
                                                up_fullreduce(data);
                                                if ((current_page++ <= max_page)){
                                                    Logger.d("current_page:%d,max_page:%d",current_page,max_page);
                                                    sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_FULLREDUCE_ID,current_page));
                                                }
                                            }else if (MessageID.SYNC_BUY_X_GIVE_X_ID == msg.what){
                                                up_buy_x_give_x(data);
                                                if ((current_page++ <= max_page)){
                                                    Logger.d("current_page:%d,max_page:%d",current_page,max_page);
                                                    sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_BUY_X_GIVE_X_ID,current_page));
                                                }
                                            }else if (MessageID.SYNC_BUY_FULL_GIVE_X_ID == msg.what){
                                                up_buy_full_give_x(data);
                                                if ((current_page++ <= max_page)){
                                                    Logger.d("current_page:%d,max_page:%d",current_page,max_page);
                                                    sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_BUY_FULL_GIVE_X_ID,current_page));
                                                }
                                            }else if (MessageID.SYNC_STEP_PROMOTION_ID == msg.what){
                                                up_step_promotion(data);
                                                if ((current_page++ <= max_page)){
                                                    Logger.d("current_page:%d,max_page:%d",current_page,max_page);
                                                    sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_BUY_FULL_GIVE_X_ID,current_page));
                                                }
                                            }else if (MessageID.SYNC_AUXILIARY_BARCODE_ID == msg.what){
                                                up_auxiliary_barcode(data);
                                                if ((current_page++ <= max_page)){
                                                    //Logger.d("current_page:%d,max_page:%d",current_page,max_page);
                                                    sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_AUXILIARY_BARCODE_ID,current_page));
                                                }
                                            }
                                        }
                                    }
                                        break;
                                        default:
                                            code = SQLiteHelper.execSQLByBatchFromJson(data,table_name ,table_cls,err,1);
                                            break;
                                }
                            }
                            if (!code)sys_name = sys_name.concat("错误:").concat(err.toString());
                            break;
                    }
                    break;
            }
            if (!code) {
                stopSync();
                if (mReportProgress) {
                    CustomApplication.sendMessage(MessageID.SYNC_ERR_ID, sys_name);
                }else{
                    CustomApplication.sendMessage(MessageID.TRANSFERSTATUS_ID,false);
                    Logger.e("%s", sys_name);
                }
            }else{
                if (!mReportProgress)
                    CustomApplication.sendMessage(MessageID.TRANSFERSTATUS_ID,true);
            }
        }catch (JSONException e){
            e.printStackTrace();
            sys_name = "同步" + table_name + "错误:" +  e.getMessage();
            if (mReportProgress) {
                removeCallbacksAndMessages(null);
                CustomApplication.sendMessage(MessageID.SYNC_ERR_ID, sys_name);
            }else {
                CustomApplication.sendMessage(MessageID.TRANSFERSTATUS_ID,false);
                Logger.e("%s", sys_name);
            }
        }
    }

    private void testNetworkStatus() throws JSONException {
        int syncInterval = 5000;
        final String test_url = mUrl + "/api/heartbeat/index";
        int err_code;

        if (mHeartbeat == null){
            final JSONObject data = new JSONObject();
            data.put("appid",mAppId);
            data.put("pos_num",mPosNum);
            data.put("cas_id",mOperId);
            mHeartbeat = data;
        }
        mHeartbeat.put("randstr", Utils.getNonce_str(8));

        final JSONObject retJson = mHttp.sendPost(test_url,HttpRequest.generate_request_parm(mHeartbeat,mAppSecret),true);
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
                        if (System.currentTimeMillis() - mLoseTime >= syncInterval && mCurrentNetworkStatusCode == HttpURLConnection.HTTP_OK) {
                            mLoseTime = System.currentTimeMillis();
                            if (mReportProgress)modifyReportProgressStatus(false);
                            sync();
                        }
                        //Logger.d_json(info_json.getString("data"));
                        break;
                }
                break;
        }
        mCurrentNetworkStatusCode = err_code;
    }

    void sync_order_info(){
        startUploadRefundOrder();
        startUploadTransferOrder();
        startUploadRetailOrder(false);

        CustomApplication.sendMessageAtFrontOfQueue(MessageID.START_SYNC_ORDER_INFO_ID);
        CustomApplication.sendMessage(MessageID.FINISH_SYNC_ORDER_INFO_ID);
    }

    /*@param reupload true只会上传 上传状态失败的订单 false 只上传 未上传的订单*/
    private void uploadRetailOrderInfo(final String appid,final String url,final String appSecret,boolean reupload) {
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

                                    send_data.put("appid",appid);
                                    send_data.put("data",data);

                                    Logger.d_json(data.toJSONString());

                                    retJson = mHttp.sendPost(url + "/api/retail_upload/order_upload",HttpRequest.generate_request_parm(send_data,appSecret),true);
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
                                                        MyDialog.ToastMessageInMainThread(err.toString());
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
            CustomApplication.sendMessage(MessageID.TRANSFERSTATUS_ID,false);
        }
    }

    private void uploadTransferOrderInfo(final String appid,final String url,final String appSecret){
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

                    send_data.put("appid",appid);
                    send_data.put("data",data);

                    JSONObject retJson = mHttp.sendPost(url + "/api/transfer/transfer_upload",HttpRequest.generate_request_parm(send_data,appSecret),true);
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
        if (err.length() != 0){
            Logger.e("上传交班单据错误:%s",err);
            CustomApplication.sendMessage(MessageID.TRANSFERSTATUS_ID,false);
        }
    }

    private void uploadRefundOrderInfo(final String appid,final String url,final String appSecret){
        final StringBuilder err = new StringBuilder();

        final JSONArray refund_orders = SQLiteHelper.getListToJson("SELECT ifnull(order_code,'') order_code,ro_code FROM refund_order where upload_status = 1 and order_status = 2 limit 500",err);
        if (refund_orders != null){
            JSONObject obj;
            for (int i = 0,size = refund_orders.size();i < size;i++){
                obj = refund_orders.getJSONObject(i);
                RefundDialog.uploadRefundOrder(appid,url,appSecret,Utils.getNullStringAsEmpty(obj,"order_code"),Utils.getNullStringAsEmpty(obj,"ro_code"),err);
            }
        }
        if (err.length() != 0){
            Logger.e("上传退货单据错误：%s",err);
            CustomApplication.sendMessage(MessageID.TRANSFERSTATUS_ID,false);
        }
    }

    private void download_pay_method_img_and_upload_pay_method(@NonNull final JSONArray datas) throws JSONException {
        String img_url_info,img_file_name;
        JSONObject object;
        final JSONArray pay_method_ids = new JSONArray();
        for (int k = 0,length = datas.size();k < length;k++){
            object = datas.getJSONObject(k);
            pay_method_ids.add(object.getIntValue("pay_method_id"));
            img_url_info = object.getString("pay_img");
            if (!img_url_info.equals("")){
                img_file_name = img_url_info.substring(img_url_info.lastIndexOf("/") + 1);
                final File file = new File(CustomApplication.getGoodsImgSavePath() + img_file_name);
                if (!file.exists()){
                    JSONObject load_img = mHttp.getFile(file,img_url_info);
                    if (load_img.getIntValue("flag") == 0){
                        Logger.e("下载支付方式图片错误：" + load_img.getString("info"));
                    }
                }
            }
        }
        if (pay_method_ids.size() != 0){
            upload_pay_method_id(pay_method_ids);
        }
    }
    private void upload_pay_method_id(final JSONArray datas) throws JSONException {
        if (datas != null && !datas.isEmpty()){
            final String url = mUrl + "/api/cashier/up_pm";

            JSONObject object = new JSONObject();

            object.put("appid",mAppId);
            object.put("pay_method_ids",datas);
            object.put("pos_num",mPosNum);

            object = mHttp.sendPost(url,HttpRequest.generate_request_parm(object,mAppSecret),true);
            if (object.getIntValue("flag") == 1){
                object = JSON.parseObject(object.getString("info"));
                if ("n".equals(object.getString("status"))){
                    Logger.e("标记已获取支付方式错误:" + object.getString("info"));
                }
            }else{
                Logger.e("标记已获取支付方式错误:" + object.getString("info"));
            }
        }
    }

    private void download_goods_img_and_upload_barcode_id(@NonNull final JSONArray datas) throws JSONException {
        String img_url_info,img_file_name;
        final JSONArray goods_ids = new JSONArray();
        JSONObject object;
        for (int k = 0,length = datas.size();k < length;k++){
            object = datas.getJSONObject(k);
            goods_ids.add(object.getIntValue("barcode_id"));
            img_url_info = object.getString("img_url");
            if (!"".equals(img_url_info)){
                img_file_name = img_url_info.substring(img_url_info.lastIndexOf("/") + 1);
                File file = new File(CustomApplication.getGoodsImgSavePath() + img_file_name);
                if (!file.exists()){
                    JSONObject load_img = mHttp.getFile(file,img_url_info);
                    if (load_img.getIntValue("flag") == 0){
                        Logger.e("下载商品图片错误：" + load_img.getString("info"));
                    }
                }
            }
        }
        if (goods_ids.size() != 0){
            upload_barcode_id(goods_ids);
        }
    }
    private boolean deal_good_group(@NonNull JSONArray data,StringBuilder err) throws JSONException {
        final JSONArray goods_list = new JSONArray(),gp_ids = new JSONArray();
        final JSONObject tmp_goods = new JSONObject();

        JSONObject gp_obj;
        for (int k = 0, size = data.size(); k < size; k++) {
            gp_obj = data.getJSONObject(k);

            gp_ids.add(gp_obj.get("gp_id"));

            final JSONArray tmp = (JSONArray)gp_obj.remove("goods_list");
            if (tmp != null)
                for (int j = 0, length = tmp.size(); j < length; j++) {
                    goods_list.add(tmp.get(j));
                }
        }
        tmp_goods.put("goods_group", data);
        tmp_goods.put("goods_group_info", goods_list);
        List<String> goods_group_cols = Arrays.asList("mnemonic_code", "gp_id", "gp_code", "gp_title", "gp_price", "status", "addtime", "unit_name", "stores_id", "img_url"),
                goods_group_info_cols = Arrays.asList("xnum", "barcode_id", "gp_id", "_id");

        boolean code = SQLiteHelper.execSQLByBatchFromJson(tmp_goods, Arrays.asList("goods_group", "goods_group_info"), Arrays.asList(goods_group_cols, goods_group_info_cols), err, 1);
        if (code){
            up_gp_goods(gp_ids);
        }
        return code;
    }
    private void up_gp_goods(final JSONArray datas) throws JSONException{
        if (datas != null && !datas.isEmpty()){
            final String url = mUrl + "/api/promotion/up_gp";

            JSONObject object = new JSONObject();

            object.put("appid",mAppId);
            object.put("gp_ids",datas);
            object.put("pos_num",mPosNum);

            object = mHttp.sendPost(url,HttpRequest.generate_request_parm(object,mAppSecret),true);
            boolean success;
            if (success = (object.getIntValue("flag") == 1)){
                object = JSON.parseObject(object.getString("info"));
                success = "y".equals(object.getString("status"));
            }
            if (!success)Logger.e("标记已获取的组合商品错误:" + object.getString("info"));
        }
    }

    private void upload_barcode_id(final JSONArray datas) throws JSONException {
        if (datas != null && !datas.isEmpty()){
            final String url = mUrl + "/api/goods/up_goods";

            JSONObject object = new JSONObject();

            object.put("appid",mAppId);
            object.put("goods_ids",datas);
            object.put("barcode_ids",datas);
            object.put("pos_num",mPosNum);

            object = mHttp.sendPost(url,HttpRequest.generate_request_parm(object,mAppSecret),true);
            boolean success;
            if (success = (object.getIntValue("flag") == 1)){
                object = JSON.parseObject(object.getString("info"));
                success = "y".equals(object.getString("status"));
            }
            if (!success)Logger.e("标记已获取商品错误:" + object.getString("info"));
        }
    }
    private void clear_download_record(){
        final String url = mUrl + "/api/cashier/clear_download_record";

        JSONObject object = new JSONObject();

        object.put("appid",mAppId);
        object.put("pos_num",mPosNum);
        object.put("stores_id",mStoresId);

        object = mHttp.sendPost(url,HttpRequest.generate_request_parm(object,mAppSecret),true);
        boolean success;
        if (success = (object.getIntValue("flag") == 1)){
            object = JSON.parseObject(object.getString("info"));
            success = "y".equals(object.getString("status"));
        }
        if (!success)Logger.e("清空已同步数据错误:%s",object.getString("info"));
    }

    private void up_category(final JSONArray datas) throws JSONException{
        if (datas != null && !datas.isEmpty()){
            JSONObject object;
            final String url = mUrl + "/api/scale/up_category";

            final JSONArray category_ids = new JSONArray();
            for (int k = 0,length = datas.size();k < length;k++) {
                object = datas.getJSONObject(k);
                category_ids.add(object.getIntValue("category_id"));
            }

            object = new JSONObject();
            object.put("appid",mAppId);
            object.put("category_ids",category_ids);
            object.put("pos_num",mPosNum);

            object = mHttp.sendPost(url,HttpRequest.generate_request_parm(object,mAppSecret),true);
            boolean success;
            if (success = (object.getIntValue("flag") == 1)){
                object = JSON.parseObject(object.getString("info"));
                success = "y".equals(object.getString("status"));
            }
            if (!success)Logger.e("标记已获取类别错误:" + object.getString("info"));
        }
    }

    private void up_sales(final JSONArray datas) throws JSONException{
        if (datas != null && !datas.isEmpty()){
            JSONObject object;
            final String url = mUrl + "/api/cashier/up_sc";

            final JSONArray sc_ids = new JSONArray();
            for (int k = 0,length = datas.size();k < length;k++) {
                object = datas.getJSONObject(k);
                sc_ids.add(object.getIntValue("sc_id"));
            }

            object = new JSONObject();
            object.put("appid",mAppId);
            object.put("sc_ids",sc_ids);
            object.put("pos_num",mPosNum);

            object = mHttp.sendPost(url,HttpRequest.generate_request_parm(object,mAppSecret),true);
            boolean success;
            if (success = (object.getIntValue("flag") == 1)){
                object = JSON.parseObject(object.getString("info"));
                success = "y".equals(object.getString("status"));
            }
            if (!success)Logger.e("标记已获取营业员错误:" + object.getString("info"));
        }
    }

    private void up_promotion(final JSONArray datas) throws JSONException{
        if (datas != null && !datas.isEmpty()){
            JSONObject object;
            final String url = mUrl + "/api/promotion/up_promotion";

            final JSONArray tlp_ids = new JSONArray();
            for (int k = 0,length = datas.size();k < length;k++) {
                object = datas.getJSONObject(k);
                tlp_ids.add(object.getIntValue("tlp_id"));
            }

            object = new JSONObject();
            object.put("appid",mAppId);
            object.put("tlp_ids",tlp_ids);
            object.put("pos_num",mPosNum);

            object = mHttp.sendPost(url,HttpRequest.generate_request_parm(object,mAppSecret),true);
            boolean success;
            if (success = (object.getIntValue("flag") == 1)){
                object = JSON.parseObject(object.getString("info"));
                success = "y".equals(object.getString("status"));
            }
            if (!success)Logger.e("标记已获取促销信息错误:" + object.getString("info"));
        }
    }

    private void up_step_promotion(final JSONArray datas) throws JSONException{
        if (datas != null && !datas.isEmpty()){
            JSONObject object;
            final String url = mUrl + "/api/promotion/up_promotion_sellstep";

            final JSONArray tlp_ids = new JSONArray();
            for (int k = 0,length = datas.size();k < length;k++) {
                object = datas.getJSONObject(k);
                tlp_ids.add(object.getIntValue("tlp_id"));
            }

            object = new JSONObject();
            object.put("appid",mAppId);
            object.put("tlp_ids",tlp_ids);
            object.put("pos_num",mPosNum);

            object = mHttp.sendPost(url,HttpRequest.generate_request_parm(object,mAppSecret),true);
            boolean success;
            if (success = (object.getIntValue("flag") == 1)){
                object = JSON.parseObject(object.getString("info"));
                success = "y".equals(object.getString("status"));
            }
            if (!success)Logger.e("标记已获取阶梯促销信息错误:" + object.getString("info"));
        }
    }
    private void up_fullreduce(final JSONArray datas) throws JSONException{
        if (datas != null && !datas.isEmpty()){
            JSONObject object;
            final String url = mUrl + "/api/promotion/up_promotion_fullreduce";

            final JSONArray tlp_ids = new JSONArray();
            for (int k = 0,length = datas.size();k < length;k++) {
                object = datas.getJSONObject(k);
                tlp_ids.add(object.getIntValue("tlp_id"));
            }

            object = new JSONObject();
            object.put("appid",mAppId);
            object.put("tlp_ids",tlp_ids);
            object.put("pos_num",mPosNum);

            object = mHttp.sendPost(url,HttpRequest.generate_request_parm(object,mAppSecret),true);
            boolean success;
            if (success = (object.getIntValue("flag") == 1)){
                object = JSON.parseObject(object.getString("info"));
                success = "y".equals(object.getString("status"));
            }
            if (!success)Logger.e("标记已获满减信息错误:" + object.getString("info"));
        }
    }

    private void up_buy_x_give_x(final JSONArray datas) throws JSONException{
        if (datas != null && !datas.isEmpty()){
            JSONObject object;
            final String url = mUrl + "/api/promotion/up_promotion_buyx_givex";

            final JSONArray tlp_ids = new JSONArray();
            for (int k = 0,length = datas.size();k < length;k++) {
                object = datas.getJSONObject(k);
                tlp_ids.add(object.getIntValue("tlp_id"));
            }

            object = new JSONObject();
            object.put("appid",mAppId);
            object.put("tlp_ids",tlp_ids);
            object.put("pos_num",mPosNum);

            object = mHttp.sendPost(url,HttpRequest.generate_request_parm(object,mAppSecret),true);
            boolean success;
            if (success = (object.getIntValue("flag") == 1)){
                object = JSON.parseObject(object.getString("info"));
                success = "y".equals(object.getString("status"));
            }
            if (!success)Logger.e("标记已获买X送X信息错误:" + object.getString("info"));
        }
    }

    private void up_buy_full_give_x(final JSONArray datas) throws JSONException{
        if (datas != null && !datas.isEmpty()){
            JSONObject object;
            final String url = mUrl + "/api/promotion/up_promotion_buyfull_givex";

            final JSONArray tlp_ids = new JSONArray();
            for (int k = 0,length = datas.size();k < length;k++) {
                object = datas.getJSONObject(k);
                tlp_ids.add(object.getIntValue("tlp_id"));
            }

            object = new JSONObject();
            object.put("appid",mAppId);
            object.put("tlp_ids",tlp_ids);
            object.put("pos_num",mPosNum);

            object = mHttp.sendPost(url,HttpRequest.generate_request_parm(object,mAppSecret),true);
            boolean success;
            if (success = (object.getIntValue("flag") == 1)){
                object = JSON.parseObject(object.getString("info"));
                success = "y".equals(object.getString("status"));
            }
            if (!success)Logger.e(getMarkErrorMsg("买满送X",object.getString("info")));
        }
    }

    private void up_auxiliary_barcode(final JSONArray datas) throws JSONException{
        if (datas != null && !datas.isEmpty()){
            JSONObject object;
            final String url = mUrl + "/api/goods_set/up_fuzhu_barcode";

            final JSONArray g_m_ids = new JSONArray();
            for (int k = 0,length = datas.size();k < length;k++) {
                object = datas.getJSONObject(k);
                g_m_ids.add(object.getIntValue("g_m_id"));
            }

            object = new JSONObject();
            object.put("appid",mAppId);
            object.put("g_m_ids",g_m_ids);
            object.put("pos_num",mPosNum);

            object = mHttp.sendPost(url,HttpRequest.generate_request_parm(object,mAppSecret),true);
            boolean success;
            if (success = (object.getIntValue("flag") == 1)){
                object = JSON.parseObject(object.getString("info"));
                success = "y".equals(object.getString("status"));
            }
            if (!success)Logger.e(getMarkErrorMsg("辅助条码",object.getString("info")));
        }
    }
    private String getMarkErrorMsg(String mark,String info){
        return String.format(Locale.CHINA,"%s:%s",CustomApplication.self().getString(R.string.mark_info_hint,mark),info);
    }


    void stop(){
        sendMessageAtFrontOfQueue(obtainMessage(MessageID.SYNC_THREAD_QUIT_ID));
    }

    void sync(){
        if (mCurrentNetworkStatusCode == HttpURLConnection.HTTP_OK){
            for (int id : mSyncBasicsDataMessageIds){
                if (!hasMessages(id))obtainMessage(id,0).sendToTarget();
            }
        }
    }
    void stopSync(){
        if (isPause)_continue();//如果已经暂停，则先唤醒线程
        for (int id : mSyncBasicsDataMessageIds){
            removeMessages(id);
        }
        removeMessages(MessageID.SYNC_FINISH_ID);
    }
    void modifyReportProgressStatus(boolean b){
        sendMessageAtFrontOfQueue(obtainMessage(MessageID.MODIFY_REPORT_PROGRESS_ID,b));
    }
    void startNetworkTest(){
        if (!hasMessages(MessageID.NETWORKSTATUS_ID)){
            obtainMessage(MessageID.NETWORKSTATUS_ID).sendToTarget();
            postDelayed(this::startNetworkTest,1000);
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
