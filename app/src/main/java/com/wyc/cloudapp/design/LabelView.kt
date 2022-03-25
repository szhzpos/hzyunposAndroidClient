package com.wyc.cloudapp.design

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.alibaba.fastjson.JSONArray
import com.google.zxing.BarcodeFormat
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.logger.Logger
import java.io.File
import java.io.FileOutputStream


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

    var physicsWidth = w_70
    var physicsHeight = h_40

    private val contentWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, physicsWidth.toFloat(),context.resources.displayMetrics).toInt()
    private val contentHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, physicsHeight.toFloat(),context.resources.displayMetrics).toInt()

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

    constructor(context: Context):this(context, null)
    constructor(context: Context, attrs: AttributeSet?):this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr){
        initPaint()
        generateBackground()
    }
    private fun toJson(){
        val a =  JSONArray.toJSON(contentList) as JSONArray
        Logger.d_json(a)
    }
    private fun toItem(){
        val a =  JSONArray.toJSON(contentList) as JSONArray
        Logger.d_json(a)

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

                }
            }
        }

    }

    private fun initPaint(){
        mPaint.isAntiAlias = true
        /*绘制Drawable边框时会模糊，所以要关闭硬件加速*/
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }
    private fun generateBackground(){
        mBackground = BitmapFactory.decodeFile(getBackgroundFileName())
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (checkTouchRegion(event.x,event.y)){
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
                    deleteItem(event.x,event.y);
                    releaseCurItem()
                }
            }
        }else releaseCurItem()
        return super.onTouchEvent(event)
    }
    private fun checkTouchRegion(clickX:Float,clickY:Float):Boolean{
        return clickX - mOffsetX in 0f..realWidth.toFloat() && clickY - mOffsetY in 0f..realHeight.toFloat()
    }
    private fun deleteItem(clickX:Float,clickY:Float){
        mCurItem?.let {
            if (it.hasDelete()){
                it.checkDeleteClick(clickX,clickY)
                if (it.hasDelete()){
                    contentList.remove(it)
                    mCurItem = null
                    invalidate()
                }
            }
        }
    }

    private fun activeItem(clickX:Float, clickY:Float):Boolean{
        for (i in contentList.size -1 downTo 0){
            val it = contentList[i]
            if (it.hasSelect(clickX - mOffsetX,clickY - mOffsetY)){
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

        when(heightSpec){
            MeasureSpec.EXACTLY -> {
                resultHeightSize = heightSize
            }
            MeasureSpec.AT_MOST -> {
                resultHeightSize = contentHeight
            }
            MeasureSpec.UNSPECIFIED -> {
                resultHeightSize = contentHeight
            }
        }
        when(widthSpec){
            MeasureSpec.EXACTLY -> {
                resultWidthSize = widthSize
            }
            MeasureSpec.AT_MOST -> {
                resultWidthSize = contentWidth
            }
            MeasureSpec.UNSPECIFIED -> {
                resultWidthSize = contentWidth
            }
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(resultWidthSize,widthSpec), MeasureSpec.makeMeasureSpec(resultHeightSize,heightSpec))

        calculateContentSize()
    }
    private fun measureItem(){
        contentList.forEach {
            it.measure(realWidth, realHeight,mPaint)
        }
    }

    private fun calculateContentSize(){
        val rightMargin = context.resources.getDimensionPixelOffset(R.dimen.size_5)
        realWidth = measuredWidth  - mOffsetX - rightMargin
        realHeight = ((realWidth.toFloat() / contentWidth.toFloat()) * contentHeight).toInt()
        measureItem()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawColor(context.resources.getColor(R.color.white,null))
            drawRule(this)
            drawBackground(this)
            drawContent(this)
        }
    }

    private fun drawRule(canvas: Canvas){
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
        //画阴影
        val color = mPaint.color
        mPaint.color = Color.WHITE
        mPaint.setShadowLayer(15f,0f,8f,Color.GRAY)
        canvas.drawRect(
            mOffsetX.toFloat(), mOffsetY.toFloat(),
            (realWidth + mOffsetX).toFloat(), (realHeight + mOffsetY).toFloat(),mPaint)
        mPaint.color = color
        mPaint.setShadowLayer(0f,0f,0f,Color.GRAY)

        mBackground?.apply {
            val matrix = Matrix()
            matrix.setScale(realWidth.toFloat() / width.toFloat(),realHeight.toFloat() / height.toFloat())
            canvas.save()
            canvas.translate(mOffsetX.toFloat(),mOffsetY.toFloat())
            canvas.drawBitmap(this,matrix,null)
            canvas.restore()
            matrix.reset()
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

                deleteBackground()
            }
        }else{
            if (mBackground != null){
                mBackground!!.recycle()
            }
            mBackground = bg
            saveBackground()
        }
    }
    private fun saveBackground(){
        val imgFile = File(getBackgroundFileName())
        FileOutputStream(imgFile).use {
            mBackground!!.compress(Bitmap.CompressFormat.JPEG,100,it)
            it.flush()
        }
    }
    private fun deleteBackground(){
        val imgFile = File(getBackgroundFileName())
        imgFile.deleteOnExit()
    }

    private fun getBackgroundFileName():String{
        return String.format("%s%s.jpg",CustomApplication.getSaveDirectory("hzYunPos/label"),String.format("%d_%d",physicsWidth,physicsHeight))
    }
    private fun addItem(item: ItemBase){
        if (mCurItem != null){
            mCurItem!!.disableItem()
        }
        item.activeItem()
        item.measure(realWidth, realHeight,mPaint)
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
        addItem(BarcodeItem(BarcodeFormat.QR_CODE))
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


    companion object{
        const val w_70 = 70
        const val h_40 = 40
        const val w_50 = 50
    }
}