package com.wyc.cloudapp.CustomizationView

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import androidx.recyclerview.widget.RecyclerView
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.Utils
import kotlin.math.abs
import kotlin.math.asin

class SlideRecycleView : RecyclerView{
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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = ev.x
                val moveY = ev.y
                val xDiff = abs(moveX - downX)
                val yDiff = abs(moveY - downY)
                if (xDiff > mTouchSlop || yDiff > mTouchSlop) {
                    val squareRoot = Math.sqrt((xDiff * xDiff + yDiff * yDiff).toDouble())
                    val degree = asin(yDiff / squareRoot) * 180 / Math.PI
                    val isMeetSlidingYAngle = degree > 45
                    val isSlideUp = moveY < downY && isMeetSlidingYAngle
                    val isSlideDown = moveY > downY && isMeetSlidingYAngle
                    val isSlideLeft = moveX < downX && !isMeetSlidingYAngle
                    val isSlideRight = moveX > downX && !isMeetSlidingYAngle
                    if ((isSlideRight && !canScrollHorizontally(-1)) || (isSlideLeft && !canScrollHorizontally(1))) {
                        return false
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!Utils.equalDouble(downX,ev.x)) {
                    return false
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}