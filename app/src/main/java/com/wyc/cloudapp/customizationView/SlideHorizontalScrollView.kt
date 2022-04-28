package com.wyc.cloudapp.customizationView

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.widget.HorizontalScrollView
import com.wyc.cloudapp.utils.Utils
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.sqrt

class SlideHorizontalScrollView:HorizontalScrollView {
    private var downX = 0f;
    private  var downY = 0f
    private var mTouchSlop = 0
    constructor(context: Context):this(context, null){

    }
    constructor(context: Context, attrs: AttributeSet?):this(context, attrs, 0){

    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr){
        mTouchSlop = ViewConfiguration.get(context).scaledPagingTouchSlop
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.rawX
                downY = ev.rawY
            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = ev.rawX
                val moveY = ev.rawY
                val xDiff = abs(moveX - downX)
                val yDiff = abs(moveY - downY)
                if (xDiff > mTouchSlop || yDiff > mTouchSlop) {
                    val squareRoot = sqrt((xDiff * xDiff + yDiff * yDiff).toDouble())
                    val degree = asin(yDiff / squareRoot) * 180 / Math.PI
                    val isMeetSlidingYAngle = degree > 45
                    val isSlideLeft = moveX < downX && !isMeetSlidingYAngle
                    val isSlideRight = moveX > downX && !isMeetSlidingYAngle
                    if ((isSlideRight && !canScrollHorizontally(-1)) || (isSlideLeft &&!canScrollHorizontally(1))) {
                        return false
                    }
                }
            }
        }
        return super.onTouchEvent(ev)
    }
}