package com.wyc.cloudapp.activity.mobile.cashierDesk

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import butterknife.OnClick
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.AbsBindingActivity
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity
import com.wyc.cloudapp.adapter.AbstractDataAdapter
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList
import com.wyc.cloudapp.data.room.entity.GiftCardPayDetail
import com.wyc.cloudapp.data.room.entity.GiftCardSaleDetail
import com.wyc.cloudapp.data.room.entity.GiftCardSaleOrder
import com.wyc.cloudapp.databinding.ActivityGiftCardOrderDetailBinding
import com.wyc.cloudapp.databinding.MobileGiftCardDetailAdapterBinding
import com.wyc.cloudapp.databinding.MobileGiftCardPayAdapterBinding

class GiftCardOrderDetailActivity : AbsBindingActivity() {
    private var mBinding: ActivityGiftCardOrderDetailBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = getBindingData()
        initOrder()
        initSaleDetail()
        initPayDetail()
    }

    @OnClick(R.id.m_print_btn)
    fun print(){
        mBinding?.giftCardSaleOrder?.print(this)
    }

    private fun initOrder(){
        val saleOrder:GiftCardSaleOrder = intent.getParcelableExtra("o") ?: throw IllegalArgumentException("mOrder must not be empty...")
        saleOrder.setSaleInfo(GiftCardSaleDetail.getSaleDetailById(saleOrder.getOrder_no()))
        saleOrder.payInfo = GiftCardPayDetail.getPayDetailById(saleOrder.getOrder_no()) ?: ArrayList()
        mBinding?.giftCardSaleOrder = saleOrder
    }

    private fun initSaleDetail(){
        val listView: RecyclerView? = findViewById(R.id._order_details_list)
        listView?.let {
            val mDetailAdapter = DetailAdapter()
            it.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
            it.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
            it.adapter = mDetailAdapter
            mDetailAdapter.setDataForList(mBinding?.giftCardSaleOrder?.getSaleInfo())
        }
    }

    private fun initPayDetail(){
        val listView: RecyclerView = findViewById(R.id.m_pay_details_list)
        val payAdapter = PayAdapter()
        listView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        listView.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))
        listView.adapter = payAdapter
        payAdapter.setDataForList(mBinding?.giftCardSaleOrder?.payInfo)
    }

    class DetailAdapter: AbstractDataAdapterForList<GiftCardSaleDetail, DetailAdapter.MyViewHolder>() {
        class MyViewHolder(itemView: View):AbstractDataAdapter.SuperViewHolder(itemView){
            init {
                ButterKnife.bind(this,itemView)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = View.inflate(parent.context,R.layout.mobile_gift_card_detail_adapter,null)
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val binding = DataBindingUtil.bind<MobileGiftCardDetailAdapterBinding>(holder.itemView);
            binding?.let {
                val giftCardPayDetail = getItem(position)
                giftCardPayDetail?.let {
                    binding.price.text = String.format("%s%.2f",holder.itemView.context.getString(R.string.gift_card_sale_price),giftCardPayDetail.getPrice())
                    binding.giftCardSaleDetail = giftCardPayDetail
                }
            }
        }

    }

    class PayAdapter: AbstractDataAdapterForList<GiftCardPayDetail, PayAdapter.MyViewHolder>() {
        class MyViewHolder(itemView: View):AbstractDataAdapter.SuperViewHolder(itemView)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = View.inflate(parent.context,R.layout.mobile_gift_card_pay_adapter,null)
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
            return MyViewHolder(view)
        }
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            DataBindingUtil.bind<MobileGiftCardPayAdapterBinding>(holder.itemView)?.giftCardPayDetail = getItem(position)
        }

    }

    companion object{
        @JvmStatic
        fun start(context: Context,@NonNull giftCardSaleOrder: GiftCardSaleOrder){
            context.startActivity(Intent(context,GiftCardOrderDetailActivity::class.java)
                    .putExtra("o",giftCardSaleOrder).putExtra(AbstractMobileActivity.TITLE_KEY,context.getString(R.string.gift_card_order_detail)))
        }
    }
    override fun getBindingLayoutId(): Int {
        return R.layout.activity_gift_card_order_detail
    }
}