package com.wyc.cloudapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
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
import com.wyc.cloudapp.dialog.HangBillDialog;
import com.wyc.cloudapp.dialog.MoreFunDialog;
import com.wyc.cloudapp.dialog.QuerySaleOrderDialog;
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
    private AtomicBoolean mNetworkStatus = new AtomicBoolean(true);//网络状态
    private AtomicBoolean mTransferStatus = new AtomicBoolean(true);//传输状态
    private AtomicBoolean mPrintStatus = new AtomicBoolean(true);//打印状态
    private long mCurrentTimestamp = 0;
    private String mAppId,mAppScret,mUrl;
    private TextView mCurrentTimeView,mSaleSumNum,mSaleSumAmount,mOrderCode,mDisSumAmt;
    private SyncManagement mSyncManagement;
    private ImageView mCloseBtn;
    private RecyclerView mSaleGoodsRecyclerView;
    private TableLayout mKeyboard;
    private String mZkCashierId = "";
    private SecondDisplay mSecondDisplay;
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
        mSaleSumNum = findViewById(R.id.sale_sum_num);
        mSaleSumAmount = findViewById(R.id.sale_sum_amt);
        mKeyboard = findViewById(R.id.keyboard_layout);
        mOrderCode = findViewById(R.id.order_code);
        mDisSumAmt = findViewById(R.id.dis_sum_amt);

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

        //初始化数据管理对象
        mSyncManagement = new SyncManagement(mHandler,mUrl,mAppId,mAppScret,mStoreInfo.getString("stores_id"),mCashierInfo.getString("pos_num"),mCashierInfo.getString("cas_id"));
        mSyncManagement.sync_order();
        mSyncManagement.start_sync(false);

        //重置订单信息
        resetOrderInfo();

        //初始化副屏
        initSecondDisplay();
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
        if (null != mCloseBtn)
            mCloseBtn.callOnClick();
    }

    private void initFunctionBtn(){
        final Button minus_num_btn = findViewById(R.id.minus_num),add_num_btn = findViewById(R.id.add_num),num_btn = findViewById(R.id.num),
                discount_btn = findViewById(R.id.discount),change_price_btn = findViewById(R.id.change_price),check_out_btn = findViewById(R.id.check_out),
                vip_btn = findViewById(R.id.vip);

        if (minus_num_btn != null)minus_num_btn.setOnClickListener(v -> mSaleGoodsViewAdapter.deleteSaleGoods(mSaleGoodsViewAdapter.getCurrentItemIndex(),1));//数量减
        if (add_num_btn != null)add_num_btn.setOnClickListener(v -> mSaleGoodsViewAdapter.addSaleGoods(mSaleGoodsViewAdapter.getCurrentContent(),mVipInfo));//数量加
        if (num_btn != null)num_btn.setOnClickListener(view -> mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 0));//数量
        if (discount_btn != null)discount_btn.setOnClickListener(v-> {setDisCashierId(mCashierInfo.getString("cas_id"));mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 2);});//打折
        if (change_price_btn != null)change_price_btn.setOnClickListener(v-> { setDisCashierId(mCashierInfo.getString("cas_id"));mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 1);});//改价
        if (check_out_btn != null)check_out_btn.setOnClickListener((View v)->{Utils.disableView(v,500);showPayDialog();});//结账
        if (vip_btn != null)vip_btn.setOnClickListener(v -> {VipInfoDialog vipInfoDialog = new VipInfoDialog(this,getString(R.string.vip_info_sz));
            vipInfoDialog.setYesOnclickListener(dialog -> {showVipInfo(dialog.getVip());dialog.dismiss(); }).show();
        });//会员

        final LinearLayout q_deal_linerLayout = findViewById(R.id.q_deal_linerLayout),other_linearLayout = findViewById(R.id.other_linearLayout),
                shift_exchange_linearLayout = findViewById(R.id.shift_exchange_linearLayout);

        if (q_deal_linerLayout != null)
            q_deal_linerLayout.setOnClickListener(v -> {
                QuerySaleOrderDialog querySaleOrderDialog = new QuerySaleOrderDialog(MainActivity.this,getString(R.string.deal_sz));
                querySaleOrderDialog.show();;
            });//查交易

        if (other_linearLayout != null)
            other_linearLayout.setOnClickListener(v -> new MoreFunDialog(MainActivity.this,getString(R.string.more_fun_dialog_sz)).show());//更多功能
        if (shift_exchange_linearLayout != null)
            shift_exchange_linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog.displayMessage(null,"交班",v.getContext());
            }
        });
    }
    private void initClearBtn(){
        final Button clearBtn = findViewById(R.id.clear);
        if (null != clearBtn){
            clearBtn.setOnClickListener(v -> {
                if (mVipInfo != null){
                    MyDialog.displayAskMessage(mDialog,"是否清除会员折扣？",this,myDialog -> {
                        clearVipInfo();
                        myDialog.dismiss();
                    },Dialog::dismiss);
                }else{
                    if (!mSaleGoodsViewAdapter.getDatas().isEmpty())
                        MyDialog.displayAskMessage(mDialog,"是否清除销售商品？",this,myDialog -> {
                            resetOrderInfo();
                            myDialog.dismiss();
                        },Dialog::dismiss);
                }
            });
        }
    }
    private void initCloseMainWindow(){
        mCloseBtn = findViewById(R.id.close);
        mCloseBtn.setOnClickListener((View V)->{
            MyDialog.displayAskMessage(mDialog,"是否退出收银？",MainActivity.this,(MyDialog myDialog)->{
                myDialog.dismiss();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                MainActivity.this.finish();
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
                        retJson = httpRequest.setConnTimeOut(3000).setReadTimeOut(3000).sendPost(mUrl + "/api/cashier/get_time",HttpRequest.generate_request_parm(json,mAppScret),true);
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
        mCurrentTimeView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(mCurrentTimestamp));
    }
    private void initCashierInfoAndStoreInfo(){
        mCashierInfo = new JSONObject();
        mStoreInfo = new JSONObject();

        if (SQLiteHelper.getLocalParameter("cashierInfo",mCashierInfo)){
            TextView cashier_name = findViewById(R.id.cashier_name),
                    store_name = findViewById(R.id.sec_store_name),
                    pos_num = findViewById(R.id.pos_num);

            cashier_name.setText(mCashierInfo.getString("cas_name"));
            pos_num.setText(mCashierInfo.getString("pos_num"));
            if (SQLiteHelper.getLocalParameter("connParam",mStoreInfo)){
                try {
                    mUrl = mStoreInfo.getString("server_url");
                    mAppId = mStoreInfo.getString("appId");
                    mAppScret = mStoreInfo.getString("appScret");

                    mStoreInfo = JSON.parseObject(mStoreInfo.getString("storeInfo"));
                    store_name.setText(String.format("%s%s%s%s",mStoreInfo.getString("stores_name"),"[",mStoreInfo.getString("stores_id"),"]"));
                } catch (JSONException e) {
                    e.printStackTrace();
                    mDialog.setMessage(e.getMessage()).setNoOnclickListener("取消", myDialog -> MainActivity.this.finish()).show();
                }
            }else{
                mDialog.setMessage(mCashierInfo.getString("info")).setNoOnclickListener("取消", myDialog -> MainActivity.this.finish()).show();
            }
        }else{
            mDialog.setMessage(mStoreInfo.getString("info")).setNoOnclickListener("取消", myDialog -> MainActivity.this.finish()).show();
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
                Utils.disableView(v,300);
                set_selected_status(v);//设置选中状态
                final JSONObject jsonObject = mGoodsInfoViewAdapter.getItem(pos),content = new JSONObject();
                if (jsonObject != null){
                    final String weigh_barcode_info = jsonObject.getString(GoodsInfoViewAdapter.W_G_MARK);
                    int id = mGoodsInfoViewAdapter.getGoodsId(jsonObject);
                    if (mGoodsInfoViewAdapter.getSingleGoods(content,weigh_barcode_info,id)){
                        mSaleGoodsViewAdapter.addSaleGoods(content,mVipInfo);
                        mSearch_content.selectAll();
                    }else{
                        MyDialog.ToastMessage("选择商品错误：" + content.getString("info"),v.getContext(),null);
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
    private void initGoodsCategoryAdapter(){
        RecyclerView goods_type_view = findViewById(R.id.goods_type_list);
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
                JSONArray datas = mSaleGoodsViewAdapter.getDatas();
                double sale_sum_num = 0.0,sale_sum_amount = 0.0,dis_sum_amt = 0.0;
                try {
                    for (int i = 0,length = datas.size();i < length;i ++){
                        JSONObject jsonObject = datas.getJSONObject(i);
                        sale_sum_num += jsonObject.getDouble("xnum");
                        sale_sum_amount += jsonObject.getDouble("sale_amt");
                        dis_sum_amt += jsonObject.getDouble("discount_amt");
                    }
                    mSaleSumNum.setText(String.format(Locale.CANADA,"%.3f",sale_sum_num));
                    mSaleSumAmount.setText(String.format(Locale.CANADA,"%.2f",sale_sum_amount));
                    mDisSumAmt.setText(String.format(Locale.CANADA,"%.2f",dis_sum_amt));

                    mSaleGoodsRecyclerView.scrollToPosition(mSaleGoodsViewAdapter.getCurrentItemIndex());
                } catch (JSONException e) {
                    e.printStackTrace();
                    MyDialog.ToastMessage("更新销售数据错误：" + e.getMessage(),MainActivity.this,null);
                }
                if (mSecondDisplay != null)mSecondDisplay.notifyChange(mSaleGoodsViewAdapter.getCurrentItemIndex());
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
        mSearch_content.setOnKeyListener((view, i, keyEvent) -> {
            if (mKeyboard.getVisibility() == View.GONE){
                int keyCode = keyEvent.getKeyCode();
                if (keyCode == KeyEvent.KEYCODE_ENTER){
                    String content = mSearch_content.getText().toString();
                    if (content.length() == 0){
                        mGoodsCategoryViewAdapter.trigger_preView();
                    }else{
                        mGoodsInfoViewAdapter.fuzzy_search_goods(mSearch_content);
                        //mSearch_content.getText().clear();
                    }
                }
            }
            return false;
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
                            mGoodsCategoryViewAdapter.trigger_preView();
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
                        break;
                }
                return false;
            }
        });
    }
    private void initTmpOrder(){
        final Button tmp_order = findViewById(R.id.tmp_order);
        tmp_order.setOnClickListener(v -> {
            final MainActivity activity = MainActivity.this;
            final HangBillDialog hangBillDialog = new HangBillDialog(activity,activity.getString(R.string.hangbill_sz));
            JSONArray datas = mSaleGoodsViewAdapter.getDatas();
            if (Utils.JsonIsNotEmpty(datas)){
                MyDialog.displayAskMessage(null, "是否挂单？", activity, myDialog -> {
                    StringBuilder err = new StringBuilder();
                    if (hangBillDialog.save(datas,mVipInfo,err)){
                        resetOrderInfo();
                        MyDialog.ToastMessage(mSaleGoodsRecyclerView,"挂单成功！",activity,null);
                        myDialog.dismiss();
                    }else{
                        MyDialog.ToastMessage(mSaleGoodsRecyclerView,"保存挂单错误：" + err,activity,null);
                    }
                }, Dialog::dismiss);
            }else{
                if (hangBillDialog.getHangCounts() > 1){
                    hangBillDialog.setGetBillDetailListener(new HangBillDialog.OnGetBillListener() {
                        @Override
                        public void onGet(JSONArray array, final JSONObject vip) {
                            if (null != vip)showVipInfo(vip);
                            JSONObject barcode_id_obj,goods_info;
                            int id = -1;
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
                        }
                    });
                    hangBillDialog.show();
                }else{
                    MyDialog.ToastMessage(mSaleGoodsRecyclerView,"无挂单信息！",activity,null);
                }
            }

        });
    }
    private void initPrintStatus(){//打印状态
        findViewById(R.id.printer_status).setOnClickListener(v -> {
            ImageView imageView = (ImageView)v;
            Bitmap printer = BitmapFactory.decodeResource(getResources(),R.drawable.printer);
            if (mPrintStatus.get()){
                mPrintStatus.set(false);
                imageView.setImageBitmap(PrintUtilsToBitbmp.drawErrorSignToBitmap(printer,15,15));
                MyDialog.ToastMessage(imageView,"打印功能已关闭！",this,getWindow());
            }else{
                mPrintStatus.set(true);
                imageView.setImageBitmap(printer);
                MyDialog.ToastMessage(imageView,"打印功能已开启！",this,getWindow());
            }
        });
    }
    private void showPayDialog(){
        final JSONArray datas = getSaleData();
        if (datas.size() != 0){
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
                    public void onProgress(PayDialog myDialog,final String info) {
                        mProgressDialog.setMessage(info).refreshMessage();
                    }

                    @Override
                    public void onSuccess(PayDialog myDialog) {
                        if (mProgressDialog.isShowing())mProgressDialog.dismiss();

                        if (mPrintStatus.get())
                            Printer.print(activity,PayDialog.get_print_content(activity,mSaleGoodsViewAdapter.getDatas(),myDialog.getContent(),myDialog.isOpenCashbox()));

                        mSyncManagement.sync_order();
                        resetOrderInfo();
                        myDialog.dismiss();
                        MyDialog.SnackbarMessage(activity.getWindow(),"结账成功！",mOrderCode);
                    }

                    @Override
                    public void onError(PayDialog myDialog, String err) {
                        if (mProgressDialog.isShowing())mProgressDialog.dismiss();
                        resetOrderCode();//提示错误得重置单号
                        MyDialog.displayErrorMessage(null,"支付错误：" + err,myDialog.getContext());
                    }
                }).show();
                if (mVipInfo != null)dialog.showVipInfo(mVipInfo,true);
            }
        }else{
            MyDialog.SnackbarMessage(getWindow(),"已选商品为空！!",getCurrentFocus());
        }
    }
    private void resetOrderInfo(){
        mZkCashierId = "";
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
            if (!mSaleGoodsViewAdapter.getDatas().isEmpty()){
                mSaleGoodsViewAdapter.deleteVipDiscountRecord();
            }
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
        if (null == id || "".equals(id)){
            mZkCashierId = mCashierInfo.getString("cas_id");
        }else
            mZkCashierId = id;

    }
    private void resetOrderCode(){
        mOrderCode.setText(mSaleGoodsViewAdapter.generateOrderCode(mCashierInfo.getString("pos_num"),1));
    }
    private void initSecondDisplay(){
        mSecondDisplay = SecondDisplay.getInstantiate(this);
        if (null != mSecondDisplay){
            mSecondDisplay.loadAdImg(mUrl,mAppId,mAppScret);
            mSecondDisplay.setDatas(mSaleGoodsViewAdapter.getDatas()).setNavigationInfo(mStoreInfo).show();
        }
    }

    public void allDiscount(double v,final String zk_cashier_id){
        setDisCashierId(zk_cashier_id);
        mSaleGoodsViewAdapter.allDiscount(v);
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
    public void autoMol(double mol_amt,final String zk_cashier_id){
        setDisCashierId(zk_cashier_id);
        mSaleGoodsViewAdapter.autoMol(mol_amt);
    }
    public void manualMol(double mol_amt,final String zk_cashier_id){
        setDisCashierId(zk_cashier_id);
        mSaleGoodsViewAdapter.manualMol(mol_amt);
    }
    public void sync(boolean b){
        if (mSyncManagement != null){
            if (mProgressDialog != null && !mProgressDialog.isShowing())mProgressDialog.setMessage("正在同步...").refreshMessage().show();
            mSyncManagement.start_sync(b);
        }
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

        registerGlobalLayoutToRecyclerView(mSaleGoodsRecyclerView,getResources().getDimension(R.dimen.sale_goods_height),new SaleGoodsItemDecoration(getColor(R.color.gray__subtransparent)));

        LinearLayout vip_info_linearLayout = findViewById(R.id.vip_info_linearLayout);
        vip_info_linearLayout.setVisibility(View.VISIBLE);
        ((TextView)vip_info_linearLayout.findViewById(R.id.vip_name)).setText(mVipInfo.getString("name"));
        ((TextView)vip_info_linearLayout.findViewById(R.id.vip_phone_num)).setText(mVipInfo.getString("mobile"));

        mSaleGoodsViewAdapter.updateGoodsInfoToVip(mVipInfo);
    }
    public double getSaleSumAmt(){
        return mSaleGoodsViewAdapter.getSaleSumAmt();
    }
    public String getPosNum(){if (null == mCashierInfo)return "";return mCashierInfo.getString("pos_num");}
    public JSONObject getCashierInfo(){
        return mCashierInfo;
    }
    public JSONObject getStoreInfo(){
        return mStoreInfo;
    }
    public String getOrderCode(){ return mOrderCode.getText().toString();}
    public String  getDisCashierId(){
        return mZkCashierId;
    }
    public GoodsInfoViewAdapter getGoodsInfoViewAdapter(){
        return mGoodsInfoViewAdapter;
    }
    public String getAppId(){
        return mAppId;
    }
    public String getAppScret(){
        return mAppScret;
    }
    public String getUrl(){
        return mUrl;
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
            }
        }
    }
}
