package com.wyc.cloudapp.bean

import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.MainActivity
import com.wyc.cloudapp.print.Printer
import com.wyc.cloudapp.utils.Utils
import java.io.Serializable
import java.util.*
import kotlin.collections.ArrayList

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.bean
 * @ClassName:      BusinessOrderPrintContent
 * @Description:    业务单据打印内容
 * @Author:         wyc
 * @CreateDate:     2021-08-20 10:24
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-08-20 10:24
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
open class OrderPrintContentBase :Serializable {
    var company:String? = ""
        get() = field
        private set(value) {
            field = value?:""
        }
    var orderName:String? = ""
        get() = field
        private set(value) {
            field = value?:""
        }

    var storeName:String? = ""
        get() = field
        private set(value) {
            field = value?:""
        }

    var outStoreName:String? = ""
        get() = field
        private set(value) {
            field = value?:""
        }

    var supOrCus:String? = ""
        get() = field
        private set(value) {
            field = value?:""
        }

    var inOutType:String? = ""
        get() = field
        private set(value) {
            field = value?:""
        }

    var operator:String? = ""
        get() = field
        private set(value) {
            field = value?:""
        }
    var orderNo:String? = ""
        get() = field
        private set(value) {
            field = value?:""
        }
    var operateDate:String? = ""
        get() = field
        private set(value) {
            field = value?:""
        }
    var remark:String? = ""
        get() = field
        private set(value) {
            field = value?:""
        }

    var goodsList:List<Goods>? = ArrayList()
        get() = field
        private set(value) {
            field = value?:ArrayList()
        }


    class Goods private constructor(){
        var barcodeId:String? = ""
            get() = field
            private set(value) {
                field = value?:""
            }
        var barcode:String? = ""
            get() = field
            private set(value) {
                field = value?:""
            }
        var name:String? = ""
            get() = field
            private set(value) {
                field = value?:""
            }
        var unit:String? = ""
            get() = field
            private set(value) {
                field = value?:""
            }
        var price:Double? = 0.00
            get() = field
            private set(value) {
                field = value?:0.0
            }
        var num:Double? = 0.00
            get() = field
            private set(value) {
                field = value?:0.0
            }

        override fun toString(): String {
            return "Goods(name=$name, unit=$unit, price=$price, num=$num)"
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Goods

            if (barcodeId != other.barcodeId) return false

            return true
        }

        override fun hashCode(): Int {
            return barcodeId.hashCode()
        }

        class Builder(){
            private val obj = Goods()
            fun barcodeId(s: String?):Builder{
                obj.barcodeId = s
                return this
            }
            fun barcode(s: String?):Builder{
                obj.barcode = s
                return this
            }
            fun name(s: String?):Builder{
                obj.name = s
                return this
            }
            fun unit(s: String?):Builder{
                obj.unit = s
                return this
            }
            fun price(v: Double):Builder{
                obj.price = v
                return this
            }
            fun num(v: Double):Builder{
                obj.num = v
                return this
            }
            fun build():Goods{
                return obj
            }
        }

    }



    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as OrderPrintContentBase

        if (orderNo != other.orderNo) return false

