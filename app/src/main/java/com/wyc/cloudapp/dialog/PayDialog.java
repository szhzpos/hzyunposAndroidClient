package com.wyc.cloudapp.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.adapter.PayDetailViewAdapter;
import com.wyc.cloudapp.adapter.PayMethodItemDecoration;
import com.wyc.cloudapp.adapter.PayMethodViewAdapter;
import com.wyc.cloudapp.interface_abstract.AbstractPayDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class PayDialog extends AbstractPayDialog {
    private MainActivity mainActivity;
    private EditText mCashMoneyEt,mZlAmtEt;
    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器
    private PayMethodViewAdapter mPayMethodViewAdapter;
    private PayDetailViewAdapter mPayDetailViewAdapter;
    private TextView mOrderAmtTv,mDiscountAmtTv,mActualAmtTv,mPayAmtTv,mAmtReceivedTv,mPayBalanceTv;
    private double mOrder_amt = 0.0,mDiscount_amt = 0.0,mActual_amt = 0.0,mPay_amt = 0.0,mAmt_received = 0.0,mPay_balance = 0.0,mCashAmt = 0.0,mZlAmt = 0.0;
    private Button mOK;
    private JSONObject mVip;
    public PayDialog(MainActivity context){
        super(context);
        mainActivity = context;
        //可以show之前访问view
        setContentView(this.getLayoutInflater().inflate(R.layout.pay_dialog_content, null));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        //初始化TextView
        mOrderAmtTv = findViewById(R.id.order_amt);//单据金额
        mDiscountAmtTv = findViewById(R.id.discount_amt);//折扣金额
        mActualAmtTv = findViewById(R.id.actual_amt);//应收金额
        mPayAmtTv = findViewById(R.id.pay_amt);//付款金额
        mAmtReceivedTv = findViewById(R.id.amt_received);//已收金额
        mPayBalanceTv = findViewById(R.id.pay_balance);//付款余额
        mZlAmtEt = findViewById(R.id.zl_amt);//找零


        //初始化支付方式
        initPayMethod();

        //初始化支付明细
        initPayDetailViewAdapter();

        //初始化现金EditText
        initCsahText();

        //初始化按钮
        mOK = findViewById(R.id._ok);
        mOK.setOnClickListener(v -> {cash_pay();});
        findViewById(R.id._close).setOnClickListener(view -> PayDialog.this.dismiss());
        findViewById(R.id.cancel).setOnClickListener(v -> {
            if (noOnclickListener != null){
                noOnclickListener.onNoClick(PayDialog.this);
            }
        });
        findViewById(R.id._back).setOnClickListener(v -> {
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
        findViewById(R.id.vip).setOnClickListener(view -> {
            VipInfoDialog vipInfoDialog = new VipInfoDialog(mainActivity);
            vipInfoDialog.setYesOnclickListener(dialog -> {
                if (showVipInfo(dialog.getVip(),false)){
                    refreshContent();
                    dialog.dismiss();
                }
            }).show();
        });
        findViewById(R.id.all_discount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ChangeNumOrPriceDialog dialog = new ChangeNumOrPriceDialog(mainActivity,mainActivity.getString(R.string.discount_sz),String.format(Locale.CHINA,"%d",100));
                dialog.setYesOnclickListener(myDialog -> {
                    if (initPayContent(mainActivity.discount(myDialog.getNewNumOrPrice()))){
                        refreshContent();
                        myDialog.dismiss();
                    }
                }).show();
            }
        });

        //初始化数字键盘
        ConstraintLayout keyboard_linear_layout;
        keyboard_linear_layout = findViewById(R.id.keyboard);
        for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
            View tmp_v = keyboard_linear_layout.getChildAt(i);
            int id = tmp_v.getId();
            if (tmp_v instanceof Button && !(id == R.id._back || id == R.id.cancel || id == R.id._ok)){
                tmp_v.setOnClickListener(button_click);
            }
        }
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

    public PayDialog  setNoOnclickListener(onNoOnclickListener onNoOnclickListener) {
        this.noOnclickListener = onNoOnclickListener;
        return this;
    }

    public PayDialog setYesOnclickListener(onYesOnclickListener onYesOnclickListener) {
        this.yesOnclickListener = onYesOnclickListener;
        return  this;
    }

    @Override
    public void setPayAmt(double amt) {

    }

    @Override
    public JSONObject getPayContent() {
        JSONArray array = mPayDetailViewAdapter.getDatas();
        for (int i = 0,length = array.length();i < length;i++){
            JSONObject object = array.optJSONObject(i);
            if (object != null && PayMethodViewAdapter.CASH_METHOD_ID.equals(object.optString("pay_method_id"))){//获取现金支付记录
                return object;
            }
        }
        return null;
    }

    public interface onYesOnclickListener {
        void onYesClick(PayDialog myDialog);
    }

    public interface onNoOnclickListener {
        void onNoClick(PayDialog myDialog);
    }

    private View.OnClickListener button_click = v -> {
        View view =  getCurrentFocus();
        if (view != null) {
            if (view.getId() == R.id.cash_amt) {
                EditText tmp_edit = ((EditText)view);
                int index = tmp_edit.getSelectionStart();
                Editable editable = tmp_edit.getText();
                String sz_button = ((Button) v).getText().toString();
                if (editable.toString().contains(".") && tmp_edit.getSelectionEnd() == editable.length()){
                    editable.replace(0,editable.toString().indexOf("."),sz_button);
                    tmp_edit.setSelection(editable.toString().indexOf("."));
                }else
                    if (".".equals(sz_button)) {
                        if (editable.toString().contains(".")){
                            tmp_edit.setSelection(editable.toString().indexOf(".") + 1);
                        }else{
                            editable.insert(index, sz_button);
                        }
                    } else {
                        if (editable.toString().contains(".") && index > editable.toString().indexOf(".")) {
                            if (index != tmp_edit.length())
                                editable.delete(index, index + 1).insert(index, sz_button);
                        } else {
                            if (index == 0 && "0".equals(sz_button)) return;
                            editable.insert(index, sz_button);
                        }
                    }
            }

        }
    };

    @Override
    protected void initPayMethod(){
        mPayMethodViewAdapter = new PayMethodViewAdapter(mainActivity,(int) mainActivity.getResources().getDimension(R.dimen.pay_method_width));
        mPayMethodViewAdapter.setDatas("1");
        mPayMethodViewAdapter.setOnItemClickListener(new PayMethodViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int pos) {
                JSONObject pay_method = mPayMethodViewAdapter.getItem(pos);
                if (pay_method != null){
                    try {
                        if (PayMethodViewAdapter.CASH_METHOD_ID.equals(pay_method.getString("pay_method_id"))) {
                            mOK.callOnClick();
                        } else {
                            PayMethodDialog payMethodDialog = new PayMethodDialog(mainActivity, pay_method);
                            payMethodDialog.show();
                        }
                        Logger.d_json(pay_method.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        MyDialog.ToastMessage("付款错误：" + e.getMessage(),mainActivity);
                    }
                }
            }
        });
        RecyclerView recyclerView = findViewById(R.id.pay_method_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.HORIZONTAL,false));
        recyclerView.addItemDecoration(new PayMethodItemDecoration(2));
        recyclerView.setAdapter(mPayMethodViewAdapter);
    }

    private void initPayDetailViewAdapter(){
        mPayDetailViewAdapter = new PayDetailViewAdapter(mainActivity);
        mPayDetailViewAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                JSONArray jsonArray = mPayDetailViewAdapter.getDatas();
                double amt = 0.0,zl_amt = 0.0,sum_amt;
                Logger.d_json(jsonArray.toString());
                try {
                    for (int i = 1,length = jsonArray.length();i < length;i ++){//第一个为表头
                        JSONObject object = jsonArray.getJSONObject(i);
                        amt += object.getDouble("pamt");
                        zl_amt += object.getDouble("pzl");
                    }
                    mAmt_received = amt - zl_amt;
                    mPay_balance = mPay_amt - mAmt_received;
                    mCashAmt = mPay_balance;

                    refreshContent();

                    if (Utils.equalDouble(mActual_amt,mAmt_received)){//支付明细数据发送变化后，计算是否已经付款完成，如果完成直接退出付款界面
                        PayDialog.this.dismiss();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.ToastMessage("付款错误：" + e.getMessage(),mainActivity);
                }
            }
        });
        RecyclerView recyclerView = findViewById(R.id.pay_detail_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity, LinearLayoutManager.VERTICAL,false));
        recyclerView.addItemDecoration(new DividerItemDecoration(mainActivity,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mPayDetailViewAdapter);
    }

    public boolean initPayContent(JSONArray datas){
        boolean isTrue = true;
        clearContent();
        for (int i = 0,length = datas.length();i < length; i ++){
            try {
                JSONObject jsonObject = datas.getJSONObject(i);
                mOrder_amt += jsonObject.getDouble("order_amt");
                mDiscount_amt += jsonObject.optDouble("discount_amt",0.00);
                mActual_amt = mOrder_amt - mDiscount_amt;
                mCashAmt = mActual_amt;
                mPay_amt = mActual_amt;
                mPay_balance = mActual_amt;
            } catch (JSONException e) {
                isTrue = false;
                e.printStackTrace();
                MyDialog.ToastMessage("初始化付款信息错误：" + e.getMessage(),mainActivity);
            }
        }
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
        JSONObject cash_json = new JSONObject(),pay_method_json = mPayMethodViewAdapter.get_pay_method(PayMethodViewAdapter.CASH_METHOD_ID);
        if (pay_method_json != null){
            try {
                cash_json.put("pay_method_id",PayMethodViewAdapter.CASH_METHOD_ID);
                cash_json.put("name",pay_method_json.getString("name"));
                cash_json.put("pamt",mCashAmt);
                cash_json.put("pzl",String.format(Locale.CHINA,"%.2f",mZlAmt));
                mPayDetailViewAdapter.addPayDetail(cash_json);
            }catch (JSONException e){
                e.printStackTrace();
                MyDialog.ToastMessage("现金付款错误：" + e.getMessage(),mainActivity);
            }
        }else{
            MyDialog.ToastMessage("现金付款方式不存在！",mainActivity);
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
                        MyDialog.ToastMessage("找零不能大于100",mainActivity);
                    }
                }else{
                    mZlAmt = 0.00;
                    mZlAmtEt.setText(mainActivity.getText(R.string.z_p_z_sz));
                }
            }
        });
        mCashMoneyEt.postDelayed(()-> mCashMoneyEt.requestFocus(),300);
    }

    public boolean showVipInfo(@NonNull JSONObject vip,boolean show){//show为true则只显示不再刷新已销售商品
        mVip = vip;
        LinearLayout vip_info_linearLayout = findViewById(R.id.vip_info_linearLayout);
        if (vip_info_linearLayout != null){
            vip_info_linearLayout.setVisibility(View.VISIBLE);
            ((TextView)vip_info_linearLayout.findViewById(R.id.vip_name)).setText(mVip.optString("name"));
            ((TextView)vip_info_linearLayout.findViewById(R.id.vip_phone_num)).setText(mVip.optString("mobile"));
        }
        return show ? show : initPayContent(mainActivity.showVipInfo(vip));
    }
}
