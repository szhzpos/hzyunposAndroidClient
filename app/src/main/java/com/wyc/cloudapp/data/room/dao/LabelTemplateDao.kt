package com.wyc.cloudapp.data.room.dao

import androidx.room.*
import com.wyc.cloudapp.design.LabelTemplate

@Dao
interface LabelTemplateDao {
    @Query("select * from labelTemplate")
    fun getAll():MutableList<LabelTemplate>
    @Query("select * from labelTemplate where templateId=:id")
    fun getLabelTemplateById(id:Int):LabelTemplate?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTemplate(labelTemplate: LabelTemplate): Long
    @Delete
    fun deleteTemplateById(labelTemplate: LabelTemplate)
}