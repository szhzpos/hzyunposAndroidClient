package com.wyc.cloudapp.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.ViewModelProvider
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.normal.NVipManageActivity
import com.wyc.cloudapp.activity.normal.NVipManageActivity.Companion.ListContent
import com.wyc.cloudapp.activity.normal.NVipManageActivity.Companion.ListTitle
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.VipInfo
import com.wyc.cloudapp.bean.VipStoreStuffInfo
import com.wyc.cloudapp.constants.InterfaceURL
import com.wyc.cloudapp.data.SQLiteHelper
import com.wyc.cloudapp.data.viewModel.OrderIdViewModel
import com.wyc.cloudapp.data.viewModel.VipInfoViewModel
import com.wyc.cloudapp.databinding.StoredNumEditBinding
import com.wyc.cloudapp.dialog.CustomProgressDialog
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.logger.Logger
import com.wyc.cloudapp.utils.http.HttpRequest
import com.wyc.cloudapp.utils.http.HttpUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.fragment
 * @ClassName:      VipStoreStuff
 * @Description:    会员存货
 * @Author:         wyc
 * @CreateDate:     2021-11-17 11:51
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-11-17 11:51
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */
class VipStoreStuffFragment: AbstractBaseFragment(),CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private var mStuffList: SnapshotStateList<VipStoreStuffInfo>? = null
    private var mOrderCode:String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewModelProvider(requireActivity()).get(VipInfoViewModel::class.java).addObserver(requireActivity()){
            mStuffList?.clear()
        }
        ViewModelProvider(requireActivity()).get(OrderIdViewModel::class.java).init("BG").observe(this){
            mOrderCode = it
        }
    }
    override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setContent {
                GenerateView()
            }
        }
    }
    @Preview(device = Devices.NEXUS_10)
    @Composable
    private fun GenerateView(){
        Column(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(),Arrangement.SpaceBetween) {

            mStuffList = remember {
                mutableStateListOf( )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.Top),Arrangement.Start) {
                var expanded by remember { mutableStateOf(false) }
                val menu = remember {listOf("按商品","按单号")}
                val menuText = remember {mutableStateOf(menu[0])}
                OutlinedTextField(value = menuText.value,{menuText.value = it},
                    Modifier
                        .width(128.dp)
                        .padding(5.dp),enabled = false,readOnly = true,
                    trailingIcon = {
                        Icon(painter = painterResource(R.drawable.content), contentDescription = "",
                            Modifier
                                .clickable {
                                    expanded = !expanded
                                }
                                .width(25.dp)
                                .height(25.dp),tint = colorResource(R.color.lightBlue))
                    },
                    textStyle = TextStyle(fontSize = 16.sp),singleLine = true,colors = NVipManageActivity.textFieldColors()
                )
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    menu.forEachIndexed { index, s ->
                        DropdownMenuItem(
                            onClick = {
                                menuText.value = s
                                expanded = false
                            }
                        ) {
                            Text(s, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
                val textFieldValue = remember {mutableStateOf(TextFieldValue(""))}
                NVipManageActivity.SearchTextField(
                    textFieldValue,placeholder = {Text(stringResource(R.string.search_input_hint))},
                    keyboardOptions = KeyboardOptions( keyboardType = KeyboardType.Text )
                )
                Button(onClick = {
                   if (menuText.value == menu[0])getGoods(textFieldValue.value.text,mStuffList)else getGoods(textFieldValue.value.text,mStuffList,1)
                },
                    Modifier
                        .padding(5.dp)
                        .align(Alignment.CenterVertically),colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(
                        R.color.lightBlue
                    ),contentColor = colorResource(
                        R.color.white
                    ))) {
                    Text(stringResource(R.string.query_sz), modifier = Modifier.padding(8.dp),fontSize = 18.sp)
                }
                Button(onClick = {
                        val vip = getVipInfo()
                        if (vip != null && !vip.isEmpty){
                            getGoods(vip.card_code,mStuffList,0)
                        }else MyDialog.toastMessage(CustomApplication.self().getString(R.string.query_vip_hint))
                    },
                    Modifier
                        .padding(5.dp)
                        .align(Alignment.CenterVertically),colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(
                        R.color.lightBlue
                    ),contentColor = colorResource(
                        R.color.white
                    ))) {
                    Text(stringResource(R.string.last_order), modifier = Modifier.padding(8.dp),fontSize = 18.sp)
                }
            }
            List(mStuffList)
            Spacer(Modifier.weight(1f,true))
            Button(onClick = {
                val vip = ViewModelProvider(requireActivity()).get(VipInfoViewModel::class.java).getVipInfo()
                if (vip != null && !vip.isEmpty){
                    storeStuff()
                }else MyDialog.toastMessage(CustomApplication.self().getString(R.string.query_vip_hint))
            },
                Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(5.dp)
                    ,colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(
                    R.color.lightBlue
                ),contentColor = colorResource(
                    R.color.white
                ))) {
                Text(stringResource(R.string.store), modifier = Modifier.padding(8.dp),fontSize = 18.sp)
            }
        }
    }

    private fun getVipInfo():VipInfo?{
        return ViewModelProvider(requireActivity()).get(VipInfoViewModel::class.java).getVipInfo()
    }

    private fun storeStuff(){
        if (mStuffList != null && mStuffList!!.isNotEmpty()){
            val data = JSONArray()
            mStuffList!!.forEach {
                val `object` = JSONObject()
                `object`["xnum"] = it.storeNum
                `object`["price"] = it.price
                `object`["buying_price"] = it.buying_price
                `object`["xnote"] = ""
                `object`["barcode_id"] = it.barcode_id
                `object`["goods_id"] = it.goods_id
                `object`["conversion"] = it.conversion
                `object`["produce_date"] = ""
                `object`["unit_id"] = it.unit_id
                data.add(`object`)
            }

            val uploadObj = JSONObject()
            uploadObj["appid"] = CustomApplication.self().appId
            uploadObj["stores_id"] = CustomApplication.self().storeId
            uploadObj["pt_user_id"] = CustomApplication.self().ptUserId
            uploadObj["member_id"] = getVipInfo()?.member_id
            uploadObj["bgd_code"] = mOrderCode
            uploadObj["remark"] = String.format("会员%s【%s】%s",getVipInfo()?.mobile?:"",getVipInfo()?.name?:"",
                CustomApplication.self().getString(R.string.store_stuff))
            uploadObj["bgd_type"] = 3
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
    /**
     * 获取存货商品列表
     * @param type 0会员消费最后一单 1按单号获取  默认按商品条码或货号查询
     * @param list 可观察列表负责显示到界面
     * */
    private fun getGoods(content:String?,list: SnapshotStateList<VipStoreStuffInfo>?,type:Int = 3) {
        launch {
            var sql = "SELECT  barcode_id,goods_id,1 storeNum,conversion,unit_name unit,unit_id,retail_price price,buying_price, only_coding itemNo, goods_title name,barcode\n" +
                    "  FROM barcode_info t1 where (barcode = '" + content +"' or only_coding = '"+ content +"') and barcode_status = 1"
            when(type){
                0->{
                    sql = "SELECT c.only_coding itemNo,b.barcode,b.buying_price,b.conversion,c.goods_title name,c.barcode_id,c.goods_id,c.unit_id,price,c.unit_name unit,b.xnum storeNum\n" +
                            "  FROM retail_order_goods b inner join barcode_info c \n" +
                            "  on b.barcode_id = c.barcode_id where b.order_code = (select order_code from retail_order where card_code ='"+ content +"' and order_status = 2 order by addtime asc )"
                }
                1->{
                    sql = "SELECT c.only_coding itemNo,b.barcode,b.buying_price,b.conversion,c.goods_title name,c.barcode_id,c.goods_id,c.unit_id,c.unit_name unit,price,xnum storeNum\n" +
                            "  FROM retail_order a inner join retail_order_goods b \n" +
                            "  on a.order_code = b.order_code and a.order_status = 2 inner join barcode_info c \n" +
                            "  on b.barcode_id = c.barcode_id where a.order_code like '%"+ content +"'"
                }
            }
            list?.let {
                Logger.d("sql:%s",sql)
                val stuffList = SQLiteHelper.getListToJson(sql,null)
                if (stuffList != null){
                    list.clear()
                    list.addAll(stuffList.toJavaList(VipStoreStuffInfo::class.java))
                }else list.clear()
            }
        }
    }

    @Composable
    private fun List(stuffList:SnapshotStateList<VipStoreStuffInfo>?){
        stuffList?.let {
            GenerateTitle()
            LazyColumn(content = {
                items(stuffList.size){index ->
                    GenerateRow(index + 1,stuffList)
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
        ListTitle(stringResource(R.string.action_sz),Modifier.width(45.dp))
        ListTitle(stringResource(R.string.row_id_sz),
            Modifier
                .width(45.dp))
        ListTitle(stringResource(R.string.barcode_sz),
            Modifier
                .width(
                    dimensionResource(R.dimen.table_barcode_width)
                ))
        ListTitle(stringResource(R.string.item_no_sz),
            Modifier
                .width(128.dp))
        ListTitle(stringResource(R.string.g_name_sz),
            Modifier
                .width(
                    dimensionResource(R.dimen.table_goods_title_width)
                ))
        ListTitle(stringResource(R.string.sec_price_sz),
            Modifier
                .width(88.dp))
        ListTitle(stringResource(R.string.unit_sz),
            Modifier
                .width(45.dp))
        ListTitle(stringResource(R.string.stored_num),
            Modifier
                .width(88.dp))
        }
    }
    @Composable
    private fun GenerateRow(index:Int,stuffList:SnapshotStateList<VipStoreStuffInfo>){
        val stuffInfo = stuffList[index - 1]
        Row(
            Modifier
                .padding(start = 5.dp, top = 2.dp, bottom = 2.dp, end = 5.dp)
                .background(colorResource(R.color.white))
                .height(40.dp)
                .fillMaxWidth(),verticalAlignment = Alignment.CenterVertically) {
            Icon(painter = painterResource(R.drawable.unsel), contentDescription = "",
                Modifier
                    .width(45.dp)
                    .height(20.dp)
                    .clickable {
                        stuffList.remove(stuffInfo)
                    },tint = Color.Red)
            ListContent(index.toString(),
                Modifier
                    .width(45.dp))
            ListContent(stuffInfo.barcode,
                Modifier
                    .width(
                        dimensionResource(R.dimen.table_barcode_width)
                    ))
            ListContent(stuffInfo.itemNo,
                Modifier
                    .width(128.dp))
            ListContent(stuffInfo.name,
                Modifier
                    .width(
                        dimensionResource(R.dimen.table_goods_title_width)
                    ))
            ListContent(String.format("%.2f",stuffInfo.price),
                Modifier
                    .width(88.dp))
            ListContent(stuffInfo.unit,
                Modifier
                    .width(45.dp))
            AndroidViewBinding(StoredNumEditBinding::inflate){
                val e = root.findViewById<EditText>(R.id.store_num)
                e.setText(String.format("%.2f",stuffInfo.storeNum))
                e.addTextChangedListener(object :TextWatcher{
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
                    override fun afterTextChanged(s: Editable?) {
                        stuffInfo.storeNum = s.toString().toDouble()
                    }
                })
            }
        }
    }

    override fun getTitle(): String {
        return CustomApplication.self().getString(R.string.store_stuff)
    }

    override fun viewCreated() {

    }
}