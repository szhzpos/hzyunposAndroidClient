package com.wyc.cloudapp.dialog.orderDialog;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.RetailDetailsGoodsInfoAdapter;
import com.wyc.cloudapp.adapter.RetailDetailsPayInfoAdapter;
import com.wyc.cloudapp.dialog.pay.AbstractSettlementDialog;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public class NormalRetailOrderDetailsDialog extends AbstractRetailOrderDetailsDialog {
    public NormalRetailOrderDetailsDialog(@NonNull MainActivity context, final JSONObject info) {
        super(context,context.getString(R.string.order_detail_sz),info);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentLayoutId(){
        return R.layout.retail_details_dialog_layout;
    }


    @Override
    protected void showOrderInfo(){
        final JSONObject object = mOrderInfo;
        if (null != object){
            final TextView oper_time_tv = findViewById(R.id.oper_time),order_code_tv = findViewById(R.id.order_code),order_amt_tv = findViewById(R.id.order_amt),reality_amt_tv = findViewById(R.id.reality_amt),
                    order_status_tv = findViewById(R.id.order_status),pay_status_tv = findViewById(R.id.pay_status),s_e_status_tv = findViewById(R.id.s_e_status),upload_status_tv = findViewById(R.id.upload_status),
                    cas_name_tv = findViewById(R.id.cas_name);
            if (oper_time_tv != null)oper_time_tv.setText(Utils.getNullStringAsEmpty(object,"oper_time"));

            if (order_code_tv != null)order_code_tv.setText(Utils.getNullStringAsEmpty(object,"order_code"));

            if (order_amt_tv != null)order_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("order_amt")));
            if (reality_amt_tv != null)reality_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("reality_amt")));
            if (order_status_tv != null)order_status_tv.setText(Utils.getNullStringAsEmpty(object,"order_status_name"));
            if (pay_status_tv != null)pay_status_tv.setText(Utils.getNullStringAsEmpty(object,"pay_status_name"));
            if (s_e_status_tv != null)s_e_status_tv.setText(Utils.getNullStringAsEmpty(object,"s_e_status_name"));
            if (upload_status_tv != null)upload_status_tv.setText(Utils.getNullStringAsEmpty(object,"upload_status_name"));
            if (cas_name_tv != null)cas_name_tv.setText(Utils.getNullStringAsEmpty(object,"cas_name"));

            final String sz_remark = Utils.getNullStringAsEmpty(object,"remark");
            if (!sz_remark.isEmpty()){
                final LinearLayout remark_layout = findViewById(R.id.remark_layout);
                if (remark_layout != null){
                    final TextView remark_tv = remark_layout.findViewById(R.id.remark);
                    if (null != remark_tv){
                        remark_layout.setVisibility(View.VISIBLE);
                        remark_tv.setText(sz_remark);
                    }
                }
            }
            final String sz_card_code = Utils.getNullStringAsEmpty(object,"card_code");
            if (!sz_card_code.isEmpty()){
                final LinearLayout vip_info_layout = findViewById(R.id.vip_info_layout);
                if (vip_info_layout != null){
                    vip_info_layout.setVisibility(View.VISIBLE);
                    final TextView card_cdoe_tv = vip_info_layout.findViewById(R.id.card_code),vip_name_tv = vip_info_layout.findViewById(R.id.vip_name),vip_mobile_tv = vip_info_layout.findViewById(R.id.vip_mobile);
                    if (null != card_cdoe_tv && vip_name_tv != null && vip_mobile_tv != null){
                        card_cdoe_tv.setText(Utils.getNullStringAsEmpty(object,"card_code"));
                        vip_name_tv.setText(Utils.getNullStringAsEmpty(object,"vip_name"));
                        vip_mobile_tv.setText(Utils.getNullStringAsEmpty(object,"mobile"));
                    }
                }
            }
        }
    }

    @Override
    protected void initGoodsDetail(){
        final RecyclerView goods_detail = findViewById(R.id.goods_details);
        if (null != goods_detail){
            final RetailDetailsGoodsInfoAdapter retailDetailsGoodsInfoAdapter = new RetailDetailsGoodsInfoAdapter(mContext);
            goods_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            goods_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            goods_detail.setAdapter(retailDetailsGoodsInfoAdapter);
            retailDetailsGoodsInfoAdapter.setDatas(Utils.getNullStringAsEmpty(mOrderInfo,"order_code"));
        }
    }

   @Override
    protected void initPayDetail(){
        final RecyclerView pay_detail = findViewById(R.id.pay_details);
        if (null != pay_detail){
            mRetailDetailsPayInfoAdapter = new RetailDetailsPayInfoAdapter(mContext);
            pay_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            pay_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            pay_detail.setAdapter(mRetailDetailsPayInfoAdapter);
            mRetailDetailsPayInfoAdapter.setDatas(Utils.getNullStringAsEmpty(mOrderInfo,"order_code"));
            mRetailDetailsPayInfoAdapter.setItemClickListener(pay_record -> mPayRecord = pay_record);
        }
    }
    @Override
    protected void initReprint(){
        final Button reprint_btn = findViewById(R.id.reprint_btn);
        if (null != reprint_btn){
            reprint_btn.setOnClickListener(v -> {
                AbstractSettlementDialog.printObj(mContext, Utils.getNullStringAsEmpty(mOrderInfo,"order_code"));
            });
        }
    }
    @Override
    protected void initVerifyPay(){
        final Button verify_pay_btn = findViewById(R.id.verify_pay_btn);
        if (null != verify_pay_btn){
            verify_pay_btn.setOnClickListener(v -> verify_pay());
        }
    }
}
