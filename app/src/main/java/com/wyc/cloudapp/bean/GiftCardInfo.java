package com.wyc.cloudapp.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.alibaba.fastjson.annotation.JSONField;
import com.wyc.cloudapp.dialog.MyDialog;

import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: GiftCardInfo
 * @Description: java类作用描述
 * @Author: wyc
 * @CreateDate: 2021-07-21 16:37
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-21 16:37
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class GiftCardInfo implements Parcelable {

    @JSONField(name = "end_time")
    private long endTime;
    @JSONField(name = "card_no")
    private String cardNo;
    @JSONField(name = "tc_xtype")
    private String tcXtype;
    @JSONField(name = "stores_id")
    private int storesId;
    @JSONField(name = "start_time")
    private long startTime;
    @JSONField(name = "pt_user_id")
    private String ptUserId;
    @JSONField(name = "balance")
    private double balance;
    @JSONField(name = "tc_money")
    private double tcMoney;
    @JSONField(name = "price")
    private double price;
    @JSONField(name = "addtime")
    private long addtime;
    @JSONField(name = "status")
    private int status;
    @JSONField(name = "shopping_id")
    private String shoppingId;
    @JSONField(name = "face_money")
    private double faceMoney;
    @JSONField(name = "shopping_name")
    private String shoppingName;
    @JSONField(name = "shopping_status")
    private int shoppingStatus;
    @JSONField(name = "appids")
    private String appids;
    @JSONField(name = "card_chip_no")
    private String cardChipNo;
    @JSONField(name = "makecard_id")
    private String makecardId;

    public GiftCardInfo(){

    }

    public boolean isSale(){
        return status == 2;
    }

    protected GiftCardInfo(Parcel in) {
        endTime = in.readLong();
        cardNo = in.readString();
        tcXtype = in.readString();
        storesId = in.readInt();
        startTime = in.readLong();
        ptUserId = in.readString();
        balance = in.readDouble();
        tcMoney = in.readDouble();
        price = in.readDouble();
        addtime = in.readLong();
        status = in.readInt();
        shoppingId = in.readString();
        faceMoney = in.readDouble();
        shoppingName = in.readString();
        shoppingStatus = in.readInt();
        appids = in.readString();
        cardChipNo = in.readString();
        makecardId = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(endTime);
        dest.writeString(cardNo);
        dest.writeString(tcXtype);
        dest.writeInt(storesId);
        dest.writeLong(startTime);
        dest.writeString(ptUserId);
        dest.writeDouble(balance);
        dest.writeDouble(tcMoney);
        dest.writeDouble(price);
        dest.writeLong(addtime);
        dest.writeInt(status);
        dest.writeString(shoppingId);
        dest.writeDouble(faceMoney);
        dest.writeString(shoppingName);
        dest.writeInt(shoppingStatus);
        dest.writeString(appids);
        dest.writeString(cardChipNo);
        dest.writeString(makecardId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GiftCardInfo> CREATOR = new Creator<GiftCardInfo>() {
        @Override
        public GiftCardInfo createFromParcel(Parcel in) {
            return new GiftCardInfo(in);
        }

        @Override
        public GiftCardInfo[] newArray(int size) {
            return new GiftCardInfo[size];
        }
    };

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public String getCardNo() {
        return cardNo;
    }

    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }

    public String getTcXtype() {
        return tcXtype;
    }

    public void setTcXtype(String tcXtype) {
        this.tcXtype = tcXtype;
    }

    public int getStoresId() {
        return storesId;
    }

    public void setStoresId(int storesId) {
        this.storesId = storesId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getPtUserId() {
        return ptUserId;
    }

    public void setPtUserId(String ptUserId) {
        this.ptUserId = ptUserId;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getTcMoney() {
        return tcMoney;
    }

    public void setTcMoney(double tcMoney) {
        this.tcMoney = tcMoney;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public long getAddtime() {
        return addtime;
    }

    public void setAddtime(long addtime) {
        this.addtime = addtime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getShoppingId() {
        return shoppingId;
    }

    public void setShoppingId(String shoppingId) {
        this.shoppingId = shoppingId;
    }

    public double getFaceMoney() {
        return faceMoney;
    }

    public void setFaceMoney(double faceMoney) {
        this.faceMoney = faceMoney;
    }

    public String getShoppingName() {
        return shoppingName;
    }

    public void setShoppingName(String shoppingName) {
        this.shoppingName = shoppingName;
    }

    public int getShoppingStatus() {
        return shoppingStatus;
    }

    public void setShoppingStatus(int shoppingStatus) {
        this.shoppingStatus = shoppingStatus;
    }

    public String getAppids() {
        return appids;
    }

    public void setAppids(String appids) {
        this.appids = appids;
    }

    public String getCardChipNo() {
        return cardChipNo;
    }

    public void setCardChipNo(String cardChipNo) {
        this.cardChipNo = cardChipNo;
    }

    public String getMakecardId() {
        return makecardId;
    }

    public void setMakecardId(String makecardId) {
        this.makecardId = makecardId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GiftCardInfo that = (GiftCardInfo) o;
        return Objects.equals(shoppingId, that.shoppingId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shoppingId);
    }

    @Override
    public String toString() {
        return "GiftCardInfo{" +
                "endTime=" + endTime +
                ", cardNo='" + cardNo + '\'' +
                ", tcXtype='" + tcXtype + '\'' +
                ", storesId=" + storesId +
                ", startTime=" + startTime +
                ", ptUserId='" + ptUserId + '\'' +
                ", balance=" + balance +
                ", tcMoney=" + tcMoney +
                ", price=" + price +
                ", addtime=" + addtime +
                ", status=" + status +
                ", shoppingId='" + shoppingId + '\'' +
                ", faceMoney=" + faceMoney +
                ", shoppingName='" + shoppingName + '\'' +
                ", shoppingStatus=" + shoppingStatus +
                ", appids='" + appids + '\'' +
                ", cardChipNo='" + cardChipNo + '\'' +
                ", makecardId='" + makecardId + '\'' +
                '}';
    }
}
