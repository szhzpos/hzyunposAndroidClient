package com.wyc.cloudapp.print.receipts

import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.data.room.AppDatabase
import com.wyc.cloudapp.data.room.entity.GiftCardSaleOrder
import com.wyc.cloudapp.fragment.PrintFormatFragment
import com.wyc.cloudapp.print.PrintItem
import com.wyc.cloudapp.print.Printer
import com.wyc.cloudapp.print.bean.PrintFormatInfo
import com.wyc.cloudapp.utils.Utils
import java.util.*

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.print.receipts
 * @ClassName:      GiftCardReceipts
 * @Description:    购物卡打印单据
 * @Author:         wyc
 * @CreateDate:     2021-12-31 15:19
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-12-31 15:19
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class GiftCardReceipts(private val order_info:GiftCardSaleOrder,open:Boolean): AbstractReceipts(formatInfo("g_card_sale"),order_info.getOrder_no(),open) {

    override fun c_format_58(
        formatInfo: PrintFormatInfo,
        orderCode: String
    ): MutableList<PrintItem> {
        val printItems = mutableListOf<PrintItem>()
        val application = CustomApplication.self()

        val store_name = formatInfo.aliasStoresName
        val new_line = "\n"
        val footer_c = formatInfo.footerContent
        val line_58 = application.getString(R.string.line_58)



        printItems.add(PrintItem.Builder().setBold(true).setDoubleHigh(true).setAlign(PrintItem.Align.CENTRE)
                .setContent(if (store_name.isEmpty()) SQLiteHelper.getStoreNameById(order_info.store_id) else store_name)
                .build()
        )
        printItems.add(PrintItem.Builder()
            .setContent(String.format("%s%s",application.getString(R.string.store_name_sz),SQLiteHelper.getStoreNameById(order_info.store_id)))
            .build()
        )
        printItems.add(PrintItem.Builder()
            .setContent(String.format("%s%s",application.getString(R.string.order_sz),order_info.getOnline_order_no()))
            .build()
        )
        printItems.add(PrintItem.Builder()
            .setContent(String.format("%s%s",application.getString(R.string.order_time_colon),order_info.getFormatTime()))
            .build()
        )

        val saleInfoList = order_info.getSaleInfo()
        printItems.add(PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent("名称  卡号      面额       售价").build())
        printItems.add(PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(line_58).build())
        printItems.add(PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(line_58).build())

        for (saleInfo in saleInfoList) {
            printItems.add(PrintItem.Builder().setContent(String.format(Locale.CHINA, "%s     %s     %.2f元   %.2f元", saleInfo.getName(), saleInfo.getGift_card_code(), saleInfo.getFace_value(), saleInfo.getPrice(), new_line)).build())
        }
        printItems.add(PrintItem.Builder().setContent(line_58).build())

        printItems.add(PrintItem.Builder()
            .setContent(String.format("%s%d",application.getString(R.string.gift_card_sale_num_print),order_info.getSaleInfo().size))
            .build()
        )
        printItems.add(PrintItem.Builder()
            .setContent(String.format("%s%.2f元",application.getString(R.string.order_amt_colon),order_info.getAmt()))
            .build()
        )

        val payDetails = order_info.payInfo
        val stringBuilder = StringBuffer()
        for (detail in payDetails) {
            val payMethod = AppDatabase.getInstance().PayMethodDao().getPayMethodById(detail.pay_method_id) ?: continue
            if (stringBuilder.isNotEmpty()) {
                stringBuilder.append(new_line)
            }
            stringBuilder.append(payMethod.name)
        }
        printItems.add(PrintItem.Builder().setContent(String.format("%s%s",application.getString(R.string.pay_method_name_colon_sz),stringBuilder)).build())
        printItems.add(PrintItem.Builder().setContent(line_58).build())

        if (footer_c.isEmpty()) {
            printItems.add(
                PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(
                    String.format(Locale.CHINA, "%s%s", application.getString(R.string.b_f_hotline_sz), application.storeTelephone)
                ).build()
            )
            printItems.add(
                PrintItem.Builder().setContent(
                    String.format(Locale.CHINA, "%s%s", application.getString(R.string.b_f_stores_address_sz), application.storeRegion)
                ).build()
            )
        } else {
            printItems.add(
                PrintItem.Builder().setAlign(PrintItem.Align.CENTRE).setContent(footer_c).build()
            )
        }
        return printItems
    }

    override fun c_format_76(
        formatInfo: PrintFormatInfo,
        orderCode: String
    ): MutableList<PrintItem> {
        return mutableListOf()
    }

    override fun c_format_80(
        formatInfo: PrintFormatInfo,
        orderCode: String
    ): MutableList<PrintItem> {
        return mutableListOf()
    }

    override fun getFormatId(): Int {
        return PrintFormatFragment.GIFT_CARD_SALE_FORMAT_ID
    }
}