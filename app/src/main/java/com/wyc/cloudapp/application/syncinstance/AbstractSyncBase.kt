package com.wyc.cloudapp.application.syncinstance

import androidx.annotation.NonNull
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.DeliveryOrderNum
import com.wyc.cloudapp.constants.MessageID
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.data.room.entity.PracticeAssociated
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.Utils
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import kotlinx.coroutines.*
import java.io.Serializable
import java.util.concurrent.atomic.AtomicBoolean

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
          {
    private var mMaxPage = -1
    private var mCountPage = -1
    private var mRemainingPage = 0
    protected val mParamObj = JSONObject()
    protected val mMarkPathKey = "U"
    protected val mError:StringBuilder = StringBuilder()
    init {
        mParamObj["appid"] = CustomApplication.self().appId
        mParamObj["stores_id"] = CustomApplication.self().storeId
        mParamObj["page"] = 0
        mParamObj["limit"] = 500
        mParamObj["pos_num"] = CustomApplication.self().posNum
    }

    /**
     * 保存已下载的数据之前对数据进行预处理
     * @param data 从服务器获取的数据
     * @return true成功 false失败
     * */
    protected open fun deal(@NonNull data: JSONArray):Boolean{
        return true
    }
    /**
    * 获取标记已下载数据的请求参数
     * @param data 需要标记的数据
     * @return 返回参数JSON对象
    * */
    protected open fun getMarkParam(@NonNull data: JSONArray):JSONObject{
        return JSONObject()
    }
    /**
     * 标记成功后再通知数据已更新
     * */
    protected open fun dataChanged(){

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

        data = HttpUtils.sendPost(CustomApplication.self().url + "/api/heartbeat/set_down_status", HttpRequest.generate_request_parma(data, CustomApplication.self().appSecret), true)
        var success: Boolean
        if ((data.getIntValue("flag") == 1).also { success = it }) {
            data = JSON.parseObject(data.getString("info"))
            success = "y" == data.getString("status")
        }
        if (!success) Logger.e(table_name + "标记心跳错误:%s", data.getString("info"))
    }

    companion object{
        private var syncing = AtomicBoolean()
        @JvmStatic
        fun dealHeartBeatUpdate(array: JSONArray) {
            if (!syncing.get() && array.isNotEmpty()) {
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
                            ISync.sync(SyncPayMethod())
                        }
                        SyncFullReduce.HEART_BEAT_KEY -> {
                            ISync.sync(SyncFullReduce())
                        }
                        SyncCashier.HEART_BEAT_KEY -> {
                            ISync.sync(SyncCashier())
                        }
                        SyncBuyNGiveN.HEART_BEAT_KEY -> {
                            ISync.sync(SyncBuyNGiveN())
                        }
                        SyncGP.HEART_BEAT_KEY -> {
                            ISync.sync(SyncGP())
                        }
                        SyncBuyFullGiveN.HEART_BEAT_KEY -> {
                            ISync.sync(SyncGP())
                        }
                        SyncGoods.HEART_BEAT_KEY -> {
                            ISync.sync(SyncGoods())
                        }
                        SyncGoods.HEART_BEAT_KEY1 -> {

                        }
                        SyncSaleman.HEART_BEAT_KEY -> {
                            ISync.sync(SyncSaleman())
                        }
                        SyncStepFullReduce.HEART_BEAT_KEY -> {
                            ISync.sync(SyncStepFullReduce())
                        }
                        SyncGoodsCategory.HEART_BEAT_KEY -> {
                            ISync.sync(SyncGoodsCategory())
                        }
                        SyncAuxiliaryBarcode.HEART_BEAT_KEY -> {
                            ISync.sync(SyncAuxiliaryBarcode())
                        }
                        SyncStepPromotion.HEART_BEAT_KEY -> {
                            ISync.sync(SyncStepPromotion())
                        }
                        SyncPromotion.HEART_BEAT_KEY -> {
                            ISync.sync(SyncPromotion())
                        }
                        SyncGoodsPractice.HEART_BEAT_KEY -> {
                            ISync.sync(SyncGoodsPractice())
                        }
                        SyncPracticeAssociated.HEART_BEAT_KEY -> {
                            ISync.sync(SyncPracticeAssociated())
                        }
                        SyncDeliveryOrder.HEART_BEAT_KEY ->{
                            ISync.sync(SyncDeliveryOrder())
                        }
                    }
                }
            }
        }
        @JvmStatic
        fun syncAllBasics(){
            CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler { _, exception ->
                CustomApplication.showSyncErrorMsg(exception.message)
                exception.printStackTrace()
            }).launch{
                 val job = launch {
                     syncing.set(true)

                     ISync.sync(SyncGoodsCategory(),this,true)
                     ISync.sync(SyncStores(),this,true)
                     ISync.sync(SyncPayMethod(),this,true)
                     ISync.sync(SyncCashier(),this,true)
                     ISync.sync(SyncGP(),this,true)
                     ISync.sync(SyncFullReduce(),this,true)
                     ISync.sync(SyncStepFullReduce(),this,true)
                     ISync.sync(SyncBuyNGiveN(),this,true)
                     ISync.sync(SyncBuyFullGiveN(),this,true)
                     ISync.sync(SyncSaleman(),this,true)
                     ISync.sync(SyncPromotion(),this,true)
                     ISync.sync(SyncStepPromotion(),this,true)
                     ISync.sync(SyncSaleOperator(),this,true)
                     ISync.sync(SyncAuxiliaryBarcode(),this,true)
                     ISync.sync(SyncGoodsPractice(),this,true)
                     ISync.sync(SyncPracticeAssociated(),this,true)
                     ISync.sync(SyncGoods(),this,true)
                }
                job.join()
                syncing.set(false)
                if (!job.isCancelled){
                    CustomApplication.finishSync()
                }
            }
        }
    }

    private tailrec fun asyncRequest(c:CoroutineScope,show:Boolean){
        if (!c.isActive){
            return
        }

        if(show)showInfo()

        val retJson = HttpUtils.sendPost(CustomApplication.self().url + path, HttpRequest.generate_request_parma(mParamObj, CustomApplication.self().appSecret), true)
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
                        when (this) {
                            is SyncStepFullReduce -> {
                                val json = Utils.getNullStringAsEmpty(info_json, "data")
                                data = if (json.startsWith("{")) {
                                    val obj = info_json.getJSONObject("data")
                                    Utils.getNullObjectAsEmptyJsonArray(obj, "fullreduce")
                                } else {
                                    Utils.getNullObjectAsEmptyJsonArray(info_json, "data")
                                }
                                deal(data)
                                markHeart()
                            }
                            is SyncDeliveryOrder -> {
                                val obj = info_json.getJSONObject("data")
                                val num = JSON.parseObject(Utils.getNullOrEmptyStringAsDefault(obj,"order_num","{}"),DeliveryOrderNum::class.java)
                                CustomApplication.sendMessageAtFrontOfQueue(MessageID.DELIVERY_ORDER_NUM_ID,num)
                                return
                            }
                            else -> data = Utils.getNullObjectAsEmptyJsonArray(info_json, "data")
                        }

                        if (mMaxPage == -1){
                            mMaxPage = info_json.getIntValue("max_page")
                            mCountPage = mMaxPage
                        }else
                            mRemainingPage = info_json.getIntValue("max_page")

                        val curPage = mParamObj.getIntValue("page")
                        if (data.isEmpty()) {
                            markHeart()
                        } else {
                            if (deal(data)) {
                                if (Utils.isNotEmpty(table_name)) {
                                    if (SQLiteHelper.execSQLByBatchFromJson(
                                            data,
                                            table_name,
                                            table_cls,
                                            mError,
                                            1
                                        )
                                    ) {
                                        sign(data)
                                        if (mCountPage-- > 0) {
                                            Logger.d(
                                                "current_page:%d,max_page:%d,remaining:%d",
                                                curPage,
                                                mMaxPage,
                                                mRemainingPage
                                            )

                                            return asyncRequest(c,show)
                                        }
                                    }else mError.insert(0,sys_name)
                                }
                            }
                        }
                    }
                }
            }
        }
        if (mError.isNotEmpty()){
            throw Exception(mError.toString())
        }
    }

    override fun request(c:CoroutineScope?, show: Boolean) {
        if (c != null){
            c.launch {
                asyncRequest(this,show)
            }
        }else{
            CoroutineScope(Dispatchers.IO + CoroutineExceptionHandler{_,exception->
                CustomApplication.transFailure()
                Logger.e("%s",exception.localizedMessage)
                exception.printStackTrace()
            }).launch{
                asyncRequest(this,show)
            }
        }
    }

    @Throws(JSONException::class)
    private fun sign(@NonNull data: JSONArray) {
        var obj = getMarkParam(data)
        if (!obj.isEmpty()){
            val url = CustomApplication.self().url + obj.remove(mMarkPathKey)
            obj = HttpUtils.sendPost(url, HttpRequest.generate_request_parma(obj, CustomApplication.self().appSecret), true)
            var success: Boolean
            if ((obj.getIntValue("flag") == 1).also { success = it }) {
                obj = JSON.parseObject(obj.getString("info"))
                success = "y" == obj.getString("status")
            }
            if (success) dataChanged() else Logger.e("标记" + sys_name + "错误:" + obj.getString("info"))
        }
    }

    private fun showInfo() {
        CustomApplication.showMsg(String.format("%s信息(%d%s)",sys_name,((mMaxPage - mRemainingPage).toFloat() / (if (mMaxPage == 0) 1f else mMaxPage.toFloat()) * 100).toInt(),"%"))
    }
}