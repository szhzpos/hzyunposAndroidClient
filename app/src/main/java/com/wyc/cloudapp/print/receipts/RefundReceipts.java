package com.wyc.cloudapp.print.receipts;

import android.content.Context;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.PrintItem;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.print.bean.PrintFormatInfo;
import com.wyc.cloudapp.print.bean.RefundOrderPrintInfo;
import com.wyc.cloudapp.print.printer.AbstractPrinter;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print.receipts
 * @ClassName: RefundReceipts
 * @Description: 退货单
 * @Author: wyc
 * @CreateDate: 2021-12-30 15:59
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-30 15:59
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class RefundReceipts extends AbstractReceipts {
    public RefundReceipts(String orderCode,boolean open) {
        super(formatInfo("r_f_info"),orderCode, open);
    }
    public static void print(final String mRefundCode,boolean open){
        AbstractPrinter.printContent(new RefundReceipts(mRefundCode,open));
    }
    @Override
    protected List<PrintItem> c_format_58(@NonNull final PrintFormatInfo format_info,@NonNull final String orderCode) {
        final List<PrintItem> printItems = new ArrayList<>();
        final RefundOrderPrintInfo order_info = RefundOrderPrintInfo.getInstance(orderCode);

        if (order_info == null || order_info.isEmpty()){
            MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.print_content_empty));
            return printItems;
        }

        final Context context = CustomApplication.self();

        final String store_name = format_info.getAliasStoresName(),pos_num = order_info.getPosNum(),
                cas_name = order_info.getCasName(),footer_c = format_info.getFooterContent()
                ,line = "--------------------------------";

        printItems.add(new PrintItem.Builder().setDoubleHigh(true).setAlign(PrintItem.Align.CENTRE).setContent(String.format(Locale.CHINA,"%s", CustomApplication.getStringByResId(R.string.r_b_title_sz))).build());
        printItems.add(new PrintItem.Builder().setDoubleHigh(true).setAlign(PrintItem.Align.CENTRE).setContent(store_name.length() == 0 ? order_info.getStoresName() : store_name).build());
        printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.LEFT).setContent(Printer.printTwoData(1, context.getString(R.string.b_f_store_id_sz).concat(String.valueOf(order_info.getStoresId())),order_info.getOperTime())).build());
        printItems.add(new PrintItem.Builder().setContent(Printer.printTwoData(1, context.getString(R.string.b_f_jh_sz).concat(pos_num), context.getString(R.string.b_f_cashier_sz).concat(cas_name))).build());
        printItems.add(new PrintItem.Builder().setContent(context.getString(R.string.b_f_order_sz).concat(order_info.getRoCode())).build());
        printItems.add(new PrintItem.Builder().setContent(context.getString(R.string.b_f_header_sz).replace("-"," ")).build());
        printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(line).build());

        //商品明细
        double refund_num = 0.0,refund_amt = 0.0,refund_sum_amt = 0.0,refund_price;
        int units_num = 0, type = 1;//商品属性 1普通 2称重 3用于服装
        final List<RefundOrderPrintInfo.SalesDTO> sales = order_info.getSales();
        for (int i = 0, size = sales.size(); i < size; i++) {
            final RefundOrderPrintInfo.SalesDTO info_obj = sales.get(i);
            if (info_obj != null) {
                type = info_obj.getType();
                if (type == 2) {
                    units_num += 1;
                } else {
                    units_num += info_obj.getRefundNum();
                }
                refund_num = info_obj.getRefundNum();
                refund_price = info_obj.getRefundPrice();
                refund_amt = refund_num * refund_price;
                refund_sum_amt += refund_amt;

                if (i == 0){
                    printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setBold(true).setContent(info_obj.getGoodsTitle()).build());
                }else
                    printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_10).setBold(true).setContent(info_obj.getGoodsTitle()).build());

                printItems.add(new PrintItem.Builder().setContent(Printer.printTwoData(1,info_obj.getBarcode(),
                        Printer.printThreeData(16,String.format(Locale.CHINA,"%.2f",refund_price), type == 2 ? String.valueOf(refund_num) : String.valueOf((int) refund_num),String.format(Locale.CHINA,"%.2f",refund_amt)))).build());

            }
        }
        printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(line).build());
        printItems.add(new PrintItem.Builder().setContent(Printer.printTwoData(1, String.format(Locale.CHINA,"%s:%.2f",context.getString(R.string.refund_amt_sz),Utils.formatDouble(refund_sum_amt,2))
                , String.format(Locale.CHINA,"%s%d",context.getString(R.string.b_f_units_sz),units_num))).build());


        //支付方式
        double pamt = 0.0;
        final List<RefundOrderPrintInfo.PaysDTO> pays = order_info.getPays();
        for (int i = 0, size = pays.size(); i < size; i++) {
            final RefundOrderPrintInfo.PaysDTO info_obj = pays.get(i);

            if (i == 0){
                printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(String.format(Locale.CHINA,"%s:%.2f元",info_obj.getPayMethodName(),pamt)).build());
            }else printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_10).setContent(String.format(Locale.CHINA,"%s:%.2f元",info_obj.getPayMethodName(),pamt)).build());

        }
        printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(line).build());

        if (footer_c.isEmpty()){
            printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.b_f_hotline_sz),order_info.getTelphone())).build());
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.b_f_stores_address_sz),order_info.getRegion())).build());
        }else {
            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE).setContent(footer_c).build());
        }

        return printItems;
    }

    @Override
    protected List<PrintItem> c_format_76(@NonNull final PrintFormatInfo format_info,@NonNull String orderCOde) {
        return null;
    }

    @Override
    protected List<PrintItem> c_format_80(@NonNull final PrintFormatInfo format_info,@NonNull String orderCOde) {
        return null;
    }

    @Override
    protected int getFormatId() {
        return R.id.refund_format;
    }

}
