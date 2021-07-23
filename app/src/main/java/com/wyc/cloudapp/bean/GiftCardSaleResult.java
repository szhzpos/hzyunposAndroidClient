package com.wyc.cloudapp.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: GiftCardSaleResult
 * @Description: 购物卡销售提交订单结果
 * @Author: wyc
 * @CreateDate: 2021-07-23 15:42
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-23 15:42
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */

public final class GiftCardSaleResult implements Serializable {

    @JSONField(name = "sc_id")
    private Object scId;
    @JSONField(name = "addtime")
    private Integer addtime;
    @JSONField(name = "order_code")
    private String orderCode;
    @JSONField(name = "order_money")
    private double orderMoney;
    @JSONField(name = "cas_id")
    private String casId;
    @JSONField(name = "stores_id")
    private String storesId;
    @JSONField(name = "origin")
    private String origin;
    @JSONField(name = "order_id")
    private Integer orderId;
    @JSONField(name = "xnum")
    private Integer xnum;

    public Object getScId() {
        return scId;
    }

    public void setScId(Object scId) {
        this.scId = scId;
    }

    public Integer getAddtime() {
        return addtime;
    }

    public void setAddtime(Integer addtime) {
        this.addtime = addtime;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public double getOrderMoney() {
        return orderMoney;
    }

    public void setOrderMoney(double orderMoney) {
        this.orderMoney = orderMoney;
    }

    public String getCasId() {
        return casId;
    }

    public void setCasId(String casId) {
        this.casId = casId;
    }

    public String getStoresId() {
        return storesId;
    }

    public void setStoresId(String storesId) {
        this.storesId = storesId;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getXnum() {
        return xnum;
    }

    public void setXnum(Integer xnum) {
        this.xnum = xnum;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GiftCardSaleResult that = (GiftCardSaleResult) o;
        return Objects.equals(orderCode, that.orderCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderCode);
    }

    @Override
    public String toString() {
        return "GiftCardSaleResult{" +
                "scId=" + scId +
                ", addtime=" + addtime +
                ", orderCode='" + orderCode + '\'' +
                ", orderMoney='" + orderMoney + '\'' +
                ", casId='" + casId + '\'' +
                ", storesId='" + storesId + '\'' +
                ", origin='" + origin + '\'' +
                ", orderId=" + orderId +
                ", xnum=" + xnum +
                '}';
    }
}
