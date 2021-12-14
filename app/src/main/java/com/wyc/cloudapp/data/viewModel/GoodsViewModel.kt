package com.wyc.cloudapp.data.viewModel

import androidx.lifecycle.MutableLiveData
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONException
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.http.HttpUtils

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.data.viewModel
 * @ClassName:      GoodsViewModel
 * @Description:    商品加载
 * @Author:         wyc
 * @CreateDate:     2021-12-13 16:34
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-12-13 16:34
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class GoodsViewModel:ViewModelBase() {
    private val currentModel: MutableLiveData<JSONArray> = MutableLiveData()
    fun refresh(sql:String){
        launchWithHandler {
            currentModel.postValue(load(sql))
        }
    }
    companion object{
        @JvmStatic
        fun load(sql:String):JSONArray?{
            val err = StringBuilder()
            val arrays = SQLiteHelper.getListToJson(sql, 0, 0, false, err)
            if (arrays == null) {
                MyDialog.ToastMessage("加载商品错误：$err", null)
            }
            return arrays
        }
    }
    fun init():MutableLiveData<JSONArray>{
       return currentModel
    }
}