package com.wyc.cloudapp.application.syncinstance

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication

class SyncBuyFullGiveN:AbstractSyncBase("buyfull_give_x", arrayOf("tlp_id", "tlpb_id", "title", "type_detail_id", "promotion_type", "promotion_object", "promotion_grade_id", "cumulation_give", "fullgive_way", "give_way", "item_discount", "buyfull_money", "givex_goods_info", "start_date", "end_date", "promotion_week", "begin_time", "end_time", "status", "xtype"), "正在同步买满送N", "/api/promotion/get_promotion_buyfull_givex") {
    override fun getMarkParam(data: JSONArray): JSONObject {
        val tlp_ids = JSONArray()
        var k = 0
        var param:JSONObject
        val length: Int = data.size
        while (k < length) {
            param = data.getJSONObject(k)
            tlp_ids.add(param.getIntValue("tlp_id"))
            k++
        }

        param = JSONObject()
        param.put("appid", CustomApplication.self().appId)
        param.put("tlp_ids", tlp_ids)
        param.put("pos_num", CustomApplication.self().posNum)
        param.put(mMarkPathKey, "/api/promotion/up_promotion_buyfull_givex")

        return param
    }
    companion object{
        @JvmStatic
        fun sync(){
            CustomApplication.sync(SyncBuyFullGiveN())
        }
        @JvmField
        val HEART_BEAT_KEY  = "time_limit_buyfull_givex"
    }
}