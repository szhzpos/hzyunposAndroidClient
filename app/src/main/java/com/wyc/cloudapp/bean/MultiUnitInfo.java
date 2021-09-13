package com.wyc.cloudapp.bean;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.adapter.business.AbstractActionAdapter;
import com.wyc.cloudapp.data.SQLiteHelper;

import java.io.Serializable;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: MultiUnitInfo
 * @Description: 多单位
 * @Author: wyc
 * @CreateDate: 2021-09-13 17:03
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-09-13 17:03
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@SQLiteHelper.TableName("barcode_info")
public class MultiUnitInfo implements Serializable, AbstractActionAdapter.Action {
    @SQLiteHelper.OrderBy
    private int barcode_id;
    private String barcode;
    private String unit_name;
    private double conversion;
    private double retail_price;
    private double yh_price;
    private double trade_price;
    private double ps_price;

    @SQLiteHelper.Where()
    private String only_coding;

    @SQLiteHelper.Ignore
    private boolean plus;

    public MultiUnitInfo(){
        this(false);
    }

    public MultiUnitInfo(boolean flag){
        plus = flag;
    }

    public int getBarcode_id() {
        return barcode_id;
    }

    public void setBarcode_id(int barcode_id) {
        this.barcode_id = barcode_id;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getUnit_name() {
        return unit_name;
    }

    public void setUnit_name(String unit_name) {
        this.unit_name = unit_name;
    }

    public double getConversion() {
        return conversion;
    }

    public void setConversion(double conversion) {
        this.conversion = conversion;
    }

    public double getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(double retail_price) {
        this.retail_price = retail_price;
    }

    public double getYh_price() {
        return yh_price;
    }

    public void setYh_price(double yh_price) {
        this.yh_price = yh_price;
    }

    public double getTrade_price() {
        return trade_price;
    }

    public void setTrade_price(double trade_price) {
        this.trade_price = trade_price;
    }

    public double getPs_price() {
        return ps_price;
    }

    public void setPs_price(double ps_price) {
        this.ps_price = ps_price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiUnitInfo that = (MultiUnitInfo) o;
        return barcode_id == that.barcode_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(barcode_id);
    }

    @NonNull
    @Override
    public String toString() {
        return "MultiUnitInfo{" +
                "barcode_id=" + barcode_id +
                ", barcode='" + barcode + '\'' +
                ", unit_name='" + unit_name + '\'' +
                ", conversion=" + conversion +
                ", retail_price=" + retail_price +
                ", yh_price=" + yh_price +
                ", trade_price=" + trade_price +
                ", ps_price=" + ps_price +
                '}';
    }

    @Override
    public boolean getPlus() {
        return plus;
    }

    @Override
    public void setPlus(boolean flag) {
        plus = flag;
    }
}
