package com.wyc.cloudapp.activity.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.MobileSaleGoodsAdapter;
import com.wyc.cloudapp.adapter.SaleGoodsItemDecoration;
import com.wyc.cloudapp.adapter.SuperItemDecoration;
import com.wyc.cloudapp.dialog.MyDialog;

public class CashierActivity extends AbstractMobileActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_cashier_desk;
    }

    @Override
    protected void initTitleText(){
        final Intent intent = getIntent();
        setMiddleText(intent.getStringExtra("title"));
        setRightText(getString(R.string.clear_sz));
    }

    @Override
    protected void initTitleClickListener(){
        setRightListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog.ToastMessage("清空",v.getContext(),null);
            }
        });
    }

    private void initSaleGoodsAdapter(){
        final RecyclerView mSaleGoodsRecyclerView = findViewById(R.id.mobile_sale_goods_list);
        final MobileSaleGoodsAdapter mNormalSaleGoodsAdapter = new MobileSaleGoodsAdapter(this);
        mNormalSaleGoodsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged(){
                final JSONArray datas = mNormalSaleGoodsAdapter.getDatas();
                double sale_sum_num = 0.0,sale_sum_amount = 0.0,dis_sum_amt = 0.0;

                for (int i = 0,length = datas.size();i < length;i ++){
                    final JSONObject jsonObject = datas.getJSONObject(i);
                    sale_sum_num += jsonObject.getDouble("xnum");
                    sale_sum_amount += jsonObject.getDouble("sale_amt");
                    dis_sum_amt += jsonObject.getDouble("discount_amt");
                }
                mSaleGoodsRecyclerView.scrollToPosition(mNormalSaleGoodsAdapter.getCurrentItemIndex());
            }
        });
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(mSaleGoodsRecyclerView,getResources().getDimension(R.dimen.sale_goods_height),new SaleGoodsItemDecoration(getColor(R.color.gray_subtransparent)));
        mSaleGoodsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        mSaleGoodsRecyclerView.setAdapter(mNormalSaleGoodsAdapter);
    }
}