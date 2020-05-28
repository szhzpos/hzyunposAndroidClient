package com.wyc.cloudapp.dialog.pay;

import android.content.ContentValues;
import android.content.Context;
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
import com.wyc.cloudapp.data.SQLiteHelper;
import com.wyc.cloudapp.dialog.baseDialog.DialogBaseOnMainActivityImp;
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

public final class PayDialog extends DialogBaseOnMainActivityImp {
    private EditText mCashMoneyEt,mZlAmtEt,mRemarkEt;
    private onPayListener mPayListener;
    private PayMethodViewAdapter mPayMethodViewAdapter;
    private PayDetailViewAdapter mPayDetailViewAdapter;
    private TextView mOrderAmtTv,mDiscountAmtTv,mActualAmtTv,mPayAmtTv,mAmtReceivedTv,mPayBalanceTv, mDiscountDescriptionTv;
    private double mOrder_amt = 0.0,mDiscount_amt = 0.0,mActual_amt = 0.0,mPay_amt = 0.0,mAmt_received = 0.0,mPay_balance = 0.0,mCashAmt = 0.0,mZlAmt = 0.0,mMolAmt = 0.0;
    private String mDiscountDesContent = "";
    private Button mOK;
    private JSONObject mVip;
    private boolean mPayStatus = true;
    private Window mWindow;
    public PayDialog(MainActivity context, final String title){
        super(context,title);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //初始化成员
        mOrderAmtTv = findViewById(R.id.order_amt);//单据金额
        mDiscountAmtTv = findViewById(R.id.dis_sum_amt);//折扣金额
        mActualAmtTv = findViewById(R.id.actual_amt);//应收金额
        mPayAmtTv = findViewById(R.id.pay_amt);//付款金额
        mAmtReceivedTv = findViewById(R.id.amt_received);//已收金额
        mPayBalanceTv = findViewById(R.id.pay_balance);//付款余额
        mZlAmtEt = findViewById(R.id.zl_amt);//找零
        mRemarkEt = findViewById(R.id.et_remark);//备注
        mDiscountDescriptionTv = findViewById(R.id.discount_description);//折扣信息

        //初始化支付方式
        initPayMethod();

        //初始化支付明细
        initPayDetailViewAdapter();

        //初始化现金EditText
        initCsahText();

        manualMolBtn();
        initRemarkBtn();
        vipBtn();
        allDiscountBtn();
         //初始化数字键盘
        initKeyboard();

        //根据金额设置按钮数字
         autoShowValueFromPayAmt();
    }
    @Override
    protected int getContentLayoutId(){
        return R.layout.pay_dialog_content_layout;
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
        if (!Utils.equalDouble(mMolAmt,0.0)){
            mMolAmt = 0.0;
            mContext.deleteMolDiscountRecord();
        }
        if (!Utils.equalDouble(mDiscount_amt,0.0)){
            mContext.deleteAlldiscountRecord();
        }
    }

