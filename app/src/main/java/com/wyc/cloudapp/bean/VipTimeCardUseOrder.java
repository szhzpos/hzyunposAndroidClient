package com.wyc.cloudapp.bean;


import androidx.annotation.NonNull;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: VipTimeCardUseReuslt
 * @Description: 次卡使用结果
 * @Author: wyc
 * @CreateDate: 2021-07-19 11:43
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-19 11:43
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class VipTimeCardUseOrder{
    @JSONField(name = "number")
    private String number;
    @JSONField(name = "stores_name")
    private String storesName;
    @JSONField(name = "addtime")
    private String addtime;
    @JSONField(name = "title")
    private String title;
    @JSONField(name = "order_code")
    private String orderCode;
    @JSONField(name = "member_name")
    private String memberName;
    @JSONField(name = "goods")
    private List<GoodsDTO> goods;
    @JSONField(name = "use_num")
    private Integer useNum;
    @JSONField(name = "member_mobile")
    private String memberMobile;
    @JSONField(name = "img_big")
    private String imgBig;
    @JSONField(name = "img")
    private String img;
    @JSONField(name = "member_card")
    private String memberCard;

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getStoresName() {
        return storesName;
    }

    public void setStoresName(String storesName) {
        this.storesName = storesName;
    }

    public String getAddtime() {
        return addtime;
    }

    public void setAddtime(String addtime) {
        this.addtime = addtime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public List<GoodsDTO> getGoods() {
        return goods;
    }

    public void setGoods(List<GoodsDTO> goods) {
        this.goods = goods;
    }

    public Integer getUseNum() {
        return useNum;
    }

    public void setUseNum(Integer useNum) {
        this.useNum = useNum;
    }

    public String getMemberMobile() {
        return memberMobile;
    }

    public void setMemberMobile(String memberMobile) {
        this.memberMobile = memberMobile;
    }

    public String getImgBig() {
        return imgBig;
    }

    public void setImgBig(String imgBig) {
        this.imgBig = imgBig;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getMemberCard() {
        return memberCard;
    }

    public void setMemberCard(String memberCard) {
        this.memberCard = memberCard;
    }

    public static class GoodsDTO {
        @JSONField(name = "goods_title")
        private String goodsTitle;
        @JSONField(name = "barcode_id")
        private String barcodeId;
        @JSONField(name = "unit")
        private String unit;
        @JSONField(name = "num")
        private Integer num;

        public String getGoodsTitle() {
            return goodsTitle;
        }

        public void setGoodsTitle(String goodsTitle) {
            this.goodsTitle = goodsTitle;
        }

        public String getBarcodeId() {
            return barcodeId;
        }

        public void setBarcodeId(String barcodeId) {
            this.barcodeId = barcodeId;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public Integer getNum() {
            return num;
        }

        public void setNum(Integer num) {
            this.num = num;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GoodsDTO goodsDTO = (GoodsDTO) o;
            return Objects.equals(barcodeId, goodsDTO.barcodeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(barcodeId);
        }

        @NonNull
        @Override
        public String toString() {
            return "GoodsDTO{" +
                    "goodsTitle='" + goodsTitle + '\'' +
                    ", barcodeId='" + barcodeId + '\'' +
                    ", unit='" + unit + '\'' +
                    ", num=" + num +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VipTimeCardUseOrder that = (VipTimeCardUseOrder) o;
        return Objects.equals(orderCode, that.orderCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orderCode);
    }

    @NonNull
    @Override
    public String toString() {
        return "VipTimeCardUseOrder{" +
                "number='" + number + '\'' +
                ", storesName='" + storesName + '\'' +
                ", addtime='" + addtime + '\'' +
                ", title='" + title + '\'' +
                ", orderCode='" + orderCode + '\'' +
                ", memberName='" + memberName + '\'' +
                ", goods=" + goods +
                ", useNum=" + useNum +
                ", memberMobile='" + memberMobile + '\'' +
                ", imgBig='" + imgBig + '\'' +
                ", img='" + img + '\'' +
                ", memberCard='" + memberCard + '\'' +
                '}';
    }
}