package com.wyc.cloudapp.design

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import androidx.annotation.CallSuper
import com.alibaba.fastjson.annotation.JSONField
import com.wyc.cloudapp.logger.Logger

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

abstract class ItemBase<T> {
    var top = 0f
    var left = 0f
    var width = 0
    var height = 0
    var radian = 0
    var content:T? = null;

    @JSONField(serialize = false)
    var active:Boolean = false
    @JSONField(serialize = false)
    var move:Boolean = false
    @JSONField(serialize = false)
    var scaling:Boolean = false

    @CallSuper
    open fun draw(offsetX:Float,offsetY:Float,canvas:Canvas,paint: Paint){
        if (move)drawItemBaseLine(offsetX, offsetY,canvas,paint)
    }
    protected open fun active(offsetX:Float,offsetY:Float,canvas:Canvas,paint: Paint){

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
    fun hasSelect(x:Float,y:Float):Boolean{
        return x >= left && x <= left + width && y >= top && y <= top + height
    }

    override fun toString(): String {
        return "ItemBase(top=$top, left=$left, width=$width, height=$height, radian=$radian, content=$content, active=$active, move=$move)"
    }

}