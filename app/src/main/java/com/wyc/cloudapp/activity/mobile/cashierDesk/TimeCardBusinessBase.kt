package com.wyc.cloudapp.activity.mobile.cashierDesk

import android.os.Bundle
import com.wyc.cloudapp.R
import com.wyc.cloudapp.adapter.FragmentPagerAdapter
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.mobileFragemt.AbstractMobileFragment

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.activity.mobile.cashierDesk
 * @ClassName:      CardSaleBusinessBase
 * @Description:    次卡销售基类，用于统一判断练习收银模式
 * @Author:         wyc
 * @CreateDate:     2021-10-27 9:35
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-10-27 9:35
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
abstract class TimeCardBusinessBase: FragmentContainerActivity<AbstractMobileFragment>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (CustomApplication.isPracticeMode()){
            MyDialog.toastMessage(getString(R.string.not_enter_practice))
            finish()
            return
        }
    }
    override fun hookEnterKey(): Boolean {
        return (view_pager.adapter as FragmentPagerAdapter<*>?)!!.getItem(_tab.selectedTabPosition).hookEnterKey()
    }
}