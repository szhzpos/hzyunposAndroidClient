package com.wyc.cloudapp.application.syncinstance

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.application
 * @ClassName:      ISync
 * @Description:    数据同步接口
 * @Author:         wyc
 * @CreateDate:     2021-11-08 16:47
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-11-08 16:47
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
interface ISync {
    fun request():Boolean
    fun error():String
    fun showInfo()
}