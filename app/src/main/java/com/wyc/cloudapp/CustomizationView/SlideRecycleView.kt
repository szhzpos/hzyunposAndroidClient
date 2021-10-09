package com.wyc.cloudapp.CustomizationView

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
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
    private var isChildLeftDrag = false
    private var isChildRightDrag = false
    private var isSlideLeft = false
    private var isSlideRight = false
    constructor(context: Context):this(context, null){

    }
    constructor(context: Context, attrs: AttributeSet?):this(context, attrs, 0){

    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr){
        mTouchSlop = ViewConfiguration.get(context).scaledPagingTouchSlop
    }

    private fun isClickView(view: View?, x: Float, y: Float): Boolean {
        if (view == null) return false
        val v_x = view.x
        val v_y = view.y
        return x >= v_x && x <= v_x + view.width && y >= v_y && y <= v_y + view.height
    }

    private fun findChildByCoordinate(x: Float, y: Float):View?{
        val count = childCount
        for (i:Int in 0..count){
            val view = getChildAt(i)
            if (isClickView(view,x,y)){
                return view
            }
        }
        return null
    }

    private fun checkChildDrag(view: View?){
        view?.let {
            isChildLeftDrag = it.canScrollHorizontally(-1)
            isChildRightDrag = it.canScrollHorizontally(1)
        }
        Logger.d("isLeftDrag:%s,isRightDrag:%s",isChildLeftDrag,isChildRightDrag)
    }

    private fun clearDragFlag(){
        isChildLeftDrag = false
        isChildRightDrag = false
        isSlideLeft = false
        isSlideRight = false
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                downX = ev.x
                downY = ev.y
                clearDragFlag()
                checkChildDrag(findChildByCoordinate(ev.x,ev.y))
            }
            MotionEvent.ACTION_MOVE -> {
                val moveX = ev.x
                val moveY = ev.y
                val xDiff = abs(moveX - downX)
                val yDiff = abs(moveY - downY)
                if (xDiff > mTouchSlop || yDiff > mTouchSlop) {
                    val squareRoot = Math.sqrt((xDiff * xDiff + yDiff * yDiff).toDouble())
                    val degree = asin(yDiff / squareRoot) * 180 / Math.PI
                    val isMeetSlidingXAngle = degree <= 45
                    if (isMeetSlidingXAngle){
                        isSlideLeft = moveX <= downX
                        isSlideRight = moveX >= downX
                        if ((isSlideRight && !isChildRightDrag && !canScrollHorizontally(-1)) || (isSlideLeft && !isChildLeftDrag && !canScrollHorizontally(1))) {
                            return false
                        }
                    }
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!Utils.equalDouble(downX, ev.x) && ((isSlideRight && !isChildRightDrag) || (isSlideLeft && !isChildLeftDrag))) {
                    return false
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}