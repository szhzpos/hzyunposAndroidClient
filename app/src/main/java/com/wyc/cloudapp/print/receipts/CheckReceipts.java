package com.wyc.cloudapp.print.receipts;

import android.content.Context;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.AbstractSaleGoodsAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.PrintItem;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.print.bean.GoodsPracticeInfo;
import com.wyc.cloudapp.print.bean.PrintFormatInfo;
import com.wyc.cloudapp.print.bean.SaleOrderPrintInfo;
import com.wyc.cloudapp.print.printer.AbstractPrinter;
import com.wyc.cloudapp.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @ProjectName: AndroidClient
 * @Package: com.wyc.cloudapp.print.receipts
 * @ClassName: CheckReceipts
 * @Description: 结账单
 * @Author: wyc
 * @CreateDate: 2021-12-29 13:02
 * @UpdateUser: 更新者
 * @UpdateDate: 2021-12-29 13:02
 * @UpdateRemark: 更新说明
 * @Version: 1.0
 */
public class CheckReceipts extends AbstractReceipts {

    public CheckReceipts(final String orderCode,boolean open){
        super(formatInfo("c_f_info"),orderCode,open);
    }
    public CheckReceipts(final String orderCode){
        this(orderCode,false);
    }

    public static void print(final String order_code,boolean open){
        AbstractPrinter.printContent(new CheckReceipts(order_code,open));
    }
    @Override
    protected int getFormatId() {
        return R.id.checkout_format;
    }


