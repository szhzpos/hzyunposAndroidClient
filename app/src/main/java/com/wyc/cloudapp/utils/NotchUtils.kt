package com.wyc.cloudapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.util.TypedValue
import android.view.WindowInsets
import androidx.annotation.NonNull
import com.wyc.cloudapp.application.CustomApplication
import java.lang.reflect.Method
import com.wyc.cloudapp.R
import java.util.*


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
        /**
         * 获取刘海高度
         */
        @JvmStatic
        fun getNotchHeight(activity: Activity): Int {
            val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())
            var notchHeight = 0
            if (hasNotchScreen(activity)) {
                //有刘海才获取高度 否则默认刘海高度是0
                if (manufacturer.equals("xiaomi", ignoreCase = true)) {
                    notchHeight = getSysStatusBarHeight() //小米刘海会比状态栏小 直接获取状态栏高度
                } else if (manufacturer.equals(
                        "huawei",
                        ignoreCase = true
                    ) || manufacturer.equals("honour", ignoreCase = true)
                ) {
                    notchHeight = getNotchSizeAtHuaWei()
                } else if (manufacturer.equals("vivo", ignoreCase = true)) {
                    //VIVO是32dp
                    notchHeight =
                        TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP,
                            32f,
                            CustomApplication.self().resources.displayMetrics
                        )
                            .toInt()
                } else if (manufacturer.equals("oppo", ignoreCase = true)) {
                    notchHeight = 80 //oppo当时是固定数值
                } else if (Build.MANUFACTURER.equals("smartisan", ignoreCase = true)) {
                    notchHeight = 82 //当时锤子PDF文档上是固定数值
                } else {
                    //其他品牌手机
                    if (activity.window != null) {
                        val decorView = activity.window.decorView
                        val windowInsets = decorView.rootWindowInsets
                        if (windowInsets != null) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                val displayCutout = windowInsets.displayCutout
                                if (displayCutout != null) {
                                    val rects: List<Rect> = displayCutout.boundingRects
                                    if (rects.size > 1) {
                                        notchHeight = rects[0].bottom
                                        return notchHeight
                                    }
                                }
                            }
                        }
                    }
                }
                return notchHeight
            }
            return 0
        }

        /**
         * 华为获取刘海高度
         * 获取刘海尺寸：width、height
         * int[0]值为刘海宽度 int[1]值为刘海高度
         */
        @JvmStatic
        fun getNotchSizeAtHuaWei(): Int {
            var height = 0
            try {
                val cl: ClassLoader = CustomApplication.self().classLoader
                val HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil")
                val get: Method = HwNotchSizeUtil.getMethod("getNotchSize")
                val ret = get.invoke(HwNotchSizeUtil) as IntArray
                height = ret[1]
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
            } catch (e: NoSuchMethodException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return height
        }

        /**
         * 获得手机状态栏高度
         * @return
         */
        private fun getSysStatusBarHeight(): Int {
            var result = 0
            val resources: Resources = CustomApplication.self().resources
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            if (result == 0) {
                result = resources.getDimension(R.dimen.size_25).toInt()
            }
            return result
        }

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