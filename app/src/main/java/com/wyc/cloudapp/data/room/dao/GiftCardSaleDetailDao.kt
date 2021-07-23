package com.wyc.cloudapp.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.wyc.cloudapp.data.room.entity.GiftCardSaleDetail

@Dao
interface GiftCardSaleDetailDao {
    @Query("select * from GiftCardSaleDetail")
    fun getAll(): List<GiftCardSaleDetail>?

    @Insert
    fun insertAll(saleInfo: List<GiftCardSaleDetail>)

    @Query("select * from GiftCardSaleDetail where order_no=:id")
    fun getDetailById(id: String): List<GiftCardSaleDetail>?
}