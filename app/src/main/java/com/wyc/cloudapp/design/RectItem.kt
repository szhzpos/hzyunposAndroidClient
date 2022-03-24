package com.wyc.cloudapp.design

import android.graphics.Canvas
import android.graphics.Paint
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.design
 * @ClassName:      RectItem
 * @Description:    画矩形
 * @Author:         wyc
 * @CreateDate:     2022/3/23 13:37
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/3/23 13:37
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class RectItem:ShapeItemBase() {
    var rc = 0f
    init {
        height = CustomApplication.self().resources.getDimensionPixelOffset(R.dimen.height_88)
    }

    override fun drawShape(offsetX: Float, offsetY: Float, canvas: Canvas, paint: Paint) {
        canvas.drawRoundRect(left + offsetX,top + offsetY,left + offsetX + width,top + offsetY + height,rc,rc,paint)
    }

    override fun toString(): String {
        return "RectItem(rc=$rc) ${super.toString()}"
    }

}