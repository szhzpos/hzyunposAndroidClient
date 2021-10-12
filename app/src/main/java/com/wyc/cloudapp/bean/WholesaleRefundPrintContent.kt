package com.wyc.cloudapp.bean

import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.MainActivity

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.bean
 * @ClassName:      WholesaleRefundPrintContent
 * @Description:    批发退货单打印内容
 * @Author:         wyc
 * @CreateDate:     2021-10-12 14:39
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-10-12 14:39
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class WholesaleRefundPrintContent: OrderPrintContentBase() {
    override fun getSupOrCusLabel(context: MainActivity):String{
        return context.getString(R.string.consumer_name_sz)
    }

    override fun getInStoreLabel(context: MainActivity): String {
        return context.getString(R.string.pur_refund_store)
    }
}