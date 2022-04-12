package com.wyc.cloudapp.application.syncinstance

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication

class SyncCashier:AbstractSyncBase("cashier_info", arrayOf("cas_id","stores_id","stores_name","cas_name","cas_account","cas_pwd","cas_addtime","cas_code","cas_phone","cas_status",
        "min_discount","is_refund","is_give","is_put","pt_user_id","pt_user_cname","remark","authority")
        , "正在同步门店收银员", "/api/scale/get_cashier_info") {


    override fun getMarkParam(data: JSONArray): JSONObject {
        var obj: JSONObject
        val ids = JSONArray()
        var k = 0
        val length: Int = data.size
        while (k < length) {
            obj = data.getJSONObject(k)
            ids.add(obj.getIntValue("cas_id"))
            k++
        }

        obj = JSONObject()
        obj["appid"] = CustomApplication.self().appId
        obj["cas_ids"] = ids
        obj["pos_num"] = CustomApplication.self().posNum
        obj[mMarkPathKey] = "/api/scale/up_cashier"

        return obj
    }

    companion object{
        @JvmStatic
        fun sync(){
            SyncCashier().request()
        }
        @JvmField
        val HEART_BEAT_KEY  = "cashier_info";
    }
}