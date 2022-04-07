package com.wyc.cloudapp.design

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.*
import com.gprinter.bean.PrinterDevices
import com.gprinter.utils.CallbackListener
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity
import com.wyc.cloudapp.activity.mobile.business.EditGoodsInfoBaseActivity
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.customizationView.TopDrawableTextView
import com.wyc.cloudapp.data.room.AppDatabase
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.FileUtils
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.io.IOException

class LabelDesignActivity : AbstractDefinedTitleActivity(), View.OnClickListener,CallbackListener {
    private var mLabelView:LabelView? = null
    private var mCurBtn:TopDrawableTextView? = null
    private var newFlag = false

    private val REQ_CROP = 108
    private val CHOOSE_PHOTO = 110
    private val REQUEST_CAPTURE_IMG = 100
    private var mImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMiddleText(getString(R.string.label_setting))

        initLabelView()
        initLabelName()
        initAddLabel()

        printerError()

        connPrinter()
    }
    private fun connPrinter(){
        GPPrinter.openBlueTooth(LabelPrintSetting.getSetting().getPrinterAddress(),this)
    }

    private fun initAddLabel(){
        setRightText(getString(R.string.add_sz))
        setRightListener {
            val addLabelFormat = AddLabelFormat(this)
            newFlag = true
            if (addLabelFormat.exec() == 1){
                val labelTemplate = addLabelFormat.getContent()
                mLabelView?.updateLabelTemplate(labelTemplate)

                findViewById<EditText>(R.id.label_name)?.setText(labelTemplate.templateName)
                (findViewById<Spinner>(R.id.label_size).adapter as? ArrayAdapter<String>)?.apply {
                    clear()
                    mLabelView?.getLabelSize()?.forEach {
                        add(it.description)
                    }
                }
            }
            newFlag = false
        }
    }

    private fun initLabelView(){
        mLabelView = findViewById(R.id.labelView)
        Observable.create<LabelTemplate> {
            val intent = intent
            var labelTemplate:LabelTemplate? = null
            if (intent != null){
                labelTemplate = intent.getParcelableExtra("label")
            }
            if (labelTemplate != null){
                it.onNext(labelTemplate)
            }else{
                LabelPrintSetting.getSetting().let { setting->
                    var t = AppDatabase.getInstance().LabelTemplateDao().getLabelTemplateById(setting.labelTemplateId)
                    if (t == null) t = LabelTemplate()
                    it.onNext(t)
                    mLabelView?.setRotate(setting.rotate.value)
                }
            }
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe ({ temp->
            mLabelView?.updateLabelTemplate(temp)
            initLabelSize()
        },{err-> err.printStackTrace()
            MyDialog.toastMessage(err.message)})
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
                       if (!newFlag)
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
                            if (!newFlag)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_CAPTURE_IMG -> {
                    crop()
                }
                REQ_CROP -> {
                    try {
                        mImageUri?.let {
                            contentResolver.openInputStream(it).use { inputStream ->
                                mLabelView?.setBackground(BitmapFactory.decodeStream(inputStream))
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        MyDialog.toastMessage(e.localizedMessage)
                    }
                }
                CHOOSE_PHOTO -> {
                    mImageUri = intent?.data
                    crop()
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, intent)
    }

    private fun crop() {
        val intent = Intent("com.android.camera.action.CROP")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.setDataAndType(mImageUri, "image/*")

        intent.putExtra("outputX", mLabelView?.getRealWidth())
        intent.putExtra("outputY", mLabelView?.getRealHeight())

        intent.putExtra("scale", true)
        intent.putExtra("return-data", false)
        val imgCropUri = FileUtils.createCropImageFile()
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imgCropUri)
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
        intent.putExtra("noFaceDetection", false)
        mImageUri = imgCropUri
        startActivityForResult(intent, REQ_CROP)
    }

    private fun openAlbum() {
        val openAlbumIntent = Intent(Intent.ACTION_GET_CONTENT)
        openAlbumIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
        openAlbumIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivityForResult(openAlbumIntent, CHOOSE_PHOTO) //打开相册
    }

    companion object{
        @JvmStatic
        fun start(context: Activity,labelTemplate: LabelTemplate? = null){
            val intent = Intent(context, LabelDesignActivity::class.java)
            if (labelTemplate != null)intent.putExtra("label",labelTemplate)
            context.startActivity(intent)
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
                    openAlbum()
                }
                R.id.save->{
                    save()
                }
                R.id.preview->{
                    this@LabelDesignActivity.findViewById<ImageView>(R.id.imageView3).setImageBitmap(mLabelView?.printSingleGoodsBitmap(""))
                }
                R.id.printLabel->{
                    if((v as TopDrawableTextView).hasNormal()){
                        CustomApplication.execute {
                            var n = LabelPrintSetting.getSetting().printNum
                            while (n-- > 0){
                                GPPrinter.sendDataToPrinter(mLabelView?.printSingleGoodsById("")?.command)
                            }
                        }
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