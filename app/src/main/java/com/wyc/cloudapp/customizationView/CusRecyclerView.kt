package com.wyc.cloudapp.customizationView

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private var mSpace = Utils.dpToPx(context,8f)
    private val mValueAnimator = ValueAnimator()
    private var mNeedIndicator:Int = 0
    init {
        mPaint.isAntiAlias = true
        mPaint.color = Color.RED
        mPaint.strokeWidth = Utils.dpToPx(context,1f).toFloat()
        mPaint.style = Paint.Style.STROKE
        mPaint.setShadowLayer(5f,0f,0f,Color.RED)

        mValueAnimator.repeatCount = ValueAnimator.INFINITE
        mValueAnimator.duration = 2000
        mValueAnimator.interpolator = AccelerateDecelerateInterpolator()
        mValueAnimator.setIntValues(18,8,18)
        mValueAnimator.addUpdateListener {
            mSpace = it.animatedValue as Int
            invalidate()
        }
    }
    constructor(context: Context):this(context,null,0)
    constructor(context: Context, attrs: AttributeSet?) : this(context,attrs,0)

    override fun draw(c: Canvas) {
        super.draw(c)
        if (hasShowIndicator()){
            drawIndicator(c)
        }
    }

    private fun drawIndicator(c: Canvas){
        (layoutManager as? LinearLayoutManager)?.apply {
            val lastVisibility = findLastCompletelyVisibleItemPosition()
            var xAxis = -1f
            var yAxis = -1f
            val vertical = orientation == VERTICAL
            if (lastVisibility + 1 < itemCount){
                if (vertical){
                    xAxis = (width / 2).toFloat()
                    yAxis = height.toFloat()
                }else{
                    xAxis = width.toFloat()
                    yAxis = (height / 2).toFloat()
                }
                drawIcon(c,xAxis,yAxis,vertical,false)
            }
            val firstVisibility = findFirstCompletelyVisibleItemPosition()
            if (firstVisibility > 0){
                if (vertical){
                    xAxis = (width / 2).toFloat()
                    yAxis = 0f
                }else{
                    xAxis = 0f
                    yAxis = (height / 2).toFloat()
                }
                drawIcon(c,xAxis,yAxis,vertical,true)
            }
        }
    }
    private fun drawIcon(c: Canvas, xAxis:Float, yAxis:Float, vert: Boolean,head:Boolean){
        val lineLen = Utils.dpToPx(context,9f)
        val radian = Math.PI / 180 * 45
        val x = (cos(radian) * lineLen).toFloat()
        val y = (sin(radian) * lineLen).toFloat()
        val space = mSpace

        val path = Path()

        if (vert){
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
        if (head){
            c.save()
            if (vert)
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
                mNeedIndicator = (mNeedIndicator or  (1 shl 1))
            }
            MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{
                removeCallbacks(disableRunnable)
                postDelayed(disableRunnable,3000)
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private val disableRunnable = Runnable {
        mNeedIndicator = (mNeedIndicator and 1)
    }

    private fun hasShowIndicator():Boolean{
        val code = mNeedIndicator == 3
        if (code && !mValueAnimator.isStarted){
            mValueAnimator.start()
        }
        return mNeedIndicator == 3
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (layoutManager as? LinearLayoutManager)?.apply {
            if((findLastCompletelyVisibleItemPosition() + 1 != itemCount) || (findFirstCompletelyVisibleItemPosition() != 0)){
                mNeedIndicator = (mNeedIndicator or  1)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        mValueAnimator.cancel()
    }
}