package com.wyc.cloudapp.application.syncinstance

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.data.room.entity.GoodsPractice
import com.wyc.cloudapp.data.room.entity.GoodsPractice.getFieldsName
import com.wyc.cloudapp.data.room.entity.PracticeAssociated

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.application.syncinstance
 * @ClassName:      SyncPracticeAssociated
 * @Description:    同步做法与商品关联
 * @Author:         wyc
 * @CreateDate:     2021-11-30 16:51
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-11-30 16:51
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class SyncPracticeAssociated :AbstractSyncBase("practiceAssociated", PracticeAssociated.getFieldsName()
    , "正在同步做法关联", "/api/goods_set/get_kouwei_relation" ){
    override fun getMarkParam(data: JSONArray): JSONObject {
        var obj: JSONObject
        val codes = JSONArray()
        var k = 0
        val length: Int = data.size
        while (k < length) {
            obj = data.getJSONObject(k)
            codes.add(obj.getString("id"))
            k++
        }

        obj = JSONObject()
        obj["appid"] = CustomApplication.self().appId
        obj["ids"] = codes
        obj["pos_num"] = CustomApplication.self().posNum
        obj[mMarkPathKey] = "/api/goods_set/up_kouwei_relation"
        return obj
    }

    companion object{
        @JvmStatic
        fun sync(){
            SyncPracticeAssociated().request()
        }
        @JvmField
        val HEART_BEAT_KEY  = "kouwei_relation"
    }
}