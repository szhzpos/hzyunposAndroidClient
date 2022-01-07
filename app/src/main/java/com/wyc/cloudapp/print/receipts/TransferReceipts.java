package com.wyc.cloudapp.print.receipts;

import android.content.Context;

import androidx.annotation.NonNull;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.print.PrintItem;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.print.parameter.SalePrintParameter;
import com.wyc.cloudapp.print.bean.TransferOrderPrintInfo;
import com.wyc.cloudapp.print.printer.AbstractPrinter;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print.receipts
 * @ClassName: TransferReceipts
 * @Description: 交班单
 * @Author: wyc
 * @CreateDate: 2021-12-31 14:05
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-31 14:05
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public final class TransferReceipts extends AbstractReceipts {
    private final boolean mTransferAmtNotVisible;
    protected TransferReceipts(String orderCode,boolean visible, boolean open) {
        super(formatInfo("t_f_info"), orderCode, open);
        mTransferAmtNotVisible = visible;
    }

    public static void print(final String order_code,boolean visible,boolean open){
        AbstractPrinter.printContent(new TransferReceipts(order_code,visible,open));
    }

    @Override
    protected List<PrintItem> c_format_58(@NonNull SalePrintParameter formatInfo, @NonNull String orderCode) {
        final TransferOrderPrintInfo order_info = TransferOrderPrintInfo.getInstance(orderCode);
        if (order_info == null || order_info.isEmpty()){
            MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.print_content_empty));
            return null;
        }
        final List<PrintItem> printItems = new ArrayList<>();
        final Context context = CustomApplication.self();
        boolean no_visible = mTransferAmtNotVisible;

        final String cas_name = order_info.getCasName(),footer_c = formatInfo.getFooterContent(),
                line = context.getString(R.string.line_58),asterisk = "****";

        String store_name = formatInfo.getAliasStoresName();
        store_name = store_name.length() == 0 ? order_info.getStoresName() : store_name;

        printItems.add(new PrintItem.Builder().setDoubleHigh(true).setAlign(PrintItem.Align.CENTRE).setContent(store_name).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.t_f_store_sz),store_name)).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.t_f_order_sz),order_info.getTiCode())).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.t_f_s_time_sz),order_info.getOrderBDate())).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.t_f_e_time_sz),order_info.getOrderEDate())).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.t_f_time_sz),order_info.getTransferTime())).build());
        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.t_f_oper_sz),cas_name)).build());
        printItems.add(new PrintItem.Builder().setContent(line).build());

        //商品销售
        final List<TransferOrderPrintInfo.RetailMoneysDTO> retail_moneys = order_info.getRetailMoneys();
        if (!retail_moneys.isEmpty()){
            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE)
                    .setContent(String.format(Locale.CHINA,"%s%s%s",asterisk,context.getString(R.string.t_f_retail_sz),asterisk)).build());

            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE)
                    .setContent(context.getString(R.string.t_f_detail_h_sz).replace("-"," ")).build());

            printItems.add(new PrintItem.Builder().setContent(line).build());

            for (int i = 0,size = retail_moneys.size();i < size;i++){
                final TransferOrderPrintInfo.RetailMoneysDTO dto = retail_moneys.get(i);
                printItems.add(new PrintItem.Builder()
                        .setContent(Printer.printThreeDataAlignRight_58(3,dto.getName(),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",dto.getPayMoney()),
                                String.valueOf(dto.getOrderNum()))).build());
            }
            printItems.add(new PrintItem.Builder().setContent(line).build());
        }
        //商品退货
        final List<TransferOrderPrintInfo.RefundMoneysDTO> refundMoneys = order_info.getRefundMoneys();
        if (!refundMoneys.isEmpty()){
            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE)
                    .setContent(String.format(Locale.CHINA,"%s%s%s",asterisk,context.getString(R.string.t_f_refund_sz),asterisk)).build());

            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE)
                    .setContent(context.getString(R.string.t_f_detail_h_sz).replace("-"," ")).build());

            for (TransferOrderPrintInfo.RefundMoneysDTO dto : refundMoneys){
                printItems.add(new PrintItem.Builder()
                        .setContent(Printer.printThreeDataAlignRight_58(3,dto.getName(),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",dto.getPayMoney()),
                                String.valueOf(dto.getOrderNum()))).build());
            }
            printItems.add(new PrintItem.Builder().setContent(line).build());
        }
        //会员充值
        final List<TransferOrderPrintInfo.RechargeMoneysDTO> recharge_moneys = order_info.getRechargeMoneys();
        if (!recharge_moneys.isEmpty()){
            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE)
                    .setContent(String.format(Locale.CHINA,"%s%s%s",asterisk,context.getString(R.string.t_f_deposit_sz),asterisk)).build());

            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE)
                    .setContent(context.getString(R.string.t_f_detail_h_sz).replace("-"," ")).build());

            for (TransferOrderPrintInfo.RechargeMoneysDTO dto : recharge_moneys){
                printItems.add(new PrintItem.Builder()
                        .setContent(Printer.printThreeDataAlignRight_58(3,dto.getName(),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",dto.getPayMoney()),
                                String.valueOf(dto.getOrderNum()))).build());
            }
            printItems.add(new PrintItem.Builder().setContent(line).build());
        }

        //次卡销售
        final List<TransferOrderPrintInfo.OncecardMoneysDTO> oncecard_moneys = order_info.getOncecardMoneys();
        if (!oncecard_moneys.isEmpty()){
            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE)
                    .setContent(String.format(Locale.CHINA,"%s%s%s",asterisk,context.getString(R.string.t_f_cards_sz),asterisk)).build());
            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE)
                    .setContent(context.getString(R.string.t_f_detail_h_sz).replace("-"," ")).build());

            for (TransferOrderPrintInfo.OncecardMoneysDTO dto : oncecard_moneys){
                printItems.add(new PrintItem.Builder()
                        .setContent(Printer.printThreeDataAlignRight_58(3,dto.getName(),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",dto.getPayMoney()),
                                String.valueOf(dto.getOrderNum()))).build());
            }
            printItems.add(new PrintItem.Builder().setContent(line).build());
        }
        //购物卡销售
        final List<TransferOrderPrintInfo.ShoppingMoneysDTO> gift_moneys = order_info.getShoppingMoneys();
        if (!gift_moneys.isEmpty()){
            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE)
                    .setContent(String.format(Locale.CHINA,"%s%s%s",asterisk,context.getString(R.string.t_f_gift_sz),asterisk)).build());
            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE)
                    .setContent(context.getString(R.string.t_f_detail_h_sz).replace("-"," ")).build());

            for (TransferOrderPrintInfo.ShoppingMoneysDTO dto : gift_moneys){
                printItems.add(new PrintItem.Builder()
                        .setContent(Printer.printThreeDataAlignRight_58(3,dto.getName(),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",dto.getPayMoney()),
                                String.valueOf(dto.getOrderNum()))).build());
            }
            printItems.add(new PrintItem.Builder().setContent(line).build());
        }

        double order_money = order_info.getOrderMoney(),refund_money = order_info.getRefundMoney(),
                recharge_money = order_info.getRechargeMoney(),cards_money = order_info.getCardsMoney(),
                gift_money = order_info.getShoppingMoney();

        if (!Utils.equalDouble(order_money,0.0))
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.t_f_retail_s_sz),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",order_money))).build());


        if (!Utils.equalDouble(recharge_money,0.0))
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.t_f_deposit_s_sz),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",recharge_money))).build());

        if (!Utils.equalDouble(cards_money,0.0))
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.t_f_cards_s_sz),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",cards_money))).build());

        if (!Utils.equalDouble(gift_money,0.0))
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.t_f_cards_s_sz),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",gift_money))).build());

        if (!Utils.equalDouble(refund_money,0.0))
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.t_f_refund_s_sz),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",refund_money))).build());

        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.t_f_s_sz),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",order_money + recharge_money + cards_money - refund_money))).build());

        printItems.add(new PrintItem.Builder().setContent(line).build());

        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s：%s",context.getString(R.string.t_f_cash_sz),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",order_info.getSjMoney()))).build());

        printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%.2f",context.getString(R.string.t_f_cashbox_sz),order_info.getCashboxMoney())).build());

        printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(line).build());


        if (footer_c.isEmpty()){
            printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.b_f_hotline_sz),order_info.getTelphone())).build());
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.b_f_stores_address_sz),order_info.getRegion())).build());
        }else {
            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE).setContent(footer_c).build());
        }
        printItems.add(new PrintItem.Builder().setContent(Printer.printTwoData(1,context.getString(R.string.t_f_f_sign_sz),context.getString(R.string.t_f_c_sign_sz))).build());

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
        return R.id.transfer_format;
    }
}
