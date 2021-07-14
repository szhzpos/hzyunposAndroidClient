package com.wyc.cloudapp.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;

import com.wyc.cloudapp.data.room.AppDatabase;

import java.util.List;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: TimeCardSaleInfo
 * @Description: 次卡销售信息
 * @Author: wyc
 * @CreateDate: 2021-06-30 15:58
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-30 15:58
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Entity(tableName = "timeCardSaleDetails",primaryKeys = {"rowId","order_no"})
public final class TimeCardSaleInfo implements Parcelable {
    private int rowId;
    private int num;
    private double amt;
    private double price;
    private int once_card_id;
    private String name;
    private double discountAmt;
    /*
     * 对应的销售订单号
     * */
    @NonNull
    private String order_no;

    public TimeCardSaleInfo(){

    }

    protected TimeCardSaleInfo(Parcel in) {
        rowId = in.readInt();
        num = in.readInt();
        amt = in.readDouble();
        price = in.readDouble();
        once_card_id = in.readInt();
        name = in.readString();
        discountAmt = in.readDouble();
        order_no = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(rowId);
        dest.writeInt(num);
        dest.writeDouble(amt);
        dest.writeDouble(price);
        dest.writeInt(once_card_id);
        dest.writeString(name);
        dest.writeDouble(discountAmt);
        dest.writeString(order_no);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TimeCardSaleInfo> CREATOR = new Creator<TimeCardSaleInfo>() {
        @Override
        public TimeCardSaleInfo createFromParcel(Parcel in) {
            return new TimeCardSaleInfo(in);
        }

        @Override
        public TimeCardSaleInfo[] newArray(int size) {
            return new TimeCardSaleInfo[size];
        }
    };

    public void setNum(int n){
        num = n;
        amt = n * price;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public int getRowId() {
        return rowId;
    }

    public int getNum() {
        return num;
    }

    public void setAmt(double amt) {
        this.amt = amt;
    }

    public double getAmt() {
        return amt;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public int getOnce_card_id() {
        return once_card_id;
    }

    public void setOnce_card_id(int once_card_id) {
        this.once_card_id = once_card_id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setDiscountAmt(double discountAmt) {
        this.discountAmt = discountAmt;
    }

    public double getDiscountAmt() {
        return discountAmt;
    }


    public void setOrder_no(@NonNull String order_no) {
        this.order_no = order_no;
    }
    @NonNull
    public String getOrder_no() {
        return order_no;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeCardSaleInfo that = (TimeCardSaleInfo) o;
        return rowId == that.rowId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowId);
    }

    public boolean equalsWithTimeCardInfo(TimeCardSaleInfo o){
        if (o == null)return false;
        return once_card_id == o.once_card_id;
    }

    public static List<TimeCardSaleInfo> getSaleInfoById(String id){
        return AppDatabase.getInstance().TimeCardSaleDetailDao().getDetailById(id);
    }

    @NonNull
    @Override
    public String toString() {
        return TimeCardInfo.class.getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '{' +
                "once_card_id = " +
                once_card_id +
                ",\r\n name = " +
                ((this.name == null) ? "<null>" : this.name) +
                ",\r\n sale_num = " + num +
                ",\r\n sale_amt = " + amt +
                '}';
    }

    public static class Builder{
        private final TimeCardSaleInfo mInfo;
        public Builder(){
            mInfo = new TimeCardSaleInfo();
        }
        public Builder timeCardId(int id){
            mInfo.setOnce_card_id(id);
            return this;
        }
        public Builder name(String sz){
            mInfo.setName(sz);
            return this;
        }
        public Builder price(double p){
            mInfo.setPrice(p);
            return this;
        }
        public Builder num(int n){
            mInfo.setNum(n);
            return this;
        }
        public Builder amt(double a){
            mInfo.setAmt(a);
            return this;
        }
        public TimeCardSaleInfo build(){
            return mInfo;
        }
    }
}
