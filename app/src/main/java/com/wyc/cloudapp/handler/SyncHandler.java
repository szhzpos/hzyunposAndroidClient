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
    public SyncHandler(Handler handler){
        this.syncActivityHandler = handler;
        mHttp = new HttpRequest();
    }

    @Override
    public void handleMessage(Message msg){
        if (msg.obj instanceof JSONObject) {
            JSONObject sync_param = (JSONObject) msg.obj;

                JSONObject object = new JSONObject(),cashierInfo,storeInfo,info_json,retJson;
                String table_name = "",sys_name = "", appid = "",appsecret = "",url = "",sz_param = "";
                String[] table_cls = null;
                StringBuilder err = new StringBuilder();

                try{
                    url = sync_param.getString("server_url");
                    appid = sync_param.getString("appId");
                    appsecret = sync_param.getString("appSecret");

                    cashierInfo = new JSONObject(sync_param.getString("cashierInfo"));
                    storeInfo = new JSONObject(sync_param.getString("storeInfo"));
                    switch (msg.what) {
                        case MessageID.SYNC_GOODS_BASE_ID:
                            syncActivityHandler.obtainMessage(SYNC_DIS_INFO_ID,1, 1, "正在同步商品相关信息....").sendToTarget();
                            table_name = "get_bases";

                            url = url + "/api/goods_set/get_bases";
                            object.put("appid",appid);

                            sz_param = HttpRequest.generate_request_parm(object,appsecret);

                            retJson = mHttp.sendPost(url,sz_param,true);

                            Logger.json(retJson.toString());

                            switch (retJson.optInt("flag")){
                                case 0:
                                    this.removeCallbacksAndMessages(null);
                                    syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID,retJson.optString("info")).sendToTarget();
                                    break;
                                case 1:
                                    info_json = new JSONObject(retJson.optString("info"));
                                    switch (info_json.optString("status")){
                                        case "n":
                                            this.removeCallbacksAndMessages(null);
                                            syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID,  "同步商品相关信息错误:" + info_json.optString("info")).sendToTarget();
                                            break;
                                        case "y":
                                            info_json = new JSONObject(info_json.getString("data"));
                                            JSONArray category_jsons,category_inserts = new JSONArray() ,brand_jsons ,spec_jsons ,units_jsons,attr_jsons,yh_mode_jsons;

                                            category_jsons = new JSONArray(info_json.optString("category"));
                                            brand_jsons = new JSONArray(info_json.optString("brand"));
                                            spec_jsons = new JSONArray(info_json.optString("spec"));
                                            units_jsons = new JSONArray(info_json.optString("units"));
                                            attr_jsons = new JSONArray(info_json.optString("attr"));
                                            yh_mode_jsons = new JSONArray(info_json.optString("yh_mode"));

                                            Logger.d("category_json:%s",category_jsons.toString());
                                            Logger.d("brand_json:%s",brand_jsons.toString());
                                            Logger.d("spec_json:%s",spec_jsons.toString());
                                            Logger.d("units_json:%s",units_jsons.toString());
                                            Logger.d("attr_json:%s",attr_jsons.toString());
                                            Logger.d("yh_mode_json:%s",yh_mode_jsons.toString());

                                            //解析商品类别
                                            parse_category_info(category_jsons,category_inserts);

                                            Logger.d("category_inserts:%s",category_inserts.toString());

                                            if (category_inserts.length() != 0) {
                                                if (!SQLiteHelper.execSQLByBatchReplaceJson(category_inserts,"goods_type_m",err)) {
                                                    this.removeCallbacksAndMessages(null);
                                                    syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, "保存商品类别：" + err).sendToTarget();
                                                }
                                            }

                                            if(brand_jsons.length() != 0){
                                                if (!SQLiteHelper.execSQLByBatchReplaceJson(brand_jsons,"goods_brand_m" ,err)) {
                                                    this.removeCallbacksAndMessages(null);
                                                    syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, "保存商品品牌：" + err).sendToTarget();
                                                }
                                            }

                                            if(spec_jsons.length() != 0){
                                                if (!SQLiteHelper.execSQLByBatchReplaceJson(spec_jsons,"goods_attr_m" ,err)) {
                                                    this.removeCallbacksAndMessages(null);
                                                    syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, "保存商品属性：" + err).sendToTarget();
                                                }
                                            }

                                            if(yh_mode_jsons.length() != 0){
                                                if (!SQLiteHelper.execSQLByBatchReplaceJson(yh_mode_jsons,"goods_yh_mode_m" ,err)) {
                                                    this.removeCallbacksAndMessages(null);
                                                    syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, "保存商品优惠方式：" + err).sendToTarget();
                                                }
                                            }

                                            if(units_jsons.length() != 0){
                                                if (!SQLiteHelper.execSQLByBatchReplaceJson(units_jsons,"goods_unit_m" ,err)) {
                                                    this.removeCallbacksAndMessages(null);
                                                    syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, "保存商品单位：" + err).sendToTarget();
                                                }
                                            }

                                            break;
                                    }
                                    break;
                            }
                            return;
                        case MessageID.SYNC_STORES_ID://仓库信息
                            table_name = "shop_stores";
                            sys_name = "正在同步仓库";

                            url = url + "/api/scale/get_stores";
                            object.put("appid",appid);
                            object.put("pt_user_id",cashierInfo.getString("pt_user_id"));
                            break;
                        case MessageID.SYNC_GOODS_ID://商品信息
                            table_name = "barcode_info";
                            sys_name = "正在同步商品";
                            url = url + "/api_v2/goods/get_goods_all";
                            object.put("appid",appid);
                            object.put("pos_num",cashierInfo.getString("pos_num"));
                              break;
                        case MessageID.SYNC_PAY_METHOD_ID://支付方式
                            table_name = "pay_method";
                            sys_name = "正在同步支付方式";
                            url = url + "/api/cashier/get_pm_info";
                            object.put("appid",appid);
                            object.put("stores_id",storeInfo.getString("stores_id"));
                            object.put("pos_num",cashierInfo.getString("pos_num"));
                            break;
                        case MessageID.SYNC_CASHIER_ID://用户信息
                            table_name = "cashier_info";
                            sys_name = "正在同步门店收银员";
                            url = url + "/api_v2/users/xlist";
                            object.put("appid",appid);
                            object.put("stores_id",storeInfo.getString("stores_id"));
                            break;
                    }
                    if (msg.what == MessageID.SYNC_FINISH_ID){
                        syncActivityHandler.obtainMessage(MessageID.SYNC_FINISH_ID).sendToTarget();//同步完成
                    }else{
                        syncActivityHandler.obtainMessage(SYNC_DIS_INFO_ID,sys_name + "信息....").sendToTarget();

                        sz_param = HttpRequest.generate_request_parm(object,appsecret);
                        retJson = mHttp.sendPost(url,sz_param,true);

                        Logger.json(retJson.toString());

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
                                        syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, sys_name.concat("错误：").contains(info_json.optString("info"))).sendToTarget();
                                        break;
                                    case "y":
                                        JSONArray user_list = info_json.getJSONArray("data");
                                        Logger.json(user_list.toString());
                                        if(user_list.length() != 0){
                                            if (!SQLiteHelper.execSQLByBatchReplaceJson(user_list,table_name ,table_cls,err)) {
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
           }else {
            syncActivityHandler.obtainMessage(MessageID.SYNC_ERR_ID, "参数错误！ " + msg.obj).sendToTarget();
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
