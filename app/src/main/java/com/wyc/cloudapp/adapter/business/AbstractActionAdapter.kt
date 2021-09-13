package com.wyc.cloudapp.adapter.business

import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wyc.cloudapp.R
import com.wyc.cloudapp.adapter.AbstractDataAdapter
import com.wyc.cloudapp.application.CustomApplication

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
            val flag = data.plus
            if (!flag){
                drawable = CustomApplication.self().getDrawable(R.drawable.minus)
            }
            holder.action.setImageDrawable(drawable)
            holder.action.tag = data
            holder.sequence.text = (position + 1).toString()

            bindHolder(holder, it,flag)
        }
    }

    override fun onClick(v: View) {
        val data = v.tag as Action
        if (data.plus){
            val index = mData.size - 1
            getItem(index)?.let {
                it.plus = false
            }
            mData.add(getDefaultData())
        }else{
            mData.remove(data)
        }
        notifyDataSetChanged()
    }

    fun setDataForList(data: MutableList<D>?) {
        data?.let {
            it.add(getDefaultData())
            mData = it
            if (Looper.myLooper() != Looper.getMainLooper()) {
                CustomApplication.runInMainThread { notifyDataSetChanged() }
            } else notifyDataSetChanged()
        }
    }

    abstract fun getContentId():Int
    abstract fun getDefaultData():D
    abstract fun bindHolder(holder: T, data: D,flag:Boolean)
    abstract fun getViewHolder(itemView: View):T

    protected fun getItem(index: Int): D? {
        return if (index in 0 until itemCount && !isEmpty) {
            mData[index]
        } else null
    }

    final override fun getItemCount(): Int {
        return if (mData == null) 0 else mData.size
    }

    interface Action{
        var plus:Boolean
    }
}