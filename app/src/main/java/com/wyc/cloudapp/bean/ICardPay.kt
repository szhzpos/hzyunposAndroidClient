package com.wyc.cloudapp.bean

import android.os.Parcelable
import com.wyc.cloudapp.activity.base.MainActivity

/*
* 泛型参数 S 表示销售明细类型
* */
interface ICardPay<S> :Parcelable{
    fun getVip_openid():String?{
        return ""
    }
    fun  getVip_card_no():String?{
        return ""
    }
    fun getVip_name():String?{
        return ""
    }
    fun getVip_mobile():String?{
        return ""
    }
    fun getDiscountAmt():Double{
        return 0.0;
    }

    fun getAmt():Double
    fun getSaleInfo():List<S>
    fun getOrder_no():String
    fun save(a: MainActivity, payDetailList:List<PayDetailInfo>)
}