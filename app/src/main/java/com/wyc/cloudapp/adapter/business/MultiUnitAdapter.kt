package com.wyc.cloudapp.adapter.business

import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.wyc.cloudapp.CustomizationView.ItemPaddingLinearLayout
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.mobile.business.MobileEditGoodInfoActivity
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.MultiUnitInfo
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.utils.Utils
import java.lang.NumberFormatException

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.adapter.business
 * @ClassName:      MultiUnitAdapter
 * @Description:    多单位适配器
 * @Author:         wyc
 * @CreateDate:     2021-09-13 17:15
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-09-13 17:15
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class MultiUnitAdapter(private val context:MobileEditGoodInfoActivity): AbstractActionAdapter<MultiUnitInfo, MultiUnitAdapter.MyViewHolder>() {
    private var mDelData:MutableList<MultiUnitInfo>? = null
    class MyViewHolder(itemView: View):AbstractActionAdapter.MyViewHolder(itemView){

        init {
            ButterKnife.bind(this,itemView)
            barcode.addTextChangedListener(ChangeText(barcode,this))
            unit.addTextChangedListener(ChangeText(unit,this))
            conversion.addTextChangedListener(ChangeText(conversion,this))
            retail_price.addTextChangedListener(ChangeText(retail_price,this))
            vip_price.addTextChangedListener(ChangeText(vip_price,this))
            trade_price.addTextChangedListener(ChangeText(trade_price,this))
            ps_price.addTextChangedListener(ChangeText(ps_price,this))
        }
        @BindView(R.id.barcode)
        lateinit var barcode:EditText
        @BindView(R.id.unit)
        lateinit var unit:EditText
        @BindView(R.id.conversion)
        lateinit var conversion:EditText
        @BindView(R.id.retail_price)
        lateinit var retail_price:EditText
        @BindView(R.id.vip_price)
        lateinit var vip_price:EditText
        @BindView(R.id.trade_price)
        lateinit var trade_price:EditText
        @BindView(R.id.ps_price)
        lateinit var ps_price:EditText
    }

    override fun getContentId(): Int {
        return R.layout.multi_unit_content
    }

    override fun getNewData(): MultiUnitInfo? {
         return if (itemCount < 2)
            MultiUnitInfo(itemCount == 0)
        else{
            getItem(itemCount - 2)?.let {
                if (Utils.isNotEmpty(it.barcode))
                    return MultiUnitInfo().copy(mData[0])
            }
            null
        }
    }

    override fun bindHolder(holder: MyViewHolder, data: MultiUnitInfo) {
        holder.barcode.setText(data.barcode)
        holder.barcode.tag = data

        if (!holder.unit.hasOnClickListeners()){
            holder.unit.setOnClickListener(click)
        }
        holder.unit.setText(data.unit_name)

        holder.conversion.setText(String.format("%.2f",data.conversion))
        holder.retail_price.setText(String.format("%.2f",data.retail_price))
        holder.vip_price.setText(String.format("%.2f",data.yh_price))
        holder.trade_price.setText(String.format("%.2f",data.trade_price))
        holder.ps_price.setText(String.format("%.2f",data.ps_price))

        if (data.plus){
            (holder.barcode.parent as? View)?.visibility = View.GONE
        }else if (holder.adapterPosition == 0){
            (holder.itemView  as? ItemPaddingLinearLayout)?.let {
                it.setDisableEvent(true)
                it.setCentreLabel("禁止修改")
            }
        }
        holder.itemView.tag = mData
    }

    override fun onViewRecycled(holder: MyViewHolder) {
        (holder.itemView  as? ItemPaddingLinearLayout)?.let {
            it.setDisableEvent(false)
            it.setCentreLabel(null)
        }
        (holder.barcode.parent as? View)?.visibility = View.VISIBLE
    }

    private val click = View.OnClickListener { v -> context.showUnit(v as TextView?) }

    override fun getViewHolder(itemView: View): MyViewHolder {
        return MyViewHolder(itemView)
    }

    fun getSubmitData(): MutableList<MultiUnitInfo> {
        val data = super.getValidData()
        return if (data.size > 0){
            data.removeAt(0)
            data
        }else data;
    }

    private class ChangeText(private val obj:EditText,private val holder: MyViewHolder):TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val data = (holder.itemView.tag as? MutableList<*>)
            val posData = data?.get(holder.adapterPosition) as? MultiUnitInfo
            posData?.let {
                try {
                    when(obj.id){
                        R.id.barcode ->{
                            it.barcode = s.toString()
                        }
                        R.id.unit ->{
                            it.unit_name = s.toString()
                        }
                        R.id.conversion ->{
                            val d = s.toString().toDouble()
                            val firstData = data[0] as? MultiUnitInfo
                            firstData?.let {
                                holder.retail_price.setText(String.format("%.2f",firstData.retail_price * d))
                                holder.vip_price.setText(String.format("%.2f",firstData.yh_price * d))
                                holder.ps_price.setText(String.format("%.2f",firstData.ps_price * d))
                                holder.trade_price.setText(String.format("%.2f",firstData.trade_price * d))
                            }
                            it.conversion = d
                        }
                        R.id.retail_price ->{
                            it.retail_price = s.toString().toDouble()
                        }
                        R.id.vip_price ->{
                            it.yh_price = s.toString().toDouble()
                        }
                        R.id.trade_price ->{
                            it.trade_price = s.toString().toDouble()
                        }
                        R.id.ps_price ->{
                            it.ps_price = s.toString().toDouble()
                        }
                    }
                }catch (e:NumberFormatException){
                    e.printStackTrace()
                    MyDialog.toastMessage(e.message)
                }
            }
        }
    }

    override fun deleteItem(data: Action) {
        (data as? MultiUnitInfo)?.let {
            it.barcode_status = 2
            if (mDelData == null)mDelData = mutableListOf()
            mDelData!!.add(it)
        }
    }

    override fun isValid(): Boolean {
        return super.getValidData().all {
            if (!Utils.isNotEmpty(it.barcode)){
                MyDialog.toastMessage(CustomApplication.getNotEmptyHintsString(CustomApplication.self().getString(R.string.barcode)))
                return false
            }
            if (!Utils.isNotEmpty(it.unit_name)){
                MyDialog.toastMessage(CustomApplication.getNotEmptyHintsString(CustomApplication.self().getString(R.string.unit_sz)))
                return false
            }
            if (!Utils.isNotEmpty(it.conversion.toString())){
                MyDialog.toastMessage(CustomApplication.getNotEmptyHintsString(CustomApplication.self().getString(R.string.conversion_ratio)))
                return false
            }
            true
        }
    }
}