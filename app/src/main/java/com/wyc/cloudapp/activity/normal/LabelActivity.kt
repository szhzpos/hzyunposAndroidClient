package com.wyc.cloudapp.activity.normal

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity

class LabelActivity : AbstractDefinedTitleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMiddleText(getString(R.string.label_setting))
    }

    override fun getContentLayoutId(): Int {
        return R.layout.activity_format
    }

    override fun hasSlide(): Boolean {
        return false
    }

    companion object{
        @JvmStatic
        fun start(context: Activity){
            context.startActivity(Intent(context,LabelActivity::class.java))
        }
    }
}