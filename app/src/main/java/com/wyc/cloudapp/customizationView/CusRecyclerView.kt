package com.wyc.cloudapp.customizationView

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.utils.Utils
import kotlin.math.cos
import kotlin.math.sin

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
open class CusRecyclerView(context: Context, attrs: AttributeSet?, defStyleAttr: Int): RecyclerView(context,attrs,defStyleAttr) {
    private val mPaint: Paint = Paint()
    private val mValueAnimator = ValueAnimator()
    /**
     * 第一位表示是否需要绘制指示符 第二位表示是否按下屏幕 第三位表示是否绘制头部指示符 第四位表示是否绘制尾部指示符
     * */
    @Volatile
    private var mNeedIndicator:Int = 2

    private val mOffsetX:Float
    private val mOffsetY:Float
    private var mSpace = 6
    private var mVerOrientation = true
    private var mHeadAxisX:Float = 0.0f
    private var mHeadAxisY:Float = 0.0f
    private var mTailAxisX:Float = 0.0f
    private var mTailAxisY:Float = 0.0f

    init {
        mPaint.isAntiAlias = true
        mPaint.color = Color.RED
        mPaint.strokeWidth = Utils.dpToPx(context,1f).toFloat()
        mPaint.style = Paint.Style.STROKE
        mPaint.setShadowLayer(5f,0f,0f,Color.RED)
        mPaint.alpha = 168

        val lineLen = Utils.dpToPx(CustomApplication.self(),6f)
        val radian = Math.PI / 180 * 45
        mOffsetX = (cos(radian) * lineLen).toFloat()
        mOffsetY = (sin(radian) * lineLen).toFloat()

        mValueAnimator.repeatCount = ValueAnimator.INFINITE
        mValueAnimator.duration = 2000
        mValueAnimator.interpolator = AccelerateDecelerateInterpolator()
        mValueAnimator.setIntValues(12,6,12)
        mValueAnimator.addUpdateListener {
            mSpace = it.animatedValue as Int
            invalidate()
        }
    }
    constructor(context: Context):this(context,null,0)
    constructor(context: Context, attrs: AttributeSet?) : this(context,attrs,0)

    override fun draw(c: Canvas) {
        super.draw(c)
        drawIndicator(c)
    }

    private fun drawIndicator(c: Canvas){
        if (hasShowIndicator()){
            drawHeadIndicator(c)
            drawTailIndicator(c)
        }
    }
    private fun drawHeadIndicator(c: Canvas){
        if (mNeedIndicator and 4 != 0){
            drawIcon(c,mHeadAxisX,mHeadAxisY,true)
        }
    }
    private fun drawTailIndicator(c: Canvas){
        if(mNeedIndicator and 8 != 0){
            drawIcon(c,mTailAxisX,mTailAxisY,false)
        }
    }
    private fun drawLoadIndicator(c: Canvas){

    }

    private fun drawIcon(c: Canvas, xAxis:Float, yAxis:Float,reverse:Boolean){
        val space = mSpace
        val x = mOffsetX
        val y = mOffsetY
        val path = Path()
        if (mVerOrientation){
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
            if (mVerOrientation)
                c.scale(1f,-1f)
            else
                c.scale(-1f,1f)

            c.drawPath(path,mPaint)
            c.restore()
        }else
            c.drawPath(path,mPaint)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when(ev.action){
            MotionEvent.ACTION_DOWN ->{
                removeCallbacks(disableRunnable)
                mNeedIndicator = (mNeedIndicator or 2)
                invalidate()
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{
                removeCallbacks(disableRunnable)
                postDelayed(disableRunnable,3000)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private val disableRunnable = Runnable {
        mNeedIndicator = (mNeedIndicator and 2.inv())
    }

    private fun hasShowIndicator():Boolean{
        val code = (mNeedIndicator and 3) == 3
        if (hasNotScrollBar()){
            if (code){
                startAnim()
            }else {
                cancelAnim()
            }
        }
        return code
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        if (changed && hasNotScrollBar()){
            (layoutManager as? LinearLayoutManager)?.apply{
                calculate(this)
            }
        }
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

            mNeedIndicator = if (findLastVisibleItemPosition() + 1 < itemCount){
                mNeedIndicator or 8
            }else {
                mNeedIndicator and 8.inv()
            }
            mNeedIndicator = if (findFirstVisibleItemPosition() > 0){
                mNeedIndicator or 4
            }else{
                mNeedIndicator and 4.inv()
            }
        }
    }

    private fun calculate(manager: LinearLayoutManager){
        manager.apply {
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

                mVerOrientation = vertical


                mNeedIndicator = mNeedIndicator or  1
                mNeedIndicator = mNeedIndicator or 8
                postDelayed(disableRunnable,3000)
            }
        }
    }

    private fun hasVerScrollBar():Boolean{
        return ((layoutManager as? LinearLayoutManager)?.orientation == VERTICAL && isVerticalScrollBarEnabled)
    }
    private fun hasHorScrollBar():Boolean{
        return ((layoutManager as? LinearLayoutManager)?.orientation == HORIZONTAL && isHorizontalScrollBarEnabled)
    }
    private fun hasNotScrollBar():Boolean{
        return !hasHorScrollBar() && !hasVerScrollBar()
    }

    private fun cancelAnim(){
        if (mValueAnimator.isRunning)mValueAnimator.cancel()
    }
    private fun startAnim(){
        if(!mValueAnimator.isStarted){
            mValueAnimator.start()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (hasNotScrollBar())addOnScrollListener(scrollListener)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancelAnim()
    }
}