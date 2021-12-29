package com.wyc.cloudapp.print.printer

import com.wyc.cloudapp.print.receipts.IReceipts

interface IPrinter {
    fun printObj(receipts:IReceipts)
}