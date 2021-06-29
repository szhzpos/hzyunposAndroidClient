package com.wyc.cloudapp.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: OnceCardInfo
 * @Description: 次卡信息
 * @Author: wyc
 * @CreateDate: 2021-06-29 17:15
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-29 17:15
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class OnceCardInfo {
    private String end_time;
    private int tc_xtype;
    private String start_time;
    private String surplus;
    private int once_card_id;
    private String validity_types;
    private int validity_type;
    private String details;
    private String mnemonic;
    private String addtime;
    private int max_day;
    private String available_limits;
    private int available;
    private int channel;
    private String channels;
    private String img_big;
    private String img;
    private String tc_xtypes;
    private double tc_money;
    private double price;
    private String title;
    private int available_limit;
    private int sy_limit;
    private String sy_limit_types;
    private int total;

    private List<GoodInfo> goods;

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setTc_xtype(int tc_xtype) {
        this.tc_xtype = tc_xtype;
    }

    public int getTc_xtype() {
        return tc_xtype;
    }

    public void setSy_limit_types(String sy_limit_types) {
        this.sy_limit_types = sy_limit_types;
    }

    public String getSy_limit_types() {
        return sy_limit_types;
    }

    public void setGoods(List<GoodInfo> goods) {
        this.goods = goods;
    }

    public List<GoodInfo> getGoods() {
        return goods;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setSurplus(String surplus) {
        this.surplus = surplus;
    }

    public String getSurplus() {
        return surplus;
    }

    public void setOnce_card_id(int once_card_id) {
        this.once_card_id = once_card_id;
    }

    public int getOnce_card_id() {
        return once_card_id;
    }

    public void setValidity_types(String validity_types) {
        this.validity_types = validity_types;
    }

    public String getValidity_types() {
        return validity_types;
    }

    public void setValidity_type(int validity_type) {
        this.validity_type = validity_type;
    }

    public int getValidity_type() {
        return validity_type;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDetails() {
        return details;
    }

    public void setMnemonic(String mnemonic) {
        this.mnemonic = mnemonic;
    }

    public String getMnemonic() {
        return mnemonic;
    }

    public void setAddtime(String addtime) {
        this.addtime = addtime;
    }

    public String getAddtime() {
        return addtime;
    }

    public void setMax_day(int max_day) {
        this.max_day = max_day;
    }

    public int getMax_day() {
        return max_day;
    }

    public void setAvailable_limits(String available_limits) {
        this.available_limits = available_limits;
    }

    public String getAvailable_limits() {
        return available_limits;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public int getAvailable() {
        return available;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannels(String channels) {
        this.channels = channels;
    }

    public String getChannels() {
        return channels;
    }

    public void setImg_big(String img_big) {
        this.img_big = img_big;
    }

    public String getImg_big() {
        return img_big;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public String getImg() {
        return img;
    }

    public void setTc_xtypes(String tc_xtypes) {
        this.tc_xtypes = tc_xtypes;
    }

    public String getTc_xtypes() {
        return tc_xtypes;
    }

    public void setTc_money(double tc_money) {
        this.tc_money = tc_money;
    }

    public double getTc_money() {
        return tc_money;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setAvailable_limit(int available_limit) {
        this.available_limit = available_limit;
    }

    public int getAvailable_limit() {
        return available_limit;
    }

    public void setSy_limit(int sy_limit) {
        this.sy_limit = sy_limit;
    }

    public int getSy_limit() {
        return sy_limit;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getTotal() {
        return total;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+ once_card_id);
        result = ((result* 31)+((this.title == null)? 0 :this.title.hashCode()));
        return result;
    }

    @Override
    public boolean equals(@Nullable Object other) {
        if (this == other){
            return true;
        }
        if (!(other instanceof OnceCardInfo)) {
            return false;
        }
        OnceCardInfo rhs = ((OnceCardInfo) other);
        return once_card_id == rhs.once_card_id && (title != null && title.equals(rhs.title));
    }

    @NonNull
    @Override
    public String toString() {
        return OnceCardInfo.class.getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' +
                "once_card_id = " +
                once_card_id +
                "\r\n" +
                "title = " +
                ((this.title == null) ? "<null>" : this.title) +
                "\r\n" +
                "goods = " +
                ((this.goods == null) ? "<null>" : Arrays.toString(goods.toArray())) +
                "\r\n" +
                ']';
    }

    private static class GoodInfo implements Parcelable {

        private String goods_title;
        private int barcode_id;
        private String only_coding;
        private String barcode;
        private String unit;
        private int num;
        private String goods_spec_code;

        public GoodInfo(){

        }
        protected GoodInfo(Parcel in) {
            goods_title = in.readString();
            barcode_id = in.readInt();
            only_coding = in.readString();
            barcode = in.readString();
            unit = in.readString();
            num = in.readInt();
            goods_spec_code = in.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(goods_title);
            dest.writeInt(barcode_id);
            dest.writeString(only_coding);
            dest.writeString(barcode);
            dest.writeString(unit);
            dest.writeInt(num);
            dest.writeString(goods_spec_code);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<GoodInfo> CREATOR = new Creator<GoodInfo>() {
            @Override
            public GoodInfo createFromParcel(Parcel in) {
                return new GoodInfo(in);
            }

            @Override
            public GoodInfo[] newArray(int size) {
                return new GoodInfo[size];
            }
        };

        public void setGoods_title(String goods_title) {
            this.goods_title = goods_title;
        }

        public String getGoods_title() {
            return goods_title;
        }

        public void setBarcode_id(int barcode_id) {
            this.barcode_id = barcode_id;
        }

        public int getBarcode_id() {
            return barcode_id;
        }

        public void setOnly_coding(String only_coding) {
            this.only_coding = only_coding;
        }

        public String getOnly_coding() {
            return only_coding;
        }

        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }

        public String getBarcode() {
            return barcode;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getUnit() {
            return unit;
        }

        public void setNum(int num) {
            this.num = num;
        }

        public int getNum() {
            return num;
        }

        public void setGoods_spec_code(String goods_spec_code) {
            this.goods_spec_code = goods_spec_code;
        }

        public String getGoods_spec_code() {
            return goods_spec_code;
        }


        @Override
        public int hashCode() {
            int result = 1;
            result = ((result* 31)+ barcode_id);
            result = ((result* 31)+((this.barcode == null)? 0 :this.barcode.hashCode()));
            result = ((result* 31)+((this.goods_title == null)? 0 :this.goods_title.hashCode()));
            return result;
        }

        @Override
        public boolean equals(@Nullable Object other) {
            if (this == other){
                return true;
            }
            if (!(other instanceof OnceCardInfo.GoodInfo)) {
                return false;
            }
            OnceCardInfo.GoodInfo rhs = ((OnceCardInfo.GoodInfo) other);
            return barcode_id == rhs.barcode_id && (goods_title != null && goods_title.equals(rhs.goods_title)) && (barcode != null && barcode.equals(rhs.barcode));
        }

        @NonNull
        @Override
        public String toString() {
            return GoodInfo.class.getName() + '@' + Integer.toHexString(System.identityHashCode(this)) + '[' +
                    "barcode_id" +
                    '=' +
                    barcode_id +
                    "\r\n" +
                    "barcode" +
                    '=' +
                    ((this.barcode == null) ? "<null>" : this.barcode) +
                    "\r\n" +
                    "goods_title" +
                    '=' +
                    ((this.goods_title == null) ? "<null>" : this.goods_title) +
                    "\r\n" +
                    ']';
        }
    }
}
