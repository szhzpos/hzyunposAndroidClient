package com.wyc.cloudapp.design

import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.annotation.JSONField
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.dialog.MyDialog
import kotlin.properties.Delegates

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.bean
 * @ClassName:      LabelPrintSetting
 * @Description:    标签打印设置
 * @Author:         wyc
 * @CreateDate:     2022/3/25 16:53
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/3/25 16:53
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class LabelPrintSetting {
    @JSONField(serialize = false)
    private var change = false
    enum class Way(s: String)  {
        BLUETOOTH_PRINT(CustomApplication.self().getString(R.string.bluetooth_way));
        val description:String = s
    }
    var way: Way by change(Way.BLUETOOTH_PRINT)

    enum class Rotate(degree:Int){
        D_0(0),D_90(90),D_180(180),D_270(270);
        val description:String = degree.toString()
    }
    /**
     * 打印偏移 单位mm
     * */
    var offsetX = 0
    var offsetY = 0

    var dpi = 203

    var rotate: Rotate by change(Rotate.D_0)

    var labelTemplateId: Int by change(0)
    var labelTemplateName:String by change("")

    var printNum by change(1)

    var printer:String by change("")
    fun getPrinterAddress():String{
        if (printer.contains("@")){
            return printer.split("@")[1]
        }
        return ""
    }

    companion object{
        @JvmStatic
        fun combinationPrinter(a:String,n:String):String{
            return String.format("%s@%s",n,a)
        }
        @JvmStatic
        fun getSetting(): LabelPrintSetting {
            val para = JSONObject()
            if (!SQLiteHelper.getLocalParameter("label_print", para)){
                MyDialog.toastMessage(para.getString("info"))
                return LabelPrintSetting()
            }
            val setting = JSONObject.parseObject(para.toString(), LabelPrintSetting::class.java)?: LabelPrintSetting()
            setting.change = false
            return setting
        }
    }

    fun saveSetting(){
        CustomApplication.execute {
            val err = StringBuilder()
            if (!SQLiteHelper.saveLocalParameter("label_print", JSONObject.toJSON(this) as? JSONObject, "标签打印参数", err)){
                MyDialog.toastMessage(err.toString())
            }else {
                change = false
                MyDialog.toastMessage(CustomApplication.self().getString(R.string.success))
            }
        }
    }

    fun hasChange():Boolean{
        return change
    }

    private fun <T> change(iv:T) = Delegates.observable(iv) { _, oldValue, newValue ->
        if (oldValue != newValue) change = true
    }

    override fun toString(): String {
        return "LabelPrintSetting(way=$way, rotate=$rotate, labelTemplateId=$labelTemplateId, labelTemplateName='$labelTemplateName', printNum=$printNum, printer='$printer')"
    }


}