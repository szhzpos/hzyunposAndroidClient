package com.wyc.cloudapp.bean;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: VipTimeCardData
 * @Description: 会员次卡数据
 * @Author: wyc
 * @CreateDate: 2021-07-16 18:04
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-16 18:04
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class VipTimeCardData implements Serializable {
    private List<VipTimeCardInfo> card;

    public List<VipTimeCardInfo> getCard() {
        return card == null ? new ArrayList<>() : card;
    }

    public void setCard(List<VipTimeCardInfo> card) {
        this.card = card;
    }

    @Override
    public String toString() {
        return "VipTimeCardData{" +
                "card=" + card +
                '}';
    }

    public static final class VipTimeCardInfo implements Serializable {

        @JSONField(name = "qrcode_img")
        private String qrcodeImg;
        @JSONField(name = "end_time")
        private String endTime;
        @JSONField(name = "member_grade")
        private String memberGrade;
        @JSONField(name = "sy_limit_types")
        private String syLimitTypes;
        @JSONField(name = "goods")
        private List<GoodsInfo> goods;
        @JSONField(name = "start_time")
        private String startTime;
        @JSONField(name = "validity_types")
        private String validityTypes;
        @JSONField(name = "buy_channel")
        private String buyChannel;
        @JSONField(name = "validity_type")
        private String validityType;
        @JSONField(name = "addtime")
        private String addtime;
        @JSONField(name = "max_day")
        private String maxDay;
        @JSONField(name = "status")
        private String status;
        @JSONField(name = "member_mobile")
        private String memberMobile;
        @JSONField(name = "available_limits")
        private String availableLimits;
        @JSONField(name = "available")
        private String available;
        @JSONField(name = "channel")
        private String channel;
        @JSONField(name = "member_card")
        private String memberCard;
        @JSONField(name = "status_name")
        private String statusName;
        @JSONField(name = "number")
        private String number;
        @JSONField(name = "channels")
        private String channels;
        @JSONField(name = "usenum")
        private Integer usenum;
        @JSONField(name = "sy_limit_type")
        private String syLimitType;
        @JSONField(name = "img_big")
        private String imgBig;
        @JSONField(name = "img")
        private String img;
        @JSONField(name = "price")
        private String price;
        @JSONField(name = "title")
        private String title;
        @JSONField(name = "available_limit")
        private String availableLimit;
        @JSONField(name = "sy_limit")
        private String syLimit;
        @JSONField(name = "member_name")
        private String memberName;
        @JSONField(name = "barcode_img")
        private String barcodeImg;

        public String getQrcodeImg() {
            return qrcodeImg;
        }

        public void setQrcodeImg(String qrcodeImg) {
            this.qrcodeImg = qrcodeImg;
        }

        public String getEndTime() {
            return endTime;
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        public String getMemberGrade() {
            return memberGrade;
        }

        public void setMemberGrade(String memberGrade) {
            this.memberGrade = memberGrade;
        }

        public String getSyLimitTypes() {
            return syLimitTypes;
        }

        public void setSyLimitTypes(String syLimitTypes) {
            this.syLimitTypes = syLimitTypes;
        }

        public List<GoodsInfo> getGoods() {
            return goods;
        }

        public void setGoods(List<GoodsInfo> goods) {
            this.goods = goods;
        }

        public String getStartTime() {
            return startTime;
        }

        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }

        public String getValidityTypes() {
            return validityTypes;
        }

        public void setValidityTypes(String validityTypes) {
            this.validityTypes = validityTypes;
        }

        public String getBuyChannel() {
            return buyChannel;
        }

        public void setBuyChannel(String buyChannel) {
            this.buyChannel = buyChannel;
        }

        public String getValidityType() {
            return validityType;
        }

        public void setValidityType(String validityType) {
            this.validityType = validityType;
        }

        public String getAddtime() {
            return addtime;
        }

        public void setAddtime(String addtime) {
            this.addtime = addtime;
        }

        public String getMaxDay() {
            return maxDay;
        }

        public void setMaxDay(String maxDay) {
            this.maxDay = maxDay;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getMemberMobile() {
            return memberMobile;
        }

        public void setMemberMobile(String memberMobile) {
            this.memberMobile = memberMobile;
        }

        public String getAvailableLimits() {
            return availableLimits;
        }

        public void setAvailableLimits(String availableLimits) {
            this.availableLimits = availableLimits;
        }

        public String getAvailable() {
            return available;
        }

        public void setAvailable(String available) {
            this.available = available;
        }

        public String getChannel() {
            return channel;
        }

        public void setChannel(String channel) {
            this.channel = channel;
        }

        public String getMemberCard() {
            return memberCard;
        }

        public void setMemberCard(String memberCard) {
            this.memberCard = memberCard;
        }

        public String getStatusName() {
            return statusName;
        }

        public void setStatusName(String statusName) {
            this.statusName = statusName;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getChannels() {
            return channels;
        }

        public void setChannels(String channels) {
            this.channels = channels;
        }

        public Integer getUsenum() {
            return usenum;
        }

        public void setUsenum(Integer usenum) {
            this.usenum = usenum;
        }

        public String getSyLimitType() {
            return syLimitType;
        }

        public void setSyLimitType(String syLimitType) {
            this.syLimitType = syLimitType;
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

        public String getPrice() {
            return price;
        }

        public void setPrice(String price) {
            this.price = price;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAvailableLimit() {
            return availableLimit;
        }

        public void setAvailableLimit(String availableLimit) {
            this.availableLimit = availableLimit;
        }

        public String getSyLimit() {
            return syLimit;
        }

        public void setSyLimit(String syLimit) {
            this.syLimit = syLimit;
        }

        public String getMemberName() {
            return memberName;
        }

        public void setMemberName(String memberName) {
            this.memberName = memberName;
        }

        public String getBarcodeImg() {
            return barcodeImg;
        }

        public void setBarcodeImg(String barcodeImg) {
            this.barcodeImg = barcodeImg;
        }

        @NonNull
        @Override
        public String toString() {
            return "VipTimeCardInfo{" +
                    "qrcodeImg='" + qrcodeImg + '\'' +
                    ", endTime='" + endTime + '\'' +
                    ", memberGrade='" + memberGrade + '\'' +
                    ", syLimitTypes='" + syLimitTypes + '\'' +
                    ", goods=" + goods +
                    ", startTime='" + startTime + '\'' +
                    ", validityTypes='" + validityTypes + '\'' +
                    ", buyChannel='" + buyChannel + '\'' +
                    ", validityType='" + validityType + '\'' +
                    ", addtime='" + addtime + '\'' +
                    ", maxDay='" + maxDay + '\'' +
                    ", status='" + status + '\'' +
                    ", memberMobile='" + memberMobile + '\'' +
                    ", availableLimits='" + availableLimits + '\'' +
                    ", available='" + available + '\'' +
                    ", channel='" + channel + '\'' +
                    ", memberCard='" + memberCard + '\'' +
                    ", statusName='" + statusName + '\'' +
                    ", number='" + number + '\'' +
                    ", channels='" + channels + '\'' +
                    ", usenum=" + usenum +
                    ", syLimitType='" + syLimitType + '\'' +
                    ", imgBig='" + imgBig + '\'' +
                    ", img='" + img + '\'' +
                    ", price='" + price + '\'' +
                    ", title='" + title + '\'' +
                    ", availableLimit='" + availableLimit + '\'' +
                    ", syLimit='" + syLimit + '\'' +
                    ", memberName='" + memberName + '\'' +
                    ", barcodeImg='" + barcodeImg + '\'' +
                    '}';
        }

        public static class GoodsInfo {
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
                GoodsInfo goodsInfo = (GoodsInfo) o;
                return Objects.equals(barcodeId, goodsInfo.barcodeId);
            }

            @Override
            public int hashCode() {
                return Objects.hash(barcodeId);
            }

            @Override
            public String toString() {
                return "GoodsInfo{" +
                        "goodsTitle='" + goodsTitle + '\'' +
                        ", barcodeId='" + barcodeId + '\'' +
                        ", unit='" + unit + '\'' +
                        ", num=" + num +
                        '}';
            }
        }
    }
}
