package com.wyc.cloudapp.fragment

import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.fragment
 * @ClassName:      DispatchingOrder
 * @Description:    商城配送中订单
 * @Author:         wyc
 * @CreateDate:     2022/6/13 17:45
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/6/13 17:45
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class DispatchingOrder : DeliveryOrderBase() {

    override fun getTitle(): String {
        return CustomApplication.self().getString(R.string.dispatching_order)
    }
}