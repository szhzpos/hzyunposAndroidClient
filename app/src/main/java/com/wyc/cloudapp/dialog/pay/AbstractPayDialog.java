package com.wyc.cloudapp.dialog.pay;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.base.MainActivity;
import com.wyc.cloudapp.application.CustomApplication;
import com.wyc.cloudapp.customizationView.KeyboardView;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogMainActivity;
import com.wyc.cloudapp.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class AbstractPayDialog extends AbstractDialogMainActivity implements IPay {
    protected EditText mPayAmtEt,mPayCode;
    protected Button mOk;
    protected CustomProgressDialog mProgressDialog;
    protected JSONObject mPayMethod;
    protected onYesOnclickListener mYesOnclickListener;
    protected double mOriginalPayAmt = 0.0;
    private onCancelListener mCancelListener;
    private boolean modifyPayAmt = true;
    public AbstractPayDialog(@NonNull MainActivity context, final String title) {
        super(context,title);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mProgressDialog = new CustomProgressDialog(mContext);

        //初始化付款码
        init_pay_code();

        //初始化金额text
        init_c_amount();

        //初始化数字键盘
        initKeyboardView();
    }

    public AbstractPayDialog setModifyPayAmt(boolean modifyPayAmt) {
        this.modifyPayAmt = modifyPayAmt;
        return this;
    }

    @Override
    protected int getContentLayoutId(){
        return R.layout.pay_method_dialog_layout;
    }

    @Override
    public void closeWindow(){
        if (mCancelListener != null)
            mCancelListener.onCancel(this);
        else
            this.dismiss();
    }

    @Override
    public void setPayAmt(double amt) {
        mOriginalPayAmt = amt;
    }

    @Override
    public JSONObject getContent() {
        return null;
    }


    @Override
    public MainActivity getPrivateContext() {
        return mContext;
    }

    @Override
    public boolean hookEnterKey() {
        if (mOk != null){
            CustomApplication.postAtFrontOfQueue(()-> mOk.callOnClick());
        }
        return true;
    }

    protected final String getTitle(){
        return mTitle.toString();
    }

    protected final void setHint(final String hint){
        mPayAmtEt.setHint(hint);
    }

    void refreshContent(){
        if (mPayAmtEt != null){
            mPayAmtEt.setText(String.format(Locale.CHINA,"%.2f",mOriginalPayAmt));
            mPayAmtEt.selectAll();
        }
    }

    private double getPayAmt(){
        if (mPayAmtEt != null){
            try {
                return Double.valueOf(mPayAmtEt.getText().toString());
            }catch (NumberFormatException e){
                e.printStackTrace();
                return 0.0;
            }
        }
        return 0.0;
    }

    protected abstract void initPayMethod();

    /**
     * 返回支付方式对象之前先验证是否有效,针对某些需要线上验证支付方式，如优惠券等。
     * */
    protected boolean verifyValid(){
        return true;
    }

    public static String getPayCode(final String pos_num) {
        return new SimpleDateFormat("yyyyMMddHHmmssSSS",Locale.CHINA).format(new Date()) + pos_num + Utils.getNonce_str(8);
    }

    /**
     * 验证必填参数
     * */
    protected boolean verify(){
        if (mPayAmtEt.getVisibility() == View.VISIBLE &&(mPayAmtEt.length() == 0 || Utils.equalDouble(getPayAmt(),0.0))){
            mPayAmtEt.requestFocus();
            return MyDialog.ToastMessage(mPayAmtEt,mContext.getString(R.string.not_empty_hint_sz,mPayAmtEt.getHint().toString()), getWindow(),false);
        }
        if (mPayCode.getVisibility() == View.VISIBLE && mPayCode.length() == 0){
            mPayCode.requestFocus();
            return MyDialog.ToastMessage(mPayCode,mContext.getString(R.string.not_empty_hint_sz,mPayCode.getHint()), getWindow(),false);
        }
        return true;
    }

    public AbstractPayDialog setYesOnclickListener(onYesOnclickListener listener) {
        if (listener != null){
            mYesOnclickListener = listener;
        }
        return this;
    }
    AbstractPayDialog setCancelListener(onCancelListener listener){
        mCancelListener = listener;
        return this;
    }

    public interface onYesOnclickListener {
        void onYesClick(AbstractPayDialog dialog);
    }
    public interface onCancelListener{
        void onCancel(AbstractPayDialog dialog);
    }

    private void init_c_amount(){
        mPayAmtEt = findViewById(R.id.c_amt);
        mPayAmtEt.setText(String.format(Locale.CHINA,"%.2f",mOriginalPayAmt));
        mPayAmtEt.setEnabled(modifyPayAmt);
    }

    private void init_pay_code(){
        mPayCode = findViewById(R.id.pay_code);
    }

    private void initKeyboardView(){
        final KeyboardView view = findViewById(R.id.keyboard_view);
        if (mContext.lessThan7Inches()){
            view.layout(R.layout.mobile_pay_method_keyboard_layout);
        }else
            view.layout(R.layout.pay_method_keyboard_layout);
        view.setCurrentFocusListener(() -> {
            final View focus = getCurrentFocus();
            if (focus instanceof EditText){
                return (EditText) focus;
            }
            return null;
        });
        view.setCancelListener(v -> closeWindow());
        view.setOkListener(v -> {
            if (verify() && verifyValid()){
                if (mYesOnclickListener != null)
                    mYesOnclickListener.onYesClick(AbstractPayDialog.this);
                else
                    setCodeAndExit(1);
            }
        });
        mOk = view.getOkBtn();
    }

    @Override
    protected void initWindowSize(){
        if (mContext.lessThan7Inches()){
            widthFullScreen();
        }else
            getWindow().setLayout((int) mContext.getResources().getDimension(R.dimen.size_382), ViewGroup.LayoutParams.WRAP_CONTENT);
    }

}
