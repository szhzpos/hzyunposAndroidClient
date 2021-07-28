package com.wyc.cloudapp.data.room.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import com.wyc.cloudapp.data.room.AppDatabase
import com.wyc.cloudapp.utils.FormatDateTimeUtils
import com.wyc.cloudapp.utils.Utils

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.data.room.entity
 * @ClassName:      GiftCardPayDetail
 * @Description:     购物卡付款明细
 * @Author:         wyc
 * @CreateDate:     2021-07-22 11:05
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-07-22 11:05
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
@Entity(tableName = "GiftCardPayDetail", primaryKeys = ["rowId", "order_no"])
class GiftCardPayDetail():Parcelable {
    var rowId = 0

    /*
    * 对应的销售订单号
    * */
    var order_no: String = ""
    var pay_method_id = 0
    var amt = 0.0
    var zl_amt = 0.0
    var online_pay_no: String? = null
    var remark: String? = null
    var status = 0
    var cas_id: String? = null
    var pay_time: String? = null

    constructor(parcel: Parcel) : this() {
        rowId = parcel.readInt()
        order_no = parcel.readString() ?: ""
        pay_method_id = parcel.readInt()
        amt = parcel.readDouble()
        zl_amt = parcel.readDouble()
        online_pay_no = parcel.readString()
        remark = parcel.readString()
        status = parcel.readInt()
        cas_id = parcel.readString()
        pay_time = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(rowId)
        parcel.writeString(order_no)
        parcel.writeInt(pay_method_id)
        parcel.writeDouble(amt)
        parcel.writeDouble(zl_amt)
        parcel.writeString(online_pay_no)
        parcel.writeString(remark)
        parcel.writeInt(status)
        parcel.writeString(cas_id)
        parcel.writeString(pay_time)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object{
        @JvmField val  CREATOR:Parcelable.Creator<GiftCardPayDetail> = object : Parcelable.Creator<GiftCardPayDetail> {
            override fun createFromParcel(parcel: Parcel): GiftCardPayDetail {
                return GiftCardPayDetail(parcel)
            }

            override fun newArray(size: Int): Array<GiftCardPayDetail?> {
                return arrayOfNulls(size)
            }
        }

        @JvmStatic
        fun update(data: List<GiftCardPayDetail>) {
            AppDatabase.getInstance().GiftCardPayDetailDao().updateAll(data)
        }
        @JvmStatic
        fun getPayDetailById(id:String):List<GiftCardPayDetail>?{
            return AppDatabase.getInstance().GiftCardPayDetailDao().getAllById(id)
        }
    }

    class Builder(order_no: String) {
        private val detail: GiftCardPayDetail = GiftCardPayDetail()
        fun pay_method_id(id: Int): Builder {
            detail.pay_method_id = id
            return this
        }

        fun rowId(r_id:Int):Builder{
            detail.rowId = r_id
            return this
        }

        fun amt(amt: Double): Builder {
            detail.amt = amt
            return this
        }

        fun zl_amt(amt: Double): Builder {
            detail.zl_amt = amt
            return this
        }

        fun online_pay_no(pay_no: String?): Builder {
            detail.online_pay_no = pay_no
            return this
        }

        fun remark(remark: String?): Builder {
            detail.remark = remark
            return this
        }

        fun status(status: Int): Builder {
            detail.status = status
            return this
        }

        fun cas_id(cas_id: String?): Builder {
            detail.cas_id = cas_id
            return this
        }

        fun pay_time(pay_time: String?): Builder {
            detail.pay_time = pay_time
            return this
        }

        fun build(): GiftCardPayDetail {
            return detail
        }

        init {
            if (!Utils.isNotEmpty(order_no)) throw IllegalArgumentException("order_no must not be empty.")
            detail.pay_time = FormatDateTimeUtils.formatCurrentTime(FormatDateTimeUtils.YYYY_MM_DD_1)
            detail.order_no = order_no;
        }
    }

    fun success() {
        status = 1
    }
    fun failure(){
        status - 2
    }

    fun getMethodName():String{
        return AppDatabase.getInstance().PayMethodDao().getPayMethodById(pay_method_id)?.name ?: ""
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GiftCardPayDetail

        if (rowId != other.rowId) return false
        if (order_no != other.order_no) return false

        return true
    }

    override fun hashCode(): Int {
        var result = rowId
        result = 31 * result + order_no.hashCode()
        return result
    }

    override fun toString(): String {
        return "GiftCardPayDetail(rowId=$rowId, order_no='$order_no', pay_method_id=$pay_method_id, amt=$amt, zl_amt=$zl_amt, online_pay_no=$online_pay_no, remark=$remark, status=$status, cas_id=$cas_id, pay_time=$pay_time)"
    }

}
