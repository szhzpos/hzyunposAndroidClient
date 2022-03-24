package com.wyc.cloudapp.design

import android.graphics.*
import androidx.annotation.CallSuper
import com.alibaba.fastjson.annotation.JSONField
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.Utils
import kotlin.math.asin
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.design
 * @ClassName:      ItemBase
 * @Description:    设计内容基类
 * @Author:         wyc
 * @CreateDate:     2022/3/16 9:57
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/3/16 9:57
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

abstract class ItemBase{
    var top = 0
    var left = 0
    var width = ACTION_RADIUS.toInt()
        set(value) {
            field = max(value, 0)
        }
    var height = ACTION_RADIUS.toInt()
        set(value) {
            field = max(value, 0)
        }
    var radian = 0

    var clsType = this::class.simpleName

    @JSONField(serialize = false)
    var active:Boolean = false
    @JSONField(serialize = false)
    var move:Boolean = false
    @JSONField(serialize = false)
    var scaling:Boolean = false
    @JSONField(serialize = false)
    var deletling:Boolean = false

    @CallSuper
    open fun draw(offsetX:Float, offsetY:Float, canvas:Canvas, paint: Paint){
        if (move)drawItemBaseLine(offsetX, offsetY,canvas,paint)
        if (active)drawAction(offsetX, offsetY,canvas,paint)
    }
    open fun measure(w:Int, h:Int, paint: Paint){
        width = min(width,w)
        height = min(height, h)
    }
    private fun drawItemBaseLine(offsetX:Float, offsetY:Float, canvas:Canvas, paint: Paint){
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.pathEffect = DashPathEffect(floatArrayOf(4f,4f),0f)

        canvas.drawLine(offsetX,top + offsetY, canvas.width.toFloat(),top + offsetY,paint)
        canvas.drawLine(offsetX,top + offsetY + height, canvas.width.toFloat(),top + offsetY + height,paint)

        canvas.drawLine(offsetX + left,offsetY, offsetX + left,canvas.height.toFloat() + offsetY,paint)
        canvas.drawLine(offsetX + left + width,offsetY, offsetX + left + width,canvas.height.toFloat() + offsetY,paint)

        paint.pathEffect = null
    }
    private fun drawAction(offsetX:Float, offsetY:Float, canvas:Canvas, paint: Paint) {
        val l = offsetX + left
        val t = offsetY + top
        val rc = Utils.dpToPxF(CustomApplication.self(),1f)

        val diameter = ACTION_RADIUS * 2

        CUR_RECT.set(l - diameter,t - diameter,l + width + diameter,t + height + diameter)
        DEL_RECT.set(CUR_RECT.left,CUR_RECT.top,CUR_RECT.left + diameter,CUR_RECT.top + diameter)
        SCALE_RECT.set(CUR_RECT.right - diameter, CUR_RECT.bottom - diameter,
            CUR_RECT.right, CUR_RECT.bottom)

        paint.color = Color.GRAY
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0f
        canvas.drawRoundRect(CUR_RECT,rc,rc,paint)
        paint.strokeWidth = MIN_BORDER_WIDTH

        paint.color = Color.RED
        paint.style = Paint.Style.FILL
        /* 左上角删除按钮 */
        canvas.drawOval(DEL_RECT,paint)
        /*右下角缩放按钮*/
        canvas.drawOval(SCALE_RECT,paint)

        //删除图标X
        paint.color = Color.WHITE
        paint.strokeWidth = CustomApplication.self().resources.getDimension(R.dimen.size_1)
        canvas.save()
        canvas.rotate(45f,DEL_RECT.centerX(),DEL_RECT.centerY())
        canvas.scale(0.6f,0.6f,DEL_RECT.centerX(),DEL_RECT.centerY())
        canvas.drawLine(DEL_RECT.centerX(),DEL_RECT.centerY() - ACTION_RADIUS  ,DEL_RECT.centerX(),DEL_RECT.centerY() + ACTION_RADIUS,paint)
        canvas.drawLine(DEL_RECT.centerX() - ACTION_RADIUS,DEL_RECT.centerY(),DEL_RECT.centerX() + ACTION_RADIUS,DEL_RECT.centerY(),paint)
        canvas.restore()


        val rightCenterX = SCALE_RECT.centerX()
        val bottomCenterY = SCALE_RECT.centerY()
        //缩放图标<-->
        canvas.save()
        canvas.rotate(SCALE_DIRECT,rightCenterX,bottomCenterY)
        canvas.scale(0.6f,0.6f,rightCenterX,bottomCenterY)
        canvas.drawLine(rightCenterX - ACTION_RADIUS,bottomCenterY,rightCenterX + ACTION_RADIUS,bottomCenterY,paint)
        val hypotenuse = CustomApplication.self().resources.getDimension(R.dimen.size_5)
        val offset  = hypotenuse * 0.5f
        canvas.drawLine(rightCenterX - ACTION_RADIUS,bottomCenterY,rightCenterX - ACTION_RADIUS + offset,bottomCenterY - offset,paint)
        canvas.drawLine(rightCenterX - ACTION_RADIUS,bottomCenterY,rightCenterX - ACTION_RADIUS + offset,bottomCenterY + offset,paint)
        canvas.drawLine(rightCenterX + ACTION_RADIUS,bottomCenterY,rightCenterX + ACTION_RADIUS - offset,bottomCenterY - offset,paint)
        canvas.drawLine(rightCenterX + ACTION_RADIUS,bottomCenterY,rightCenterX + ACTION_RADIUS - offset,bottomCenterY + offset,paint)
        canvas.restore()
        paint.strokeWidth = 0f
    }

