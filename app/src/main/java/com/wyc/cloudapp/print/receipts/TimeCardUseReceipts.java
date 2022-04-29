package com.wyc.cloudapp.print.receipts;

import static com.wyc.cloudapp.fragment.PrintFormat.TIME_CARD_USE_FORMAT_ID;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.VipTimeCardUseOrder;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.PrintItem;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.print.parameter.SalePrintParameter;
import com.wyc.cloudapp.print.printer.AbstractPrinter;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print.receipts
 * @ClassName: TimeCardUseReceipts
 * @Description: 次卡使用打印内容
 * @Author: wyc
 * @CreateDate: 2021-12-31 17:13
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-31 17:13
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class TimeCardUseReceipts extends AbstractReceipts {
    final VipTimeCardUseOrder mOrderInfo;
    protected TimeCardUseReceipts(@NonNull VipTimeCardUseOrder order, boolean open) {
        super(formatInfo("t_card_use"), order.getOrderCode(), open);
        mOrderInfo = order;
    }

    public static void print(@NonNull VipTimeCardUseOrder order, boolean open){
        AbstractPrinter.printContent(new TimeCardUseReceipts(order,open));
    }

    @Override
    protected List<PrintItem> c_format_58(@NonNull SalePrintParameter formatInfo, @NonNull String orderCode) {
        final List<PrintItem> printItems = new ArrayList<>();

        final String store_name = formatInfo.getAliasStoresName();
        final String footer_c = formatInfo.getFooterContent();

        final CustomApplication application = CustomApplication.self();

        printItems.add(new PrintItem.Builder().setBold(true).setDoubleHigh(true).setAlign(PrintItem.Align.CENTRE)
                .setContent(store_name.length() == 0 ? application.getStoreName() : store_name).build());

        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.order_sz),orderCode)).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.use_order_time),mOrderInfo.getAddtime())).build());

        final JSONObject member_parameter = new JSONObject();
        if (!SQLiteHelper.getLocalParameter("MEMBER_PARAMETER",member_parameter)) Logger.d("查询会员参数错误:%s",member_parameter.getString("info"));
        String vip_name = mOrderInfo.getMemberName(),card_code = mOrderInfo.getMemberCard(),mobile = mOrderInfo.getMemberMobile();
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
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.use_vip_name),vip_name)).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.use_vip_mobile),mobile)).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.use_vip_card),card_code)).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.use_store),mOrderInfo.getStoresName())).build());

        String line_58 = application.getString(R.string.line_58),space_sz = " ",name;

        printItems.add(new PrintItem.Builder().setContent(line_58).build());

        name = mOrderInfo.getTitle();
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
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s  扣减 %d 次",name,mOrderInfo.getUseNum())).build());
        printItems.add(new PrintItem.Builder().setContent(line_58).build());

        if (footer_c.isEmpty()){
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.hotline_sz),application.getStoreTelephone())).build());
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.stores_address_sz),application.getStoreRegion())).build());
        }else {
            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE).setContent(footer_c).build());
        }
        return printItems;
    }

    @Override
    protected List<PrintItem> c_format_76(@NonNull SalePrintParameter formatInfo, @NonNull String orderCode) {
        return null;
    }

    @Override
    protected List<PrintItem> c_format_80(@NonNull SalePrintParameter formatInfo, @NonNull String orderCode) {
        return null;
    }

    @Override
    protected int getFormatId() {
        return TIME_CARD_USE_FORMAT_ID;
    }
}
