package com.wyc.cloudapp.print.bean;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print.bean
 * @ClassName: TransferOrderPrintInfo
 * @Description: 交班单打印内容
 * @Author: wyc
 * @CreateDate: 2021-12-31 13:45
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-31 13:45
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */

public class TransferOrderPrintInfo{

    @JSONField(name = "cards_money")
    private Double cardsMoney;
    @JSONField(name = "cards_num")
    private Double cardsNum;
    @JSONField(name = "region")
    private String region;
    @JSONField(name = "cas_id")
    private String casId;
    @JSONField(name = "stores_id")
    private String storesId;
    @JSONField(name = "recharge_moneys")
    private List<RechargeMoneysDTO> rechargeMoneys;
    @JSONField(name = "retail_moneys")
    private List<RetailMoneysDTO> retailMoneys;
    @JSONField(name = "oncecard_moneys")
    private List<OncecardMoneysDTO> oncecardMoneys;
    @JSONField(name = "recharge_num")
    private Double rechargeNum;
    @JSONField(name = "sj_money")
    private Double sjMoney;
    @JSONField(name = "shopping_money")
    private Double shoppingMoney;
    @JSONField(name = "refund_moneys")
    private List<RefundMoneysDTO> refundMoneys;
    @JSONField(name = "ti_code")
    private String tiCode;
    @JSONField(name = "stores_name")
    private String storesName;
    @JSONField(name = "order_num")
    private Integer orderNum;
    @JSONField(name = "transfer_time")
    private String transferTime;
    @JSONField(name = "order_b_date")
    private String orderBDate;
    @JSONField(name = "order_e_date")
    private String orderEDate;
    @JSONField(name = "order_money")
    private Double orderMoney;
    @JSONField(name = "shopping_num")
    private Double shoppingNum;
    @JSONField(name = "cashbox_money")
    private Double cashboxMoney;
    @JSONField(name = "telphone")
    private String telphone;
    @JSONField(name = "refund_money")
    private Double refundMoney;
    @JSONField(name = "cas_name")
    private String casName;
    @JSONField(name = "sum_money")
    private Double sumMoney;
    @JSONField(name = "recharge_money")
    private Double rechargeMoney;
    @JSONField(name = "refund_num")
    private Double refundNum;
    @JSONField(name = "shopping_moneys")
    private List<ShoppingMoneysDTO> shoppingMoneys;

    public Double getCardsMoney() {
        return cardsMoney == null ? 0.0 : cardsMoney;
    }

    public void setCardsMoney(Double cardsMoney) {
        this.cardsMoney = cardsMoney;
    }

    public Double getCardsNum() {
        return cardsNum == null ? 0.0 : cardsNum;
    }

    public void setCardsNum(Double cardsNum) {
        this.cardsNum = cardsNum;
    }

