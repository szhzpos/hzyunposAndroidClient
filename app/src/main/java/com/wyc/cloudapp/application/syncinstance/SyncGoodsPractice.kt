package com.wyc.cloudapp.application.syncinstance

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.data.room.entity.GoodsPractice
import com.wyc.cloudapp.logger.Logger

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.application.syncinstance
 * @ClassName:      SyncGoodsPractice
 * @Description:    同步做法口味
 * @Author:         wyc
 * @CreateDate:     2021-11-30 15:26
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-11-30 15:26
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class SyncGoodsPractice:AbstractSyncBase("goodsPractice",GoodsPractice.getFieldsName()
, "正在同步口味做法", "/api/goods_set/get_kouwei" ){

    override fun getMarkParam(data: JSONArray): JSONObject {
        var obj: JSONObject
        val codes = JSONArray()
        var k = 0
        val length: Int = data.size
        while (k < length) {
            obj = data.getJSONObject(k)
            codes.add(obj.getString("kw_code"))
            k++
        }

        obj = JSONObject()
        obj["appid"] = CustomApplication.self().appId
        obj["kw_codes"] = codes
        obj["pos_num"] = CustomApplication.self().posNum
        obj[mMarkPathKey] = "/api/goods_set/up_kouwei"
        return obj
    }

    companion object{
        @JvmStatic
        fun sync(){
            SyncGoodsPractice().request()
        }
        @JvmField
        val HEART_BEAT_KEY  = "kouwei"
    }
}