package com.wyc.cloudapp.dialog;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.OrderDetailsGoodsInfoAdapter;
import com.wyc.cloudapp.adapter.OrderDetailsPayInfoAdapter;
import com.wyc.cloudapp.utils.Utils;

public class OrderDetaislDialog extends BaseDialog {
    private JSONObject mOrderInfo;
    private OrderDetailsGoodsInfoAdapter mOrderDetailsGoodsInfoAdapter;
    private OrderDetailsPayInfoAdapter mOrderDetailsPayInfoAdapter;
    public OrderDetaislDialog(@NonNull MainActivity context, final String title, final JSONObject info) {
        super(context,title);
        mOrderInfo = info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.order_details_dialog_layout);

        initGoodsDetail();
        initPayDetail();
        showOrderInfo();
    }

    private void showOrderInfo(){
        final JSONObject object = mOrderInfo;
        if (null != object){
            final TextView oper_time_tv = findViewById(R.id.oper_time),order_code_tv = findViewById(R.id.order_code),order_amt_tv = findViewById(R.id.order_amt),reality_amt_tv = findViewById(R.id.reality_amt),
                    order_status_tv = findViewById(R.id.order_status),pay_status_tv = findViewById(R.id.pay_status),s_e_status_tv = findViewById(R.id.s_e_status),upload_status_tv = findViewById(R.id.upload_status),
                    cas_name_tv = findViewById(R.id.cas_name),remark_tv = findViewById(R.id.remark);
            if (oper_time_tv != null)oper_time_tv.setText(Utils.getNullStringAsEmpty(object,"oper_time"));
            if (order_code_tv != null)order_code_tv.setText(Utils.getNullStringAsEmpty(object,"order_code"));
            if (order_amt_tv != null)order_amt_tv.setText(Utils.getNullStringAsEmpty(object,"order_amt"));
            if (reality_amt_tv != null)reality_amt_tv.setText(Utils.getNullStringAsEmpty(object,"reality_amt"));
            if (order_status_tv != null)order_status_tv.setText(Utils.getNullStringAsEmpty(object,"order_status_name"));
            if (pay_status_tv != null)pay_status_tv.setText(Utils.getNullStringAsEmpty(object,"pay_status_name"));
            if (s_e_status_tv != null)s_e_status_tv.setText(Utils.getNullStringAsEmpty(object,"s_e_status_name"));
            if (upload_status_tv != null)upload_status_tv.setText(Utils.getNullStringAsEmpty(object,"upload_status_name"));
            if (cas_name_tv != null)cas_name_tv.setText(Utils.getNullStringAsEmpty(object,"cas_name"));
            if (remark_tv != null){
                final String sz_remark = Utils.getNullStringAsEmpty(object,"remark");
                if (!sz_remark.isEmpty()){
                    remark_tv.setVisibility(View.VISIBLE);
                    remark_tv.setText(sz_remark);
                }
            }

        }
    }
    private void initGoodsDetail(){
            final RecyclerView goods_detail = findViewById(R.id.goods_details);
            if (null != goods_detail){
                mOrderDetailsGoodsInfoAdapter = new OrderDetailsGoodsInfoAdapter(mContext);
                goods_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
                goods_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
                goods_detail.setAdapter(mOrderDetailsGoodsInfoAdapter);
                mOrderDetailsGoodsInfoAdapter.setDatas(mOrderInfo.getString("order_code"));
            }
    }
    private void initPayDetail(){
        final RecyclerView pay_detail = findViewById(R.id.pay_details);
        if (null != pay_detail){
            mOrderDetailsPayInfoAdapter = new OrderDetailsPayInfoAdapter(mContext);
            pay_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            pay_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            pay_detail.setAdapter(mOrderDetailsPayInfoAdapter);
            mOrderDetailsPayInfoAdapter.setDatas(mOrderInfo.getString("order_code"));
        }
    }
}
