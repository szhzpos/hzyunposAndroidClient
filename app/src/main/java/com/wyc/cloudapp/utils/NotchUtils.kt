package com.wyc.cloudapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.DisplayCutout
import android.view.View
import android.view.WindowInsets
import androidx.annotation.NonNull
import java.lang.reflect.Array

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.utils
 * @ClassName:      NotchUtils
 * @Description:    刘海屏适配
 * @Author:         wyc
 * @CreateDate:     2021-11-12 14:02
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-11-12 14:02
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class NotchUtils {
    companion object{
        @JvmStatic
        fun hasNotchScreen(activity: Activity):Boolean{
            return (isAndroidP(activity) || getInt("ro.miui.notch",activity) == 1 || hasNotchInHuaWei(activity) ||
                    hasNotchInVivo(activity) || hasNotchInOppo(activity) || hasNotchInXiaoMi(activity))
        }

        @JvmStatic
        private fun isXiaoMi():Boolean{
            return "Xiaomi".equals(Build.MANUFACTURER)
        }
        @JvmStatic
        fun isAndroidP(@NonNull activity:Activity):Boolean{
            val decorView = activity.window.decorView
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
                val insets:WindowInsets? = decorView.rootWindowInsets;
                insets?.let {
                    return insets.displayCutout != null
                }
            }
            return false
        }
        @SuppressLint("PrivateApi")
        @JvmStatic
        fun getInt(key:String,activity: Activity):Int{
            var result = 0
            if (isXiaoMi()){
                try {
                    val classLoader = activity.classLoader
                    val systemProperties = classLoader.loadClass("android.os.SystemProperties")

                    val getInt = systemProperties.getMethod("getInt",String::class.java,Int::class.java)
                    result = getInt.invoke(systemProperties,key,0) as Int
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
            return result
        }

        @JvmStatic
        fun hasNotchInXiaoMi(context:Context):Boolean{
            if (isXiaoMi() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                return context.resources.getDimensionPixelSize(context.resources.getIdentifier("status_bar_height","dimen","android")) > 0
            }
            return false
        }

        @JvmStatic
        fun hasNotchInHuaWei(context: Context):Boolean{
            try {
                val cl = context.classLoader
                val hWNotchSizeUtils = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil")
                val hasNotchInScreen = hWNotchSizeUtils.getMethod("hasNotchInScreen")
                return hasNotchInScreen.invoke(hWNotchSizeUtils) as Boolean
            }catch (_:Exception){
            }
            return false
        }

        @SuppressLint("PrivateApi")
        @JvmStatic
        fun hasNotchInVivo(context: Context):Boolean{
            try {
                val cl = context.classLoader
                val ftFeature = cl.loadClass("android.util.Ftfeature")
                val methods = ftFeature.declaredMethods
                methods.forEach {
                    it?.let {
                       if (it.name.equals("isFeatureSupport",true)){
                            return it.invoke(ftFeature,0x00000020) as Boolean
                       }
                    }
                }
            }catch (_:Exception){
            }
            return false
        }

        @JvmStatic
        fun hasNotchInOppo(context: Context):Boolean{
            return context.packageManager.hasSystemFeature("com.oppo.feature.screen.heteromorphism")
        }
    }
}