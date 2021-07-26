package com.wyc.cloudapp.activity.mobile.cashierDesk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity
import com.wyc.cloudapp.activity.mobile.cashierDesk.AbstractSelectActivity.SELECT_ITEM
import com.wyc.cloudapp.bean.GiftCardInfo
import com.wyc.cloudapp.constants.InterfaceURL
import com.wyc.cloudapp.dialog.CustomProgressDialog
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.utils.FormatDateTimeUtils
import com.wyc.cloudapp.utils.Utils
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import com.wyc.cloudapp.utils.http.callback.ArrayCallback

class QueryGiftCardInfoActivity : AbstractMobileActivity() {
    private  var mGiftCardInfo:GiftCardInfo? = null

    @BindView(R.id.gift_card_name)
    lateinit var gift_card_name:TextView
    @BindView(R.id.gift_card_code)
    lateinit var gift_card_code:TextView
    @BindView(R.id.gift_card_status)
    lateinit var gift_card_status:TextView
    @BindView(R.id.gift_card_face_value)
    lateinit var gift_card_face_value:TextView
    @BindView(R.id.gift_card_price)
    lateinit var gift_card_price:TextView
    @BindView(R.id.gift_card_balance)
    lateinit var gift_card_balance:TextView
    @BindView(R.id.use_validity)
    lateinit var use_validity:TextView
    @BindView(R.id.made_card_time)
    lateinit var made_card_time:TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMiddleText(getString(R.string.gift_card_info))
        ButterKnife.bind(this)
        initSearchContent();
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchContent(){
        val _search_content = findViewById<EditText>(R.id.search_gift_card);
        _search_content?.setOnKeyListener(object : View.OnKeyListener {
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.action == KeyEvent.ACTION_UP) {
                    queryGiftCardByCode(_search_content.text.toString())
                    return true;
                }
                return false;
            }
        })
        _search_content?.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN){
                val dx:Float = event.getX()
                val w:Int = _search_content.width
                if (dx > (w - _search_content.compoundPaddingRight)) {
                    queryGiftCardByCode(_search_content.text.toString())
                }else if(dx < _search_content.compoundPaddingLeft){
                    finish()
                }
            }
            false
        }
    }

    private fun queryGiftCardByCode(code: String) {
        if (!Utils.isNotEmpty(code)){
            MyDialog.toastMessage(getString(R.string.not_empty_hint_sz, getString(R.string.input_gift_card_hints)))
            return
        }
        val obj = JSONObject()
        obj["appid"] = appId
        obj["pos_num"] = posNum
        obj["stores_id"] = storeId
        obj["card_no"] = code

        val progressDialog : CustomProgressDialog = CustomProgressDialog.showProgress(this, getString(R.string.hints_query_data_sz));
        HttpUtils.sendAsyncPost(url + InterfaceURL.GIFT_CARD_INFO, HttpRequest.generate_request_parm(obj, appSecret))
                .enqueue(object : ArrayCallback<GiftCardInfo>(GiftCardInfo::class.java) {
                    override fun onError(msg: String?) {
                        progressDialog.dismiss()
                        MyDialog.toastMessage(msg)
                    }
                    override fun onSuccessForResult(d: List<GiftCardInfo>?, hint: String?) {
                        if (d != null){
                            if (d.isNotEmpty())
                                showGiftCard(d[0])
                            else MyDialog.toastMessage(getString(R.string.not_exist_hint_sz, "卡号：$code"))
                        }else MyDialog.toastMessage(hint)
                        progressDialog.dismiss()
                    }
                })
    }

    private fun showGiftCard(giftCardInfo: GiftCardInfo?){
        giftCardInfo?.let {
            mGiftCardInfo = it
            gift_card_name.text = it.shoppingName
            gift_card_code.text = it.cardNo
            gift_card_status.text = if (it.isSale) getString(R.string.sold) else getString(R.string.unsold)
            gift_card_face_value.text = String.format("%.2f元", it.faceMoney)
            gift_card_price.text = String.format("%.2f元", it.price)
            gift_card_balance.text = String.format("%.2f元", it.balance)
            use_validity.text = String.format("%s至%s", FormatDateTimeUtils.formatDate(it.startTime * 1000), FormatDateTimeUtils.formatDate(it.endTime * 1000))
            made_card_time.text = String.format("%s", FormatDateTimeUtils.formatTimeWithTimestamp(it.addtime * 1000))
        }
    }

    @OnClick(R.id.select_btn)
    fun select(){
        setResult()
    }
    private fun setResult() {
        if (mGiftCardInfo != null){
            if (mGiftCardInfo!!.isSale){
                MyDialog.toastMessage(getString(R.string.sold))
                return
            }
            setResult(RESULT_OK, Intent().putExtra(AbstractSelectActivity.ITEM_KEY, mGiftCardInfo))
            finish()
        }else MyDialog.toastMessage(getString(R.string.hint_unfounded))
    }
    override fun getContentLayoutId(): Int {
        return R.layout.activity_query_gift_card_info
    }
    companion object{
        @JvmStatic
        fun start(context: Activity){
            context.startActivityForResult(Intent(context, QueryGiftCardInfoActivity::class.java), SELECT_ITEM)
        }
    }
}