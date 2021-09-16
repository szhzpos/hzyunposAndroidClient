package com.wyc.cloudapp.adapter.business

import android.annotation.SuppressLint
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.MainActivity
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.AuxiliaryBarcode
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.dialog.business.BusinessSelectGoodsDialog
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.mobileFragemt.ScanFragment
import com.wyc.cloudapp.utils.Utils

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.adapter.business
 * @ClassName:      AuxiliaryAdapter
 * @Description:    辅助条码适配器
 * @Author:         wyc
 * @CreateDate:     2021-09-13 10:01
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-09-13 10:01
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class AuxiliaryBarcodeAdapter(private val mContext: MainActivity) : AbstractActionAdapter<AuxiliaryBarcode, AuxiliaryBarcodeAdapter.MyViewHolder>() {
    private var mDelData:MutableList<AuxiliaryBarcode>? = null
    class MyViewHolder(itemView: View):AbstractActionAdapter.MyViewHolder(itemView){
        var mWatcher: TextWatcher? = null
        init {
            ButterKnife.bind(this, itemView)
        }
        @BindView(R.id.barcode)
        lateinit var barcode:EditText
    }

    override fun getContentId(): Int {
       return R.layout.auxiliary_barcode_content
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindHolder(holder: MyViewHolder, data: AuxiliaryBarcode) {
        if (data.plus){
            holder.barcode.isEnabled = false
        }else{
            holder.barcode.isEnabled = true
            holder.barcode.setText(data.fuzhu_barcode)
        }
        if (holder.mWatcher == null){
            holder.mWatcher = object :TextWatcher{
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    getItem(holder.adapterPosition)?.let {
                        it.fuzhu_barcode = s.toString()
                    }
                }
            }
            holder.barcode.addTextChangedListener(holder.mWatcher)
        }
        if (holder.adapterPosition == itemCount - 1){
            holder.barcode.visibility = View.INVISIBLE
        }else{
            if (holder.barcode.visibility == View.INVISIBLE){
                holder.barcode.visibility = View.VISIBLE
            }
            holder.barcode.setOnTouchListener(touch)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    val touch = View.OnTouchListener { v, motionEvent ->
        if (motionEvent.action == MotionEvent.ACTION_DOWN){
            (v as? EditText)?.let {
                val dx: Float = motionEvent.x
                val w: Int = it.width
                if (dx > w - it.compoundPaddingRight) {
                    ScanFragment.beginScan(mContext, object : ScanFragment.ScanCallback {
                        override fun scan(code: String) {
                            it.setText(code)
                        }
                    })
                    return@OnTouchListener true
                }
            }
        }
        false
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        holder.barcode.text.clear()
    }

    override fun getNewData(): AuxiliaryBarcode? {
        return if (itemCount < 2)
             AuxiliaryBarcode(itemCount == 0)
        else{
            getItem(itemCount - 2)?.let {
                if (Utils.isNotEmpty(it.fuzhu_barcode)){
                    return AuxiliaryBarcode(false)
                }
            }
            null
        }
    }

    override fun getViewHolder(itemView: View): MyViewHolder {
        return MyViewHolder(itemView)
    }

    override fun deleteItem(data: Action) {
        (data as? AuxiliaryBarcode)?.let {
            if (it.hasNotNew()){
                if (null == mDelData)mDelData = mutableListOf()
                it.status = 2
                mDelData!!.add(it)
            }
        }
    }
    override fun getValidData(): MutableList<AuxiliaryBarcode> {
        val data = super.getValidData()
        mDelData?.let { data.addAll(it) }
        return data
    }
    override fun isValid(): Boolean {
        return super.getValidData().all {
            if (!Utils.isNotEmpty(it.fuzhu_barcode)){
                MyDialog.toastMessage(CustomApplication.getNotEmptyHintsString(CustomApplication.self().getString(R.string.barcode)))
                return false
            }
            true
        }
    }
}