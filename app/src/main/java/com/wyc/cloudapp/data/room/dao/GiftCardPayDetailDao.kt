package com.wyc.cloudapp.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.wyc.cloudapp.data.room.entity.GiftCardPayDetail
import com.wyc.cloudapp.data.room.entity.TimeCardPayDetail

@Dao
interface GiftCardPayDetailDao {
    @Query("select * from GiftCardPayDetail")
    fun getAll(): List<GiftCardPayDetail>

    @Query("select * from GiftCardPayDetail where order_no=:id")
    fun getAllById(id: String): List<GiftCardPayDetail>?

    @Insert
    fun insertAll(details: List<GiftCardPayDetail>)

    @Update
    fun updateAll(details: List<GiftCardPayDetail>)
}