package com.wyc.cloudapp.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: GoodsCategory
 * @Description: 商品类别信息
 * @Author: wyc
 * @CreateDate: 2021-10-12 11:16
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-10-12 11:16
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class GoodsCategory implements Parcelable {
    private int category_id = -1;
    private String category_code;
    private String name;
    private String parent_id;
    private int depth;
    private String path;
    private int status;
    private int sort;

    public GoodsCategory(){

    }

    public GoodsCategory(final Integer id,final String code, final String name){
        category_id = id;
        category_code = code;
        this.name = name;
    }

    protected GoodsCategory(Parcel in) {
        category_id = in.readInt();
        category_code = in.readString();
        name = in.readString();
        parent_id = in.readString();
        depth = in.readInt();
        path = in.readString();
        status = in.readInt();
        sort = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(category_id);
        dest.writeString(category_code);
        dest.writeString(name);
        dest.writeString(parent_id);
        dest.writeInt(depth);
        dest.writeString(path);
        dest.writeInt(status);
        dest.writeInt(sort);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<GoodsCategory> CREATOR = new Creator<GoodsCategory>() {
        @Override
        public GoodsCategory createFromParcel(Parcel in) {
            return new GoodsCategory(in);
        }

        @Override
        public GoodsCategory[] newArray(int size) {
            return new GoodsCategory[size];
        }
    };

    public Integer getCategory_id() {
        return category_id;
    }

    public void setCategory_id(Integer category_id) {
        this.category_id = category_id;
    }

    public String getCategory_code() {
        return category_code;
    }

    public void setCategory_code(String category_code) {
        this.category_code = category_code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getParent_id() {
        return parent_id;
    }

    public void setParent_id(String parent_id) {
        this.parent_id = parent_id;
    }

    public Integer getDepth() {
        return depth;
    }

    public void setDepth(Integer depth) {
        this.depth = depth;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GoodsCategory that = (GoodsCategory) o;
        return Objects.equals(category_id, that.category_id) &&
                Objects.equals(category_code, that.category_code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category_id, category_code);
    }

    @Override
    public String toString() {
        return "GoodsCategory{" +
                "category_id=" + category_id +
                ", category_code='" + category_code + '\'' +
                ", name='" + name + '\'' +
                ", parent_id='" + parent_id + '\'' +
                ", depth=" + depth +
                ", path='" + path + '\'' +
                ", status=" + status +
                ", sort=" + sort +
                '}';
    }
}
