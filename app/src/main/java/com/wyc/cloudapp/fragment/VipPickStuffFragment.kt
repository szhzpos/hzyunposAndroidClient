package com.wyc.cloudapp.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.ViewModelProvider
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.normal.NVipManageActivity.Companion.ListContent
import com.wyc.cloudapp.activity.normal.NVipManageActivity.Companion.ListTitle
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.VipInfo
import com.wyc.cloudapp.bean.VipPickStuffInfo
import com.wyc.cloudapp.constants.InterfaceURL
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.data.viewModel.VipInfoViewModel
import com.wyc.cloudapp.databinding.StoredNumEditBinding
import com.wyc.cloudapp.dialog.CustomProgressDialog
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.utils.Utils
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import com.wyc.cloudapp.utils.http.callback.ArrayCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.fragment
 * @ClassName:      VipPickStuffFragment
 * @Description:    会员取货
 * @Author:         wyc
 * @CreateDate:     2021-11-17 15:09
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-11-17 15:09
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class VipPickStuffFragment: AbstractBaseFragment(),CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private var mStuffList: SnapshotStateList<VipPickStuffInfo>? = null
    private var mAllChecked:MutableState<Boolean> = mutableStateOf(false)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewModelProvider(requireActivity()).get(VipInfoViewModel::class.java).addObserver(requireActivity()){
            mStuffList?.clear()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                GetViewLayout()
            }
        }
    }

    @Composable
    private fun GetViewLayout(){
        mStuffList = remember {
            mutableStateListOf( )
        }
        Column(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(),Arrangement.SpaceBetween) {
            List()
            Spacer(Modifier.weight(1f,true))
            Row(Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceEvenly){
                Button(onClick = {
                    loadDetail()
                },
                    Modifier
                        .padding(5.dp)
                    ,colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(
                        R.color.lightBlue
                    ),contentColor = colorResource(
                        R.color.white
                    ))) {
                    Text(stringResource(R.string.refresh), modifier = Modifier.padding(8.dp),fontSize = 18.sp)
                }

                Button(onClick = {
                    pickStuff()
                },
                    Modifier
                        .padding(5.dp)
                    ,colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(
                        R.color.lightBlue
                    ),contentColor = colorResource(
                        R.color.white
                    ))) {
                    Text(stringResource(R.string.pick_stuff), modifier = Modifier.padding(8.dp),fontSize = 18.sp)
                }
            }
            loadDetail()
        }
    }

    private fun pickStuff(){
        if (mStuffList != null && mStuffList!!.isNotEmpty() && mStuffList!!.any { it.isSel  }){
            val data = JSONArray()
            mStuffList!!.forEach {
                if (it.isSel){
                    val `object` = JSONObject()
                    `object`["xnum"] = it.pickNum
                    `object`["price"] = it.price
                    `object`["barcode_id"] = it.barcodeId
                    `object`["goods_id"] = it.goodsId
                    `object`["unit_id"] = it.unitId
                    `object`["conversion"] = it.conversion
                    data.add(`object`)
                }
            }

            val uploadObj = JSONObject()
            uploadObj["appid"] = CustomApplication.self().appId
            uploadObj["stores_id"] = CustomApplication.self().storeId
            uploadObj["pt_user_id"] = CustomApplication.self().ptUserId
            uploadObj["bgd_code"] = mStuffList!![0].bgdCode
            uploadObj["member_id"] = getVipInfo()?.member_id
            uploadObj["bgd_type"] = 7
            uploadObj["goods_list_json"] = data

            val progress = CustomProgressDialog.showProgress(requireActivity(), getString(R.string.upload_order_hints))
            launch {
                var retJson = HttpUtils.sendPost(CustomApplication.self().url + InterfaceURL.O_OUT_IN_UPLOAD, HttpRequest.generate_request_parma(uploadObj,CustomApplication.self().appSecret), true)
                if (HttpUtils.checkRequestSuccess(retJson)) {
                    var info = JSON.parseObject(retJson.getString("info"))
                    if (HttpUtils.checkBusinessSuccess(info)) {
                        uploadObj.clear()

                        uploadObj["appid"] = CustomApplication.self().appId
                        uploadObj["stores_id"] = CustomApplication.self().storeId
                        uploadObj["pt_user_id"] = CustomApplication.self().ptUserId
                        uploadObj["bgd_id"] = info.getString("bgd_id")
                        retJson = HttpUtils.sendPost(CustomApplication.self().url + InterfaceURL.OUT_IN_SH, HttpRequest.generate_request_parma(uploadObj,CustomApplication.self().appSecret), true)
                        if (HttpUtils.checkRequestSuccess(retJson)){
                            info = JSON.parseObject(retJson.getString("info"))
                            if (HttpUtils.checkBusinessSuccess(info)){
                                mAllChecked.value = false
                                mStuffList?.clear()
                                MyDialog.toastMessage(getString(R.string.success))
                            }else MyDialog.toastMessage(info.getString("info"))
                        }
                    }else MyDialog.toastMessage(info.getString("info"))
                }
                progress.dismiss()
            }
        }else MyDialog.toastMessage(CustomApplication.self().getString(R.string.stuff_not_empty))
    }

    @Composable
    private fun List(){
        mStuffList!!.let {
            GenerateTitle()
            LazyColumn(content = {
                items(it.size){index ->
                    GenerateRow(index + 1,it)
                }
            })
        }
    }

    @Composable
    private fun GenerateTitle(){
        Row(
            Modifier
                .padding(start = 5.dp, end = 5.dp)
                .background(colorResource(R.color.lightBlue))
                .height(45.dp)
                .fillMaxWidth(),verticalAlignment = Alignment.CenterVertically) {

            mAllChecked = remember {
                mutableStateOf(false)
            }
            Checkbox(checked = mAllChecked.value, onCheckedChange = { mAllChecked.value = it
                mStuffList?.apply {
                    val l = toMutableList()
                    l.forEach { i ->
                        i.isSel = mAllChecked.value
                    }
                    clear()
                    addAll(l)
                } },
                Modifier
                    .width(30.dp)
                    .height(30.dp),colors =  CheckboxDefaults.colors(uncheckedColor = colorResource(
                    R.color.white
                )))

            ListTitle(
                stringResource(R.string.row_id_sz),
                Modifier
                    .width(45.dp))

            ListTitle(
                stringResource(R.string.barcode_sz),
                Modifier
                    .width(
                        dimensionResource(R.dimen.table_barcode_width)
                    ))

            ListTitle(
                stringResource(R.string.item_no_sz),
                Modifier
                    .width(dimensionResource(R.dimen.table_barcode_width)))

            ListTitle(
                stringResource(R.string.g_name_sz),
                Modifier
                    .width(
                        dimensionResource(R.dimen.table_goods_title_width)
                    ))

            ListTitle(
                stringResource(R.string.sum_store_num),
                Modifier
                    .width(88.dp))

            ListTitle(
                stringResource(R.string.packed_num),
                Modifier
                    .width(88.dp))

            ListTitle(
                stringResource(R.string.unit_sz),
                Modifier
                    .width(45.dp))
            ListTitle(
                stringResource(R.string.pick_num),
                Modifier
                    .width(88.dp))
        }
    }

    @Composable
    private fun GenerateRow(index:Int,list:SnapshotStateList<VipPickStuffInfo>){
        val stuffInfo = list[index - 1]
        Row(
            Modifier
                .padding(start = 5.dp, top = 2.dp, bottom = 2.dp, end = 5.dp)
                .background(colorResource(R.color.white))
                .height(40.dp)
                .fillMaxWidth(),verticalAlignment = Alignment.CenterVertically) {

            val able = remember {
                mutableStateOf(false)
            }
            able.value = stuffInfo.isSel

            var checked by remember {
                mutableStateOf(false)
            }
            checked = stuffInfo.isSel
            Checkbox(checked = checked, onCheckedChange = { checked = it
                stuffInfo.isSel = checked
                able.value = checked

                mAllChecked.value = list.all { i->
                    i.isSel
                } },
                Modifier
                    .width(30.dp)
                    .height(30.dp))
            ListContent(index.toString(),
                Modifier
                    .width(45.dp))
            ListContent(stuffInfo.barcode,
                Modifier
                    .width(
                        dimensionResource(R.dimen.table_barcode_width)
                    ))
            ListContent(stuffInfo.onlyCoding,
                Modifier
                    .width(dimensionResource(R.dimen.table_barcode_width)))
            ListContent(stuffInfo.goodsTitle,
                Modifier
                    .width(
                        dimensionResource(R.dimen.table_goods_title_width)
                    ))

            ListContent(String.format("%.2f",stuffInfo.xnumJicun),
                Modifier
                    .width(88.dp))

            ListContent(String.format("%.2f",stuffInfo.xnumOut),
                Modifier
                    .width(88.dp))

            ListContent(stuffInfo.unit,
                Modifier
                    .width(45.dp))
            AndroidViewBinding(StoredNumEditBinding::inflate){
                val e = root.findViewById<EditText>(R.id.store_num)
                e.setText(String.format("%.2f",stuffInfo.xnumSurplus))
                e.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(
                        s: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int
                    ) {

                    }

                    override fun onTextChanged(
                        s: CharSequence?,
                        start: Int,
                        before: Int,
                        count: Int
                    ) {

                    }
                    override fun afterTextChanged(s: Editable) {
                        var num = s.toString().toDouble()
                        if (Utils.lessDouble(stuffInfo.xnumSurplus,num)){
                            num = stuffInfo.xnumSurplus
                            e.setText(String.format("%.2f",num))
                            e.selectAll()
                            MyDialog.toastMessage(getString(R.string.pick_num_error_hint))
                        }
                        stuffInfo.pickNum = num
                    }
                })
                e.isEnabled = able.value
            }
        }
    }

    private fun loadDetail(){
        val vipInfo = getVipInfo()
        if (vipInfo != null && !vipInfo.isEmpty){
            val param = JSONObject()
            param["appid"]= CustomApplication.self().appId
            param["pt_user_id"]= CustomApplication.self().ptUserId
            param["xtype"]= "card_code"
            param["stores_id"]= CustomApplication.self().storeId
            param["keyword"] = vipInfo.card_code

            val progressDialog = CustomProgressDialog.showProgress(requireContext(), requireContext().getString(R.string.hints_query_data_sz))
            HttpUtils.sendAsyncPost(CustomApplication.self().url + "/api/bgd/member_deposit_list",HttpRequest.generate_request_parma(param,CustomApplication.self().appSecret))
                .enqueue(object : ArrayCallback<VipPickStuffInfo>(VipPickStuffInfo::class.java) {
                    override fun onError(msg: String?) {
                        MyDialog.toastMessage(msg)
                        progressDialog.dismiss()
                    }

                    override fun onSuccessForResult(
                        d: MutableList<VipPickStuffInfo>?,
                        hint: String?
                    ) {
                        d?.let {
                            launch {
                                dealData(d)
                                mStuffList?.apply {
                                    clear()
                                    addAll(it)
                                }
                            }
                        }
                        progressDialog.dismiss()
                        MyDialog.toastMessage(hint)
                    }
                })
        }else MyDialog.toastMessage(CustomApplication.self().getString(R.string.query_vip_hint))
    }

    private fun dealData(d: MutableList<VipPickStuffInfo>){
        val info = JSONObject()
        d.forEach {
            if (SQLiteHelper.execSql(info,"select retail_price,conversion,unit_id from barcode_info where barcode_id = '"+ it.barcodeId +"'")){
                it.conversion = info.getIntValue("conversion")
                it.unitId = info.getString("unit_id")
                it.price = info.getDoubleValue("retail_price")
            }else {
                MyDialog.toastMessage(info.getString("info"))
                return@forEach
            }
        }
    }

    private fun getVipInfo(): VipInfo?{
        return ViewModelProvider(requireActivity()).get(VipInfoViewModel::class.java).getVipInfo()
    }

    override fun getTitle(): String {
        return CustomApplication.self().getString(R.string.pick_stuff)
    }

    override fun viewCreated() {

    }
}