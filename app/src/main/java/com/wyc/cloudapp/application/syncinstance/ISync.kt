package com.wyc.cloudapp.application.syncinstance

import androidx.annotation.NonNull
import kotlinx.coroutines.CoroutineScope

interface ISync {
    /**
     *请求同步数据
     *@param c 协程作用域 show 是否显示请求进度
     * */
    fun request(c: CoroutineScope? = null, show:Boolean = false)
    companion object{
        @JvmStatic
        fun sync(@NonNull sync:ISync,c: CoroutineScope? = null, show:Boolean = false){
            sync.request(c,show)
        }
    }
}