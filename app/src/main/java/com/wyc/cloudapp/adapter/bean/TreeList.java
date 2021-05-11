package com.wyc.cloudapp.adapter.bean;

import android.view.View;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.bean
 * @ClassName: TreeList
 * @Description: java类作用描述
 * @Author: wyc
 * @CreateDate: 2021/5/10 16:15
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/5/10 16:15
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TreeList implements Cloneable,Serializable {
        /*    Item{
            p_ref,level,unfold,isSel,item_id,item_name,kids; <p_ref , kids>存在上下级时必须存在
        }*/
    private TreeList p_ref;
    private int level;
    private boolean unfold;
    private boolean isSel;
    private String item_id;
    private String code;
    private String item_name;
    private List<TreeList> kids;

    public TreeList getP_ref() {
        return p_ref;
    }

    public void setP_ref(TreeList p_ref) {
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

    public List<TreeList> getKids() {
        if (kids == null)return new ArrayList<>();
        return kids;
    }

    public void setKids(List<TreeList> kids) {
        this.kids = kids;
    }

    @Override
    public TreeList clone() {
        TreeList o = null;
        try {
            o = (TreeList) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return o;
    }
    public static TreeList getViewTagValue(@NonNull final View v){
        final Object o = v.getTag();
        if (o instanceof TreeList){
            return (TreeList)o;
        }
        return new TreeList();
    }

    public boolean isEmpty(){
        return item_id == null || "".equals(item_id);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.CHINA,"item_id:%s,item_name:%s",item_id,item_name);
    }
}
