package com.wyc.cloudapp.activity.mobile.cashierDesk

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import butterknife.BindView
import butterknife.OnClick
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.AbsBindingActivity
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity.TITLE_KEY
import com.wyc.cloudapp.activity.mobile.cashierDesk.AbstractSelectActivity.SELECT_ITEM
import com.wyc.cloudapp.bean.GiftCardInfo
import com.wyc.cloudapp.data.viewModel.GiftCardInfoModel
import com.wyc.cloudapp.databinding.ActivityQueryGiftCardInfoBinding
import com.wyc.cloudapp.dialog.MyDialog

class QueryGiftCardInfoActivity : AbsBindingActivity() {
    @BindView(R.id.search_gift_card)
    lateinit var search_content: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initSearchContent();
        ViewModelProvider(this).get(GiftCardInfoModel::class.java).AddObserver(this){
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
        ViewModelProvider(this).get(GiftCardInfoModel::class.java).refresh(this,code)
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