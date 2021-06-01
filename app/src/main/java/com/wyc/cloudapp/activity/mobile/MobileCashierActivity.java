package com.wyc.cloudapp.activity.mobile;

import android.annotation.SuppressLint;
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

import androidx.annotation.CallSuper;
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
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.decoration.GoodsInfoItemDecoration;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.decoration.SaleGoodsItemDecoration;
import com.wyc.cloudapp.decoration.SuperItemDecoration;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.goods.AddGoodsInfoDialog;
import com.wyc.cloudapp.dialog.orderDialog.HangBillDialog;
import com.wyc.cloudapp.dialog.orderDialog.MobileQueryRetailOrderDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.dialog.pay.MobileSettlementDialog;
import com.wyc.cloudapp.dialog.pay.AbstractSettlementDialog;
import com.wyc.cloudapp.dialog.vip.AbstractVipChargeDialog;
import com.wyc.cloudapp.dialog.vip.VipInfoDialog;
import com.wyc.cloudapp.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.Locale;

import static com.wyc.cloudapp.constants.ScanCallbackCode.PAY_REQUEST_CODE;
import static com.wyc.cloudapp.constants.ScanCallbackCode.CODE_REQUEST_CODE;

public class MobileCashierActivity extends SaleActivity implements View.OnClickListener {
    private BasketView mBasketView;
    private EditText mSearchContent,mMobileSearchGoods;
    private GoodsInfoViewAdapter mGoodsInfoViewAdapter;
    private String mOrderCode = "";
    private WeakReference<ScanCallback> mScanCallback;
    private TextView mSaleSumAmtTv;
    private boolean isScan,isSelected;//协助完成界面切换，当isScan为true，isSelected为false的时候需要切换。
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_cashier);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN) ;//显示状态栏

        mSaleSumAmtTv = findViewById(R.id.mobile_sale_amt);

        initGoodsInfoAdapter();
        initGoodsCategoryAdapter();
        initSaleGoodsAdapter();

        initBasketView();
        initSearchContent();
        initCheckout();
        initVipBtn();
        initSaleBtn();

        //重置订单信息
        resetOrderInfo();

        initOtherFunction();

        initTitle();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mSearchContent.isShown())mSearchContent.clearFocus();
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
            setAllRefundStatusView(null,true);
        }
    }
    private void hangOrder(final View btn){
        final TmpOrderButton tmp_order = (TmpOrderButton)btn;
        final JSONArray datas = mSaleGoodsAdapter.getData();
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

    private void initSaleBtn(){
        final Button btn = findViewById(R.id.mobile_sale_man_btn);
        if (btn != null)
            btn.setOnClickListener(v -> {
                final TextView sale_man_name = findViewById(R.id.sale_man_name);
                final JSONObject object = AbstractVipChargeDialog.showSaleInfo(this);

                final String name = Utils.getNullStringAsEmpty(object,"item_name");

                mSaleManInfo = new JSONObject();
                mSaleManInfo.put("id",Utils.getNullStringAsEmpty(object,"item_id"));
                mSaleManInfo.put("name",name);
                sale_man_name.setText(name);
            });
    }

    private void initCheckout(){
        final Button btn = findViewById(R.id.mobile_checkout_btn);
        btn.setOnClickListener(v -> {
            Utils.disableView(v,500);
            if (!mSaleGoodsAdapter.isEmpty()){
                if (!getSingleRefundStatus()){
                    final AbstractSettlementDialog dialog = new MobileSettlementDialog(this,getString(R.string.affirm_pay_sz));
                    if (dialog.initPayContent()){
                        if (mVipInfo != null)dialog.setVipInfo(mVipInfo,true);
                        if (dialog.exec() == 1){
                            mApplication.sync_retail_order();
                            resetOrderInfo();
                            MyDialog.ToastMessage("结账成功!",this,null);
                        }
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
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(goods_info_view,getResources().getDimension(R.dimen.goods_height),new GoodsInfoItemDecoration());
        mGoodsInfoViewAdapter.setOnGoodsSelectListener(object -> {
            if (mMobileSearchGoods != null && mMobileSearchGoods.getVisibility() == View.VISIBLE){
                mMobileSearchGoods.getText().clear();
                switchView();
            }
            if (isScan)isSelected = true;
            addSaleGoods(object);
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
                final int w = search.getWidth();
                if (dx > (w - search.getCompoundPaddingRight())) {
                    search.requestFocus();
                    final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                    startActivityForResult(intent, CODE_REQUEST_CODE);
                }else if(dx < search.getCompoundPaddingLeft()){
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

            mMobileSearchGoods.requestFocus();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent){//条码回调
        if (resultCode == RESULT_OK ){
            final String _code = intent.getStringExtra("auth_code");
            if (requestCode == CODE_REQUEST_CODE) {
                if (mSearchContent != null)selectGoodsWithSearchContent(_code);
            }else if (requestCode == PAY_REQUEST_CODE){
                if (mScanCallback != null){
                    final ScanCallback callback = mScanCallback.get();
                    if (callback != null)callback.callback(_code);
                }
            }
        }
        super.onActivityResult(requestCode,resultCode,intent);
    }

    private void selectGoodsWithSearchContent(final String content){
        if (null != content && content.length() != 0){
            isScan = true;

            final EditText search = mSearchContent;
            search.setText(content);
            search.selectAll();
            if (_search_goods(content)){
                if (isScan && !isSelected){
                    isScan = false;
                    switchView();
                }else isSelected = false;
            }
        }
    }

    private void selectGoodsWithMobileSearchGoods(){
        final EditText search = mMobileSearchGoods;
        final String content = search.getText().toString();
        if (content.length() != 0){
            search.selectAll();
            _search_goods(content);
        }
    }

    private boolean _search_goods(final String content){
        if (!mGoodsInfoViewAdapter.fuzzy_search_goods(content,true)) {
            CustomApplication.runInMainThread(()->{
                if (mApplication.isConnection() && AddGoodsInfoDialog.verifyGoodsAddPermissions(this)) {
                    if (1 == MyDialog.showMessageToModalDialog(this,"未找到匹配商品，是否新增?")){
                        final AddGoodsInfoDialog addGoodsInfoDialog = new AddGoodsInfoDialog(this);
                        addGoodsInfoDialog.setBarcode(content);
                        addGoodsInfoDialog.setFinishListener(barcode -> {
                            mGoodsInfoViewAdapter.fuzzy_search_goods(content,true);
                            addGoodsInfoDialog.dismiss();
                        });
                        addGoodsInfoDialog.show();
                    }
                } else
                    MyDialog.ToastMessage("无此商品!", this, getWindow());
            });
            return false;
        }
        return true;
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

        boolean singleRefundStatus = intent.getBooleanExtra("singleRefundStatus",false);
        if (singleRefundStatus){
            setAllRefundStatusView(right,true);
        }
    }

    public void setAllRefundStatusView(TextView view,boolean b){
        setSingleRefundStatus(b);
        if (null == view)view = findViewById(R.id.right_title_tv);
        b = mSaleGoodsAdapter.getSingleRefundStatus();
        if (b){
            view.setVisibility(View.VISIBLE);
            view.setText(R.string.all_refund_sz);
            view.setOnClickListener(v -> {
                final MobileQueryRetailOrderDialog dialog = new MobileQueryRetailOrderDialog(this);
                dialog.show();
            });
        }else {
            view.setText(R.string.space_sz);
            view.setOnClickListener(null);
        }
    }

    private void clear(){
        if (!mSaleGoodsAdapter.isEmpty()){
            clearSaleGoods();
        }else {
            if (getSingleRefundStatus())resetOrderInfo();
        }
    }

    @Override
    public void resetOrderInfo(){
        super.resetOrderInfo();
        setAllRefundStatusView(null,false);
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
        mSaleGoodsAdapter.setDataListener((total_num, total_sale_amt, total_discount_amt) -> {
            mSaleSumAmtTv.setText(String.format(Locale.CANADA,"%.2f",total_sale_amt));
            if (mBasketView != null)mBasketView.update(total_num);
            mSaleGoodsRecyclerView.scrollToPosition(mSaleGoodsAdapter.getCurrentItemIndex());
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
    protected void addSaleGoods(final @NonNull JSONObject jsonObject){
        final JSONObject content = new JSONObject();
        if (mGoodsInfoViewAdapter.getSingleGoods(content,null,mGoodsInfoViewAdapter.getGoodsId(jsonObject))){
            mSaleGoodsAdapter.addSaleGoods(content);
        }else{
            MyDialog.ToastMessage("选择商品错误：" + content.getString("info"),this,null);
        }
    }
    @Override
    public void resetOrderCode(){
        mOrderCode = mSaleGoodsAdapter.generateOrderCode(getPosNum(),1);
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

    @CallSuper
    public void clearSaleManInfo(){
        if (mSaleManInfo != null){
            mSaleManInfo = null;
            final TextView sale_man_name = findViewById(R.id.sale_man_name);
            if (null != sale_man_name){
                sale_man_name.setText(getText(R.string.space_sz));
            }
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
        mSaleGoodsAdapter.deleteVipDiscountRecord();
    }

    @Override
    public void clearSearchEt(){
        if (mSearchContent != null)mSearchContent.getText().clear();
    }

    @Override
    public void setScanCallback(final ScanCallback callback){
        if (mScanCallback == null || callback != mScanCallback.get()){
            mScanCallback = new WeakReference<>(callback);
        }
    }
    @Override
    public boolean findGoodsByBarcodeId(@NonNull final JSONObject out_goods,final String barcode_id){
        return mGoodsInfoViewAdapter.getSingleGoods(out_goods,null,barcode_id);
    }
}