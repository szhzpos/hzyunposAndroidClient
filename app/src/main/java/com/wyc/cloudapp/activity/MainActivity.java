package com.wyc.cloudapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.GoodsInfoItemDecoration;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.adapter.GoodsCategoryViewAdapter;
import com.wyc.cloudapp.adapter.SaleGoodsItemDecoration;
import com.wyc.cloudapp.adapter.SaleGoodsViewAdapter;
import com.wyc.cloudapp.adapter.SuperItemDecoration;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.AddGoodsInfoDialog;
import com.wyc.cloudapp.dialog.HangBillDialog;
import com.wyc.cloudapp.dialog.MoreFunDialog;
import com.wyc.cloudapp.dialog.CustomizationView.TmpOrderButton;
import com.wyc.cloudapp.dialog.VerifyPermissionDialog;
import com.wyc.cloudapp.dialog.orderDialog.QuerySaleOrderDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.dialog.orderDialog.TransferDialog;
import com.wyc.cloudapp.dialog.pay.PayDialog;
import com.wyc.cloudapp.dialog.SecondDisplay;
import com.wyc.cloudapp.dialog.vip.VipInfoDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.network.sync.SyncManagement;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.print.PrintUtilsToBitbmp;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.Utils;

import com.alibaba.fastjson.JSONArray;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private SaleGoodsViewAdapter mSaleGoodsViewAdapter;
    private GoodsCategoryViewAdapter mGoodsCategoryViewAdapter;
    private GoodsInfoViewAdapter mGoodsInfoViewAdapter;
    private EditText mSearch_content;
    private JSONObject mCashierInfo,mStoreInfo,mVipInfo;
    private Myhandler mHandler;
    private CustomProgressDialog mProgressDialog;
    private MyDialog mDialog;
    private AtomicBoolean mNetworkStatus;
    private AtomicBoolean mTransferStatus;
    private long mCurrentTimestamp = 0;
    private String mAppId, mAppSecret,mUrl;
    private TextView mCurrentTimeViewTv, mSaleSumNumTv, mSaleSumAmtTv, mOrderCodeTv, mDisSumAmtTv;
    private SyncManagement mSyncManagement;
    private ImageView mCloseBtn;
    private RecyclerView mSaleGoodsRecyclerView;
    private TableLayout mKeyboard;
    private String mPermissionCashierId = "";
    private SecondDisplay mSecondDisplay;
    private ConstraintLayout mLastOrderInfo;
    private ImageView mPrinterStatusIv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initMemberVariable();

        initNetworkStatus();
        initLastOrderInfo();

        //初始化adapter
        initGoodsInfoAdapter();
        initGoodsCategoryAdapter();
        initSaleGoodsAdapter();

        //初始化收银员、仓库信息
        initCashierInfoAndStoreInfo();
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

        //初始化数据管理对象
        initSyncManagement();

        //重置订单信息
        resetOrderInfo();

        //初始化副屏
        initSecondDisplay();

    }
    private void initMemberVariable(){
        mHandler = new Myhandler(this);
        mProgressDialog = new CustomProgressDialog(this);
        mDialog = new MyDialog(this);
        mCurrentTimeViewTv = findViewById(R.id.current_time);
        mSaleSumNumTv = findViewById(R.id.sale_sum_num);
        mSaleSumAmtTv = findViewById(R.id.sale_sum_amt);
        mKeyboard = findViewById(R.id.keyboard_layout);
        mOrderCodeTv = findViewById(R.id.order_code);
        mDisSumAmtTv = findViewById(R.id.dis_sum_amt);
        mTransferStatus = new AtomicBoolean(true);//传输状态
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
    public void onDestroy(){
        super.onDestroy();
        //清除资源
        clearResource();
    }
    @Override
    public void onBackPressed(){
        if (null != mCloseBtn)mCloseBtn.callOnClick();
    }
    @Override
    public void finalize(){
        Logger.d("MainActivity finalize");
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
    private void showLastOrderInfo(final String order_code){
        final ConstraintLayout constraintLayout = mLastOrderInfo;
        if (constraintLayout != null){
            constraintLayout.setVisibility(View.VISIBLE);
            final JSONObject order_info = new JSONObject();
            if (SQLiteHelper.execSql(order_info,"SELECT order_code,sum(pre_sale_money) pre_amt,sum(pay_money) pay_amt,sum(pre_sale_money - pay_money) zl_amt FROM " +
                    "retail_order_pays where order_code = '" + order_code +"' group by order_code")){

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
                last_reprint_btn.setOnClickListener(v -> Printer.print(this, PayDialog.get_print_content(this,last_order_code.getText().toString(),false)));
            }else {
                MyDialog.ToastMessage(order_info.getString("info"),this,getWindow());
            }
        }
    }

    private void initNetworkStatus(){
        final Intent intent = getIntent();
        boolean code = intent.getBooleanExtra("network",false);
        mNetworkStatus = new AtomicBoolean(code);
        final ImageView imageView = findViewById(R.id.network_status);
        if (imageView != null && !code){
            imageView.setImageResource(R.drawable.network_err);
            imageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_x));
        }
    }
    public void data_upload(){
        if (mSyncManagement != null)mSyncManagement.sync_order_info();
    }
    private void initSyncManagement(){
        mSyncManagement = new SyncManagement(mHandler,mUrl,mAppId, mAppSecret,mStoreInfo.getString("stores_id"),mCashierInfo.getString("pos_num"),mCashierInfo.getString("cas_id"));
        mSyncManagement.sync_order_info();
        mSyncManagement.start_sync(false);
    }

    private boolean verifyNumBtnPermissions(){
        return verifyPermissions("25",null);
    }
    private boolean verifyQueryBtnBtnPermissions(){
        return verifyPermissions("26",null);
    }
    private void initFunctionBtn(){
        final Button minus_num_btn = findViewById(R.id.minus_num),add_num_btn = findViewById(R.id.add_num),num_btn = findViewById(R.id.num),
                discount_btn = findViewById(R.id.discount),change_price_btn = findViewById(R.id.change_price),check_out_btn = findViewById(R.id.check_out),
                vip_btn = findViewById(R.id.vip);

        if (minus_num_btn != null)minus_num_btn.setOnClickListener(v -> mSaleGoodsViewAdapter.deleteSaleGoods(mSaleGoodsViewAdapter.getCurrentItemIndex(),1));//数量减
        if (add_num_btn != null)add_num_btn.setOnClickListener(v -> mSaleGoodsViewAdapter.addSaleGoods(mSaleGoodsViewAdapter.getCurrentContent(),mVipInfo));//数量加
        if (num_btn != null)num_btn.setOnClickListener(view -> {if (verifyNumBtnPermissions())mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 0);});//数量
        if (discount_btn != null)discount_btn.setOnClickListener(v-> {mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 2);});//打折
        if (change_price_btn != null)change_price_btn.setOnClickListener(v-> {mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 1);});//改价
        if (check_out_btn != null)check_out_btn.setOnClickListener((View v)->{Utils.disableView(v,500);showPayDialog();});//结账
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
                if (verifyQueryBtnBtnPermissions()){
                    final QuerySaleOrderDialog querySaleOrderDialog = new QuerySaleOrderDialog(this);
                    querySaleOrderDialog.show();
                    querySaleOrderDialog.triggerQuery();
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
    private void initTransferBtn(){
        final LinearLayout shift_exchange_linearLayout = findViewById(R.id.shift_exchange_linearLayout);
        if (shift_exchange_linearLayout != null)
            shift_exchange_linearLayout.setOnClickListener(v -> {
                if (verifyPermissions("6",null)){
                    final MainActivity activity = MainActivity.this;
                    final TransferDialog transferDialog = new TransferDialog(activity);
                    transferDialog.setFinishListener(() -> {
                        mSyncManagement.sync_transfer_order();
                        MyDialog my_dialog = new MyDialog(activity);
                        my_dialog.setMessage("交班成功！").setYesOnclickListener(activity.getString(R.string.OK), new MyDialog.onYesOnclickListener() {
                            @Override
                            public void onYesClick(MyDialog myDialog) {
                                transferDialog.dismiss();
                                myDialog.dismiss();
                                final Intent intent = new Intent(activity, LoginActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                activity.startActivity(intent);
                                activity.finish();
                            }
                        }).show();
                    });
                    transferDialog.verifyTransfer();
                }
            });
    }
    private void initClearBtn(){
        final Button clearBtn = findViewById(R.id.clear);
        if (null != clearBtn){
            clearBtn.setOnClickListener(v -> {
                if (!mSaleGoodsViewAdapter.getDatas().isEmpty()){
                    if (verifyClearPermissions()){
                        MyDialog.displayAskMessage(mDialog,"是否清除销售商品？",this,myDialog -> {
                            resetOrderInfo();
                            myDialog.dismiss();
                        },Dialog::dismiss);
                    }
                }else {
                    if (getSingle()){
                        resetOrderInfo();
                    }
                }
            });
        }
    }

    private boolean verifyClearPermissions(){
        return verifyPermissions("2",null);
    }

    private void initCloseMainWindow(){
        mCloseBtn = findViewById(R.id.close);
        mCloseBtn.setOnClickListener((View V)->{
            MyDialog.displayAskMessage(mDialog,"是否退出收银？",MainActivity.this,(MyDialog myDialog)->{
                myDialog.dismiss();
                Intent intent = new Intent(this,LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                this.finish();
            }, Dialog::dismiss);
        });
    }
    private void clearResource(){
        if (mHandler != null)mHandler.removeCallbacksAndMessages(null);
        if (mSyncManagement != null) {
            mSyncManagement.quit();
        }
        if (mProgressDialog.isShowing())mProgressDialog.dismiss();
        if (mDialog.isShowing())mDialog.dismiss();
        if (mSecondDisplay != null)mSecondDisplay.dismiss();
    }
    private void startSyncCurrentTime(){
        if (mCurrentTimestamp == 0){
            if (mNetworkStatus.get()){
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
                    mHandler.postDelayed(this::startSyncCurrentTime,0);
                });
            }else{
                mCurrentTimestamp = System.currentTimeMillis();
                mHandler.postDelayed(this::startSyncCurrentTime,1000);
            }
        }else{
            mCurrentTimestamp += (1000 + System.currentTimeMillis() - mCurrentTimestamp);
            mHandler.postDelayed(this::startSyncCurrentTime,1000);
        }
        mCurrentTimeViewTv.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(mCurrentTimestamp));
    }
    private void initCashierInfoAndStoreInfo(){
        final JSONObject cas_info = mCashierInfo = new JSONObject();
        final JSONObject st_info = mStoreInfo = new JSONObject();

        if (SQLiteHelper.getLocalParameter("cashierInfo",cas_info)){
            final TextView cashier_name = findViewById(R.id.cashier_name),
                    store_name = findViewById(R.id.store_name),
                    pos_num = findViewById(R.id.pos_num);

            cashier_name.setText(cas_info.getString("cas_name"));
            pos_num.setText(cas_info.getString("pos_num"));
            if (SQLiteHelper.getLocalParameter("connParam",st_info)){
                try {
                    mUrl = st_info.getString("server_url");
                    mAppId = st_info.getString("appId");
                    mAppSecret = st_info.getString("appScret");

                    mStoreInfo = JSON.parseObject(st_info.getString("storeInfo"));
                    store_name.setText(String.format("%s%s%s%s",mStoreInfo.getString("stores_name"),"[",mStoreInfo.getString("stores_id"),"]"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    mDialog.setMessage(e.getMessage()).setNoOnclickListener("取消", myDialog -> MainActivity.this.finish()).show();
                }
            }else{
                mDialog.setMessage(cas_info.getString("info")).setNoOnclickListener("取消", myDialog -> MainActivity.this.finish()).show();
            }
        }else{
            mDialog.setMessage(st_info.getString("info")).setNoOnclickListener("取消", myDialog -> MainActivity.this.finish()).show();
        }
    }
    private void initGoodsInfoAdapter(){
        mGoodsInfoViewAdapter = new GoodsInfoViewAdapter(this);
        final RecyclerView goods_info_view = findViewById(R.id.goods_info_list);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this,GoodsInfoViewAdapter.spanCount);
        goods_info_view.setLayoutManager(gridLayoutManager);
        registerGlobalLayoutToRecyclerView(goods_info_view,getResources().getDimension(R.dimen.goods_height),new GoodsInfoItemDecoration());
        mGoodsInfoViewAdapter.setOnItemClickListener(v -> {
            hideLastOrderInfo();
            Utils.disableView(v,300);
            final JSONObject jsonObject = mGoodsInfoViewAdapter.getSelectGoods(v);
            if (jsonObject != null){
                addSaleGoods(jsonObject);
            }
        });
        goods_info_view.setAdapter(mGoodsInfoViewAdapter);
    }
    private void initGoodsCategoryAdapter(){
        final RecyclerView goods_type_view = findViewById(R.id.goods_type_list);
        mGoodsCategoryViewAdapter = new GoodsCategoryViewAdapter(this,findViewById(R.id.goods_sec_l_type_list));
        goods_type_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        mGoodsCategoryViewAdapter.setDatas(0);
        goods_type_view.setAdapter(mGoodsCategoryViewAdapter);
    }
    private void initSaleGoodsAdapter(){
        mSaleGoodsRecyclerView = findViewById(R.id.sale_goods_list);
        mSaleGoodsViewAdapter = new SaleGoodsViewAdapter(this);
        mSaleGoodsViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged(){
                final JSONArray datas = mSaleGoodsViewAdapter.getDatas();
                double sale_sum_num = 0.0,sale_sum_amount = 0.0,dis_sum_amt = 0.0;

                for (int i = 0,length = datas.size();i < length;i ++){
                    final JSONObject jsonObject = datas.getJSONObject(i);
                    sale_sum_num += jsonObject.getDouble("xnum");
                    sale_sum_amount += jsonObject.getDouble("sale_amt");
                    dis_sum_amt += jsonObject.getDouble("discount_amt");
                }
                mSaleSumNumTv.setText(String.format(Locale.CANADA,"%.3f",sale_sum_num));
                mSaleSumAmtTv.setText(String.format(Locale.CANADA,"%.2f",sale_sum_amount));
                mDisSumAmtTv.setText(String.format(Locale.CANADA,"%.2f",dis_sum_amt));

                mSaleGoodsRecyclerView.scrollToPosition(mSaleGoodsViewAdapter.getCurrentItemIndex());

                if (mSecondDisplay != null)mSecondDisplay.notifyChange(mSaleGoodsViewAdapter.getCurrentItemIndex());
            }
        });
        reSizeSaleGoodsView();
        mSaleGoodsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        mSaleGoodsRecyclerView.setAdapter(mSaleGoodsViewAdapter);
    }
    private void reSizeSaleGoodsView(){
        registerGlobalLayoutToRecyclerView(mSaleGoodsRecyclerView,getResources().getDimension(R.dimen.sale_goods_height),new SaleGoodsItemDecoration(getColor(R.color.gray_subtransparent)));
    }
    @SuppressLint("ClickableViewAccessibility")
    private void initSearch(){
        final EditText search = findViewById(R.id.search_content);;
        search.setOnFocusChangeListener((v,b)->Utils.hideKeyBoard((EditText) v));
        mHandler.postDelayed(search::requestFocus,300);
        search.setSelectAllOnFocus(true);
        search.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_DOWN){
                final MainActivity context = MainActivity.this;
                final String content = search.getText().toString();
                if (content.length() == 0){
                    mGoodsCategoryViewAdapter.trigger_preView();
                }else{
                    if (!mGoodsInfoViewAdapter.fuzzy_search_goods(content,true)) {
                        mHandler.post(()->{
                            if (mNetworkStatus.get() && AddGoodsInfoDialog.verifyGoodsAddPermissions(context)) {
                                if (1 == MyDialog.showMessageToModalDialog(context,"未找到匹配商品，是否新增?")){
                                    final AddGoodsInfoDialog addGoodsInfoDialog = new AddGoodsInfoDialog(context);
                                    addGoodsInfoDialog.setBarcode(mSearch_content.getText().toString());
                                    addGoodsInfoDialog.setFinishListener(barcode -> {
                                        mGoodsInfoViewAdapter.fuzzy_search_goods(content,true);
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
            private TextWatcher textWatcher = new TextWatcher() {
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
            private View.OnClickListener mKeyboardListener = new View.OnClickListener() {
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
                            mGoodsCategoryViewAdapter.trigger_preView();
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
        final MainActivity activity = this;
        tmp_order.setNum(HangBillDialog.getHangCounts(activity));
        tmp_order.setOnClickListener(v -> {
            JSONArray datas = mSaleGoodsViewAdapter.getDatas();
            final HangBillDialog hangBillDialog = new HangBillDialog(activity);
            if (Utils.JsonIsNotEmpty(datas)){
                //MyDialog.displayAskMessage(null, "是否挂单？", activity, myDialog -> {
                    final StringBuilder err = new StringBuilder();
                    if (hangBillDialog.save(datas,mVipInfo,err)){
                        tmp_order.setNum(HangBillDialog.getHangCounts(activity));
                        resetOrderInfo();
                        MyDialog.ToastMessage(mSaleGoodsRecyclerView,"挂单成功！",activity,null);
                        //myDialog.dismiss();
                    }else{
                        MyDialog.ToastMessage(mSaleGoodsRecyclerView,"保存挂单错误：" + err,activity,null);
                    }
                //}, Dialog::dismiss);
            }else{
                if (HangBillDialog.getHangCounts(activity) > 0){
                    hangBillDialog.setGetBillDetailListener((array, vip) -> {
                        hideLastOrderInfo();
                        if (null != vip)showVipInfo(vip);
                        JSONObject barcode_id_obj,goods_info;
                        String id;
                        for (int i = 0,length = array.size();i < length;i ++){
                            barcode_id_obj = array.getJSONObject(i);
                            if (barcode_id_obj != null){
                                goods_info = new JSONObject();
                                final String isBarcodeWeighingGoods = barcode_id_obj.getString(GoodsInfoViewAdapter.W_G_MARK);
                                id = mGoodsInfoViewAdapter.getGoodsId(barcode_id_obj);
                                if (mGoodsInfoViewAdapter.getSingleGoods(goods_info,isBarcodeWeighingGoods,id)){
                                    goods_info.put("xnum",barcode_id_obj.getDoubleValue("xnum"));//挂单取出重量
                                    mSaleGoodsViewAdapter.addSaleGoods(goods_info,mVipInfo);
                                    hangBillDialog.dismiss();
                                }else{
                                    MyDialog.ToastMessage(mSaleGoodsRecyclerView,"查询商品信息错误：" + goods_info.getString("info"),activity,getWindow());
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
                imageView.setImageBitmap(PrintUtilsToBitbmp.drawErrorSignToBitmap(printer,Utils.px2dip(this,15),Utils.px2dip(this,15)));
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
                            imageView.setImageBitmap(PrintUtilsToBitbmp.drawErrorSignToBitmap(printer,15,15));
                        }
                    }
                }
            }
            imageView.setTag(print_s);
        }
    }

    private void showPayDialog(){
        if (!mSaleGoodsViewAdapter.isEmpty()){
            if (!getSingle()){
                final PayDialog dialog = new PayDialog(this,getString(R.string.affirm_pay_sz));
                if (dialog.initPayContent()){
                    final MainActivity activity = MainActivity.this;
                    dialog.setPayListener(new PayDialog.onPayListener() {
                        @Override
                        public void onStart(PayDialog myDialog) {
                            mProgressDialog.setCancel(false).setMessage("正在保存单据...").refreshMessage().show();
                            final StringBuilder err = new StringBuilder();
                            if (myDialog.saveOrderInfo(err)){
                                CustomApplication.execute(myDialog::requestPay);
                            }else{
                                mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,err.toString()).sendToTarget();
                            }
                        }
                        @Override
                        public void onProgress(PayDialog myDialog, final String info) {
                            mProgressDialog.setMessage(info).refreshMessage();
                        }

                        @Override
                        public void onSuccess(PayDialog myDialog) {
                            if (mProgressDialog.isShowing())mProgressDialog.dismiss();
                            mSyncManagement.sync_retail_order();
                            showLastOrderInfo(mOrderCodeTv.getText().toString());
                            resetOrderInfo();
                            myDialog.dismiss();
                            MyDialog.SnackbarMessage(activity.getWindow(),"结账成功！", mOrderCodeTv);
                        }

                        @Override
                        public void onError(PayDialog myDialog, String err) {
                            if (mProgressDialog.isShowing())mProgressDialog.dismiss();
                            resetOrderCode();//提示错误得重置单号
                            MyDialog.displayErrorMessage(null,"支付错误：" + err,activity);
                        }
                    }).show();
                    if (mVipInfo != null)dialog.showVipInfo(mVipInfo,true);
                }
            }else {
                final RefundDialog refundDialog = new RefundDialog(this,"");
                refundDialog.show();
            }
        }else{
            MyDialog.SnackbarMessage(getWindow(),"已选商品为空！!",getCurrentFocus());
        }
    }
    public void resetOrderInfo(){
        mPermissionCashierId = "";
        mSaleGoodsViewAdapter.clearGoods();
        clearVipInfo();
        setSingle(false);
        resetOrderCode();
    }
    private void clearVipInfo(){
        if (mVipInfo != null){
            mVipInfo = null;
            final LinearLayout vip_info_linearLayout = findViewById(R.id.vip_info_linearLayout);
            vip_info_linearLayout.setVisibility(View.GONE);
            ((TextView)vip_info_linearLayout.findViewById(R.id.vip_name)).setText(getText(R.string.space_sz));
            ((TextView)vip_info_linearLayout.findViewById(R.id.vip_phone_num)).setText(getText(R.string.space_sz));
            if (!mSaleGoodsViewAdapter.getDatas().isEmpty()){
                mSaleGoodsViewAdapter.deleteVipDiscountRecord();
            }
            reSizeSaleGoodsView();
        }
    }
    private void registerGlobalLayoutToRecyclerView(@NonNull final View view,final float size,@NonNull final SuperItemDecoration superItemDecoration){
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int getVerSpacing(int viewHeight,int m_height){
                double vertical_space ,vertical_counts,per_vertical_space;
                vertical_space = viewHeight % m_height;
                vertical_counts = viewHeight / m_height;
                per_vertical_space = vertical_space / (vertical_counts != 0 ? vertical_counts:1);

                return (int)Utils.formatDouble(per_vertical_space,0);
            }
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = view.getMeasuredHeight();
                if (view instanceof RecyclerView){
                    final RecyclerView recyclerView = ((RecyclerView)view);
                    if (recyclerView.getItemDecorationCount() > 0){
                        recyclerView.removeItemDecorationAt(0);
                    }
                    superItemDecoration.setSpace(getVerSpacing(height,(int) size));
                    recyclerView.addItemDecoration(superItemDecoration);
                }
            }
        });
    }

    private void resetOrderCode(){
        mOrderCodeTv.setText(mSaleGoodsViewAdapter.generateOrderCode(mCashierInfo.getString("pos_num"),1));
    }
    private void initSecondDisplay(){
        mSecondDisplay = SecondDisplay.getInstantiate(this);
        if (null != mSecondDisplay){
            if (isConnection())mSecondDisplay.loadAdImg(mUrl,mAppId, mAppSecret);
            mSecondDisplay.setDatas(mSaleGoodsViewAdapter.getDatas()).setNavigationInfo(mStoreInfo).show();
        }
    }

    public boolean allDiscount(double v){
       return mSaleGoodsViewAdapter.allDiscount(v);
    }
    public void deleteMolDiscountRecord(){
        mSaleGoodsViewAdapter.deleteMolDiscountRecord();
    }
    public void deleteAlldiscountRecord(){
        mSaleGoodsViewAdapter.deleteAlldiscountRecord();
    }
    public String discountRecordsToString(){
        return mSaleGoodsViewAdapter.discountRecordsToString();
    }
    public void autoMol(double mol_amt){
        mSaleGoodsViewAdapter.autoMol(mol_amt);
    }
    public void manualMol(double mol_amt){
        mSaleGoodsViewAdapter.manualMol(mol_amt);
    }
    public void sync(boolean b){
        if (mSyncManagement != null){
            if (mProgressDialog != null && !mProgressDialog.isShowing())mProgressDialog.setMessage("正在同步...").refreshMessage().show();
            mSyncManagement.start_sync(b);
        }
    }
    public void sync_refund_order(){
        if (mSyncManagement != null) mSyncManagement.sync_refund_order();
    }
    public JSONArray getSaleData(){
        return mSaleGoodsViewAdapter.getDatas();
    }
    public JSONArray getDiscountRecords(){
        return mSaleGoodsViewAdapter.getDiscountRecords();
    }
    public boolean splitCombinationalGoods(final JSONArray combination_goods,int gp_id,double gp_price,double gp_num,StringBuilder err){
        return mSaleGoodsViewAdapter.splitCombinationalGoods(combination_goods,gp_id,gp_price,gp_num,err);
    }
    public void showVipInfo(@NonNull JSONObject vip){
        mVipInfo = vip;

        reSizeSaleGoodsView();

        final LinearLayout vip_info_linearLayout = findViewById(R.id.vip_info_linearLayout);
        vip_info_linearLayout.setVisibility(View.VISIBLE);
        ((TextView)vip_info_linearLayout.findViewById(R.id.vip_name)).setText(mVipInfo.getString("name"));
        ((TextView)vip_info_linearLayout.findViewById(R.id.vip_phone_num)).setText(mVipInfo.getString("mobile"));

        mSaleGoodsViewAdapter.updateGoodsInfoToVip(mVipInfo);
    }
    public double getSumAmt(int type){
        return mSaleGoodsViewAdapter.getSumAmt(type);
    }
    public String getPosNum(){if (null == mCashierInfo)return "";return mCashierInfo.getString("pos_num");}
    public JSONObject getCashierInfo(){
        return mCashierInfo;
    }
    public JSONObject getStoreInfo(){
        return mStoreInfo;
    }
    public String getOrderCode(){ return mOrderCodeTv.getText().toString();}
    public String getPermissionCashierId(){
        if ("".equals(mPermissionCashierId))return mCashierInfo.getString("cas_id");
        return mPermissionCashierId;
    }
    public GoodsInfoViewAdapter getGoodsInfoViewAdapter(){
        return mGoodsInfoViewAdapter;
    }
    public String getAppId(){
        return mAppId;
    }
    public String getAppSecret(){
        return mAppSecret;
    }
    public String getUrl(){
        return mUrl;
    }
    public boolean getPrintStatus(){
        if (mPrinterStatusIv != null){
            final Object o = mPrinterStatusIv.getTag();
            if (o instanceof Boolean){
                return (boolean)o;
            }
        }
        return false;
    }
    public void disposeHangBill(){
        if (mSaleGoodsViewAdapter.isEmpty()){
            final Button tmp_order = findViewById(R.id.tmp_order);
            if (tmp_order != null)tmp_order.callOnClick();
        }
    }
    public boolean isConnection(){
        return mNetworkStatus.get();
    }

    public boolean verifyPermissions(final String per_id,final String requested_cas_code){
        return verifyPermissions(per_id,requested_cas_code,true);
    }
    public boolean verifyPermissions(final String per_id,final String requested_cas_code,boolean isShow){
        boolean code = false;
         if (mCashierInfo != null && mStoreInfo != null){
            String cashier_id = Utils.getNullStringAsEmpty(mCashierInfo,"cas_code"),cas_pwd = Utils.getNullStringAsEmpty(mCashierInfo,"cas_pwd"),stores_id = mStoreInfo.getString("stores_id");
            final StringBuilder err = new StringBuilder();
            if (null != requested_cas_code){
                cas_pwd = Utils.getUserIdAndPasswordCombinationOfMD5(requested_cas_code);
                Logger.i("操作员:%s,向:%s请求权限:%s",cashier_id,cas_pwd,per_id);
            }
            final String authority = SQLiteHelper.getString("SELECT authority FROM cashier_info where cas_pwd = '" + cas_pwd +"' and stores_id = " + stores_id,err);
            if (null != authority){
                try {
                    JSONArray permissions;
                    if (authority.startsWith("{")){
                        final JSONObject jsonObject = JSON.parseObject(authority);
                        permissions = new JSONArray();
                        for (String key : jsonObject.keySet()){
                            permissions.add(jsonObject.get(key));
                        }
                    }else {
                        permissions = JSON.parseArray(authority);
                    }
                    if (permissions != null){
                        for (int i = 0,size = permissions.size();i < size;i ++){
                            final JSONObject obj = permissions.getJSONObject(i);
                            if (obj != null){
                                if (Utils.getNullStringAsEmpty(obj,"authority").equals(per_id)){
                                    code = (1 == obj.getIntValue("is_have"));
                                    if (isShow){
                                        if (!code){
                                            final VerifyPermissionDialog verifyPermissionDialog = new VerifyPermissionDialog(this);
                                            verifyPermissionDialog.setHintPerName(Utils.getNullStringAsEmpty(obj,"authority_name"));
                                            verifyPermissionDialog.setFinishListener(dialog -> {
                                                if (verifyPermissions(per_id,dialog.getContent(),true)){
                                                    dialog.setCodeAndExit(1);
                                                }else{
                                                    dialog.setCodeAndExit(0);
                                                }

                                            });
                                            code = verifyPermissionDialog.exec() == 1;
                                        }else {
                                            mPermissionCashierId = Utils.getNullStringAsEmpty(obj,"cas_id");
                                        }
                                    }
                                }
                            }
                        }
                    }else {
                        MyDialog.displayErrorMessage(mDialog,"未找到授权工号的权限记录,请确定输入是否正确!",this);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.displayErrorMessage(mDialog,"权限数据解析错误：" + e.getMessage(),this);
                }
            }else {
                MyDialog.displayErrorMessage(mDialog,"权限查询错误：" + err,this);
            }
        }
        return code;
    }
    public boolean verifyDiscountPermissions(double discount,final String requested_cas_code){
        boolean code = false;
        if (mCashierInfo != null && mStoreInfo != null){
            String cashier_id = Utils.getNullStringAsEmpty(mCashierInfo,"cas_code"),cas_pwd = Utils.getNullStringAsEmpty(mCashierInfo,"cas_pwd"),stores_id = mStoreInfo.getString("stores_id");
            if (null != requested_cas_code){
                cas_pwd = Utils.getUserIdAndPasswordCombinationOfMD5(requested_cas_code);
                Logger.i("操作员:%s,向:%s请求折扣为%f的权限",cashier_id,cas_pwd,discount);
            }
            final JSONObject discount_ojb = new JSONObject();
            if (SQLiteHelper.execSql(discount_ojb,"SELECT ifnull(min_discount,0.0) min_discount,cas_id FROM cashier_info where cas_pwd = '" + cas_pwd +"' and stores_id = " + stores_id)){
                if (!discount_ojb.isEmpty()){
                    double local_dis = discount_ojb.getDoubleValue("min_discount") / 10;

                    Logger.d("local_dis:%f,discount:%f",local_dis,discount);

                    if (local_dis > discount){
                        final VerifyPermissionDialog verifyPermissionDialog = new VerifyPermissionDialog(this);
                        verifyPermissionDialog.setHintPerName(String.format(Locale.CHINA,"%.1f%s",discount * 10,"折"));
                        verifyPermissionDialog.setFinishListener(dialog -> {
                            if (verifyDiscountPermissions(discount,dialog.getContent())){
                                dialog.setCodeAndExit(1);
                            }else
                                dialog.setCodeAndExit(0);
                        });
                        code = verifyPermissionDialog.exec() == 1;
                    }else {
                        mPermissionCashierId = discount_ojb.getString("cas_id");
                        code = true;
                    }
                }else{
                    MyDialog.displayErrorMessage(mDialog,"未找到授权工号的权限记录,请确定输入是否正确!",this);
                }
            } else {
                MyDialog.displayErrorMessage(mDialog,"权限查询错误：" + discount_ojb.getString("info"),this);
            }
        }
        return code;
    }

    public void setSingle(boolean b){
        if (mSaleGoodsViewAdapter != null)mSaleGoodsViewAdapter.setSingle(b);
        if (b)resetOrderCode();
    }
    public boolean getSingle(){
        return mSaleGoodsViewAdapter != null && mSaleGoodsViewAdapter.getSingle();
    }
    public boolean present(){
        return null != mSaleGoodsViewAdapter && mSaleGoodsViewAdapter.present();
    }
    public void clearSearchEt(){
        if (null != mSearch_content){
            final Editable editable = mSearch_content.getText();
            if (editable.length() != 0){
                editable.clear();
                if (mKeyboard.getVisibility() == View.VISIBLE)mGoodsCategoryViewAdapter.trigger_preView();
            }
        }
    }
    public void addSaleGoods(final @NonNull JSONObject jsonObject){
        final JSONObject content = new JSONObject();
        final String id = mGoodsInfoViewAdapter.getGoodsId(jsonObject);
        final String weigh_barcode_info = (String) jsonObject.remove(GoodsInfoViewAdapter.W_G_MARK);//删除称重标志否则重新选择商品时不弹出称重界面
        if (mGoodsInfoViewAdapter.getSingleGoods(content,weigh_barcode_info,id)){
            mSaleGoodsViewAdapter.addSaleGoods(content,mVipInfo);
        }else{
            MyDialog.ToastMessage("选择商品错误：" + content.getString("info"),this,null);
        }
    }
    public void triggerPsClick(){
        if (null != mPrinterStatusIv)mPrinterStatusIv.callOnClick();
    }

    private static class Myhandler extends Handler {
        private WeakReference<MainActivity> weakHandler;
        private Myhandler(MainActivity mainActivity){
            this.weakHandler = new WeakReference<>(mainActivity);
        }
        public void handleMessage(@NonNull Message msg){
            ImageView imageView;
            MainActivity activity = weakHandler.get();
            if (null == activity)return;

            switch (msg.what){
                case MessageID.DIS_ERR_INFO_ID:
                case MessageID.SYNC_ERR_ID://资料同步错误
                    if (activity.mProgressDialog != null && activity.mProgressDialog.isShowing())activity.mProgressDialog.dismiss();
                    if (msg.obj instanceof String)
                        MyDialog.displayErrorMessage(activity.mDialog,msg.obj.toString(),activity);
                    break;
                case MessageID.SYNC_FINISH_ID:
                    if (activity.mProgressDialog != null && activity.mProgressDialog.isShowing())activity.mProgressDialog.dismiss();
                    activity.mSyncManagement.start_sync(false);
                    break;
                case MessageID.TRANSFERSTATUS_ID://传输状态
                    if (activity.mProgressDialog != null && activity.mProgressDialog.isShowing())activity.mProgressDialog.dismiss();
                    if (msg.obj instanceof Boolean){
                        imageView = activity.findViewById(R.id.upload_status);
                        boolean code = (boolean)msg.obj;
                        if (code && imageView.getAnimation() == null){
                            imageView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake_y));
                        }
                        if (activity.mTransferStatus.getAndSet(code) != code){
                            if (imageView != null){
                                if (code){
                                    imageView.setImageResource(R.drawable.transfer);
                                }else{
                                    imageView.setImageResource(R.drawable.transfer_err);
                                    imageView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake_x));
                                }
                            }
                        }
                    }
                    break;
                case MessageID.NETWORKSTATUS_ID://网络状态
                    if (msg.obj instanceof Boolean){
                        boolean code = (boolean)msg.obj;
                        imageView = activity.findViewById(R.id.network_status);
                        if (activity.mNetworkStatus.getAndSet(code) != code){
                            if (imageView != null){
                                if (code){
                                    imageView.setImageResource(R.drawable.network);
                                }else{
                                    imageView.setImageResource(R.drawable.network_err);
                                    imageView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake_x));
                                }
                            }
                        }
                    }
                    break;
                case MessageID.SYNC_DIS_INFO_ID://资料同步进度信息
                    if (activity.mProgressDialog != null){
                        activity.mProgressDialog.setMessage(msg.obj.toString()).refreshMessage();
                        if (!activity.mProgressDialog.isShowing()) {
                            activity.mProgressDialog.setCancel(false).show();
                        }
                    }
                    break;
                case MessageID.START_SYNC_ORDER_INFO_ID:
                    Toast.makeText(activity,"开始上传数据",Toast.LENGTH_SHORT).show();
                    break;
                case MessageID.FINISH_SYNC_ORDER_INFO_ID:
                    Toast.makeText(activity,"数据上传完成",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
