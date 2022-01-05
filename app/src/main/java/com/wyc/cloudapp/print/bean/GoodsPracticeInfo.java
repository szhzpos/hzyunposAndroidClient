package com.wyc.cloudapp.print.bean;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print.bean
 * @ClassName: GoodsPracticeInfo
 * @Description: 商品选择的做法信息
 * @Author: wyc
 * @CreateDate: 2021-12-28 17:38
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-28 17:38
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class GoodsPracticeInfo{

    @JSONField(name = "barcode_id")
    private String barcodeId;
    @JSONField(name = "id")
    private Integer id;
    @JSONField(name = "kw_code")
    private String kwCode;
    @JSONField(name = "kw_name")
    private String kwName;
    @JSONField(name = "kw_xnum")
    private Double kwXnum;
    @JSONField(name = "kw_price")
    private Double kwPrice;

    public String getBarcodeId() {
        return barcodeId;
    }

    public void setBarcodeId(String barcodeId) {
        this.barcodeId = barcodeId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKwCode() {
        return kwCode;
    }

    public void setKwCode(String kwCode) {
        this.kwCode = kwCode;
    }

    public String getKwName() {
        return kwName == null ? "" : kwName;
    }

    public void setKwName(String kwName) {
        this.kwName = kwName;
    }

    public Double getKwXnum() {
        return kwXnum;
    }

    public void setKwXnum(Double kwXnum) {
        this.kwXnum = kwXnum;
    }

    public Double getKwPrice() {
        return kwPrice == null ? 0.00 : kwPrice;
    }

    public void setKwPrice(Double kwPrice) {
        this.kwPrice = kwPrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoodsPracticeInfo that = (GoodsPracticeInfo) o;
        return Objects.equals(barcodeId, that.barcodeId) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(barcodeId, id);
    }

    @Override
    public String toString() {
        return "GoodsPracticeInfo{" +
                "barcodeId='" + barcodeId + '\'' +
                ", id=" + id +
                ", kwCode='" + kwCode + '\'' +
                ", kwName='" + kwName + '\'' +
                ", kwXnum=" + kwXnum +
                ", kwPrice=" + kwPrice +
                '}';
    }

    public static String generateGoodsPracticeInfo(@NonNull List<GoodsPracticeInfo> array){
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0,size = array.size();i < size;i ++){
            final GoodsPracticeInfo obj = array.get(i);
            stringBuilder.append(String.format(Locale.CHINA,"%s(%.2f)",obj.getKwName(),obj.getKwPrice()));
        }
        return stringBuilder.toString();
    }
}
