package com.wyc.cloudapp.adapter.bean;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.bean
 * @ClassName: FullReduceRule
 * @Description: 满减规则
 * @Author: wyc
 * @CreateDate: 2021/3/12 15:40
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/3/12 15:40
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class FullReduceRule implements Comparable<FullReduceRule>, Serializable {
    int tlpb_id;
    String title;
    int type_detail_id;
    int promotion_type;
    int promotion_object;
    int cumulation_give;
    double buyfull_money;
    double reduce_money;

    public void setTlpb_id(int tlp_id) {
        this.tlpb_id = tlp_id;
    }

    public int getTlpb_id() {
        return tlpb_id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setType_detail_id(int type_detail_id) {
        this.type_detail_id = type_detail_id;
    }

    public int getType_detail_id() {
        return type_detail_id;
    }

    public void setPromotion_type(int promotion_type) {
        this.promotion_type = promotion_type;
    }

    public int getPromotion_type() {
        return promotion_type;
    }

    public void setPromotion_object(int promotion_object) {
        this.promotion_object = promotion_object;
    }

    public int getPromotion_object() {
        return promotion_object;
    }

    public void setCumulation_give(int cumulation_give) {
        this.cumulation_give = cumulation_give;
    }

    public int getCumulation_give() {
        return cumulation_give;
    }

    public void setBuyfull_money(double buyfull_money) {
        this.buyfull_money = buyfull_money;
    }

    public double getBuyfull_money() {
        return buyfull_money;
    }

    public void setReduce_money(double reduce_money) {
        this.reduce_money = reduce_money;
    }

    public double getReduce_money() {
        return reduce_money;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.CHINA,"tlpb_id:%d,buyfull_money:%f,reduce_money:%f",tlpb_id,buyfull_money,reduce_money);
    }

    @Override
    public int compareTo(FullReduceRule o) {
        return Double.compare(buyfull_money,o.buyfull_money);
    }
}
