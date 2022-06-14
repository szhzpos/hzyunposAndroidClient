package com.wyc.cloudapp.application.syncinstance

import com.wyc.cloudapp.constants.InterfaceURL


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.application.syncinstance
 * @ClassName:      SyncDeliveryOrder
 * @Description:    同步商城订单
 * @Author:         wyc
 * @CreateDate:     2022/6/14 10:37
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/6/14 10:37
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class SyncDeliveryOrder : AbstractSyncBase("deliveryOrder",arrayOf(),"正在同步商城订单",InterfaceURL.DELIVERY_QUERY) {

    companion object{
        @JvmField
        val HEART_BEAT_KEY  = "store_order"
    }
}