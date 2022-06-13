package com.wyc.cloudapp.fragment

import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.fragment
 * @ClassName:      CompleteDeliveryOrder
 * @Description:    已完成配送订单
 * @Author:         wyc
 * @CreateDate:     2022/6/13 17:47
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/6/13 17:47
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class CompleteDeliveryOrder : DeliveryOrderBase() {

    override fun getTitle(): String {
        return CustomApplication.self().getString(R.string.complete_order)
    }
}