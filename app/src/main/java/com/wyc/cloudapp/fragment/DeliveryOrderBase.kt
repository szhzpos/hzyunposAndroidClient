package com.wyc.cloudapp.fragment

import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.fragment
 * @ClassName:      DeliveryOrderBase
 * @Description:    全部配送订单
 * @Author:         wyc
 * @CreateDate:     2022/6/13 17:38
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/6/13 17:38
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

open class DeliveryOrderBase : AbstractBaseFragment(){
    override fun viewCreated() {

    }

    override fun getTitle(): String {
        return CustomApplication.self().getString(R.string.all_order)
    }
}