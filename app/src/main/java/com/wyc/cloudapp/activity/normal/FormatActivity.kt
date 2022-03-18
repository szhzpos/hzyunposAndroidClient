package com.wyc.cloudapp.activity.normal

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.wyc.cloudapp.R

class FormatActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_format)
    }

    companion object{
        @JvmStatic
        fun start(context: Activity){
            context.startActivity(Intent(context,FormatActivity::class.java))
        }
    }
}