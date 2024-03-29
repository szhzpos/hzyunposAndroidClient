package com.wyc.cloudapp.data.viewModel

import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.Supplier
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.callback.ArrayCallback
import java.net.HttpURLConnection

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.data.viewModel
 * @ClassName:      SupplierViewModel
 * @Description:    供应商视图模型
 * @Author:         wyc
 * @CreateDate:     2021-08-24 9:19
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-08-24 9:19
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class SupplierViewModel:ViewModelBase() {
    private var currentModel: MutableLiveData<List<Supplier>>?  = null
    private var codeModel: MutableLiveData<String>?  = null
    fun getCurrentModel():MutableLiveData<List<Supplier>> {
        if (currentModel == null)currentModel = MutableLiveData()
        val app = CustomApplication.self()
        val `object` = JSONObject()
        `object`["appid"] = app.appId
        `object`["stores_id"] = app.storeId
        netRequestAsync(app.url + "/api/supplier_search/xlist", HttpRequest.generate_request_parma(`object`, app.appSecret), object : ArrayCallback<Supplier>(Supplier::class.java) {
            override fun onSuccessForResult(d: MutableList<Supplier>?, hint: String?) {
                netFinished()
                currentModel!!.postValue(d)
            }

            override fun onError(msg: String?) {
                netFinished()
                MyDialog.toastMessage(msg)
            }
        })
        return currentModel!!
    }
    fun getCodeModel(): MutableLiveData<String>  {
        if (codeModel == null)codeModel = MutableLiveData()
        launchWithHandler {
            val app = CustomApplication.self()
            val `object` = JSONObject()
            `object`["appid"] = app.appId
            `object`["cs_xinzi"] = 1
            netRequest(app.url + "/api/supplier_search/get_code", HttpRequest.generate_request_parma(`object`, app.appSecret)).execute().use {
                val code: Int = it.code()
                if (code == HttpURLConnection.HTTP_OK){
                    parseObject(String::class.java,it.body()?.string())?.let {
                        if (it.isSuccess)
                            codeModel!!.postValue(it.data)
                        else
                            MyDialog.toastMessage(it.info)
                    }
                }else{
                    MyDialog.toastMessage(it.message())
                }
                netFinished()
            }
        }
        return codeModel!!
    }
}