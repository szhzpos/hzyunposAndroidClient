package com.wyc.cloudapp.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.wyc.cloudapp.utils.Utils;

import java.io.Serializable;
import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: Consumer
 * @Description: 客户
 * @Author: wyc
 * @CreateDate: 2021/5/19 10:37
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/19 10:37
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class Consumer implements Serializable, Parcelable {
    private String c_s_id;
    private String cs_code;
    private String cs_name;
    private String address;
    private String name;
    private String mobile;
    private String roles;
    private int cs_kf_price;
    private String cs_kf_price_name;
    private int customer_settlement_cycle_id;
    private String customer_settlement_cycle_name;

    public Consumer(){

    }

    public Consumer(Parcel in){
        c_s_id = in.readString();
        cs_code = in.readString();
        cs_name = in.readString();
        address = in.readString();
        name = in.readString();
        mobile = in.readString();
        roles = in.readString();
        cs_kf_price = in.readInt();
        cs_kf_price_name = in.readString();
        customer_settlement_cycle_id = in.readInt();
        customer_settlement_cycle_name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(c_s_id);
        dest.writeString(cs_code);
        dest.writeString(cs_name);
        dest.writeString(address);
        dest.writeString(name);
        dest.writeString(mobile);
        dest.writeString(roles);
        dest.writeInt(cs_kf_price);
        dest.writeString(cs_kf_price_name);
        dest.writeInt(customer_settlement_cycle_id);
        dest.writeString(customer_settlement_cycle_name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static Creator<Consumer> CREATOR = new Creator<Consumer>() {
        @Override
        public Consumer createFromParcel(Parcel source) {
            return new Consumer(source);
        }

        @Override
        public Consumer[] newArray(int size) {
            return new Consumer[size];
        }
    };

    public void setC_s_id(String c_s_id) {
        this.c_s_id = c_s_id;
    }

    public String getC_s_id() {
        return c_s_id;
    }

    public void setCs_code(String cs_code) {
        this.cs_code = cs_code;
    }

    public String getCs_code() {
        return cs_code;
    }

    public void setCs_name(String cs_name) {
        this.cs_name = cs_name;
    }

    public String getCs_name() {
        return cs_name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getRoles() {
        return roles;
    }

    public void setCs_kf_price(int cs_kf_price) {
        this.cs_kf_price = cs_kf_price;
    }

    public int getCs_kf_price() {
        return cs_kf_price;
    }

    public void setCs_kf_price_name(String cs_kf_price_name) {
        this.cs_kf_price_name = cs_kf_price_name;
    }

    public String getCs_kf_price_name() {
        return cs_kf_price_name;
    }

    public void setCustomer_settlement_cycle_id(int customer_settlement_cycle_id) {
        this.customer_settlement_cycle_id = customer_settlement_cycle_id;
    }

    public int getCustomer_settlement_cycle_id() {
        return customer_settlement_cycle_id;
    }

    public void setCustomer_settlement_cycle_name(String customer_settlement_cycle_name) {
        this.customer_settlement_cycle_name = customer_settlement_cycle_name;
    }

    public String getCustomer_settlement_cycle_name() {
        return customer_settlement_cycle_name;
    }
    public boolean isNotEmpty(){
        return Utils.isNotEmpty(c_s_id);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.CHINA,"cs_id[%s]-cs_code[%s]-cs_name[%s]",c_s_id,cs_code,cs_name);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }
}
