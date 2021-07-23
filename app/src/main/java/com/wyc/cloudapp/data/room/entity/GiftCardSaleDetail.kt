package com.wyc.cloudapp.data.room.entity

import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Entity
import com.wyc.cloudapp.utils.Utils

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.bean
 * @ClassName:      GiftCardSaleDetail
 * @Description:     购物卡销售明细
 * @Author:         wyc
 * @CreateDate:     2021-07-22 10:29
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-07-22 10:29
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
@Entity(tableName = "GiftCardSaleDetail", primaryKeys = arrayOf("rowId", "order_no"))
class GiftCardSaleDetail():Parcelable {
    private var rowId = 0
    private var num = 0
    private var amt = 0.0
    private var price = 0.0
    private var face_value = 0.0
    private var gift_card_code = ""
    private var card_chip_no = ""
    private var name: String? = null
    private var discountAmt = 0.0
    /*
    * 对应的销售订单号
 * */
    @NonNull
    private var order_no:String = ""

    constructor(parcel: Parcel) : this() {
        rowId = parcel.readInt()
        num = parcel.readInt()
        amt = parcel.readDouble()
        price = parcel.readDouble()
        face_value = parcel.readDouble()
        gift_card_code = parcel.readString() ?: ""
        card_chip_no = parcel.readString() ?: ""
        name = parcel.readString()
        discountAmt = parcel.readDouble()
        order_no = parcel.readString() ?: ""
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(rowId)
        parcel.writeInt(num)
        parcel.writeDouble(amt)
        parcel.writeDouble(price)
        parcel.writeDouble(face_value)
        parcel.writeString(gift_card_code)
        parcel.writeString(card_chip_no)
        parcel.writeString(name)
        parcel.writeDouble(discountAmt)
        parcel.writeString(order_no)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<GiftCardSaleDetail> {
        override fun createFromParcel(parcel: Parcel): GiftCardSaleDetail {
            return GiftCardSaleDetail(parcel)
        }

        override fun newArray(size: Int): Array<GiftCardSaleDetail?> {
            return arrayOfNulls(size)
        }
    }
    fun setRowId(id: Int){
        rowId = id
    }
    fun getRowId(): Int {
        return rowId
    }
    fun setNum(n: Int){
        num = n
        amt = n * price
    }
    fun getNum(): Int {
        return num
    }
    fun setAmt(a: Double){
        amt = a
    }
    fun getAmt(): Double {
        return amt
    }
    fun setPrice(p: Double){
        price = p
    }
    fun getPrice(): Double {
        return price
    }
    fun setFace_value(f: Double){
        face_value = f;
    }
    fun getFace_value(): Double {
        return face_value
    }
    fun getGift_card_code(): String{
        return gift_card_code
    }
    fun setGift_card_code(code: String){
        gift_card_code = code
    }

    fun getCard_chip_no():String{
        return card_chip_no
    }
    fun setCard_chip_no(code: String){
        card_chip_no = code
    }

    fun setName(n: String?){
        name = n
    }
    fun getName(): String? {
        return name
    }
    fun setDiscountAmt(a: Double){
        discountAmt = a;
    }
    fun getDiscountAmt(): Double {
        return discountAmt
    }
    fun setOrder_no(no: String){
        if (!Utils.isNotEmpty(no)) throw IllegalArgumentException("order_no must not be empty.")
        order_no = no
    }
    fun getOrder_no(): String {
        if (!Utils.isNotEmpty(order_no)) throw IllegalArgumentException("order_no must not be empty.")
        return order_no
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GiftCardSaleDetail

        if (rowId != other.rowId) return false

        return true
    }

    override fun hashCode(): Int {
        return rowId
    }



    fun equalsWithTimeCardInfo(o: GiftCardSaleDetail?): Boolean {
        return if (o == null) false else gift_card_code == o.gift_card_code
    }

    override fun toString(): String {
        return "GiftCardSaleDetail(rowId=$rowId, num=$num, amt=$amt, price=$price, face_value=$face_value, gift_card_code='$gift_card_code', card_chip_no='$card_chip_no', name=$name, discountAmt=$discountAmt, order_no='$order_no')"
    }

    class Builder {
        private val mInfo: GiftCardSaleDetail = GiftCardSaleDetail()
        fun giftCode(code: String): Builder {
            mInfo.setGift_card_code(code)
            return this
        }

        fun card_chip_no(code: String):Builder{
            mInfo.setCard_chip_no(code)
            return this
        }

        fun name(sz: String?): Builder {
            mInfo.setName(sz)
            return this
        }

        fun price(p: Double): Builder {
            mInfo.setPrice(p)
            return this
        }

        fun face_value(p: Double): Builder {
            mInfo.setFace_value(p)
            return this
        }

        fun num(n: Int): Builder {
            mInfo.setNum(n)
            return this
        }

        fun build(): GiftCardSaleDetail {
            return mInfo
        }

    }

}