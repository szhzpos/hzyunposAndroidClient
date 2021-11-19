package com.wyc.cloudapp.application.syncinstance

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication

class SyncAuxiliaryBarcode:AbstractSyncBase("auxiliary_barcode_info", arrayOf("id", "g_m_id", "barcode_id", "fuzhu_barcode", "status"), "正在辅助条码", "/api/goods_set/get_fuzhu_barcode") {

    override fun getMarkParam(data: JSONArray): JSONObject {
        var obj: JSONObject
        val g_m_ids = JSONArray()
        val ids = JSONArray()
        var k = 0
        val length: Int = data.size
        while (k < length) {
            obj = data.getJSONObject(k)
            g_m_ids.add(obj.getIntValue("g_m_id"))
            ids.add(obj.getIntValue("id"))
            k++
        }

        obj = JSONObject()
        obj["appid"] = CustomApplication.self().appId
        obj["g_m_ids"] = g_m_ids
        obj["ids"] = ids
        obj["pos_num"] = CustomApplication.self().posNum
        obj[mMarkPathKey] = "/api/goods_set/up_fuzhu_barcode"

        return obj
    }
    companion object{
        @JvmStatic
        fun sync(){
            SyncAuxiliaryBarcode().request()
        }
        @JvmField
        val HEART_BEAT_KEY  = "fuzhu_barcode"
    }
}