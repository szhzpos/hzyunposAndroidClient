package com.wyc.cloudapp.activity.mobile.cashierDesk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
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
import com.wyc.cloudapp.data.viewModel.GiftCardInfoModel
import com.wyc.cloudapp.databinding.ActivityNGiftSaleBinding
import com.wyc.cloudapp.databinding.ActivityQueryGiftCardInfoBinding
import com.wyc.cloudapp.dialog.CustomProgressDialog
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.utils.Utils
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import com.wyc.cloudapp.utils.http.callback.ArrayCallback

class QueryGiftCardInfoActivity : AbsBindingActivity() {
    private val giftCardViewModel = GiftCardInfoModel()
    @BindView(R.id.search_gift_card)
    lateinit var search_content: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSearchContent();
        giftCardViewModel.AddObserver(this){
            getBindingData<ActivityQueryGiftCardInfoBinding>()?.giftCardInfo = it[0]
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
        giftCardViewModel.refresh(this,code)
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