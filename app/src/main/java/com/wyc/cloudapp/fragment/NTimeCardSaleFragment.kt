package com.wyc.cloudapp.fragment

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity
import com.wyc.cloudapp.adapter.AbstractDataAdapter
import com.wyc.cloudapp.adapter.business.TimeCardSaleAdapterBase

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.fragment
 * @ClassName:      NTimeCardSaleFragment
 * @Description:    宽屏次卡销售
 * @Author:         wyc
 * @CreateDate:     2021-10-19 13:58
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-10-19 13:58
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class NTimeCardSaleFragment: TimeCardSaleFragmentBase() {
    override fun getRootLayout(): Int {
        return R.layout.normal_time_card_sale
    }

    override fun getSaleAdapter(): TimeCardSaleAdapterBase<out AbstractDataAdapter.SuperViewHolder> {
        return NTimeCardSaleAdapter(mContext)
    }

    private class NTimeCardSaleAdapter(c: MainActivity?) : TimeCardSaleAdapterBase<NTimeCardSaleAdapter.MyViewHolder>(c) {

        public class MyViewHolder(itemView: View?) : SuperViewHolder(itemView) {
            init {
                ButterKnife.bind(this, itemView!!)
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
             return MyViewHolder(null)
        }

        override fun bindView(holder: MyViewHolder, position: Int) {

        }
    }
}