package com.wyc.cloudapp.bean;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: GoodsStockInfo
 * @Description: 商品库存信息
 * @Author: wyc
 * @CreateDate: 2022-3-2 14:49
 * @UpdateUser: 更新者
 * @UpdateDate: 2022-3-2 14:49
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class GoodsStockInfo {
    private String only_coding;
    private String barcode;
    private String goods_title;
    private String goods_spec_code;
    private String spec;
    private String unit;
    private Double stock_num;
    private Double stock_money;

    public String getOnly_coding() {
        return only_coding;
    }

    public void setOnly_coding(String only_coding) {
        this.only_coding = only_coding;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getGoods_title() {
        return goods_title;
    }

    public void setGoods_title(String goods_title) {
        this.goods_title = goods_title;
    }

    public String getGoods_spec_code() {
        return goods_spec_code;
    }

    public void setGoods_spec_code(String goods_spec_code) {
        this.goods_spec_code = goods_spec_code;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getStock_num() {
        return stock_num;
    }

    public void setStock_num(Double stock_num) {
        this.stock_num = stock_num;
    }

    public Double getStock_money() {
        return stock_money;
    }

    public void setStock_money(Double stock_money) {
        this.stock_money = stock_money;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoodsStockInfo that = (GoodsStockInfo) o;
        return Objects.equals(only_coding, that.only_coding) && Objects.equals(barcode, that.barcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(only_coding, barcode);
    }

    @NonNull
    @Override
    public String toString() {
        return "GoodsStockInfo{" +
                "only_coding='" + only_coding + '\'' +
                ", barcode='" + barcode + '\'' +
                ", goods_title='" + goods_title + '\'' +
                ", goods_spec_code='" + goods_spec_code + '\'' +
                ", spec='" + spec + '\'' +
                ", unit='" + unit + '\'' +
                ", stock_num=" + stock_num +
                ", stock_money=" + stock_money +

                '}';
    }
}
