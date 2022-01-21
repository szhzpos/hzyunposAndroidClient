package com.wyc.cloudapp.bean;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: BarcodeScaleInfo
 * @Description: 条码秤实体
 * @Author: wyc
 * @CreateDate: 2022-01-20 14:26
 * @UpdateUser: 更新者
 * @UpdateDate: 2022-01-20 14:26
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */

public class BarcodeScaleInfo {
    @JSONField(name = "s_product_t")
    private String sProductT;
    @JSONField(name = "s_class_id")
    private String sClassId;
    @JSONField(name = "remark")
    private String remark;
    @JSONField(name = "g_c_id")
    private String gCId;
    @JSONField(name = "scale_port")
    private String scalePort;
    @JSONField(name = "scale_ip")
    private String scaleIp;
    @JSONField(name = "g_c_name")
    private String gCName;
    @JSONField(name = "_id")
    private Integer id;
    @JSONField(name = "s_manufacturer")
    private String sManufacturer;

    public String getsProductT() {
        return sProductT;
    }

    public void setsProductT(String sProductT) {
        this.sProductT = sProductT;
    }

    public String getsClassId() {
        return sClassId;
    }

    public void setsClassId(String sClassId) {
        this.sClassId = sClassId;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getgCId() {
        return gCId;
    }

    public void setgCId(String gCId) {
        this.gCId = gCId;
    }

    public String getScalePort() {
        return scalePort;
    }

    public void setScalePort(String scalePort) {
        this.scalePort = scalePort;
    }

    public String getScaleIp() {
        return scaleIp;
    }

    public void setScaleIp(String scaleIp) {
        this.scaleIp = scaleIp;
    }

    public String getgCName() {
        return gCName;
    }

    public void setgCName(String gCName) {
        this.gCName = gCName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getsManufacturer() {
        return sManufacturer;
    }

    public void setsManufacturer(String sManufacturer) {
        this.sManufacturer = sManufacturer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BarcodeScaleInfo that = (BarcodeScaleInfo) o;
        return Objects.equals(sClassId, that.sClassId) && Objects.equals(scalePort, that.scalePort) && Objects.equals(scaleIp, that.scaleIp) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sClassId, scalePort, scaleIp, id);
    }

    @NonNull
    @Override
    public String toString() {
        return "BarcodeScaleInfo{" +
                "sProductT='" + sProductT + '\'' +
                ", sClassId='" + sClassId + '\'' +
                ", remark='" + remark + '\'' +
                ", gCId='" + gCId + '\'' +
                ", scalePort='" + scalePort + '\'' +
                ", scaleIp='" + scaleIp + '\'' +
                ", gCName='" + gCName + '\'' +
                ", id=" + id +
                ", sManufacturer='" + sManufacturer + '\'' +
                '}';
    }
}
