package com.wyc.cloudapp.activity.normal;

import static com.wyc.cloudapp.fragment.PrintFormatFragment.ACTION_USB_PERMISSION;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.method.ReplacementTransformationMethod;
import android.view.Gravity;
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

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.SaleActivity;
import com.wyc.cloudapp.adapter.GoodsCategoryAdapter;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.adapter.NormalSaleGoodsAdapter;
import com.wyc.cloudapp.adapter.TreeListBaseAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.bean.ModulePermission;
import com.wyc.cloudapp.constants.MessageID;
import com.wyc.cloudapp.customizationView.FlowLayout;
import com.wyc.cloudapp.customizationView.IndicatorRecyclerView;
import com.wyc.cloudapp.customizationView.InterceptLinearLayout;
import com.wyc.cloudapp.customizationView.JumpTextView;
import com.wyc.cloudapp.customizationView.TmpOrderButton;
import com.wyc.cloudapp.customizationView.WeightInfoView;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.decoration.GridItemDecoration;
import com.wyc.cloudapp.decoration.LinearItemDecoration;
import com.wyc.cloudapp.decoration.SaleGoodsItemDecoration;
import com.wyc.cloudapp.decoration.SuperItemDecoration;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.dialog.MoreFunDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.SecondDisplay;
import com.wyc.cloudapp.dialog.goods.AddGoodsInfoDialog;
import com.wyc.cloudapp.dialog.orderDialog.AbstractTransferDialog;
import com.wyc.cloudapp.dialog.orderDialog.HangBillDialog;
import com.wyc.cloudapp.dialog.orderDialog.NormalTransferDialog;
import com.wyc.cloudapp.dialog.orderDialog.QueryRetailOrderDialog;
import com.wyc.cloudapp.dialog.orderDialog.RefundDialog;
import com.wyc.cloudapp.dialog.pay.AbstractSettlementDialog;
import com.wyc.cloudapp.dialog.pay.NormalSettlementDialog;
import com.wyc.cloudapp.dialog.serialScales.AbstractWeightedScaleImp;
import com.wyc.cloudapp.dialog.vip.AbstractVipChargeDialog;
import com.wyc.cloudapp.dialog.vip.VipInfoDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.PrintUtilsToBitbmp;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.print.bean.PrinterStatus;
import com.wyc.cloudapp.print.printer.AbstractPrinter;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;

public final class NormalMainActivity extends SaleActivity implements CustomApplication.MessageCallback {
    private RecyclerView mSaleGoodsRecyclerView;
    private GoodsCategoryAdapter mGoodsCategoryAdapter;
    private GoodsInfoViewAdapter mGoodsInfoViewAdapter;
    private long mCurrentTimestamp = 0;
    private TextView mCurrentTimeViewTv, mSaleSumNumTv, mSaleSumAmtTv, mOrderCodeTv, mDisSumAmtTv;
    private ImageView mCloseBtn;
    private FlowLayout mKeyboard;
    private SecondDisplay mSecondDisplay;
    private ConstraintLayout mLastOrderInfo;
    private ImageView mPrinterStatusIv;
    private WeightInfoView mWeighView;
    private EditText mSearch_content;
    private CustomProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.normal_activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initMemberVariable();

        initNetworkStatus();

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

        //启动同步
        launchSync();

