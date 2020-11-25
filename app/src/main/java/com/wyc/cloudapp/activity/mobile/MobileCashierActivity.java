package com.wyc.cloudapp.activity.mobile;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.CustomizationView.BasketView;
import com.wyc.cloudapp.CustomizationView.InterceptLinearLayout;
import com.wyc.cloudapp.CustomizationView.TmpOrderButton;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.SaleActivity;
import com.wyc.cloudapp.adapter.GoodsCategoryAdapter;
import com.wyc.cloudapp.adapter.GoodsInfoItemDecoration;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.adapter.SaleGoodsItemDecoration;
import com.wyc.cloudapp.adapter.SuperItemDecoration;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.goods.AddGoodsInfoDialog;
import com.wyc.cloudapp.dialog.orderDialog.HangBillDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.dialog.pay.PayDialog;
import com.wyc.cloudapp.dialog.vip.VipInfoDialog;
import com.wyc.cloudapp.utils.Utils;

import java.util.Locale;

public class MobileCashierActivity extends SaleActivity implements View.OnClickListener {
    public static final int PAY_REQUEST_CODE = 0x000000aa;
    private static final int CODE_REQUEST_CODE = 0x000000bb;
    private BasketView mBasketView;
    private EditText mSearchContent,mMobileSearchGoods;
    private GoodsInfoViewAdapter mGoodsInfoViewAdapter;
    private String mOrderCode = "";
    private ScanCallback mScanCallback;
    private TextView mSaleSumAmtTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_cashier);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) ;//显示状态栏

        mSaleSumAmtTv = findViewById(R.id.mobile_sale_amt);

        initGoodsInfoAdapter();
        initGoodsCategoryAdapter();
        initSaleGoodsAdapter();

        initTitle();
        initBasketView();
        initSearchContent();
        initCheckout();
        initVipBtn();

        //重置订单信息
        resetOrderInfo();

        initOtherFunction();
    }

    private void initOtherFunction(){
        final Button mobile_other_fun_btn = findViewById(R.id.mobile_other_fun_btn);
        final InterceptLinearLayout mobile_other_fun_hide_layout = findViewById(R.id.mobile_other_fun_hide_layout);
        final TmpOrderButton tmp_order = mobile_other_fun_hide_layout.findViewById(R.id.mobile_hang_orderl_btn);
        mobile_other_fun_btn.setOnClickListener(v -> {
            boolean isHide = mobile_other_fun_hide_layout.getVisibility() == View.GONE;
            if (isHide){
                tmp_order.setNum(HangBillDialog.getHangCounts(this));
                mobile_other_fun_hide_layout.setClickListener(this);
                mobile_other_fun_hide_layout.setVisibility(View.VISIBLE);
            }else {
                mobile_other_fun_hide_layout.setClickListener(null);
                mobile_other_fun_hide_layout.setVisibility(View.GONE);
            }
        });

        //是否自动取单
        final Intent intent = getIntent();
        boolean disposeHang = intent.getBooleanExtra("disposeHang",false);
        if (disposeHang){
            mobile_other_fun_btn.callOnClick();
            tmp_order.callOnClick();
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.mobile_hang_orderl_btn){
            hangOrder(v);
        }else if(id == R.id.mobile_clear_btn){
            clear();
        }else if (id == R.id.mobile_present_btn){
            present();
        }else if (id == R.id.mobile_refund_btn){
            setSingle(true);
        }
    }
    private void hangOrder(final View btn){
        final TmpOrderButton tmp_order = (TmpOrderButton)btn;
        final JSONArray datas = mSaleGoodsAdapter.getDatas();
        final HangBillDialog hangBillDialog = new HangBillDialog(this);
        if (Utils.JsonIsNotEmpty(datas)){
            final StringBuilder err = new StringBuilder();
            if (hangBillDialog.save(datas,mVipInfo,err)){
                tmp_order.setNum(HangBillDialog.getHangCounts(this));
                resetOrderInfo();
                MyDialog.ToastMessage("挂单成功！",this,null);
            }else{
                MyDialog.ToastMessage("保存挂单错误：" + err,this,null);
            }
        }else{
            if (HangBillDialog.getHangCounts(this) > 0){
                hangBillDialog.setGetBillDetailListener((array, vip) -> {
                    if (null != vip)showVipInfo(vip);
                    JSONObject barcode_id_obj,goods_info;
                    for (int i = 0,length = array.size();i < length;i ++){
                        barcode_id_obj = array.getJSONObject(i);
                        if (barcode_id_obj != null){
                            goods_info = new JSONObject();
                            if (mGoodsInfoViewAdapter.getSingleGoods(goods_info,barcode_id_obj.getString(GoodsInfoViewAdapter.W_G_MARK),mGoodsInfoViewAdapter.getGoodsId(barcode_id_obj))){
                                goods_info.put("xnum",barcode_id_obj.getDoubleValue("xnum"));//挂单取出重量
                                mSaleGoodsAdapter.addSaleGoods(goods_info);
                                hangBillDialog.dismiss();
                            }else{
                                MyDialog.ToastMessage("选择商品错误：" + goods_info.getString("info"),this,null);
                                return;
                            }
                        }
                    }
                });
                hangBillDialog.setOnDismissListener(dialog -> {
                    tmp_order.setNum(HangBillDialog.getHangCounts(MobileCashierActivity.this));
                    mSearchContent.clearFocus();
                });
                hangBillDialog.show();
            }else{
                MyDialog.ToastMessage("无挂单信息！",this,null);
            }
        }
    }

    private void initVipBtn(){
        final Button btn = findViewById(R.id.mobile_vip_btn);
        if (btn != null)
            btn.setOnClickListener(v -> {
            final VipInfoDialog vipInfoDialog = new VipInfoDialog(this);
            if (mVipInfo != null){
                if (1 == MyDialog.showMessageToModalDialog(this,"已存在会员信息,是否清除？")){
                    clearVipInfo();
                }
            }else
                vipInfoDialog.setYesOnclickListener(dialog -> {showVipInfo(dialog.getVip());dialog.dismiss(); }).show();
        });
    }

    private void initCheckout(){
        final Button btn = findViewById(R.id.mobile_checkout_btn);
        btn.setOnClickListener(v -> {
            Utils.disableView(v,500);
            if (!mSaleGoodsAdapter.isEmpty()){
                if (!getSingle()){
                    final PayDialog dialog = new PayDialog(this,getString(R.string.affirm_pay_sz));
                    dialog.initPayContent();
                    if (mVipInfo != null)dialog.setVipInfo(mVipInfo,true);
                    if (dialog.exec() == 1){
                        mApplication.sync_retail_order();
                        resetOrderInfo();
                        MyDialog.ToastMessage("结账成功!",this,null);
                    }
                }else {
                    final RefundDialog refundDialog = new RefundDialog(this,"");
                    refundDialog.show();
                }
            }else{
                MyDialog.ToastMessage("已选商品为空!",this,null);
            }
        });
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
        final GoodsCategoryAdapter mGoodsCategoryAdapter = new GoodsCategoryAdapter(this, null);
        goods_type_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        mGoodsCategoryAdapter.setDatas(0);
        goods_type_view.setAdapter(mGoodsCategoryAdapter);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText search = findViewById(R.id.mobile_search_content);
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
        search.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_UP){
                selectGoodsWithSearchContent(search.getText().toString());
                return true;
            }
            return false;
        });
        search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = mSearchContent.getWidth();
                if (dx > (w - mSearchContent.getCompoundPaddingRight())) {
                    mSearchContent.requestFocus();
                    final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    startActivityForResult(intent, CODE_REQUEST_CODE);
                }else if(dx < mSearchContent.getCompoundPaddingLeft()){
                    switchView();
                }
            }
            return false;
        });
        mSearchContent = search;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void switchView(){
        final ViewGroup sale_info_layout = findViewById(R.id.sale_info_layout),
                goods_info_layout = findViewById(R.id.goods_info_layout);
        if (sale_info_layout.getVisibility() == View.GONE){
            sale_info_layout.setVisibility(View.VISIBLE);
            goods_info_layout.setVisibility(View.GONE);
            if (mMobileSearchGoods != null)mMobileSearchGoods.setVisibility(View.GONE);
            mSearchContent.requestFocus();
        }else {
            sale_info_layout.setVisibility(View.GONE);
            goods_info_layout.setVisibility(View.VISIBLE);

            if (mMobileSearchGoods == null){
                final EditText mobile_search_goods = goods_info_layout.findViewById(R.id.mobile_search_goods);
                mobile_search_goods.setVisibility(View.VISIBLE);
                mobile_search_goods.setOnTouchListener((v, motionEvent) -> {
                    if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                        final float dx = motionEvent.getX();
                        final int w = mobile_search_goods.getWidth();
                        if (dx > (w - mobile_search_goods.getCompoundPaddingRight())) {
                            selectGoodsWithMobileSearchGoods();
                            return true;
                        }else if(dx < mSearchContent.getCompoundPaddingLeft()){
                            switchView();
                            return true;
                        }
                    }
                    return false;
                });
                mMobileSearchGoods = mobile_search_goods;
            }else mMobileSearchGoods.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){//条码回调
        if (resultCode == RESULT_OK ){
            final String _code = intent.getStringExtra("auth_code");
            if (requestCode == CODE_REQUEST_CODE) {
                if (mSearchContent != null)selectGoodsWithSearchContent(_code);
            }else if (requestCode == PAY_REQUEST_CODE){
                if (mScanCallback != null)mScanCallback.callback(_code);
            }
        }
        super.onActivityResult(requestCode,resultCode,intent);
    }

    private void selectGoodsWithSearchContent(final String content){
        if (null != content && content.length() != 0){
            final EditText search = mSearchContent;
            search.setText(content);
            search.selectAll();
            if (!mGoodsInfoViewAdapter.fuzzy_search_goods(content,true)) {
                search.post(()->{
                    if (mApplication.isConnection() && AddGoodsInfoDialog.verifyGoodsAddPermissions(this)) {
                        if (1 == MyDialog.showMessageToModalDialog(this,"未找到匹配商品，是否新增?")){
                            final AddGoodsInfoDialog addGoodsInfoDialog = new AddGoodsInfoDialog(this);
                            addGoodsInfoDialog.setBarcode(search.getText().toString());
                            addGoodsInfoDialog.setFinishListener(barcode -> {
                                mGoodsInfoViewAdapter.fuzzy_search_goods(content,true);
                                addGoodsInfoDialog.dismiss();
                            });
                            addGoodsInfoDialog.show();
                        }
                    } else
                        MyDialog.ToastMessage("无此商品!", this, getWindow());
                });
            } else if (mGoodsInfoViewAdapter.getItemCount() > 1){
                if (!mGoodsInfoViewAdapter.isAutoSelect()){
                    switchView();
                    mMobileSearchGoods.setText(content);
                    mMobileSearchGoods.requestFocus();
                }
            }
        }
    }

    private void selectGoodsWithMobileSearchGoods(){
        final EditText search = mMobileSearchGoods;
        final String content = search.getText().toString();
        if (content.length() != 0){
            search.selectAll();
            if (!mGoodsInfoViewAdapter.fuzzy_search_goods(content,true)) {
                search.post(()->{
                    if (mApplication.isConnection() && AddGoodsInfoDialog.verifyGoodsAddPermissions(this)) {
                        if (1 == MyDialog.showMessageToModalDialog(this,"未找到匹配商品，是否新增?")){
                            final AddGoodsInfoDialog addGoodsInfoDialog = new AddGoodsInfoDialog(this);
                            addGoodsInfoDialog.setBarcode(search.getText().toString());
                            addGoodsInfoDialog.setFinishListener(barcode -> {
                                mGoodsInfoViewAdapter.fuzzy_search_goods(content,true);
                                addGoodsInfoDialog.dismiss();
                            });
                            addGoodsInfoDialog.show();
                        }
                    } else
                        MyDialog.ToastMessage("无此商品!", this, getWindow());
                });
            }
        }
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
        right.setVisibility(View.INVISIBLE);
    }
    private void clear(){
        if (!mSaleGoodsAdapter.isEmpty()){
            clearSaleGoods();
        }else {
            if (getSingle())resetOrderInfo();
        }
    }

    @Override
    public void onBackPressed(){
        final ViewGroup goods_info_layout = findViewById(R.id.goods_info_layout);
        if (goods_info_layout.getVisibility() == View.VISIBLE){
            switchView();
        }else{
            if (mSaleGoodsAdapter.isEmpty()){
                super.onBackPressed();
            }else{
                MyDialog.ToastMessage("已存在销售商品!",this,null);
            }
        }
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
                mSaleSumAmtTv.setText(String.format(Locale.CANADA,"%.2f",sale_sum_amount));
                if (mBasketView != null)mBasketView.update(sale_sum_num);
                mSaleGoodsRecyclerView.scrollToPosition(mSaleGoodsAdapter.getCurrentItemIndex());
            }
        });
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(mSaleGoodsRecyclerView,getResources().getDimension(R.dimen.sale_goods_height),new SaleGoodsItemDecoration(getColor(R.color.gray_subtransparent)));
        mSaleGoodsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        mSaleGoodsRecyclerView.setAdapter(mSaleGoodsAdapter);
    }

    @Override
    public boolean containGoods(){
        return mGoodsInfoViewAdapter != null && mGoodsInfoViewAdapter.getItemCount() != 0;
    }

    @Override
    public void loadGoods(final String id){
        mGoodsInfoViewAdapter.loadGoodsByCategoryId(id);
    }
    @Override
    public void addSaleGoods(final @NonNull JSONObject jsonObject){
        final JSONObject content = new JSONObject();
        final String id = mGoodsInfoViewAdapter.getGoodsId(jsonObject);
        if (mGoodsInfoViewAdapter.getSingleGoods(content,null,id)){
            if (mMobileSearchGoods != null && mMobileSearchGoods.getVisibility() == View.VISIBLE){
                mMobileSearchGoods.getText().clear();
                switchView();
            }
            mSaleGoodsAdapter.addSaleGoods(content);
        }else{
            MyDialog.ToastMessage("选择商品错误：" + content.getString("info"),this,null);
        }
    }
    @Override
    public void resetOrderCode(){
        mOrderCode = mSaleGoodsAdapter.generateOrderCode(mCashierInfo.getString("pos_num"),1);
    }
    @Override
    public String getOrderCode(){ return mOrderCode;}

    @Override
    public void showVipInfo(final JSONObject vip){
        super.showVipInfo(vip);
        final ConstraintLayout mobile_bottom_btn_layout = findViewById(R.id.mobile_bottom_btn_layout);
        if ( vip != null && mobile_bottom_btn_layout != null){
            final TextView vip_name_tv = mobile_bottom_btn_layout.findViewById(R.id.vip_name);
            if (null != vip_name_tv){
                vip_name_tv.setText(vip.getString("name"));
            }
            mSaleGoodsAdapter.updateGoodsInfoToVip(vip);
        }
    }
    @Override
    public void clearVipInfo(){
        super.clearVipInfo();
        final ConstraintLayout mobile_bottom_btn_layout = findViewById(R.id.mobile_bottom_btn_layout);
        final TextView vip_name_tv = mobile_bottom_btn_layout.findViewById(R.id.vip_name);
        if (null != vip_name_tv){
            vip_name_tv.setText(getText(R.string.space_sz));
        }
        if (!mSaleGoodsAdapter.getDatas().isEmpty()){
            mSaleGoodsAdapter.deleteVipDiscountRecord();
        }
    }

    @Override
    public void clearSearchEt(){
        if (mSearchContent != null)mSearchContent.getText().clear();
    }

    @Override
    public void setScanCallback(final ScanCallback callback){
        mScanCallback = callback;
    }
    @Override
    public void clearScanCallback(){
        if (mScanCallback != null)mScanCallback = null;
    }
}