package com.wyc.cloudapp.bean;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.Serializable;

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
public class VipInfo implements Serializable {
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
    private int mm_type;
    private String pay_pwd;
    private int grade_id;
    private String ref_member_id;
    private String name;
    private String login_pwd;
    private String sex;
    private double money_sum;

    public String getHead_img_id() {
        return head_img_id;
    }
    public void setHead_img_id(String head_img_id) {
        this.head_img_id = head_img_id;
    }
    public String getCard_code() {
        return card_code;
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

    @NonNull
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(VipInfo.class.getName()).append('@').append(Integer.toHexString(System.identityHashCode(this))).append('[');
        sb.append("headImgId");
        sb.append('=');
        sb.append(((this.head_img_id == null)?"<null>":this.head_img_id));
        sb.append("\r\n");
        sb.append("cardCode");
        sb.append('=');
        sb.append(((this.card_code == null)?"<null>":this.card_code));
        sb.append("\r\n");
        sb.append("scId");
        sb.append('=');
        sb.append(((this.sc_id == null)?"<null>":this.sc_id));
        sb.append("\r\n");
        sb.append("idcard");
        sb.append('=');
        sb.append(((this.idcard == null)?"<null>":this.idcard));
        sb.append("\r\n");
        sb.append("discount");
        sb.append('=');
        sb.append(this.discount);
        sb.append("\r\n");
        sb.append("birthday");
        sb.append('=');
        sb.append(((this.birthday == null)?"<null>":this.birthday));
        sb.append("\r\n");
        sb.append("memberId");
        sb.append('=');
        sb.append(member_id);
        sb.append("\r\n");
        sb.append("openid");
        sb.append('=');
        sb.append(((this.openid == null)?"<null>":this.openid));
        sb.append("\r\n");
        sb.append("remarks");
        sb.append('=');
        sb.append(((this.remarks == null)?"<null>":this.remarks));
        sb.append("\r\n");
        sb.append("addtime");
        sb.append('=');
        sb.append(((this.addtime == null)?"<null>":this.addtime));
        sb.append("\r\n");
        sb.append("moneyCreditLimit");
        sb.append('=');
        sb.append(this.money_credit_limit);
        sb.append("\r\n");
        sb.append("status");
        sb.append('=');
        sb.append(this.status);
        sb.append("\r\n");
        sb.append("relegatedTime");
        sb.append('=');
        sb.append(((this.relegated_time == null)?"<null>":this.relegated_time));
        sb.append("\r\n");
        sb.append("birthdayToday");
        sb.append('=');
        sb.append(this.birthday_today);
        sb.append("\r\n");
        sb.append("gradeName");
        sb.append('=');
        sb.append(((this.gradeName == null)?"<null>":this.gradeName));
        sb.append("\r\n");
        sb.append("minRechargeMoney");
        sb.append('=');
        sb.append(this.min_recharge_money);
        sb.append("\r\n");
        sb.append("pointsSum");
        sb.append('=');
        sb.append(this.points_sum);
        sb.append("\r\n");
        sb.append("upgradeLock");
        sb.append('=');
        sb.append(((this.upgrade_lock == null)?"<null>":this.upgrade_lock));
        sb.append("\r\n");
        sb.append("birthdayType");
        sb.append('=');
        sb.append(this.birthday_type);
        sb.append("\r\n");
        sb.append("mobile");
        sb.append('=');
        sb.append(((this.mobile == null)?"<null>":this.mobile));
        sb.append("\r\n");
        sb.append("upgradePoints");
        sb.append('=');
        sb.append(this.upgrade_points);
        sb.append("\r\n");
        sb.append("mmType");
        sb.append('=');
        sb.append(this.mm_type);
        sb.append("\r\n");
        sb.append("payPwd");
        sb.append('=');
        sb.append(((this.pay_pwd == null)?"<null>":this.pay_pwd));
        sb.append("\r\n");
        sb.append("gradeId");
        sb.append('=');
        sb.append(this.grade_id);
        sb.append("\r\n");
        sb.append("refMemberId");
        sb.append('=');
        sb.append(((this.ref_member_id == null)?"<null>":this.ref_member_id));
        sb.append("\r\n");
        sb.append("name");
        sb.append('=');
        sb.append(((this.name == null)?"<null>":this.name));
        sb.append("\r\n");
        sb.append("loginPwd");
        sb.append('=');
        sb.append(((this.login_pwd == null)?"<null>":this.login_pwd));
        sb.append("\r\n");
        sb.append("sex");
        sb.append('=');
        sb.append(((this.sex == null)?"<null>":this.sex));
        sb.append("\r\n");
        sb.append("moneySum");
        sb.append('=');
        sb.append(this.money_sum);
        sb.append(']');
        return sb.toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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
        if (!(other instanceof VipInfo)) {
            return false;
        }
        VipInfo rhs = ((VipInfo) other);
        return member_id == rhs.member_id && (card_code != null && card_code.equals(rhs.card_code));
    }
    public boolean isEmpty(){
        return card_code == null || "".equals(card_code);
    }
}
