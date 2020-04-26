package com.wyc.cloudapp.dialog.pay;

import android.app.Dialog;
import android.content.ContentValues;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.GoodsInfoViewAdapter;
import com.wyc.cloudapp.adapter.PayDetailViewAdapter;
import com.wyc.cloudapp.adapter.PayMethodItemDecoration;
import com.wyc.cloudapp.adapter.PayMethodViewAdapter;
import com.wyc.cloudapp.adapter.SaleGoodsViewAdapter;
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.ChangeNumOrPriceDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.vip.VipInfoDialog;
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

public class PayDialog extends Dialog {
    private MainActivity mainActivity;
    private EditText mCashMoneyEt,mZlAmtEt,mRemarkEt;
    private onPayListener mPayListener;
    private PayMethodViewAdapter mPayMethodViewAdapter;
    private PayDetailViewAdapter mPayDetailViewAdapter;
    private TextView mOrderAmtTv,mDiscountAmtTv,mActualAmtTv,mPayAmtTv,mAmtReceivedTv,mPayBalanceTv;
    private double mOrder_amt = 0.0,mDiscount_amt = 0.0,mActual_amt = 0.0,mPay_amt = 0.0,mAmt_received = 0.0,mPay_balance = 0.0,mCashAmt = 0.0,mZlAmt = 0.0;
    private Button mOK,mCancel;
    private JSONObject mVip;
    private boolean mPayStatus = true,mOpenCashbox;
    private Window mWindow;
    public PayDialog(MainActivity context){
        super(context);
        mainActivity = context;
        //可以show之前访问view
        setContentView(this.getLayoutInflater().inflate(R.layout.pay_dialog_content_layout, null));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        //初始化TextView
        mOrderAmtTv = findViewById(R.id.order_amt);//单据金额
        mDiscountAmtTv = findViewById(R.id.dis_sum_amt);//折扣金额
        mActualAmtTv = findViewById(R.id.actual_amt);//应收金额
        mPayAmtTv = findViewById(R.id.pay_amt);//付款金额
        mAmtReceivedTv = findViewById(R.id.amt_received);//已收金额
        mPayBalanceTv = findViewById(R.id.pay_balance);//付款余额
        mZlAmtEt = findViewById(R.id.zl_amt);//找零
        mRemarkEt = findViewById(R.id.et_remark);//备注


        //初始化支付方式
        initPayMethod();

        //初始化支付明细
        initPayDetailViewAdapter();

        //初始化现金EditText
        initCsahText();

        //初始化按钮
        mOK = findViewById(R.id._ok);
        mCancel = findViewById(R.id._cancel);

        findViewById(R.id._close).setOnClickListener(view -> mCancel.callOnClick());
        findViewById(R.id.mo_l).setOnClickListener(v -> {
            ChangeNumOrPriceDialog changeNumOrPriceDialog = new ChangeNumOrPriceDialog(getContext(),mainActivity.getString(R.string.mo_l_sz),String.format(Locale.CHINA,"%.2f",mActual_amt - ((int)mActual_amt)));
            changeNumOrPriceDialog.setYesOnclickListener(myDialog -> {
                double value = myDialog.getContentToDouble();
                if (initPayContent(mainActivity.discount((mActual_amt - value) / mOrder_amt * 100,null))){
                    refreshContent();
                    myDialog.dismiss();
                }
                myDialog.dismiss();
            }).show();
        });
        findViewById(R.id.vip).setOnClickListener(view -> {
            VipInfoDialog vipInfoDialog = new VipInfoDialog(mainActivity);
            vipInfoDialog.setYesOnclickListener(dialog -> {
                if (showVipInfo(dialog.getVip(),false)){
                    refreshContent();
                    dialog.dismiss();
                }
            }).show();
        });
        findViewById(R.id.all_discount).setOnClickListener(view -> {
            ChangeNumOrPriceDialog dialog = new ChangeNumOrPriceDialog(mainActivity,mainActivity.getString(R.string.discount_sz),String.format(Locale.CHINA,"%d",100));
            dialog.setYesOnclickListener(myDialog -> {
                if (initPayContent(mainActivity.discount(myDialog.getContentToDouble(),""))){
                    refreshContent();
                    myDialog.dismiss();
                }
            }).show();
        });
        findViewById(R.id.remark).setOnClickListener(v -> {
            if (mRemarkEt.getVisibility() == View.VISIBLE){
                mRemarkEt.clearFocus();
                mRemarkEt.getText().clear();
                mRemarkEt.setVisibility(View.GONE);
                mCashMoneyEt.requestFocus();
            }else{
                mRemarkEt.setVisibility(View.VISIBLE);
                mRemarkEt.requestFocus();
            }
        });

        //初始化数字键盘
        ConstraintLayout keyboard_linear_layout;
        keyboard_linear_layout = findViewById(R.id.keyboard);
        for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
            View tmp_v = keyboard_linear_layout.getChildAt(i);
            int id = tmp_v.getId();
            if (tmp_v instanceof Button){
                switch (id){
                    case R.id._back:
                        findViewById(id).setOnClickListener(v -> {
                            View view =  getCurrentFocus();
                            if (view != null) {
                                if (view.getId() == R.id.cash_amt) {
                                    EditText tmp_edit = ((EditText)view);
                                    Editable editable = tmp_edit.getText();
                                    int index = tmp_edit.getSelectionStart(),end = tmp_edit.getSelectionEnd();
                                    if (index !=end && end == editable.length()){
                                        tmp_edit.setText(mainActivity.getString(R.string.d_zero_point_sz));
                                    }else{
                                        if (index == 0)return;
                                        if (index > editable.length())index = editable.length();
                                        if (view.getId() == R.id.cash_amt) {
                                            if (index == editable.toString().indexOf(".") + 1) {
                                                tmp_edit.setSelection(index - 1);
                                            } else if (index > editable.toString().indexOf(".")) {
                                                editable.replace(index - 1, index, "0");
                                                tmp_edit.setSelection(index - 1);
                                            } else {
                                                editable.delete(index - 1, index);
                                            }
                                        }
                                    }
                                }
                            }
                        });
                        break;
                    case R.id._cancel:
                        mCancel.setOnClickListener(v -> PayDialog.this.dismiss());
                        break;
                    case R.id._ok:
                        mOK.setOnClickListener(v -> {
                            v.setEnabled(false);
                            cash_pay();
                            v.postDelayed(()->v.setEnabled(true),300);
                        });
                        break;
                        default:
                            tmp_v.setOnClickListener(button_click);
                            break;
                }
            }
        }

