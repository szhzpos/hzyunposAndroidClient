package com.wyc.cloudapp.print.printer

import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.print.Printer
import com.wyc.cloudapp.print.receipts.IReceipts
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
abstract class AbstractPrinter: IPrinter {
    companion object{
        @JvmStatic
        fun printContent(receipts: IReceipts) {
            val printerParam = JSONObject()
            if (hasPrinter(printerParam)) {
                val cls_id = Utils.getNullStringAsEmpty(printerParam, "cls_id")
                try {
                    val printerClass = Class.forName("com.wyc.cloudapp.print.printer.$cls_id")
                    val constructor = printerClass.getConstructor()
                    (constructor.newInstance() as AbstractPrinter).printObj(receipts)
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

    protected fun finalize(){
        Logger.d("%s has finalized",javaClass.simpleName)
    }
}