package com.wyc.cloudapp.fragment

import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.fragment
 * @ClassName:      商城配送新订单
 * @Description:    作用描述
 * @Author:         wyc
 * @CreateDate:     2022/6/13 17:33
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/6/13 17:33
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class NewDeliveryOrder : DeliveryOrderBase() {
    override fun viewCreated() {

    }

    override fun getTitle(): String {
        return CustomApplication.self().getString(R.string.new_order)
    }
}