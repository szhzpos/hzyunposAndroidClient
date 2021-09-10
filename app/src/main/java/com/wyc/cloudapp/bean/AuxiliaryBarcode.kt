package com.wyc.cloudapp.bean

import com.wyc.cloudapp.data.SQLiteHelper

@SQLiteHelper.TableName("auxiliary_barcode_info")
data class AuxiliaryBarcode(var id:Int = 0,var g_m_id:Int = 0,var barcode_id:String = "",var fuzhu_barcode:String = "",var status:Int = 0){
    constructor():this(0)
}
