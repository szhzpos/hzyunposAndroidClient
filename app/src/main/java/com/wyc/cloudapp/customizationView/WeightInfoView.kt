package com.wyc.cloudapp.customizationView

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.core.view.setPadding
import com.wyc.cloudapp.R
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.dialog.serialScales.AbstractSerialScaleImp
import com.wyc.cloudapp.utils.Utils
import java.lang.NumberFormatException

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.customizationView
 * @ClassName:      WeightInfoView
 * @Description:    称重信息
 * @Author:         wyc
 * @CreateDate:     2021-12-23 17:40
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-12-23 17:40
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class WeightInfoView(context: Context, attrs: AttributeSet?, defStyleAttr: Int): ConstraintLayout(context,attrs,defStyleAttr) {
    private val stableIco:TextView
    private val weigh:TextView
    private val mPrice:TextView
    private val mAmt:TextView
    private var mAction:OnAction? = null

    init {
        initView()

        stableIco = findViewById(R.id.stable_ico)
        weigh = findViewById(R.id.weigh)
        mPrice = findViewById(R.id.price_tv)
        mAmt = findViewById(R.id.amt_tv)
    }
    constructor(context: Context):this(context,null,0)
    constructor(context: Context, attrs: AttributeSet?) : this(context,attrs,0)
    private fun initView(){
        inflate(context, R.layout.scale, this)
        val size2 = resources.getDimension(R.dimen.size_2).toInt()
        val size5 = resources.getDimension(R.dimen.size_5).toInt()
        setPadding(size2,size5,size2,size5)

        findViewById<ImageView>(R.id.hide_btn)?.setOnClickListener {
            val group = findViewById<Group>(R.id.group_btn)
            val zeroBtn = findViewById<Button>(R.id.r_zero)
            val tareBtn = findViewById<Button>(R.id.tare)

            mAction?.apply {
                if (group.isVisible){
                    group.visibility = GONE
                    zeroBtn.setOnClickListener(null)
                    tareBtn.setOnClickListener(null)
                }else{
                    group.visibility = VISIBLE

                    zeroBtn.setOnClickListener{
                        onZero()
                    }
                    tareBtn.setOnClickListener {
                        onTare()
                    }
                }
            }
        }
    }
    fun updateInfo(stat:Int,num:Double,price:Double){
        post {
            if (Utils.getViewTagValue(stableIco, AbstractSerialScaleImp.OnReadStatus.NO_STABLE) != stat){
                stableIco.tag = stat
                when(stat){
                    AbstractSerialScaleImp.OnReadStatus.STABLE ->{
                        stableIco.setBackgroundColor(resources.getColor(R.color.stableColor))
                    }
                    AbstractSerialScaleImp.OnReadStatus.NO_STABLE ->{
                        stableIco.setBackgroundColor(resources.getColor(R.color.black))
                    }
                    else->{
                        stableIco.setBackgroundColor(0xFF888888.toInt())
                    }
                }
            }
            val inv =  hasInvalidWeight(num)
            val n = if (inv) 0.0 else num
            weigh.text = if (inv) CustomApplication.getStringByResId(R.string.invalid_weight) else String.format("%.3f",n)
            mPrice.text = String.format("%.2f",price)
            mAmt.text = String.format("%.2f",n * price)
        }
    }
    fun updatePrice(price:Double){
        var num = 0.00
        try {
            num = weigh.text.toString().toDouble()
        }catch (e:NumberFormatException){
            MyDialog.toastMessage(e.message)
        }
        mPrice.text = String.format("%.2f",price)
        mAmt.text = String.format("%.2f",num * price)
    }
    interface OnAction{
        fun onZero()
        fun onTare()
    }
    fun setAction(action: OnAction){
        mAction = action
    }
    companion object{
        @JvmField
        val INVALID = -9999.0
        @JvmStatic
        fun hasInvalidWeight(num: Double):Boolean{
            return Utils.equalDouble(num, INVALID)
        }
    }
}