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
    /**
     * 打印物理尺寸 单位毫米
     * */
    var width = 0
    var height = 0

    /**
     * 用于重新计算item尺寸，同一个格式可能会加载到不同尺寸的界面
     * */
    var realWidth = 0
    var realHeight = 0

    var itemList:String = "[]"

    init {
        width = w
        height = h
        templateName = String.format("%s_%d_%d",name,w,h)
    }

    fun width2Dot(dpi:Int):Int{
        return (width * dpi * (1.0f / 25.4f)).toInt()
    }
    fun height2Dot(dpi:Int):Int{
        return (height * dpi * (1.0f / 25.4f)).toInt()
    }

    override fun toString(): String {
        return "LabelTemplate(templateId=$templateId, templateName='$templateName', width=$width, height=$height, realWidth=$realWidth, realHeight=$realHeight, itemList='$itemList')"
    }

}