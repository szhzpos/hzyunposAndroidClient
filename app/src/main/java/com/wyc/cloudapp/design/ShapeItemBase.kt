package com.wyc.cloudapp.design
import android.graphics.*
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.logger.Logger
import kotlin.math.max


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.design
 * @ClassName:      ShapeItem
 * @Description:    形状
 * @Author:         wyc
 * @CreateDate:     2022/3/16 14:21
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/3/16 14:21
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

open class ShapeItemBase:ItemBase() {
    init {
        width = CustomApplication.self().resources.getDimensionPixelOffset(R.dimen.width_88)
    }
    var borderWidth = MIN_BORDER_WIDTH
        set(value) {
            field = max(value, MIN_BORDER_WIDTH)
        }
    var borderColor: Int = Color.BLACK
    var hasfill = false
    var hasDash = false

    override fun drawItem(offsetX: Float, offsetY: Float, canvas: Canvas, paint: Paint) {
        paint.strokeWidth = borderWidth
        paint.color = borderColor

        if (hasfill)
            paint.style = Paint.Style.FILL
        else paint.style = Paint.Style.STROKE

        if (hasDash)paint.pathEffect = DashPathEffect(floatArrayOf(4f,4f),0f)

        drawShape(offsetX, offsetY, canvas,paint)

        paint.strokeWidth = MIN_BORDER_WIDTH
        if (hasfill)paint.style = Paint.Style.STROKE
        if (hasDash)paint.pathEffect = null
    }
    open fun drawShape(offsetX: Float, offsetY: Float, canvas: Canvas, paint: Paint){

    }

    override fun createItemBitmap(bgColor:Int): Bitmap {
        val offset = borderWidth / 2
        val bmp = Bitmap.createBitmap((width + offset).toInt(), (height + offset).toInt(), Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        c.translate(-left + offset, -top + offset)
        c.drawColor(bgColor)
        draw(0f,0f,c, Paint())
        return bmp
    }

    override fun toString(): String {
        return "ShapeItemBase(borderWidth=$borderWidth, borderColor=$borderColor, hasfill=$hasfill, hasDash=$hasDash) ${super.toString()}"
    }

}