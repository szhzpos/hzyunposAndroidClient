package com.wyc.cloudapp.design

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.design
 * @ClassName:      LabelTemplate
 * @Description:    标签模板
 * @Author:         wyc
 * @CreateDate:     2022/3/25 18:17
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/3/25 18:17
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */
@Entity(tableName = "labelTemplate")
class LabelTemplate(w:Int = 70,h:Int = 40,name:String = "未命名") {
    @PrimaryKey
    var templateId:Int = UUID.randomUUID().toString().hashCode()
    var templateName:String = ""
    var width = 0
    var height = 0
    var itemList:String = "[]"

    init {
        width = w
        height = h
        templateName = String.format("%s_%d_%d",name,w,h)
    }

    override fun toString(): String {
        return "LabelTemplate(templateId=$templateId, templateName='$templateName', width=$width, height=$height, itemList=$itemList)"
    }
}