package com.wyc.cloudapp.activity.mobile.cashierDesk

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.ReplacementTransformationMethod
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.customizationView.BasketView
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity
import com.wyc.cloudapp.adapter.MobileGiftCardSaleAdapter
import com.wyc.cloudapp.adapter.TreeListBaseAdapter
import com.wyc.cloudapp.bean.GiftCardInfo
import com.wyc.cloudapp.constants.InterfaceURL
import com.wyc.cloudapp.data.room.entity.GiftCardSaleDetail
import com.wyc.cloudapp.data.room.entity.GiftCardSaleOrder
import com.wyc.cloudapp.decoration.LinearItemDecoration
import com.wyc.cloudapp.dialog.CustomProgressDialog
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.dialog.vip.AbstractVipChargeDialog
import com.wyc.cloudapp.utils.Utils
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import com.wyc.cloudapp.utils.http.callback.ArrayCallback

open class GiftCardSaleActivity : AbstractMobileActivity() {
    private lateinit var mSaleAdapter: MobileGiftCardSaleAdapter
    @BindView(R.id._search_content)
    lateinit var search_content: EditText

    @BindView(R.id.basketView)
    lateinit var mBasketView: BasketView

    @BindView(R.id.sale_amt_tv)
    lateinit var sale_amt_tv: TextView

    @BindView(R.id.sale_man_tv)
    lateinit var sale_man_tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)

        initSearchContent()
        initSaleBtn()
        initSaleList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SelectTimeCardActivity.SELECT_ITEM) {
                if (null != data) {
                    add(AbstractSelectActivity.getItem(data))
                }
            } else if (requestCode == CardPayBaseActivity.ONCE_CARD_REQUEST_PAY) {
                clearContent()
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @OnClick(R.id._checkout_btn)
    fun checkout(){
        GiftCardPayActivity.start(this,disposeOrder())
    }

    private fun disposeOrder(): GiftCardSaleOrder {
        return GiftCardSaleOrder.Builder().amt(sale_amt_tv.text.toString().toDouble()).
        saleman(getSaleManId()).cas_id(cashierId).saleInfo(mSaleAdapter.list).build()
    }

    @OnClick(R.id._other_fun_btn)
    open fun other_func() {
        val _clear_btn = findViewById<Button>(R.id._clear_btn)
        if (_clear_btn.visibility == View.GONE) {
            _clear_btn.visibility = View.VISIBLE
            if (!_clear_btn.hasOnClickListeners()) _clear_btn.setOnClickListener {
                if (!mSaleAdapter.isEmpty && MyDialog.showMessageToModalDialog(this,"是否清空?") == 1){
                    mSaleAdapter.clear()
                }
            }
        } else _clear_btn.visibility = View.GONE
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
                mBasketView.update(num.toDouble())
                sale_amt_tv.text = String.format("%.2f", amt)
            }
        })
        list.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        list.addItemDecoration(LinearItemDecoration(this.getColor(R.color.gray_subtransparent), 3))
        list.adapter = mSaleAdapter
    }

    private fun initSaleBtn(){
        val  saleManBtn  = findViewById<Button>(R.id._sale_man_btn);
        saleManBtn.setOnClickListener { setSaleman(AbstractVipChargeDialog.showSaleInfo(this@GiftCardSaleActivity)) }
    }
    private fun setSaleman(obj: JSONObject?){
        sale_man_tv.tag = Utils.getNullStringAsEmpty(obj, TreeListBaseAdapter.COL_ID)
        sale_man_tv.text = Utils.getNullStringAsEmpty(obj, TreeListBaseAdapter.COL_NAME)
    }
    private fun getSaleManId(): String {
        return Utils.getViewTagValue(sale_man_tv, "")
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchContent() {
        search_content.let {
            it.transformationMethod = object : ReplacementTransformationMethod() {
                override fun getOriginal(): CharArray {
                    return charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                            'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')
                }
                override fun getReplacement(): CharArray {
                    return charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
                            'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
                }
            }
            it.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN){
                    val dx:Float = event.getX()
                    val w:Int = it.width
                    if (dx > (w - it.compoundPaddingRight)) {
                        queryGiftCardByCode(it.text.toString())
                        return@setOnTouchListener true
                    }else if(dx < it.compoundPaddingLeft){
                        QueryGiftCardInfoActivity.start(this)
                    }
                }
                false
            }
        }
    }

    override fun hookEnterKey(): Boolean {
        search_content.let {
            queryGiftCardByCode(it.text.toString())
            return true
        }
    }

    private fun queryGiftCardByCode(id:String) {
        if (!Utils.isNotEmpty(id)){
            MyDialog.ToastMessage(getString(R.string.not_empty_hint_sz, getString(R.string.input_gift_card_hints)),window)
            return
        }
        val obj = JSONObject()
        obj["appid"] = appId
        obj["pos_num"] = posNum
        obj["stores_id"] = storeId
        obj["card_no"] = id

        val progressDialog : CustomProgressDialog = CustomProgressDialog.showProgress(this, getString(R.string.hints_query_data_sz));
        HttpUtils.sendAsyncPost(url + InterfaceURL.GIFT_CARD_INFO, HttpRequest.generate_request_parm(obj, appSecret))
                .enqueue(object : ArrayCallback<GiftCardInfo>(GiftCardInfo::class.java){
                    override fun onError(msg: String?) {
                        progressDialog.dismiss()
                        MyDialog.toastMessage(msg)
                    }

                    override fun onSuccessForResult(d: List<GiftCardInfo>?, hint: String?) {
                        d?.let {
                            if (it.size == 1){
                                val giftCardInfo = it[0]
                                if (!giftCardInfo.isSale){
                                    add(giftCardInfo)
                                }else MyDialog.toastMessage("已出售...")
                            }
                        }
                        MyDialog.toastMessage(hint)
                        progressDialog.dismiss()
                    }
                })
    }

    private fun clearContent() {
        mSaleAdapter.clear()
        setSaleman(null)
        search_content?.text?.clear()
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
    override fun getContentLayoutId(): Int {
        return R.layout.activity_gift_card_sale;
    }
}