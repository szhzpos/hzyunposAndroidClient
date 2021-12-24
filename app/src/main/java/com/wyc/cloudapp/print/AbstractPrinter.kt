package com.wyc.cloudapp.print

import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.utils.Utils
import java.lang.reflect.InvocationTargetException

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.print
 * @ClassName:      AbstractPrinter
 * @Description:    内置驱动打印基类
 * @Author:         wyc
 * @CreateDate:     2021-12-24 14:00
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-12-24 14:00
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
abstract class AbstractPrinter:IPrinter {
    companion object{
        @JvmStatic
        fun printContent(c:String) {
            val printerParam = JSONObject()
            if (hasPrinter(printerParam)) {
                val cls_id = Utils.getNullStringAsEmpty(printerParam, "cls_id")
                try {
                    val printerClass = Class.forName("com.wyc.cloudapp.print.$cls_id")
                    val constructor = printerClass.getConstructor()
                    MyDialog.toastMessage(CustomApplication.self().getString(R.string.begin_print))
                    (constructor.newInstance() as AbstractPrinter).print(c)
                    MyDialog.toastMessage(CustomApplication.self().getString(R.string.end_print))
                } catch (e: ClassNotFoundException) {
                    e.printStackTrace()
                    MyDialog.toastMessage(e.message)
                } catch (e: NoSuchMethodException) {
                    e.printStackTrace()
                    MyDialog.toastMessage(e.message)
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                    MyDialog.toastMessage(e.message)
                } catch (e: InstantiationException) {
                    e.printStackTrace()
                    MyDialog.toastMessage(e.message)
                } catch (e: InvocationTargetException) {
                    e.printStackTrace()
                    MyDialog.toastMessage(e.message)
                }
            }else MyDialog.toastMessage(printerParam.getString("info"))
        }
        @JvmStatic
        fun hasPrinter(oriObj: JSONObject?): Boolean {
            var obj = oriObj
            if (null == obj) obj = JSONObject()
            return if (Printer.getPrinterSetting(obj)) {
                if ("NONE" != Utils.getNullOrEmptyStringAsDefault(obj, "v", "NONE")) true else{
                    obj["info"] = "打印机未设置"
                    false
                }
            } else false
        }
    }
}