package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.callback.EditTextReplacement;
import com.wyc.cloudapp.utils.Utils;

public class ChangeNumOrPriceDialog extends Dialog {
    private Context mContext;
    private EditText new_price_text;
    private String mTitle,mInitVal;
    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器
    private ChangeNumOrPriceDialog(Context context){
        super(context);
        this.mContext = context;
    }
    public ChangeNumOrPriceDialog(Context context,final String title,final String initVal){
        this(context);
        this.mTitle = title;
        mInitVal = initVal;

        setCancelable(false);
        setCanceledOnTouchOutside(false);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(this.getLayoutInflater().inflate(R.layout.change_price_dialog_layout, null));

        new_price_text = findViewById(R.id.new_numOrprice_text);
        new_price_text.setSelectAllOnFocus(true);
        new_price_text.setOnFocusChangeListener((v, hasFocus) -> Utils.hideKeyBoard((EditText)v));
        new_price_text.postDelayed(()->{
            new_price_text.requestFocus();
        },300);
        if ("".equals(mInitVal)) {
            new_price_text.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
            new_price_text.setTransformationMethod(new EditTextReplacement());
        }
        new_price_text.setText(mInitVal);



        findViewById(R.id._ok).setOnClickListener(v -> {
            if (yesOnclickListener != null){
                yesOnclickListener.onYesClick(ChangeNumOrPriceDialog.this);
            }
        });
        findViewById(R.id.cancel).setOnClickListener(v -> {
            if (noOnclickListener != null){
                noOnclickListener.onNoClick(ChangeNumOrPriceDialog.this);
            }else{
                ChangeNumOrPriceDialog.this.dismiss();
            }
        });
        findViewById(R.id._close).setOnClickListener(v -> {
            if (noOnclickListener != null){
                noOnclickListener.onNoClick(ChangeNumOrPriceDialog.this);
            }else{
                ChangeNumOrPriceDialog.this.dismiss();
            }
        });
        findViewById(R.id._back).setOnClickListener(v -> {
            View view =  getCurrentFocus();
            if (view != null) {
                if (view.getId() == R.id.new_numOrprice_text) {
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

        //初始化数字键盘
        ConstraintLayout keyboard_layout;
        keyboard_layout = findViewById(R.id.keyboard);
        for (int i = 0,child  = keyboard_layout.getChildCount(); i < child;i++){
            View tmp_v = keyboard_layout.getChildAt(i);
            int id = tmp_v.getId();
            if (tmp_v instanceof Button && !(id == R.id._back || id == R.id.cancel || id == R.id._ok )){
                tmp_v.setOnClickListener(button_click);
            }
        }

        //处理标题
        ((TextView)findViewById(R.id.title)).setText(mTitle);

    }

    @Override
    public void dismiss(){
        super.dismiss();
        new_price_text.setText(mContext.getString(R.string.space_sz));
    }
    @Override
    public void show(){
        super.show();
    }

    public ChangeNumOrPriceDialog  setNoOnclickListener(onNoOnclickListener onNoOnclickListener) {
        this.noOnclickListener = onNoOnclickListener;
        return this;
    }

    public ChangeNumOrPriceDialog setYesOnclickListener(onYesOnclickListener onYesOnclickListener) {
        this.yesOnclickListener = onYesOnclickListener;
        return  this;
    }

    public double getContentToDouble(){
        Editable editable = new_price_text.getText();
        double code = 0.0;
        try {
            if (editable.length() == 0){
                code = Double.valueOf(mInitVal);
            }else{
                code = Double.valueOf(editable.toString());
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
        return code;
    }

    String getContentToStr(){
        return new_price_text.getText().toString();
    }

    public interface onYesOnclickListener {
        void onYesClick(ChangeNumOrPriceDialog myDialog);
    }

    public interface onNoOnclickListener {
        void onNoClick(ChangeNumOrPriceDialog myDialog);
    }

    private View.OnClickListener button_click = v -> {
        View view =  getCurrentFocus();
        if (view != null) {
            if (view.getId() == R.id.new_numOrprice_text) {
                EditText tmp_edit = ((EditText)view);
                int index = tmp_edit.getSelectionStart();
                Editable editable = tmp_edit.getText();
                String sz_button = ((Button) v).getText().toString();
                if (index != tmp_edit.getSelectionEnd())editable.clear();
                editable.insert(index, sz_button);
            }
        }
    };
}
