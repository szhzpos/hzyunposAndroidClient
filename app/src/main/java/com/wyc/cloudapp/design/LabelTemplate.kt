package com.wyc.cloudapp.design

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import android.util.TypedValue
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.data.room.entity.GiftCardPayDetail
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
class LabelTemplate(w:Int = 70,h:Int = 40,name:String = "未命名"):Parcelable {
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
    var realWidth = width2Pixel(this,CustomApplication.self())
    var realHeight = height2Pixel(this,CustomApplication.self())

    var itemList:String = "[]"
    /**
     * 背景base64字符串
     * */
    var backgroundImg = ""

    constructor(parcel: Parcel) : this() {
        templateId = parcel.readInt()
        templateName = parcel.readString()?:""
        width = parcel.readInt()
        height = parcel.readInt()
        realWidth = parcel.readInt()
        realHeight = parcel.readInt()
        itemList = parcel.readString()?:"[]"
        backgroundImg = parcel.readString()?:""
    }

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


    companion object{
        @JvmStatic
        fun getDefaultSize():MutableList<LabelSize>{
            return mutableListOf(LabelSize(70,40),LabelSize(50,40),LabelSize(30,20))
        }
        @JvmStatic
        fun width2Pixel(labelTemplate: LabelTemplate,context:Context):Int{
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, labelTemplate.width.toFloat(),context.resources.displayMetrics).toInt()
        }
        @JvmStatic
        fun height2Pixel(labelTemplate: LabelTemplate,context:Context):Int{
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, labelTemplate.height.toFloat(),context.resources.displayMetrics).toInt()
        }

        @JvmField val  CREATOR:Parcelable.Creator<LabelTemplate> = object : Parcelable.Creator<LabelTemplate> {
            override fun createFromParcel(parcel: Parcel): LabelTemplate {
                return LabelTemplate(parcel)
            }

            override fun newArray(size: Int): Array<LabelTemplate?> {
                return arrayOfNulls(size)
            }
        }
    }

    class LabelSize(w:Int,h:Int){
        val rW = w
        val rH = h
        val description:String = String.format("%d*%d",w,h)
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as LabelSize

            if (rW != other.rW) return false
            if (rH != other.rH) return false

            return true
        }

        override fun hashCode(): Int {
            var result = rW
            result = 31 * result + rH
            return result
        }

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(templateId)
        parcel.writeString(templateName)
        parcel.writeInt(width)
        parcel.writeInt(height)
        parcel.writeInt(realWidth)
        parcel.writeInt(realHeight)
        parcel.writeString(itemList)
        parcel.writeString(backgroundImg)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "LabelTemplate(templateId=$templateId, templateName='$templateName', width=$width, height=$height, realWidth=$realWidth, realHeight=$realHeight, itemList='$itemList', backgroundImg='$backgroundImg')"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LabelTemplate

        if (templateId != other.templateId) return false

        return true
    }

    override fun hashCode(): Int {
        return templateId
    }

}