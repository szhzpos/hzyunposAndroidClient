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

open class BarcodeItem(var barcodeFormat: BarcodeFormat = BarcodeFormat.CODE_128):ItemBase() {
    var content: String = "6922711043401"
        set(value) {
            field = value
            mBitmap = generateBitmap()
        }

    @JSONField(serialize = false)
    private var mBitmap:Bitmap? = null
    @JSONField(serialize = false)
    private var mBottomMarge = Rect()

    override fun draw(offsetX: Float, offsetY: Float, canvas: Canvas, paint: Paint) {
        mBitmap?.let {
            val l = left + offsetX
            val t = top + offsetY
            canvas.drawBitmap(it, null,RectF(l,t,l + width,t + height),paint)

            drawContent(l,t,canvas,paint)

            super.draw(offsetX, offsetY, canvas, paint)
        }
    }
    private fun drawContent(l: Float, t: Float,canvas: Canvas, paint: Paint){
        if (barcodeFormat == BarcodeFormat.CODE_128){
            paint.color = Color.WHITE
            paint.style = Paint.Style.FILL
            paint.textSize = CustomApplication.self().resources.getDimension(R.dimen.font_size_14)
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

    override fun measure(w: Int, h: Int, paint: Paint) {
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

            if (mBitmap != null){
                if (width >= mBitmap!!.width || height >= mBitmap!!.height ){
                    mBitmap!!.recycle()
                    mBitmap = null
                }else mBitmap!!.reconfigure(width,height,Bitmap.Config.ARGB_8888)
            }
            val bitmap = if (mBitmap != null){
                mBitmap!!
            }else{
                Bitmap.createBitmap(codeWidth, codeHeight, Bitmap.Config.ARGB_8888)
            }
            bitmap.setPixels(pixels, 0, codeWidth, 0, 0, codeWidth, codeHeight)

            return bitmap
        } catch (e: WriterException) {
            MyDialog.toastMessage(e.message)
        }
        return null
    }
}