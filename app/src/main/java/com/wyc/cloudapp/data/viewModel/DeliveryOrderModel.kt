package com.wyc.cloudapp.data.viewModel

import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.DeliveryData
import com.wyc.cloudapp.bean.DeliveryOrderInfo
import com.wyc.cloudapp.bean.Supplier
import com.wyc.cloudapp.constants.InterfaceURL
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.callback.ArrayCallback
import com.wyc.cloudapp.utils.http.callback.ObjectCallback


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.data.viewModel
 * @ClassName:      DeliveryOrderModel
 * @Description:    商城配送订单
 * @Author:         wyc
 * @CreateDate:     2022/6/14 17:17
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/6/14 17:17
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class DeliveryOrderModel:ViewModelBase() {
    private val model = MutableLiveData<List<DeliveryOrderInfo>>()
    fun getCurrentModel():MutableLiveData<List<DeliveryOrderInfo>> {
        return model
    }
    fun query(obj:JSONObject){
        val app = CustomApplication.self()

        obj["appid"] = app.appId
        obj["stores_id"] = app.storeId

        Logger.d_json(obj)

        netRequestAsync(app.url + InterfaceURL.DELIVERY_QUERY, HttpRequest.generate_request_parma(obj, app.appSecret), object : ObjectCallback<DeliveryData>(
            DeliveryData::class.java) {
            override fun onSuccessForResult(d: DeliveryData?, hint: String?) {
                netFinished()
                d?.apply {
                    model.postValue(d.order_info)
                }
            }
            override fun onError(msg: String?) {
                netFinished()
                MyDialog.toastMessage(msg)
            }
        })
    }
}