    public String getRegion() {
        return region == null ? "" : region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCasId() {
        return casId == null ? "" : casId;
    }

    public void setCasId(String casId) {
        this.casId = casId;
    }

    public String getStoresId() {
        return storesId == null ? "" : storesId;
    }

    public void setStoresId(String storesId) {
        this.storesId = storesId;
    }

    public List<RechargeMoneysDTO> getRechargeMoneys() {
        return rechargeMoneys == null ? new ArrayList<>() : rechargeMoneys;
    }

    public void setRechargeMoneys(List<RechargeMoneysDTO> rechargeMoneys) {
        this.rechargeMoneys = rechargeMoneys;
    }

    public List<RetailMoneysDTO> getRetailMoneys() {
        return retailMoneys == null ? new ArrayList<>() : retailMoneys;
    }

    public void setRetailMoneys(List<RetailMoneysDTO> retailMoneys) {
        this.retailMoneys = retailMoneys;
    }

    public List<OncecardMoneysDTO> getOncecardMoneys() {
        return oncecardMoneys == null ? new ArrayList<>() : oncecardMoneys;
    }

    public void setOncecardMoneys(List<OncecardMoneysDTO> oncecardMoneys) {
        this.oncecardMoneys = oncecardMoneys;
    }

    public Double getRechargeNum() {
        return rechargeNum == null ? 0.0 : rechargeNum;
    }

    public void setRechargeNum(Double rechargeNum) {
        this.rechargeNum = rechargeNum;
    }

    public Double getSjMoney() {
        return sjMoney == null ? 0.0 : sjMoney;
    }

    public void setSjMoney(Double sjMoney) {
        this.sjMoney = sjMoney;
    }

    public Double getShoppingMoney() {
        return shoppingMoney == null ? 0.0 : shoppingMoney;
    }

    public void setShoppingMoney(Double shoppingMoney) {
        this.shoppingMoney = shoppingMoney;
    }

    public List<RefundMoneysDTO> getRefundMoneys() {
        return refundMoneys == null ? new ArrayList<>() : refundMoneys;
    }

    public void setRefundMoneys(List<RefundMoneysDTO> refundMoneys) {
        this.refundMoneys = refundMoneys;
    }

    public String getTiCode() {
        return tiCode == null ? "" : tiCode;
    }

    public void setTiCode(String tiCode) {
        this.tiCode = tiCode;
    }

    public String getStoresName() {
        return storesName == null ? "" : storesName;
    }

    public void setStoresName(String storesName) {
        this.storesName = storesName;
    }

    public Integer getOrderNum() {
        return orderNum == null ? 0 : orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }

    public String getTransferTime() {
        return transferTime == null ? "" : transferTime;
    }

    public void setTransferTime(String transferTime) {
        this.transferTime = transferTime;
    }

    public String getOrderBDate() {
        return orderBDate == null ? "" : orderBDate;
    }

    public void setOrderBDate(String orderBDate) {
        this.orderBDate = orderBDate;
    }

    public String getOrderEDate() {
        return orderEDate == null ? "" : orderEDate;
    }

    public void setOrderEDate(String orderEDate) {
        this.orderEDate = orderEDate;
    }

    public Double getOrderMoney() {
        return orderMoney == null ? 0.0 : orderMoney;
    }

    public void setOrderMoney(Double orderMoney) {
        this.orderMoney = orderMoney;
    }

    public Double getShoppingNum() {
        return shoppingNum == null ? 0.0 : shoppingNum;
    }

    public void setShoppingNum(Double shoppingNum) {
        this.shoppingNum = shoppingNum;
    }

    public Double getCashboxMoney() {
        return cashboxMoney == null ? 0.0 : cashboxMoney;
    }

    public void setCashboxMoney(Double cashboxMoney) {
        this.cashboxMoney = cashboxMoney;
    }

    public String getTelphone() {
        return telphone == null ? "" : telphone;
    }

    public void setTelphone(String telphone) {
        this.telphone = telphone;
    }

    public Double getRefundMoney() {
        return refundMoney == null ? 0.0 : refundMoney;
    }

    public void setRefundMoney(Double refundMoney) {
        this.refundMoney = refundMoney;
    }

    public String getCasName() {
        return casName == null ? "" : casName;
    }

    public void setCasName(String casName) {
        this.casName = casName;
    }

    public Double getSumMoney() {
        return sumMoney == null ? 0.0 : sumMoney;
    }

    public void setSumMoney(Double sumMoney) {
        this.sumMoney = sumMoney;
    }

    public Double getRechargeMoney() {
        return rechargeMoney == null ? 0.0 : rechargeMoney;
    }

    public void setRechargeMoney(Double rechargeMoney) {
        this.rechargeMoney = rechargeMoney;
    }

    public Double getRefundNum() {
        return refundNum == null ? 0.0 : refundNum;
    }

    public void setRefundNum(Double refundNum) {
        this.refundNum = refundNum;
    }

    public List<ShoppingMoneysDTO> getShoppingMoneys() {
        return shoppingMoneys == null ? new ArrayList<>() : shoppingMoneys;
    }

    public void setShoppingMoneys(List<ShoppingMoneysDTO> shoppingMoneys) {
        this.shoppingMoneys = shoppingMoneys;
    }

    public boolean isEmpty(){
        return !Utils.isNotEmpty(tiCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransferOrderPrintInfo that = (TransferOrderPrintInfo) o;
        return Objects.equals(casId, that.casId) && Objects.equals(storesId, that.storesId) && Objects.equals(tiCode, that.tiCode);
    }
    @Override
    public int hashCode() {
        return Objects.hash(casId, storesId, tiCode);
    }
    @NonNull
    @Override
    public String toString() {
        return "TransferOrderPrintInfo{" +
                "cardsMoney=" + cardsMoney +
                ", cardsNum=" + cardsNum +
                ", region='" + region + '\'' +
                ", casId='" + casId + '\'' +
                ", storesId='" + storesId + '\'' +
                ", rechargeMoneys=" + rechargeMoneys +
                ", retailMoneys=" + retailMoneys +
                ", oncecardMoneys=" + oncecardMoneys +
                ", rechargeNum=" + rechargeNum +
                ", sjMoney=" + sjMoney +
                ", shoppingMoney=" + shoppingMoney +
                ", refundMoneys=" + refundMoneys +
                ", tiCode='" + tiCode + '\'' +
                ", storesName='" + storesName + '\'' +
                ", orderNum=" + orderNum +
                ", transferTime='" + transferTime + '\'' +
                ", orderBDate='" + orderBDate + '\'' +
                ", orderEDate='" + orderEDate + '\'' +
                ", orderMoney=" + orderMoney +
                ", shoppingNum=" + shoppingNum +
                ", cashboxMoney=" + cashboxMoney +
                ", telphone='" + telphone + '\'' +
                ", refundMoney=" + refundMoney +
                ", casName='" + casName + '\'' +
                ", sumMoney=" + sumMoney +
                ", rechargeMoney=" + rechargeMoney +
                ", refundNum=" + refundNum +
                ", shoppingMoneys=" + shoppingMoneys +
                '}';
    }


    public static class ShoppingMoneysDTO {
        @JSONField(name = "order_num")
        private Integer orderNum;
        @JSONField(name = "pay_money")
        private Double payMoney;
        @JSONField(name = "name")
        private String name;

        public Integer getOrderNum() {
            return orderNum == null ? 0 : orderNum;
        }

        public void setOrderNum(Integer orderNum) {
            this.orderNum = orderNum;
        }

        public Double getPayMoney() {
            return payMoney == null ? 0.0 : payMoney;
        }

        public void setPayMoney(Double payMoney) {
            this.payMoney = payMoney;
        }

        public String getName() {
            return name == null ? "" : name;
        }

        public void setName(String name) {
            this.name = name;
        }
        @NonNull
        @Override
        public String toString() {
            return "ShoppingMoneysDTO{" +
                    "orderNum=" + orderNum +
                    ", payMoney=" + payMoney +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
    public static class RetailMoneysDTO {
        @JSONField(name = "order_num")
        private Integer orderNum;
        @JSONField(name = "pay_money")
        private Double payMoney;
        @JSONField(name = "name")
        private String name;
        public Integer getOrderNum() {
            return orderNum == null ? 0 : orderNum;
        }

        public void setOrderNum(Integer orderNum) {
            this.orderNum = orderNum;
        }

        public Double getPayMoney() {
            return payMoney == null ? 0.0 : payMoney;
        }

        public void setPayMoney(Double payMoney) {
            this.payMoney = payMoney;
        }

        public String getName() {
            return name == null ? "" : name;
        }

        public void setName(String name) {
            this.name = name;
        }
        @NonNull
        @Override
        public String toString() {
            return "ShoppingMoneysDTO{" +
                    "orderNum=" + orderNum +
                    ", payMoney=" + payMoney +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
    public static class RefundMoneysDTO {
        @JSONField(name = "order_num")
        private Integer orderNum;
        @JSONField(name = "pay_money")
        private Double payMoney;
        @JSONField(name = "name")
        private String name;

        public Integer getOrderNum() {
            return orderNum == null ? 0 : orderNum;
        }

        public void setOrderNum(Integer orderNum) {
            this.orderNum = orderNum;
        }

        public Double getPayMoney() {
            return payMoney == null ? 0.0 : payMoney;
        }

        public void setPayMoney(Double payMoney) {
            this.payMoney = payMoney;
        }

        public String getName() {
            return name == null ? "" : name;
        }

        public void setName(String name) {
            this.name = name;
        }
        @NonNull
        @Override
        public String toString() {
            return "ShoppingMoneysDTO{" +
                    "orderNum=" + orderNum +
                    ", payMoney=" + payMoney +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
    public static class RechargeMoneysDTO {
        @JSONField(name = "order_num")
        private Integer orderNum;
        @JSONField(name = "pay_money")
        private Double payMoney;
        @JSONField(name = "name")
        private String name;

        public Integer getOrderNum() {
            return orderNum == null ? 0 : orderNum;
        }

        public void setOrderNum(Integer orderNum) {
            this.orderNum = orderNum;
        }

        public Double getPayMoney() {
            return payMoney == null ? 0.0 : payMoney;
        }

        public void setPayMoney(Double payMoney) {
            this.payMoney = payMoney;
        }

        public String getName() {
            return name == null ? "" : name;
        }

        public void setName(String name) {
            this.name = name;
        }
        @NonNull
        @Override
        public String toString() {
            return "ShoppingMoneysDTO{" +
                    "orderNum=" + orderNum +
                    ", payMoney=" + payMoney +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
    public static class OncecardMoneysDTO {
        @JSONField(name = "order_num")
        private Integer orderNum;
        @JSONField(name = "pay_money")
        private Double payMoney;
        @JSONField(name = "name")
        private String name;

        public Integer getOrderNum() {
            return orderNum == null ? 0 : orderNum;
        }

        public void setOrderNum(Integer orderNum) {
            this.orderNum = orderNum;
        }

        public Double getPayMoney() {
            return payMoney == null ? 0.0 : payMoney;
        }

        public void setPayMoney(Double payMoney) {
            this.payMoney = payMoney;
        }

        public String getName() {
            return name == null ? "" : name;
        }

        public void setName(String name) {
            this.name = name;
        }
        @NonNull
        @Override
        public String toString() {
            return "ShoppingMoneysDTO{" +
                    "orderNum=" + orderNum +
                    ", payMoney=" + payMoney +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public static TransferOrderPrintInfo getInstance(final String ti_code) {
        final String details_where_sql = " where ti_code = '"+ ti_code +"'",
                transfer_sum_sql = "SELECT a.shopping_num,a.shopping_money,a.sj_money,a.cards_num,a.cards_money,a.order_money,datetime(a.order_e_date, 'unixepoch', 'localtime') order_e_date,datetime(a.order_b_date, 'unixepoch', 'localtime') order_b_date" +
                        ",a.recharge_num,a.recharge_money,a.refund_num, a.refund_money,a.cashbox_money,a.sum_money,a.ti_code,datetime(a.transfer_time, 'unixepoch', 'localtime') transfer_time,a.order_num," +
                        "a.cas_id,a.stores_id,b.stores_name,b.telphone,b.region,c.cas_name FROM transfer_info a inner join shop_stores b on a.stores_id = b.stores_id inner join cashier_info c on a.cas_id = c.cas_id" + details_where_sql,
                transfer_retails_sql = "SELECT a.order_num,b.name,a.pay_money FROM transfer_money_info a left join pay_method b on a.pay_method = b.pay_method_id" + details_where_sql,
                transfer_cards_sql = "SELECT a.order_num,b.name,a.pay_money  FROM transfer_once_cardsc a left join pay_method b on a.pay_method = b.pay_method_id" + details_where_sql,
                transfer_gift_sql = "SELECT a.order_num,b.name,a.pay_money  FROM transfer_gift_money a left join pay_method b on a.pay_method = b.pay_method_id" + details_where_sql,
                transfer_recharge_sql = "SELECT a.order_num,b.name,a.pay_money  FROM transfer_recharge_money a left join pay_method b on a.pay_method = b.pay_method_id" + details_where_sql,
                transfer_refund_sql = "SELECT a.order_num,b.name,a.pay_money FROM transfer_refund_money a left join pay_method b on a.pay_method = b.pay_method_id" + details_where_sql;

        final JSONObject order_info = new JSONObject();
        if (SQLiteHelper.execSql(order_info,transfer_sum_sql)){
            final StringBuilder err = new StringBuilder();

            final JSONArray transfer_retails_arr = SQLiteHelper.getListToJson(transfer_retails_sql,err),transfer_cards_arr = SQLiteHelper.getListToJson(transfer_cards_sql,err),
                    transfer_recharge_arr = SQLiteHelper.getListToJson(transfer_recharge_sql,err),transfer_refund_arr = SQLiteHelper.getListToJson(transfer_refund_sql,err),
                    gift_arr = SQLiteHelper.getListToJson(transfer_gift_sql,err);

            if (null != transfer_retails_arr && transfer_cards_arr != null && transfer_recharge_arr != null && transfer_refund_arr != null && null != gift_arr){
                order_info.put("retail_moneys",transfer_retails_arr);
                order_info.put("refund_moneys",transfer_refund_arr);
                order_info.put("recharge_moneys",transfer_recharge_arr);
                order_info.put("oncecard_moneys",transfer_cards_arr);
                order_info.put("shopping_moneys",gift_arr);

                return order_info.toJavaObject(TransferOrderPrintInfo.class);
            }else {
                MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.l_p_c_err_hint_sz,err));
            }
        }else MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.l_p_c_err_hint_sz,order_info.getString("info")));
        return null;
    }
}
