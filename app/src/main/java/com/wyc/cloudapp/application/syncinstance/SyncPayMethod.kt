package com.wyc.cloudapp.application.syncinstance

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.http.HttpRequest
import java.io.File

class SyncPayMethod:AbstractSyncBase("pay_method", arrayOf("pay_method_id", "name", "status", "remark", "is_check", "shortcut_key", "sort", "xtype", "pay_img", "master_img",
        "is_show_client", "is_cardno", "is_scan", "wr_btn_img", "unified_pay_order", "unified_pay_query", "rule", "is_open", "is_enable", "is_moling", "support"), "正在同步支付方式", "/api/cashier/get_pm_info") {
    override fun getMarkParam(data: JSONArray): JSONObject {
        val obj = JSONObject()
        val ids = download_pay_method_img_and_upload_pay_method(data)
        if (!ids.isEmpty()){
            obj["appid"] = CustomApplication.self().appId
            obj["pay_method_ids"] = ids
            obj["pos_num"] = CustomApplication.self().posNum
            obj[mMarkPathKey] = "/api/cashier/up_pm"
        }
        return obj
    }

    @Throws(JSONException::class)
    private fun download_pay_method_img_and_upload_pay_method(datas: JSONArray):JSONArray {
        var img_url_info: String
        var img_file_name: String
        var obj: JSONObject
        val pay_method_ids = JSONArray()
        var k = 0
        val length = datas.size
        val httpRequest = HttpRequest()
        while (k < length) {
            obj = datas.getJSONObject(k)
            pay_method_ids.add(obj.getIntValue("pay_method_id"))
            img_url_info = obj.getString("pay_img")
            if (img_url_info != "") {
                img_file_name = img_url_info.substring(img_url_info.lastIndexOf("/") + 1)
                val file = File(CustomApplication.getGoodsImgSavePath() + img_file_name)
                if (!file.exists()) {
                    val load_img = httpRequest.getFile(file, img_url_info)
                    if (load_img.getIntValue("flag") == 0) {
                        Logger.e("下载支付方式图片错误：%s,url:%s",load_img.getString("info"),img_file_name)
                    }
                }
            }
            k++
        }
        return pay_method_ids
    }
    companion object{
        @JvmStatic
        fun sync(){
            SyncPayMethod().request()
        }
        @JvmField
        val  HEART_BEAT_KEY  = "pay_method"

    }
}