package com.wyc.cloudapp.activity.mobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.adapter.MobileSaleGoodsAdapter;
import com.wyc.cloudapp.adapter.SaleGoodsItemDecoration;
import com.wyc.cloudapp.adapter.SuperItemDecoration;
import com.wyc.cloudapp.dialog.CustomizationView.BasketView;
import com.wyc.cloudapp.dialog.MyDialog;

public class MobileCashierActivity extends SaleActivity {
    private BasketView mBasketView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_cashier);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) ;//显示状态栏

        initSaleGoodsAdapter();
        initTitle();
        initBasketView();
    }

    private void initBasketView(){
        mBasketView = findViewById(R.id.basketView);
        mBasketView.setOnClickListener(new View.OnClickListener() {
            int num = 1;
            @Override
            public void onClick(View v) {

                mBasketView.update(num++);
            }
        });
    }

    private void initTitle(){
        final TextView left = findViewById(R.id.left_title_tv),middle = findViewById(R.id.middle_title_tv),right = findViewById(R.id.right_title_tv);

        //默认退出
        left.setOnClickListener(v -> onBackPressed());
        left.setText(R.string.back);

        final Intent intent = getIntent();
        middle.setText(intent.getStringExtra("title"));

        right.setText(R.string.clear_sz);
        right.setOnClickListener(new View.OnClickListener() {
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