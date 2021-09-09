package com.wyc.cloudapp.data.viewModel

import androidx.lifecycle.ViewModel
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.TypeReference
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.http.HttpUtils
import com.wyc.cloudapp.utils.http.callback.ArrayResult
import com.wyc.cloudapp.utils.http.callback.ObjectResult
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.Callback
import kotlin.coroutines.*

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.data.viewModel
 * @ClassName:      ViewModelBase
 * @Description:    视图模型基类，实现协程
 * @Author:         wyc
 * @CreateDate:     2021-08-24 9:59
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-08-24 9:59
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
open class ViewModelBase:ViewModel(),CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private var call:Call? = null
    @Volatile
    private var  isFinish:Boolean = false
    protected fun launchWithHandler(block: suspend CoroutineScope.() -> Unit){
        block.createCoroutine(this,object :Continuation<Unit>{
            override val context: CoroutineContext
                get() = coroutineContext

            override fun resumeWith(result: Result<Unit>) {
                result.onFailure {
                    it.printStackTrace()
                    MyDialog.ToastMessageInMainThread("${this@ViewModelBase::class.simpleName}：${it.message}")
                }
                close()
            }
        }).resume(Unit)
    }

    private fun close(){
        if (null != call){
            if (!isFinish){
                call!!.cancel()
            }
            call = null
        }
    }
    final override fun onCleared() {
        close()
        cancel()
    }

    protected fun netRequestAsync(url:String,param:String,back:Callback){
        call = HttpUtils.sendAsyncPost(url,param)
        call!!.enqueue(back)
    }
    protected fun netRequest(url:String,param:String):Call{
        call = HttpUtils.sendAsyncPost(url,param)
        return call!!
    }
    protected fun netFinished(){
        isFinish = true
    }
    protected fun <T> parseArray(clazz: Class<T>,value:String?):ArrayResult<T>{
        return JSONObject.parseObject(value, object : TypeReference<ArrayResult<T>>(clazz) {}.type)
    }
    protected fun <T> parseObject(clazz: Class<T>,value:String?):ObjectResult<T>{
        return JSONObject.parseObject(value, object : TypeReference<ObjectResult<T>>(clazz) {}.type)
    }
    protected fun finalize(){
        Logger.d("%s has finalized",javaClass.simpleName)
    }
}