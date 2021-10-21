package com.wyc.cloudapp.activity.normal

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.Nullable
import androidx.viewpager2.widget.ViewPager2
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity
import com.wyc.cloudapp.adapter.FragmentPagerAdapter
import com.wyc.cloudapp.fragment.NTimeCardSaleFragment
import com.wyc.cloudapp.mobileFragemt.*
import com.wyc.cloudapp.print.Printer
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
class NTimeCardBusiness:AbstractMobileActivity() {
    @BindView(R.id._fragment_tab)
    lateinit var _tab: TabLayout
    @BindView(R.id.view_pager)
    lateinit var view_pager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMiddleText(getString(R.string.time_card_business))
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        ButterKnife.bind(this)
        init()
    }

    private fun init() {
        val fragments: MutableList<AbstractMobileFragment> = ArrayList()
        fragments.add(if (lessThan7Inches()) TimeCardSaleFragment() else NTimeCardSaleFragment())
        fragments.add(TimeCardUseFragment())
        fragments.add(TimeCardSaleQueryFragment())
        fragments.add(TimeCardUseQueryFragment())
        val adapter = FragmentPagerAdapter(fragments, this)
        view_pager.adapter = adapter
        TabLayoutMediator(_tab, view_pager) { tab: TabLayout.Tab, position: Int -> tab.text = adapter.getItem(position).title }.attach()
    }

    override fun getContentLayoutId(): Int {
        return R.layout.normal_fragment_pager_container
    }

    override fun hookEnterKey(): Boolean {
        return (view_pager.adapter as FragmentPagerAdapter<*>).getItem(_tab.selectedTabPosition).hookEnterKey()
    }

    companion object{
        @JvmStatic
        fun start(context: Activity){
            context.startActivity(Intent(context, NTimeCardBusiness::class.java))
        }
        @JvmStatic
        fun verifyTimeCardPermissions(@Nullable context: MainActivity): Boolean {
            return context.verifyPermissions("27", null, false)
        }
    }
}