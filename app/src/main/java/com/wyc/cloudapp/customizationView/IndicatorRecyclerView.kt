package com.wyc.cloudapp.customizationView

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.Utils
import kotlinx.coroutines.*
import java.lang.Runnable
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.*

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.customizationView
 * @ClassName:      CusRecyclerView
 * @Description:    提示存在多于一页的数据RecyclerView
 * @Author:         wyc
 * @CreateDate:     2021-12-02 17:22
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-12-02 17:22
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
open class IndicatorRecyclerView(context: Context, attrs: AttributeSet?, defStyleAttr: Int): RecyclerView(context,attrs,defStyleAttr),
    CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val mPaint: Paint = Paint()
    protected var downX = 0f
    private var downY = 0f
    private var mTouchSlop = 0
    /**
     * 第一位表示是否需要绘制指示符 第二位表示是否按下屏幕 第三位表示是否绘制头部指示符 第四位表示是否绘制尾部指示符
     * 第五位 1表示左滑动 第六位 1表示右滑动 第7位 1表示上滑动 第8位 1表示下滑动 第9位 1表示终止加载 第10位 1表示表示加载完成
     * 第11位 1表示可以继续加载 第12位 1表示准备加载，在这种状态下释放按键后会进入加载状态
     * */
    @Volatile
    private var mNeedIndicator:Int = 2

    /* 更多的标识符参数 **/
    private val mValueAnimator = ValueAnimator()
    private val mOffsetX:Float
    private val mOffsetY:Float
    private var mSpace = CustomApplication.getDimension(R.dimen.size_4).toInt()
    private var mHeadAxisX:Float = 0.0f
    private var mHeadAxisY:Float = 0.0f
    private var mTailAxisX:Float = 0.0f
    private var mTailAxisY:Float = 0.0f
    /**/

    private var mLoadAnimStatus = AtomicBoolean(false)
    /**用于移动标识坐标*/
    private var mIndicatorMoveY = 0
    private var mIndicatorMoveX = 0
    private val mIndicatorMaxOffset = CustomApplication.getDimension(R.dimen.size_68).toInt()
    private var mChildOffset = 0
    /**/

    private var mStartAngle = 0f
    private val mLoadIndicatorPoint = PointF()

    private var mLoading = AtomicBoolean(false)

    init {
        mPaint.isAntiAlias = true
        mPaint.color = Color.RED
        mPaint.strokeWidth = Utils.dpToPx(context,1f).toFloat()
        mPaint.style = Paint.Style.STROKE
        mPaint.setShadowLayer(3f,0f,0f,mPaint.color)
        mPaint.alpha = 168

        val lineLen = CustomApplication.getDimension(R.dimen.size_6)
        val radian = Math.PI / 180 * 45
        mOffsetX = (cos(radian) * lineLen).toFloat()
        mOffsetY = (sin(radian) * lineLen).toFloat()

        initIndicatorAnimator()

        overScrollMode = OVER_SCROLL_NEVER

        mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }
    constructor(context: Context):this(context,null,0)
    constructor(context: Context, attrs: AttributeSet?) : this(context,attrs,0)

    override fun draw(c: Canvas) {
        super.draw(c)
        drawIndicator(c)
        drawLoadAnim(c)
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (changed){
            calculate(layoutManager as? LinearLayoutManager)
            initLoadIndicatorPoint()
        }
    }

    private fun initIndicatorAnimator(){
        mValueAnimator.repeatCount = ValueAnimator.INFINITE
        mValueAnimator.duration = 2000
        mValueAnimator.interpolator = AccelerateDecelerateInterpolator()
        mValueAnimator.setIntValues(CustomApplication.getDimension(R.dimen.size_8).toInt(),mSpace,
            CustomApplication.getDimension(R.dimen.size_8).toInt()
        )

        mValueAnimator.addUpdateListener {
            mSpace = it.animatedValue as Int
            invalidate()
        }
    }

    private fun initLoadIndicatorPoint(){
        if (hasContinueLoad()){
            val diameter = CustomApplication.getDimension(R.dimen.size_18).toInt()
            val radius = diameter shr 1
            if (isVerOrientation()){
                mLoadIndicatorPoint.x = ((width - radius)  shr  1).toFloat()
                mLoadIndicatorPoint.y = radius.toFloat()
            }else{
                mLoadIndicatorPoint.x = radius.toFloat()
                mLoadIndicatorPoint.y = ((height - radius)  shr  1).toFloat()
            }
        }
    }

    private fun drawIndicator(c: Canvas){
        if (hasShowIndicator()){
            drawHeadIndicator(c)
            drawTailIndicator(c)
        }
    }
    private fun drawHeadIndicator(c: Canvas){
        if (hasHeadIndicator()){
            drawIcon(c,mHeadAxisX,mHeadAxisY,true)
        }
    }
    private fun drawTailIndicator(c: Canvas){
        if(hasTailIndicator()){
            drawIcon(c,mTailAxisX,mTailAxisY,false)
        }
    }

    private fun headIndicatorFlag(code: Boolean){
        if (hasHeadIndicator() != code){
            mNeedIndicator = if (code)
                mNeedIndicator or 4
            else mNeedIndicator and 4.inv()
        }
    }

    private fun hasHeadIndicator():Boolean{
        return mNeedIndicator and 4 == 4
    }

    private fun tailIndicatorFlag(code: Boolean){
        if (hasTailIndicator() != code){
            mNeedIndicator = if (code)
                mNeedIndicator or 8
            else mNeedIndicator and 8.inv()
        }
    }
    private fun hasTailIndicator():Boolean{
        return mNeedIndicator and 8 == 8
    }

    private fun drawLoadAnim(c: Canvas){
        if (hasLoadIndicator()){
            drawRaindrop(c)
        }
    }

    private fun hasLoadIndicator():Boolean{
        return (mLoading.get() || (mIndicatorMoveY != 0 && isVerOrientation() && (hasSlideDown() && !hasHeadIndicator() || hasSlideUp() && !hasTailIndicator()) ||
                        mIndicatorMoveX != 0 && isHorOrientation() &&(hasSlideLeft() && !hasTailIndicator() || hasSlideRight() && !hasHeadIndicator())))
    }
    private fun hasEnterLoad():Boolean{
        return hasContinueLoad() && (!hasHeadIndicator() ||!hasTailIndicator())
    }

    private fun drawRaindrop(c: Canvas){
        val orientationUp = hasSlideUp()
        val orientationLeft = hasSlideLeft()
        val indicatorMoveX = mIndicatorMoveX
        val indicatorMoveY = mIndicatorMoveY

        val centrePointX = if (isHorOrientation()){
            if (orientationLeft)width - mLoadIndicatorPoint.x - indicatorMoveX else mLoadIndicatorPoint.x + indicatorMoveX
        }else{
            mLoadIndicatorPoint.x
        }

        val centrePointY = if (isVerOrientation()){
            if (orientationUp)height - mLoadIndicatorPoint.y - indicatorMoveY else mLoadIndicatorPoint.y + indicatorMoveY
        }else{
            mLoadIndicatorPoint.y
        }
        val radius = if (isVerOrientation()) mLoadIndicatorPoint.y else mLoadIndicatorPoint.x

        val controlX = radius
        val controlY = radius


        val leftPointX = if (isHorOrientation()){
            (if (orientationLeft)centrePointX else centrePointX  - indicatorMoveX ) - radius
        }  else centrePointX - radius
        val leftPointY = centrePointY


        val firstControlX = centrePointX - controlX
        val firstControlY = centrePointY - controlY


        val upPointX = centrePointX
        val upPointY = (if(orientationUp) centrePointY else centrePointY  - indicatorMoveY) - radius


        val secControlX = centrePointX + controlX
        val secControlY = centrePointY - controlY


        val rightPointX = if (isHorOrientation()) {
                (if (orientationLeft)centrePointX + indicatorMoveX else centrePointX) + radius
        }  else centrePointX + radius
        val rightPointY = centrePointY


        val thirdControlX = centrePointX + controlX
        val thirdControlY = centrePointY + controlY


        val downPointX = centrePointX
        val downPointY = (if(orientationUp) centrePointY + indicatorMoveY else centrePointY) + radius


        val forthControlX = centrePointX - controlX
        val forthControlY = centrePointY + controlY


        mPaint.color = Color.BLUE
        mPaint.style = Paint.Style.FILL

        val path = Path()
        path.moveTo(leftPointX, leftPointY)
        path.quadTo(firstControlX,firstControlY,upPointX,upPointY)
        path.lineTo(upPointX,upPointY)
        path.quadTo(secControlX,secControlY,rightPointX,rightPointY)
        path.lineTo(rightPointX,rightPointY)
        path.quadTo(thirdControlX,thirdControlY,downPointX,downPointY)
        path.lineTo(downPointX,downPointY)
        path.quadTo(forthControlX,forthControlY,leftPointX,leftPointY)
        c.drawPath(path,mPaint)

        drawLoadIndicator(c,centrePointX,centrePointY)
    }
    private fun drawLoadIndicator(c: Canvas,x:Float,y:Float){
        val loadIndicator = CustomApplication.getDimension(R.dimen.size_6)
        mPaint.color = Color.YELLOW
        mPaint.style = Paint.Style.STROKE
        c.drawArc(x - loadIndicator,y - loadIndicator,x + loadIndicator,y + loadIndicator,mStartAngle,270f,false,mPaint)
        mPaint.color = Color.RED
    }

    private fun drawIcon(c: Canvas, xAxis:Float, yAxis:Float,reverse:Boolean){
        val space = mSpace
        val x = mOffsetX
        val y = mOffsetY
        val path = Path()
        if (isVerOrientation()){
            path.moveTo(xAxis - x,yAxis - y)
            path.lineTo(xAxis,yAxis)
            path.lineTo(xAxis + x,yAxis - y)

            path.moveTo(xAxis - x,yAxis - y - space)
            path.lineTo(xAxis,yAxis - space)
            path.lineTo(xAxis + x,yAxis - y - space)

            path.moveTo(xAxis - x,yAxis - y - space * 2)
            path.lineTo(xAxis,yAxis - space * 2)
            path.lineTo(xAxis + x,yAxis - y - space * 2)
        }else {

            path.moveTo(xAxis - y,yAxis + x)
            path.lineTo(xAxis,yAxis)
            path.lineTo(xAxis - y,yAxis - x)

            path.moveTo(xAxis - y - space,yAxis + x)
            path.lineTo(xAxis - space,yAxis)
            path.lineTo(xAxis - y - space,yAxis - x)

            path.moveTo(xAxis - y - space * 2,yAxis + x)
            path.lineTo(xAxis - space * 2,yAxis)
            path.lineTo(xAxis - y - space * 2,yAxis - x)
        }
        if (reverse){
            c.save()
            if (isVerOrientation())
                c.scale(1f,-1f)
            else
                c.scale(-1f,1f)

            c.drawPath(path,mPaint)
            c.restore()
        }else
            c.drawPath(path,mPaint)
    }

    private fun resetChildLocation(){
        mChildOffset = 0
        mIndicatorMoveY = 0
        mIndicatorMoveX = 0
        if (hasSlideUp()){
            scrollToPosition(layoutManager?.itemCount?:0)
            tailIndicatorFlag(false)
        }else if (hasSlideDown()) {
            scrollToPosition(0)
            headIndicatorFlag(false)
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when(ev.action){
            MotionEvent.ACTION_DOWN ->{
                pointerDown()
                downX = ev.x
                downY = ev.y
                finishLoadFlag(false)
                abortedLoadFlag(false)
            }
            MotionEvent.ACTION_UP ->{
                pointerUp()
                startLoad()
                resetChildLocation()
                touchMove(false)
            }
            MotionEvent.ACTION_CANCEL ->{
                pointerUp()
            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = ev.x
                val moveY = ev.y
                val xDiff = abs(moveX - downX)
                val yDiff = abs(moveY - downY)

                if (xDiff > mTouchSlop || yDiff > mTouchSlop ) {
                    val squareRoot = sqrt((xDiff * xDiff + yDiff * yDiff).toDouble())
                    val degreeX = asin(yDiff / squareRoot) * 180 / Math.PI
                    val degreeY = asin(xDiff / squareRoot) * 180 / Math.PI
                    if (degreeX < 45){
                        slideLeft(moveX < downX)
                        slideRight(moveX > downX)
                    }
                    if (degreeY < 45){
                        slideUp(moveY < downY)
                        slideDown(moveY > downY)
                    }
                }

                if (hasEnterLoad()){
                    if (isHorOrientation()){
                        if (mChildOffset == 0){
                            mChildOffset = moveX.toInt()
                        } else{
                            if (!hasHeadIndicator()){
                                mIndicatorMoveX = layoutManager?.getChildAt(0)?.left?:0
                            }else if (!hasTailIndicator()){
                                mIndicatorMoveX = width - (layoutManager?.getChildAt(layoutManager!!.childCount - 1)?.right?:0)
                            }
                            if (mIndicatorMoveX < mIndicatorMaxOffset){
                                offsetChildrenHorizontal((moveX - mChildOffset).toInt())
                            }
                            mChildOffset = 0
                        }
                    }else{
                        if (mChildOffset == 0){
                            mChildOffset = moveY.toInt()
                        } else{
                            if (!hasHeadIndicator()){
                                mIndicatorMoveY = layoutManager?.getChildAt(0)?.top?:0
                            }else if (!hasTailIndicator()){
                                mIndicatorMoveY = height - (layoutManager?.getChildAt(layoutManager!!.childCount - 1)?.bottom?:0)
                            }
                            if (mIndicatorMoveY < mIndicatorMaxOffset){
                                offsetChildrenVertical((moveY - mChildOffset).toInt())
                            }
                            mChildOffset = 0
                        }
                    }
                    if (mIndicatorMoveY > mIndicatorMaxOffset shr 1 || mIndicatorMoveX > mIndicatorMaxOffset shr 1){
                        touchMove(true)
                    }else {
                        touchMove(false)
                    }
                    invalidate()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun slideLeft(code:Boolean){
        if (hasSlideLeft() != code){
            mNeedIndicator = if (code)
                mNeedIndicator or 16
            else mNeedIndicator and 16.inv()
        }
    }
    private fun slideRight(code:Boolean){
        if (hasSlideRight() != code){
            mNeedIndicator = if (code)
                mNeedIndicator or 32
            else mNeedIndicator and 32.inv()
        }
    }

    private fun slideUp(code:Boolean){
        if (hasSlideUp() != code){
            mNeedIndicator = if (code)
                mNeedIndicator or 64
            else mNeedIndicator and 64.inv()
        }
    }
    private fun slideDown(code:Boolean){
        if (hasSlideDown() != code){
            mNeedIndicator = if (code)
                mNeedIndicator or 128
            else mNeedIndicator and 128.inv()
        }
    }
    protected fun hasSlideLeft():Boolean{
        return (mNeedIndicator and 16) == 16
    }
    protected fun hasSlideRight():Boolean{
        return (mNeedIndicator and 32) == 32
    }
    private fun hasSlideUp():Boolean{
        return (mNeedIndicator and 64) == 64
    }
    private fun hasSlideDown():Boolean{
        return (mNeedIndicator and 128) == 128
    }

    private fun hasAborted():Boolean{
        return (mNeedIndicator and 256) == 256
    }
    private fun abortedLoadFlag(code:Boolean){
        mNeedIndicator = if (code)
            mNeedIndicator or 256
        else mNeedIndicator and 256.inv()
    }

    private fun finishLoadFlag(code:Boolean){
        mNeedIndicator = if (code)
            mNeedIndicator or 512
        else mNeedIndicator and 512.inv()
    }
    private fun finishLoad(){
        mLoading.set(false)
        finishLoadFlag(true)
    }
    private fun hasFinishLoad():Boolean{
        return (mNeedIndicator and 512) == 512
    }
    private fun continueFlag(code:Boolean){
        mNeedIndicator = if (code)
            mNeedIndicator or 1024
        else mNeedIndicator and 1024.inv()
    }
    private fun hasContinueLoad():Boolean{
        return (mNeedIndicator and 1024) == 1024 && (adapter as OnLoad).continueLoad()
    }

    private fun touchMove(code:Boolean){
        if (isTouchMove() != code){
            mNeedIndicator = if (code) {
                startLoadAnim()
                mNeedIndicator or 2048
            }else {
                if (!mLoading.get())cancelLoadAnim()
                mNeedIndicator and 2048.inv()
            }
        }
    }

    private fun isTouchMove():Boolean{
        return (mNeedIndicator and 2048) == 2048
    }

    protected fun clearLeftRight(){
        slideLeft(false)
        slideRight(false)
    }

    private fun clearUpDown(){
        slideUp(false)
        slideDown(false)
    }

    private fun pointerDown(){
        if (hasNotScrollBar()){
            removeCallbacks(disableRunnable)
            mNeedIndicator = (mNeedIndicator or 2)
            invalidate()
        }
    }
    private fun pointerUp(){
        if (hasNotScrollBar()) {
            removeCallbacks(disableRunnable)
            postDelayed(disableRunnable, 3000)
        }
    }

    private val disableRunnable = Runnable {
        mNeedIndicator = mNeedIndicator and 2.inv()
    }

    private fun hasShowIndicator():Boolean{
        var code = false
        if (hasNotScrollBar()){
            code = (mNeedIndicator and 3) == 3
            if (code){
                startAnim()
            }else {
                cancelAnim()
            }
        }
        return code
    }

    private val scrollListener = object: RecyclerView.OnScrollListener() {
         override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
             checkPosition()
        }
    }

    private fun checkPosition(){
        (layoutManager as? LinearLayoutManager)?.apply {
            if (mNeedIndicator and 1 != 1){
                calculate(this)
            }
            headIndicatorFlag(findFirstCompletelyVisibleItemPosition() > 0)
            tailIndicatorFlag(findLastCompletelyVisibleItemPosition() + 1 < itemCount)
        }
    }

    private fun calculate(manager: LinearLayoutManager?){
        if (hasNotScrollBar()){
            manager?.apply {
                val lastVisibility = findLastVisibleItemPosition()
                val firstVisibility = findFirstVisibleItemPosition()

                if (itemCount != 0 && (lastVisibility + 1 != itemCount || firstVisibility != 0)){
                    var xAxis:Float
                    var yAxis:Float
                    val vertical = orientation == VERTICAL
                    if (vertical){
                        xAxis = measuredWidth.toFloat() - mOffsetX * 2
                        yAxis = measuredHeight.toFloat()
                    }else{
                        xAxis = measuredWidth.toFloat()
                        yAxis = measuredHeight.toFloat() - mOffsetY * 2
                    }
                    mTailAxisX = xAxis
                    mTailAxisY = yAxis

                    if (vertical){
                        xAxis = measuredWidth.toFloat() - mOffsetX * 2
                        yAxis = 0f
                    }else{
                        xAxis = 0f
                        yAxis = measuredHeight.toFloat() - mOffsetY * 2
                    }
                    mHeadAxisX = xAxis
                    mHeadAxisY = yAxis

                    mNeedIndicator = mNeedIndicator or  1
                    tailIndicatorFlag(true)
                    postDelayed(disableRunnable,3000)
                }
            }
        }
    }

    private fun hasVerScrollBar():Boolean{
        return isVerOrientation() && isVerticalScrollBarEnabled
    }
    private fun hasHorScrollBar():Boolean{
        return isHorOrientation() && isHorizontalScrollBarEnabled
    }
    private fun hasNotScrollBar():Boolean{
        return !hasHorScrollBar() && !hasVerScrollBar()
    }
    private fun isVerOrientation():Boolean{
        return (layoutManager as? LinearLayoutManager)?.orientation == VERTICAL
    }
    private fun isHorOrientation():Boolean{
        return (layoutManager as? LinearLayoutManager)?.orientation == HORIZONTAL
    }

    private fun cancelAnim(){
        if (mValueAnimator.isRunning)mValueAnimator.cancel()
    }
    private fun startAnim(){
        if(!mValueAnimator.isStarted){
            mValueAnimator.start()
        }
    }

    private fun startLoad(){
        if (isTouchMove()){
            if (!hasAborted() && hasContinueLoad()){
                if (mLoading.compareAndSet(false,true)){
                    launch {
                        (adapter as OnLoad).onLoad(if (isHorOrientation() && hasSlideRight() || isVerOrientation() && hasSlideDown()) OnLoad.LOADMODE.FRONT else OnLoad.LOADMODE.BEHIND)
                        endLoad()
                        withContext(Dispatchers.Main){
                            if (isVerOrientation() && hasSlideUp())
                                scrollBy(0,28)
                            else if (isHorOrientation() && hasSlideLeft()){
                                scrollBy(28 ,0)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun endLoad(){
        Logger.d("endLoad")
        finishLoad()
        cancelLoadAnim()
    }
    private fun abortLoad(){
        Logger.d("abortedLoad:%s",hasAborted())
        abortedLoadFlag(true)

        if (mLoading.get()){
            (adapter as? OnLoad)?.onAbort()
        }

        finishLoad()
        cancelLoadAnim()
    }

    private fun cancelLoadAnim(){
        mLoadAnimStatus.set(false)
    }
    private fun startLoadAnim(){
        if (mLoadAnimStatus.compareAndSet(false,true)){
            launch{
                while (isActive && mLoadAnimStatus.get()) {
                    delay(50)
                    if (!mLoadAnimStatus.get()) break
                    if (mStartAngle > 360f)mStartAngle = 0f
                    mStartAngle += 20f
                    postInvalidate()
                }
            }
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        addOnScrollListener(scrollListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelAnim()
        abortLoad()
        cancel()
    }

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        continueFlag((adapter as? OnLoad)?.continueLoad()?:false)
    }

    /**
     * orientation FRONT表示加载的数据应该在前面显示 BEHIND表示加载的数据应该在后面显示 OVER覆盖现有数据
     * */
    interface OnLoad{
        enum class LOADMODE {
            FRONT,
            BEHIND,
            OVER
        }
        /**
         * 需要同步返回
         * */
        fun onLoad(loadMode:LOADMODE)
        /**
         * true可以继续加载 false数据已经加载完毕
         * */
        fun continueLoad():Boolean
        fun onAbort()
    }
}