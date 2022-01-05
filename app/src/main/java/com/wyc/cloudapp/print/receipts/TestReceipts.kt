package com.wyc.cloudapp.print.receipts

import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.fragment.PrintFormatFragment.CHECKOUt_FORMAT_ID
import com.wyc.cloudapp.print.PrintItem
import com.wyc.cloudapp.print.parameter.SalePrintParameter
import com.wyc.cloudapp.print.printer.AbstractPrinter

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.print.receipts
 * @ClassName:      TestReceipts
 * @Description:    测试打印
 * @Author:         wyc
 * @CreateDate:     2022-01-04 13:39
 * @UpdateUser:     更新者
 * @UpdateDate:     2022-01-04 13:39
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class TestReceipts : AbstractReceipts(formatInfo("c_f_info"),"T",false) {

    companion object{
        @JvmStatic
        fun print(){
            AbstractPrinter.printContent(TestReceipts())
        }
    }

    override fun c_format_58(
        formatInfo: SalePrintParameter,
        orderCode: String
    ): MutableList<PrintItem> {
        val printItems = mutableListOf<PrintItem>()
        val store_name = formatInfo.aliasStoresName
        printItems.add(
            PrintItem.Builder().setDoubleHigh(true).setAlign(PrintItem.Align.CENTRE)
                .setContent(if (store_name.isEmpty()) CustomApplication.self().storeName else store_name)
                .build()
        )
        printItems.add(
            PrintItem.Builder().setDoubleHigh(true).setAlign(PrintItem.Align.CENTRE)
                .setContent(CustomApplication.getStringByResId(R.string.testPrint))
                .build()
        )
        return printItems
    }

    override fun c_format_76(
        formatInfo: SalePrintParameter,
        orderCode: String
    ): MutableList<PrintItem> {
        return mutableListOf()
    }

    override fun c_format_80(
        formatInfo: SalePrintParameter,
        orderCode: String
    ): MutableList<PrintItem> {
        return mutableListOf()
    }

    override fun getFormatId(): Int {
        return CHECKOUt_FORMAT_ID;
    }
}