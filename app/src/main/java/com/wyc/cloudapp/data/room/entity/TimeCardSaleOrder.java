package com.wyc.cloudapp.data.room.entity;

import android.os.CountDownTimer;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.bean.VipInfo;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.data.room.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.data.room.entity
 * @ClassName: TimeCardSaleOrder
 * @Description: 次卡销售订单
 * @Author: wyc
 * @CreateDate: 2021-07-09 10:43
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-09 10:43
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Entity(tableName = "timeCardSaleOrder")
public final class TimeCardSaleOrder implements Parcelable {
    @PrimaryKey
    @NonNull
    private String order_no;
    private String online_order_no;
    private String vip_openid;
    private String vip_card_no;
    private String vip_mobile;
    private String vip_name;
    private double amt;
    private int status;
    private String saleman;
    private String operator;
    @ColumnInfo(defaultValue = "(datetime('now', 'localtime'))")
    private String time;

    @Ignore
    private List<TimeCardSaleInfo> saleInfo;

    public TimeCardSaleOrder() {
        order_no = generateOrderNo();
    }

    public static class Builder{
        private final TimeCardSaleOrder order;
        public Builder(){
            order = new TimeCardSaleOrder();
        }

        public Builder order_no(String order_no){
            order.setOrder_no(order_no);
            return this;
        }
        public Builder online_order_no(String order_no){
            order.setOnline_order_no(order_no);
            return this;
        }

        public Builder vip_openid(String openid){
            order.setVip_openid(openid);
            return this;
        }

        public Builder vip_card_no(String card_no){
            order.setVip_card_no(card_no);
            return this;
        }
        public Builder vip_mobile(String mobile){
            order.setVip_mobile(mobile);
            return this;
        }
        public Builder vip_name(String name){
            order.setVip_name(name);
            return this;
        }
        public Builder amt(double amt){
            order.setAmt(amt);
            return this;
        }
        public Builder status(int status){
            order.setStatus(status);
            return this;
        }
        public Builder operator(String operator){
            order.setOperator(operator);
            return this;
        }
        public Builder saleman(String saleman){
            order.setSaleman(saleman);
            return this;
        }
        public Builder time(String time){
            order.setTime(time);
            return this;
        }
        public Builder saleInfo(List<TimeCardSaleInfo> saleInfoList){
            order.setSaleInfo(saleInfoList);
            return this;
        }

        public TimeCardSaleOrder build(){
            return  order;
        }
    }

    protected TimeCardSaleOrder(Parcel in) {
        order_no = in.readString();
        online_order_no = in.readString();
        vip_openid = in.readString();
        vip_card_no = in.readString();
        vip_mobile = in.readString();
        vip_name = in.readString();
        amt = in.readDouble();
        status = in.readInt();
        saleman = in.readString();
        operator = in.readString();
        time = in.readString();
        saleInfo = in.createTypedArrayList(TimeCardSaleInfo.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(order_no);
        dest.writeString(online_order_no);
        dest.writeString(vip_openid);
        dest.writeString(vip_card_no);
        dest.writeString(vip_mobile);
        dest.writeString(vip_name);
        dest.writeDouble(amt);
        dest.writeInt(status);
        dest.writeString(saleman);
        dest.writeString(operator);
        dest.writeString(time);
        dest.writeTypedList(saleInfo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TimeCardSaleOrder> CREATOR = new Creator<TimeCardSaleOrder>() {
        @Override
        public TimeCardSaleOrder createFromParcel(Parcel in) {
            return new TimeCardSaleOrder(in);
        }

        @Override
        public TimeCardSaleOrder[] newArray(int size) {
            return new TimeCardSaleOrder[size];
        }
    };

    @NonNull
    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(@NonNull String order_no) {
        this.order_no = order_no;
    }

    public String getOnline_order_no() {
        return online_order_no;
    }

    public void setOnline_order_no(String online_order_no) {
        this.online_order_no = online_order_no;
    }

    public String getVip_openid() {
        return vip_openid;
    }

    public void setVip_openid(String vip_openid) {
        this.vip_openid = vip_openid;
    }

    public String getVip_card_no() {
        return vip_card_no;
    }

    public void setVip_card_no(String vip_card_no) {
        this.vip_card_no = vip_card_no;
    }

    public String getVip_mobile() {
        return vip_mobile;
    }

    public void setVip_mobile(String vip_mobile) {
        this.vip_mobile = vip_mobile;
    }

    public String getVip_name() {
        return vip_name;
    }

    public void setVip_name(String vip_name) {
        this.vip_name = vip_name;
    }

    public double getAmt() {
        return amt;
    }

    public void setAmt(double amt) {
        this.amt = amt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSaleman() {
        return saleman;
    }

    public void setSaleman(String saleman) {
        this.saleman = saleman;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<TimeCardSaleInfo> getSaleInfo() {
        return saleInfo;
    }

    public void setSaleInfo(List<TimeCardSaleInfo> saleInfo) {
        if (null != saleInfo)
            for (TimeCardSaleInfo info : saleInfo){
                info.setOrder_no(order_no);
            }
        this.saleInfo = saleInfo;
    }

    public String getStatusName(){
        if (status == 1){
            return CustomApplication.self().getString(R.string.success);
        }else if (status == 2){
            return CustomApplication.self().getString(R.string.failure);
        }else if (status == 3){
            return CustomApplication.self().getString(R.string.paying);
        }else return CustomApplication.self().getString(R.string.uploading);
    }

    public void save(List<TimeCardPayDetail> payDetails){
        AppDatabase.getInstance().TimeCardSaleOrderDao().insertWithDetails(this,saleInfo,payDetails);
    }
    public void updateOnlineOrderNo(String _order_no){//保存线上订单并更新状态为正在支付
        AppDatabase.getInstance().TimeCardSaleOrderDao().updateOrder(order_no,_order_no,3);
    }
    public void success(){
        AppDatabase.getInstance().TimeCardSaleOrderDao().updateOrder(order_no,1);
    }

    public String getCashierName(){
        return SQLiteHelper.getString("select cas_name from cashier_info where cas_code = '"+ operator + "'",null);
    }

    public int getDetailNum(){
        return AppDatabase.getInstance().TimeCardSaleDetailDao().getCountsById(order_no);
    }

    private static String generateOrderNo() {
        int row = AppDatabase.getInstance().TimeCardSaleOrderDao().count() + 1;
        return "CK" + CustomApplication.self().getPosNum() + "-" + new SimpleDateFormat("yyMMddHHmmss", Locale.CHINA).format(new Date()) + "-" + String.format(Locale.CHINA,"%04d",row);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeCardSaleOrder that = (TimeCardSaleOrder) o;
        return Objects.equals(order_no,that.order_no);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order_no);
    }

    @Override
    public String toString() {
        return "TimeCardSaleOrder{" +
                "order_no='" + order_no + '\'' +
                ", online_order_no='" + online_order_no + '\'' +
                ", vip_openid='" + vip_openid + '\'' +
                ", vip_card_no='" + vip_card_no + '\'' +
                ", vip_mobile='" + vip_mobile + '\'' +
                ", vip_name='" + vip_name + '\'' +
                ", amt=" + amt +
                ", status=" + status +
                ", saleman='" + saleman + '\'' +
                ", operator='" + operator + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
