package com.wyc.cloudapp.data.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.Consumer
import com.wyc.cloudapp.bean.Supplier
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import com.wyc.cloudapp.utils.http.callback.ArrayCallback
import kotlinx.coroutines.*

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
    private val currentModel: MutableLiveData<List<Supplier>>  = MutableLiveData()
    fun getCurrentModel():MutableLiveData<List<Supplier>> {
        val app = CustomApplication.self()
        val `object` = JSONObject()
        `object`["appid"] = app.appId
        `object`["stores_id"] = app.storeId
        netRequestAsync(app.url + "/api/supplier_search/xlist",HttpRequest.generate_request_parm(`object`, app.appSecret),object: ArrayCallback<Supplier>(Supplier::class.java) {
            override fun onSuccessForResult(d: MutableList<Supplier>?, hint: String?) {
                netFinished()
                currentModel.postValue(d)
            }
            override fun onError(msg: String?) {
                netFinished()
                MyDialog.toastMessage(msg)
            }
        })
        return currentModel
    }
}