        return true
    }

    override fun hashCode(): Int {
        return orderNo.hashCode()
    }

    open class Builder(private val obj: OrderPrintContentBase = OrderPrintContentBase()) {
        fun company(v: String?):Builder{
            obj.company = v
            return this
        }
        fun orderName(v: String?):Builder{
            obj.orderName = v
            return this
        }
        fun storeName(v: String?):Builder{
            obj.storeName = v
            return this
        }
        fun outStoreName(v: String?):Builder{
            obj.outStoreName = v
            return this
        }
        fun supOrCus(v: String?):Builder{
            obj.supOrCus = v
            return this
        }
        fun inOutType(v: String?):Builder{
            obj.inOutType = v
            return this
        }
        fun operator(v: String?):Builder{
            obj.operator = v
            return this
        }
        fun orderNo(v: String):Builder{
            obj.orderNo = v
            return this
        }
        fun operateDate(v: String?):Builder{
            obj.operateDate = v
            return this
        }
        fun remark(v: String?):Builder{
            obj.remark = v
            return this
        }
        fun goodsList(l: List<Goods>):Builder{
            obj.goodsList = l
            return this
        }
        fun build():OrderPrintContentBase{
            return obj
        }
    }

    open fun getSupOrCusLabel(context: MainActivity):String{
       return context.getString(R.string.sup)
    }

    open fun getInStoreLabel(context: MainActivity):String{
        return context.getString(R.string.in_store)
    }

    open fun getOutStoreLabel(context: MainActivity):String{
        return context.getString(R.string.in_store)
    }

    fun format58(context: MainActivity, printSetting: BusinessOrderPrintSetting):String{
        val line = context.getString(R.string.line_58)
        val new_line = "\n"
        val new_line_10 = Printer.commandToStr(Printer.LINE_SPACING_10)
        val new_line_2 = Printer.commandToStr(Printer.LINE_SPACING_2)
        val new_line_d = Printer.commandToStr(Printer.LINE_SPACING_DEFAULT)
        val right_space =  "        "

        val out = StringBuilder()
        val info = StringBuilder()
        var count = printSetting.print_num

        while (count-- > 0){
            if (info.isNotEmpty()){
                info.append(new_line).append(new_line)
                out.append(info)
                continue
            }
            //单头
            info.append(Printer.commandToStr(Printer.DOUBLE_HEIGHT)).append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(company).append(new_line)
                    .append(orderName).append(Printer.commandToStr(Printer.NORMAL)).append(new_line)

            info.append(line).append(Printer.commandToStr(Printer.ALIGN_LEFT)).append(new_line)

            printItem(info,getOutStoreLabel(context), outStoreName)
            printItem(info,getInStoreLabel(context), storeName)
            printItem(info,getSupOrCusLabel(context), supOrCus)
            printItem(info, context.getString(R.string.out_in), inOutType)
            printItem(info, "  " + context.getString(R.string.oper), operator)
            printItem(info, context.getString(R.string.order_no), orderNo)
            printItem(info, context.getString(R.string.date), operateDate)
            printItem(info, context.getString(R.string.remark_sz), remark)

            info.append(line).append(new_line)

            var sum_num = 0.0
            var sum_amt = 0.0
            var sum: Double

            val isEnquiry = this is EnquiryOrderPrintContent

            if (isEnquiry){
                info.append(context.getString(R.string.enquiry_header_sz).replace("-", " ")).append(new_line_2).append(new_line).append(line).append(new_line)
                goodsList?.forEachIndexed { index, it ->
                    sum = it.num?.times(it.price!!) ?: 0.0
                    sum_num += it.num!!
                    sum_amt += sum

                    if (index != 0) info.append(new_line_10)

                    info.append(Printer.commandToStr(Printer.BOLD)).append(String.format("%s", it.name)).append(new_line).append(new_line_d).append(Printer.commandToStr(Printer.BOLD_CANCEL))
                    info.append(Printer.printThreeDataAlignRight_58(2, {s:String?->  var v = s
                        if (Utils.isNotEmpty(s))if (s!!.length > 13)v = s.substring(0..12)
                        v
                    }(it.barcode),String.format("%s", it.unit), String.format("%.2f", it.num))).append(new_line)
                }
            }else{
                info.append(context.getString(R.string.b_f_header_sz).replace("-", " ")).append(new_line_2).append(new_line).append(line).append(new_line)

                goodsList?.forEachIndexed { index, it ->
                    sum = it.num?.times(it.price!!) ?: 0.0
                    sum_num += it.num!!
                    sum_amt += sum

                    if (index != 0) info.append(new_line_10)

                    info.append(Printer.commandToStr(Printer.BOLD)).append(String.format("%s(%s)", it.name, it.unit)).append(new_line).append(new_line_d).append(Printer.commandToStr(Printer.BOLD_CANCEL))
                    info.append(Printer.printTwoData(1, {s:String?->  var v = s
                        if (Utils.isNotEmpty(s))if (s!!.length > 13)v = s.substring(0..12)
                        v
                    }(it.barcode),
                            Printer.printThreeData(16, String.format("%.2f", it.price), String.format("%.2f", it.num), String.format(Locale.CHINA, "%.2f", sum)))).append(new_line)
                }
            }

            info.append(line).append(new_line)

            if (isEnquiry){
                info.append(String.format("%s：%.2f", context.getString(R.string.num_not_colon_sz), sum_num)).append(Printer.commandToStr(Printer.ALIGN_RIGHT)).append(new_line).append(new_line)
            }else
                info.append(Printer.printTwoData(1, String.format("%s：%.2f", context.getString(R.string.num_not_colon_sz), sum_num),
                        String.format("%s：%.2f", context.getString(R.string.amt_not_colon_sz), sum_amt))).append(new_line).append(new_line)

            info.append(Printer.printTwoData(1, String.format("%s：", context.getString(R.string.handlerName)),
                    String.format("%s：%s", context.getString(R.string.accountant),right_space))).append(new_line).append(new_line)

            info.append(Printer.printTwoData(1, String.format("%s：", context.getString(R.string.auditor)),
                    String.format("%s：%s", context.getString(R.string.signer),right_space))).append(new_line).append(new_line)
        }
        info.append(new_line).append(new_line).append(new_line)

        out.append(info)

        return out.toString()
    }

    private fun printItem(stringBuilder: StringBuilder, prefix: String, content: String?):StringBuilder{
        if (Utils.isNotEmpty(content)){
            stringBuilder.append("${prefix}：${content}").append("\n")
        }
        return stringBuilder
    }

    override fun toString(): String {
        return "OrderPrintContentBase(company=$company, orderName=$orderName, storeName=$storeName, outStoreName=$outStoreName, supOrCus=$supOrCus, inOutType=$inOutType, operator=$operator, orderNo=$orderNo, operateDate=$operateDate, remark=$remark, goodsList=$goodsList)"
    }
}