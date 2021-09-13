package com.wyc.cloudapp.adapter.business

import android.view.View
import android.widget.EditText
import butterknife.BindView
import butterknife.ButterKnife
import com.wyc.cloudapp.R
import com.wyc.cloudapp.bean.AuxiliaryBarcode
import com.wyc.cloudapp.bean.MultiUnitInfo

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
class MultiUnitAdapter: AbstractActionAdapter<MultiUnitInfo, MultiUnitAdapter.MyViewHolder>() {
    init {
        mData = ArrayList()
        mData.add(MultiUnitInfo(true))
    }
    class MyViewHolder(itemView: View):AbstractActionAdapter.MyViewHolder(itemView){
        init {
            ButterKnife.bind(this,itemView)
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

    override fun getDefaultData(): MultiUnitInfo {
        return MultiUnitInfo(true)
    }

    override fun bindHolder(holder: MyViewHolder, data: MultiUnitInfo, flag: Boolean) {
        holder.barcode.setText(data.barcode)
        holder.unit.setText(data.unit_name)
        holder.conversion.setText(data.conversion.toString())
        holder.retail_price.setText(data.retail_price.toString())
        holder.vip_price.setText(data.yh_price.toString())
        holder.trade_price.setText(data.trade_price.toString())
        holder.ps_price.setText(data.ps_price.toString())
        if (flag){
            (holder.barcode.parent as? View)?.visibility = View.GONE
        }
    }

    override fun getViewHolder(itemView: View): MyViewHolder {
        return MyViewHolder(itemView)
    }
}