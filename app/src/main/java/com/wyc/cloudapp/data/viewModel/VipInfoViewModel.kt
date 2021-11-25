package com.wyc.cloudapp.data.viewModel

import android.app.Activity
import android.util.Log
import androidx.annotation.NonNull
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.TimeCardData
import com.wyc.cloudapp.bean.TimeCardInfo
import com.wyc.cloudapp.bean.VipInfo
import com.wyc.cloudapp.constants.InterfaceURL
import com.wyc.cloudapp.dialog.CustomProgressDialog
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.dialog.vip.VipInfoDialog
import com.wyc.cloudapp.utils.Utils
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.callback.ObjectResult
import java.net.HttpURLConnection

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.data.viewModel
 * @ClassName:      VipInfoViewModel
 * @Description:    会员信息
 * @Author:         wyc
 * @CreateDate:     2021-11-24 13:44
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-11-24 13:44
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class VipInfoViewModel: ViewModelBase() {
    private val currentModel: MutableLiveData<VipInfo> = MutableLiveData()
    fun refresh(context: Activity, c: String?){
        val progressDialog = CustomProgressDialog.showProgress(context, context.getString(R.string.hints_query_data_sz))
        launchWithHandler {
            currentModel.postValue(try {
                VipInfoDialog.searchVip(c).getJSONObject(0).toJavaObject(VipInfo::class.java)
            }catch (e:Exception){
                MyDialog.toastMessage(e.localizedMessage)
                VipInfo()
            })
            progressDialog.dismiss()
        }
    }
    fun getVipInfo(): VipInfo? {
        return currentModel.value
    }
    fun addObserver(@NonNull owner:LifecycleOwner,@NonNull observer:Observer<VipInfo> ){
        if (currentModel.hasObservers()){
            Log.w("VipInfoViewModel","model has existed observer")
        }
        currentModel.observe(owner,observer)
    }
    fun hasObserver():Boolean{
        return currentModel.hasObservers()
    }
}