package com.wyc.cloudapp.activity.normal

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.Nullable
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity
import com.wyc.cloudapp.activity.mobile.cashierDesk.TimeCardBusinessBase
import com.wyc.cloudapp.fragment.NTimeCardSaleFragment
import com.wyc.cloudapp.mobileFragemt.*
import java.util.*

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.activity.normal
 * @ClassName:      NTimeCardBusiness
 * @Description:    宽屏端次卡业务管理
 * @Author:         wyc
 * @CreateDate:     2021-10-19 10:56
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-10-19 10:56
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class NTimeCardBusiness: TimeCardBusinessBase() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMiddleText(getString(R.string.time_card_business))
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
    }

    override fun createFragments(): MutableList<AbstractMobileFragment> {
        val fragments: MutableList<AbstractMobileFragment> = ArrayList()
        fragments.add(if (lessThan7Inches()) TimeCardSaleFragment() else NTimeCardSaleFragment())
        fragments.add(TimeCardUseFragment())
        fragments.add(TimeCardSaleQueryFragment())
        fragments.add(TimeCardUseQueryFragment())
        return fragments
    }

    override fun getContentLayoutId(): Int {
        return R.layout.normal_fragment_pager_container
    }

    companion object{
        @JvmStatic
        fun start(context: Activity){
            context.startActivity(Intent(context, NTimeCardBusiness::class.java))
        }
        @JvmStatic
        fun verifyTimeCardPermissions(@Nullable context: MainActivity): Boolean {
            return context.verifyPermissions(context.getString(R.string.time_card_per_id), null, false)
        }
    }
}