package com.wyc.cloudapp.mobileFragemt

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.zxing.client.android.CaptureActivity

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.mobileFragemt
 * @ClassName:      ScanFragment
 * @Description:    扫描辅助
 * @Author:         wyc
 * @CreateDate:     2021-09-16 10:11
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-09-16 10:11
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class ScanFragment:Fragment() {
    private val REQUESTCODE = 0x111
    private var mCallBack: ScanCallback? = null

    companion object{
        @JvmStatic
        fun beginScan(activity: FragmentActivity, callback: ScanCallback){
            val fragment = ScanFragment()
            fragment.mCallBack = callback
            fragment.attachActivity(activity)
        }

    }

    private fun attachActivity(activity: FragmentActivity) {
        activity.supportFragmentManager.beginTransaction().add(this, this.toString()).commitAllowingStateLoss()
    }

    private fun detachActivity(activity: FragmentActivity) {
        activity.supportFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
    }
    interface ScanCallback {
        fun scan(code: String)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        startActivityForResult(Intent(context,CaptureActivity::class.java,),REQUESTCODE)
    }

    override fun onDestroy() {
        super.onDestroy()
        mCallBack = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
         if (resultCode == RESULT_OK && requestCode == REQUESTCODE){
             data?.getStringExtra(CaptureActivity.CALLBACK_CODE)?.let { mCallBack?.scan(it) }
         }
        mCallBack = null
        activity?.let {
            detachActivity(it)
        }
    }
}