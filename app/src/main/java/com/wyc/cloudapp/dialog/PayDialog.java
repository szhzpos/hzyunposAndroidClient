package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.adapter.PayDetailItemDecoration;
import com.wyc.cloudapp.adapter.PayDetailViewAdapter;
import com.wyc.cloudapp.adapter.PayMethodItemDecoration;
import com.wyc.cloudapp.adapter.PayMethodViewAdapter;
import com.wyc.cloudapp.adapter.SaleGoodsViewAdapter;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class PayDialog extends Dialog {
    private Context mContext;
    private EditText mCashMoneyEt,mZlAmtEt;
    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器
    private PayMethodViewAdapter mPayMethodViewAdapter;
    private PayDetailViewAdapter mPayDetailViewAdapter;
    private SaleGoodsViewAdapter mSaleGoodsViewAdpter;
    private TextView mOrderAmtTv,mDiscountAmtTv,mActualAmtTv,mPayAmtTv,mAmtReceivedTv,mPayBalanceTv;
    private double mOrder_amt = 0.0,mDiscount_amt = 0.0,mActual_amt = 0.0,mPay_amt = 0.0,mAmt_received = 0.0,mPay_balance = 0.0,mCashAmt = 0.0,mZlAmt = 0.0;
    private Button mOK;
    public PayDialog(Context context,SaleGoodsViewAdapter saleGoodsViewAdapter){
        super(context);
        this.mContext = context;
        mSaleGoodsViewAdpter = saleGoodsViewAdapter;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(this.getLayoutInflater().inflate(R.layout.pay_dialog_content, null));
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
        initPayMethodAdapter();

        //初始化支付明细
        initPayDetailViewAdapter();

        //初始化现金金额
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
                mCashAmt = Double.valueOf(editable.toString());
                if ((mZlAmt = mCashAmt - mPay_balance) > 0){
                    if (mZlAmt < 100)
                        mZlAmtEt.setText(String.format(Locale.CHINA,"%.2f",mZlAmt));
                    else{
                        mCashMoneyEt.setText(mPayBalanceTv.getText());
                        mCashMoneyEt.selectAll();
                        MyDialog.ToastMessage("找零不能大于100",mContext);
                    }
                }else{
                    mZlAmt = 0.00;
                    mZlAmtEt.setText(mContext.getText(R.string.z_p_z_sz));
                }
            }
        });
        mCashMoneyEt.postDelayed(()->{
            mCashMoneyEt.requestFocus();
        },300);

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
                EditText tmp_edit = ((EditText)view);
                int index = tmp_edit.getSelectionStart(),end = tmp_edit.getSelectionEnd();
                if (index !=end && end == tmp_edit.getText().length()){
                    tmp_edit.setText(mContext.getString(R.string.d_zero_point_sz));
                }else{
                    if (index == 0)return;
                    if (view.getId() == R.id.cash_amt) {
                        if (index == tmp_edit.getText().toString().indexOf(".") + 1) {
                            tmp_edit.setSelection(index - 1);
                        } else if (index > tmp_edit.getText().toString().indexOf(".")) {
                            tmp_edit.getText().replace(index - 1, index, "0");
                            tmp_edit.setSelection(index - 1);
                        } else {
                            tmp_edit.getText().delete(index - 1, index);
                        }
                    }
                }
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

    public interface onYesOnclickListener {
        void onYesClick(PayDialog myDialog);
    }

    public interface onNoOnclickListener {
        void onNoClick(PayDialog myDialog);
    }

    private View.OnClickListener button_click = v -> {
        View view =  getCurrentFocus();
        if (view != null) {
            EditText tmp_edit = ((EditText)view);
            int index = tmp_edit.getSelectionStart();
            String sz_button = ((Button) v).getText().toString();
            if (tmp_edit.getSelectionEnd() == tmp_edit.getText().length()){
                tmp_edit.setText(mContext.getString(R.string.d_zero_point_sz));
            }
            if (view.getId() == R.id.cash_amt) {
                if (".".equals(sz_button)) {
                    tmp_edit.setSelection(tmp_edit.getText().toString().indexOf(".") + 1);
                } else {
                    if (index > tmp_edit.getText().toString().indexOf(".")) {
                        if (index != tmp_edit.length())
                            tmp_edit.getText().delete(index, index + 1).insert(index, sz_button);
                    } else {
                        if (index == 0 && "0".equals(sz_button)) return;
                        tmp_edit.getText().insert(index, sz_button);
                    }
                }
            }

        }
    };

    private void initPayMethodAdapter(){
        mPayMethodViewAdapter = new PayMethodViewAdapter(mContext);
        mPayMethodViewAdapter.setDatas();
        mPayMethodViewAdapter.setOnItemClickListener(new PayMethodViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int pos) {
                JSONObject pay_method = mPayMethodViewAdapter.getItem(pos);
                String id;
                if (pay_method != null){
                    try {
                        id = pay_method.getString("pay_method_id");
                        if (PayMethodViewAdapter.CASH_METHOD_ID.equals(id)){
                            mOK.callOnClick();
                        }
                        Logger.d_json(pay_method.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                        MyDialog.ToastMessage("付款错误：" + e.getMessage(),mContext);
                    }
                }
            }
        });
        RecyclerView recyclerView = findViewById(R.id.pay_method_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL,false));
        recyclerView.addItemDecoration(new PayMethodItemDecoration(2));
        recyclerView.setAdapter(mPayMethodViewAdapter);
    }

    private void initPayDetailViewAdapter(){
        mPayDetailViewAdapter = new PayDetailViewAdapter(mContext);
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

                    if (mActual_amt == mAmt_received){//支付明细数据发送变化后，计算是否已经付款完成，如果完成直接退出付款界面
                        PayDialog.this.dismiss();
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                    MyDialog.ToastMessage("付款错误：" + e.getMessage(),mContext);
                }
            }
        });
        RecyclerView recyclerView = findViewById(R.id.pay_detail_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL,false));
        /*recyclerView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int getVerSpacing(int viewHeight,int m_height){
                int vertical_space ,vertical_counts,per_vertical_space;
                vertical_space = viewHeight % m_height;
                vertical_counts = viewHeight / m_height;
                per_vertical_space = vertical_space / (vertical_counts != 0 ? vertical_counts:1);

                return per_vertical_space;
            }
            @Override
            public void onGlobalLayout() {
                recyclerView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int height = recyclerView.getMeasuredHeight();
                float itemHeight = mContext.getResources().getDimension(R.dimen.sale_goods_height);
                recyclerView.addItemDecoration(new PayDetailItemDecoration(getVerSpacing(height,(int) itemHeight)));
            }
        });*/
        recyclerView.addItemDecoration(new DividerItemDecoration(mContext,DividerItemDecoration.VERTICAL));
        recyclerView.setAdapter(mPayDetailViewAdapter);
    }

    public boolean initPayContent(JSONArray datas){
        boolean isTrue = true;
        for (int i = 0,length = datas.length();i < length; i ++){
            try {
                JSONObject jsonObject = datas.getJSONObject(i);
                mOrder_amt += jsonObject.getDouble("sale_sum_amt");
                mDiscount_amt += jsonObject.optDouble("discount_amt",0.00);
                mActual_amt = mOrder_amt - mDiscount_amt;
                mCashAmt = mActual_amt;
                mPay_amt = mActual_amt;
                mPay_balance = mActual_amt;
            } catch (JSONException e) {
                isTrue = false;
                e.printStackTrace();
                MyDialog.ToastMessage("初始化付款信息错误：" + e.getMessage(),mContext);
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
    }

    private void cash_pay(){
        JSONObject cash_json = new JSONObject(),pay_method_json = mPayMethodViewAdapter.get_pay_method(PayMethodViewAdapter.CASH_METHOD_ID);
        if (pay_method_json != null){
            try {
                String id = pay_method_json.getString("pay_method_id");

                cash_json.put("pay_method_id",id);
                cash_json.put("name",pay_method_json.getString("name"));
                cash_json.put("pamt",mCashAmt);
                cash_json.put("pzl",String.format(Locale.CHINA,"%.2f",mZlAmt));

                mPayDetailViewAdapter.addPayDetail(cash_json);
            }catch (JSONException e){
                e.printStackTrace();
                MyDialog.ToastMessage("现金付款错误：" + e.getMessage(),mContext);
            }
        }else{
            MyDialog.ToastMessage("现金付款方式不存在！",mContext);
        }
    }
}
