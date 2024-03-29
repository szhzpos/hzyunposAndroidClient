package com.wyc.cloudapp.bean;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.annotation.JSONField;
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
    @JSONField(serialize = false)
    private int unit_id;
    @JSONField(name = "sys_unit_name")
    private String unit_name;
    private double conversion = 1.0;
    private double retail_price;
    private double yh_price;
    private double trade_price;
    private double ps_price;
    @JSONField(name = "del_status")
    @SQLiteHelper.Where(index = 1)
    private int barcode_status;/*条码删除状态 1正常(默认) 2删除此条码*/

    @SQLiteHelper.Where()
    private String only_coding;

    @JSONField(serialize = false)
    @SQLiteHelper.Ignore
    private boolean plus;

    public MultiUnitInfo(){
        this(false);
    }

    public MultiUnitInfo(boolean flag){
        plus = flag;
    }


    public MultiUnitInfo copy(MultiUnitInfo data){
        conversion = data.conversion;
        retail_price = data.retail_price;
        yh_price = data.yh_price;
        trade_price = data.trade_price;
        ps_price = data.ps_price;
        only_coding = data.only_coding;
        barcode_status = data.barcode_status;
        plus = data.plus;
        return this;
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

    public int getUnit_id() {
        return unit_id;
    }

    public void setUnit_id(int unit_id) {
        this.unit_id = unit_id;
    }

    public int getBarcode_status() {
        return barcode_status;
    }

    public void setBarcode_status(int barcode_status) {
        this.barcode_status = barcode_status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MultiUnitInfo that = (MultiUnitInfo) o;
        return barcode_id == that.barcode_id &&
                Objects.equals(barcode, that.barcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(barcode_id, barcode);
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
