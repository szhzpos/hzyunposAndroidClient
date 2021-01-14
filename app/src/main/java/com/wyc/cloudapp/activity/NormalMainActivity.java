package com.wyc.cloudapp.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.CustomizationView.InterceptLinearLayout;
import com.wyc.cloudapp.CustomizationView.ScaleView;
import com.wyc.cloudapp.CustomizationView.TmpOrderButton;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.GoodsCategoryAdapter;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.decoration.GoodsInfoItemDecoration;
import com.wyc.cloudapp.decoration.SaleGoodsItemDecoration;
import com.wyc.cloudapp.decoration.SuperItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MoreFunDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.SecondDisplay;
import com.wyc.cloudapp.dialog.goods.AddGoodsInfoDialog;
import com.wyc.cloudapp.dialog.orderDialog.AbstractTransferDialog;
import com.wyc.cloudapp.dialog.orderDialog.HangBillDialog;
import com.wyc.cloudapp.dialog.orderDialog.NormalTransferDialog;
import com.wyc.cloudapp.dialog.orderDialog.QueryRetailOrderDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.dialog.pay.NormalSettlementDialog;
import com.wyc.cloudapp.dialog.pay.AbstractSettlementDialog;
import com.wyc.cloudapp.dialog.vip.AbstractVipChargeDialog;
import com.wyc.cloudapp.dialog.vip.VipInfoDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.PrintUtilsToBitbmp;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.text.SimpleDateFormat;
import java.util.Locale;

public final class NormalMainActivity extends SaleActivity implements CustomApplication.MessageCallback {
    private RecyclerView mSaleGoodsRecyclerView;
    private GoodsCategoryAdapter mGoodsCategoryAdapter;
    private GoodsInfoViewAdapter mGoodsInfoViewAdapter;
    private long mCurrentTimestamp = 0;
    private TextView mCurrentTimeViewTv, mSaleSumNumTv, mSaleSumAmtTv, mOrderCodeTv, mDisSumAmtTv;
    private ImageView mCloseBtn;
    private TableLayout mKeyboard;
    private SecondDisplay mSecondDisplay;
    private ConstraintLayout mLastOrderInfo;
    private ImageView mPrinterStatusIv;
    private ScaleView mScaleView;
    private CustomProgressDialog mProgressDialog;
    private EditText mSearch_content;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.normal_activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initMemberVariable();

        initNetworkStatus();
        initLastOrderInfo();

        //初始化adapter
        initGoodsInfoAdapter();
        initGoodsCategoryAdapter();
        initSaleGoodsAdapter();

        showCashierInfoAndStoreInfo();
        //更新当前时间
        startSyncCurrentTime();
        //初始化搜索框
        initSearch();
        //挂单
        initTmpOrder();
        //打印状态
        initPrintStatus();
        //关闭收银主窗口
        initCloseMainWindow();
        //清除按钮
        initClearBtn();
        //初始化功能按钮
        initFunctionBtn();

        //初始化交班
        initTransferBtn();

        initMoreFunBtn();

        //重置订单信息
        resetOrderInfo();

        //初始化副屏
        initSecondDisplay();

        initScaleView();

