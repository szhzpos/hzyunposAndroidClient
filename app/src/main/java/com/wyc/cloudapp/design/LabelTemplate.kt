package com.wyc.cloudapp.design


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.design
 * @ClassName:      LabelTemplate
 * @Description:    标签模板
 * @Author:         wyc
 * @CreateDate:     2022/3/25 18:17
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/3/25 18:17
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class LabelTemplate {
    var templateId:Int = 0
    var templateName:String = ""
    var width = 0
    var height = 0
    var itemList:MutableList<ItemBase> = mutableListOf()
}