package com.wyc.cloudapp.data.room.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.Entity;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.data.room.entity
 * @ClassName: GoodsPractice
 * @Description: 商品做法口味
 * @Author: wyc
 * @CreateDate: 2021-11-30 14:48
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-11-30 14:48
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */

@Entity(tableName = "goodsPractice",primaryKeys = {"kw_id","kw_code"})
public class GoodsPractice implements Parcelable {
    @NonNull
    private Integer kw_id = 0;
    @NonNull
    private String kw_code = "";
    private String kw_name;
    private Double kw_price;
    private Integer status;//状态 1为正常 2为删除

    public GoodsPractice(){

    }

    public static String[] getFieldsName(){
        return new String[]{"kw_id","kw_code","kw_price","kw_name","status"};
    }

    protected GoodsPractice(Parcel in) {
        if (in.readByte() == 0) {
            kw_id = 0;
        } else {
            kw_id = in.readInt();
        }
        kw_code = in.readString();
        kw_name = in.readString();
        if (in.readByte() == 0) {
            kw_price = null;
        } else {
            kw_price = in.readDouble();
        }
        if (in.readByte() == 0) {
            status = null;
        } else {
            status = in.readInt();
        }
    }

    public static final Creator<GoodsPractice> CREATOR = new Creator<GoodsPractice>() {
        @Override
        public GoodsPractice createFromParcel(Parcel in) {
            return new GoodsPractice(in);
        }

        @Override
        public GoodsPractice[] newArray(int size) {
            return new GoodsPractice[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) 1);
        dest.writeInt(kw_id);
        dest.writeString(kw_code);
        dest.writeString(kw_name);
        if (kw_price == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeDouble(kw_price);
        }
        if (status == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(status);
        }
    }

    public @NonNull Integer getKw_id() {
        return kw_id;
    }

    public void setKw_id(@NonNull Integer kw_id) {
        this.kw_id = kw_id;
    }

    public @NonNull String getKw_code() {
        return kw_code;
    }

    public void setKw_code(@NonNull String kw_code) {
        this.kw_code = kw_code;
    }

    public String getKw_name() {
        return kw_name;
    }

    public void setKw_name(String kw_name) {
        this.kw_name = kw_name;
    }

    public Double getKw_price() {
        return kw_price;
    }

    public void setKw_price(Double kw_price) {
        this.kw_price = kw_price;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoodsPractice that = (GoodsPractice) o;
        return Objects.equals(kw_id, that.kw_id) && Objects.equals(kw_code, that.kw_code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(kw_id, kw_code);
    }

    @NonNull
    @Override
    public String toString() {
        return "GoodsPractice{" +
                "kw_id=" + kw_id +
                ", kw_code='" + kw_code + '\'' +
                ", kw_name='" + kw_name + '\'' +
                ", kw_price=" + kw_price +
                ", status=" + status +
                '}';
    }
}