        //初始化数据管理
        initSyncManagement();
    }
    private void initMemberVariable(){
        mProgressDialog = new CustomProgressDialog(this);
        mCurrentTimeViewTv = findViewById(R.id.current_time);
        mSaleSumNumTv = findViewById(R.id.sale_sum_num);
        mSaleSumAmtTv = findViewById(R.id.sale_sum_amt);
        mKeyboard = findViewById(R.id.keyboard_layout);
        mOrderCodeTv = findViewById(R.id.order_code);
        mDisSumAmtTv = findViewById(R.id.dis_sum_amt);
    }
    @Override
    public void onResume(){
        super.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onBackPressed(){
        if (null != mCloseBtn)mCloseBtn.callOnClick();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        clearResource();
    }

    private void initSyncManagement(){
        mApplication.registerHandleMessage(this);
        mApplication.sync_order_info();
        mApplication.start_sync(false);
    }

    private void showCashierInfoAndStoreInfo(){
        final TextView cashier_name = findViewById(R.id.cashier_name),
                store_name = findViewById(R.id.store_name),
                pos_num = findViewById(R.id.pos_num);
        cashier_name.setText(mCashierInfo.getString("cas_name"));
        pos_num.setText(mCashierInfo.getString("pos_num"));
        store_name.setText(String.format("%s%s%s%s",mStoreInfo.getString("stores_name"),"[",mStoreInfo.getString("stores_id"),"]"));
    }
    private void initScaleView(){
        mScaleView = findViewById(R.id.scaleView);
    }
    private void initLastOrderInfo(){
        final ConstraintLayout constraintLayout =  findViewById(R.id.last_order_info_c_layout);
        if (constraintLayout != null){
            final TextView close_tv = constraintLayout.findViewById(R.id.order_info_close_tv);
            close_tv.setOnClickListener(v -> constraintLayout.setVisibility(View.GONE));
            mLastOrderInfo = constraintLayout;
        }
    }
    private void hideLastOrderInfo(){
        final ConstraintLayout constraintLayout = mLastOrderInfo;
        if (constraintLayout != null && constraintLayout.getVisibility() == View.VISIBLE){
            constraintLayout.setVisibility(View.GONE);
        }
    }
    private void showLastOrderInfo(){
        final ConstraintLayout constraintLayout = mLastOrderInfo;
        if (constraintLayout != null){
            constraintLayout.setVisibility(View.VISIBLE);
            final JSONObject order_info = new JSONObject();
            if (SQLiteHelper.execSql(order_info,"SELECT order_code,sum(pre_sale_money) pre_amt,sum(pay_money) pay_amt,sum(pre_sale_money - pay_money) zl_amt FROM " +
                    "retail_order_pays where order_code = '" + mOrderCodeTv.getText() +"' group by order_code")){

                final TextView last_order_code = constraintLayout.findViewById(R.id.last_order_code),last_reality_amt = constraintLayout.findViewById(R.id.last_reality_amt),
                        last_rec_amt = constraintLayout.findViewById(R.id.last_rec_amt),last_zl = constraintLayout.findViewById(R.id.last_zl),close_tv = constraintLayout.findViewById(R.id.order_info_close_tv);

                final Button last_reprint_btn = constraintLayout.findViewById(R.id.last_reprint_btn);

                last_order_code.setText(order_info.getString("order_code"));
                last_rec_amt.setText(String.format(Locale.CHINA,"%.2f",order_info.getDoubleValue("pay_amt")));
                last_reality_amt.setText(String.format(Locale.CHINA,"%.2f",order_info.getDoubleValue("pre_amt")));
                last_zl.setText(String.format(Locale.CHINA,"%.2f",order_info.getDoubleValue("zl_amt")));
                close_tv.setOnClickListener(v -> {
                    constraintLayout.setVisibility(View.GONE);
                    close_tv.setOnClickListener(null);
                    last_reprint_btn.setOnClickListener(null);
                });
                last_reprint_btn.setOnClickListener(v -> Printer.print(this, AbstractSettlementDialog.get_print_content(this,last_order_code.getText().toString(),false)));
            }else {
                MyDialog.ToastMessage(order_info.getString("info"),this,getWindow());
            }
        }
    }
    private void initNetworkStatus(){
        final ImageView imageView = findViewById(R.id.network_status);
        if (imageView != null && !mApplication.isConnection()){
            imageView.setImageResource(R.drawable.network_err);
            imageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_x));
        }
    }

    private void initFunctionBtn(){
        final Button minus_num_btn = findViewById(R.id.minus_num),add_num_btn = findViewById(R.id.add_num),num_btn = findViewById(R.id.num),
                discount_btn = findViewById(R.id.discount),change_price_btn = findViewById(R.id.change_price),check_out_btn = findViewById(R.id.check_out),
                vip_btn = findViewById(R.id.vip);

        if (minus_num_btn != null)minus_num_btn.setOnClickListener(v -> minusOneGoods());//数量减
        if (add_num_btn != null)add_num_btn.setOnClickListener(v -> addOneSaleGoods());//数量加
        if (num_btn != null)num_btn.setOnClickListener(view -> alterGoodsNumber());//数量
        if (discount_btn != null)discount_btn.setOnClickListener(v-> discount());//打折
        if (change_price_btn != null)change_price_btn.setOnClickListener(v-> alterGoodsPrice());//改价
        if (check_out_btn != null)check_out_btn.setOnClickListener((View v)->{
            Utils.disableView(v,500);showPayDialog();});//结账
        if (vip_btn != null)vip_btn.setOnClickListener(v -> {
            final VipInfoDialog vipInfoDialog = new VipInfoDialog(this);
            if (mVipInfo != null){
                if (1 == MyDialog.showMessageToModalDialog(this,"已存在会员信息,是否清除？")){
                    clearVipInfo();
                }
            }else
                vipInfoDialog.setYesOnclickListener(dialog -> {showVipInfo(dialog.getVip());dialog.dismiss(); }).show();
        });//会员

        final LinearLayout q_deal_linerLayout = findViewById(R.id.q_deal_linerLayout),other_linearLayout = findViewById(R.id.other_linearLayout),cloud_background_layout = findViewById(R.id.cloud_background_layout);
        if (q_deal_linerLayout != null)q_deal_linerLayout.setOnClickListener(v -> {
            if (verifyQueryBtnPermissions()){
                final QueryRetailOrderDialog queryRetailOrderDialog = new QueryRetailOrderDialog(this);
                queryRetailOrderDialog.show();
                queryRetailOrderDialog.triggerQuery();
            }
        });//查交易
        if (other_linearLayout != null)other_linearLayout.setOnClickListener(v -> new MoreFunDialog(this,getString(R.string.more_fun_dialog_sz)).show());//更多功能
        if (cloud_background_layout != null)cloud_background_layout.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getUrl())));
            }catch (ActivityNotFoundException e){
                MyDialog.ToastMessage("系统未安装浏览器!",this,getWindow());
            }
        });
    }

    private void initMoreFunBtn(){
        final Button btn = findViewById(R.id.more_fun_btn);
        btn.setOnClickListener(v -> {

            final PopupWindow window = new PopupWindow(this);
            window.setContentView(View.inflate(this,R.layout.more_fun_popup_window_layout,null));
            window.setOutsideTouchable(true);
            window.setBackgroundDrawable(getDrawable(R.color.transparent));

            btn.post(()->{
                final View contentView = window.getContentView();
                contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

                final int content_height = contentView.getMeasuredHeight();
                int width = btn.getWidth(),height = btn.getHeight();
                int[] ints = new int[2];
                btn.getLocationInWindow(ints);
                window.showAsDropDown(btn,width, -content_height );

                final InterceptLinearLayout interceptLinearLayout = contentView.findViewById(R.id.interceptLinearLayout);
                if (interceptLinearLayout != null){
                    interceptLinearLayout.setClickListener(view -> {
                        int id = view.getId();
                        if (id == R.id.pop_present_btn){
                            if (present())window.dismiss();
                        }else if (id == R.id.pop_o_cashbox){
                            if (verifyOpenCashboxPermissions()){
                                Printer.print(this, Printer.commandToStr(Printer.OPEN_CASHBOX));
                                window.dismiss();
                            }
                        }else if (id == R.id.pop_sale_man_btn){
                            final JSONObject object = AbstractVipChargeDialog.showSaleInfo(this);
                            final String name = Utils.getNullStringAsEmpty(object,"item_name");
                            mSaleManInfo = new JSONObject();
                            mSaleManInfo.put("id",Utils.getNullStringAsEmpty(object,"item_id"));
                            mSaleManInfo.put("name",name);
                            setSaleManView(name);
                            window.dismiss();
                        }else if (id == R.id.pop_refund_btn){
                            if (RefundDialog.verifyRefundPermission(this)){
                                setSingleRefundStatus(true);
                                window.dismiss();
                            }
                        }
                    });

                }
            });

        });
    }

    @CallSuper
    public void clearSaleManInfo(){
        if (mSaleManInfo != null){
            mSaleManInfo = null;
            setSaleManView(getText(R.string.space_sz).toString());
        }
    }
    private void setSaleManView(final String s){
        final TextView sale_man_name = findViewById(R.id.sale_man_tv);
        if (null != sale_man_name){
            sale_man_name.setText(s);
        }
    }

    private void initTransferBtn(){
        final LinearLayout shift_exchange_linearLayout = findViewById(R.id.shift_exchange_linearLayout);
        if (shift_exchange_linearLayout != null)
            shift_exchange_linearLayout.setOnClickListener(v -> {
                final NormalMainActivity activity = NormalMainActivity.this;
                if (AbstractTransferDialog.verifyTransferPermissions(activity)){
                    final AbstractTransferDialog transferDialog = new NormalTransferDialog(activity);
                    transferDialog.verifyTransfer();
                }
            });
    }
    private void initClearBtn(){
        final Button clearBtn = findViewById(R.id.clear);
        if (null != clearBtn){
            clearBtn.setOnClickListener(v -> {
                if (!mSaleGoodsAdapter.isEmpty()){
                    clearSaleGoods();
                }else {
                    if (getSingleRefundStatus())resetOrderInfo();
                }
            });
        }
    }
    private void initCloseMainWindow(){
        mCloseBtn = findViewById(R.id.close);
        mCloseBtn.setOnClickListener((View V)->{
            MyDialog.displayAskMessage(this, "是否退出收银？", (MyDialog myDialog)->{
                myDialog.dismiss();
                /*Intent intent = new Intent(this,LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);*/
                this.finish();
            }, Dialog::dismiss);
        });
    }

    private void clearResource(){
        if (mSecondDisplay != null)mSecondDisplay.dismiss();
    }

    private void startSyncCurrentTime(){
        final View view = getWindow().getDecorView();
        if (mCurrentTimestamp == 0){
            if (mApplication.isConnection()){
                CustomApplication.execute(()->{
                    long cur = System.currentTimeMillis();
                    try {
                        HttpRequest httpRequest = new HttpRequest();
                        JSONObject json = new JSONObject(),retJson,info_json;
                        json.put("appid",mAppId);
                        retJson = httpRequest.setConnTimeOut(3000).setReadTimeOut(3000).sendPost(mUrl + "/api/cashier/get_time",HttpRequest.generate_request_parm(json, mAppSecret),true);
                        switch (retJson.getIntValue("flag")) {
                            case 0:
                                mCurrentTimestamp = System.currentTimeMillis();
                                Logger.e("同步时间错误:%s",retJson.getString("info"));
                                break;
                            case 1:
                                info_json = JSON.parseObject(retJson.getString("info"));
                                switch (info_json.getString("status")){
                                    case "n":
                                        mCurrentTimestamp = System.currentTimeMillis();
                                        Logger.e("同步时间错误:%s",info_json.getString("info"));
                                        break;
                                    case "y":
                                        mCurrentTimestamp = info_json.getLong("time") * 1000 + (System.currentTimeMillis() - cur);
                                        break;
                                }
                                break;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Logger.e("同步时间错误:%s",e.getMessage());
                    }
                    view.postDelayed(this::startSyncCurrentTime,0);
                });
            }else{
                mCurrentTimestamp = System.currentTimeMillis();
                view.postDelayed(this::startSyncCurrentTime,1000);
            }
        }else{
            mCurrentTimestamp += (1000 + System.currentTimeMillis() - mCurrentTimestamp);
            view.postDelayed(this::startSyncCurrentTime,1000);
        }
        mCurrentTimeViewTv.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(mCurrentTimestamp));
    }
    private void initGoodsInfoAdapter(){
        mGoodsInfoViewAdapter = new GoodsInfoViewAdapter(this);
        final RecyclerView goods_info_view = findViewById(R.id.goods_info_list);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this,GoodsInfoViewAdapter.SPAN_COUNT);
        goods_info_view.setLayoutManager(gridLayoutManager);
        mGoodsInfoViewAdapter.setOnGoodsSelectListener(this::addSaleGoods);
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(goods_info_view,getResources().getDimension(R.dimen.goods_height),new GoodsInfoItemDecoration());
        goods_info_view.setAdapter(mGoodsInfoViewAdapter);
    }
    private void initGoodsCategoryAdapter(){
        final RecyclerView goods_type_view = findViewById(R.id.goods_type_list);
        mGoodsCategoryAdapter = new GoodsCategoryAdapter(this,findViewById(R.id.goods_sec_l_type_list));
        goods_type_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        mGoodsCategoryAdapter.setDatas(0);
        goods_type_view.setAdapter(mGoodsCategoryAdapter);
    }
    private void initSaleGoodsAdapter(){
        mSaleGoodsRecyclerView = findViewById(R.id.sale_goods_list);
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

                setScaleCurrent((float) sale_sum_num * 1000);

                mSaleSumNumTv.setText(String.format(Locale.CANADA,"%.3f",sale_sum_num));
                mSaleSumAmtTv.setText(String.format(Locale.CANADA,"%.2f",sale_sum_amount));
                mDisSumAmtTv.setText(String.format(Locale.CANADA,"%.2f",dis_sum_amt));

                mSaleGoodsRecyclerView.scrollToPosition(mSaleGoodsAdapter.getCurrentItemIndex());

                if (mSecondDisplay != null)mSecondDisplay.notifyChange(mSaleGoodsAdapter.getCurrentItemIndex());
            }
        });
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(mSaleGoodsRecyclerView,getResources().getDimension(R.dimen.sale_goods_height),new SaleGoodsItemDecoration(getColor(R.color.gray_subtransparent)));
        mSaleGoodsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        mSaleGoodsRecyclerView.setAdapter(mSaleGoodsAdapter);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearch(){
        final EditText search = findViewById(R.id.search_content);
        search.setOnKeyListener((v, keyCode, event) -> {
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
                                    addGoodsInfoDialog.setBarcode(mSearch_content.getText().toString());
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
        search.setOnTouchListener(new View.OnTouchListener() {
            private final TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() != 0){
                        mGoodsInfoViewAdapter.fuzzy_search_goods(s.toString(),false);
                    }
                }
            };
            private final View.OnClickListener mKeyboardListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int v_id = view.getId();
                    final Editable editable = search.getText();
                    if (v_id == R.id.DEL){
                        editable.clear();
                    }else if (v_id == R.id.back){
                        if (editable.length() != 0)
                            editable.delete(editable.length() - 1,editable.length());
                    }else if(v_id == R.id.enter){
                        if (editable.length() == 0){
                            mGoodsCategoryAdapter.trigger_preView();
                        }else{
                            mGoodsInfoViewAdapter.fuzzy_search_goods(editable.toString(),true);
                        }
                    }else if(v_id == R.id.hide){
                        search.removeTextChangedListener(textWatcher);
                        mKeyboard.setVisibility(View.GONE);
                    }else {
                        if (search.getSelectionStart() != search.getSelectionEnd()){
                            editable.replace(0,editable.length(),((Button)view).getText());
                            search.setSelection(editable.length());
                        }else
                            editable.append(((Button)view).getText());
                    }
                }
            };
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if (motionEvent.getX() > (search.getWidth() - search.getCompoundPaddingRight())){
                            TableLayout keyboard = mKeyboard;
                            int visible = keyboard.getVisibility();
                            if (visible == View.VISIBLE ){
                                search.removeTextChangedListener(textWatcher);
                                keyboard.setVisibility(View.GONE);
                            }else {
                                search.addTextChangedListener(textWatcher);
                                keyboard.setVisibility(View.VISIBLE);
                            }
                            search.selectAll();
                            View vObj;
                            for(int i = 0,childCounts = keyboard.getChildCount();i < childCounts;i ++){
                                vObj = keyboard.getChildAt(i);
                                if ( vObj instanceof TableRow){
                                    final TableRow tableRow = (TableRow)vObj ;
                                    int buttons = tableRow.getChildCount();
                                    for (int j = 0;j < buttons;j ++){
                                        vObj = tableRow.getChildAt(j);
                                        if (vObj instanceof Button){
                                            final Button button = (Button)vObj;
                                            if (keyboard.getVisibility() == View.VISIBLE){
                                                button.setOnClickListener(mKeyboardListener);
                                            }else{
                                                button.setOnClickListener(null);
                                            }
                                        }
                                    }
                                }
                            }
                            return true;
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });
        mSearch_content = search;
    }
    private void initTmpOrder(){
        final TmpOrderButton tmp_order = findViewById(R.id.tmp_order);
        final SaleActivity activity = this;
        tmp_order.setNum(HangBillDialog.getHangCounts(activity));
        tmp_order.setOnClickListener(v -> {
            if (isAdjustPriceMode()){
                MyDialog.ToastMessage(mSaleGoodsRecyclerView,"调价模式不允许挂单操作!",activity,null);
            }else {
                final JSONArray datas = mSaleGoodsAdapter.getDatas();
                final HangBillDialog hangBillDialog = new HangBillDialog(activity);
                if (Utils.JsonIsNotEmpty(datas)){
                    final StringBuilder err = new StringBuilder();
                    if (hangBillDialog.save(datas,mVipInfo,err)){
                        tmp_order.setNum(HangBillDialog.getHangCounts(activity));
                        resetOrderInfo();
                        MyDialog.ToastMessage(mSaleGoodsRecyclerView,"挂单成功！",activity,null);
                    }else{
                        MyDialog.ToastMessage(mSaleGoodsRecyclerView,"保存挂单错误：" + err,activity,null);
                    }
                }else{
                    if (HangBillDialog.getHangCounts(activity) > 0){
                        hangBillDialog.setGetBillDetailListener((array, vip) -> {
                            hideLastOrderInfo();
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
                                        if (!isAdjustPriceMode()) MyDialog.ToastMessage("选择商品错误：" + goods_info.getString("info"),this,null);
                                        return;
                                    }
                                }
                            }
                        });
                        hangBillDialog.setOnDismissListener(dialog -> tmp_order.setNum(HangBillDialog.getHangCounts(activity)));
                        hangBillDialog.show();
                    }else{
                        MyDialog.ToastMessage(mSaleGoodsRecyclerView,"无挂单信息！",activity,null);
                    }
                }
            }
        });
    }
    private void initPrintStatus(){//打印开关
        final ImageView imageView = findViewById(R.id.printer_status);
        imageView.setOnClickListener(v -> {
            final Bitmap printer = BitmapFactory.decodeResource(getResources(),R.drawable.printer);
            final Object ps = imageView.getTag();
            boolean b = false;
            if (ps instanceof Boolean)b = (boolean)ps;
            if (b){
                b = false;
                imageView.setImageBitmap(PrintUtilsToBitbmp.drawErrorSignToBitmap(this,printer,Utils.dpToPx(this,15),Utils.dpToPx(this,15)));
                MyDialog.ToastMessage(imageView,"打印功能已关闭！",this,getWindow());
            }else{
                b = true;
                imageView.setImageBitmap(printer);
                MyDialog.ToastMessage(imageView,"打印功能已开启！",this,getWindow());
            }
            saveAndShowPrintStatus(b,true);
        });

        imageView.setTag(true);
        mPrinterStatusIv = imageView;

        saveAndShowPrintStatus(true,false);
    }
    private void saveAndShowPrintStatus(boolean print_s,boolean type){
        final JSONObject object = new JSONObject();
        final StringBuilder err = new StringBuilder();
        final ImageView imageView = mPrinterStatusIv;
        if (null != imageView){
            if (type){
                object.put("v",print_s);
                if (!SQLiteHelper.saveLocalParameter("print_s",object,"打印开关",err)){
                    MyDialog.ToastMessage(imageView,"保存打印状态错误:" + err,this,getWindow());
                }
            }else {
                if (SQLiteHelper.getLocalParameter("print_s",object)){
                    if (!object.isEmpty()){
                        print_s = object.getBooleanValue("v");
                        final Bitmap printer = BitmapFactory.decodeResource(getResources(),R.drawable.printer);
                        if (print_s){
                            imageView.setImageBitmap(printer);
                        }else {
                            imageView.setImageBitmap(PrintUtilsToBitbmp.drawErrorSignToBitmap(this,printer,Utils.dpToPx(this,15),Utils.dpToPx(this,15)));
                        }
                    }
                }
            }
            imageView.setTag(print_s);
        }
    }
    private void showPayDialog(){
        if (isAdjustPriceMode()){
            MyDialog.ToastMessage(mSaleGoodsRecyclerView,"调价模式不允许收款操作!",this,null);
        }else {
            if (!mSaleGoodsAdapter.isEmpty()){
                if (!getSingleRefundStatus()){
                    final AbstractSettlementDialog dialog = new NormalSettlementDialog(this,getString(R.string.affirm_pay_sz));
                    dialog.initPayContent();
                    if (mVipInfo != null)dialog.setVipInfo(mVipInfo,true);
                    if (dialog.exec() == 1){
                        mApplication.sync_retail_order();
                        showLastOrderInfo();
                        resetOrderInfo();
                        MyDialog.SnackbarMessage(getWindow(),"结账成功！", mOrderCodeTv);
                    }else {
                        //取消之后重置订单号
                        resetOrderCode();
                    }
                }else {
                    final RefundDialog refundDialog = new RefundDialog(this,"");
                    refundDialog.show();
                }
            }else{
                MyDialog.SnackbarMessage(getWindow(),"已选商品为空！!",getCurrentFocus());
            }
        }
    }
    @Override
    public void clearVipInfo(){
        super.clearVipInfo();
        final LinearLayout vip_info_linearLayout = findViewById(R.id.vip_info_linearLayout);
        vip_info_linearLayout.setVisibility(View.GONE);
        final TextView vip_name_tv = vip_info_linearLayout.findViewById(R.id.vip_name),vip_phone_num_tv = vip_info_linearLayout.findViewById(R.id.vip_phone_num);
        if (null != vip_name_tv && vip_phone_num_tv != null){
            vip_name_tv.setText(getText(R.string.space_sz));
            vip_phone_num_tv.setText(getText(R.string.space_sz));
        }
        if (!mSaleGoodsAdapter.getDatas().isEmpty()){
            mSaleGoodsAdapter.deleteVipDiscountRecord();
        }
        reSizeSaleGoodsView();
    }
    @Override
    public void showVipInfo(final JSONObject vip){
        super.showVipInfo(vip);
        final LinearLayout vip_info_linearLayout = findViewById(R.id.vip_info_linearLayout);
        if (vip_info_linearLayout != null && vip != null){
            vip_info_linearLayout.setVisibility(View.VISIBLE);
            final TextView vip_name_tv = vip_info_linearLayout.findViewById(R.id.vip_name),vip_phone_num_tv = vip_info_linearLayout.findViewById(R.id.vip_phone_num);
            if (null != vip_name_tv && vip_phone_num_tv != null){
                vip_name_tv.setText(vip.getString("name"));
                vip_phone_num_tv.setText(vip.getString("mobile"));
            }
            mSaleGoodsAdapter.updateGoodsInfoToVip(vip);
        }
        reSizeSaleGoodsView();
    }
    protected void reSizeSaleGoodsView(){
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(mSaleGoodsRecyclerView,getResources().getDimension(R.dimen.sale_goods_height),null);
        mSaleGoodsRecyclerView.scrollToPosition(0);
    }

    @Override
    public void resetOrderCode(){
        mOrderCodeTv.setText(mSaleGoodsAdapter.generateOrderCode(mCashierInfo.getString("pos_num"),1));
    }
    private void initSecondDisplay(){
        mSecondDisplay = SecondDisplay.getInstantiate(this);
        if (null != mSecondDisplay){
            if (mApplication.isConnection())mSecondDisplay.loadAdImg(mUrl,mAppId, mAppSecret);
            mSecondDisplay.setDatas(mSaleGoodsAdapter.getDatas()).setNavigationInfo(mStoreInfo).show();
        }
    }
    @Override
    public String getOrderCode(){ return mOrderCodeTv.getText().toString();}
    @Override
    public void loadGoods(final String id){
        if (mGoodsInfoViewAdapter != null)mGoodsInfoViewAdapter.loadGoodsByCategoryId(id);
    }

    @Override
    public boolean getPrintStatus(){
        if (mPrinterStatusIv != null){
            final Object o = mPrinterStatusIv.getTag();
            if (o instanceof Boolean){
                return (boolean)o;
            }
        }
        return false;
    }

    @Override
    public void disposeHangBill(){
        if (mSaleGoodsAdapter.isEmpty()){
            final Button tmp_order = findViewById(R.id.tmp_order);
            if (tmp_order != null)tmp_order.callOnClick();
        }
    }

    @Override
    public void clearSearchEt(){
        if (null != mSearch_content){
            final Editable editable = mSearch_content.getText();
            if (editable.length() != 0){
                editable.clear();
                if (mKeyboard.getVisibility() == View.VISIBLE) mGoodsCategoryAdapter.trigger_preView();
            }
        }
    }

    @Override
    protected void addSaleGoods(final @NonNull JSONObject jsonObject){
        final JSONObject content = new JSONObject();
        final String weigh_barcode_info = (String) jsonObject.remove(GoodsInfoViewAdapter.W_G_MARK);//删除称重标志否则重新选择商品时不弹出称重界面
        if (mGoodsInfoViewAdapter.getSingleGoods(content,weigh_barcode_info,mGoodsInfoViewAdapter.getGoodsId(jsonObject))){
            hideLastOrderInfo();
            mSaleGoodsAdapter.addSaleGoods(content);
        }else{
            if (!isAdjustPriceMode()) MyDialog.ToastMessage("选择商品错误：" + content.getString("info"),this,null);
        }
    }
    private boolean isAdjustPriceMode(){
        return mGoodsInfoViewAdapter != null  && mGoodsInfoViewAdapter.isPriceAdjustMode() ;
    }
    @Override
    public void adjustPriceRefreshGoodsInfo(final JSONArray array){
        if(mGoodsInfoViewAdapter != null && null != array)mGoodsInfoViewAdapter.updateGoodsInfo(array);
    }

    @Override
    public void switchPrintStatus(){
        if (null != mPrinterStatusIv)mPrinterStatusIv.callOnClick();
    }
    @Override
    public void showAdjustPriceDialog(){
        if (mGoodsInfoViewAdapter != null)mGoodsInfoViewAdapter.showAdjustPriceDialog(findViewById(R.id.goods_type_list));
    }
    public void setScaleCurrent(float v){
        if (mScaleView != null)mScaleView.setCurrentValue(v);
    }

    @Override
    public void handleMessage(Handler handler, Message msg) {
        ImageView imageView;
        switch (msg.what){
            case MessageID.DIS_ERR_INFO_ID:
            case MessageID.SYNC_ERR_ID://资料同步错误
                if (mProgressDialog.isShowing())mProgressDialog.dismiss();
                if (msg.obj instanceof String)
                    MyDialog.displayErrorMessage(this, msg.obj.toString());
                break;
            case MessageID.SYNC_FINISH_ID:
                if (mProgressDialog.isShowing())mProgressDialog.dismiss();
                mApplication.start_sync(false);
                break;
            case MessageID.TRANSFERSTATUS_ID://传输状态
                if (msg.obj instanceof Boolean){
                    imageView = findViewById(R.id.upload_status);
                    boolean code = (boolean)msg.obj;
                    if (code && imageView.getAnimation() == null){
                        imageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_y));
                    }
                    if (mApplication.getAndSetTransferStatus(code) != code){
                        if (imageView != null){
                            if (code){
                                imageView.setImageResource(R.drawable.transfer);
                            }else{
                                imageView.setImageResource(R.drawable.transfer_err);
                                imageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_x));
                            }
                        }
                    }
                }
                break;
            case MessageID.NETWORKSTATUS_ID://网络状态
                if (msg.obj instanceof Boolean){
                    boolean code = (boolean)msg.obj;
                    imageView = findViewById(R.id.network_status);
                    if (mApplication.getAndSetNetworkStatus(code) != code){
                        if (imageView != null){
                            if (code){
                                imageView.setImageResource(R.drawable.network);
                            }else{
                                imageView.setImageResource(R.drawable.network_err);
                                imageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_x));
                            }
                        }
                    }
                }
                break;
            case MessageID.SYNC_DIS_INFO_ID://资料同步进度信息
                mProgressDialog.setMessage(msg.obj.toString()).refreshMessage();
                if (!mProgressDialog.isShowing()) {
                    mProgressDialog.setCancel(false).show();
                }
                break;
            case MessageID.START_SYNC_ORDER_INFO_ID:
                Toast.makeText(this,"开始上传数据",Toast.LENGTH_SHORT).show();
                break;
            case MessageID.FINISH_SYNC_ORDER_INFO_ID:
                Toast.makeText(this,"数据上传完成",Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
