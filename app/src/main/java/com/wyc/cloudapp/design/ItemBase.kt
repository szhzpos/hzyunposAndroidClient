package com.wyc.cloudapp.design

import android.graphics.*
import androidx.annotation.CallSuper
import androidx.core.graphics.transform
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
    var top = 88
    var left = 88
    var width = ACTION_RADIUS.toInt()
        set(value) {
            field = max(value, 0)
        }
    var height = ACTION_RADIUS.toInt()
        set(value) {
            field = max(value, 0)
        }
    var radian = 0f

    var clsType = this::class.simpleName

    @JSONField(serialize = false)
    private var active:Boolean = false
    @JSONField(serialize = false)
    private var move:Boolean = false
    @JSONField(serialize = false)
    private var scaling:Boolean = false
    @JSONField(serialize = false)
    private var deletling:Boolean = false

    fun draw(offsetX:Float, offsetY:Float, canvas:Canvas, paint: Paint){
        if (move)drawItemBaseLine(offsetX, offsetY,canvas,paint)

        val r = radian != 0f
        if (r){
            canvas.save()
            canvas.rotate(radian,offsetX+ left + width / 2f,offsetY + top + height / 2f)
        }

        if (active)drawAction(offsetX, offsetY,canvas,paint)
        drawItem(offsetX, offsetY,canvas,paint)

        if (r){
            canvas.restore()
        }
        if (radian != 0f){
            val matrix = Matrix()
            matrix.setRotate(radian,offsetX+ left + width / 2f,offsetY + top + height / 2f)
            DEL_RECT.transform(matrix)
            matrix.reset()
            matrix.setRotate(radian,offsetX+ left + width / 2f,offsetY + top + height / 2f)
            SCALE_RECT.transform(matrix)
        }
    }

    protected open fun drawItem(offsetX:Float, offsetY:Float, canvas:Canvas, paint: Paint){

    }

    open fun zoom(){
        scale(width * 0.2f,height * 0.2f)
    }
    open  fun shrink(){
        scale(-width * 0.2f,-height * 0.2f)
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
            if ((scaleX > 0.0f || scaleY > 0f)  || !DEL_RECT.intersect(SCALE_RECT)){
                val directRadius = (asin(scaleY / sqrt((scaleX * scaleX + scaleY * scaleY).toDouble())) * 180 / Math.PI).toFloat()
                if (!directRadius.isNaN()){
                    SCALE_DIRECT = directRadius
                }
                scale(scaleX,scaleY)
                scaling = true
                return true
            }else scaling =false
        }
        return false
    }
    protected open fun scale(scaleX:Float, scaleY:Float){
        width += scaleX.toInt()
        height += scaleY.toInt()
    }

    @CallSuper
    open fun moveCurItem(rWidth:Int,rHeight:Int,clickX:Float, clickY:Float,offsetX:Int, offsetY:Int,moveX:Float,moveY:Float){
        val hScale = if(top + height + ACTION_RADIUS * 2 < rHeight && left + width + ACTION_RADIUS * 2 < rWidth){
            checkScaleClick(clickX,clickY,moveX,moveY)
        }else false

        if (!deletling && !hScale){
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
        active = x >= left - diameter && x <= left + width + diameter && y >= top - diameter && y <= top + height + diameter
        return active
    }
    fun disableItem(){
        active = false
        move = false
        scaling = false
        deletling = false
        CUR_RECT.setEmpty()
    }

    fun releaseItem() {
        move = false
        scaling = false
    }

    fun hasDelete():Boolean{
        return deletling
    }

    fun activeItem(){
        active = true
    }

    override fun toString(): String {
        return "ItemBase(top=$top, left=$left, width=$width, height=$height, radian=$radian, clsType=$clsType, active=$active, move=$move)"
    }

    protected fun finalize(){
        Logger.d("%s has finalized",javaClass.simpleName)
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