    private void allDiscountBtn(){
        final Button all_discount_btn = findViewById(R.id.all_discount);
        if (null != all_discount_btn)
            all_discount_btn.setOnClickListener(view -> {
                ChangeNumOrPriceDialog dialog = new ChangeNumOrPriceDialog(mContext, mContext.getString(R.string.discount_sz),String.format(Locale.CHINA,"%d",100));
                dialog.setYesOnclickListener(myDialog -> {
                    if (mContext.allDiscount(myDialog.getContent())){
                        deleteMolDiscountRecord();
                        refreshPayContent();
                        myDialog.dismiss();
                    }
                }).show();
            });
    }
    private void vipBtn(){
        final Button vip_btn = findViewById(R.id.vip);
        if (null != vip_btn)
            vip_btn.setOnClickListener(view -> {
                final VipInfoDialog vipInfoDialog = new VipInfoDialog(mContext);
                vipInfoDialog.setYesOnclickListener(dialog -> {
                    deleteMolDiscountRecord();
                    showVipInfo(dialog.getVip(),false);
                    dialog.dismiss();
                }).show();
            });
    }
    private void initRemarkBtn(){
        final Button remark_btn = findViewById(R.id.remark);
        if (null != remark_btn)
            remark_btn.setOnClickListener(v -> {
                if (mRemarkEt.isShown()){
                    mRemarkEt.clearFocus();
                    mRemarkEt.getText().clear();
                    mRemarkEt.setVisibility(View.GONE);
                    mCashMoneyEt.requestFocus();
                }else{
                    mRemarkEt.setVisibility(View.VISIBLE);
                    mRemarkEt.requestFocus();
                }
            });
    }
    private void manualMolBtn(){
        final Button mo_l_btn = findViewById(R.id.mo_l);
        if (mo_l_btn != null)
            mo_l_btn.setOnClickListener(v -> {//手动抹零
                ChangeNumOrPriceDialog changeNumOrPriceDialog = new ChangeNumOrPriceDialog(mContext, mContext.getString(R.string.mo_l_sz),String.format(Locale.CHINA,"%.2f",mActual_amt - ((int)mActual_amt)));
                changeNumOrPriceDialog.setYesOnclickListener(myDialog -> {
                    double mol_amt = mMolAmt = myDialog.getContent();
                    if (!Utils.equalDouble(mol_amt,0.0)){
                        if (mContext.verifyDiscountPermissions(mol_amt / mActual_amt,null)){
                            mContext.manualMol(mol_amt);
                            calculatePayContent();
                            refreshContent();
                        }
                    }
                    myDialog.dismiss();
                }).show();
            });
    }
    private void initKeyboard(){
        ConstraintLayout keyboard_linear_layout;
        keyboard_linear_layout = findViewById(R.id.keyboard);
        if (null != keyboard_linear_layout)
            for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
            View tmp_v = keyboard_linear_layout.getChildAt(i);
            int id = tmp_v.getId();
            if (tmp_v instanceof Button){
                switch (id){
                    case R.id._back:
                        tmp_v.setOnClickListener(v -> {
                            View view =  getCurrentFocus();
                            if (view != null) {
                                if (view.getId() == R.id.cash_amt) {
                                    EditText tmp_edit = ((EditText)view);
                                    Editable editable = tmp_edit.getText();
                                    int index = tmp_edit.getSelectionStart(),end = tmp_edit.getSelectionEnd();
                                    if (index !=end && end == editable.length()){
                                        tmp_edit.setText(mContext.getString(R.string.d_zero_point_sz));
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
                        tmp_v.setOnClickListener(v -> closeWindow());
                        break;
                    case R.id._ok:
                        mOK = (Button) tmp_v;
                        tmp_v.setOnClickListener(v -> {
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
                    editable.replace(0, editable.length(),sz_button.concat(mContext.getString(R.string.d_zero_point_sz)));
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
        mPayMethodViewAdapter = new PayMethodViewAdapter(mContext,(int) mContext.getResources().getDimension(R.dimen.pay_method_width));
        mPayMethodViewAdapter.setDatas("1");
        mPayMethodViewAdapter.setOnItemClickListener((v, pos) -> {
            JSONObject pay_method = mPayMethodViewAdapter.getItem(pos);
            if (pay_method != null){
                try {
                    pay_method = Utils.JsondeepCopy(pay_method);
                    final String pay_method_id = pay_method.getString("pay_method_id");
                    if (PayMethodViewAdapter.CASH_METHOD_ID.equals(pay_method_id)) {
                        mOK.callOnClick();
                    } else {
                        if (verifyPayBalance()) {
                            if (Utils.equalDouble(mPay_balance, 0) && mPayDetailViewAdapter.findPayDetailById(pay_method_id) == null) {//剩余金额为零，同时不存在此付款方式的记录。
                                MyDialog.SnackbarMessage(mWindow, "剩余金额为零！", getCurrentFocus());
                            } else {
                                if (mVip != null){
                                    pay_method.put("card_code",mVip.getString("card_code"));
                                }
                                final PayMethodDialogImp payMethodDialogImp = new PayMethodDialogImp(mContext, pay_method);
                                deleteMolDiscountRecord();//现金之外的付款需要删除抹零金额
                                payMethodDialogImp.setPayAmt(mPay_balance);
                                payMethodDialogImp.setYesOnclickListener(dialog -> {
                                    final JSONObject jsonObject = dialog.getContent();
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
                    MyDialog.ToastMessage("付款错误：" + e.getMessage(), mContext,null);
                }
            }
        });
        final RecyclerView recyclerView = findViewById(R.id.pay_method_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL,false));
        recyclerView.addItemDecoration(new PayMethodItemDecoration(2));
        recyclerView.setAdapter(mPayMethodViewAdapter);
    }

    private void initPayDetailViewAdapter() {
        mPayDetailViewAdapter = new PayDetailViewAdapter(mContext);
        mPayDetailViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                final JSONArray jsonArray = getContent();
                double pay_amt = 0.0,zl_amt = 0.0,sale_amt;
                try {
                    for (int i = 0,length = jsonArray.size();i < length;i ++){//第一个为表头
                        JSONObject object = jsonArray.getJSONObject(i);
                        pay_amt += object.getDouble("pamt");
                        zl_amt += object.getDouble("pzl");
                    }

                    Logger.d("amt:%f - zl_amt:%f = %f",pay_amt,zl_amt,pay_amt - zl_amt);

                    mAmt_received = pay_amt - zl_amt;
                    mPay_balance = mPay_amt - mAmt_received;
                    mCashAmt = mPay_balance;

                    refreshContent();

                    if (verifyPayBalance()){
                        if (Utils.equalDouble(mActual_amt,mAmt_received)){//支付明细数据发送变化后，计算是否已经付款完成，如果完成触发支付完成事件
                            sale_amt = mContext.getSaleSumAmt();
                            pay_amt = mPayDetailViewAdapter.getPaySumAmt();

                            if (Utils.equalDouble(sale_amt,pay_amt)){
                                if (mPayListener != null){
                                    mPayListener.onStart(PayDialog.this);
                                }
                            }else{
                                MyDialog.displayErrorMessage(null,String.format(Locale.CHINA,"销售金额:%f  不等于 付款金额:%f",sale_amt,pay_amt), mContext);
                            }
                        }
                    }else{
                        MyDialog.SnackbarMessage(mWindow,"剩余付款金额不能小于零！",mPayBalanceTv);
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.ToastMessage("付款错误：" + e.getMessage(), mContext,null);
                }
            }
        });
        RecyclerView recyclerView = findViewById(R.id.pay_detail_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mPayDetailViewAdapter);
    }
    private void setMolAmt(){
        double  sum = 0.0,original_amt = 0.0,disSumAmt = 0.0,disc = 0.0;
        final JSONArray datas = mContext.getSaleData();
        final JSONObject object = new JSONObject();
        if (SQLiteHelper.getLocalParameter("auto_mol",object)){
            if (object.getIntValue("s") == 1){
                for (int i = 0,length = datas.size();i < length; i ++){
                    JSONObject jsonObject = datas.getJSONObject(i);
                    if (null != jsonObject){
                        original_amt += jsonObject.getDoubleValue("original_amt");
                        disSumAmt += jsonObject.getDoubleValue("discount_amt");
                    }
                }
                sum = original_amt - disSumAmt;

                int v = object.getIntValue("v");
                switch (v){
                    case 1://四舍五入到元
                        mMolAmt =sum - Double.valueOf(String.format(Locale.CHINA,"%.0f",sum));
                        break;
                    case 2://四舍五入到角
                        mMolAmt =sum - Double.valueOf(String.format(Locale.CHINA,"%.1f",sum));
                        break;
                }
                Logger.d("mMolAmt:%f,sum：%f",mMolAmt,sum);
            }
        }else{
            MyDialog.ToastMessage("自动抹零错误：" + object.getString("info"), mContext,null);
        }
    }
    private void antoMol(){//自动抹零
        setMolAmt();
        if (!Utils.equalDouble(mMolAmt,0.0))
            mContext.autoMol(mMolAmt);
     }
    private void deleteMolDiscountRecord(){
        if (!Utils.equalDouble(mMolAmt,0.0)){
            mMolAmt = 0.0;
            mContext.deleteMolDiscountRecord();
            calculatePayContent();
            refreshContent();
        }
    }
    private void calculatePayContent(){
        JSONObject jsonObject;
        final JSONArray datas = mContext.getSaleData();
        clearContent();
        for (int i = 0,length = datas.size();i < length; i ++){
            jsonObject = datas.getJSONObject(i);
            mOrder_amt += jsonObject.getDouble("original_amt");
            mDiscount_amt += jsonObject.getDoubleValue("discount_amt");
        }
        mActual_amt = mOrder_amt - mDiscount_amt;
        mPay_amt = mActual_amt;
        mPay_balance = mActual_amt - mAmt_received;//剩余付款金额等于应收金额已收金额
        mCashAmt = mPay_balance;
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

        showDiscountDescription();

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
        //mMolAmt = 0.0;
        mDiscountDesContent = "";
    }
    private void showDiscountDescription(){
        mDiscountDesContent = mContext.discountRecordsToString();
        if (mDiscountDesContent.length() != 0){
            if (!mDiscountDescriptionTv.isShown()){
                mDiscountDescriptionTv.setVisibility(View.VISIBLE);
            }
            mDiscountDescriptionTv.setText(mDiscountDesContent);
        }else{
            if (mDiscountDescriptionTv.isShown()){
                mDiscountDescriptionTv.setText(mDiscountDesContent);
                mDiscountDescriptionTv.setVisibility(View.GONE);
            }
        }

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
                    MyDialog.ToastMessage("现金付款方式不存在！", mContext,null);
                }
            }
        }else{
            MyDialog.SnackbarMessage(mWindow,"剩余付款金额不能小于零！",mPayBalanceTv);
        }
    }


    private boolean generateOrderInfo(final JSONObject info){
        double sale_sum_amt = 0.0,dis_sum_amt = 0.0,total = 0.0,zl_amt = 0.0;

        long time = System.currentTimeMillis() / 1000;

        JSONObject order_info = new JSONObject(),tmp_json;
        JSONArray orders = new JSONArray(),combination_goods = new JSONArray(),sales_data,pays_data,discount_records;

        final String order_code = mContext.getOrderCode(),stores_id = mContext.getStoreInfo().getString("stores_id"),zk_cashier_id = mContext.getDisCashierId();
        final StringBuilder err = new StringBuilder();
        //处理销售明细
        sales_data = Utils.JsondeepCopy(mContext.getSaleData());//不能直接获取引用，需要重新复制一份否则会修改原始数据；如果业务不能正常完成，之前数据会遭到破坏
        for(int i = 0;i < sales_data.size();i ++){
            tmp_json = sales_data.getJSONObject(i);
            int gp_id = tmp_json.getIntValue("gp_id");

            sale_sum_amt += tmp_json.getDouble("sale_amt");
            dis_sum_amt += tmp_json.getDouble("discount_amt");

            if (-1 != gp_id){
                tmp_json = (JSONObject) sales_data.remove(i--);
                if (!mContext.splitCombinationalGoods(combination_goods,gp_id,tmp_json.getDouble("price"),tmp_json.getDouble("xnum"),err)){
                    info.put("info",err.toString());
                    return false;
                }
                Logger.d_json(combination_goods.toString());
            }else{
                tmp_json.put("order_code",order_code);
                tmp_json.put("zk_cashier_id",zk_cashier_id);//使用折扣的收银员ID,默认当前收银员
                tmp_json.put("total_money",tmp_json.remove("sale_amt"));
                tmp_json.put("y_price",tmp_json.remove("original_price"));

                ///删除不需要的内容
                tmp_json.remove("goods_id");
                tmp_json.remove("discount");
                tmp_json.remove("discount_amt");
                tmp_json.remove("original_amt");
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

        //处理优惠记录
        discount_records = Utils.JsondeepCopy(mContext.getDiscountRecords());
        for (int i = 0,size = discount_records.size();i < size;i++){
            tmp_json = discount_records.getJSONObject(i);
            tmp_json.put("order_code",order_code);
            tmp_json.put("stores_id",stores_id);
        }

        //处理付款明细
        pays_data = Utils.JsondeepCopy(getContent());//不能直接获取引用，需要重新复制一份否则会修改原始数据；如果业务不能正常完成，之前数据会遭到破坏
        JSONObject pay;
        double pamt = 0.0,pzl = 0.0;
        for (int i= 0,size = pays_data.size();i < size;i++){
            tmp_json = (JSONObject) pays_data.remove(0);

            pay = new JSONObject();

            pamt  = tmp_json.getDouble("pamt");
            pzl = tmp_json.getDouble("pzl");

            pay.put("order_code",order_code);
            pay.put("pay_code",tmp_json.getString("pay_code"));
            pay.put("pay_method",tmp_json.getString("pay_method_id"));
            pay.put("pay_money",pamt - pzl);
            pay.put("is_check",tmp_json.getDouble("is_check"));
            pay.put("pay_time",0);
            pay.put("pay_status",1);
            pay.put("pay_serial_no","");//第三方返回的支付流水号
            pay.put("remark","");
            pay.put("zk_money",0.0);
            pay.put("pre_sale_money",pamt);
            pay.put("give_change_money",pzl);
            pay.put("discount_money",0.0);
            pay.put("xnote",Utils.getNullStringAsEmpty(tmp_json,"xnote"));
            pay.put("return_code","");
            pay.put("v_num",tmp_json.getString("v_num"));
            pay.put("print_info","");

            pays_data.add(pay);
        }

        //处理单据
        total = sale_sum_amt + dis_sum_amt;

        order_info.put("stores_id",stores_id);
        order_info.put("order_code",order_code);
        order_info.put("total",total);
        order_info.put("discount_price",sale_sum_amt);
        order_info.put("discount_money",total);
        order_info.put("discount",String.format(Locale.CHINA,"%.4f",sale_sum_amt / total));
        order_info.put("cashier_id", mContext.getCashierInfo().getString("cas_id"));
        order_info.put("addtime",time);
        order_info.put("pos_code", mContext.getPosNum());
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

        info.put("retail_order",orders);
        info.put("retail_order_goods",sales_data);
        info.put("retail_order_pays",pays_data);
        info.put("discount_record",discount_records);

        return true;
    }
    public boolean saveOrderInfo(final StringBuilder err){
        boolean code;
        final JSONObject counts = new JSONObject(),data = new JSONObject();
        final List<String>  tables = Arrays.asList("retail_order","retail_order_goods","retail_order_pays","discount_record"),
                retail_order_cols = Arrays.asList("stores_id","order_code","discount","discount_price","total","cashier_id","addtime","pos_code","order_status","pay_status","pay_time","upload_status",
                        "upload_time","transfer_status","transfer_time","is_rk","mobile","name","card_code","sc_ids","sc_tc_money","member_id","discount_money","zl_money","ss_money","remark","zk_cashier_id"),
                retail_order_goods_cols = Arrays.asList("order_code","barcode_id","xnum","price","buying_price","retail_price","trade_price","cost_price","ps_price","tax_rate","tc_mode","tc_rate","gp_id",
                        "zk_cashier_id","total_money",GoodsInfoViewAdapter.W_G_MARK,"conversion","barcode","y_price"),
                retail_order_pays_cols = Arrays.asList("order_code","pay_method","pay_money","pay_time","pay_status","pay_serial_no","pay_code","remark","is_check","zk_money","pre_sale_money","give_change_money",
                        "discount_money","xnote","card_no","return_code","v_num","print_info"),
                discount_record_cols = Arrays.asList("order_code","discount_type","type","stores_id","relevant_id","discount_money","details");


           if (code = generateOrderInfo(data)){
               if ((code = SQLiteHelper.execSql(counts,"select count(order_code) counts from retail_order where order_code = '" + mContext.getOrderCode() +"' and stores_id = '" + mContext.getStoreInfo().getString("stores_id") +"'"))){
                   if (code = (0 == counts.getIntValue("counts"))){
                       if (!(code = SQLiteHelper.execSQLByBatchFromJson(data,tables,Arrays.asList(retail_order_cols,retail_order_goods_cols,retail_order_pays_cols,discount_record_cols),err,0))){
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
    private int updateOrderToPayingStatus(final String order_code,final StringBuilder err){
         final ContentValues values = new ContentValues();
        values.put("pay_status",3);
        return SQLiteHelper.execUpdateSql("retail_order",values," order_code = ?",new String[]{order_code},err);
    }
    public void requestPay(){

        if (mPayListener != null) mContext.runOnUiThread(()->mPayListener.onProgress(PayDialog.this,"正在支付..."));
        mPayStatus = true;
        int is_check,pay_status = 1;
        long pay_time = 0;
        double discount_money = 0.0;
        boolean open_cashbox = false;

        JSONObject retJson,pay_detail,pay_method_json,info_json;
        HttpRequest httpRequest = null;
        String pay_method_id = "",pay_money,pay_code,unified_pay_order,unified_pay_query,sz_param,v_num,order_code_son = "",third_pay_order_id = "",discount_xnote = "";

        final String order_code = mContext.getOrderCode(),url = mContext.getUrl(),appId = mContext.getAppId(),appScret = mContext.getAppScret(),
                stores_id = mContext.getStoreInfo().getString("stores_id"),pos_num = mContext.getCashierInfo().getString("pos_num");

        final List<ContentValues> valueList = new ArrayList<>();
        final List<String> tables = new ArrayList<>(),whereClauseList = new ArrayList<>();
        final List<String[]> whereArgsList = new ArrayList<>();

        final StringBuilder err = new StringBuilder();

        //更新订单到正在支付状态
        if (updateOrderToPayingStatus(order_code,err) < 0){
            if (mPayListener != null){
                mContext.runOnUiThread(()-> mPayListener.onError(PayDialog.this,err.toString()));
            }
            return;
        }

        final JSONArray pays = SQLiteHelper.getListToJson("select pay_method,pay_money,pay_code,is_check,v_num from retail_order_pays where order_code = '" + order_code +"'",0,0,false,err);
        if (null != pays){
            try{
                for (int i = 0,size = pays.size();i < size && mPayStatus;i++){
                    pay_detail = pays.getJSONObject(i);
                    pay_method_id = pay_detail.getString("pay_method");
                    order_code_son = pay_detail.getString("pay_code");

                    if (PayMethodViewAdapter.CASH_METHOD_ID.equals(pay_method_id)){
                        open_cashbox = true;
                    }
                    is_check = pay_detail.getIntValue("is_check");
                    if (is_check == 2){
                        pay_status = 2;
                        pay_time = System.currentTimeMillis()/1000;
                    }else{
                        pay_code = pay_detail.getString("pay_code");
                        v_num = pay_detail.getString("v_num");

                        if (httpRequest == null)httpRequest = new HttpRequest();

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

                            final JSONObject data_ = new JSONObject();
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
                                                    if (info_json.containsKey("xnote")){
                                                        discount_xnote = info_json.getString("xnote");
                                                        final JSONObject jsonObject = mPayDetailViewAdapter.findPayDetailById(pay_method_id);
                                                        if (jsonObject != null)
                                                            jsonObject.put("xnote",discount_xnote);
                                                    }
                                                    third_pay_order_id = info_json.getString("pay_code");
                                                    discount_money = info_json.getDouble("discount");
                                                    pay_time = info_json.getLong("pay_time");
                                                    pay_status = info_json.getIntValue("pay_status");
                                                    break;
                                                case 2:
                                                    mPayStatus = false;
                                                    err.append(info_json.getString("info"));
                                                    break;
                                                case 3:
                                                case 4:
                                                    if (mPayListener != null)
                                                        mContext.runOnUiThread(()->mPayListener.onProgress(PayDialog.this,"正在查询支付状态..."));

                                                    while (mPayStatus && (res_code == 3 ||  res_code == 4)){
                                                        final JSONObject object = new JSONObject();
                                                        object.put("appid",appId);
                                                        object.put("pay_code",info_json.getString("pay_code"));
                                                        object.put("order_code_son",info_json.getString("order_code_son"));
                                                        if (res_code == 4){
                                                            mContext.runOnUiThread(()->{
                                                                final ChangeNumOrPriceDialog password_dialog = new ChangeNumOrPriceDialog(mContext,"请输入密码","");
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
                                                                                    discount_xnote = info_json.getString("xnote");
                                                                                    final JSONObject jsonObject = mPayDetailViewAdapter.findPayDetailById(pay_method_id);
                                                                                    if (jsonObject != null)
                                                                                        jsonObject.put("xnote",discount_xnote);
                                                                                }
                                                                                third_pay_order_id = info_json.getString("pay_code");
                                                                                discount_money = info_json.getDouble("discount");
                                                                                pay_time = info_json.getLong("pay_time");
                                                                                pay_status = info_json.getIntValue("pay_status");
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
                    if (mPayStatus){
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
                        whereArgsList.add(new String[]{order_code,order_code_son,pay_method_id});
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
            final ContentValues values = new ContentValues();
            values.put("order_status",3);
            values.put("spare_param1",err.toString());
            if (SQLiteHelper.execUpdateSql("retail_order",values,"order_code = ?",new String[]{order_code},err) >= 0){
                Logger.e("支付更新订单状态错误：%s",err);
            }
        }else{
            tables.add("retail_order");

            final ContentValues values_order = new ContentValues();
            values_order.put("order_status",2);
            values_order.put("pay_status",2);
            values_order.put("pay_time",pay_time);
            valueList.add(values_order);

            whereClauseList.add("order_code = ?");
            whereArgsList.add(new String[]{order_code});

            int[] rows = SQLiteHelper.execBatchUpdateSql(tables,valueList,whereClauseList,whereArgsList,err);

            if (rows == null){
                Logger.e("支付更新订单状态错误：%s",err);
            }else{
                int index = SQLiteHelper.verifyUpdateResult(rows);
                if (index == -1){
                    if (mContext.getPrintStatus()){
                        Printer.print(mContext, PayDialog.get_print_content(mContext,order_code,open_cashbox));
                    }
                    if (mPayListener != null){
                        mContext.runOnUiThread(()-> mPayListener.onSuccess(PayDialog.this));
                    }
                } else{
                    final String sz_err = String.format(Locale.CHINA,"数据表,%s未更新，value:%s,whereClause:%s,whereArgs:%s",tables.get(index),valueList.get(index),
                            whereClauseList.get(index),Arrays.toString(whereArgsList.get(index)));
                    Logger.e(sz_err);
                    err.append(sz_err);
                }
            }
        }

        if (err.length() != 0 && mPayListener != null)
            mContext.runOnUiThread(()-> mPayListener.onError(PayDialog.this,err.toString()));
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
                        MyDialog.ToastMessage("找零不能大于100", mContext,null);
                    }
                }else{
                    mZlAmt = 0.00;
                    mZlAmtEt.setText(mContext.getText(R.string.z_p_z_sz));
                }
            }
        });
        mCashMoneyEt.postDelayed(()-> mCashMoneyEt.requestFocus(),300);
    }
    private String getCashPayCode() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS",Locale.CHINA).format(new Date())+ mContext.getPosNum() + Utils.getNonce_str(8);
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

    private static String c_format_58(final Context context, final JSONObject format_info, final JSONObject order_info, boolean is_open_cash_box){

        final StringBuilder info = new StringBuilder();
        int print_count = Utils.getNotKeyAsNumberDefault(format_info,"p_c",1),footer_space = Utils.getNotKeyAsNumberDefault(format_info,"f_s",5);

        final String store_name = Utils.getNullStringAsEmpty(format_info,"s_n"),pos_num = Utils.getNullOrEmptyStringAsDefault(order_info,"pos_num",""),
                cas_name = Utils.getNullOrEmptyStringAsDefault(order_info,"cas_name",""),footer_c = Utils.getNullStringAsEmpty(format_info,"f_c"),
                new_line = "\r\n",//Printer.commandToStr(Printer.NEW_LINE);
                new_line_16 = Printer.commandToStr(Printer.LINE_SPACING_16),
                new_line_2 = Printer.commandToStr(Printer.LINE_SPACING_2),new_line_d = Printer.commandToStr(Printer.LINE_SPACING_DEFAULT),
                line = context.getString(R.string.line_58);

        if (is_open_cash_box)//开钱箱
            info.append(Printer.commandToStr(Printer.OPEN_CASHBOX));

        while (print_count-- > 0) {//打印份数
            info.append(Printer.commandToStr(Printer.DOUBLE_HEIGHT)).append(Printer.commandToStr(Printer.ALIGN_CENTER))
                    .append(store_name.length() == 0 ? Utils.getNullStringAsEmpty(order_info,"stores_name") : store_name).append(Printer.commandToStr(Printer.NORMAL)).append(new_line).append(new_line).
                    append(Printer.commandToStr(Printer.ALIGN_LEFT));

            info.append(Printer.printTwoData(1, context.getString(R.string.b_f_store_id_sz).concat(Utils.getNullStringAsEmpty(order_info,"stores_id")),Utils.getNullStringAsEmpty(order_info,"oper_time"))).append(new_line);
            info.append(Printer.printTwoData(1, context.getString(R.string.b_f_jh_sz).concat(pos_num), context.getString(R.string.b_f_cashier_sz).concat(cas_name))).append(new_line);
            info.append(context.getString(R.string.b_f_order_sz)).append(Utils.getNullStringAsEmpty(order_info,"order_code")).append(new_line).append(new_line);

            info.append(context.getString(R.string.b_f_header_sz).replace("-"," ")).append(new_line_2).append(new_line).append(line).append(new_line_2).append(new_line).append(new_line_d);
            //商品明细
            JSONObject info_obj;
            double discount_amt = 0.0, xnum = 0.0,order_amt = 0.0,actual_amt = 0.0,sum_dis_amt = 0.0;
            int units_num = 0, type = 1;//商品属性 1普通 2称重 3用于服装
            final JSONArray sales = Utils.getNullObjectAsEmptyJsonArray(order_info,"sales");
            for (int i = 0, size = sales.size(); i < size; i++) {
                info_obj = sales.getJSONObject(i);
                if (info_obj != null) {
                    order_amt += info_obj.getDoubleValue("original_amt");

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

                    info.append(Printer.commandToStr(Printer.BOLD)).append(Utils.getNullStringAsEmpty(info_obj,"goods_title")).append(new_line).append(Printer.commandToStr(Printer.BOLD_CANCEL));
                    info.append(Printer.printTwoData(1,Utils.getNullStringAsEmpty(info_obj,"barcode"),
                            Printer.printThreeData(16,Utils.getNullStringAsEmpty(info_obj,"price"), type == 2 ? String.valueOf(xnum) : String.valueOf((int) xnum),Utils.getNullStringAsEmpty(info_obj,"sale_amt"))));

                    if (!Utils.equalDouble(discount_amt, 0.0)) {

                        sum_dis_amt += discount_amt;

                        info.append(new_line).append(Printer.printTwoData(1, context.getString(R.string.b_f_ori_price_sz).concat(Utils.getNullStringAsEmpty(info_obj,"original_price")),
                                context.getString(R.string.b_f_disco_sz).concat(String.format(Locale.CHINA, "%.2f", discount_amt))));
                    }
                    if (i + 1 != size)
                        info.append(new_line_16);
                    else
                        info.append(new_line_2);

                    info.append(new_line);
                }
            }
            info.append(line).append(new_line_2).append(new_line).append(new_line_d);

            info.append(Printer.printTwoData(1, context.getString(R.string.b_f_amt_sz).concat(String.format(Locale.CHINA, "%.2f", order_amt))
                    , context.getString(R.string.b_f_units_sz).concat(String.valueOf(units_num)))).append(new_line);

            info.append(Printer.printTwoData(1, context.getString(R.string.b_f_rec_sz).concat(String.format(Locale.CHINA, "%.2f", order_amt - sum_dis_amt)),
                    context.getString(R.string.b_f_disco_sz).concat(String.format(Locale.CHINA, "%.2f", sum_dis_amt)))).
                    append(new_line_2).append(new_line).append(line).append(new_line_2).append(new_line).append(new_line_d);

            //支付方式
            double zl = 0.0, pamt = 0.0;
            final JSONArray pays = Utils.getNullObjectAsEmptyJsonArray(order_info,"pays");
            for (int i = 0, size = pays.size(); i < size; i++) {
                info_obj = pays.getJSONObject(i);
                zl = info_obj.getDoubleValue("pzl");
                pamt = info_obj.getDoubleValue("pamt");
                info.append(Utils.getNullOrEmptyStringAsDefault(info_obj,"name","")).append("：").append(pamt - zl).append("元").append(new_line);

                info.append(context.getString(R.string.b_f_yus_sz)).append(pamt);
                if (!Utils.equalDouble(zl, 0.0)) {
                    info.append(",").append(context.getString(R.string.b_f_zl_sz)).append(zl);
                }
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
            info.append(context.getString(R.string.b_f_hotline_sz)).append(Utils.getNullOrEmptyStringAsDefault(order_info,"telphone","")).append(new_line);
            info.append(context.getString(R.string.b_f_stores_address_sz)).append(Utils.getNullOrEmptyStringAsDefault(order_info,"region","")).append(new_line);

            info.append(Printer.commandToStr(Printer.ALIGN_CENTER)).append(footer_c);
            for (int i = 0; i < footer_space; i++) info.append(" ").append(new_line);

            if (print_count > 0){
                info.append(new_line).append(new_line).append(new_line);
            }
        }

        Logger.d(info);

        return info.toString();
    }
    private void refreshPayContent(){
        if (initPayContent()){
            refreshContent();
            if (null != mPayDetailViewAdapter)mPayDetailViewAdapter.notifyDataSetChanged();
        }
    }

    public PayDialog setPayListener(onPayListener listener) {
        this.mPayListener = listener;
        return  this;
    }
    public interface onPayListener {
        void onStart(PayDialog myDialog);
        void onProgress(PayDialog myDialog, final String info);
        void onSuccess(PayDialog myDialog);
        void onError(PayDialog myDialog, final String err);
    }
    public boolean initPayContent(){
        final JSONArray datas = mContext.getSaleData();
        if (!datas.isEmpty()){
            antoMol();
            calculatePayContent();
            return true;
        }
        return false;
    }
    public JSONArray getContent(){
        return mPayDetailViewAdapter.getDatas();
    }
    public void showVipInfo(@NonNull JSONObject vip,boolean show){//show为true则只显示不再刷新已销售商品
        mVip = vip;
        final LinearLayout vip_info_linearLayout = findViewById(R.id.vip_info_linearLayout);
        if (vip_info_linearLayout != null){
            vip_info_linearLayout.setVisibility(View.VISIBLE);
            ((TextView)vip_info_linearLayout.findViewById(R.id.vip_name)).setText(mVip.getString("name"));
            ((TextView)vip_info_linearLayout.findViewById(R.id.vip_phone_num)).setText(mVip.getString("mobile"));
        }
        if (!show){
            mContext.showVipInfo(vip);
            refreshPayContent();
        }
    }

    public static String get_print_content(final MainActivity context,final String order_code,boolean is_open_cash_box){
        final JSONObject print_format_info = new JSONObject();
        String content = "";
        if (SQLiteHelper.getLocalParameter("c_f_info",print_format_info)){
            if (print_format_info.getIntValue("f") == R.id.checkout_format){
                final JSONObject order_info = new JSONObject();
                if (getPrintOrderInfo(order_code,order_info)){
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
                    context.runOnUiThread(()->MyDialog.ToastMessage("加载打印内容错误：" + order_info.getString("info"), context,null));
                }
            }
        }else
            context.runOnUiThread(()->MyDialog.ToastMessage("加载打印格式错误：" + print_format_info.getString("info"), context,null));

        return content;
    }

    private static boolean getPrintOrderInfo(final String order_code,final JSONObject order_info) {
        boolean code = false;
        if (SQLiteHelper.execSql(order_info, "SELECT a.order_code,b.cas_name,a.pos_code pos_num,a.stores_id,c.stores_name,datetime(a.addtime, 'unixepoch', 'localtime') oper_time,c.telphone,c.region" +
                " FROM retail_order a  left join cashier_info b on a.cashier_id = b.cas_id\n" +
                "left join shop_stores c on a.stores_id = c.stores_id where a.order_code = '" + order_code + "'")) {
            final StringBuilder err = new StringBuilder();
            final String goods_info_sql = "SELECT a.barcode,b.goods_title,a.price,a.retail_price original_price,a.xnum,a.retail_price * a.xnum original_amt,\n" +
                    "a.total_money sale_amt,a.retail_price * a.xnum - a.total_money discount_amt FROM retail_order_goods a \n" +
                    "left join barcode_info b on a.barcode_id = b.barcode_id where order_code = '" + order_code + "'", pays_info_sql = "SELECT  b.name,pre_sale_money pamt,give_change_money pzl,xnote FROM retail_order_pays a \n" +
                    "left join pay_method b on a.pay_method = b.pay_method_id where order_code = '" + order_code + "'";

            final JSONArray sales = SQLiteHelper.getListToJson(goods_info_sql, err), pays = SQLiteHelper.getListToJson(pays_info_sql, err);
            if (sales != null && pays != null) {

                order_info.put("sales", sales);
                order_info.put("pays", pays);

                code = true;
            }else {
                order_info.put("info",err.toString());
            }
        }
        return code;
    }
}
