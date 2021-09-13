package com.wyc.cloudapp.adapter.business

import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.wyc.cloudapp.R
import com.wyc.cloudapp.adapter.AbstractDataAdapter
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList
import com.wyc.cloudapp.bean.AuxiliaryBarcode

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
class AuxiliaryBarcodeAdapter : AbstractActionAdapter<AuxiliaryBarcode, AuxiliaryBarcodeAdapter.MyViewHolder>() {
    init {
        mData = ArrayList()
        mData.add(AuxiliaryBarcode(true))
    }
    class MyViewHolder(itemView: View):AbstractActionAdapter.MyViewHolder(itemView){
        init {
            ButterKnife.bind(this,itemView)
        }
        @BindView(R.id.barcode)
        lateinit var barcode:EditText
    }

    override fun getContentId(): Int {
       return R.layout.auxiliary_barcode_content
    }

    override fun bindHolder(holder: MyViewHolder, data: AuxiliaryBarcode,flag:Boolean) {
        if (flag){
            holder.barcode.isEnabled = false
        }else{
            holder.barcode.isEnabled = true
            holder.barcode.setText(data.fuzhu_barcode)
        }
    }

    override fun getDefaultData(): AuxiliaryBarcode {
        return AuxiliaryBarcode(true)
    }

    override fun getViewHolder(itemView: View): MyViewHolder {
        return MyViewHolder(itemView)
    }
}