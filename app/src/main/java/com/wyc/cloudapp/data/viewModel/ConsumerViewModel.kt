package com.wyc.cloudapp.data.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.Consumer
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import kotlinx.coroutines.*

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
class ConsumerViewModel:ViewModel() {
    val currentModel: LiveDataBase<List<Consumer>>  = LiveDataBase()
    init {
        currentModel.init {
            val app = CustomApplication.self()
            val httpRequest = HttpRequest()
            val `object` = JSONObject()
            `object`["appid"] = app.appId
            `object`["stores_id"] = app.storeId
            val sz_param = HttpRequest.generate_request_parm(`object`, app.appSecret)
            val retJson = httpRequest.sendPost(app.getUrl() + "/api/supplier_search/customer_xlist", sz_param, true)
            if (HttpUtils.checkRequestSuccess(retJson)) {
                val info_obj = JSONObject.parseObject(retJson.getString("info"))
                if (HttpUtils.checkBusinessSuccess(info_obj)) {
                    currentModel.postValue(info_obj.getJSONArray("data")?.toJavaList(Consumer::class.java))
                } else {
                    MyDialog.ToastMessageInMainThread("查询客户信息错误:" + info_obj.getString("info"))
                }
            }
        }
    }
}