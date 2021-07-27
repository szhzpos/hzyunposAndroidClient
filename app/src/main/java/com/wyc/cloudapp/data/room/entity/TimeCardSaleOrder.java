package com.wyc.cloudapp.data.room.entity;

import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.sqlite.db.SimpleSQLiteQuery;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.ICardPay;
import com.wyc.cloudapp.bean.PayDetailInfo;
import com.wyc.cloudapp.bean.TimeCardPayInfo;
import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.bean.UnifiedPayResult;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.data.room.AppDatabase;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;
import com.wyc.cloudapp.utils.http.callback.ObjectCallback;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static com.wyc.cloudapp.fragment.PrintFormatFragment.TIME_CARD_SALE_FORMAT_ID;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.data.room.entity
 * @ClassName: TimeCardSaleOrder
 * @Description: 次卡销售订单
 * @Author: wyc
 * @CreateDate: 2021-07-09 10:43
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-07-09 10:43
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
@Entity(tableName = "timeCardSaleOrder")
public final class TimeCardSaleOrder implements ICardPay<TimeCardSaleInfo> {
    @PrimaryKey
    @NonNull
    private String order_no;
    private String online_order_no;
    private String vip_openid;
    private String vip_card_no;
    private String vip_mobile;
    private String vip_name;
    private double amt;
    @ColumnInfo(defaultValue = "0")
    private int status;
    private String saleman;
    private String cas_id;
    @ColumnInfo(defaultValue = "0")
    private long time;
    @ColumnInfo(defaultValue = "0")
    private int transfer_status;

    @Ignore
    private List<TimeCardSaleInfo> saleInfo;
    @Ignore
    private List<TimeCardPayDetail> payInfo;

    public TimeCardSaleOrder() {
        order_no = generateOrderNo();
    }

    @Override
    public double getDiscountAmt() {
        List<TimeCardSaleInfo> list = getSaleInfo();
        double amt = 0.0;
        for (TimeCardSaleInfo saleInfo : list){
            amt += saleInfo.getDiscountAmt();
        }
        return amt;
    }

    public static class Builder{
        private final TimeCardSaleOrder order;
        public Builder(){
            order = new TimeCardSaleOrder();
        }

        public Builder order_no(String order_no){
            order.setOrder_no(order_no);
            return this;
        }
        public Builder online_order_no(String order_no){
            order.setOnline_order_no(order_no);
            return this;
        }

        public Builder vip_openid(String openid){
            order.setVip_openid(openid);
            return this;
        }

        public Builder vip_card_no(String card_no){
            order.setVip_card_no(card_no);
            return this;
        }
        public Builder vip_mobile(String mobile){
            order.setVip_mobile(mobile);
            return this;
        }
        public Builder vip_name(String name){
            order.setVip_name(name);
            return this;
        }
        public Builder amt(double amt){
            order.setAmt(amt);
            return this;
        }
        public Builder status(int status){
            order.setStatus(status);
            return this;
        }
        public Builder cas_id(String cas_id){
            order.setCas_id(cas_id);
            return this;
        }
        public Builder saleman(String saleman){
            order.setSaleman(saleman);
            return this;
        }
        public Builder time(long time){
            order.setTime(time);
            return this;
        }
        public Builder transfer_status(int status){
            order.setTransfer_status(status);
            return this;
        }
        public Builder saleInfo(List<TimeCardSaleInfo> saleInfoList){
            order.setSaleInfo(saleInfoList);
            return this;
        }

        public Builder payInfo(List<TimeCardPayDetail> payDetailList){
            order.setPayInfo(payDetailList);
            return this;
        }

        public TimeCardSaleOrder build(){
            return  order;
        }
    }

