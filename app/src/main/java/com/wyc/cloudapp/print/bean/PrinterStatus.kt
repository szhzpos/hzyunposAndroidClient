package com.wyc.cloudapp.print.bean

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.constants.MessageID
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.dialog.MyDialog
import java.io.Serializable

data class PrinterStatus(private var value:Int,private var msg:String?):Serializable{
    constructor() : this(0,"")

    fun isOpen():Boolean{
        return value == OPEN
    }
    fun isClose():Boolean{
        return value == CLOSE
    }
    fun isError():Boolean{
        return value == ERROR
    }
    fun setValue(v: Int){
        value = v;
    }
    fun getValue():Int{
        return value
    }
    fun setMsg(sz: String){
        msg = sz
    }
    fun getMsg():String{
        return msg ?: ""
    }
    companion object{
        const val OPEN = 0
        const val CLOSE = 1
        const val ERROR = 2
        @JvmStatic
        fun savePrinterStatus(value: Int, msg: String) {
            val err = StringBuilder()
            val printerStatus = PrinterStatus(value, msg)
            if (!SQLiteHelper.saveLocalParameter("print_s", JSON.toJSON(printerStatus) as JSONObject, "打印开关", err)) {
                MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.save_print_param_error, err))
            }
        }
        @JvmStatic
        fun setPrinterErrorMsg(msg: String){
            error(msg)
        }
        @JvmStatic
        fun getPrinterStatus(): PrinterStatus {
            val `object` = JSONObject()
            if (SQLiteHelper.getLocalParameter("print_s", `object`)) {
                return `object`.toJavaObject(PrinterStatus::class.java)
            }
            MyDialog.toastMessage(`object`.getString("info"))
            return PrinterStatus(-1, "")
        }
        @JvmStatic
        fun error(msg: String){
            CustomApplication.sendMessage(MessageID.PRINTER_ERROR,msg)
        }
        fun success(){
            CustomApplication.sendMessage(MessageID.PRINTER_SUCCESS)
        }
        @JvmStatic
        fun switchPrintStatus() {
            val status = getPrinterStatus()
            if (status.isError()) {
                MyDialog.toastMessage(status.getMsg())
            } else {
                var value = OPEN
                if (status.isClose()) {
                    MyDialog.toastMessage(R.string.print_close_hint)
                } else if (status.isOpen()) {
                    value = CLOSE
                    MyDialog.toastMessage(R.string.print_open_hint)
                }
                savePrinterStatus(value, "")
            }
        }
        @JvmStatic
        fun printerIsNotClose(): Boolean {
            return !getPrinterStatus().isClose()
        }
    }
}
