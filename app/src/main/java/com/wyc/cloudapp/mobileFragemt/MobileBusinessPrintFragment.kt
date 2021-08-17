package com.wyc.cloudapp.mobileFragemt

import android.animation.Animator
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import butterknife.BindView
import butterknife.OnClick
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.bean.BusinessOrderPrintSetting
import com.wyc.cloudapp.bean.TreeListItem
import com.wyc.cloudapp.databinding.MoblieBusinessPrintSettingBinding
import com.wyc.cloudapp.dialog.tree.TreeListDialogForObj
import com.wyc.cloudapp.logger.Logger
import java.util.*


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.mobileFragemt
 * @ClassName:      MobileBusinessPrintFragment
 * @Description:    业务单据打印设置
 * @Author:         wyc
 * @CreateDate:     2021-08-17 10:48
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-08-17 10:48
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class MobileBusinessPrintFragment: AbstractMobileFragment() {
    private lateinit var mSetting: BusinessOrderPrintSetting
    @BindView(R.id.format_container)
    lateinit var mFormatContainer:ConstraintLayout
    override fun viewCreated() {
        mSetting = getSetting()
        DataBindingUtil.bind<MoblieBusinessPrintSettingBinding>(rootView)?.setting = mSetting
    }

    private fun getSetting():BusinessOrderPrintSetting{
        val setting: BusinessOrderPrintSetting? = null
        return setting?: BusinessOrderPrintSetting()
    }

    @OnClick(R.id.print_template_tv)
    fun click() {
        if (mFormatContainer.visibility == View.GONE){
            mFormatContainer.visibility = View.VISIBLE
            mFormatContainer.animate().setListener(null).cancel()
            mFormatContainer.animate().alpha(1f).setDuration(300).start()
        }else{
            mFormatContainer.animate().setListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) {
                }

                override fun onAnimationEnd(animation: Animator?) {
                    mFormatContainer.visibility = View.GONE
                }

                override fun onAnimationCancel(animation: Animator?) {
                    mFormatContainer.visibility = View.GONE
                }

                override fun onAnimationRepeat(animation: Animator?) {
                }
            }).alpha(0f).setDuration(300).start()
        }
    }

    @OnClick(R.id.paper_spec_tv)
    fun click1(view: View){
        val treeListDialog = TreeListDialogForObj(mContext, mContext.getString(R.string.paper_spec))
        treeListDialog.setData(convertSpec(), null, true)
        if (treeListDialog.exec() == 1) {
            val obj = treeListDialog.singleContent
            (view as TextView).text = obj.item_name

            val setting:BusinessOrderPrintSetting? = DataBindingUtil.bind<MoblieBusinessPrintSettingBinding>(rootView)?.setting
            setting?.spec = BusinessOrderPrintSetting.Spec.valueOf(obj.item_id)

            Logger.d(setting)
            val o = JSONObject.toJSON(setting)
            Logger.d(o)
            Logger.d(JSONObject.parseObject(o.toString(),BusinessOrderPrintSetting::class.java))
        }
    }
    private fun convertSpec(): List<TreeListItem> {
        val data: MutableList<TreeListItem> = ArrayList()
        val  values: Array<BusinessOrderPrintSetting.Spec> = BusinessOrderPrintSetting.Spec.values()
        values.iterator().forEach {
            val item = TreeListItem()
            item.item_id = it.name
            item.item_name = it.description
            data.add(item)
        }
        return data
    }

    @OnClick(R.id.print_type_tv)
    fun click2(view: View){
        val treeListDialog = TreeListDialogForObj(mContext, mContext.getString(R.string.paper_spec))
        treeListDialog.setData(convertType(), null, true)
        if (treeListDialog.exec() == 1) {
            val obj = treeListDialog.singleContent
            (view as TextView).text = obj.item_name

            val setting:BusinessOrderPrintSetting? = DataBindingUtil.bind<MoblieBusinessPrintSettingBinding>(rootView)?.setting
            setting?.type = BusinessOrderPrintSetting.Type.valueOf(obj.item_id)

            Logger.d(setting)
            val o = JSONObject.toJSON(setting)
            Logger.d(o)
            Logger.d(JSONObject.parseObject(o.toString(),BusinessOrderPrintSetting::class.java))
        }
    }
    private fun convertType(): List<TreeListItem> {
        val data: MutableList<TreeListItem> = ArrayList()
        val  values: Array<BusinessOrderPrintSetting.Type> = BusinessOrderPrintSetting.Type.values()
        values.iterator().forEach {
            val item = TreeListItem()
            item.item_id = it.name
            item.item_name = it.description
            data.add(item)
        }
        return data
    }

    override fun getRootLayout(): Int {
         return R.layout.moblie_business_print_setting
    }
}