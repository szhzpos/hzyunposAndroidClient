package com.wyc.cloudapp.dialog.pay;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.alibaba.fastjson.JSONObject;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.dialog.MyDialog;
import com.wyc.cloudapp.utils.Utils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public abstract class AbstractPayDialog extends Dialog implements IPay {
    protected EditText mPayAmtEt,mPayCode;
    protected Context mContext;
    protected Button mOk;
    protected CustomProgressDialog mProgressDialog;
    protected JSONObject mPayMethod;
    protected onYesOnclickListener mYesOnclickListener;
    protected double mOriginalPayAmt = 0.0;
    protected Window mDialogWindow;
    private TextView mTitleTv;
    private onCancelListener mCancelListener;
    public AbstractPayDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.pay_method_dialog_layout);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        mProgressDialog = new CustomProgressDialog(mContext);
        mOk = findViewById(R.id._ok);
        mTitleTv = findViewById(R.id.title);

        //初始化付款码
        init_pay_code();

        //初始化金额text
        init_c_amount();

        //初始化按钮事件
        findViewById(R.id._close).setOnClickListener(view-> findViewById(R.id._cancel).callOnClick());

        //初始化数字键盘
        initKeyboard();

        //回车监听
        setOnKeyListener((dialog, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER){
                if (mOk != null)mOk.callOnClick();
                return true;
            }
            return false;
        });

    }
    @Override
    public void onAttachedToWindow(){
        super.onAttachedToWindow();
        mDialogWindow = getWindow();
    }
    @Override
    public void onDetachedFromWindow(){
        super.onDetachedFromWindow();
        mDialogWindow = getWindow();
    }

    private void initKeyboard(){
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
                                int tmp_id = view.getId();
                                if (tmp_id == R.id.c_amt || tmp_id == R.id.pay_code) {
                                    EditText tmp_edit = ((EditText)view);
                                    int index = tmp_edit.getSelectionStart(),end = tmp_edit.getSelectionEnd();
                                    if (index != end && end  == tmp_edit.getText().length()){
                                        tmp_edit.setText(mContext.getString(R.string.space_sz));
                                    }else{
                                        if (index == 0)return;
                                        tmp_edit.getText().delete(index - 1, index);
                                    }
                                }
                            }
                        });
                        break;
                    case R.id._cancel:
                        findViewById(id).setOnClickListener(view -> {
                            if (mCancelListener != null)
                                mCancelListener.onCancel(AbstractPayDialog.this);
                            else
                                AbstractPayDialog.this.dismiss();
                        });
                        break;
                    case R.id._ok:
                        mOk.setOnClickListener(v -> {
                            if (verify() && mYesOnclickListener != null)mYesOnclickListener.onYesClick(AbstractPayDialog.this);
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
            int id = view.getId();
            if (id == R.id.c_amt || id == R.id.pay_code) {
                final EditText tmp_edit = ((EditText)view);
                int index = tmp_edit.getSelectionStart();
                final Editable editable = tmp_edit.getText();
                final String sz_button = ((Button) v).getText().toString();
                if (index != tmp_edit.getSelectionEnd())editable.clear();
                editable.insert(index, sz_button);
            }
        }

        this.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if (i == KeyEvent.KEYCODE_ENTER){
                    if (mOk != null)mOk.callOnClick();
                }
                return false;
            }
        });
    };

    @Override
    public void setPayAmt(double amt) {
        mOriginalPayAmt = amt;
    }

    @Override
    public JSONObject getContent() {
        return null;
    }

    protected void setTitle(final String title){
        if (mTitleTv != null)mTitleTv.setText(title);
    }

    protected String getTitle(){
        mTitleTv.getText();
        return "";
    }

    protected void setHint(final String hint){
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

    @SuppressLint("SimpleDateFormat")
    String getPayCode() {
        String pos_num = "";
        if (mContext instanceof MainActivity){
            pos_num = ((MainActivity)mContext).getPosNum();
        }
        return new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + pos_num + Utils.getNonce_str(8);
    }

    protected boolean verify(){
        if (mPayAmtEt.getVisibility() == View.VISIBLE &&(mPayAmtEt.length() == 0 || Utils.equalDouble(getPayAmt(),0.0))){
            mPayAmtEt.requestFocus();
            return MyDialog.ToastMessage(mPayAmtEt,mPayAmtEt.getHint().toString().concat(mContext.getString(R.string.not_zero_hint_sz)),mContext,getWindow(),false);
        }
        if (mPayCode.getVisibility() == View.VISIBLE && mPayCode.length() == 0){
            mPayCode.requestFocus();
            return MyDialog.ToastMessage(mPayCode,mPayCode.getHint().toString().concat(mContext.getString(R.string.not_empty_hint_sz)),mContext,getWindow(),false);
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
        mPayAmtEt.setSelectAllOnFocus(true);
        mPayAmtEt.setOnFocusChangeListener((view, b) -> Utils.hideKeyBoard((EditText) view));
    }

    private void init_pay_code(){
        mPayCode = findViewById(R.id.pay_code);
        mPayCode.setSelectAllOnFocus(true);
        mPayCode.setOnFocusChangeListener((view, b) -> Utils.hideKeyBoard((EditText) view));
    }

}
