package com.wyc.cloudapp.design

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Base64
import android.view.MotionEvent
import android.view.View
import com.alibaba.fastjson.JSONArray
import com.google.zxing.BarcodeFormat
import com.gprinter.command.LabelCommand
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.TreeListItem
import com.wyc.cloudapp.data.room.AppDatabase
import com.wyc.cloudapp.design.LabelTemplate.Companion.height2Pixel
import com.wyc.cloudapp.design.LabelTemplate.Companion.width2Pixel
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.dialog.tree.TreeListDialogForObj
import com.wyc.cloudapp.logger.Logger
import org.greenrobot.eventbus.EventBus
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.min


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.design
 * @ClassName:      格式设计界面
 * @Description:    作用描述
 * @Author:         wyc
 * @CreateDate:     2022/3/16 15:01
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/3/16 15:01
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class LabelView: View {
    private val mOffsetX = context.resources.getDimensionPixelOffset(R.dimen.size_14)
    private val mOffsetY = context.resources.getDimensionPixelOffset(R.dimen.size_14)

    private var realWidth = 1
    private var realHeight = 1

    private val contentList = mutableListOf<ItemBase>()
    private val mPaint = Paint()

    private val mTextBound = Rect()
    private var mBackground:Bitmap?  = null

    private var mCurItem:ItemBase? = null

    private var mLastX = 0f
    private var mLastY = 0f
    private var mMoveX = 0f
    private var mMoveY = 0f

    private var mLabelTemplate:LabelTemplate = LabelTemplate()

    private val mLabelSize = LabelTemplate.getDefaultSize()

    private var mItemChange = false

    private var mRotate = 0
    /**
     * 当前模式 true 预览 false 编辑
     * */
    private var mModel = false


    private var count = 0
    private var firClick: Long = 0
    private var mItemDClick:OnItemDoubleClick? = null

    constructor(context: Context):this(context, null)
    constructor(context: Context, attrs: AttributeSet?):this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr){
        initPaint()
    }

    fun updateLabelTemplate(labelTemplate: LabelTemplate){
        mLabelTemplate = labelTemplate
        mItemChange = true
        adjustLabelSize(labelTemplate.width,labelTemplate.height)
        contentList.clear()
        contentList.addAll(toItem(labelTemplate.itemList))
        generateBackground()
        requestLayout()
        invalidate()
    }

    fun setLabelTemplate(label: LabelTemplate){
        updateLabelTemplate(label)
    }
    fun getLabelTemplate():LabelTemplate{
        return mLabelTemplate
    }

    private fun toJson():JSONArray{
        return JSONArray.toJSON(contentList) as? JSONArray?:JSONArray()
    }
    private fun toItem(json:String):MutableList<ItemBase>{
        val a =  JSONArray.parseArray(json)
        val list = mutableListOf<ItemBase>()
        for (i in 0 until a.size){
            val obj = a.getJSONObject(i)
            when(obj.getString("clsType")){
                TextItem::class.simpleName ->{
                    list.add(obj.toJavaObject(TextItem::class.java))
                }
                LineItem::class.simpleName ->{
                    list.add(obj.toJavaObject(LineItem::class.java))
                }
                RectItem::class.simpleName ->{
                    list.add(obj.toJavaObject(RectItem::class.java))
                }
                CircleItem::class.simpleName ->{
                    list.add(obj.toJavaObject(CircleItem::class.java))
                }
                BarcodeItem::class.simpleName ->{
                    list.add(obj.toJavaObject(BarcodeItem::class.java))
                }
                DateItem::class.simpleName ->{
                    list.add(obj.toJavaObject(DateItem::class.java))
                }
                DataItem::class.simpleName ->{
                    list.add(obj.toJavaObject(DataItem::class.java))
                }
            }
        }
        return list
    }

    private fun initPaint(){
        mPaint.isAntiAlias = true
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }
    private fun generateBackground(){
        CustomApplication.execute {
            val bmp = decodeImage(mLabelTemplate.backgroundImg)
            mBackground = bmp
            postInvalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!mModel && checkTouchRegion(event.x,event.y)){
            when(event.action){
                MotionEvent.ACTION_DOWN->{
                    mCurItem?.checkDeleteClick(event.x,event.y)
                    if (activeItem(event.x,event.y)){
                        mLastX = event.x
                        mLastY = event.y
                        return true
                    }
                };
                MotionEvent.ACTION_MOVE->{
                    mCurItem?.let {
                        mMoveX = event.x - mLastX
                        mMoveY = event.y - mLastY

                        mLastX = event.x
                        mLastY = event.y

                        it.moveCurItem(realWidth,realHeight,event.x,event.y,mOffsetX,mOffsetY,mMoveX,mMoveY)
                        invalidate()
                    }
                };
                MotionEvent.ACTION_UP->{
                    if (!deleteItem(event.x,event.y) && !checkDoubleClick()){
                        releaseCurItem()
                    }
                }
            }
            return true
        }else releaseCurItem()

        return super.onTouchEvent(event)
    }
    private fun checkDoubleClick():Boolean{
        mCurItem?.apply {
            count++
            val interval = 200
            if (1 == count) {
                firClick = System.currentTimeMillis()
            } else if (2 == count) {
                val secClick = System.currentTimeMillis()
                if (secClick - firClick < interval) {
                    popMenu(this@LabelView)
                    count = 0
                    firClick = 0
                    return true
                } else {
                    firClick = secClick
                    count = 1
                }
            }
        }
        return false
    }
    interface OnItemDoubleClick{
        fun onDoubleClick(item: ItemBase)
    }


    private fun checkTouchRegion(clickX:Float,clickY:Float):Boolean{
        return clickX - mOffsetX in 0f..realWidth.toFloat() && clickY - mOffsetY in 0f..realHeight.toFloat()
    }
    private fun deleteItem(clickX:Float,clickY:Float):Boolean{
        mCurItem?.let {
            it.checkDeleteClick(clickX,clickY)
            if (it.hasDelete()){
                contentList.remove(it)
                mCurItem = null
                invalidate()
                return true
            }
        }
        return false
    }

    private fun activeItem(clickX:Float, clickY:Float):Boolean{
        for (i in contentList.size -1 downTo 0){
            val it = contentList[i]
            if (it.hasSelect(clickX,clickY,mOffsetX,mOffsetY)){
                if (mCurItem != it){
                    mCurItem?.apply {
                        disableItem()
                    }
                    contentList[i] = contentList[contentList.size -1]
                    contentList[contentList.size -1] = it
                    mCurItem = it
                }
                return true
            }
        }
        if (mCurItem != null){
            mCurItem!!.disableItem()
            mCurItem = null
            invalidate()
        }
        return false
    }

    private fun releaseCurItem(){
        mCurItem?.let {
            it.releaseItem()
            invalidate()
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthSpec = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightSpec = MeasureSpec.getMode(heightMeasureSpec)

        var resultWidthSize = 0
        var resultHeightSize = 0

        when(widthSpec){
            MeasureSpec.EXACTLY -> {
                resultWidthSize = widthSize
            }
            MeasureSpec.AT_MOST -> {
                resultWidthSize = selectMeasureWidth()
            }
            MeasureSpec.UNSPECIFIED -> {
                resultWidthSize = selectMeasureWidth()
            }
        }

        when(heightSpec){
            MeasureSpec.EXACTLY,MeasureSpec.AT_MOST,MeasureSpec.UNSPECIFIED -> {
                resultHeightSize = selectMeasureHeight(widthSpec,widthSize)
            }
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(resultWidthSize,widthSpec), MeasureSpec.makeMeasureSpec(resultHeightSize ,heightSpec))

        calculateContentSize()
    }
    /**
     * 已宽度为参照进行缩放,如果宽度是具体尺寸，则需要先计算缩放后的高度。否则可能缩放后高度比实际高度要大
     * */
    private fun selectMeasureHeight(widthSpec:Int,widthSize:Int):Int{
        var h = min(height2Pixel(mLabelTemplate,context), mLabelTemplate.realHeight)
        if (widthSpec == MeasureSpec.EXACTLY){
            val rightMargin = context.resources.getDimensionPixelOffset(R.dimen.size_5)
            val hh = ((widthSize  - mOffsetX - rightMargin) / width2Pixel(mLabelTemplate,context).toFloat() * height2Pixel(mLabelTemplate,context)).toInt()
            h = max(hh,h)
        }
        return h + mOffsetY + 8
    }
    private fun selectMeasureWidth():Int{
        return max(width2Pixel(mLabelTemplate,context), mLabelTemplate.realWidth) + mOffsetX
    }

    private fun measureItem(){
        contentList.forEach {
            it.measure(realWidth, realHeight)
        }
    }

    private fun calculateContentSize(){
        val margin = context.resources.getDimensionPixelOffset(R.dimen.size_5)
        realWidth = measuredWidth  - mOffsetX - margin
        realHeight = ((realWidth.toFloat() /  width2Pixel(mLabelTemplate,context).toFloat()) * height2Pixel(mLabelTemplate,context)).toInt()
        measureItem()
    }


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        if (changed || mItemChange)
            layoutItem()
    }
    private fun layoutItem(){
        if (mItemChange){
            mItemChange = false
            val tWidth = mLabelTemplate.realWidth
            val tHeight = mLabelTemplate.realHeight
            if (tWidth != 0 && tHeight != 0){
                val scaleX = realWidth.toFloat() / tWidth
                val scaleY = realHeight.toFloat()/ tHeight
                contentList.forEach {
                    it.transform(scaleX,scaleY)
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.apply {
            if (mRotate != 0){
                save()
                rotate(mRotate.toFloat(),width / 2f,height / 2f)
            }
            drawRule(this)
            drawBackground(this)
            drawContent(this)

            if (mRotate != 0){
                restore()
            }
        }
    }

    private fun drawRule(canvas: Canvas){
        val physicsWidth = mLabelTemplate.width
        val physicsHeight = mLabelTemplate.height
        var perGap = realWidth / physicsWidth.toFloat()
        var coordinate: Float
        var num:String
        val lineHeight = context.resources.getDimension(R.dimen.size_4)

        mPaint.textSize = context.resources.getDimension(R.dimen.font_size_6)
        mPaint.color = Color.GRAY
        mPaint.style = Paint.Style.FILL
        mPaint.strokeWidth = 0f
        for (i in 0..physicsWidth){
            num = i.toString()
            coordinate = if (i == physicsWidth){
                i * perGap + mOffsetX - mPaint.strokeWidth
            }else
                i * perGap + mOffsetX

            if (i % 10 == 0){
                canvas.drawLine(coordinate,
                    mOffsetY.toFloat(),coordinate,mOffsetY - lineHeight * 2,mPaint)
                canvas.drawText(num,coordinate - mPaint.measureText(num) / 2,mOffsetY - lineHeight * 2,mPaint)
            }else
                canvas.drawLine(coordinate,
                    mOffsetY.toFloat(),coordinate,mOffsetY - lineHeight,mPaint)
        }
        canvas.drawLine(mOffsetX.toFloat(), mOffsetY.toFloat(),
            (realWidth + mOffsetX).toFloat(), mOffsetY.toFloat(),mPaint)

        perGap = realHeight / physicsHeight.toFloat()
        for (i in 0..physicsHeight){
            num = i.toString()
            coordinate = if (i == physicsHeight){
                i * perGap + mOffsetY - mPaint.strokeWidth
            }else
                i * perGap + mOffsetY

            if (i % 10 == 0) {
                canvas.drawLine(mOffsetY.toFloat(), coordinate, mOffsetY - lineHeight * 2, coordinate, mPaint)

                mPaint.getTextBounds(num,0,num.length,mTextBound)
                canvas.save()
                canvas.rotate(-90f,mOffsetY - lineHeight * 2 ,coordinate)
                canvas.drawText(num,mOffsetY - lineHeight * 2 - mPaint.measureText(num) / 2,coordinate,mPaint)
                canvas.restore()
            }else
                canvas.drawLine(mOffsetY.toFloat(),coordinate,mOffsetY - lineHeight,coordinate,mPaint)
        }
        canvas.drawLine(mOffsetX.toFloat(), mOffsetY.toFloat(),
            mOffsetX.toFloat(), (realHeight + mOffsetY).toFloat(),mPaint)
    }
    private fun drawBackground(canvas: Canvas){
        mBackground?.apply {
            //画阴影
            val color = mPaint.color
            mPaint.color = Color.WHITE
            mPaint.setShadowLayer(15f,0f,8f,Color.GRAY)
            canvas.drawRect(
                mOffsetX.toFloat(), mOffsetY.toFloat(),
                (realWidth + mOffsetX).toFloat(), (realHeight + mOffsetY).toFloat(),mPaint)
            mPaint.color = color
            mPaint.setShadowLayer(0f,0f,0f,Color.GRAY)

            val matrix = Matrix()
            matrix.setScale(realWidth.toFloat() / width.toFloat(),realHeight.toFloat() / height.toFloat())
            canvas.save()
            canvas.translate(mOffsetX.toFloat(),mOffsetY.toFloat())
            canvas.drawBitmap(this,matrix,null)
            canvas.restore()
        }
    }
    private fun drawContent(canvas: Canvas){
        if (contentList.isNotEmpty()){
            canvas.clipRect(mOffsetX,mOffsetY,realWidth + mOffsetX,realHeight + mOffsetY)
            contentList.forEach {
                it.draw(mOffsetX.toFloat(),mOffsetY.toFloat(),canvas,mPaint)
            }
        }
    }

    /**
     * @param bg 背景位图。如果为null则清除当前背景
     * */
    fun setBackground(bg:Bitmap?){
        if (bg == null){
            if (mBackground != null){
                mBackground!!.recycle()
                mBackground = null

                mLabelTemplate.backgroundImg = ""
            }
        }else{
            if (mBackground != null){
                mBackground!!.recycle()
            }
            mBackground = bg
            mLabelTemplate.backgroundImg = encodeImage(bg)
        }
        postInvalidate()
    }

    private fun encodeImage(bitmap: Bitmap):String {
        ByteArrayOutputStream().use {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, it)
            val bytes: ByteArray = it.toByteArray()
            return Base64.encodeToString(bytes, Base64.DEFAULT)
        }
    }
    private fun decodeImage(bmMsg: String):Bitmap? {
        val input = Base64.decode(bmMsg, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(input, 0, input.size)
    }

    private fun addItem(item: ItemBase){
        if (mCurItem != null){
            mCurItem!!.disableItem()
        }
        item.activeItem()
        item.measure(realWidth, realHeight)
        mCurItem = item

        contentList.add(item)

        postInvalidate()
    }
    private fun swapCurItem(item: ItemBase){
        if (mCurItem != null){
            mCurItem!!.disableItem()
        }
        item.activeItem()
        mCurItem = item
        postInvalidate()
    }

    fun getRealWidth():Int{
        return realWidth
    }
    fun getRealHeight():Int{
        return realHeight
    }

    fun addTextItem(){
        addItem(TextItem())
    }
    fun addLineItem(){
        addItem(LineItem())
    }

    fun addRectItem(){
        addItem(RectItem())
    }

    fun addCircleItem(){
        addItem(CircleItem())
    }

    fun addBarcodeItem(){
        addItem(BarcodeItem())
    }

    fun addQRCodeItem(){
        val item = BarcodeItem()
        item.barcodeFormat = BarcodeFormat.QR_CODE
        addItem(item)
    }

    fun addDateItem(){
        addItem(DateItem())
    }

    fun addDataItem(){
        val treeListDialog = TreeListDialogForObj(context, context.getString(R.string.data))
        treeListDialog.setData(convertField(), null, true)
        if (treeListDialog.exec() == 1) {
            val obj = treeListDialog.singleContent
            if (DataItem.FIELD.Barcode.field == obj.item_id){
                val item = BarcodeItem()
                item.field = obj.item_id
                addItem(item)
            }else{
                val item = DataItem()
                item.field = obj.item_id
                item.content = obj.item_name
                addItem(item)
            }
        }
    }
    private fun convertField(): List<TreeListItem> {
        val data: MutableList<TreeListItem> = ArrayList()
        val  values: Array<DataItem.FIELD> = DataItem.FIELD.values()
        values.iterator().forEach {
            val item = TreeListItem()
            item.item_id = it.field
            item.item_name = it.description
            data.add(item)
        }
        return data
    }

    fun deleteItem(){
        mCurItem?.apply {
            contentList.remove(this)
            swapCurItem(this)
        }
    }

    fun shrinkItem(){
        mCurItem?.apply {
            shrink()
            invalidate()
        }
    }
    fun zoomItem(){
        mCurItem?.apply {
            zoom()
            invalidate()
        }
    }
    fun rotateItem(){
        mCurItem?.apply {
            radian += 15
            invalidate()
        }
    }

    fun previewModel(){
        mModel = true
    }
    fun editModel(){
        mModel = false
    }
    fun hasPreviewModel():Boolean{
        return mModel
    }
    fun hasEditModel():Boolean{
        return !mModel
    }

    fun getLabelName():String{
        return mLabelTemplate.templateName
    }

    fun updateLabelName(n:String){
        mLabelTemplate.templateName = n
    }

    fun updateLabelSize(w:Int,h: Int){
        adjustLabelSize(w,h)
        calculateContentSize()

        requestLayout()
        postInvalidate()
    }
    private fun adjustLabelSize(w:Int, h: Int){
        sortLabelSize(w,h)
        mLabelTemplate.width = w
        mLabelTemplate.height = h
    }
    private fun sortLabelSize(w: Int,h: Int){
        mLabelSize.forEachIndexed{index,size ->
            if (size.rW == w && size.rH == h){
                if (index != 0){
                    val t = mLabelSize[0]
                    mLabelSize[0] = mLabelSize[index]
                    mLabelSize[index] = t
                }
                return
            }
        }
        mLabelSize.add(0,LabelTemplate.LabelSize(w,h))
    }

    fun save(){
        CustomApplication.execute {
            val template = mLabelTemplate
            template.realWidth = realWidth
            template.realHeight = realHeight
            template.itemList = toJson().toString()
            AppDatabase.getInstance().LabelTemplateDao().insertTemplate(template)
            MyDialog.toastMessage(CustomApplication.self().getString(R.string.success))
            EventBus.getDefault().post(template)
        }
    }

    fun getLabelSize():MutableList<LabelTemplate.LabelSize>{
        return mLabelSize
    }

    private fun getPrintBitmap():Bitmap{
        val dpi = LabelPrintSetting.getSetting().dpi
        val bmp = Bitmap.createBitmap(mLabelTemplate.width2Dot(dpi), mLabelTemplate.height2Dot(dpi),Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.WHITE)
        getPrintItem().forEach {
            it.draw(0f,0f,c, Paint())
        }
        Logger.d("bWidth:%d,bHeight:%d",bmp.width,bmp.height)
        return bmp
    }

    fun getPrintBitmap2():Bitmap{
        val dpi = LabelPrintSetting.getSetting().dpi
        val contentWidth = mLabelTemplate.width2Dot(dpi)
        val contentHeight = mLabelTemplate.height2Dot(dpi)
        val w = realWidth
        val h = realHeight
        val bmp = Bitmap.createBitmap(contentWidth, contentHeight,Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.WHITE)

        c.clipRect(0,0, w,h)
        c.scale(contentWidth.toFloat() / realWidth.toFloat(),contentHeight.toFloat() / realHeight.toFloat())
        c.translate((-mOffsetX).toFloat(), (-mOffsetX).toFloat())

        val b = mBackground
        mBackground = null
        draw(c)
        mBackground = b

        Logger.d("bWidth:%d,bHeight:%d",bmp.width,bmp.height)
        return bmp
    }

    fun getPrintItem():MutableList<ItemBase>{
        val scaleX = mLabelTemplate.height2Dot(LabelPrintSetting.getSetting().dpi) / mLabelTemplate.realHeight.toFloat()
        val content = toItem(mLabelTemplate.itemList)
        content.forEach {
            it.transform(scaleX,scaleX)
            if (it is DataItem){
                it.hasMark = false
            }
        }
        return content
    }

    fun printSingleGoodsById(barcodeId:String = ""):LabelCommand{
        val printItem = getPrintItem()
        generatePrinterDataItem(printItem,barcodeId)
        return getGPTscCommand(printItem)
    }

    fun printSingleGoodsBitmap(barcodeId:String = ""):Bitmap{
        val dpi = LabelPrintSetting.getSetting().dpi
        val printItem = getPrintItem()
        generatePrinterDataItem(printItem,barcodeId)
        val bmp = Bitmap.createBitmap(mLabelTemplate.width2Dot(dpi),mLabelTemplate.height2Dot(dpi),Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.drawColor(Color.WHITE)

        mBackground?.apply {
            val matrix = Matrix()
            matrix.setScale(bmp.width / width.toFloat(),bmp.height / height.toFloat())
            c.save()
            c.drawBitmap(this,matrix,null)
            c.restore()
        }


        printItem.forEach {
            val b = it.createItemBitmap(Color.TRANSPARENT)
            c.save()
            c.translate(it.left.toFloat(), it.top.toFloat())
            c.drawBitmap(b,Matrix(),null)
            c.restore()
        }
        return bmp
    }

    private fun generatePrinterDataItem(printItem: MutableList<ItemBase>, barcodeId:String = ""){
        DataItem.getGoodsDataById(barcodeId)?.apply {
            printItem.forEach {
                if (it is DataItem){
                    it.content = getValueByField(it.field)
                    if (it.updateNewline()){
                        it.height = (it.height * mLabelTemplate.width2Dot(LabelPrintSetting.getSetting().dpi) / mLabelTemplate.realWidth.toFloat()).toInt()
                    }
                }else if (it is BarcodeItem && it.field.isNotEmpty()){
                    it.hasMark = false
                    it.content = getValueByField(it.field)
                }
            }
        }
    }

    fun printSingleGoods(printItem: List<ItemBase>, goods:DataItem.LabelGoods):LabelCommand{
        val itemCopy = mutableListOf<ItemBase>()
        printItem.forEach {
            val item = it.clone()
            if (item is DataItem){
                item.content = goods.getValueByField(item.field)
                if (item.updateNewline()){
                    item.height = (item.height * mLabelTemplate.width2Dot(LabelPrintSetting.getSetting().dpi) / mLabelTemplate.realWidth.toFloat()).toInt()
                }
            }else if (item is BarcodeItem && item.field.isNotEmpty()){
                item.hasMark = false
                item.content = goods.getValueByField(item.field)
            }
            itemCopy.add(item)
        }
        return getGPTscCommand(itemCopy)
    }



    fun getGPTscCommand(data: List<ItemBase>):LabelCommand{
        val setting = LabelPrintSetting.getSetting()
        val offsetX = setting.offsetX
        val offsetY = setting.offsetY
        Logger.d("offsetX:%d,offsetY:%d",offsetX,offsetY)

        val tsc = LabelCommand()
        tsc.addSize(mLabelTemplate.width, mLabelTemplate.height)
        tsc.addGap(5)
        tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL)
        tsc.addReference(offsetX, offsetY)
        tsc.addDensity(LabelCommand.DENSITY.DNESITY4)
        tsc.addCls()

        data.forEach {
            val b = it.createItemBitmap()
            tsc.drawImage(it.left,it.top,b.width,b)
        }
        tsc.addPrint(1, 1)
        return tsc
    }

    fun setPreviewData(labelGoods: DataItem.LabelGoods){
        if (hasPreviewModel()){
            labelGoods.apply {
                contentList.forEach {
                    if (it is DataItem){
                        it.content = getValueByField(it.field)
                        it.updateNewline()
                    }else if (it is BarcodeItem && it.field.isNotEmpty()){
                        it.hasMark = false
                        it.content = getValueByField(it.field)
                    }
                }
                postInvalidate()
            }
        }
    }

    fun setRotate(degree:Int){
        mRotate = degree
        postInvalidate()
    }

    fun getGPTscCommand2():LabelCommand{
        val tsc = LabelCommand()
        tsc.addSize(mLabelTemplate.width, mLabelTemplate.height)
        tsc.addGap(5)
        tsc.addDirection(LabelCommand.DIRECTION.FORWARD, LabelCommand.MIRROR.NORMAL)
        tsc.addReference(0, 0)
        tsc.addDensity(LabelCommand.DENSITY.DNESITY1)
        tsc.addCls()
        val b = getPrintBitmap()
        tsc.drawJPGImage(0,0,b.width,b)
        tsc.addPrint(1, 1)
        return tsc
    }
}