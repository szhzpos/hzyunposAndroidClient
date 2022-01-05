package com.wyc.cloudapp.print.bean;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.print.printer.AbstractPrinter;
import com.wyc.cloudapp.print.receipts.VipRechargeReceipts;

import java.io.Serializable;
import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print.bean
 * @ClassName: VipChargeOrderPrint
 * @Description: 会员充值打印内容
 * @Author: wyc
 * @CreateDate: 2021-12-30 18:17
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-30 18:17
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */

public class VipChargeOrderPrintInfo{

    @JSONField(name = "member")
    private List<MemberDTO> member;
    @JSONField(name = "welfare")
    private List<String> welfare;
    @JSONField(name = "status")
    private String status;
    @JSONField(name = "info")
    private String info;
    @JSONField(name = "money_order")
    private List<MoneyOrderDTO> moneyOrder;
    @JSONField(name = "money_sum")
    private Double moneySum;

    public List<MemberDTO> getMember() {
        return member;
    }

    public void setMember(List<MemberDTO> member) {
        this.member = member;
    }

    public List<String> getWelfare() {
        return welfare;
    }

    public void setWelfare(List<String> welfare) {
        this.welfare = welfare;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public List<MoneyOrderDTO> getMoneyOrder() {
        return moneyOrder;
    }

    public void setMoneyOrder(List<MoneyOrderDTO> moneyOrder) {
        this.moneyOrder = moneyOrder;
    }

    public Double getMoneySum() {
        return moneySum;
    }

    public void setMoneySum(Double moneySum) {
        this.moneySum = moneySum;
    }

    @Override
    public String toString() {
        return "VipChargeOrderPrint{" +
                "member=" + member +
                ", welfare=" + welfare +
                ", status='" + status + '\'' +
                ", info='" + info + '\'' +
                ", moneyOrder=" + moneyOrder +
                ", moneySum=" + moneySum +
                '}';
    }

    public static class MoneyOrderDTO {
        @JSONField(name = "xnote")
        private String xnote;
        @JSONField(name = "pay_method_name")
        private String payMethodName;
        @JSONField(name = "order_code")
        private String orderCode;
        @JSONField(name = "order_money")
        private Double orderMoney;
        @JSONField(name = "pay_method")
        private String payMethod;
        @JSONField(name = "stores_id")
        private String storesId;
        @JSONField(name = "order_id")
        private String orderId;
        @JSONField(name = "give_money")
        private Double giveMoney;
        @JSONField(name = "hand_give_money")
        private Double handGiveMoney;
        @JSONField(name = "source_order_code")
        private String sourceOrderCode;
        @JSONField(name = "member_id")
        private String memberId;
        @JSONField(name = "addtime")
        private String addtime;
        @JSONField(name = "status")
        private String status;
        @JSONField(name = "sc_ids")
        private String scIds;
        @JSONField(name = "cashier_id")
        private String cashierId;
        @JSONField(name = "sc_tc_money")
        private String scTcMoney;

        public String getXnote() {
            return xnote;
        }

        public void setXnote(String xnote) {
            this.xnote = xnote;
        }

        public String getPayMethodName() {
            return payMethodName;
        }

        public void setPayMethodName(String payMethodName) {
            this.payMethodName = payMethodName;
        }

        public String getOrderCode() {
            return orderCode;
        }

        public void setOrderCode(String orderCode) {
            this.orderCode = orderCode;
        }

        public Double getOrderMoney() {
            return orderMoney;
        }

        public void setOrderMoney(Double orderMoney) {
            this.orderMoney = orderMoney;
        }

        public String getPayMethod() {
            return payMethod;
        }

        public void setPayMethod(String payMethod) {
            this.payMethod = payMethod;
        }

        public String getStoresId() {
            return storesId;
        }

        public void setStoresId(String storesId) {
            this.storesId = storesId;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }

        public Double getGiveMoney() {
            return giveMoney;
        }

        public void setGiveMoney(Double giveMoney) {
            this.giveMoney = giveMoney;
        }

        public Double getHandGiveMoney() {
            return handGiveMoney;
        }

        public void setHandGiveMoney(Double handGiveMoney) {
            this.handGiveMoney = handGiveMoney;
        }

        public String getSourceOrderCode() {
            return sourceOrderCode;
        }

        public void setSourceOrderCode(String sourceOrderCode) {
            this.sourceOrderCode = sourceOrderCode;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getAddtime() {
            return addtime;
        }

        public void setAddtime(String addtime) {
            this.addtime = addtime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getScIds() {
            return scIds;
        }

        public void setScIds(String scIds) {
            this.scIds = scIds;
        }

        public String getCashierId() {
            return cashierId;
        }

        public void setCashierId(String cashierId) {
            this.cashierId = cashierId;
        }

        public String getScTcMoney() {
            return scTcMoney;
        }

        public void setScTcMoney(String scTcMoney) {
            this.scTcMoney = scTcMoney;
        }

        @Override
        public String toString() {
            return "MoneyOrderDTO{" +
                    "xnote='" + xnote + '\'' +
                    ", payMethodName='" + payMethodName + '\'' +
                    ", orderCode='" + orderCode + '\'' +
                    ", orderMoney=" + orderMoney +
                    ", payMethod='" + payMethod + '\'' +
                    ", storesId='" + storesId + '\'' +
                    ", orderId='" + orderId + '\'' +
                    ", giveMoney=" + giveMoney +
                    ", handGiveMoney=" + handGiveMoney +
                    ", sourceOrderCode='" + sourceOrderCode + '\'' +
                    ", memberId='" + memberId + '\'' +
                    ", addtime='" + addtime + '\'' +
                    ", status='" + status + '\'' +
                    ", scIds='" + scIds + '\'' +
                    ", cashierId='" + cashierId + '\'' +
                    ", scTcMoney='" + scTcMoney + '\'' +
                    '}';
        }
    }

    static public class MemberDTO {
        @JSONField(name = "upgrade_lock")
        private String upgradeLock;
        @JSONField(name = "points_sum")
        private Double pointsSum;
        @JSONField(name = "head_img_id")
        private String headImgId;
        @JSONField(name = "birthday_type")
        private String birthdayType;
        @JSONField(name = "sc_id")
        private String scId;
        @JSONField(name = "card_code")
        private String cardCode;
        @JSONField(name = "discount")
        private Double discount;
        @JSONField(name = "idcard")
        private String idcard;
        @JSONField(name = "mm_type")
        private String mmType;
        @JSONField(name = "upgrade_points")
        private Double upgradePoints;
        @JSONField(name = "mobile")
        private String mobile;
        @JSONField(name = "pay_pwd")
        private String payPwd;
        @JSONField(name = "birthday")
        private String birthday;
        @JSONField(name = "openid")
        private String openid;
        @JSONField(name = "member_id")
        private String memberId;
        @JSONField(name = "grade_id")
        private String gradeId;
        @JSONField(name = "remarks")
        private String remarks;
        @JSONField(name = "addtime")
        private String addtime;
        @JSONField(name = "status")
        private String status;
        @JSONField(name = "money_credit_limit")
        private String moneyCreditLimit;
        @JSONField(name = "ref_member_id")
        private String refMemberId;
        @JSONField(name = "relegated_time")
        private String relegatedTime;
        @JSONField(name = "name")
        private String name;
        @JSONField(name = "login_pwd")
        private String loginPwd;
        @JSONField(name = "sex")
        private String sex;
        @JSONField(name = "money_sum")
        private Double moneySum;

        public String getUpgradeLock() {
            return upgradeLock;
        }

        public void setUpgradeLock(String upgradeLock) {
            this.upgradeLock = upgradeLock;
        }

        public Double getPointsSum() {
            return pointsSum;
        }

        public void setPointsSum(Double pointsSum) {
            this.pointsSum = pointsSum;
        }

        public String getHeadImgId() {
            return headImgId;
        }

        public void setHeadImgId(String headImgId) {
            this.headImgId = headImgId;
        }

        public String getBirthdayType() {
            return birthdayType;
        }

        public void setBirthdayType(String birthdayType) {
            this.birthdayType = birthdayType;
        }

        public String getScId() {
            return scId;
        }

        public void setScId(String scId) {
            this.scId = scId;
        }

        public String getCardCode() {
            return cardCode;
        }

        public void setCardCode(String cardCode) {
            this.cardCode = cardCode;
        }

        public Double getDiscount() {
            return discount;
        }

        public void setDiscount(Double discount) {
            this.discount = discount;
        }

        public String getIdcard() {
            return idcard;
        }

        public void setIdcard(String idcard) {
            this.idcard = idcard;
        }

        public String getMmType() {
            return mmType;
        }

        public void setMmType(String mmType) {
            this.mmType = mmType;
        }

        public Double getUpgradePoints() {
            return upgradePoints;
        }

        public void setUpgradePoints(Double upgradePoints) {
            this.upgradePoints = upgradePoints;
        }

        public String getMobile() {
            return mobile;
        }

        public void setMobile(String mobile) {
            this.mobile = mobile;
        }

        public String getPayPwd() {
            return payPwd;
        }

        public void setPayPwd(String payPwd) {
            this.payPwd = payPwd;
        }

        public String getBirthday() {
            return birthday;
        }

        public void setBirthday(String birthday) {
            this.birthday = birthday;
        }

        public String getOpenid() {
            return openid;
        }

        public void setOpenid(String openid) {
            this.openid = openid;
        }

        public String getMemberId() {
            return memberId;
        }

        public void setMemberId(String memberId) {
            this.memberId = memberId;
        }

        public String getGradeId() {
            return gradeId;
        }

        public void setGradeId(String gradeId) {
            this.gradeId = gradeId;
        }

        public String getRemarks() {
            return remarks;
        }

        public void setRemarks(String remarks) {
            this.remarks = remarks;
        }

        public String getAddtime() {
            return addtime;
        }

        public void setAddtime(String addtime) {
            this.addtime = addtime;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMoneyCreditLimit() {
            return moneyCreditLimit;
        }

        public void setMoneyCreditLimit(String moneyCreditLimit) {
            this.moneyCreditLimit = moneyCreditLimit;
        }

        public String getRefMemberId() {
            return refMemberId;
        }

        public void setRefMemberId(String refMemberId) {
            this.refMemberId = refMemberId;
        }

        public String getRelegatedTime() {
            return relegatedTime;
        }

        public void setRelegatedTime(String relegatedTime) {
            this.relegatedTime = relegatedTime;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getLoginPwd() {
            return loginPwd;
        }

        public void setLoginPwd(String loginPwd) {
            this.loginPwd = loginPwd;
        }

        public String getSex() {
            return sex;
        }

        public void setSex(String sex) {
            this.sex = sex;
        }

        public Double getMoneySum() {
            return moneySum;
        }

        public void setMoneySum(Double moneySum) {
            this.moneySum = moneySum;
        }

        @Override
        public String toString() {
            return "MemberDTO{" +
                    "upgradeLock='" + upgradeLock + '\'' +
                    ", pointsSum=" + pointsSum +
                    ", headImgId='" + headImgId + '\'' +
                    ", birthdayType='" + birthdayType + '\'' +
                    ", scId='" + scId + '\'' +
                    ", cardCode='" + cardCode + '\'' +
                    ", discount=" + discount +
                    ", idcard='" + idcard + '\'' +
                    ", mmType='" + mmType + '\'' +
                    ", upgradePoints=" + upgradePoints +
                    ", mobile='" + mobile + '\'' +
                    ", payPwd='" + payPwd + '\'' +
                    ", birthday='" + birthday + '\'' +
                    ", openid='" + openid + '\'' +
                    ", memberId='" + memberId + '\'' +
                    ", gradeId='" + gradeId + '\'' +
                    ", remarks='" + remarks + '\'' +
                    ", addtime='" + addtime + '\'' +
                    ", status='" + status + '\'' +
                    ", moneyCreditLimit='" + moneyCreditLimit + '\'' +
                    ", refMemberId='" + refMemberId + '\'' +
                    ", relegatedTime='" + relegatedTime + '\'' +
                    ", name='" + name + '\'' +
                    ", loginPwd='" + loginPwd + '\'' +
                    ", sex='" + sex + '\'' +
                    ", moneySum=" + moneySum +
                    '}';
        }
    }
    public static JSONObject getInstance(final String order_code){
        final JSONObject xnote = new JSONObject();
        if (SQLiteHelper.execSql(xnote,"SELECT xnote, b.cas_name ,c.stores_name,c.telphone,c.region\n" +
                "FROM member_order_info a  left join cashier_info b on a.cashier_id = b.cas_id left join shop_stores c on a.stores_id = c.stores_id where order_code = '" + order_code + "'")){
            try {
                final JSONObject obj = JSON.parseObject(xnote.getString("xnote"));
                if (obj != null){
                    obj.put("stores_name",xnote.getString("stores_name"));
                    obj.put("cas_name",xnote.getString("cas_name"));
                    obj.put("telphone",xnote.getString("telphone"));
                    obj.put("region",xnote.getString("region"));
                }
                return obj;
            }catch (JSONException e){
                MyDialog.toastMessage(e.getMessage());
            }
        }
        return null;
    }
}
