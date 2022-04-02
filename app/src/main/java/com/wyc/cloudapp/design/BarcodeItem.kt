package com.wyc.cloudapp.design

import android.graphics.*
import android.util.TypedValue
import androidx.core.graphics.plus
import com.alibaba.fastjson.annotation.JSONField
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger
import kotlin.math.max


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.design
 * @ClassName:      ImageItem
 * @Description:    图片
 * @Author:         wyc
 * @CreateDate:     2022/3/16 14:51
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/3/16 14:51
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

open class BarcodeItem:ItemBase() {
    var barcodeFormat: BarcodeFormat = BarcodeFormat.CODE_128
    var fontSize = CustomApplication.self().resources.getDimension(R.dimen.font_size_14)
    var content: String = "6922711043401"
        set(value) {
            field = value
            mBitmap = generateBitmap()
        }
    /**
     * 引用数据字段。如果不为空则content的值需要从数据源获取，获取数据源的具体值由field的值决定
     * */
    var field = ""

    @JSONField(serialize = false)
    private var mBitmap:Bitmap? = null
    @JSONField(serialize = false)
    private var mBottomMarge = Rect()
    @JSONField(serialize = false)
    var hasMark = true

    override fun drawItem(offsetX: Float, offsetY: Float, canvas: Canvas, paint: Paint) {
        mBitmap?.let {
            val l = left + offsetX
            val t = top + offsetY
            canvas.drawBitmap(it, null,RectF(l,t,l + width,t + height),paint)

            drawContent(l,t,canvas,paint)

            if (hasMark && field.isNotEmpty()){
                val r = radian % 360 != 0f
                if (r){
                    canvas.save()
                    canvas.rotate(-radian,offsetX+ left + width / 2f,offsetY + top + height / 2f)
                }

                paint.color = Color.RED
                paint.style = Paint.Style.STROKE
                canvas.drawRect(cRECT,paint)

                if (r){
                    canvas.restore()
                }
            }
        }
    }
    private fun drawContent(l: Float, t: Float,canvas: Canvas, paint: Paint){
        if (barcodeFormat == BarcodeFormat.CODE_128){
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            paint.textSize = fontSize
            paint.getTextBounds(content,0,content.length,mBottomMarge)

            val textHeight = mBottomMarge.height()

            mBottomMarge.bottom += CustomApplication.self().resources.getDimensionPixelSize(R.dimen.size_4)
            mBottomMarge.right += (width - mBottomMarge.width())
            mBottomMarge.offsetTo(l.toInt(), (height - mBottomMarge.height() + t).toInt())

            canvas.drawRect(mBottomMarge,paint)

            paint.color = Color.BLACK

            var textWidth = 0f
            content.forEach {
                textWidth += paint.measureText(it.toString())
            }
            val letterSpacing = ((mBottomMarge.width() - textWidth) / (content.length - 1)) + textWidth / content.length
            val textY = mBottomMarge.bottom - (mBottomMarge.height() - textHeight) / 2f
            content.forEachIndexed {index,it ->
                canvas.drawText(it.toString(),l  + index * letterSpacing,textY,paint)
            }
        }
    }

    override fun measureItem(w: Int, h: Int) {
        if (mBitmap == null){
            when(barcodeFormat){
                BarcodeFormat.QR_CODE ->{
                    if (width <= ACTION_RADIUS.toInt()){
                        width = 231
                        height = width
                    }
                }else ->{
                    if (width <= ACTION_RADIUS.toInt())width = 370
                    if (height <= ACTION_RADIUS.toInt())height = CustomApplication.self().resources.getDimensionPixelSize(R.dimen.size_28)
                }
            }
            mBitmap = generateBitmap()
        }
    }

    override fun transform(scaleX: Float, scaleY: Float) {
        super.transform(scaleX, scaleY)
        fontSize *= scaleX
    }

    override fun scale(scaleX: Float, scaleY: Float) {
        when(barcodeFormat){
            BarcodeFormat.QR_CODE ->{
                width += scaleX.coerceAtLeast(scaleY).toInt()
                height = width
            }else ->{
                super.scale(scaleX, scaleY)
            }
        }
    }
    private fun generateBitmap():Bitmap?{
        val writer = MultiFormatWriter()
        var w = 0
        var h = 0
        when(barcodeFormat){
            BarcodeFormat.QR_CODE ->{
                w = 231
                h = w
            }else ->{
                w = 370
                h = CustomApplication.self().resources.getDimensionPixelSize(R.dimen.size_28)
            }
        }
        try {
            val result: BitMatrix = writer.encode(content,barcodeFormat, w,h,hashMapOf(Pair(EncodeHintType.MARGIN,0)) )

            val codeWidth = result.width
            val codeHeight = result.height
            val pixels = IntArray(codeWidth * codeHeight)

            for (y in 0 until codeHeight) {
                val offset = y * codeWidth
                for (x in 0 until codeWidth) {
                    if (y < result.height)
                        pixels[offset + x] = if (result[x, y]) Color.BLACK else Color.WHITE
                    else pixels[offset + x] = Color.WHITE
                }
            }
            val bitmap = Bitmap.createBitmap(codeWidth, codeHeight, Bitmap.Config.ARGB_8888)
            bitmap.setPixels(pixels, 0, codeWidth, 0, 0, codeWidth, codeHeight)

            return bitmap
        } catch (e: WriterException) {
            MyDialog.toastMessage(e.message)
        }
        return null
    }
}