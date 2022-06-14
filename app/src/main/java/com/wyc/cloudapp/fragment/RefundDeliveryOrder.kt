package com.wyc.cloudapp.fragment

import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.fragment
 * @ClassName:      RefundDeliveryOrder
 * @Description:    配送申请退货
 * @Author:         wyc
 * @CreateDate:     2022/6/13 17:55
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/6/13 17:55
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class RefundDeliveryOrder : DeliveryOrderBase() {

    override fun getTitle(): String {
        return CustomApplication.self().getString(R.string.return_request_order)
    }
    override fun getNumber(): Int {
        return getOrderNum().refundOrder
    }
}