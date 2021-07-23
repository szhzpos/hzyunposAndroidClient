package com.wyc.cloudapp.data.room.dao

import androidx.room.*
import com.wyc.cloudapp.data.room.entity.GiftCardPayDetail
import com.wyc.cloudapp.data.room.entity.GiftCardSaleDetail
import com.wyc.cloudapp.data.room.entity.GiftCardSaleOrder

@Dao
interface GiftCardSaleOrderDao {
    @Query("select * from GiftCardSaleOrder")
    fun getAll():List<GiftCardSaleOrder>

    @Query("select count(rowId) as counts from GiftCardSaleOrder")
    fun count(): Int

    @Transaction
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertWithDetails(saleOrders: GiftCardSaleOrder, saleInfoList: List<GiftCardSaleDetail>, payDetails: List<GiftCardPayDetail>)

    @Transaction
    @Delete
    fun deleteWithDetails(saleOrders: GiftCardSaleOrder, saleInfoList: List<GiftCardSaleDetail>, payDetails: List<GiftCardPayDetail>)

    @Update
    fun updateOrder(saleOrders: GiftCardSaleOrder)
}