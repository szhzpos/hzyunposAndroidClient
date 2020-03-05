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
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.adapter.GoodsTypeItemDecoration;
import com.wyc.cloudapp.adapter.GoodsTypeViewAdapter;
import com.wyc.cloudapp.adapter.SaleGoodsViewAdapter;
import com.wyc.cloudapp.adapter.SaleGoodsItemDecoration;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.network.NetworkManagement;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.MessageID;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {
    private SaleGoodsViewAdapter saleGoodsViewAdapter;
    private GoodsTypeViewAdapter goodsTypeViewAdapter;
    private GoodsInfoViewAdapter goodsInfoViewAdapter;
    private EditText search_content;
    private JSONObject mCashierInfo,mStoreInfo;
    private Myhandler mHandler;
    private CustomProgressDialog mProgressDialog;
    private MyDialog mDialog;
    private AtomicBoolean mNetworkStatus = new AtomicBoolean(true);//网络状态
    private long mCurrentTimestamp = 0;
    private String mAppId,mAppScret,mUrl;
    private TextView mCurrentTimeView;
    private Timer mTimer;//更新当前时间计时器
    private NetworkManagement mNetworkManagement;
    private ImageView mClose;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //初始化成员变量
        mHandler = new Myhandler(this);
        mProgressDialog = new CustomProgressDialog(this,R.style.CustomDialog);
        mDialog = new MyDialog(this);
        search_content = findViewById(R.id.search_content);
        mCurrentTimeView = findViewById(R.id.current_time);
        mClose = findViewById(R.id.close);

        //初始化收银员、仓库信息
        initCashierInfoAndStoreInfo();
        //更新当前时间
        startSyncCurrentTime();

        search_content.setOnFocusChangeListener((View v,boolean hasFocus)->{
            Utils.hideKeyBoard((EditText) v);
        });

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

        findViewById(R.id.keyboard).setOnClickListener((View v)->{
            TableLayout tableLayout = findViewById(R.id.keyboard_layout);
            tableLayout.setVisibility(tableLayout.getVisibility()== View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        mClose.setOnClickListener((View V)->{
            MyDialog.displayAskMessage("是否退出收银？",MainActivity.this,(MyDialog myDialog)->{
                myDialog.dismiss();
                mNetworkManagement.stop_sync();
                Intent intent = new Intent(MainActivity.this,LoginActivity.class);
                startActivity(intent);
                MainActivity.this.finish();

            }, Dialog::dismiss);
        });

        //初始化商品类别
        RecyclerView goods_type_view = findViewById(R.id.goods_type_list);
        goods_type_view.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false));
        goodsTypeViewAdapter = new GoodsTypeViewAdapter(this);
        goodsTypeViewAdapter.setDatas();
        goods_type_view.setAdapter(goodsTypeViewAdapter);

        //初始化商品信息
        final RecyclerView goods_info_view = findViewById(R.id.goods_info_list);
        //goods_info_view.setLayoutManager(new GridLayoutManager(this,5));
        goods_info_view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                goods_info_view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int width = goods_info_view.getMeasuredWidth();
                int height = goods_info_view.getMeasuredHeight();

                float itemWidth = MainActivity.this.getResources().getDimension(R.dimen.goods_width);
                float itemHeight = MainActivity.this.getResources().getDimension(R.dimen.goods_height);

                goods_info_view.setLayoutManager(new GridLayoutManager(MainActivity.this,(int) (width / itemWidth)));


                Logger.d("width:%d；height:%d",width,height);
            }
        });
        goodsInfoViewAdapter = new GoodsInfoViewAdapter(this);
        goodsInfoViewAdapter.setDatas();
        goods_info_view.setAdapter(goodsInfoViewAdapter);

        //初始化已选商品
        RecyclerView recyclerView = findViewById(R.id.sale_goods_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        saleGoodsViewAdapter = new SaleGoodsViewAdapter(this);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_SETTLING){

                }
            }
        });
        recyclerView.removeItemDecoration(recyclerView.getItemDecorationAt(0));
        recyclerView.addItemDecoration(new SaleGoodsItemDecoration());
        recyclerView.setAdapter(saleGoodsViewAdapter);

        //初始化数据管理对象
        mNetworkManagement = new NetworkManagement(mHandler,mUrl,mAppId,mAppScret,mCashierInfo.optString("pos_num"),mCashierInfo.optString("cas_id"));
        mNetworkManagement.start_sync();

    }
    @Override
    public void onResume(){
        super.onResume();
        //reSpacing(findViewById(R.id.goods_type_list),50,88);
    }
    @Override
    public void onPause(){
        super.onPause();
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
        mClose.callOnClick();
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
                    store_name = findViewById(R.id.store_name);

            cashier_name.setText(mCashierInfo.optString("cas_name"));
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
}
