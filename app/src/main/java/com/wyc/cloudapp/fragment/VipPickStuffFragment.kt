package com.wyc.cloudapp.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.Text
import androidx.compose.ui.platform.ComposeView
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.fragment
 * @ClassName:      VipPickStuffFragment
 * @Description:    会员取货
 * @Author:         wyc
 * @CreateDate:     2021-11-17 15:09
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-11-17 15:09
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class VipPickStuffFragment: AbstractBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Text(text = "7777777777777777")
            }
        }
    }

    override fun getTitle(): String {
        return CustomApplication.self().getString(R.string.pick_stuff)
    }

    override fun viewCreated() {

    }
}