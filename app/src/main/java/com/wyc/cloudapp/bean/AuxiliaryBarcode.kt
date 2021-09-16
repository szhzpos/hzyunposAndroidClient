package com.wyc.cloudapp.bean

import com.alibaba.fastjson.annotation.JSONField
import com.wyc.cloudapp.adapter.business.AbstractActionAdapter
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.utils.Utils

/*
* 辅助条码
* */
@SQLiteHelper.TableName("auxiliary_barcode_info")
data class AuxiliaryBarcode(var id:Int = -1, @JSONField(serialize = false) var g_m_id:Int = -1,
                            @SQLiteHelper.Where(index = 0) @JSONField(serialize = false)var barcode_id:String = "",
                            var fuzhu_barcode:String? = "",@SQLiteHelper.Where(index = 1) var status:Int = 1):AbstractActionAdapter.Action{

    constructor():this(0)
    constructor(flag:Boolean):this(0){
        plus = flag
    }
    @JSONField(serialize = false)
    @SQLiteHelper.Ignore
    override var plus: Boolean = false
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AuxiliaryBarcode

        if (id != other.id) return false
        if (g_m_id != other.g_m_id) return false
        if (barcode_id != other.barcode_id) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + g_m_id
        result = 31 * result + barcode_id.hashCode()
        return result
    }
    fun hasNotNew():Boolean{
        return g_m_id != -1 && Utils.isNotEmpty(barcode_id)
    }

}
