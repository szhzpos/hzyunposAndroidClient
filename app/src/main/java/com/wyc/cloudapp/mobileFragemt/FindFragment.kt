package com.wyc.cloudapp.mobileFragemt

import android.app.Activity.RESULT_OK
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.zxing.client.android.CaptureActivity
import com.wyc.cloudapp.adapter.business.AbstractBusinessOrderDataAdapter
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.mobileFragemt
 * @ClassName:      ScanFragment
 * @Description:    启动Activity并返回结果
 * @Author:         wyc
 * @CreateDate:     2021-09-16 10:11
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-09-16 10:11
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class FindFragment(private var mIntent: Intent):Fragment() {
    private val request_code = 0x111
    private val request_code_1 = 0x112
    private var mCallBack: Callback? = null
    private var mRequestType = 0
    companion object{
        @JvmStatic
        fun beginScan(activity: FragmentActivity, callback: Callback){
            val fragment = FindFragment(Intent(activity,CaptureActivity::class.java))
            fragment.mCallBack = callback
            fragment.attachActivity(activity)
        }

        @JvmStatic
        fun beginRequestOrderId(activity: FragmentActivity, intent: Intent, callback: Callback){
            val fragment = FindFragment(intent)
            fragment.mRequestType = 1
            fragment.mCallBack = callback
            fragment.attachActivity(activity)
        }
    }

    protected fun finalize(){
        Logger.d("%s has finalized",javaClass.simpleName)
    }

    private fun attachActivity(activity: FragmentActivity) {
        activity.supportFragmentManager.beginTransaction().add(this, this.toString()).commitAllowingStateLoss()
    }

    private fun detachActivity(activity: FragmentActivity) {
        activity.supportFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
    }
    interface Callback {
        fun scan(code: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            when(mRequestType){
                1 ->{
                    startActivityForResult(mIntent,request_code_1)
                }else ->{
                startActivityForResult(mIntent,request_code)
            }
            }
        }catch (e: ActivityNotFoundException){
            MyDialog.toastMessage(e.message)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCallBack = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         if (resultCode == RESULT_OK){
             when(requestCode){
                 request_code->{
                     data?.getStringExtra(CaptureActivity.CALLBACK_CODE)?.let {
                         mCallBack?.scan(it)
                     }
                 }
                 request_code_1->{
                     data?.getStringExtra(AbstractBusinessOrderDataAdapter.KEY)?.let {
                         mCallBack?.scan(it)
                     }
                 }
             }
         }
        mCallBack = null
        activity?.let {
            detachActivity(it)
        }
    }
}