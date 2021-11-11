package com.wyc.cloudapp.application.syncinstance

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication

class SyncBuyNGiveN:AbstractSyncBase("buy_x_give_x", arrayOf("tlp_id", "tlpb_id", "promotion_type", "promotion_object", "promotion_grade_id", "cumulation_give", "xnum_buy", "xnum_give", "markup_price", "barcode_id", "barcode_id_give", "start_date", "end_date", "promotion_week", "begin_time", "end_time", "status", "xtype"), "正在同步买N送N", "/api/promotion/get_promotion_buyx_givex") {
    override fun getMarkParam(data: JSONArray): JSONObject {
        val tlp_ids: JSONArray = JSONArray()
        var k = 0
        var obj:JSONObject
        val length: Int = data.size
        while (k < length) {
            obj = data.getJSONObject(k)
            tlp_ids.add(obj.getIntValue("tlp_id"))
            k++
        }
        obj = JSONObject()
        obj["appid"] = CustomApplication.self().appId
        obj["tlp_ids"] = tlp_ids
        obj["pos_num"] = CustomApplication.self().posNum
        obj[mMarkPathKey] = "/api/promotion/up_promotion_buyx_givex"

        return obj
    }
    companion object{
        @JvmStatic
        fun sync(){
            CustomApplication.sync(SyncBuyNGiveN())
        }
        @JvmField
        val HEART_BEAT_KEY  = "time_limit_buyx_givex"
    }
}