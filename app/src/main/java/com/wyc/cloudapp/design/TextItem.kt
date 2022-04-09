package com.wyc.cloudapp.design

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.SeekBar
import com.alibaba.fastjson.annotation.JSONField
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.customizationView.MySeekBar
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.Utils
import kotlin.math.abs
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
    private val minFontSize = CustomApplication.self().resources.getDimensionPixelSize(R.dimen.font_size_10)
    private val maxFontSize = 128

    var content = "编辑内容"
    var mFontSize = CustomApplication.self().resources.getDimension(R.dimen.font_size_18)
    var mFontColor = Color.BLACK
    var mLetterSpacing = 0f
    var hasNewLine = true
    var hasBold = false
    var hasItalic = false
    var hasUnderLine = false
    var hasDelLine = false

    @JSONField(serialize = false)
    private var mPaint:Paint = Paint()
    @JSONField(serialize = false)
    private val mRect = Rect()

    override fun measureItem(w:Int,h:Int) {
        updatePaintAttr()

        getBound(mRect)

        width = min(mRect.width(),w)
        height = min(mRect.height(),h)
    }
    private fun updatePaintAttr(){
        mPaint.textSize = mFontSize
        mPaint.letterSpacing = mLetterSpacing
        mPaint.isFakeBoldText = hasBold
        mPaint.textSkewX = if (hasItalic) -0.5f else 0f
        mPaint.isUnderlineText = hasUnderLine
        mPaint.isStrikeThruText = hasDelLine
    }

    override fun transform(scaleX: Float, scaleY: Float) {
        super.transform(scaleX, scaleY)
        mFontSize *= min(scaleX,scaleY)
    }

    override fun drawItem(offsetX:Float,offsetY:Float,canvas: Canvas,paint: Paint) {
        if (Utils.isNotEmpty(content)){
            mPaint = paint

            paint.style = Paint.Style.FILL
            paint.color = mFontColor

            updatePaintAttr()

            canvas.save()

            val l = left + offsetX
            val t = top + offsetY
            canvas.clipRect(l,t,l + width ,t + height)

            if (content.contains("\n")){
                val str = content.split("\n")
                var currentY  = 0f
                str.forEach {
                    paint.getTextBounds(it,0,it.length,mRect)
                    canvas.drawText(it,l,t + mRect.height() + currentY,paint)
                    currentY += mRect.height() + paint.fontMetrics.descent + paint.fontMetrics.leading
                }
            }else{
                val baseLineY = height / 2 + (abs(paint.fontMetrics.ascent) - paint.fontMetrics.descent) / 2
                canvas.drawText(content,l,t + baseLineY,paint)
            }

            canvas.restore()
        }
    }

    override fun shrink() {
        scale(0f,-mFontSize * 0.2f)
    }

    override fun zoom() {
        scale(0f,mFontSize * 0.2f)
    }

    override fun scale(scaleX: Float, scaleY: Float) {
        if (abs(scaleY) >= 1.0f){
            mFontSize += scaleY
            if (mFontSize <= maxFontSize) {
                if (mFontSize < minFontSize)mFontSize = minFontSize.toFloat()
            }else mFontSize = maxFontSize.toFloat()
        }else if (abs(scaleX) >= 1.0f){
            width = (width + scaleX).toInt()
        }
        updateNewline()
    }
    fun updateNewline(){
        updatePaintAttr()
        if (hasNewLine){
            if (content.contains("\n")){
                content = content.replace("\n","")
            }
            mPaint.getTextBounds(content,0,content.length,mRect)
            val newWidth = mRect.width()

            if (newWidth > width){

                mPaint.letterSpacing = 0f
                mPaint.getTextBounds(content,0,content.length,mRect)
                val oldWidth = mRect.width()
                mPaint.letterSpacing = mLetterSpacing

                val space = (newWidth - oldWidth).toFloat() / content.length * 2f

                val stringBuilder = StringBuilder(content)
                var len = 0f

                stringBuilder.forEachIndexed { index, c ->
                    mPaint.getTextBounds(c.toString(),0,1,mRect)
                    len += mRect.width().toFloat() + space * 2
                    if (len > width){
                        if (index > 0 && stringBuilder[index - 1] != '\n'){
                            stringBuilder.insert(index,"\n")
                        }
                        len = 0f
                    }
                }
                content = stringBuilder.toString()
            }
        }else{
            if (content.contains("\n")){
                content = content.replace("\n","")
            }
        }
        getBound(mRect)
        height = mRect.height()
    }

    private fun getBound(b:Rect){
        if (content.contains("\n")){
            val t = Rect()
            val aStr = content.split("\n")
            var currentY: Float
            var maxWidth = 0
            var maxHeight = 0
            aStr.forEach {
                mPaint.getTextBounds(it,0,it.length,t)
                currentY = t.height() + mPaint.fontMetrics.leading + mPaint.fontMetrics.descent
                maxHeight += currentY.toInt()
                if (t.width() > maxWidth)
                    maxWidth = t.width()
            }
            b.set(0,0,maxWidth,maxHeight)
        }else{
            mPaint.getTextBounds(content,0,content.length,b)
        }
    }

    override fun popMenu(labelView: LabelView) {
        val view = View.inflate(labelView.context,R.layout.text_item_attr,null)
        showEditDialog(labelView.context,view)

        val font: MySeekBar = view.findViewById(R.id.font)
        font.minValue = minFontSize
        font.max = maxFontSize - minFontSize
        font.progress = mFontSize.toInt() - minFontSize
        font.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mFontSize = progress.toFloat() + minFontSize

                updateNewline()
                labelView.postInvalidate()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }

        })

        val bCheckBox:CheckBox = view.findViewById(R.id.bold)
        bCheckBox.isChecked = hasBold
        bCheckBox.setOnCheckedChangeListener{ _, check ->
            hasBold = check
            labelView.postInvalidate()
        }

        val italic:CheckBox = view.findViewById(R.id.italic)
        italic.isChecked = hasItalic
        italic.setOnCheckedChangeListener { _, isChecked ->
            hasItalic = isChecked
            updateNewline()
            labelView.postInvalidate()
        }

        val underLine:CheckBox = view.findViewById(R.id.underLine)
        underLine.isChecked = hasUnderLine
        underLine.setOnCheckedChangeListener { _, isChecked ->
            hasUnderLine = isChecked
            labelView.postInvalidate()
        }

        val delLine:CheckBox = view.findViewById(R.id.delLine)
        delLine.isChecked = hasDelLine
        delLine.setOnCheckedChangeListener { _, isChecked ->
            hasDelLine = isChecked
            labelView.postInvalidate()
        }

        val newline:CheckBox = view.findViewById(R.id.newline)
        newline.isChecked = hasNewLine
        newline.setOnCheckedChangeListener { _, isChecked ->
            hasNewLine = isChecked
            updateNewline()
            labelView.postInvalidate()
        }

        val letterSpacing:SeekBar = view.findViewById(R.id.letterSpacing)
        letterSpacing.progress = (mLetterSpacing * 10).toInt()
        letterSpacing.max = 20
        letterSpacing.setOnSeekBarChangeListener(object :SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mLetterSpacing = progress / 10f
                updateNewline()
                labelView.postInvalidate()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }

        })

        val et:EditText = view.findViewById(R.id.content)
        et.setText(content)
        if (this is DataItem){
            et.isEnabled = false
        }
        et.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

            }
            override fun afterTextChanged(s: Editable) {
                content = s.toString()
                updateNewline()
                labelView.postInvalidate()
            }

        })
    }

    override fun toString(): String {
        return "TextItem(content='$content', mFontSize=$mFontSize, mFontColor=$mFontColor, mLetterSpacing=$mLetterSpacing,mPaint=$mPaint, mRect=$mRect) ${super.toString()}"
    }


}