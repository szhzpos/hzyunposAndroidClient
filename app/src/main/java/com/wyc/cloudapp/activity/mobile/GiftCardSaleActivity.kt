package com.wyc.cloudapp.activity.mobile

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.method.ReplacementTransformationMethod
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import butterknife.ButterKnife
import com.wyc.cloudapp.R

open class GiftCardSaleActivity : AbstractMobileActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMiddleText(getString(R.string.gift_card_sale))
        ButterKnife.bind(this)

        initSearchContent()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initSearchContent():Unit {
        val _search_content = findViewById<EditText>(R.id._search_content);
        _search_content?.transformationMethod = object : ReplacementTransformationMethod() {
            override fun getOriginal(): CharArray {
                return charArrayOf('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z')
            }
            override fun getReplacement(): CharArray {
                return charArrayOf('A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
                        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z')
            }
        }
        _search_content?.setOnKeyListener(object : View.OnKeyListener{
            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
                if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.action == KeyEvent.ACTION_UP){
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
                    SelectTimeCardActivity.start(this,SelectGiftCardActivity::class.java)
                }
            }
            false
        }
    }

    private fun queryGiftCardByCode(id:String):Unit{

    }

    override fun getContentLayoutId(): Int {
        return R.layout.activity_gift_card_sale;
    }
}