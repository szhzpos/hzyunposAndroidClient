package com.wyc.cloudapp.data.viewModel

import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.data.viewModel
 * @ClassName:      StringViewModel
 * @Description:    业务单据获取单号视图模型
 * @Author:         wyc
 * @CreateDate:     2021-08-23 18:11
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-08-23 18:11
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class OrderIdViewModel :ViewModelBase() {
    private val currentModel: MutableLiveData<String>  = MutableLiveData()
    fun init(prefix: String):MutableLiveData<String> {
        launchWithHandler {
            request(true,prefix)
        }
        return currentModel
    }

    private fun request(async:Boolean,prefix: String):String{
        val app = CustomApplication.self()
        val parameterObj = JSONObject()
        parameterObj["appid"] = app.getAppId()
        parameterObj["prefix"] = prefix
        val sz_param = HttpRequest.generate_request_parma(parameterObj, app.getAppSecret())
        val retJson = HttpUtils.sendPost(app.getUrl() + "/api/codes/mk_code", sz_param, true)

        if (HttpUtils.checkRequestSuccess(retJson)) {
            try {
                val info = JSON.parseObject(retJson.getString("info"))
                if (HttpUtils.checkBusinessSuccess(info)) {
                    if (async)
                        currentModel.postValue(info.getString("code"))
                    else return info.getString("code")
                } else {
                    MyDialog.toastMessage(info.getString("info"))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
                MyDialog.toastMessage(e.localizedMessage)
            }
        } else {
            MyDialog.toastMessage(app.getString(R.string.query_business_order_id_hint_sz, retJson.getString("info")))
        }
        return ""
    }
    fun syncGetOrderId(prefix: String):String{
        return request(false,prefix)
    }
}