package com.wyc.cloudapp.dialog;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.activity.MainActivity;
import com.wyc.cloudapp.callback.PasswordEditTextReplacement;
import com.wyc.cloudapp.dialog.CustomizationView.KeyboardView;
import com.wyc.cloudapp.dialog.baseDialog.AbstractDialogBaseOnMainActivityImp;
import com.wyc.cloudapp.utils.Utils;

public class ChangeNumOrPriceDialog extends AbstractDialogBaseOnMainActivityImp {
    private EditText mNew_price_text;
    private String mInitVal;
    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器
    private ChangeNumOrPriceDialog(MainActivity context, final String title){
        super(context,title);
    }
    public ChangeNumOrPriceDialog(MainActivity context, final String title, final String initVal){
        this(context,title);
        mInitVal = initVal;
    }
    public ChangeNumOrPriceDialog(MainActivity context, final CharSequence title, final String initVal){
        super(context,title);
        mInitVal = initVal;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initNewPrice();

        //初始化数字键盘
        initKeyboardView();

    }
    @Override
    protected int getContentLayoutId(){
        return R.layout.change_price_dialog_layout;
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

    public ChangeNumOrPriceDialog setNoOnclickListener(onNoOnclickListener onNoOnclickListener) {
        this.noOnclickListener = onNoOnclickListener;
        return this;
    }

    public ChangeNumOrPriceDialog setYesOnclickListener(onYesOnclickListener onYesOnclickListener) {
        this.yesOnclickListener = onYesOnclickListener;
        return  this;
    }

    public double getContent(){
        final Editable editable = mNew_price_text.getText();
        double value = 0.0;
        try {
            if (editable.length() == 0){
                if (null == mInitVal)
                    value = 0.0;
                else
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
            if ("".equals(mInitVal)) {
                et.setInputType(InputType.TYPE_NUMBER_VARIATION_PASSWORD);
                et.setTransformationMethod(new PasswordEditTextReplacement());
            }
            et.setText(mInitVal);
        }
    }
    private void initKeyboardView(){
        final KeyboardView view = findViewById(R.id.keyboard_view);
        view.layout(R.layout.change_price_keyboard_layout);
        view.setCurrentFocusListenner(() -> {
            final View focus = getCurrentFocus();
            if (focus instanceof EditText){
                return (EditText) focus;
            }
            return null;
        });
        view.setCancelListener(v -> closeWindow());
        view.setOkListener(v -> {
            if (yesOnclickListener != null){
                yesOnclickListener.onYesClick(ChangeNumOrPriceDialog.this);
            }
        });
    }
}
