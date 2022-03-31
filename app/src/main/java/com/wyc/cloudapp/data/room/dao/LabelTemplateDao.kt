package com.wyc.cloudapp.data.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.wyc.cloudapp.design.LabelTemplate

@Dao
interface LabelTemplateDao {
    @Query("select * from labelTemplate")
    fun getAll():MutableList<LabelTemplate>
    @Query("select * from labelTemplate where templateId=:id")
    fun getLabelTemplateById(id:Int):LabelTemplate?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTemplate(labelTemplate: LabelTemplate): Long
}