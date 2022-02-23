package com.wyc.cloudapp.bean;


import androidx.annotation.NonNull;

import com.wyc.cloudapp.utils.http.callback.Result;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: CouponResult
 * @Description: 优惠券明细查询数据
 * @Author: wyc
 * @CreateDate: 2022-02-22 9:30
 * @UpdateUser: 更新者
 * @UpdateDate: 2022-02-22 9:30
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class CouponResult extends Result {
    private DiscountCouponInfo detail;

    public DiscountCouponInfo getDetail() {
        return detail;
    }

    public void setDetail(DiscountCouponInfo detail) {
        this.detail = detail;
    }

    @NonNull
    @Override
    public String toString() {
        return "CouponResult{" +
                "detail=" + detail +
                "} " + super.toString();
    }
}
