package com.wyc.cloudapp.activity.mobile.report

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity
import com.wyc.cloudapp.activity.normal.NVipManageActivity
import com.wyc.cloudapp.adapter.AbstractDataAdapterForList
import com.wyc.cloudapp.bean.GoodsStockInfo
import com.wyc.cloudapp.customizationView.IndicatorRecyclerView
import com.wyc.cloudapp.data.viewModel.GoodsStockViewModel
import com.wyc.cloudapp.databinding.NormalGoodsStockLayoutBinding
import com.wyc.cloudapp.utils.Utils

class GoodsStockActivity : MainActivity() {
    private var mAdapter:StockQueryAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAdapter = StockQueryAdapter(this)

        setContent { LayoutContent() }
    }

    @Composable
    fun LayoutContent(){
        Column(Modifier.background(colorResource(R.color.white))){
            Box(
                Modifier
                    .height(dimensionResource(R.dimen.height_45))
                    .fillMaxWidth()
                    .background(colorResource(R.color.activity_title_color))) {
                Row(
                    Modifier
                        .clickable { finish() }
                        .wrapContentWidth()
                        .fillMaxHeight()
                        .align(Alignment.CenterStart),verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.back_ico), contentDescription = "",
                        Modifier
                            .padding(dimensionResource(R.dimen.margin_2))
                        ,tint = Color.White )
                    Text(text = stringResource(R.string.back),color = colorResource(R.color.white),fontSize = dimensionResource(R.dimen.font_size_16).value.sp)
                }
                Text(text = stringResource(R.string.commodity_stocks_sz),modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = dimensionResource(R.dimen.size_3)),color = colorResource(R.color.white),fontSize = dimensionResource(R.dimen.font_size_20).value.sp,textAlign = TextAlign.Center)
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.Top),Arrangement.Start) {
                var expanded by remember { mutableStateOf(false) }

                val barcode = stringResource(R.string.barcode_sz)
                val name = stringResource(R.string.g_name_sz)
                val code = stringResource(R.string.only_code)
                val menu = remember {listOf(barcode,name,code)}
                val index = remember{ mutableStateOf(0)}
                val menuText = remember { mutableStateOf(menu[0]) }
                OutlinedTextField(value = menuText.value,{menuText.value = it},
                    Modifier
                        .width(dimensionResource(R.dimen.size_188))
                        .padding(dimensionResource(R.dimen.size_5)),enabled = false,readOnly = true,
                    trailingIcon = {
                        Icon(painter = painterResource(R.drawable.content), contentDescription = "",
                            Modifier
                                .clickable {
                                    expanded = !expanded
                                }
                                .width(dimensionResource(R.dimen.size_25))
                                .height(dimensionResource(R.dimen.size_25)),tint = colorResource(R.color.lightBlue))
                    },
                    textStyle = TextStyle(fontSize = dimensionResource(R.dimen.font_size_16).value.sp),singleLine = true,colors = NVipManageActivity.textFieldColors()
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    menu.forEachIndexed { i, s ->
                        DropdownMenuItem(
                            onClick = {
                                menuText.value = s
                                index.value = i
                                expanded = false
                            }
                        ) {
                            Text(s, modifier = Modifier.padding(dimensionResource(R.dimen.size_8)))
                        }
                    }
                }
                val textFieldValue = remember { mutableStateOf(TextFieldValue("")) }
                NVipManageActivity.SearchTextField(
                    textFieldValue,placeholder = {Text(getString(R.string.input_hint,menuText.value),fontSize = dimensionResource(R.dimen.font_size_16).value.sp)},
                    keyboardOptions = KeyboardOptions( keyboardType = KeyboardType.Text )
                )
                Button(onClick = {
                    val cond = GoodsStockViewModel.Condition(textFieldValue.value.text)
                    if (Utils.isNotEmpty(cond.content)){
                        when(index.value){
                            0 ->{
                                cond.type = GoodsStockViewModel.QueryType.BARCODE
                            }
                            1 ->{
                                cond.type = GoodsStockViewModel.QueryType.NAME
                            }else ->{
                                cond.type = GoodsStockViewModel.QueryType.ONLYCODE
                            }
                        }
                    }
                    mAdapter?.query(cond)
                },
                    Modifier
                        .padding(dimensionResource(R.dimen.size_5))
                        .align(Alignment.CenterVertically),colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(
                        R.color.lightBlue
                    ),contentColor = colorResource(
                        R.color.white
                    ))) {
                    Text(stringResource(R.string.query_sz), modifier = Modifier.padding(dimensionResource(R.dimen.size_8)),fontSize = dimensionResource(R.dimen.font_size_18).value.sp)
                }
            }
            Column(
                Modifier
                    .wrapContentHeight()
                    .weight(1f)){
                AndroidViewBinding(NormalGoodsStockLayoutBinding::inflate){
                    findViewById<IndicatorRecyclerView>(R.id.stock_list)?.apply {
                        layoutManager = LinearLayoutManager(this@GoodsStockActivity, LinearLayoutManager.VERTICAL,false)
                        if (itemDecorationCount == 0)
                            addItemDecoration(DividerItemDecoration(this@GoodsStockActivity, DividerItemDecoration.VERTICAL))
                        adapter = mAdapter
                    }
                }
            }
        }
    }

    class StockQueryAdapter(private val mContext:GoodsStockActivity):AbstractDataAdapterForList<GoodsStockInfo, StockQueryAdapter.MyViewHolder>(),IndicatorRecyclerView.OnLoad {

        private var mLoadMore = true
        private var mLoadMode: IndicatorRecyclerView.OnLoad.LOADMODE = IndicatorRecyclerView.OnLoad.LOADMODE.FRONT
        private var mPageIndex = 0
        private var mCondition:GoodsStockViewModel.Condition? = null

        init {
            init()
        }

        class MyViewHolder(itemView: View) : SuperViewHolder(itemView) {
            @BindView(R.id.rowId)
            lateinit var row_tv: TextView
            @BindView(R.id.only_code)
            lateinit var only_code_tv: TextView
            @BindView(R.id.barcode)
            lateinit var barcode_tv: TextView
            @BindView(R.id.name)
            lateinit var goods_name_tv: TextView
            @BindView(R.id.stock)
            lateinit var num_tv: TextView
            @BindView(R.id.unit)
            lateinit var unit_tv: TextView
            @BindView(R.id.spec)
            lateinit var spec_tv: TextView
            init {
                ButterKnife.bind(this, itemView)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = View.inflate(parent.context, R.layout.normal_goods_stock_content_layout, null)
            val lp = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            itemView.layoutParams = lp
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            mData?.apply {
                val goodsStockInfo = get(position)
                holder.row_tv.text = (position + 1).toString()
                holder.only_code_tv.text = goodsStockInfo.only_coding
                holder.barcode_tv.text = goodsStockInfo.barcode
                holder.goods_name_tv.text = goodsStockInfo.goods_title
                holder.num_tv.text = String.format("%.2f",goodsStockInfo.stock_num)
                holder.unit_tv.text = goodsStockInfo.unit
                holder.spec_tv.text = goodsStockInfo.spec
            }
        }

        override fun onLoad(loadMode: IndicatorRecyclerView.OnLoad.LOADMODE) {
            mPageIndex++
            mLoadMode = loadMode
            query()
        }

        override fun continueLoad(): Boolean {
            return mLoadMore
        }

        override fun onAbort() {

        }

        @SuppressLint("NotifyDataSetChanged")
        private fun init(){
            ViewModelProvider(mContext).get(GoodsStockViewModel::class.java).getCurrentModel().observe(mContext){
                when(mLoadMode){
                    IndicatorRecyclerView.OnLoad.LOADMODE.BEHIND ->{
                        mData?.addAll(it.data)
                    }
                    IndicatorRecyclerView.OnLoad.LOADMODE.FRONT ->{
                        mData?.addAll(0,it.data)
                    }else ->{
                        mData = it.data
                    }
                }
                mLoadMore = it.total > mCondition?.limit?:0 && it.total > mData.size
                notifyDataSetChanged()
            }
        }
        fun query(cond:GoodsStockViewModel.Condition){
            mLoadMode = IndicatorRecyclerView.OnLoad.LOADMODE.OVER
            mPageIndex = 0
            mCondition = cond
            mLoadMore = false
            query()
        }
        private fun query(){
            mCondition?.apply {
                this.offset = mPageIndex
                ViewModelProvider(mContext).get(GoodsStockViewModel::class.java).query(this)
            }
        }
    }

    companion object{
        @JvmStatic
        fun start(context: Activity){
            context.startActivity(Intent(context,GoodsStockActivity::class.java))
        }
    }
}