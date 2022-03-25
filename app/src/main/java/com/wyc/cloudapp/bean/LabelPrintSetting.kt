package com.wyc.cloudapp.bean

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
    var rotate:Rotate by change(Rotate.D_0)

    enum class LabelTemplate(s: String){
        LABEL_70_40("70X40纸张"),LABEL_50_40("50X40纸张"),LABEL_30_20("30X20纸张");
        val description:String = s
    }
    var labelTemplate:LabelTemplate by change(LabelTemplate.LABEL_70_40)

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
        fun getSetting():LabelPrintSetting{
            val para = JSONObject()
            if (!SQLiteHelper.getLocalParameter("b_order_print", para)){
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
            if (!SQLiteHelper.saveLocalParameter("b_order_print", JSONObject.toJSON(this) as? JSONObject, "业务单据打印参数", err)){
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
}