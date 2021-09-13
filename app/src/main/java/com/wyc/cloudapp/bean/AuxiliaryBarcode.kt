package com.wyc.cloudapp.bean

import com.wyc.cloudapp.adapter.business.AbstractActionAdapter
import com.wyc.cloudapp.data.SQLiteHelper
/*
* 辅助条码
* */
@SQLiteHelper.TableName("auxiliary_barcode_info")
data class AuxiliaryBarcode(var id:Int = 0, var g_m_id:Int = 0, @SQLiteHelper.Where(index = 0) var barcode_id:String = "",
                            var fuzhu_barcode:String = "", var status:Int = 0):AbstractActionAdapter.Action{

    constructor():this(0)
    constructor(flag:Boolean):this(0){
        plus = flag
    }
    @SQLiteHelper.Ignore
    override var plus: Boolean = false

}
