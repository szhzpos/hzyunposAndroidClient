package com.wyc.cloudapp.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wyc.cloudapp.data.room.entity.PracticeAssociated
@Dao
interface PracticeAssociatedDao {
    @Query("select *from PracticeAssociated")
    fun getAll(): List<PracticeAssociated?>?

    @Query("select * from PracticeAssociated where kw_code =:code and status = 1")
    fun getPracticeById(code: Int): PracticeAssociated?

    @Query("select * from PracticeAssociated where barcode_id =:barcodeId and status = 1")
    fun getPracticeByBarcodeId(barcodeId: String): List<PracticeAssociated>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(associated: List<PracticeAssociated>): LongArray
}