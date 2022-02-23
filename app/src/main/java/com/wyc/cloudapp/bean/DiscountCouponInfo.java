package com.wyc.cloudapp.bean;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.annotation.JSONField;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: DiscountCouponDetail
 * @Description: 会员优惠券明细
 * @Author: wyc
 * @CreateDate: 2022-02-21 11:37
 * @UpdateUser: 更新者
 * @UpdateDate: 2022-02-21 11:37
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */

/**
 *
 *        "moneyClass": {定义优惠方式
 *          "1": "减免",
 *          "2": "折扣"
 *        },
 *        "receivetype": {
 *          "1": "不限制",
 *          "4": "特定渠道使用券",
 *          "3": "新人注册会员领取",
 *          "2": "只限会员领取"
 *        },
 *        "moneyType": {定义优惠条件
 *          "1": "无门槛",
 *          "2": "消费满"
 *        },
 *        "storesType": {是否指定使用门店
 *          "1": "全部门店",
 *          "2": "指定门店"
 *        },
 *        "couponType": {优惠券类型
 *          "1": "现金券",
 *          "3": "满折券",
 *          "2": "满减券"
 *        },
 *        "nogoodsType": {	是否指定不参与商品
 *          "1": "无",
 *          "3": "指定商品",
 *          "2": "指定分类"
 *        },
 *        "goodsType": {是否指定参与商品
 *          "1": "全部商品",
 *          "3": "指定商品",
 *          "2": "指定分类"
 *        }
 *
 *        status 优惠券状态，1 正常 2 已删除
 *        storesOffline 是否线下可用,1为线下可用
 *
 * */
public class DiscountCouponInfo {
    private static final String SEPARATOR = ",";

    @JSONField(name = "goods_type_val_json")
    private String goodsTypeValJson;
    @JSONField(name = "no_goods_type")
    private String noGoodsType;
    @JSONField(name = "end_time")
    private Long endTime;
    @JSONField(name = "stores_online")
    private String storesOnline;
    @JSONField(name = "stores_id")
    private String storesId;
    @JSONField(name = "stores_offline")
    private Integer storesOffline;
    @JSONField(name = "start_time")
    private Long startTime;
    @JSONField(name = "stores_val_json")
    private String storesValJson;
    @JSONField(name = "logno")
    private String logno;
    @JSONField(name = "status")
    private Integer status;
    @JSONField(name = "coupon_type")
    private Integer couponType;
    @JSONField(name = "coupon_id")
    private String couponId;
    @JSONField(name = "receive_num")
    private String receiveNum;
    @JSONField(name = "money_type")
    private String moneyType;
    @JSONField(name = "coupon_zhe")
    private Double couponZhe;
    @JSONField(name = "stores_type")
    private String storesType;
    @JSONField(name = "sh_status")
    private Integer shStatus;
    @JSONField(name = "coupon_name")
    private String couponName;
    @JSONField(name = "goods_type_val")
    private String goodsTypeVal;
    @JSONField(name = "money_class")
    private String moneyClass;
    @JSONField(name = "description")
    private String description;
    @JSONField(name = "receive_type")
    private Integer receiveType;
    @JSONField(name = "no_goods_type_val_json")
    private String noGoodsTypeValJson;
    @JSONField(name = "coupon_money")
    private Double couponMoney;
    @JSONField(name = "no_goods_type_val")
    private String noGoodsTypeVal;
    @JSONField(name = "coupon_condition")
    private Double couponCondition;
    @JSONField(name = "goods_type")
    private String goodsType;

    public String getGoodsTypeValJson() {
        return goodsTypeValJson;
    }

    public void setGoodsTypeValJson(String goodsTypeValJson) {
        this.goodsTypeValJson = goodsTypeValJson;
    }

    public String getNoGoodsType() {
        return noGoodsType;
    }

    public @NonNull List<String> getNoGoodsTypeToArray() {
        String[] c = Utils.isNotEmpty(noGoodsTypeVal)? noGoodsTypeVal.split(SEPARATOR) : null;
        if (c != null)return Arrays.asList(c);
        return new ArrayList<>();
    }

    public void setNoGoodsType(String noGoodsType) {
        this.noGoodsType = noGoodsType;
    }

    public Long getEndTime() {
        return endTime;
    }

    public String getStoresOnline() {
        return storesOnline;
    }

