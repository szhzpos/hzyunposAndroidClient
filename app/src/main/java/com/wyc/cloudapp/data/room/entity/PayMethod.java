package com.wyc.cloudapp.data.room.entity;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.UnifiedPayResult;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.dialog.ChangeNumOrPriceDialog;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;
import com.wyc.cloudapp.utils.http.callback.TypeCallback;

import java.io.Serializable;
import java.util.Objects;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.data.room.entity
 * @ClassName: PayMethod
 * @Description: 支付方式
 * @Author: wyc
 * @CreateDate: 2021-07-08 13:40
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-08 13:40
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Entity(tableName = "pay_method")
public final class PayMethod implements Serializable,Cloneable {
    @PrimaryKey
    private int pay_method_id;
    private String name;
    private Integer status;
    private String remark;
    private Integer is_check;
    private String shortcut_key;
    private Integer sort;
    private String xtype;
    private String pay_img;
    private String master_img;
    private Integer is_show_client;
    @ColumnInfo(defaultValue = "1")
    private Integer is_cardno;
    @ColumnInfo(defaultValue = "2")
    private Integer is_scan;
    private String wr_btn_img;
    private String unified_pay_order;
    private String unified_pay_query;
    private String rule;
    @ColumnInfo(defaultValue = "1")
    private Integer is_open;
    private String support;
    @ColumnInfo(defaultValue = "1")
    private Integer is_enable;
    @ColumnInfo(defaultValue = "1")
    private Integer is_moling;

    @Ignore
    public static final int CASH_METHOD_ID = 1;//现金支付方式id
    @Ignore
    public static final int VIP_METHOD_ID = 5;//会员支付方式id
    @Ignore
    private boolean isCur = false;
    @Ignore
    private boolean isDefault;

    public boolean isVipPay(){
        return pay_method_id == VIP_METHOD_ID;
    }

    public int getPay_method_id() {
        return pay_method_id;
    }

