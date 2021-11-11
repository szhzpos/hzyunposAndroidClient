package com.wyc.cloudapp.application.syncinstance

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import java.lang.StringBuilder
import java.util.*

/*
* 组合商品
* */
class SyncGP:AbstractSyncBase("", arrayOf(), "正在同步组合商品", "/api/promotion/get_gp_info") {
    override fun deal(data: JSONArray):Boolean {
        return deal_good_group(data)
    }
    @Throws(JSONException::class)
    private fun deal_good_group(data: JSONArray): Boolean {
        val goods_list = JSONArray()
        val gp_ids = JSONArray()
        val tmp_goods = JSONObject()
        var gp_obj: JSONObject
        var k = 0
        val size = data.size
        while (k < size) {
            gp_obj = data.getJSONObject(k)
            gp_ids.add(gp_obj["gp_id"])
            val tmp = gp_obj.remove("goods_list") as JSONArray?
            if (tmp != null) {
                var j = 0
                val length = tmp.size
                while (j < length) {
                    goods_list.add(tmp[j])
                    j++
                }
            }
            k++
        }
        tmp_goods["goods_group"] = data
        tmp_goods["goods_group_info"] = goods_list
        val goods_group_cols = Arrays.asList("mnemonic_code", "gp_id", "gp_code", "gp_title", "gp_price", "status", "addtime", "unit_name", "stores_id", "img_url")
        val goods_group_info_cols = Arrays.asList("xnum", "barcode_id", "gp_id", "_id")
        val code = SQLiteHelper.execSQLByBatchFromJson(tmp_goods, Arrays.asList("goods_group", "goods_group_info"), Arrays.asList(goods_group_cols, goods_group_info_cols), mError, 1)
        if (code && !gp_ids.isEmpty()) {
            up_gp_goods(gp_ids)
        }
        return code
    }
    @Throws(JSONException::class)
    private fun up_gp_goods(datas: JSONArray) {
        val url = CustomApplication.self().url + "/api/promotion/up_gp"
        var `object` = JSONObject()
        `object`["appid"] = CustomApplication.self().appId
        `object`["gp_ids"] = datas
        `object`["pos_num"] = CustomApplication.self().posNum
        `object` = HttpUtils.sendPost(url, HttpRequest.generate_request_parm(`object`, CustomApplication.self().appSecret), true)
        var success: Boolean
        if ((`object`.getIntValue("flag") == 1).also { success = it }) {
            `object` = JSON.parseObject(`object`.getString("info"))
            success = "y" == `object`.getString("status")
        }
        if (!success) Logger.e("标记已获取的组合商品错误:" + `object`.getString("info"))
    }
    companion object{
        @JvmStatic
        fun sync(){
            CustomApplication.sync(SyncGP())
        }
        @JvmField
        val HEART_BEAT_KEY  = "goods_group"
    }
}