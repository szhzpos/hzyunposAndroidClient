package com.wyc.cloudapp.bean

import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.MainActivity

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.bean
 * @ClassName:      WholesalePrintContext
 * @Description:    批发销售据打印内容
 * @Author:         wyc
 * @CreateDate:     2021-08-23 15:30
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-08-23 15:30
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class WholesaleSoldPrintContent:OrderPrintContentBase() {
    override fun getSupOrCusLabel(context: MainActivity):String{
        return context.getString(R.string.consumer_name_sz)
    }

    override fun getInStoreLabel(context: MainActivity): String {
        return context.getString(R.string.out_store)
    }
}