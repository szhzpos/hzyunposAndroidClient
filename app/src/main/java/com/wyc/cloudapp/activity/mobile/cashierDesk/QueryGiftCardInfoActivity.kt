package com.wyc.cloudapp.activity.mobile.cashierDesk

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity
import com.wyc.cloudapp.activity.mobile.cashierDesk.AbstractSelectActivity.SELECT_ITEM

class QueryGiftCardInfoActivity : AbstractMobileActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
 
    }

    override fun getContentLayoutId(): Int {
        return R.layout.activity_query_gift_card_info
    }
    companion object{
        @JvmStatic
        fun start(context:Activity){
            context.startActivityForResult(Intent(context,QueryGiftCardInfoActivity::class.java),SELECT_ITEM)
        }
    }
}