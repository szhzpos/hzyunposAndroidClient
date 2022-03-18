package com.wyc.cloudapp.design

import android.graphics.Canvas
import android.graphics.Paint


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

class ShapeItem:ItemBase<ShapeItem.Shape>() {
    enum class Shape {
        LINE,RECT,CIRCLE
    }
    override fun draw(offsetX:Float,offsetY:Float,canvas: Canvas,paint: Paint) {

    }
}