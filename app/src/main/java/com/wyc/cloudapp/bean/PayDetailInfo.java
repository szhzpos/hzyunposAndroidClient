package com.wyc.cloudapp.bean;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @ProjectName:    AndroidClient
 * @Package:        com.wyc.cloudapp.bean
 * @ClassName:      PayDetail
 * @Description:    支付明细
 * @Author:         wyc
 * @CreateDate:     2021-07-22 16:06
 * @UpdateUser:     更新者
 * @UpdateDate:     2021-07-22 16:06
 * @UpdateRemark:   更新说明
 * @Version:        1.0
 */

public final class PayDetailInfo implements Serializable {

    @JSONField(name = "pay_method_id")
    private int method_id = -1;
    @JSONField(name = "name")
    private String method_name = "";
    @JSONField(name = "pamt")
    private double pay_amt = 0.0;
    @JSONField(name = "pzl")
    private double  zl_amt = 0.0;
    @JSONField(name = "v_num")
    private String v_num = ""; //存放支付需要的额外信息，比如移动支付的支付码、会员支付的会员卡号等

    public int getMethod_id() {
        return method_id;
    }

    public void setMethod_id(int method_id) {
        this.method_id = method_id;
    }

    public String getMethod_name() {
        return method_name;
    }

    public void setMethod_name(String method_name) {
        this.method_name = method_name;
    }

    public double getPay_amt() {
        return pay_amt;
    }

    public void setPay_amt(double pay_amt) {
        this.pay_amt = pay_amt;
    }

    public double getZl_amt() {
        return zl_amt;
    }

    public void setZl_amt(double zl_amt) {
        this.zl_amt = zl_amt;
    }

    public String getV_num() {
        return v_num;
    }

    public void setV_num(String v_num) {
        this.v_num = v_num;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PayDetailInfo that = (PayDetailInfo) o;
        return method_id == that.method_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(method_id);
    }

    @Override
    public String toString() {
        return "PayDetailInfo{" +
                "method_id=" + method_id +
                ", method_name='" + method_name + '\'' +
                ", pay_amt=" + pay_amt +
                ", pzl=" + zl_amt +
                ", v_num='" + v_num + '\'' +
                '}';
    }
}