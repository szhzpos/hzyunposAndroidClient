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

open class ItemBase:Cloneable{
    var top = 88
    var left = 88
    var width = -1
        set(value) {
            field = max(value, 0)
        }
    var height = -1
        set(value) {
            field = max(value, 0)
        }
    var radian = 0f
        set(value) {
            field = value % 360
        }

    var clsType = this::class.simpleName

    @JSONField(serialize = false)
    private var active:Boolean = false
    @JSONField(serialize = false)
    private var move:Boolean = false
    @JSONField(serialize = false)
    protected var scaling:Boolean = false
    @JSONField(serialize = false)
    private var deleting:Boolean = false
    @JSONField(serialize = false)
    protected val cRECT = RectF()

    fun draw(offsetX:Float, offsetY:Float, canvas:Canvas, paint: Paint){
        updateContentRect(offsetX + left,offsetY + top)

        val r = radian != 0f
        if (r){
            canvas.save()
            canvas.rotate(radian,offsetX+ left + width / 2f,offsetY + top + height / 2f)
        }
        drawItem(offsetX, offsetY,canvas,paint)
        if (r){
            canvas.restore()
        }
        if (active)drawAction(offsetX, offsetY,canvas,paint)
        if (move)drawItemBaseLine(offsetX, offsetY,canvas,paint)
    }

    public override fun clone(): ItemBase {
        return super.clone() as ItemBase
    }

    @CallSuper
    open fun transform(scaleX: Float,scaleY: Float){
        left = (left * scaleX).toInt()
        top = (top * scaleY).toInt()
        width = (width * scaleX).toInt()
        height = (height * scaleY).toInt()
    }

    private fun updateActionRect(l:Float,t:Float){
        val diameter = ACTION_RADIUS * 2
        CUR_RECT.set(l - diameter,t - diameter,l + width + diameter,t + height + diameter)
        DEL_RECT.set(CUR_RECT.left,CUR_RECT.top,CUR_RECT.left + diameter,CUR_RECT.top + diameter)
        SCALE_RECT.set(CUR_RECT.right - diameter, CUR_RECT.bottom - diameter, CUR_RECT.right, CUR_RECT.bottom)

        if (radian != 0f){
            ROTATE_MATRIX.setRotate(radian,l + width / 2f,t + height / 2f)
            DEL_RECT.transform(ROTATE_MATRIX)
            SCALE_RECT.transform(ROTATE_MATRIX)
            CUR_RECT.transform(ROTATE_MATRIX)
        }
    }

