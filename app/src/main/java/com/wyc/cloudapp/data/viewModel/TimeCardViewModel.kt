package com.wyc.cloudapp.data.viewModel

import android.app.Activity
import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.TimeCardData
import com.wyc.cloudapp.bean.TimeCardInfo
import com.wyc.cloudapp.constants.InterfaceURL
import com.wyc.cloudapp.dialog.CustomProgressDialog
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.utils.Utils
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.callback.ObjectResult
import java.net.HttpURLConnection

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.data.viewModel
 * @ClassName:      TimeCardViewModel
 * @Description:    次卡信息
 * @Author:         wyc
 * @CreateDate:     2021-10-20 14:22
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-10-20 14:22
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class TimeCardViewModel: ViewModelBase() {
    private val currentModel: MutableLiveData<List<TimeCardInfo>> = MutableLiveData()
    fun refresh(context: Activity, c: String?): MutableLiveData<List<TimeCardInfo>> {
        val progressDialog = CustomProgressDialog.showProgress(context, context.getString(R.string.hints_query_data_sz))
        launchWithHandler {
            val app = CustomApplication.self()
            val obj = JSONObject()
            obj["appid"] = app.appId
            obj["channel"] = 1
            if (Utils.isNotEmpty(c)) {
                obj["title"] = c
            }
            netRequest(app.url + InterfaceURL.ONCE_CARD, HttpRequest.generate_request_parma(obj, app.appSecret)).execute().use {
                val code: Int = it.code()
                if (code == HttpURLConnection.HTTP_OK){
                    val data: ObjectResult<TimeCardData>? = parseObject(TimeCardData::class.java, it.body()?.string())
                    data?.let {d ->
                        if (d.isSuccess){
                            currentModel.postValue(d.data.card)
                        }else MyDialog.toastMessage(d.info)
                    }
                }else{
                    MyDialog.toastMessage(it.message())
                }
                progressDialog.dismiss()
                netFinished()
            }
        }
        return currentModel
    }
}