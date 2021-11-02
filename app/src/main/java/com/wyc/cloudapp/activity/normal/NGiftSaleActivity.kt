package com.wyc.cloudapp.activity.normal

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import androidx.constraintlayout.widget.Group
import butterknife.BindView
import com.wyc.cloudapp.customizationView.KeyboardView
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.AbsBindingActivity
import com.wyc.cloudapp.data.viewModel.GiftCardInfoModel
import com.wyc.cloudapp.databinding.ActivityNGiftSaleBinding

class NGiftSaleActivity : AbsBindingActivity() {
    private val giftCardViewModel = GiftCardInfoModel()
    @BindView(R.id.search_content)
    lateinit var search_content: EditText
    @BindView(R.id.gift_info_group)
    lateinit var gift_info_group:Group

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        initKeyboardView()
        giftCardViewModel.AddObserver(this){
            getBindingData<ActivityNGiftSaleBinding>()?.giftCardInfo = it[0]
            gift_info_group.visibility = View.VISIBLE
        }
    }

    override fun getBindingLayoutId(): Int {
        return R.layout.activity_n_gift_sale
    }

    override fun getMiddleText(): String {
        return getString(R.string.gift_card_sale)
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
        view.setCancelListener { v: View? -> finish() }
        view.setOkListener { v: View? ->
            giftCardViewModel.refresh(this,search_content.text.toString())
        }
    }

    companion object{
        @JvmStatic
        fun start(context: Activity){
            context.startActivity(Intent(context,NGiftSaleActivity::class.java))
        }
    }
}