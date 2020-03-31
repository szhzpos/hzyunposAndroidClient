package com.wyc.cloudapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
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

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.GoodsInfoItemDecoration;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.adapter.GoodsTypeViewAdapter;
import com.wyc.cloudapp.adapter.SaleGoodsItemDecoration;
import com.wyc.cloudapp.adapter.SaleGoodsViewAdapter;
import com.wyc.cloudapp.adapter.SuperItemDecoration;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.dialog.MoreFunDialog;
import com.wyc.cloudapp.dialog.PayDialog;
import com.wyc.cloudapp.dialog.VipInfoDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.network.sync.SyncManagement;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private SaleGoodsViewAdapter mSaleGoodsViewAdapter;
    private GoodsTypeViewAdapter mGoodsTypeViewAdapter;
    private GoodsInfoViewAdapter mGoodsInfoViewAdapter;
    private EditText mSearch_content;
    private JSONObject mCashierInfo,mStoreInfo,mVipInfo;
    private Myhandler mHandler;
    private CustomProgressDialog mProgressDialog;
    private MyDialog mDialog;
    private AtomicBoolean mNetworkStatus = new AtomicBoolean(true);//网络状态
    private AtomicBoolean mTransferStatus = new AtomicBoolean(true);//传输状态
    private long mCurrentTimestamp = 0;
    private String mAppId,mAppScret,mUrl;
    private TextView mCurrentTimeView,mSaleSumNum,mSaleSumAmount,mOrderCode,mDisSumAmt;
    private SyncManagement mSyncManagement;
    private ImageView mCloseBtn;
    private RecyclerView mSaleGoodsRecyclerView;
    private TableLayout mKeyboard;
    private String mRemark = "",zk_cashier_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //初始化成员变量
        mHandler = new Myhandler(this);
        mProgressDialog = new CustomProgressDialog(this);
        mDialog = new MyDialog(this);
        mSearch_content = findViewById(R.id.search_content);
        mCurrentTimeView = findViewById(R.id.current_time);
        mCloseBtn = findViewById(R.id.close);
        mSaleSumNum = findViewById(R.id.sale_sum_num);
        mSaleSumAmount = findViewById(R.id.sale_sum_amt);
        mKeyboard = findViewById(R.id.keyboard_layout);
        mOrderCode = findViewById(R.id.order_code);
        mDisSumAmt = findViewById(R.id.dis_sum_amt);

        //初始化adapter
        initGoodsInfoAdapter();
        initGoodsTypeAdapter();
        initSaleGoodsAdapter();

        //初始化收银员、仓库信息
        initCashierInfoAndStoreInfo();
        //更新当前时间
        startSyncCurrentTime();

        //初始化搜索框
        initSearch();

        //初始化功能按钮事件
        findViewById(R.id.clear).setOnClickListener(v -> {
            resetOrderInfo();
            mSaleGoodsViewAdapter.clearGoods();
        });//清空
        findViewById(R.id.minus_num).setOnClickListener(v -> mSaleGoodsViewAdapter.deleteSaleGoods(mSaleGoodsViewAdapter.getCurrentItemIndex(),1));//数量减
        findViewById(R.id.add_num).setOnClickListener(v -> mSaleGoodsViewAdapter.addSaleGoods(mSaleGoodsViewAdapter.getCurrentContent(),mVipInfo));//数量加
        mCloseBtn.setOnClickListener((View V)->{
            MyDialog.displayAskMessage(mDialog,"是否退出收银？",MainActivity.this,(MyDialog myDialog)->{
                myDialog.dismiss();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                MainActivity.this.finish();
            }, Dialog::dismiss);
        });//退出收银
        findViewById(R.id.num).setOnClickListener(view -> mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 0));//数量
        findViewById(R.id.discount).setOnClickListener(v-> {
            setDisCashierId(mCashierInfo.optString("cas_id"));
            mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 2);});//打折

        findViewById(R.id.change_price).setOnClickListener(v-> {
            setDisCashierId(mCashierInfo.optString("cas_id"));
            mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 1);});//改价
        findViewById(R.id.check_out).setOnClickListener((View v)->{
            v.setEnabled(false);
            showPayDialog();
            v.setEnabled(true);
        });//结账
        findViewById(R.id.vip).setOnClickListener(v -> {
            VipInfoDialog vipInfoDialog = new VipInfoDialog(v.getContext());
            vipInfoDialog.setYesOnclickListener(dialog -> {
                showVipInfo(dialog.getVip());
                dialog.dismiss();
            }).show();
        });//会员

        findViewById(R.id.q_deal_linerLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog.displayMessage(null,"查交易",v.getContext());
            }
        });
        findViewById(R.id.shift_exchange_linearLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog.displayMessage(null,"交班",v.getContext());
            }
        });
        findViewById(R.id.other_linearLayout).setOnClickListener(v -> {
            new MoreFunDialog(MainActivity.this).show();
        });

        //初始化数据管理对象
        mSyncManagement = new SyncManagement(mHandler,mUrl,mAppId,mAppScret,mStoreInfo.optString("stores_id"),mCashierInfo.optString("pos_num"),mCashierInfo.optString("cas_id"));
        mSyncManagement.start_sync(false);

        //重置订单信息
        resetOrderInfo();
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
        if (mHandler != null)mHandler.removeCallbacksAndMessages(null);
        if (mSyncManagement != null) {
            mSyncManagement.quit();
        }
        if (mProgressDialog.isShowing())mProgressDialog.dismiss();
        if (mDialog.isShowing())mDialog.dismiss();
        stopSyncCurrentTime();
    }

    @Override
    public void onBackPressed(){
        mCloseBtn.callOnClick();
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
                        retJson = httpRequest.setConnTimeOut(3000).setReadTimeOut(3000).sendPost(mUrl + "/api/cashier/get_time",HttpRequest.generate_request_parm(json,mAppScret),true);
                        switch (retJson.optInt("flag")) {
                            case 0:
                                mCurrentTimestamp = System.currentTimeMillis();
                                Logger.e("同步时间错误:%s",retJson.getString("info"));
                                break;
                            case 1:
                                info_json = new JSONObject(retJson.getString("info"));
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
        mCurrentTimeView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(mCurrentTimestamp));
    }
    private void stopSyncCurrentTime(){
        mHandler.removeCallbacksAndMessages(null);
    }

    private void initCashierInfoAndStoreInfo(){
        mCashierInfo = new JSONObject();
        mStoreInfo = new JSONObject();

        if (SQLiteHelper.getLocalParameter("cashierInfo",mCashierInfo)){
            TextView cashier_name = findViewById(R.id.cashier_name),
                    store_name = findViewById(R.id.store_name),
                    pos_num = findViewById(R.id.pos_num);

            cashier_name.setText(mCashierInfo.optString("cas_name"));
            pos_num.setText(mCashierInfo.optString("pos_num"));
            if (SQLiteHelper.getLocalParameter("connParam",mStoreInfo)){
                try {
                    mUrl = mStoreInfo.getString("server_url");
                    mAppId = mStoreInfo.getString("appId");
                    mAppScret = mStoreInfo.getString("appScret");

                    mStoreInfo = new JSONObject(mStoreInfo.getString("storeInfo"));
                    store_name.setText(String.format("%s%s%s%s",mStoreInfo.getString("stores_name"),"[",mStoreInfo.optString("stores_id"),"]"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    mDialog.setMessage(e.getMessage()).setNoOnclickListener("取消", myDialog -> MainActivity.this.finish()).show();
                }
            }else{
                mDialog.setMessage(mCashierInfo.optString("info")).setNoOnclickListener("取消", myDialog -> MainActivity.this.finish()).show();
            }
        }else{
            mDialog.setMessage(mStoreInfo.optString("info")).setNoOnclickListener("取消", myDialog -> MainActivity.this.finish()).show();
        }
    }

    private void initGoodsInfoAdapter(){
        mGoodsInfoViewAdapter = new GoodsInfoViewAdapter(this);
        final RecyclerView goods_info_view = findViewById(R.id.goods_info_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this,5);
        goods_info_view.setLayoutManager(gridLayoutManager);
        registerGlobalLayoutToRecyclerView(goods_info_view,getResources().getDimension(R.dimen.goods_height),new GoodsInfoItemDecoration());
        mGoodsInfoViewAdapter.setOnItemClickListener(new GoodsInfoViewAdapter.OnItemClickListener() {
            View mCurrentView;
            @Override
            public void onClick(View v, int pos) {
                set_selected_status(v);//设置选中状态
                JSONObject jsonObject = mGoodsInfoViewAdapter.getItem(pos),content = new JSONObject();
                if (jsonObject != null){
                    try {
                        int id = jsonObject.getInt("barcode_id");
                        if (-1 == id){//组合商品
                            id = jsonObject.getInt("gp_id");
                        }
                        if (mGoodsInfoViewAdapter.getSingleGoods(content,id)){
                            mSaleGoodsViewAdapter.addSaleGoods(content,mVipInfo);
                            mSearch_content.selectAll();
                        }else{
                            MyDialog.ToastMessage("选择商品错误：" + content.getString("info"),v.getContext(),null);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        MyDialog.ToastMessage("选择商品错误：" + e.getMessage(),v.getContext(),null);
                    }
                }
            }
            private void set_selected_status(View v){
                TextView goods_name;
                if(null != mCurrentView){
                    goods_name = mCurrentView.findViewById(R.id.goods_title);
                    goods_name.clearAnimation();
                    goods_name.setTextColor(MainActivity.this.getColor(R.color.good_name_color));
                }
                goods_name = v.findViewById(R.id.goods_title);
                goods_name.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_x));
                goods_name.setTextColor(MainActivity.this.getColor(R.color.blue));

                if (mCurrentView != v)mCurrentView = v;
            }
        });
        goods_info_view.setAdapter(mGoodsInfoViewAdapter);
    }

    private void initGoodsTypeAdapter(){
        mGoodsTypeViewAdapter = new GoodsTypeViewAdapter(this, mGoodsInfoViewAdapter);
        RecyclerView goods_type_view = findViewById(R.id.goods_type_list);
        goods_type_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        mGoodsTypeViewAdapter.setDatas();
        goods_type_view.setAdapter(mGoodsTypeViewAdapter);
    }

    private void initSaleGoodsAdapter(){
        mSaleGoodsRecyclerView = findViewById(R.id.sale_goods_list);
        mSaleGoodsViewAdapter = new SaleGoodsViewAdapter(this);
        mSaleGoodsViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged(){
                JSONArray datas = mSaleGoodsViewAdapter.getDatas();
                double sale_sum_num = 0.0,sale_sum_amount = 0.0,dis_sum_amt = 0.0;
                try {
                    for (int i = 0,length = datas.length();i < length;i ++){
                        JSONObject jsonObject = datas.getJSONObject(i);
                        sale_sum_num += jsonObject.getDouble("xnum");
                        sale_sum_amount += jsonObject.getDouble("sale_amt");
                        dis_sum_amt = jsonObject.getDouble("discount_amt");
                    }
                    mSaleSumNum.setText(String.format(Locale.CANADA,"%.4f",sale_sum_num));
                    mSaleSumAmount.setText(String.format(Locale.CANADA,"%.2f",sale_sum_amount));
                    mDisSumAmt.setText(String.format(Locale.CANADA,"%.2f",dis_sum_amt));

                    mSaleGoodsRecyclerView.scrollToPosition(mSaleGoodsViewAdapter.getCurrentItemIndex());
                } catch (JSONException e) {
                    e.printStackTrace();
                    MyDialog.ToastMessage("更新销售数据错误：" + e.getMessage(),MainActivity.this,null);
                }
            }
        });

        registerGlobalLayoutToRecyclerView(mSaleGoodsRecyclerView,getResources().getDimension(R.dimen.sale_goods_height),new SaleGoodsItemDecoration(getColor(R.color.gray__subtransparent)));

        mSaleGoodsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        mSaleGoodsRecyclerView.setAdapter(mSaleGoodsViewAdapter);
    }

    private void initSearch(){
        mSearch_content.setOnFocusChangeListener((v,b)->Utils.hideKeyBoard((EditText) v));
        mHandler.postDelayed(()-> mSearch_content.requestFocus(),100);
        mSearch_content.setSelectAllOnFocus(true);
        mSearch_content.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (mKeyboard.getVisibility() == View.VISIBLE){
                    if (editable.length() == 0){
                        mGoodsTypeViewAdapter.trigger_preView();
                    }else{
                        mGoodsInfoViewAdapter.fuzzy_search_goods(mSearch_content);
                    }
                }
            }
        });
        mSearch_content.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                if (mKeyboard.getVisibility() == View.GONE){
                    int keyCode = keyEvent.getKeyCode();
                    if (keyCode == KeyEvent.KEYCODE_ENTER){
                        String content = mSearch_content.getText().toString();
                        if (content.length() == 0){
                            mGoodsTypeViewAdapter.trigger_preView();
                        }else{
                            mGoodsInfoViewAdapter.fuzzy_search_goods(mSearch_content);
                            mSearch_content.selectAll();
                        }
                    }
                }
                return false;
            }
        });
        mSearch_content.setTransformationMethod(new ReplacementTransformationMethod() {
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
        mSearch_content.setOnTouchListener(new View.OnTouchListener() {
            private View.OnClickListener mKeyboardListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int v_id = view.getId();
                    Editable editable = mSearch_content.getText();
                    if (v_id == R.id.DEL){
                        editable.clear();
                    }else if (v_id == R.id.back){
                        if (editable.length() != 0)
                            editable.delete(editable.length() - 1,editable.length());
                    }else if(v_id == R.id.enter){
                        if (editable.length() == 0){
                            mGoodsTypeViewAdapter.trigger_preView();
                        }else{
                            mGoodsInfoViewAdapter.fuzzy_search_goods(mSearch_content);
                        }
                        mSearch_content.selectAll();
                    }else if(v_id == R.id.hide){
                        mKeyboard.setVisibility(View.GONE);
                    }else {
                        if (mSearch_content.getSelectionStart() != mSearch_content.getSelectionEnd()){
                            editable.replace(0,editable.length(),((Button)view).getText());
                            mSearch_content.setSelection(editable.length());
                        }else
                            editable.append(((Button)view).getText());
                    }
                }
            };
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        if (motionEvent.getX() > (mSearch_content.getWidth() - mSearch_content.getCompoundPaddingRight())){

                            mKeyboard.setVisibility(mKeyboard.getVisibility()== View.VISIBLE ? View.GONE : View.VISIBLE);
                            //registerGlobalLayoutToRecyclerView(findViewById(R.id.goods_info_list),MainActivity.this.getResources().getDimension(R.dimen.goods_height),new GoodsInfoItemDecoration());
                            mSearch_content.selectAll();
                            for(int i = 0,childCounts = mKeyboard.getChildCount();i < childCounts;i ++){
                                View vObj = mKeyboard.getChildAt(i);
                                if ( vObj instanceof TableRow){
                                    TableRow tableRow = (TableRow)vObj ;
                                    int buttons = tableRow.getChildCount();
                                    for (int j = 0;j < buttons;j ++){
                                        vObj = tableRow.getChildAt(j);
                                        if (vObj instanceof Button){
                                            final Button button = (Button)vObj;
                                            if (mKeyboard.getVisibility() == View.VISIBLE){
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
                        //barcode_text.performClick();
                        break;
                }
                return false;
            }
        });
    }

    private void showPayDialog(){
        final JSONArray datas = mSaleGoodsViewAdapter.getDatas();
        if (datas.length() != 0){
            final PayDialog dialog = new PayDialog(this);
            if (mVipInfo != null)dialog.showVipInfo(mVipInfo,true);
            if (dialog.initPayContent(datas)){
                dialog.setPayFinishListener(new PayDialog.onPayListener() {
                    @Override
                    public void onStart(PayDialog myDialog) {
                        try {
                            if (saveOrderInfo(generateOrderInfo(Utils.JsondeepCopy(datas),Utils.JsondeepCopy(myDialog.getContent())))){
                                dialog.requestPay(mOrderCode.getText().toString(),mUrl,mAppId,mAppScret,mStoreInfo.getString("stores_id"),mCashierInfo.getString("pos_num"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            MyDialog.ToastMessage("生成订单信息错误：" + e.getMessage(),myDialog.getContext(),null);
                        }
                    }

                    @Override
                    public void onProgress(PayDialog myDialog,final String info) {
                        if (mProgressDialog.isShowing()){
                            mProgressDialog.setMessage(info).refreshMessage();
                        }else{
                            mProgressDialog.setCancel(false).setMessage(info).refreshMessage().show();
                        }
                    }

                    @Override
                    public void onSuccess(PayDialog myDialog) {
                        if (mProgressDialog.isShowing())mProgressDialog.dismiss();
                        mSyncManagement.sync_order();
                        MyDialog.SnackbarMessage(MainActivity.this.getWindow(),"结账成功！",mOrderCode);
                        resetOrderInfo();
                        myDialog.dismiss();
                    }

                    @Override
                    public void onError(PayDialog myDialog, String err) {
                        if (mProgressDialog.isShowing())mProgressDialog.dismiss();
                        //myDialog.clearPayInfo();
                        resetOrderCode();//提示错误得重置单号
                        MyDialog.displayErrorMessage(null,"支付错误：" + err,myDialog.getContext());
                    }
                }).show();
            }
        }else{
            MyDialog.SnackbarMessage(getWindow(),"已选商品为空！!",getCurrentFocus());
        }
    }

    private void resetOrderInfo(){
        zk_cashier_id = mRemark  = "";
        resetOrderCode();
        mSaleGoodsViewAdapter.clearGoods();
        clearVipInfo();
    }
    private void clearVipInfo(){
        if (mVipInfo != null){
            mVipInfo = null;

            registerGlobalLayoutToRecyclerView(mSaleGoodsRecyclerView,getResources().getDimension(R.dimen.sale_goods_height),new SaleGoodsItemDecoration(getColor(R.color.gray__subtransparent)));

            LinearLayout vip_info_linearLayout = findViewById(R.id.vip_info_linearLayout);
            vip_info_linearLayout.setVisibility(View.GONE);
            ((TextView)vip_info_linearLayout.findViewById(R.id.vip_name)).setText(getText(R.string.space_sz));
            ((TextView)vip_info_linearLayout.findViewById(R.id.vip_phone_num)).setText(getText(R.string.space_sz));
        }
    }
    private void registerGlobalLayoutToRecyclerView(@NonNull final View view,final float size,@NonNull final SuperItemDecoration superItemDecoration){
        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int getVerSpacing(int viewHeight,int m_height){
                double vertical_space ,vertical_counts,per_vertical_space;
                vertical_space = viewHeight % m_height;
                vertical_counts = viewHeight / m_height;
                per_vertical_space = vertical_space / (vertical_counts != 0 ? vertical_counts:1);

                return (int) Utils.formatDouble(per_vertical_space,0);
            }
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = view.getMeasuredHeight();
                if (view instanceof RecyclerView){
                    RecyclerView recyclerView = ((RecyclerView)view);
                    if (recyclerView.getItemDecorationCount() > 0){
                        recyclerView.removeItemDecorationAt(0);
                    }
                    superItemDecoration.setSpace(getVerSpacing(height,(int) size));
                    recyclerView.addItemDecoration(superItemDecoration);
                }
            }
        });
    }
    private void setDisCashierId(final String id){
        zk_cashier_id = id;
    }
    private JSONObject generateOrderInfo(JSONArray sales_data,JSONArray pays_data) throws JSONException {
        double sale_sum_amt = Double.valueOf(mSaleSumAmount.getText().toString()),
                dis_sum_amt = Double.valueOf(mDisSumAmt.getText().toString()),
                total = dis_sum_amt + sale_sum_amt,zl_amt = 0.0;

        long time = System.currentTimeMillis() / 1000;

        JSONObject order_info = new JSONObject(),data = new JSONObject(),tmp_json;
        JSONArray orders = new JSONArray(),combination_goods = new JSONArray();
        StringBuilder err = new StringBuilder();

        String order_code = mOrderCode.getText().toString();

        order_info.put("stores_id",mStoreInfo.getString("stores_id"));
        order_info.put("order_code",order_code);
        order_info.put("total",total);
        order_info.put("discount_price",sale_sum_amt);
        order_info.put("discount_money",total);
        order_info.put("discount",String.format(Locale.CHINA,"%.4f",sale_sum_amt / total));
        order_info.put("cashier_id",mCashierInfo.getString("cas_id"));
        order_info.put("addtime",time);
        order_info.put("pos_code",mCashierInfo.getString("pos_num"));
        order_info.put("order_status",1);//订单状态（1未付款，2已付款，3已取消，4已退货）
        order_info.put("pay_status",1);//支付状态（1未支付，2已支付，3支付中）
        order_info.put("pay_time",time);
        order_info.put("upload_status",1);//上传状态（1未上传，2已上传）
        order_info.put("upload_time",0);
        order_info.put("transfer_status",1);//交班状态（1未交班，2已交班）
        order_info.put("transfer_time",0);
        order_info.put("is_rk",2);//是否已经扣减库存（1是，2否）
        if (mVipInfo != null){
            order_info.put("member_id",mVipInfo.getString("member_id"));
            order_info.put("mobile",mVipInfo.getString("mobile"));
            order_info.put("name",mVipInfo.getString("name"));
            order_info.put("card_code",mVipInfo.getString("card_code"));
        }
        order_info.put("sc_ids","");
        order_info.put("sc_tc_money",0.00);
        order_info.put("zl_money",zl_amt);
        order_info.put("ss_money",0.0);
        order_info.put("remark",mRemark);
        order_info.put("zk_cashier_id",zk_cashier_id);//使用折扣的收银员ID,默认当前收银员

        orders.put(order_info);

        //处理销售明细
        for(int i = 0;i < sales_data.length();i ++){
            tmp_json = sales_data.getJSONObject(i);
            int gp_id = tmp_json.getInt("gp_id");
            if (-1 != gp_id){
                tmp_json = (JSONObject) sales_data.remove(i--);
                if (!mSaleGoodsViewAdapter.splitCombinationalGoods(combination_goods,gp_id,tmp_json.getDouble("price"),tmp_json.getDouble("xnum"),err)){
                    MyDialog.displayErrorMessage(null,"拆分组合商品错误：" + err,this);
                    return null;
                }
                Logger.d_json(combination_goods.toString());
            }else{
                tmp_json.put("order_code",order_code);
                tmp_json.put("zk_cashier_id",zk_cashier_id);//使用折扣的收银员ID,默认当前收银员
                tmp_json.put("total_money",tmp_json.remove("sale_amt"));
                tmp_json.put("y_price",tmp_json.remove("old_price"));

                ///删除不需要的内容
                tmp_json.remove("goods_id");
                tmp_json.remove("discount");
                tmp_json.remove("discount_amt");
                tmp_json.remove("old_amt");
                tmp_json.remove("goods_title");
                tmp_json.remove("unit_name");
                tmp_json.remove("yh_mode");
                tmp_json.remove("yh_price");
            }
        }
        //处理组合商品
        for(int i = 0,size = combination_goods.length();i < size;i++){
            tmp_json = combination_goods.getJSONObject(i);
            tmp_json.put("order_code",order_code);
            tmp_json.put("zk_cashier_id",zk_cashier_id);//使用折扣的收银员ID,默认当前收银员
            tmp_json.put("total_money",String.format(Locale.CHINA,"%.2f",tmp_json.getDouble("xnum") * tmp_json.getDouble("price")));
            tmp_json.put("y_price",tmp_json.getDouble("retail_price"));

            sales_data.put(tmp_json);
        }

        //处理付款明细
        for (int i= 0,size = pays_data.length();i < size;i++){
            tmp_json = pays_data.getJSONObject(i);
            JSONObject pay = new JSONObject();

            pay.put("order_code",order_code);
            pay.put("pay_code",tmp_json.getString("pay_code"));
            pay.put("pay_method",tmp_json.getString("pay_method_id"));
            pay.put("pay_money",tmp_json.getDouble("pamt"));
            pay.put("is_check",tmp_json.getDouble("is_check"));
            pay.put("pay_time",0);
            pay.put("pay_status",1);
            pay.put("pay_serial_no","");//第三方返回的支付流水号
            pay.put("remark","");
            pay.put("zk_money",0.0);
            pay.put("pre_sale_money",tmp_json.getDouble("pamt"));
            pay.put("give_change_money",tmp_json.getDouble("pzl"));
            pay.put("discount_money",0.0);
            pay.put("xnote","");
            pay.put("return_code","");
            pay.put("v_num",tmp_json.getString("v_num"));
            pay.put("print_info","");

            pays_data.put(i,pay);
        }

        data.put("retail_order",orders);
        data.put("retail_order_goods",sales_data);
        data.put("retail_order_pays",pays_data);

        return data;
    }
    private boolean saveOrderInfo(JSONObject data){
        boolean code;
        StringBuilder err = new StringBuilder();
        JSONObject count_json = new JSONObject();
        List<String>  tables = Arrays.asList("retail_order","retail_order_goods","retail_order_pays"),
                retail_order_cols = Arrays.asList("stores_id","order_code","discount","discount_price","total","cashier_id","addtime","pos_code","order_status","pay_status","pay_time","upload_status",
                        "upload_time","transfer_status","transfer_time","is_rk","mobile","name","card_code","sc_ids","sc_tc_money","member_id","discount_money","zl_money","ss_money","remark","zk_cashier_id"),
                retail_order_goods_cols = Arrays.asList("order_code","barcode_id","xnum","price","buying_price","retail_price","trade_price","cost_price","ps_price","tax_rate","tc_mode","tc_rate","gp_id",
                        "zk_cashier_id","total_money","conversion","barcode","y_price"),
                retail_order_pays_cols = Arrays.asList("order_code","pay_method","pay_money","pay_time","pay_status","pay_serial_no","pay_code","remark","is_check","zk_money","pre_sale_money","give_change_money",
                        "discount_money","xnote","card_no","return_code","v_num","print_info");

        if (data == null)return false;
        if ((code = SQLiteHelper.execSql(count_json,"select count(order_code) counts from retail_order where order_code = '" + mOrderCode.getText() +"' and stores_id = '" + mStoreInfo.optString("stores_id") +"'"))){
            if (0 == count_json.optInt("counts")){
                if (!(code = SQLiteHelper.execSQLByBatchFromJson(data,tables,Arrays.asList(retail_order_cols,retail_order_goods_cols,retail_order_pays_cols),err,0))){
                    MyDialog.displayErrorMessage(null,"保存订单信息错误：" + err,this);
                }
            }else{
                code = false;
                MyDialog.displayErrorMessage(null,"本地已存在此订单信息，请重新下单！",this);
            }
        }else{
            MyDialog.displayErrorMessage(null,"查询订单信息错误：" + count_json.optString("info"),this);
        }
        return code;
    }
    private void resetOrderCode(){
        mOrderCode.setText(mGoodsInfoViewAdapter.generateOrderCode(mCashierInfo.optString("pos_num")));
    }
     public JSONArray discount(double discount,final String zk_cashier_id){
        if (null == zk_cashier_id || "".equals(zk_cashier_id)){
            setDisCashierId(mCashierInfo.optString("cas_id"));
        }else
            setDisCashierId(zk_cashier_id);
        return mSaleGoodsViewAdapter.discount(discount);
    }
    public void sync(boolean b){
        if (mSyncManagement != null){
            if (mProgressDialog != null && !mProgressDialog.isShowing())mProgressDialog.setMessage("正在同步...").refreshMessage().show();
            mSyncManagement.start_sync(b);
        }
    }
    public JSONArray showVipInfo(@NonNull JSONObject vip){
        mVipInfo = vip;

        registerGlobalLayoutToRecyclerView(mSaleGoodsRecyclerView,getResources().getDimension(R.dimen.sale_goods_height),new SaleGoodsItemDecoration(getColor(R.color.gray__subtransparent)));

        LinearLayout vip_info_linearLayout = findViewById(R.id.vip_info_linearLayout);
        vip_info_linearLayout.setVisibility(View.VISIBLE);
        ((TextView)vip_info_linearLayout.findViewById(R.id.vip_name)).setText(mVipInfo.optString("name"));
        ((TextView)vip_info_linearLayout.findViewById(R.id.vip_phone_num)).setText(mVipInfo.optString("mobile"));

        return  mSaleGoodsViewAdapter.updateGoodsInfoToVip(mVipInfo);
    }
    public void set_order_remark(final String remark){
        mRemark = mRemark.concat(remark);
    }
    public double getSaleSumAmt(){
        return mSaleGoodsViewAdapter.getSaleSumAmt();
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
                        if (activity.mNetworkStatus.getAndSet(code) != code){
                            imageView = activity.findViewById(R.id.network_status);
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
                case MessageID.DIS_PAY_DIALOG_ID:
                    break;
            }
        }
    }
}
