package com.wyc.cloudapp.print.printer

import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.print.Printer
import com.wyc.cloudapp.print.bean.PrinterStatus
import com.wyc.cloudapp.print.parameter.IParameter
import com.wyc.cloudapp.print.receipts.GiftCardReceipts
import com.wyc.cloudapp.print.receipts.IReceipts
import com.wyc.cloudapp.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
abstract class AbstractPrinter : IPrinter{

    protected fun showError(msg: String?) {
        if (Utils.isNotEmpty(msg)) {
            CoroutineScope(Dispatchers.IO).launch {
                PrinterStatus.setPrinterErrorMsg(msg!!)
                MyDialog.toastMessage(msg)
            }
        }
    }
    protected fun printSuccess(){
        CoroutineScope(Dispatchers.IO).launch {
            if (PrinterStatus.getPrinterStatus().isError()){
                PrinterStatus.success()
            }
        }
    }
    companion object{
        @Volatile
        private var sPrinter: IPrinter? = null

        @JvmStatic
        fun printContent(receipts: IReceipts<out IParameter>) {
            if (PrinterStatus.printerIsNotClose()){
                CoroutineScope(Dispatchers.IO).launch {
                    getInstance()?.apply {
                        printObj(receipts)
                    }
                }
            }else MyDialog.toastMessage("打印功能已关闭！")
        }
        @JvmStatic
        fun openCashDrawer(){
            CoroutineScope(Dispatchers.IO).launch {
                getInstance()?.openCashBox()
            }
        }
        @JvmStatic
        private fun getInstance():IPrinter?{
            val printerParam = JSONObject()
            if (hasPrinter(printerParam)) {
                val cls_id = Utils.getNullStringAsEmpty(printerParam, "cls_id")
                try {
                    if (sPrinter == null){
                        synchronized(AbstractPrinter::class){
                            if (sPrinter == null){
                                val printerClass = Class.forName("com.wyc.cloudapp.print.printer.$cls_id")
                                sPrinter = if (ToledoPrinter::class.java.simpleName.equals(cls_id)){
                                    printerClass.getMethod("getInstance").invoke(null) as IPrinter
                                }else{
                                    printerClass.getConstructor().newInstance() as IPrinter
                                }
                            }
                        }
                    }
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

           return sPrinter
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
        @JvmStatic
        fun clearResource(){
            synchronized(AbstractPrinter::class){
                sPrinter?.apply {
                    clear()
                    sPrinter = null
                }
            }
        }
        @JvmStatic
        fun resetPrinter(printerCls:String?){
            synchronized(AbstractPrinter::class){
                sPrinter?.apply {
                    if (!javaClass.simpleName.equals(printerCls)){
                        clearResource();
                    }
                }
            }
        }
    }

    protected fun finalize(){
        Logger.d("%s has finalized",javaClass.simpleName)
    }
}