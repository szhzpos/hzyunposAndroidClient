package com.wyc.cloudapp.application.syncinstance

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication

class SyncFullReduce:AbstractSyncBase("fullreduce_info_new", arrayOf("tlp_id","tlpb_id","title","promotion_type","type_detail_id","promotion_object","promotion_grade_id","cumulation_give","buyfull_money","reduce_money","start_date"
        ,"end_date","promotion_week","begin_time","end_time","status","xtype"), "正在同步满减", "/api/promotion/get_promotion_fullreduce") {
    override fun getMarkParam(data: JSONArray): JSONObject {
        val tlp_ids = JSONArray()
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
        obj[mMarkPathKey] = "/api/promotion/up_promotion_fullreduce"

        return obj
    }
    companion object{
        @JvmStatic
        fun sync(){
            CustomApplication.sync(SyncFullReduce())
        }
        @JvmField
        val HEART_BEAT_KEY  = "time_limit_fullreduce";
    }
}