package com.wyc.cloudapp.dialog;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.SaleReturnGoodsInfoAdapter;


public class SaleReturnDialog extends BaseDialog {
    private SaleReturnGoodsInfoAdapter mSaleReturnGoodsInfoAdapter;
    private String mOrderCode;
    public SaleReturnDialog(@NonNull MainActivity context,final String title,final String order_code) {
        super(context, title);
        mOrderCode = order_code;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.sale_return_dialog_layout);

        initOrderCodeTv();
        initGoodsDetails();
    }

    private void initGoodsDetails(){
        final RecyclerView goods_detail = findViewById(R.id.goods_details);
        if (null != goods_detail){
            mSaleReturnGoodsInfoAdapter = new SaleReturnGoodsInfoAdapter(mContext);
            goods_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            goods_detail.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
            goods_detail.setAdapter(mSaleReturnGoodsInfoAdapter);
            mSaleReturnGoodsInfoAdapter.setDatas(mOrderCode);
        }
    }

    private void initOrderCodeTv(){
        final TextView order_code_tv = findViewById(R.id.order_code);
        if (order_code_tv != null){
            order_code_tv.setText(mOrderCode);
        }
    }
}
