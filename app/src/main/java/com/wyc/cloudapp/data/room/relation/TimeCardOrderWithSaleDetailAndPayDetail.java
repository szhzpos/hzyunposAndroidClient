package com.wyc.cloudapp.data.room.relation;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Relation;

import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.data.room.entity.TimeCardPayDetail;
import com.wyc.cloudapp.data.room.entity.TimeCardSaleOrder;

import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.data.room.relation
 * @ClassName: TimeCardOrderWithDetailWithPayDetail
 * @Description: 查询次卡订单以及明细辅助类
 * @Author: wyc
 * @CreateDate: 2021-07-15 10:23
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-15 10:23
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TimeCardOrderWithSaleDetailAndPayDetail {
    @Embedded
    private TimeCardSaleOrder order;
    @Relation(entity = TimeCardSaleInfo.class,parentColumn = "order_no",entityColumn = "order_no")
    private List<TimeCardSaleInfo> saleInfoList;
    @Relation(entity = TimeCardPayDetail.class,parentColumn = "order_no",entityColumn = "order_no")
    private List<TimeCardPayDetail> payDetailList;

    public TimeCardSaleOrder getOrder() {
        return order;
    }

    public void setOrder(TimeCardSaleOrder order) {
        this.order = order;
    }

    public List<TimeCardSaleInfo> getSaleInfoList() {
        return saleInfoList;
    }

    public void setSaleInfoList(List<TimeCardSaleInfo> saleInfoList) {
        this.saleInfoList = saleInfoList;
    }

    public List<TimeCardPayDetail> getPayDetailList() {
        return payDetailList;
    }

    public void setPayDetailList(List<TimeCardPayDetail> payDetailList) {
        this.payDetailList = payDetailList;
    }

    @NonNull
    @Override
    public String toString() {
        return "TimeCardOrderWithDetailWithPayDetail{" +
                "order=" + order +
                ", saleInfoList=" + saleInfoList +
                ", payDetailList=" + payDetailList +
                '}';
    }
}
