package com.wyc.cloudapp.fragment

import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.Group
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.alibaba.fastjson.JSONObject
import com.bumptech.glide.Glide
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity
import com.wyc.cloudapp.adapter.AbstractDataAdapter
import com.wyc.cloudapp.adapter.AbstractSelectAdapter
import com.wyc.cloudapp.adapter.business.TimeCardSaleAdapterBase
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.TimeCardInfo
import com.wyc.cloudapp.bean.VipInfo
import com.wyc.cloudapp.decoration.SuperItemDecoration
import com.wyc.cloudapp.utils.Utils
import java.util.*

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
    private lateinit var mAdapter:TimeCardAdapter
    @BindView(R.id.sale_sum_num)
    lateinit var mSaleSumNum:TextView
    override fun viewCreated() {
        super.viewCreated()
        initCardList()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        queryTimeCardByName(null)
    }

    private fun initCardList(){
        val cardList = findViewById<RecyclerView>(R.id.card_info_list)
        cardList?.let {
            it.layoutManager = GridLayoutManager(mContext, 5)
            mAdapter = TimeCardAdapter(mContext)
            mAdapter.setSelectListener { o->add(o) }
            SuperItemDecoration.registerGlobalLayoutToRecyclerView(cardList, resources.getDimension(R.dimen.goods_height), object : SuperItemDecoration() {
                override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                    super.getItemOffsets(outRect, view, parent, state)
                    val size = mSpace shr 1
                    outRect[size, size, size] = size
                }
            })
            it.adapter = mAdapter
        }
    }

    @OnClick(R.id._clear_btn)
    fun clear(){
        clearContent()
    }
    @OnClick(R.id.add_num)
    fun addNum(){
        addNum(1)
    }
    @OnClick(R.id.minus_num)
    fun minusNum(){
        addNum(-1)
    }

    override fun getRootLayout(): Int {
        return R.layout.normal_mian_time_card
    }

    override fun getSaleAdapter(): TimeCardSaleAdapterBase<out AbstractDataAdapter.SuperViewHolder> {
        return NTimeCardSaleAdapter(mContext)
    }

    override fun cardDataChange(data: List<TimeCardInfo>?) {
        mAdapter.setDataForList(data)
    }

    override fun soldDataChange(num: Double, amt: Double) {
        super.soldDataChange(num, amt)
        mSaleSumNum.text = String.format("%.2f",num)
    }

    override fun setVipInfo(vip: VipInfo?) {
        super.setVipInfo(vip)
        findViewById<TextView>(R.id.vip_phone_num)?.text = vip?.mobile?:""
        //hideVip(vip != null);
    }

    override fun clearVipInfo() {
        super.clearVipInfo()
        //hideVip(false)
    }

    override fun setSaleman(obj: JSONObject?) {
        super.setSaleman(obj)
        //findViewById<Group>(R.id.sale_group)?.visibility = if (obj != null && !obj.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun hideVip(v: Boolean) {
        findViewById<Group>(R.id.vip_group)?.visibility = if (v) View.VISIBLE else View.GONE
    }

    class TimeCardAdapter(private val context: MainActivity) : AbstractSelectAdapter<TimeCardInfo?, TimeCardAdapter.MyViewHolder>(), View.OnClickListener {
        override fun onClick(v: View) {
            if (hasListener()) {
                val id = Utils.getViewTagValue(v, -1)
                val cardInfo = getItem(id)
                invoke(cardInfo)
            }
        }

        class MyViewHolder(itemView: View?) : SuperViewHolder(itemView) {
            @BindView(R.id.img)
            lateinit var img: ImageView
            @BindView(R.id.name_tv)
            lateinit var name_tv: TextView
            @BindView(R.id.available_times_tv)
            lateinit var available_times_tv: TextView
            @BindView(R.id.price_tv)
            lateinit var price_tv: TextView

            init {
                ButterKnife.bind(this, itemView!!)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = View.inflate(CustomApplication.self(), R.layout.normal_once_card_info, null)
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            view.setOnClickListener(this)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val cardInfo = mData[position]!!
            val img_url = cardInfo.img
            if (Utils.isNotEmpty(img_url)) {
                Glide.with(context).load(img_url).into(holder.img)
            } else Glide.with(holder.img).load(R.drawable.nodish).into(holder.img)

            holder.name_tv.text = cardInfo.title
            if (cardInfo.available_limit == 1) holder.available_times_tv.text = String.format(Locale.CHINA, "%s次", cardInfo.available_limits) else holder.available_times_tv.text = String.format(Locale.CHINA, "%d次", cardInfo.available)
            holder.price_tv.text = String.format(Locale.CHINA, "￥%.2f", cardInfo.price)
            holder.itemView.tag = position
        }
    }

    internal class NTimeCardSaleAdapter(c: MainActivity?) : TimeCardSaleAdapterBase<NTimeCardSaleAdapter.MyViewHolder>(c) {
        class MyViewHolder(itemView: View?) : SuperViewHolder(itemView) {
            @BindView(R.id.once_card_id)
            lateinit var once_card_id: TextView
            @BindView(R.id.name_tv)
            lateinit var name_tv: TextView
            @BindView(R.id.sale_price)
            lateinit var sale_price: TextView
            @BindView(R.id.sale_num)
            lateinit var sale_num: TextView
            @BindView(R.id.sale_amt)
            lateinit var sale_amt: TextView
            init {
                ButterKnife.bind(this, itemView!!)
            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = View.inflate(mContext, R.layout.normal_time_card_sale_detail, null)
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            return MyViewHolder(view)
        }

        override fun bindView(holder: MyViewHolder, position: Int) {
            val saleInfo = getItem(position)
            saleInfo?.let {
                holder.once_card_id.text = saleInfo.once_card_id.toString()
                holder.name_tv.text = saleInfo.name
                holder.sale_price.text = String.format(Locale.CHINA, "%.2f", saleInfo.price)
                holder.sale_num.text = saleInfo.num.toString()
                holder.sale_amt.text = String.format(Locale.CHINA, "%.2f", saleInfo.amt)
            }
        }
    }
}