package com.wyc.cloudapp.activity.mobile.report;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.mobile.AbstractMobileActivity;
import com.wyc.cloudapp.adapter.report.MobileGoodsDetailsAdapter;
import com.wyc.cloudapp.utils.Utils;
import java.text.SimpleDateFormat;
import java.util.Locale;

public final class OrderDetailsActivity extends AbstractMobileActivity {
    private View mCurrentDetailsView;
    private MobileGoodsDetailsAdapter mDetailsAdapter;
    private JSONArray saleDetails,payDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setMiddleText(getString(R.string.order_detail_sz));
        show_order_info();
        initDetailsTv();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_order_details;
    }

    private void show_order_info(){
        final Intent intent = getIntent();
        if (null != intent){
            try {
                final JSONObject object = JSONObject.parseObject(intent.getStringExtra("order_info"));

                show_order_info(object);
                final JSONArray pay_infos = object.getJSONArray("pay_info"),pays = new JSONArray();
                JSONObject obj,pay_obj;
                for (int i = 0,size = pay_infos.size();i < size;i++){
                    obj = pay_infos.getJSONObject(i);
                    pay_obj = new JSONObject();
                    pay_obj.put("goods_title",obj.getString("pay_method_name"));
                    pay_obj.put("price_xnum",obj.getString("pay_money"));
                    pays.add(pay_obj);
                }
                payDetails = pays;
                saleDetails = object.getJSONArray("goods_info");

                initOrderList(saleDetails);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this,e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void show_order_info(final JSONObject object){
        final TextView order_code_tv = findViewById(R.id.order_code_tv),stores_name_tv = findViewById(R.id.stores_name_tv),
                order_time_tv = findViewById(R.id.order_time_tv),order_amt_tv = findViewById(R.id.order_amt_tv),vip_card_id_tv = findViewById(R.id.vip_card_id_tv);

        order_code_tv.setText(object.getString("order_code"));
        stores_name_tv.setText(object.getString("stores_name"));
        order_time_tv.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(object.getLongValue("addtime") * 1000));
        order_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("discount_price")));

        final String member_card = Utils.getNullStringAsEmpty(object,"member_card");
        if (!"".equals(member_card))
            vip_card_id_tv.setText(member_card);
        else {
            final View parent = (View) vip_card_id_tv.getParent();
            if (null != parent)parent.setVisibility(View.GONE);
        }

    }

    private void initOrderList(final JSONArray datas){
        final RecyclerView recyclerView  = findViewById(R.id.goods_details_list);
        if (null != recyclerView){
            mDetailsAdapter = new MobileGoodsDetailsAdapter(this);
            mDetailsAdapter.setDatas(datas,0);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false));
            recyclerView.setAdapter(mDetailsAdapter);
        }
    }

    private void initDetailsTv(){
        final LinearLayout date_layout = findViewById(R.id.details_layout);
        View view;
        if (null != date_layout){
            for (int i = 0,counts = date_layout.getChildCount();i < counts; i++){
                view = date_layout.getChildAt(i);
                view.setOnClickListener(date_view_listener);
                if (view.getId() == R.id.sale_details)mCurrentDetailsView = view;
            }
        }
    }
    private final View.OnClickListener date_view_listener = view -> {
        if (mCurrentDetailsView != view) {
            if (mCurrentDetailsView != null) {
                ((TextView) mCurrentDetailsView).setTextColor(getColor(R.color.text_color));
                mCurrentDetailsView.setBackgroundColor(Color.WHITE);
            }
            mCurrentDetailsView = view;
            ((TextView) view).setTextColor(getColor(R.color.lightBlue));
            view.setBackground(getDrawable(R.drawable.mobile_report_date_btn_style));
        }
        if (mDetailsAdapter != null){
            switch (view.getId()){
                case R.id.sale_details:
                    mDetailsAdapter.setDatas(saleDetails,0);
                    break;
                case R.id.pay_details:
                    mDetailsAdapter.setDatas(payDetails,1);
                    break;
            }
        }
    };
}