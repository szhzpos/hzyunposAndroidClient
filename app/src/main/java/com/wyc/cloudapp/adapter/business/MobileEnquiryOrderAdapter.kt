package com.wyc.cloudapp.adapter.business

import android.text.Html
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.MainActivity
import com.wyc.cloudapp.utils.FormatDateTimeUtils

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.adapter.business
 * @ClassName:      MobileEnquiryOrderAdapter
 * @Description:    要货申请单数据适配器
 * @Author:         wyc
 * @CreateDate:     2021-09-03 9:59
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-09-03 9:59
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class MobileEnquiryOrderAdapter(context: MainActivity): MobileBaseOrderAdapter<MobileEnquiryOrderAdapter.MyViewHolder>(context) {

    class MyViewHolder(itemView: View?) : MobileBaseOrderAdapter.MyViewHolder(itemView) {
        var target_wh_tv: TextView
        init {
            target_wh_tv = findViewById(R.id.target_wh_tv)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = View.inflate(mContext, R.layout.mobile_enquiry_order_content_adapter, null)
        itemView.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        return MyViewHolder(itemView)
    }

    override fun bindViewHolder(holder: MyViewHolder, `object`: JSONObject?) {
        holder.order_code_tv.text = Html.fromHtml("<u>" + `object`!!.getString("yhd_code") + "</u>")
        holder.order_code_tv.tag = `object`!!.getString("yhd_id")
        holder.date_tv.text = FormatDateTimeUtils.formatTimeWithTimestamp(`object`!!.getLongValue("addtime") * 1000)
        holder.target_wh_tv.setText(`object`!!.getString("mb_wh_name"))
    }
}