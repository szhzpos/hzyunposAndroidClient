package com.wyc.cloudapp.data.room.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.wyc.cloudapp.activity.MainActivity
import com.wyc.cloudapp.bean.CardPay
import com.wyc.cloudapp.bean.PayDetailInfo

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
class GiftCardSaleOrder():CardPay<GiftCardSaleDetail> {
    @PrimaryKey
    @NonNull
    private var order_no: String = ""

    var online_order_no:String = ""
        get() = field
        set(value) {field = value}

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

    var store_id = "0"
        get() = field
        set(value) {field = value}

    @ColumnInfo(defaultValue = "0")
    var time:Long = 0
        get() = field
        set(value) {field = value}

    @ColumnInfo(defaultValue = "0")
    var transfer_status:Int = 0
        get() = field
        set(value) {field = value}

    @Ignore
    private var saleInfo:List<GiftCardSaleDetail>? = null
        set(value) {field = value}
    @Ignore
    var payInfo:List<TimeCardPayDetail>? = null
        get() = field ?: ArrayList()
        set(value) {field = value}

    fun setOrder_no(id:String){
        order_no = id
    }
    fun setAmt(a:Double){
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
        saleInfo = parcel.createTypedArrayList(GiftCardSaleDetail)
        payInfo = parcel.createTypedArrayList(TimeCardPayDetail.CREATOR)
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

    companion object CREATOR : Parcelable.Creator<GiftCardSaleOrder> {
        override fun createFromParcel(parcel: Parcel): GiftCardSaleOrder {
            return GiftCardSaleOrder(parcel)
        }

        override fun newArray(size: Int): Array<GiftCardSaleOrder?> {
            return arrayOfNulls(size)
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

        fun online_order_no(order_no: String): Builder {
            order.online_order_no = order_no
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

        fun store_id(id: String): Builder {
            order.store_id = id
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
            order.saleInfo = saleInfoList
            return this
        }

        fun payInfo(payDetailList: List<TimeCardPayDetail>?): Builder {
            order.payInfo = payDetailList
            return this
        }

        fun build(): GiftCardSaleOrder {
            return order
        }

    }

    override fun getAmt(): Double {
        return amt;
    }

    override fun getSaleInfo(): List<GiftCardSaleDetail> {
        return saleInfo ?: ArrayList();
    }

    override fun getOrder_no(): String {
        return order_no
    }

    override fun save(a: MainActivity, payDetailList: List<PayDetailInfo>) {

    }

}