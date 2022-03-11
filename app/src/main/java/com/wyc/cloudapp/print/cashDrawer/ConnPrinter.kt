package com.wyc.cloudapp.print.cashDrawer

import com.wyc.cloudapp.print.printer.AbstractPrinter


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.print.cashDrawer
 * @ClassName:      ConnPrinter
 * @Description:    打印机开钱箱
 * @Author:         wyc
 * @CreateDate:     2022/3/11 16:18
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/3/11 16:18
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class ConnPrinter:IOpen {
    override fun open() {
        AbstractPrinter.openCashDrawer()
    }
}