package com.wyc.cloudapp.data.room.entity

import android.app.Activity
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.sqlite.db.SimpleSQLiteQuery
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.annotation.JSONField
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.GiftCardSaleResult
import com.wyc.cloudapp.bean.ICardPay
import com.wyc.cloudapp.bean.PayDetailInfo
import com.wyc.cloudapp.bean.UnifiedPayResult
import com.wyc.cloudapp.constants.InterfaceURL
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.data.room.AppDatabase
import com.wyc.cloudapp.data.room.dao.GiftCardSaleOrderDao
import com.wyc.cloudapp.dialog.CustomProgressDialog
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.fragment.PrintFormatFragment
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.print.Printer
import com.wyc.cloudapp.utils.FormatDateTimeUtils
import com.wyc.cloudapp.utils.Utils
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import com.wyc.cloudapp.utils.http.callback.ObjectCallback
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.data.room.entity
 * @ClassName:      GiftCardSaleOrder
 * @Description:     购物卡销售订单
 * @Author:         wyc
 * @CreateDate:     2021-07-22 11:00
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-07-22 11:00
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
@Entity(tableName = "GiftCardSaleOrder")
class GiftCardSaleOrder():ICardPay<GiftCardSaleDetail> {
    @PrimaryKey
    @NonNull
    private var order_no: String = generateOrderNo()

    @ColumnInfo(name = "online_order_no")
    private var online_order_no:String = ""

    private var amt:Double = 0.0

    @ColumnInfo(defaultValue = "0")
    var status:Int = 0
        get() = field
        set(value) {field = value}

    var saleId:String = "0"
        get() = field
        set(value) {field = value}

    var cas_id = "0"
        get() = field
        set(value) {field = value}

    @ColumnInfo(defaultValue = "-1")
    var store_id = CustomApplication.self().storeId
        get() = field
        set(value) {field = value}

    @ColumnInfo(defaultValue = "0")
    private var time:Long = 0

    @ColumnInfo(defaultValue = "0")
    var transfer_status:Int = 0
        get() = field
        set(value) {field = value}

    @Ignore
    @JSONField(serialize =false)
    private var saleInfo:List<GiftCardSaleDetail> = ArrayList()

    @Ignore
    @JSONField(serialize =false)
    var payInfo:List<GiftCardPayDetail> = ArrayList()
        get() = field
        set(value) {field = value}

    fun setOrder_no(id: String){
        order_no = id
    }
    fun setAmt(a: Double){
        amt = a
    }

