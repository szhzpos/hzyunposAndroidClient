package com.wyc.cloudapp.activity.normal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.wyc.cloudapp.R
import com.wyc.cloudapp.activity.base.MainActivity
import com.wyc.cloudapp.adapter.FragmentPagerAdapter
import com.wyc.cloudapp.customizationView.RoundCornerTabLayout
import com.wyc.cloudapp.databinding.FragmentPagerContainerBinding
import com.wyc.cloudapp.fragment.*


/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.activity.normal
 * @ClassName:      DeliveryActivity
 * @Description:    配送相关功能
 * @Author:         wyc
 * @CreateDate:     2022/6/13 10:44
 * @UpdateUser:     更新者：
 * @UpdateDate:     2022/6/13 10:44
 * @UpdateRemark:   更新说明：
 * @Version:        1.0
 */

class DeliveryActivity : MainActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { RootLayout() }
    }

    @Preview
    @Composable
    fun RootLayout(){
        Column(
            Modifier
                .background(colorResource(R.color.white))
                .fillMaxHeight()) {
            Box(
                Modifier
                    .height(dimensionResource(R.dimen.height_58))
                    .fillMaxWidth()
                    .background(
                        colorResource(R.color.appColor)
                    )) {
                Row(
                    Modifier
                        .clickable { finish() }
                        .fillMaxHeight()
                        .wrapContentWidth()
                        .align(Alignment.CenterStart), verticalAlignment =  Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.back_ico), contentDescription = "",Modifier.padding(dimensionResource(R.dimen.margin_2)), tint = Color.White)
                    Text(text = stringResource(id = R.string.back), color = Color.White, fontSize = dimensionResource(R.dimen.font_size_18).value.sp)
                }
                Text(text = stringResource(id = R.string.delivery_orders),modifier = Modifier
                    .align(Alignment.Center)
                    .padding(top = dimensionResource(R.dimen.size_3)),color = colorResource(R.color.white),fontSize = dimensionResource(R.dimen.font_size_22).value.sp,textAlign = TextAlign.Center)
            }

            AndroidViewBinding(FragmentPagerContainerBinding::inflate){

                val fragments: MutableList<DeliveryOrderBase> = mutableListOf()
                fragments.add(NewDeliveryOrder())
                fragments.add(DispatchingOrder())
                fragments.add(CompleteDeliveryOrder())
                fragments.add(DeliveryOrderBase())
                fragments.add(RefundDeliveryOrder())

                val adapter: FragmentPagerAdapter<DeliveryOrderBase> = FragmentPagerAdapter(fragments,this@DeliveryActivity)
                val tabLayout = root.findViewById<RoundCornerTabLayout>(R.id._fragment_tab)
                val view_pager = root.findViewById<ViewPager2>(R.id.view_pager)

                view_pager.adapter = adapter
                TabLayoutMediator(
                    tabLayout, view_pager
                ) { tab: TabLayout.Tab, position: Int ->
                    tab.text = adapter.getItem(position).title
                    val num = adapter.getItem(position).getNumber()
                    if (num > 0){
                        tab.orCreateBadge.backgroundColor = getColor(R.color.appColor)
                        tab.orCreateBadge.number = adapter.getItem(position).getNumber()
                        tab.orCreateBadge.badgeTextColor = getColor(R.color.orange_1)
                    }
                }.attach()
            }
        }
    }

    companion object{
        @JvmStatic
        fun start(context: Context){
            context.startActivity(Intent(context,DeliveryActivity::class.java))
        }
    }
}