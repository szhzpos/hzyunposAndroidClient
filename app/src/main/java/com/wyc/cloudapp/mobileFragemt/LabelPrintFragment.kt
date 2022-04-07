package com.wyc.cloudapp.mobileFragemt

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import butterknife.OnClick
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity
import com.wyc.cloudapp.activity.mobile.MobileSetupActivity
import com.wyc.cloudapp.design.LabelDesignActivity
import com.wyc.cloudapp.design.LabelPrintSetting
import com.wyc.cloudapp.bean.TreeListItem
import com.wyc.cloudapp.data.room.AppDatabase
import com.wyc.cloudapp.databinding.LabelPrintSettingBinding
import com.wyc.cloudapp.design.BrowseLabelActivity
import com.wyc.cloudapp.design.LabelTemplate
import com.wyc.cloudapp.dialog.CustomProgressDialog
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.dialog.tree.TreeListDialogForObj
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.BluetoothUtils
import java.lang.NumberFormatException


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.mobileFragemt
 * @ClassName:      LabelPrintFragment
 * @Description:    标签打印设置
 * @Author:         wyc
 * @CreateDate:     2022/3/25 16:34
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/3/25 16:34
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class LabelPrintFragment: AbstractMobileFragment() {
    private var mProgressDialog: CustomProgressDialog? = null
    private var mBluetoothDevices:MutableList<TreeListItem>? = null
    private var mLabelTemplateSelector: ActivityResultLauncher<Intent>? = null
    override fun viewCreated() {
        if (mContext is MobileSetupActivity){
            val abs = mContext as MobileSetupActivity
            abs.setRightTitle(getString(R.string.save_sz)) { DataBindingUtil.bind<LabelPrintSettingBinding>(rootView)?.setting?.saveSetting() }
        }
        initParam()

        registerGoodsCallback()
    }
    private fun initParam(){
        val setting = LabelPrintSetting.getSetting()
        DataBindingUtil.bind<LabelPrintSettingBinding>(rootView)?.setting = setting
        BluetoothUtils.bondBlueTooth(setting.getPrinterAddress())
    }

    private fun registerGoodsCallback(){
        mLabelTemplateSelector = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AbstractDefinedTitleActivity.RESULT_OK){
                it.data?.getParcelableExtra<LabelTemplate>(BrowseLabelActivity.LABEL_KEY)?.apply {
                    findViewById<TextView>(R.id.template_tv)?.text = templateName
                    val setting: LabelPrintSetting? = DataBindingUtil.bind<LabelPrintSettingBinding>(rootView)?.setting
                    try {
                        setting?.labelTemplateId = templateId
                    }catch (e:NumberFormatException){
                        e.printStackTrace()
                    }
                    setting?.labelTemplateName = templateName
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        BluetoothUtils.attachReceiver(context,receiver)
    }
    override fun onPause() {
        super.onPause()
        BluetoothUtils.stopBlueToothDiscovery()
    }

    override fun onDetach() {
        super.onDetach()
        BluetoothUtils.detachReceiver(requireContext(),receiver)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == BluetoothUtils.REQUEST_BLUETOOTH_ENABLE) {
            BluetoothUtils.startBlueToothDiscovery(this)
        }
    }

    override fun onBackPressed(): Boolean {
        if (DataBindingUtil.bind<LabelPrintSettingBinding>(rootView)?.setting?.hasChange() == true){
            DataBindingUtil.bind<LabelPrintSettingBinding>(rootView)?.setting?.saveSetting()
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
                    val bluetoothDevice_found: BluetoothDevice? = intent.getParcelableExtra(
                        BluetoothDevice.EXTRA_DEVICE)
                    bluetoothDevice_found?.let {
                        val device_style = it.bluetoothClass.majorDeviceClass
                        if (device_style == BluetoothClass.Device.Major.IMAGING || device_style == BluetoothClass.Device.Major.MISC) {
                            val address = it.address
                            if (mBluetoothDevices == null)mBluetoothDevices = mutableListOf()
                            try {
                                mBluetoothDevices!!.first {
                                    it.item_id == address
                                }
                            }catch (e: NoSuchElementException){
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
                DataBindingUtil.bind<LabelPrintSettingBinding>(rootView)?.run {
                    if (it.size == 1){
                        setting?.printer =
                            LabelPrintSetting.combinationPrinter(it[0].item_id,it[0].item_name)
                    }else{
                        val treeListDialog = TreeListDialogForObj(mContext, mContext.getString(R.string.printer))
                        treeListDialog.setData(mBluetoothDevices, null, true)
                        if (treeListDialog.exec() == 1) {
                            val obj = treeListDialog.singleContent
                            setting?.printer = LabelPrintSetting.combinationPrinter(obj.item_id,obj.item_name)
                        }
                    }
                    invalidateAll()
                }
            }
        }
    }

    @OnClick(R.id.template_tv)
    fun spec() {
        mLabelTemplateSelector?.launch(Intent(requireContext(),BrowseLabelActivity::class.java))
    }

    @OnClick(R.id.rotate_tv)
    fun rotate(view: View){
        val treeListDialog = TreeListDialogForObj(mContext, mContext.getString(R.string.paper_spec))
        treeListDialog.setData(convertRotate(), null, true)
        if (treeListDialog.exec() == 1) {
            val obj = treeListDialog.singleContent
            (view as TextView).text = obj.item_name

            val setting: LabelPrintSetting? = DataBindingUtil.bind<LabelPrintSettingBinding>(rootView)?.setting
            setting?.rotate = LabelPrintSetting.Rotate.valueOf(obj.item_id)
        }
    }
    private fun convertRotate(): List<TreeListItem> {
        val data: MutableList<TreeListItem> = ArrayList()
        val  values: Array<LabelPrintSetting.Rotate> = LabelPrintSetting.Rotate.values()
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
        val bind = DataBindingUtil.bind<LabelPrintSettingBinding>(rootView)
        val setting = bind?.setting
        val num = setting?.printNum?:0
        setting?.printNum = num - i
        bind?.invalidateAll()
    }

    @OnClick(R.id.plusX, R.id.minusX)
    fun offsetX(view: View){
        var i = -1
        if (view.id == R.id.minusX){
            i = 1
        }
        val bind = DataBindingUtil.bind<LabelPrintSettingBinding>(rootView)
        val setting = bind?.setting
        val num = setting?.offsetX?:0
        setting?.offsetX = num - i
        bind?.invalidateAll()
    }

    @OnClick(R.id.plusY, R.id.minusY)
    fun offsetY(view: View){
        var i = -1
        if (view.id == R.id.minusY){
            i = 1
        }
        val bind = DataBindingUtil.bind<LabelPrintSettingBinding>(rootView)
        val setting = bind?.setting
        val num = setting?.offsetY?:0
        setting?.offsetY = num - i
        bind?.invalidateAll()
    }

    @OnClick(R.id.printer_tv)
    fun printer(){
        XXPermissions.with(this)
            .permission(Manifest.permission.ACCESS_FINE_LOCATION)
            .request(object : OnPermissionCallback {
                override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                    BluetoothUtils.startBlueToothDiscovery(this@LabelPrintFragment)
                }

                override fun onDenied(permissions: MutableList<String>?, never: Boolean) {
                    if (never) {
                        MyDialog.toastMessage("....")
                    }
                }
            })
    }

    @OnClick(R.id.print_template_tv)
    fun templateDesign(){
        LabelDesignActivity.start(mContext)
    }

    override fun getRootLayout(): Int {
        return R.layout.label_print_setting
    }
}