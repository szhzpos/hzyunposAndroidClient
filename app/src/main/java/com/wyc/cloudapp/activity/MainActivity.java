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
import com.wyc.cloudapp.dialog.PayDialog;
import com.wyc.cloudapp.dialog.VipInfoDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.network.NetworkManagement;
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
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
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
    private TextView mCurrentTimeView,mSaleSumNum,mSaleSumAmount;
    private Timer mTimer;//更新当前时间计时器
    private NetworkManagement mNetworkManagement;
    private ImageView mCloseBtn;
    private RecyclerView mSaleGoodsRecyclerView;
    private TableLayout mKeyboard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            MyDialog.displayAskMessage("是否退出收银？",MainActivity.this,(MyDialog myDialog)->{
                myDialog.dismiss();
                mNetworkManagement.quit();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                MainActivity.this.finish();
                startActivity(intent);
            }, Dialog::dismiss);
        });//退出收银
        findViewById(R.id.num).setOnClickListener(view -> mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 0));//数量
        findViewById(R.id.discount).setOnClickListener(v-> mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 2));//打折
        findViewById(R.id.change_price).setOnClickListener(v-> mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 1));//改价
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
                MyDialog.displayMessage("查交易",v.getContext());
            }
        });
        findViewById(R.id.shift_exchange_linearLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog.displayMessage("交班",v.getContext());
            }
        });
        findViewById(R.id.other_linearLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyDialog.displayMessage("更多",v.getContext());
            }
        });

        //初始化数据管理对象
        mNetworkManagement = new NetworkManagement(mHandler,false,mUrl,mAppId,mAppScret,mStoreInfo.optString("stores_id"),mCashierInfo.optString("pos_num"),mCashierInfo.optString("cas_id"));
        mNetworkManagement.start_sync(false);

    }
    @Override
    public void onResume(){
        super.onResume();
        mHandler.postDelayed(()->{
            mSearch_content.requestFocus();
        },500);
    }
    @Override
    public void onPause(){
        super.onPause();
        mSearch_content.clearFocus();
    }
    @Override
    public void onDestroy(){
        super.onDestroy();
        if (mNetworkManagement != null){
            mNetworkManagement.quit();
        }
        stopSyncCurrentTime();
        if (mProgressDialog.isShowing())mProgressDialog.dismiss();
    }

    @Override
    public void onBackPressed(){
        mCloseBtn.callOnClick();
    }

    private void startSyncCurrentTime(){
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                final String prefix = "同步时间错误：";
                try {
                    if (mCurrentTimestamp == 0){
                        if (mNetworkStatus.get()){
                            HttpRequest httpRequest = new HttpRequest();
                            JSONObject json = new JSONObject(),retJson,info_json;
                            json.put("appid",mAppId);
                            retJson = httpRequest.setConnTimeOut(5000).setReadTimeOut(5000).sendPost(mUrl + "/api/cashier/get_time",HttpRequest.generate_request_parm(json,mAppScret),true);
                            switch (retJson.optInt("flag")) {
                                case 0:
                                    mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,prefix.concat(retJson.optString("info"))).sendToTarget();
                                    break;
                                case 1:
                                    info_json = new JSONObject(retJson.getString("info"));
                                    switch (info_json.getString("status")){
                                        case "n":
                                            mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,prefix + info_json.getString("info")).sendToTarget();
                                            break;
                                        case "y":
                                            mCurrentTimestamp = info_json.getLong("time");
                                            break;
                                    }
                                    break;
                            }
                        }else{
                            mCurrentTimestamp = System.currentTimeMillis()/1000;
                        }
                    }else{
                       mCurrentTimestamp += 1;
                    }
                    mHandler.obtainMessage(MessageID.UPDATE_TIME_ID).sendToTarget();
                } catch (JSONException e) {
                    mHandler.obtainMessage(MessageID.DIS_ERR_INFO_ID,prefix + e.getMessage()).sendToTarget();
                    e.printStackTrace();
                }
            }
        },0,1000);
    }
    private void stopSyncCurrentTime(){
        mTimer.cancel();
        mTimer = null;
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
                    if (activity.mProgressDialog != null && activity.mProgressDialog.isShowing())activity.mProgressDialog.dismiss();
                    if (msg.obj instanceof String)
                        MyDialog.displayErrorMessage(msg.obj.toString(),activity);
                    break;
                case MessageID.UPDATE_TIME_ID://更新当前时间
                    activity.mCurrentTimeView.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(activity.mCurrentTimestamp * 1000));
                    break;
                case MessageID.TRANSFERSTATUS_ID://传输状态
                    imageView = activity.findViewById(R.id.upload_status);
                    if (msg.obj instanceof Boolean){
                        boolean code = (boolean)msg.obj;
                        if (activity.mTransferStatus.getAndSet(code) != code){
                            if (imageView != null){
                                if (code){
                                    imageView.setImageResource(R.drawable.transfer);
                                }else{
                                    imageView.setImageResource(R.drawable.transfer_err);
                                    imageView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake));
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
                                    imageView.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.shake));
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }

    private void initGoodsInfoAdapter(){
        mGoodsInfoViewAdapter = new GoodsInfoViewAdapter(this);
        final RecyclerView goods_info_view = findViewById(R.id.goods_info_list);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(MainActivity.this,5);
        goods_info_view.setLayoutManager(gridLayoutManager);

        registerGlobalLayoutToRecyclerView(goods_info_view,getResources().getDimension(R.dimen.goods_height),new GoodsInfoItemDecoration());

        mGoodsInfoViewAdapter.setDatas(null);
        mGoodsInfoViewAdapter.setOnItemClickListener(new GoodsInfoViewAdapter.OnItemClickListener() {
            View mCurrentView;
            @Override
            public void onClick(View v, int pos) {
                set_selected_status(v);//设置选中状态
                JSONObject jsonObject = mGoodsInfoViewAdapter.getItem(pos),content = new JSONObject();
                if (jsonObject != null){
                    try {
                        if (mGoodsInfoViewAdapter.getSingleGoods(content,jsonObject.getString("goods_id"),jsonObject.getString("barcode_id"))){
                            mSaleGoodsViewAdapter.addSaleGoods(content,mVipInfo);
                            mSearch_content.selectAll();
                        }else{
                            MyDialog.ToastMessage("选择商品错误：" + content.getString("info"),v.getContext());
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        MyDialog.ToastMessage("选择商品错误：" + e.getMessage(),v.getContext());
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
                goods_name.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake));
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
                double sale_sum_num = 0.0,sale_sum_amount = 0.0;
                try {
                    for (int i = 0,length = datas.length();i < length;i ++){
                        JSONObject jsonObject = datas.getJSONObject(i);
                        sale_sum_num += jsonObject.getDouble("sale_num");
                        sale_sum_amount += jsonObject.getDouble("sale_amt");
                    }
                    mSaleSumNum.setText(String.format(Locale.CANADA,"%.4f",sale_sum_num));
                    mSaleSumAmount.setText(String.format(Locale.CANADA,"%.2f",sale_sum_amount));

                    mSaleGoodsRecyclerView.scrollToPosition(mSaleGoodsViewAdapter.getCurrentItemIndex());
                } catch (JSONException e) {
                    e.printStackTrace();
                    MyDialog.displayErrorMessage("更新销售数据错误：" + e.getMessage(),MainActivity.this);
                }
            }
        });

        registerGlobalLayoutToRecyclerView(mSaleGoodsRecyclerView,getResources().getDimension(R.dimen.sale_goods_height),new SaleGoodsItemDecoration(getColor(R.color.gray__subtransparent)));

        mSaleGoodsRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        mSaleGoodsRecyclerView.setAdapter(mSaleGoodsViewAdapter);
    }

    private void initSearch(){
        mSearch_content.setOnFocusChangeListener((View v, boolean hasFocus)->{
            Utils.hideKeyBoard((EditText) v);
        });
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
                        mGoodsInfoViewAdapter.fuzzy_search_goods(editable.toString());
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
                        mGoodsInfoViewAdapter.fuzzy_search_goods(mSearch_content.getText().toString());
                        mSearch_content.selectAll();
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
                            mGoodsInfoViewAdapter.fuzzy_search_goods(editable.toString());
                        }
                        mSearch_content.selectAll();
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
        JSONArray datas = mSaleGoodsViewAdapter.getDatas();
        if (datas.length() != 0){
            PayDialog dialog = new PayDialog(this);
            if (mVipInfo != null)dialog.showVipInfo(mVipInfo,true);
            if (dialog.initPayContent(datas)){
                dialog.setPayFinishListener(new PayDialog.onPayFinishListener() {
                    @Override
                    public void onClick(PayDialog myDialog) {
                        JSONArray sales = mSaleGoodsViewAdapter.getDatas(),
                                pays = myDialog.getContent();

                        Logger.d("sales:%s,pays:%s",sales.toString(),pays.toString());
                        myDialog.dismiss();
                    }
                }).show();
            }
        }else{
            MyDialog.ToastMessage(getWindow().getDecorView(),"已选商品为空！!",getCurrentFocus());
        }
    }

    private void resetOrderInfo(){
        clearVipInfo();
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

    public JSONArray discount(double discount){
        return mSaleGoodsViewAdapter.discount(discount);
    }

}
