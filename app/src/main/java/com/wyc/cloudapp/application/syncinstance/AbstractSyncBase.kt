package com.wyc.cloudapp.application.syncinstance

import androidx.annotation.NonNull
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.constants.MessageID
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.Utils
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import kotlinx.coroutines.*
import java.io.Serializable
import java.util.concurrent.atomic.AtomicInteger

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.application
 * @ClassName:      AbstractSyncBase
 * @Description:    同步抽象父类
 * @Author:         wyc
 * @CreateDate:     2021-11-08 16:49
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-11-08 16:49
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
abstract class AbstractSyncBase(private val table_name: String, private val table_cls: Array<String>, private val sys_name: String, private val path: String):Serializable,ISync
        ,CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private var mMaxPage = -1
    protected val mParamObj = JSONObject()
    protected val mMarkPathKey = "U"
    protected val mError:StringBuilder = StringBuilder()
    init {
        mParamObj["appid"] = CustomApplication.self().appId
        mParamObj["stores_id"] = CustomApplication.self().storeId
        mParamObj["page"] = null
        mParamObj["limit"] = 500
        mParamObj["pos_num"] = CustomApplication.self().posNum
    }

    //用于保存数据之前处理数据
    protected open fun deal(@NonNull data: JSONArray):Boolean{
        return true
    }
    //获取保存数据之后回传服务器的参数
    protected open fun getMarkParam(@NonNull data: JSONArray):JSONObject{
        return JSONObject()
    }

    protected fun removePosNumForParam(){
        mParamObj.remove("pos_num")
    }
    private fun markHeart() {
        var data = JSONObject()
        data["appid"] = CustomApplication.self().appId
        data["pos_num"] = CustomApplication.self().posNum
        data["cas_id"] = CustomApplication.self().cashierId
        try {
            data["table_name"] = this::class.java.getField("HEART_BEAT_KEY").get(null) as String
        }catch (e:NoSuchFieldException ){
            e.printStackTrace()
        }
        data["status"] = 1

        data = HttpUtils.sendPost(CustomApplication.self().url + "/api/heartbeat/set_down_status", HttpRequest.generate_request_parm(data, CustomApplication.self().appSecret), true)
        var success: Boolean
        if ((data.getIntValue("flag") == 1).also { success = it }) {
            data = JSON.parseObject(data.getString("info"))
            success = "y" == data.getString("status")
        }
        if (!success) Logger.e(table_name + "标记心跳错误:%s", data.getString("info"))
    }

    companion object{
        private val mTasks:AtomicInteger = AtomicInteger()
        @JvmStatic
        private fun addTask(){
            Logger.d("inTask:%d", mTasks.incrementAndGet())
        }
        @JvmStatic
        private fun delTask(){
            Logger.d("outTask:%d", mTasks.decrementAndGet())
        }
        /**
        *
        * */
        @JvmStatic
        private fun waitTaskFinish():Boolean{
            val c = mTasks.get()
            if (c == 0){
                if (CustomApplication.self().isReportProgress){
                    CustomApplication.self().finishSync()
                }
                return true
            }
            Logger.d("task:%d", c)
            return false;
        }
        @JvmStatic
        fun dealHeartBeatUpdate(array: JSONArray) {
            if (waitTaskFinish() && array.isNotEmpty()) {
                val `object` = array.getJSONObject(0)
                val keys: Iterator<String> = `object`.innerMap.keys.iterator()
                var value: Int
                var key: String
                while (keys.hasNext()) {
                    key = keys.next()
                    value = `object`.getIntValue(key)
                    if (value == 1) continue
                    when (key){
                        SyncPayMethod.HEART_BEAT_KEY -> {
                            SyncPayMethod.sync()
                        }
                        SyncFullReduce.HEART_BEAT_KEY -> {
                            SyncFullReduce.sync()
                        }
                        SyncCashier.HEART_BEAT_KEY -> {
                            SyncCashier.sync()
                        }
                        SyncBuyNGiveN.HEART_BEAT_KEY -> {
                            SyncBuyNGiveN.sync()
                        }
                        SyncGP.HEART_BEAT_KEY -> {
                            SyncGP.sync()
                        }
                        SyncBuyFullGiveN.HEART_BEAT_KEY -> {
                            SyncGP.sync()
                        }
                        SyncGoods.HEART_BEAT_KEY -> {
                            SyncGoods.sync()
                        }
                        SyncGoods.HEART_BEAT_KEY1 -> {

                        }
                        SyncSaleman.HEART_BEAT_KEY -> {
                            SyncSaleman.sync()
                        }
                        SyncStepFullReduce.HEART_BEAT_KEY -> {
                            SyncStepFullReduce.sync()
                        }
                        SyncGoodsCategory.HEART_BEAT_KEY -> {
                            SyncGoodsCategory.sync()
                        }
                        SyncAuxiliaryBarcode.HEART_BEAT_KEY -> {
                            SyncAuxiliaryBarcode.sync()
                        }
                        SyncStepPromotion.HEART_BEAT_KEY -> {
                            SyncStepPromotion.sync()
                        }
                        SyncPromotion.HEART_BEAT_KEY -> {
                            SyncPromotion.sync()
                        }
                    }
                }
            }
        }
    }

    private tailrec fun asyncRequest():Boolean{
        if (CustomApplication.self().isReportProgress)showInfo()
        addTask()

        val retJson = HttpUtils.sendPost(CustomApplication.self().url + path, HttpRequest.generate_request_parm(mParamObj, CustomApplication.self().appSecret), true)
        when (retJson.getIntValue("flag")) {
            0 -> {
                mError.append(sys_name).append("错误:").append(retJson.getString("info"))
            }
            1 -> {
                val info_json = JSON.parseObject(retJson.getString("info"))
                when (info_json.getString("status")) {
                    "n" -> {
                        mError.append(sys_name).append("错误:").append(info_json.getString("info"))
                    }
                    "y" -> {
                        val data: JSONArray
                        if (this is SyncStepFullReduce) {
                            val json = Utils.getNullStringAsEmpty(info_json, "data")
                            if (json.startsWith("{")) {
                                val obj = info_json.getJSONObject("data")
                                data = Utils.getNullObjectAsEmptyJsonArray(obj, "fullreduce")
                            } else {
                                data = Utils.getNullObjectAsEmptyJsonArray(info_json, "data")
                            }
                            deal(data)
                            markHeart()
                        } else
                            data = Utils.getNullObjectAsEmptyJsonArray(info_json, "data")

                        if (mMaxPage == -1) mMaxPage = info_json.getIntValue("max_page")
                        val curPage = mParamObj.getIntValue("page")
                        if (data.isEmpty()) {
                            markHeart()
                        } else {
                            if (deal(data)) {
                                //表名为空数据已在deal处理
                                if (Utils.isNotEmpty(table_name)) {
                                    Logger.d_json(data)
                                    if (SQLiteHelper.execSQLByBatchFromJson(data, table_name, table_cls, mError, 1)) {
                                        sign(data)
                                        if (mMaxPage-- > 0) {
                                            Logger.d("current_page:%d,max_page:%d", curPage, mMaxPage)
                                            mParamObj["page"] = 0

                                            delTask()
                                            return asyncRequest()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        delTask()
        return mError.isEmpty()
    }

    /*
    * SyncGoods 在心跳线程处理
    * */
    override  fun request():Boolean {
        if (this is SyncGoods){
            return asyncRequest()
        }else {
            launch {
                val a = async { asyncRequest() }
                a.await()
                if (mError.isNotEmpty()){
                    cancel()
                    if (CustomApplication.self().isReportProgress) {
                        CustomApplication.self().finishSync()
                        CustomApplication.sendMessage(MessageID.SYNC_ERR_ID,mError.toString())
                    } else {
                        CustomApplication.sendMessage(MessageID.TRANSFERSTATUS_ID, false)
                        Logger.e("%s",mError)
                    }
                }
            }
        }
        return true
    }

    @Throws(JSONException::class)
    private fun sign(@NonNull data: JSONArray) {
        var obj = getMarkParam(data)
        if (!obj.isEmpty()){
            val url = CustomApplication.self().url + obj.getString(mMarkPathKey)
            obj = HttpUtils.sendPost(url, HttpRequest.generate_request_parm(obj, CustomApplication.self().appSecret), true)
            var success: Boolean
            if ((obj.getIntValue("flag") == 1).also { success = it }) {
                obj = JSON.parseObject(obj.getString("info"))
                success = "y" == obj.getString("status")
            }
            if (!success) Logger.e("标记" + sys_name + "错误:" + obj.getString("info"))
        }
    }

    override fun error(): String {
        return mError.toString()
    }

    override fun showInfo() {
        CustomApplication.sendMessage(MessageID.SYNC_DIS_INFO_ID, sys_name + "信息....")
    }
}