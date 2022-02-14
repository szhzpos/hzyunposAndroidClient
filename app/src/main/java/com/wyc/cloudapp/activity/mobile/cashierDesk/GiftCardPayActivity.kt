package com.wyc.cloudapp.activity.mobile.cashierDesk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity
import com.wyc.cloudapp.data.room.entity.GiftCardSaleOrder
import com.wyc.cloudapp.dialog.MyDialog

class GiftCardPayActivity : CardPayBaseActivity<GiftCardSaleOrder>() {
    override fun getSupportPayMethod(): String {
        return "7"
    }
    companion object{
        @JvmStatic
        fun start(context: Activity, order: GiftCardSaleOrder?) {
            requireNotNull(order) { "the second parameter must not be empty..." }
            if (order.getSaleInfo().isEmpty()) {
                MyDialog.toastMessage("购物卡销售记录不能为空！")
                return
            }
            context.startActivityForResult(Intent(context, GiftCardPayActivity::class.java).putExtra(ORDER_INFO, order)
                    .putExtra(AbstractDefinedTitleActivity.TITLE_KEY,context.getString(R.string.gift_card_pay)), ONCE_CARD_REQUEST_PAY)
        }
    }
}