        //根据金额设置按钮数字
         autoShowValueFromPayAmt();
    }

    @Override
    public void dismiss(){
        super.dismiss();
    }
    @Override
    public void show(){
        super.show();
        refreshContent();
    }
    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
        mWindow = getWindow();
    }
    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        mWindow = getWindow();
    }

    public PayDialog setPayFinishListener(onPayListener listener) {
        this.mPayListener = listener;
        return  this;
    }

    public JSONArray getContent(){
        return mPayDetailViewAdapter.getDatas();
    }

    public interface onPayListener {
        void onStart(PayDialog myDialog);
        void onProgress(PayDialog myDialog,final String info);
        void onSuccess(PayDialog myDialog);
        void onError(PayDialog myDialog,final String err);
    }

    private View.OnClickListener button_click = v -> {
        View view =  getCurrentFocus();
        if (view != null) {
            if (view.getId() == R.id.cash_amt) {
                EditText tmp_edit = ((EditText)view);
                Editable editable = tmp_edit.getText();
                int index = tmp_edit.getSelectionStart(),point_index = editable.toString().indexOf(".");
                String sz_button = ((Button) v).getText().toString();
                if (-1 != point_index && tmp_edit.getSelectionEnd() == editable.length()){
                    editable.replace(0, editable.length(),sz_button.concat(mainActivity.getString(R.string.d_zero_point_sz)));
                    point_index = editable.toString().indexOf(".");
                    tmp_edit.setSelection(point_index);
                }else{
                    if (".".equals(sz_button)) {
                        if (-1 != point_index){
                            tmp_edit.setSelection(point_index + 1);
                        }else{
                            editable.insert(index, sz_button);
                        }
                    } else {
                        if (-1 != point_index && index > point_index) {
                            if (index != tmp_edit.length())
                                editable.delete(index, index + 1).insert(index, sz_button);
                        } else {
                            if (index == 0 && "0".equals(sz_button)) return;
                            editable.insert(index, sz_button);
                        }
                    }
                }
            }
        }
    };

    private void initPayMethod(){
        mPayMethodViewAdapter = new PayMethodViewAdapter(mainActivity,(int) mainActivity.getResources().getDimension(R.dimen.pay_method_width));
        mPayMethodViewAdapter.setDatas("1");
        mPayMethodViewAdapter.setOnItemClickListener((v, pos) -> {
            JSONObject pay_method = mPayMethodViewAdapter.getItem(pos);
            if (pay_method != null){
                try {
                    pay_method = Utils.JsondeepCopy(pay_method);
                    String pay_method_id = pay_method.getString("pay_method_id");
                    if (PayMethodViewAdapter.CASH_METHOD_ID.equals(pay_method_id)) {
                        mOK.callOnClick();
                    } else {
                        if (verifyPayBalance()) {
                            if (Utils.equalDouble(mPay_balance, 0) && mPayDetailViewAdapter.findPayDetailById(pay_method_id) == null) {//剩余金额为零，同时不存在此付款方式的记录。
                                MyDialog.SnackbarMessage(mWindow, "剩余金额为零！", getCurrentFocus());
                            } else {
                                PayMethodDialog payMethodDialog = new PayMethodDialog(mainActivity, pay_method);
                                payMethodDialog.setPayAmt(mPay_balance);
                                payMethodDialog.setYesOnclickListener(dialog -> {
                                    JSONObject jsonObject = dialog.getContent();
                                    if (jsonObject != null) {
                                        mPayDetailViewAdapter.addPayDetail(jsonObject);
                                        dialog.dismiss();
                                    }
                                }).setCancelListener(dialog -> {
                                    mPayMethodViewAdapter.showDefaultPayMethod(null);
                                    dialog.dismiss();
                                }).show();
                            }
                        }else{
                            MyDialog.SnackbarMessage(mWindow,"剩余付款金额不能小于零！",mPayBalanceTv);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    MyDialog.ToastMessage("付款错误：" + e.getMessage(),mainActivity,null);
                }
            }
        });
        RecyclerView recyclerView = findViewById(R.id.pay_method_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL,false));
        recyclerView.addItemDecoration(new PayMethodItemDecoration(2));
        recyclerView.setAdapter(mPayMethodViewAdapter);
    }

    private void initPayDetailViewAdapter() {
        mPayDetailViewAdapter = new PayDetailViewAdapter(mainActivity);
        mPayDetailViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                JSONArray jsonArray = getContent();
                double pay_amt = 0.0,zl_amt = 0.0,sale_amt;
                mOpenCashbox = false;
                try {
                    for (int i = 0,length = jsonArray.size();i < length;i ++){//第一个为表头
                        JSONObject object = jsonArray.getJSONObject(i);
                        pay_amt += object.getDouble("pamt");
                        zl_amt += object.getDouble("pzl");
                        if (PayMethodViewAdapter.CASH_METHOD_ID.equals(object.getString("pay_method_id"))){
                            mOpenCashbox = true;
                        }
                    }

                    Logger.d("amt:%f - zl_amt:%f = %f",pay_amt,zl_amt,pay_amt - zl_amt);

                    mAmt_received = pay_amt - zl_amt;
                    mPay_balance = mPay_amt - mAmt_received;
                    mCashAmt = mPay_balance;

                    refreshContent();

                    if (verifyPayBalance()){
                        if (Utils.equalDouble(mActual_amt,mAmt_received)){//支付明细数据发送变化后，计算是否已经付款完成，如果完成触发支付完成事件
                            sale_amt = mainActivity.getSaleSumAmt();
                            pay_amt = mPayDetailViewAdapter.getPaySumAmt();
                            if (Utils.equalDouble(sale_amt,pay_amt)){
                                if (mPayListener != null){
                                    mPayListener.onStart(PayDialog.this);
                                }
                            }else{
                                MyDialog.displayErrorMessage(null,String.format(Locale.CHINA,"销售金额:%f  不等于 付款金额:%f",sale_amt,pay_amt),mainActivity);
                            }
                        }
                    }else{
                        MyDialog.SnackbarMessage(mWindow,"剩余付款金额不能小于零！",mPayBalanceTv);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.ToastMessage("付款错误：" + e.getMessage(),mainActivity,null);
                }
            }
        });
        RecyclerView recyclerView = findViewById(R.id.pay_detail_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL,false));
        recyclerView.addItemDecoration(new DividerItemDecoration(mainActivity,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mPayDetailViewAdapter);
    }
    public void antoMol(final JSONArray datas){
        JSONObject object = new JSONObject();
        double value = 0.0,sum = 0.0,old_amt = 0.0,disSumAmt = 0.0,disc = 0.0;
        if (SQLiteHelper.getLocalParameter("auto_mol",object)){
            int v = 0;
            if (object.getIntValue("s") == 1){
                for (int i = 0,length = datas.size();i < length; i ++){
                    JSONObject jsonObject = datas.getJSONObject(i);
                    if (null != jsonObject){
                        old_amt += jsonObject.getDoubleValue("old_amt");
                        disSumAmt += jsonObject.getDoubleValue("discount_amt");
                    }
                }
                sum = old_amt - disSumAmt;

                v = object.getIntValue("v");
                switch (v){
                    case 1://四舍五入到元
                        value =sum - Double.valueOf(String.format(Locale.CHINA,"%.0f",sum));
                        break;
                    case 2://四舍五入到角
                        value =sum - Double.valueOf(String.format(Locale.CHINA,"%.1f",sum));
                        break;
                }
                if (!Utils.equalDouble(old_amt,0.0))
                    mainActivity.discount((sum - value) / old_amt * 100,null);
            }
        }else{
            MyDialog.ToastMessage("自动抹零错误：" + object.getString("info"),mainActivity,null);
        }
     }
    public boolean initPayContent(JSONArray datas){
        boolean isTrue = true;
        if (null == datas)return false;
        clearContent();
        antoMol(datas);
        for (int i = 0,length = datas.size();i < length; i ++){
            JSONObject jsonObject = datas.getJSONObject(i);
            mOrder_amt += jsonObject.getDouble("old_amt");
            mDiscount_amt += jsonObject.getDoubleValue("discount_amt");
            mActual_amt = mOrder_amt - mDiscount_amt;

            mPay_amt = mActual_amt;
            mPay_balance = mActual_amt - mAmt_received;//剩余付款金额等于应收金额已收金额
            mCashAmt = mPay_balance;
        }
        if (null != mPayDetailViewAdapter)mPayDetailViewAdapter.notifyDataSetChanged();
        return isTrue;
    }

    private void refreshContent(){
        mOrderAmtTv.setText(String.format(Locale.CHINA,"%.2f",mOrder_amt));
        mDiscountAmtTv.setText(String.format(Locale.CHINA,"%.2f",mDiscount_amt));
        mActualAmtTv.setText(String.format(Locale.CHINA,"%.2f",mActual_amt));
        mCashMoneyEt.setText(String.format(Locale.CHINA,"%.2f",mCashAmt));
        mPayAmtTv.setText(String.format(Locale.CHINA,"%.2f",mPay_amt));
        mAmtReceivedTv.setText(String.format(Locale.CHINA,"%.2f",mAmt_received));
        mPayBalanceTv.setText(String.format(Locale.CHINA,"%.2f",mPay_balance));
        mZlAmtEt.setText(String.format(Locale.CHINA,"%.2f",mZlAmt));

        mCashMoneyEt.selectAll();
    }

    private void clearContent(){
        mOrder_amt = 0.0;
        mDiscount_amt = 0.0;
        mActual_amt = 0.0;
        mPay_amt = 0.0;
        mAmt_received = 0.0;
        mPay_balance = 0.0;
        mCashAmt = 0.0;
        mZlAmt = 0.0;
    }
    private void cash_pay(){
        JSONObject pay_method_json;
        if (verifyPayBalance()){
            if (Utils.equalDouble(mPay_balance,0.0) && mPayDetailViewAdapter.getDatas().size() != 0){
                mPayDetailViewAdapter.notifyDataSetChanged();
            }else{
                if ((pay_method_json = mPayMethodViewAdapter.get_pay_method(PayMethodViewAdapter.CASH_METHOD_ID)) != null){
                    pay_method_json = Utils.JsondeepCopy(pay_method_json);
                    pay_method_json.put("pay_code",getCashPayCode());
                    pay_method_json.put("pamt",mCashAmt);
                    pay_method_json.put("pzl",String.format(Locale.CHINA,"%.2f",mZlAmt));
                    pay_method_json.put("v_num","");
                    mPayDetailViewAdapter.addPayDetail(pay_method_json);
                }else{
                    MyDialog.ToastMessage("现金付款方式不存在！",mainActivity,null);
                }
            }
        }else{
            MyDialog.SnackbarMessage(mWindow,"剩余付款金额不能小于零！",mPayBalanceTv);
        }
    }


    private JSONObject generateOrderInfo() throws JSONException {
        double sale_sum_amt = 0.0,dis_sum_amt = 0.0,total = 0.0,zl_amt = 0.0;

        long time = System.currentTimeMillis() / 1000;

        JSONObject order_info = new JSONObject(),data = new JSONObject(),tmp_json;
        JSONArray orders = new JSONArray(),combination_goods = new JSONArray(),sales_data,pays_data;
        StringBuilder err = new StringBuilder();

        String order_code = mainActivity.getOrderCode(),zk_cashier_id = mainActivity.getDisCashierId();

        //处理销售明细
        SaleGoodsViewAdapter saleGoodsViewAdapter = mainActivity.getSaleGoodsViewAdapter();
        sales_data = Utils.JsondeepCopy(saleGoodsViewAdapter.getDatas());//不能直接获取引用，需要重新复制一份否则会修改原始数据；如果业务不能正常完成，之前数据会遭到破坏
        for(int i = 0;i < sales_data.size();i ++){
            tmp_json = sales_data.getJSONObject(i);
            int gp_id = tmp_json.getIntValue("gp_id");

            sale_sum_amt += tmp_json.getDouble("sale_amt");
            dis_sum_amt += tmp_json.getDouble("discount_amt");

            if (-1 != gp_id){
                tmp_json = (JSONObject) sales_data.remove(i--);
                if (!saleGoodsViewAdapter.splitCombinationalGoods(combination_goods,gp_id,tmp_json.getDouble("price"),tmp_json.getDouble("xnum"),err)){
                    throw new JSONException("拆分组合商品错误，" + err);
                }
                Logger.d_json(combination_goods.toString());
            }else{
                tmp_json.put("order_code",order_code);
                tmp_json.put("zk_cashier_id",zk_cashier_id);//使用折扣的收银员ID,默认当前收银员
                tmp_json.put("total_money",tmp_json.remove("sale_amt"));
                tmp_json.put("y_price",tmp_json.remove("old_price"));

                ///删除不需要的内容
                tmp_json.remove("goods_id");
                tmp_json.remove("discount");
                tmp_json.remove("discount_amt");
                tmp_json.remove("old_amt");
                tmp_json.remove("goods_title");
                tmp_json.remove("unit_name");
                tmp_json.remove("yh_mode");
                tmp_json.remove("yh_price");
            }
        }
        //处理组合商品
        for(int i = 0,size = combination_goods.size();i < size;i++){
            tmp_json = combination_goods.getJSONObject(i);
            tmp_json.put("order_code",order_code);
            tmp_json.put("zk_cashier_id",zk_cashier_id);//使用折扣的收银员ID,默认当前收银员
            tmp_json.put("total_money",String.format(Locale.CHINA,"%.2f",tmp_json.getDouble("xnum") * tmp_json.getDouble("price")));
            tmp_json.put("y_price",tmp_json.getDouble("retail_price"));

            sales_data.add(tmp_json);
        }

        //处理付款明细
        pays_data = Utils.JsondeepCopy(getContent());//不能直接获取引用，需要重新复制一份否则会修改原始数据；如果业务不能正常完成，之前数据会遭到破坏
        for (int i= 0,size = pays_data.size();i < size;i++){
            tmp_json = pays_data.getJSONObject(i);
            JSONObject pay = new JSONObject();

            pay.put("order_code",order_code);
            pay.put("pay_code",tmp_json.getString("pay_code"));
            pay.put("pay_method",tmp_json.getString("pay_method_id"));
            pay.put("pay_money",tmp_json.getDouble("pamt"));
            pay.put("is_check",tmp_json.getDouble("is_check"));
            pay.put("pay_time",0);
            pay.put("pay_status",1);
            pay.put("pay_serial_no","");//第三方返回的支付流水号
            pay.put("remark","");
            pay.put("zk_money",0.0);
            pay.put("pre_sale_money",tmp_json.getDouble("pamt"));
            pay.put("give_change_money",tmp_json.getDouble("pzl"));
            pay.put("discount_money",0.0);
            pay.put("xnote","");
            pay.put("return_code","");
            pay.put("v_num",tmp_json.getString("v_num"));
            pay.put("print_info","");

            pays_data.add(i,pay);
        }

        //处理单据
        total = sale_sum_amt + dis_sum_amt;

        order_info.put("stores_id",mainActivity.getStoreInfo().getString("stores_id"));
        order_info.put("order_code",order_code);
        order_info.put("total",total);
        order_info.put("discount_price",sale_sum_amt);
        order_info.put("discount_money",total);
        order_info.put("discount",String.format(Locale.CHINA,"%.4f",sale_sum_amt / total));
        order_info.put("cashier_id",mainActivity.getCashierInfo().getString("cas_id"));
        order_info.put("addtime",time);
        order_info.put("pos_code",mainActivity.getPosNum());
        order_info.put("order_status",1);//订单状态（1未付款，2已付款，3已取消，4已退货）
        order_info.put("pay_status",1);//支付状态（1未支付，2已支付，3支付中）
        order_info.put("pay_time",time);
        order_info.put("upload_status",1);//上传状态（1未上传，2已上传）
        order_info.put("upload_time",0);
        order_info.put("transfer_status",1);//交班状态（1未交班，2已交班）
        order_info.put("transfer_time",0);
        order_info.put("is_rk",2);//是否已经扣减库存（1是，2否）
        if (mVip != null){
            order_info.put("member_id",mVip.getString("member_id"));
            order_info.put("mobile",mVip.getString("mobile"));
            order_info.put("name",mVip.getString("name"));
            order_info.put("card_code",mVip.getString("card_code"));
        }
        order_info.put("sc_ids","");
        order_info.put("sc_tc_money",0.00);
        order_info.put("zl_money",zl_amt);
        order_info.put("ss_money",0.0);
        order_info.put("remark",mRemarkEt.getText().toString());
        order_info.put("zk_cashier_id",zk_cashier_id);//使用折扣的收银员ID,默认当前收银员
        orders.add(order_info);

        data.put("retail_order",orders);
        data.put("retail_order_goods",sales_data);
        data.put("retail_order_pays",pays_data);

        return data;
    }
    public boolean saveOrderInfo(final StringBuilder err){
        boolean code;
        JSONObject count_json = new JSONObject(),data;
        List<String>  tables = Arrays.asList("retail_order","retail_order_goods","retail_order_pays"),
                retail_order_cols = Arrays.asList("stores_id","order_code","discount","discount_price","total","cashier_id","addtime","pos_code","order_status","pay_status","pay_time","upload_status",
                        "upload_time","transfer_status","transfer_time","is_rk","mobile","name","card_code","sc_ids","sc_tc_money","member_id","discount_money","zl_money","ss_money","remark","zk_cashier_id"),
                retail_order_goods_cols = Arrays.asList("order_code","barcode_id","xnum","price","buying_price","retail_price","trade_price","cost_price","ps_price","tax_rate","tc_mode","tc_rate","gp_id",
                        "zk_cashier_id","total_money", GoodsInfoViewAdapter.W_G_MARK,"conversion","barcode","y_price"),
                retail_order_pays_cols = Arrays.asList("order_code","pay_method","pay_money","pay_time","pay_status","pay_serial_no","pay_code","remark","is_check","zk_money","pre_sale_money","give_change_money",
                        "discount_money","xnote","card_no","return_code","v_num","print_info");

        try {
            data = generateOrderInfo();

            if ((code = SQLiteHelper.execSql(count_json,"select count(order_code) counts from retail_order where order_code = '" + mainActivity.getOrderCode() +"' and stores_id = '" + mainActivity.getStoreInfo().getString("stores_id") +"'"))){
                if (0 == count_json.getIntValue("counts")){
                    if (!(code = SQLiteHelper.execSQLByBatchFromJson(data,tables,Arrays.asList(retail_order_cols,retail_order_goods_cols,retail_order_pays_cols),err,0))){
                        err.insert(0,"保存订单信息错误：");
                    }
                }else{
                    code = false;
                    err.append("本地已存在此订单信息，请重新下单！");
                }
            }else{
                err.append("查询订单信息错误：").append(count_json.getString("info"));
            }
        } catch (JSONException e) {
            code = false;
            err.append("保存订单信息错误：").append(e.getMessage());
            e.printStackTrace();
        }

        return code;
    }
    public void requestPay(final String order_code, final String url, final String appId, final String appScret, final String stores_id, final String pos_num){
        if (mPayListener != null)mainActivity.runOnUiThread(()->mPayListener.onProgress(PayDialog.this,"正在支付..."));
        mPayStatus = true;
        int is_check;
        long pay_time = 0;
        double discount_money = 0.0;
        String pay_method_id,pay_money,pay_code,unified_pay_order,unified_pay_query,sz_param,v_num,third_pay_order_id = "",discount_xnote = "";
        JSONObject retJson,pay_detail,pay_method_json,info_json;
        HttpRequest httpRequest;
        final StringBuilder err = new StringBuilder();
        ContentValues values = new ContentValues();
        JSONArray pays = SQLiteHelper.getListToJson("select pay_method,pay_money,pay_code,is_check,v_num from retail_order_pays where order_code = '" + order_code +"'",0,0,false,err);
        if (null != pays){
            try{
                for (int i = 0,size = pays.size();i < size && mPayStatus;i++){
                    pay_detail = pays.getJSONObject(i);

                    is_check = pay_detail.getIntValue("is_check");
                    pay_code = pay_detail.getString("pay_code");
                    v_num = pay_detail.getString("v_num");

                    pay_time = System.currentTimeMillis()/1000;
                    //发起支付请求
                    if (is_check != 2){

                        httpRequest = new HttpRequest();

                        pay_method_id = pay_detail.getString("pay_method");
                        pay_money = pay_detail.getString("pay_money");

                        pay_method_json = mPayMethodViewAdapter.get_pay_method(pay_method_id);

                        if (pay_method_json != null){

                            unified_pay_order = pay_method_json.getString("unified_pay_order");
                            unified_pay_query = pay_method_json.getString("unified_pay_query");

                            if ("null".equals(unified_pay_order) || "".equals(unified_pay_order)){
                                unified_pay_order = "/api/pay2/index";
                            }
                            if ("null".equals(unified_pay_query) || "".equals(unified_pay_query)){
                                unified_pay_query = "/api/pay2_query/query";
                            }

                            JSONObject data_ = new JSONObject();
                            data_.put("appid",appId);
                            data_.put("stores_id",stores_id);
                            data_.put("order_code",order_code);
                            data_.put("pos_num",pos_num);
                            data_.put("is_wuren",2);
                            data_.put("order_code_son",pay_code);
                            data_.put("pay_money", pay_money);
                            data_.put("pay_method",pay_method_id);
                            data_.put("pay_code_str",v_num);

                            sz_param = HttpRequest.generate_request_parm(data_,appScret);

                            Logger.i("结账支付参数:url:%s%s,param:%s",url ,unified_pay_order,sz_param);
                            retJson = httpRequest.sendPost(url + unified_pay_order,sz_param,true);
                            Logger.i("结账支付请求返回:%s",retJson.toString());

                            switch (retJson.getIntValue("flag")){
                                case 0:
                                    mPayStatus = false;
                                    err.append(retJson.getString("info"));
                                    break;
                                case 1:
                                    info_json = JSON.parseObject(retJson.getString("info"));
                                    switch (info_json.getString("status")){
                                        case "n":
                                            mPayStatus = false;
                                            err.append(info_json.getString("info"));
                                            break;
                                        case "y":
                                            int res_code = info_json.getIntValue("res_code");
                                            switch (res_code){
                                                case 1://支付成功
                                                    third_pay_order_id = "";
                                                    pay_time = System.currentTimeMillis()/1000;
                                                    break;
                                                case 2:
                                                    mPayStatus = false;
                                                    err.append(info_json.getString("info"));
                                                    break;
                                                case 3:
                                                case 4:
                                                    if (mPayListener != null)
                                                        mainActivity.runOnUiThread(()->mPayListener.onProgress(PayDialog.this,"正在查询支付状态..."));

                                                    while (mPayStatus && (res_code == 3 ||  res_code == 4)){
                                                        final JSONObject object = new JSONObject();
                                                        object.put("appid",appId);
                                                        object.put("pay_code",info_json.getString("pay_code"));
                                                        object.put("order_code_son",info_json.getString("order_code_son"));
                                                        if (res_code == 4){
                                                            mainActivity.runOnUiThread(()->{
                                                                ChangeNumOrPriceDialog password_dialog = new ChangeNumOrPriceDialog(mainActivity,"请输入密码","");
                                                                password_dialog.setOnDismissListener(dialog -> {
                                                                    synchronized (this){
                                                                        notifyAll();
                                                                    }
                                                                });
                                                                password_dialog.setYesOnclickListener(myDialog -> {
                                                                    try {
                                                                        object.put("pay_password",myDialog.getContentToStr());
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                    myDialog.dismiss();
                                                                }).setNoOnclickListener(myDialog -> {
                                                                    mPayStatus = false;
                                                                    err.append("密码验证已取消！");
                                                                    myDialog.dismiss();
                                                                }).show();
                                                            });
                                                            synchronized (this){
                                                                try {
                                                                    wait();
                                                                } catch (InterruptedException e) {
                                                                    e.printStackTrace();
                                                                }
                                                            }
                                                        }
                                                        if (mPayStatus){
                                                            sz_param = HttpRequest.generate_request_parm(object,appScret);

                                                            Logger.i("结账支付查询参数:url:%s%s,param:%s",url,unified_pay_order,sz_param);
                                                            retJson = httpRequest.sendPost(url + unified_pay_query,sz_param,true);
                                                            Logger.i("结账支付查询返回:%s",retJson.toString());

                                                            switch (retJson.getIntValue("flag")){
                                                                case 0:
                                                                    mPayStatus = false;
                                                                    err.append(retJson.getString("info"));
                                                                    break;
                                                                case 1:
                                                                    info_json = JSON.parseObject(retJson.getString("info"));
                                                                    Logger.json(info_json.toString());
                                                                    switch (info_json.getString("status")){
                                                                        case "n":
                                                                            mPayStatus = false;
                                                                            err.append(info_json.getString("info"));
                                                                            break;
                                                                        case "y":
                                                                            res_code = info_json.getIntValue("res_code");
                                                                            if (res_code == 1){//支付成功
                                                                                Logger.d_json(info_json.toString());
                                                                                if (info_json.containsKey("xnote")){
                                                                                    JSONObject jsonObject = mPayDetailViewAdapter.findPayDetailById(pay_method_id);
                                                                                    if (jsonObject != null)
                                                                                        jsonObject.put("xnote",info_json.getJSONArray("xnote"));
                                                                                }
                                                                                third_pay_order_id = info_json.getString("pay_code");
                                                                                discount_money = info_json.getDouble("discount");
                                                                                pay_time = info_json.getLong("pay_time");
                                                                                break;
                                                                            }
                                                                            if (res_code == 2){//支付失败
                                                                                mPayStatus = false;
                                                                                err.append(info_json.getString("info"));
                                                                                break;
                                                                            }
                                                                            break;
                                                                    }
                                                                    break;
                                                            }
                                                        }
                                                    }
                                                    break;
                                            }
                                            break;
                                    }
                                    break;
                            }

                        }else{
                            mPayStatus = false;
                            err.append("付款方式不存在:pay_method_id--").append(pay_method_id);
                        }
                    }
                }
            }catch (JSONException e){
                e.printStackTrace();
                mPayStatus = false;
                err.append(e.getMessage());
            }
        }else{
            mPayStatus = false;
        }

        if (!mPayStatus){
            values.put("order_status",3);
            values.put("spare_param1",err.toString());
            if (!SQLiteHelper.execUpdateSql("retail_order",values,"order_code = ?",new String[]{order_code},err)){
                Logger.d("更新订单状态错误：",err);
            }
            if (mPayListener != null)
                mainActivity.runOnUiThread(()-> mPayListener.onError(PayDialog.this,err.toString()));
        }else{
            List<String> sqls = new ArrayList<>();
            String sql = "update retail_order set order_status = 2,pay_status = 2,pay_time ='" + pay_time +"' where order_code = '" + order_code + "'";

            sqls.add(sql);

            sql = "update retail_order_pays set pay_status = 2,pay_serial_no = '" + third_pay_order_id +"',pay_time = '" + pay_time + "',discount_money = '" + discount_money +"',xnote = '" + discount_xnote +"',return_code = '"+ third_pay_order_id +"' where order_code = '" + order_code + "'";

            sqls.add(sql);

            if (!SQLiteHelper.execBatchUpdateSql(sqls,err)){
                if (mPayListener != null)
                    mainActivity.runOnUiThread(()-> mPayListener.onError(PayDialog.this,err.toString()));
            }else{
                if (mPayListener != null){
                    mainActivity.runOnUiThread(()-> mPayListener.onSuccess(PayDialog.this));
                }
            }
        }
    }

    private void initCsahText(){
        mCashMoneyEt = findViewById(R.id.cash_amt);
        mCashMoneyEt.setText(String.format(Locale.CHINA,"%.2f",mActual_amt));
        mCashMoneyEt.setSelectAllOnFocus(true);
        mCashMoneyEt.setOnFocusChangeListener((view, b) -> Utils.hideKeyBoard((EditText) view));
        mCashMoneyEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                try {
                    mCashAmt = Double.valueOf(editable.toString());
                }catch (NumberFormatException e){
                    mCashAmt = 0.0;
                }
                if ((mZlAmt = mCashAmt - mPay_balance) > 0){
                    if (mZlAmt < 100)
                        mZlAmtEt.setText(String.format(Locale.CHINA,"%.2f",mZlAmt));
                    else{
                        mCashMoneyEt.setText(mPayBalanceTv.getText());
                        mCashMoneyEt.selectAll();
                        MyDialog.ToastMessage("找零不能大于100",mainActivity,null);
                    }
                }else{
                    mZlAmt = 0.00;
                    mZlAmtEt.setText(mainActivity.getText(R.string.z_p_z_sz));
                }
            }
        });
        mCashMoneyEt.postDelayed(()-> mCashMoneyEt.requestFocus(),300);
    }
    private String getCashPayCode() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date())+ mainActivity.getPosNum() + Utils.getNonce_str(8);
    }
    private boolean verifyPayBalance(){
      return (mPay_balance > 0.0 || Utils.equalDouble(mPay_balance,0.0));
    }
    private void autoShowValueFromPayAmt(){
        int amt = (int)mCashAmt,tmp;
        Button first = findViewById(R.id._ten),sec = findViewById(R.id._twenty),third = findViewById(R.id._fifty),fourth = findViewById(R.id._one_hundred);
        tmp = amt +(5 - amt % 5);
        first.setText(String.valueOf(tmp));
        sec.setText(String.valueOf((tmp = tmp +(10- tmp % 10))));
        third.setText(String.valueOf((tmp = tmp +(20- tmp % 20))));
        fourth.setText(String.valueOf( tmp +(50- tmp % 50)));
    }

    private String c_format_58(JSONObject format_info,final JSONArray sales){

        StringBuilder info = new StringBuilder();
        String store_name = "",footer_c,new_line ,new_line_16,new_line_2,new_line_d,line,pos_num,cas_name;
        int print_count = 1,footer_space = 5;
        JSONObject cas_info = mainActivity.getCashierInfo(),st_info = mainActivity.getStoreInfo();

        store_name = format_info.getString("s_n");
        pos_num = Utils.getNullOrEmptyStringAsDefault(cas_info,"pos_num","");
        cas_name = Utils.getNullOrEmptyStringAsDefault(cas_info,"cas_name","");;

        footer_c = format_info.getString("f_c");
        print_count = Utils.getNotKeyAsDefault(format_info,"p_c",1);
        footer_space = Utils.getNotKeyAsDefault(format_info,"f_s",5);
        new_line = "\r\n";//Printer.commandToStr(Printer.NEW_LINE);
        new_line_16 = Printer.commandToStr(Printer.LINE_SPACING_16);
        new_line_2 = Printer.commandToStr(Printer.LINE_SPACING_2);
        new_line_d = Printer.commandToStr(Printer.LINE_SPACING_DEFAULT);
        line = "--------------------------------";

        if (mOpenCashbox)//开钱箱
            info.append(Printer.commandToStr(Printer.OPEN_CASHBOX));

        while (print_count-- > 0) {//打印份数
            info.append(Printer.commandToStr(Printer.DOUBLE_HEIGHT)).append(Printer.commandToStr(Printer.ALIGN_CENTER))
                    .append(store_name.length() == 0 ? st_info.getString("stores_name") : store_name).append(new_line).append(new_line).append(Printer.commandToStr(Printer.NORMAL)).
                    append(Printer.commandToStr(Printer.ALIGN_LEFT));

            info.append(Printer.printTwoData(1, "店号：".concat(st_info.getString("stores_id")), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))).append(new_line);
            info.append(Printer.printTwoData(1, "机号：".concat(pos_num), "收银员：".concat(cas_name))).append(new_line);
            info.append("单号：").append(mainActivity.getOrderCode()).append(new_line).append(new_line);

            info.append("商品名称      单价   数量   小计").append(new_line_2).append(new_line).append(line).append(new_line_2).append(new_line).append(new_line_d);

            //商品明细
            JSONObject info_obj;
            double discount_amt = 0.0, xnum = 0.0;
            int units_num = 0, type = 1;//商品属性 1普通 2称重 3用于服装
            for (int i = 0, size = sales.size(); i < size; i++) {
                info_obj = sales.getJSONObject(i);
                if (info_obj != null) {
                    type = info_obj.getIntValue("type");
                    if (type == 2) {
                        units_num += 1;
                    } else {
                        units_num += info_obj.getIntValue("xnum");
                    }
                    xnum = info_obj.getDoubleValue("xnum");
                    discount_amt = info_obj.getDoubleValue("discount_amt");

                    if (i > 0) {
                        info.append(new_line_d);
                    }

                    info.append(Printer.commandToStr(Printer.BOLD)).append(info_obj.getString("goods_title")).append(new_line).append(Printer.commandToStr(Printer.BOLD_CANCEL));
                    info.append(Printer.printTwoData(1, info_obj.getString("barcode"),
                            Printer.printThreeData(16, info_obj.getString("price"), type == 2 ? String.valueOf(xnum) : String.valueOf((int) xnum), info_obj.getString("sale_amt"))));

                    if (!Utils.equalDouble(discount_amt, 0.0)) {
                        info.append(new_line).append(Printer.printTwoData(1, "原价：".concat(info_obj.getString("old_price")), "优惠：".concat(String.valueOf(discount_amt))));
                    }
                    if (i + 1 != size)
                        info.append(new_line_16);
                    else
                        info.append(new_line_2);

                    info.append(new_line);
                }
            }
            info.append(line).append(new_line_2).append(new_line).append(new_line_d);

            info.append(Printer.printTwoData(1, "总价：".concat(String.format(Locale.CHINA, "%.2f", mOrder_amt))
                    , "件数：".concat(String.valueOf(units_num)))).append(new_line);
            ;
            info.append(Printer.printTwoData(1, "应收：".concat(String.format(Locale.CHINA, "%.2f", mActual_amt)), "优惠：".concat(String.format(Locale.CHINA, "%.2f", mDiscount_amt)))).
                    append(new_line_2).append(new_line).append(line).append(new_line_2).append(new_line).append(new_line_d);

            //支付方式
            double zl = 0.0, pamt = 0.0;
            JSONArray pays = getContent();
            for (int i = 0, size = pays.size(); i < size; i++) {
                info_obj = pays.getJSONObject(i);
                zl = info_obj.getDoubleValue("pzl");
                pamt = info_obj.getDoubleValue("pamt");
                info.append(Utils.getNullOrEmptyStringAsDefault(info_obj,"name","")).append("：").append(pamt - zl).append("元").append(new_line);

                info.append("预收：").append(pamt);
                if (!Utils.equalDouble(zl, 0.0)) {
                    info.append(",").append("找零：").append(zl);
                }
                if (info_obj.containsKey("xnote")) {
                    JSONArray xnotes = info_obj.getJSONArray("xnote");
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
            info.append("门店热线：").append(Utils.getNullOrEmptyStringAsDefault(st_info,"telphone","")).append(new_line);
            info.append("门店地址：").append(Utils.getNullOrEmptyStringAsDefault(st_info,"region","")).append(new_line);

            info.append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(footer_c);
            for (int i = 0; i < footer_space; i++) info.append(" ").append(new_line);

            if (print_count > 0){
                info.append(new_line).append(new_line).append(new_line);
            }
        }

        Logger.d(info);

        return info.toString();
    }

    public boolean showVipInfo(@NonNull JSONObject vip,boolean show){//show为true则只显示不再刷新已销售商品
        mVip = vip;
        LinearLayout vip_info_linearLayout = findViewById(R.id.vip_info_linearLayout);
        if (vip_info_linearLayout != null){
            vip_info_linearLayout.setVisibility(View.VISIBLE);
            ((TextView)vip_info_linearLayout.findViewById(R.id.vip_name)).setText(mVip.getString("name"));
            ((TextView)vip_info_linearLayout.findViewById(R.id.vip_phone_num)).setText(mVip.getString("mobile"));
        }
        return show ? show : initPayContent(mainActivity.showVipInfo(vip));
    }

    public String get_print_content(JSONArray sales){
        JSONObject print_format_info = new JSONObject();
        String content = "";
        if (SQLiteHelper.getLocalParameter("c_f_info",print_format_info)){
            if (print_format_info.getIntValue("f") == R.id.checkout_format){
                switch (print_format_info.getIntValue("f_z")){
                    case R.id.f_58:
                        content = c_format_58(print_format_info,sales);
                        break;
                    case R.id.f_76:
                        break;
                    case R.id.f_80:
                        break;
                }
            }
        }else
            MyDialog.ToastMessage("加载打印格式错误：" + print_format_info.getString("info"),mainActivity,getWindow());

        return content;
    }
}
