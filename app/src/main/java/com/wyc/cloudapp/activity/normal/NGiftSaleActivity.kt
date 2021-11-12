package com.wyc.cloudapp.activity.normal

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.OnClick
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.AbsBindingActivity
import com.wyc.cloudapp.activity.mobile.cashierDesk.CardPayBaseActivity
import com.wyc.cloudapp.activity.mobile.cashierDesk.GiftCardPayActivity
import com.wyc.cloudapp.adapter.MobileGiftCardSaleAdapter
import com.wyc.cloudapp.adapter.TreeListBaseAdapter
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.GiftCardInfo
import com.wyc.cloudapp.customizationView.KeyboardView
import com.wyc.cloudapp.data.room.entity.GiftCardSaleDetail
import com.wyc.cloudapp.data.room.entity.GiftCardSaleOrder
import com.wyc.cloudapp.data.viewModel.GiftCardInfoModel
import com.wyc.cloudapp.databinding.ActivityNGiftSaleBinding
import com.wyc.cloudapp.decoration.LinearItemDecoration
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.dialog.vip.AbstractVipChargeDialog
import com.wyc.cloudapp.utils.Utils

class NGiftSaleActivity : AbsBindingActivity() {
    private lateinit var mSaleAdapter: MobileGiftCardSaleAdapter

    @BindView(R.id.search_content)
    lateinit var search_content: EditText

    @BindView(R.id.sale_amt_tv)
    lateinit var sale_amt_tv: TextView

    @BindView(R.id.sale_man_tv)
    lateinit var sale_man_tv: TextView

    @BindView(R.id.sale_sum_num)
    lateinit var sale_sum_num:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (CustomApplication.isPracticeMode()){
            MyDialog.toastMessage(getString(R.string.not_enter_practice))
            finish()
            return
        }
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        initKeyboardView()
        initSearchContent()
        initSaleList()
        ViewModelProvider(this).get(GiftCardInfoModel::class.java).AddObserver(this){
            getBindingData<ActivityNGiftSaleBinding>()?.giftCardInfo = it[0]
        }
    }

    override fun getBindingLayoutId(): Int {
        return R.layout.activity_n_gift_sale
    }

    override fun getMiddleText(): String {
        return getString(R.string.gift_card_sale)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CardPayBaseActivity.ONCE_CARD_REQUEST_PAY) {
                clearContent()
                getBindingData<ActivityNGiftSaleBinding>()?.giftCardInfo = null
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @OnClick(R.id._clear_btn)
    fun clear(){
        if (!mSaleAdapter.isEmpty && MyDialog.showMessageToModalDialog(this,"是否清空?") == 1){
            mSaleAdapter.clear()
        }
    }

    @OnClick(R.id.sale_man_btn)
    fun sale_man(){
        setSaleman(AbstractVipChargeDialog.showSaleInfo(this))
    }

    @OnClick(R.id.select_btn)
    fun select(){
        getBindingData<ActivityNGiftSaleBinding>()?.giftCardInfo?.let {
            if (it.isSale){
                MyDialog.toastMessage("已出售...")
            }else
                add(it)
        }
    }

    @OnClick(R.id.checkout_btn)
    fun checkout(){
        GiftCardPayActivity.start(this,disposeOrder())
    }
    private fun disposeOrder(): GiftCardSaleOrder {
        return GiftCardSaleOrder.Builder().amt(sale_amt_tv.text.toString().toDouble()).
        saleman(getSaleManId()).cas_id(cashierId).saleInfo(mSaleAdapter.list).build()
    }

    private fun initSaleList() {
        val list = findViewById<RecyclerView>(R.id.sale_gift_card_list)
        mSaleAdapter = MobileGiftCardSaleAdapter(this)
        mSaleAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                var num = 0
                var amt = 0.0
                val saleInfoList: List<GiftCardSaleDetail>? = mSaleAdapter.list
                saleInfoList?.let {
                    for (info in it) {
                        num += info.getNum()
                        amt += info.getAmt()
                    }
                }
                sale_sum_num.text = num.toString()
                sale_amt_tv.text = String.format("%.2f", amt)
            }
        })
        list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        list.addItemDecoration(LinearItemDecoration(this.getColor(R.color.gray_subtransparent), 3))
        list.adapter = mSaleAdapter
    }

    private fun initKeyboardView() {
        val view: KeyboardView = findViewById(R.id.keyboard_view)
        view.layout(R.layout.keyboard_layout)
        view.setCurrentFocusListener {
            val focus = currentFocus
            if (focus is EditText) {
                return@setCurrentFocusListener focus
            }
            null
        }
        view.setCancelListener { v: View? -> search_content.text.clear() }
        view.setOkListener { v: View? ->
            ViewModelProvider(this).get(GiftCardInfoModel::class.java).refresh(this,search_content.text.toString())
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchContent(){
        search_content.let {
            it.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN){
                    val dx:Float = event.getX()
                    val w:Int = it.width
                    if (dx > (w - it.compoundPaddingRight)) {
                        ViewModelProvider(this).get(GiftCardInfoModel::class.java).refresh(this,it.text.toString())
                    }
                }
                false
            }
        }
    }

    private fun clearContent() {
        mSaleAdapter.clear()
        setSaleman(null)
        search_content.text?.clear()
    }
    private fun add(info: GiftCardInfo) {
        val saleInfo = GiftCardSaleDetail.Builder()
                .giftCode(info.cardNo)
                .card_chip_no(info.cardChipNo)
                .name(info.shoppingName)
                .face_value(info.faceMoney)
                .price(info.price)
                .num(1).build()
        mSaleAdapter.addGiftCard(saleInfo)
    }
    private fun setSaleman(obj: JSONObject?){
        sale_man_tv.tag = Utils.getNullStringAsEmpty(obj, TreeListBaseAdapter.COL_ID)
        sale_man_tv.text = Utils.getNullStringAsEmpty(obj, TreeListBaseAdapter.COL_NAME)
    }
    private fun getSaleManId(): String {
        return Utils.getViewTagValue(sale_man_tv, "")
    }
    companion object{
        @JvmStatic
        fun start(context: Activity){
            context.startActivity(Intent(context,NGiftSaleActivity::class.java))
        }
    }
}