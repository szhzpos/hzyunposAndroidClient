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
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.RefundDetailsGoodsInfoAdapter;
import com.wyc.cloudapp.adapter.RefundDetailsPayInfoAdapter;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;


public class RefundOrderDetailsDialog extends AbstractDialogMainActivity {
    private final JSONObject mRefundOrderInfo;
    private String mRefundOrderCode;
    public RefundOrderDetailsDialog(@NonNull MainActivity context, final JSONObject info) {
        super(context, context.getString(R.string.refund_details_sz));
        mRefundOrderInfo = info;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showOrderInfo();
        initReprint();
    }
    @Override
    protected int getContentLayoutId(){
        return R.layout.refund_details_dialog_layout;
    }

    @Override
    public void dismiss(){
        super.dismiss();
        Printer.dismissPrintIcon(mContext);
    }

    @Override
    public void show(){
        super.show();
        Printer.showPrintIcon(mContext);
    }

    private void initReprint(){
        final Button reprint_btn = findViewById(R.id.reprint_btn);
        if (null != reprint_btn){
            reprint_btn.setOnClickListener(v -> Printer.print(mContext, RefundDialog.get_print_content(mContext, mRefundOrderCode,false)));
        }
    }
    private void showOrderInfo(){
        final JSONObject object = mRefundOrderInfo;
        if (null != object){
            Logger.d_json(object.toJSONString());

            final TextView oper_time_tv = findViewById(R.id.oper_time),order_code_tv = findViewById(R.id.refund_order_code),order_amt_tv = findViewById(R.id.refund_order_amt),
                    refund_amt_tv = findViewById(R.id.refund_amt),refund_status_tv = findViewById(R.id.refund_status),upload_status_tv = findViewById(R.id.upload_status),
                    cas_name_tv = findViewById(R.id.cas_name),refund_type_tv = findViewById(R.id.refund_type),s_e_status_tv = findViewById(R.id.s_e_status);
            if (oper_time_tv != null)oper_time_tv.setText(Utils.getNullStringAsEmpty(object,"oper_time"));

            if (order_code_tv != null)order_code_tv.setText(mRefundOrderCode = Utils.getNullStringAsEmpty(object,"refund_order_code"));

            if (order_amt_tv != null)order_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("refund_order_amt")));
            if (refund_amt_tv != null)refund_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("refund_amt")));
            if (refund_type_tv != null)refund_type_tv.setText(Utils.getNullStringAsEmpty(object,"refund_type_name"));
            if (s_e_status_tv != null)s_e_status_tv.setText(Utils.getNullStringAsEmpty(object,"s_e_status_name"));
            if (refund_status_tv != null)refund_status_tv.setText(Utils.getNullStringAsEmpty(object,"refund_status_name"));
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
            initGoodsDetail();
            initPayDetail();
        }
    }

    private void initGoodsDetail(){
        final RecyclerView goods_detail = findViewById(R.id.goods_details);
        if (null != goods_detail){
            final RefundDetailsGoodsInfoAdapter refundDetailsGoodsInfoAdapter = new RefundDetailsGoodsInfoAdapter(mContext);
            goods_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            goods_detail.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
            goods_detail.setAdapter(refundDetailsGoodsInfoAdapter);
            refundDetailsGoodsInfoAdapter.setDatas(mRefundOrderInfo.getString("refund_order_code"));
        }
    }
    private void initPayDetail(){
        final RecyclerView pay_detail = findViewById(R.id.pay_details);
        if (null != pay_detail){
            final RefundDetailsPayInfoAdapter refundDetailsPayInfoAdapter = new RefundDetailsPayInfoAdapter(mContext);
            pay_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            pay_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            pay_detail.setAdapter(refundDetailsPayInfoAdapter);
            refundDetailsPayInfoAdapter.setDatas(mRefundOrderInfo.getString("refund_order_code"));
        }
    }
}
