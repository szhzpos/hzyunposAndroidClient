package com.wyc.cloudapp.interface_abstract;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.wyc.cloudapp.R;
import com.wyc.cloudapp.dialog.CustomProgressDialog;
import com.wyc.cloudapp.utils.Utils;
import org.json.JSONObject;
import java.util.Locale;

public abstract class AbstractPayDialog extends Dialog implements IPay {
    protected EditText mPayAmtEt,mPayCode;
    protected Context mContext;
    protected Button mOk;
    protected CustomProgressDialog mProgressDialog;
    protected JSONObject mPayMethod;
    protected onYesOnclickListener mYesOnclickListener;
    protected double mPayAmt = 0.0;
    public AbstractPayDialog(@NonNull Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.pay_method_dialog_content);
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        mProgressDialog = new CustomProgressDialog(mContext);
        mOk = findViewById(R.id._ok);

        //初始化付款码
        init_pay_code();

        //初始化金额text
        init_c_amount();

        //初始化按钮事件
        findViewById(R.id._close).setOnClickListener(view-> AbstractPayDialog.this.dismiss());
        findViewById(R.id._back).setOnClickListener(v -> {
            View view =  getCurrentFocus();
            if (view != null) {
                int id = view.getId();
                if (id == R.id.c_amt || id == R.id.pay_code) {
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
        findViewById(R.id._cancel).setOnClickListener(view -> AbstractPayDialog.this.dismiss());
        mOk.setOnClickListener(v -> {
            if (mYesOnclickListener != null)mYesOnclickListener.onYesClick(AbstractPayDialog.this);
        });

        //初始化数字键盘
        ConstraintLayout keyboard_linear_layout;
        keyboard_linear_layout = findViewById(R.id.keyboard);
        for (int i = 0,child  = keyboard_linear_layout.getChildCount(); i < child;i++){
            View tmp_v = keyboard_linear_layout.getChildAt(i);
            int id = tmp_v.getId();
            if (tmp_v instanceof Button && !(id == R.id._back || id == R.id._cancel || id == R.id._ok)){
                tmp_v.setOnClickListener(button_click);
            }
        }
    }

    private View.OnClickListener button_click = v -> {
        View view =  getCurrentFocus();
        if (view != null) {
            int id = view.getId();
            if (id == R.id.c_amt || id == R.id.pay_code) {
                EditText tmp_edit = ((EditText)view);
                int index = tmp_edit.getSelectionStart();
                Editable editable = tmp_edit.getText();
                String sz_button = ((Button) v).getText().toString();
                if (index != tmp_edit.getSelectionEnd())editable.clear();
                editable.insert(index, sz_button);
            }
        }
    };

    @Override
    public void setPayAmt(double amt) {
        mPayAmt = amt;
    }

    @Override
    public JSONObject getContent() {
        return null;
    }

    protected void setTitle(final String title){
        ((TextView)findViewById(R.id.title)).setText(title);
    }

    protected void setHint(final String hint){
        mPayAmtEt.setHint(hint);
    }

    protected void refreshContent(){

    }

    protected abstract void initPayMethod();

    public AbstractPayDialog setYesOnclickListener(onYesOnclickListener listener) {
        if (listener != null){
            mYesOnclickListener = listener;
        }
        return this;
    }

    public interface onYesOnclickListener {
        void onYesClick(AbstractPayDialog dialog);
    }

    private void init_c_amount(){
        mPayAmtEt = findViewById(R.id.c_amt);
        mPayAmtEt.setText(String.format(Locale.CHINA,"%.2f",mPayAmt));
        mPayAmtEt.setSelectAllOnFocus(true);
        mPayAmtEt.setOnFocusChangeListener((view, b) -> Utils.hideKeyBoard((EditText) view));
        mPayAmtEt.postDelayed(()->{ mPayAmtEt.requestFocus();},300);
    }

    private void init_pay_code(){
        mPayCode = findViewById(R.id.pay_code);
        mPayCode.setSelectAllOnFocus(true);
        mPayCode.setOnFocusChangeListener((view, b) -> Utils.hideKeyBoard((EditText) view));
    }
}
