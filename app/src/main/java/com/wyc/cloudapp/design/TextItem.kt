package com.wyc.cloudapp.design

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.alibaba.fastjson.annotation.JSONField
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.Utils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.design
 * @ClassName:      TextItem
 * @Description:    文本
 * @Author:         wyc
 * @CreateDate:     2022/3/16 14:54
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/3/16 14:54
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

open class TextItem:ItemBase() {
    open var content = "编辑内容"
    var mFontSize = CustomApplication.self().resources.getDimension(R.dimen.font_size_18)
        set(value) {
            if (value <= 200f) {
                field = value

                mRect.setEmpty()

                mPaint.textSize = value
                mPaint.letterSpacing = mLetterSpacing

                getBound(mRect)
                if (width <= mRect.width()) {
                    width = mRect.width()
                } else if (mTextLastWidth > mRect.width()) {
                    width += mRect.width() - mTextLastWidth
                }
                height = mRect.height()
            }else field = 200f
        }

    var mFontColor = Color.BLACK
    var mLetterSpacing = 0f
    var hasNewLine = false

    @JSONField(serialize = false)
    private val scaleFactor = CustomApplication.self().resources.displayMetrics.scaledDensity
    @JSONField(serialize = false)
    protected var mPaint:Paint = Paint()
    @JSONField(serialize = false)
    protected val mRect = Rect()
    @JSONField(serialize = false)
    private var mTextLastWidth = 0

    override fun measureItem(w:Int,h:Int) {
        getBound(mRect)

        width = min(mRect.width(),w)
        height = min(mRect.height(),h)

        mRect.setEmpty()
    }

    override fun transform(scaleX: Float, scaleY: Float) {
        super.transform(scaleX, scaleY)
        mFontSize *= scaleX
        mLetterSpacing *= scaleX
    }

    override fun drawItem(offsetX:Float,offsetY:Float,canvas: Canvas,paint: Paint) {
        if (Utils.isNotEmpty(content)){
            paint.textSize = mFontSize
            paint.color = mFontColor
            paint.style = Paint.Style.FILL
            paint.letterSpacing = mLetterSpacing

            mPaint = paint

            if (content.contains("\n")){
                val str = content.split("\n")
                var currentY  = 0f
                mRect.setEmpty()
                str.forEach {
                    paint.getTextBounds(it,0,it.length,mRect)
                    canvas.drawText(it,left + offsetX,top + offsetY + mRect.height() + currentY,paint)
                    currentY += mRect.height() + paint.fontMetrics.descent + paint.fontMetrics.leading
                }
            }else{
                val baseLineY = height / 2 + (abs(paint.fontMetrics.ascent) - paint.fontMetrics.descent) / 2
                canvas.drawText(content,left + offsetX,top + offsetY + baseLineY,paint)
            }
        }
    }

    override fun shrink() {
        scale(0f,-mFontSize * 0.2f)
    }

    override fun zoom() {
        scale(0f,mFontSize * 0.2f)
    }

    override fun scale(scaleX: Float, scaleY: Float) {
        mRect.setEmpty()

        if (abs(scaleY) >= scaleFactor){
            mFontSize += scaleY / 2f
        }else if (abs(scaleX) >= scaleFactor){
            width += scaleX.toInt()
            updateNewline()
        }
        mTextLastWidth = mRect.width()
    }
    fun updateNewline():Boolean{
        if (hasNewLine){
            val stringBuffer = StringBuffer(content.replace("\n",""))
            var len = 0f

            mPaint.textSize = mFontSize
            mPaint.letterSpacing = mLetterSpacing

            stringBuffer.forEachIndexed {index,chars->
                if (chars == '\n')return@forEachIndexed
                len += mPaint.measureText(chars.toString())
                if (len > width){
                    if (index > 0 && stringBuffer[index - 1] != '\n'){
                        stringBuffer.insert(index,"\n")
                    }
                    len = 0f
                }
            }
            content = stringBuffer.toString()
            getBound(mRect)
            height = mRect.height()

            return true
        }else{
            content = content.replace("\n","")
        }
        return false
    }

    protected fun getBound(b:Rect,c:String = ""){
        val t = Rect()
        val tmp = if (Utils.isNotEmpty(c)) c else content
        if (tmp.contains("\n")){
            val aStr = tmp.split("\n")
            var currentY: Float
            var maxWidth = 0
            aStr.forEach {
                mPaint.getTextBounds(it,0,it.length,t)
                currentY = t.height() + mPaint.fontMetrics.leading + mPaint.fontMetrics.descent
                b.bottom += currentY.toInt()
                if (t.width() > maxWidth)
                    maxWidth = t.width()
            }
            b.right += maxWidth
        }else{
            mPaint.getTextBounds(tmp,0,tmp.length,t)
            b.right += t.width()
            b.bottom += t.height()
        }
    }

    override fun toString(): String {
        return "TextItem(content='$content', mFontSize=$mFontSize, mFontColor=$mFontColor, mLetterSpacing=$mLetterSpacing, scaleFactor=$scaleFactor, mPaint=$mPaint, mRect=$mRect, mTextLastWidth=$mTextLastWidth) ${super.toString()}"
    }


}