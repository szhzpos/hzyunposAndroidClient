package com.wyc.cloudapp.activity.mobile.cashierDesk

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity
import com.wyc.cloudapp.data.room.entity.TimeCardSaleOrder
import com.wyc.cloudapp.dialog.MyDialog

class TimeCardPayActivity : CardPayBaseActivity<TimeCardSaleOrder>() {
    companion object{
        @JvmStatic
        fun start(context: Fragment, order: TimeCardSaleOrder?) {
            requireNotNull(order) { "the second parameter must not be empty..." }
            if (order.getSaleInfo().isEmpty()) {
                MyDialog.toastMessage("次卡销售记录不能为空！")
                return
            }
            context.startActivityForResult(Intent(context.context, TimeCardPayActivity::class.java).putExtra(ORDER_INFO, order)
                    .putExtra(AbstractMobileActivity.TITLE_KEY,context.getString(R.string.once_card_pay)), ONCE_CARD_REQUEST_PAY)
        }
    }
}