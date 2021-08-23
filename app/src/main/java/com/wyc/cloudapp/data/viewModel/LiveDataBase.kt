package com.wyc.cloudapp.data.viewModel

import androidx.lifecycle.MutableLiveData
import com.wyc.cloudapp.logger.Logger
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.data.viewModel
 * @ClassName:      LiveDataBase
 * @Description:    实现CoroutineScope，使用协程加载数据
 * @Author:         wyc
 * @CreateDate:     2021-08-23 17:31
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-08-23 17:31
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class LiveDataBase<T>(override val coroutineContext: CoroutineContext = Dispatchers.IO) : MutableLiveData<T>(), CoroutineScope {
    private var mJob: Job? = null
    override fun onInactive() {
        mJob?.cancel()
    }
    fun init(block: suspend CoroutineScope.()->Unit):LiveDataBase<T>{
        mJob = launch { block.invoke(this) }
        return this
    }
    protected fun finalize(){
        Logger.d("%s has finalized",javaClass.simpleName)
    }
}