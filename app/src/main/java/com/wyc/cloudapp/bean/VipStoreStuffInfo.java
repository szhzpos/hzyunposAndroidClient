package com.wyc.cloudapp.bean;

import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: VipStoreStuffInfo
 * @Description: 会员存物实体
 * @Author: wyc
 * @CreateDate: 2021-11-23 16:48
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-11-23 16:48
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class VipStoreStuffInfo {
    private String barcode_id;
    private String goods_id;
    private String barcode;
    private String itemNo;
    private String name;
    private Double price;
    private String unit_id;
    private String unit;
    private Integer conversion;
    private Double storeNum = 1.0;
    private Double buying_price;

    public String getBarcode_id() {
        return barcode_id;
    }

    public void setBarcode_id(String barcode_id) {
        this.barcode_id = barcode_id;
    }

    public String getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(String goods_id) {
        this.goods_id = goods_id;
    }

    public String getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(String unit_id) {
        this.unit_id = unit_id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getItemNo() {
        return itemNo;
    }

    public void setItemNo(String itemNo) {
        this.itemNo = itemNo;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getStoreNum() {
        return storeNum;
    }

    public Integer getConversion() {
        return conversion;
    }

    public void setConversion(Integer conversion) {
        this.conversion = conversion;
    }

    public void setStoreNum(Double storeNum) {
        this.storeNum = storeNum;
    }

    public Double getBuying_price() {
        return buying_price;
    }

    public void setBuying_price(Double buying_price) {
        this.buying_price = buying_price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VipStoreStuffInfo that = (VipStoreStuffInfo) o;
        return Objects.equals(barcode_id, that.barcode_id) && Objects.equals(goods_id, that.goods_id) && Objects.equals(barcode, that.barcode) && Objects.equals(itemNo, that.itemNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(barcode_id, goods_id, barcode, itemNo);
    }

    @Override
    public String toString() {
        return "VipStoreStuffInfo{" +
                "barcode_id='" + barcode_id + '\'' +
                ", goods_id='" + goods_id + '\'' +
                ", barcode='" + barcode + '\'' +
                ", itemNo='" + itemNo + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", unit_id='" + unit_id + '\'' +
                ", unit='" + unit + '\'' +
                ", conversion=" + conversion +
                ", storeNum=" + storeNum +
                ", buying_price=" + buying_price +
                '}';
    }
}