    private fun updateContentRect(l: Float,t: Float){
        cRECT.set(l,t,l + width,t + height)
        if (radian != 0f){
            ROTATE_MATRIX.setRotate(radian,l + width / 2f,t + height / 2f)
            cRECT.transform(ROTATE_MATRIX)
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

    fun measure(w:Int, h:Int){
        if (hasInit()){
            measureItem(w,h)
        }
    }
    protected fun hasInit():Boolean{
        return width == -1 && height == -1
    }

    protected open fun measureItem(w:Int, h:Int) {
        width = min(width,w)
        height = min(height, h)
    }
    private fun drawItemBaseLine(offsetX:Float, offsetY:Float, canvas:Canvas, paint: Paint){
        paint.color = Color.RED
        paint.style = Paint.Style.STROKE
        paint.pathEffect = DashPathEffect(floatArrayOf(4f,4f),0f)

        canvas.drawLine(offsetX,cRECT.top, canvas.width.toFloat(),cRECT.top,paint)
        canvas.drawLine(offsetX,cRECT.top + cRECT.height(),canvas.width.toFloat(),cRECT.top + cRECT.height(),paint)

        canvas.drawLine(cRECT.left,offsetY, cRECT.left,canvas.height + offsetY,paint)
        canvas.drawLine(cRECT.left + cRECT.width(),offsetY,  cRECT.left + cRECT.width(),canvas.height + offsetY,paint)

        paint.pathEffect = null
    }
    private fun drawAction(offsetX:Float, offsetY:Float, canvas:Canvas, paint: Paint) {
        updateActionRect(offsetX + left,offsetY + top)

        val rc = Utils.dpToPxF(CustomApplication.self(),1f)

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
        deleting = active && DEL_RECT.contains(clickX,clickY)
    }
    private fun checkScaleClick(clickX:Float, clickY:Float, scaleX:Float, scaleY:Float):Boolean{
        if (scaling || SCALE_RECT.contains(clickX,clickY)){
            if ((scaleX > 0.0f || scaleY > 0f)  || !DEL_RECT.intersect(SCALE_RECT)){
                val directRadius = (asin(scaleY / sqrt((scaleX * scaleX + scaleY * scaleY).toDouble())) * 180 / Math.PI).toFloat()
                if (!directRadius.isNaN()){
                    SCALE_DIRECT = directRadius
                }
                scaling = true

                scale(scaleX,scaleY)
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

        if (!deleting && !hScale){
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
    fun hasSelect(x:Float, y:Float, offsetX: Int, offsetY: Int):Boolean{
        cRECT.set(left.toFloat(), top.toFloat(), (left + width).toFloat(), (top + height).toFloat())
        if (radian != 0f){
            ROTATE_MATRIX.setRotate(radian,left + width / 2f,top + height / 2f)
            cRECT.transform(ROTATE_MATRIX)
        }
        active = DEL_RECT.contains(x,y) || SCALE_RECT.contains(x,y) || cRECT.contains(x - offsetX,y - offsetY)
        return active
    }
    fun disableItem(){
        active = false
        move = false
        scaling = false
        deleting = false
    }

    fun releaseItem() {
        move = false
        scaling = false
    }

    fun hasDelete():Boolean{
        return deleting
    }

    fun activeItem(){
        active = true
    }

    open fun createItemBitmap(bgColor:Int = Color.WHITE):Bitmap{
        var bmp:Bitmap = Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888)
        if (radian != 0f){
            val rect = RectF(left.toFloat(), top.toFloat(), left + width.toFloat(), top + height.toFloat())

            val matrix = Matrix()
            matrix.setRotate(radian,rect.centerX(),rect.centerY())
            matrix.mapRect(rect)
            left = rect.left.toInt()
            top = rect.top.toInt()

            bmp.recycle()
            bmp = Bitmap.createBitmap(rect.width().toInt(), rect.height().toInt(),Bitmap.Config.ARGB_8888)
            val c = Canvas(bmp)
            c.drawColor(bgColor)

            c.translate((-left).toFloat(), (-top).toFloat())
            c.rotate(radian,  rect.centerX(), rect.centerY())

            drawItem((rect.width() - width) / 2f,(rect.height() - height) / 2f,c, Paint())
        }else{
            val c = Canvas(bmp)
            c.drawColor(bgColor)
            c.translate((-left).toFloat(), (-top).toFloat())
            drawItem(0f,0f,c, Paint())
        }
        return bmp
    }

    override fun toString(): String {
        return "ItemBase(top=$top, left=$left, width=$width, height=$height, radian=$radian, clsType=$clsType, active=$active, move=$move, scaling=$scaling, deleting=$deleting, cRECT=$cRECT)"
    }

    companion object{
        @JvmField
        val CUR_RECT = RectF()
        @JvmField
        val DEL_RECT = RectF()
        @JvmField
        val SCALE_RECT = RectF()
        @JSONField
        val ROTATE_MATRIX = Matrix()
        @JvmField
        val ACTION_RADIUS = CustomApplication.self().resources.getDimension(R.dimen.size_10)
        @JvmField
        var SCALE_DIRECT = 45.0f
        @JvmField
        val MIN_BORDER_WIDTH = CustomApplication.self().resources.getDimension(R.dimen.size_1)
    }
}