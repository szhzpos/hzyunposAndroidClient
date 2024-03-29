package com.wyc.cloudapp.print.cashDrawer

import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.fragment.PeripheralSetting
import com.wyc.cloudapp.print.printer.AbstractPrinter
import com.wyc.cloudapp.utils.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.print.cashDrawer
 * @ClassName:      UtilsIOpenCashDrawer
 * @Description:    开钱箱工具
 * @Author:         wyc
 * @CreateDate:     2022/3/11 16:20
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/3/11 16:20
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class UtilsOpenCashDrawer {
    companion object{
        @JvmStatic
        fun open(){
            CoroutineScope(Dispatchers.IO).launch {
                val obj = JSONObject()
                if (PeripheralSetting.loadCashboxSetting(obj)){
                    val type = if (obj.isEmpty()){
                        PeripheralSetting.connPrinter
                    }else Utils.getNullStringAsEmpty(obj,"c_box")
                    if (type == PeripheralSetting.connPrinter){
                        AbstractPrinter.openCashDrawer()
                    }
                }
            }
        }
    }
}