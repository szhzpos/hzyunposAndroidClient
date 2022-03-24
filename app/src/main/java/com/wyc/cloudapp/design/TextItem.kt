package com.wyc.cloudapp.design

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.alibaba.fastjson.annotation.JSONField
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
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

class TextItem:ItemBase() {
    var content = ""
    var mFontSize = CustomApplication.self().resources.getDimension(R.dimen.font_size_18)
        set(value) {
            if (value <= 200f) {
                field = value

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

    @JSONField(serialize = false)
    private val scaleFactor = CustomApplication.self().resources.displayMetrics.scaledDensity
    @JSONField(serialize = false)
    private var mPaint:Paint? = null
    @JSONField(serialize = false)
    private val mRect = Rect()
    @JSONField(serialize = false)
    private var mTextLastWidth = 0

    override fun measure(w:Int,h:Int,paint: Paint) {
        mPaint = paint
        getBound(mRect)

        width = min(mRect.width(),w)
        height = min(mRect.height(),h)

        mRect.setEmpty()
    }

    override fun draw(offsetX:Float,offsetY:Float,canvas: Canvas,paint: Paint) {
        if (Utils.isNotEmpty(content)){
            paint.textSize = mFontSize
            paint.color = mFontColor
            paint.style = Paint.Style.FILL
            paint.letterSpacing = mLetterSpacing

            mRect.setEmpty()

            if (content.contains("\n")){
                val str = content.split("\n")
                var currentY  = 0f
                str.forEach {
                    paint.getTextBounds(it,0,it.length,mRect)
                    canvas.drawText(it,left + offsetX,top + offsetY + mRect.height() + currentY,paint)
                    currentY += mRect.height() + paint.fontMetrics.descent + paint.fontMetrics.leading
                }
            }else{
                paint.getTextBounds(content,0,content.length,mRect)
                canvas.drawText(content,left + offsetX,top + offsetY + mRect.height() - paint.fontMetrics.descent,paint)
            }
            mPaint = paint

            super.draw(offsetX, offsetY, canvas, paint)
        }
    }
    override fun scale(scaleX: Float, scaleY: Float) {
        mRect.setEmpty()
        if (abs(scaleY) >= scaleFactor){
            mFontSize += scaleY
        }else if (abs(scaleX) >= scaleFactor){
            width += scaleX.toInt()

            val stringBuffer = StringBuffer(content.replace("\n",""))
            var len = 0f
            stringBuffer.forEachIndexed {index,chars->
                if (chars == '\n')return@forEachIndexed
                len += (mPaint?.measureText(chars.toString())?:0f)
                if (len > width){

                    if (index > 0 && stringBuffer[index - 1] != '\n')
                        stringBuffer.insert(index,"\n")

                    len = 0f
                }
            }
            content = stringBuffer.toString()

            getBound(mRect)
            height = mRect.height()
        }
        mTextLastWidth = mRect.width()
    }
    private fun getBound(b:Rect){
        mPaint?.let { paint ->
            paint.textSize = mFontSize
            paint.letterSpacing = mLetterSpacing
            val t = Rect()
            if (content.contains("\n")){
                val aStr = content.split("\n")
                var currentY: Float
                var maxWidth = 0
                aStr.forEach {
                    paint.getTextBounds(it,0,it.length,t)
                    currentY = t.height() + paint.fontMetrics.leading + paint.fontMetrics.descent
                    b.bottom += currentY.toInt()
                    if (t.width() > maxWidth)
                        maxWidth = t.width()
                }
                b.right += maxWidth
            }else{
                paint.getTextBounds(content,0,content.length,t)
                b.right += t.width()
                b.bottom += t.height()
            }
        }
    }

    override fun toString(): String {
        return "TextItem(content='$content', mFontSize=$mFontSize, mFontColor=$mFontColor, mLetterSpacing=$mLetterSpacing, scaleFactor=$scaleFactor, mPaint=$mPaint, mRect=$mRect, mTextLastWidth=$mTextLastWidth) ${super.toString()}"
    }

}