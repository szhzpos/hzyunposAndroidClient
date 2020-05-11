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
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.callback.EditTextReplacement;
import com.wyc.cloudapp.utils.Utils;

public class ChangeNumOrPriceDialog extends BaseDialog {
    private EditText mNew_price_text;
    private String mInitVal;
    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器
    private ChangeNumOrPriceDialog(MainActivity context, final String title){
        super(context,title);
    }
    public ChangeNumOrPriceDialog(MainActivity context,final String title,final String initVal){
        this(context,title);
        mInitVal = initVal;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentLayout(R.layout.change_price_dialog_layout);

        initNewPrice();

        //初始化数字键盘
        initKeyboard();

        //处理标题
        ((TextView)findViewById(R.id.title)).setText(mTitle);

    }
    @Override
    protected void closeWindow(){
        if (noOnclickListener != null){
            noOnclickListener.onNoClick(ChangeNumOrPriceDialog.this);
        }
        super.closeWindow();
    }

    @Override
    public void dismiss(){
        super.dismiss();
        if (null != mNew_price_text)mNew_price_text.setText(mContext.getString(R.string.space_sz));
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

    public double getContent(){
        Editable editable = mNew_price_text.getText();
        double value = 0.0;
        try {
            if (editable.length() == 0){
                value = Double.valueOf(mInitVal);
            }else{
                value = Double.valueOf(editable.toString());
            }
        }catch (NumberFormatException e){
            e.printStackTrace();
        }
        return value;
    }

    public String getContentToStr(){
        return mNew_price_text.getText().toString();
    }

    public interface onYesOnclickListener {
        void onYesClick(ChangeNumOrPriceDialog myDialog);
    }

    public interface onNoOnclickListener {
        void onNoClick(ChangeNumOrPriceDialog myDialog);
    }

    private void initNewPrice(){
        final EditText et = mNew_price_text =  findViewById(R.id.new_numOrprice_text);
        if (et != null){
            et.setSelectAllOnFocus(true);
            et.setOnFocusChangeListener((v, hasFocus) -> Utils.hideKeyBoard((EditText)v));
            et.postDelayed(et::requestFocus,300);
            if ("".equals(mInitVal)) {
                et.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                et.setTransformationMethod(new EditTextReplacement());
            }
            et.setText(mInitVal);
        }
    }
    private void initKeyboard(){
        final ConstraintLayout  keyboard_layout = findViewById(R.id.keyboard);
        if (null != keyboard_layout)
            for (int i = 0,child  = keyboard_layout.getChildCount(); i < child;i++){
                View tmp_v = keyboard_layout.getChildAt(i);
                int id = tmp_v.getId();
                if (tmp_v instanceof Button){
                    switch (id) {
                        case R.id._back:
                            tmp_v.setOnClickListener(v -> {
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
                            break;
                        case R.id.cancel:
                            tmp_v.setOnClickListener(v -> {
                                if (noOnclickListener != null){
                                    noOnclickListener.onNoClick(ChangeNumOrPriceDialog.this);
                                }else{
                                    ChangeNumOrPriceDialog.this.dismiss();
                                }
                            });
                            break;
                        case R.id._ok:
                            tmp_v.setOnClickListener(v -> {
                                if (yesOnclickListener != null){
                                    yesOnclickListener.onYesClick(ChangeNumOrPriceDialog.this);
                                }
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
            if (view.getId() == R.id.new_numOrprice_text) {
                final EditText tmp_edit = ((EditText)view);
                int index = tmp_edit.getSelectionStart();
                final Editable editable = tmp_edit.getText();
                final String sz_button = ((Button) v).getText().toString();
                if (index != tmp_edit.getSelectionEnd())editable.clear();
                editable.insert(index, sz_button);
            }
        }
    };
}
