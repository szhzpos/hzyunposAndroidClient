package com.wyc.cloudapp.design

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.design
 * @ClassName:      DataItem
 * @Description:    数据列
 * @Author:         wyc
 * @CreateDate:     2022/3/28 17:32
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/3/28 17:32
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class DataItem:TextItem() {
    var field = ""

    override fun drawItem(offsetX: Float, offsetY: Float, canvas: Canvas, paint: Paint) {
        super.drawItem(offsetX, offsetY, canvas, paint)


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

    enum class FIELD(f:String,n:String){
        Title("goodsTitle","商品名称"),ProductionPlace("origin","产地"),Unit("unit_id","单位"),
        Spec("specifi","规格"),Level("level","等级"),Barcode("barcode","条码"),VipPrice("yh_price","会员价"),
        RetailPrice("retail_price","零售价");
        val field = f
        val description = n
    }
}