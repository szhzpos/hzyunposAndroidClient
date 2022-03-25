package com.wyc.cloudapp.activity.normal

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity
import com.wyc.cloudapp.customizationView.TopDrawableTextView
import com.wyc.cloudapp.design.LabelView

class LabelActivity : AbstractDefinedTitleActivity(), View.OnClickListener {
    private var mLabelView:LabelView? = null
    private var mCurBtn:TopDrawableTextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMiddleText(getString(R.string.label_setting))

        mLabelView = findViewById<LabelView>(R.id.labelView)
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
    private fun swapCurBtn(v: View){
        if (mCurBtn != null){
            mCurBtn!!.triggerAnimation(false)
        }
        mCurBtn = v as? TopDrawableTextView
        mCurBtn?.triggerAnimation(true)
    }

    override fun onClick(v: View) {
        swapCurBtn(v)

        mLabelView?.apply {
            when(v.id){
                R.id.delete->{
                    deleteItem()
                }
                R.id.shrink->{
                    shrinkItem()
                }
                R.id.zoom->{
                    zoomItem()
                }
                R.id.rotate->{
                    rotateItem()
                }
                R.id.undo->{

                }
                R.id.text->{
                    addTextItem()
                }
                R.id.barcode->{
                    addBarcodeItem()
                }
                R.id.qrcode->{
                    addQRCodeItem()
                }
                R.id.line->{
                    addLineItem()
                }
                R.id.rect->{
                    addRectItem()
                }
                R.id.circle->{
                    addCircleItem()
                }
                R.id.date->{

                }
                R.id.data->{

                }
                R.id.image->{

                }
            }
        }
    }
}