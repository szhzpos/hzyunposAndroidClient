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
 * @ClassName:      VipStoreStuff
 * @Description:    会员存货
 * @Author:         wyc
 * @CreateDate:     2021-11-17 11:51
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-11-17 11:51
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class VipStoreStuffFragment: AbstractBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                Text(text = "8888888888888888888")
            }
        }
    }

    override fun getTitle(): String {
        return CustomApplication.self().getString(R.string.store_stuff)
    }

    override fun viewCreated() {

    }
}