package com.wyc.cloudapp.adapter.bean;

import androidx.annotation.NonNull;

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
public class PromotionRule implements Comparable<PromotionRule> {
    int tlp_id;
    double price,upper_limit_num,lower_limit_num;

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

