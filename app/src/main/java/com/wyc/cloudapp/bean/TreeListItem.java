package com.wyc.cloudapp.bean;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: TreeList
 * @Description: 树形结构内容对象
 * @Author: wyc
 * @CreateDate: 2021/5/10 16:15
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/10 16:15
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class TreeListItem implements Cloneable,Serializable {
        /*    Item{
            p_ref,level,unfold,isSel,item_id,item_name,kids; <p_ref , kids>存在上下级时必须存在
        }*/
    private TreeListItem p_ref;//父对象
    private int level;//所属层
    private boolean unfold;//是否折叠
    private boolean isSel;//是否选中
    private String item_id;//唯一标识
    private String code;//外部标识
    private String item_name;//名称
    private List<TreeListItem> kids;//子对象数组

    public TreeListItem getP_ref() {
        return p_ref;
    }

    public void setP_ref(TreeListItem p_ref) {
        this.p_ref = p_ref;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void setUnfold(boolean unfold) {
        this.unfold = unfold;
    }

    public boolean isUnfold() {
        return unfold;
    }

    public void setSel(boolean sel) {
        isSel = sel;
    }

    public boolean isSel() {
        return isSel;
    }

    public String getItem_id() {
        if (item_id == null)return "";
        return item_id;
    }

    public void setItem_id(String item_id) {
        this.item_id = item_id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getItem_name() {
        return item_name;
    }

    public void setItem_name(String item_name) {
        this.item_name = item_name;
    }

    public List<TreeListItem> getKids() {
        if (kids == null)return new ArrayList<>();
        return kids;
    }

    public void setKids(List<TreeListItem> kids) {
        this.kids = kids;
    }

    @Override
    public TreeListItem clone() {
        TreeListItem o = null;
        try {
            o = (TreeListItem) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return o;
    }
    public static TreeListItem getViewTagValue(@NonNull final View v){
        final Object o = v.getTag();
        if (o instanceof TreeListItem){
            return (TreeListItem)o;
        }
        return new TreeListItem();
    }

    public boolean isEmpty(){
        return item_id == null || "".equals(item_id);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.CHINA,"item_id:%s,item_name:%s",item_id,item_name);
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
