package com.wyc.cloudapp.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: DeliveryOrderInfo
 * @Description: 商城配送单
 * @Author: wyc
 * @CreateDate: 2022/6/14 17:25
 * @UpdateUser: 更新者：
 * @UpdateDate: 2022/6/14 17:25
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class DeliveryOrderInfo {
    @JSONField(name = "end_time")
    private String endTime;
    @JSONField(name = "stores_img")
    private String storesImg;
    @JSONField(name = "real_money")
    private Double realMoney;
    @JSONField(name = "order_status")
    private String orderStatus;
    @JSONField(name = "start_time")
    private String startTime;
    @JSONField(name = "order_time")
    private String orderTime;
    @JSONField(name = "logistics_status")
    private String logisticsStatus;
    @JSONField(name = "pay_money")
    private Double payMoney;
    @JSONField(name = "freight")
    private Double freight;
    @JSONField(name = "member_mobile")
    private String memberMobile;
    @JSONField(name = "distributor_mobile")
    private String distributorMobile;
    @JSONField(name = "member_card")
    private String memberCard;
    @JSONField(name = "sh_tel")
    private String shTel;
    @JSONField(name = "status_name")
    private String statusName;
    @JSONField(name = "stores_name")
    private String storesName;
    @JSONField(name = "discount_money")
    private Double discountMoney;
    @JSONField(name = "order_code")
    private String orderCode;
    @JSONField(name = "sh_name")
    private String shName;
    @JSONField(name = "sh_address")
    private String shAddress;
    @JSONField(name = "order_money")
    private Double orderMoney;
    @JSONField(name = "slot")
    private String slot;
    @JSONField(name = "order_id")
    private String orderId;
    @JSONField(name = "mode_name")
    private String modeName;
    @JSONField(name = "mode")
    private String mode;
    @JSONField(name = "member_name")
    private String memberName;
    @JSONField(name = "distributor_name")
    private String distributorName;
    @JSONField(name = "packs")
    private Double packs;
    @JSONField(name = "send_money")
    private Double sendMoney;

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStoresImg() {
        return storesImg;
    }

    public void setStoresImg(String storesImg) {
        this.storesImg = storesImg;
    }

    public Double getRealMoney() {
        return realMoney;
    }

    public void setRealMoney(Double realMoney) {
        this.realMoney = realMoney;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(String orderTime) {
        this.orderTime = orderTime;
    }

    public String getLogisticsStatus() {
        return logisticsStatus;
    }

    public void setLogisticsStatus(String logisticsStatus) {
        this.logisticsStatus = logisticsStatus;
    }

    public Double getPayMoney() {
        return payMoney;
    }

    public void setPayMoney(Double payMoney) {
        this.payMoney = payMoney;
    }

    public Double getFreight() {
        return freight;
    }

    public void setFreight(Double freight) {
        this.freight = freight;
    }

    public String getMemberMobile() {
        return memberMobile;
    }

    public void setMemberMobile(String memberMobile) {
        this.memberMobile = memberMobile;
    }

    public String getDistributorMobile() {
        return distributorMobile;
    }

    public void setDistributorMobile(String distributorMobile) {
        this.distributorMobile = distributorMobile;
    }

    public String getMemberCard() {
        return memberCard;
    }

    public void setMemberCard(String memberCard) {
        this.memberCard = memberCard;
    }

    public String getShTel() {
        return shTel;
    }

    public void setShTel(String shTel) {
        this.shTel = shTel;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getStoresName() {
        return storesName;
    }

    public void setStoresName(String storesName) {
        this.storesName = storesName;
    }

    public Double getDiscountMoney() {
        return discountMoney;
    }

    public void setDiscountMoney(Double discountMoney) {
        this.discountMoney = discountMoney;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getShName() {
        return shName;
    }

    public void setShName(String shName) {
        this.shName = shName;
    }

    public String getShAddress() {
        return shAddress;
    }

    public void setShAddress(String shAddress) {
        this.shAddress = shAddress;
    }

    public Double getOrderMoney() {
        return orderMoney;
    }

    public void setOrderMoney(Double orderMoney) {
        this.orderMoney = orderMoney;
    }

    public String getSlot() {
        return slot;
    }

    public void setSlot(String slot) {
        this.slot = slot;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getModeName() {
        return modeName;
    }

    public void setModeName(String modeName) {
        this.modeName = modeName;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getDistributorName() {
        return distributorName;
    }

    public void setDistributorName(String distributorName) {
        this.distributorName = distributorName;
    }

    public Double getPacks() {
        return packs;
    }

    public void setPacks(Double packs) {
        this.packs = packs;
    }

    public Double getSendMoney() {
        return sendMoney;
    }

    public void setSendMoney(Double sendMoney) {
        this.sendMoney = sendMoney;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeliveryOrderInfo that = (DeliveryOrderInfo) o;
        return Objects.equals(orderCode, that.orderCode) && Objects.equals(orderId, that.orderId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderCode, orderId);
    }

    @Override
    public String toString() {
        return "DeliveryOrderInfo{" +
                "endTime='" + endTime + '\'' +
                ", storesImg='" + storesImg + '\'' +
                ", realMoney=" + realMoney +
                ", orderStatus='" + orderStatus + '\'' +
                ", startTime='" + startTime + '\'' +
                ", orderTime='" + orderTime + '\'' +
                ", logisticsStatus='" + logisticsStatus + '\'' +
                ", payMoney=" + payMoney +
                ", freight=" + freight +
                ", memberMobile='" + memberMobile + '\'' +
                ", distributorMobile='" + distributorMobile + '\'' +
                ", memberCard='" + memberCard + '\'' +
                ", shTel='" + shTel + '\'' +
                ", statusName='" + statusName + '\'' +
                ", storesName='" + storesName + '\'' +
                ", discountMoney=" + discountMoney +
                ", orderCode='" + orderCode + '\'' +
                ", shName='" + shName + '\'' +
                ", shAddress='" + shAddress + '\'' +
                ", orderMoney=" + orderMoney +
                ", slot='" + slot + '\'' +
                ", orderId='" + orderId + '\'' +
                ", modeName='" + modeName + '\'' +
                ", mode='" + mode + '\'' +
                ", memberName='" + memberName + '\'' +
                ", distributorName='" + distributorName + '\'' +
                ", packs=" + packs +
                ", sendMoney=" + sendMoney +
                '}';
    }
}
