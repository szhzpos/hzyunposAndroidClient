package com.wyc.cloudapp.activity.base

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import butterknife.ButterKnife
import com.wyc.cloudapp.R
import com.wyc.cloudapp.databinding.BindingActivityBinding

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.activity
 * @ClassName:      AbsBindingActivity
 * @Description:    activity界面绑定数据基类
 * @Author:         wyc
 * @CreateDate:     2021-07-28 14:19
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-07-28 14:19
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
abstract class AbsBindingActivity: MainActivity(), ITitle {
    private var mContentView:View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView()
        ButterKnife.bind(this)
    }
    private fun setContentView(){
        val rootBinding:BindingActivityBinding = DataBindingUtil.setContentView(this, R.layout.binding_activity)
        rootBinding.setTitle(this)
        mContentView = View.inflate(this, getBindingLayoutId(), null)
        if (null == mContentView){
            Log.e(localClassName, "mContentView is null...")
        }else
            findViewById<LinearLayout>(R.id._main)?.addView(mContentView, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    protected  fun <T : ViewDataBinding> getBindingData():T? {
        return mContentView?.let { DataBindingUtil.bind(it) }
    }

    protected abstract fun getBindingLayoutId(): Int

    override fun getLeftText(): String {
        return getString(R.string.back)
    }

    override fun getMiddleText(): String {
        return intent.getStringExtra(AbstractDefinedTitleActivity.TITLE_KEY) ?: ""
    }

    override fun getRightText(): String {
         return ""
    }

    override fun onLeftClick(view: View) {
        onBackPressed()
    }

    override fun onMiddleClick(view: View) {

    }

    override fun onRightClick(view: View) {

    }
}