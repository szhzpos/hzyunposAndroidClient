package com.wyc.cloudapp.dialog.orderDialog;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.MobileTransferDetailsAdapter;
import com.wyc.cloudapp.callback.PasswordEditTextReplacement;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class MobileTransferDialog extends AbstractTransferDialog {
    public MobileTransferDialog(@NonNull MainActivity context) {
        super(context);
        mTransferDetailsAdapter = new MobileTransferDetailsAdapter(mContext,new JSONArray());
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initTransferInfoList();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.mobile_transfer_dialog_layout;
    }

    @Override
    protected void initWindowSize(){//初始化窗口尺寸
        fullScreen();
    }


    private void initTransferInfoList(){
        mTransferDetailsAdapter.setDatas(mContext.getCashierId());
        final RecyclerView retail_details_list = findViewById(R.id.retail_details_list),refund_details_list = findViewById(R.id.refund_details_list),
                recharge_details_list = findViewById(R.id.recharge_details_list),time_card_details = findViewById(R.id.time_card_details),gift_card_details = findViewById(R.id.gift_card_details);

        retail_details_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        refund_details_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        recharge_details_list.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        time_card_details.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        gift_card_details.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));

        retail_details_list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        refund_details_list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        recharge_details_list.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        time_card_details.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
        gift_card_details.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));

        retail_details_list.setAdapter(new MobileTransferDetailsAdapter(mContext,aggregateRetails()));
        refund_details_list.setAdapter(new MobileTransferDetailsAdapter(mContext,aggregateRefunds()));
        recharge_details_list.setAdapter(new MobileTransferDetailsAdapter(mContext,aggregateDeposits()));
        time_card_details.setAdapter(new MobileTransferDetailsAdapter(mContext,aggregateTimeCard()));
        gift_card_details.setAdapter(new MobileTransferDetailsAdapter(mContext,aggregateGiftCard()));

        setFooterInfo();
    }

    private JSONArray aggregateRetails(){
        final JSONArray retails = ((MobileTransferDetailsAdapter)mTransferDetailsAdapter).getTransferRetails();
        final JSONObject object = new JSONObject();
        if (!retails.isEmpty()){
            final JSONObject transfer_sum = mTransferDetailsAdapter.getTransferSumInfo();
            double amt = transfer_sum.getDoubleValue("order_money");
            int num = transfer_sum.getIntValue("retail_total_orders");

            if (!Utils.equalDouble(amt,0.0) || num != 0){
                object.put("pay_name",mContext.getString(R.string.sum));
            }else{
                object.put("pay_name",mContext.getString(R.string.Not_data));
            }
            object.put("order_num",num);
            object.put("pay_money",amt);
        }else{
            object.put("pay_name",mContext.getString(R.string.Not_data));
            object.put("order_num",0.0);
            object.put("pay_money",0.0);
        }
        retails.add(object);

        return retails;
    }
    private JSONArray aggregateRefunds(){
        final JSONArray refunds = ((MobileTransferDetailsAdapter)mTransferDetailsAdapter).getTransferRefunds();
        final JSONObject object = new JSONObject();
        if (!refunds.isEmpty()){
            final JSONObject transfer_sum = mTransferDetailsAdapter.getTransferSumInfo();
            double amt = transfer_sum.getDoubleValue("refund_money");
            int num = transfer_sum.getIntValue("refund_total_orders");

            if (!Utils.equalDouble(amt,0.0) || num != 0){
                object.put("pay_name",mContext.getString(R.string.sum));
            }else{
                object.put("pay_name",mContext.getString(R.string.Not_data));
            }
            object.put("order_num",num);
            object.put("pay_money",amt);
        }else{
            object.put("pay_name",mContext.getString(R.string.Not_data));
            object.put("order_num",0.0);
            object.put("pay_money",0.0);
        }
        refunds.add(object);

        return refunds;
    }
    private JSONArray aggregateDeposits(){
        final JSONArray deposits = ((MobileTransferDetailsAdapter)mTransferDetailsAdapter).getTransferDeposits();
        final JSONObject object = new JSONObject();
        if (!deposits.isEmpty()){
            final JSONObject transfer_sum = mTransferDetailsAdapter.getTransferSumInfo();
            double amt = transfer_sum.getDoubleValue("recharge_money");
            int num = transfer_sum.getIntValue("deposits_total_orders");

            if (!Utils.equalDouble(amt,0.0) || num != 0){
                object.put("pay_name",mContext.getString(R.string.sum));
            }else{
                object.put("pay_name",mContext.getString(R.string.Not_data));
            }
            object.put("order_num",num);
            object.put("pay_money",amt);
        }else{
            object.put("pay_name",mContext.getString(R.string.Not_data));
            object.put("order_num",0.0);
            object.put("pay_money",0.0);
        }
        deposits.add(object);

        return deposits;
    }

    private JSONArray aggregateTimeCard(){
        final JSONArray timeCard = ((MobileTransferDetailsAdapter)mTransferDetailsAdapter).getTransferTimeCard();
        final JSONObject object = new JSONObject();
        if (!timeCard.isEmpty()){
            final JSONObject transfer_sum = mTransferDetailsAdapter.getTransferSumInfo();
            double amt = transfer_sum.getDoubleValue("cards_money");
            int num = transfer_sum.getIntValue("cardsc_total_orders");

            if (!Utils.equalDouble(amt,0.0) || num != 0){
                object.put("pay_name",mContext.getString(R.string.sum));
            }else{
                object.put("pay_name",mContext.getString(R.string.Not_data));
            }
            object.put("order_num",num);
            object.put("pay_money",amt);
        }else{
            object.put("pay_name",mContext.getString(R.string.Not_data));
            object.put("order_num",0.0);
            object.put("pay_money",0.0);
        }
        timeCard.add(object);

        return timeCard;
    }

    private JSONArray aggregateGiftCard(){
        final JSONArray giftCard = ((MobileTransferDetailsAdapter)mTransferDetailsAdapter).getTransferGiftCard();
        final JSONObject object = new JSONObject();
        if (!giftCard.isEmpty()){
            final JSONObject transfer_sum = mTransferDetailsAdapter.getTransferSumInfo();
            double amt = transfer_sum.getDoubleValue("shopping_money");
            int num = transfer_sum.getIntValue("gift_total_orders");

            if (!Utils.equalDouble(amt,0.0) || num != 0){
                object.put("pay_name",mContext.getString(R.string.sum));
            }else{
                object.put("pay_name",mContext.getString(R.string.Not_data));
            }
            object.put("order_num",num);
            object.put("pay_money",amt);
        }else{
            object.put("pay_name",mContext.getString(R.string.Not_data));
            object.put("order_num",0.0);
            object.put("pay_money",0.0);
        }
        giftCard.add(object);

        return giftCard;
    }

    private void setFooterInfo(){
        final JSONObject object = mTransferDetailsAdapter.getTransferSumInfo();
        final TextView cas_name = findViewById(R.id.cas_name_tv);
        cas_name.setText(mContext.getCashierName());

        if (!object.isEmpty()){
            final TextView ti_start_time_tv = findViewById(R.id.ti_start_time_tv),payable_amt = findViewById(R.id.payable_amt)
                    ,ti_end_time_tv = findViewById(R.id.ti_end_time_tv),ti_code_tv = findViewById(R.id.ti_code_tv);

            ti_code_tv.setText(object.getString("ti_code"));
            if (mTransferDetailsAdapter.isTransferAmtNotVisible())payable_amt.setTransformationMethod(new PasswordEditTextReplacement());

            final SimpleDateFormat sf = new SimpleDateFormat(FormatDateTimeUtils.YYYY_MM_DD_1, Locale.CHINA);
            ti_start_time_tv.setText(sf.format(object.getLongValue("order_b_date") * 1000));
            ti_end_time_tv.setText(sf.format(object.getLongValue("order_e_date") * 1000));

            payable_amt.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("sj_money")));
        }
    }
}
