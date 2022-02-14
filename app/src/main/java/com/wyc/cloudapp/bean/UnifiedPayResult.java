package com.wyc.cloudapp.bean;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.annotation.JSONField;
import com.wyc.cloudapp.utils.http.callback.Result;

import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: UnifiedPayResult
 * @Description: 统一支付返回结果
 * @Author: wyc
 * @CreateDate: 2021-07-13 14:58
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-13 14:58
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class UnifiedPayResult extends Result {
    @JSONField(serialize = false)
    public static final int SUCCESS = 1;
    @JSONField(serialize = false)
    public static final int FAILURE = 2;
    @JSONField(serialize = false)
    public static final int INPUT_PASSWORD = 3;
    @JSONField(serialize = false)
    public static final int PASSWORD_POP = 4;

    private int res_code;
    private String order_code;
    private String order_code_son;
    private String pay_code;
    private double pay_money;
    private String discount;
    private String discount_xnote;
    private String xnote;
    private String pay_status;
    private String pay_time;

    public int getRes_code() {
        return res_code;
    }

    public void setRes_code(int res_code) {
        this.res_code = res_code;
    }

    public String getOrder_code() {
        return order_code;
    }

    public void setOrder_code(String order_code) {
        this.order_code = order_code;
    }

    public String getOrder_code_son() {
        return order_code_son;
    }

    public void setOrder_code_son(String order_code_son) {
        this.order_code_son = order_code_son;
    }

    public String getPay_code() {
        return pay_code;
    }

    public void setPay_code(String pay_code) {
        this.pay_code = pay_code;
    }

    public double getPay_money() {
        return pay_money;
    }

    public void setPay_money(double pay_money) {
        this.pay_money = pay_money;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getDiscount_xnote() {
        return discount_xnote;
    }

    public void setDiscount_xnote(String discount_xnote) {
        this.discount_xnote = discount_xnote;
    }

    public String getXnote() {
        return xnote;
    }

    public void setXnote(String xnote) {
        this.xnote = xnote;
    }

    public String getPay_status() {
        return pay_status;
    }

    public void setPay_status(String pay_status) {
        this.pay_status = pay_status;
    }

    public String getPay_time() {
        return pay_time;
    }

    public void setPay_time(String pay_time) {
        this.pay_time = pay_time;
    }

    public void copy(@NonNull UnifiedPayResult o){
        setStatus(o.getStatus());
        setInfo(o.getInfo());
        res_code = o.res_code;
        order_code = o.order_code;
        order_code_son = o.order_code_son;
        pay_code = o.pay_code;
        pay_money = o.pay_money;
        discount = o.discount;
        discount_xnote = o.discount_xnote;
        xnote = o.xnote;
        pay_status = o.pay_status;
        pay_time = o.pay_time;
    }

    public void failure(String err){
        res_code = FAILURE;
        setStatus("n");
        setInfo(err);
    }

    @Override
    public boolean isSuccess() {
        return super.isSuccess() && res_code == SUCCESS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnifiedPayResult that = (UnifiedPayResult) o;
        return Objects.equals(order_code, that.order_code) &&
                Objects.equals(order_code_son, that.order_code_son);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order_code, order_code_son);
    }

    @NonNull

    @Override
    public String toString() {
        return "UnifiedPayResult{" +
                "res_code=" + res_code +
                ", order_code='" + order_code + '\'' +
                ", order_code_son='" + order_code_son + '\'' +
                ", pay_code='" + pay_code + '\'' +
                ", pay_money=" + pay_money +
                ", discount='" + discount + '\'' +
                ", discount_xnote='" + discount_xnote + '\'' +
                ", xnote='" + xnote + '\'' +
                ", pay_status='" + pay_status + '\'' +
                ", pay_time='" + pay_time + '\'' +
                '}';
    }
}
