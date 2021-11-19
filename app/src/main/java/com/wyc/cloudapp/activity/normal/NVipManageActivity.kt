package com.wyc.cloudapp.activity.normal

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity
import com.wyc.cloudapp.adapter.FragmentPagerAdapter
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.VipInfo
import com.wyc.cloudapp.customizationView.RoundCornerTabLayout
import com.wyc.cloudapp.databinding.FragmentPagerContainerBinding
import com.wyc.cloudapp.databinding.VipDetailInfoLayoutBinding
import com.wyc.cloudapp.dialog.CustomProgressDialog
import com.wyc.cloudapp.dialog.MyDialog
import com.wyc.cloudapp.dialog.vip.VipInfoDialog
import com.wyc.cloudapp.fragment.AbstractBaseFragment
import com.wyc.cloudapp.fragment.VipPickStuffFragment
import com.wyc.cloudapp.fragment.VipStoreStuffFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NVipManageActivity : MainActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LayoutContent()
        }
    }

    @Preview(device = Devices.NEXUS_10)
    @Composable
    fun LayoutContent(){
        Column(Modifier.background(colorResource(R.color.white))){
            Box(
                Modifier
                    .height(45.dp)
                    .fillMaxWidth()
                    .background(colorResource(R.color.activity_title_color))) {
                Row(
                    Modifier
                        .wrapContentWidth()
                        .align(Alignment.CenterStart),verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(R.drawable.back_ico), contentDescription = "",Modifier.padding(2.dp),tint = Color.White )
                    Text(text = stringResource(R.string.back),modifier = Modifier.clickable { finish() }
                        ,color = colorResource(R.color.white),fontSize = 16.sp)
                }
                Text(text = stringResource(R.string.vip_manage),modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = 3.dp),color = colorResource(R.color.white),fontSize = 20.sp,textAlign = TextAlign.Center)
            }
            Row{
                Column(
                    Modifier
                        .border(0.5.dp, Color.Gray)
                        .padding(5.dp)
                        .fillMaxHeight()
                        .weight(0.4f),horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(R.string.vip_dialog_title_sz),
                        Modifier, colorResource(R.color.text_color) ,fontSize = 18.sp,textAlign = TextAlign.Center)
                    Search()
                }
                Column(
                    Modifier
                        .wrapContentHeight()
                        .weight(1f)){

                    AndroidViewBinding(FragmentPagerContainerBinding::inflate){

                        val fragments: MutableList<AbstractBaseFragment> = mutableListOf()
                        fragments.add(VipStoreStuffFragment())
                        fragments.add(VipPickStuffFragment())

                        val adapter: FragmentPagerAdapter<AbstractBaseFragment> = FragmentPagerAdapter(fragments, this@NVipManageActivity)
                        val tabLayout = root.findViewById<RoundCornerTabLayout>(R.id._fragment_tab)
                        val view_pager = root.findViewById<ViewPager2>(R.id.view_pager)

                        view_pager.adapter = adapter
                        TabLayoutMediator(
                            tabLayout, view_pager
                        ) { tab: TabLayout.Tab, position: Int ->
                            tab.text = adapter.getItem(position).title
                        }.attach()
                    }
                }
            }
        }
    }


    @Composable
    private fun Search(){
        var vip by remember {mutableStateOf(VipInfo())}
        val text = remember {mutableStateOf("")}
        OutlinedTextField(value = text.value, onValueChange = {text.value = it},modifier = Modifier
             .padding(top = 5.dp,bottom = 5.dp),placeholder = {Text(stringResource(R.string.vip_search_hint_sz))},
            trailingIcon = {
                Icon(painter = painterResource(R.drawable.r_card), contentDescription = "",
                    Modifier.clickable {
                        val progressDialog = CustomProgressDialog.showProgress(this@NVipManageActivity,CustomApplication.self().getString(R.string.search_vip_hint))
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                vip = VipInfoDialog.searchVip(text.value).getJSONObject(0).toJavaObject(VipInfo::class.java)
                            }catch (e:Exception){
                                MyDialog.toastMessage(e.localizedMessage)
                            }
                            progressDialog.dismiss()
                        }
                    },tint = colorResource(R.color.lightBlue))
            },textStyle = TextStyle(fontSize = 16.sp),singleLine = true,colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = colorResource(R.color.text_color),
                placeholderColor = colorResource(R.color.gray),
                cursorColor = colorResource(R.color.gray),
                focusedBorderColor = colorResource(R.color.lightBlue),
                unfocusedBorderColor = colorResource(R.color.gray_subtransparent)),keyboardOptions = KeyboardOptions( keyboardType = KeyboardType.Number )
        )
        AndroidViewBinding(VipDetailInfoLayoutBinding::inflate){
            this.vipName.text = vip.name
            this.vipCardId.text = vip.card_code
            this.vipPNum.text = vip.mobile
            this.vipSex.text = vip.sex
            this.vipGradeTv.text = vip.gradeName
            this.vipDiscount.text = vip.discount.toString()
            this.vipBalance.text = vip.money_sum.toString()
            this.vipIntegral.text = vip.points_sum.toString()
        }
    }

    companion object{
        @JvmStatic
        fun start(context:Activity){
            context.startActivity(Intent(context,NVipManageActivity::class.java))
        }
    }
}