        checkUsbPermission();

    }
    private void initMemberVariable(){
        mCurrentTimeViewTv = findViewById(R.id.current_time);
        mSaleSumNumTv = findViewById(R.id.sale_sum_num);
        mSaleSumAmtTv = findViewById(R.id.sale_sum_amt);
        mKeyboard = findViewById(R.id.keyboard_layout);
        mOrderCodeTv = findViewById(R.id.order_code);
        mDisSumAmtTv = findViewById(R.id.dis_sum_amt);
    }

    private void showProgress(final String mess,boolean isCancel){
        if (mProgressDialog == null)mProgressDialog = new CustomProgressDialog(this);
        mProgressDialog.setMessage(mess).refreshMessage();
        mProgressDialog.setCancel(isCancel);
        mProgressDialog.show();
    }
    private void dismissProgress(){
        if (mProgressDialog != null){
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
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

    private void checkUsbPermission(){
        final JSONObject object = new JSONObject();
        if (SQLiteHelper.getLocalParameter("printer",object)){
            final String printer_info = Utils.getNullStringAsEmpty(object,"v");
            int status_id = object.getIntValue("id");
            final String[] vals = printer_info.split("\t");
            if (status_id == R.id.usb_p && vals.length > 1){
                final UsbManager manager = (UsbManager)getSystemService(Context.USB_SERVICE);
                if (null != manager){
                    HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
                    UsbDevice device = null;
                    boolean isExist = false;
                    for(String sz:deviceList.keySet()){
                        device = deviceList.get(sz);
                        if (null != device){
                            int id = device.getVendorId(),pid = device.getProductId();
                            vals[0] = vals[0].substring(vals[0].indexOf(":") + 1);
                            vals[1] = vals[1].substring(vals[1].indexOf(":") + 1);
                            if (String.valueOf(id).equals(vals[0]) && vals[1].equals(String.valueOf(pid))){
                                isExist = true;
                                break;
                            }
                        }
                    }
                    if (isExist && !manager.hasPermission(device)){
                        PendingIntent permissionIntent = PendingIntent.getBroadcast(mApplication, 0, new Intent(ACTION_USB_PERMISSION), 0);
                        manager.requestPermission(device, permissionIntent);
                    }
                }
            }
        }else
            MyDialog.ToastMessage("加载打印机参数错误：" + object.getString("info"), null);
    }

    private void launchSync(){
        mApplication.registerHandleMessage(this);
        mApplication.sync_order_info();
    }

    private void showCashierInfoAndStoreInfo(){
        final TextView cashier_name = findViewById(R.id.cashier_name),
                store_name = findViewById(R.id.store_name),
                pos_num = findViewById(R.id.pos_num);
        cashier_name.setText(getCashierName());
        pos_num.setText(getPosNum());
        store_name.setText(String.format("%s%s%s%s",getStoreName(),"[",getStoreId(),"]"));
    }
    private void initScaleView(){
        if (((NormalSaleGoodsAdapter)mSaleGoodsAdapter).hasAutoGetWeigh()){
            ((NormalSaleGoodsAdapter)mSaleGoodsAdapter).initScale();
            mWeighView = findViewById(R.id.weighView);
            mWeighView.setVisibility(View.VISIBLE);
            mWeighView.setAction(new WeightInfoView.OnAction() {
                @Override
                public void onZero() {
                    ((NormalSaleGoodsAdapter)mSaleGoodsAdapter).rZero();
                }

                @Override
                public void onTare() {
                    ((NormalSaleGoodsAdapter)mSaleGoodsAdapter).tare();
                }
            });
            mWeighView.updateInfo(AbstractWeightedScaleImp.OnReadStatus.STABLE,getWeigh(),Utils.getNotKeyAsNumberDefault(mSaleGoodsAdapter.getCurrentContent(),"price",0.000));
        }
    }

    private WindowManager.LayoutParams createPopupLayoutParams(final View anchor) {
        final WindowManager.LayoutParams p = new WindowManager.LayoutParams();
        p.packageName = anchor.getContext().getPackageName();
        p.gravity = Gravity.TOP|Gravity.START;
        p.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        p.type = WindowManager.LayoutParams.TYPE_APPLICATION_PANEL;
        p.token = anchor.getApplicationWindowToken();

        int[] location = new int[2];
        anchor.getLocationInWindow(location);
        p.x = location[0] + 8;
        p.y = location[1] + 8;

        p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED;
        p.format = PixelFormat.RGBA_8888;
        p.height = WindowManager.LayoutParams.WRAP_CONTENT;
        p.width = WindowManager.LayoutParams.WRAP_CONTENT;
        return p;
    }
    private void showLastOrderInfo() {
        final ConstraintLayout constraintLayout = (ConstraintLayout) View.inflate(this,R.layout.last_order_info,null);
        if (constraintLayout != null){
            final JSONObject order_info = new JSONObject();
            if (SQLiteHelper.execSql(order_info,"SELECT order_code,sum(pre_sale_money) pre_amt,sum(pay_money) pay_amt,sum(pre_sale_money - pay_money) zl_amt FROM " +
                    "retail_order_pays where order_code = '" + mOrderCodeTv.getText() +"' group by order_code")){

                final CountDownTimer countDownTimer = new CountDownTimer(30000,1000){
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }
                    @Override
                    public void onFinish() {
                        hideLastOrderInfo();
                    }
                };
                constraintLayout.setTag(countDownTimer);

                final TextView last_order_code = constraintLayout.findViewById(R.id.last_order_code),last_reality_amt = constraintLayout.findViewById(R.id.last_reality_amt),
                        last_rec_amt = constraintLayout.findViewById(R.id.last_rec_amt),last_zl = constraintLayout.findViewById(R.id.last_zl),close_tv = constraintLayout.findViewById(R.id.order_info_close_tv);

                final JumpTextView last_reprint_btn = constraintLayout.findViewById(R.id.last_reprint_btn);

                last_order_code.setText(order_info.getString("order_code"));
                last_rec_amt.setText(String.format(Locale.CHINA,"%.2f",order_info.getDoubleValue("pay_amt")));
                last_reality_amt.setText(String.format(Locale.CHINA,"%.2f",order_info.getDoubleValue("pre_amt")));
                last_zl.setText(String.format(Locale.CHINA,"%.2f",order_info.getDoubleValue("zl_amt")));

                close_tv.setOnClickListener(v ->{
                    hideLastOrderInfo();
                    countDownTimer.cancel();
                });
                last_reprint_btn.setOnClickListener(v -> AbstractSettlementDialog.printObj(last_order_code.getText().toString(),false));

                getWindowManager().addView(constraintLayout,createPopupLayoutParams(mSaleGoodsRecyclerView));
                countDownTimer.start();
            }else {
                MyDialog.ToastMessage(order_info.getString("info"), getWindow());
            }
        }
        mLastOrderInfo = constraintLayout;
    }

    private void hideLastOrderInfo(){
        if (mLastOrderInfo != null){
            ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).removeViewImmediate(mLastOrderInfo);
            final Object o = mLastOrderInfo.getTag();
            if (o instanceof CountDownTimer){
                ((CountDownTimer)o).cancel();
            }
            mLastOrderInfo.setTag(null);
            mLastOrderInfo = null;
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
        final Button minus_num_btn = findViewById(R.id.minus_num),add_num_btn = findViewById(R.id.add_num),discount_btn = findViewById(R.id.discount),
                change_price_btn = findViewById(R.id.change_price),check_out_btn = findViewById(R.id.check_out),
                vip_btn = findViewById(R.id.vip);

        final JumpTextView del = findViewById(R.id.del),num_btn = findViewById(R.id.num);

        if (null != del)del.setOnClickListener(v -> delSingleGoods());
        if (num_btn != null)num_btn.setOnClickListener(view -> alterGoodsNumber());//数量

        if (minus_num_btn != null)minus_num_btn.setOnClickListener(v -> minusOneGoods());//数量减
        if (add_num_btn != null)add_num_btn.setOnClickListener(v -> addOneSaleGoods());//数量加
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
            if (hasCheckDealOrderModule()){
                if (verifyQueryBtnPermissions()){
                    final QueryRetailOrderDialog queryRetailOrderDialog = new QueryRetailOrderDialog(this);
                    queryRetailOrderDialog.show();
                    queryRetailOrderDialog.triggerQuery();
                }
            }else q_deal_linerLayout.setVisibility(View.GONE);

        });//查交易
        if (other_linearLayout != null)other_linearLayout.setOnClickListener(v -> new MoreFunDialog(this,getString(R.string.more_fun_dialog_sz)).show());//更多功能
        if (cloud_background_layout != null)cloud_background_layout.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.hzyunpos.com/login")));
            }catch (ActivityNotFoundException e){
                MyDialog.ToastMessage("系统未安装浏览器!", getWindow());
            }
        });
    }
    private boolean hasCheckDealOrderModule(){
        return ModulePermission.checkModulePermission(26);
    }

    private void initMoreFunBtn(){
        final Button btn = findViewById(R.id.more_fun_btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PopupWindow window = new PopupWindow(NormalMainActivity.this);
                window.setContentView(View.inflate(NormalMainActivity.this,R.layout.more_fun_popup_window_layout,null));
                window.setOutsideTouchable(true);
                window.setBackgroundDrawable(ContextCompat.getDrawable(NormalMainActivity.this,R.color.transparent));

                runOnUiThread(() -> {
                    final View contentView = window.getContentView();
                    contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                    final int content_height = contentView.getMeasuredHeight();
                    int width = btn.getWidth();
                    int[] ints = new int[2];
                    btn.getLocationInWindow(ints);
                    window.showAsDropDown(btn,width, -content_height );

                    final InterceptLinearLayout interceptLinearLayout = contentView.findViewById(R.id.interceptLinearLayout);
                    if (interceptLinearLayout != null){
                        interceptLinearLayout.setClickListener(clickListener);
                    }
                });
            }

            private final View.OnClickListener clickListener = view -> {
                int id = view.getId();
                if (id == R.id.pop_present_btn){
                    present();
                }else if (id == R.id.pop_o_cashbox){
                    if (verifyOpenCashboxPermissions()){
                        AbstractPrinter.openCashDrawer();
                    }
                }else if (id == R.id.pop_sale_man_btn){
                    final JSONObject object = AbstractVipChargeDialog.showSaleInfo(NormalMainActivity.this);
                    final String name = Utils.getNullStringAsEmpty(object, TreeListBaseAdapter.COL_NAME);
                    mSaleManInfo = new JSONObject();
                    mSaleManInfo.put("id",Utils.getNullStringAsEmpty(object, TreeListBaseAdapter.COL_ID));
                    mSaleManInfo.put("name",name);
                    setSaleManView(name);
                }else if (id == R.id.pop_refund_btn){
                    if (RefundDialog.verifyRefundPermission(NormalMainActivity.this)){
                        setSingleRefundStatus(true);
                    }
                }else if (id == R.id.pop_time_card_btn){
                    NTimeCardBusiness.start(NormalMainActivity.this);
                }else if (id == R.id.goods_practice){
                    disposeGoodsPractice();
                }
            };
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
        if (shift_exchange_linearLayout != null){
            if (hasCheckShiftExchangeModule()){
                shift_exchange_linearLayout.setOnClickListener(v -> {
                    final NormalMainActivity activity = NormalMainActivity.this;
                    if (AbstractTransferDialog.verifyTransferPermissions(activity)){
                        final AbstractTransferDialog transferDialog = new NormalTransferDialog(activity);
                        transferDialog.verifyTransfer();
                    }
                });
            }else shift_exchange_linearLayout.setVisibility(View.GONE);
        }
    }

    private boolean hasCheckShiftExchangeModule(){
        return ModulePermission.checkModulePermission(6);
    }

    private void initClearBtn(){
        final JumpTextView clearBtn = findViewById(R.id.clear);
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
            if (mSaleGoodsAdapter.isEmpty()){
                MyDialog.displayAskMessage(this, "是否退出收银？", (MyDialog myDialog)->{
                    myDialog.dismiss();
                    this.finish();
                }, Dialog::dismiss);
            }else {
                MyDialog.toastMessage(getString(R.string.exist_goods_hint));
            }
        });
    }

    private void clearResource(){
        ((NormalSaleGoodsAdapter)mSaleGoodsAdapter).closeScale();
        hideLastOrderInfo();
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
                        retJson = httpRequest.setConnTimeOut(3000).setReadTimeOut(3000).sendPost(mUrl + "/api/cashier/get_time",HttpRequest.generate_request_parma(json, mAppSecret),true);
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
        mCurrentTimeViewTv.setText(new SimpleDateFormat(FormatDateTimeUtils.YYYY_MM_DD_1, Locale.CHINA).format(mCurrentTimestamp));
    }
    private void initGoodsInfoAdapter(){
        mGoodsInfoViewAdapter = new GoodsInfoViewAdapter(this);
        final IndicatorRecyclerView goods_info_view = findViewById(R.id.goods_info_list);
        final GridLayoutManager gridLayoutManager = new GridLayoutManager(this,GoodsInfoViewAdapter.SPAN_COUNT);
        goods_info_view.setLayoutManager(gridLayoutManager);
        mGoodsInfoViewAdapter.setOnGoodsSelectListener(this::addSaleGoods);
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(goods_info_view,getResources().getDimension(R.dimen.goods_height),new GridItemDecoration());
        goods_info_view.setAdapter(mGoodsInfoViewAdapter);
    }
    private void initGoodsCategoryAdapter(){
        final RecyclerView goods_type_view = findViewById(R.id.goods_type_list);
        mGoodsCategoryAdapter = new GoodsCategoryAdapter(this,findViewById(R.id.goods_sec_l_type_list));
        goods_type_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        goods_type_view.addItemDecoration(new LinearItemDecoration(getColor(R.color.lightBlue),1));
        mGoodsCategoryAdapter.setDatas(0);
        goods_type_view.setAdapter(mGoodsCategoryAdapter);
    }
    private void initSaleGoodsAdapter(){
        mSaleGoodsRecyclerView = findViewById(R.id.sale_goods_list);
        mSaleGoodsAdapter.setDataListener((total_num, total_sale_amt, total_discount_amt) -> {
            mSaleSumNumTv.setText(String.format(Locale.CANADA,"%.3f",total_num));
            mSaleSumAmtTv.setText(String.format(Locale.CANADA,"%.2f",total_sale_amt));
            mDisSumAmtTv.setText(String.format(Locale.CANADA,"%.2f",total_discount_amt));

            final int cur = mSaleGoodsAdapter.getCurrentItemIndex();
            mSaleGoodsRecyclerView.scrollToPosition(cur);
            final int offset = mSaleGoodsRecyclerView.computeVerticalScrollOffset();
            if ((mSaleGoodsRecyclerView.canScrollVertically(0) || mSaleGoodsRecyclerView.canScrollVertically(-1)) && offset != 0){
                mSaleGoodsRecyclerView.scrollBy(0,offset);
            }
            if (mSecondDisplay != null)mSecondDisplay.notifyChange(cur);
        });

        SuperItemDecoration.registerGlobalLayoutToRecyclerView(mSaleGoodsRecyclerView,getResources().getDimension(R.dimen.sale_goods_height),new SaleGoodsItemDecoration(getColor(R.color.gray_subtransparent)));
        mSaleGoodsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        mSaleGoodsRecyclerView.setAdapter(mSaleGoodsAdapter);
    }
    @Override
    public void setScaleInfo(int stat, float v){
        if (mWeighView != null)mWeighView.updateInfo(AbstractWeightedScaleImp.OnReadStatus.STABLE,getWeigh(),Utils.getNotKeyAsNumberDefault(mSaleGoodsAdapter.getCurrentContent(),"price",0.000));
    }
    @Override
    public void updateScalePrice(double price){
        if (mWeighView != null)mWeighView.updatePrice(price);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearch(){
        final EditText search = findViewById(R.id.search_content);
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
                        mKeyboard.setVisibility(View.GONE);
                    }else {
                        if (search.getSelectionStart() != search.getSelectionEnd()){
                            editable.replace(0,editable.length(),((Button)view).getText());
                            search.setSelection(editable.length());
                        }else
                            editable.append(((Button)view).getText());

                        if (editable.length() != 0){
                            mGoodsInfoViewAdapter.fuzzy_search_goods(editable.toString(),false);
                        }
                    }
                }
            };
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if (motionEvent.getX() > (search.getWidth() - search.getCompoundPaddingRight())){
                            FlowLayout keyboard = mKeyboard;
                            int visible = keyboard.getVisibility();
                            if (visible == View.VISIBLE ){
                                keyboard.setVisibility(View.GONE);
                            }else {
                                keyboard.setVisibility(View.VISIBLE);
                            }
                            search.selectAll();
                            View vObj;
                            for(int i = 0,childCounts = keyboard.getChildCount();i < childCounts;i ++){
                                vObj = keyboard.getChildAt(i);
                                if (keyboard.getVisibility() == View.VISIBLE){
                                    vObj.setOnClickListener(mKeyboardListener);
                                }else{
                                    vObj.setOnClickListener(null);
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

    @Override
    public boolean hookEnterKey() {
        CustomApplication.postAtFrontOfQueue(()->{
            final SaleActivity context = this;
            final String content = mSearch_content.getText().toString();
            if (content.length() == 0){
                mGoodsCategoryAdapter.trigger_preView();
            }else{
                if (!mGoodsInfoViewAdapter.fuzzy_search_goods(content,true)) {
                    CustomApplication.runInMainThread(()->{
                        if (mApplication.isConnection() && AddGoodsInfoDialog.verifyGoodsAddPermissions(context)) {
                            if (1 == MyDialog.showMessageToModalDialog(context,"未找到匹配商品，是否新增?")){
                                final AddGoodsInfoDialog addGoodsInfoDialog = new AddGoodsInfoDialog(context);
                                addGoodsInfoDialog.setBarcode(content);
                                addGoodsInfoDialog.setFinishListener(barcode -> {
                                    mGoodsInfoViewAdapter.fuzzy_search_goods(content,true);
                                    addGoodsInfoDialog.dismiss();
                                });
                                addGoodsInfoDialog.show();
                            }else mSearch_content.selectAll();
                        } else
                            MyDialog.ToastMessage("无此商品!", getWindow());
                    });
                }
            }
        });
        return true;
    }

    private void initTmpOrder(){
        final TmpOrderButton tmp_order = findViewById(R.id.tmp_order);
        final SaleActivity activity = this;
        tmp_order.setNum(HangBillDialog.getHangCounts(activity));
        tmp_order.setOnClickListener(v -> {
            if (isAdjustPriceMode()){
                MyDialog.ToastMessage(mSaleGoodsRecyclerView,"调价模式不允许挂单操作!", null);
            }else {
                final JSONArray datas = mSaleGoodsAdapter.getData();
                final HangBillDialog hangBillDialog = new HangBillDialog(activity);
                if (Utils.JsonIsNotEmpty(datas)){
                    final StringBuilder err = new StringBuilder();
                    if (hangBillDialog.save(datas,mVipInfo,err)){
                        tmp_order.setNum(HangBillDialog.getHangCounts(activity));
                        resetOrderInfo();
                        MyDialog.ToastMessage(mSaleGoodsRecyclerView,"挂单成功！", null);
                    }else{
                        MyDialog.ToastMessage(mSaleGoodsRecyclerView,"保存挂单错误：" + err, null);
                    }
                }else{
                    if (HangBillDialog.getHangCounts(activity) > 0){
                        hangBillDialog.setGetBillDetailListener((array, vip) -> {
                            hideLastOrderInfo();
                            if (null != vip)showVipInfo(vip);
                            JSONObject barcode_id_obj,goods_info;
                            final StringBuilder not_found_id = new StringBuilder();
                            for (int i = 0,length = array.size();i < length;i ++){
                                barcode_id_obj = array.getJSONObject(i);

                                if (GoodsInfoViewAdapter.isBuyXGiveX(barcode_id_obj))continue;//跳过存买X送X商品

                                if (barcode_id_obj != null){
                                    goods_info = new JSONObject();
                                    if (mGoodsInfoViewAdapter.getSingleSaleGoods(goods_info,barcode_id_obj.getString(GoodsInfoViewAdapter.W_G_MARK),mGoodsInfoViewAdapter.getGoodsId(barcode_id_obj))){
                                        goods_info.put("xnum",barcode_id_obj.getDoubleValue("xnum"));//挂单取出重量
                                        goods_info.put("goodsPractice",Utils.getNullObjectAsEmptyJsonArray(barcode_id_obj,"goodsPractice"));
                                        mSaleGoodsAdapter.addSaleGoods(goods_info);
                                    }else{
                                        if (goods_info.isEmpty()){
                                            if (not_found_id.length() != 0){
                                                not_found_id.append(",");
                                            }
                                            not_found_id.append(String.format(Locale.CHINA,"%s",Utils.getNullStringAsEmpty(barcode_id_obj,"goods_title")));
                                        }else {
                                            MyDialog.ToastMessage("选择商品错误：" + goods_info.getString("info"), null);
                                            return false;
                                        }
                                    }
                                }
                            }
                            boolean code = true;
                            if (not_found_id.length() != 0){
                                final JEventLoop loop = new JEventLoop();
                                final String hint = String.format(Locale.CHINA,"%s\n%s",CustomApplication.getStringByResId(R.string.not_found_goods, not_found_id.toString()
                                ),CustomApplication.getStringByResId(R.string.hang_tips));
                                MyDialog.displayAskMessage(this,hint, myDialog -> {
                                    myDialog.dismiss();
                                    loop.done(1);
                                }, myDialog -> {
                                    myDialog.dismiss();
                                    loop.done(0);
                                });
                                code = loop.exec() == 1;
                            }
                            if (code){
                                hangBillDialog.dismiss();
                                return true;
                            }else {
                                mSaleGoodsAdapter.clearGoods();
                                return false;
                            }
                        });
                        hangBillDialog.setOnDismissListener(dialog -> tmp_order.setNum(HangBillDialog.getHangCounts(activity)));
                        hangBillDialog.show();
                    }else{
                        MyDialog.ToastMessage(mSaleGoodsRecyclerView,"无挂单信息！", null);
                    }
                }
            }
        });
    }
    private void initPrintStatus(){//打印开关
        final ImageView imageView = findViewById(R.id.printer_status);
        imageView.setOnClickListener(v -> {
            final Bitmap printer = BitmapFactory.decodeResource(getResources(),R.drawable.printer);
            final PrinterStatus status = PrinterStatus.getPrinterStatus();
            int value = PrinterStatus.OPEN;
            if (status.isOpen()){
                value = PrinterStatus.CLOSE;
                imageView.setImageBitmap(PrintUtilsToBitbmp.drawErrorToBitmap(printer,(int) CustomApplication.getDimension(R.dimen.size_15),(int) CustomApplication.getDimension(R.dimen.size_15)));
                MyDialog.ToastMessage(imageView,CustomApplication.getStringByResId(R.string.print_close_hint), getWindow());
            }else if (status.isClose()){
                imageView.setImageBitmap(printer);
                MyDialog.ToastMessage(imageView,CustomApplication.getStringByResId(R.string.print_open_hint), getWindow());
            }else {
                value = PrinterStatus.ERROR;
                MyDialog.ToastMessage(imageView,status.getMsg(), getWindow());
            }
            PrinterStatus.savePrinterStatus(value,status.getMsg());
        });
        mPrinterStatusIv = imageView;
        showPrintStatus();
    }
    private void showPrintStatus(){
        final ImageView imageView = mPrinterStatusIv;
        if (null != imageView){
            final PrinterStatus printerStatus = PrinterStatus.getPrinterStatus();
            final Bitmap printer = BitmapFactory.decodeResource(getResources(),R.drawable.printer);
            if (printerStatus.isOpen()){
                imageView.setImageBitmap(printer);
            }else if (printerStatus.isClose()){
                imageView.setImageBitmap(Printer.drawPrintClose(printer));
            }else {
                imageView.setImageBitmap(Printer.drawPrintWarn(printer));
            }
        }
    }
    private void showPayDialog(){
        if (isAdjustPriceMode()){
            MyDialog.ToastMessage(mSaleGoodsRecyclerView,"调价模式不允许收款操作!", null);
        }else {
            if (!mSaleGoodsAdapter.isEmpty()){
                if (!getSingleRefundStatus()){
                    final AbstractSettlementDialog dialog = new NormalSettlementDialog(this,getString(R.string.affirm_pay_sz));
                    if (dialog.initPayContent()){
                        if (mVipInfo != null)dialog.setVipInfo(mVipInfo,true);
                        if (dialog.exec() == 1){
                            mApplication.sync_retail_order();
                            showLastOrderInfo();
                            resetOrderInfo();
                            MyDialog.SnackBarMessage(getWindow(),"结账成功！", mOrderCodeTv);
                        }else {
                            //取消之后重置订单号
                            resetOrderCode();
                        }
                    }
                }else {
                    final RefundDialog refundDialog = new RefundDialog(this,"");
                    refundDialog.show();
                }
            }else{
                MyDialog.SnackBarMessage(getWindow(),"已选商品为空！!",getCurrentFocus());
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
        mSaleGoodsAdapter.deleteVipDiscountRecord();
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
        mOrderCodeTv.setText(mSaleGoodsAdapter.generateOrderCode(getPosNum(),1));
    }
    private void initSecondDisplay(){
        mSecondDisplay = SecondDisplay.getInstantiate(this);
        if (null != mSecondDisplay){
            if (mApplication.isConnection())mSecondDisplay.loadAdImg(mUrl,mAppId, mAppSecret);
            mSecondDisplay.setDatas(mSaleGoodsAdapter.getData()).show();
        }
    }
    @Override
    public String getOrderCode(){ return mOrderCodeTv.getText().toString();}
    @Override
    public void loadGoods(final String id){
        if (mGoodsInfoViewAdapter != null)mGoodsInfoViewAdapter.loadGoodsByCategoryId(id);
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
    protected void addSaleGoods(final @NonNull JSONObject saleGoods){
        hideLastOrderInfo();
        mSaleGoodsAdapter.addSaleGoods(saleGoods);
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

    @Override
    public boolean findGoodsByBarcodeId(@NonNull final JSONObject out_goods,final String barcode_id){
        return mGoodsInfoViewAdapter.getSingleSaleGoods(out_goods,null,barcode_id);
    }

    @Override
    public void handleMessage(Handler handler, Message msg) {
        ImageView imageView;
        switch (msg.what){
            case MessageID.DIS_ERR_INFO_ID:
            case MessageID.SYNC_ERR_ID://资料同步错误
                dismissProgress();
                if (msg.obj != null)
                    MyDialog.displayErrorMessage(this, msg.obj.toString());
                break;
            case MessageID.SYNC_FINISH_ID:
                dismissProgress();
                break;
            case MessageID.GoodsCategory_CHANGE_ID:
                if (mGoodsCategoryAdapter != null)mGoodsCategoryAdapter.setDatas(0);
                break;
            case MessageID.TRANSFERSTATUS_ID://传输状态
                imageView = findViewById(R.id.upload_status);
                switch (msg.arg1){
                    case 2:
                        imageView.setImageResource(R.drawable.transfer);
                        break;
                    case 3:
                        imageView.setImageResource(R.drawable.transfer_err);
                        imageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_x));
                        break;
                    default:
                        if (imageView.getAnimation() == null){
                            imageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_y));
                        }
                }
                break;
            case MessageID.NETWORKSTATUS_ID://网络状态
                imageView = findViewById(R.id.network_status);
                if (imageView != null){
                    if ((boolean)msg.obj){
                        imageView.setImageResource(R.drawable.network);
                    }else{
                        imageView.setImageResource(R.drawable.network_err);
                        imageView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.shake_x));
                    }
                }
                break;
            case MessageID.SYNC_DIS_INFO_ID://资料同步进度信息
                if (msg.obj != null)
                    showProgress(msg.obj.toString(),false);
                break;
            case MessageID.PRINTER_ERROR:
                if (msg.obj instanceof String){
                    PrinterStatus.savePrinterStatus(PrinterStatus.ERROR,msg.obj.toString());
                    if (mPrinterStatusIv != null)mPrinterStatusIv.setImageBitmap(Printer.drawPrintWarn(null));
                }
                break;
            case MessageID.PRINTER_SUCCESS:
                if (mPrinterStatusIv != null){
                    PrinterStatus.savePrinterStatus(PrinterStatus.OPEN,"");
                    mPrinterStatusIv.setImageBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.printer));
                }
                break;
        }
    }
}
