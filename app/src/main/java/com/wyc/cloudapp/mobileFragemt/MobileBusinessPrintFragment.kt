package com.wyc.cloudapp.mobileFragemt

import android.Manifest
import android.animation.Animator
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import butterknife.BindView
import butterknife.OnClick
import com.alibaba.fastjson.JSONObject
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.mobile.MobileSetupActivity
import com.wyc.cloudapp.bean.BusinessOrderPrintSetting
import com.wyc.cloudapp.bean.TreeListItem
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.databinding.MoblieBusinessPrintSettingBinding
import com.wyc.cloudapp.dialog.CustomProgressDialog
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.dialog.tree.TreeListDialogForObj
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.BluetoothUtils
import java.util.*
import kotlin.collections.ArrayList


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
    @BindView(R.id.format_container)
    lateinit var mFormatContainer:ConstraintLayout
    private var mProgressDialog: CustomProgressDialog? = null
    private var mBluetoothDevices:MutableList<TreeListItem>? = null
    override fun viewCreated() {
        if (mContext is MobileSetupActivity){
            val abs = mContext as MobileSetupActivity
            abs.setRightTitle(getString(R.string.save_sz)) { DataBindingUtil.bind<MoblieBusinessPrintSettingBinding>(rootView)?.setting?.saveSetting() }
        }
        initParam()
    }
    private fun initParam(){
        val setting = BusinessOrderPrintSetting.getSetting()
        DataBindingUtil.bind<MoblieBusinessPrintSettingBinding>(rootView)?.setting = setting
        BluetoothUtils.bondBlueTooth(setting.getPrinterAddress())
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND)
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        context.registerReceiver(receiver, intentFilter)
    }
    override fun onPause() {
        super.onPause()
        BluetoothUtils.stopBlueToothDiscovery()
    }

    override fun onDetach() {
        super.onDetach()
        context?.unregisterReceiver(receiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == BluetoothUtils.REQUEST_BLUETOOTH_ENABLE) {
            BluetoothUtils.startBlueToothDiscovery(this)
        }
    }

    override fun onBackPressed(): Boolean {
        if (DataBindingUtil.bind<MoblieBusinessPrintSettingBinding>(rootView)?.setting?.isChange() == true){
            if (MyDialog.showMessageToModalDialog(mContext,"是否保存?") == 1){
                DataBindingUtil.bind<MoblieBusinessPrintSettingBinding>(rootView)?.setting?.saveSetting()
            }
        }
        return super.onBackPressed()
    }

    private fun showProgress(sz:String){
        if (mProgressDialog == null)
            mProgressDialog = CustomProgressDialog.showProgress(context,sz).setCancel(true)
        else
            mProgressDialog!!.setMessage(sz).refreshMessage().show()
    }
    private fun dismissProgress(){
        mProgressDialog?.dismiss()
    }

    private val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when(intent?.action){
                BluetoothDevice.ACTION_FOUND -> {
                    val bluetoothDevice_found:BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    bluetoothDevice_found?.let {
                        val device_style = it.bluetoothClass.majorDeviceClass
                        if (device_style == BluetoothClass.Device.Major.IMAGING || device_style == BluetoothClass.Device.Major.MISC) {
                            val address = it.address
                            if (mBluetoothDevices == null)mBluetoothDevices = mutableListOf()
                            try {
                                mBluetoothDevices!!.first {
                                    it.item_id == address
                                }
                            }catch (e:NoSuchElementException){
                                mBluetoothDevices!!.add(TreeListItem(address,it.name))
                            }
                        }
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    showProgress(getString(R.string.searching_bluetooth))
                    mProgressDialog?.setOnCancelListener {
                        BluetoothUtils.stopBlueToothDiscovery()
                    }
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {

                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    dismissProgress()
                    setPrinter()
                }
            }
        }
    }
    private fun setPrinter(){
        mBluetoothDevices?.let {
            if (it.isNotEmpty()){
                DataBindingUtil.bind<MoblieBusinessPrintSettingBinding>(rootView)?.run {
                    if (it.size == 1){
                        setting?.printer =BusinessOrderPrintSetting.combinationPrinter(it[0].item_id,it[0].item_name)
                    }else{
                        val treeListDialog = TreeListDialogForObj(mContext, mContext.getString(R.string.printer))
                        treeListDialog.setData(mBluetoothDevices, null, true)
                        if (treeListDialog.exec() == 1) {
                            val obj = treeListDialog.singleContent
                            setting?.printer = BusinessOrderPrintSetting.combinationPrinter(obj.item_id,obj.item_name)
                        }
                    }
                    invalidateAll()
                }
            }
        }
    }

    @OnClick(R.id.print_template_tv)
    fun template() {
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
    fun spec(view: View){
        val treeListDialog = TreeListDialogForObj(mContext, mContext.getString(R.string.paper_spec))
        treeListDialog.setData(convertSpec(), null, true)
        if (treeListDialog.exec() == 1) {
            val obj = treeListDialog.singleContent
            (view as TextView).text = obj.item_name

            val setting:BusinessOrderPrintSetting? = DataBindingUtil.bind<MoblieBusinessPrintSettingBinding>(rootView)?.setting
            setting?.spec = BusinessOrderPrintSetting.Spec.valueOf(obj.item_id)
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
    fun type(view: View){
        val treeListDialog = TreeListDialogForObj(mContext, mContext.getString(R.string.print_type))
        treeListDialog.setData(convertType(), null, true)
        if (treeListDialog.exec() == 1) {
            val obj = treeListDialog.singleContent
            if (!obj.isEmpty){
                (view as TextView).text = obj.item_name

                val setting:BusinessOrderPrintSetting? = DataBindingUtil.bind<MoblieBusinessPrintSettingBinding>(rootView)?.setting
                setting?.type = BusinessOrderPrintSetting.Type.valueOf(obj.item_id)
            }
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

    @OnClick(R.id.plus, R.id.minus)
    fun action(view: View){
        var i = 1
        if (view.id == R.id.plus){
            i = -1
        }
        val bind = DataBindingUtil.bind<MoblieBusinessPrintSettingBinding>(rootView)
        val setting = bind?.setting
        val num = setting?.print_num?:0
        setting?.print_num = num - i
        bind?.invalidateAll()
    }

    @OnClick(R.id.printer_tv)
    fun printer(){
        XXPermissions.with(this)
                .permission(Manifest.permission.ACCESS_FINE_LOCATION)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                        BluetoothUtils.startBlueToothDiscovery(this@MobileBusinessPrintFragment)
                    }

                    override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                        if (never) {
                            MyDialog.toastMessage("....")
                        }
                    }
                })
    }

    override fun getRootLayout(): Int {
         return R.layout.moblie_business_print_setting
    }
}