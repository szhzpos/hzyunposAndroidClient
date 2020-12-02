package com.wyc.cloudapp.dialog.orderDialog;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;

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

            mRetailDetailsPayInfoAdapter = retailDetailsPayInfoAdapter;
        }
    }

    @Override
    protected void initReprint() {
        final Button m_print_btn = findViewById(R.id.m_print_btn);
        if (m_print_btn != null)m_print_btn.setOnClickListener(v -> Printer.print(mContext, PayDialog.get_print_content(mContext, mRetailOrderCode,false)));
    }

    @Override
    protected void initVerifyPay() {
        final Button m_pay_verify_btn = findViewById(R.id.m_pay_verify_btn);
        if (m_pay_verify_btn != null)m_pay_verify_btn.setOnClickListener(v -> verify_pay());
    }
}
