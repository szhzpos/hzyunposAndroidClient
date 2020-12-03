package com.wyc.cloudapp.dialog.orderDialog;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.MobileRetailDetailsGoodsInfoAdapter;
import com.wyc.cloudapp.adapter.RetailDetailsPayInfoAdapter;
import com.wyc.cloudapp.dialog.pay.PayDialog;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public class MobileRetailOrderDetailsDialog extends AbstractRetailOrderDetailsDialog {
    public MobileRetailOrderDetailsDialog(@NonNull MainActivity context, final JSONObject info) {
        super(context, context.getString(R.string.order_detail_sz),info);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void showOrderInfo() {
        final JSONObject object = mOrderInfo;
        if (null != object){
            final TextView m_order_id_tv = findViewById(R.id.m_order_id_tv),m_order_time_tv = findViewById(R.id.m_order_time_tv),m_vip_no_tv = findViewById(R.id.m_vip_no_tv),
                    m_sale_man_tv = findViewById(R.id.m_sale_man_tv),m_order_amt_tv = findViewById(R.id.m_order_amt_tv),m_disc_amt_tv = findViewById(R.id.m_disc_amt_tv);

            if (m_order_id_tv != null)m_order_id_tv.setText(Utils.getNullStringAsEmpty(object,"order_code"));
            if (m_sale_man_tv != null)m_sale_man_tv.setText(Utils.getNullStringAsEmpty(object,"sc_name"));
            if (m_vip_no_tv != null)m_vip_no_tv.setText(Utils.getNullStringAsEmpty(object,"card_code"));
            if (m_order_time_tv != null)m_order_time_tv.setText(Utils.getNullStringAsEmpty(object,"oper_time"));
            if (m_order_amt_tv != null)m_order_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("order_amt")));
            if (m_disc_amt_tv != null)m_disc_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("order_amt") - object.getDoubleValue("reality_amt")));
        }
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.mobile_retail_details_dialog_layout;
    }

    @Override
    protected void initWindowSize(){
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @Override
    protected void initGoodsDetail(){
        final RecyclerView goods_detail = findViewById(R.id.m_order_details_list);
        if (null != goods_detail){
            final MobileRetailDetailsGoodsInfoAdapter retailDetailsGoodsInfoAdapter = new MobileRetailDetailsGoodsInfoAdapter(mContext);
            goods_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            goods_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            goods_detail.setAdapter(retailDetailsGoodsInfoAdapter);
            retailDetailsGoodsInfoAdapter.setDatas(Utils.getNullStringAsEmpty(mOrderInfo,"order_code"));
        }
    }

    @Override
    protected void initPayDetail(){
        final RecyclerView pay_detail = findViewById(R.id.m_pay_details_list);
        if (null != pay_detail){
            final RetailDetailsPayInfoAdapter retailDetailsPayInfoAdapter = new RetailDetailsPayInfoAdapter(mContext);
            pay_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            pay_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            pay_detail.setAdapter(retailDetailsPayInfoAdapter);
            retailDetailsPayInfoAdapter.setDatas(Utils.getNullStringAsEmpty(mOrderInfo,"order_code"));
            retailDetailsPayInfoAdapter.setItemClickListener(pay_record -> mPayRecord = pay_record);
            mRetailDetailsPayInfoAdapter = retailDetailsPayInfoAdapter;
        }
    }

    @Override
    protected void initReprint() {
        final Button m_print_btn = findViewById(R.id.m_print_btn);
        if (m_print_btn != null)m_print_btn.setOnClickListener(v -> Printer.print(mContext, PayDialog.get_print_content(mContext, Utils.getNullStringAsEmpty(mOrderInfo,"order_code"),false)));
    }

    @Override
    protected void initVerifyPay() {
        final Button m_pay_verify_btn = findViewById(R.id.m_pay_verify_btn);
        if (m_pay_verify_btn != null)m_pay_verify_btn.setOnClickListener(v -> verify_pay());
    }

    private void initRefund(){
        final Button m_refund_btn = findViewById(R.id.m_refund_btn);
        m_refund_btn.setOnClickListener(v -> {
            if (RefundDialog.verifyRefundPermission(mContext)){
                if (mContext.getSingleRefundStatus())mContext.setSingleRefundStatus(false);
                final RefundDialog refundDialog = new RefundDialog(mContext,Utils.getNullStringAsEmpty(mOrderInfo,"order_code"));
                refundDialog.show();
            }
        });
    }
}
