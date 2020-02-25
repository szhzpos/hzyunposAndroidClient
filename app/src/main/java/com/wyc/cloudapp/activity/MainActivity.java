package com.wyc.cloudapp.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.SaleGoodsViewAdapter;
import com.wyc.cloudapp.adapter.SaleGoodsItemDecoration;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private SaleGoodsViewAdapter saleGoodsViewAdapter;
    private EditText search_content;
    private JSONObject mCashierInfo,mStoreInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initCashierInfoAndStoreInfo();//初始化收银员、仓库信息

        search_content = findViewById(R.id.search_content);

        search_content.setOnFocusChangeListener((View v,boolean hasFocus)->{
            Utils.hideKeyBoard((EditText) v);
        });

        findViewById(R.id.q_deal_linerLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.displayMessage("查交易",v.getContext(), Utils.ErrType.INFO);
            }
        });
        findViewById(R.id.shift_exchange_linearLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.displayMessage("交班",v.getContext(), Utils.ErrType.INFO);
            }
        });
        findViewById(R.id.other_linearLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.displayMessage("更多",v.getContext(), Utils.ErrType.INFO);
            }
        });
        findViewById(R.id.network_status).setOnClickListener((View v)->{
            ImageView imageView = (ImageView) v;
            imageView.setImageResource(R.drawable.network_err);
        });
        findViewById(R.id.keyboard).setOnClickListener((View v)->{
            TableLayout tableLayout = findViewById(R.id.keyboard_layout);
            tableLayout.setVisibility(tableLayout.getVisibility()== View.VISIBLE ? View.GONE : View.VISIBLE);
        });
        findViewById(R.id.close).setOnClickListener((View V)->{
            final Activity activity = MainActivity.this;
            MyDialog dialog = new MyDialog(activity);
            dialog.setMessage("是否退出收银？").setYesOnclickListener("是",(MyDialog mydialog)->{
                Intent intent = new Intent(activity,LoginActivity.class);
                startActivity(intent);
                activity.finish();
                mydialog.dismiss();
            }).setNoOnclickListener("否",(MyDialog mydialog)->{
                mydialog.dismiss();
            }).show();

        });

        RecyclerView recyclerView = findViewById(R.id.sale_goods_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        saleGoodsViewAdapter = new SaleGoodsViewAdapter(this);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_SETTLING){
                    //load_inventory_goods_details();
                }
            }
        });

        recyclerView.removeItemDecoration(recyclerView.getItemDecorationAt(0));
        recyclerView.addItemDecoration(new SaleGoodsItemDecoration());
        recyclerView.setAdapter(saleGoodsViewAdapter);

    }

    private void initCashierInfoAndStoreInfo(){
        mCashierInfo = new JSONObject();
        mStoreInfo = new JSONObject();

        if (SQLiteHelper.getLocalParameter("cashierInfo",mCashierInfo)){
            Logger.json(mCashierInfo.toString());
            TextView cashier_name = findViewById(R.id.cashier_name),
                    store_name = findViewById(R.id.store_name);

            cashier_name.setText(mCashierInfo.optString("cas_name"));
            if (SQLiteHelper.getLocalParameter("connParam",mStoreInfo)){
                try {
                    mStoreInfo = new JSONObject(mStoreInfo.getString("storeInfo"));
                    store_name.setText(String.format("%s%s%s%s",mStoreInfo.optString("stores_name"),"[",mStoreInfo.optString("stores_id"),"]"));
                } catch (JSONException e) {
                    Utils.displayMessage(e.getMessage(),getApplicationContext(),Utils.ErrType.ERROR);
                    e.printStackTrace();
                    this.finish();
                }
            }else{
                Utils.displayMessage(mCashierInfo.optString("info"),getApplicationContext(),Utils.ErrType.ERROR);
                this.finish();
            }
        }else{
            Utils.displayMessage(mStoreInfo.optString("info"),getApplicationContext(),Utils.ErrType.ERROR);
            this.finish();
        }
    }
}
