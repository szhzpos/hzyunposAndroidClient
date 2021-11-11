package com.wyc.cloudapp.application.syncinstance

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication

class SyncStepPromotion:AbstractSyncBase("step_promotion_info", arrayOf("tlpb_id", "tlp_id", "type_detail_id", "status", "way", "promotion_object", "promotion_grade_id", "promotion_type",
        "xnum_one", "promotion_price_one", "xnum_two", "promotion_price_two", "xnum_three", "promotion_price_three", "xnum_four", "promotion_price_four", "xnum_five", "promotion_price_five",
        "stores_id", "start_date", "end_date", "promotion_week", "begin_time", "end_time", "xtype"), "正在同步零售阶梯促销", "/api/promotion/get_promotion_sellstep") {
    override fun getMarkParam(data: JSONArray): JSONObject {
        var obj: JSONObject
        val tlp_ids = JSONArray()
        var k = 0
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
        obj[mMarkPathKey] = "/api/promotion/up_promotion_sellstep"
        
        return obj
    }
    companion object{
        @JvmStatic
        fun sync(){
            CustomApplication.sync(SyncStepPromotion())
        }
        @JvmField
        val HEART_BEAT_KEY  = "time_limit_sellstep"
    }
}