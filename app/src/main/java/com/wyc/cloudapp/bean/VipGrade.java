package com.wyc.cloudapp.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: VipCategory
 * @Description: 会员类别
 * @Author: wyc
 * @CreateDate: 2021/5/19 17:00
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/19 17:00
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class VipGrade implements Serializable, Parcelable {
    private String grade_id;
    private String grade_name;
    private String upgrade_points;
    private double discount;
    private String valid_months;
    private double cover_charge;
    private String grade_bg_img_id;
    private double points_multiple;
    private double min_recharge_money;
    private String grade_ico_img_id;
    private double cover_charge_cut_out;
    private int grade_sort;

    public VipGrade(){

    }

    protected VipGrade(Parcel in) {
        grade_id = in.readString();
        grade_name = in.readString();
        upgrade_points = in.readString();
        discount = in.readDouble();
        valid_months = in.readString();
        cover_charge = in.readDouble();
        grade_bg_img_id = in.readString();
        points_multiple = in.readDouble();
        min_recharge_money = in.readDouble();
        grade_ico_img_id = in.readString();
        cover_charge_cut_out = in.readDouble();
        grade_sort = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(grade_id);
        dest.writeString(grade_name);
        dest.writeString(upgrade_points);
        dest.writeDouble(discount);
        dest.writeString(valid_months);
        dest.writeDouble(cover_charge);
        dest.writeString(grade_bg_img_id);
        dest.writeDouble(points_multiple);
        dest.writeDouble(min_recharge_money);
        dest.writeString(grade_ico_img_id);
        dest.writeDouble(cover_charge_cut_out);
        dest.writeInt(grade_sort);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<VipGrade> CREATOR = new Creator<VipGrade>() {
        @Override
        public VipGrade createFromParcel(Parcel in) {
            return new VipGrade(in);
        }

        @Override
        public VipGrade[] newArray(int size) {
            return new VipGrade[size];
        }
    };

    public void setGrade_id(String grade_id) {
        this.grade_id = grade_id;
    }

    public String getGrade_id() {
        return grade_id;
    }

    public void setGrade_name(String grade_name) {
        this.grade_name = grade_name;
    }

    public String getGrade_name() {
        return grade_name;
    }

    public void setUpgrade_points(String upgrade_points) {
        this.upgrade_points = upgrade_points;
    }

    public String getUpgrade_points() {
        return upgrade_points;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public double getDiscount() {
        return discount;
    }

    public void setValid_months(String valid_months) {
        this.valid_months = valid_months;
    }

    public String getValid_months() {
        return valid_months;
    }

    public void setCover_charge(double cover_charge) {
        this.cover_charge = cover_charge;
    }

    public double getCover_charge() {
        return cover_charge;
    }

    public void setGrade_bg_img_id(String grade_bg_img_id) {
        this.grade_bg_img_id = grade_bg_img_id;
    }

    public String getGrade_bg_img_id() {
        return grade_bg_img_id;
    }

    public void setPoints_multiple(double points_multiple) {
        this.points_multiple = points_multiple;
    }

    public double getPoints_multiple() {
        return points_multiple;
    }

    public void setCover_charge_cut_out(double cover_charge_cut_out) {
        this.cover_charge_cut_out = cover_charge_cut_out;
    }

    public double getCover_charge_cut_out() {
        return cover_charge_cut_out;
    }

    public void setGrade_ico_img_id(String grade_ico_img_id) {
        this.grade_ico_img_id = grade_ico_img_id;
    }

    public String getGrade_ico_img_id() {
        return grade_ico_img_id;
    }

    public void setGrade_sort(int grade_sort) {
        this.grade_sort = grade_sort;
    }

    public int getGrade_sort() {
        return grade_sort;
    }

    public void setMin_recharge_money(double min_recharge_money) {
        this.min_recharge_money = min_recharge_money;
    }

    public double getMin_recharge_money() {
        return min_recharge_money;
    }
    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.CHINA,"grade_id[%s]-grade_name[%s]",grade_id,grade_name);
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
