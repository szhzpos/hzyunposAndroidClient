package com.wyc.cloudapp.customizationView

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import android.widget.OverScroller
import androidx.core.view.*
import com.wyc.cloudapp.R
import com.wyc.cloudapp.logger.Logger
import kotlin.math.abs
import kotlin.math.min

/**
 *
 * @ProjectName:    Android Animator
 * @Package:        com.example.myapplication
 * @ClassName:      FlowLayout
 * @Description:    流式布局
 * @Author:         wyc
 * @CreateDate:     2022-01-18 9:32
 * @UpdateUser:     更新者
 * @UpdateDate:     2022-01-18 9:32
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class FlowLayout(context: Context, attributes: AttributeSet?, defStyleAttr:Int, defStyleRes:Int) : ViewGroup(context,attributes,defStyleAttr,defStyleRes) {
    private var mScroller: OverScroller = OverScroller(context)
    private val mTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var mYDown = 0f
    private var mYMove = 0f
    private var mYLastMove = 0f

    private var mTopBorder = 0
    private var mBottomBorder = 0

    private val mHorizontalSpacing:Float
    private val mVerticalSpacing:Float
    private val mSeparatorSize:Float
    private val mRowCount:Int

    private val mSeparatorPaint = Paint()

    private val mChildContainer = mutableListOf<MutableList<View>>()

    private val mClosing:Boolean
    private var mCanScroll = false

    private var mVelocityTracker: VelocityTracker? = null
    private var mMinimumVelocity = 0
    private var mMaximumVelocity = 0
    private var mActivePointerId = -1

    init {
        val a: TypedArray = context.obtainStyledAttributes(attributes,R.styleable.FlowLayout)
        mHorizontalSpacing = a.getDimension(R.styleable.FlowLayout_item_horizontal_spacing, 0f)
        mVerticalSpacing = a.getDimension(R.styleable.FlowLayout_item_vertical_spacing, 0f)
        mSeparatorSize = min(a.getDimension(R.styleable.FlowLayout_separator_size,context.resources.getDimension(R.dimen.margin_1)), min(mHorizontalSpacing,mVerticalSpacing))
        mClosing = a.getBoolean(R.styleable.FlowLayout_closing,false)

        mRowCount = a.getInt(R.styleable.FlowLayout_rowCount,0)

        mSeparatorPaint.color = a.getColor(R.styleable.FlowLayout_separator_color,Color.GRAY)
        mSeparatorPaint.isAntiAlias = true
        mSeparatorPaint.style = Paint.Style.STROKE
        mSeparatorPaint.strokeWidth = mSeparatorSize

        a.recycle()

        setWillNotDraw(false)

        initVelocity()
    }

    constructor(context: Context):this(context,null)
    constructor(context: Context,attributes: AttributeSet?):this(context,attributes,0)
    constructor(context: Context,attributes: AttributeSet?,defStyleAttr:Int):this(context,attributes,defStyleAttr,0)

    private fun initVelocity() {
        val configuration = ViewConfiguration.get(context)
        mMinimumVelocity = configuration.scaledMinimumFlingVelocity
        mMaximumVelocity = configuration.scaledMaximumFlingVelocity
    }

    private fun initVelocityTrackerIfNotExists() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain()
        }
    }

    private fun recycleVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker!!.recycle()
            mVelocityTracker = null
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        recycleVelocityTracker()
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (mCanScroll){
            initVelocityTrackerIfNotExists()
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    mScroller.abortAnimation()

                    mYDown = ev.y
                    mYLastMove = mYDown

                    mActivePointerId = ev.getPointerId(0)
                    mVelocityTracker!!.addMovement(ev)
                }
                MotionEvent.ACTION_MOVE -> {
                    mYMove = ev.y
                    val diff = abs(mYMove - mYDown)
                    mYLastMove = mYMove
                    if (diff > mTouchSlop) {
                        return true
                    }
                }
            }
        }
        return super.onInterceptTouchEvent(ev)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (mCanScroll){
            when (event.action) {
                MotionEvent.ACTION_MOVE -> {
                    mVelocityTracker?.addMovement(event)

                    mYMove = event.y
                    val scrolledY = (mYLastMove - mYMove).toInt()

                    Logger.d("scrollY:%d,scrolledY:%d,mTopBorder:%d,mBottomBorder:%d，height：%d",scrollY,scrolledY,mTopBorder,mBottomBorder,height)

                    if (scrollY + scrolledY < mTopBorder) {
                        scrollTo(0, 0)
                        return true
                    } else if (scrollY + height + scrolledY > mBottomBorder) {
                        scrollTo(0, mBottomBorder - height)
                        return true
                    }
                    scrollBy(0, scrolledY)
                    mYLastMove = mYMove
                }
                MotionEvent.ACTION_UP -> {
                    val velocityTracker = mVelocityTracker
                    velocityTracker?.computeCurrentVelocity(1000, mMaximumVelocity.toFloat())
                    val initialYVelocity = velocityTracker?.getYVelocity(mActivePointerId)?.toInt() ?: 0

                    mActivePointerId = -1

                    recycleVelocityTracker()

                    if ( abs(initialYVelocity) > mMinimumVelocity) {
                        mScroller.fling(0, scrollY, 0, -initialYVelocity * 2, 0, 0, 0, mBottomBorder - height)
                        invalidate()
                    }
                    return performClick()
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.currX, mScroller.currY)
            invalidate()
        }
    }

    override fun onDragEvent(event: DragEvent?): Boolean {
        return super.onDragEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawClosingBorder(canvas)
        drawHorizontalSeparator(canvas)
        drawVerticalSeparator(canvas)
    }
    private fun drawClosingBorder(canvas: Canvas){
        if (mClosing){
            canvas.drawRect((left + paddingLeft).toFloat(),(top + paddingTop + scrollY).toFloat(),(right - paddingRight).toFloat(),(bottom - paddingBottom + scrollY).toFloat(),mSeparatorPaint)
        }
    }

    private fun drawHorizontalSeparator(canvas: Canvas){
        val space = mVerticalSpacing / 2
        val offset = (mHorizontalSpacing + mSeparatorSize / 2f )/ 2f
        mChildContainer.forEach {sub ->
            val view = getMaxHeightViewOfRow(sub)
            val bottom = (if (view != null) view.bottom + view.marginBottom else 0) + space
            sub.forEachIndexed{index,it ->
                if (index != sub.size - 1){
                    val startX = it.right.toFloat() + it.marginRight + offset
                    val stopX = it.right.toFloat() + it.marginRight + offset
                    canvas.drawLine(startX,it.top.toFloat() - it.marginTop - space,stopX, bottom,mSeparatorPaint)
                }
            }
        }
    }
    private fun drawVerticalSeparator(canvas: Canvas){
        val offset = (mVerticalSpacing + mSeparatorSize / 2f )/ 2f
        var bottom: Float
        mChildContainer.forEachIndexed {index,sub ->
            val view = getMaxHeightViewOfRow(sub)
            view?.let {
                if (index != mChildContainer.size - 1){
                    bottom = it.bottom.toFloat() + it.marginBottom + offset
                    if (mClosing)
                        canvas.drawLine(left.toFloat() + paddingTop,bottom,right.toFloat() - paddingRight, bottom,mSeparatorPaint)
                    else
                        canvas.drawLine(left.toFloat() ,bottom,right.toFloat() , bottom,mSeparatorPaint)
                }
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (mChildContainer.isNotEmpty())mChildContainer.clear()

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val widthSpec = MeasureSpec.getMode(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val heightSpec = MeasureSpec.getMode(heightMeasureSpec)

        var maxHeight = 0
        var maxWidth = 0

        var childWidthWeight: Float
        var childHeightWeight: Float

        var heightList:MutableList<View> = mutableListOf()
        var lineWidth = 0

        forEach {
            if (it.isGone)return@forEach

            val lp = it.layoutParams as LayoutParams
            childWidthWeight = lp.weightWidth
            childHeightWeight = lp.weightHeight

            if ((lp.height == ViewGroup.LayoutParams.WRAP_CONTENT || lp.height == 0) && childHeightWeight > 0f){
                lp.height = (childHeightWeight * (heightSize - (paddingTop + paddingBottom))).toInt()
            }
            if (mRowCount == 0){
                if ((lp.width == ViewGroup.LayoutParams.WRAP_CONTENT || lp.width == 0) && childWidthWeight > 0f){
                    lp.width = (childWidthWeight * (widthSize - (paddingLeft + paddingRight))).toInt()
                }
            }else{
                val horOffset = (mRowCount - 1) * mHorizontalSpacing
                if ((lp.width == ViewGroup.LayoutParams.WRAP_CONTENT || lp.width == 0) && childWidthWeight > 0f){
                    lp.width = (childWidthWeight * (widthSize - (paddingLeft + paddingRight + horOffset))).toInt()
                }
            }

            measureChild(it,widthMeasureSpec,heightMeasureSpec)

            lineWidth += it.measuredWidth + lp.leftMargin + lp.rightMargin
            if (lineWidth > measuredWidth - paddingLeft - paddingRight){
                maxHeight += getMaxHeightOfRow(heightList)
                lineWidth = it.measuredWidth

                mChildContainer.add(heightList)

                heightList = mutableListOf()
            }
            heightList.add(it)
        }
        if (heightList.isNotEmpty()){
            mChildContainer.add(heightList)
            maxHeight += getMaxHeightOfRow(heightList)
        }

        maxHeight += ((paddingTop + paddingBottom) + if (mChildContainer.isEmpty()) 0 else ((mChildContainer.size - 1) * mVerticalSpacing).toInt())
        maxWidth += (paddingLeft + paddingRight) + calculateMaxWidth()

        var resultSize = 0
        when(heightSpec){
            MeasureSpec.EXACTLY -> {
                resultSize = heightSize
            }
            MeasureSpec.AT_MOST -> {
                resultSize = maxHeight.coerceAtMost(heightSize)
            }
            MeasureSpec.UNSPECIFIED -> {
                resultSize = maxHeight
            }
        }
        val newHeightMeasureSpec = MeasureSpec.makeMeasureSpec(resultSize,heightSpec)

        when(widthSpec){
            MeasureSpec.EXACTLY -> {
                resultSize = widthSize
            }
            MeasureSpec.AT_MOST -> {
                resultSize = maxWidth.coerceAtMost(widthSize)
            }
            MeasureSpec.UNSPECIFIED -> {
                resultSize = maxWidth
            }
        }
        val newWidthMeasureSpec = MeasureSpec.makeMeasureSpec(resultSize,widthSpec)

        super.onMeasure(newWidthMeasureSpec, newHeightMeasureSpec)
    }
    private fun calculateMaxWidth():Int{
        var w = 0
        val maxList = mutableListOf<Int>()
        mChildContainer.forEach {
            it.forEach {sub ->
                val lp = sub.layoutParams as LayoutParams
                w += sub.measuredWidth + lp.leftMargin + lp.rightMargin
            }
            w += if (it.isEmpty()) 0 else ((it.size - 1) * mHorizontalSpacing).toInt()
            maxList.add(w)
            w = 0
        }
        maxList.sortDescending()
        return if (maxList.isEmpty()) 0 else maxList[0]
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (changed){
            var lineHeight = 0
            var leftChild = 0
            var horizontalSpace: Float
            var previous:MutableList<View> = mutableListOf()
            mChildContainer.forEachIndexed{indexSub,sub ->
                if (indexSub > 0 ){
                    lineHeight += getMaxHeightOfRow(previous)
                    lineHeight += mVerticalSpacing.toInt()
                    previous = sub
                }else previous = sub

                sub.forEachIndexed {index,it ->
                    val lp = it.layoutParams as MarginLayoutParams
                    leftChild += lp.leftMargin

                    setChildFrame(it,leftChild + paddingLeft,paddingTop + lineHeight + lp.topMargin,it.measuredWidth,it.measuredHeight)

                    horizontalSpace = if (index != sub.size - 1) mHorizontalSpacing else 0f
                    leftChild += it.measuredWidth + lp.rightMargin + horizontalSpace.toInt()
                }

                leftChild = 0
            }
            if (mChildContainer.isNotEmpty()){
                mTopBorder = getMinTopOfRow(mChildContainer[0]) - paddingTop
                mBottomBorder = getMaxBottomOfRow(mChildContainer[mChildContainer.size - 1]) + paddingBottom
            }

            mCanScroll = mBottomBorder > height
        }
    }
    private fun setChildFrame(child: View, left: Int, top: Int, width: Int, height: Int) {
        child.layout(left, top, left + width, top + height)
    }

    private fun getMaxBottomOfRow(heightList:MutableList<View>):Int{
        if (heightList.isEmpty())return 0
        val list = heightList.toMutableList()
        list.sortBy{
            val lp = it.layoutParams as MarginLayoutParams
            lp.topMargin
        }
        val view = list[list.size - 1]
        return view.bottom + (view.layoutParams as MarginLayoutParams).bottomMargin
    }

    private fun getMinTopOfRow(heightList:MutableList<View>):Int{
        if (heightList.isEmpty())return 0
        val list = heightList.toMutableList()
        list.sortBy{
            it.top
        }
        return list[0].top
    }

    private fun getMaxHeightOfRow(heightList:MutableList<View>):Int{
        val view = getMaxHeightViewOfRow(heightList)
        view?.apply {
            val lp = layoutParams as MarginLayoutParams
            return measuredHeight + lp.topMargin + lp.bottomMargin
        }
        return 0
    }
    private fun getMaxHeightViewOfRow(heightList:MutableList<View>):View?{
        if (heightList.isEmpty())return null
        val list = heightList.toMutableList()
        list.sortByDescending {
            val lp = it.layoutParams as MarginLayoutParams
            it.measuredHeight + lp.topMargin + lp.bottomMargin
        }
        return list[0]
    }

    override fun generateLayoutParams(attrs:AttributeSet): LayoutParams {
         return LayoutParams(context,attrs)
    }

    override fun generateLayoutParams(lp:ViewGroup.LayoutParams):ViewGroup.LayoutParams{
        if (lp is FrameLayout.LayoutParams) {
            return FrameLayout.LayoutParams((lp as FrameLayout.LayoutParams?)!!)
        } else if (lp is MarginLayoutParams) {
            return FrameLayout.LayoutParams((lp as MarginLayoutParams?)!!)
        }
        return LayoutParams(lp)
    }

    class LayoutParams : MarginLayoutParams {
        var weightWidth = 0f
        var weightHeight = 0f

        constructor(context: Context,attrs:AttributeSet?) : super(context,attrs){
            val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout_Layout)
            weightWidth = a.getFloat(R.styleable.FlowLayout_Layout_layout_weight_width, 0f)
            weightHeight = a.getFloat(R.styleable.FlowLayout_Layout_layout_weight_height, 0f)
            a.recycle()
        }
        constructor(width:Int,height:Int) : super(width,height)
        constructor(source: ViewGroup.LayoutParams) : super(source){
            if (source is LayoutParams){
                weightWidth = source.weightWidth
                weightHeight = source.weightHeight
            }
        }
        constructor(source: MarginLayoutParams) : super(source){
            if (source is LayoutParams){
                weightWidth = source.weightWidth
                weightHeight = source.weightHeight
            }
        }
        constructor(source: LayoutParams) : super(source){
            weightWidth = source.weightWidth
            weightHeight = source.weightHeight
        }
    }
}