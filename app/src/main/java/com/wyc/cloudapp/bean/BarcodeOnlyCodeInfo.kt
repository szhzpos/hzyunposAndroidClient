package com.wyc.cloudapp.bean

import java.io.Serializable

data class BarcodeOnlyCodeInfo(var only_coding: String = "", var barcode: String = ""):Serializable{
    constructor() : this("")
}
