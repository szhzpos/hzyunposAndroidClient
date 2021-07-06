package com.wyc.cloudapp.bean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.Locale;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: OnceCardPayInfo
 * @Description: 次卡支付信息
 * @Author: wyc
 * @CreateDate: 2021-07-06 15:36
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-06 15:36
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class OnceCardPayInfo implements Serializable {
    private int if_pwd;
    private String order_code;
    private PayInfo pay_info;

    public void setIf_pwd(int if_pwd) {
        this.if_pwd = if_pwd;
    }

    public int getIf_pwd() {
        return if_pwd;
    }

    public void setOrder_code(String order_code) {
        this.order_code = order_code;
    }

    public String getOrder_code() {
        return order_code;
    }

    public void setPay_info(PayInfo pay_info) {
        this.pay_info = pay_info;
    }

    public PayInfo getPay_info() {
        return pay_info;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.CHINA,"{if_pwd:%d,order_code:%s,pay_info:%s}",if_pwd,order_code,pay_info);
    }

    public static class PayInfo implements Serializable{
        private String goods_titles;
        private String order_code;
        private double pay_money;
        private String pay_code;

        public void setGoods_titles(String goods_titles) {
            this.goods_titles = goods_titles;
        }

        public String getGoods_titles() {
            return goods_titles;
        }

        public void setOrder_code(String order_code) {
            this.order_code = order_code;
        }

        public String getOrder_code() {
            return order_code;
        }

        public void setPay_money(double pay_money) {
            this.pay_money = pay_money;
        }

        public double getPay_money() {
            return pay_money;
        }

        public void setPay_code(String pay_code) {
            this.pay_code = pay_code;
        }

        public String getPay_code() {
            return pay_code;
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            return super.equals(obj);
        }

        @NonNull
        @Override
        public String toString() {
            return String.format(Locale.CHINA,"{goods_titles:%s,order_code:%s,pay_money:%f,pay_code:%s}",goods_titles,order_code,pay_money,pay_code);
        }
    }
}
