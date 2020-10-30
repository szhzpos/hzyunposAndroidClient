package com.wyc.cloudapp.dialog.vip;

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
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.adapter.VipDepositDetailsPayInfoAdapter;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogSaleActivity;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.Utils;


public class VipDepositDetailsDialog extends AbstractDialogSaleActivity {
    private final JSONObject mOrderInfo;
    public VipDepositDetailsDialog(@NonNull SaleActivity context, final JSONObject object) {
        super(context, context.getString(R.string.order_detail_sz));
        mOrderInfo = object;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showOrderInfo();
        initReprint();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.vip_deposit_details_dialog_layout;
    }

    private void showOrderInfo(){
        final JSONObject object = mOrderInfo;
        if (null != object){
            final TextView oper_time_tv = findViewById(R.id.oper_time),order_code_tv = findViewById(R.id.order_code),order_amt_tv = findViewById(R.id.order_amt),
                    order_status_tv = findViewById(R.id.order_status),s_e_status_tv = findViewById(R.id.s_e_status),cas_name_tv = findViewById(R.id.cas_name);
            if (oper_time_tv != null)oper_time_tv.setText(Utils.getNullStringAsEmpty(object,"oper_time"));

            if (order_code_tv != null)order_code_tv.setText(Utils.getNullStringAsEmpty(object,"order_code"));

            if (order_amt_tv != null)order_amt_tv.setText(Utils.getNullStringAsEmpty(object,"order_amt"));
            if (order_status_tv != null)order_status_tv.setText(Utils.getNullStringAsEmpty(object,"status_name"));
            if (s_e_status_tv != null)s_e_status_tv.setText(Utils.getNullStringAsEmpty(object,"s_e_status_name"));
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
                        vip_name_tv.setText(Utils.getNullStringAsEmpty(object,"name"));
                        vip_mobile_tv.setText(Utils.getNullStringAsEmpty(object,"mobile"));
                    }
                }
            }
            initPayDetail();
        }
    }
    private void initPayDetail(){
        final RecyclerView pay_detail = findViewById(R.id.pay_details);
        if (null != pay_detail){
            final VipDepositDetailsPayInfoAdapter vipDepositDetailsPayInfoAdapter = new VipDepositDetailsPayInfoAdapter(mContext);
            pay_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            pay_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            pay_detail.setAdapter(vipDepositDetailsPayInfoAdapter);
            vipDepositDetailsPayInfoAdapter.setDatas(mOrderInfo.getString("order_code"));
        }
    }
    private void initReprint(){
        final Button reprint_btn = findViewById(R.id.reprint_btn);
        if (null != reprint_btn){
            reprint_btn.setOnClickListener(v -> {
                Printer.print(mContext, VipChargeDialogImp.get_print_content(mContext,mOrderInfo.getString("order_code")));
            });
        }
    }
}
