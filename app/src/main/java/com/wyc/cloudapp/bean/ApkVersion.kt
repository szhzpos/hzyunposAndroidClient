package com.wyc.cloudapp.bean

data class ApkVersion(val name: String?, val hash: String, val size: Int, val dwn_url: String):Comparable<ApkVersion> {
    private var version = ""
    init {
        name?.let {
            val vIndex = it.indexOf("v")
            val lastIndex = it.lastIndexOf(".")
            if (vIndex != -1 && lastIndex != -1){
                version = it.substring(vIndex + 1, lastIndex)
            }
        }
    }
    override fun compareTo(other: ApkVersion): Int {
       return -getVersion().compareTo(other.getVersion())
    }
    fun getVersion():String{
        return version
    }
}
