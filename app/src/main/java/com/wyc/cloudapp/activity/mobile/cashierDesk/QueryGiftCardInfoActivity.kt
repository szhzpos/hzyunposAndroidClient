package com.wyc.cloudapp.activity.mobile.cashierDesk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.EditText
import butterknife.BindView
import butterknife.OnClick
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.AbsBindingActivity
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity.TITLE_KEY
import com.wyc.cloudapp.activity.mobile.cashierDesk.AbstractSelectActivity.SELECT_ITEM
import com.wyc.cloudapp.bean.GiftCardInfo
import com.wyc.cloudapp.constants.InterfaceURL
import com.wyc.cloudapp.databinding.ActivityQueryGiftCardInfoBinding
import com.wyc.cloudapp.dialog.CustomProgressDialog
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.utils.Utils
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import com.wyc.cloudapp.utils.http.callback.ArrayCallback

class QueryGiftCardInfoActivity : AbsBindingActivity() {
    @BindView(R.id.search_gift_card)
    lateinit var search_content: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initSearchContent();
    }
    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchContent(){
        search_content.let {
            it.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_DOWN){
                    val dx:Float = event.getX()
                    val w:Int = it.width
                    if (dx > (w - it.compoundPaddingRight)) {
                        queryGiftCardByCode(it.text.toString())
                    }else if(dx < it.compoundPaddingLeft){
                        finish()
                    }
                }
                false
            }
        }
    }

    override fun hookEnterKey(): Boolean {
        search_content.let {
            queryGiftCardByCode(it.text.toString())
            return true;
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
                                getBindingData<ActivityQueryGiftCardInfoBinding>()?.giftCardInfo = d[0]
                            else MyDialog.toastMessage(getString(R.string.not_exist_hint_sz, "卡号：$code"))
                        }else MyDialog.toastMessage(hint)
                        progressDialog.dismiss()
                    }
                })
    }

    @OnClick(R.id.select_btn)
    fun select(){
        setResult()
    }
    private fun setResult() {
        val mGiftCardInfo:GiftCardInfo? = getBindingData<ActivityQueryGiftCardInfoBinding>()?.giftCardInfo;
        if (mGiftCardInfo != null){
            if (mGiftCardInfo.isSale){
                MyDialog.toastMessage(getString(R.string.sold))
                return
            }
            setResult(RESULT_OK, Intent().putExtra(AbstractSelectActivity.ITEM_KEY, mGiftCardInfo))
            finish()
        }else MyDialog.toastMessage(getString(R.string.hint_unfounded))
    }

    override fun getBindingLayoutId(): Int {
        return R.layout.activity_query_gift_card_info
    }
    companion object{
        @JvmStatic
        fun start(context: Activity){
            context.startActivityForResult(Intent(context, QueryGiftCardInfoActivity::class.java).
            putExtra(TITLE_KEY,context.getString(R.string.gift_card_info)), SELECT_ITEM)
        }
    }
}