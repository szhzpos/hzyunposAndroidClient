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
import com.wyc.cloudapp.print.printer.AbstractPrinter;
import com.wyc.cloudapp.print.receipts.RefundReceipts;
import com.wyc.cloudapp.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print.bean
 * @ClassName: RefundOrderPrintInfo
 * @Description: 退货单打印内容
 * @Author: wyc
 * @CreateDate: 2021-12-30 16:48
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-30 16:48
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class RefundOrderPrintInfo implements Serializable {

    @JSONField(name = "sales")
    private List<SalesDTO> sales;
    @JSONField(name = "stores_name")
    private String storesName;
    @JSONField(name = "region")
    private String region;
    @JSONField(name = "oper_time")
    private String operTime;
    @JSONField(name = "stores_id")
    private Integer storesId;
    @JSONField(name = "ro_code")
    private String roCode;
    @JSONField(name = "telphone")
    private String telphone;
    @JSONField(name = "pays")
    private List<PaysDTO> pays;
    @JSONField(name = "pos_num")
    private String posNum;
    @JSONField(name = "cas_name")
    private String casName;

    public List<SalesDTO> getSales() {
        return sales == null ? new ArrayList<>() : sales;
    }

    public void setSales(List<SalesDTO> sales) {
        this.sales = sales;
    }

    public String getStoresName() {
        return storesName == null ? "" : storesName;
    }

    public void setStoresName(String storesName) {
        this.storesName = storesName;
    }

    public String getRegion() {
        return region == null ? "" : region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getOperTime() {
        return operTime == null ? "" : operTime;
    }

    public void setOperTime(String operTime) {
        this.operTime = operTime;
    }

    public Integer getStoresId() {
        return storesId == null ? 0 : storesId;
    }

    public void setStoresId(Integer storesId) {
        this.storesId = storesId;
    }

    public String getRoCode() {
        return roCode == null ? "" : roCode;
    }

    public void setRoCode(String roCode) {
        this.roCode = roCode;
    }

    public String getTelphone() {
        return telphone == null ? "" : telphone;
    }

    public void setTelphone(String telphone) {
        this.telphone = telphone;
    }

    public List<PaysDTO> getPays() {
        return pays == null ? new ArrayList<>() : pays;
    }

    public void setPays(List<PaysDTO> pays) {
        this.pays = pays;
    }

    public String getPosNum() {
        return posNum == null ? "" : posNum;
    }

    public void setPosNum(String posNum) {
        this.posNum = posNum;
    }

    public String getCasName() {
        return casName == null ? "" : casName;
    }

    public void setCasName(String casName) {
        this.casName = casName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefundOrderPrintInfo that = (RefundOrderPrintInfo) o;
        return Objects.equals(storesId, that.storesId) && Objects.equals(roCode, that.roCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storesId, roCode);
    }
    @NonNull
    @Override
    public String toString() {
        return "RefundOrderPrintInfo{" +
                "sales=" + sales +
                ", storesName='" + storesName + '\'' +
                ", region='" + region + '\'' +
                ", operTime='" + operTime + '\'' +
                ", storesId=" + storesId +
                ", roCode='" + roCode + '\'' +
                ", telphone='" + telphone + '\'' +
                ", pays=" + pays +
                ", posNum='" + posNum + '\'' +
                ", casName='" + casName + '\'' +
                '}';
    }

    static public class PaysDTO {
        @JSONField(name = "pay_method_name")
        private String payMethodName;
        @JSONField(name = "pay_money")
        private Double payMoney;

        public String getPayMethodName() {
            return payMethodName == null ? "" : payMethodName;
        }

        public void setPayMethodName(String payMethodName) {
            this.payMethodName = payMethodName;
        }

        public Double getPayMoney() {
            return payMoney == null ? 0.0 : payMoney;
        }

        public void setPayMoney(Double payMoney) {
            this.payMoney = payMoney;
        }

        @NonNull
        @Override
        public String toString() {
            return "PaysDTO{" +
                    "payMethodName='" + payMethodName + '\'' +
                    ", payMoney=" + payMoney +
                    '}';
        }
    }

    static public class SalesDTO {
        @JSONField(name = "goods_title")
        private String goodsTitle;
        @JSONField(name = "refund_price")
        private Double refundPrice;
        @JSONField(name = "barcode")
        private String barcode;
        @JSONField(name = "refund_num")
        private Double refundNum;
        @JSONField(name = "type")
        private Integer type;

        public String getGoodsTitle() {
            return goodsTitle == null ? "" : goodsTitle;
        }

        public void setGoodsTitle(String goodsTitle) {
            this.goodsTitle = goodsTitle;
        }

        public Double getRefundPrice() {
            return refundPrice == null ? 0.0 : refundPrice;
        }

        public void setRefundPrice(Double refundPrice) {
            this.refundPrice = refundPrice;
        }

        public String getBarcode() {
            return barcode == null ? "" : barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public Double getRefundNum() {
            return refundNum == null ? 0.0 : refundNum;
        }

        public void setRefundNum(Double refundNum) {
            this.refundNum = refundNum;
        }

        public Integer getType() {
            return type == null ? 0 : type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SalesDTO salesDTO = (SalesDTO) o;
            return Objects.equals(goodsTitle, salesDTO.goodsTitle) && Objects.equals(barcode, salesDTO.barcode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(goodsTitle, barcode);
        }
        @NonNull
        @Override
        public String toString() {
            return "SalesDTO{" +
                    "goodsTitle='" + goodsTitle + '\'' +
                    ", refundPrice=" + refundPrice +
                    ", barcode='" + barcode + '\'' +
                    ", refundNum=" + refundNum +
                    ", type=" + type +
                    '}';
        }
    }

    public boolean isEmpty(){
        return !Utils.isNotEmpty(roCode);
    }

    public static RefundOrderPrintInfo getInstance (final String refund_code){
        final JSONObject order_info = new JSONObject();
        if (SQLiteHelper.execSql(order_info,"SELECT a.ro_code,b.cas_name,a.pos_code pos_num,a.stores_id,c.stores_name,datetime(a.addtime, 'unixepoch', 'localtime') oper_time,c.telphone,c.region" +
                " FROM refund_order a  left join cashier_info b on a.cashier_id = b.cas_id\n" +
                "left join shop_stores c on a.stores_id = c.stores_id where a.ro_code = '" +refund_code +"'")) {

            final StringBuilder err = new StringBuilder();
            final String goods_info_sql = "SELECT b.barcode,b.goods_title,b.type,a.refund_num,a.refund_price FROM refund_order_goods a left join barcode_info b on a.barcode_id = b.barcode_id where ro_code = '" + refund_code + "'", pays_info_sql = "SELECT pay_method_name,pay_money FROM refund_order_pays where ro_code = '" + refund_code + "'";
            final JSONArray sales = SQLiteHelper.getListToJson(goods_info_sql, err), pays = SQLiteHelper.getListToJson(pays_info_sql, err);
            if (sales != null && pays != null) {
                order_info.put("sales", sales);
                order_info.put("pays", pays);

                return order_info.toJavaObject(RefundOrderPrintInfo.class);
            }else {
                MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.l_p_c_err_hint_sz,order_info.getString("info")));
            }
        }
        return null;
    }
}
