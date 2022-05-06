package com.wyc.cloudapp.data.viewModel

import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.GoodsStockInfo
import com.wyc.cloudapp.bean.Supplier
import com.wyc.cloudapp.constants.InterfaceURL
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.callback.ArrayCallback
import com.wyc.cloudapp.utils.http.callback.Result
import com.wyc.cloudapp.utils.http.callback.TypeCallback
import java.io.Serializable

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.data.viewModel
 * @ClassName:      GoodsStockViewModel
 * @Description:    商品库存查询
 * @Author:         wyc
 * @CreateDate:     2022-3-2 14:45
 * @UpdateUser:     更新者
 * @UpdateDate:     2022-3-2 14:45
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class GoodsStockViewModel: ViewModelBase() {
    private val currentModel:MutableLiveData<StockResult>  = MutableLiveData()
    fun getCurrentModel():MutableLiveData<StockResult>{
        return currentModel
    }

    fun query(cond: Condition){

        val app = CustomApplication.self()
        val `object` = JSONObject()
        `object`["appid"] = app.appId
        `object`["stores_id"] = app.storeId
        `object`["offset"] = cond.offset * cond.limit
        `object`["limit"] = cond.limit

        when(cond.type){
            QueryType.ONLYCODE ->{
                `object`["only_coding"] = cond.content
            }
            QueryType.NAME ->{
                `object`["goods_title"] = cond.content
            }
            QueryType.BARCODE -> {
             `object`["barcode"] = cond.content
            }
            else -> {}
        }
        netRequestAsync(app.url + InterfaceURL.STOCK_QUERY, HttpRequest.generate_request_parma(`object`, app.appSecret),object : TypeCallback<StockResult>(StockResult::class.java){
            override fun onError(msg: String?) {
                netFinished()
                MyDialog.toastMessage(msg)
            }

            override fun onSuccess(data: StockResult?) {
                netFinished()
                data?.apply {
                    currentModel.postValue(this)
                }
            }

        })
    }
    class StockResult:Result(), Serializable{
        var total = 0.0
        var total_stock_num = 0.0
        var total_stock_money = 0.0
        var data:MutableList<GoodsStockInfo>  = mutableListOf()

        override fun toString(): String {
            return "StockResult(total=$total, total_stock_num=$total_stock_num, total_stock_money=$total_stock_money, data=$data)"
        }


    }

    class Condition(val content:String, var offset:Int = 0, var type:QueryType = QueryType.NONE,val limit: Int = 50)
    enum class QueryType{
        BARCODE,NAME,ONLYCODE,NONE
    }
}