package com.wyc.cloudapp.dialog.orderDialog;

import android.content.ContentValues;
import android.content.Context;
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
import com.wyc.cloudapp.adapter.PayMethodViewAdapter;
import com.wyc.cloudapp.adapter.RefundDialogAdapter;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
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


public final class RefundDialog extends AbstractDialogMainActivity {
    private RefundDialogAdapter mRefundDialogAdapter;
    private String mOrderCode,mRefundCode;
    private CustomProgressDialog mProgressDialog;
    private EditText mRemarkEt,mOrderCodeEt;
    private JSONObject mVipInfo;
    private Button mQueryBtn, mRefundBtn,mRemarkBtn;
    private boolean isRefundCheck;
    private final boolean lessThan7Inches;
    private int mRefundType = 1;//退货类型（1全部退货，2部分退货,3 单品退货,4 无货可退，用于隐藏按钮）
    public RefundDialog(@NonNull MainActivity context, final String order_code) {
        super(context, context.getString(R.string.refund_dialog_title_sz));
        mOrderCode = order_code;
        lessThan7Inches = context.lessThan7Inches();
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new CustomProgressDialog(mContext);

        initRefundBtn();
        initOrderCodeTv();
        initGoodsDetails();
        initRemarkBtn();
    }
    @Override
    protected int getContentLayoutId(){
        if (lessThan7Inches)return R.layout.mobile_refund_dialog_layout;
        return R.layout.refund_dialog_layout;
    }

    @Override
    protected void initWindowSize(){
        if (lessThan7Inches)fullScreen();
    }

    public void dismiss(){
        super.dismiss();
        if (mProgressDialog != null && mProgressDialog.isShowing())mProgressDialog.dismiss();
        Printer.dismissPrintIcon(mContext);
    }

    @Override
    public void show(){
        super.show();
        Printer.showPrintIcon(mContext);
        if (mRefundDialogAdapter.isSingleRefundStatus() && mRefundBtn != null){
            mRefundBtn.callOnClick();
        }else {
            if (mQueryBtn != null && mOrderCode != null && mOrderCode.length() != 0)mQueryBtn.callOnClick();
        }
    }

    private void initRemarkBtn(){
        final Button remark_btn = findViewById(R.id.remark_btn);
        if (remark_btn != null){
            remark_btn.setOnClickListener(v -> {
                final EditText remark_et = findViewById(R.id.remark_et);
                if (remark_et != null){
                    if (remark_et.getVisibility() == View.GONE){
                        remark_et.setVisibility(View.VISIBLE);
                    }else{
                        remark_et.clearFocus();
                        remark_et.setVisibility(View.GONE);
                    }
                    mRemarkEt = remark_et;
                }
            });
            mRemarkBtn = remark_btn;
        }
    }

    private void initQueryBtn(){
        final LinearLayout query_condition_layout = findViewById(R.id.query_condition_layout);
        if (query_condition_layout != null){
            if (mRefundDialogAdapter.isSingleRefundStatus()){
                query_condition_layout.setVisibility(View.GONE);
                mRefundDialogAdapter.setData(mContext.getSaleData());
            }else {
                final Button query_btn =  query_condition_layout.findViewById(R.id.query_btn);
                if (query_btn != null){
                    query_btn.setOnClickListener(v -> {
                        if (mOrderCode != null && mOrderCode.length() != 0){
                            if (CustomApplication.self().isConnection()){
                                mProgressDialog.setCancel(false).setMessage("正在查询订单信息...").refreshMessage().show();
                                CustomApplication.execute(()->{
                                    final StringBuilder err = new StringBuilder();
                                    mRefundDialogAdapter.setDatas(mOrderCode,err);
                                    mContext.runOnUiThread(()->{
                                        if (err.length() == 0){
                                            err.append("操作成功！");
                                            initVipInfoLayout();
                                            mRefundDialogAdapter.notifyDataSetChanged();
                                        }
                                        mProgressDialog.dismiss();
                                        MyDialog.ToastMessage(err.toString(),mContext,getWindow());
                                    });
                                });
                            }else{
                                if (mRefundBtn != null) mRefundBtn.setVisibility(View.GONE);
                                MyDialog.ToastMessage("断网状态不允许退单！",mContext,null);
                            }
                        }else {
                            mOrderCodeEt.requestFocus();
                            MyDialog.ToastMessage(mOrderCodeEt,mContext.getString(R.string.not_empty_hint_sz,mContext.getString(R.string.retail_order_code_sz)),mContext,getWindow());
                        }
                    });
                    mQueryBtn = query_btn;
                }
            }
        }
    }

