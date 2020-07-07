package com.wyc.cloudapp.dialog.orderDialog;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;

import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.TransferDetailsAdapter;
import com.wyc.cloudapp.callback.PasswordEditTextReplacement;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.ChangeNumOrPriceDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractShowPrinterICODialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransferDialog extends AbstractShowPrinterICODialog {
    private TransferDetailsAdapter mTransferDetailsAdapter;
    private onFinishListener mFinishListener;
    public TransferDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.s_e_sz));

        mTransferDetailsAdapter = new TransferDetailsAdapter(mContext);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTransferInfoList();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.transfer_dialog_layout;
    }

    private void initTransferInfoList(){
        final RecyclerView transfer_list = findViewById(R.id.transfer_info_list);
        mTransferDetailsAdapter.setDatas(mContext.getCashierInfo().getString("cas_id"));
        transfer_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        transfer_list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        transfer_list.setAdapter(mTransferDetailsAdapter);

        setFooterInfo();
        initTransferBtn();
    }

    private void setFooterInfo(){
        final JSONObject object = mTransferDetailsAdapter.getTransferSumInfo();
        final TextView cas_name = findViewById(R.id.cas_name);
        cas_name.setText(mContext.getCashierInfo().getString("cas_name"));

        if (!object.isEmpty()){
            final TextView retail_sum_order_num_tv = findViewById(R.id.retail_sum_order_num),retail_sum_amt_tv = findViewById(R.id.retail_sum_amt),
                    refund_sum_order_num_tv = findViewById(R.id.refund_sum_order_num),refund_sum_amt_tv = findViewById(R.id.refund_sum_amt),
                    deposit_sum_order_num_tv = findViewById(R.id.deposit_sum_order_num),rdeposit_sum_amt_tv = findViewById(R.id.rdeposit_sum_amt),
                    transfer_time = findViewById(R.id.transfer_time),payable_amt = findViewById(R.id.payable_amt);

            boolean visible = mTransferDetailsAdapter.isTransferAmtNotVisible();
            if (visible){
                final PasswordEditTextReplacement editTextReplacement = new PasswordEditTextReplacement();
                retail_sum_amt_tv.setTransformationMethod(editTextReplacement);

                refund_sum_amt_tv.setTransformationMethod(editTextReplacement);

                rdeposit_sum_amt_tv.setTransformationMethod(editTextReplacement);

                payable_amt.setTransformationMethod(editTextReplacement);
            }

            retail_sum_order_num_tv.setText(object.getString("order_num"));

            retail_sum_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("order_money")));

            refund_sum_order_num_tv.setText(object.getString("refund_num"));
            refund_sum_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("refund_money")));

            deposit_sum_order_num_tv.setText(object.getString("recharge_num"));
            rdeposit_sum_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("recharge_money")));

            final Editable editable = transfer_time.getEditableText();
            editable.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(object.getLongValue("order_b_date") * 1000)).append(" ").append(mContext.getString(R.string.to_sz)).append(" ").
                    append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(object.getLongValue("order_e_date") * 1000));

            payable_amt.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("sj_money")));
        }
    }

    private void initTransferBtn(){
        final Button btn = findViewById(R.id.ok_);
        if (btn != null){
            btn.setOnClickListener(v -> {
                final EditText transfer_time = findViewById(R.id.transfer_time);
                if (mTransferDetailsAdapter != null && transfer_time.getText().length() != 0){
                    final ChangeNumOrPriceDialog dialog = new ChangeNumOrPriceDialog(mContext,"钱箱现金",String.valueOf(0.0));
                    dialog.setYesOnclickListener(myDialog -> {
                        final StringBuilder err  = new StringBuilder();
                        if (mTransferDetailsAdapter.saveTransferDetailInfo(dialog.getContent(),err)){
                            Printer.print(mContext,get_print_content(mContext,mTransferDetailsAdapter.getTransferSumInfo().getString("ti_code"),mTransferDetailsAdapter.isTransferAmtNotVisible()));
                            mContext.runOnUiThread(()-> {
                                if (mFinishListener != null)mFinishListener.onFinish();
                            });
                        }else {
                            mContext.runOnUiThread(()-> MyDialog.displayErrorMessage(null,"保存交班信息错误：" +err,mContext));
                        }
                        myDialog.dismiss();
                    }).setNoOnclickListener(ChangeNumOrPriceDialog::dismiss).show();
                }
            });
        }
    }

    public interface onFinishListener{
        void onFinish();
    }
    public void setFinishListener(onFinishListener listener){
        mFinishListener = listener;
    }
    public void verifyTransfer(){
        final StringBuilder info  = new StringBuilder();
        int code = mTransferDetailsAdapter.verifyTransfer(info);
        switch (code){
            case 0:
                this.show();
                break;
            case 1://有正在支付的订单
                MyDialog.displayAskMessage(null, "有正在支付订单，是否现在处理?", mContext, myDialog -> {
                    final QuerySaleOrderDialog querySaleOrderDialog = new QuerySaleOrderDialog(mContext);
                    querySaleOrderDialog.show();
                    querySaleOrderDialog.setQueryCondition(info.toString());
                    myDialog.dismiss();
                }, MyDialog::dismiss);
                break;
            case 2://当前收银员有挂单没处理
                MyDialog.displayAskMessage(null, "当前收银员有挂单信息没处理，是否现在处理?", mContext, myDialog -> {
                    mContext.disposeHangBill();
                    myDialog.dismiss();
                }, MyDialog::dismiss);
                break;
            default:
                MyDialog.displayErrorMessage(null,"确定是否可以交班错误：" + info,mContext);
                break;
        }
    }
    private static String c_format_58(final Context context, final JSONObject format_info, final JSONObject order_info,boolean no_visible){
        Logger.d_json(order_info.toJSONString());

        final StringBuilder info = new StringBuilder();
        int print_count = Utils.getNotKeyAsNumberDefault(format_info,"p_c",1),footer_space = Utils.getNotKeyAsNumberDefault(format_info,"f_s",5);
        final String cas_name = Utils.getNullOrEmptyStringAsDefault(order_info,"cas_name",""),footer_c = Utils.getNullStringAsEmpty(format_info,"f_c"),
                new_line = "\r\n",//Printer.commandToStr(Printer.NEW_LINE);
                new_line_16 = Printer.commandToStr(Printer.LINE_SPACING_16),
                new_line_2 = Printer.commandToStr(Printer.LINE_SPACING_2),new_line_d = Printer.commandToStr(Printer.LINE_SPACING_DEFAULT),
                line = context.getString(R.string.line_58),asterisk = "****";

        String store_name = Utils.getNullStringAsEmpty(format_info,"s_n");
        store_name = store_name.length() == 0 ? Utils.getNullStringAsEmpty(order_info,"stores_name") : store_name;

        info.append(Printer.commandToStr(Printer.OPEN_CASHBOX));

        while (print_count-- > 0) {//打印份数
            info.append(Printer.commandToStr(Printer.DOUBLE_HEIGHT)).append(Printer.commandToStr(Printer.ALIGN_CENTER))
                    .append(store_name).append(Printer.commandToStr(Printer.NORMAL))
                    .append(new_line).append(new_line).append(Printer.commandToStr(Printer.ALIGN_LEFT));

            info.append(context.getString(R.string.t_f_store_sz)).append(store_name).append(new_line);
            info.append(context.getString(R.string.t_f_order_sz)).append(Utils.getNullStringAsEmpty(order_info,"ti_code")).append(new_line);
            info.append(context.getString(R.string.t_f_s_time_sz)).append(Utils.getNullStringAsEmpty(order_info,"order_b_date")).append(new_line);
            info.append(context.getString(R.string.t_f_e_time_sz)).append(Utils.getNullStringAsEmpty(order_info,"order_e_date")).append(new_line);
            info.append(context.getString(R.string.t_f_time_sz)).append(Utils.getNullStringAsEmpty(order_info,"transfer_time")).append(new_line);
            info.append(context.getString(R.string.t_f_oper_sz)).append(cas_name).append(new_line);
            info.append(line).append(new_line);

            JSONObject tmp;
            final JSONArray retail_moneys = Utils.getNullObjectAsEmptyJsonArray(order_info,"retail_moneys");
            if (!retail_moneys.isEmpty()){
                info.append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(asterisk).append(context.getString(R.string.t_f_retail_sz)).append(asterisk).append(new_line).append(Printer.commandToStr(Printer.ALIGN_LEFT));
                info.append(context.getString(R.string.t_f_detail_h_sz).replace("-"," ")).append(new_line).append(line).append(new_line);
                for (int i = 0,size = retail_moneys.size();i < size;i++){
                    tmp = retail_moneys.getJSONObject(i);
                    info.append(Printer.printThreeDataAlignRight_58(3,Utils.getNullStringAsEmpty(tmp,"name"),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",tmp.getDoubleValue("pay_money")),
                            String.valueOf(tmp.getIntValue("order_num")))).append(new_line);
                }
                info.append(line).append(new_line);
            }

            final JSONArray refund_moneys = Utils.getNullObjectAsEmptyJsonArray(order_info,"refund_moneys");
            if (!refund_moneys.isEmpty()){
                info.append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(asterisk).append(context.getString(R.string.t_f_refund_sz)).append(asterisk).append(new_line).append(Printer.commandToStr(Printer.ALIGN_LEFT));
                info.append(context.getString(R.string.t_f_detail_h_sz).replace("-"," ")).append(new_line).append(line).append(new_line);
                for (int i = 0,size = refund_moneys.size();i < size;i++){
                    tmp = refund_moneys.getJSONObject(i);
                    info.append(Printer.printThreeDataAlignRight_58(3,Utils.getNullStringAsEmpty(tmp,"name"),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",tmp.getDoubleValue("pay_money")),
                            String.valueOf(tmp.getIntValue("order_num")))).append(new_line);
                }
                info.append(line).append(new_line);
            }

            final JSONArray recharge_moneys = Utils.getNullObjectAsEmptyJsonArray(order_info,"recharge_moneys");
            if (!recharge_moneys.isEmpty()){
                info.append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(asterisk).append(context.getString(R.string.t_f_deposit_sz)).append(asterisk).append(new_line).append(Printer.commandToStr(Printer.ALIGN_LEFT));
                info.append(context.getString(R.string.t_f_detail_h_sz).replace("-"," ")).append(new_line).append(line).append(new_line);
                for (int i = 0,size = recharge_moneys.size();i < size;i++){
                    tmp = recharge_moneys.getJSONObject(i);
                    info.append(Printer.printThreeDataAlignRight_58(3,Utils.getNullStringAsEmpty(tmp,"name"),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",tmp.getDoubleValue("pay_money")),
                            String.valueOf(tmp.getIntValue("order_num")))).append(new_line);
                }
                info.append(line).append(new_line);
            }
            final JSONArray oncecard_moneys = Utils.getNullObjectAsEmptyJsonArray(order_info,"oncecard_moneys");
            if (!oncecard_moneys.isEmpty()){
                info.append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(asterisk).append(context.getString(R.string.t_f_cards_sz)).append(asterisk).append(new_line).append(Printer.commandToStr(Printer.ALIGN_LEFT));
                info.append(context.getString(R.string.t_f_detail_h_sz).replace("-"," ")).append(new_line).append(line).append(new_line);
                for (int i = 0,size = oncecard_moneys.size();i < size;i++){
                    tmp = oncecard_moneys.getJSONObject(i);
                    info.append(Printer.printThreeData(3,Utils.getNullStringAsEmpty(tmp,"name"),no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",tmp.getDoubleValue("pay_money")),
                            String.valueOf(tmp.getIntValue("order_num")))).append(new_line);
                }
                info.append(line).append(new_line);
            }

            double order_money = order_info.getDoubleValue("order_money"),refund_money = order_info.getDoubleValue("refund_money"),
                    recharge_money = order_info.getDoubleValue("recharge_money"),cards_money = order_info.getDoubleValue("cards_money");

            if (!Utils.equalDouble(order_money,0.0))
                info.append(context.getString(R.string.t_f_retail_s_sz)).append(no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",order_money)).append(new_line);
            if (!Utils.equalDouble(recharge_money,0.0))
                info.append(context.getString(R.string.t_f_deposit_s_sz)).append(no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",recharge_money)).append(new_line);
            if (!Utils.equalDouble(cards_money,0.0))
                info.append(context.getString(R.string.t_f_cards_s_sz)).append(no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",cards_money)).append(new_line);
            if (!Utils.equalDouble(refund_money,0.0))
                info.append(context.getString(R.string.t_f_refund_s_sz)).append(no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",refund_money)).append(new_line);

            info.append(context.getString(R.string.t_f_s_sz)).append(no_visible ? asterisk : String.format(Locale.CHINA,"%.2f",order_money - refund_money)).append(new_line);
            info.append(line).append(new_line);

            info.append(context.getString(R.string.t_f_cash_sz)).append("：").append(no_visible ? asterisk : order_info.getDoubleValue("sj_money")).append(new_line);
            info.append(context.getString(R.string.t_f_cashbox_sz)).append(order_info.getDoubleValue("cashbox_money")).append(new_line);

            info.append(line).append(new_line_2).append(new_line).append(new_line_d);

            if (footer_c.isEmpty()){
                info.append(context.getString(R.string.b_f_hotline_sz)).append(Utils.getNullOrEmptyStringAsDefault(order_info,"telphone","")).append(new_line);
                info.append(context.getString(R.string.b_f_stores_address_sz)).append(Utils.getNullOrEmptyStringAsDefault(order_info,"region","")).append(new_line);
            }else {
                info.append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(footer_c).append(new_line);;
            }
            info.append(Printer.printTwoData(1,context.getString(R.string.t_f_f_sign_sz),context.getString(R.string.t_f_c_sign_sz))).append(new_line);

            for (int i = 0; i < footer_space; i++) info.append(" ").append(new_line);

            if (print_count > 0){
                info.append(new_line).append(new_line).append(new_line);
            }
            info.append(Printer.commandToStr(Printer.RESET));
        }

        Logger.d(info);

        return info.toString();
    }
    private static String get_print_content(final MainActivity context,final String ti_code,boolean no_visible){
        final JSONObject print_format_info = new JSONObject(),order_info = new JSONObject();
        String content = "";
        if (SQLiteHelper.getLocalParameter("t_f_info",print_format_info)){
            if (print_format_info.getIntValue("f") == R.id.transfer_format){
                if (getPrintOrderInfo(ti_code,order_info)){
                    switch (print_format_info.getIntValue("f_z")){
                        case R.id.f_58:
                            content = c_format_58(context,print_format_info,order_info,no_visible);
                            break;
                        case R.id.f_76:
                            break;
                        case R.id.f_80:
                            break;
                    }
                }else {
                    context.runOnUiThread(()->MyDialog.ToastMessage(context.getString(R.string.l_p_c_err_hint_sz,order_info.getString("info")), context,context.getWindow()));
                }
            }else {
                context.runOnUiThread(()->MyDialog.ToastMessage(context.getString(R.string.f_not_sz), context,context.getWindow()));
            }
        }else
            context.runOnUiThread(()->MyDialog.ToastMessage(context.getString(R.string.l_p_f_err_hint_sz,print_format_info.getString("info")), context,context.getWindow()));

        return content;
    }
    private static boolean getPrintOrderInfo(final String ti_code,final JSONObject order_info) {
        boolean code = false;
        String details_where_sql = " where ti_code = '"+ ti_code +"'",
                transfer_sum_sql = "SELECT a.sj_money,a.cards_num,a.cards_money,a.order_money,datetime(a.order_e_date, 'unixepoch', 'localtime') order_e_date,datetime(a.order_b_date, 'unixepoch', 'localtime') order_b_date" +
                ",a.recharge_num,a.recharge_money,a.refund_num, a.refund_money,a.cashbox_money,a.sum_money,a.ti_code,datetime(a.transfer_time, 'unixepoch', 'localtime') transfer_time,a.order_num," +
                "a.cas_id,a.stores_id,b.stores_name,b.telphone,b.region,c.cas_name FROM transfer_info a inner join shop_stores b on a.stores_id = b.stores_id inner join cashier_info c on a.cas_id = c.cas_id" + details_where_sql,
                transfer_retails_sql = "SELECT a.order_num,b.name,a.pay_money FROM transfer_money_info a left join pay_method b on a.pay_method = b.pay_method_id" + details_where_sql,
                transfer_cards_sql = "SELECT a.order_num,b.name,a.pay_money  FROM transfer_once_cardsc a left join pay_method b on a.pay_method = b.pay_method_id" + details_where_sql,
                transfer_recharge_sql = "SELECT a.order_num,b.name,a.pay_money  FROM transfer_recharge_money a left join pay_method b on a.pay_method = b.pay_method_id" + details_where_sql,
                transfer_refund_sql = "SELECT a.order_num,b.name,a.pay_money FROM transfer_refund_money a left join pay_method b on a.pay_method = b.pay_method_id" + details_where_sql;

        if (SQLiteHelper.execSql(order_info,transfer_sum_sql)){
            final StringBuilder err = new StringBuilder();

            final JSONArray transfer_retails_arr = SQLiteHelper.getListToJson(transfer_retails_sql,err),transfer_cards_arr = SQLiteHelper.getListToJson(transfer_cards_sql,err),
                    transfer_recharge_arr = SQLiteHelper.getListToJson(transfer_recharge_sql,err),transfer_refund_arr = SQLiteHelper.getListToJson(transfer_refund_sql,err);

            if (null != transfer_retails_arr && transfer_cards_arr != null && transfer_recharge_arr != null && transfer_refund_arr != null){
                order_info.put("retail_moneys",transfer_retails_arr);
                order_info.put("refund_moneys",transfer_refund_arr);
                order_info.put("recharge_moneys",transfer_recharge_arr);
                order_info.put("oncecard_moneys",transfer_cards_arr);
                order_info.put("giftcard_moneys",new JSONArray());

                Logger.d_json(order_info.toJSONString());

                code = true;
            }else {
                order_info.put("info",err);
            }
        }
        return code;
    }
}
