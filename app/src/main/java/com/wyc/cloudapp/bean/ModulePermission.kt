package com.wyc.cloudapp.bean

import android.view.View
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.ModulePermission.Companion.getModulePermission
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.Utils

data class ModulePermission(var mdl_snid:Int,var mdl_id:Int,var mdl_pid:Int,var mdl_name:String,var mdl_status:Int){

    constructor(snid:Int):this(snid,0,0,"",1)

    fun invalid():Boolean{
        return mdl_status == 2
    }

    companion object{
        @JvmStatic
        fun getModulePermission():MutableList<ModulePermission>{
            val `object` = JSONObject()
            val sql =
                "select parameter_content from local_parameter where parameter_id = 'module_permission'"
            if (SQLiteHelper.execSql(`object`, sql)) {
                val ids = Utils.getNullObjectAsEmptyJsonArray(`object`, "parameter_content")
                Logger.d_json(ids)
                return ids.toJavaList(
                    ModulePermission::class.java
                )
            } else {
                MyDialog.toastMessage(`object`.getString("info"))
            }
            return mutableListOf()
        }

        @JvmStatic
        fun checkModulePermission(moduleId:Int):Boolean{
            val permissionList = CustomApplication.self().modulePermissions
            val index = permissionList.indexOf(ModulePermission(moduleId))
            if (index >= 0) {
                return permissionList[index].invalid()
            }
            return false
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ModulePermission

        if (mdl_snid != other.mdl_snid) return false

        return true
    }

    override fun hashCode(): Int {
        return mdl_snid
    }
}
