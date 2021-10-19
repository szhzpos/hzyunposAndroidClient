package com.wyc.cloudapp.bean

import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.bean
 * @ClassName:      PurchaseRefundOrderPrintContent
 * @Description:    采购退货单
 * @Author:         wyc
 * @CreateDate:     2021-10-12 14:28
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-10-12 14:28
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class PurchaseRefundOrderPrintContent:OrderPrintContentBase() {
    override fun getInStoreLabel(context: MainActivity): String {
        return context.getString(R.string.pur_refund_store)
    }
}