package com.wyc.cloudapp.activity.normal

import android.app.Activity
import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity
import com.wyc.cloudapp.adapter.FragmentPagerAdapter
import com.wyc.cloudapp.application.CustomApplication
import com.wyc.cloudapp.bean.ModulePermission
import com.wyc.cloudapp.bean.VipInfo
import com.wyc.cloudapp.customizationView.RoundCornerTabLayout
import com.wyc.cloudapp.data.viewModel.VipInfoViewModel
import com.wyc.cloudapp.databinding.FragmentPagerContainerBinding
import com.wyc.cloudapp.databinding.VipDetailInfoLayoutBinding
import com.wyc.cloudapp.fragment.AbstractBaseFragment
import com.wyc.cloudapp.fragment.VipPickStuffFragment
import com.wyc.cloudapp.fragment.VipStoreStuffFragment

class NVipManageActivity : MainActivity(){
    private var mVipInfo:MutableState<VipInfo>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LayoutContent()
        }
        ViewModelProvider(this).get(VipInfoViewModel::class.java).addObserver(this){
            mVipInfo?.value = it
        }
    }

    @Preview(device = Devices.NEXUS_10)
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
                Text(text = stringResource(R.string.vip_manage),modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = dimensionResource(R.dimen.size_3)),color = colorResource(R.color.white),fontSize = dimensionResource(R.dimen.font_size_20).value.sp,textAlign = TextAlign.Center)
            }
            Row{
                Column(
                    Modifier
                        .padding(dimensionResource(R.dimen.size_5))
                        .fillMaxHeight()
                        .weight(0.4f),horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = stringResource(R.string.vip_dialog_title_sz),
                        Modifier, colorResource(R.color.text_color) ,fontSize = dimensionResource(R.dimen.font_size_18).value.sp,textAlign = TextAlign.Center)
                    Search()
                }
                Spacer(
                    Modifier
                        .fillMaxHeight()
                        .width(dimensionResource(R.dimen.size_1))
                        .background(colorResource(R.color.gray_subtransparent)))
                Column(
                    Modifier
                        .wrapContentHeight()
                        .weight(1f)){

                    AndroidViewBinding(FragmentPagerContainerBinding::inflate){

                        val fragments: MutableList<AbstractBaseFragment> = mutableListOf()
                        if (hasStorePermission())fragments.add(VipStoreStuffFragment())
                        if (hasPickPermission())fragments.add(VipPickStuffFragment())

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
    private fun hasStorePermission():Boolean{
        return  ModulePermission.checkModulePermission(37)
    }
    private fun hasPickPermission():Boolean{
        return  ModulePermission.checkModulePermission(38)
    }

    fun Number.toComposeSp()= (toFloat() / Resources.getSystem().displayMetrics.scaledDensity).sp

    @Composable
    private fun Search(){
        mVipInfo  =  remember {mutableStateOf(VipInfo())}
        val text = remember {mutableStateOf(TextFieldValue(""))}
        SearchTextField(text,{
            Icon(painter = painterResource(R.drawable.r_card), contentDescription = "",
                Modifier
                    .focusable()
                    .clickable {
                        ViewModelProvider(this)
                            .get(VipInfoViewModel::class.java)
                            .refresh(this, text.value.text)
                    },tint = colorResource(R.color.lightBlue))
        })
        mVipInfo?.value?.let {
            AndroidViewBinding(VipDetailInfoLayoutBinding::inflate){
                this.vipName.text = it.name
                this.vipCardId.text = it.card_code
                this.vipPNum.text = it.mobile
                this.vipSex.text = it.sex
                this.vipGradeTv.text = it.gradeName
                this.vipDiscount.text = it.discount.toString()
                this.vipBalance.text = it.money_sum.toString()
                this.vipIntegral.text = it.points_sum.toString()
            }
        }
    }

    companion object{
        @JvmStatic
        @Composable
        fun ListContent(content:String?,modifier: Modifier){
            Text(content?:"",modifier.padding(start = dimensionResource(R.dimen.size_5),top = dimensionResource(R.dimen.size_2),bottom = dimensionResource(R.dimen.size_2)
                ,end = dimensionResource(R.dimen.size_5)),color = colorResource(R.color.text_color),fontSize = dimensionResource(R.dimen.font_size_14).value.sp,textAlign = TextAlign.Center)
        }
        @JvmStatic
        @Composable
        fun ListTitle(title:String?,modifier: Modifier,c: Color = colorResource(R.color.white)){
            Text(title?:"",modifier.padding(dimensionResource(R.dimen.size_5)),color = c,fontSize = dimensionResource(R.dimen.font_size_16).value.sp,textAlign = TextAlign.Center)
        }

        @JvmStatic
        @Composable
        fun SearchTextField(textFieldValue:MutableState<TextFieldValue>,trailingIcon: @Composable (() -> Unit)? = null,modifier: Modifier = Modifier
            .onFocusChanged { focusState ->
                if (focusState.isFocused) {
                    val text = textFieldValue.value
                    textFieldValue.value =
                        textFieldValue.value.copy(selection = TextRange(0, text.text.length))
                }
            }
            .padding(top = dimensionResource(R.dimen.size_5), bottom = dimensionResource(R.dimen.size_5)),
                            placeholder: @Composable (() -> Unit)? = {Text(stringResource(R.string.vip_search_hint_sz),fontSize = dimensionResource(R.dimen.font_size_16).value.sp)},keyboardOptions: KeyboardOptions = KeyboardOptions( keyboardType = KeyboardType.Number )){

            OutlinedTextField(value = textFieldValue.value, onValueChange = {textFieldValue.value = it},modifier = modifier,placeholder = placeholder,
                trailingIcon = trailingIcon,textStyle = TextStyle(fontSize = dimensionResource(R.dimen.font_size_16).value.sp),singleLine = true,colors = textFieldColors(),keyboardOptions = keyboardOptions
            )
        }
        @JvmStatic
        @Composable
        fun textFieldColors():TextFieldColors{
            return TextFieldDefaults.outlinedTextFieldColors(
                textColor = colorResource(R.color.text_color),
                placeholderColor = colorResource(R.color.gray),
                cursorColor = colorResource(R.color.gray),
                focusedBorderColor = colorResource(R.color.lightBlue),
                unfocusedBorderColor = colorResource(R.color.gray_subtransparent))
        }
        @JvmStatic
        fun start(context:Activity){
            context.startActivity(Intent(context,NVipManageActivity::class.java))
        }
    }
}
