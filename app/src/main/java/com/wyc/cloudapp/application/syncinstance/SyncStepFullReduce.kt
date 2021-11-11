package com.wyc.cloudapp.application.syncinstance

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.data.SQLiteHelper

class SyncStepFullReduce :AbstractSyncBase("fullreduce_info", arrayOf("full_id", "title", "modes", "fold", "rule", "start_time", "end_time", "starttime", "endtime"), "正在同步阶梯满减", "/api/promotion/fullreduce_info"){
    init {
        mParamObj["type"] = 1
        removePosNumForParam()
    }

    override fun deal(data: JSONArray): Boolean {
        return SQLiteHelper.execDelete("fullreduce_info", null, null, mError) >= 0
    }

    companion object{
        @JvmStatic
        fun sync(){
            CustomApplication.sync(SyncStepFullReduce())
        }
        @JvmField
        val HEART_BEAT_KEY  = "full_reduce";
    }
}