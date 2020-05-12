package com.wyc.cloudapp.dialog;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.OrderDetailsGoodsInfoAdapter;
import com.wyc.cloudapp.adapter.OrderDetailsPayInfoAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.pay.PayDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.util.ArrayList;
import java.util.List;

public class OrderDetaislDialog extends BaseDialog {
    private JSONObject mOrderInfo;
    private OrderDetailsGoodsInfoAdapter mOrderDetailsGoodsInfoAdapter;
    private OrderDetailsPayInfoAdapter mOrderDetailsPayInfoAdapter;
    private TextView mOrderCode;
    private CustomProgressDialog mProgressDialog;
    public OrderDetaislDialog(@NonNull MainActivity context, final String title, final JSONObject info) {
        super(context,title);
        mOrderInfo = info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.order_details_dialog_layout);

        showOrderInfo();
        initReprint();
    }

    private void showOrderInfo(){
        final JSONObject object = mOrderInfo;
        if (null != object){
            final TextView oper_time_tv = findViewById(R.id.oper_time),order_code_tv = mOrderCode = findViewById(R.id.order_code),order_amt_tv = findViewById(R.id.order_amt),reality_amt_tv = findViewById(R.id.reality_amt),
                    order_status_tv = findViewById(R.id.order_status),pay_status_tv = findViewById(R.id.pay_status),s_e_status_tv = findViewById(R.id.s_e_status),upload_status_tv = findViewById(R.id.upload_status),
                    cas_name_tv = findViewById(R.id.cas_name);
            if (oper_time_tv != null)oper_time_tv.setText(Utils.getNullStringAsEmpty(object,"oper_time"));
            if (order_code_tv != null)order_code_tv.setText(Utils.getNullStringAsEmpty(object,"order_code"));
            if (order_amt_tv != null)order_amt_tv.setText(Utils.getNullStringAsEmpty(object,"order_amt"));
            if (reality_amt_tv != null)reality_amt_tv.setText(Utils.getNullStringAsEmpty(object,"reality_amt"));
            if (order_status_tv != null)order_status_tv.setText(Utils.getNullStringAsEmpty(object,"order_status_name"));
            if (pay_status_tv != null)pay_status_tv.setText(Utils.getNullStringAsEmpty(object,"pay_status_name"));
            if (s_e_status_tv != null)s_e_status_tv.setText(Utils.getNullStringAsEmpty(object,"s_e_status_name"));
            if (upload_status_tv != null)upload_status_tv.setText(Utils.getNullStringAsEmpty(object,"upload_status_name"));
            if (cas_name_tv != null)cas_name_tv.setText(Utils.getNullStringAsEmpty(object,"cas_name"));

            final String sz_remark = Utils.getNullStringAsEmpty(object,"remark");
            if (!sz_remark.isEmpty()){
                final LinearLayout remark_layout = findViewById(R.id.remark_layout);
                if (remark_layout != null){
                    final TextView remark_tv = remark_layout.findViewById(R.id.remark);
                    if (null != remark_tv){
                        remark_layout.setVisibility(View.VISIBLE);
                        remark_tv.setText(sz_remark);
                    }
                }
            }
            final String sz_card_code = Utils.getNullStringAsEmpty(object,"card_code");
            if (!sz_card_code.isEmpty()){
                final LinearLayout vip_info_layout = findViewById(R.id.vip_info_layout);
                if (vip_info_layout != null){
                    vip_info_layout.setVisibility(View.VISIBLE);
                    final TextView card_cdoe_tv = vip_info_layout.findViewById(R.id.card_code),vip_name_tv = vip_info_layout.findViewById(R.id.vip_name),vip_mobile_tv = vip_info_layout.findViewById(R.id.vip_mobile);
                    if (null != card_cdoe_tv && vip_name_tv != null && vip_mobile_tv != null){
                        card_cdoe_tv.setText(Utils.getNullStringAsEmpty(object,"card_code"));
                        vip_name_tv.setText(Utils.getNullStringAsEmpty(object,"vip_name"));
                        vip_mobile_tv.setText(Utils.getNullStringAsEmpty(object,"mobile"));
                    }
                }
            }
            initGoodsDetail();
            initPayDetail();
        }
    }
    private void initGoodsDetail(){
            final RecyclerView goods_detail = findViewById(R.id.goods_details);
            if (null != goods_detail){
                mOrderDetailsGoodsInfoAdapter = new OrderDetailsGoodsInfoAdapter(mContext);
                goods_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
                goods_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
                goods_detail.setAdapter(mOrderDetailsGoodsInfoAdapter);
                mOrderDetailsGoodsInfoAdapter.setDatas(mOrderInfo.getString("order_code"));
            }
    }
    private void initPayDetail(){
        final RecyclerView pay_detail = findViewById(R.id.pay_details);
        if (null != pay_detail){
            mOrderDetailsPayInfoAdapter = new OrderDetailsPayInfoAdapter(mContext);
            pay_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            pay_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            pay_detail.setAdapter(mOrderDetailsPayInfoAdapter);
            mOrderDetailsPayInfoAdapter.setDatas(mOrderInfo.getString("order_code"));
            mOrderDetailsPayInfoAdapter.setItemClickListener(new OrderDetailsPayInfoAdapter.ItemClickCallBack() {
                @Override
                public void onClick(JSONObject pay_record) {
                    initVerifyPay(pay_record);
                }
            });
        }
    }
    private void initReprint(){
        final Button reprint_btn = findViewById(R.id.reprint_btn);
        if (null != reprint_btn){
            reprint_btn.setOnClickListener(v -> Printer.print(mContext,PayDialog.get_print_content(mContext,mOrderDetailsGoodsInfoAdapter.getSaleGoods(),mOrderDetailsPayInfoAdapter.getPayInfo(),false)));
        }
    }
    private void initVerifyPay(final JSONObject pay_record){
        final Button verify_pay_btn = findViewById(R.id.verify_pay_btn);
        if (null != verify_pay_btn){
            if (null != pay_record && 2 != pay_record.getIntValue("is_check")){
                verify_pay_btn.setVisibility(View.VISIBLE);
                verify_pay_btn.setOnClickListener(v -> {
                    mProgressDialog = new CustomProgressDialog(mContext);
                    mProgressDialog.setCancel(false).setMessage("正在查询支付结果...").refreshMessage().show();
                    CustomApplication.execute(()->verify_pay(pay_record));
                });
            }else{
                verify_pay_btn.setVisibility(View.GONE);
                verify_pay_btn.setOnClickListener(null);
            }
        }
    }

    private void verify_pay(final JSONObject pay_record){


        boolean query_status = false;
        final JSONObject object = new JSONObject();
        final HttpRequest httpRequest = new HttpRequest();
        final StringBuilder err = new StringBuilder();
        String unified_pay_query = Utils.getNullStringAsEmpty(pay_record,"unified_pay_query"),discount_xnote = "",third_pay_order_id = "",pay_code = Utils.getNullStringAsEmpty(pay_record,"pay_code");

        double discount_money = 0.0;
        long pay_time = 0;

        if (unified_pay_query.isEmpty()){
            unified_pay_query = "/api/pay2_query/query";
        }
        object.put("appid",mContext.getAppId());
        if (!pay_code.isEmpty())
            object.put("pay_code",pay_code);
        object.put("order_code_son",pay_record.getString("order_code_son"));

        final String sz_param = HttpRequest.generate_request_parm(object,mContext.getAppScret());
        final JSONObject retJson = httpRequest.sendPost(mContext.getUrl() + unified_pay_query,sz_param,true);
        switch (retJson.getIntValue("flag")){
            case 0:
                err.append(retJson.getString("info"));
                break;
            case 1:
                final JSONObject info_json = JSON.parseObject(retJson.getString("info"));
                Logger.json(info_json.toString());
                switch (info_json.getString("status")){
                    case "n":
                        err.append(info_json.getString("info"));
                        break;
                    case "y":
                        int res_code = info_json.getIntValue("res_code");
                        if (res_code == 1){//支付成功
                            query_status = true;
                            Logger.d_json(info_json.toString());
                            if (info_json.containsKey("xnote")){
                                discount_xnote = info_json.getString("xnote");
                            }
                            third_pay_order_id = info_json.getString("pay_code");
                            discount_money = info_json.getDoubleValue("discount");
                            pay_time = info_json.getLong("pay_time");
                        }
                        if (res_code == 2){//支付失败
                            err.append(info_json.getString("info"));
                        }
                        break;
                }
                break;
        }
        final ContentValues values = new ContentValues();
        final String sz_order_code = mOrderCode.getText().toString();
        if (!query_status){
            values.put("order_status",3);
            values.put("spare_param1",err.toString());
            if (!SQLiteHelper.execUpdateSql("retail_order",values,"order_code = ?",new String[]{sz_order_code},err)){
                Logger.d("更新订单状态错误：",err);
            }
        }else{
            final List<String> sqls = new ArrayList<>();
            int pay_method_id = pay_record.getIntValue("pay_method");
            String sql = "update retail_order set order_status = 2,pay_status = 2,pay_time ='" + pay_time +"' where order_code = '" + sz_order_code + "'";

            sqls.add(sql);

            sql = "update retail_order_pays set pay_status = 2,pay_serial_no = '" + third_pay_order_id +"',pay_time = '" + pay_time + "',discount_money = '" + discount_money +"',xnote = '" + discount_xnote +"',return_code = '"+ third_pay_order_id +"' " +
                    "where order_code = '" + sz_order_code + "' and pay_method = " + pay_method_id;

            sqls.add(sql);

            if (!SQLiteHelper.execBatchUpdateSql(sqls,err)){
                Logger.d("更新订单状态错误：",err);
            }else{
                mContext.runOnUiThread(()-> MyDialog.ToastMessage("支付成功！",mContext,getWindow()));
            }
        }
        if (mProgressDialog != null){
            mContext.runOnUiThread(()->mProgressDialog.dismiss());
        }
        if (err.length() != 0){
            mContext.runOnUiThread(()-> MyDialog.ToastMessage(err.toString(),mContext,getWindow()));
        }
    }

}
