package com.wyc.cloudapp.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: VipPickStuffInfo
 * @Description: 会员取货实体类
 * @Author: wyc
 * @CreateDate: 2021-11-29 14:54
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-11-29 14:54
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */

public class VipPickStuffInfo {
    @JSONField(name = "card_code")
    private String cardCode;
    @JSONField(name = "wh_id")
    private String whId;
    @JSONField(name = "bgd_code")
    private String bgdCode;
    @JSONField(name = "goods_id")
    private String goodsId;
    @JSONField(name = "barcode")
    private String barcode;
    @JSONField(name = "bgd_mode")
    private String bgdMode;
    @JSONField(name = "source_code")
    private String sourceCode;
    @JSONField(name = "member_id")
    private String memberId;
    @JSONField(name = "addtime")
    private String addtime;
    @JSONField(name = "status")
    private String status;
    @JSONField(name = "unit_id")
    private String unit;
    @JSONField(name = "only_coding")
    private String onlyCoding;
    @JSONField(name = "goods_spec_code")
    private String goodsSpecCode;
    @JSONField(name = "xnum_surplus")
    private Double xnumSurplus;
    @JSONField(name = "_id")
    private String id;
    @JSONField(name = "xnum_out")
    private Double xnumOut;
    @JSONField(name = "barcode_id")
    private String barcodeId;
    @JSONField(name = "mobile")
    private String mobile;
    @JSONField(name = "sh_status")
    private Integer shStatus;
    @JSONField(name = "spec")
    private String spec;
    @JSONField(name = "bgd_id")
    private String bgdId;
    @JSONField(name = "spec_val_json")
    private String specValJson;
    @JSONField(name = "goods_title")
    private String goodsTitle;
    @JSONField(name = "bgd_type")
    private String bgdType;
    @JSONField(name = "wh_name")
    private String whName;
    @JSONField(name = "name")
    private String name;
    @JSONField(name = "js_pt_user_id")
    private String jsPtUserId;
    @JSONField(name = "xnum_jicun")
    private Double xnumJicun;
    @JSONField(name = "xnum")

    private Double xnum;
    private boolean sel;
    private Double price;
    private Double pickNum;
    private String unitId;
    private int conversion;

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getConversion() {
        return conversion;
    }

    public void setConversion(int conversion) {
        this.conversion = conversion;
    }

    public void setPickNum(Double pickNum) {
        this.pickNum = pickNum;
    }

    public Double getPickNum() {
        return pickNum;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getCardCode() {
        return cardCode;
    }

    public void setCardCode(String cardCode) {
        this.cardCode = cardCode;
    }

    public String getWhId() {
        return whId;
    }

    public void setWhId(String whId) {
        this.whId = whId;
    }

    public String getBgdCode() {
        return bgdCode;
    }

    public void setBgdCode(String bgdCode) {
        this.bgdCode = bgdCode;
    }

    public String getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(String goodsId) {
        this.goodsId = goodsId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getBgdMode() {
        return bgdMode;
    }

    public void setBgdMode(String bgdMode) {
        this.bgdMode = bgdMode;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getAddtime() {
        return addtime;
    }

    public void setAddtime(String addtime) {
        this.addtime = addtime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUnitId() {
        return unitId;
    }

    public void setUnitId(String unitId) {
        this.unitId = unitId;
    }

    public String getOnlyCoding() {
        return onlyCoding;
    }

    public void setOnlyCoding(String onlyCoding) {
        this.onlyCoding = onlyCoding;
    }

    public String getGoodsSpecCode() {
        return goodsSpecCode;
    }

    public void setGoodsSpecCode(String goodsSpecCode) {
        this.goodsSpecCode = goodsSpecCode;
    }

    public Double getXnumSurplus() {
        return xnumSurplus;
    }

    public void setXnumSurplus(Double xnumSurplus) {
        this.xnumSurplus = xnumSurplus;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Double getXnumOut() {
        return xnumOut;
    }

    public void setXnumOut(Double xnumOut) {
        this.xnumOut = xnumOut;
    }

    public String getBarcodeId() {
        return barcodeId;
    }

    public void setBarcodeId(String barcodeId) {
        this.barcodeId = barcodeId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public Integer getShStatus() {
        return shStatus;
    }

    public void setShStatus(Integer shStatus) {
        this.shStatus = shStatus;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public String getBgdId() {
        return bgdId;
    }

    public void setBgdId(String bgdId) {
        this.bgdId = bgdId;
    }

    public String getSpecValJson() {
        return specValJson;
    }

    public void setSpecValJson(String specValJson) {
        this.specValJson = specValJson;
    }

    public String getGoodsTitle() {
        return goodsTitle;
    }

    public void setGoodsTitle(String goodsTitle) {
        this.goodsTitle = goodsTitle;
    }

    public String getBgdType() {
        return bgdType;
    }

    public void setBgdType(String bgdType) {
        this.bgdType = bgdType;
    }

    public String getWhName() {
        return whName;
    }

    public void setWhName(String whName) {
        this.whName = whName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJsPtUserId() {
        return jsPtUserId;
    }

    public void setJsPtUserId(String jsPtUserId) {
        this.jsPtUserId = jsPtUserId;
    }

    public Double getXnumJicun() {
        return xnumJicun;
    }

    public void setXnumJicun(Double xnumJicun) {
        this.xnumJicun = xnumJicun;
    }

    public Double getXnum() {
        return xnum;
    }

    public void setXnum(Double xnum) {
        this.xnum = xnum;
    }

    public boolean isSel() {
        return sel;
    }

    public void setSel(boolean sel) {
        this.sel = sel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VipPickStuffInfo that = (VipPickStuffInfo) o;
        return Objects.equals(id, that.id) && Objects.equals(barcodeId, that.barcodeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, barcodeId);
    }

    @Override
    public String toString() {
        return "VipPickStuffInfo{" +
                "cardCode='" + cardCode + '\'' +
                ", whId='" + whId + '\'' +
                ", bgdCode='" + bgdCode + '\'' +
                ", goodsId='" + goodsId + '\'' +
                ", barcode='" + barcode + '\'' +
                ", bgdMode='" + bgdMode + '\'' +
                ", sourceCode='" + sourceCode + '\'' +
                ", memberId='" + memberId + '\'' +
                ", addtime='" + addtime + '\'' +
                ", status='" + status + '\'' +
                ", unitId='" + unitId + '\'' +
                ", onlyCoding='" + onlyCoding + '\'' +
                ", goodsSpecCode='" + goodsSpecCode + '\'' +
                ", xnumSurplus=" + xnumSurplus +
                ", id='" + id + '\'' +
                ", xnumOut=" + xnumOut +
                ", barcodeId='" + barcodeId + '\'' +
                ", mobile='" + mobile + '\'' +
                ", shStatus=" + shStatus +
                ", spec='" + spec + '\'' +
                ", bgdId='" + bgdId + '\'' +
                ", specValJson='" + specValJson + '\'' +
                ", goodsTitle='" + goodsTitle + '\'' +
                ", bgdType='" + bgdType + '\'' +
                ", whName='" + whName + '\'' +
                ", name='" + name + '\'' +
                ", jsPtUserId='" + jsPtUserId + '\'' +
                ", xnumJicun=" + xnumJicun +
                ", xnum=" + xnum +
                ", sel=" + sel +
                '}';
    }
}
