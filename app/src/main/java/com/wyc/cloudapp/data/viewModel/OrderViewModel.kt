package com.wyc.cloudapp.data.viewModel

import androidx.lifecycle.ViewModel
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.mobile.business.AbstractMobileAddOrderActivity
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.dialog.CustomProgressDialog
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.data.viewModel
 * @ClassName:      OrderViewModel
 * @Description:    加载业务单据
 * @Author:         wyc
 * @CreateDate:     2021-08-23 18:42
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-08-23 18:42
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class OrderViewModel:ViewModel() {
    private val currentModel: LiveDataBase<JSONObject>  = LiveDataBase()
    fun getCurrentModel(url:String,param:String):LiveDataBase<JSONObject>{
        return currentModel.init {
            val retJson = HttpUtils.sendPost(url,param, true)
            if (HttpUtils.checkRequestSuccess(retJson)) {
                try {
                    val info = JSONObject.parseObject(retJson.getString("info"))
                    if (HttpUtils.checkBusinessSuccess(info)) {
                        currentModel.postValue(info.getJSONObject("data"))
                    } else throw JSONException(info.getString("info"))
                } catch (e: JSONException) {
                    e.printStackTrace()
                    MyDialog.ToastMessageInMainThread(e.message)
                }
            }
        }
    }
}