    protected TimeCardSaleOrder(Parcel in) {
        order_no = in.readString();
        online_order_no = in.readString();
        vip_openid = in.readString();
        vip_card_no = in.readString();
        vip_mobile = in.readString();
        vip_name = in.readString();
        amt = in.readDouble();
        status = in.readInt();
        saleman = in.readString();
        cas_id = in.readString();
        time = in.readLong();
        transfer_status = in.readInt();
        saleInfo = in.createTypedArrayList(TimeCardSaleInfo.CREATOR);
        payInfo = in.createTypedArrayList(TimeCardPayDetail.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(order_no);
        dest.writeString(online_order_no);
        dest.writeString(vip_openid);
        dest.writeString(vip_card_no);
        dest.writeString(vip_mobile);
        dest.writeString(vip_name);
        dest.writeDouble(amt);
        dest.writeInt(status);
        dest.writeString(saleman);
        dest.writeString(cas_id);
        dest.writeLong(time);
        dest.writeInt(transfer_status);
        dest.writeTypedList(saleInfo);
        dest.writeTypedList(payInfo);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<TimeCardSaleOrder> CREATOR = new Creator<TimeCardSaleOrder>() {
        @Override
        public TimeCardSaleOrder createFromParcel(Parcel in) {
            return new TimeCardSaleOrder(in);
        }

        @Override
        public TimeCardSaleOrder[] newArray(int size) {
            return new TimeCardSaleOrder[size];
        }
    };

    @NonNull
    @Override
    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(@NonNull String order_no) {
        this.order_no = order_no;
    }

    public String getOnline_order_no() {
        return online_order_no == null ? "" : online_order_no;
    }

    public void setOnline_order_no(String online_order_no) {
        this.online_order_no = online_order_no;
    }

    @Override
    public String getVip_openid() {
        return vip_openid;
    }

    public void setVip_openid(String vip_openid) {
        this.vip_openid = vip_openid;
    }

    public String getVip_card_no() {
        return vip_card_no;
    }

    public void setVip_card_no(String vip_card_no) {
        this.vip_card_no = vip_card_no;
    }

    @Override
    public String getVip_mobile() {
        return vip_mobile;
    }

    public void setVip_mobile(String vip_mobile) {
        this.vip_mobile = vip_mobile;
    }

    @Override
    public String getVip_name() {
        return vip_name;
    }

    public void setVip_name(String vip_name) {
        this.vip_name = vip_name;
    }

    @Override
    public double getAmt() {
        return amt;
    }

    public void setAmt(double amt) {
        this.amt = amt;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSaleman() {
        return saleman;
    }

    public void setSaleman(String saleman) {
        this.saleman = saleman;
    }

    public String getCas_id() {
        return cas_id;
    }

    public void setCas_id(String cas_id) {
        this.cas_id = cas_id;
    }

    public long getTime() {
        return time;
    }

    public String getFormatTime(){
        return FormatDateTimeUtils.formatTimeWithTimestamp(time * 1000);
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getTransfer_status() {
        return transfer_status;
    }

    public void setTransfer_status(int transfer_status) {
        this.transfer_status = transfer_status;
    }

    @NonNull
    @Override
    public List<TimeCardSaleInfo> getSaleInfo() {
        return saleInfo == null ? new ArrayList<>() : saleInfo;
    }

    public void setSaleInfo(List<TimeCardSaleInfo> saleInfo) {
        if (null != saleInfo)
            for (TimeCardSaleInfo info : saleInfo){
                info.setOrder_no(order_no);
            }
        this.saleInfo = saleInfo;
    }

    public List<TimeCardPayDetail> getPayInfo() {
        return payInfo == null ? new ArrayList<>() : payInfo;
    }

    public void setPayInfo(List<TimeCardPayDetail> payInfo) {
        this.payInfo = payInfo;
    }

    public String getStatusName(){
        if (status == 1){
            return CustomApplication.self().getString(R.string.success);
        }else if (status == 2){
            return CustomApplication.self().getString(R.string.failure);
        }else if (status == 3){
            return CustomApplication.self().getString(R.string.paying);
        }else return CustomApplication.self().getString(R.string.uploading);
    }

    @Override
    public void save(@NonNull MainActivity activity,@NonNull List<PayDetailInfo> payDetailInfoList){
        setTime(System.currentTimeMillis() / 1000);
        setOrderPayInfo(activity,payDetailInfoList);

        AppDatabase.getInstance().TimeCardSaleOrderDao().deleteWithDetails(this,saleInfo,payInfo);
        AppDatabase.getInstance().TimeCardSaleOrderDao().insertWithDetails(this,saleInfo,payInfo);

        startPay(activity);
    }

    private void startPay(MainActivity activity){
        final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(activity,"正在支付...");

        final JSONObject param = new JSONObject();
        param.put("appid",activity.getAppId());
        param.put("member_openid",getVip_openid());
        param.put("origin",5);
        param.put("stores_id",activity.getStoreId());
        param.put("cards",getCards().toString());
        param.put("sales_id",getSaleman());
        param.put("cas_id",activity.getCashierId());
        HttpUtils.sendAsyncPost(activity.getUrl() + InterfaceURL.ONCE_CARD_UPLOAD, HttpRequest.generate_request_parm(param,activity.getAppSecret()))//生成订单
                .enqueue(new ObjectCallback<TimeCardPayInfo>(TimeCardPayInfo.class) {
                    @Override
                    protected void onError(String msg) {
                        MyDialog.toastMessage(msg);
                        progressDialog.dismiss();
                    }

                    @Override
                    protected void onSuccessForResult(TimeCardPayInfo d, String hint) {
                        //提交支付
                        try {
                            TimeCardPayInfo.PayInfo payInfo = d.getPay_info();
                            final String online_order_no = payInfo.getOrder_code();

                            //更新状态以及保存线上单号
                            updateOnlineOrderNo(online_order_no);

                            boolean allSuccess = false;
                            final List<TimeCardPayDetail> payDetails = getPayInfo();
                            for (TimeCardPayDetail detail : payDetails){
                                final PayMethod payMethod = AppDatabase.getInstance().PayMethodDao().getPayMethodById(detail.getPay_method_id());
                                if (payMethod.isCheckApi()){
                                    final UnifiedPayResult result = payMethod.payWithApi(activity,payInfo.getPay_money(),
                                            online_order_no,getOrder_no(),detail.getRemark(),getClass().getSimpleName());

                                    if (result.isSuccess()){
                                        allSuccess = true;
                                        detail.success();
                                        detail.setOnline_pay_no(result.getPay_code());
                                    }else {
                                        detail.failure();
                                        MyDialog.toastMessage(result.getInfo());
                                        allSuccess = false;
                                        break;
                                    }
                                }else {
                                    allSuccess = true;
                                    detail.success();
                                }
                            }
                            //更新支付状态
                            TimeCardPayDetail.update(payDetails);

                            if (allSuccess){
                                uploadPayInfo(s -> {
                                    print(activity);

                                    activity.setResult(RESULT_OK);
                                    activity.finish();
                                    MyDialog.toastMessage(hint);
                                    progressDialog.dismiss();
                                }, s -> {
                                    MyDialog.toastMessage(s);
                                    progressDialog.dismiss();
                                });
                            }else progressDialog.dismiss();
                        }catch (Exception e){
                            e.printStackTrace();
                            progressDialog.dismiss();
                            MyDialog.displayErrorMessage(activity,"次卡支付错误:" + e.getLocalizedMessage());
                        }
                    }
                });
    }


    public void print(MainActivity context ){
        CustomApplication.execute(()-> Printer.print(get_print_content(context,this)));
    }

    private static String get_print_content(final MainActivity context,TimeCardSaleOrder order_info){
        final JSONObject print_format_info = new JSONObject();
        String content = "";
        if (SQLiteHelper.getLocalParameter("t_card_sale",print_format_info)){
            if (print_format_info.getIntValue("f") == TIME_CARD_SALE_FORMAT_ID){
                switch (print_format_info.getIntValue("f_z")){
                    case R.id.f_58:
                        content = c_format_58(context,print_format_info,order_info);
                        break;
                    case R.id.f_76:
                        break;
                    case R.id.f_80:
                        break;
                }
            }else {
                context.runOnUiThread(()->MyDialog.ToastMessage(context.getString(R.string.f_not_sz), context.getWindow()));
            }
        }else
            context.runOnUiThread(()->MyDialog.ToastMessage(context.getString(R.string.l_p_f_err_hint_sz,print_format_info.getString("info")), context.getWindow()));

        return content;
    }

    private static String c_format_58(final MainActivity context, final JSONObject format_info, final TimeCardSaleOrder order_info){
        final StringBuilder info = new StringBuilder(),out = new StringBuilder();

        final String store_name = Utils.getNullStringAsEmpty(format_info,"s_n");
        final String new_line =  "\n";
        final String footer_c = Utils.getNullStringAsEmpty(format_info,"f_c");

        int print_count = Utils.getNotKeyAsNumberDefault(format_info,"p_c",1);
        int footer_space = Utils.getNotKeyAsNumberDefault(format_info,"f_s",5);

        final CustomApplication application = CustomApplication.self();

        while (print_count-- > 0) {//打印份数
            if (info.length() > 0){
                info.append(new_line).append(new_line);
                out.append(info);
                continue;
            }
            info.append(Printer.commandToStr(Printer.DOUBLE_HEIGHT)).append(Printer.commandToStr(Printer.ALIGN_CENTER))
                    .append(store_name.length() == 0 ? application.getStoreName() : store_name).append(new_line).append(new_line).append(Printer.commandToStr(Printer.NORMAL)).
                    append(Printer.commandToStr(Printer.ALIGN_LEFT));

            info.append(context.getString(R.string.store_name_sz).concat(application.getStoreName())).append(new_line);
            info.append(context.getString(R.string.order_sz).concat(order_info.getOnline_order_no())).append(new_line);
            info.append(context.getString(R.string.time_card_print_order_time).concat(order_info.getFormatTime())).append(new_line);

            final JSONObject member_parameter = new JSONObject();
            if (!SQLiteHelper.getLocalParameter("MEMBER_PARAMETER",member_parameter)) Logger.d("查询会员参数错误:%s",member_parameter.getString("info"));
            String vip_name = order_info.getVip_name(),card_code = order_info.getVip_card_no(),mobile = order_info.getVip_mobile();
            if (Utils.getNotKeyAsNumberDefault(member_parameter,"member_secret_protect",0) == 1){
                if (vip_name.length() > 2)
                    vip_name = vip_name.replace(vip_name.substring(1),Printer.REPLACEMENT);
                else {
                    vip_name = vip_name.concat(Printer.REPLACEMENT);
                }
                int len = card_code.length();
                if (len <= 3){
                    card_code = card_code.concat(Printer.REPLACEMENT);
                }else if (len <= 7){
                    card_code = card_code.replace(card_code.substring(3,len - 1),Printer.REPLACEMENT);
                }else {
                    card_code = card_code.replace(card_code.substring(3,7),Printer.REPLACEMENT);
                }
                int mobile_len = mobile.length();
                if (mobile_len <= 3){
                    mobile = mobile.concat(Printer.REPLACEMENT);
                }else if (len <= 7){
                    mobile = mobile.replace(mobile.substring(3,len - 1),Printer.REPLACEMENT);
                }else {
                    mobile = mobile.replace(mobile.substring(3,7),Printer.REPLACEMENT);
                }
            }

            //info.append(context.getString(R.string.oper_sz).concat("：").concat(order_info.getCashierName())).append(new_line);
            info.append(context.getString(R.string.time_card_print_vip_name).concat(vip_name)).append(new_line);
            info.append(context.getString(R.string.time_card_print_vip_mobile).concat(mobile)).append(new_line);
            info.append(context.getString(R.string.time_card_print_vip_card).concat(card_code)).append(new_line);
            info.append(context.getString(R.string.time_card_print_num).concat(String.valueOf(order_info.getSaleInfo().size()))).append(new_line);
            info.append(context.getString(R.string.time_card_print_amt).concat(String.format(Locale.CHINA,"%.2f元",order_info.getAmt()))).append(new_line);

            final List<TimeCardPayDetail> payDetails = order_info.getPayInfo();
            final StringBuilder stringBuilder = new StringBuilder();
            for (TimeCardPayDetail detail : payDetails){
                final PayMethod payMethod = AppDatabase.getInstance().PayMethodDao().getPayMethodById(detail.getPay_method_id());
                if (null == payMethod)continue;
                if (stringBuilder.length() > 0){
                    stringBuilder.append(new_line);
                }
                stringBuilder.append(payMethod.getName());
            }
            info.append(context.getString(R.string.time_card_print_pay_method).concat(stringBuilder.toString())).append(new_line);

            final List<TimeCardSaleInfo> saleInfoList = order_info.getSaleInfo();
            String line_58 = application.getString(R.string.line_58),space_sz = " ",name;
            stringBuilder.delete(0,stringBuilder.length());
            stringBuilder.append(line_58).append(new_line);
            for(TimeCardSaleInfo saleInfo : saleInfoList){
                name = saleInfo.getName();
                int space = 8 - name.length();
                if (space > 0){
                    final StringBuilder sb = new StringBuilder(name);
                    for (int i = 0;i < space;i ++){
                        sb.append(space_sz);
                    }
                    name = sb.toString();
                }else {
                    name = name.substring(0,7);
                }
                stringBuilder.append(String.format(Locale.CHINA,"%s  %d张 %.2f元%s",name,saleInfo.getNum(),saleInfo.getAmt(),new_line));
            }
            stringBuilder.append(line_58);
            info.append(stringBuilder).append(new_line);

            if (footer_c.isEmpty()){
                info.append(context.getString(R.string.hotline_sz)).append(Utils.getNullOrEmptyStringAsDefault(application.getStoreInfo(),"telphone","")).append(new_line);
                info.append(context.getString(R.string.stores_address_sz)).append(Utils.getNullOrEmptyStringAsDefault(application.getStoreInfo(),"region","")).append(new_line);
            }else {
                info.append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(footer_c).append(Printer.commandToStr(Printer.ALIGN_LEFT));
            }

            for (int i = 0; i < footer_space; i++) info.append(" ").append(new_line);
        }
        out.append(info);

        return out.toString();
    }

    private JSONArray getCards(){
        List<TimeCardSaleInfo> saleInfoList = getSaleInfo();
        final JSONArray array = new JSONArray();
        for (TimeCardSaleInfo saleInfo : saleInfoList){
            final JSONObject object = new JSONObject();
            object.put("once_card_id",saleInfo.getOnce_card_id());
            object.put("num",saleInfo.getNum());
            array.add(object);
        }
        return array;
    }

    private void setOrderPayInfo(MainActivity context,@NotNull List<PayDetailInfo> payDetailList) {
        final List<TimeCardPayDetail> payDetails = new ArrayList<>();
        double pamt = 0.0,zl_amt = 0.0;
        int index = 1;
        for (PayDetailInfo payDetailInfo : payDetailList){
            pamt = payDetailInfo.getPay_amt();
            zl_amt = payDetailInfo.getZl_amt();
            final TimeCardPayDetail payDetail = new TimeCardPayDetail.Builder(getOrder_no()).rowId(index++).
                    pay_method_id(payDetailInfo.getMethod_id()).remark(payDetailInfo.getV_num())
                    .amt(pamt - zl_amt).zl_amt(zl_amt).cas_id(context.getCashierId()).build();
            payDetails.add(payDetail);
        }
        setPayInfo(payDetails);
    }

    private void updateOnlineOrderNo(String _order_no){//保存线上订单并更新状态为正在支付
        setOnline_order_no(_order_no);
        AppDatabase.getInstance().TimeCardSaleOrderDao().updateOrder(order_no,_order_no,3);
    }
    public void success(){
        setStatus(1);
        AppDatabase.getInstance().TimeCardSaleOrderDao().updateOrder(order_no,1);
    }
    public boolean isSuccess(){
        return status == 1;
    }

    public @Nullable String getCashierName(){
        return SQLiteHelper.getCashierNameById(cas_id);
    }

    public @Nullable String getSalemanName(){
        return SQLiteHelper.getShopAssistantById(saleman);
    }

    private static String generateOrderNo() {
        int row = AppDatabase.getInstance().TimeCardSaleOrderDao().count() + 1;
        return "CK" + CustomApplication.self().getPosNum() + "-" + new SimpleDateFormat("yyMMddHHmmss", Locale.CHINA).format(new Date()) + "-" + String.format(Locale.CHINA,"%04d",row);
    }

    public static List<TimeCardSaleOrder> getOrderByCondition(String query){
        return AppDatabase.getInstance().TimeCardSaleOrderDao().getOrderByCondition(new SimpleSQLiteQuery(query));
    }

    public void uploadPayInfo(Consumer<String> success,Consumer<String> error){
        final JSONObject pay = new JSONObject();
        pay.put("appid",CustomApplication.self().getAppId());
        pay.put("order_code",getOnline_order_no());
        if (payInfo == null || payInfo.isEmpty()){
            MyDialog.toastMessage(CustomApplication.self().getString(R.string.hints_pay_detail_not_empty));
            return;
        }
        pay.put("pay_method",payInfo.get(0).getPay_method_id());
        HttpUtils.sendAsyncPost(CustomApplication.self().getUrl() + InterfaceURL.ONCE_CARD_PAY, HttpRequest.generate_request_parm(pay,CustomApplication.self().getAppSecret()))
                .enqueue(new ObjectCallback<String>(String.class) {
                    @Override
                    protected void onError(String msg) {
                        if (null != error)error.accept(msg);
                    }

                    @Override
                    protected void onSuccessForResult(String d, String hint) {
                        success();
                        if (null != success)
                            success.accept(hint);
                    }
                });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeCardSaleOrder that = (TimeCardSaleOrder) o;
        return Objects.equals(order_no,that.order_no);
    }

    @Override
    public int hashCode() {
        return Objects.hash(order_no);
    }

    @Override
    public String toString() {
        return super.toString() +"{" +
                "order_no='" + order_no + '\'' +
                ", online_order_no='" + online_order_no + '\'' +
                ", vip_openid='" + vip_openid + '\'' +
                ", vip_card_no='" + vip_card_no + '\'' +
                ", vip_mobile='" + vip_mobile + '\'' +
                ", vip_name='" + vip_name + '\'' +
                ", amt=" + amt +
                ", status=" + status +
                ", saleman='" + saleman + '\'' +
                ", cas_id='" + cas_id + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
