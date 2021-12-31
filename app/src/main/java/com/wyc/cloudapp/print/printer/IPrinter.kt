package com.wyc.cloudapp.print.printer

import com.wyc.cloudapp.print.receipts.IReceipts
import java.io.Serializable

interface IPrinter<T>:Serializable {
    fun printObj(receipts:IReceipts<T>)
    fun openCashBox();
}