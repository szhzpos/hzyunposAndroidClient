package com.wyc.cloudapp.dialog;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.SaleReturnGoodsInfoAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class SaleReturnDialog extends BaseDialog {
    private SaleReturnGoodsInfoAdapter mSaleReturnGoodsInfoAdapter;
    private String mOrderCode;
    double mRefundSumAmt;
    int mRefundType = 1;//退货类型（1全部退货，2部分退货）
    private String mRefundOperId,mRefundOperName;
    private String mRefunRemark,mRefundCode;
    private CustomProgressDialog mProgressDialog;
    public SaleReturnDialog(@NonNull MainActivity context,final String title,final String order_code) {
        super(context, title);
        mOrderCode = order_code;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.sale_return_dialog_layout);

        initOrderCodeTv();
        initGoodsDetails();
        initRefundBtn();
    }

    private void initGoodsDetails(){
        final RecyclerView goods_detail = findViewById(R.id.goods_details);
        final TextView refund_sum_num_tv = findViewById(R.id.r_num),refund_sum_amt_tv = findViewById(R.id.r_sum_amt);
        if (null != goods_detail && null != refund_sum_num_tv && null != refund_sum_amt_tv){
            mSaleReturnGoodsInfoAdapter = new SaleReturnGoodsInfoAdapter(this);
            mSaleReturnGoodsInfoAdapter.setRefundDataChange(datas -> {
                double refund_num = 0.0,refund_sum_num = 0.0,refund_sum_amt = 0.0,refund_price = 0.0;
                JSONObject record;
                for (int i = 0,size = datas.size();i < size;i++){
                    record = datas.getJSONObject(i);

                    refund_price = record.getDoubleValue("refund_price");
                    refund_num = record.getDoubleValue("refund_num");
                    refund_sum_num += refund_num;
                    refund_sum_amt += refund_num * refund_price;
                }
                refund_sum_num_tv.setText(String.format(Locale.CHINA,"%.2f",refund_sum_num));
                mRefundSumAmt = refund_sum_amt;
                refund_sum_amt_tv.setText(String.format(Locale.CHINA,"%.2f",refund_sum_amt));
            });
            goods_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            goods_detail.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
            goods_detail.setAdapter(mSaleReturnGoodsInfoAdapter);
            mSaleReturnGoodsInfoAdapter.setDatas(mOrderCode);
        }
    }
    private void initOrderCodeTv(){
        final TextView order_code_tv = findViewById(R.id.order_code);
        if (order_code_tv != null){
            order_code_tv.setText(mOrderCode);
        }
    }
    private void initRefundBtn(){
        final Button all_return_btn = findViewById(R.id.all_return_btn);
        if (null != all_return_btn){
            all_return_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!Utils.equalDouble(mRefundSumAmt,0.0)){
                        setRefundOpertor(null,null);
                        final StringBuilder err = new StringBuilder();
                        if (saveOrderInfo(err)){
                            if (mProgressDialog == null)mProgressDialog = new CustomProgressDialog(mContext);
                            mProgressDialog.setCancel(false).setMessage("正在退款...").show();
                            CustomApplication.execute(()->{
                                allRefund();
                            });
                        }else {
                            MyDialog.ToastMessage(err.toString(),mContext,getWindow());
                        }
                    }else {
                        MyDialog.ToastMessage("无可退商品！",mContext,getWindow());
                    }
                }
            });

        }
    }

    private void setRefundOpertor(final String cashier_id,final String cashier_name){
        if ((cashier_id == null || "".equals(cashier_id))){
            mRefundOperId = mContext.getCashierInfo().getString("cas_id");
        }else
            mRefundOperId = cashier_id;

        if ((cashier_name == null || "".equals(cashier_name))){
            mRefundOperName = mContext.getCashierInfo().getString("cas_name");
        }else {
            mRefundOperName = cashier_name;
        }
    }
    private String generateRefundOrderCode(final String pos_num){
        String prefix = "T" + pos_num + "-" + new SimpleDateFormat("yyMMddHHmmss").format(new Date()) + "-",order_code ;
        JSONObject orders= new JSONObject();
        if (SQLiteHelper.execSql(orders,"SELECT count(ro_id) + 1 ro_code from refund_order where date(addtime,'unixepoch' ) = date('now')")){
            order_code =orders.getString("ro_code");
            order_code = prefix + "0000".substring(order_code.length()) + order_code;
        }else{
            order_code = prefix + "0001";;
            MyDialog.ToastMessage("生成订单号错误：" + orders.getString("info"),mContext,null);
        }
        return order_code;
    }
    private JSONArray generatePayInfo(final String order_code,final StringBuilder err){
        JSONArray pay_records = null;
        switch (mRefundType){
            case 1:
                mRefunRemark = "原路退款";
                pay_records = SQLiteHelper.getListToJson("SELECT a.pay_method,b.name pay_method_name,a.pay_money,a.pay_code,a.is_check FROM retail_order_pays a " +
                        "inner join pay_method b on a.pay_method = b.pay_method_id where order_code = '" + order_code +"'",err);
                break;
            case 2:
                mRefunRemark = "部分退款";
                break;
        }
        return pay_records;
    }

    private boolean generateRefundData(final @NonNull JSONObject refund_info){
        final String stores_id = mContext.getStoreInfo().getString("stores_id"),pos_num = mContext.getPosNum(),cashier_id = mContext.getCashierInfo().getString("cas_id"),
                cashier_name = mContext.getCashierInfo().getString("cas_name");
        JSONObject tmp_record;
        long time = System.currentTimeMillis() / 1000;
        double order_sum_amt = 0.0;

        mRefundCode = generateRefundOrderCode(pos_num);

        //处理退货商品
        final JSONArray goods_datas = Utils.JsondeepCopy(mSaleReturnGoodsInfoAdapter.getRefundGoods());
        for (int i = 0,size = goods_datas.size();i < size;i++){
            tmp_record = goods_datas.getJSONObject(i);

            tmp_record.remove("returnable_num");
            tmp_record.put("xnum",tmp_record.remove("refund_num"));
            tmp_record.put("ro_code",mRefundCode);

            order_sum_amt += tmp_record.getDoubleValue("sale_amt");
        }

        //处理支付信息
        final StringBuilder err = new StringBuilder();
        final JSONArray refund_pay_records = generatePayInfo(mOrderCode,err);
        if (refund_pay_records == null){
            refund_info.put("info",err);
            return false;
        }
        for (int i = 0,size = refund_pay_records.size();i < size;i ++){
            tmp_record = refund_pay_records.getJSONObject(i);
            tmp_record.put("ro_code",mRefundCode);
            tmp_record.put("remark",mRefunRemark);
            tmp_record.put("pay_time",0);
            tmp_record.put("pay_status",1);
        }

        //处理订单信息
        final JSONObject order_info = new JSONObject();
        order_info.put("stores_id",stores_id);
        order_info.put("ro_code",mRefundCode);
        order_info.put("order_code",mOrderCode);
        order_info.put("total",order_sum_amt);
        order_info.put("type",mRefundType);

        order_info.put("cashier_id",cashier_id);
        order_info.put("cashier_name",cashier_name);

        order_info.put("addtime",time);
        order_info.put("pos_code",pos_num);
        order_info.put("order_status",1);
        order_info.put("upload_status",1);
        order_info.put("upload_time",0);
        order_info.put("is_rk",1);
        order_info.put("refund_ment",1);
        order_info.put("mobile","");
        order_info.put("name","");
        order_info.put("card_code","");
        order_info.put("member_id","");
        order_info.put("remark","");
        order_info.put("transfer_status",1);
        order_info.put("transfer_time",1);
        order_info.put("ok_cashier_id",mRefundOperId);
        order_info.put("ok_cashier_name",mRefundOperName);
        order_info.put("refund_total",mRefundSumAmt);


        refund_info.put("refund_order",new JSONArray(){{add(order_info);}});
        refund_info.put("refund_order_goods",goods_datas);
        refund_info.put("refund_order_pays",refund_pay_records);

        return true;
    }
    private boolean saveOrderInfo(final StringBuilder err){
        boolean code;
        final JSONObject counts = new JSONObject(),data = new JSONObject();
        List<String> tables = Arrays.asList("refund_order","refund_order_goods","refund_order_pays"),
                refund_order_cols = Arrays.asList("refund_total","ok_cashier_name","ok_cashier_id","cashier_name","member_id","transfer_time","transfer_status","remark","card_code","name",
                        "mobile","refund_ment","is_rk","upload_time","upload_status","order_status","pos_code","addtime","cashier_id","member_card","type","total","order_code","ro_code","stores_id"),
                refund_order_goods_cols = Arrays.asList("produce_date","conversion", "is_rk","unit_name","barcode","goods_title","rog_id","refund_price","price", "xnum","barcode_id","ro_code"),
                refund_order_pays_cols = Arrays.asList("road_pay_status","pay_method_name","is_check","remark","pay_code","pay_serial_no","pay_status","pay_time","pay_money","pay_method","ro_code");


        if (code = generateRefundData(data)){
            if ((code = SQLiteHelper.execSql(counts,"select count(order_code) counts from retail_order where order_code = '" + mContext.getOrderCode() +"' and stores_id = '" + mContext.getStoreInfo().getString("stores_id") +"'"))){
                if (code = (0 == counts.getIntValue("counts"))){
                    if (!(code = SQLiteHelper.execSQLByBatchFromJson(data,tables,Arrays.asList(refund_order_cols,refund_order_goods_cols,refund_order_pays_cols),err,0))){
                        err.insert(0,"保存订单信息错误：");
                    }
                }else{
                    err.append("本地已存在此订单信息，请重新下单！");
                }
            }else{
                err.append("查询订单信息错误：").append(counts.getString("info"));
            }
        }else {
            err.append("生成订单错误：").append(data.getString("info"));
        }
        return code;
    }
    private void allRefund(){//全部退款，按原路返回
        final JSONObject object = new JSONObject();
        object.put("appid",mContext.getAppId());
        object.put("order_code",mOrderCode);
        final String sz_param = HttpRequest.generate_request_parm(object,mContext.getAppScret());
        HttpRequest httpRequest = new HttpRequest();
        JSONObject retJson = httpRequest.sendPost(mContext.getUrl() + "/api/pay2_refund/refund",sz_param,true);
        final StringBuilder err = new StringBuilder();
        switch (retJson.getIntValue("flag")){
            case 0:
                err.append(retJson.getString("info"));
                break;
            case 1:
                JSONObject info = JSONObject.parseObject(retJson.getString("info"));
                switch (info.getString("status")){
                    case "n":
                        err.append(info.getString("info"));
                        break;
                    case "y":
                        switch (info.getIntValue("recode")){
                            case 1:
                                Logger.d_json(info.toJSONString());

                                final JSONArray refund_money_info = info.getJSONArray("refund_money_info");
                                if (updateRefundResult(refund_money_info,mOrderCode,mRefundCode,err)){
                                    object.put("appid",mContext.getAppId());
                                    object.put("retail_code",mOrderCode);
                                    object.put("stores_id",mContext.getStoreInfo().getString("stores_id"));

                                    mProgressDialog.setMessage("正在获取退单信息...").refreshMessage();
                                    retJson = httpRequest.sendPost(mContext.getUrl() + "/api/refund/getretailrefund",
                                            HttpRequest.generate_request_parm(object,mContext.getAppScret()),true);

                                    info = JSON.parseObject(retJson.getString("info"));
                                    Logger.d_json(info.toJSONString());
                                }
                                break;
                                default:
                                    break;
                        }

                        break;
                }
                break;
        }
        mContext.runOnUiThread(()->{
            mSaleReturnGoodsInfoAdapter.setDatas(mOrderCode);
            if (null != mProgressDialog)mProgressDialog.dismiss();
            if (err.length() == 0)err.append("操作成功!");
            MyDialog.ToastMessage(err.toString(),mContext,getWindow());
        });
    }

    private boolean updateRefundResult(@NonNull JSONArray refund_money_info,final String order_code,final String ro_code,final StringBuilder err){
        final List<String> update_sqls_list = new ArrayList<>();
        final StringBuilder update_sqls = new StringBuilder();
        long pay_time = 0;
        int pay_status = 2;
        String pay_serial_no,order_code_son;
        for(int i = 0,size = refund_money_info.size();i < size;i ++){
            final JSONObject record = refund_money_info.getJSONObject(i);
            pay_time = record.getLongValue("pay_time");
            pay_serial_no = record.getString("pay_code");
            order_code_son = record.getString("order_code_son");
            update_sqls.append("update refund_order_pays set road_pay_status = 2,pay_serial_no = ").append("'").append(pay_serial_no).append("'").append(",pay_status =").append(pay_status)
                    .append(",pay_time=").append(pay_time).append(" where ro_code =").append("'").append(ro_code).append("'").append(" and pay_code =").append("'").append(order_code_son).append("'");

            update_sqls_list.add(update_sqls.toString());
            update_sqls.delete(0,update_sqls.length());
        }
        update_sqls_list.add("update refund_order set order_status = 2 where order_code = '"+ order_code +"' and ro_code = '"+ ro_code +"'");
        return SQLiteHelper.execBatchUpdateSql(update_sqls_list,err);
    }
}
