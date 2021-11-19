package com.wyc.cloudapp.bean

import com.alibaba.fastjson.JSONObject
import com.alibaba.fastjson.annotation.JSONField
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.dialog.MyDialog
import java.io.Serializable
import kotlin.properties.Delegates

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.bean
 * @ClassName:      BusinessOrderPrintSetting
 * @Description:    单据打印设置参数
 * @Author:         wyc
 * @CreateDate:     2021-08-17 16:02
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-08-17 16:02
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class BusinessOrderPrintSetting:Serializable {
    @JSONField(serialize = false)
    private var change = false
    enum class Way(s: String)  {
        BLUETOOTH_PRINT(CustomApplication.self().getString(R.string.bluetooth_way));
        val description:String = s
    }

    enum class Spec(s:String){
        SPEC_58(CustomApplication.self().getString(R.string.spec_58)),SPEC_76(CustomApplication.self().getString(R.string.spec_76)),SPEC_80(CustomApplication.self().getString(R.string.spec_80));
        val description:String = s
    }

    enum class Type(s:String){
        ASK(CustomApplication.self().getString(R.string.ask_print_hints)),AUTO(CustomApplication.self().getString(R.string.auto_print)),FORBID(CustomApplication.self().getString(R.string.forbid_print));
        val description:String = s
    }
    enum class Format(s: String){
        DEFAULT(CustomApplication.self().getString(R.string.default_template_format));
        val description:String = s
    }

    var way:Way by change(Way.BLUETOOTH_PRINT)

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
        fun getSetting():BusinessOrderPrintSetting{
            val para = JSONObject()
            if (!SQLiteHelper.getLocalParameter("b_order_print", para)){
                MyDialog.toastMessage(para.getString("info"))
                return BusinessOrderPrintSetting()
            }
            val setting = JSONObject.parseObject(para.toString(), BusinessOrderPrintSetting::class.java)?: BusinessOrderPrintSetting()
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

    var spec:Spec by change(Spec.SPEC_58)

    var type:Type by change(Type.ASK)

    var print_num by change(1)

    override fun toString(): String {
        return "BusinessOrderPrintSetting(way=${way.description}, printer='$printer', spec=${spec.description}, type=${type.description}, print_num=$print_num)"
    }

    private fun <T> change(iv:T) = Delegates.observable(iv, { _, oldValue, newValue ->
        if (oldValue != newValue)change = true
    })

    fun isChange():Boolean{
        return change
    }
 }