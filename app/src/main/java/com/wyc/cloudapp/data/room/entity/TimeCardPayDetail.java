package com.wyc.cloudapp.data.room.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.room.AppDatabase;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.data.room.entity
 * @ClassName: TimeCardPayDetail
 * @Description: 次卡销售支付明细
 * @Author: wyc
 * @CreateDate: 2021-07-09 13:47
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-09 13:47
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Entity(tableName = "timeCardPayDetail",primaryKeys = {"rowId","order_no"})
public class TimeCardPayDetail implements Parcelable {
    private int rowId;
    /*
    * 对应的销售订单号
    * */
    @NonNull
    private String order_no;
    private int pay_method_id;
    private double amt;
    private double zl_amt;
    private String online_pay_no;
    private String remark;
    private int status;
    private String operator;
    private String pay_time;

    public TimeCardPayDetail(@NonNull String order_no) {
        this.order_no = order_no;
    }

    public static void update(List<TimeCardPayDetail> data){
        AppDatabase.getInstance().TimeCardPayDetailDao().updateAll(data);
    }

    public static List<TimeCardPayDetail> getPayDetailByOrderNo(String order_no){
        return AppDatabase.getInstance().TimeCardPayDetailDao().getAllById(order_no);
    }

    public static class Builder{
        private final TimeCardPayDetail detail;
        public Builder(@NonNull String order_no){
            detail = new TimeCardPayDetail(order_no);
            detail.setPay_time(FormatDateTimeUtils.formatCurrentTime(FormatDateTimeUtils.YYYY_MM_DD_1));
        }
        public Builder pay_method_id(int id){
            detail.setPay_method_id(id);
            return this;
        }
        public Builder amt(double amt){
            detail.setAmt(amt);
            return this;
        }
        public Builder zl_amt(double amt){
            detail.setZl_amt(amt);
            return this;
        }
        public Builder online_pay_no(String pay_no){
            detail.setOnline_pay_no(pay_no);
            return this;
        }
        public Builder remark(String remark){
            detail.setRemark(remark);
            return this;
        }
        public Builder status(int status){
            detail.setStatus(status);
            return this;
        }
        public Builder operator(String operator){
            detail.setOperator(operator);
            return this;
        }
        public Builder pay_time(String pay_time){
            detail.setPay_time(pay_time);
            return this;
        }
        public TimeCardPayDetail build(){
            return detail;
        }
    }

    public void success(){
        setStatus(1);
    }
    public void failure(){
        setStatus(2);
    }

    protected TimeCardPayDetail(Parcel in) {
        rowId = in.readInt();
        order_no = in.readString();
        pay_method_id = in.readInt();
        amt = in.readDouble();
        zl_amt = in.readDouble();
        online_pay_no = in.readString();
        remark = in.readString();
        status = in.readInt();
        operator = in.readString();
        pay_time = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(rowId);
        dest.writeString(order_no);
        dest.writeInt(pay_method_id);
        dest.writeDouble(amt);
        dest.writeDouble(zl_amt);
        dest.writeString(online_pay_no);
        dest.writeString(remark);
        dest.writeInt(status);
        dest.writeString(operator);
        dest.writeString(pay_time);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TimeCardPayDetail> CREATOR = new Creator<TimeCardPayDetail>() {
        @Override
        public TimeCardPayDetail createFromParcel(Parcel in) {
            return new TimeCardPayDetail(in);
        }

        @Override
        public TimeCardPayDetail[] newArray(int size) {
            return new TimeCardPayDetail[size];
        }
    };

    private String getPayCode() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS", Locale.CHINA).format(new Date())+ CustomApplication.self().getPosNum() + Utils.getNonce_str(8);
    }

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public int getPay_method_id() {
        return pay_method_id;
    }

    public void setPay_method_id(int pay_method_id) {
        this.pay_method_id = pay_method_id;
    }

    public double getAmt() {
        return amt;
    }

    public void setAmt(double amt) {
        this.amt = amt;
    }

    public double getZl_amt() {
        return zl_amt;
    }

    public void setZl_amt(double zl_amt) {
        this.zl_amt = zl_amt;
    }

    public String getOnline_pay_no() {
        return online_pay_no;
    }

    public void setOnline_pay_no(String online_pay_no) {
        this.online_pay_no = online_pay_no;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @NonNull
    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(@NonNull String order_no) {
        this.order_no = order_no;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getPay_time() {
        return pay_time;
    }

    public void setPay_time(String pay_time) {
        this.pay_time = pay_time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeCardPayDetail that = (TimeCardPayDetail) o;
        return rowId == that.rowId &&
                Objects.equals(order_no, that.order_no);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowId, order_no);
    }

    @NonNull
    @Override
    public String toString() {
        return "TimeCardPayDetails{" +
                "rowId=" + rowId +
                ", order_no='" + order_no + '\'' +
                ", pay_method_id=" + pay_method_id +
                ", amt=" + amt +
                ", online_pay_no='" + online_pay_no + '\'' +
                ", operator='" + operator + '\'' +
                ", pay_time='" + pay_time + '\'' +
                '}';
    }
}
