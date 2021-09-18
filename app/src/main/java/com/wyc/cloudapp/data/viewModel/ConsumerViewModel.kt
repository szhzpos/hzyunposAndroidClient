package com.wyc.cloudapp.data.viewModel

import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.TypeReference
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.Consumer
import com.wyc.cloudapp.bean.Supplier
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import com.wyc.cloudapp.utils.http.callback.ArrayResult
import kotlinx.coroutines.*
import java.io.IOException
import java.net.HttpURLConnection

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.data
 * @ClassName:      ConsumerViewModel
 * @Description:    业务单据客户视图模型
 * @Author:         wyc
 * @CreateDate:     2021-08-23 16:25
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-08-23 16:25
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class ConsumerViewModel:ViewModelBase() {
    private val currentModel: MutableLiveData<List<Consumer>>  = MutableLiveData()
    fun getCurrentModel(): MutableLiveData<List<Consumer>>  {
        launchWithHandler {
            val app = CustomApplication.self()
            val `object` = JSONObject()
            `object`["appid"] = app.appId
            `object`["stores_id"] = app.storeId
            netRequest(app.url + "/api/supplier_search/customer_xlist", HttpRequest.generate_request_parm(`object`, app.appSecret)).execute().use {
                val code: Int = it.code()
                if (code == HttpURLConnection.HTTP_OK){
                    val data:ArrayResult<Consumer> = parseArray(Consumer::class.java,it.body()?.string())
                    if (data.isSuccess)
                        currentModel.postValue(data.data)
                    else
                        MyDialog.ToastMessageInMainThread(data.info)
                }else{
                    MyDialog.ToastMessageInMainThread(it.message())
                }
                netFinished()
            }
        }
        return currentModel
    }
}