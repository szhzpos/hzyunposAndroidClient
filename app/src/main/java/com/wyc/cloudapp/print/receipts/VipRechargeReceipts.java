package com.wyc.cloudapp.print.receipts;

import android.content.Context;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.PrintItem;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.print.parameter.SalePrintParameter;
import com.wyc.cloudapp.print.bean.VipChargeOrderPrintInfo;
import com.wyc.cloudapp.print.printer.AbstractPrinter;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print.receipts
 * @ClassName: VipRechargeReceipts
 * @Description: 会员充值打印内容
 * @Author: wyc
 * @CreateDate: 2021-12-31 11:50
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-31 11:50
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class VipRechargeReceipts extends AbstractReceipts {
    public VipRechargeReceipts(String orderCode, boolean open) {
        super(formatInfo("v_f_info"), orderCode, open);
    }
    public static void print(final String orderCode){
        AbstractPrinter.printContent(new VipRechargeReceipts(orderCode,false));
    }
    @Override
    protected List<PrintItem> c_format_58(@NonNull SalePrintParameter formatInfo, @NonNull String orderCode) {

        final JSONObject order_info = VipChargeOrderPrintInfo.getInstance(orderCode);
        final JSONArray welfare = Utils.getNullObjectAsEmptyJsonArray(order_info,"welfare"),money_orders = Utils.getNullObjectAsEmptyJsonArray(order_info,"money_order"),
                members = Utils.getNullObjectAsEmptyJsonArray(order_info,"member");

        if (money_orders.isEmpty() || members.isEmpty()){
            MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.print_content_empty));
            return null;
        }

        final List<PrintItem> printItems = new ArrayList<>();
        final Context context = CustomApplication.self();

        final StringBuilder info = new StringBuilder(),out = new StringBuilder();
        final String new_line =  "\n";
        final String footer_c = formatInfo.getFooterContent();

        final JSONObject money_order = money_orders.getJSONObject(0),member = members.getJSONObject(0);

        String store_name = formatInfo.getAliasStoresName();


        printItems.add(new PrintItem.Builder().setDoubleHigh(true).setAlign(PrintItem.Align.CENTRE).setContent(store_name.length() == 0 ? store_name = Utils.getNullStringAsEmpty(order_info,"stores_name") : store_name).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.t_f_store_sz),store_name)).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.order_sz),Utils.getNullStringAsEmpty(money_order,"order_code"))).build());


        final String origin_order_code = Utils.getNullStringAsEmpty(money_order,"source_order_code");
        if (!"".equals(origin_order_code))
                printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s:%s",context.getString(R.string.origin_order_code_sz),origin_order_code)).build());

        final JSONObject vip_member = new JSONObject();
        if (!SQLiteHelper.getLocalParameter("MEMBER_PARAMETER",vip_member)) Logger.d("查询会员参数错误:%s",vip_member.getString("info"));
        String vip_name = Utils.getNullStringAsEmpty(member,"name"),card_code = Utils.getNullStringAsEmpty(member,"card_code"),mobile = Utils.getNullStringAsEmpty(member,"mobile");
        if (Utils.getNotKeyAsNumberDefault(vip_member,"member_secret_protect",0) == 1){
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
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s:%s",context.getString(R.string.oper_sz),Utils.getNullStringAsEmpty(order_info,"cas_name"))).build());

        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.vip_card_id_sz),card_code)).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s","会员姓名：",vip_name)).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s","支付方式：",Utils.getNullStringAsEmpty(money_order,"pay_method_name"))).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.charge_amt_colon_sz),Utils.getNullStringAsEmpty(money_order,"order_money"))).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s：%s",context.getString(R.string.give_amt),Utils.getNullOrEmptyStringAsDefault(money_order,"give_money","0.00"))).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s","会员余额：",Utils.getNullStringAsEmpty(member,"money_sum"))).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s","会员积分：",Utils.getNullStringAsEmpty(member,"points_sum"))).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s","会员电话：",mobile)).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s","时    间：",new SimpleDateFormat(FormatDateTimeUtils.YYYY_MM_DD_1, Locale.CHINA).format(money_order.getLongValue("addtime") * 1000))).build());

        if (welfare.size() != 0){
            for (int i = 0,size = welfare.size();i < size;i++){
                if (i == 0)info.append("优惠信息").append(new_line);
                printItems.add(new PrintItem.Builder().setContent(welfare.getString(i)).build());
            }
        }
        if (footer_c.isEmpty()){
            printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.b_f_hotline_sz),Utils.getNullStringAsEmpty(order_info,"telphone"))).build());
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.b_f_stores_address_sz),Utils.getNullStringAsEmpty(order_info,"region"))).build());
        }else {
            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE).setContent(footer_c).build());
        }
        return printItems;
    }

    @Override
    protected List<PrintItem> c_format_76(@NonNull SalePrintParameter mPrintFormatInfo, @NonNull String orderCOde) {
        return null;
    }

    @Override
    protected List<PrintItem> c_format_80(@NonNull SalePrintParameter mPrintFormatInfo, @NonNull String orderCOde) {
        return null;
    }

    @Override
    protected int getFormatId() {
        return R.id.vip_c_format;
    }
}
