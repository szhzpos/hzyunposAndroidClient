package com.wyc.cloudapp.bean

import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.application
 * @ClassName:      WholesalePrintContent
 * @Description:    批发订货单打印内容
 * @Author:         wyc
 * @CreateDate:     2021-10-12 14:36
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-10-12 14:36
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class WholesalePrintContent: OrderPrintContentBase() {
    override fun getSupOrCusLabel(context: MainActivity):String{
        return context.getString(R.string.consumer_name_sz)
    }

    override fun getInStoreLabel(context: MainActivity): String {
        return context.getString(R.string.pur_store)
    }
}