    public void setPay_method_id(int pay_method_id) {
        this.pay_method_id = pay_method_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getIs_check() {
        return is_check;
    }

    public void setIs_check(Integer is_check) {
        this.is_check = is_check;
    }

    public String getShortcut_key() {
        return shortcut_key;
    }

    public void setShortcut_key(String shortcut_key) {
        this.shortcut_key = shortcut_key;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public String getXtype() {
        return xtype;
    }

    public void setXtype(String xtype) {
        this.xtype = xtype;
    }

    public String getPay_img() {
        return pay_img;
    }

    public void setPay_img(String pay_img) {
        this.pay_img = pay_img;
    }

    public String getMaster_img() {
        return master_img;
    }

    public void setMaster_img(String master_img) {
        this.master_img = master_img;
    }

    public Integer getIs_show_client() {
        return is_show_client;
    }

    public void setIs_show_client(Integer is_show_client) {
        this.is_show_client = is_show_client;
    }

    public Integer getIs_cardno() {
        return is_cardno;
    }

    public void setIs_cardno(Integer is_cardno) {
        this.is_cardno = is_cardno;
    }

    public Integer getIs_scan() {
        return is_scan;
    }

    public void setIs_scan(Integer is_scan) {
        this.is_scan = is_scan;
    }

    public String getWr_btn_img() {
        return wr_btn_img;
    }

    public void setWr_btn_img(String wr_btn_img) {
        this.wr_btn_img = wr_btn_img;
    }

    public String getUnified_pay_order() {
        return unified_pay_order;
    }

    public void setUnified_pay_order(String unified_pay_order) {
        this.unified_pay_order = unified_pay_order;
    }

    public String getUnified_pay_query() {
        return unified_pay_query;
    }

    public void setUnified_pay_query(String unified_pay_query) {
        this.unified_pay_query = unified_pay_query;
    }

    public String getRule() {
        return rule;
    }

    public void setRule(String rule) {
        this.rule = rule;
    }

    public Integer getIs_open() {
        return is_open;
    }

    public void setIs_open(Integer is_open) {
        this.is_open = is_open;
    }

    public String getSupport() {
        return support;
    }

    public void setSupport(String support) {
        this.support = support;
    }

    public Integer getIs_enable() {
        return is_enable;
    }

    public void setIs_enable(Integer is_enable) {
        this.is_enable = is_enable;
    }

    public Integer getIs_moling() {
        return is_moling;
    }

    public void setIs_moling(Integer is_moling) {
        this.is_moling = is_moling;
    }

    public boolean isCur() {
        return isCur;
    }

    public void setCur(boolean cur) {
        isCur = cur;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public boolean isCheckApi(){
        return is_check == 1;
    }

    /*
    * @param pay_amt 支付金额
    * @param order_code 支付订单号
    * @param order_code_son 子支付订单号，可以用于之后的支付状态查询
    * @param pay_code 需要校验的支付码(比如微信的付款码)
    * @param tag 做日志记录使用，如果为null 则不记录此次付款。
    *
    * @return UnifiedPayResult
    * */
    public UnifiedPayResult payWithApi(MainActivity activity, double pay_amt, @NonNull String order_code, @NonNull String order_code_son, @NonNull String pay_code, String tag){
        final UnifiedPayResult result = new UnifiedPayResult();

        String unified_pay_order = getUnified_pay_order();
        final JEventLoop loop = new JEventLoop();

        if (!Utils.isNotEmpty(unified_pay_order)){
            unified_pay_order = InterfaceURL.UNIFIED_PAY;
        }
        final JSONObject param = new JSONObject();
        param.put("appid",activity.getAppId());
        param.put("stores_id",activity.getStoreId());
        param.put("order_code",order_code);
        param.put("pos_num",activity.getPosNum());
        param.put("is_wuren",2);
        param.put("order_code_son",order_code_son);
        param.put("pay_money",pay_amt);
        param.put("pay_method",pay_method_id);
        param.put("pay_code_str",pay_code);

        final String sz_param = HttpRequest.generate_request_parm(param,activity.getAppSecret());
        String url = activity.getUrl() + unified_pay_order;

        Logger.i("%s:url:%s%s,param:%s",tag,url ,unified_pay_order,sz_param);
        HttpUtils.sendAsyncPost(url,sz_param).enqueue(new TypeCallback<UnifiedPayResult>(UnifiedPayResult.class) {
            @Override
            protected void onError(String msg) {
                result.failure(msg);
                loop.done(0);
            }

            @Override
            protected void onSuccess(UnifiedPayResult data) {
                Logger.i("%s支付返回:%s",tag,data);

                int res_code = data.getRes_code();
                switch (res_code){
                    case UnifiedPayResult.SUCCESS:
                    case UnifiedPayResult.FAILURE:
                        loop.done(1);
                        break;
                    case UnifiedPayResult.INPUT_PASSWORD:
                    case UnifiedPayResult.PASSWORD_POP:
                        while (res_code == UnifiedPayResult.INPUT_PASSWORD || res_code == UnifiedPayResult.PASSWORD_POP){
                            queryPay(activity, loop, data, tag);
                            res_code = data.getRes_code();
                        }
                        break;
                }
                result.copy(data);
                loop.done(1);
            }
        });
        loop.exec();
        return result;
    }
    private void queryPay(MainActivity activity, JEventLoop loop, final UnifiedPayResult result, String tag){

        final JSONObject param = new JSONObject();
        param.put("appid",CustomApplication.self().getAppId());
        param.put("pay_code",result.getPay_code());
        param.put("order_code_son",result.getOrder_code_son());

        if (result.getRes_code() == 4){
            final ChangeNumOrPriceDialog password_dialog = new ChangeNumOrPriceDialog(activity,"请输入密码","");
            int code = password_dialog.exec();
            if (code == 0){
                result.failure("密码验证已取消！");
                return;
            }else {
                param.put("pay_password",password_dialog.getContentToStr());
            }
        }
        String unified_pay_query = getUnified_pay_query();
        if (!Utils.isNotEmpty(unified_pay_query)){
            unified_pay_query = InterfaceURL.UNIFIED_PAY_QUERY;
        }

        final String sz_param = HttpRequest.generate_request_parm(param,activity.getAppSecret());
        String url = activity.getUrl() + unified_pay_query;

        Logger.i("%s查询请求:url:%s,param:%s",tag,url ,sz_param);
        HttpUtils.sendAsyncPost(url,sz_param).enqueue(new TypeCallback<UnifiedPayResult>(UnifiedPayResult.class) {
            @Override
            protected void onError(String msg) {
                result.failure(msg);
                loop.done(0);
            }

            @Override
            protected void onSuccess(UnifiedPayResult data) {
                Logger.i("%s查询请求返回:%s",tag,data);
                result.copy(data);
                loop.done(1);
            }
        });
        loop.exec();
    }

    @NonNull
    @Override
    public PayMethod clone() {
        try {
            return (PayMethod)super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PayMethod payMethod = (PayMethod) o;
        return pay_method_id == payMethod.pay_method_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pay_method_id);
    }

    @Override
    public String toString() {
        return "PayMethod{" +
                "pay_method_id=" + pay_method_id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", remark='" + remark + '\'' +
                ", is_check=" + is_check +
                ", shortcut_key='" + shortcut_key + '\'' +
                ", sort=" + sort +
                ", xtype='" + xtype + '\'' +
                ", pay_img='" + pay_img + '\'' +
                ", master_img='" + master_img + '\'' +
                ", is_show_client=" + is_show_client +
                ", is_cardno=" + is_cardno +
                ", is_scan=" + is_scan +
                ", wr_btn_img='" + wr_btn_img + '\'' +
                ", unified_pay_order='" + unified_pay_order + '\'' +
                ", unified_pay_query='" + unified_pay_query + '\'' +
                ", rule='" + rule + '\'' +
                ", is_open=" + is_open +
                ", support='" + support + '\'' +
                ", is_enable=" + is_enable +
                ", is_moling=" + is_moling +
                ", isCur=" + isCur +
                ", isDefault=" + isDefault +
                '}';
    }

}
