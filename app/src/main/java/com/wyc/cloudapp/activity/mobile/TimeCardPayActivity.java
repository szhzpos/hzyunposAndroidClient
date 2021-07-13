package com.wyc.cloudapp.activity.mobile;

import android.content.Intent;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.PayDetailViewAdapter;
import com.wyc.cloudapp.adapter.PayMethodAdapterForObj;
import com.wyc.cloudapp.bean.TimeCardPayInfo;
import com.wyc.cloudapp.bean.TimeCardSaleInfo;
import com.wyc.cloudapp.bean.UnifiedPayResult;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.data.room.AppDatabase;
import com.wyc.cloudapp.data.room.entity.PayMethod;
import com.wyc.cloudapp.data.room.entity.TimeCardPayDetail;
import com.wyc.cloudapp.data.room.entity.TimeCardSaleOrder;
import com.wyc.cloudapp.decoration.PayMethodItemDecoration;
import com.wyc.cloudapp.decoration.SuperItemDecoration;
import com.wyc.cloudapp.dialog.ChangeNumOrPriceDialog;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.pay.AbstractSettlementDialog;
import com.wyc.cloudapp.dialog.pay.PayMethodDialogImp;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.FormatDateTimeUtils;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;
import com.wyc.cloudapp.utils.http.callback.ObjectCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.wyc.cloudapp.utils.FormatDateTimeUtils.formatCurrentTime;

public class TimeCardPayActivity extends AbstractMobileActivity {
    public static final int ONCE_CARD_REQUEST_PAY = 0x000000dd;
    private static final String ORDER_INFO = "o";

    private PayMethodAdapterForObj mPayMethodViewAdapter;
    private RecyclerView mPayMethodView;
    private PayDetailViewAdapter mPayDetailViewAdapter;
    private PayMethod mPayMethod;
    private TimeCardSaleOrder mOrder;
    CustomProgressDialog mProgressDialog;

    private double mPay_balance,mPay_amt,mCashAmt,mOrder_amt,mDiscount_amt,mActual_amt,mZlAmt,mAmt_received;

    @BindView(R.id.vip_name)
    TextView vip_name;
    @BindView(R.id.vip_phone_num)
    TextView vip_phone_num;
    @BindView(R.id.order_amt)
    TextView order_amt;
    @BindView(R.id.dis_sum_amt)
    TextView dis_sum_amt;
    @BindView(R.id.actual_amt)
    TextView actual_amt;
    @BindView(R.id.cash_amt)
    EditText mCashMoneyEt;
    @BindView(R.id.zl_amt)
    EditText mZlAmtEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mOrder = getIntent().getParcelableExtra(ORDER_INFO);

        WindowManager.LayoutParams attributes = getWindow().getAttributes();
        attributes.dimAmount = 0.5f;
        attributes.x = 0;
        attributes.y = 0;
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT;
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getWindow().setAttributes(attributes);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        ButterKnife.bind(this);

        setMiddleText(getString(R.string.once_card_pay));

        initPayDetailViewAdapter();
        initPayMethod();
        initKeyboard();
        initCashText();

        initPayContent();
        showVip();

