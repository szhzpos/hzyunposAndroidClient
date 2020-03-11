package com.wyc.cloudapp.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.wyc.cloudapp.R;
import com.wyc.cloudapp.utils.Utils;

import static android.content.Context.WINDOW_SERVICE;

public class ChangeNumOrPriceDialog extends Dialog {
    private Context mContext;
    private EditText new_price_text;
    private TextView mTitle;
    private String szTitle;
    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器
    public ChangeNumOrPriceDialog(Context context){
        super(context);
        this.mContext = context;
    }
    public ChangeNumOrPriceDialog(Context context,String sztitle){
        this(context);
        this.szTitle = sztitle;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(this.getLayoutInflater().inflate(R.layout.change_price_content, null));
        setCancelable(false);
        setCanceledOnTouchOutside(false);

        new_price_text = findViewById(R.id.new_numOrprice_text);
        new_price_text.setSelectAllOnFocus(true);
        new_price_text.setOnFocusChangeListener((v, hasFocus) -> Utils.hideKeyBoard((EditText)v));
        new_price_text.postDelayed(()->{
            new_price_text.requestFocus();
            new_price_text.setSelection(0);
        },500);

        mTitle = findViewById(R.id.title);
        mTitle.setText(szTitle);

        findViewById(R.id._ok).setOnClickListener(v -> {
            if (yesOnclickListener != null){
                yesOnclickListener.onYesClick(ChangeNumOrPriceDialog.this);
            }
        });
        findViewById(R.id.cancel).setOnClickListener(v -> {
            if (noOnclickListener != null){
                noOnclickListener.onNoClick(ChangeNumOrPriceDialog.this);
            }
        });
        findViewById(R.id._close).setOnClickListener(v -> {
            ChangeNumOrPriceDialog.this.dismiss();
        });
        findViewById(R.id._back).setOnClickListener(v -> {
            View view =  getCurrentFocus();
            if (view != null) {
                EditText tmp_edit = ((EditText)view);
                int index = tmp_edit.getSelectionStart(),end = tmp_edit.getSelectionEnd();
                if (index != end && end  == tmp_edit.getText().length()){
                    tmp_edit.setText(mContext.getString(R.string.d_zero_point_sz));
                }else {
                    if (index == 0)return;
                    if (view.getId() == R.id.new_numOrprice_text) {
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
        ConstraintLayout keyboard_layout;
        keyboard_layout = findViewById(R.id.keyboard);
        for (int i = 0,child  = keyboard_layout.getChildCount(); i < child;i++){
            View tmp_v = keyboard_layout.getChildAt(i);
            int id = tmp_v.getId();
            if (tmp_v instanceof Button && !(id == R.id._back || id == R.id.cancel || id == R.id._ok )){
                tmp_v.setOnClickListener(button_click);
            }
        }
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

    public double getNewNumOrPrice(){
        return  Double.valueOf(new_price_text.getText().toString());
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
            EditText tmp_edit = ((EditText)view);
            int index = tmp_edit.getSelectionStart();
            String sz_button = ((Button) v).getText().toString();
            if (view.getId() == R.id.new_numOrprice_text) {
                if (".".equals(sz_button)) {
                    tmp_edit.setSelection(tmp_edit.getText().toString().indexOf(".") + 1);
                } else {
                    if (index > tmp_edit.getText().toString().indexOf(".")) {
                        if (index != tmp_edit.length())
                            tmp_edit.getText().delete(index, index + 1).insert(index, sz_button);
                            /*else
                                tmp_edit.setSelection(tmp_edit.getText().toString().indexOf("."));*/
                    } else {
                        if (index == 0 && "0".equals(sz_button)) return;
                        tmp_edit.getText().insert(index, sz_button);
                    }
                }
            }

        }
    };

}
