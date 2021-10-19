package com.wyc.cloudapp.activity.base

import android.view.View

interface ITitle {
    fun getLeftText():String
    fun getMiddleText():String
    fun getRightText():String
    fun onLeftClick(view:View)
    fun onMiddleClick(view:View)
    fun onRightClick(view:View)
}