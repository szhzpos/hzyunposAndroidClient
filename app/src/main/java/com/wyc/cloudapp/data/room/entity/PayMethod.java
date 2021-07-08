package com.wyc.cloudapp.data.room.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.data.room.entity
 * @ClassName: PayMethod
 * @Description: 支付方式
 * @Author: wyc
 * @CreateDate: 2021-07-08 13:40
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-08 13:40
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Entity(tableName = "pay_method")
public final class PayMethod implements Serializable {
    @PrimaryKey
    private int pay_method_id;
    private String name;
    private Integer status;
    private String remark;
    private Integer is_check;
    private String shortcut_key;
    private Integer sort;
    private String xtype;
    private String pay_img;
    private String master_img;
    private Integer is_show_client;
    @ColumnInfo(defaultValue = "1")
    private Integer is_cardno;
    @ColumnInfo(defaultValue = "2")
    private Integer is_scan;
    private String wr_btn_img;
    private String unified_pay_order;
    private String unified_pay_query;
    private String rule;
    @ColumnInfo(defaultValue = "1")
    private Integer is_open;
    private String support;
    @ColumnInfo(defaultValue = "1")
    private Integer is_enable;
    @ColumnInfo(defaultValue = "1")
    private Integer is_moling;

    public int getPay_method_id() {
        return pay_method_id;
    }

    public void setPay_method_id(int pay_method_id) {
        this.pay_method_id = pay_method_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getIs_check() {
        return is_check;
    }

    public void setIs_check(Integer is_check) {
        this.is_check = is_check;
    }

    public String getShortcut_key() {
        return shortcut_key;
    }

    public void setShortcut_key(String shortcut_key) {
        this.shortcut_key = shortcut_key;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getXtype() {
        return xtype;
    }

    public void setXtype(String xtype) {
        this.xtype = xtype;
    }

    public String getPay_img() {
        return pay_img;
    }

    public void setPay_img(String pay_img) {
        this.pay_img = pay_img;
    }

    public String getMaster_img() {
        return master_img;
    }

    public void setMaster_img(String master_img) {
        this.master_img = master_img;
    }

    public Integer getIs_show_client() {
        return is_show_client;
    }

    public void setIs_show_client(Integer is_show_client) {
        this.is_show_client = is_show_client;
    }

    public Integer getIs_cardno() {
        return is_cardno;
    }

    public void setIs_cardno(Integer is_cardno) {
        this.is_cardno = is_cardno;
    }

    public Integer getIs_scan() {
        return is_scan;
    }

    public void setIs_scan(Integer is_scan) {
        this.is_scan = is_scan;
    }

    public String getWr_btn_img() {
        return wr_btn_img;
    }

    public void setWr_btn_img(String wr_btn_img) {
        this.wr_btn_img = wr_btn_img;
    }

    public String getUnified_pay_order() {
        return unified_pay_order;
    }

    public void setUnified_pay_order(String unified_pay_order) {
        this.unified_pay_order = unified_pay_order;
    }

    public String getUnified_pay_query() {
        return unified_pay_query;
    }

    public void setUnified_pay_query(String unified_pay_query) {
        this.unified_pay_query = unified_pay_query;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public Integer getIs_open() {
        return is_open;
    }

    public void setIs_open(Integer is_open) {
        this.is_open = is_open;
    }

    public String getSupport() {
        return support;
    }

    public void setSupport(String support) {
        this.support = support;
    }

    public Integer getIs_enable() {
        return is_enable;
    }

    public void setIs_enable(Integer is_enable) {
        this.is_enable = is_enable;
    }

    public Integer getIs_moling() {
        return is_moling;
    }

    public void setIs_moling(Integer is_moling) {
        this.is_moling = is_moling;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PayMethod payMethod = (PayMethod) o;
        return pay_method_id == payMethod.pay_method_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pay_method_id);
    }

    @Override
    public String toString() {
        return "PayMethod{" +
                "pay_method_id=" + pay_method_id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", remark='" + remark + '\'' +
                ", is_check=" + is_check +
                ", shortcut_key='" + shortcut_key + '\'' +
                ", sort=" + sort +
                ", xtype='" + xtype + '\'' +
                ", pay_img='" + pay_img + '\'' +
                ", master_img='" + master_img + '\'' +
                ", is_show_client=" + is_show_client +
                ", is_cardno=" + is_cardno +
                ", is_scan=" + is_scan +
                ", wr_btn_img='" + wr_btn_img + '\'' +
                ", unified_pay_order='" + unified_pay_order + '\'' +
                ", unified_pay_query='" + unified_pay_query + '\'' +
                ", rule='" + rule + '\'' +
                ", is_open=" + is_open +
                ", support='" + support + '\'' +
                ", is_enable=" + is_enable +
                '}';
    }
}