    public void setStoresOnline(String storesOnline) {
        this.storesOnline = storesOnline;
    }

    public String getStoresId() {
        return storesId;
    }

    public void setStoresId(String storesId) {
        this.storesId = storesId;
    }

    public Integer getStoresOffline() {
        return storesOffline;
    }

    public void setStoresOffline(Integer storesOffline) {
        this.storesOffline = storesOffline;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public String getStoresValJson() {
        return storesValJson;
    }

    public void setStoresValJson(String storesValJson) {
        this.storesValJson = storesValJson;
    }

    public String getLogno() {
        return logno;
    }

    public void setLogno(String logno) {
        this.logno = logno;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCouponType() {
        return couponType;
    }

    public void setCouponType(Integer couponType) {
        this.couponType = couponType;
    }

    public String getCouponId() {
        return couponId;
    }

    public void setCouponId(String couponId) {
        this.couponId = couponId;
    }

    public String getReceiveNum() {
        return receiveNum;
    }

    public void setReceiveNum(String receiveNum) {
        this.receiveNum = receiveNum;
    }

    public String getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(String moneyType) {
        this.moneyType = moneyType;
    }

    public Double getCouponZhe() {
        return couponZhe;
    }

    public void setCouponZhe(Double couponZhe) {
        this.couponZhe = couponZhe;
    }

    public String getStoresType() {
        return storesType;
    }

    public void setStoresType(String storesType) {
        this.storesType = storesType;
    }

    public Integer getShStatus() {
        return shStatus;
    }

    public void setShStatus(Integer shStatus) {
        this.shStatus = shStatus;
    }

    public String getCouponName() {
        return couponName;
    }

    public void setCouponName(String couponName) {
        this.couponName = couponName;
    }

    public String getGoodsTypeVal() {
        return goodsTypeVal;
    }
    public @NonNull List<String> getGoodsTypeValToArray() {
        String[] c = Utils.isNotEmpty(goodsTypeVal)? goodsTypeVal.split(SEPARATOR) : null;
        if (c != null)return Arrays.asList(c);
        return new ArrayList<>();
    }

    public void setGoodsTypeVal(String goodsTypeVal) {
        this.goodsTypeVal = goodsTypeVal;
    }

    public String getMoneyClass() {
        return moneyClass;
    }

    public void setMoneyClass(String moneyClass) {
        this.moneyClass = moneyClass;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getReceiveType() {
        return receiveType;
    }

    public void setReceiveType(Integer receiveType) {
        this.receiveType = receiveType;
    }

    public String getNoGoodsTypeValJson() {
        return noGoodsTypeValJson;
    }

    public void setNoGoodsTypeValJson(String noGoodsTypeValJson) {
        this.noGoodsTypeValJson = noGoodsTypeValJson;
    }

    public Double getCouponMoney() {
        return couponMoney;
    }

    public void setCouponMoney(Double couponMoney) {
        this.couponMoney = couponMoney;
    }

    public String getNoGoodsTypeVal() {
        return noGoodsTypeVal;
    }

    public void setNoGoodsTypeVal(String noGoodsTypeVal) {
        this.noGoodsTypeVal = noGoodsTypeVal;
    }

    public Double getCouponCondition() {
        return couponCondition;
    }

    public void setCouponCondition(Double couponCondition) {
        this.couponCondition = couponCondition;
    }

    public String getGoodsType() {
        return goodsType;
    }

    public void setGoodsType(String goodsType) {
        this.goodsType = goodsType;
    }

    public boolean isNormalStatus(){
        return 1 == status;
    }

    public boolean isUseTimeValid(){
        long cur = new Date().getTime() / 1000;
       return startTime <= cur && cur <= endTime;
    }

    public boolean isStoresOffline(){
        return 1 == storesOffline;
    }

    public boolean isDerate(){
        return "1".equals(moneyClass);
    }

    public boolean isDiscount(){
        return "2".equals(moneyClass);
    }
    /**
     * 无门槛
     * */
    public boolean isUnlimited(){
        return "1".equals(moneyType);
    }
    /**
     * 消费满
     * */
    public boolean isMoneyOff(){
        return "2".equals(moneyType);
    }

    /**
     * 当前门店是否可用
     * */
    public boolean isStoreValid(){
        if ("1".equals(storesType)){
            return true;
        }else {
            final String[] stores = storesId == null ? null : storesId.split(",");
            if (stores == null || stores.length == 0){
                return false;
            }else return Arrays.asList(stores).contains(CustomApplication.self().getStoreId());
        }
    }

    /**
     * 是否为现金券
     * */
    public boolean isCashCoupon(){
       return   1 == couponType;
    }

    /**
     * 是否为满折券
     * */
    public boolean isAttainDiscountCoupon(){
        return   3 == couponType;
    }

    /**
     * 是否为满减券
     * */
    public boolean isAttainReductionCoupon(){
        return   2 == couponType;
    }

    /**
     * 判断当前优惠券是否有效
     * 参与参数： 状态、有效期、使用门店、是否为线下可用
     * */
    public boolean hasValid(){
        if (isNormalStatus()){
            if (isUseTimeValid()){
                if (isStoreValid()){
                    if (isStoresOffline()){
                        if (isAttainReductionCoupon() || isCashCoupon()){
                            return true;
                        }else {
                            MyDialog.toastMessage("目前不支持满折券!");
                        }
                    }else {
                        MyDialog.toastMessage(R.string.coupon_not_use_offline);
                    }
                }else {
                    MyDialog.toastMessage(R.string.coupon_not_use_hint);
                }
            }else {
                MyDialog.toastMessage(R.string.coupon_expire_hint);
            }
        }else {
            MyDialog.toastMessage(R.string.coupon_disable_hint);
        }
        return false;
    }
    /**
     * 所有商品参与
     * */
    public boolean isAllGoodsParticipate(){
        return "1".equals(goodsType);
    }
    /**
     * 指定商品参与
     * */
    public boolean isGoodsParticipate(){
        return "3".equals(goodsType);
    }
    /**
     * 指定分类参与
     * */
    public boolean isCategoryParticipate(){
        return "2".equals(goodsType);
    }
    /**
     * 不参与商品
     * */
    public boolean isAllGoodsNotParticipate(){
        return "1".equals(noGoodsType);
    }
    /**
     * 指定商品不参与
     * */
    public boolean isGoodsNotParticipate(){
        return "3".equals(noGoodsType);
    }
    /**
     * 指定分类不参与
     * */
    public boolean isCategoryNotParticipate(){
        return "2".equals(noGoodsType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiscountCouponInfo that = (DiscountCouponInfo) o;
        return Objects.equals(storesId, that.storesId) && Objects.equals(logno, that.logno) && Objects.equals(couponId, that.couponId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storesId, logno, couponId);
    }

    @NonNull
    @Override
    public String toString() {
        return "DiscountCouponDetail{" +
                "goodsTypeValJson='" + goodsTypeValJson + '\'' + "\n" +
                ", noGoodsType='" + noGoodsType + '\'' + "\n" +
                ", endTime='" + endTime + '\'' + "\n" +
                ", storesOnline='" + storesOnline + '\'' + "\n" +
                ", storesId='" + storesId + '\'' +
                ", storesOffline='" + storesOffline + '\'' + "\n" +
                ", startTime='" + startTime + '\'' +
                ", storesValJson='" + storesValJson + '\'' + "\n" +
                ", logno='" + logno + '\'' + "\n" +
                ", status=" + status + "\n" +
                ", couponType=" + couponType + "\n" +
                ", couponId='" + couponId + '\'' + "\n" +
                ", receiveNum='" + receiveNum + '\'' + "\n" +
                ", moneyType='" + moneyType + '\'' + "\n" +
                ", couponZhe=" + couponZhe +
                ", storesType='" + storesType + '\'' + "\n" +
                ", shStatus=" + shStatus +
                ", couponName='" + couponName + '\'' + "\n" +
                ", goodsTypeVal='" + goodsTypeVal + '\'' + "\n" +
                ", moneyClass='" + moneyClass + '\'' + "\n" +
                ", description='" + description + '\'' + "\n" +
                ", receiveType=" + receiveType + "\n" +
                ", noGoodsTypeValJson='" + noGoodsTypeValJson + '\'' + "\n" +
                ", couponMoney=" + couponMoney + "\n" +
                ", noGoodsTypeVal='" + noGoodsTypeVal + '\'' + "\n" +
                ", couponCondition=" + couponCondition + "\n" +
                ", goodsType='" + goodsType + '\'' +
                '}';
    }
}
