package com.wyc.cloudapp.bean

import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.bean
 * @ClassName:      TransferOutInOrder
 * @Description:    配入配出打印内容
 * @Author:         wyc
 * @CreateDate:     2021-08-30 9:49
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-08-30 9:49
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class TransferOutInOrder:OrderPrintContentBase() {
    override fun getInStoreLabel(context: MainActivity): String {
        return context.getString(R.string.in_store_alias)
    }
}