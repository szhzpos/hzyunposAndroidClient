package com.wyc.cloudapp.design

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.ContextMenu
import android.view.ContextMenu.ContextMenuInfo
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.AbstractDefinedTitleActivity
import com.wyc.cloudapp.adapter.AbstractDataAdapter
import com.wyc.cloudapp.adapter.AbstractSelectAdapter
import com.wyc.cloudapp.data.room.AppDatabase
import com.wyc.cloudapp.decoration.LinearItemDecoration
import com.wyc.cloudapp.decoration.SuperItemDecoration
import com.wyc.cloudapp.dialog.MyDialog
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers


class BrowseLabelActivity : AbstractDefinedTitleActivity() {
    private var mCurLabel:LabelTemplate? = null
    private var mAdapter:LabelAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setMiddleText(getString(R.string.local_label))
        initAdapter()
    }

    private fun initAdapter() {
        val recyclerView: RecyclerView = findViewById(R.id.label_list)
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(recyclerView, resources.getDimension(R.dimen.size_48), LinearItemDecoration(getColor(R.color.gray_subtransparent)))
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        mAdapter = LabelAdapter(this)
        Observable.create<List<LabelTemplate>>{
            it.onNext(AppDatabase.getInstance().LabelTemplateDao().getAll())
        }.observeOn(AndroidSchedulers.mainThread()).subscribeOn(Schedulers.io()).subscribe ({
            mAdapter!!.setDataForList(it)
        },{err-> MyDialog.toastMessage(err.message)})

        mAdapter!!.setSelectListener {
            val intent = Intent()
            intent.putExtra(LABEL_KEY, it)
            setResult(RESULT_OK, intent)
            finish()
        }
        recyclerView.adapter = mAdapter
    }

    override fun getContentLayoutId(): Int {
        return R.layout.activity_browse_label
    }

    override fun onCreateContextMenu(menu: ContextMenu, v: View?, menuInfo: ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.add(1, 1, 1, getString(R.string.delete))
        menu.add(1, 2, 1, getString(R.string.modify_sz))
        mCurLabel = v?.tag as? LabelTemplate
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            1 -> {
                mCurLabel?.let {
                    if (MyDialog.showMessageToModalDialog(this,getString(R.string.delete_label_hint,it.templateName)) == 1){
                        AppDatabase.getInstance().LabelTemplateDao().deleteTemplateById(it)
                        mAdapter?.deleteLabel(it)
                    }
                }
            }
            2 -> {
                mCurLabel?.let {
                    LabelDesignActivity.start(this,it)
                    finish()
                }
            }
        }
        return super.onContextItemSelected(item)
    }

    companion object{
        const val LABEL_KEY = "label"
        @JvmStatic
        fun start(context:Context){
            context.startActivity(Intent(context,BrowseLabelActivity::class.java))
        }
    }

    private class LabelAdapter(private val context: BrowseLabelActivity): AbstractSelectAdapter<LabelTemplate, LabelAdapter.MyViewHolder>(),View.OnClickListener {
        class MyViewHolder(itemView: View): AbstractDataAdapter.SuperViewHolder(itemView) {
            val labelView:LabelView = findViewById(R.id.labelView)
            val name: TextView = itemView.findViewById(R.id.name)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val view = View.inflate(parent.context,R.layout.browse_label_adapter,null)
            view.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT)
            view.setOnClickListener(this)
            context.registerForContextMenu(view)
            return MyViewHolder(view)
        }
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val label = getItem(position)
            holder.name.text = label.templateName
            holder.labelView.postDelayed({
                holder.labelView.setLabelTemplate(label)
            },100)
            holder.labelView.previewModel()

            holder.itemView.tag = label
        }

        fun deleteLabel(labelTemplate: LabelTemplate){
            mData?.apply {
                val index = indexOf(labelTemplate)
                removeAt(index)
                notifyItemRemoved(index)
            }
        }

        override fun onClick(v: View) {
            val obj = v.tag
            if (obj is LabelTemplate) {
                invoke(obj)
            }
        }
    }
}