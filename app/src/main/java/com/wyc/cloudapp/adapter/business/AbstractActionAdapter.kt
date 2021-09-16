package com.wyc.cloudapp.adapter.business

import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import com.wyc.cloudapp.R
import com.wyc.cloudapp.adapter.AbstractDataAdapter
import com.wyc.cloudapp.application.CustomApplication
import kotlin.collections.ArrayList

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.adapter.business
 * @ClassName:      AbstractActionAdapter
 * @Description:    新增删除数据适配器
 * @Author:         wyc
 * @CreateDate:     2021-09-13 10:21
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-09-13 10:21
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
abstract class AbstractActionAdapter<D : AbstractActionAdapter.Action, T : AbstractActionAdapter.MyViewHolder>: AbstractDataAdapter<MutableList<D>, T>(),View.OnClickListener {
    init {
        mData = ArrayList()
    }
    open class MyViewHolder(itemView: View):AbstractDataAdapter.SuperViewHolder(itemView){
        val action:ImageView = findViewById(R.id.action)
        val sequence:TextView = findViewById(R.id.sequence)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): T {
        val itemView = View.inflate(CustomApplication.self(), R.layout.action_adapter, null)
        itemView.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val viewStub = itemView.findViewById<ViewStub>(R.id.viewStub)
        viewStub.layoutResource = getContentId()
        viewStub.inflate()
        return getViewHolder(itemView)
    }

    final override fun onBindViewHolder(holder: T, position: Int) {
        val data = getItem(position)
        data?.let {
            holder.action.setOnClickListener(this)
            var drawable = CustomApplication.self().getDrawable(R.drawable.plus)
            if (!data.plus){
                drawable = CustomApplication.self().getDrawable(R.drawable.minus)
            }
            holder.action.setImageDrawable(drawable)
            holder.action.tag = position
            holder.sequence.text = (position + 1).toString()

            bindHolder(holder, it)
        }
    }

    override fun onClick(v: View) {
        val pos = v.tag as? Int
        pos?.let {
            mData[pos].let {
                if (it.plus){
                    getNewData()?.let { d -> mData.add( mData.size - 1, d) }
                }else{
                    deleteItem(mData.removeAt(pos))
                }
                notifyDataSetChanged()
            }
        }
    }

    fun setDataForList(data: MutableList<D>?) {
        data?.let {
            getNewData()?.let { d -> it.add(d) }
            mData = it
            if (Looper.myLooper() != Looper.getMainLooper()) {
                CustomApplication.runInMainThread { notifyDataSetChanged() }
            } else notifyDataSetChanged()
        }
    }

    abstract fun getContentId():Int
    abstract fun getNewData():D?
    abstract fun bindHolder(holder: T, data: D)
    abstract fun getViewHolder(itemView: View):T
    abstract fun deleteItem(data: Action)

    open fun isValid():Boolean{
        return true
    }

    protected fun getItem(index: Int): D? {
        return if (index in 0 until itemCount && !isEmpty) {
            mData[index]
        } else null
    }

    final override fun getItemCount(): Int {
        return if (mData == null) 0 else mData.size
    }

    @CallSuper
    open fun getValidData(): MutableList<D> {
        return if (mData.size > 0) {
            val data = mData.toMutableList()
            data.removeAt(data.size - 1)
            data
        }else mData
    }

    interface Action{
        var plus:Boolean
    }
}