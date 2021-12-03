package com.wyc.cloudapp.data.room.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.data.room.entity
 * @ClassName: PracticeAssociating
 * @Description: 做法关联信息
 * @Author: wyc
 * @CreateDate: 2021-11-30 18:23
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-11-30 18:23
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Entity(tableName = "practiceAssociated",primaryKeys = {"id"})
public class PracticeAssociated implements Parcelable {
    private Integer id;
    private String barcode_id;
    private Integer kw_id;

    @Nullable
    @ColumnInfo(defaultValue = "")
    private String kw_code;

    @Nullable
    @ColumnInfo(defaultValue = "")
    private String kw_name;
    @ColumnInfo(defaultValue = "0.0")
    private Double kw_price;
    /**
     * 1为正常 2为删除
     * */
    private Integer status;

    @Ignore
    @JSONField(serialize =false)
    private boolean sel;

    public PracticeAssociated(){

    }

    public static String[] getFieldsName(){
        return new String[]{"id","barcode_id","kw_id","kw_code","kw_name","kw_price","status"};
    }

    protected PracticeAssociated(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        barcode_id = in.readString();
        if (in.readByte() == 0) {
            kw_id = null;
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

    public static final Creator<PracticeAssociated> CREATOR = new Creator<PracticeAssociated>() {
        @Override
        public PracticeAssociated createFromParcel(Parcel in) {
            return new PracticeAssociated(in);
        }

        @Override
        public PracticeAssociated[] newArray(int size) {
            return new PracticeAssociated[size];
        }
    };

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getBarcode_id() {
        return barcode_id;
    }

    public void setBarcode_id(String barcode_id) {
        this.barcode_id = barcode_id;
    }

    public Integer getKw_id() {
        return kw_id;
    }

    public void setKw_id(Integer kw_id) {
        this.kw_id = kw_id;
    }

    public String getKw_code() {
        return kw_code;
    }

    public void setKw_code(String kw_code) {
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

    public boolean getSel() {
        return sel;
    }

    public void setSel(boolean sel) {
        this.sel = sel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PracticeAssociated that = (PracticeAssociated) o;
        return Objects.equals(id, that.id) && Objects.equals(barcode_id, that.barcode_id) && Objects.equals(kw_code, that.kw_code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, barcode_id, kw_code);
    }

    @NonNull
    @Override
    public String toString() {
        return "PracticeAssociated{" +
                "id=" + id +
                ", barcode_id='" + barcode_id + '\'' +
                ", kw_id=" + kw_id +
                ", kw_code='" + kw_code + '\'' +
                ", kw_name='" + kw_name + '\'' +
                ", kw_price=" + kw_price +
                ", status=" + status +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(barcode_id);
        if (kw_id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(kw_id);
        }
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
}
