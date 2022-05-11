package com.wyc.cloudapp.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.wyc.cloudapp.utils.Utils;
import com.wyc.label.LabelGoods;

import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: PrintLabelGoods
 * @Description: 标签打印内容
 * @Author: wyc
 * @CreateDate: 2022/5/11 10:46
 * @UpdateUser: 更新者：
 * @UpdateDate: 2022/5/11 10:46
 * @UpdateRemark: 更新说明：
 * @Version: 1.0
 */
final public class PrintLabelGoods implements Parcelable{
    private String barcodeId;
    private String goodsTitle = "";
    private String barcode = "";
    private String unit = "";
    private String origin = "";
    private String spec = "";
    private double yh_price  = 0.0;
    private double retail_price = 0.0;
    private int num = 1;

    public PrintLabelGoods(){

    }

    protected PrintLabelGoods(Parcel in) {
        barcodeId = in.readString();
        goodsTitle = in.readString();
        barcode = in.readString();
        unit = in.readString();
        origin = in.readString();
        spec = in.readString();
        yh_price = in.readDouble();
        retail_price = in.readDouble();
        num = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(barcodeId);
        dest.writeString(goodsTitle);
        dest.writeString(barcode);
        dest.writeString(unit);
        dest.writeString(origin);
        dest.writeString(spec);
        dest.writeDouble(yh_price);
        dest.writeDouble(retail_price);
        dest.writeInt(num);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<PrintLabelGoods> CREATOR = new Parcelable.Creator<PrintLabelGoods>() {
        @Override
        public PrintLabelGoods createFromParcel(Parcel in) {
            return new PrintLabelGoods(in);
        }

        @Override
        public PrintLabelGoods[] newArray(int size) {
            return new PrintLabelGoods[size];
        }
    };

    public String getBarcodeId() {
        return barcodeId;
    }

    public void setBarcodeId(String barcodeId) {
        this.barcodeId = barcodeId;
    }

    public String getGoodsTitle() {
        return goodsTitle;
    }

    public void setGoodsTitle(String goodsTitle) {
        this.goodsTitle = goodsTitle;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getSpec() {
        if (Utils.isNotEmpty(spec))return spec;
        return "无";
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public double getYh_price() {
        return yh_price;
    }

    public void setYh_price(double yh_price) {
        this.yh_price = yh_price;
    }

    public double getRetail_price() {
        return retail_price;
    }

    public void setRetail_price(double retail_price) {
        this.retail_price = retail_price;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        if (num < 1)num = 1;
        this.num = num;
    }

    public LabelGoods toLabelGoods(){
        final LabelGoods goods = new LabelGoods();
        goods.setBarcodeId(barcodeId);
        goods.setBarcode(barcode);
        goods.setGoodsTitle(goodsTitle);
        goods.setOrigin(origin);
        goods.setSpec(spec);
        goods.setUnit(unit);
        goods.setRetail_price(retail_price);
        goods.setYh_price(yh_price);
        return goods;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrintLabelGoods that = (PrintLabelGoods) o;
        return barcodeId == that.barcodeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(barcodeId);
    }

    @Override
    public String toString() {
        return "PrintLabelGoods{" +
                "barcodeId=" + barcodeId +
                ", goodsTitle='" + goodsTitle + '\'' +
                ", barcode='" + barcode + '\'' +
                ", unit='" + unit + '\'' +
                ", origin='" + origin + '\'' +
                ", spec='" + spec + '\'' +
                ", yh_price=" + yh_price +
                ", retail_price=" + retail_price +
                ", num=" + num +
                '}';
    }
}
