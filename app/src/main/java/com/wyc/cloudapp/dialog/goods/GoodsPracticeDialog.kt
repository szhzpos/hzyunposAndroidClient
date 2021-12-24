package com.wyc.cloudapp.dialog.goods

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.ButterKnife
import butterknife.OnClick
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.data.room.entity.PracticeAssociated
import com.wyc.cloudapp.decoration.GridItemDecoration
import com.wyc.cloudapp.decoration.SuperItemDecoration
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.Utils

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.dialog.goods
 * @ClassName:      GoodsPracticeDialog
 * @Description:    做法展示对话框
 * @Author:         wyc
 * @CreateDate:     2021-12-01 11:38
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-12-01 11:38
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class GoodsPracticeDialog(context:MainActivity,private var mList : List<PracticeAssociated>?,selectedList : List<PracticeAssociated>?): AbstractDialogMainActivity(context,context.getString(R.string.select_practice)) {
    private val mAdapter:Adapter = Adapter()
    private var mListener: OnResultListener? = null
    private var mAmtTv:TextView? = null
    private var mGoodsPracticeAmt = 0.0
    init {
        Logger.d("mList:%s,selectedList:%s", mList.orEmpty().toTypedArray().contentToString(),selectedList.orEmpty().toTypedArray().contentToString())
        selectedList?.forEach { s ->
            mList?.forEach{ l ->
                if (s == l){
                    l.sel = true
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ButterKnife.bind(this)
        initList()
        initAmt()
    }
    private fun initAmt(){
        mGoodsPracticeAmt = mList?.filter { it.sel }?.sumOf { it.kw_price }?:0.0
        mAmtTv = findViewById(R.id.goods_practice_amt_tv)
        mAmtTv?.text = mContext.getString(R.string.plus_price_hint,String.format("%.2f",mGoodsPracticeAmt))
    }

    override fun getWidthRatio(): Double {
        if (mContext.lessThan7Inches()){
            return 0.98
        }
        return CustomApplication.getDimension(R.dimen.size_480).toDouble()
    }

    private fun initList(){
        val view = findViewById<RecyclerView>(R.id.practice_list)
        mAdapter.setDataForList(mList)
        view.layoutManager = GridLayoutManager(mContext,if (mContext.lessThan7Inches()) 3 else 4)
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(view,mContext.resources.getDimension(R.dimen.goods_practice_item_height),
            GridItemDecoration()
        )
        view.adapter = mAdapter
        mAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
                val obj = mAdapter.list[positionStart]
                if (obj.sel){
                    mGoodsPracticeAmt += obj.kw_price
                }else mGoodsPracticeAmt -= obj.kw_price
                mAmtTv?.text = mContext.getString(R.string.plus_price_hint,String.format("%.2f",mGoodsPracticeAmt))
            }
        })
    }

    private fun getContent():MutableList<PracticeAssociated>{
        mAdapter.list?.apply {
           return filter { it.sel }.toMutableList()
        }
        return mutableListOf()
    }

    private class Adapter :AbstractDataAdapterForList<PracticeAssociated, Adapter.MyViewHolder>(),View.OnClickListener {
        class MyViewHolder(itemView: View?) : SuperViewHolder(itemView) {
            var name: TextView = findViewById(R.id.name)
            var price: TextView = findViewById(R.id.price)
            val sign:ImageView = findViewById(R.id.sign)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = View.inflate(parent.context, R.layout.goods_practice_item, null)
            itemView.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,parent.context.resources.getDimension(R.dimen.goods_practice_item_height).toInt())
            itemView.setOnClickListener(this)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            if (!isEmpty){
                val obj = getItem(position)
                obj?.apply {
                    holder.name.tag = kw_id
                    holder.name.text = kw_name
                    holder.price.text = String.format("+%.2f",kw_price)

                    if (sel){
                        holder.sign.visibility = View.VISIBLE
                    }else holder.sign.visibility = View.GONE

                    holder.itemView.tag = position
                }
            }
        }

        override fun onClick(v: View?) {
            val index = Utils.getViewTagValue(v,-1)
            getItem(index)?.apply {
                sel = !sel
                notifyItemChanged(index)
            }
        }
    }

    override fun getContentLayoutId(): Int {
        return R.layout.goods_practice_dialog
    }

    @OnClick(R.id.t_cancel)
    fun cancelBtn(){
        dismiss()
    }

    @OnClick(R.id.ok_btn)
    fun ok(){
        mListener?.apply {
            onResult(getContent())
        }
        dismiss()
    }
    interface OnResultListener{
        fun onResult(data: MutableList<PracticeAssociated>)
    }
    fun setResultListener(listener: OnResultListener){
        mListener = listener
    }
}