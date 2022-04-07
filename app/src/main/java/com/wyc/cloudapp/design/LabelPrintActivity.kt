package com.wyc.cloudapp.design

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.ReplacementTransformationMethod
import android.view.ContextMenu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.zxing.client.android.CaptureActivity
import com.gprinter.bean.PrinterDevices
import com.gprinter.utils.CallbackListener
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity
import com.wyc.cloudapp.activity.mobile.business.MobileSelectGoodsActivity
import com.wyc.cloudapp.adapter.LabelGoodsAdapter
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.constants.ScanCallbackCode
import com.wyc.cloudapp.customizationView.TopDrawableTextView
import com.wyc.cloudapp.data.room.AppDatabase
import com.wyc.cloudapp.decoration.LinearItemDecoration
import com.wyc.cloudapp.decoration.SuperItemDecoration
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.Utils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

class LabelPrintActivity : AbstractDefinedTitleActivity() , CallbackListener {
    private var mSelectGoodsLauncher: ActivityResultLauncher<Intent>? = null
    private var mLabelGoodsAdapter:LabelGoodsAdapter? = null
    private var mLabelView:LabelView? = null
    private var mPrintItem:List<ItemBase>? = null
    private var mConnecting = false
    private var mSearch:EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)

        setMiddleText(getString(R.string.label_print))

        registerGoodsCallback()

        initSearchContent()
        initLabelView()
        initAdapter()
        initPrint()
        initClear()
        connPrinter()
    }
    private fun initClear(){
        setRightText(getString(R.string.clear_sz))
        setRightListener {
            if (mLabelGoodsAdapter?.isEmpty != true){
                MyDialog.displayAskMessage(this,getString(R.string.clear_goods_hints), { myDialog ->
                    mLabelGoodsAdapter?.clear()
                    myDialog.dismiss()
                }) { myDialog -> myDialog.dismiss() }
            }
        }
    }

    private fun connPrinter(){
        if (!mConnecting){
            mConnecting = true
            GPPrinter.openBlueTooth(LabelPrintSetting.getSetting().getPrinterAddress(),this)
        }else MyDialog.toastMessage(R.string.printer_connecting)
    }

    private fun initPrint(){
        findViewById<TopDrawableTextView>(R.id.label_print)?.setOnClickListener { it ->
            (it as TopDrawableTextView).triggerAnimation(true)
            if (it.hasNormal()){
                if (mPrintItem != null){
                    CustomApplication.execute {
                        val n = LabelPrintSetting.getSetting().printNum
                        var index = n
                        mLabelGoodsAdapter?.list?.forEach {
                            while (index-- > 0){
                                GPPrinter.sendDataToPrinter(mLabelView?.printSingleGoods(mPrintItem!!,it)?.command)
                            }
                            index = n
                        }
                    }
                }
            }else connPrinter()
        }
    }
    private fun initAdapter() {
        val recyclerView: RecyclerView = findViewById(R.id.goods_list)
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(recyclerView, resources.getDimension(R.dimen.size_48), LinearItemDecoration(getColor(R.color.gray_subtransparent)))
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mLabelGoodsAdapter = LabelGoodsAdapter()
        mLabelGoodsAdapter!!.setSelectListener {
            mLabelView?.setPreviewData(it)
        }
        recyclerView.adapter = mLabelGoodsAdapter
    }

    private fun initLabelView(){
        mLabelView = findViewById(R.id.label)
        mLabelView?.previewModel()
        loadLabelTemplate()
        registerForContextMenu(mLabelView)
    }

    private fun loadLabelTemplate(){
        Observable.create<LabelTemplate> {
            LabelPrintSetting.getSetting().let { setting->
                var t = AppDatabase.getInstance().LabelTemplateDao().getLabelTemplateById(setting.labelTemplateId)
                if (t == null) t = LabelTemplate()
                it.onNext(t)
            }
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe ({ temp->
            updateLabel(temp)
        },{err-> err.printStackTrace()
            MyDialog.toastMessage(err.message)})
    }

    private fun updateLabel(labelTemplate: LabelTemplate){
        mLabelView?.updateLabelTemplate(labelTemplate)
        mPrintItem = mLabelView?.getPrintItem()
    }

    private fun registerGoodsCallback(){
        mSelectGoodsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK){
                val barcodeId: String? = it.data?.getStringExtra("barcode_id")
                Logger.d("barcodeId:%s",barcodeId)
                if (barcodeId != null){
                    val goods = DataItem.getGoodsDataById(barcodeId)
                    goods?.apply {
                        mLabelGoodsAdapter?.addData(this)
                    }
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchContent() {
        val search = findViewById<EditText>(R.id._search_content)
        search.transformationMethod = object : ReplacementTransformationMethod() {
            override fun getOriginal(): CharArray {
                return charArrayOf(
                    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                    'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
                )
            }

            override fun getReplacement(): CharArray {
                return charArrayOf(
                    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
                    'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
                )
            }
        }
        search.setOnTouchListener { view: View?, motionEvent: MotionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                val dx = motionEvent.x
                val w = search.width
                if (dx > w - search.compoundPaddingRight) {
                    search.requestFocus()
                    val intent = Intent("com.google.zxing.client.android.SCAN")
                    startActivityForResult(intent, ScanCallbackCode.CODE_REQUEST_CODE)
                } else if (dx < search.compoundPaddingLeft) {
                    val intent = Intent(this, MobileSelectGoodsActivity::class.java)
                    intent.putExtra(MobileSelectGoodsActivity.TITLE_KEY, getString(R.string.select_goods_label))
                    intent.putExtra(MobileSelectGoodsActivity.IS_SEL_KEY, true)
                    intent.putExtra(MobileSelectGoodsActivity.MODIFIABLE, false)
                    mSelectGoodsLauncher?.launch(intent)
                }
            }
            false
        }
        mSearch = search
    }

    override fun hookEnterKey(): Boolean {
        if (currentFocus === mSearch) {
            runOnUiThread{
                searchGoods()
            }
            return true
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) { //条码回调
        if (resultCode == RESULT_OK) {
            val code = intent?.getStringExtra(CaptureActivity.CALLBACK_CODE)
            if (requestCode == ScanCallbackCode.CODE_REQUEST_CODE && Utils.isNotEmpty(code)) {
                mSearch?.apply {
                    setText(code)
                    selectAll()

                    searchGoods()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, intent)
    }
    private fun searchGoods(){
        val barcode = mSearch!!.text.toString()
        val list = DataItem.getGoodsDataByBarcode(barcode)
        if (list.size > 1){
            val intent = Intent(this, MobileSelectGoodsActivity::class.java)
            intent.putExtra(MobileSelectGoodsActivity.SEARCH_KEY, barcode)
            intent.putExtra(MobileSelectGoodsActivity.TITLE_KEY, getString(R.string.select_goods_label))
            intent.putExtra(MobileSelectGoodsActivity.IS_SEL_KEY, true)
            mSelectGoodsLauncher?.launch(intent)
        }else if(list.size == 1){
            mLabelGoodsAdapter?.addData(list[0])
        }
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(1, 1, 1, getString(R.string.modify_sz))
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                LabelDesignActivity.start(this,mLabelView?.getLabelTemplate())
            }
        }
        return super.onContextItemSelected(item)
    }

    @Subscribe
    fun handlerMsg(msg: LabelTemplate?) {
        loadLabelTemplate()
    }

    override fun onBackPressed() {
        if (mLabelGoodsAdapter?.isEmpty == true) {
            super.onBackPressed()
        } else {
            MyDialog.toastMessage(getString(R.string.exist_goods_hint))
        }
    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)

        super.onDestroy()
        //关闭打印机
        GPPrinter.close()
    }

    override fun getContentLayoutId(): Int {
        return R.layout.activity_label_print
    }

    override fun onConnecting() {
        mConnecting = true
        MyDialog.toastMessage(R.string.printer_connecting)
    }

    override fun onCheckCommand() {

    }

    override fun onSuccess(p0: PrinterDevices?) {
        mConnecting = false
        MyDialog.toastMessage(R.string.conn_success)
        printerNormal()
    }

    override fun onReceive(p0: ByteArray?) {

    }

    override fun onFailure() {
        mConnecting = false
        MyDialog.toastMessage(R.string.conn_fail)
        printerError()
    }

    override fun onDisconnect() {
        mConnecting = false
        MyDialog.toastMessage(R.string.printer_disconnect)
        printerError()
    }

    private fun printerError(){
        findViewById<TopDrawableTextView>(R.id.label_print)?.apply {
            warn()
        }
    }
    private fun printerNormal(){
        findViewById<TopDrawableTextView>(R.id.label_print)?.apply {
            normal()
        }
    }
}