package com.wyc.cloudapp.application.syncinstance

import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.http.HttpRequest
import java.io.File
class SyncGoods: AbstractSyncBase("barcode_info", SQLiteHelper.getGoodsCols(), "正在同步商品", "/api/goods/get_goods_all") {
    override fun getMarkParam(data: JSONArray): JSONObject {
        val goods_ids = download_goods_img_and_upload_barcode_id(data)
        return JSONObject().fluentPut("appid", CustomApplication.self().appId)
                .fluentPut("goods_ids", goods_ids).fluentPut("barcode_ids", goods_ids)
                .fluentPut("pos_num", CustomApplication.self().posNum).fluentPut(mMarkPathKey,"/api/goods/up_goods")
    }
    @Throws(JSONException::class)
    private fun download_goods_img_and_upload_barcode_id(datas: JSONArray):JSONArray {
        var img_url_info: String
        var img_file_name: String
        val goods_ids = JSONArray()
        var `object`: JSONObject
        var k = 0
        val length = datas.size
        val httpRequest = HttpRequest()
        while (k < length) {
            `object` = datas.getJSONObject(k)
            goods_ids.add(`object`.getIntValue("barcode_id"))
            img_url_info = `object`.getString("img_url")
            if ("" != img_url_info) {
                img_file_name = img_url_info.substring(img_url_info.lastIndexOf("/") + 1)
                val file = File(CustomApplication.getGoodsImgSavePath() + img_file_name)
                if (!file.exists()) {
                    val load_img = httpRequest.getFile(file, img_url_info)
                    if (load_img.getIntValue("flag") == 0) {
                        Logger.e("下载商品图片错误：%s,url:%s",load_img.getString("info"),img_url_info)
                    }
                }
            }
            k++
        }
        return goods_ids
    }
    companion object{
        @JvmStatic
        fun sync(){
            SyncGoods().request()
        }
        @JvmField
        val HEART_BEAT_KEY  =  "goods_barcode"
        @JvmField
        val HEART_BEAT_KEY1  =  "shop_barcode"
    }
}