package com.wyc.cloudapp.dialog.pay;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.activity.mobile.cashierDesk.MobileCashierActivity;
import com.wyc.cloudapp.adapter.PayMethodViewAdapter;
import com.wyc.cloudapp.bean.CouponResult;
import com.wyc.cloudapp.bean.DiscountCouponInfo;
import com.wyc.cloudapp.constants.InterfaceURL;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.JEventLoop;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.logger.Logger;
import com.wyc.cloudapp.utils.Utils;
import com.wyc.cloudapp.utils.http.HttpRequest;
import com.wyc.cloudapp.utils.http.HttpUtils;
import com.wyc.cloudapp.utils.http.callback.TypeCallback;

import static com.wyc.cloudapp.constants.ScanCallbackCode.PAY_REQUEST_CODE;

import java.util.Objects;

public class PayMethodDialogImp extends AbstractPayDialog implements MobileCashierActivity.ScanCallback {
    private DiscountCouponInfo mCouponDetail;
    public PayMethodDialogImp(@NonNull MainActivity context, @NonNull final JSONObject pay_method) {
        super(context,Utils.getNullStringAsEmpty(pay_method,"name"));
        mPayMethod = Objects.requireNonNull(pay_method);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        initPayMethod();
        setWatcherToPayAmt();
    }

    @Override
    public JSONObject getContent() {
        mPayMethod.put("pay_code",getPayCode(mContext.getPosNum()));
        mPayMethod.put("pamt", mPayAmtEt.getText().toString());
        mPayMethod.put("pzl",0.00);
        mPayMethod.put("v_num",mPayCode.getText().toString());
         return mPayMethod;
    }

    @Override
    protected boolean verifyValid() {
        if (PayMethodViewAdapter.isDiscountCouponPay(mPayMethod)){
            final String member_id = Utils.getNullStringAsEmpty(mPayMethod,"member_id");
            if (Utils.isNotEmpty(member_id)){
                final JEventLoop loop = new JEventLoop();

                final JSONObject param = new JSONObject();
                param.put("appid",mContext.getAppId());
                param.put("stores_id",mContext.getStoreId());
                param.put("member_id",member_id);
                param.put("cas_id",mContext.getCashierId());
                param.put("logno",mPayCode.getText().toString());

                final CustomProgressDialog progressDialog = CustomProgressDialog.showProgress(mContext,mContext.getString(R.string.query_coupon_info));
                final String sz_param = HttpRequest.generate_request_parma(param,mContext.getAppSecret());
                HttpUtils.sendAsyncPost(mContext.getUrl() + InterfaceURL.COUPON_CHECK,sz_param).enqueue(new TypeCallback<CouponResult>(CouponResult.class) {
                    @Override
                    protected void onError(String msg) {
                        MyDialog.toastMessage(msg);
                        loop.done(0);
                    }

                    @Override
                    protected void onSuccess(CouponResult data) {
                        if (data.isSuccess()){
                            final DiscountCouponInfo discountCouponDetails = data.getDetail();
                            Logger.d(discountCouponDetails);
                            if (discountCouponDetails != null){
                                if (discountCouponDetails.hasValid()){
                                    mCouponDetail = discountCouponDetails;
                                    loop.done(1);
                                }else {
                                    loop.done(0);
                                }
                            }else {
                                MyDialog.toastMessage(R.string.not_exist_coupon_hint);
                                loop.done(0);
                            }
                        }else {
                            MyDialog.toastMessage(data.getInfo());
                            loop.done(0);
                        }
                    }
                });
                boolean code = loop.exec() == 1;
                progressDialog.dismiss();
                return code;
            }else {
                MyDialog.toastMessage(R.string.discount_coupon_hints);
                return false;
            }
        }else mCouponDetail = null;

        return super.verifyValid();
    }
    public DiscountCouponInfo getCouponDetail(){
        return mCouponDetail;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void initPayMethod(){
        if (mPayMethod != null) {
            if (PayMethodViewAdapter.isDiscountCouponPay(mPayMethod) || mPayMethod.getIntValue("is_check") != 2){ //显示付款码输入框
                if (mContext.lessThan7Inches()){
                    mPayCode.setOnTouchListener((view, motionEvent) -> {
                        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                            final float dx = motionEvent.getX();
                            final int w = mPayCode.getWidth();
                            if (dx > (w - mPayCode.getCompoundPaddingRight())) {
                                mPayCode.requestFocus();
                                final Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                                mContext.startActivityForResult(intent, PAY_REQUEST_CODE);
                            }
                        }
                        return false;
                    });
                    mContext.setScanCallback(this);
                }

                if (mPayMethod.containsKey("card_code"))
                    mPayCode.setText(mPayMethod.getString("card_code"));
                else
                    mPayCode.setHint(mPayMethod.getString("xtype"));

                mPayCode.postDelayed(()->mPayCode.requestFocus(),350);
                mPayCode.setVisibility(View.VISIBLE);

                //mPayAmtEt.setEnabled(false);
                if (Utils.equalDouble(mOriginalPayAmt,0.0)){
                    mPayAmtEt.setVisibility(View.GONE);
                }
            }else{
                mPayCode.clearFocus();

                mPayCode.setOnTouchListener(null);
                mContext.setScanCallback(null);

                mPayCode.getText().clear();
                mPayCode.setVisibility(View.GONE);
                mPayAmtEt.postDelayed(()-> mPayAmtEt.requestFocus(),300);
            }
        }
    }
    @Override
    public void callback(String code) {
        if (mPayCode.isShown()){
            mPayCode.setText(code);
            if (mOk != null)mOk.callOnClick();
        }
    }

    private void setWatcherToPayAmt(){
        //付款方式不能找零
        mPayAmtEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                if (length > 0){
                    int index = editable.toString().indexOf('.');
                    if (length == 1 && index > -1)return;
                    if (index > -1 && length >= (index += 3)){//保留两位小数
                        Logger.d("index:%d",index);
                        editable.delete(index,editable.length());
                    }
                    if (Double.parseDouble(editable.toString()) - mOriginalPayAmt> 0){
                        refreshContent();
                        MyDialog.SnackBarMessage(getWindow(),getTitle().concat(mContext.getString(R.string.not_zl_hint_sz)),null);
                    }
                }
            }
        });
    }
}
