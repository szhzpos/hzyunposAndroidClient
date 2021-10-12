package com.wyc.cloudapp.bean

import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.MainActivity

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.bean
 * @ClassName:      OtherWarehouseOrderPrintContent
 * @Description:    其他出入库打印内容
 * @Author:         wyc
 * @CreateDate:     2021-10-12 15:05
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-10-12 15:05
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class OtherWarehouseOrderPrintContent:OrderPrintContentBase() {
    override fun getInStoreLabel(context: MainActivity): String {
        return context.getString(R.string.o_store)
    }
}