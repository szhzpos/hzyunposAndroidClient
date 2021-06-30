package com.wyc.cloudapp.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wyc.cloudapp.utils.Utils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: OnceCardSaleInfo
 * @Description: 次卡销售信息
 * @Author: wyc
 * @CreateDate: 2021-06-30 15:58
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-30 15:58
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class OnceCardSaleInfo implements Serializable {
    private int num;
    private double amt;
    private double price;
    private int once_card_id;
    private String name;

    public OnceCardSaleInfo(){

    }


    public void setNum(int n){
        num = n;
        amt = n * price;
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

    @Override
    public int hashCode() {
        return Objects.hash(once_card_id,name,price,num, amt);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other){
            return true;
        }
        if (!(other instanceof OnceCardSaleInfo)) {
            return false;
        }
        OnceCardSaleInfo rhs = ((OnceCardSaleInfo) other);
        return once_card_id == rhs.once_card_id;
    }

    @NonNull
    @Override
    public String toString() {
        return OnceCardInfo.class.getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '{' +
                "once_card_id = " +
                once_card_id +
                ",\r\n name = " +
                ((this.name == null) ? "<null>" : this.name) +
                ",\r\n sale_num = " + num +
                ",\r\n sale_amt = " + amt +
                '}';
    }

    public static class Builder{
        private final OnceCardSaleInfo mInfo;
        public Builder(){
            mInfo = new OnceCardSaleInfo();
        }
        public Builder onceCardId(int id){
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

        public OnceCardSaleInfo build(){
            return mInfo;
        }
    }
}
