package com.wyc.cloudapp.dialog.orderDialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.method.ReplacementTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.MobileRetailDetailsGoodsInfoAdapter;
import com.wyc.cloudapp.adapter.RetailDetailsPayInfoAdapter;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.TreeListDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.utils.Utils;

public class MobileRetailOrderDetailsDialog extends AbstractDialogMainActivity {
    private JSONObject mOrderInfo;
    private JSONArray mOrderList;
    private EditText mSearchContent;
    private MobileRetailDetailsGoodsInfoAdapter mRetailDetailsGoodsInfoAdapter;
    private RetailDetailsPayInfoAdapter mRetailDetailsPayInfoAdapter;
    public MobileRetailOrderDetailsDialog(@NonNull MainActivity context) {
        super(context, context.getString(R.string.m_sale_query_sz));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initSearchContent();
        initGoodsDetail();
        initPayDetail();
        initSwitchCondition();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.mobile_retail_details_dialog_layout;
    }

    @Override
    protected void initWindowSize(){
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initSearchContent(){
        final EditText order_vip__search = findViewById(R.id.order_vip__search);
        order_vip__search.setTransformationMethod(new ReplacementTransformationMethod() {
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
        order_vip__search.setOnKeyListener((v, keyCode, event) -> {
            if ((keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_NUMPAD_ENTER) && event.getAction() == KeyEvent.ACTION_UP){
                query();
                return true;
            }
            return false;
        });
        order_vip__search.setOnTouchListener((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                final float dx = motionEvent.getX();
                final int w = order_vip__search.getWidth();
                if (dx > (w - order_vip__search.getCompoundPaddingRight())) {
                    query();
                }
            }
            return false;
        });
        mSearchContent = order_vip__search;
    }

    private void initSwitchCondition(){
        final TextView switch_condition = findViewById(R.id.switch_condition);
        final JSONArray array = createSwitchConditionContentAndSetDefaultValue(switch_condition);
        switch_condition.setOnClickListener(v -> {
            final String pay_method_name_colon_sz = mContext.getString(R.string.pay_method_name_colon_sz);
            final TreeListDialog treeListDialog = new TreeListDialog(mContext,pay_method_name_colon_sz.substring(0,pay_method_name_colon_sz.length() - 1));
            treeListDialog.setDatas(array,null,true);
            switch_condition.post(()->{
                if (treeListDialog.exec() == 1){
                    final JSONObject object = treeListDialog.getSingleContent();
                    switch_condition.setTag(object.getIntValue("item_id"));
                    switch_condition.setText(object.getString("item_name"));
                }
            });
        });
    }
    private JSONArray createSwitchConditionContentAndSetDefaultValue(@NonNull final TextView view){
        final JSONArray array = new JSONArray();
        final String search_hint = mContext.getString(R.string.m_search_hint);
        if (search_hint != null){
            final String[] sz = search_hint.split("/");
            if (sz != null && sz.length > 1){
                JSONObject object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put("item_id",1);
                object.put("item_name",sz[0]);

                array.add(object);

                view.setTag(1);
                view.setText(sz[0]);

                object = new JSONObject();
                object.put("level",0);
                object.put("unfold",false);
                object.put("isSel",false);
                object.put("item_id",2);
                object.put("item_name",sz[1]);

                array.add(object);
            }
        }
        return array;
    }

    private void initGoodsDetail(){
        final RecyclerView goods_detail = findViewById(R.id.m_order_details_list);
        if (null != goods_detail){
            final MobileRetailDetailsGoodsInfoAdapter retailDetailsGoodsInfoAdapter = new MobileRetailDetailsGoodsInfoAdapter(mContext);
            goods_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            goods_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            goods_detail.setAdapter(retailDetailsGoodsInfoAdapter);
            retailDetailsGoodsInfoAdapter.setDatas(Utils.getNullStringAsEmpty(mOrderInfo,"order_code"));
            mRetailDetailsGoodsInfoAdapter = retailDetailsGoodsInfoAdapter;
        }
    }
    private void initPayDetail(){
        final RecyclerView pay_detail = findViewById(R.id.m_pay_details_list);
        if (null != pay_detail){
            final RetailDetailsPayInfoAdapter retailDetailsPayInfoAdapter = new RetailDetailsPayInfoAdapter(mContext);
            pay_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            pay_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            pay_detail.setAdapter(retailDetailsPayInfoAdapter);
            retailDetailsPayInfoAdapter.setDatas(Utils.getNullStringAsEmpty(mOrderInfo,"order_code"));

            mRetailDetailsPayInfoAdapter = retailDetailsPayInfoAdapter;
        }
    }

    private void query(){
        if (mSearchContent != null){
            final String content = mSearchContent.getText().toString(),where_sql;
            if (!content.isEmpty()){
                if (Utils.getViewTagValue(findViewById(R.id.switch_condition),1) == 1){
                    where_sql = " order_code ='"+ content +"'";
                }else {
                    where_sql = " card_code ='"+ content +"'";
                }
            }else
                where_sql = "";

            final String sql = "SELECT \n" +
                    "       a.remark," +
                    "       a.card_code," +
                    "       a.name vip_name," +
                    "       a.mobile," +
                    "       a.transfer_status s_e_status,\n" +
                    "       case a.transfer_status when 1 then '未交班' when 2 then '已交班' else '其他' end s_e_status_name,\n" +
                    "       a.upload_status,\n" +
                    "       case a.upload_status when 1 then '未上传' when 2 then '已上传' else '其他' end upload_status_name,\n" +
                    "       a.pay_status,\n" +
                    "       case a.pay_status when 1 then '未支付' when 2 then '已支付' when 3 then '支付中' else '其他' end pay_status_name,\n" +
                    "       a.order_status,\n" +
                    "       case a.order_status when 1 then '未付款' when 2 then '已付款' when 3 then '已取消' when 4 then '已退货' else '其他'  end order_status_name,\n" +
                    "       datetime(a.addtime, 'unixepoch', 'localtime') oper_time,\n" +
                    "       a.remark,\n" +
                    "       a.cashier_id,\n" +
                    "       b.cas_name,\n" +
                    "       a.discount_price reality_amt,\n" +
                    "       a.total order_amt,\n" +
                    "       a.order_code,\n" +
                    "       c.sc_name\n" +
                    "  FROM retail_order a left join cashier_info b on a.cashier_id = b.cas_id left join sales_info c on a.sc_ids = c.sc_id " + where_sql;

            final StringBuilder err = new StringBuilder();
            mOrderList = SQLiteHelper.getListToJson(sql,err);
            if (mOrderList != null){
                mOrderInfo = mOrderList.getJSONObject(0);
                refresh(Utils.getNullStringAsEmpty(mOrderInfo,"order_code"));
            }else {
                MyDialog.ToastMessage(err.toString(),mContext,getWindow());
            }
        }
    }

    private void refresh(final String code){
        mRetailDetailsGoodsInfoAdapter.setDatas(code);
        mRetailDetailsPayInfoAdapter.setDatas(code);

        mRetailDetailsGoodsInfoAdapter.notifyDataSetChanged();
        mRetailDetailsPayInfoAdapter.notifyDataSetChanged();
    }
}
