package com.wyc.cloudapp.activity.mobile.cashierDesk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity
import com.wyc.cloudapp.data.room.entity.GiftCardSaleOrder
import com.wyc.cloudapp.databinding.ActivityGiftCardOrderDetailBinding

class GiftCardOrderDetailActivity : AbstractMobileActivity() {
    private var mGiftCardSaleOrder: GiftCardSaleOrder? = null
    private lateinit var mBinding: ActivityGiftCardOrderDetailBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mGiftCardSaleOrder = intent.getParcelableExtra("o")

        mBinding = DataBindingUtil.setContentView(this, contentLayoutId)
        mBinding.giftCardSaleOrder = mGiftCardSaleOrder
    }

    companion object{
        @JvmStatic
        fun start(context: Context,@NonNull giftCardSaleOrder: GiftCardSaleOrder){
            context.startActivity(Intent(context,GiftCardOrderDetailActivity::class.java).putExtra("o",giftCardSaleOrder))
        }
    }

    override fun getContentLayoutId(): Int {
        return R.layout.activity_gift_card_order_detail
    }
}