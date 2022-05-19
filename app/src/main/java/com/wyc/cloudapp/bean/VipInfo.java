package com.wyc.cloudapp.bean;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.bean
 * @ClassName: VipInfo
 * @Description: 会员信息对象
 * @Author: wyc
 * @CreateDate: 2021-06-29 14:41
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-06-29 14:41
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
final public class VipInfo implements Serializable {
    private String head_img_id;
    private String card_code;
    private String sc_id;
    private String idcard;
    private double discount;
    private String birthday;
    private int member_id;
    private String openid;
    private String remarks;
    private String addtime;
    private double money_credit_limit;
    private int status;
    private String relegated_time;
    private int birthday_today;
    private String gradeName;
    private double min_recharge_money;
    private double points_sum;
    private String upgrade_lock;
    private int birthday_type;
    private String mobile;
    private double upgrade_points;
    private double minimum_money;
    private int mm_type;
    private String pay_pwd;
    private int grade_id;
    private String ref_member_id;
    private String name;
    private String login_pwd;
    private String sex;
    private double money_sum;

    public void setBirthday_today(int birthday_today) {
        this.birthday_today = birthday_today;
    }

    public double getMinimum_money() {
        return minimum_money;
    }

    public void setMinimum_money(double minimum_money) {
        this.minimum_money = minimum_money;
    }

    public String getHead_img_id() {
        return head_img_id;
    }
    public void setHead_img_id(String head_img_id) {
        this.head_img_id = head_img_id;
    }
    public String getCard_code() {
        return card_code == null ? "" : card_code;
    }
    public void setCard_code(String card_code) {
        this.card_code = card_code;
    }
    public String getSc_id() {
        return sc_id;
    }
    public void setSc_id(String sc_id) {
        this.sc_id = sc_id;
    }
    public String getIdcard() {
        return idcard;
    }
    public void setIdcard(String idcard) {
        this.idcard = idcard;
    }
    public double getDiscount() {
        return discount;
    }
    public void setDiscount(double discount) {
        this.discount = discount;
    }
    public String getBirthday() {
        return birthday;
    }
    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }
    public int getMember_id() {
        return member_id;
    }
    public void setMember_id(int member_id) {
        this.member_id = member_id;
    }
    public String getOpenid() {
        return openid;
    }
    public void setOpenid(String openid) {
        this.openid = openid;
    }
    public String getRemarks() {
        return remarks;
    }
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    public String getAddtime() {
        return addtime;
    }
    public void setAddtime(String addtime) {
        this.addtime = addtime;
    }

    public double getMoney_credit_limit() {
        return money_credit_limit;
    }
    public void setMoney_credit_limit(double money_credit_limit) {
        this.money_credit_limit = money_credit_limit;
    }
    public int getStatus() {
        return status;
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public String getRelegated_time() {
        return relegated_time;
    }
    public void setRelegated_time(String relegated_time) {
        this.relegated_time = relegated_time;
    }
    public int getBirthday_today() {
        return birthday_today;
    }
    public void setBirthday_today(Integer birthday_today) {
        this.birthday_today = birthday_today;
    }
    public String getGradeName() {
        return gradeName;
    }
    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }
    public double getMin_recharge_money() {
        return min_recharge_money;
    }
    public void setMin_recharge_money(double min_recharge_money) {
        this.min_recharge_money = min_recharge_money;
    }
    public double getPoints_sum() {
        return points_sum;
    }

    public void setPoints_sum(double points_sum) {
        this.points_sum = points_sum;
    }

    public String getUpgrade_lock() {
        return upgrade_lock;
    }

    public void setUpgrade_lock(String upgrade_lock) {
        this.upgrade_lock = upgrade_lock;
    }

    public int getBirthday_type() {
        return birthday_type;
    }

    public void setBirthday_type(int birthday_type) {
        this.birthday_type = birthday_type;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public double getUpgrade_points() {
        return upgrade_points;
    }

    public void setUpgrade_points(double upgrade_points) {
        this.upgrade_points = upgrade_points;
    }

    public int getMm_type() {
        return mm_type;
    }

    public void setMm_type(int mm_type) {
        this.mm_type = mm_type;
    }

    public String getPay_pwd() {
        return pay_pwd;
    }

    public void setPay_pwd(String pay_pwd) {
        this.pay_pwd = pay_pwd;
    }

    public int getGrade_id() {
        return grade_id;
    }

    public void setGrade_id(int grade_id) {
        this.grade_id = grade_id;
    }

    public String getRef_member_id() {
        return ref_member_id;
    }

    public void setRef_member_id(String ref_member_id) {
        this.ref_member_id = ref_member_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogin_pwd() {
        return login_pwd;
    }

    public void setLogin_pwd(String login_pwd) {
        this.login_pwd = login_pwd;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public double getMoney_sum() {
        return money_sum;
    }

    public void setMoney_sum(double money_sum) {
        this.money_sum = money_sum;
    }

    @Override
    public String toString() {
        return "VipInfo{" +
                "head_img_id='" + head_img_id + '\'' +
                ", card_code='" + card_code + '\'' +
                ", sc_id='" + sc_id + '\'' +
                ", idcard='" + idcard + '\'' +
                ", discount=" + discount +
                ", birthday='" + birthday + '\'' +
                ", member_id=" + member_id +
                ", openid='" + openid + '\'' +
                ", remarks='" + remarks + '\'' +
                ", addtime='" + addtime + '\'' +
                ", money_credit_limit=" + money_credit_limit +
                ", status=" + status +
                ", relegated_time='" + relegated_time + '\'' +
                ", birthday_today=" + birthday_today +
                ", gradeName='" + gradeName + '\'' +
                ", min_recharge_money=" + min_recharge_money +
                ", points_sum=" + points_sum +
                ", upgrade_lock='" + upgrade_lock + '\'' +
                ", birthday_type=" + birthday_type +
                ", mobile='" + mobile + '\'' +
                ", upgrade_points=" + upgrade_points +
                ", minimum_money=" + minimum_money +
                ", mm_type=" + mm_type +
                ", pay_pwd='" + pay_pwd + '\'' +
                ", grade_id=" + grade_id +
                ", ref_member_id='" + ref_member_id + '\'' +
                ", name='" + name + '\'' +
                ", login_pwd='" + login_pwd + '\'' +
                ", sex='" + sex + '\'' +
                ", money_sum=" + money_sum +
                '}';
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = ((result* 31)+ member_id);
        result = ((result* 31)+((this.card_code == null)? 0 :this.card_code.hashCode()));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other == null ||(getClass() != other.getClass()))return false;
        VipInfo rhs = ((VipInfo) other);
        return member_id == rhs.member_id && (card_code != null && card_code.equals(rhs.card_code));
    }
    public boolean isEmpty(){
        return card_code == null || "".equals(card_code);
    }
}
