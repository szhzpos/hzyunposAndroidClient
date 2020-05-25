package com.wyc.cloudapp.dialog.orderDialog;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
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
import com.wyc.cloudapp.activity.LoginActivity;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.TransferDetailsAdapter;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.ChangeNumOrPriceDialog;
import com.wyc.cloudapp.dialog.HangBillDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnMainActivityImp;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class TransferDialog extends DialogBaseOnMainActivityImp {
    private TransferDetailsAdapter mTransferDetailsAdapter;
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
                            mContext.runOnUiThread(()-> {
                                TransferDialog.this.dismiss();
                                MyDialog my_dialog = new MyDialog(mContext);
                                my_dialog.setMessage("交班成功！").setYesOnclickListener(mContext.getString(R.string.OK), new MyDialog.onYesOnclickListener() {
                                    @Override
                                    public void onYesClick(MyDialog myDialog) {
                                        myDialog.dismiss();
                                        Intent intent = new Intent(mContext, LoginActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        mContext.startActivity(intent);
                                        mContext.finish();
                                    }
                                }).show();
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

    public void verifyTransfer(){
        final StringBuilder err  = new StringBuilder();
        int code = mTransferDetailsAdapter.verifyTransfer(err);
        switch (code){
            case 0:
                this.show();
                break;
            case 1://有正在支付的订单
                MyDialog.displayAskMessage(null, "有正在支付订单，是否现在处理?", mContext, new MyDialog.onYesOnclickListener() {
                    @Override
                    public void onYesClick(MyDialog myDialog) {
                        final QuerySaleOrderDialog querySaleOrderDialog = new QuerySaleOrderDialog(mContext);
                        querySaleOrderDialog.show();
                        myDialog.dismiss();
                    }
                }, MyDialog::dismiss);
                break;
            case 2://当前收银员有挂单没处理
                MyDialog.displayAskMessage(null, "当前收银员有挂单信息没处理，是否现在处理?", mContext, myDialog -> {
                    mContext.disposeHangBill();
                    myDialog.dismiss();
                }, MyDialog::dismiss);
                break;
            default:
                MyDialog.displayErrorMessage(null,"确定是否可以交班错误：" + err,mContext);
                break;
        }
    }

    private static String c_format_58(final Context context, final JSONObject format_info, final JSONObject order_info, boolean is_open_cash_box){
        return "";
    }
    public static String get_print_content(final MainActivity context,final String ti_code,boolean is_open_cash_box){
        final JSONObject print_format_info = new JSONObject(),order_info = new JSONObject();
        String content = "";
        if (SQLiteHelper.getLocalParameter("c_f_info",print_format_info)){
            if (print_format_info.getIntValue("f") == R.id.checkout_format){

                switch (print_format_info.getIntValue("f_z")){
                    case R.id.f_58:
                        content = c_format_58(context,print_format_info,order_info,is_open_cash_box);
                        break;
                    case R.id.f_76:
                        break;
                    case R.id.f_80:
                        break;
                }
            }
        }else
            context.runOnUiThread(()->MyDialog.ToastMessage("加载打印格式错误：" + print_format_info.getString("info"), context,null));

        return content;
    }
}
