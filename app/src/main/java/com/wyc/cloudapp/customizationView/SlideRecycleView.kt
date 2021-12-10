package com.wyc.cloudapp.customizationView

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.Utils
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.sqrt

class SlideRecycleView : IndicatorRecyclerView{
    private var isChildLeftDrag = false
    private var isChildRightDrag = false

    constructor(context: Context):this(context, null)
    constructor(context: Context, attrs: AttributeSet?):this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int):super(context, attrs, defStyleAttr)

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
        clearLeftRight()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        val code = super.dispatchTouchEvent(ev)
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                clearDragFlag()
                checkChildDrag(findChildByCoordinate(ev.x,ev.y))
            }
            MotionEvent.ACTION_MOVE -> {
                if ((hasSlideRight() && !isChildRightDrag && !canScrollHorizontally(-1)) || (hasSlideLeft() && !isChildLeftDrag && !canScrollHorizontally(1))) {
                    return false
                }
            }
            MotionEvent.ACTION_UP -> {
                if (!Utils.equalDouble(downX, ev.x) && ((hasSlideRight() && !isChildRightDrag) || (hasSlideLeft() && !isChildLeftDrag))) {
                    return false
                }
            }
        }
        return code
    }
}