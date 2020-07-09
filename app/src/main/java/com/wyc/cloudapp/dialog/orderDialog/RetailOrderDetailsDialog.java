package com.wyc.cloudapp.dialog.orderDialog;

import android.content.ContentValues;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.RetailDetailsGoodsInfoAdapter;
import com.wyc.cloudapp.adapter.RetailDetailsPayInfoAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractShowPrinterICODialog;
import com.wyc.cloudapp.dialog.pay.PayDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RetailOrderDetailsDialog extends AbstractShowPrinterICODialog {
    private JSONObject mOrderInfo, mPayRecord;
    private RetailDetailsPayInfoAdapter mRetailDetailsPayInfoAdapter;
    private String mRetailOrderCode;
    private CustomProgressDialog mProgressDialog;
    public RetailOrderDetailsDialog(@NonNull MainActivity context, final JSONObject info) {
        super(context,context.getString(R.string.order_detail_sz));
        mOrderInfo = info;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showOrderInfo();
        showDetails();
        initReprint();
        initVerifyPay();
    }

    @Override
    protected int getContentLayoutId(){
        return R.layout.retail_details_dialog_layout;
    }

    private void showOrderInfo(){
        final JSONObject object = mOrderInfo;
        if (null != object){
            final TextView oper_time_tv = findViewById(R.id.oper_time),order_code_tv = findViewById(R.id.order_code),order_amt_tv = findViewById(R.id.order_amt),reality_amt_tv = findViewById(R.id.reality_amt),
                    order_status_tv = findViewById(R.id.order_status),pay_status_tv = findViewById(R.id.pay_status),s_e_status_tv = findViewById(R.id.s_e_status),upload_status_tv = findViewById(R.id.upload_status),
                    cas_name_tv = findViewById(R.id.cas_name);
            if (oper_time_tv != null)oper_time_tv.setText(Utils.getNullStringAsEmpty(object,"oper_time"));

            if (order_code_tv != null)order_code_tv.setText(mRetailOrderCode = Utils.getNullStringAsEmpty(object,"order_code"));

            if (order_amt_tv != null)order_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("order_amt")));
            if (reality_amt_tv != null)reality_amt_tv.setText(String.format(Locale.CHINA,"%.2f",object.getDoubleValue("reality_amt")));
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
        }
    }

    private void showDetails(){
        initGoodsDetail();
        initPayDetail();
    }
    private void initGoodsDetail(){
            final RecyclerView goods_detail = findViewById(R.id.goods_details);
            if (null != goods_detail){
                final RetailDetailsGoodsInfoAdapter retailDetailsGoodsInfoAdapter = new RetailDetailsGoodsInfoAdapter(mContext);
                goods_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
                goods_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
                goods_detail.setAdapter(retailDetailsGoodsInfoAdapter);
                retailDetailsGoodsInfoAdapter.setDatas(Utils.getNullStringAsEmpty(mOrderInfo,"order_code"));
            }
    }
    private void initPayDetail(){
        final RecyclerView pay_detail = findViewById(R.id.pay_details);
        if (null != pay_detail){
            mRetailDetailsPayInfoAdapter = new RetailDetailsPayInfoAdapter(mContext);
            pay_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            pay_detail.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
            pay_detail.setAdapter(mRetailDetailsPayInfoAdapter);
            mRetailDetailsPayInfoAdapter.setDatas(Utils.getNullStringAsEmpty(mOrderInfo,"order_code"));
            mRetailDetailsPayInfoAdapter.setItemClickListener(pay_record -> mPayRecord = pay_record);
        }
    }
    private void initReprint(){
        final Button reprint_btn = findViewById(R.id.reprint_btn);
        if (null != reprint_btn){
            reprint_btn.setOnClickListener(v -> Printer.print(mContext, PayDialog.get_print_content(mContext, mRetailOrderCode,false)));
        }
    }
    private void initVerifyPay(){
        final Button verify_pay_btn = findViewById(R.id.verify_pay_btn);
        if (null != verify_pay_btn){
            verify_pay_btn.setOnClickListener(v -> {
                mProgressDialog = new CustomProgressDialog(mContext);
                mProgressDialog.setCancel(false).setMessage("正在查询支付结果...").refreshMessage().show();
                CustomApplication.execute(this::verify_pay);
            });
        }
    }

    private void verify_pay(){
        if (null != mPayRecord){
            final JSONObject pay_record = mPayRecord;
            boolean query_status = false;
            final JSONObject object = new JSONObject();
            final HttpRequest httpRequest = new HttpRequest();
            final StringBuilder err = new StringBuilder();
            String unified_pay_query,discount_xnote = "",third_pay_order_id = "";
            double discount_money = 0.0;
            long pay_time = 0;
            int pay_status = 1;
            final String pay_code = Utils.getNullStringAsEmpty(pay_record,"pay_code");
            if (2 == pay_record.getIntValue("is_check")){
                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CANADA);
                final TextView oper_time_tv = findViewById(R.id.oper_time) ;
                try {
                    final Date date = simpleDateFormat.parse(oper_time_tv.getText().toString());
                    if (date != null)
                        pay_time = date.getTime() / 1000; ;
                } catch (ParseException e) {
                    e.printStackTrace();
                    mContext.runOnUiThread(()-> MyDialog.ToastMessage("格式化时间错误：" + e.getLocalizedMessage(),mContext,getWindow()));
                }
                query_status = true;
                pay_status = 2;
                if (pay_time == 0){
                    pay_time = System.currentTimeMillis() / 1000;
                }
            }else{
                unified_pay_query = Utils.getNullStringAsEmpty(pay_record,"unified_pay_query");
                if (unified_pay_query.isEmpty()){
                    unified_pay_query = "/api/pay2_query/query";
                }
                object.put("appid",mContext.getAppId());
                if (!pay_code.isEmpty())
                    object.put("pay_code",pay_code);
                object.put("order_code_son", pay_record.getString("order_code_son"));

                final String sz_param = HttpRequest.generate_request_parm(object,mContext.getAppSecret());
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
                                pay_status = 1;
                                err.append(info_json.getString("info"));
                                break;
                            case "y":
                                int res_code = info_json.getIntValue("res_code");
                                if (res_code == 1 || res_code == 2){//支付成功
                                    query_status = true;
                                    Logger.d_json(info_json.toString());
                                    if (info_json.containsKey("xnote")){
                                        discount_xnote = info_json.getString("xnote");
                                    }
                                    third_pay_order_id = info_json.getString("pay_code");
                                    discount_money = info_json.getDoubleValue("discount");
                                    pay_time = info_json.getLong("pay_time");
                                    pay_status = info_json.getIntValue("pay_status");
                                }
                                if (res_code == 2){//支付失败
                                    err.append(info_json.getString("info"));
                                }
                                break;
                        }
                        break;
                }
            }

            final String sz_order_code = mRetailOrderCode;
            if (!query_status){
                final ContentValues values = new ContentValues();

                values.put("order_status",3);
                values.put("pay_status",pay_status);
                values.put("spare_param1",err.toString());
                final String whereClause = "order_code = ?";
                final String[] whereArgs = new String[]{sz_order_code};
                int rows = SQLiteHelper.execUpdateSql("retail_order",values,whereClause,whereArgs,err);
                if (rows < 0){
                    Logger.e("更新订单状态错误：%s",err);
                }else if (rows == 0){
                    Logger.i("未更新任何数据Table：%s,values:%s,whereClause:%s,whereArgs:%s","retail_order",values,whereClause, Arrays.toString(whereArgs));
                }
            }else{
                final List<String> tables = new ArrayList<>();
                final List<ContentValues> valueList = new ArrayList<>();
                final List<String> whereClauseList = new ArrayList<>();
                final List<String[]> whereArgsList = new ArrayList<>();

                final String pay_method_id = pay_record.getString("pay_method");
                final String order_code_son = pay_record.getString("order_code_son");

                tables.add("retail_order_pays");

                final ContentValues values_pays = new ContentValues();
                values_pays.put("pay_status",pay_status);
                values_pays.put("pay_serial_no",third_pay_order_id);
                values_pays.put("pay_time",pay_time);
                values_pays.put("discount_money",discount_money);
                values_pays.put("xnote",discount_xnote);
                values_pays.put("return_code",third_pay_order_id);
                valueList.add(values_pays);

                whereClauseList.add("order_code = ? and pay_code = ? and pay_method = ?");
                whereArgsList.add(new String[]{sz_order_code,order_code_son,pay_method_id});

                //更新当前付款记录
                pay_record.put("pay_status",pay_status);
                pay_record.put("pay_time",new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(pay_time * 1000));
                if (pay_status == 2){
                    pay_record.put("pay_status_name","已支付");
                }

                if (mRetailDetailsPayInfoAdapter.isPaySuccess()){//所有付款记录成功付款再更新单据为已支付
                    pay_status = 2;
                    mOrderInfo.put("pay_status_name","已支付");
                }

                tables.add("retail_order");

                final ContentValues values_order = new ContentValues();
                values_order.put("order_status",pay_status == 2 ? 2 : 1);
                values_order.put("pay_status",pay_status);
                values_order.put("pay_time",pay_time);
                valueList.add(values_order);

                whereClauseList.add("order_code = ?");
                whereArgsList.add(new String[]{sz_order_code});

                int[] rows = SQLiteHelper.execBatchUpdateSql(tables,valueList,whereClauseList,whereArgsList,err);

                if (rows == null){
                    Logger.e("更新订单状态错误：%s",err);
                }else {
                    int index = SQLiteHelper.verifyUpdateResult(rows);
                    mContext.runOnUiThread(()->{
                        mRetailDetailsPayInfoAdapter.notifyDataSetChanged();
                        if (index == -1){
                            if (err.length() == 0)err.append("支付成功！");
                        } else{
                            final String sz_err = String.format(Locale.CHINA,"数据表%s未更新，value:%s,whereClause:%s,whereArgs:%s",tables.get(index),valueList.get(index),
                                    whereClauseList.get(index),Arrays.toString(whereArgsList.get(index)));
                            Logger.e(sz_err);
                            err.append(sz_err);
                        }
                    });
                    mContext.runOnUiThread(this::showOrderInfo);
                }
            }
            mContext.runOnUiThread(()-> MyDialog.ToastMessage(err.toString(),mContext,getWindow()));
        }else {
            mContext.runOnUiThread(()-> MyDialog.ToastMessage("请选择验证记录！",mContext,getWindow()));
        }
        if (mProgressDialog != null){
            mContext.runOnUiThread(()->mProgressDialog.dismiss());
        }
    }
}