        AbstractSettlementDialog.autoShowValueFromPayAmt(getWindow().getDecorView(), (int) mCashAmt);
    }

    private void initCashText(){
        mCashMoneyEt.setText(String.format(Locale.CHINA,"%.2f",mActual_amt));
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
                    mCashAmt = Double.parseDouble(editable.toString());
                }catch (NumberFormatException e){
                    mCashAmt = 0.0;
                }
                if ((mZlAmt = mCashAmt - mPay_balance) > 0){
                    if (mZlAmt < 100)
                        mZlAmtEt.setText(String.format(Locale.CHINA,"%.2f",mZlAmt));
                    else{
                        mCashMoneyEt.setText(String.valueOf(mPay_balance));
                        mCashMoneyEt.selectAll();
                        MyDialog.toastMessage("找零不能大于100");
                    }
                }else{
                    mZlAmt = 0.00;
                    mZlAmtEt.setText(getText(R.string.zero_p_z_sz));
                }
            }
        });
        mCashMoneyEt.postDelayed(mCashMoneyEt::requestFocus,300);
    }

    private void showVip(){
        vip_name.setText(mOrder.getVip_name());
        vip_phone_num.setText(mOrder.getVip_mobile());
    }

    private void initPayDetailViewAdapter() {
        mPayDetailViewAdapter = new PayDetailViewAdapter(this);
        mPayDetailViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (mPayMethodViewAdapter != null)mPayMethodViewAdapter.notifyDataSetChanged();

                final JSONArray jsonArray = getContent();
                double pay_amt = 0.0,zl_amt = 0.0;
                JSONObject object;
                for (int i = 0,length = jsonArray.size();i < length;i ++){
                    object = jsonArray.getJSONObject(i);
                    pay_amt += object.getDouble("pamt");
                    zl_amt += object.getDouble("pzl");
                }

                mAmt_received = pay_amt - zl_amt;
                mPay_balance = mPay_amt - mAmt_received;
                mCashAmt = mPay_balance;

                refreshContent();

                Logger.d("amt:%f - zl_amt:%f = %f,mActual_amt:%f,mAmt_received:%f",pay_amt,zl_amt,pay_amt - zl_amt,mActual_amt,mAmt_received);
                if (!mPayDetailViewAdapter.isEmpty()){
                    double sale_amt = Utils.formatDouble(getSaleAmt(),2);
                    double rec_pay_amt = Utils.formatDouble(mPayDetailViewAdapter.getPaySumAmt(),2);
                    if (Utils.equalDouble(sale_amt,rec_pay_amt)){//再次验证销售金额以及付款金额是否相等
                        save();
                    }else{
                        MyDialog.displayErrorMessage(TimeCardPayActivity.this, String.format(Locale.CHINA,"销售金额:%f  不等于 付款金额:%f",sale_amt,rec_pay_amt));
                    }
                }
            }
        });
    }

    private double getSaleAmt(){
        return mOrder.getAmt();
    }
    private double getDiscountAmt(){
        double amt = 0.0;
        List<TimeCardSaleInfo> saleInfoList = mOrder.getSaleInfo();
        for (TimeCardSaleInfo info : saleInfoList){
            amt += info.getDiscountAmt();
        }
        return amt;
    }

    private void save(){
        List<TimeCardPayDetail> payDetails = new ArrayList<>();
        JSONArray array = mPayDetailViewAdapter.getDatas();
        TimeCardPayDetail payDetail = null;
        for (int i = 0,size = array.size();i < size;i ++){
            final JSONObject object = array.getJSONObject(i);
            payDetail = new TimeCardPayDetail.Builder(mOrder.getOrder_no())
                    .pay_method_id(object.getIntValue("pay_method_id"))
                    .amt(object.getDoubleValue("pamt")).operator(getCashierCode()).build();

            payDetails.add(payDetail);
        }
        try {
            mOrder.setTime(formatCurrentTime(FormatDateTimeUtils.YYYY_MM_DD_1));
            mOrder.save(payDetails);
            startPay();
        }catch (SQLiteException e){
            e.printStackTrace();
            MyDialog.toastMessage(e.getMessage());
        }
    }

    private void startPay(){
        mProgressDialog = CustomProgressDialog.showProgress(this,"正在支付...");
        final JSONObject param = new JSONObject();
        param.put("appid",getAppId());
        param.put("member_openid",mOrder.getVip_openid());
        param.put("origin",5);
        param.put("stores_id",getStoreId());
        param.put("cards",getCards().toString());
        param.put("sales_id",mOrder.getSaleman());
        param.put("cas_id",getCashierId());
        HttpUtils.sendAsyncPost(getUrl() + InterfaceURL.ONCE_CARD_UPLOAD, HttpRequest.generate_request_parm(param,getAppSecret()))//生成订单
                .enqueue(new ObjectCallback<TimeCardPayInfo>(TimeCardPayInfo.class) {
                    @Override
                    protected void onError(String msg) {
                        MyDialog.toastMessage(msg);
                        dismissProgress();
                    }

                    @Override
                    protected void onSuccessForResult(TimeCardPayInfo d, String hint) {
                        //提交支付
                        try {
                            TimeCardPayInfo.PayInfo payInfo = d.getPay_info();
                            final String online_order_no = payInfo.getOrder_code();

                            //更新状态以及保存线上单号
                            mOrder.updateOnlineOrderNo(online_order_no);

                            final JSONArray payDetails = mPayDetailViewAdapter.getDatas();
                            for (int i = 0,size = payDetails.size();i < size;i ++){
                                final JSONObject object = payDetails.getJSONObject(i);
                                final PayMethod payMethod = AppDatabase.getInstance().PayMethodDao().getPayMethodById(object.getIntValue("pay_method_id"));
                                if (payMethod.isCheckApi()){
                                    final UnifiedPayResult result = payMethod.payWithApi(TimeCardPayActivity.this,payInfo.getPay_money(),
                                            online_order_no,mOrder.getOrder_no(),object.getString("v_num"),getLocalClassName());

                                    if (result.isSuccess()){
                                        uploadPayInfo(online_order_no,payMethod.getPay_method_id());
                                    }else MyDialog.toastMessage(result.getInfo());
                                }else uploadPayInfo(online_order_no,payMethod.getPay_method_id());
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                            MyDialog.toastMessage("获取次卡支付信息错误:" + e.getLocalizedMessage());
                        }
                        dismissProgress();
                    }
                });
    }

    private void uploadPayInfo(final String online_order_no,int pay_method_id){
        final JSONObject pay = new JSONObject();
        pay.put("appid",getAppId());
        pay.put("order_code",online_order_no);
        pay.put("pay_method",pay_method_id);
        HttpUtils.sendAsyncPost(getUrl() + InterfaceURL.ONCE_CARD_PAY,HttpRequest.generate_request_parm(pay,getAppSecret()))
                .enqueue(new ObjectCallback<String>(String.class) {
                    @Override
                    protected void onError(String msg) {
                        MyDialog.toastMessage(msg);
                        dismissProgress();
                    }

                    @Override
                    protected void onSuccessForResult(String d, String hint) {
                        mOrder.success();
                        setResult(RESULT_OK);
                        finish();
                        MyDialog.toastMessage(hint);
                        dismissProgress();
                    }
                });
    }
    private void dismissProgress(){
        if (mProgressDialog != null)mProgressDialog.dismiss();
    }

    private JSONArray getCards(){
        List<TimeCardSaleInfo> saleInfoList = mOrder.getSaleInfo();
        final JSONArray array = new JSONArray();
        for (TimeCardSaleInfo saleInfo : saleInfoList){
            final JSONObject object = new JSONObject();
            object.put("once_card_id",saleInfo.getOnce_card_id());
            object.put("num",saleInfo.getNum());
            array.add(object);
        }
        return array;
    }

    private void initPayContent(){
        calculatePayContent();
        refreshContent();
    }

    private void calculatePayContent(){
        clearContent();
        mAmt_received = Utils.formatDouble(mPayDetailViewAdapter == null ? 0.0 :mPayDetailViewAdapter.getPaySumAmt(),2);
        mDiscount_amt = Utils.formatDouble(getDiscountAmt(),2);
        mActual_amt = Utils.formatDouble(getSaleAmt(),2);
        mOrder_amt = Utils.formatDouble(mActual_amt + mDiscount_amt,2);
        mPay_amt = mActual_amt;
        mPay_balance = mActual_amt - mAmt_received;//剩余付款金额等于应收金额已收金额
        mCashAmt = mPay_balance;
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
    private void refreshContent(){
        order_amt.setText(String.format(Locale.CHINA,"%.2f",mOrder_amt));
        dis_sum_amt.setText(String.format(Locale.CHINA,"%.2f",mDiscount_amt));
        actual_amt.setText(String.format(Locale.CHINA,"%.2f",mActual_amt));
        mCashMoneyEt.setText(String.format(Locale.CHINA,"%.2f",mCashAmt));
        mZlAmtEt.setText(String.format(Locale.CHINA,"%.2f",mZlAmt));
        mCashMoneyEt.selectAll();
    }

    private boolean verifyPayBalance(){
        Logger.d("mPay_balance:%f",mPay_balance);
        return (mPay_balance > 0.0 || Utils.equalDouble(mPay_balance,0.0));
    }

    public @NonNull JSONArray getContent(){
        return mPayDetailViewAdapter.getDatas();
    }

    private void initPayMethod(){
        mPayMethodViewAdapter = new PayMethodAdapterForObj(this,mPayDetailViewAdapter);
        mPayMethodViewAdapter.setData("6");
        mPayMethodViewAdapter.setOnItemClickListener((object) -> {
            //次卡销售只支持一种付款方式，选择之前先清除已付款的记录
            if (!mPayDetailViewAdapter.isEmpty()){
                final JSONObject method = mPayDetailViewAdapter.getDatas().getJSONObject(0);
                if (MyDialog.showMessageToModalDialog(this,getString(R.string.pay_method_exist_hints,method.getString("name"))) == 1){
                    mPayDetailViewAdapter.clear();
                }else return;
            }
            try {
                if (verifyPayBalance()){
                    mPayMethod = object.clone();
                    final String payMethodId = String.valueOf(mPayMethod.getPay_method_id());

                    final JSONObject detail = new JSONObject();
                    detail.put("pay_method_id",payMethodId);
                    if (String.valueOf(PayMethod.CASH_METHOD_ID).equals(payMethodId)) {
                        detail.put("pay_code",getCashPayCode());
                        detail.put("pamt",mCashAmt);
                        detail.put("pzl",String.format(Locale.CHINA,"%.2f",mZlAmt));
                        detail.put("v_num","");
                        mPayDetailViewAdapter.addPayDetail(detail);
                    } else {
                        if (Utils.equalDouble(mPay_balance, 0) && mPayDetailViewAdapter.findPayDetailById(payMethodId) == null) {//剩余金额为零，同时不存在此付款方式的记录。
                            mPayMethodViewAdapter.showDefaultPayMethod();
                            MyDialog.SnackbarMessage(getWindow(), "剩余金额为零！", getCurrentFocus());
                        } else {
                            detail.put("name", mPayMethod.getName());
                            detail.put("xtype",mPayMethod.getXtype());
                            detail.put("is_check",mPayMethod.getIs_check());
                            if (mPayMethod.isVipPay()){
                                detail.put("card_code",mOrder.getVip_card_no());
                            }
                            final PayMethodDialogImp payMethodDialogImp = new PayMethodDialogImp(TimeCardPayActivity.this, detail);
                            payMethodDialogImp.setModifyPayAmt(false).setPayAmt(mPay_balance);
                            final int code = payMethodDialogImp.exec();
                            mPayMethodViewAdapter.showDefaultPayMethod();
                            if (code == 1){
                                final JSONObject jsonObject = payMethodDialogImp.getContent();
                                mPayDetailViewAdapter.addPayDetail(jsonObject);
                            }
                        }
                    }
                }else{
                    MyDialog.toastMessage(getString(R.string.pay_amt_less_zero_hints));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                MyDialog.ToastMessage("付款错误：" + e.getMessage(), this,null);
            }
        });
        final RecyclerView recyclerView = findViewById(R.id.pay_method_list);
        recyclerView.setLayoutManager(new GridLayoutManager(this,4));
        SuperItemDecoration.registerGlobalLayoutToRecyclerView(recyclerView,getResources().getDimension(R.dimen.pay_method_height),new PayMethodItemDecoration());
        recyclerView.setAdapter(mPayMethodViewAdapter);
        mPayMethodView = recyclerView;
    }

    private String getCashPayCode() {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS",Locale.CHINA).format(new Date())+ getPosNum() + Utils.getNonce_str(8);
    }

    private void initKeyboard(){
        final ConstraintLayout keyboard_linear_layout  = findViewById(R.id.keyboard);
        if (null != keyboard_linear_layout)
            for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
                final View tmp_v = keyboard_linear_layout.getChildAt(i);
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
                                            tmp_edit.setText(getString(R.string.d_zero_point_sz));
                                        }else{
                                            if (index == 0)return;
                                            if (index > editable.length())index = editable.length();
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
                            });
                            break;
                        case R.id._cancel:
                            tmp_v.setOnClickListener(v -> finish());
                            break;
                        case R.id._ok:
                            tmp_v.setOnClickListener(v -> {
                                v.setEnabled(false);
                                triggerDefaultPay();
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

    private void triggerDefaultPay(){
        final int id = mPayMethodViewAdapter.getDefaultPayMethodId();
        final int index = mPayMethodViewAdapter.findPayMethodIndexById(id);
        if (index != -1){
            RecyclerView.ViewHolder viewHolder = mPayMethodView.findViewHolderForAdapterPosition(index);
            if (viewHolder != null){
                viewHolder.itemView.callOnClick();
            }else
                mPayMethodView.scrollToPosition(index);//如果找不到view则滚动
        }else {
            MyDialog.ToastMessage(String.format(Locale.CHINA,"ID为%d的支付方式不存在!",id),this,getWindow());
        }
    }

    private final View.OnClickListener button_click = v -> {
        final View view =  getCurrentFocus();
        if (view != null) {
            if (view.getId() == R.id.cash_amt) {
                final EditText tmp_edit = ((EditText)view);
                final Editable editable = tmp_edit.getText();
                int index = tmp_edit.getSelectionStart(),point_index = editable.toString().indexOf(".");
                final String sz_button = ((Button) v).getText().toString();
                if (-1 != point_index && tmp_edit.getSelectionEnd() == editable.length()){
                    editable.replace(0, editable.length(),sz_button.concat(getString(R.string.d_zero_point_sz)));
                    point_index = editable.toString().indexOf(".");
                    if (point_index == index)point_index += 1;
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

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_once_card_pay;
    }

    public static void start(@NonNull Fragment context,TimeCardSaleOrder order){
        if (order == null){
            throw new IllegalArgumentException("the second parameter must not be empty...");
        }
        Intent intent = new Intent(context.getContext(), TimeCardPayActivity.class)
                .putExtra(ORDER_INFO,order);
        context.startActivityForResult(intent,ONCE_CARD_REQUEST_PAY);
    }
}