    @Override
    protected List<PrintItem> c_format_58(@NonNull final PrintFormatInfo format_info, @NonNull final String orderCode){
        final List<PrintItem> printItems = new ArrayList<>();
        final SaleOrderPrintInfo order_info = SaleOrderPrintInfo.getInstance(orderCode);
        if (order_info == null || order_info.isEmpty()){
            MyDialog.toastMessage(CustomApplication.getStringByResId(R.string.print_content_empty));
            return printItems;
        }

        final Context context = CustomApplication.self();

        final String store_name = format_info.getAliasStoresName(),pos_num = order_info.getPosNum(),
                cas_name = order_info.getCasName(),footer_c = format_info.getFooterContent(),
                line = context.getString(R.string.line_58);

        printItems.add(new PrintItem.Builder().setDoubleHigh(true).setAlign(PrintItem.Align.CENTRE).setContent(store_name.length() == 0 ? order_info.getStoresName() : store_name).build());
        printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.LEFT).setContent(Printer.printTwoData(1, context.getString(R.string.b_f_store_id_sz).concat(String.valueOf(order_info.getStoresId())),order_info.getOperTime())).build());
        printItems.add(new PrintItem.Builder().setContent(Printer.printTwoData(1, context.getString(R.string.b_f_jh_sz).concat(pos_num), context.getString(R.string.b_f_cashier_sz).concat(cas_name))).build());
        printItems.add(new PrintItem.Builder().setContent(context.getString(R.string.b_f_order_sz).concat(order_info.getOrderCode())).build());
        printItems.add(new PrintItem.Builder().setContent(context.getString(R.string.b_f_header_sz).replace("-"," ")).build());
        printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(line).build());

        //商品明细
        SaleOrderPrintInfo.SalesDTO info_obj;
        double discount_amt = 0.0, xnum = 0.0,original_order_amt = 0.0,actual_amt = 0.0,sum_dis_amt = 0.0;
        int units_num = 0, type = 1;//商品属性 1普通 2称重 3用于服装
        final List<SaleOrderPrintInfo.SalesDTO> sales = order_info.getSales();
        for (int i = 0, size = sales.size(); i < size; i++) {
            info_obj = sales.get(i);
            if (info_obj != null) {
                original_order_amt += info_obj.getOriginalAmt();
                actual_amt += info_obj.getSaleAmt();

                type = info_obj.getType();
                if (type == 2) {
                    units_num += 1;
                } else {
                    units_num += info_obj.getXnum();
                }
                xnum = info_obj.getXnum();
                discount_amt = Utils.formatDouble(info_obj.getDiscountAmt(),2);

                if (i == 0){
                    printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setBold(true).setContent(info_obj.getGoodsTitle()).build());
                }else
                    printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_10).setBold(true).setContent(info_obj.getGoodsTitle()).build());

                printItems.add(new PrintItem.Builder().setContent(Printer.printTwoData(1,info_obj.getBarcode(),
                        Printer.printThreeData(16,String.format(Locale.CHINA, "%.2f", info_obj.getPrice()),
                                type == 2 ? String.valueOf(xnum) : String.valueOf((int) xnum),String.format(Locale.CHINA, "%.2f", info_obj.getSaleAmt())))).build());

                if (Utils.greaterDouble(discount_amt, 0.0)) {
                    sum_dis_amt += discount_amt;
                    printItems.add(new PrintItem.Builder().setContent(Printer.printTwoData(1, context.getString(R.string.b_f_ori_price_sz).concat(String.format(Locale.CHINA,"%.2f",info_obj.getOriginalAmt())),
                            context.getString(R.string.b_f_disco_sz).concat(String.format(Locale.CHINA, "%.2f", discount_amt)))).build());
                }

                final List<GoodsPracticeInfo> goodsPractices = info_obj.getGoodsPracticeList();
                if (!goodsPractices.isEmpty()){
                    printItems.add(new PrintItem.Builder().setContent(String.format("%s:%s",context.getString(R.string.goods_practice), GoodsPracticeInfo.generateGoodsPracticeInfo(goodsPractices))).build());
                }
            }
        }
        printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(line).build());
        printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(Printer.printTwoData(1, context.getString(R.string.b_f_amt_sz).concat(String.format(Locale.CHINA, "%.2f", original_order_amt))
                , context.getString(R.string.b_f_units_sz).concat(String.valueOf(units_num)))).build());
        printItems.add(new PrintItem.Builder().setContent(Printer.printTwoData(1, context.getString(R.string.b_f_rec_sz).concat(String.format(Locale.CHINA, "%.2f",actual_amt)),
                context.getString(R.string.b_f_disco_sz).concat(String.format(Locale.CHINA, "%.2f", sum_dis_amt)))).build());
        printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(line).build());

        //支付方式
        SaleOrderPrintInfo.PaysDTO paysDTO;
        double zl = 0.0, pamt = 0.0;
        final List<SaleOrderPrintInfo.PaysDTO> pays = order_info.getPays();
        for (int i = 0, size = pays.size(); i < size; i++) {
            paysDTO = pays.get(i);
            zl = paysDTO.getPzl();
            pamt = paysDTO.getPamt();

            if (i == 0){
                printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(String.format(Locale.CHINA,"%s:%.2f%s",paysDTO.getName(),pamt - zl,"元")).build());
            }else printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_10).setContent(String.format(Locale.CHINA,"%s:%.2f%s",paysDTO.getName(),pamt - zl,"元")).build());
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%.2f,%s%.2f",context.getString(R.string.b_f_yus_sz),pamt,context.getString(R.string.b_f_zl_sz),zl)).build());

            final List<String> xnote = paysDTO.getXnoteList();
            if (xnote != null) {
                int length = xnote.size();
                if (length > 0) {
                    for (int j = 0; j < length; j++) {
                        if (i > 0 && j + 1 != length)
                            printItems.add(new PrintItem.Builder().setContent(xnote.get(j)).build());
                    }
                }
            }
        }
        printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(line).build());

        //会员积分信息
        final SaleOrderPrintInfo.VipIntegralInfo integral_info = order_info.getIntegralInfoObj();
        if (integral_info != null){
            final JSONObject MEMBER_PARAMETER = new JSONObject();
            if (!SQLiteHelper.getLocalParameter("MEMBER_PARAMETER",MEMBER_PARAMETER)) Logger.d("查询会员参数错误:%s",MEMBER_PARAMETER.getString("info"));
            String vip_name = order_info.getVipName(),card_code = order_info.getCardCode();

            if (Utils.getNotKeyAsNumberDefault(MEMBER_PARAMETER,"member_secret_protect",0) == 1){
                if (vip_name.length() > 2)
                    vip_name = vip_name.replace(vip_name.substring(1),Printer.REPLACEMENT);
                else {
                    vip_name = vip_name.concat(Printer.REPLACEMENT);
                }
                int len = card_code.length();
                if (len <= 3){
                    card_code = card_code.concat(Printer.REPLACEMENT);
                }else if (len <= 7){
                    card_code = card_code.replace(card_code.substring(3,len - 1),"***");
                }else {
                    card_code = card_code.replace(card_code.substring(3,7),"***");
                }
            }
            printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.vip_name_colon_sz),vip_name)).build());
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.m_vip_colon_sz),card_code)).build());

            double point_num = integral_info.getPointNum();
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%.2f",context.getString(R.string.current_vip_integral),point_num)).build());
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%.2f",context.getString(R.string._vip_integral),integral_info.getPointsSum() + point_num)).build());
            printItems.add(new PrintItem.Builder().setContent(line).build());
        }
        if (footer_c.isEmpty()){
            printItems.add(new PrintItem.Builder().setLineSpacing(PrintItem.LineSpacing.SPACING_2).setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.b_f_hotline_sz),order_info.getTelphone())).build());
            printItems.add(new PrintItem.Builder().setContent(String.format(Locale.CHINA,"%s%s",context.getString(R.string.b_f_stores_address_sz),order_info.getRegion())).build());
        }else {
            printItems.add(new PrintItem.Builder().setAlign(PrintItem.Align.CENTRE).setContent(footer_c).build());
        }
        return printItems;
    }

    @Override
    protected List<PrintItem> c_format_76(@NonNull final PrintFormatInfo format_info,@NonNull final String orderCode) {
        return null;
    }

    @Override
    protected List<PrintItem> c_format_80(@NonNull final PrintFormatInfo format_info,@NonNull final String orderCode) {
        return null;
    }

}
