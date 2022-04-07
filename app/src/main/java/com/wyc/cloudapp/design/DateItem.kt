package com.wyc.cloudapp.design

import android.graphics.Canvas
import android.graphics.Paint
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.FormatDateTimeUtils
import java.lang.StringBuilder
import kotlin.math.min


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.design
 * @ClassName:      DateItem
 * @Description:    日期
 * @Author:         wyc
 * @CreateDate:     2022/3/28 13:38
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/3/28 13:38
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class DateItem: TextItem() {
    var dateFormat = FORMAT.Y_M_D_H_M_S
    override var content: String = FormatDateTimeUtils.formatCurrentTime(dateFormat.format)

    var autoUpdate = true

    override fun drawItem(offsetX: Float, offsetY: Float, canvas: Canvas, paint: Paint) {
        if (autoUpdate){
            val v = FormatDateTimeUtils.formatCurrentTime(dateFormat.format)
            val index = content.indexOf("\n")
            if (index != -1 && v.length > index){
                val stringBuilder = StringBuilder(v)
                content = stringBuilder.insert(index,"\n").toString()
            }
        }
        super.drawItem(offsetX, offsetY, canvas, paint)
    }

    enum class FORMAT(f:String,d:String){
        Y_M("YYYY-MM","年-月"),Y_M_D("YYYY-MM-dd","年-月-日"),Y_M_D_H_M("YYYY-MM-dd HH:mm","年-月-日 时:分"),
        Y_M_D_H_M_S("YYYY-MM-dd HH:mm:ss","年-月-日 时:分:秒"),D_M_Y("dd-MM-YYYY","日-月-年");
        val format = f
        val description = d
    }
}