package com.wyc.cloudapp.application.syncinstance

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication

class SyncSaleman:AbstractSyncBase("sales_info", arrayOf("sc_id", "sc_name", "sc_phone", "stores_id", "tc_mode", "is_tc", "tc_rate", "sc_status", "appids", "sc_addtime"), "正在同步营业员", "/api/cashier/get_sc_info") {
    override fun getMarkParam(data: JSONArray): JSONObject {
        var obj: JSONObject
        val sc_ids = JSONArray()
        var k = 0
        val length: Int = data.size
        while (k < length) {
            obj = data.getJSONObject(k)
            sc_ids.add(obj.getIntValue("sc_id"))
            k++
        }

        obj = JSONObject()
        obj["appid"] = CustomApplication.self().appId
        obj["sc_ids"] = sc_ids
        obj["pos_num"] = CustomApplication.self().posNum
        obj[mMarkPathKey] = "/api/cashier/up_sc"

        return obj
    }

    companion object{
        @JvmField
        val HEART_BEAT_KEY  = "sales_clerk"
    }
}