    private void initGoodsDetails(){
        final RecyclerView goods_detail = findViewById(R.id.goods_details);
        final TextView refund_sum_num_tv = findViewById(R.id.r_num),refund_sum_amt_tv = findViewById(R.id.r_sum_amt);
        if (null != goods_detail && null != refund_sum_num_tv && null != refund_sum_amt_tv){
            mRefundDialogAdapter = new RefundDialogAdapter(this);
            mRefundDialogAdapter.setRefundDataChange(datas -> {
                if (datas != null){
                    double refund_num = 0.0,refund_sum_num = 0.0,refund_sum_amt = 0.0,refund_price = 0.0,xnum = 0.0,returnable_sum_num = 0.0,returnable_num = 0.0;
                    JSONObject record;
                    boolean isPart = false;
                    int refund_type = 1;
                    for (int i = 0,size = datas.size();i < size;i++){
                        record = datas.getJSONObject(i);

                        xnum += record.getDoubleValue("xnum");

                        refund_price = record.getDoubleValue("refund_price");
                        refund_num = record.getDoubleValue("refund_num");

                        returnable_num = record.getDoubleValue("returnable_num");
                        if (Utils.equalDouble(returnable_num,0.0) && !isPart){
                            isPart = true;
                        }

                        refund_sum_num += refund_num;
                        refund_sum_amt += refund_num * refund_price;
                        returnable_sum_num += returnable_num;
                    }
                    refund_sum_num_tv.setText(String.format(Locale.CHINA,"%.3f",refund_sum_num));
                    refund_sum_amt_tv.setText(String.format(Locale.CHINA,"%.2f",Utils.formatDouble(refund_sum_amt,2)));

                    if (!mRefundDialogAdapter.isSingleRefundStatus()){
                        if (Utils.equalDouble(refund_sum_num,0.0)){
                            refund_sum_num = xnum;
                        }
                        if (Utils.equalDouble(returnable_sum_num,0.0)){
                            refund_type = 4;
                        }else if (isPart || !Utils.equalDouble(refund_sum_num,returnable_sum_num) || !Utils.equalDouble(xnum,refund_sum_num)){
                            refund_type = 2;
                        }
                    }else {
                        refund_type = 3;
                    }

                    updateRefundBtnStatusAndSetRefundType(refund_type);
                }
            });
            mRefundDialogAdapter.setRefundPayDataChange(datas -> {
                double pay_sum_amt = 0.0,refund_sum_amt = mRefundDialogAdapter.getRefundAmt();
                for (int i = 0,size = datas.size();i < size;i++){
                    pay_sum_amt += datas.getJSONObject(i).getDoubleValue("pay_money");
                }
                pay_sum_amt = Utils.formatDouble(pay_sum_amt,2);
                if (Utils.equalDouble(refund_sum_amt,pay_sum_amt)){
                    requestRefund();
                }else {
                    MyDialog.displayErrorMessage(mContext, String.format(Locale.CHINA,"退货金额:%f  不等于 付款金额:%f",refund_sum_amt,pay_sum_amt));
                }
            });

            goods_detail.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
            goods_detail.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL));
            goods_detail.setAdapter(mRefundDialogAdapter);

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
                    mRefundDialogAdapter.clearOrderInfo();
                }
            });
        }
    }

    private void updateRefundBtnStatusAndSetRefundType(int type){
        mRefundType = type;
        final Button refund_btn = mRefundBtn;
        if (refund_btn != null){
            if (type == 4){
                if (refund_btn.getVisibility() == View.VISIBLE){
                    refund_btn.setVisibility(View.GONE);
                }
                if (mRemarkBtn != null && mRemarkBtn.getVisibility() == View.VISIBLE){
                    mRemarkBtn.setVisibility(View.GONE);
                }
            }else  if (type == 2){
                refund_btn.setText(mContext.getString(R.string.part_return_sz));
            }else if (type == 3){
                refund_btn.setText(mContext.getString(R.string.single_refund_sz));
            }else {
                refund_btn.setText(mContext.getString(R.string.all_refund_sz));
            }
        }
    }

    public static boolean verifyRefundPermission(MainActivity activity){
        return activity.verifyPermissions("13",null);
    }
    private void initRefundBtn(){
        final Button return_btn = mRefundBtn = findViewById(R.id.return_btn);
        if (null != return_btn){
            return_btn.setOnClickListener(v -> {
                final int type = mRefundType;
                if (type == 1){
                    mRefundDialogAdapter.allRefund();
                }else if(type == 2 || type == 3){
                    if (!Utils.equalDouble(mRefundDialogAdapter.getRefundAmt(),0.0)){
                        final RefundPayDialogImp refundPayDialogImp = new RefundPayDialogImp(mContext);
                        refundPayDialogImp.setPayAmt(mRefundDialogAdapter.getRefundAmt());
                        refundPayDialogImp.setYesOnclickListener(dialog -> {
                            mRefundDialogAdapter.addPayInfo(dialog.getContent());
                            dialog.dismiss();
                        }).show();
                    }else
                        MyDialog.ToastMessage("无可退商品！",mContext,getWindow());
                }
            });
        }
    }

    private void requestRefund(){
        final MyDialog myDialog = new MyDialog(mContext,"退款信息");
        myDialog.setMessage(mRefundDialogAdapter.PayDatasToString()).setYesOnclickListener(mContext.getString(R.string.OK), myDialog1 -> {
            myDialog1.dismiss();
            mProgressDialog.setCancel(false).setMessage("正在保存单据...").refreshMessage().show();
            CustomApplication.execute(()->{
                final StringBuilder err = new StringBuilder();
                try {

                    final JSONObject data = new JSONObject();
                    if (generateRefundData(data)){
                        if (saveRefundOrderInfo(data,err)){
                            mProgressDialog.setCancel(false).setMessage("正在退款...").refreshMessage().show();
                            if (isRefundCheck){
                                refundWithCheck(err);
                            }else{
                                refundWithNotCheck(err);
                            }
                            isRefundCheck = false;//重置
                        }
                    }else {
                        err.append(data.getString("info"));
                    }

                }catch (Exception e){
                    e.printStackTrace();
                }

                if (err.length() != 0){
                    mContext.runOnUiThread(()->{
                        mProgressDialog.dismiss();
                        MyDialog.ToastMessage(err.toString(),mContext,getWindow());
                    });
                }else {
                    mContext.runOnUiThread(()->{
                        if (mRefundDialogAdapter.isSingleRefundStatus()){
                            mProgressDialog.dismiss();
                            mRefundDialogAdapter.clearOrderInfo();
                            this.dismiss();
                        }
                        if (null != mRemarkEt){
                            mRemarkEt.setVisibility(View.GONE);
                            mRemarkEt.getText().clear();
                        }
                    });
                    Printer.print(mContext,get_print_content(mContext,mRefundCode,!isRefundCheck));
                }
            });

        }).setNoOnclickListener(mContext.getString(R.string.cancel),MyDialog::dismiss).show();
    }

    private void refundWithNotCheck(final StringBuilder err){
        if (updateFromRefundResult(null,mOrderCode,mRefundCode,err)){
            if (mRefundDialogAdapter.isSingleRefundStatus()){//单品退货允许离线操作,保存单据之后再发起上传单据消息启动异步上传
                mRefundDialogAdapter.sync_refund_order();
            }else {
                if (uploadRefundOrder(mContext.getAppId(),mContext.getUrl(),mContext.getAppSecret(),mOrderCode, mRefundCode, err)) {
                    mContext.runOnUiThread(this::dismiss);
                }
            }
        }
    }
    private void refundWithCheck(final StringBuilder err){
        final JSONObject object = new JSONObject();
        object.put("appid",mContext.getAppId());
        object.put("order_code",mOrderCode);
        final String sz_param = HttpRequest.generate_request_parm(object,mContext.getAppSecret());
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
                                if (uploadRefundOrder(mContext.getAppId(),mContext.getUrl(),mContext.getAppSecret(),mOrderCode, mRefundCode, err)) {
                                    mContext.runOnUiThread(this::dismiss);
                                }
                            }
                        }else {
                            err.append(info.getString("info"));
                        }
                        break;
                }
                break;
        }
    }
    private void initVipInfoLayout(){
        final LinearLayout vip_info_layout = findViewById(R.id.vip_info_layout);
        if (null != vip_info_layout){
            final JSONObject vip_info = mVipInfo = mRefundDialogAdapter.getVipInfo();
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
    public static String generateRefundOrderCode(Context context,final String pos_num){
        String prefix = "T" + pos_num + "-" + new SimpleDateFormat("yyMMddHHmmss",Locale.CHINA).format(new Date()) + "-",order_code ;
        JSONObject orders= new JSONObject();
        if (SQLiteHelper.execSql(orders,"SELECT count(ro_id) + 1 ro_code from refund_order where date(addtime,'unixepoch' ) = date('now')")){
            order_code =orders.getString("ro_code");
            order_code = prefix + "0000".substring(order_code.length()) + order_code;
        }else{
            order_code = prefix + "0001";;
            MyDialog.ToastMessage("生成订单号错误：" + orders.getString("info"),context,null);
        }
        return order_code;
    }
    private boolean generateRefundData(final @NonNull JSONObject refund_info){
        final String stores_id = mContext.getStoreId(),pos_num = mContext.getPosNum(),cashier_id = mContext.getCashierId(),
                cashier_name = mContext.getCashierName(),sz_refund_remark = mRemarkEt == null ? "" : mRemarkEt.getText().toString();

        JSONObject tmp_record;
        long time = System.currentTimeMillis() / 1000;


        final String refund_code = generateRefundOrderCode(mContext,pos_num);
        mRefundCode = refund_code;

        //处理退货商品
        final JSONArray goods_datas = Utils.JsondeepCopy(mRefundDialogAdapter.getRefundGoods());
        double refund_num = 0.0d,order_sum_amt = 0.0,refund_sum_amt = 0.0;
        for (int i = 0;i < goods_datas.size();i++){
            tmp_record = goods_datas.getJSONObject(i);
            order_sum_amt += Utils.formatDouble(tmp_record.getDoubleValue("xnum") * tmp_record.getDoubleValue("price"),2);

            refund_num = tmp_record.getDoubleValue("refund_num");
            if (Utils.equalDouble(refund_num,0.0)){
                goods_datas.remove(i--);
            }else {
                tmp_record.remove("returnable_num");
                tmp_record.put("ro_code",refund_code);
                refund_sum_amt += Utils.formatDouble(refund_num * tmp_record.getDoubleValue("refund_price"),2);
            }
        }

        //处理支付信息
        final JSONArray refund_pay_records = mRefundDialogAdapter.getPayDatas();
        if (refund_pay_records == null){
            refund_info.put("info","支付记录不能为空！");
            return false;
        }
        for (int i = 0,size = refund_pay_records.size();i < size;i ++){
            tmp_record = refund_pay_records.getJSONObject(i);
            int is_check = tmp_record.getIntValue("is_check");
            tmp_record.put("ro_code",refund_code);
            tmp_record.put("remark",sz_refund_remark);
            tmp_record.put("road_pay_status",1);
            if (PayMethodViewAdapter.isApiCheck(is_check)){//记账方式支付状态为已支付
                isRefundCheck = true;
                tmp_record.put("pay_time",0);
                tmp_record.put("pay_status",1);
            }else{
                tmp_record.put("pay_time",System.currentTimeMillis() / 1000);
                tmp_record.put("pay_status",2);
            }
        }

        //处理订单信息
        final JSONObject order_info = new JSONObject();
        final JSONObject vip_info = mVipInfo;
        if (null != vip_info){
            order_info.put("mobile",vip_info.getString("mobile"));
            order_info.put("name",vip_info.getString("name"));
            order_info.put("card_code",vip_info.getString("card_code"));
            order_info.put("member_id",vip_info.getString("member_id"));
        }

        order_info.put("stores_id",stores_id);
        order_info.put("ro_code",refund_code);
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
        order_info.put("is_rk",2);//默认需要入库
        order_info.put("refund_ment",1);

        order_info.put("remark",sz_refund_remark);
        order_info.put("transfer_status",1);
        order_info.put("transfer_time",0);

        final String per_cashier_id = mContext.getPermissionCashierId();
        order_info.put("ok_cashier_id",per_cashier_id);
        order_info.put("ok_cashier_name",SQLiteHelper.getString("SELECT ifnull(cas_name,'') FROM cashier_info where cas_id = "+ per_cashier_id,null));

        order_info.put("refund_total",refund_sum_amt);

        final JSONArray refund_orders = new JSONArray();
        refund_orders.add(order_info);

        refund_info.put("refund_order",refund_orders);
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
    public static boolean uploadRefundOrder(final String appid,final String url,final String appSecret,final String order_code,final String ro_code,@NonNull final StringBuilder err){
        final String refund_order_sql = "SELECT refund_total total,member_id,remark,card_code,name,mobile,\n" +
                "       pos_code,addtime,cashier_id,type,total order_money,order_code,ro_code,stores_id FROM refund_order where order_status = 2 and upload_status = 1 and ro_code = '" + ro_code +"' and (ifnull(order_code,'') = '" + order_code +"')",
                refund_goods_sql = "SELECT produce_date,conversion,is_rk,rog_id,refund_price,refund_num xnum,price,barcode_id,ro_code FROM refund_order_goods where ro_code = '"+ ro_code +"';",
                refund_pay_sql = "SELECT road_pay_status,remark,pay_code,pay_serial_no, pay_status,pay_time,pay_money,pay_method,ro_code FROM refund_order_pays where ro_code = '"+ ro_code +"';";

        boolean code;

        final JSONArray refund_orders = SQLiteHelper.getListToJson(refund_order_sql,err);
        if (code = (null != refund_orders)){
            final JSONArray refund_goods = SQLiteHelper.getListToJson(refund_goods_sql,err);
            final JSONArray refund_pays = SQLiteHelper.getListToJson(refund_pay_sql,err);
            if (code = (refund_goods != null && refund_pays != null)){
                if (code = (!refund_goods.isEmpty() && !refund_pays.isEmpty())){
                    final JSONObject data = new JSONObject(),send_data = new JSONObject();
                    final HttpRequest httpRequest = new HttpRequest();

                    data.put("order_arr",refund_orders);
                    data.put("order_goods",refund_goods);
                    data.put("order_pay",refund_pays);

                    send_data.put("appid",appid);
                    send_data.put("data",data);

                    Logger.d_json(data.toJSONString());

                    JSONObject retJson = httpRequest.sendPost(url + "/api/refund/order_upload",HttpRequest.generate_request_parm(send_data,appSecret),true);
                    switch (retJson.getIntValue("flag")){
                        case 0:
                            code = false;
                            err.append(retJson.getString("info"));
                            break;
                        case 1:
                            retJson = JSON.parseObject(retJson.getString("info"));
                            final ContentValues values = new ContentValues();
                            switch (retJson.getString("status")){
                                case "n":
                                    values.put("upload_status",3);
                                    err.append(retJson.getString("info"));
                                    break;
                                case "y":
                                    values.put("upload_status",2);
                                    break;
                            }
                            values.put("upload_time",System.currentTimeMillis() / 1000);
                            int rows = SQLiteHelper.execUpdateSql("refund_order",values,"ifnull(order_code,'') = ? and ro_code = ?",new String[]{order_code,ro_code},err);
                            code = rows > 0;
                            if (rows == 0)err.append("未更新任何数据！");
                            break;
                    }
                } else {
                    err.append("上传明细为空！");
                    Logger.e("退货单:%s,order_code:%s,ro_code:%s",err,order_code,ro_code);
                }
            }
        }
        return code && err.length() == 0;
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
        update_sqls_list.add("update refund_order set order_status = 2 where ifnull(order_code,'') = '"+ order_code +"' and ro_code = '"+ ro_code +"'");
        if (!mRefundDialogAdapter.isSingleRefundStatus()){
            if (mRefundType == 2){
                update_sqls_list.add("update retail_order set order_status = 88 where order_code = '"+ order_code +"'");//部分退货
            }else if (mRefundType == 1)
                update_sqls_list.add("update retail_order set order_status = 4 where order_code = '"+ order_code +"'");
        }
        return SQLiteHelper.execBatchUpdateSql(update_sqls_list,err);
    }
    private static String c_format_58(final Context context, final JSONObject format_info, final JSONObject order_info, boolean is_open_cash_box){

        final StringBuilder info = new StringBuilder();
        int print_count = Utils.getNotKeyAsNumberDefault(format_info,"p_c",1),footer_space = Utils.getNotKeyAsNumberDefault(format_info,"f_s",5);
        final String store_name = Utils.getNullStringAsEmpty(format_info,"s_n"),pos_num = Utils.getNullOrEmptyStringAsDefault(order_info,"pos_num",""),
                cas_name = Utils.getNullOrEmptyStringAsDefault(order_info,"cas_name",""),footer_c = Utils.getNullStringAsEmpty(format_info,"f_c"),
                new_line = "\r\n",//Printer.commandToStr(Printer.NEW_LINE);
                new_line_10 = Printer.commandToStr(Printer.LINE_SPACING_10),
                new_line_2 = Printer.commandToStr(Printer.LINE_SPACING_2),new_line_d = Printer.commandToStr(Printer.LINE_SPACING_DEFAULT),
                line = "--------------------------------";

        if (is_open_cash_box)//开钱箱
            info.append(Printer.commandToStr(Printer.OPEN_CASHBOX));

        while (print_count-- > 0) {//打印份数
            info.append(Printer.commandToStr(Printer.DOUBLE_HEIGHT)).append(Printer.commandToStr(Printer.ALIGN_CENTER))
                    .append(context.getString(R.string.r_b_title_sz)).append(new_line).append(store_name.length() == 0 ? Utils.getNullStringAsEmpty(order_info,"stores_name") : store_name).append(Printer.commandToStr(Printer.NORMAL))
                    .append(new_line).append(new_line).append(Printer.commandToStr(Printer.ALIGN_LEFT));
            info.append(Printer.printTwoData(1, context.getString(R.string.b_f_store_id_sz).concat(Utils.getNullStringAsEmpty(order_info,"stores_id")),Utils.getNullStringAsEmpty(order_info,"oper_time"))).append(new_line);
            info.append(Printer.printTwoData(1, context.getString(R.string.b_f_jh_sz).concat(pos_num), context.getString(R.string.b_f_cashier_sz).concat(cas_name))).append(new_line);
            info.append(context.getString(R.string.b_f_order_sz)).append(Utils.getNullStringAsEmpty(order_info,"ro_code")).append(new_line).append(new_line);

            info.append(context.getString(R.string.b_f_header_sz).replace("-"," ")).append(new_line_2).append(new_line).append(line).append(new_line);
            //商品明细
            JSONObject info_obj;
            double refund_num = 0.0,refund_amt = 0.0,refund_sum_amt = 0.0,refund_price;
            int units_num = 0, type = 1;//商品属性 1普通 2称重 3用于服装
            final JSONArray sales = Utils.getNullObjectAsEmptyJsonArray(order_info,"sales");
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

                    if (i != 0)info.append(new_line_10);

                    info.append(Printer.commandToStr(Printer.BOLD)).append(Utils.getNullStringAsEmpty(info_obj,"goods_title")).append(new_line).append(new_line_d).append(Printer.commandToStr(Printer.BOLD_CANCEL));
                    info.append(Printer.printTwoData(1,Utils.getNullStringAsEmpty(info_obj,"barcode"),
                            Printer.printThreeData(16,String.format(Locale.CHINA,"%.2f",refund_price), type == 2 ? String.valueOf(refund_num) : String.valueOf((int) refund_num),String.format(Locale.CHINA,"%.2f",refund_amt)))).append(new_line);;

                }
            }
            info.append(line).append(new_line_2).append(new_line).append(new_line_d);

            info.append(Printer.printTwoData(1, context.getString(R.string.refund_amt_sz).concat("：").concat(String.format(Locale.CHINA, "%.2f",Utils.formatDouble(refund_sum_amt,2)))
                    , context.getString(R.string.b_f_units_sz).concat(String.valueOf(units_num)))).append(new_line_2).append(new_line).append(line);

            //支付方式
            double pamt = 0.0;
            final JSONArray pays = Utils.getNullObjectAsEmptyJsonArray(order_info,"pays");
            for (int i = 0, size = pays.size(); i < size; i++) {
                info_obj = pays.getJSONObject(i);

                if (i != 0)info.append(new_line_10);

                pamt = info_obj.getDoubleValue("pay_money");
                info.append(Utils.getNullOrEmptyStringAsDefault(info_obj,"pay_method_name","")).append("：").append(pamt).append("元").append(new_line).append(new_line_d);
            }
            info.append(line).append(new_line_2).append(new_line).append(new_line_d);
            if (footer_c.isEmpty()){
                info.append(context.getString(R.string.b_f_hotline_sz)).append(Utils.getNullOrEmptyStringAsDefault(order_info,"telphone","")).append(new_line);
                info.append(context.getString(R.string.b_f_stores_address_sz)).append(Utils.getNullOrEmptyStringAsDefault(order_info,"region","")).append(new_line);
            }else {
                info.append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(footer_c);
            }
            for (int i = 0; i < footer_space; i++) info.append(" ").append(new_line);

            if (print_count > 0){
                info.append(new_line).append(new_line).append(new_line);
            }
            info.append(Printer.commandToStr(Printer.RESET));
        }

        Logger.d(info);

        return info.toString();
    }
    static String get_print_content(final MainActivity context,final String refund_code,boolean is_open_cash_box){
        String content = "";
        if (context.getPrintStatus()){
            final JSONObject print_format_info = new JSONObject(),order_info = new JSONObject();
            if (SQLiteHelper.getLocalParameter("r_f_info",print_format_info)){
                if (print_format_info.getIntValue("f") == R.id.refund_format){
                    if (getPrintOrderInfo(refund_code,order_info)){
                        switch (print_format_info.getIntValue("f_z")){
                            case R.id.f_58:
                                content = c_format_58(context,print_format_info,order_info,is_open_cash_box);
                                break;
                            case R.id.f_76:
                                break;
                            case R.id.f_80:
                                break;
                        }
                    }else {
                        context.runOnUiThread(()->MyDialog.ToastMessage(context.getString(R.string.l_p_c_err_hint_sz,order_info.getString("info")), context,context.getWindow()));
                    }
                }else {
                    context.runOnUiThread(()->MyDialog.ToastMessage(context.getString(R.string.f_not_sz), context,context.getWindow()));
                }
            }else
                context.runOnUiThread(()->MyDialog.ToastMessage(context.getString(R.string.l_p_f_err_hint_sz,print_format_info.getString("info")), context,context.getWindow()));
        }
        return content;
    }
    private static boolean getPrintOrderInfo(final String refund_code,final JSONObject order_info){
        boolean code = false;
        if (SQLiteHelper.execSql(order_info,"SELECT a.ro_code,b.cas_name,a.pos_code pos_num,a.stores_id,c.stores_name,datetime(a.addtime, 'unixepoch', 'localtime') oper_time,c.telphone,c.region" +
                " FROM refund_order a  left join cashier_info b on a.cashier_id = b.cas_id\n" +
                "left join shop_stores c on a.stores_id = c.stores_id where a.ro_code = '" +refund_code +"'")) {

            final StringBuilder err = new StringBuilder();
            final String goods_info_sql = "SELECT b.barcode,b.goods_title,b.type,a.refund_num,a.refund_price FROM refund_order_goods a left join barcode_info b on a.barcode_id = b.barcode_id where ro_code = '" + refund_code + "'", pays_info_sql = "SELECT pay_method_name,pay_money FROM refund_order_pays where ro_code = '" + refund_code + "'";
            final JSONArray sales = SQLiteHelper.getListToJson(goods_info_sql, err), pays = SQLiteHelper.getListToJson(pays_info_sql, err);
            if (sales != null && pays != null) {
                order_info.put("sales", sales);
                order_info.put("pays", pays);

                code =true;
            }else {
                order_info.put("info",err.toString());
            }
        }
        return code;
    }

}
