package com.wyc.cloudapp.print.receipts;

import static com.wyc.cloudapp.fragment.PrintFormatFragment.TIME_CARD_SALE_FORMAT_ID;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.data.room.AppDatabase;
import com.wyc.cloudapp.data.room.entity.PayMethod;
import com.wyc.cloudapp.data.room.entity.TimeCardPayDetail;
import com.wyc.cloudapp.data.room.entity.TimeCardSaleOrder;
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
 * @ClassName: TimeCardReceipts
 * @Description: 次卡销售打印内容
 * @Author: wyc
 * @CreateDate: 2021-12-31 16:34
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-31 16:34
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class TimeCardReceipts extends AbstractReceipts {
    private final TimeCardSaleOrder order_info;
    protected TimeCardReceipts(final TimeCardSaleOrder order_info,boolean open) {
        super(formatInfo("t_card_sale"), order_info.getOrder_no(), open);
        this.order_info = order_info;
    }

    public static void print(final TimeCardSaleOrder order_info,boolean open){
        AbstractPrinter.printContent(new TimeCardReceipts(order_info,open));
    }

    @Override
    protected List<PrintItem> c_format_58(@NonNull SalePrintParameter formatInfo, @NonNull String orderCode) {
        final List<PrintItem> printItems = new ArrayList<>();


        final String store_name = formatInfo.getAliasStoresName();
        final String new_line =  "\n";
        final String footer_c = formatInfo.getFooterContent();

        final CustomApplication application = CustomApplication.self();


        printItems.add(new PrintItem.Builder().setBold(true).setDoubleHigh(true).setAlign(PrintItem.Align.CENTRE)
                .setContent(store_name.length() == 0 ? application.getStoreName() : store_name).build());

        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.store_name_sz),application.getStoreName())).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.order_sz),order_info.getOnline_order_no())).build());

        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.time_card_print_order_time),order_info.getFormatTime())).build());


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

        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.time_card_print_vip_name),vip_name)).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.time_card_print_vip_mobile),mobile)).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.time_card_print_vip_card),card_code)).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%d",application.getString(R.string.time_card_print_num),order_info.getSaleInfo().size())).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%.2f元",application.getString(R.string.time_card_print_amt),order_info.getAmt())).build());

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
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",application.getString(R.string.time_card_print_pay_method),stringBuilder)).build());

        final List<TimeCardSaleInfo> saleInfoList = order_info.getSaleInfo();
        String line_58 = application.getString(R.string.line_58),space_sz = " ",name;
        stringBuilder.delete(0,stringBuilder.length());
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
        printItems.add(new PrintItem.Builder().setContent(line_58).build());
        printItems.add(new PrintItem.Builder().setContent(stringBuilder.toString()).build());

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
        return TIME_CARD_SALE_FORMAT_ID;
    }
}
