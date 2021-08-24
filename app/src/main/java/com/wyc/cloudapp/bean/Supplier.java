package com.wyc.cloudapp.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.wyc.cloudapp.utils.Utils;

import java.util.Objects;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: Supplier
 * @Description: 供应商
 * @Author: wyc
 * @CreateDate: 2021/5/17 11:55
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/17 11:55
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class Supplier implements Parcelable {
    private String c_s_id;
    private String cs_code;
    private String cs_name;

    private String gs_id;
    private String gs_code;
    private String gs_name;

    private String name;
    private String mobile;
    private String roles;
    private String address;
    private int hz_method;
    private String hz_method_name;
    private int supplier_settlement_cycle_id;
    private String supplier_settlement_cycle_name;

    public Supplier(){

    }

    protected Supplier(Parcel in) {
        c_s_id = in.readString();
        cs_code = in.readString();
        cs_name = in.readString();

        gs_id = in.readString();
        gs_code = in.readString();
        gs_name = in.readString();

        name = in.readString();
        mobile = in.readString();
        roles = in.readString();
        address = in.readString();
        hz_method = in.readInt();
        hz_method_name = in.readString();
        supplier_settlement_cycle_id = in.readInt();
        supplier_settlement_cycle_name = in.readString();
    }

    public static final Creator<Supplier> CREATOR = new Creator<Supplier>() {
        @Override
        public Supplier createFromParcel(Parcel in) {
            return new Supplier(in);
        }

        @Override
        public Supplier[] newArray(int size) {
            return new Supplier[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(c_s_id);
        dest.writeString(cs_code);
        dest.writeString(cs_name);

        dest.writeString(gs_id);
        dest.writeString(gs_code);
        dest.writeString(gs_name);

        dest.writeString(name);
        dest.writeString(mobile);
        dest.writeString(roles);
        dest.writeString(address);
        dest.writeInt(hz_method);
        dest.writeString(hz_method_name);
        dest.writeInt(supplier_settlement_cycle_id);
        dest.writeString(supplier_settlement_cycle_name);
    }

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

    public String getGs_id() {
        return gs_id;
    }

    public void setGs_id(String gs_id) {
        this.gs_id = gs_id;
    }

    public String getGs_code() {
        return gs_code;
    }

    public void setGs_code(String gs_code) {
        this.gs_code = gs_code;
    }

    public String getGs_name() {
        return gs_name;
    }

    public void setGs_name(String gs_name) {
        this.gs_name = gs_name;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getRoles() {
        return roles;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setHz_method(int hz_method) {
        this.hz_method = hz_method;
    }

    public int getHz_method() {
        return hz_method;
    }

    public void setHz_method_name(String hz_method_name) {
        this.hz_method_name = hz_method_name;
    }

    public String getHz_method_name() {
        return hz_method_name;
    }

    public void setSupplier_settlement_cycle_id(int supplier_settlement_cycle_id) {
        this.supplier_settlement_cycle_id = supplier_settlement_cycle_id;
    }

    public int getSupplier_settlement_cycle_id() {
        return supplier_settlement_cycle_id;
    }

    public void setSupplier_settlement_cycle_name(String supplier_settlement_cycle_name) {
        this.supplier_settlement_cycle_name = supplier_settlement_cycle_name;
    }

    public String getSupplier_settlement_cycle_name() {
        return supplier_settlement_cycle_name;
    }

    public boolean isNotEmpty(){
        return Utils.isNotEmpty(c_s_id);
    }

    @Override
    public String toString() {
        return "Supplier{" +
                "c_s_id='" + c_s_id + '\'' +
                ", cs_code='" + cs_code + '\'' +
                ", cs_name='" + cs_name + '\'' +
                ", gs_id='" + gs_id + '\'' +
                ", gs_code='" + gs_code + '\'' +
                ", gs_name='" + gs_name + '\'' +
                ", name='" + name + '\'' +
                ", mobile='" + mobile + '\'' +
                ", roles='" + roles + '\'' +
                ", address='" + address + '\'' +
                ", hz_method=" + hz_method +
                ", hz_method_name='" + hz_method_name + '\'' +
                ", supplier_settlement_cycle_id=" + supplier_settlement_cycle_id +
                ", supplier_settlement_cycle_name='" + supplier_settlement_cycle_name + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Supplier supplier = (Supplier) o;
        return Objects.equals(c_s_id, supplier.c_s_id) &&
                Objects.equals(cs_code, supplier.cs_code) &&
                Objects.equals(gs_id, supplier.gs_id) &&
                Objects.equals(gs_code, supplier.gs_code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(c_s_id, cs_code, gs_id, gs_code);
    }
}
