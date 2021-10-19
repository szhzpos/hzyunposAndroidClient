package com.wyc.cloudapp.activity.base

import android.os.Bundle
import android.view.WindowManager
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.activity
 * @ClassName:      BaseWindowActivity
 * @Description:    窗口模式activity
 * @Author:         wyc
 * @CreateDate:     2021-09-23 9:40
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-09-23 9:40
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
abstract class BaseWindowActivity: AbstractMobileActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val attributes = window.attributes
        attributes.dimAmount = 0.5f
        attributes.x = 0
        attributes.y = 0
        attributes.width = with()
        attributes.height = height()
        window.attributes = attributes
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }
    open fun with():Int{
        return WindowManager.LayoutParams.MATCH_PARENT
    }
    open fun height():Int{
        return WindowManager.LayoutParams.WRAP_CONTENT
    }
}