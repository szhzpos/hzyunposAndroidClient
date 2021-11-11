package com.wyc.cloudapp.application.syncinstance

class SyncSaleOperator:AbstractSyncBase("sale_operator_info", arrayOf("sales_id","name","sort")
        , "正在同步经办人", "/api/Jingbanren/xlist") {
    init {
        removePosNumForParam()
    }
}