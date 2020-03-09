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

public class PayDialog extends Dialog {
    private Context mContext;
    private EditText mMoney;
    private onNoOnclickListener noOnclickListener;//取消按钮被点击了的监听器
    private onYesOnclickListener yesOnclickListener;//确定按钮被点击了的监听器
    public PayDialog(Context context){
        super(context);
        this.mContext = context;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(this.getLayoutInflater().inflate(R.layout.pay_dialog_content, null));
        setCancelable(false);
        setCanceledOnTouchOutside(false);




        //初始化金额
        mMoney = findViewById(R.id.money);
        mMoney.setOnFocusChangeListener((view, b) -> Utils.hideKeyBoard((EditText) view));
        mMoney.postDelayed(()->{
            mMoney.requestFocus();
        },300);

        //初始化按钮
        findViewById(R.id._close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PayDialog.this.dismiss();
            }
        });
        findViewById(R.id._ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (yesOnclickListener != null){
                    yesOnclickListener.onYesClick(PayDialog.this);
                }
            }
        });
        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noOnclickListener != null){
                    noOnclickListener.onNoClick(PayDialog.this);
                }
            }
        });
        findViewById(R.id._back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View view =  getCurrentFocus();
                if (view != null) {
                    EditText tmp_edit = ((EditText)view);
                    int index = tmp_edit.getSelectionStart();
                    if (index == 0)return;
                    switch (view.getId()){
                        case R.id.money:
                            if (index == tmp_edit.getText().toString().indexOf(".") + 1){
                                tmp_edit.setSelection(index - 1);
                            }else if (index > tmp_edit.getText().toString().indexOf(".")){
                                tmp_edit.getText().replace(index - 1, index,"0");
                                tmp_edit.setSelection(index - 1);
                            }else {
                                tmp_edit.getText().delete(index - 1, index);
                            }
                            break;
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

        //初始化对话框大小
        WindowManager m = (WindowManager)mContext.getSystemService(WINDOW_SERVICE);
        if (m != null){
            Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
            Point point = new Point();
            d.getSize(point);
            Window dialogWindow = this.getWindow();
            if (dialogWindow != null){
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                dialogWindow.setGravity(Gravity.CENTER);
                lp.width = (int)(0.7 * point.x); // 宽度
                dialogWindow.setAttributes(lp);
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
            switch (view.getId()){
                case R.id.money:
                    if (".".equals(sz_button)) {
                        tmp_edit.setSelection(tmp_edit.getText().toString().indexOf(".") + 1);
                    }else {
                        if (index > tmp_edit.getText().toString().indexOf(".")){
                            if (index != tmp_edit.length())
                                tmp_edit.getText().delete(index,index + 1).insert(index,sz_button);
                        }else {
                            if(index == 0 && "0".equals(sz_button) )return;
                            tmp_edit.getText().insert(index, sz_button);
                        }
                    }
                    break;
            }

        }
    };

}
