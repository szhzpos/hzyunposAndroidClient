package com.wyc.cloudapp.activity.mobile.cashierDesk

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.MainActivity
import com.wyc.cloudapp.adapter.AbstractSelectAdapter
import com.wyc.cloudapp.bean.GiftCardInfo
import com.wyc.cloudapp.constants.InterfaceURL
import com.wyc.cloudapp.dialog.CustomProgressDialog
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.utils.Utils
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import com.wyc.cloudapp.utils.http.callback.ArrayCallback

class SelectGiftCardActivity : AbstractSelectActivity<GiftCardInfo, SelectGiftCardActivity.GiftCardAdapter>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMiddleText(getString(R.string.select_gift_card))
    }
    override fun getAdapter(): GiftCardAdapter {
        return GiftCardAdapter(this)
    }

    override fun loadData(c: String) {
        val obj = JSONObject();
        obj["appid"] = appId;
        obj["pos_num"] = posNum;
        obj["stores_id"] = storeId;
        if (Utils.isNotEmpty(c)){
            obj["card_no"] = c;
        }
        val progressDialog : CustomProgressDialog = CustomProgressDialog.showProgress(this, getString(R.string.hints_query_data_sz));
        HttpUtils.sendAsyncPost(url + InterfaceURL.GIFT_CARD_INFO, HttpRequest.generate_request_parm(obj, appSecret))
                .enqueue(object : ArrayCallback<GiftCardInfo>(GiftCardInfo::class.java){
                    override fun onError(msg: String?) {
                        progressDialog.dismiss()
                        MyDialog.toastMessage(msg)
                    }

                    override fun onSuccessForResult(d: List<GiftCardInfo>?, hint: String?) {
                        setData(d)
                        progressDialog.dismiss()
                    }
                })
    }



    class GiftCardAdapter (context: MainActivity): AbstractSelectAdapter<GiftCardInfo, GiftCardAdapter.MyViewHolder>(), View.OnClickListener {
        private var mContext: MainActivity = context;

        class MyViewHolder(itemView: View) : SuperViewHolder(itemView){
            @BindView(R.id.id_tv)
            lateinit var id_tv : TextView;
            @BindView(R.id.name_tv)
            lateinit var name_tv : TextView;
            @BindView(R.id.card_code_tv)
            lateinit var card_code_tv : TextView;
            @BindView(R.id.face_value_tv)
            lateinit var face_value_tv : TextView;
            @BindView(R.id.price_tv)
            lateinit var price_tv : TextView;
            init {
                ButterKnife.bind(this, itemView)
            }
        }

        override fun onClick(v: View) {
            val obj = v.tag
            if (obj is Int){
                val giftCardInfo : GiftCardInfo = getItem(obj);
                if (giftCardInfo.isSale){
                    MyDialog.toastMessage("已出售...")
                    return
                }
                invoke(giftCardInfo)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view : View = View.inflate(mContext, R.layout.mobile_gift_card_info_adapter, null);
            view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mContext.resources.getDimension(R.dimen.once_card_item_height).toInt())
            view.setOnClickListener(this)
            return MyViewHolder(view)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val giftCardInfo : GiftCardInfo? = getItem(position);
            holder.id_tv.setText(String.format("%d、",position + 1))
            holder.name_tv.text = giftCardInfo?.shoppingName
            holder.card_code_tv.text =  String.format("%s%s",mContext.getString(R.string.gift_card_code),giftCardInfo?.cardNo)
            holder.face_value_tv.text = String.format("%s%.2f",mContext.getString(R.string.gift_card_face_value),giftCardInfo?.faceMoney)
            holder.price_tv.text = String.format("%s%.2f",mContext.getString(R.string.gift_card_sale_price),giftCardInfo?.price)
            holder.itemView.tag = position
        }
    }
}