    constructor(parcel: Parcel) : this() {
        order_no = parcel.readString()?: ""
        online_order_no = parcel.readString()?: ""
        amt = parcel.readDouble()
        status = parcel.readInt()
        saleId = parcel.readString()?: ""
        cas_id = parcel.readString()?: ""
        store_id = parcel.readString() ?: ""
        time = parcel.readLong()
        transfer_status = parcel.readInt()
        saleInfo = parcel.createTypedArrayList(GiftCardSaleDetail.CREATOR) ?: ArrayList()
        payInfo = parcel.createTypedArrayList(GiftCardPayDetail.CREATOR) ?: ArrayList()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(order_no)
        parcel.writeString(online_order_no)
        parcel.writeDouble(amt)
        parcel.writeInt(status)
        parcel.writeString(saleId)
        parcel.writeString(cas_id)
        parcel.writeString(store_id)
        parcel.writeLong(time)
        parcel.writeInt(transfer_status)
        parcel.writeTypedList(saleInfo)
        parcel.writeTypedList(payInfo)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object{
        @JvmField
        val CREATOR : Parcelable.Creator<GiftCardSaleOrder>  = object : Parcelable.Creator<GiftCardSaleOrder> {
            override fun createFromParcel(parcel: Parcel): GiftCardSaleOrder {
                return GiftCardSaleOrder(parcel)
            }

            override fun newArray(size: Int): Array<GiftCardSaleOrder?> {
                return arrayOfNulls(size)
            }
        }
        @JvmStatic
        fun getOrderList(start: Long, end: Long, order_no: String):List<GiftCardSaleOrder>{
            val where_sql = java.lang.StringBuilder("select * from GiftCardSaleOrder where time between $start and $end")
            if (Utils.isNotEmpty(order_no)){
                where_sql.append(" and online_order_no like '%").append(order_no).append("'")
            }
            where_sql.append(" order by time desc")
            Logger.d("sql:%s", where_sql)
            return AppDatabase.getInstance().GiftCardSaleOrderDao().getOrderById(SimpleSQLiteQuery(where_sql.toString()))
        }
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GiftCardSaleOrder

        if (order_no != other.order_no) return false

        return true
    }

    override fun hashCode(): Int {
        return order_no.hashCode()
    }

    override fun toString(): String {
        return "GiftCardSaleOrder(order_no='$order_no', online_order_no='$online_order_no', amt=$amt, status=$status, saleId=$saleId, cas_id=$cas_id, store_id=$store_id, time=$time, transfer_status=$transfer_status, saleInfo=$saleInfo, payInfo=$payInfo)"
    }

    class Builder {
        private val order: GiftCardSaleOrder = GiftCardSaleOrder()
        fun order_no(order_no: String?): Builder {
            order.order_no = order_no!!
            return this
        }

        fun amt(amt: Double): Builder {
            order.amt = amt
            return this
        }

        fun status(status: Int): Builder {
            order.status = status
            return this
        }

        fun cas_id(cas_id: String): Builder {
            order.cas_id = cas_id
            return this
        }

        fun saleman(saleId: String): Builder {
            order.saleId = saleId
            return this
        }

        fun time(time: Long): Builder {
            order.time = time
            return this
        }

        fun transfer_status(status: Int): Builder {
            order.transfer_status = status
            return this
        }

        fun saleInfo(saleInfoList: List<GiftCardSaleDetail>?): Builder {
            order.setSaleInfo(saleInfoList ?: ArrayList())
            return this
        }

        fun build(): GiftCardSaleOrder {
            return order
        }

    }

    fun getStatusName(): String {
        return if (status == 1) {
            CustomApplication.self().getString(R.string.success)
        } else if (status == 2) {
            CustomApplication.self().getString(R.string.failure)
        } else if (status == 3) {
            CustomApplication.self().getString(R.string.paying)
        } else CustomApplication.self().getString(R.string.uploading)
    }

    fun getFormatTime(): String? {
        return FormatDateTimeUtils.formatTimeWithTimestamp(time * 1000)
    }

    fun getDetailCount():Int{
        return AppDatabase.getInstance().GiftCardSaleOrderDao().detailCount(order_no)
    }

    fun getCasName():String?{
        return SQLiteHelper.getCashierNameById(cas_id)
    }

    private fun generateOrderNo(): String {
        val row = AppDatabase.getInstance().GiftCardSaleOrderDao().count() + 1
        return "GW" + CustomApplication.self().posNum + "-" + SimpleDateFormat("yyMMddHHmmss", Locale.CHINA).format(Date()) + "-" + String.format(Locale.CHINA, "%04d", row)
    }

    fun setOnline_order_no(value: String){
        online_order_no = value;
    }
    fun getOnline_order_no(): String {
        return online_order_no;
    }

    fun setTime(value: Long){
        time = value;
    }
    fun getTime():Long{
        return time;
    }

    override fun getAmt(): Double {
        return amt;
    }

    override fun getSaleInfo(): List<GiftCardSaleDetail> {
        return saleInfo
    }

    fun setSaleInfo(value: List<GiftCardSaleDetail>?){
        saleInfo = value ?: ArrayList()
        saleInfo.listIterator().forEach {
            it.setOrder_no(order_no)
        }
    }

    override fun getOrder_no(): String {
        return order_no
    }

    override fun save(a: MainActivity, payDetailList: List<PayDetailInfo>) {
        time = System.currentTimeMillis() / 1000
        setOrderPayInfo(a, payDetailList)

        val dao:GiftCardSaleOrderDao = AppDatabase.getInstance().GiftCardSaleOrderDao();
        dao.deleteWithDetails(this, getSaleInfo(), payInfo)
        dao.insertWithDetails(this, getSaleInfo(), payInfo)

        startPay(a)
    }

    private fun startPay(activity: MainActivity) {
        val progressDialog = CustomProgressDialog.showProgress(activity, "正在支付...")
        val param = JSONObject()
        param["appid"] = activity.appId
        param["order_money"] = amt
        param["origin"] = 5
        param["stores_id"] = activity.storeId
        param["card_json"] = getCards()
        param["sc_id"] = saleId
        param["cas_id"] = activity.cashierId
        HttpUtils.sendAsyncPost(activity.url + InterfaceURL.GIFT_CARD_ORDER_UPLOAD, HttpRequest.generate_request_parm(param, activity.appSecret)) //生成订单
                .enqueue(object : ObjectCallback<GiftCardSaleResult>(GiftCardSaleResult::class.java) {
                    override fun onError(msg: String) {
                        MyDialog.toastMessage(msg)
                        progressDialog.dismiss()
                    }

                    override fun onSuccessForResult(d: GiftCardSaleResult?, hint: String) {
                        try {
                            d?.let {
                                //更新状态以及保存线上单号
                                updateOnlineOrderNo(it.orderCode)

                                var allSuccess = false
                                for (payDetail in payInfo) {
                                    val payMethod: PayMethod = PayMethod.getMethodById(payDetail.pay_method_id)
                                    if (payMethod.isCheckApi) {
                                        val payResult: UnifiedPayResult = payMethod.payWithApi(activity, d.orderMoney, d.orderCode, getOrder_no(), payDetail.remark
                                                ?: "", GiftCardSaleOrder.toString())
                                        if (payResult.isSuccess) {
                                            allSuccess = true
                                            payDetail.success()
                                            payDetail.online_pay_no = payResult.pay_code
                                        } else {
                                            allSuccess = false
                                            payDetail.failure()
                                            MyDialog.toastMessage(payResult.info)
                                            break
                                        }
                                    } else {
                                        allSuccess = true
                                        payDetail.success()
                                    }
                                }
                                //更新支付状态
                                GiftCardPayDetail.update(payInfo)

                                if (allSuccess) {
                                    uploadPayInfo(it.orderMoney, {
                                        print(activity)

                                        activity.setResult(Activity.RESULT_OK)
                                        activity.finish()
                                        MyDialog.toastMessage(it)
                                        progressDialog.dismiss()
                                    }, {
                                        MyDialog.toastMessage(it)
                                        progressDialog.dismiss()
                                    })
                                } else progressDialog.dismiss()
                            }
                        } catch (e: Exception) {
                            MyDialog.toastMessage(e.localizedMessage)
                            progressDialog.dismiss()
                        }
                    }
                })
    }

    public fun print(activity: MainActivity){
        CustomApplication.execute { Printer.print(get_print_content(activity)) }
    }
    private fun get_print_content(context: MainActivity): String {
        var content = ""
        val print_format_info = JSONObject()
        if (SQLiteHelper.getLocalParameter("g_card_sale", print_format_info)) {
            if (print_format_info.getIntValue("f") == PrintFormatFragment.GIFT_CARD_SALE_FORMAT_ID) {
                when (print_format_info.getIntValue("f_z")) {
                    R.id.f_58 -> {
                        content = c_format_58(context, print_format_info, this)
                    }
                    R.id.f_76 -> {
                    }
                    R.id.f_80 -> {
                    }
                }
            } else {
                context.runOnUiThread { MyDialog.ToastMessage(context.getString(R.string.f_not_sz), context.window) }
            }
        } else context.runOnUiThread { MyDialog.ToastMessage(context.getString(R.string.l_p_f_err_hint_sz, print_format_info.getString("info")), context.window) }
        return content
    }

    private fun c_format_58(context: MainActivity, format_info: JSONObject, order_info: GiftCardSaleOrder): String {
        val info = StringBuilder()
        val out = StringBuilder()
        val store_name = Utils.getNullStringAsEmpty(format_info, "s_n")
        val new_line = "\n"
        val footer_c = Utils.getNullStringAsEmpty(format_info, "f_c")
        var print_count = Utils.getNotKeyAsNumberDefault(format_info, "p_c", 1)
        val footer_space = Utils.getNotKeyAsNumberDefault(format_info, "f_s", 5)
        val application = CustomApplication.self()
        while (print_count-- > 0) { //打印份数
            if (info.isNotEmpty()) {
                info.append(new_line).append(new_line)
                out.append(info)
                continue
            }
            info.append(Printer.commandToStr(Printer.DOUBLE_HEIGHT)).append(Printer.commandToStr(Printer.ALIGN_CENTER))
                    .append(if (store_name.length == 0) application.storeName else store_name).append(new_line).append(new_line).append(Printer.commandToStr(Printer.NORMAL)).append(Printer.commandToStr(Printer.ALIGN_LEFT))
            info.append(context.getString(R.string.store_name_sz) + application.storeName).append(new_line)
            info.append(context.getString(R.string.order_sz) + order_info.online_order_no).append(new_line)
            info.append(context.getString(R.string.order_time_colon) + order_info.getFormatTime()).append(new_line)


            val stringBuilder = StringBuilder()
            val saleInfoList = order_info.getSaleInfo()
            val line_58 = application.getString(R.string.line_58)
            stringBuilder.append(line_58).append(Printer.commandToStr(Printer.LINE_SPACING_2)).append("名称  卡号      面额       售价")
                    .append(Printer.commandToStr(Printer.LINE_SPACING_2)).append(new_line).append(line_58).append(new_line)
            for (saleInfo in saleInfoList) {
                stringBuilder.append(String.format(Locale.CHINA, "%s\n     %s     %.2f元   %.2f元\n", saleInfo.getName(), saleInfo.getGift_card_code(), saleInfo.getFace_value(), saleInfo.getPrice(), new_line))
            }
            stringBuilder.append(line_58).append(new_line)
            info.append(stringBuilder)
            info.append(context.getString(R.string.gift_card_sale_num_print) + order_info.getSaleInfo().size.toString()).append(new_line)
            info.append(context.getString(R.string.order_amt_colon) + String.format(Locale.CHINA, "%.2f元", order_info.getAmt())).append(new_line)

            stringBuilder.delete(0, stringBuilder.length)
            val payDetails = order_info.payInfo
            for (detail in payDetails) {
                val payMethod = AppDatabase.getInstance().PayMethodDao().getPayMethodById(detail.pay_method_id) ?: continue
                if (stringBuilder.isNotEmpty()) {
                    stringBuilder.append(new_line)
                }
                stringBuilder.append(payMethod.name)
            }
            info.append(context.getString(R.string.pay_method_name_colon_sz) + stringBuilder.toString()).append(new_line).append(line_58).append(new_line)

            if (footer_c.isEmpty()) {
                info.append(context.getString(R.string.hotline_sz)).append(Utils.getNullOrEmptyStringAsDefault(application.storeInfo, "telphone", "")).append(new_line)
                info.append(context.getString(R.string.stores_address_sz)).append(Utils.getNullOrEmptyStringAsDefault(application.storeInfo, "region", "")).append(new_line)
            } else {
                info.append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(footer_c).append(Printer.commandToStr(Printer.ALIGN_LEFT))
            }
            for (i in 0 until footer_space) info.append(" ").append(new_line)
        }
        out.append(info)
        return out.toString()
    }

    private fun uploadPayInfo(amt: Double, suc: androidx.core.util.Consumer<String>, failure: androidx.core.util.Consumer<String>){
        if (payInfo.isEmpty()) {
            MyDialog.toastMessage(CustomApplication.self().getString(R.string.hints_pay_detail_not_empty))
            return
        }

        val param = JSONObject()
        param["appid"] = CustomApplication.self().appId
        param["order_code"] = online_order_no
        param["cas_pay_money"] = amt
        param["pay_method"] = payInfo[0].pay_method_id

        HttpUtils.sendAsyncPost(CustomApplication.self().url + "/api/api_shopping/pay", HttpRequest.generate_request_parm(param, CustomApplication.self().appSecret))
                .enqueue(object : ObjectCallback<String>(String::class.java) {

                    override fun onSuccessForResult(d: String?, hint: String?) {
                        success()
                        suc.accept(hint)
                    }

                    override fun onError(msg: String?) {
                        failure.accept(msg);
                    }
                });
    }

    private fun success(){
        status = 1
        AppDatabase.getInstance().GiftCardSaleOrderDao().updateOrder(this)
    }

    private fun updateOnlineOrderNo(online_no: String){
        online_order_no = online_no
        status = 3
        AppDatabase.getInstance().GiftCardSaleOrderDao().updateOrder(this)
    }

    private fun getCards(): String {
        val array = JSONArray()
        saleInfo.listIterator().forEach {
            val obj = JSONObject()
            obj["card_chip_no"] = it.getCard_chip_no()
            obj["price"] = it.getPrice()
            array.add(obj)
        }
        return array.toString()
    }

    private fun setOrderPayInfo(context: MainActivity, payDetailList: List<PayDetailInfo>) {
        val payDetails: MutableList<GiftCardPayDetail> = java.util.ArrayList()
        var pamt: Double
        var zl_amt: Double
        var index = 1
        for (payDetailInfo in payDetailList) {
            pamt = payDetailInfo.pay_amt
            zl_amt = payDetailInfo.zl_amt
            val payDetail = GiftCardPayDetail.Builder(getOrder_no()).rowId(index++).pay_method_id(payDetailInfo.method_id).remark(payDetailInfo.v_num)
                    .amt(pamt - zl_amt).zl_amt(zl_amt).cas_id(context.cashierId).build()
            payDetails.add(payDetail)
        }
        payInfo = payDetails
    }
}