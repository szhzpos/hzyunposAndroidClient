package com.wyc.cloudapp.application.syncinstance

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication

class SyncGoodsCategory: AbstractSyncBase("shop_category", arrayOf("category_id", "category_code", "name", "parent_id", "depth", "path", "status", "sort")
        , "正在同步商品类别", "/api/scale/get_category_info") {

    override fun getMarkParam(data: JSONArray): JSONObject {
        var obj: JSONObject
        val category_ids = JSONArray()
        var k = 0
        val length: Int = data.size
        while (k < length) {
            obj = data.getJSONObject(k)
            category_ids.add(obj.getIntValue("category_id"))
            k++
        }

        obj = JSONObject()
        obj["appid"] = CustomApplication.self().appId
        obj["category_ids"] = category_ids
        obj["pos_num"] = CustomApplication.self().posNum
        obj[mMarkPathKey] = "/api/scale/up_category"
        return obj
    }
    companion object{
        @JvmStatic
        fun sync(){
            CustomApplication.sync(SyncGoodsCategory())
        }
        @JvmField
        val HEART_BEAT_KEY  =  "shop_category"
    }
}