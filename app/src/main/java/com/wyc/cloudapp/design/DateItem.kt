package com.wyc.cloudapp.design

import android.graphics.Paint
import com.wyc.cloudapp.utils.FormatDateTimeUtils
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
        get() {
            if (!hasInit() && autoUpdate && !scaling){
                val stringBuffer = StringBuffer(FormatDateTimeUtils.formatCurrentTime(dateFormat.format))
                var len = 0f
                stringBuffer.forEachIndexed {index,chars->
                    if (chars == '\n')return@forEachIndexed
                    len += (mPaint?.measureText(chars.toString())?:0f)
                    if (len > width){

                        if (index > 0 && stringBuffer[index - 1] != '\n')
                            stringBuffer.insert(index,"\n")

                        len = 0f
                    }
                }
                content = stringBuffer.toString()

                mRect.setEmpty()
                getBound(mRect,stringBuffer.toString())
                height = mRect.height()
            }
            return field
        }
    var autoUpdate = true

    enum class FORMAT(f:String,d:String){
        Y_M("YYYY-MM","年-月"),Y_M_D("YYYY-MM-dd","年-月-日"),Y_M_D_H_M("YYYY-MM-dd HH:mm","年-月-日 时:分"),
        Y_M_D_H_M_S("YYYY-MM-dd HH:mm:ss","年-月-日 时:分:秒"),D_M_Y("dd-MM-YYYY","日-月-年");
        val format = f
        val description = d
    }
}