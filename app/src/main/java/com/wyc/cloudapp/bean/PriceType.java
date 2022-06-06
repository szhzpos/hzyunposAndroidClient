package com.wyc.cloudapp.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.wyc.cloudapp.constants.PriceTypeId;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: PriceType
 * @Description: 业务单据价格类型
 * @Author: wyc
 * @CreateDate: 2022/6/6 10:15
 * @UpdateUser: 更新者：
 * @UpdateDate: 2022/6/6 10:15
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
public class PriceType implements Parcelable {
    private Integer priceType = PriceTypeId.RETAIL_PRICE;
    private Double discount = 1.0;

    public PriceType(){

    }

    protected PriceType(Parcel in) {
        if (in.readByte() == 0) {
            priceType = null;
        } else {
            priceType = in.readInt();
        }
        if (in.readByte() == 0) {
            discount = null;
        } else {
            discount = in.readDouble();
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (priceType == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(priceType);
        }
        if (discount == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(discount);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PriceType> CREATOR = new Creator<PriceType>() {
        @Override
        public PriceType createFromParcel(Parcel in) {
            return new PriceType(in);
        }

        @Override
        public PriceType[] newArray(int size) {
            return new PriceType[size];
        }
    };

    public Integer getPriceType() {
        return priceType;
    }

    public void setPriceType(Integer priceType) {
        this.priceType = priceType;
    }

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    @Override
    public String toString() {
        return "PriceType{" +
                "priceType=" + priceType +
                ", discount=" + discount +
                '}';
    }
}
