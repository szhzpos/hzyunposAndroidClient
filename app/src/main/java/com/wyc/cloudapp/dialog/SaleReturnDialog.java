package com.wyc.cloudapp.dialog;

import android.content.ContentValues;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnMainActivity;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.print.Printer;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class SaleReturnDialog extends DialogBaseOnMainActivity {
    private SaleReturnGoodsInfoAdapter mSaleReturnGoodsInfoAdapter;
    private double mRefundSumAmt;
    private int mRefundType = 1;//退货类型（1全部退货，2部分退货）
    private String mRefundOperId,mRefundOperName;
    private String mOrderCode,mRefundCode;
    private CustomProgressDialog mProgressDialog;
    private EditText mRemarkEt,mOrderCodeEt;
    private JSONObject mVipInfo;
    private Button mQueryBtn;
    private boolean isRefundCheck;
    public SaleReturnDialog(@NonNull MainActivity context,final String order_code) {
        super(context, context.getString(R.string.refund_dialog_title_sz));
        mOrderCode = order_code;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.sale_return_dialog_layout);

        mProgressDialog = new CustomProgressDialog(mContext);

        initOrderCodeTv();
        initGoodsDetails();
        initRefundBtn();
        initRemarkBtn();
    }

    @Override
    public void show(){
        super.show();
        if (mQueryBtn != null && mOrderCode != null && mOrderCode.length() != 0)mQueryBtn.callOnClick();
    }

    private void initRemarkBtn(){
        final Button remark_btn = findViewById(R.id.remark_btn);
        if (remark_btn != null){
            remark_btn.setOnClickListener(v -> {
                final EditText remark_et = mRemarkEt = findViewById(R.id.remark_et);
                if (remark_et != null){
                    if (remark_et.getVisibility() == View.GONE){
                        remark_et.setVisibility(View.VISIBLE);
                    }else{
                        remark_et.clearFocus();
                        remark_et.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void initQueryBtn(){
        final Button query_btn = mQueryBtn = findViewById(R.id.query_btn);
        if (query_btn != null){
            query_btn.setOnClickListener(v -> {
                if (mOrderCode != null && mOrderCode.length() != 0){
                    mProgressDialog.setCancel(false).setMessage("正在查询订单信息...").refreshMessage().show();
                    CustomApplication.execute(()->{
                        final StringBuilder err = new StringBuilder();
                        mSaleReturnGoodsInfoAdapter.setDatas(mOrderCode,err);
                        mContext.runOnUiThread(()->{
                            if (err.length() == 0){
                                err.append("操作成功！");
                                initVipInfoLayout();
                                mSaleReturnGoodsInfoAdapter.notifyDataSetChanged();
                            }
                            mProgressDialog.dismiss();
                            MyDialog.ToastMessage(err.toString(),mContext,null);
                        });
                    });
                }else {
                    mOrderCodeEt.requestFocus();
                    MyDialog.ToastMessage(mOrderCodeEt,mContext.getString(R.string.sale_order_code_sz).concat(mContext.getString(R.string.not_empty_hint_sz)),mContext,getWindow());
                }
            });
        }
    }

    private void initGoodsDetails(){
        final RecyclerView goods_detail = findViewById(R.id.goods_details);
        final TextView refund_sum_num_tv = findViewById(R.id.r_num),refund_sum_amt_tv = findViewById(R.id.r_sum_amt);
        if (null != goods_detail && null != refund_sum_num_tv && null != refund_sum_amt_tv){
            mSaleReturnGoodsInfoAdapter = new SaleReturnGoodsInfoAdapter(this);
            mSaleReturnGoodsInfoAdapter.setRefundDataChange(datas -> {
                if (datas != null){
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
                }
            });
            goods_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            goods_detail.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
            goods_detail.setAdapter(mSaleReturnGoodsInfoAdapter);

            initQueryBtn();
        }
    }
    private void initOrderCodeTv(){
        final TextView order_code_tv = mOrderCodeEt = findViewById(R.id.order_code);
        if (order_code_tv != null){
            order_code_tv.setText(mOrderCode);
            order_code_tv.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    mOrderCode = s.toString();
                    mSaleReturnGoodsInfoAdapter.clearOrderInfo();
                }
            });
        }
    }
    private void initRefundBtn(){
        final Button all_return_btn = findViewById(R.id.all_return_btn);
        if (null != all_return_btn){
            all_return_btn.setOnClickListener(v -> requestRefund());
        }
    }

    private void requestRefund(){
        if (!Utils.equalDouble(mRefundSumAmt,0.0)){
            MyDialog myDialog = new MyDialog(mContext);
            myDialog.setTitle("退款信息").setMessage(mSaleReturnGoodsInfoAdapter.PayDatasToString()).setYesOnclickListener(mContext.getString(R.string.OK), myDialog1 -> {
                setRefundOpertor(null,null);
                final StringBuilder err = new StringBuilder();
                final JSONObject data = new JSONObject();
                if (generateRefundData(data)){
                    if (saveRefundOrderInfo(data,err)){
                        myDialog1.dismiss();

                        mProgressDialog.setCancel(false).setMessage("正在退款...").refreshMessage().show();
                        CustomApplication.execute(()->{
                            if (isRefundCheck){
                                refundWithCheck(err);
                            }else{
                                refundWithNotCheck(err);
                            }
                            if (err.length() != 0){
                                mContext.runOnUiThread(()->{
                                    mProgressDialog.dismiss();
                                    MyDialog.ToastMessage(err.toString(),mContext,getWindow());
                                });
                            }else {
                                Printer.print(mContext,get_print_content(mContext,mRefundCode,mSaleReturnGoodsInfoAdapter.getRefundGoods(),mSaleReturnGoodsInfoAdapter.getPayDatas(),!isRefundCheck));
                            }
                            isRefundCheck = false;//重置
                        });

                    }else {
                        MyDialog.ToastMessage(err.toString(),mContext,getWindow());
                    }
                }else {
                    MyDialog.ToastMessage("生成退货信息错误：" + data.getString("info"),mContext,getWindow());
                }
            }).setNoOnclickListener(mContext.getString(R.string.cancel),MyDialog::dismiss).show();
        }else {
            MyDialog.ToastMessage("无可退商品！",mContext,getWindow());
        }
    }

    private void initVipInfoLayout(){
        final LinearLayout vip_info_layout = findViewById(R.id.vip_info_layout);
        if (null != vip_info_layout){
            final JSONObject vip_info = mVipInfo = mSaleReturnGoodsInfoAdapter.getVipInfo();
            if (vip_info != null && !vip_info.isEmpty()){
                vip_info_layout.setVisibility(View.VISIBLE);
                final TextView vip_name = vip_info_layout.findViewById(R.id.vip_name),card_code = vip_info_layout.findViewById(R.id.card_code),
                        vip_mobile = vip_info_layout.findViewById(R.id.vip_mobile),member_id = vip_info_layout.findViewById(R.id.member_id);
                vip_name.setText(vip_info.getString("name"));
                card_code.setText(vip_info.getString("card_code"));
                vip_mobile.setText(vip_info.getString("mobile"));
                member_id.setText(vip_info.getString("member_id"));
            }
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
        String prefix = "T" + pos_num + "-" + new SimpleDateFormat("yyMMddHHmmss",Locale.CHINA).format(new Date()) + "-",order_code ;
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

    private boolean generateRefundData(final @NonNull JSONObject refund_info){
        final String stores_id = mContext.getStoreInfo().getString("stores_id"),pos_num = mContext.getPosNum(),cashier_id = mContext.getCashierInfo().getString("cas_id"),
                cashier_name = mContext.getCashierInfo().getString("cas_name"),sz_refund_remark = mRemarkEt == null ? "" : mRemarkEt.getText().toString();

        JSONObject tmp_record;
        long time = System.currentTimeMillis() / 1000;
        double order_sum_amt = 0.0;

        mRefundCode = generateRefundOrderCode(pos_num);

        //处理退货商品
        final JSONArray goods_datas = Utils.JsondeepCopy(mSaleReturnGoodsInfoAdapter.getRefundGoods());
        double refund_num = 0.0;
        for (int i = 0;i < goods_datas.size();i++){
            tmp_record = goods_datas.getJSONObject(i);

            refund_num = tmp_record.getDoubleValue("refund_num");
            if (Utils.equalDouble(refund_num,0.0)){
                goods_datas.remove(i--);
            }else {
                tmp_record.remove("returnable_num");
                tmp_record.put("ro_code",mRefundCode);
                order_sum_amt += tmp_record.getDoubleValue("xnum") * tmp_record.getDoubleValue("price");
            }
        }

        //处理支付信息
        final JSONArray refund_pay_records = mSaleReturnGoodsInfoAdapter.getPayDatas();
        if (refund_pay_records == null){
            refund_info.put("info","支付记录不能为空！");
            return false;
        }
        for (int i = 0,size = refund_pay_records.size();i < size;i ++){
            tmp_record = refund_pay_records.getJSONObject(i);
            int is_check = tmp_record.getIntValue("is_check");
            tmp_record.put("ro_code",mRefundCode);
            tmp_record.put("remark",sz_refund_remark);
            tmp_record.put("road_pay_status",1);
            if (is_check == 2){//记账方式支付状态为已支付
                tmp_record.put("pay_time",System.currentTimeMillis() / 1000);
                tmp_record.put("pay_status",2);
            }else{
                isRefundCheck = true;
                tmp_record.put("pay_time",0);
                tmp_record.put("pay_status",1);
            }
        }

        //处理订单信息
        final JSONObject order_info = new JSONObject();
        if (null != mVipInfo){
            order_info.put("mobile",mVipInfo.getString("mobile"));
            order_info.put("name",mVipInfo.getString("name"));
            order_info.put("card_code",mVipInfo.getString("card_code"));
            order_info.put("member_id",mVipInfo.getString("member_id"));
        }

        order_info.put("stores_id",stores_id);
        order_info.put("ro_code",mRefundCode);
        order_info.put("order_code",mOrderCode);
        order_info.put("total",order_sum_amt);
        order_info.put("type",mRefundType);

        order_info.put("cashier_id",cashier_id);
        order_info.put("cashier_name",cashier_name);

        order_info.put("addtime",time);
        order_info.put("pos_code",pos_num);
        order_info.put("order_status",!isRefundCheck ? 2 : 1);//如果不需要请求退款则退单状态为已成功
        order_info.put("upload_status",1);
        order_info.put("upload_time",0);
        order_info.put("is_rk",2);//默认需要入库
        order_info.put("refund_ment",1);

        order_info.put("remark",sz_refund_remark);
        order_info.put("transfer_status",1);
        order_info.put("transfer_time",0);
        order_info.put("ok_cashier_id",mRefundOperId);
        order_info.put("ok_cashier_name",mRefundOperName);
        order_info.put("refund_total",mRefundSumAmt);


        refund_info.put("refund_order",new JSONArray(){{add(order_info);}});
        refund_info.put("refund_order_goods",goods_datas);
        refund_info.put("refund_order_pays",refund_pay_records);

        return true;
    }
    private boolean saveRefundOrderInfo(final JSONObject data,final StringBuilder err){

        List<String> tables = Arrays.asList("refund_order","refund_order_goods","refund_order_pays"),
                refund_order_cols = Arrays.asList("refund_total","ok_cashier_name","ok_cashier_id","cashier_name","member_id","transfer_time","transfer_status","remark","card_code","name",
                        "mobile","refund_ment","is_rk","upload_time","upload_status","order_status","pos_code","addtime","cashier_id","member_card","type","total","order_code","ro_code","stores_id"),
                refund_order_goods_cols = Arrays.asList("produce_date","conversion", "is_rk","unit_name","barcode","goods_title","rog_id","refund_price","refund_num","price", "xnum","barcode_id","ro_code"),
                refund_order_pays_cols = Arrays.asList("road_pay_status","pay_method_name","is_check","remark","pay_code","pay_serial_no","pay_status","pay_time","pay_money","pay_method","ro_code");

        return SQLiteHelper.execSQLByBatchFromJson(data,tables,Arrays.asList(refund_order_cols,refund_order_goods_cols,refund_order_pays_cols),err,0);
    }

    private void refundWithNotCheck(final StringBuilder err){
        if (updateFromRefundResult(null,mOrderCode,mRefundCode,err)){
            if (uploadRefundOrder(mOrderCode, mRefundCode, err)) {
                //重新获取退单信息
                if (mQueryBtn != null)mQueryBtn.callOnClick();
            }
        }
    }
    private void refundWithCheck(final StringBuilder err){
        final JSONObject object = new JSONObject();
        object.put("appid",mContext.getAppId());
        object.put("order_code",mOrderCode);
        final String sz_param = HttpRequest.generate_request_parm(object,mContext.getAppScret());
        HttpRequest httpRequest = new HttpRequest();
        JSONObject retJson = httpRequest.sendPost(mContext.getUrl() + "/api/pay2_refund/refund",sz_param,true);
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
                        if (info.getIntValue("recode") == 1) {
                            final JSONArray refund_money_info = info.getJSONArray("refund_money_info");
                            //更新本地订单状态
                            if (updateFromRefundResult(refund_money_info, mOrderCode, mRefundCode, err)) {

                                mProgressDialog.setMessage("正在上传退单信息...").refreshMessage();
                                //上传本地订单
                                if (uploadRefundOrder(mOrderCode, mRefundCode, err)) {
                                    //重新获取退单信息
                                    if (mQueryBtn != null)mQueryBtn.callOnClick();
                                }
                            }
                        } else {
                            err.append(info.getString("info"));
                        }
                        break;
                }
                break;
        }
    }

    private boolean uploadRefundOrder(final String order_code,final String ro_code,final StringBuilder err){
        final String refund_order_sql = "SELECT refund_total total,member_id,remark,card_code,name,mobile,\n" +
                "       pos_code,addtime,cashier_id,type,total order_money,order_code,ro_code,stores_id FROM refund_order where order_status = 2 and upload_status = 1 and ro_code = '" + ro_code +"' and order_code = '" + order_code +"'",
                refund_goods_sql = "SELECT produce_date,conversion,is_rk,rog_id,refund_price,refund_num xnum,price,barcode_id,ro_code FROM refund_order_goods where ro_code = '"+ ro_code +"';",
                refund_pay_sql = "SELECT road_pay_status,remark,pay_code,pay_serial_no, pay_status,pay_time,pay_money,pay_method,ro_code FROM refund_order_pays where ro_code = '"+ ro_code +"';";

        boolean code;

        final JSONArray refund_orders = SQLiteHelper.getListToJson(refund_order_sql,err);
        if (code = (null != refund_orders)){
            final JSONArray refund_goods = SQLiteHelper.getListToJson(refund_goods_sql,err);
            final JSONArray refund_pays = SQLiteHelper.getListToJson(refund_pay_sql,err);
            if (code = (refund_goods != null && refund_pays != null)){
                final JSONObject data = new JSONObject(),send_data = new JSONObject();
                final HttpRequest httpRequest = new HttpRequest();

                data.put("order_arr",refund_orders);
                data.put("order_goods",refund_goods);
                data.put("order_pay",refund_pays);

                send_data.put("appid",mContext.getAppId());
                send_data.put("data",data);

                JSONObject retJson = httpRequest.sendPost(mContext.getUrl() + "/api/refund/order_upload",HttpRequest.generate_request_parm(send_data,mContext.getAppScret()),true);
                switch (retJson.getIntValue("flag")){
                    case 0:
                        code = false;
                        err.append(retJson.getString("info"));
                        break;
                    case 1:
                        retJson = JSON.parseObject(retJson.getString("info"));
                        switch (retJson.getString("status")){
                            case "n":
                                code = false;
                                err.append(retJson.getString("info"));
                                break;
                            case "y":
                                Logger.d_json(retJson.toJSONString());
                                final String ret_order_code = Utils.getNullStringAsEmpty(retJson,"order_code"),ret_ro_code = Utils.getNullStringAsEmpty(retJson,"ro_code");
                                if (code = (ret_order_code.equals(order_code) && ret_ro_code.equals(ro_code))){
                                    final ContentValues values = new ContentValues();
                                    values.put("upload_status",2);
                                    values.put("upload_time",System.currentTimeMillis() / 1000);
                                    code = SQLiteHelper.execUpdateSql("refund_order",values,"order_code = ? and ro_code = ?",new String[]{order_code,ro_code},err);
                                }else {
                                    err.append("上传成功，但返回订单号与上传的订单号不一致！");
                                }
                                break;
                        }
                        break;
                }
            }
        }
        return code;
    }

    private boolean updateFromRefundResult(JSONArray refund_money_info,final String order_code,final String ro_code,final StringBuilder err){
        final List<String> update_sqls_list = new ArrayList<>();
        final StringBuilder update_sqls = new StringBuilder();
        long pay_time = 0;
        int pay_status = 2;
        if (refund_money_info != null){
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
        }
        update_sqls_list.add("update refund_order set order_status = 2 where order_code = '"+ order_code +"' and ro_code = '"+ ro_code +"'");
        update_sqls_list.add("update retail_order set order_status = 4 where order_code = '"+ order_code +"'");
        return SQLiteHelper.execBatchUpdateSql(update_sqls_list,err);
    }

    private String c_format_58(final MainActivity context,final String refund_code,final JSONObject format_info,@NonNull final JSONArray sales,@NonNull final JSONArray pays,boolean is_open_cash_box){

        final StringBuilder info = new StringBuilder();
        int print_count = Utils.getNotKeyAsNumberDefault(format_info,"p_c",1),footer_space = Utils.getNotKeyAsNumberDefault(format_info,"f_s",5);
        final JSONObject cas_info = context.getCashierInfo(),st_info = context.getStoreInfo();
        final String store_name = Utils.getNullStringAsEmpty(format_info,"s_n"),pos_num = Utils.getNullOrEmptyStringAsDefault(cas_info,"pos_num",""),
                cas_name = Utils.getNullOrEmptyStringAsDefault(cas_info,"cas_name",""),footer_c = Utils.getNullStringAsEmpty(format_info,"f_c"),
                new_line = "\r\n",//Printer.commandToStr(Printer.NEW_LINE);
                new_line_16 = Printer.commandToStr(Printer.LINE_SPACING_16),
                new_line_2 = Printer.commandToStr(Printer.LINE_SPACING_2),new_line_d = Printer.commandToStr(Printer.LINE_SPACING_DEFAULT),
                line = "--------------------------------";

        if (is_open_cash_box)//开钱箱
            info.append(Printer.commandToStr(Printer.OPEN_CASHBOX));

        while (print_count-- > 0) {//打印份数
            info.append(Printer.commandToStr(Printer.DOUBLE_HEIGHT)).append(Printer.commandToStr(Printer.ALIGN_CENTER))
                    .append(context.getString(R.string.refund_order_sz)).append(new_line).append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(store_name.length() == 0 ? Utils.getNullStringAsEmpty(st_info,"stores_name") : store_name).append(new_line).append(new_line).append(Printer.commandToStr(Printer.NORMAL)).
                    append(Printer.commandToStr(Printer.ALIGN_LEFT));

            info.append(Printer.printTwoData(1, context.getString(R.string.b_f_store_id_sz).concat(Utils.getNullStringAsEmpty(st_info,"stores_id")), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA).format(new Date()))).append(new_line);
            info.append(Printer.printTwoData(1, context.getString(R.string.b_f_jh_sz).concat(pos_num), context.getString(R.string.b_f_cashier_sz).concat(cas_name))).append(new_line);
            info.append(context.getString(R.string.b_f_order_sz)).append(refund_code).append(new_line).append(new_line);

            info.append(context.getString(R.string.b_f_header_sz).replace("-"," ")).append(new_line_2).append(new_line).append(line).append(new_line_2).append(new_line).append(new_line_d);
            //商品明细
            JSONObject info_obj;
            double refund_num = 0.0,refund_amt = 0.0,refund_sum_amt = 0.0,refund_price;
            int units_num = 0, type = 1;//商品属性 1普通 2称重 3用于服装
            for (int i = 0, size = sales.size(); i < size; i++) {
                info_obj = sales.getJSONObject(i);
                if (info_obj != null) {
                    type = info_obj.getIntValue("type");
                    if (type == 2) {
                        units_num += 1;
                    } else {
                        units_num += info_obj.getIntValue("refund_num");
                    }
                    refund_num = info_obj.getDoubleValue("refund_num");
                    refund_price = info_obj.getDoubleValue("refund_price");
                    refund_amt = refund_num * refund_price;
                    refund_sum_amt += refund_amt;

                    if (i > 0) {
                        info.append(new_line_d);
                    }

                    info.append(Printer.commandToStr(Printer.BOLD)).append(Utils.getNullStringAsEmpty(info_obj,"goods_title")).append(new_line).append(Printer.commandToStr(Printer.BOLD_CANCEL));
                    info.append(Printer.printTwoData(1,Utils.getNullStringAsEmpty(info_obj,"barcode"),
                            Printer.printThreeData(16,String.valueOf(refund_price), type == 2 ? String.valueOf(refund_num) : String.valueOf((int) refund_num),String.format(Locale.CHINA,"%.2f",refund_amt))));

                    if (i + 1 != size)
                        info.append(new_line_16);
                    else
                        info.append(new_line_2);

                    info.append(new_line);
                }
            }
            info.append(line).append(new_line_2).append(new_line).append(new_line_d);

            info.append(Printer.printTwoData(1, context.getString(R.string.refund_amt_sz).concat("：").concat(String.format(Locale.CHINA, "%.2f",refund_sum_amt))
                    , context.getString(R.string.b_f_units_sz).concat(String.valueOf(units_num)))).append(new_line).
                    append(new_line_2).append(line).append(new_line_2).append(new_line_d);

            //支付方式
            double pamt = 0.0;
            for (int i = 0, size = pays.size(); i < size; i++) {
                info_obj = pays.getJSONObject(i);

                pamt = info_obj.getDoubleValue("pay_money");
                info.append(Utils.getNullOrEmptyStringAsDefault(info_obj,"pay_method_name","")).append("：").append(pamt).append("元");

                if (info_obj.containsKey("xnote")) {
                    final JSONArray xnotes = JSON.parseArray(Utils.getNullOrEmptyStringAsDefault(info_obj,"xnote","[]"));
                    if (xnotes != null) {
                        int length = xnotes.size();
                        if (length > 0) {
                            info.append(new_line);
                            for (int j = 0; j < length; j++) {
                                if (j + 1 != length)
                                    info.append(xnotes.getString(j)).append(new_line);
                            }
                        }
                    }
                }
                if (i + 1 != size)
                    info.append(new_line_16);
                else
                    info.append(new_line_2);

                info.append(new_line).append(new_line_d);
            }
            info.append(line).append(new_line_2).append(new_line).append(new_line_d);
            info.append(context.getString(R.string.b_f_hotline_sz)).append(Utils.getNullOrEmptyStringAsDefault(st_info,"telphone","")).append(new_line);
            info.append(context.getString(R.string.b_f_stores_address_sz)).append(Utils.getNullOrEmptyStringAsDefault(st_info,"region","")).append(new_line);

            info.append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(footer_c);
            for (int i = 0; i < footer_space; i++) info.append(" ").append(new_line);

            if (print_count > 0){
                info.append(new_line).append(new_line).append(new_line);
            }
        }

        Logger.d(info);

        return info.toString();
    }
    private String get_print_content(final MainActivity context,final String refund_code, final JSONArray sales, JSONArray pays, boolean isOpenCashbox){
        final JSONObject print_format_info = new JSONObject();
        String content = "";
        if (SQLiteHelper.getLocalParameter("c_f_info",print_format_info)){
            if (print_format_info.getIntValue("f") == R.id.checkout_format){
                switch (print_format_info.getIntValue("f_z")){
                    case R.id.f_58:
                        content = c_format_58(context,refund_code,print_format_info,sales,pays,isOpenCashbox);
                        break;
                    case R.id.f_76:
                        break;
                    case R.id.f_80:
                        break;
                }
            }
        }else
            MyDialog.ToastMessage("加载打印格式错误：" + print_format_info.getString("info"), context,context.getWindow());

        return content;
    }

}
