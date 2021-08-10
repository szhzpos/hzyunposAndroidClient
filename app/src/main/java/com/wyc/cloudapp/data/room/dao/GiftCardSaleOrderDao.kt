package com.wyc.cloudapp.data.room.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.wyc.cloudapp.data.room.entity.GiftCardPayDetail
import com.wyc.cloudapp.data.room.entity.GiftCardSaleDetail
import com.wyc.cloudapp.data.room.entity.GiftCardSaleOrder

@Dao
interface GiftCardSaleOrderDao {
    @RawQuery
    fun getOrderById(query: SimpleSQLiteQuery):List<GiftCardSaleOrder>

    @Query("select count(rowId) as counts from GiftCardSaleOrder where date(time,'unixepoch' ) = date('now')")
    fun count(): Int

    @Query("select count(1) as counts from GiftCardSaleDetail where order_no =:order_no")
    fun detailCount(order_no:String):Int

    @Transaction
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertWithDetails(saleOrders: GiftCardSaleOrder, saleInfoList: List<GiftCardSaleDetail>, payDetails: List<GiftCardPayDetail>)

    @Transaction
    @Delete
    fun deleteWithDetails(saleOrders: GiftCardSaleOrder, saleInfoList: List<GiftCardSaleDetail>, payDetails: List<GiftCardPayDetail>)

    @Update
    fun updateOrder(saleOrders: GiftCardSaleOrder)
}