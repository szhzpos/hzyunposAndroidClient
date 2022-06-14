package com.wyc.cloudapp.bean;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: DeliveryOrderNum
 * @Description: 作用描述
 * @Author: wyc
 * @CreateDate: 2022/6/14 13:29
 * @UpdateUser: 更新者：
 * @UpdateDate: 2022/6/14 13:29
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class DeliveryOrderNum {
    @JSONField(name = "all")
    private int allOrder = 0;
    /*新订单*/
    @JSONField(name = "xdd")
    private int newOrder = 0;
    /*备货中*/
    @JSONField(name = "bhz")
    private int prepareOrder = 0;
    /*配送中*/
    @JSONField(name = "psz")
    private int dispatchingOrder = 0;
    /*已完成*/
    @JSONField(name = "ywc")
    private int completeOrder = 0;
    /*退货申请*/
    @JSONField(name = "thsq")
    private int refundOrder = 1;

    public int getAllOrder() {
        return allOrder;
    }

    public void setAllOrder(int allOrder) {
        this.allOrder = allOrder;
    }

    public int getNewOrder() {
        return newOrder;
    }

    public void setNewOrder(int newOrder) {
        this.newOrder = newOrder;
    }

    public int getPrepareOrder() {
        return prepareOrder;
    }

    public void setPrepareOrder(int prepareOrder) {
        this.prepareOrder = prepareOrder;
    }

    public int getDispatchingOrder() {
        return dispatchingOrder;
    }

    public void setDispatchingOrder(int dispatchingOrder) {
        this.dispatchingOrder = dispatchingOrder;
    }

    public int getCompleteOrder() {
        return completeOrder;
    }

    public void setCompleteOrder(int completeOrder) {
        this.completeOrder = completeOrder;
    }

    public int getRefundOrder() {
        return refundOrder;
    }

    public void setRefundOrder(int refundOrder) {
        this.refundOrder = refundOrder;
    }

    @Override
    public String toString() {
        return "DeliveryOrderNum{" +
                "allOrder=" + allOrder +
                ", newOrder=" + newOrder +
                ", prepareOrder=" + prepareOrder +
                ", dispatchingOrder=" + dispatchingOrder +
                ", completeOrder=" + completeOrder +
                ", refundOrder=" + refundOrder +
                '}';
    }
}
