package com.wyc.cloudapp.design

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import com.gprinter.bean.PrinterDevices
import com.gprinter.utils.CallbackListener
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity
import com.wyc.cloudapp.customizationView.TopDrawableTextView
import com.wyc.cloudapp.dialog.MyDialog

class LabelDesignActivity : AbstractDefinedTitleActivity(), View.OnClickListener,CallbackListener {
    private var mLabelView:LabelView? = null
    private var mCurBtn:TopDrawableTextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMiddleText(getString(R.string.label_setting))

        initLabelView()
        initLabelName()

        printerError()

        connPrinter()
    }
    private fun connPrinter(){
        GPPrinter.openBlueTooth(LabelPrintSetting.getSetting().getPrinterAddress(),this)
    }

    private fun initLabelView(){
        mLabelView = findViewById(R.id.labelView)
        mLabelView?.setLoadListener(object : LabelView.OnLoaded{
            override fun loaded(labelTemplate: LabelTemplate) {
                initLabelSize()
            }
        })
    }

    private fun initLabelName(){
        findViewById<EditText>(R.id.label_name)?.apply {
               setText(mLabelView?.getLabelName())
               addTextChangedListener(object :TextWatcher{
                   override fun beforeTextChanged(
                       s: CharSequence?,
                       start: Int,
                       count: Int,
                       after: Int
                   ) {

                   }

                   override fun onTextChanged(
                       s: CharSequence?,
                       start: Int,
                       before: Int,
                       count: Int
                   ) {

                   }

                   override fun afterTextChanged(s: Editable?) {
                       mLabelView?.updateLabelName(s.toString())
                   }
               })
        }
    }
    private fun initLabelSize(){
        findViewById<Spinner>(R.id.label_size)?.apply {
            val adapter = ArrayAdapter<String>(this@LabelDesignActivity, R.layout.drop_down_style)
            adapter.setDropDownViewResource(R.layout.drop_down_style)
            mLabelView?.getLabelSize()?.forEach {
                adapter.add(it.description)
            }
            setAdapter(adapter)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    mLabelView?.getLabelSize()?.forEach {
                        if (it.description == adapter.getItem(position)){
                            mLabelView?.updateLabelSize(it.rW,it.rH)
                            return
                        }
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //关闭打印机
        GPPrinter.close()
    }

    override fun getContentLayoutId(): Int {
        return R.layout.activity_format
    }

    override fun hasSlide(): Boolean {
        return false
    }

    companion object{
        @JvmStatic
        fun start(context: Activity){
            context.startActivity(Intent(context, LabelDesignActivity::class.java))
        }
    }
    private fun swapCurBtn(v: View){
        if (mCurBtn != null){
            mCurBtn!!.triggerAnimation(false)
        }
        mCurBtn = v as? TopDrawableTextView
        mCurBtn?.triggerAnimation(true)
    }

    override fun onClick(v: View) {
        swapCurBtn(v)

        mLabelView?.apply {
            when(v.id){
                R.id.delete->{
                    deleteItem()
                }
                R.id.shrink->{
                    shrinkItem()
                }
                R.id.zoom->{
                    zoomItem()
                }
                R.id.rotate->{
                    rotateItem()
                }
                R.id.undo->{

                }
                R.id.text->{
                    addTextItem()
                }
                R.id.barcode->{
                    addBarcodeItem()
                }
                R.id.qrcode->{
                    addQRCodeItem()
                }
                R.id.line->{
                    addLineItem()
                }
                R.id.rect->{
                    addRectItem()
                }
                R.id.circle->{
                    addCircleItem()
                }
                R.id.date->{
                    addDateItem()
                }
                R.id.data->{
                    addDataItem()
                }
                R.id.image->{

                }
                R.id.save->{
                    save()
                }
                R.id.printLabel->{
                    if((v as TopDrawableTextView).hasNormal()){
                        var n = mLabelView?.getPrintNumber()?:0
                        while (n-- > 0){
                            GPPrinter.sendDataToPrinter(mLabelView?.printSingleGoodsById("20765")?.command)
                        }
                        this@LabelDesignActivity.findViewById<ImageView>(R.id.imageView3).setImageBitmap(mLabelView?.printSingleGoodsBitmap("20765"))
                    }else{
                        connPrinter()
                    }
                }
            }
        }
    }

    private fun printerError(){
        findViewById<TopDrawableTextView>(R.id.printLabel).apply {
            warn()
        }
    }
    private fun printerNormal(){
        findViewById<TopDrawableTextView>(R.id.printLabel).apply {
            normal()
        }
    }

    override fun onConnecting() {
        MyDialog.toastMessage(R.string.printer_connecting)
    }

    override fun onCheckCommand() {

    }

    override fun onSuccess(p0: PrinterDevices?) {
        MyDialog.toastMessage(R.string.conn_success)
        printerNormal()
    }

    override fun onReceive(p0: ByteArray?) {

    }

    override fun onFailure() {
        MyDialog.toastMessage(R.string.conn_fail)
        printerError()
    }

    override fun onDisconnect() {
        MyDialog.toastMessage(R.string.printer_disconnect)
        printerError()
    }

}