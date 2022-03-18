package com.wyc.cloudapp.design

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.Utils
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max


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

class LayoutView: View {
    private val mOffsetX = context.resources.getDimension(R.dimen.size_14)
    private val mOffsetY = context.resources.getDimension(R.dimen.size_14)

    var physicsWidth = w_70
    var physicsHeight = h_40

    private val contentWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, physicsWidth.toFloat(),context.resources.displayMetrics)
    private val contentHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, physicsHeight.toFloat(),context.resources.displayMetrics)

    private var realWidth = 1.0f
    private var realHeight = 1.0f

    private val contentList = mutableListOf<ItemBase<*>>()
    private val mPaint = Paint()

    private val mTextBound = Rect()
    private var mBackground:Bitmap?  = null
    private val mMatrix = Matrix()

    private var mCurItem:ItemBase<*>? = null

    private var mLastX = 0f
    private var mLastY = 0f
    private var mMoveX = 0f
    private var mMoveY = 0f

    private val mCurRect = RectF()
    private val mActionRadius = context.resources.getDimension(R.dimen.size_3)

    constructor(context: Context):this(context, null)
    constructor(context: Context, attrs: AttributeSet?):this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr){
        initPaint()
        generateBackground()
        test()
    }
    private fun initPaint(){
        mPaint.isAntiAlias = true
        /*绘制Drawable边框时会模糊，所以要关闭硬件加速*/
        setLayerType(LAYER_TYPE_SOFTWARE, null)
    }
    private fun generateBackground(){
        mBackground = BitmapFactory.decodeFile(getBackgroundFileName())
    }
    private fun test(){
        var item = TextItem()
        item.content = "gAG吴9"
        item.width = 128
        item.height = 88
        addTextItem(item)

        item = TextItem()
        item.content = "wypg舞"
        addTextItem(item)
    }

    private fun addTextItem(item:TextItem){
        if (Utils.isNotEmpty(item.content)){
            val o = mPaint.textSize
            val bound = Rect()
            mPaint.textSize = item.fontSize
            mPaint.getTextBounds(item.content,0,item.content!!.length,bound)
            mPaint.textSize = o

            item.width = max(item.width,bound.width())
            item.height = max(item.height,bound.height())
            contentList.add(item)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN->{
                if (activeItem(event.x,event.y))return true
            };
            MotionEvent.ACTION_MOVE->{
                moveCurItem(event.x,event.y)
            };
            MotionEvent.ACTION_UP->{
                releaseCurItem()
            };
        }
        return super.dispatchTouchEvent(event)
    }
    private fun activeItem(clickX:Float, clickY:Float):Boolean{
        for (i in contentList.size -1 downTo 0){
            val it = contentList[i]
            if (it.hasSelect(clickX - mOffsetX,clickY - mOffsetY)){
                mLastX = clickX
                mLastY = clickY
                if (mCurItem != it){
                    it.active = true
                    mCurItem = it
                }
                checkDeleteClick(clickX,clickY)
                return true
            }
        }
        if (mCurItem != null){
            mCurItem!!.active = false
            mCurItem = null
            invalidate()
        }
        return false
    }
    private fun checkDeleteClick(clickX:Float,clickY:Float){
        Logger.d("clickX:%f,clickY:%f,rect.left:%f,mCurRect.top:%f,mActionRadius:%f",clickX,clickY,mCurRect.left,mCurRect.top,mActionRadius)
        if (clickX >= mCurRect.left && clickX <=mCurRect.left + mActionRadius * 2
            && clickY >= mCurRect.top && clickY <= mCurRect.top + mActionRadius * 2){
            contentList.remove(mCurItem)
            mCurItem = null
            invalidate()
        }
    }
    private fun checkScaleClick(clickX:Float,clickY:Float):Boolean{
        if (mCurItem!!.scaling || clickX >= mCurRect.right - mActionRadius * 2  && clickX <=mCurRect.right
            && clickY >= mCurRect.bottom - mActionRadius * 2 && clickY <= mCurRect.bottom){

            mCurItem!!.width += mMoveX.toInt()
            mCurItem!!.height += mMoveY.toInt()

            mCurItem!!.scaling = true

            return true
        }
        return false
    }
    private fun moveCurItem(clickX:Float, clickY:Float){
        Logger.d(mCurItem)
        mCurItem?.let {

            mMoveX = clickX - mLastX
            mMoveY = clickY - mLastY

            mLastX = clickX
            mLastY = clickY

            if (!checkScaleClick(clickX,clickY)){
                val edgeX = clickX - it.left + mOffsetX
                val edgeY = clickY - it.top + mOffsetY
                it.move = true
                if (edgeX >= 0 && edgeX + it.width <=  realWidth){
                    it.left += mMoveX
                }
                if (edgeY >= 0 && edgeY + it.height <= realHeight){
                    it.top += mMoveY
                }
            }
            invalidate()
        }
    }

    private fun releaseCurItem(){
        mCurItem?.let {
            it.move = false
            it.scaling = false
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
                resultHeightSize = contentHeight.toInt()
            }
            MeasureSpec.UNSPECIFIED -> {
                resultHeightSize = contentHeight.toInt()
            }
        }
        when(widthSpec){
            MeasureSpec.EXACTLY -> {
                resultWidthSize = widthSize
            }
            MeasureSpec.AT_MOST -> {
                resultWidthSize = contentWidth.toInt()
            }
            MeasureSpec.UNSPECIFIED -> {
                resultWidthSize = contentWidth.toInt()
            }
        }
        super.onMeasure(MeasureSpec.makeMeasureSpec(resultWidthSize,widthSpec), MeasureSpec.makeMeasureSpec(resultHeightSize,heightSpec))
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (!isInEditMode)Logger.d("contentWidth:%f,contentHeight:%f,measuredWidth:%d,measuredHeight:%d",
            contentWidth,contentHeight,measuredWidth,measuredHeight)

        if(changed){
            val rightMargin = context.resources.getDimension(R.dimen.size_5)
            realWidth = measuredWidth.toFloat() - mOffsetX - rightMargin
            realHeight = (realWidth / contentWidth) * contentHeight
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.apply {
            drawColor(context.resources.getColor(R.color.white,null))
            drawRule(this)
            drawBackground(this)


            drawContent(this)

            drawAction(this)
        }
    }
    private fun drawAction(canvas: Canvas) {
        mCurItem?.let {
            if (it.active){
                val l = mOffsetX + it.left
                val t = mOffsetX + it.top
                val rc = Utils.dpToPxF(CustomApplication.self(),2f)

                mCurRect.set(l,t,l + it.width,t + it.height)

                mPaint.color = Color.BLUE
                mPaint.style = Paint.Style.STROKE
                canvas.drawRoundRect(mCurRect,rc,rc,mPaint)

                val leftCenterX = l + mActionRadius
                val topCenterY = t + mActionRadius

                val rightCenterX = mCurRect.right - mActionRadius
                val bottomCenterY = mCurRect.bottom - mActionRadius

                mPaint.color = Color.RED
                mPaint.style = Paint.Style.FILL
                /* 左上角删除按钮 */
                canvas.drawCircle(leftCenterX,topCenterY,mActionRadius,mPaint)
                /*右下角缩放按钮*/
                canvas.drawCircle(rightCenterX,bottomCenterY,mActionRadius,mPaint)

                //删除图标
                mPaint.color = Color.WHITE
                canvas.save()
                canvas.rotate(45f,leftCenterX,topCenterY)
                canvas.scale(0.6f,0.6f,leftCenterX,topCenterY)
                canvas.drawLine(leftCenterX,topCenterY - mActionRadius  ,leftCenterX,topCenterY + mActionRadius,mPaint)
                canvas.drawLine(leftCenterX - mActionRadius,topCenterY,leftCenterX + mActionRadius,topCenterY,mPaint)
                canvas.restore()

                //缩放图标
                canvas.save()
                canvas.rotate(45f,rightCenterX,bottomCenterY)
                canvas.scale(0.6f,0.6f,rightCenterX,bottomCenterY)
                canvas.drawLine(rightCenterX - mActionRadius,bottomCenterY,rightCenterX + mActionRadius,bottomCenterY,mPaint)
                val hypotenuse = Utils.dpToPx(context,3f)
                val offset  = hypotenuse * 0.5f
                canvas.drawLine(rightCenterX - mActionRadius,bottomCenterY,rightCenterX - mActionRadius + offset,bottomCenterY - offset,mPaint)
                canvas.drawLine(rightCenterX - mActionRadius,bottomCenterY,rightCenterX - mActionRadius + offset,bottomCenterY + offset,mPaint)
                canvas.drawLine(rightCenterX + mActionRadius,bottomCenterY,rightCenterX + mActionRadius - offset,bottomCenterY - offset,mPaint)
                canvas.drawLine(rightCenterX + mActionRadius,bottomCenterY,rightCenterX + mActionRadius - offset,bottomCenterY + offset,mPaint)
                canvas.restore()
            }
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
        for (i in 0..physicsWidth){
            num = i.toString()
            coordinate = if (i == physicsWidth){
                i * perGap + mOffsetX - mPaint.strokeWidth
            }else
                i * perGap + mOffsetX

            if (i % 10 == 0){
                canvas.drawLine(coordinate,mOffsetY,coordinate,mOffsetY - lineHeight * 2,mPaint)
                canvas.drawText(num,coordinate - mPaint.measureText(num) / 2,mOffsetY - lineHeight * 2,mPaint)
            }else
                canvas.drawLine(coordinate,mOffsetY,coordinate,mOffsetY - lineHeight,mPaint)
        }
        canvas.drawLine(mOffsetX,mOffsetY, realWidth + mOffsetX,mOffsetY,mPaint)

        perGap = realHeight / physicsHeight.toFloat()
        for (i in 0..physicsHeight){
            num = i.toString()
            coordinate = if (i == physicsHeight){
                i * perGap + mOffsetY - mPaint.strokeWidth
            }else
                i * perGap + mOffsetY

            if (i % 10 == 0) {
                canvas.drawLine(mOffsetY, coordinate, mOffsetY - lineHeight * 2, coordinate, mPaint)

                mPaint.getTextBounds(num,0,num.length,mTextBound)
                canvas.save()
                canvas.rotate(-90f,mOffsetY - lineHeight * 2 ,coordinate)
                canvas.drawText(num,mOffsetY - lineHeight * 2 - mPaint.measureText(num) / 2,coordinate,mPaint)
                canvas.restore()
            }else
                canvas.drawLine(mOffsetY,coordinate,mOffsetY - lineHeight,coordinate,mPaint)
        }
        canvas.drawLine(mOffsetX,mOffsetY, mOffsetX, realHeight + mOffsetY,mPaint)
    }
    private fun drawBackground(canvas: Canvas){
        //画阴影
        val color = mPaint.color
        mPaint.color = Color.WHITE
        mPaint.setShadowLayer(15f,0f,8f,Color.GRAY)
        canvas.drawRect(mOffsetX,mOffsetY,realWidth + mOffsetX,realHeight + mOffsetY,mPaint)
        mPaint.color = color
        mPaint.setShadowLayer(0f,0f,0f,Color.GRAY)

        mBackground?.apply {
            mMatrix.setScale(realWidth / width,realHeight / height)
            canvas.save()
            canvas.translate(mOffsetX,mOffsetY)
            canvas.drawBitmap(this,mMatrix,null)
            canvas.restore()
            mMatrix.reset()
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

    private fun drawContent(canvas: Canvas){
        if (contentList.isNotEmpty()){
            canvas.clipRect(mOffsetX,mOffsetY,realWidth + mOffsetX,realHeight + mOffsetY)
            contentList.forEach {
                it.draw(mOffsetX,mOffsetY,canvas,mPaint)
            }
        }
    }

    companion object{
        const val w_70 = 70
        const val h_40 = 40
        const val w_50 = 50
    }
}