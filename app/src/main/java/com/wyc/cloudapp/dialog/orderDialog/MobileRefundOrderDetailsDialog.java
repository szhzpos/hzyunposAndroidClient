package com.wyc.cloudapp.dialog.orderDialog;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.adapter.MobileRefundDetailsGoodsInfoAdapter;
import com.wyc.cloudapp.adapter.MobileRefundDetailsPayInfoAdapter;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.dialog.pay.AbstractSettlementDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public class MobileRefundOrderDetailsDialog extends AbstractDialogMainActivity {
    private final JSONObject mOrderInfo;
    public MobileRefundOrderDetailsDialog(@NonNull MainActivity context,final JSONObject object) {
        super(context, context.getString(R.string.order_detail_sz));
        mOrderInfo = object;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showOrderInfo();
        initGoodsDetail();
        initPayDetail();
        initReprint();
    }

    @Override
    protected void initWindowSize(){
        fullScreen();
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

    @Override
    protected int getContentLayoutId() {
        return R.layout.mobile_refund_details_dialog_layout;
    }

    private void showOrderInfo() {
        final JSONObject object = mOrderInfo;
        if (null != object){

            Logger.d_json(object.toJSONString());

            final TextView m_order_id_tv = findViewById(R.id.m_sale_id_tv),m_order_time_tv = findViewById(R.id.m_order_time_tv),m_vip_no_tv = findViewById(R.id.m_vip_no_tv),
                    m_sale_man_tv = findViewById(R.id.m_sale_man_tv),m_order_amt_tv = findViewById(R.id.m_order_amt_tv),m_disc_amt_tv = findViewById(R.id.m_disc_amt_tv),
                    m_refund_id_tv = findViewById(R.id.m_refund_id_tv),m_refund_type_tv = findViewById(R.id.m_refund_type_tv);

            if (m_order_id_tv != null)m_order_id_tv.setText(Utils.getNullStringAsEmpty(object,"retail_order_code"));
            if (m_refund_id_tv != null)m_refund_id_tv.setText(Utils.getNullStringAsEmpty(object,"refund_order_code"));
            if (m_sale_man_tv != null)m_sale_man_tv.setText(Utils.getNullStringAsEmpty(object,"sc_name"));
            if (m_vip_no_tv != null)m_vip_no_tv.setText(Utils.getNullStringAsEmpty(object,"card_code"));
            if (m_order_time_tv != null)m_order_time_tv.setText(Utils.getNullStringAsEmpty(object,"oper_time"));
            if (m_refund_type_tv != null)m_refund_type_tv.setText(Utils.getNullStringAsEmpty(object,"refund_type_name"));
            if (m_order_amt_tv != null)m_order_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("refund_order_amt")));
        }
    }

    private void initReprint() {
        final Button m_print_btn = findViewById(R.id.m_print_btn);
        if (m_print_btn != null)m_print_btn.setOnClickListener(v -> Printer.print(AbstractSettlementDialog.get_print_content(mContext, Utils.getNullStringAsEmpty(mOrderInfo,"refund_order_code"),false)));
    }

    private void initGoodsDetail(){
        final RecyclerView goods_detail = findViewById(R.id.m_order_details_list);
        if (null != goods_detail){
            final MobileRefundDetailsGoodsInfoAdapter refundDetailsGoodsInfoAdapter = new MobileRefundDetailsGoodsInfoAdapter(mContext);
            goods_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            goods_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            goods_detail.setAdapter(refundDetailsGoodsInfoAdapter);
            refundDetailsGoodsInfoAdapter.setDatas(Utils.getNullStringAsEmpty(mOrderInfo,"refund_order_code"));
        }
    }

    private void initPayDetail(){
        final RecyclerView pay_detail = findViewById(R.id.m_pay_details_list);
        if (null != pay_detail){
            final MobileRefundDetailsPayInfoAdapter refundDetailsPayInfoAdapter = new MobileRefundDetailsPayInfoAdapter(mContext);
            pay_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            pay_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            pay_detail.setAdapter(refundDetailsPayInfoAdapter);
            refundDetailsPayInfoAdapter.setDatas(Utils.getNullStringAsEmpty(mOrderInfo,"refund_order_code"));
        }
    }
}
