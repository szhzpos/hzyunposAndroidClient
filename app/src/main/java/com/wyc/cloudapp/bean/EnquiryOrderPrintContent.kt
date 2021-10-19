package com.wyc.cloudapp.bean

import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.bean
 * @ClassName:      EnquiryOrderPrintContent
 * @Description:    要货单打印内容
 * @Author:         wyc
 * @CreateDate:     2021-09-03 14:35
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-09-03 14:35
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class EnquiryOrderPrintContent:OrderPrintContentBase() {
    override fun getInStoreLabel(context: MainActivity): String {
        val sz = context.getString(R.string.enquiry_store)
        return sz.substring(0, sz.length - 1)
    }

    override fun getOutStoreLabel(context: MainActivity): String {
        val sz = context.getString(R.string.target_store)
        return sz.substring(0, sz.length - 1)
    }
}