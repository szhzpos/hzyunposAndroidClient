package com.wyc.cloudapp.activity.mobile;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.CustomizationView.BasketView;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.adapter.GoodsCategoryAdapter;
import com.wyc.cloudapp.adapter.GoodsInfoItemDecoration;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.adapter.SaleGoodsItemDecoration;
import com.wyc.cloudapp.adapter.SuperItemDecoration;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.goods.AddGoodsInfoDialog;
import com.wyc.cloudapp.logger.Logger;

public class MobileCashierActivity extends SaleActivity {
    private static final int CODE_REQUEST_CODE = 0x000000bb;
    private BasketView mBasketView;
    private EditText mSearchContent;
    private GoodsInfoViewAdapter mGoodsInfoViewAdapter;
    private GoodsCategoryAdapter mGoodsCategoryAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_cashier);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) ;//显示状态栏

        initGoodsInfoAdapter();
        initGoodsCategoryAdapter();
        initSaleGoodsAdapter();

        initTitle();
        initBasketView();
        initSearchContent();
    }

    private void initGoodsInfoAdapter(){
        mGoodsInfoViewAdapter = new GoodsInfoViewAdapter(this);
        final RecyclerView goods_info_view = findViewById(R.id.mobile_goods_info_list);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this,GoodsInfoViewAdapter.MOBILE_SPAN_COUNT);
        goods_info_view.setLayoutManager(gridLayoutManager);
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(goods_info_view,getResources().getDimension(R.dimen.goods_height),new GoodsInfoItemDecoration(-1));
        mGoodsInfoViewAdapter.setOnItemClickListener(object -> {
            if (object != null)addSaleGoods(object);
        });
        goods_info_view.setAdapter(mGoodsInfoViewAdapter);
    }

    private void initGoodsCategoryAdapter(){
        final RecyclerView goods_type_view = findViewById(R.id.mobile_goods_type_list);
        mGoodsCategoryAdapter = new GoodsCategoryAdapter(this,null);
        goods_type_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        mGoodsCategoryAdapter.setDatas(0);
        goods_type_view.setAdapter(mGoodsCategoryAdapter);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText search = findViewById(R.id.mobile_search_content);
        search.setOnKeyListener((v, keyCode, event) -> {
            Logger.d("keyCode:%d,action:%d",keyCode,event.getAction());
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_DOWN){
                final SaleActivity context = this;
                final String content = search.getText().toString();
                if (content.length() == 0){
                    mGoodsCategoryAdapter.trigger_preView();
                }else{
                    if (!mGoodsInfoViewAdapter.fuzzy_search_goods(content,true)) {
                        search.post(()->{
                            if (mApplication.isConnection() && AddGoodsInfoDialog.verifyGoodsAddPermissions(context)) {
                                if (1 == MyDialog.showMessageToModalDialog(context,"未找到匹配商品，是否新增?")){
                                    final AddGoodsInfoDialog addGoodsInfoDialog = new AddGoodsInfoDialog(context);
                                    addGoodsInfoDialog.setBarcode(search.getText().toString());
                                    addGoodsInfoDialog.setFinishListener(barcode -> {
                                        mGoodsInfoViewAdapter.fuzzy_search_goods(content,true);
                                        addGoodsInfoDialog.dismiss();
                                    });
                                    addGoodsInfoDialog.show();
                                }
                            } else
                                MyDialog.ToastMessage("无此商品!", context, getWindow());
                        });
                    }
                }
                return true;
            }
            return false;
        });
        search.setTransformationMethod(new ReplacementTransformationMethod() {
            @Override
            protected char[] getOriginal() {
                return new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o',
                        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
            }

            @Override
            protected char[] getReplacement() {
                return new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O',
                        'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
            }
        });
        search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = mSearchContent.getWidth();
                if (dx > (w - mSearchContent.getCompoundPaddingRight())) {
                    mSearchContent.requestFocus();
                    final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    startActivityForResult(intent, CODE_REQUEST_CODE);
                }else if(dx < w - mSearchContent.getCompoundPaddingLeft()){
                    switchView();
                }
            }
            return false;
        });
        mSearchContent = search;
    }

    private void switchView(){
        final ViewGroup sale_info_layout = findViewById(R.id.sale_info_layout),
                goods_info_layout = findViewById(R.id.goods_info_layout);
        if (sale_info_layout.getVisibility() == View.GONE){
            sale_info_layout.setVisibility(View.VISIBLE);
            goods_info_layout.setVisibility(View.GONE);
        }else {
            sale_info_layout.setVisibility(View.GONE);
            goods_info_layout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){//条码回调
        if (resultCode == RESULT_OK ){
            if (requestCode == CODE_REQUEST_CODE) {
                if (mSearchContent != null){
                    mSearchContent.setText(intent.getStringExtra("auth_code"));
                    CustomApplication.execute(()->{
                        final Instrumentation inst = new Instrumentation();
                        inst.sendKeyDownUpSync(KeyEvent.KEYCODE_ENTER);
                    });
                }
            }
        }
        super.onActivityResult(requestCode,resultCode,intent);
    }

    private void initBasketView(){
        mBasketView = findViewById(R.id.basketView);
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

    @Override
    public void onBackPressed(){
        final ViewGroup goods_info_layout = findViewById(R.id.goods_info_layout);
        if (goods_info_layout.getVisibility() == View.VISIBLE){
            switchView();
        }else
            super.onBackPressed();
    }

    private void initSaleGoodsAdapter(){
        final RecyclerView mSaleGoodsRecyclerView = findViewById(R.id.mobile_sale_goods_list);
        mSaleGoodsAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged(){
                final JSONArray datas = mSaleGoodsAdapter.getDatas();
                double sale_sum_num = 0.0,sale_sum_amount = 0.0,dis_sum_amt = 0.0;

                for (int i = 0,length = datas.size();i < length;i ++){
                    final JSONObject jsonObject = datas.getJSONObject(i);
                    sale_sum_num += jsonObject.getDouble("xnum");
                    sale_sum_amount += jsonObject.getDouble("sale_amt");
                    dis_sum_amt += jsonObject.getDouble("discount_amt");
                }
                if (mBasketView != null)mBasketView.update(sale_sum_num);
                mSaleGoodsRecyclerView.scrollToPosition(mSaleGoodsAdapter.getCurrentItemIndex());
            }
        });
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(mSaleGoodsRecyclerView,getResources().getDimension(R.dimen.sale_goods_height),new SaleGoodsItemDecoration(getColor(R.color.gray_subtransparent)));
        mSaleGoodsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        mSaleGoodsRecyclerView.setAdapter(mSaleGoodsAdapter);
    }

    @Override
    public void loadGoods(final String id){
        if (mGoodsInfoViewAdapter != null)mGoodsInfoViewAdapter.loadGoodsByCategoryId(id);
    }
    @Override
    public void addSaleGoods(final @NonNull JSONObject jsonObject){
        final JSONObject content = new JSONObject();
        final String id = mGoodsInfoViewAdapter.getGoodsId(jsonObject);
        if (mGoodsInfoViewAdapter.getSingleGoods(content,null,id)){
            mSaleGoodsAdapter.addSaleGoods(content);
        }else{
            MyDialog.ToastMessage("选择商品错误：" + content.getString("info"),this,null);
        }
    }
}