    fun checkDeleteClick(clickX:Float,clickY:Float){
        deletling = active && DEL_RECT.contains(clickX,clickY)
    }
    private fun checkScaleClick(clickX:Float, clickY:Float, scaleX:Float, scaleY:Float):Boolean{
        if (scaling || SCALE_RECT.contains(clickX,clickY)){

            val directRadius = (asin(scaleY / sqrt((scaleX * scaleX + scaleY * scaleY).toDouble())) * 180 / Math.PI).toFloat()
            if (!directRadius.isNaN()){
                SCALE_DIRECT = directRadius
            }
            if ((scaleX > 0.0f || scaleY > 0f)  || !DEL_RECT.intersect(SCALE_RECT)){
                scale(scaleX,scaleY)
                scaling = true
            }
            return true
        }
        return false
    }
    protected open fun scale(scaleX:Float, scaleY:Float){
        width += scaleX.toInt()
        height += scaleY.toInt()
    }

    @CallSuper
    open fun moveCurItem(rWidth:Int,rHeight:Int,clickX:Float, clickY:Float,offsetX:Int, offsetY:Int,moveX:Float,moveY:Float){
        if (!deletling && !checkScaleClick(clickX,clickY,moveX,moveY)){
            left += moveX.toInt()
            top += moveY.toInt()
            move = true

            if (!(left >= 0 && left + width <= rWidth && top >= 0 && top + height <= height)){
                if (left < 0f)left = 0
                if (left + width > rWidth)left = rWidth - width
                if (top < 0f)top = 0
                if (top + height > rHeight)top = rHeight - height
            }
        }
    }
    fun hasSelect(x:Float,y:Float):Boolean{
        val diameter = ACTION_RADIUS * 2
        return x >= left - diameter && x <= left + width + diameter && y >= top - diameter && y <= top + height + diameter
    }
    fun disableItem(){
        active = false
        move = false
        scaling = false
        deletling = false
        CUR_RECT.setEmpty()
    }

    override fun toString(): String {
        return "ItemBase(top=$top, left=$left, width=$width, height=$height, radian=$radian, clsType=$clsType, active=$active, move=$move)"
    }

    companion object{
        @JvmField
        val CUR_RECT = RectF()
        @JvmField
        val DEL_RECT = RectF()
        @JvmField
        val SCALE_RECT = RectF()
        @JvmField
        val ACTION_RADIUS = CustomApplication.self().resources.getDimension(R.dimen.size_10)
        @JvmField
        var SCALE_DIRECT = 45.0f
        @JvmField
        val MIN_BORDER_WIDTH = CustomApplication.self().resources.getDimension(R.dimen.size_1)
    }

}