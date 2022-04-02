package com.wyc.cloudapp.design

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.ReplacementTransformationMethod
import android.view.MotionEvent
import android.view.View
import android.widget.Button
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
import com.wyc.cloudapp.constants.ScanCallbackCode
import com.wyc.cloudapp.decoration.LinearItemDecoration
import com.wyc.cloudapp.decoration.SuperItemDecoration
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger

class LabelPrintActivity : AbstractDefinedTitleActivity() , CallbackListener {
    private var mSelectGoodsLauncher: ActivityResultLauncher<Intent>? = null
    private var mLabelGoodsAdapter:LabelGoodsAdapter? = null
    private var mLabelView:LabelView? = null
    private var mPrintItem:List<ItemBase>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        setMiddleText(getString(R.string.label_print))

        registerGoodsCallback()

        initSearchContent()
        initLabelView()
        initSaleGoodsAdapter()
        initPrint()

        connPrinter()
    }
    private fun connPrinter(){
        GPPrinter.openBlueTooth(LabelPrintSetting.getSetting().getPrinterAddress(),this)
    }

    private fun initPrint(){
        findViewById<Button>(R.id.label_print)?.setOnClickListener {
            val n = mLabelView?.getPrintNumber()?:0
            var index = n
            mLabelGoodsAdapter?.list?.forEach {
                while (index-- > 0){
                    GPPrinter.sendDataToPrinter(mLabelView?.printSingleGoods(mLabelView?.getPrintItem()!!,it)?.command)
                }
                index = n
            }
        }
    }
    private fun initSaleGoodsAdapter() {
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
        mLabelView?.setLoadListener(object :LabelView.OnLoaded{
            override fun loaded(labelTemplate: LabelTemplate) {
                mPrintItem = mLabelView?.getPrintItem()
            }
        })
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
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) { //条码回调
        if (resultCode == RESULT_OK) {
            val _code = intent?.getStringExtra(CaptureActivity.CALLBACK_CODE)
            if (requestCode == ScanCallbackCode.CODE_REQUEST_CODE) {
                Logger.d(_code)
            }
        }
        super.onActivityResult(requestCode, resultCode, intent)
    }


    override fun onDestroy() {
        super.onDestroy()
        //关闭打印机
        GPPrinter.close()
    }

    override fun getContentLayoutId(): Int {
        return R.layout.activity_label_print
    }

    override fun onConnecting() {
        MyDialog.toastMessage(R.string.printer_connecting)
    }

    override fun onCheckCommand() {

    }

    override fun onSuccess(p0: PrinterDevices?) {
        MyDialog.toastMessage(R.string.conn_success)
        //printerNormal()
    }

    override fun onReceive(p0: ByteArray?) {

    }

    override fun onFailure() {
        MyDialog.toastMessage(R.string.conn_fail)
        //printerError()
    }

    override fun onDisconnect() {
        MyDialog.toastMessage(R.string.printer_disconnect)
        //printerError()
    }
}