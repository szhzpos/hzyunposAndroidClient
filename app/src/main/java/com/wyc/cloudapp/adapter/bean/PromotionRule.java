package com.wyc.cloudapp.adapter.bean;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

/**
 * @ProjectName: CloudApp
 * @Package: com.wyc.cloudapp.adapter.bean
 * @ClassName: PromotionRule
 * @Description: 促销规则
 * @Author: wyc
 * @CreateDate: 2021/3/12 15:37
 * @UpdateUser: 更新者
 * @UpdateDate: 2021/3/12 15:37
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class PromotionRule implements Comparable<PromotionRule> , Serializable {
    private int tlp_id,promotion_type;
    private double price,upper_limit_num,lower_limit_num;
    private String type_detail_id;
    private double current_sum_promotion_xnum;
    private double current_goods_num;
    private double current_goods_promotion_num;
    private List<Integer> not_promotion_barcode_ids;

    public void setTlp_id(int tlp_id) {
        this.tlp_id = tlp_id;
    }

    public int getTlp_id() {
        return tlp_id;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPrice() {
        return price;
    }

    public void setLower_limit_num(double lower_limit_num) {
        this.lower_limit_num = lower_limit_num;
    }

    public double getLower_limit_num() {
        return lower_limit_num;
    }

    public void setUpper_limit_num(double upper_limit_num) {
        this.upper_limit_num = upper_limit_num;
    }

    public double getUpper_limit_num() {
        return upper_limit_num;
    }

    public void setPromotion_type(int promotion_type) {
        this.promotion_type = promotion_type;
    }

    public int getPromotion_type() {
        return promotion_type;
    }

    public void setType_detail_id(String type_detail_id) {
        this.type_detail_id = type_detail_id;
    }

    public String getType_detail_id() {
        return type_detail_id;
    }

    public void setCurrent_sum_promotion_xnum(double current_sum_promotion_xnum) {
        this.current_sum_promotion_xnum = current_sum_promotion_xnum;
    }

    public double getCurrent_sum_promotion_xnum() {
        return current_sum_promotion_xnum;
    }

    public void setCurrent_goods_num(double current_goods_num) {
        this.current_goods_num = current_goods_num;
    }

    public double getCurrent_goods_num() {
        return current_goods_num;
    }

    public void setCurrent_goods_promotion_num(double current_goods_promotion_num) {
        this.current_goods_promotion_num = current_goods_promotion_num;
    }

    public double getCurrent_goods_promotion_num() {
        return current_goods_promotion_num;
    }

    public void setNot_promotion_barcode_ids(List<Integer> not_promotion_barcode_ids) {
        this.not_promotion_barcode_ids = not_promotion_barcode_ids;
    }

    public List<Integer> getNot_promotion_barcode_ids() {
        return not_promotion_barcode_ids;
    }

    @Override
    public int compareTo(PromotionRule o) {
        return Double.compare(price,o.price);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.CHINA,"tlp_id:%d,price:%f,upper_limit_num:%f,lower_limit_num:%f",tlp_id,price,upper_limit_num,lower_limit_num);
    }
}

