package com.wyc.cloudapp.print.bean;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
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
 * @ClassName: SaleOrderPrintInfo
 * @Description: 结账单打印内容
 * @Author: wyc
 * @CreateDate: 2021-12-28 12:53
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-28 12:53
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class SaleOrderPrintInfo{

    @JSONField(name = "sales")
    private List<SalesDTO> sales;
    @JSONField(name = "stores_name")
    private String storesName;
    @JSONField(name = "card_code")
    private String cardCode;
    @JSONField(name = "order_code")
    private String orderCode;
    @JSONField(name = "region")
    private String region;
    @JSONField(name = "stores_id")
    private Integer storesId;
    @JSONField(name = "telphone")
    private String telphone;
    @JSONField(name = "cas_name")
    private String casName;
    @JSONField(name = "pos_num")
    private String posNum;
    @JSONField(name = "oper_time")
    private String operTime;
    @JSONField(name = "vip_name")
    private String vipName;
    @JSONField(name = "integral_info")
    private String integralInfo;
    @JSONField(name = "pays")
    private List<PaysDTO> pays;

    public List<SalesDTO> getSales() {
        return sales == null ? new ArrayList<>() : sales;
    }

    public void setSales(List<SalesDTO> sales) {
        this.sales = sales;
    }

    public String getStoresName() {
        return storesName;
    }

    public void setStoresName(String storesName) {
        this.storesName = storesName;
    }

    public String getCardCode() {
        return cardCode == null ? "" : cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getRegion() {
        return region == null ? "" : region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public Integer getStoresId() {
        return storesId == null ? 0 : storesId;
    }

    public void setStoresId(Integer storesId) {
        this.storesId = storesId;
    }

    public String getTelphone() {
        return telphone == null ? "" : telphone;
    }

    public void setTelphone(String telphone) {
        this.telphone = telphone;
    }

    public String getCasName() {
        return casName;
    }

    public void setCasName(String casName) {
        this.casName = casName;
    }

    public String getPosNum() {
        return posNum;
    }

    public void setPosNum(String posNum) {
        this.posNum = posNum;
    }

    public String getOperTime() {
        return operTime;
    }

    public void setOperTime(String operTime) {
        this.operTime = operTime;
    }

    public String getVipName() {
        return vipName == null ? "" : vipName;
    }

    public void setVipName(String vipName) {
        this.vipName = vipName;
    }

    public VipIntegralInfo getIntegralInfoObj() {
        return JSON.parseObject(integralInfo,VipIntegralInfo.class);
    }

    public String getIntegralInfo() {
        return integralInfo;
    }

    public void setIntegralInfo(String integralInfo) {
        this.integralInfo = integralInfo;
    }

    public List<PaysDTO> getPays() {
        return pays == null ? new ArrayList<>() : pays;
    }

    public void setPays(List<PaysDTO> pays) {
        this.pays = pays;
    }

    public boolean isEmpty(){
        return !Utils.isNotEmpty(orderCode);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SaleOrderPrintInfo that = (SaleOrderPrintInfo) o;
        return Objects.equals(orderCode, that.orderCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderCode);
    }

    @NonNull
    @Override
    public String toString() {
        return "SaleOrderPrintInfo{" +
                "sales=" + sales +
                ", storesName='" + storesName + '\'' +
                ", cardCode='" + cardCode + '\'' +
                ", orderCode='" + orderCode + '\'' +
                ", region='" + region + '\'' +
                ", storesId=" + storesId +
                ", telphone='" + telphone + '\'' +
                ", casName='" + casName + '\'' +
                ", posNum='" + posNum + '\'' +
                ", operTime='" + operTime + '\'' +
                ", vipName='" + vipName + '\'' +
                ", integralInfo='" + integralInfo + '\'' +
                ", pays=" + pays +
                '}';
    }

    public static class SalesDTO {
        @JSONField(name = "goods_title")
        private String goodsTitle;
        @JSONField(name = "price")
        private Double price;
        @JSONField(name = "original_price")
        private Double originalPrice;
        @JSONField(name = "goodsPractice")
        private String goodsPractice;
        @JSONField(name = "discount_amt")
        private Double discountAmt;
        @JSONField(name = "sale_amt")
        private Double saleAmt;
        @JSONField(name = "barcode")
        private String barcode;
        @JSONField(name = "original_amt")
        private Double originalAmt;
        @JSONField(name = "type")
        private Integer type;
        @JSONField(name = "xnum")
        private Double xnum;

        public String getGoodsTitle() {
            return goodsTitle == null ? "" : goodsTitle;
        }

        public void setGoodsTitle(String goodsTitle) {
            this.goodsTitle = goodsTitle;
        }

        public Double getPrice() {
            return price == null ? 0.0 : price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Double getOriginalPrice() {
            return originalPrice;
        }

        public void setOriginalPrice(Double originalPrice) {
            this.originalPrice = originalPrice;
        }

        public @NonNull List<GoodsPracticeInfo> getGoodsPracticeList() {
            final List<GoodsPracticeInfo> list = JSONArray.parseArray(getGoodsPractice(),GoodsPracticeInfo.class);
            return list == null ? new ArrayList<>() : list;
        }

        public String getGoodsPractice() {
            return goodsPractice;
        }

        public void setGoodsPractice(String goodsPractice) {
            this.goodsPractice = goodsPractice;
        }

        public Double getDiscountAmt() {
            return discountAmt == null ? 0.0 : discountAmt;
        }

        public void setDiscountAmt(Double discountAmt) {
            this.discountAmt = discountAmt;
        }

        public Double getSaleAmt() {
            return saleAmt == null ? 0.0 : saleAmt;
        }

        public void setSaleAmt(Double saleAmt) {
            this.saleAmt = saleAmt;
        }

        public String getBarcode() {
            return barcode == null ? "" : barcode;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public Double getOriginalAmt() {
            return originalAmt == null ? 0.00 : originalAmt;
        }

        public void setOriginalAmt(Double originalAmt) {
            this.originalAmt = originalAmt;
        }

        public Integer getType() {
            return type;
        }

        public void setType(Integer type) {
            this.type = type;
        }

        public Double getXnum() {
            return xnum == null ? 0.0 : xnum;
        }

        public void setXnum(Double xnum) {
            this.xnum = xnum;
        }

        @NonNull
        @Override
        public String toString() {
            return "SalesDTO{" +
                    "goodsTitle='" + goodsTitle + '\'' +
                    ", price=" + price +
                    ", originalPrice=" + originalPrice +
                    ", goodsPractice='" + goodsPractice + '\'' +
                    ", discountAmt=" + discountAmt +
                    ", saleAmt=" + saleAmt +
                    ", barcode='" + barcode + '\'' +
                    ", originalAmt=" + originalAmt +
                    ", type=" + type +
                    ", xnum=" + xnum +
                    '}';
        }
    }
    public static class PaysDTO {
        @JSONField(name = "xnote")
        private String xnote;
        @JSONField(name = "pzl")
        private Double pzl;
        @JSONField(name = "pamt")
        private Double pamt;
        @JSONField(name = "name")
        private String name;

        public List<String> getXnoteList(){
            List<String> list = JSONArray.parseArray(getXnote(),String.class);
            return list == null ? new ArrayList<>() : list;
        }
        public String getXnote() {
            return xnote == null ? "[]" : xnote;
        }

        public void setXnote(String xnote) {
            this.xnote = xnote;
        }

        public Double getPzl() {
            return pzl == null ? 0.0 : pzl;
        }

        public void setPzl(Double pzl) {
            this.pzl = pzl;
        }

        public Double getPamt() {
            return pamt == null ? 0.0 : pamt;
        }

        public void setPamt(Double pamt) {
            this.pamt = pamt;
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
            return "PaysDTO{" +
                    "xnote='" + xnote + '\'' +
                    ", pzl=" + pzl +
                    ", pamt=" + pamt +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    public static SaleOrderPrintInfo getInstance(final String order_code){
        final JSONObject order_info = new JSONObject();
        if (SQLiteHelper.execSql(order_info, "SELECT a.order_code,a.name vip_name,a.card_code,a.integral_info,b.cas_name,a.pos_code pos_num,a.stores_id,c.stores_name,datetime(a.addtime, 'unixepoch', 'localtime') oper_time,c.telphone,c.region" +
                " FROM retail_order a  left join cashier_info b on a.cashier_id = b.cas_id\n" +
                "left join shop_stores c on a.stores_id = c.stores_id where a.order_code = '" + order_code + "'")) {
            final StringBuilder err = new StringBuilder();
            final String goods_info_sql = "SELECT a.barcode,b.goods_title,b.type,a.price,a.retail_price original_price,a.xnum,a.retail_price * a.xnum original_amt,\n" +
                    "a.total_money sale_amt,a.retail_price * a.xnum - a.total_money discount_amt,a.goodsPractice FROM retail_order_goods a \n" +
                    "left join barcode_info b on a.barcode_id = b.barcode_id where order_code = '" + order_code + "'", pays_info_sql = "SELECT  b.name,pre_sale_money pamt,(pre_sale_money - pay_money) pzl,xnote FROM retail_order_pays a \n" +
                    "left join pay_method b on a.pay_method = b.pay_method_id where order_code = '" + order_code + "'";

            final JSONArray sales = SQLiteHelper.getListToJson(goods_info_sql, err), pays = SQLiteHelper.getListToJson(pays_info_sql, err);
            if (sales != null && pays != null) {

                order_info.put("sales", sales);
                order_info.put("pays", pays);

                return order_info.toJavaObject(SaleOrderPrintInfo.class);
            }else {
                MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.l_p_c_err_hint_sz,order_info.getString("info")));
            }
        }
        return null;
    }

    public static class VipIntegralInfo{
        @JSONField(name = "points_sum")
        private Double pointsSum = 0.0;
        @JSONField(name = "point_num")
        private Double pointNum = 0.0;

        public Double getPointsSum() {
            return pointsSum;
        }

        public void setPointsSum(Double pointsSum) {
            this.pointsSum = pointsSum;
        }

        public Double getPointNum() {
            return pointNum;
        }

        public void setPointNum(Double pointNum) {
            this.pointNum = pointNum;
        }
    }
}
