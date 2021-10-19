package com.wyc.cloudapp.activity.mobile.cashierDesk

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity
import com.wyc.cloudapp.adapter.AbstractDataAdapter
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList
import com.wyc.cloudapp.data.room.entity.GiftCardSaleOrder
import com.wyc.cloudapp.decoration.LinearItemDecoration
import com.wyc.cloudapp.utils.FormatDateTimeUtils
import com.wyc.cloudapp.utils.Utils
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

class GiftCardOrderQueryActivity : AbstractMobileActivity() {
    private var mAdapter:OrderAdapter? = null
    private lateinit var mSearchContent:EditText

    @BindView(R.id._tab_layout)
    lateinit var _tab_layout: TabLayout
    @BindView(R.id.m_start_date)
    lateinit var mStartDate: TextView
    @BindView(R.id.m_end_date)
    lateinit var mEndDate: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        initTimeTv()
        initSearchContent()
        initOrderList()

        initTab();
    }

    private fun initOrderList(){
        val list = findViewById<RecyclerView>(R.id._order_list)
        list?.let {
            mAdapter = OrderAdapter(this)
            list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            list.addItemDecoration(LinearItemDecoration(getColor(R.color.white)))
            list.adapter = mAdapter
        }
    }

    private fun initTab() {
        _tab_layout.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectTab(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                selectTab(tab)
            }
        })
        _tab_layout.addTab(_tab_layout.newTab().setText(R.string.today_sz))
        _tab_layout.addTab(_tab_layout.newTab().setText(R.string.yesterday_sz))
        _tab_layout.addTab(_tab_layout.newTab().setText(R.string.other_sz))
    }

    private fun selectTab(tab: TabLayout.Tab) {
        val index = tab.position
        val rightNow = Calendar.getInstance()
        rightNow.timeZone = TimeZone.getDefault()
        var start: Long = 0
        var end: Long = 0
        val _query_time_layout = findViewById<LinearLayout>(R.id._query_time_layout)
        when (index) {
            1 -> {
                _query_time_layout.visibility = View.GONE
                rightNow.time = Date()
                rightNow.add(Calendar.DAY_OF_YEAR, -1)
                FormatDateTimeUtils.setStartTime(rightNow)
                start = rightNow.time.time
                FormatDateTimeUtils.setEndTime(rightNow)
                end = rightNow.time.time
            }
            2 -> {
                _query_time_layout.visibility = View.VISIBLE
                val sdf = SimpleDateFormat(FormatDateTimeUtils.YYYY_MM_DD_1, Locale.CHINA)
                try {
                    rightNow.time = Objects.requireNonNull(sdf.parse(mStartDate.text.toString() + " 00:00:00"))
                    start = rightNow.time.time
                    rightNow.time = Objects.requireNonNull(sdf.parse(mEndDate.text.toString() + " 23:59:59"))
                    end = rightNow.time.time
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }
            else -> {
                _query_time_layout.visibility = View.GONE
                rightNow.time = Date()
                FormatDateTimeUtils.setStartTime(rightNow)
                start = rightNow.time.time
                FormatDateTimeUtils.setEndTime(rightNow)
                end = rightNow.time.time
            }
        }
        query(start / 1000, end / 1000, mSearchContent.text.toString())
    }

    private fun query(start: Long, end: Long, order_id: String){
        mAdapter?.let {
            thread(true){
                val orders:List<GiftCardSaleOrder> = GiftCardSaleOrder.getOrderList(start, end, order_id)
                it.setDataForList(orders)
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchContent(){
        val search:EditText? = findViewById(R.id.order_search)
        search?.let {
            it.setOnTouchListener { _: View?, motionEvent: MotionEvent ->
                if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                    val dx = motionEvent.x
                    val w: Int = it.width
                    if (dx > w - it.compoundPaddingRight) {
                        _tab_layout.selectTab(_tab_layout.getTabAt(_tab_layout.selectedTabPosition))
                    }
                }
                false
            }
            mSearchContent = it
        }
    }

    override fun hookEnterKey(): Boolean {
        _tab_layout.selectTab(_tab_layout.getTabAt(_tab_layout.selectedTabPosition))
        return true
    }

    private fun initTimeTv() {
        mStartDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date()))
        mStartDate.setOnClickListener { Utils.showDatePickerDialog(this, mStartDate, Calendar.getInstance()) }
        mEndDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date()))
        mEndDate.setOnClickListener { Utils.showDatePickerDialog(this, mEndDate, Calendar.getInstance()) }
        mStartDate.addTextChangedListener(textWatcher)
        mEndDate.addTextChangedListener(textWatcher)
    }
    private val textWatcher:TextWatcher = object :TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

        }

        override fun afterTextChanged(s: Editable?) {
            _tab_layout.selectTab(_tab_layout.getTabAt(_tab_layout.selectedTabPosition));
        }

    }

    override fun getContentLayoutId(): Int {
        return R.layout.activity_gift_card_query
    }

    class OrderAdapter(context: MainActivity) : AbstractDataAdapterForList<GiftCardSaleOrder, OrderAdapter.MyViewHolder>(),View.OnClickListener {
        private val mContext = context
        class MyViewHolder(itemView: View): AbstractDataAdapter.SuperViewHolder(itemView) {
            @BindView(R.id.order_code)
            lateinit var order_code: TextView
            @BindView(R.id._order_status)
            lateinit var  _order_status: TextView
            @BindView(R.id._order_time)
            lateinit var _order_time: TextView
            @BindView(R.id._order_cas_name)
            lateinit var _order_cas_name: TextView
            @BindView(R.id._order_amt)
            lateinit var _order_amt: TextView
            @BindView(R.id.num_label)
            lateinit var num_label: TextView
            @BindView(R.id._order_detail)
            lateinit var _order_detail: TextView
            init {
                ButterKnife.bind(this, itemView)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = View.inflate(mContext, R.layout.gift_card_sale_query_adapter, null)
            view.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val giftCardSaleOrder = getItem(position)
            giftCardSaleOrder?.let {
                holder.order_code.text = it.getOnline_order_no()
                holder._order_status.text = it.getStatusName()
                holder._order_time.text = it.getFormatTime()
                holder._order_cas_name.text =it.getCasName()
                holder._order_amt.text = String.format("%.2f",it.getAmt())
                holder.num_label.text = String.format("%s%d",mContext.getString(R.string.gift_card_sale_num),it.getDetailCount())
                if (!holder._order_detail.hasOnClickListeners()){
                    holder._order_detail.setOnClickListener(this)
                }
                holder._order_detail.tag = giftCardSaleOrder
            }
        }

        override fun onClick(v: View) {
            val giftCardSaleOrder = v.tag as? GiftCardSaleOrder
            giftCardSaleOrder?.let {
                GiftCardOrderDetailActivity.start(mContext,giftCardSaleOrder)
            }
        }
    }
}