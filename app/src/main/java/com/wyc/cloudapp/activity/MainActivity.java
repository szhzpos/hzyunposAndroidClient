package com.wyc.cloudapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
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
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.GoodsInfoItemDecoration;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.adapter.GoodsTypeViewAdapter;
import com.wyc.cloudapp.adapter.SaleGoodsItemDecoration;
import com.wyc.cloudapp.adapter.SaleGoodsViewAdapter;
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
    private JSONObject mCashierInfo,mStoreInfo;
    private Myhandler mHandler;
    private CustomProgressDialog mProgressDialog;
    private MyDialog mDialog;
    private AtomicBoolean mNetworkStatus = new AtomicBoolean(true);//网络状态
    private long mCurrentTimestamp = 0;
    private String mAppId,mAppScret,mUrl;
    private TextView mCurrentTimeView,mSaleSumNum,mSaleSumAmount;
    private Timer mTimer;//更新当前时间计时器
    private NetworkManagement mNetworkManagement;
    private ImageView mCloseBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化成员变量
        mHandler = new Myhandler(this);
        mProgressDialog = new CustomProgressDialog(this,R.style.CustomDialog);
        mDialog = new MyDialog(this);
        mSearch_content = findViewById(R.id.search_content);
        mCurrentTimeView = findViewById(R.id.current_time);
        mCloseBtn = findViewById(R.id.close);
        mSaleSumNum = findViewById(R.id.sale_sum_num);
        mSaleSumAmount = findViewById(R.id.sale_sum_amt);

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

        //初始化键盘
        initKeyboard();

        //初始化功能按钮事件
        findViewById(R.id.clear).setOnClickListener(v -> mSaleGoodsViewAdapter.clearGoods());//清空
        findViewById(R.id.minus_num).setOnClickListener(v -> mSaleGoodsViewAdapter.deleteSaleGoods(mSaleGoodsViewAdapter.getCurrentItemIndex(),1));//数量减
        findViewById(R.id.add_num).setOnClickListener(v -> mSaleGoodsViewAdapter.addSaleGoods(mSaleGoodsViewAdapter.getCurrentContent()));//数量加
        mCloseBtn.setOnClickListener((View V)->{
            MyDialog.displayAskMessage("是否退出收银？",MainActivity.this,(MyDialog myDialog)->{
                myDialog.dismiss();
                mNetworkManagement.stop_sync();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                MainActivity.this.finish();

            }, Dialog::dismiss);
        });//退出收银
        findViewById(R.id.num).setOnClickListener(view -> {
            mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 0);
        });//数量
        findViewById(R.id.discount).setOnClickListener(v->{
            mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 2);
        });//打折
        findViewById(R.id.change_price).setOnClickListener(v->{
            mSaleGoodsViewAdapter.updateSaleGoodsDialog((short) 1);
        });//改价
        findViewById(R.id.check_out).setOnClickListener(view -> mSaleGoodsViewAdapter.showPayDialog(Double.valueOf(mSaleSumAmount.getText().toString())));

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
        mNetworkManagement = new NetworkManagement(mHandler,mUrl,mAppId,mAppScret,mCashierInfo.optString("pos_num"),mCashierInfo.optString("cas_id"));
        mNetworkManagement.start_sync();

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
        mNetworkManagement.stop_sync();
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
                case MessageID.DOWNLOADSTATUS_ID://下载错误
                    break;
                case MessageID.NETWORKSTATUS_ID://网络状态
                    if (msg.obj instanceof Boolean){
                        boolean code = (boolean)msg.obj;
                        if (activity.mNetworkStatus.get() != code){
                            activity.mNetworkStatus.set(code);
                            ImageView imageView = activity.findViewById(R.id.network_status);
                            if (code){
                                imageView.setImageResource(R.drawable.network);
                            }else{
                                imageView.setImageResource(R.drawable.network_err);
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
        goods_info_view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int getVerSpacing(int viewHeight,int m_height){
                int vertical_space ,vertical_counts,per_vertical_space;
                vertical_space = viewHeight % m_height;
                vertical_counts = viewHeight / m_height;
                per_vertical_space = vertical_space / (vertical_counts != 0 ? vertical_counts:1);

                return per_vertical_space;
            }
            @Override
            public void onGlobalLayout() {
                goods_info_view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = goods_info_view.getMeasuredHeight();
                float itemHeight = MainActivity.this.getResources().getDimension(R.dimen.goods_height);
                goods_info_view.addItemDecoration(new GoodsInfoItemDecoration(getVerSpacing(height,(int) itemHeight)));
            }
        });
        mGoodsInfoViewAdapter.setDatas(null);
        mGoodsInfoViewAdapter.setOnItemClickListener(new GoodsInfoViewAdapter.OnItemClickListener() {
            View mPreName;
            @Override
            public void onClick(View v, int pos) {
                set_selected_status(v);//设置选中状态

                mSaleGoodsViewAdapter.addSaleGoods(mGoodsInfoViewAdapter.getItem(pos));
            }
            private void set_selected_status(View v){
                TextView goods_name;
                if(null != mPreName){
                    goods_name = mPreName.findViewById(R.id.goods_title);
                    goods_name.clearAnimation();
                    goods_name.setTextColor(MainActivity.this.getColor(R.color.good_name_color));
                }
                goods_name = v.findViewById(R.id.goods_title);
                Animation shake = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake);
                goods_name.startAnimation(shake);
                goods_name.setTextColor(MainActivity.this.getColor(R.color.blue));
                mPreName = v;
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
        final RecyclerView recyclerView = findViewById(R.id.sale_goods_list);
        mSaleGoodsViewAdapter = new SaleGoodsViewAdapter(this);
        mSaleGoodsViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged(){
                JSONArray datas = mSaleGoodsViewAdapter.getDatas();
                double sale_sum_num = 0.0,sale_sum_amount = 0.0;
                try {
                    for (int i = 0,length = datas.length();i < length;i ++){
                        JSONObject jsonObject = datas.getJSONObject(i);
                        sale_sum_num += jsonObject.getDouble("sale_sum_num");
                        sale_sum_amount += jsonObject.getDouble("sale_sum_amt");
                    }
                    mSaleSumNum.setText(String.format(Locale.CANADA,"%.4f",sale_sum_num));
                    mSaleSumAmount.setText(String.format(Locale.CANADA,"%.2f",sale_sum_amount));

                    recyclerView.scrollToPosition(mSaleGoodsViewAdapter.getCurrentItemIndex());
                } catch (JSONException e) {
                    e.printStackTrace();
                    MyDialog.displayErrorMessage("更新销售数据错误：" + e.getMessage(),MainActivity.this);
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int getVerSpacing(int viewHeight,int m_height){
                int vertical_space ,vertical_counts,per_vertical_space;
                vertical_space = viewHeight % m_height;
                vertical_counts = viewHeight / m_height;
                per_vertical_space = vertical_space / (vertical_counts != 0 ? vertical_counts:1);

                return per_vertical_space;
            }
            @Override
            public void onGlobalLayout() {
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = recyclerView.getMeasuredHeight();
                float itemHeight = MainActivity.this.getResources().getDimension(R.dimen.sale_goods_height);
                recyclerView.addItemDecoration(new SaleGoodsItemDecoration(getVerSpacing(height,(int) itemHeight)));
            }
        });
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mSaleGoodsViewAdapter);
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
                if (editable.length() != 0)
                    mGoodsInfoViewAdapter.search_goods(editable.toString());
                else
                    mGoodsTypeViewAdapter.trigger_preView();
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
    }

    private void initKeyboard(){
        findViewById(R.id.keyboard).setOnClickListener((new View.OnClickListener() {
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
                    }else
                        editable.append(((Button)view).getText());
                }
            };
            @Override
            public void onClick(View view) {
                final TableLayout tableLayout = findViewById(R.id.keyboard_layout);
                tableLayout.setVisibility(tableLayout.getVisibility()== View.VISIBLE ? View.GONE : View.VISIBLE);
                findViewById(R.id.goods_info_list).requestLayout();
                for(int i = 0,childCounts = tableLayout.getChildCount();i < childCounts;i ++){
                    View vObj = tableLayout.getChildAt(i);
                    if ( vObj instanceof TableRow){
                        TableRow tableRow = (TableRow)vObj ;
                        int buttons = tableRow.getChildCount();
                        for (int j = 0;j < buttons;j ++){
                            vObj = tableRow.getChildAt(j);
                            if (vObj instanceof Button){
                                final Button button = (Button)vObj;
                                if (tableLayout.getVisibility() == View.VISIBLE){
                                    button.setOnClickListener(mKeyboardListener);
                                }else{
                                    button.setOnClickListener(null);
                                }
                            }
                        }
                    }
                }
            }
        }));
    }



}
