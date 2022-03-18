package com.wyc.cloudapp.design

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.Utils
import kotlin.math.max


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

class TextItem:ItemBase<String>() {
    var fontSize = Utils.sp2px(CustomApplication.self(),12.0f).toFloat()
    var fontColor = Color.BLACK
    override fun draw(offsetX:Float,offsetY:Float,canvas: Canvas,paint: Paint) {
        if (Utils.isNotEmpty(content)){
            super.draw(offsetX, offsetY, canvas, paint)

            paint.textSize = fontSize
            paint.color = fontColor
            paint.style = Paint.Style.FILL
            val bound = Rect()
            paint.getTextBounds(content,0,content!!.length,bound)
            canvas.drawText(content!!,left + offsetX,top + offsetY + bound.height() - paint.fontMetrics.descent,paint)
        }
    }

    override fun active(offsetX: Float, offsetY: Float, canvas: Canvas, paint: Paint) {
        paint.color = Color.BLUE
        paint.style = Paint.Style.STROKE
        val l = offsetX + left
        val t = offsetX + top
        val rc = Utils.dpToPxF(CustomApplication.self(),2f)
        canvas.drawRoundRect(l,t,l + width,t + height,rc,rc,paint)
    }
}